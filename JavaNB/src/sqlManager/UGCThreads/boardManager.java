/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sqlManager.UGCThreads;

import com.mysql.jdbc.PreparedStatement;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.*;
import javanb.customQueue;
import javanb.userpackage.userException;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 * @author srivid
 */
public class boardManager extends threadManager {

    private int maxCommentsCount;

    /**
     * this class is necessary to build a structured output for a board which is an n-ary tree
     * root is board details and children being comments (first/second level comments being in first/second levels of tree)
     */
    private class tree {

        private Object data;
        private Hashtable<String, tree> childrenTable;

        public tree(Object data) {
            this.data = data;
            childrenTable = new Hashtable<String, tree>();
        }
        
        public synchronized void deleteChildren() {
            this.childrenTable = null;
        }
        
        public synchronized void deleteChidlren(String key) {
            if(this.childrenTable.contains(key)) {
                this.childrenTable.remove(key);
            }
        }

        public synchronized void addChildren(Hashtable children) {
            this.childrenTable = new Hashtable<String, tree>();
            Enumeration<String> keys = children.keys();
            while (keys.hasMoreElements()) {
                String key = keys.nextElement();
                this.childrenTable.put(key, (tree) children.get(key));
            }
        }

        public synchronized void addChildren(tree child, String key) {
            this.childrenTable.put(key, child);
        }

        public tree getChild(String key) {
            if (this.childrenTable.containsKey(key)) {
                return this.childrenTable.get(key);
            }
            return null;
        }

        public Object getData() {
            return data;
        }

        public synchronized void setData(Object data) {
            this.data = data;
        }

        public Hashtable getChildrenHashTable() {
            return this.childrenTable;
        }

        /**
         * API to convert tree structure to JSONObject
         * @param node
         * @return JSONObject
         */
        public JSONObject toJSON(tree node) {
            JSONObject obj = (JSONObject) node.getData();
            if (node.getChildrenHashTable().size() > 0) {
                Hashtable hash = node.getChildrenHashTable();
                Enumeration keys = hash.keys();
                while (keys.hasMoreElements()) {
                    String key = (String) keys.nextElement();
                    JSONObject child = this.toJSON((tree) hash.get(key));
                    obj.put(key, child);
                }
            }
            return obj;
        }
    }
    private tree root = null;

    public boardManager() throws FileNotFoundException, IOException, SQLException {
        super();
        Properties prop = new Properties();
        FileInputStream fis;
        fis = new FileInputStream("/Users/srivid/myProjects/myWorld/repository/JavaNB/src/sqlManager/UGCThreads/threadConfig-properties.xml");
        prop.loadFromXML(fis);
        this.maxCommentsCount = Integer.parseInt(prop.getProperty("maxCommentsCount"));
    }

    /**
     * inserts a board row
     * @param boardDetails
     * @throws userException 
     */
    public void insertBoard(JSONObject boardDetails) throws userException {
        boardDetails.put("table", "board");
        int res = this.insertThread(boardDetails);
        if (res > 0) {
            try {
                //cache purging
                PreparedStatement stmt = (PreparedStatement) this.threadConnection.prepareStatement("select * from board where uuid=?");
                String uuid = boardDetails.getString("uuid");
                stmt.setString(1, uuid);
                this.invalidateCache(stmt);
            } catch (SQLException ex) {
                throw new userException("error occured during purging cache:" + ex.getMessage());
            }
        }
    }

    /**
     * delete a board. deletes in a reverse fashion where in it deletes all second level comments first, followed 
     * by first level comments and later the board itself. Hence fetches entire board first and then does the
     * delete operation
     * @param uuid
     * @param boardId
     * @throws userException
     * @throws Exception 
     */
    public void deleteBoard(String uuid, String boardId) throws userException, Exception {
        tree root = null;
        this.initComments(boardId, root);
        customQueue queue = new customQueue();
        if (!((JSONObject) root.getData()).getString("uuid").equals(uuid)) {
            throw new userException("this given user with uuid:" + uuid + " has no permission to delete this board:" + boardId);
        }
        if (root != null) {
            queue.enqueue(root);
            Stack stack = new Stack();
            while (true) {
                tree temp = (tree) queue.dequeue();
                Hashtable hash = temp.getChildrenHashTable();
                Enumeration keys = hash.keys();
                while (keys.hasMoreElements()) {
                    String key = (String) keys.nextElement();
                    queue.enqueue(hash.get(key));
                }
                stack.push(temp);
                if (queue.isEmpty()) {
                    break;
                }
            }
            //TODO: wrap same level threadId in runnable/callable
            int prevLevel = 2;
            ArrayList<Callable<Void>> list = new ArrayList<Callable<Void>>();
            while (!stack.empty()) {
                final JSONObject thread = (JSONObject) stack.pop();
                Callable<Void> deleteThreadCallable = new Callable<Void>() {

                    @Override
                    public Void call() throws userException {
                        if (thread.containsKey("boardId")) {
                            boardManager.this.deleteThread("board", thread.getString("boardId"));
                        } else if (thread.containsKey("commentId")) {
                            boardManager.this.deleteThread("comment", thread.getString("commentId"));
                        }
                        return null;
                    }
                };
                int level = thread.getInt("level");
                if (level != prevLevel) {
                    prevLevel = level;
                    if(list.size() > 0) {
                        this.executorService.invokeAll(list);
                        list = new ArrayList<Callable<Void>>();
                    }
                } else {
                    list.add(deleteThreadCallable);
                }
            }
            PreparedStatement stmts[] = new PreparedStatement[2];
            stmts[0] = (PreparedStatement) this.threadConnection.prepareStatement("select * from board where uuid=?");
            stmts[0].setString(1, uuid);
            stmts[1] = (PreparedStatement) this.threadConnection.prepareStatement("select * from ratings where uuid=?");
            stmts[1].setString(1, uuid);
            this.invalidateCache(stmts);
        } else {
            throw new userException("some error occured in constructing tree of board and comments");
        }
    }

    /**
     * gets board details for set of boardIds. This contains just the board without comments
     * @param boardIds
     * @return JSONArray of details
     * @throws userException 
     */
    public JSONArray getBoard(ArrayList<String> boardIds) throws userException {
        try {
            String query = "select * from board where boardId in ({})";
            JSONArray results = new JSONArray();
            int boardIdLen = boardIds.size();
            String replacement = "";
            if (boardIdLen > 0) {
                int index = 0;
                while (index < boardIdLen) {
                    replacement += "?,";
                    index++;
                }
                replacement = replacement.substring(0, replacement.length() - 1); //strip last comma
                query.replaceAll("{}", replacement);
                PreparedStatement stmt = (PreparedStatement) this.threadConnection.prepareStatement(query);
                index = 0;
                while (index < boardIdLen) {
                    stmt.setString(index, boardIds.get(index));
                    index++;
                }
                ResultSet rs = this.executeQuery(stmt);
                while (rs.next()) {
                    JSONObject result = new JSONObject();
                    result.put("boardId", rs.getString("boardId"));
                    result.put("title", rs.getString("title"));
                    result.put("description", rs.getString("description"));
                    result.put("uuid", rs.getString("uuid"));
                    result.put("content", rs.getString("content"));
                    result.put("abuseFlag", rs.getString("abuseFlag"));
                    result.put("tags", rs.getString("tags"));
                    result.put("type", rs.getString("type"));
                    result.put("timestamp", rs.getLong("timestamp"));
                    result.put("shareCount", rs.getLong("shareCount"));
                    result.put("upRatingsCount", rs.getLong("upRatingsCount"));
                    result.put("downRatingsCount", rs.getLong("downRatingsCount"));
                    result.put("commentsCount", rs.getLong("commentsCount"));
                    results.add(result);
                }
            }
            return results;
        } catch (SQLException ex) {
            throw new userException("error occured while fetching boards for multiple boardIds:" + ex.getMessage());
        }
    }

    /**
     * gets board details for a boardId. This contains just the board without comments
     * @param boardId
     * @return JSONObject of board details
     * @throws userException 
     */
    public JSONObject getBoard(String boardId) throws userException {
        try {
            String query = "select * from board where boardId=?";
            PreparedStatement stmt = (PreparedStatement) this.threadConnection.prepareStatement(query);
            stmt.setString(1, boardId);
            ResultSet rs = this.executeQuery(stmt);
            JSONObject result = new JSONObject();
            while (rs.next()) {
                result.put("boardId", rs.getString("boardId"));
                result.put("title", rs.getString("title"));
                result.put("description", rs.getString("description"));
                result.put("uuid", rs.getString("uuid"));
                result.put("content", rs.getString("content"));
                result.put("abuseFlag", rs.getString("abuseFlag"));
                result.put("tags", rs.getString("tags"));
                result.put("type", rs.getString("type"));
                result.put("timestamp", rs.getLong("timestamp"));
                result.put("shareCount", rs.getLong("shareCount"));
                result.put("upRatingsCount", rs.getLong("upRatingsCount"));
                result.put("downRatingsCount", rs.getLong("downRatingsCount"));
                result.put("commentsCount", rs.getLong("commentsCount"));
            }
            return result;
        } catch (SQLException ex) {
            throw new userException("error occured while fetching board details");
        }
    }

    /**
     * retrieves first and second level comments of the board
     * @param boardId
     * @param start comments offset
     * @param count num comments
     * @return JSONObject represeting n-ary tree
     * @throws userException 
     */
    public JSONObject getComments(String boardId, int start, int count) throws userException {
        //TODO: cant pass null reference and expect function to populate it correctly
        tree root = null;
        this.initComments(boardId, start, count, root);
        return (root != null) ? root.toJSON(root) : null;
    }

    /**
     * inits the n-ary tree of a board
     * @param boardId
     * @param target target node where the resulting tree's reference is stored
     * @throws userException 
     */
    private void initComments(final String boardId, tree target) throws userException {
        try {
            JSONObject root = new JSONObject();
            root.put("level", 0);
            final tree rootNode = new tree(root);
            target = rootNode;

            Callable<Void> rootCallable = new Callable<Void>() {

                @Override
                public Void call() {
                    try {
                        JSONObject result = boardManager.this.getBoard(boardId);
                        result.put("level", 0);
                        rootNode.setData(result);
                    } catch (userException ex) {
                        System.err.println("exception occured in the thread while fetching board details:" + ex.getMessage());
                    }
                    return null;
                }
            };

            Callable<Void> firstCommentsCallable = new Callable<Void>() {

                @Override
                public Void call() {
                    try {
                        JSONArray firstComments = boardManager.this.getCommentsForThread(boardId);
                        int totalComments = firstComments.size();
                        for (int i = 0; i < totalComments; i++) {
                            JSONObject firstComment = firstComments.getJSONObject(i);
                            firstComment.put("level", 1);
                            tree temp = new tree(firstComment);
                            rootNode.addChildren(temp, firstComment.getString("commentId"));
                        }
                    } catch (userException ex) {
                        System.err.println("exception occured in the thread while fetching board details:" + ex.getMessage());
                    }
                    return null;
                }
            };

            ArrayList<Callable<Void>> list = new ArrayList<Callable<Void>>();
            list.add(rootCallable);
            list.add(firstCommentsCallable);
            this.executorService.invokeAll(list);
            //do nothing with the returned result since it just returns null always

            Enumeration<String> commentKeys = rootNode.childrenTable.keys();
            ArrayList<Future<JSONArray>> futureList = new ArrayList<Future<JSONArray>>();
            ArrayList<Callable<JSONArray>> callableList = new ArrayList<Callable<JSONArray>>();
            while (commentKeys.hasMoreElements()) {
                String key = commentKeys.nextElement();
                JSONObject data = (JSONObject) ((rootNode.getChild(key)).data);
                final String threadId = data.getString("commentId");
                Callable myThread = new Callable() {

                    @Override
                    public Object call() throws userException {
                        return boardManager.this.getCommentsForThread(threadId);
                    }
                };
                callableList.add(myThread);
            }
            futureList = (ArrayList<Future<JSONArray>>) this.executorService.invokeAll(callableList);
            for (Future future : futureList) {
                try {
                    JSONArray comments = (JSONArray) future.get();
                    if (comments.size() > 0) {
                        String parentThread = comments.getJSONObject(0).getString("parentId");
                        tree child = rootNode.getChild(parentThread);
                        if (child != null) {
                            for (Object comment : comments) {
                                ((JSONObject) comment).put("level", 2);
                                tree newChild = new tree(comment);
                                child.addChildren(newChild, ((JSONObject) comment).getString("commentId"));
                            }
                        }
                    }
                } catch (InterruptedException ex) {
                    throw new userException("error occured while fetching comment details:" + ex.getMessage());
                } catch (ExecutionException ex) {
                    throw new userException("error occured while fetching comment details:" + ex.getMessage());
                }
            }
        } catch (InterruptedException ex) {
            throw new userException("error occured while fetching comment details:" + ex.getMessage());
        }
    }

    /**
     * inits the n-ary tree of board with comments starting from start upto count
     * @param boardId
     * @param start
     * @param count
     * @param target
     * @throws userException 
     */
    public void initComments(final String boardId, final int start, final int count, tree target) throws userException {
        try {
            JSONObject root = new JSONObject();
            root.put("level", 0);
            final int commentsCount = Math.min(count, this.maxCommentsCount);
            final tree rootNode = new tree(root);
            target = rootNode;

            Callable<Void> rootCallable = new Callable<Void>() {

                @Override
                public Void call() {
                    try {
                        JSONObject result = boardManager.this.getBoard(boardId);
                        result.put("level", 0);
                        rootNode.setData(result);
                    } catch (userException ex) {
                        System.err.println("exception occured in the thread while fetching board details:" + ex.getMessage());
                    }
                    return null;
                }
            };

            Callable<Void> firstCommentsCallable = new Callable<Void>() {

                @Override
                public Void call() {
                    try {
                        JSONArray firstComments = boardManager.this.getCommentsForThread(boardId);
                        int totalComments = firstComments.size();
                        for (int i = 0; i < totalComments; i++) {
                            JSONObject firstComment = firstComments.getJSONObject(i);
                            firstComment.put("level", 1);
                            tree temp = new tree(firstComment);
                            rootNode.addChildren(temp, firstComment.getString("commentId"));
                        }
                    } catch (userException ex) {
                        System.err.println("exception occured in the thread while fetching board details:" + ex.getMessage());
                    }
                    return null;
                }
            };

            ArrayList<Callable<Void>> list = new ArrayList<Callable<Void>>();
            list.add(rootCallable);
            list.add(firstCommentsCallable);
            this.executorService.invokeAll(list);
            //do nothing with the returned result since it just returns null always

            Enumeration<String> commentKeys = rootNode.childrenTable.keys();
            ArrayList<Future<JSONArray>> futureList = new ArrayList<Future<JSONArray>>();
            ArrayList<Callable<JSONArray>> callableList = new ArrayList<Callable<JSONArray>>();
            while (commentKeys.hasMoreElements()) {
                String key = commentKeys.nextElement();
                JSONObject data = (JSONObject) ((rootNode.getChild(key)).data);
                final String threadId = data.getString("commentId");
                Callable myThread = new Callable() {

                    @Override
                    public Object call() throws userException {
                        return boardManager.this.getCommentsForThread(threadId);
                    }
                };
                callableList.add(myThread);
            }
            futureList = (ArrayList<Future<JSONArray>>) this.executorService.invokeAll(callableList);
            for (Future future : futureList) {
                try {
                    JSONArray comments = (JSONArray) future.get();
                    if (comments.size() > 0) {
                        String parentThread = comments.getJSONObject(0).getString("parentId");
                        tree child = rootNode.getChild(parentThread);
                        if (child != null) {
                            int counter = 0;
                            for (Object comment : comments) {
                                ((JSONObject) comment).put("level", 2);
                                tree newChild = new tree(comment);
                                child.addChildren(newChild, ((JSONObject) comment).getString("commentId"));
                                counter++;
                                if (counter == this.maxCommentsCount) {
                                    break;
                                }
                            }
                        }
                    }
                } catch (InterruptedException ex) {
                    throw new userException("error occured while fetching comment details:" + ex.getMessage());
                } catch (ExecutionException ex) {
                    throw new userException("error occured while fetching comment details:" + ex.getMessage());
                }
            }
        } catch (InterruptedException ex) {
            throw new userException("error occured while fetching comment details:" + ex.getMessage());
        }
    }

    /**
     * gets all boards posted by a user (without comments)
     * @param uuid
     * @return
     * @throws userException 
     */
    public JSONArray getBoardsOfEntity(String uuid) throws userException {
        try {
            JSONArray results = new JSONArray();
            String boardQuery = "select * from board where uuid=?";
            String ratingsQuery = "select * from ratings where uuid=?";
            final PreparedStatement boardStmt = (PreparedStatement) this.threadConnection.prepareStatement(boardQuery);
            final PreparedStatement ratingsStmt = (PreparedStatement) this.threadConnection.prepareStatement(ratingsQuery);
            boardStmt.setString(1, uuid);
            ratingsStmt.setString(1, uuid);
            ArrayList<PreparedStatement> stmts = new ArrayList<PreparedStatement>();
            stmts.add(boardStmt);
            stmts.add(ratingsStmt);
            Hashtable comboResult = this.executeMultipleQueries(stmts);

            ResultSet boardRs = (ResultSet) comboResult.get(boardStmt.toString());
            ResultSet ratingsRs = (ResultSet) comboResult.get(ratingsStmt.toString());

            Hashtable<String, Integer> boardHash = new Hashtable<String, Integer>();
            ArrayList<String> boardIds = new ArrayList<String>();
            while (boardRs.next()) {
                JSONObject result = new JSONObject();
                boardHash.put(boardRs.getString("boardId"), 1);
                result.put("boardId", boardRs.getString("boardId"));
                result.put("title", boardRs.getString("title"));
                result.put("description", boardRs.getString("description"));
                result.put("uuid", boardRs.getString("uuid"));
                result.put("content", boardRs.getString("content"));
                result.put("abuseFlag", boardRs.getString("abuseFlag"));
                result.put("tags", boardRs.getString("tags"));
                result.put("type", boardRs.getString("type"));
                result.put("timestamp", boardRs.getLong("timestamp"));
                result.put("shareCount", boardRs.getLong("shareCount"));
                result.put("upRatingsCount", boardRs.getLong("upRatingsCount"));
                result.put("downRatingsCount", boardRs.getLong("downRatingsCount"));
                result.put("commentsCount", boardRs.getLong("commentsCount"));
                results.add(result);
            }

            while (ratingsRs.next()) {
                String threadId = ratingsRs.getString("threadId");
                String likeRating = ratingsRs.getString("likeRating");
                String share = ratingsRs.getString("share");
                if (threadId.startsWith("board_") && !boardHash.containsKey(threadId) && (likeRating.equals("1") || share.equals("1"))) {
                    boardIds.add(threadId);
                }
            }

            JSONArray ratingsResult = this.getBoard(boardIds);
            results.addAll(ratingsResult);
            return results;
        } catch (SQLException ex) {
            throw new userException("error occured while fetching boards:" + ex.getMessage());
        }
    }
    
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sqlManager.UGCThreads;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.PreparedStatement;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import javanb.userpackage.userException;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import sqlManager.sqlUtils;

/**
 *
 * @author srivid
 */
public class threadManager extends sqlUtils {

    Connection threadConnection = null;

    public threadManager() throws FileNotFoundException, IOException, SQLException {
        super();
        this.threadConnection = this.getConnection();
    }

    /**
     * API to insert a thread into comment/board
     *
     * @param threadDetails - details to be inserted
     * @return number of rows that were inserted
     * @throws userException
     */
    public int insertThread(JSONObject threadDetails) throws userException {
        try {
            if (!threadDetails.containsKey("table")) {
                throw new userException("Table is mentioned");
            } else {
                String table = threadDetails.getString("table");
                String query = "";
                PreparedStatement stmt = null;
                //creating a new board
                if ("board".equals(table)) {
                    query = "insert into board (boardId,shareCount,upRatingsCount,downRatingsCount,"
                            + "title,description,timestamp,uuid,abuseFlag,tags,type,content,targetUuid) values(?,?,?,?,?,?,?,?,?,?,?,?,?)";
                    stmt = (PreparedStatement) this.threadConnection.prepareStatement(query);
                    stmt.setString(11, threadDetails.getString("type"));
                    stmt.setString(12, threadDetails.getString("content"));
                    stmt.setString(13, threadDetails.getString("targetUuid"));
                } else if ("comment".equals(table)) {
                    //creating a new comment under a comment/board
                    query = "insert into comment (commentId,shareCount,upRatingsCount,downRatingsCount,"
                            + "title,description,timestamp,uuid,abuseFlag,tags,parentId) values(?,?,?,?,?,?,?,?,?,?,?)";
                    stmt = (PreparedStatement) this.threadConnection.prepareStatement(query);
                    stmt.setString(11, threadDetails.getString("parentId"));
                } else {
                    throw new userException("Table is invalid. It needs to be either board or comment");
                }
                stmt.setString(1, threadDetails.getString("threadId"));
                stmt.setLong(2, threadDetails.containsKey("shareCount") ? threadDetails.getLong("shareCount") : 0L);
                stmt.setLong(3, threadDetails.containsKey("upRatingsCount") ? threadDetails.getLong("upRatingsCount") : 0L);
                stmt.setLong(4, threadDetails.containsKey("downRatingsCount") ? threadDetails.getLong("downRatingsCount") : 0L);
                stmt.setString(5, threadDetails.containsKey("title") ? threadDetails.getString("title") : "");
                stmt.setString(6, threadDetails.containsKey("description") ? threadDetails.getString("description") : "");
                stmt.setLong(7, threadDetails.getLong("timestamp"));
                stmt.setString(8, threadDetails.getString("uuid"));
                stmt.setString(9, threadDetails.containsKey("abuseFlag") ? threadDetails.getString("abuseFlag") : "0");
                stmt.setString(10, threadDetails.getString("tags"));
                return stmt.executeUpdate();
            }
        } catch (SQLException ex) {
            throw new userException("error occured while inserting thread:" + ex.getMessage());
        }
    }

    /**
     * API to update/insert ratings into ratings table. Ratings can be for any
     * thread (comment/board) It does an insert first and if the uuid & threadId
     * combo is already present, it results in SQLException as a result of which
     * API does an update
     *
     * @param threadId
     * @param uuid
     * @param parameter field name
     * @param value field value
     * @return number of rows updated (ideally 1)
     * @throws userException
     */
    public int updateThreadInRatings(String threadId, String uuid, String parameter, String value) throws userException {
        try {
            String query = "insert into ratings (threadId,uuid,?) values (?,?,?)";
            PreparedStatement stmt = (PreparedStatement) this.threadConnection.prepareStatement(query);
            stmt.setString(1, parameter);
            stmt.setString(2, threadId);
            stmt.setString(3, uuid);
            stmt.setString(4, value);
            try {
                stmt.executeUpdate();
            } catch (SQLException sqle) {
                //there already exists a row with this uuid and threadId combo, hence do an update
                query = "update ratings set ?=? where threadId=? and uuid=?";
                stmt = (PreparedStatement) this.threadConnection.prepareStatement(query);
                stmt.setString(1, parameter);
                stmt.setString(2, value);
                stmt.setString(3, threadId);
                stmt.setString(4, uuid);
                try {
                    stmt.executeUpdate();
                } catch (SQLException ex) {
                    throw new userException("unable to update ratings table:" + ex.getMessage());
                }
            }
            //purge cache
            PreparedStatement stmts[] = new PreparedStatement[1];
            stmts[0] = (PreparedStatement) this.threadConnection.prepareStatement("select * from ratings where uuid=?");
            stmts[0].setString(1, uuid);
            this.invalidateCache(stmts);
            return stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new userException("error occured while sharing the thread:" + ex.getMessage());
        }
    }

    /**
     * @param table - comment/board for now
     * @param threadId - id of the board/comment
     * @return true/false depending on whether the operation was successful
     * @throws userException
     */
    public void deleteThread(String table, String threadId) throws userException {
        try {
            String query = "", ratingsQuery = "delete from ratings where threadId=?";
            final PreparedStatement ratingsStmt = (PreparedStatement) this.threadConnection.prepareStatement(ratingsQuery);
            ratingsStmt.setString(1, threadId);
            if ("board".equals(table)) {
                query = "delete from board where boardId=?";
            } else if ("comment".equals(table)) {
                query = "delete from comment where commentId=?";
            } else {
                throw new userException("delete operation is not applicable for this table:" + table);
            }

            final PreparedStatement stmt = (PreparedStatement) this.threadConnection.prepareStatement(query);
            stmt.setString(1, threadId);

            Callable deleteFromThread = new Callable() {

                @Override
                public Object call() throws SQLException {
                    return stmt.executeUpdate();
                }
            };

            Callable deleteFromRatings = new Callable() {

                @Override
                public Object call() throws SQLException {
                    return ratingsStmt.executeUpdate();
                }
            };

            ArrayList<Callable<Object>> list = new ArrayList<Callable<Object>>();
            list.add(deleteFromThread);
            list.add(deleteFromRatings);

            ArrayList<Future<Object>> futureList = new ArrayList<Future<Object>>();
            futureList = (ArrayList<Future<Object>>) this.executorService.invokeAll(list);

            //logic for purging cache
            PreparedStatement stmts[] = new PreparedStatement[2];
            stmts[0] = (PreparedStatement) this.threadConnection.prepareStatement("select * from ratings where threadId=?");
            stmts[0].setString(1, threadId);
            if ("board".equals(table)) {
                stmts[1] = (PreparedStatement) this.threadConnection.prepareStatement("select * from board where boardId=?");
            } else if ("comment".equals(table)) {
                stmts[1] = (PreparedStatement) this.threadConnection.prepareStatement("select * from comment where commentId=?");
            }
            stmts[1].setString(1, threadId);
            this.invalidateCache(stmts);
        } catch (InterruptedException ex) {
            throw new userException("error occured while deleting thread:" + ex.getMessage());
        } catch (SQLException ex) {
            throw new userException("error occured while deleting thread:" + ex.getMessage());
        }
    }

    /**
     * API to fetch all comments under a thread. It only fetches the first level
     * comments under a thread
     *
     * @param threadId
     * @return JSONArray of commentDetails
     * @throws userException
     */
    public JSONArray getCommentsForThread(String threadId) throws userException {
        try {
            String query = "select * from comment where parentId=?";
            PreparedStatement stmt = (PreparedStatement) this.threadConnection.prepareStatement(query);
            stmt.setString(1, threadId);
            ResultSet rs = this.executeQuery(stmt);
            JSONArray results = new JSONArray();
            while (rs.next()) {
                JSONObject result = new JSONObject();
                result.put("commentId", rs.getString("commentId"));
                result.put("title", rs.getString("title"));
                result.put("description", rs.getString("description"));
                result.put("uuid", rs.getString("uuid"));
                result.put("parentId", rs.getString("parentId"));
                result.put("tags", rs.getString("tags"));
                result.put("abuseFlag", rs.getString("abuseFlag"));
                result.put("shareCount", rs.getLong("shareCount"));
                result.put("upRatingsCount", rs.getLong("upRatingsCount"));
                result.put("downRatingsCount", rs.getLong("downRatingsCount"));
                result.put("timestamp", rs.getLong("timestamp"));
                results.add(result);
            }
            return results;
        } catch (SQLException ex) {
            throw new userException("error occured while fetching comments for thread:" + threadId + "\n" + ex.getMessage());
        }

    }

    public int likeThread(String threadId, String table, String key, boolean undo) throws userException {
        try {
            String query = "update " + table + " set upRatingsCount=upRatingsCount+1 where " + key + "=?";
            if (undo) {
                query = "update " + table + " set upRatingsCount=upRatingsCount-1 where " + key + "=?";
            }
            PreparedStatement stmt = (PreparedStatement) this.threadConnection.prepareStatement(query);
            stmt.setString(1, threadId);
            int res = stmt.executeUpdate();
            if (res > 0) {
                this.invalidateThread(threadId, table, key);
            }
            return res;
        } catch (SQLException ex) {
            throw new userException("error occured while liking thread:" + ex.getMessage());
        }
    }

    public int dislikeThread(String threadId, String table, String key, boolean undo) throws userException {
        try {
            String query = "update " + table + " set downRatingsCount=downRatingsCount+1 where " + key + "=?";
            if (undo) {
                query = "update " + table + " set downRatingsCount=downRatingsCount-1 where " + key + "=?";
            }
            PreparedStatement stmt = (PreparedStatement) this.threadConnection.prepareStatement(query);
            stmt.setString(1, threadId);
            int res = stmt.executeUpdate();
            if (res > 0) {
                this.invalidateThread(threadId, table, key);
            }
            return res;
        } catch (SQLException ex) {
            throw new userException("error occured while liking thread:" + ex.getMessage());
        }
    }

    public int shareThread(String threadId, String table, String key, boolean undo) throws userException {
        try {
            String query = "update " + table + " set shareCount=shareCount+1 where " + key + "=?";
            if (undo) {
                query = "update " + table + " set shareCount=shareCount-1 where " + key + "=?";
            }
            PreparedStatement stmt = (PreparedStatement) this.threadConnection.prepareStatement(query);
            stmt.setString(1, threadId);
            int res = stmt.executeUpdate();
            if (res > 0) {
                this.invalidateThread(threadId, table, key);
            }
            return res;
        } catch (SQLException ex) {
            throw new userException("error occured while liking thread:" + ex.getMessage());
        }
    }

    public int abuseThread(String threadId, String table, String key, boolean undo) throws userException {
        try {
            String query = "update " + table + " set abuseFlag='1' where " + key + "=?";
            if (undo) {
                query = "update " + table + " set abuseFlag='0' where " + key + "=?";
            }
            PreparedStatement stmt = (PreparedStatement) this.threadConnection.prepareStatement(query);
            stmt.setString(1, threadId);
            int res = stmt.executeUpdate();
            if (res > 0) {
                this.invalidateThread(threadId, table, key);
            }
            return res;
        } catch (SQLException ex) {
            throw new userException("error occured while liking thread:" + ex.getMessage());
        }
    }

    private void invalidateThread(String threadId, String table, String key) throws userException {
        try {
            PreparedStatement cachedStmt = (PreparedStatement) this.threadConnection.prepareStatement("select * from " + table + " where " + key + "=?");
            cachedStmt.setString(1, threadId);
            this.invalidateCache(cachedStmt);
        } catch (SQLException ex) {
            throw new userException("error while purging cache:" + ex.getMessage());
        }
    }

    public void updateThread(String threadId, String table, String primaryKey, JSONObject obj) throws userException {
        try {
            String query = "update " + table + " set {} where " + primaryKey + "=?";
            String replacements = "";
            Iterator it = obj.keys();
            while (it.hasNext()) {
                String key = (String) it.next();
                replacements += key + "=?,";
            }
            int length = replacements.length();
            if (length > 0) {
                replacements = replacements.substring(0, length - 1);
            }
            query = query.replaceAll("{}", replacements);
            PreparedStatement stmt = (PreparedStatement) this.threadConnection.prepareStatement(query);
            it = obj.keys();
            int index = 0;
            while (it.hasNext()) {
                String key = (String) it.next();
                stmt.setString(index++, obj.getString(key));
            }
            stmt.setString(index++, threadId);
            stmt.executeUpdate();
            this.invalidateThread(threadId, table, primaryKey);
        } catch (SQLException ex) {
            throw new userException("error occured while updating thread of user:" + ex.getMessage());
        }
    }
}

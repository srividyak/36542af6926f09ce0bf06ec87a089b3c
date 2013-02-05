/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package UGCThreads;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Hashtable;
import javanb.userpackage.userException;
import net.sf.json.JSONObject;
import sqlManager.UGCThreads.boardManager;

/**
 *
 * @author srivid
 */
public class board extends thread {

    private String type;
    private Hashtable typeMap;
    private String content;
    private String boardId;
    private String targetUuid; //indicates where the user wants to post. This can be equal to creatorUuid/other's uuid/pageId/groupId etc

    public String getBoardId() {
        return boardId;
    }

    public void setBoardId(String boardId) {
        this.boardId = boardId;
    }

    /**
     * generates a hashtable for type of board
     */
    public board() {
        typeMap = new Hashtable();
        typeMap.put("text", "0");
        typeMap.put("image", "1");
        typeMap.put("video", "2");
        typeMap.put("link", "3");
    }

    public board(String id) {
        super(id);
    }

    /**
     * API to fetch a board given boardId
     *
     * @param boardId
     * @return
     * @throws userException
     */
    public JSONObject fetchBoard(String boardId) throws userException {
        try {
            boardManager sqlManager = new boardManager();
            JSONObject result = sqlManager.getBoard(boardId);
            this.initBoard(result);
            return result;
        } catch (FileNotFoundException ex) {
            throw new userException("error occured while fetching board:" + ex.getMessage());
        } catch (IOException ex) {
            throw new userException("error occured while fetching board:" + ex.getMessage());
        } catch (SQLException ex) {
            throw new userException("error occured while fetching board:" + ex.getMessage());
        }
    }

    public void setType(String type) {
        this.type = type;
    }

    /**
     * API to insert a board
     *
     * @param boardDetails
     * @throws userException
     */
    public void insertBoard(JSONObject boardDetails) throws userException {
        try {
            this.initBoard(boardDetails);
            boardDetails.put("threadId", this.threadId);
            boardDetails.put("timestamp", this.timestamp);
            boardDetails.put("targetUuid", this.getTargetUuid());
            String tags = "";
            if (this.tags != null) {
                for (String tag : this.tags) {
                    tags += tag + ",";
                }
            }
            boardDetails.put("tags", (this.tags == null) ? "" : tags);
            boardDetails.put("type", this.typeMap.get(type));
            boardManager sqlManager = new boardManager();
            sqlManager.insertBoard(boardDetails);
        } catch (FileNotFoundException ex) {
            throw new userException("error occured while fetching board:" + ex.getMessage());
        } catch (IOException ex) {
            throw new userException("error occured while fetching board:" + ex.getMessage());
        } catch (SQLException ex) {
            throw new userException("error occured while fetching board:" + ex.getMessage());
        }
    }

    /**
     * API to init all local class params from boardDetails
     *
     * @param boardDetails
     * @throws userException
     */
    public void initBoard(JSONObject boardDetails) throws userException {
        this.initThread(boardDetails);
        if (boardDetails.containsKey("type")) {
            this.setType(boardDetails.getString("type"));
        } else {
            throw new userException("type of board is mandatory");
        }
        if (boardDetails.containsKey("content")) {
            this.setContent(boardDetails.getString("content"));
        } else {
            this.setContent("");
        }
        if (boardDetails.containsKey("targetUuid")) {
            this.setTargetUuid(boardDetails.getString("targetUuid"));
        } else {
            this.setTargetUuid(this.creatorUuid);
        }
        if (boardDetails.containsKey("boardId")) {
            this.setThreadId(boardDetails.getString("boardId"));
        } else {
            this.setThreadId(this.generateThreadId("board_"));
        }
        this.setBoardId(this.threadId);
    }

    public String getTargetUuid() {
        return targetUuid;
    }

    public void setTargetUuid(String targetUuid) {
        this.targetUuid = targetUuid;
    }

    /**
     * API to init the board id
     *
     * @return boardId
     */
    public String generateBoardId() {
        return this.generateThreadId("board_");
    }

    public void setContent(String string) {
        this.content = string;
    }

    public String getContent() {
        return content;
    }

    public String getType() {
        return type;
    }

    /**
     * gets comments for a board (JSONObject notation of n-ary tree)
     *
     * @param start offset for comments
     * @param count num of comments
     * @return
     * @throws userException
     */
    public JSONObject getComments(int start, int count) throws userException {
        try {
            return new boardManager().getComments(this.threadId, start, count);
        } catch (FileNotFoundException ex) {
            throw new userException("could not fetch comments for this id:" + ex.getMessage());
        } catch (IOException ex) {
            throw new userException("could not fetch comments for this id:" + ex.getMessage());
        } catch (SQLException ex) {
            throw new userException("could not fetch comments for this id:" + ex.getMessage());
        }
    }

    /**
     * gets comments for a board (JSONObject notation of n-ary tree). offset is
     * 0 and count being 10
     *
     * @return
     * @throws userException
     */
    public JSONObject getComments() throws userException {
        try {
            return new boardManager().getComments(this.threadId, 0, 10); //to be read from config ideally
        } catch (FileNotFoundException ex) {
            throw new userException("could not fetch comments for this id:" + ex.getMessage());
        } catch (IOException ex) {
            throw new userException("could not fetch comments for this id:" + ex.getMessage());
        } catch (SQLException ex) {
            throw new userException("could not fetch comments for this id:" + ex.getMessage());
        }
    }

    public void updateBoard(JSONObject obj) throws userException {
        try {
            boardManager sqlManager = new boardManager();
            if (obj.containsKey("like")) {
                if (obj.getString("like").equals("1")) {
                    if (sqlManager.updateThreadInRatings(boardId, obj.getString("uuid"), "likeRating", "1") == 1) {
                        sqlManager.likeThread(boardId, "board", "boardId", true);
                    }
                } else {
                    if (sqlManager.updateThreadInRatings(boardId, obj.getString("uuid"), "likeRating", "0") == 1) {
                        sqlManager.likeThread(boardId, "board", "boardId", false);
                    }
                }
            } else if (obj.containsKey("dislike")) {
                if (obj.getString("dislike").equals("1")) {
                    if (sqlManager.updateThreadInRatings(boardId, obj.getString("uuid"), "likeRating", "2") == 1) {
                        sqlManager.dislikeThread(boardId, "board", "boardId", true);
                    }
                } else {
                    if (sqlManager.updateThreadInRatings(boardId, obj.getString("uuid"), "likeRating", "0") == 1) {
                        sqlManager.dislikeThread(boardId, "board", "boardId", false);
                    }
                }
            } else if (obj.containsKey("share")) {
                if (obj.getString("share").equals("1")) {
                    if (sqlManager.updateThreadInRatings(boardId, obj.getString("uuid"), "share", "1") == 1) {
                        sqlManager.shareThread(boardId, "board", "boardId", true);
                    }
                } else {
                    if (sqlManager.updateThreadInRatings(boardId, obj.getString("uuid"), "share", "0") == 1) {
                        sqlManager.shareThread(boardId, "board", "boardId", false);
                    }
                }
            } else if (obj.containsKey("abuse")) {
                if (obj.getString("abuse").equals("1")) {
                    if (sqlManager.updateThreadInRatings(boardId, obj.getString("uuid"), "abuse", "1") == 1) {
                        sqlManager.abuseThread(boardId, "board", "boardId", true);
                    }
                } else {
                    if (sqlManager.updateThreadInRatings(boardId, obj.getString("uuid"), "abuse", "0") == 1) {
                        sqlManager.abuseThread(boardId, "board", "boardId", false);
                    }
                }
            } else {
                sqlManager.updateThread(boardId, "board", "boardId", obj);
            }
        } catch (FileNotFoundException ex) {
            throw new userException("error occured while updating comments:" + ex.getMessage());
        } catch (IOException ex) {
            throw new userException("error occured while updating comments:" + ex.getMessage());
        } catch (SQLException ex) {
            throw new userException("error occured while updating comments:" + ex.getMessage());
        }
    }
}

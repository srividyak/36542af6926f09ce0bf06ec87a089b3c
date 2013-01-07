/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package UGCThreads;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;
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
     * @param boardDetails
     * @throws userException 
     */
    public void insertBoard(JSONObject boardDetails) throws userException {
        try {
            this.threadId = this.generateThreadId("board_");
            this.initBoard(boardDetails);
            boardDetails.put("threadId", this.threadId);
            boardDetails.put("timestamp", this.timestamp);
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
     * @param boardDetails
     * @throws userException 
     */
    private void initBoard(JSONObject boardDetails) throws userException {
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
        this.setBoardId(this.threadId);
    }

    /**
     * API to init the board id
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
     * gets comments for a board (JSONObject notation of n-ary tree). offset is 0 and count being 10
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
}
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package entity;

import UGCThreads.board;
import UGCThreads.comments;
import UGCThreads.page;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javanb.userpackage.userException;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import sqlManager.UGCThreads.boardManager;
import sqlManager.UGCThreads.commentsManager;

/**
 *
 * @author srivid
 */
public class entity {
    protected String uuid;
    
    public void fetchEntity() throws userException {
        //does nothing. Left to the class which extends entity
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
    
    public void createBoard(JSONObject boardDetails) throws userException {
        board b = new board();
        boardDetails.put("uuid", this.uuid);
        b.insertBoard(boardDetails);
    }
    
    public void updateBoard(JSONObject boardDetails) throws userException {
        board b = new board();
        boardDetails.put("uuid", this.uuid);
        b.updateBoard(boardDetails);
    }
    
    public void createComment(JSONObject commentDetails) throws userException {
        comments c = new comments();
        commentDetails.put("uuid", this.uuid);
        c.insertComment(commentDetails);
    }
    
    public void updateComment(JSONObject commentDetails) throws userException {
        comments c = new comments();
        commentDetails.put("uuid", this.uuid);
        c.updateComment(commentDetails);
    }
    
    public void createPage(JSONObject pageDetails) throws userException {
        page p = new page();
        pageDetails.put("owner", this.uuid);
        p.insertPage(pageDetails);
    }
    
    public void updatePage(JSONObject pageDetails) {
        
    }
    
    public void createGroup(JSONObject groupDetails) {
        
    }
    
    public void updateGroup(JSONObject groupDetails) {
        
    }
    
    /**
     * API to fetch all boards posted by an entity (user/page/group)
     *
     * @return a linked list of boards where each element is an instance of
     * board class
     * @throws userException
     */
    public List<board> getAllBoards() throws userException {
        try {
            boardManager sqlManager = new boardManager();
            JSONArray allBoards = sqlManager.getBoardsOfEntity(uuid);
            List<board> boardsList = new LinkedList<board>();
            for (Iterator it = allBoards.iterator(); it.hasNext();) {
                JSONObject boardObj = (JSONObject) it.next();
                board b = new board();
                b.initBoard(boardObj);
                boardsList.add(b);
            }
            return boardsList;
        } catch (FileNotFoundException ex) {
            throw new userException("some exception occured while fetching boards" + ex.getMessage());
        } catch (IOException ex) {
            throw new userException("some exception occured while fetching boards" + ex.getMessage());
        } catch (SQLException ex) {
            throw new userException("some exception occured while fetching boards" + ex.getMessage());
        }
    }
    
    
    /**
     * API to fetch all comments posted by an entity (user/page/group)
     * @return
     * @throws userException 
     */
    public List<comments> getAllComments() throws userException {
        try {
            commentsManager sqlManager = new commentsManager();
            JSONArray allComments = sqlManager.getCommentsOfEntity(uuid);
            List<comments> commentsList = new LinkedList<comments>();
            for (Iterator it = allComments.iterator(); it.hasNext();) {
                JSONObject commentObj = (JSONObject) it.next();
                comments c = new comments();
                c.initComment(commentObj);
                commentsList.add(c);
            }
            return commentsList;
        } catch (FileNotFoundException ex) {
            throw new userException("some exception occured while fetching boards" + ex.getMessage());
        } catch (IOException ex) {
            throw new userException("some exception occured while fetching boards" + ex.getMessage());
        } catch (SQLException ex) {
            throw new userException("some exception occured while fetching boards" + ex.getMessage());
        }
    }
}

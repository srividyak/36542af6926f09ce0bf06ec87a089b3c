/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package UGCThreads;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javanb.userpackage.userException;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import sqlManager.UGCThreads.commentsManager;

/**
 *
 * @author srivid
 */
public class comments extends thread {

    private String parentId;
    private String commentId;

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public comments() {
    }

    public comments(String threadId) {
        super(threadId);
    }

    /**
     * API to post comment under board/comment
     * @param commentDetails
     * @throws userException 
     */
    public void insertComment(JSONObject commentDetails) throws userException {
        try {
            this.threadId = this.generateThreadId("comment_");
            this.initComment(commentDetails);
            commentDetails.put("threadId", this.threadId);
            commentDetails.put("timestamp", this.timestamp);
            String tags = "";
            if (this.tags != null) {
                for (String tag : this.tags) {
                    tags += tag + ",";
                }
            }
            commentDetails.put("tags", (this.tags == null) ? "" : tags);
            commentsManager sqlManager = new commentsManager();
            sqlManager.insertComment(commentDetails);
        } catch (FileNotFoundException ex) {
            throw new userException("error occured while inserting comments:" + ex.getMessage());
        } catch (IOException ex) {
            throw new userException("error occured while inserting comments:" + ex.getMessage());
        } catch (SQLException ex) {
            throw new userException("error occured while inserting comments:" + ex.getMessage());
        }
    }

    private void initComment(JSONObject commentDetails) throws userException {
        this.initThread(commentDetails);
        if (commentDetails.containsKey("parentId")) {
            this.setParentId(commentDetails.getString("parentId"));
        } else {
            throw new userException("parentId is mandatory");
        }
        this.setCommentId(this.threadId);
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    /**
     * API to get comments under a comment
     * @param start offset
     * @param count
     * @return
     * @throws userException 
     */
    public JSONArray getComments(int start, int count) throws userException {
        try {
            JSONArray result = new JSONArray();
            commentsManager sqlManager = new commentsManager();
            result = sqlManager.getComments(this.threadId, start, count);
            return result;
        } catch (FileNotFoundException ex) {
            throw new userException("error occured while fetching comments:" + ex.getMessage());
        } catch (IOException ex) {
            throw new userException("error occured while fetching comments:" + ex.getMessage());
        } catch (SQLException ex) {
            throw new userException("error occured while fetching comments:" + ex.getMessage());
        }
    }
    
    /**
     * API to fetch default num of comments from 0
     * @return
     * @throws userException 
     */
    public JSONArray getComments() throws userException {
        return this.getComments(0, 10);
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package UGCThreads;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
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
     *
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

    public void initComment(JSONObject commentDetails) throws userException {
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
     *
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
     *
     * @return
     * @throws userException
     */
    public JSONArray getComments() throws userException {
        return this.getComments(0, 10);
    }

    /**
     * API to update fields of comment (title/description/like/dislike etc)
     * @param uuid
     * @param obj
     * @throws userException
     */
    public void updateComment(JSONObject obj) throws userException {
        try {
            commentsManager sqlManager = new commentsManager();
            if (obj.containsKey("like")) {
                if (obj.getString("like").equals("1")) {
                    if (sqlManager.updateThreadInRatings(commentId, obj.getString("uuid"), "likeRating", "1") == 1) {
                        sqlManager.likeThread(commentId, "comment", "commentId", true);
                    }
                } else {
                    if (sqlManager.updateThreadInRatings(commentId, obj.getString("uuid"), "likeRating", "0") == 1) {
                        sqlManager.likeThread(commentId, "comment", "commentId", false);
                    }
                }
            } else if (obj.containsKey("dislike")) {
                if (obj.getString("dislike").equals("1")) {
                    if (sqlManager.updateThreadInRatings(commentId, obj.getString("uuid"), "likeRating", "2") == 1) {
                        sqlManager.dislikeThread(commentId, "comment", "commentId", true);
                    }
                } else {
                    if (sqlManager.updateThreadInRatings(commentId, obj.getString("uuid"), "likeRating", "0") == 1) {
                        sqlManager.dislikeThread(commentId, "comment", "commentId", false);
                    }
                }
            } else if (obj.containsKey("share")) {
                if (obj.getString("share").equals("1")) {
                    if (sqlManager.updateThreadInRatings(commentId, obj.getString("uuid"), "share", "1") == 1) {
                        sqlManager.shareThread(commentId, "comment", "commentId", true);
                    }
                } else {
                    if (sqlManager.updateThreadInRatings(commentId, obj.getString("uuid"), "share", "0") == 1) {
                        sqlManager.shareThread(commentId, "comment", "commentId", false);
                    }
                }
            } else if (obj.containsKey("abuse")) {
                if (obj.getString("abuse").equals("1")) {
                    if (sqlManager.updateThreadInRatings(commentId, obj.getString("uuid"), "abuse", "1") == 1) {
                        sqlManager.abuseThread(commentId, "comment", "commentId", true);
                    }
                } else {
                    if (sqlManager.updateThreadInRatings(commentId, obj.getString("uuid"), "abuse", "0") == 1) {
                        sqlManager.abuseThread(commentId, "comment", "commentId", false);
                    }
                }
            } else {
                sqlManager.updateThread(commentId, "comment", "commentId", obj);
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

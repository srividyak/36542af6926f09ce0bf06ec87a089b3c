/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sqlManager.UGCThreads;

import com.mysql.jdbc.PreparedStatement;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javanb.userpackage.userException;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 * @author srivid
 */
public class commentsManager extends threadManager {

    private int maxCommentsCount;

    /**
     * inits max number of comments that can be fetched at once
     * @throws FileNotFoundException
     * @throws IOException
     * @throws SQLException 
     */
    public commentsManager() throws FileNotFoundException, IOException, SQLException {
        super();
        Properties prop = new Properties();
        FileInputStream fis;
        fis = new FileInputStream("/Users/srivid/myProjects/myWorld/repository/JavaNB/src/sqlManager/UGCThreads/threadConfig-properties.xml");
        prop.loadFromXML(fis);
        this.maxCommentsCount = Integer.parseInt(prop.getProperty("maxCommentsCount"));
    }

    /**
     * API to insert a comment in comment table. Also purges the corresponding get query for the parentId in case 
     * insert is successful
     * @param commentDetails
     * @throws userException 
     */
    public void insertComment(JSONObject commentDetails) throws userException {
        commentDetails.put("table", "comment");
        int res = this.insertThread(commentDetails);
        if (res > 0) {
            try {
                String parentId = commentDetails.getString("parentId");
                String query = "select * from comment where parentId=?";
                PreparedStatement stmt = (PreparedStatement) this.threadConnection.prepareStatement(query);
                stmt.setString(1, parentId);
                this.invalidateCache(stmt);
            } catch (SQLException ex) {
                throw new userException("could not purge cache:" + ex.getMessage());
            }
        }
    }

    /**
     * API to fetch comments under a thread.
     * @param commentId parentId in comment table
     * @param start
     * @param count
     * @return JSONArray of comment details
     * @throws userException 
     */
    public JSONArray getComments(String commentId, int start, int count) throws userException {
        JSONArray comments = this.getCommentsForThread(commentId);
        JSONArray result = new JSONArray();
        if(count > this.maxCommentsCount) {
            return result;
        }
        int totalCount = comments.size();
        totalCount = Math.min(start + count,totalCount);
        if (start >= totalCount) {
            return result;
        }
        for (int i = start; i < totalCount; i++) {
            result.add(comments.getJSONObject(i));
        }
        return result;
    }
    
    /**
     * Assumes the start is 0 and count is maxCommentsCount
     * @param commentId parentId in comment table
     * @return JSONArray of comment details
     * @throws userException 
     */
    public JSONArray getComments(String commentId) throws userException {
        return this.getComments(commentId, 0, this.maxCommentsCount);
    }
}

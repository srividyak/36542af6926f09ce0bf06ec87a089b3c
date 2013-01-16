/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sqlManager.UGCThreads;

import com.mysql.jdbc.PreparedStatement;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import javanb.userpackage.userException;
import net.sf.json.JSONObject;

/**
 *
 * @author srivid
 */
public class groupThreadManager extends threadManager {

    public groupThreadManager() throws FileNotFoundException, IOException, SQLException {
        super();
    }

    /**
     * Insert a page/group into corresponding tables
     * @param threadDetails
     * @return
     * @throws userException 
     */
    @Override
    public int insertThread(JSONObject threadDetails) throws userException {
        String table = "", query = "";
        PreparedStatement stmt = null;
        if (threadDetails.containsKey("table")) {
            table = threadDetails.getString("table");
        } else {
            throw new userException("table name is mandatory to insert a thread");
        }
        try {
            if ("pages".equals(table)) {
                query = "insert into pages (owner,title,description,tags,timestamp,privacyLevel,pageId) values (?,?,?,?,?,?,?)";
                stmt = (PreparedStatement) this.threadConnection.prepareStatement(query);
                stmt.setString(7, threadDetails.getString("pageId"));
            } else if ("groups".equals(table)) {
                query = "insert into groups (owner,title,description,tags,timestamp,privacyLevel,groupId,moderators) values (?,?,?,?,?,?,?,?)";
                stmt.setString(7, threadDetails.getString("groupId"));
                stmt.setString(8, threadDetails.getString("moderators"));
            } else {
                throw new userException("no such table exists:" + table);
            }
            stmt.setString(1, threadDetails.getString("owner"));
            stmt.setString(2, threadDetails.getString("title"));
            stmt.setString(3, threadDetails.getString("description"));
            stmt.setString(4, threadDetails.getString("tags"));
            stmt.setLong(5, threadDetails.getLong("timestamp"));
            stmt.setString(6, threadDetails.getString("privacyLevel"));
            return stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new userException("error occured while inserting thread:" + ex.getMessage());
        }
    }
    
    @Override
    public void deleteThread(String table, String threadId) throws userException {
        try {
            String key = "";
            if(table.equals("pages")) {
                key = "pageId";
            } else if(table.equals("groups")) {
                key = "groupId";
            } else {
                throw new userException("no such table exists:" + table);
            }
            String query = "update " + table + " set disabled='1' where " + key + "=?";
            PreparedStatement stmt = (PreparedStatement) this.threadConnection.prepareStatement(query);
            stmt.setString(1, threadId);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new userException("error occured while disabling/deleting thread:" + ex.getMessage());
        }
    }
}

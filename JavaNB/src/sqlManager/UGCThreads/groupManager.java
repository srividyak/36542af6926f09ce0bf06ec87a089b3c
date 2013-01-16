/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sqlManager.UGCThreads;

import com.mysql.jdbc.PreparedStatement;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import javanb.userpackage.userException;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 * @author srivid
 */
public class groupManager extends groupThreadManager {

    public groupManager() throws FileNotFoundException, IOException, SQLException {
        super();
    }

    public void insertGroup(JSONObject groupDetails) throws userException {
        groupDetails.put("table", "groups");
        if (!groupDetails.containsKey("timestamp")) {
            groupDetails.put("timestamp", (new Date()).getTime());
        }
        this.insertThread(groupDetails);
    }

    public JSONObject getGroup(String groupId) throws userException {
        try {
            JSONObject result = new JSONObject();
            String query = "select * from groups where groupId=?";
            PreparedStatement stmt = (PreparedStatement) this.threadConnection.prepareStatement(query);
            stmt.setString(1, groupId);
            ResultSet rs = this.executeQuery(stmt);
            while (rs.next()) {
                result.put("groupId", rs.getString("groupId"));
                result.put("numMembers", rs.getLong("numMembers"));
                result.put("timestamp", rs.getLong("timestamp"));
                result.put("owner", rs.getString("owner"));
                result.put("moderators", rs.getString("moderators"));
                result.put("privacyLevel", rs.getString("privacyLevel"));
                result.put("title", rs.getString("title"));
                result.put("description", rs.getString("description"));
                result.put("tags", rs.getString("tags"));
                result.put("disabled", rs.getString("disabled"));
                result.put("abuseFlag", rs.getString("abuseFlag"));
            }
            return result;
        } catch (SQLException ex) {
            throw new userException("error occured while fetching group:" + ex.getMessage());
        }
    }

    public JSONArray getGroup(ArrayList<String> groupIds) throws userException {
        JSONArray results = new JSONArray();
        String query = "select * from groups where groupId in ({})";
        String replacements = "";
        int max = groupIds.size();
        for (int i = 0; i < max; i++) {
            replacements += "?,";
        }
        if (max > 0) {
            try {
                replacements = replacements.substring(0, max - 1);
                query = query.replaceAll("{}", replacements);
                PreparedStatement stmt = (PreparedStatement) this.threadConnection.prepareStatement(query);
                int index = 0;
                for (String groupId : groupIds) {
                    stmt.setString(index++, groupId);
                }
                ResultSet rs = this.executeQuery(stmt);
                while (rs.next()) {
                    JSONObject result = new JSONObject();
                    result.put("groupId", rs.getString("groupId"));
                    result.put("numMembers", rs.getLong("numMembers"));
                    result.put("timestamp", rs.getLong("timestamp"));
                    result.put("owner", rs.getString("owner"));
                    result.put("moderators", rs.getString("moderators"));
                    result.put("privacyLevel", rs.getString("privacyLevel"));
                    result.put("title", rs.getString("title"));
                    result.put("description", rs.getString("description"));
                    result.put("tags", rs.getString("tags"));
                    result.put("disabled", rs.getString("disabled"));
                    result.put("abuseFlag", rs.getString("abuseFlag"));
                    results.add(result);
                }
            } catch (SQLException ex) {
                throw new userException("error occured while fetching the group:" + ex.getMessage());
            }
        }
        return results;
    }

    public JSONArray getAllGroupsOfUser(String uuid) throws userException {
        try {
            JSONArray results = new JSONArray();
            String query = "select * from groups where owner=?";
            PreparedStatement stmt = (PreparedStatement) this.threadConnection.prepareStatement(query);
            stmt.setString(1, uuid);
            return results;
        } catch (SQLException ex) {
            throw new userException("error occured while fetching groups owned by user:" + uuid + ":" + ex.getMessage());
        }
    }
}

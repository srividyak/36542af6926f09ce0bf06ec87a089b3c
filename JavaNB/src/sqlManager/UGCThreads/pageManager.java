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
public class pageManager extends groupThreadManager {

    public pageManager() throws FileNotFoundException, IOException, SQLException {
        super();
    }

    public void insertPage(JSONObject pageDetails) throws userException {
        pageDetails.put("table", "pages");
        if (!pageDetails.containsKey("timestamp")) {
            pageDetails.put("timestamp", (new Date()).getTime());
        }
        this.insertThread(pageDetails);
    }

    public JSONObject getPage(String pageId) throws userException {
        try {
            String query = "select * from pages where pageId=?";
            JSONObject result = new JSONObject();
            PreparedStatement stmt = (PreparedStatement) this.threadConnection.prepareStatement(query);
            stmt.setString(1, pageId);
            ResultSet rs = this.executeQuery(stmt);
            while (rs.next()) {
                result.put("pageId", rs.getString("pageId"));
                result.put("owner", rs.getString("owner"));
                result.put("title", rs.getString("title"));
                result.put("description", rs.getString("description"));
                result.put("tags", rs.getString("tags"));
                result.put("privacyLevel", rs.getString("privacyLevel"));
                result.put("disabled", rs.getString("disabled"));
                result.put("abuseFlag", rs.getString("abuseFlag"));
                result.put("upRatingsCount", rs.getLong("upRatingsCount"));
                result.put("downRatingsCount", rs.getLong("downRatingsCount"));
                result.put("timestamp", rs.getLong("timestamp"));
                result.put("shareCount", rs.getLong("shareCount"));
            }
            return result;
        } catch (SQLException ex) {
            throw new userException("error occured while fetching the page:" + ex.getMessage());
        }
    }

    public JSONArray getPage(ArrayList<String> pageIds) throws userException {
        JSONArray results = new JSONArray();
        String query = "select * from pages where pageId in ({})";
        String replacements = "";
        int max = pageIds.size();
        for (int i = 0; i < max; i++) {
            replacements += "?,";
        }
        if (max > 0) {
            try {
                replacements = replacements.substring(0, max - 1);
                query = query.replaceAll("{}", replacements);
                PreparedStatement stmt = (PreparedStatement) this.threadConnection.prepareStatement(query);
                int index = 0;
                for (String pageId : pageIds) {
                    stmt.setString(index++, pageId);
                }
                ResultSet rs = this.executeQuery(stmt);
                while (rs.next()) {
                    JSONObject result = new JSONObject();
                    result.put("pageId", rs.getString("pageId"));
                    result.put("owner", rs.getString("owner"));
                    result.put("title", rs.getString("title"));
                    result.put("description", rs.getString("description"));
                    result.put("tags", rs.getString("tags"));
                    result.put("privacyLevel", rs.getString("privacyLevel"));
                    result.put("disabled", rs.getString("disabled"));
                    result.put("abuseFlag", rs.getString("abuseFlag"));
                    result.put("upRatingsCount", rs.getLong("upRatingsCount"));
                    result.put("downRatingsCount", rs.getLong("downRatingsCount"));
                    result.put("timestamp", rs.getLong("timestamp"));
                    result.put("shareCount", rs.getLong("shareCount"));
                    results.add(result);
                }
            } catch (SQLException ex) {
                throw new userException("error occured while fetching the page:" + ex.getMessage());
            }
        }
        return results;
    }

    public JSONArray getAllPagesOfUser(String uuid) throws userException {
        try {
            JSONArray results = new JSONArray();
            String query = "select * from pages where owner=?";
            PreparedStatement stmt = (PreparedStatement) this.threadConnection.prepareStatement(query);
            stmt.setString(1, uuid);
            return results;
        } catch (SQLException ex) {
            throw new userException("error occured while fetching pages owned by user:" + uuid + ":" + ex.getMessage());
        }
    }
}

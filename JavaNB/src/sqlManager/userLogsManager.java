/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sqlManager;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.PreparedStatement;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import javanb.userpackage.userException;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 * @author srivid
 */
public class userLogsManager  extends sqlUtils {
    private Connection userLogsConnection;
    
    public userLogsManager() throws FileNotFoundException, IOException, SQLException {
        this.userLogsConnection = this.getConnection();
    }
    
    public JSONArray getUserLogs(String uuid) throws userException {
        JSONArray userLogs = new JSONArray();
        try {
            String query = "select * from userLogs where uuid=? order by timestampLogin";
            PreparedStatement stmt = (PreparedStatement) this.userLogsConnection.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            while(rs.next()) {
                JSONObject log = new JSONObject();
                log.put("timestampLogin", rs.getLong("timestampLogin"));
                log.put("timestampLogout", rs.getLong("timestampLogout"));
                userLogs.add(log);
            }
        } catch (SQLException ex) {
            throw new userException("error in getting user logs for user with uuid:" + uuid + " " +ex.getMessage());
        }
        return userLogs;
    }
    
    public JSONArray getAllLogs() throws userException {
        try {
            JSONArray logs = new JSONArray();
            String query = "select * from userLogs order by timestampLogin";
            PreparedStatement stmt = (PreparedStatement) this.userLogsConnection.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            while(rs.next()) {
                JSONObject log = new JSONObject();
                log.put("timestampLogin", rs.getLong("timestampLogin"));
                log.put("timestampLogout", rs.getLong("timestampLogout"));
                log.put("uuid", rs.getString("uuid"));
            }
            return logs;
        } catch (SQLException ex) {
            throw new userException("error while fetching user logs:" +ex.getMessage());
        }
    }
    
    public void insertLog(String uuid, long timestampLogin, long timestampLogout) throws userException {
        try {
            String query = "insert into userLogs (uuid,timestampLogin,timestampLogout) values (?,?,?)";
            PreparedStatement stmt = (PreparedStatement) this.userLogsConnection.prepareStatement(query);
            stmt.setString(1, uuid);
            stmt.setLong(2, timestampLogin);
            stmt.setLong(3, timestampLogout);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new userException("error inserting into userLogs table for user:" + uuid + " " +ex.getMessage());
        }
        
    }
    
}

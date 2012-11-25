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
import java.util.logging.Level;
import java.util.logging.Logger;
import javanb.userpackage.userException;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 * @author srivid
 */
public class userLogsManager {
    private Connection userLogsConnection;
    
    private void getUserLogsConnection() throws FileNotFoundException, IOException, SQLException {
        sqlUtils utils = new sqlUtils();
        this.userLogsConnection = utils.getConnection();
    }
    
    public userLogsManager() throws userException {
        try {
            this.getUserLogsConnection();
        } catch (FileNotFoundException ex) {
            throw new userException("error in getting connection to user logs table:"+ex.getMessage());
        } catch (IOException ex) {
            throw new userException("error in getting connection to user logs table:"+ex.getMessage());
        } catch (SQLException ex) {
            throw new userException("error in getting connection to user logs table:"+ex.getMessage());
        }
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
    
    
}

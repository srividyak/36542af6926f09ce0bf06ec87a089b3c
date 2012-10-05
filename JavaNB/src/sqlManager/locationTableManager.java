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
import net.sf.json.JSONObject;

/**
 *
 * @author srivid
 */
public class locationTableManager {
    private Connection locationConnection;
    private JSONObject locationDetails;
    
    private void getLocationConnection() throws FileNotFoundException, IOException, SQLException {
        sqlUtils utils = new sqlUtils();
        this.locationConnection = utils.getConnection();
    }
    
    private void setLocationDetails(String id, String description) {
        this.locationDetails = new JSONObject();
        this.locationDetails.put("id", id);
        this.locationDetails.put("description", description);
    }
    
    private void getLocation(String locId, String description) throws SQLException {
        String query = "select * from locations where id = ?";
        PreparedStatement getRow = (PreparedStatement) this.locationConnection.prepareStatement(query);
        getRow.setString(1, locId);
        try {
            ResultSet locationResultSet = getRow.executeQuery();
            this.setLocationDetails(locationResultSet.getString("id"), locationResultSet.getString("description"));
        } catch(SQLException sqle) {
            //there is no such location hence create a new location with that id
            String insertQuery = "insert into locations (id,description) values (?,?)";
            PreparedStatement insertRow = (PreparedStatement) this.locationConnection.prepareStatement(insertQuery);
            insertRow.setString(1,locId);
            insertRow.setString(2,description);
            try {
                insertRow.executeUpdate();
                this.setLocationDetails(locId, "");
            } catch(SQLException insertSQLE) {
                System.out.println("Some error inserting into locations table:" + insertSQLE.getMessage());
            }
        }
    }
    
    public locationTableManager(String locId) throws FileNotFoundException, IOException, SQLException {
        this.getLocationConnection();
        this.getLocation(locId,"");
    }
    
    public locationTableManager(String locId, String description) throws FileNotFoundException, IOException, SQLException {
        this.getLocationConnection();
        this.getLocation(locId,description);
    }

    public JSONObject getLocationDetails() {
        return locationDetails;
    }
}

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
    
    private void setLocationDetails(String id, String name, String stateId, String stateName, String countryId, String countryName, String description) {
        this.locationDetails = new JSONObject();
        this.locationDetails.put("id", id);
        this.locationDetails.put("name", name);
        this.locationDetails.put("stateId", stateId);
        this.locationDetails.put("stateName", stateName);
        this.locationDetails.put("countryId", countryId);
        this.locationDetails.put("countryName", countryName);
        this.locationDetails.put("description", description);
    }
    
    //TODO - search usecase
    public String searchLocations(String name) {
        String searchQuery = "select * from locations where name = ? or stateName = ? or countryName = ?";
        return "";
    }
    
    public locationTableManager(JSONObject locData) throws FileNotFoundException, IOException, SQLException, userException {
        this.getLocationConnection();
        if(locData.containsKey("id")) {
            String selectString = "select * from locations where id = ?";
            PreparedStatement selectQuery = (PreparedStatement) this.locationConnection.prepareStatement(selectString);
            selectQuery.setString(1, locData.getString("id"));
            ResultSet getRow = selectQuery.executeQuery();
            boolean noLoc = true;
            while(getRow.next()) {
                noLoc = false;
                this.setLocationDetails(getRow.getString("id"), getRow.getString("name"), getRow.getString("stateId"), getRow.getString("stateName"), getRow.getString("countryId"), getRow.getString("countryName"), getRow.getString("description"));
            }
            if(noLoc) {
                try {
                    String insertString = "insert into locations (id,name,stateId,stateName,countryId,countryName,description) values(?,?,?,?,?,?,?)";
                    PreparedStatement insertQuery = (PreparedStatement) this.locationConnection.prepareStatement(insertString);
                    insertQuery.setString(1, locData.getString("id"));
                    insertQuery.setString(2, (locData.containsKey("name")) ? locData.getString("name") : "");
                    insertQuery.setString(3, (locData.containsKey("stateId")) ? locData.getString("stateId") : "");
                    insertQuery.setString(4, (locData.containsKey("stateName")) ? locData.getString("stateName") : "");
                    insertQuery.setString(5, (locData.containsKey("countryId")) ? locData.getString("countryId") : "");
                    insertQuery.setString(6, (locData.containsKey("countryName")) ? locData.getString("countryName") : "");
                    insertQuery.setString(7, (locData.containsKey("description")) ? locData.getString("description") : "");
                    insertQuery.executeUpdate();
                    this.setLocationDetails(locData.getString("id"), locData.getString("name"), locData.getString("stateId"), locData.getString("stateName"), locData.getString("countryId"), locData.getString("countryName"), locData.containsKey("description") ? locData.getString("description") : "");
                } catch(SQLException sqle) {
                    System.out.println("Some error inserting into location table:" + sqle.getMessage());
                }
            }
        }
    }

    public JSONObject getLocationDetails() {
        return locationDetails;
    }
}

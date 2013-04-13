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
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javanb.locationpackage.location;
import javanb.userpackage.userException;
import net.sf.json.JSONObject;

/**
 *
 * @author srivid
 */
public class locationTableManager extends sqlUtils {

    private Connection locationConnection;
    private JSONObject locationDetails;
    private location location;

    public location getLocation() {
        return location;
    }

    //TODO - search usecase
    public String searchLocations(String name) {
        String searchQuery = "select * from locations where name = ? or stateName = ? or countryName = ?";
        return "";
    }

    public locationTableManager(JSONObject locData) throws FileNotFoundException, IOException, SQLException, userException {
        this.dbConnection = this.getConnection();
        if (locData.containsKey("id")) {
            String selectString = "select * from locations where id = ?";
            PreparedStatement selectQuery = (PreparedStatement) this.locationConnection.prepareStatement(selectString);
            selectQuery.setString(1, locData.getString("id"));
            ResultSet getRow = selectQuery.executeQuery();
            boolean noLoc = true;
            while (getRow.next()) {
                noLoc = false;
            }
            if (noLoc) {
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
                } catch (SQLException sqle) {
                    System.out.println("Some error inserting into location table:" + sqle.getMessage());
                }
            }
        }
    }

    public locationTableManager() {
        try {
            this.dbConnection = this.getConnection();
        } catch (SQLException ex) {
            Logger.getLogger(locationTableManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public locationTableManager(String id, location location) {
        try {
            this.dbConnection = this.getConnection();
            String query = "select * from locations where id=?";
            PreparedStatement stmt = (PreparedStatement) this.dbConnection.prepareStatement(query);
            stmt.setString(1, id);
            ResultSet rs = this.executeQuery(stmt);
            if (rs.next()) {
                this.resultSetToJavaObject(location, rs);
                this.location = location;
            }
        } catch (userException ex) {
            Logger.getLogger(locationTableManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(locationTableManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public JSONObject getLocationDetails() {
        return locationDetails;
    }
    
    public ArrayList<location> getAllLocations() {
        try {
            String query = "select * from locations";
            PreparedStatement stmt = (PreparedStatement) this.dbConnection.prepareStatement(query);
            ArrayList<location> locations = new ArrayList<location>();
            ResultSet rs = this.executeQuery(stmt);
            while(rs.next()) {
                location location = new location();
                this.resultSetToJavaObject(location, rs);
                locations.add(location);
            }
            return locations;
        } catch (userException ex) {
            Logger.getLogger(locationTableManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(locationTableManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}

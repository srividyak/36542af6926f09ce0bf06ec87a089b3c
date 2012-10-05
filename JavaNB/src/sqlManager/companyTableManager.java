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
public class companyTableManager {
    private Connection companyConnection;
    private JSONObject companyDetails;
    
    private void getCompanyConnection() throws FileNotFoundException, IOException, SQLException {
        sqlUtils utils = new sqlUtils();
        this.companyConnection = utils.getConnection();
    }
    
    private void setCompanyDetails(String id, String description) {
        this.companyDetails = new JSONObject();
        this.companyDetails.put("id",id);
        this.companyDetails.put("description",description);
    }
    
    private void getCompany(String id, String description) throws SQLException {
        String query = "select * from companies where id = ?";
        PreparedStatement getRow = (PreparedStatement) this.companyConnection.prepareStatement(query);
        getRow.setString(1, id);
        try {
            ResultSet locationResultSet = getRow.executeQuery();
            this.setCompanyDetails(locationResultSet.getString("id"), locationResultSet.getString("description"));
        } catch(SQLException sqle) {
            //there is no such location hence create a new location with that id
            String insertQuery = "insert into locations (id,description) values (?,?)";
            PreparedStatement insertRow = (PreparedStatement) this.companyConnection.prepareStatement(insertQuery);
            insertRow.setString(1,id);
            insertRow.setString(2,description);
            try {
                insertRow.executeUpdate();
                this.setCompanyDetails(id, "");
            } catch(SQLException insertSQLE) {
                System.out.println("Some error inserting into locations table:" + insertSQLE.getMessage());
            }
        }
    }

    public JSONObject getCompanyDetails() {
        return companyDetails;
    }
    
    public companyTableManager(String id) throws FileNotFoundException, IOException, SQLException {
        this.getCompanyConnection();
        this.getCompany(id,"");
    }
    
    public companyTableManager(String id, String description) throws FileNotFoundException, IOException, SQLException {
        this.getCompanyConnection();
        this.getCompany(id, description);
    }
}

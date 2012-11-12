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
public class companyTableManager {
    private Connection companyConnection;
    private JSONObject companyDetails;
    
    private void getCompanyConnection() throws FileNotFoundException, IOException, SQLException {
        sqlUtils utils = new sqlUtils();
        this.companyConnection = utils.getConnection();
    }
    
    private void setCompanyDetails(String id, String name, String description) {
        this.companyDetails = new JSONObject();
        this.companyDetails.put("id",id);
        this.companyDetails.put("name",name);
        this.companyDetails.put("description",description);
    }
    
    public JSONObject getCompanyDetails() {
        return companyDetails;
    }
    
    public companyTableManager(JSONObject compData) throws FileNotFoundException, IOException, SQLException, userException {
        this.getCompanyConnection();
        if(compData.containsKey("id")) {
            //first do a search for company
            String id = compData.getString("id");
            String selectString = "select * from companies where id=?";
            PreparedStatement selectQuery = (PreparedStatement) this.companyConnection.prepareStatement(selectString);
            selectQuery.setString(1, id);
            boolean noComp = true;
            ResultSet selectResultSet = selectQuery.executeQuery();
            while(selectResultSet.next()) {
                noComp = false;
                this.setCompanyDetails(selectResultSet.getString("id"), selectResultSet.getString("name"), selectResultSet.getString("description"));
            }
            if(noComp) {
                try {
                    String insertString = "insert into companies (id,name,description) values (?,?,?)";
                    PreparedStatement insertQuery = (PreparedStatement) this.companyConnection.prepareStatement(insertString);
                    insertQuery.setString(1, id);
                    insertQuery.setString(2, compData.getString("name"));
                    insertQuery.setString(3, compData.containsKey("description") ? compData.getString("description") : "");
                    insertQuery.executeUpdate();
                    this.setCompanyDetails(compData.getString("id"), compData.getString("name"), compData.containsKey("description") ? compData.getString("description") : "");
                } catch(SQLException sqle) {
                    System.out.println("Some error inserting into company table:" + sqle.getMessage());
                }
            }
        } else {
            throw new userException("id of company missing");
        }
    }
}

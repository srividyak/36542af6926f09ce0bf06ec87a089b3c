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
import java.util.Iterator;
import javanb.userpackage.userException;
import net.sf.json.JSONObject;

/**
 *
 * @author srivid
 */
public class educationTableManager {
    private Connection educationConnection = null;
    private JSONObject educationDetails;
    
    private void getEducationConnection() throws FileNotFoundException, IOException, SQLException {
        sqlUtils utils = new sqlUtils();
        this.educationConnection = utils.getConnection();
    }
    
    private void setEducationDetails(String id, String name, String type, String major, String description) {
        this.educationDetails = new JSONObject();
        this.educationDetails.put("id", id);
        this.educationDetails.put("name", name);
        this.educationDetails.put("type", type);
        this.educationDetails.put("major", major);
        this.educationDetails.put("description", description);
    }
    
    public JSONObject getEducationDetails() {
        return this.educationDetails;
    }
    
    public void updateEducationTable(JSONObject eduData, String id) throws userException, SQLException {
        String updateQuery = "update education set ";
        Iterator<String> keys = eduData.keys();
        while (keys.hasNext()) {
            String column = (String) keys.next();
            String value = eduData.getString(column);
            updateQuery += column + "=" + "\"" + value + "\",";
        }
        if (updateQuery.charAt(updateQuery.length() - 1) == ',') {
            updateQuery = updateQuery.substring(0, updateQuery.length() - 1);
        }
        updateQuery += " where id=\"" + id + "\"";
        PreparedStatement updateStmt = (PreparedStatement) this.educationConnection.prepareStatement(updateQuery);
        updateStmt.executeUpdate();
    }
    
    public educationTableManager() throws FileNotFoundException, IOException, SQLException {
        this.getEducationConnection();
    }
    
    public educationTableManager(JSONObject eduData) throws SQLException, FileNotFoundException, IOException, userException {
        this.getEducationConnection();
        if(eduData.containsKey("id")) {
            String id = eduData.getString("id");
            String query = "select * from education where id=?";
            PreparedStatement getQuery = (PreparedStatement) this.educationConnection.prepareStatement(query);
            getQuery.setString(1, eduData.getString("id"));
            try {
                ResultSet educationResultSet = getQuery.executeQuery();
                boolean noEdu = true;
                while(educationResultSet.next()) {
                    noEdu = false;
                    boolean noMajor = true, noType = true;
                    String majorInDB = educationResultSet.getString("major");
                    String typeInDB = educationResultSet.getString("type");
                    String[] major = majorInDB.split(",");
                    String[] type = typeInDB.split(",");
                    for(int i=0,max=major.length;i<max;i++) {
                        if(!eduData.containsKey("major")) {
                            break;
                        } else {
                            if(eduData.getString("major").equals(major[i])) {
                                noMajor = false;
                            }
                        }
                    }
                    for(int i=0,max=type.length;i<max;i++) {
                        if(!eduData.containsKey("type")) {
                            break;
                        } else {
                            if(eduData.getString("type").equals(type[i])) {
                                noType = false;
                            }
                        }
                    }
                    JSONObject newEduDetails = new JSONObject();
                    if(noMajor) { //the specified major is not added into database
                        majorInDB += "," + eduData.getString("major");
                        newEduDetails.put("major", majorInDB);
                    }
                    if(noType) {
                        typeInDB += "," + eduData.getString("type");
                        newEduDetails.put("type", typeInDB);
                    }
                    this.updateEducationTable(newEduDetails,eduData.getString("id"));
                    this.setEducationDetails(educationResultSet.getString("id"), educationResultSet.getString("name"), typeInDB, majorInDB, educationResultSet.getString("description"));
                }
                if(noEdu) {
                    String insertQueryString;
                    if(eduData.containsKey("name")) {
                        insertQueryString = "insert into education (id,name,type,major,description) values (?,?,?,?,?)";
                        PreparedStatement insertQuery = (PreparedStatement) this.educationConnection.prepareStatement(insertQueryString);
                        insertQuery.setString(1, id);
                        insertQuery.setString(2, eduData.getString("name"));
                        insertQuery.setString(3, eduData.containsKey("type") ? eduData.getString("type") : "");
                        insertQuery.setString(4, eduData.containsKey("major") ? eduData.getString("major") : "");
                        insertQuery.setString(5, eduData.containsKey("description") ? eduData.getString("description") : "");
                        insertQuery.executeUpdate();
                        this.setEducationDetails(id, eduData.getString("name"), eduData.getString("type"), eduData.getString("major"), eduData.containsKey("description") ? eduData.getString("description") : "");
                    } else {
                        throw new userException("invalid education name passed or no education name passed");
                    }
                }
            } catch(SQLException sqle) {
                System.out.println("Some error inserting into education table:" + sqle.getMessage());
            } 
        } else {
            throw new userException("education id missing");
        }
    }
}

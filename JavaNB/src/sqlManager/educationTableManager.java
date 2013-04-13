/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sqlManager;

import com.mysql.jdbc.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javanb.educationpackage.education;
import javanb.userpackage.userException;
import net.sf.json.JSONObject;

/**
 *
 * @author srivid
 */
public class educationTableManager extends sqlUtils {

    public void updateEducationTable(JSONObject eduData, String id) {
        try {
            this.dbConnection = this.getConnection();
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
            PreparedStatement updateStmt = (PreparedStatement) this.dbConnection.prepareStatement(updateQuery);
            updateStmt.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(educationTableManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public educationTableManager() {
        try {
            this.dbConnection = this.getConnection();
        } catch (SQLException ex) {
            Logger.getLogger(educationTableManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public educationTableManager(String id, education education) {
        try {
            this.dbConnection = this.getConnection();
            String query = "select * from education where id=?";
            PreparedStatement stmt = (PreparedStatement) this.dbConnection.prepareStatement(query);
            stmt.setString(1, id);
            ResultSet rs = this.executeQuery(stmt);
            if (rs.next()) {
                this.resultSetToJavaObject(education, rs);
            }
        } catch (userException ex) {
            Logger.getLogger(educationTableManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(educationTableManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public ArrayList<education> getAllEducation() {
        try {
            ArrayList<education> educationList = new ArrayList<education>();
            String query = "select * from education";
            PreparedStatement stmt = (PreparedStatement) this.dbConnection.prepareStatement(query);
            ResultSet rs = this.executeQuery(stmt);
            while(rs.next()) {
                education e = new education();
                this.resultSetToJavaObject(e, rs);
                educationList.add(e);
            }
            return educationList;
        } catch (userException ex) {
            Logger.getLogger(educationTableManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(educationTableManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public educationTableManager(JSONObject eduData) throws userException {
        try {
            this.dbConnection = this.getConnection();
            if (eduData.containsKey("id")) {
                try {
                    String id = eduData.getString("id");
                    String query = "select * from education where id=?";
                    PreparedStatement getQuery = (PreparedStatement) this.dbConnection.prepareStatement(query);
                    getQuery.setString(1, eduData.getString("id"));
                    try {
                        ResultSet educationResultSet = getQuery.executeQuery();
                        boolean noEdu = true;
                        while (educationResultSet.next()) {
                            noEdu = false;
                            boolean noMajor = true, noType = true;
                            String majorInDB = educationResultSet.getString("major");
                            String typeInDB = educationResultSet.getString("type");
                            String[] major = majorInDB.split(",");
                            String[] type = typeInDB.split(",");
                            for (int i = 0, max = major.length; i < max; i++) {
                                if (!eduData.containsKey("major")) {
                                    break;
                                } else {
                                    if (eduData.getString("major").equals(major[i])) {
                                        noMajor = false;
                                    }
                                }
                            }
                            for (int i = 0, max = type.length; i < max; i++) {
                                if (!eduData.containsKey("type")) {
                                    break;
                                } else {
                                    if (eduData.getString("type").equals(type[i])) {
                                        noType = false;
                                    }
                                }
                            }
                            JSONObject newEduDetails = new JSONObject();
                            if (noMajor) { //the specified major is not added into database
                                majorInDB += "," + eduData.getString("major");
                                newEduDetails.put("major", majorInDB);
                            }
                            if (noType) {
                                typeInDB += "," + eduData.getString("type");
                                newEduDetails.put("type", typeInDB);
                            }
                            this.updateEducationTable(newEduDetails, eduData.getString("id"));
                        }
                        if (noEdu) {
                            String insertQueryString;
                            if (eduData.containsKey("name")) {
                                insertQueryString = "insert into education (id,name,type,major,description) values (?,?,?,?,?)";
                                PreparedStatement insertQuery = (PreparedStatement) this.dbConnection.prepareStatement(insertQueryString);
                                insertQuery.setString(1, id);
                                insertQuery.setString(2, eduData.getString("name"));
                                insertQuery.setString(3, eduData.containsKey("type") ? eduData.getString("type") : "");
                                insertQuery.setString(4, eduData.containsKey("major") ? eduData.getString("major") : "");
                                insertQuery.setString(5, eduData.containsKey("description") ? eduData.getString("description") : "");
                                insertQuery.executeUpdate();
                            } else {
                                throw new userException("invalid education name passed or no education name passed");
                            }
                        }
                    } catch (SQLException sqle) {
                        System.out.println("Some error inserting into education table:" + sqle.getMessage());
                    }
                } catch (SQLException ex) {
                    Logger.getLogger(educationTableManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                throw new userException("education id missing");
            }
        } catch (SQLException ex) {
            Logger.getLogger(educationTableManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

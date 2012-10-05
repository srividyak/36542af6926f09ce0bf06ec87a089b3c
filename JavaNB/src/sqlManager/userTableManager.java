/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sqlManager;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.PreparedStatement;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import javanb.userpackage.user;
import javanb.userpackage.userException;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

/**
 *
 * @author srivid
 */
public class userTableManager {
    private Connection userConnection = null;
    private JSONObject userDetails;
    
    private void getUserConnection() throws FileNotFoundException, IOException, SQLException {
        sqlUtils utils = new sqlUtils();
        this.userConnection = utils.getConnection();
    }
    
    public JSONObject getUserDetails() {
        return this.userDetails;
    }
    
    public boolean updateGender(String uuid, boolean gender) throws SQLException, FileNotFoundException, IOException {
        String updateQuery = "insert into users (gender) values (?) where uuid=?";
        this.getUserConnection();
        PreparedStatement stmt = (PreparedStatement) this.userConnection.prepareStatement(updateQuery);
        stmt.setBoolean(1, gender);
        stmt.setString(2,uuid);
        try {
            int rowCount = stmt.executeUpdate();
            this.userConnection.close();
            if (rowCount > 0) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException sqle) {
            System.err.println("error updating field: dob for user: " + uuid);
            this.userConnection.close();
            return false;
        }
    }
    
    public boolean updateDob(String uuid, String date) throws ParseException, FileNotFoundException, IOException, SQLException {
        SimpleDateFormat simpleDob = new SimpleDateFormat("yyyy-mm-dd", Locale.ENGLISH);
        java.util.Date dob = simpleDob.parse(date);
        java.sql.Date sqlDate = new java.sql.Date(dob.getTime());
        String updateQuery = "insert into users (dob) values (?) where uuid=?";
        this.getUserConnection();
        PreparedStatement stmt = (PreparedStatement) this.userConnection.prepareStatement(updateQuery);
        stmt.setDate(1, sqlDate);
        stmt.setString(2, uuid);
        try {
            int rowCount = stmt.executeUpdate();
            this.userConnection.close();
            if (rowCount > 0) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException sqle) {
            System.err.println("error updating field: dob for user: " + uuid);
            this.userConnection.close();
            return false;
        }
    }
    
    public boolean updateStringField(String uuid, String field, String value) throws FileNotFoundException, IOException, SQLException {
        String updateQuery = "insert into users (?) values (?) where uuid=?";
        this.getUserConnection();
        PreparedStatement stmt = (PreparedStatement) this.userConnection.prepareStatement(updateQuery);
        stmt.setString(1, field);
        stmt.setString(2, value);
        stmt.setString(3, uuid);
        try {
            int rowCount = stmt.executeUpdate();
            this.userConnection.close();
            if (rowCount > 0) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException sqle) {
            System.err.println("error updating field: " + field + "for user: " + uuid);
            this.userConnection.close();
            return false;
        }
    }
    
    //default constructor for tableManager
    public userTableManager() {
        
    }
    
    //user already exists. do a select
    public userTableManager(String uuid) throws FileNotFoundException, IOException, SQLException {
        this.getUserConnection();
        //TODO: debug as to why preparedstatement is not working for select queries
        String query = "select * from users where uuid="+"\""+uuid+"\"";
        //String query = "select * from users where uuid = ?";
        PreparedStatement stmt = (PreparedStatement) this.userConnection.prepareStatement(query);
        //stmt.setString(1, uuid);
        ResultSet resultSet = stmt.executeQuery(query);
        this.userDetails = new JSONObject();
        while(resultSet.next()) {
            this.userDetails.put("firstName",resultSet.getString(1));
            this.userDetails.put("middleName",resultSet.getString(2));
            this.userDetails.put("lastName",resultSet.getString(3));
            this.userDetails.put("dob",resultSet.getString(4));
            this.userDetails.put("uuid",resultSet.getString(5));
            this.userDetails.put("interests",resultSet.getString(6));
            this.userDetails.put("phoneNum",resultSet.getString(7));
            this.userDetails.put("gender",resultSet.getBoolean(8));
            this.userDetails.put("relStatus",resultSet.getString(9));
            this.userDetails.put("lang",resultSet.getString(10));
            this.userDetails.put("locations",resultSet.getString(11));
            this.userDetails.put("homeTown",resultSet.getString(12));
            this.userDetails.put("education",resultSet.getString(13));
            this.userDetails.put("about",resultSet.getString(14));
        }
        this.userConnection.close();
    }
    
    //new user to be created. do an insert
    public userTableManager(JSONObject userInfo) throws FileNotFoundException, IOException, SQLException, ParseException {
        this.getUserConnection();
        SimpleDateFormat simpleDob = new SimpleDateFormat("yyyy-mm-dd", Locale.ENGLISH);
        java.util.Date dob = simpleDob.parse(userInfo.getString("dob"));
        java.sql.Date sqlDate = new java.sql.Date(dob.getTime());
        String query = "insert into users (firstName,middleName,lastName,dob,uuid,interests,phoneNum,gender,relStatus,lang,locations,homeTown,education,about) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?);";
        PreparedStatement insertRow = (PreparedStatement) this.userConnection.prepareStatement(query);
        insertRow.setString(1, userInfo.getString("firstName"));
        try {
            insertRow.setString(2, userInfo.getString("middleName"));
        } catch(JSONException jsone) {
            insertRow.setNull(2,java.sql.Types.NULL);
        }
        insertRow.setString(3, userInfo.getString("lastName"));
        insertRow.setDate(4, sqlDate);
        insertRow.setString(5, userInfo.getString("uuid"));
        try {
            insertRow.setString(6, userInfo.getString("interests"));
        } catch(JSONException jsone) {
            insertRow.setNull(6,java.sql.Types.NULL);
        }
        try {
            insertRow.setString(7, userInfo.getString("phoneNum"));
        } catch(JSONException jsone) {
            insertRow.setNull(7,java.sql.Types.NULL);
        }
        insertRow.setBoolean(8, userInfo.getBoolean("gender"));
        try {
            insertRow.setString(9, userInfo.getString("relStatus"));
        } catch(JSONException jsone) {
            insertRow.setNull(9,java.sql.Types.NULL);
        }
        try {
            insertRow.setString(10, userInfo.getString("lang"));
        } catch(JSONException jsone) {
            insertRow.setNull(10,java.sql.Types.NULL);
        }
        try {
            insertRow.setString(11, userInfo.getString("locations"));
        } catch(JSONException jsone) {
            insertRow.setNull(11,java.sql.Types.NULL);
        }
        try {
            insertRow.setString(12, userInfo.getString("hometown"));
        } catch(JSONException jsone) {
            insertRow.setNull(12,java.sql.Types.NULL);
        }
        try {
            insertRow.setString(13, userInfo.getString("education"));
        } catch(JSONException jsone) {
            insertRow.setNull(13,java.sql.Types.NULL);
        }
        try {
            insertRow.setString(14, userInfo.getString("about"));
        } catch(JSONException jsone) {
            insertRow.setNull(14,java.sql.Types.NULL);
        }
        
        try {
            insertRow.executeUpdate();
        } catch(SQLException sqle) {
            System.err.println("Could not insert user");
        }
        this.userConnection.close();
    }
}

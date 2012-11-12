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
import java.util.logging.Level;
import java.util.logging.Logger;
import javanb.userpackage.user;
import javanb.userpackage.userException;
import net.sf.json.JSONArray;
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
        String updateQuery = "update users set gender=" + gender + " where uuid=\'" + uuid + "\'";
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
        String updateQuery = "update users set dob=\'"+sqlDate+"\' where uuid=\'"+"\'";
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
        //TODO: investigate why placeholder logic isnt working
        String updateQuery = "update users set " + field + "=\'" + value + "\' where uuid=" + "\'" + uuid + "\'";
        PreparedStatement stmt = (PreparedStatement) this.userConnection.prepareStatement(updateQuery);
//        stmt.setString(1, field);
//        stmt.setString(2, value);
//        stmt.setString(3, uuid);
        try {
            int rowCount = stmt.executeUpdate();
            this.userConnection.close();
            if (rowCount > 0) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException sqle) {
            System.err.println("error updating field: " + field + "for user: " + uuid + sqle.getMessage());
            this.userConnection.close();
            return false;
        }
    }
    
    //default constructor for tableManager
    public userTableManager() throws FileNotFoundException, IOException, SQLException {
        this.getUserConnection();
    }
    
    public JSONArray getMultipleUsers(ArrayList<String> uuids) throws userException {
        try {
            String query = "select * from users where uuid in ({})";
            JSONArray results = new JSONArray();
            String replacements = "";
            for(int i=0,max=uuids.size();i<max;i++) {
                if(i == max - 1) {
                    replacements += "?";
                } else {
                    replacements += "?,";
                }
            }
            query = query.replace("{}", replacements);
            PreparedStatement stmt = (PreparedStatement) this.userConnection.prepareStatement(query);
            for(int i=0,max=uuids.size();i<max;i++) {
                stmt.setString(i+1, uuids.get(i));
            }
            ResultSet resultSet = stmt.executeQuery();
            while(resultSet.next()) {
                this.userDetails = new JSONObject();
                this.userDetails.put("firstName",resultSet.getString("firstName"));
                this.userDetails.put("middleName",resultSet.getString("middleName"));
                this.userDetails.put("lastName",resultSet.getString("lastName"));
                this.userDetails.put("dob",resultSet.getString("dob"));
                this.userDetails.put("uuid",resultSet.getString("uuid"));
                this.userDetails.put("interests",resultSet.getString("interests"));
                this.userDetails.put("phoneNum",resultSet.getString("phoneNum"));
                this.userDetails.put("gender",resultSet.getBoolean("gender"));
                this.userDetails.put("relStatus",resultSet.getString("relStatus"));
                this.userDetails.put("lang",resultSet.getString("lang"));
                this.userDetails.put("locations",resultSet.getString("locations"));
                this.userDetails.put("hometown",resultSet.getString("homeTown"));
                this.userDetails.put("educations",resultSet.getString("education"));
                this.userDetails.put("about",resultSet.getString("about"));
                this.userDetails.put("companies",resultSet.getString("company"));
                this.userDetails.put("email",resultSet.getString("email"));
                results.add(this.userDetails);
            }
            return results;
        } catch (SQLException ex) {
            throw new userException("error occured during fetching multiple users:"+ex.getMessage());
        }
    }
    
    //user already exists. do a select
    public userTableManager(String uuid) throws userException, FileNotFoundException, IOException, SQLException {
        //TODO: debug as to why preparedstatement is not working for select queries
        this.getUserConnection();
        String query = "select * from users where uuid=?";
        PreparedStatement stmt = (PreparedStatement) this.userConnection.prepareStatement(query);
        stmt.setString(1, uuid);
        try {
            ResultSet resultSet = stmt.executeQuery();
            this.userDetails = new JSONObject();
            boolean noUserFlag = true;
            while(resultSet.next()) {
                noUserFlag = false;
                this.userDetails.put("firstName",resultSet.getString("firstName"));
                this.userDetails.put("middleName",resultSet.getString("middleName"));
                this.userDetails.put("lastName",resultSet.getString("lastName"));
                this.userDetails.put("dob",resultSet.getString("dob"));
                this.userDetails.put("uuid",resultSet.getString("uuid"));
                this.userDetails.put("interests",resultSet.getString("interests"));
                this.userDetails.put("phoneNum",resultSet.getString("phoneNum"));
                this.userDetails.put("gender",resultSet.getBoolean("gender"));
                this.userDetails.put("relStatus",resultSet.getString("relStatus"));
                this.userDetails.put("lang",resultSet.getString("lang"));
                this.userDetails.put("locations",resultSet.getString("locations"));
                this.userDetails.put("hometown",resultSet.getString("homeTown"));
                this.userDetails.put("educations",resultSet.getString("education"));
                this.userDetails.put("about",resultSet.getString("about"));
                this.userDetails.put("companies",resultSet.getString("company"));
                this.userDetails.put("email",resultSet.getString("email"));
            }
            if(noUserFlag) {
                throw new userException("Could not find user with uuid="+uuid);
            }
        } catch(SQLException sqle) {
            throw new userException("Could not find user with uuid="+uuid);
        }
        
        this.userConnection.close();
    }
    
    public void updateFriends(String uuid, String friends) throws userException {
        String query = "update users set friends=concat(friends+\',\'+?) where uuid=?";
        try {
            PreparedStatement updateStmt = (PreparedStatement) this.userConnection.prepareStatement(query);
            updateStmt.setString(1, friends);
            updateStmt.setString(2, uuid);
            updateStmt.executeUpdate();
        } catch (SQLException ex) {
            throw new userException("unable to add friend to uuid="+uuid);
        }
    }
    
    public JSONObject getFields(String[] fields, String uuid) throws userException {
        String query = "select {placeholder} from users where uuid=?";
        String replacements = "";
        ResultSet rs;
        for(int i=0,max=fields.length;i<max;i++) {
            if(i==max-1) {
                replacements += fields[i];
            } else {
                replacements += fields[i] + ",";
            }
        }
        query = query.replace("{placeholder}", replacements);
        try {
            PreparedStatement stmt = (PreparedStatement) this.userConnection.prepareStatement(query);
            stmt.setString(1, uuid);
            rs = stmt.executeQuery();
            JSONObject result = new JSONObject();
            while(rs.next()) {
                for(int i=0,max=fields.length;i<max;i++) {
                    result.put(fields[i],rs.getString(fields[i]));
                }
            }
            return result;
        } catch(SQLException sqle) {
            throw new userException("unable to get specified fields");
        }
    }
    
    public void updateFriendsCount(String uuid) throws userException {
        try {
            String query = "update users set friendsCount=friendsCount+1 where uuid=?";
            PreparedStatement stmt = (PreparedStatement) this.userConnection.prepareStatement(query);
            stmt.setString(1,uuid);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new userException("error occured during updating friends count for uuid="+uuid+ex.getMessage());
        }
        
    }
    
    //new user to be created. do an insert
    public userTableManager(JSONObject userInfo) throws FileNotFoundException, IOException, SQLException, ParseException, userException {
        this.getUserConnection();
        String email = userInfo.getString("email");
        String selectQuery = "select * from users where email=?";
        PreparedStatement selectStmt = (PreparedStatement) this.userConnection.prepareStatement(selectQuery);
        selectStmt.setString(1, email);
        ResultSet rs = selectStmt.executeQuery();
        while(rs.next()) {
            throw new userException("user with this email:"+email+" already exists");
        }
        SimpleDateFormat simpleDob = new SimpleDateFormat("yyyy-mm-dd", Locale.ENGLISH);
        java.util.Date dob = simpleDob.parse(userInfo.getString("dob"));
        java.sql.Date sqlDate = new java.sql.Date(dob.getTime());
        String query = "insert into users (firstName,middleName,lastName,dob,uuid,interests,phoneNum,gender,relStatus,lang,locations,homeTown,education,about,company,email) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?);";
        PreparedStatement insertRow = (PreparedStatement) this.userConnection.prepareStatement(query);
        insertRow.setString(1, userInfo.getString("firstName"));
        insertRow.setString(2, userInfo.containsKey("middleName") ? userInfo.getString("middleName") : "");
        insertRow.setString(3, userInfo.getString("lastName"));
        insertRow.setDate(4, sqlDate);
        insertRow.setString(5, userInfo.getString("uuid"));
        insertRow.setString(6, userInfo.containsKey("interests") ? userInfo.getString("interests") : "");
        insertRow.setString(7, userInfo.containsKey("phoneNum") ? userInfo.getString("phoneNum") : "");
        insertRow.setBoolean(8, userInfo.getBoolean("gender"));
        insertRow.setString(9, userInfo.containsKey("relStatus") ? userInfo.getString("relStatus") : "");
        insertRow.setString(10, userInfo.containsKey("lang") ? userInfo.getString("lang") : "");
        insertRow.setString(11, userInfo.containsKey("locations") ? userInfo.getString("locations") : "");
        insertRow.setString(12, userInfo.containsKey("hometown") ? userInfo.getString("hometown") : "");
        insertRow.setString(13, userInfo.containsKey("educations") ? userInfo.getString("educations") : "");
        insertRow.setString(14, userInfo.containsKey("about") ? userInfo.getString("about") : "");
        insertRow.setString(15, userInfo.containsKey("companies") ? userInfo.getString("companies") : "");
        insertRow.setString(16, email);
        try {
            insertRow.executeUpdate();
        } catch(SQLException sqle) {
            System.err.println("Could not insert user");
        }
        this.userConnection.close();
    }
}

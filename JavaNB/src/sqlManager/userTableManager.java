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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import javanb.userpackage.userException;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 * @author srivid
 */
public class userTableManager extends sqlUtils {

    private Connection userConnection = null;
    private JSONObject userDetails;
    public static String[] queries = {
        "select * from users where uuid=?",
        "select * from users where email=?"
    };

    public JSONObject getUserDetails() {
        return this.userDetails;
    }

    public void updateUser(String uuid, JSONObject userDetails) throws userException {
        try {
            String query = "update users set params where uuid=?";
            String replacements = "";
            Iterator keys = userDetails.keys();
            while (keys.hasNext()) {
                replacements += keys.next() + "=?,";
            }
            int length = replacements.length();
            if (length > 0) {
                replacements = replacements.substring(0, length - 1);
            }
            query = query.replaceAll("params", replacements);
            PreparedStatement stmt = (PreparedStatement) this.userConnection.prepareStatement(query);
            keys = userDetails.keys();
            int index = 1;
            while (keys.hasNext()) {
                String key = (String) keys.next();
                if ("gender".equals(key)) {
                    stmt.setBoolean(index, userDetails.getBoolean(key));
                } else if ("dob".equals(key)) {
                    SimpleDateFormat simpleDob = new SimpleDateFormat("yyyy-mm-dd", Locale.ENGLISH);
                    java.util.Date dob = simpleDob.parse(userDetails.getString(key));
                    java.sql.Date sqlDate = new java.sql.Date(dob.getTime());
                    stmt.setDate(index, sqlDate);
                } else {
                    stmt.setString(index, userDetails.getString(key));
                }
                index++;
            }
            stmt.setString(index,uuid);
            stmt.executeUpdate();
            this.invalidateCacheForUuids(uuid);
        } catch (ParseException ex) {
            throw new userException("error occured while updating user:error in date format:" + ex.getMessage());
        } catch (SQLException ex) {
            throw new userException("error occured while updating user:" + ex.getMessage());
        }
    }

    //default constructor for tableManager
    public userTableManager() throws FileNotFoundException, IOException, SQLException {
        super();
        this.userConnection = this.getConnection();
    }

    /**
     * API to get multiple users in one shot. This query's results are not
     * cached for performance reasons.
     *
     * @param uuids - list of uuids whose user info are to be fetched
     * @return JSONArray of user info
     * @throws userException
     */
    public JSONArray getMultipleUsers(ArrayList<String> uuids) throws userException {
        try {
            String query = "select * from users where uuid in ({})";
            JSONArray results = new JSONArray();
            String replacements = "";
            for (int i = 0, max = uuids.size(); i < max; i++) {
                if (i == max - 1) {
                    replacements += "?";
                } else {
                    replacements += "?,";
                }
            }
            query = query.replace("{}", replacements);
            PreparedStatement stmt = (PreparedStatement) this.userConnection.prepareStatement(query);
            for (int i = 0, max = uuids.size(); i < max; i++) {
                stmt.setString(i + 1, uuids.get(i));
            }
            ResultSet resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                this.userDetails = new JSONObject();
                this.userDetails.put("firstName", resultSet.getString("firstName"));
                this.userDetails.put("middleName", resultSet.getString("middleName"));
                this.userDetails.put("lastName", resultSet.getString("lastName"));
                this.userDetails.put("dob", resultSet.getString("dob"));
                this.userDetails.put("uuid", resultSet.getString("uuid"));
                this.userDetails.put("interests", resultSet.getString("interests"));
                this.userDetails.put("phoneNum", resultSet.getString("phoneNum"));
                this.userDetails.put("gender", resultSet.getBoolean("gender"));
                this.userDetails.put("relStatus", resultSet.getString("relStatus"));
                this.userDetails.put("lang", resultSet.getString("lang"));
                this.userDetails.put("locations", resultSet.getString("locations"));
                this.userDetails.put("hometown", resultSet.getString("homeTown"));
                this.userDetails.put("educations", resultSet.getString("education"));
                this.userDetails.put("about", resultSet.getString("about"));
                this.userDetails.put("companies", resultSet.getString("company"));
                this.userDetails.put("email", resultSet.getString("email"));
                results.add(this.userDetails);
            }
            return results;
        } catch (SQLException ex) {
            throw new userException("error occured during fetching multiple users:" + ex.getMessage());
        }
    }

    //user already exists. do a select
    public userTableManager(String uuid) throws userException, FileNotFoundException, IOException, SQLException {
        //TODO: debug as to why preparedstatement is not working for select queries
        super();
        this.userConnection = this.getConnection();
        String query = "select * from users where uuid=?";
        PreparedStatement stmt = (PreparedStatement) this.userConnection.prepareStatement(query);
        stmt.setString(1, uuid);
        try {
            ResultSet resultSet = this.executeQuery(stmt);
            this.userDetails = new JSONObject();
            boolean noUserFlag = true;
            while (resultSet.next()) {
                noUserFlag = false;
                this.userDetails.put("firstName", resultSet.getString("firstName"));
                this.userDetails.put("middleName", resultSet.getString("middleName"));
                this.userDetails.put("lastName", resultSet.getString("lastName"));
                this.userDetails.put("dob", resultSet.getString("dob"));
                this.userDetails.put("uuid", resultSet.getString("uuid"));
                this.userDetails.put("interests", resultSet.getString("interests"));
                this.userDetails.put("phoneNum", resultSet.getString("phoneNum"));
                this.userDetails.put("gender", resultSet.getBoolean("gender"));
                this.userDetails.put("relStatus", resultSet.getString("relStatus"));
                this.userDetails.put("lang", resultSet.getString("lang"));
                this.userDetails.put("locations", resultSet.getString("locations"));
                this.userDetails.put("hometown", resultSet.getString("homeTown"));
                this.userDetails.put("educations", resultSet.getString("education"));
                this.userDetails.put("about", resultSet.getString("about"));
                this.userDetails.put("companies", resultSet.getString("company"));
                this.userDetails.put("email", resultSet.getString("email"));
            }
            if (noUserFlag) {
                throw new userException("Could not find user with uuid=" + uuid);
            }
        } catch (SQLException sqle) {
            throw new userException("Could not find user with uuid=" + uuid);
        }
    }

    public JSONObject getFields(String[] fields, String uuid) throws userException {
        String query = "select * from users where uuid=?";
        ResultSet rs;
        try {
            PreparedStatement stmt = (PreparedStatement) this.userConnection.prepareStatement(query);
            stmt.setString(1, uuid);
            rs = this.executeQuery(stmt);
            JSONObject result = new JSONObject();
            while (rs.next()) {
                for (int i = 0, max = fields.length; i < max; i++) {
                    result.put(fields[i], rs.getString(fields[i]));
                }
            }
            return result;
        } catch (SQLException sqle) {
            throw new userException("unable to get specified fields");
        }
    }

    public JSONArray getFieldsForMultipleUuid(String[] fields, ArrayList<String> uuids) throws userException {
        JSONArray result = new JSONArray();
        String query = "select * from users where uuid in ({})";
        ResultSet rs;
        String replacements = "";
        for (int i = 0, max = uuids.size(); i < max; i++) {
            if (i == max - 1) {
                replacements += "?";
            } else {
                replacements += "?,";
            }
        }
        query = query.replace("{}", replacements);
        try {
            PreparedStatement stmt = (PreparedStatement) this.userConnection.prepareStatement(query);
            for (int i = 0, max = uuids.size(); i < max; i++) {
                stmt.setString(i + 1, uuids.get(i));
            }
            rs = stmt.executeQuery();
            while (rs.next()) {
                for (int i = 0, max = fields.length; i < max; i++) {
                    JSONObject temp = new JSONObject();
                    temp.put(fields[i], rs.getString(fields[i]));
                    result.add(temp);
                }
            }
        } catch (SQLException sqle) {
            throw new userException("unable to get specified fields");
        }
        return result;
    }

    public void updateFriendsCount(String uuid, int increase) throws userException {
        try {
            String query;
            if (increase == 0) {
                query = "update users set friendsCount=friendsCount-1 where uuid=?";
            } else {
                query = "update users set friendsCount=friendsCount+1 where uuid=?";
            }
            PreparedStatement stmt = (PreparedStatement) this.userConnection.prepareStatement(query);
            stmt.setString(1, uuid);
            stmt.executeUpdate();
            PreparedStatement invalidateStmt = this.getPreparedStatement(this.userConnection, userTableManager.queries[0], uuid);
            this.invalidateCache(invalidateStmt);
        } catch (SQLException ex) {
            throw new userException("error occured during updating friends count for uuid=" + uuid + ex.getMessage());
        }

    }

    public void invalidateCacheForUuids(String uuid) throws userException {
        String[] uuids = uuid.split(",");
        PreparedStatement[] stmts = new PreparedStatement[uuids.length];
        for (int i = 0, max = stmts.length; i < max; i++) {
            stmts[i] = this.getPreparedStatement(userConnection, userTableManager.queries[i], uuids[i]);
        }
        this.invalidateCache(stmts);
    }

    //new user to be created. do an insert
    public userTableManager(JSONObject userInfo) throws FileNotFoundException, IOException, SQLException, ParseException, userException {
        super();
        this.userConnection = this.getConnection();
        String email = userInfo.getString("email");
        String selectQuery = "select * from users where email=?";
        PreparedStatement selectStmt = (PreparedStatement) this.userConnection.prepareStatement(selectQuery);
        selectStmt.setString(1, email);
        ResultSet rs = selectStmt.executeQuery();
        while (rs.next()) {
            throw new userException("user with this email:" + email + " already exists");
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
        } catch (SQLException sqle) {
            System.err.println("Could not insert user");
        }
    }
    
}

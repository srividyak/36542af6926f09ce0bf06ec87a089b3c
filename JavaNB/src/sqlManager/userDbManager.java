/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sqlManager;

import com.mysql.jdbc.PreparedStatement;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javanb.userpackage.user;
import javanb.userpackage.userException;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author srivid
 */
public class userDbManager extends sqlUtils {

    private user user;

    public userDbManager() {
        try {
            this.dbConnection = this.getConnection();
        } catch (SQLException ex) {
            Logger.getLogger(userDbManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public userDbManager(user user) {
        super();
        this.user = user;
        try {
            this.dbConnection = this.getConnection();
        } catch (SQLException ex) {
            Logger.getLogger(userDbManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public userDbManager(String uuid, user user) {
        super();
        this.user = user;
        try {
            this.dbConnection = this.getConnection();
            String query = "select * from users where uuid=?";
            PreparedStatement stmt = (PreparedStatement) this.dbConnection.prepareStatement(query);
            stmt.setString(1, uuid);
            ResultSet resultSet = this.executeQuery(stmt);
            this.setUser(resultSet);
        } catch (userException ex) {
            Logger.getLogger(userDbManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(userDbManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public ArrayList<user> getMultipleUsers(ArrayList<String> uuids) {
        int len = uuids.size();
        String replacements = "";
        PreparedStatement stmt;
        ResultSet resultSet;
        ArrayList<user> users = new ArrayList<user>();
        try {
            String query = "select * from users where uuid in ({})";
            for (int i = 0; i < len; i++) {
                replacements += "?,";
            }
            replacements = StringUtils.strip(replacements, ",");
            query = query.replaceAll("{}", replacements);
            stmt = (PreparedStatement) this.dbConnection.prepareStatement(query);
            for (int i = 0; i < len; i++) {
                stmt.setString(i + 1, uuids.get(i));
            }
            resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                user user = new user();
                this.resultSetToJavaObject(user, resultSet);
                users.add(user);
            }
            return users;
        } catch (SQLException ex) {
            Logger.getLogger(userDbManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * Gets all users in the DB (Killer call. Hence not cached)
     *
     * @return arraylist of user type containing all users in DB
     */
    public ArrayList<user> getMultipleUsers() {
        String query = "select * from users";
        try {
            PreparedStatement stmt = (PreparedStatement) this.dbConnection.prepareStatement(query);
            ResultSet rs = stmt.executeQuery();
            ArrayList<user> users = new ArrayList<user>();
            while (rs.next()) {
                user user = new user();
                //TODO: needs to be processed parallely to gain some performance
                this.resultSetToJavaObject(user, rs);
                users.add(user);
            }
            return users;
        } catch (SQLException ex) {
            Logger.getLogger(userDbManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public userDbManager(JSONObject userInfo) throws userException {
        try {
            this.dbConnection = this.getConnection();
            String query = "insert into users ";
            String columns[] = tablesMetaDataHandler.getTableInfo("users");
            String keys = "";
            String values = "";
            PreparedStatement insertRow;

            /*
             * populating keys and values
             */
            for (String column : columns) {
                if (userInfo.containsKey(column)) {
                    keys += column + ",";
                    values += "?,";
                }
            }
            keys = StringUtils.strip(keys, ",");
            keys = "(" + keys + ")";

            values = StringUtils.strip(values, ",");
            values = "values(" + values + ")";

            query += " " + keys + " " + values;
            String email = userInfo.getString("email");
            SimpleDateFormat simpleDob = new SimpleDateFormat("yyyy-mm-dd", Locale.ENGLISH);
            java.util.Date dob = simpleDob.parse(userInfo.getString("dob"));
            java.sql.Date sqlDate = new java.sql.Date(dob.getTime());
            insertRow = (PreparedStatement) this.dbConnection.prepareStatement(query);
            int j = 1;
            for (int i = 0, max = columns.length; i < max; i++) {
                String column = columns[i];
                if (column.equals("dob")) {
                    insertRow.setDate(j, sqlDate);
                    j++;
                } else if (column.equals("gender")) {
                    insertRow.setBoolean(j, userInfo.getBoolean(column));
                    j++;
                } else {
                    if (userInfo.containsKey(columns[i])) {
                        insertRow.setString(j, userInfo.getString(columns[i]));
                        j++;
                    }
                }
            }
            try {
                insertRow.executeUpdate();
            } catch (SQLException sqle) {
                Logger.getLogger(userDbManager.class.getName()).log(Level.SEVERE, null, sqle);
            }
        } catch (ParseException ex) {
            Logger.getLogger(userDbManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(userDbManager.class.getName()).log(Level.SEVERE, null, ex);
        }
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
            PreparedStatement stmt = (PreparedStatement) this.dbConnection.prepareStatement(query);
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
                } else if ("friendsCount".equals(key)) {
                    stmt.setInt(index, userDetails.getInt(key));
                } else {
                    stmt.setString(index, userDetails.getString(key));
                }
                index++;
            }
            stmt.setString(index, uuid);
            stmt.executeUpdate();
            this.invalidateCacheForUuids(uuid);
        } catch (ParseException ex) {
            Logger.getLogger(userDbManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(userDbManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void setUser(ResultSet userResultSet) {
        if (this.user == null) {
            this.user = new user();
        }
        try {
            userResultSet.next();
            this.resultSetToJavaObject(this.user, userResultSet);
        } catch (SQLException ex) {
            Logger.getLogger(userDbManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public user getUser() {
        return user;
    }

    public void invalidateCacheForUuids(String uuid) throws userException {
        String[] uuids = uuid.split(",");
        PreparedStatement[] stmts = new PreparedStatement[uuids.length];
        for (int i = 0, max = stmts.length; i < max; i++) {
            stmts[i] = this.getPreparedStatement(dbConnection, userTableManager.queries[i], uuids[i]);
        }
        this.invalidateCache(stmts);
    }
}

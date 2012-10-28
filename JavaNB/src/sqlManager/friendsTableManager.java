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
import java.util.Date;
import java.util.InvalidPropertiesFormatException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javanb.userpackage.userException;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 * @author srivid
 */
public class friendsTableManager {
    private Connection friendsConnection;
    
    private void getConnection() throws FileNotFoundException, IOException, SQLException {
        sqlUtils utils;
        utils = new sqlUtils();
        this.friendsConnection = utils.getConnection();
    }
    
    public friendsTableManager() throws FileNotFoundException, IOException, SQLException {
        this.getConnection();
    }
    
    public void sendRequest(String myUuid, String friendUuid) throws userException {
        String query = "insert into friends (myUuid,friendUuid,relation,timeSent,timestamp) values(?,?,?,?,?)";
        try {
            long date = (new Date()).getTime();
            PreparedStatement stmt = (PreparedStatement) this.friendsConnection.prepareStatement(query);
            stmt.setString(1, myUuid);
            stmt.setString(2, friendUuid);
            stmt.setString(3, "0");
            stmt.setLong(4,date);
            stmt.setLong(5,date);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new userException("some error occured while sending request:" + ex.getMessage());
        }
        
    }
    
    public void acceptRequest(String myUuid, String friendUuid) throws userException {
        String query = "update friends set relation=?, timeAccepted=?, reverseTimestamp=? where myUuid=? and friendUuid=?";
        try {
            long date = (new Date()).getTime();
            PreparedStatement stmt = (PreparedStatement) this.friendsConnection.prepareStatement(query);
            stmt.setString(1,"1");
            stmt.setLong(2,date);
            stmt.setLong(3,date);
            stmt.setString(4, friendUuid);
            stmt.setString(5,myUuid);
            stmt.executeUpdate();
        } catch (SQLException sqle) {
            throw new userException("some error occured while accepting request:" + sqle.getMessage());
        }
    }
    
    public void unfriend(String myUuid, String friendUuid, boolean delete) throws userException {
        String query;
        if(delete) {
            query = "delete from friends where (myUuid=? and friendUuid=?) or (myUuid=? and friendUuid=?)";
        } else {
            query = "update friends set relation='2',timeTerminated=? where (myUuid=? and friendUuid=?) or (myUuid=? and friendUuid=?)";
        }
        try {
            Long date = (new Date()).getTime();
            PreparedStatement stmt = (PreparedStatement) this.friendsConnection.prepareStatement(query);
            stmt.setLong(1, date);
            stmt.setString(2,myUuid);
            stmt.setString(3,friendUuid);
            stmt.setString(4,myUuid);
            stmt.setString(5,friendUuid);
            stmt.executeUpdate();
        } catch(SQLException sqle) {
            throw new userException("some error occured while unfriending:" + sqle.getMessage());
        }
    }
    
    public void unfriend(String myUuid, String friendUuid) throws userException {
        this.unfriend(myUuid, friendUuid, false);
    }
    
    //updates viewCount and timestamp
    public void updateViewCount(String viewerUuid, String vieweeUuid) throws userException {
        String query = "update friends set viewCount=viewCount+1,timestamp=? where myUuid=? and friendUuid=? or set reverseViewCount=reverseViewCount+1,reverseTimestamp=? where myUuid = ? and friendUuid=?";
        PreparedStatement stmt;
        Long date = (new Date()).getTime();
        try {
            stmt = (PreparedStatement) this.friendsConnection.prepareStatement(query);
            stmt.setLong(1,date);
            stmt.setString(2, viewerUuid);
            stmt.setString(3, vieweeUuid);
            stmt.setLong(4,date);
            stmt.setString(5, vieweeUuid);
            stmt.setString(6, viewerUuid);
            stmt.executeUpdate();
        } catch(SQLException sqle) {
            query = "update friends set auxViewCount=auxViewCount+1,timestamp=?  where myUuid=? or set reverseAuxViewCount=reverseAuxViewCount+1,timestamp=? where friendUuid=?";
            try {
                stmt = (PreparedStatement) this.friendsConnection.prepareStatement(query);
                stmt.setLong(1, date);
                stmt.setString(2, viewerUuid);
                stmt.setString(3, vieweeUuid);
                stmt.setLong(4, date);
                stmt.setString(5, vieweeUuid);
                stmt.setString(6, viewerUuid);
            } catch(SQLException sqle2) {
                throw new userException("Some error occured during updating viewCount:"+sqle2.getMessage());
            }
        }
    }
    
    public JSONArray getFriends(String uuid, String type) throws userException {
        JSONArray friends = new JSONArray();
        String query = "";
        if(type.trim().toLowerCase().equals("all")) {
            query = "select * from friends where friendUuid=? and (relation='1' or relation='0') union select * from friends where myUuid=? and (relation='1' or relation='0')";
        } else if(type.trim().toLowerCase().equals("top")) {
            query = "select * from friends where friendUuid=? union select * from friends where myUuid=?";
        }
        try {
            PreparedStatement stmt = (PreparedStatement) this.friendsConnection.prepareStatement(query);
            stmt.setString(1, uuid);
            stmt.setString(2, uuid);
            ResultSet rs = stmt.executeQuery();
            while(rs.next()) {
                JSONObject friend = new JSONObject();
                if(uuid.equals(rs.getString("myUuid"))) {
                    friend.put("uuid",rs.getString("friendUuid"));
                    friend.put("viewCount",rs.getString("viewCount"));
                    friend.put("auxViewCount",rs.getString("auxViewCount"));
                    friend.put("timestamp", rs.getString("timestamp"));
                } else if(uuid.equals(rs.getString("friendUuid"))) {
                    friend.put("uuid",rs.getString("myUuid"));
                    friend.put("viewCount",rs.getString("reverseViewCount"));
                    friend.put("auxViewCount",rs.getString("reverseAuxViewCount"));
                    friend.put("timestamp", rs.getString("reverseTimestamp"));
                } else {
                    throw new userException("error in record");
                }
                friends.add(friend);
            }
        } catch(SQLException sqle) {
            throw new userException("some error occured while fetching friends:" + sqle.getMessage());
        }
        return friends;
    }
    
    public String[] getMutualFriends(String uuid1, String uuid2) throws userException {
        String[] friends = {};
        String query = "select uuid from (select myUuid as uuid from friends where friendUuid=?" +
                "union select friendUuid as uuid from friends where myUuid=?) as table1 where uuid in" + 
                "(select myUuid as uuid from friends where friendUuid=?" + 
                "union select friendUuid as uuid from friends where myUuid=?)";
        try {
            PreparedStatement stmt = (PreparedStatement) this.friendsConnection.prepareStatement(query);
            stmt.setString(1, uuid1);
            stmt.setString(2, uuid1);
            stmt.setString(3, uuid2);
            stmt.setString(4, uuid2);
        } catch (SQLException sqle) {
            throw new userException("exception encountered while fetching mutual friends"+sqle.getMessage());
        }
        return friends;
    }
    
}

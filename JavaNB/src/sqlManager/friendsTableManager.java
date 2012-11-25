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
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javanb.userpackage.userException;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 *
 * @author srivid
 */
public class friendsTableManager extends sqlUtils {

    private Connection friendsConnection;
    public static String[] queries = {
        "select * from friends where myUuid=? and friendUuid=?",
        "select * from friends where (myUuid=? and friendUuid=?) or (myUuid=? and friendUuid=?)",
        "select * from friends where friendUuid=? union select * from friends where myUuid=?"
    };

    public friendsTableManager() throws FileNotFoundException, IOException, SQLException {
        super();
        this.friendsConnection = this.getConnection();
    }

    public void sendRequest(String myUuid, String friendUuid) throws userException {
        //doesn't result in any data change as such since getAlFriends and getViewed would remain the same
        String directQuery = "update friends set relation='0' where myUuid=? and friendUuid=?";
        String reverseQuery = "select * from friends where myUuid=? and friendUuid=?";
        String query = "insert into friends (myUuid,friendUuid,relation,timeSent,timestamp) values(?,?,?,?,?)";
        int result;
        try {
            PreparedStatement directStmt = (PreparedStatement) this.friendsConnection.prepareStatement(directQuery);
            PreparedStatement reverseStmt = (PreparedStatement) this.friendsConnection.prepareStatement(reverseQuery);
            directStmt.setString(1, myUuid);
            directStmt.setString(2, friendUuid);
            result = directStmt.executeUpdate();
            if (result == 1) {
                return;
            } else {
                reverseStmt.setString(1, friendUuid);
                reverseStmt.setString(2, myUuid);
                //ResultSet rs = reverseStmt.executeQuery();
                ResultSet rs = this.executeQuery(reverseStmt); //reads from db or cache accordingly
                while (rs.next()) {
                    //doesn't result in any data change as such since getAlFriends and getViewed would remain the same
                    String curMyUuid = rs.getString("myUuid");
                    String curFriendUuid = rs.getString("friendUuid");
                    long viewCount = rs.getLong("viewCount");
                    long reverseViewCount = rs.getLong("reverseViewCount");
                    long timestamp = rs.getLong("timestamp");
                    long reverseTimestamp = rs.getLong("reverseTimestamp");
                    long auxViewCount = rs.getLong("auxViewCount");
                    long reverseAuxViewCount = rs.getLong("reverseAuxViewCount");
                    reverseQuery = "update friends set myUuid=?,friendUuid=?,relation=?,viewCount=?,"
                            + "auxViewCount=?,reverseViewCount=?,reverseAuxViewCount=?,"
                            + "timestamp=?,reverseTimestamp=? where myUuid=? and friendUuid=?";
                    reverseStmt = (PreparedStatement) this.friendsConnection.prepareStatement(reverseQuery);
                    reverseStmt.setString(1, curFriendUuid);
                    reverseStmt.setString(2, curMyUuid);
                    reverseStmt.setString(3, "0");
                    reverseStmt.setLong(4, reverseViewCount);
                    reverseStmt.setLong(5, reverseAuxViewCount);
                    reverseStmt.setLong(6, viewCount);
                    reverseStmt.setLong(7, auxViewCount);
                    reverseStmt.setLong(8, reverseTimestamp);
                    reverseStmt.setLong(9, timestamp);
                    reverseStmt.setString(10, curMyUuid);
                    reverseStmt.setString(11, curFriendUuid);
                    reverseStmt.executeUpdate();
                    return;
                }
            }
            long date = (new Date()).getTime();
            PreparedStatement stmt = (PreparedStatement) this.friendsConnection.prepareStatement(query);
            stmt.setString(1, myUuid);
            stmt.setString(2, friendUuid);
            stmt.setString(3, "0");
            stmt.setLong(4, date);
            stmt.setLong(5, date);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            throw new userException("some error occured while sending request:" + ex.getMessage());
        }

    }

    public void acceptRequest(String myUuid, String friendUuid) throws userException {
        String query = "update friends set relation=?, timeAccepted=?, reverseTimestamp=? where myUuid=? and friendUuid=? and relation='0'";
        try {
            long date = (new Date()).getTime();
            PreparedStatement stmt = (PreparedStatement) this.friendsConnection.prepareStatement(query);
            stmt.setString(1, "1");
            stmt.setLong(2, date);
            stmt.setLong(3, date);
            stmt.setString(4, friendUuid);
            stmt.setString(5, myUuid);
            int count = stmt.executeUpdate();
            if (count > 0) {
                userTableManager sqlManager = new userTableManager();
                sqlManager.updateFriendsCount(myUuid, 1);
                sqlManager.updateFriendsCount(friendUuid, 1);
                //for invalidating cache
                PreparedStatement stmts[] = new PreparedStatement[6];
                stmts[0] = this.getPreparedStatement(friendsConnection, friendsTableManager.queries[0], friendUuid + "," + myUuid);
                stmts[1] = this.getPreparedStatement(friendsConnection, friendsTableManager.queries[1], friendUuid + "," + myUuid + "," + myUuid + "," + friendUuid);
                stmts[2] = this.getPreparedStatement(friendsConnection, friendsTableManager.queries[2], myUuid + "," + myUuid);
                stmts[3] = this.getPreparedStatement(friendsConnection, friendsTableManager.queries[2], friendUuid + "," + friendUuid);
                this.invalidateCache(stmts);
                sqlManager.invalidateCacheForUuids(myUuid + "," + friendUuid);
            }
        } catch (FileNotFoundException ex) {
            throw new userException("some error occured while accepting request:" + ex.getMessage());
        } catch (IOException ex) {
            throw new userException("some error occured while accepting request:" + ex.getMessage());
        } catch (SQLException sqle) {
            throw new userException("some error occured while accepting request:" + sqle.getMessage());
        }
    }

    public void unfriend(String myUuid, String friendUuid, boolean delete) throws userException {
        String query;
        if (delete) {
            query = "delete from friends where (myUuid=? and friendUuid=?) or (myUuid=? and friendUuid=?)";
        } else {
            query = "update friends set relation='2',timeTerminated=? where (myUuid=? and friendUuid=?) or (myUuid=? and friendUuid=?)";
        }
        try {
            Long date = (new Date()).getTime();
            PreparedStatement stmt = (PreparedStatement) this.friendsConnection.prepareStatement(query);
            stmt.setLong(1, date);
            stmt.setString(2, myUuid);
            stmt.setString(3, friendUuid);
            stmt.setString(4, myUuid);
            stmt.setString(5, friendUuid);
            int count = stmt.executeUpdate();
            if (count > 0) {
                userTableManager sqlManager = new userTableManager();
                sqlManager.updateFriendsCount(myUuid, 0);
                sqlManager.updateFriendsCount(friendUuid, 0);
                //invalidate cache
                PreparedStatement stmts[] = new PreparedStatement[6];
                stmts[0] = this.getPreparedStatement(friendsConnection, friendsTableManager.queries[0], friendUuid + "," + myUuid);
                stmts[1] = this.getPreparedStatement(friendsConnection, friendsTableManager.queries[1], friendUuid + "," + myUuid + "," + myUuid + "," + friendUuid);
                stmts[2] = this.getPreparedStatement(friendsConnection, friendsTableManager.queries[2], myUuid + "," + myUuid);
                stmts[3] = this.getPreparedStatement(friendsConnection, friendsTableManager.queries[2], friendUuid + "," + friendUuid);
                this.invalidateCache(stmts);
                sqlManager.invalidateCacheForUuids(myUuid + "," + friendUuid);
            }
        } catch (FileNotFoundException ex) {
            throw new userException("some error occured while unfriending:" + ex.getMessage());
        } catch (IOException ex) {
            Logger.getLogger(friendsTableManager.class.getName()).log(Level.SEVERE, null, ex);
            throw new userException("some error occured while unfriending:" + ex.getMessage());
        } catch (SQLException sqle) {
            throw new userException("some error occured while unfriending:" + sqle.getMessage());
        }
    }

    public void unfriend(String myUuid, String friendUuid) throws userException {
        this.unfriend(myUuid, friendUuid, false);
    }

    //updates viewCount and timestamp
    public void updateViewCount(String viewerUuid, String vieweeUuid) throws userException {
        String getQuery = "select * from friends where (myUuid=? and friendUuid=?) or (myUuid=? and friendUuid=?)", query = "";
        PreparedStatement stmt;
        Long date = (new Date()).getTime();
        try {
            PreparedStatement getStmt = (PreparedStatement) this.friendsConnection.prepareStatement(getQuery);
            getStmt.setString(1, viewerUuid);
            getStmt.setString(2, vieweeUuid);
            getStmt.setString(3, vieweeUuid);
            getStmt.setString(4, viewerUuid);
            ResultSet rs = getStmt.executeQuery();
            while (rs.next()) {
                if (rs.getString("myUuid").equals(viewerUuid) && rs.getString("friendUuid").equals(vieweeUuid)) {
                    query = "update friends set viewCount=viewCount+1,timestamp=? where myUuid=? and friendUuid=?";
                    stmt = (PreparedStatement) this.friendsConnection.prepareStatement(query);
                    stmt.setString(2, viewerUuid);
                    stmt.setString(3, vieweeUuid);
                    stmt.setLong(1, date);
                    try {
                        int count = stmt.executeUpdate();
                        if (count > 0) {
                            //invalidate cache
                            PreparedStatement stmts[] = new PreparedStatement[3];
                            stmts[0] = this.getPreparedStatement(friendsConnection, friendsTableManager.queries[0], viewerUuid + "," + vieweeUuid);
                            stmts[1] = this.getPreparedStatement(friendsConnection, friendsTableManager.queries[1], viewerUuid + "," + vieweeUuid + "," + vieweeUuid + "," + viewerUuid);
                            stmts[2] = this.getPreparedStatement(friendsConnection, friendsTableManager.queries[2], viewerUuid + "," + viewerUuid);
                            this.invalidateCache(stmts);
                            return;
                        }
                    } catch (SQLException sqle) {
                        query = "update friends set auxViewCount=auxViewCount+1,timestamp=? where myUuid=? and friendUuid=?";
                        stmt = (PreparedStatement) this.friendsConnection.prepareStatement(query);
                        stmt.setString(2, viewerUuid);
                        stmt.setString(3, vieweeUuid);
                        stmt.setLong(1, date);
                        stmt.executeUpdate();
                        //invalidate cache
                        PreparedStatement stmts[] = new PreparedStatement[3];
                        stmts[0] = this.getPreparedStatement(friendsConnection, friendsTableManager.queries[0], viewerUuid + "," + vieweeUuid);
                        stmts[1] = this.getPreparedStatement(friendsConnection, friendsTableManager.queries[1], viewerUuid + "," + vieweeUuid + "," + vieweeUuid + "," + viewerUuid);
                        stmts[2] = this.getPreparedStatement(friendsConnection, friendsTableManager.queries[2], viewerUuid + "," + viewerUuid);
                        this.invalidateCache(stmts);
                        return;
                    }
                } else if (rs.getString("myUuid").equals(vieweeUuid) && rs.getString("friendUuid").equals(viewerUuid)) {
                    query = "update friends set reverseViewCount=reverseViewCount+1,timestamp=? where myUuid=? and friendUuid=?";
                    stmt = (PreparedStatement) this.friendsConnection.prepareStatement(query);
                    stmt.setString(2, vieweeUuid);
                    stmt.setString(3, viewerUuid);
                    stmt.setLong(1, date);
                    try {
                        int count = stmt.executeUpdate();
                        if (count > 0) {
                            //invalidate cache
                            PreparedStatement stmts[] = new PreparedStatement[3];
                            stmts[0] = this.getPreparedStatement(friendsConnection, friendsTableManager.queries[0], vieweeUuid + "," + viewerUuid);
                            stmts[1] = this.getPreparedStatement(friendsConnection, friendsTableManager.queries[1], vieweeUuid + "," + viewerUuid + "," + viewerUuid + "," + vieweeUuid);
                            stmts[2] = this.getPreparedStatement(friendsConnection, friendsTableManager.queries[2], vieweeUuid + "," + vieweeUuid);
                            this.invalidateCache(stmts);
                            return;
                        }
                    } catch (SQLException sqle) {
                        query = "update friends set reverseAuxViewCount=reverseAuxViewCount+1,timestamp=? where myUuid=? and friendUuid=?";
                        stmt = (PreparedStatement) this.friendsConnection.prepareStatement(query);
                        stmt.setString(2, vieweeUuid);
                        stmt.setString(3, viewerUuid);
                        stmt.setLong(1, date);
                        int count = stmt.executeUpdate();
                        if (count > 0) {
                            //invalidate cache
                            PreparedStatement stmts[] = new PreparedStatement[3];
                            stmts[0] = this.getPreparedStatement(friendsConnection, friendsTableManager.queries[0], vieweeUuid + "," + viewerUuid);
                            stmts[1] = this.getPreparedStatement(friendsConnection, friendsTableManager.queries[1], vieweeUuid + "," + viewerUuid + "," + viewerUuid + "," + vieweeUuid);
                            stmts[2] = this.getPreparedStatement(friendsConnection, friendsTableManager.queries[2], vieweeUuid + "," + vieweeUuid);
                            this.invalidateCache(stmts);
                            return;
                        }
                    }
                }
            }
            query = "insert into friends (myUuid,friendUuid,relation,timestamp,viewCount) values(?,?,?,?,?)";
            stmt = (PreparedStatement) this.friendsConnection.prepareStatement(query);
            stmt.setString(1, viewerUuid);
            stmt.setString(2, vieweeUuid);
            stmt.setString(3, "2");
            stmt.setLong(4, date);
            stmt.setInt(5, 1);
            stmt.executeUpdate();
            //invalidate cache
            PreparedStatement stmts[] = new PreparedStatement[3];
            stmts[0] = this.getPreparedStatement(friendsConnection, friendsTableManager.queries[0], viewerUuid + "," + vieweeUuid);
            stmts[1] = this.getPreparedStatement(friendsConnection, friendsTableManager.queries[1], viewerUuid + "," + vieweeUuid + "," + vieweeUuid + "," + viewerUuid);
            stmts[2] = this.getPreparedStatement(friendsConnection, friendsTableManager.queries[2], viewerUuid + "," + viewerUuid);
            this.invalidateCache(stmts);
        } catch (SQLException sqle) {
            throw new userException("Some error occured during updating viewCount:" + sqle.getMessage());
        }
    }

    /**
     * API to fetch all friends of a user or top viewed and friends of a user
     *
     * @param uuid - id of the person whose friends are to be returned
     * @param type - all/top
     * @return JSONArray of friends
     * @throws userException
     */
    public JSONArray getFriends(String uuid, String type) throws userException {
        JSONArray friends = new JSONArray();
        String query = "select * from friends where friendUuid=? union select * from friends where myUuid=?";
        try {
            PreparedStatement stmt = (PreparedStatement) this.friendsConnection.prepareStatement(query);
            stmt.setString(1, uuid);
            stmt.setString(2, uuid);
            ResultSet rs = this.executeQuery(stmt);
            while (rs.next()) {
                if (type.trim().toLowerCase().equals("all")) {
                    String relation = rs.getString("relation");
                    if (relation.equals("2") || relation.equals("0")) {
                        continue;
                    }
                }
                JSONObject friend = new JSONObject();
                if (uuid.equals(rs.getString("myUuid"))) {
                    friend.put("uuid", rs.getString("friendUuid"));
                    friend.put("viewCount", rs.getString("viewCount"));
                    friend.put("auxViewCount", rs.getString("auxViewCount"));
                    friend.put("timestamp", rs.getString("timestamp"));
                } else if (uuid.equals(rs.getString("friendUuid"))) {
                    friend.put("uuid", rs.getString("myUuid"));
                    friend.put("viewCount", rs.getString("reverseViewCount"));
                    friend.put("auxViewCount", rs.getString("reverseAuxViewCount"));
                    friend.put("timestamp", rs.getString("reverseTimestamp"));
                } else {
                    throw new userException("error in record");
                }
                friends.add(friend);
            }
        } catch (SQLException sqle) {
            throw new userException("some error occured while fetching friends:" + sqle.getMessage());
        }
        return friends;
    }

    /**
     * Unused API. DO NOT USE this API since it is not friendly for caching
     *
     * @param uuid1
     * @param uuid2
     * @return
     * @throws userException
     */
    public ArrayList<String> getMutualFriends(String uuid1, String uuid2) throws userException {
        ArrayList<String> friends = new ArrayList<String>();
        String query = "select uuid from (select myUuid as uuid from friends where friendUuid=?"
                + " union select friendUuid as uuid from friends where myUuid=?) as table1 where uuid in"
                + " (select myUuid as uuid from friends where friendUuid=?"
                + " union select friendUuid as uuid from friends where myUuid=?)";
        try {
            PreparedStatement stmt = (PreparedStatement) this.friendsConnection.prepareStatement(query);
            stmt.setString(1, uuid1);
            stmt.setString(2, uuid1);
            stmt.setString(3, uuid2);
            stmt.setString(4, uuid2);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                friends.add(rs.getString("uuid"));
            }
        } catch (SQLException sqle) {
            throw new userException("exception encountered while fetching mutual friends" + sqle.getMessage());
        }
        return friends;
    }

    /**
     * Unused API. DO NOT USE this API since it is not friendly for caching
     *
     * @param friend
     * @param myUuid
     * @return
     * @throws userException
     */
    public ArrayList<String> getNonFriends(String friend, String myUuid) throws userException {
        ArrayList<String> friends = new ArrayList<String>();
        String query = "select uuid from (select myUuid as uuid from friends where friendUuid=? union "
                + " select friendUuid as uuid from friends where myUuid=?) as rec1 where uuid not in"
                + " (select myUuid as uuid from friends where friendUuid=? union"
                + " select friendUuid as uuid from friends where myUuid=?)";
        try {
            PreparedStatement stmt = (PreparedStatement) this.friendsConnection.prepareStatement(query);
            stmt.setString(1, friend);
            stmt.setString(2, friend);
            stmt.setString(3, myUuid);
            stmt.setString(4, myUuid);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                friends.add(rs.getString("uuid"));
            }
        } catch (SQLException sqle) {
            throw new userException("exception encontered while fetching non friends" + sqle.getMessage());
        }
        return friends;
    }
}

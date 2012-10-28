/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package friends;

import com.mysql.jdbc.PreparedStatement;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javanb.userpackage.userException;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import sqlManager.friendsTableManager;

/**
 *
 * @author srivid
 */
public class friends {

    private String uuid;
    private final int numTopFriends;
    private final int numTopViewed;
    private JSONArray allFriendsFromDB = null;
    private JSONArray topViewedFromDB = null;
    private String sortBasis = "viewCount";

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public friends(String myUuid) throws userException {
        this.setUuid(myUuid);
        Properties prop = new Properties();
        FileInputStream fis;
        try {
            fis = new FileInputStream("/Users/srivid/myProjects/myWorld/repository/JavaNB/src/friends/friendsConfig-properties.xml");
            prop.loadFromXML(fis);
            this.numTopFriends = Integer.parseInt(prop.getProperty("numTopFriends"));
            this.numTopViewed = Integer.parseInt(prop.getProperty("numTopViewed"));
        } catch (InvalidPropertiesFormatException ex) {
            throw new userException("error reading properties file:" + ex.getMessage());
        } catch (FileNotFoundException ex) {
            throw new userException("error reading properties file:" + ex.getMessage());
        } catch (IOException ex) {
            throw new userException("error reading properties file:" + ex.getMessage());
        }
    }

    public String[] getAllFriends() throws userException {
        String[] friends = {};
        if(this.allFriendsFromDB == null) {
            this.fetchAllFriends();
        }
        for (int i = 0, max = allFriendsFromDB.size(); i < max; i++) {
            friends[i] = (String) allFriendsFromDB.get(i);
        }
        return friends;
    }
    
    public void reload() throws userException {
        this.fetchAllFriends();
        this.fetchAllViewed();
    }
    
    private void fetchAllFriends() throws userException {
        try {
            friendsTableManager sqlManager = new friendsTableManager();
            allFriendsFromDB = sqlManager.getFriends(uuid, "all");
        } catch (FileNotFoundException ex) {
            throw new userException("some exception occured while fetching friends:" + ex.getMessage());
        } catch (IOException ex) {
            throw new userException("some exception occured while fetching friends:" + ex.getMessage());
        } catch (SQLException ex) {
            throw new userException("some exception occured while fetching friends:" + ex.getMessage());
        }
    }

    private void fetchAllViewed() throws userException {
        try {
            friendsTableManager sqlManager = new friendsTableManager();
            topViewedFromDB = sqlManager.getFriends(uuid, "top");
        } catch (FileNotFoundException ex) {
            throw new userException("some exception occured while fetching friends:" + ex.getMessage());
        } catch (IOException ex) {
            throw new userException("some exception occured while fetching friends:" + ex.getMessage());
        } catch (SQLException ex) {
            throw new userException("some exception occured while fetching friends:" + ex.getMessage());
        }
    }

    //API to get all users whom the current user has viewed
    public String[] getTopViewed() throws userException {
        String[] friends = {};
        if(this.topViewedFromDB == null) {
            this.fetchAllViewed();
        }
        this.sortBasis = "viewCount";
        this.sort("top");
        for (int i = topViewedFromDB.size() - 1, max = topViewedFromDB.size(); max - i >= this.numTopViewed; i--) {
            friends[i] = topViewedFromDB.getJSONObject(i).getString("uuid");
        }
        return friends;
    }

    public String[] getRecentlyViewed() throws userException {
        String[] friends = {};
        this.sortBasis = "recentlyViewed";
        if(this.topViewedFromDB == null) {
            this.fetchAllViewed();
        }
        this.sort("top");
        for (int i = topViewedFromDB.size() - 1, max = topViewedFromDB.size(); max - i >= this.numTopViewed; i--) {
            friends[i] = topViewedFromDB.getJSONObject(i).getString("uuid");
        }
        return friends;
    }

    //to get top friends of a user
    public String[] getTopfriends() throws userException {
        String[] friends = {};
        HashMap friendsMap = new HashMap(this.numTopFriends);
        if(this.allFriendsFromDB == null) {
            this.fetchAllFriends();
        }
        //sort based on viewCount first
        this.sortBasis = "viewCount";
        this.sort("all");
        int index=0;
        for (int i = allFriendsFromDB.size() - 1, max = allFriendsFromDB.size(); max - i >= this.numTopFriends/2; i--) {
            String uuid = allFriendsFromDB.getJSONObject(i).getString("uuid");
            friendsMap.put(uuid, true);
            friends[index++] = uuid;
        }
        this.sortBasis = "recentlyViewed";
        this.sort("all");
        for(int i = allFriendsFromDB.size();i-->=0;) {
            String uuid = allFriendsFromDB.getJSONObject(i).getString("uuid");
            if(friendsMap.size() == this.numTopFriends) {
                break;
            }
            if(!friendsMap.containsKey(uuid)) {
                friendsMap.put(uuid, true);
                friends[index++] = uuid;
            }
        }
        return friends;
    }

    private void sort(String type) throws userException {
        JSONArray friendsJSONArray = new JSONArray();
        //sort all friends
        if (type.equals("all")) {
            friendsJSONArray = this.allFriendsFromDB;
        } else if (type.equals("top")) {
            friendsJSONArray = this.topViewedFromDB;
        } else {
            throw new userException("invalid type passed:" + type);
        }
        int low = 0, high = friendsJSONArray.size() - 1;
        this.qsort(friendsJSONArray, low, high);
    }

    private void qsort(JSONArray friendsJSONArray, int low, int high) {
        if (low < high) {
            int partition = this.partition(friendsJSONArray, low, high);
            qsort(friendsJSONArray, low, partition - 1);
            qsort(friendsJSONArray, partition + 1, high);
        }
    }

    private int partition(JSONArray friendsJSONArray, int i, int j) {
        int pivot = i;
        while (i < j) {
            JSONObject jsoni = friendsJSONArray.getJSONObject(i);
            JSONObject jsonj = friendsJSONArray.getJSONObject(j);
            JSONObject jsonp = friendsJSONArray.getJSONObject(pivot);
            if (this.sortBasis.equals("viewCount")) {
                while ((jsonp.getInt("auxViewCount") > jsoni.getInt("auxViewCount"))
                        || (jsonp.getInt("viewCount") > jsoni.getInt("viewCount") && jsonp.getInt("auxViewCount") == jsoni.getInt("auxViewCount"))) {
                    i++;
                    jsoni = friendsJSONArray.getJSONObject(i);
                }
                while ((jsonp.getInt("auxViewCount") < jsonj.getInt("auxViewCount"))
                        || (jsonp.getInt("viewCount") < jsonj.getInt("viewCount") && jsonp.getInt("auxViewCount") == jsonj.getInt("auxViewCount"))) {
                    j--;
                    jsonj = friendsJSONArray.getJSONObject(j);
                }
            } else if (this.sortBasis.equals("recentlyViewed")) {
                while ((jsonp.getInt("timestamp") > jsoni.getInt("timestamp"))) {
                    i++;
                    jsoni = friendsJSONArray.getJSONObject(i);
                }
                while ((jsonp.getInt("timestamp") < jsonj.getInt("timestamp"))) {
                    j--;
                    jsonj = friendsJSONArray.getJSONObject(j);
                }
            }
            if (i < j) {
                JSONObject temp = jsoni;
                jsoni = jsonj;
                jsonj = temp;
            } else {
                JSONObject temp = jsonj;
                jsonj = jsonp;
                jsonp = temp;
            }
        }
        return j;
    }
    
    public String[] getMutualFriends(String uuid1, String uuid2) throws userException {
        String[] friends = {};
        try {
            friendsTableManager sqlManager = new friendsTableManager();
            friends = sqlManager.getMutualFriends(uuid1, uuid2);
        } catch (FileNotFoundException ex) {
            throw new userException("some exception occured while fetching mutual friends:" + ex.getMessage());
        } catch (IOException ex) {
            throw new userException("some exception occured while fetching mutual friends:" + ex.getMessage());
        } catch (SQLException ex) {
            throw new userException("some exception occured while fetching mutual friends:" + ex.getMessage());
        }
        
        return friends;
    }
}

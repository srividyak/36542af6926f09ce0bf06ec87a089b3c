/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package friends;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javanb.basicEducationData;
import javanb.basicUserData;
import javanb.trie;
import javanb.trieNode;
import javanb.userpackage.multipleObjectsHandler;
import javanb.userpackage.user;
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
    private String sortBasis = "viewCount";
    private boolean trieFlag = false;

    private class networkBlob {

        public String uuid;
        public int childCount = 0;
        public ArrayList<networkBlob> children;
        public networkBlob parent;

        public networkBlob(String id) {
            this.uuid = id;
            this.children = null;
            this.parent = null;
        }

        public void addChild(networkBlob child) {
            if (this.childCount == 0) {
                children = new ArrayList<networkBlob>(1);
            }
            children.add(child);
            this.childCount++;
        }

        public void setParent(networkBlob parentNode) {
            this.parent = parentNode;
        }
    }

    protected networkBlob myNetwork = null;
    
    protected trie firstnameCommonTrie = null;
    protected trie middlenameCommonTrie = null;
    protected trie lastnameCommonTrie = null;

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
            this.numTopFriends = Integer.parseInt(prop.getProperty("numOfTopFriends"));
            this.numTopViewed = Integer.parseInt(prop.getProperty("numOfTopViewed"));
        } catch (InvalidPropertiesFormatException ex) {
            throw new userException("error reading properties file:" + ex.getMessage());
        } catch (FileNotFoundException ex) {
            throw new userException("error reading properties file:" + ex.getMessage());
        } catch (IOException ex) {
            throw new userException("error reading properties file:" + ex.getMessage());
        }
    }

    public ArrayList<String> getAllFriends() throws userException {
        JSONArray allFriendsFromDB = this.fetchAllFriends();
        ArrayList<String> friends = new ArrayList<String>();
        for (int i = 0, max = allFriendsFromDB.size(); i < max; i++) {
            friends.add((allFriendsFromDB.getJSONObject(i)).getString("uuid"));
        }
        return friends;
    }

    private JSONArray fetchAllFriends() throws userException {
        try {
            friendsTableManager sqlManager = new friendsTableManager();
            return sqlManager.getFriends(uuid, "all");
        } catch (FileNotFoundException ex) {
            throw new userException("some exception occured while fetching friends:" + ex.getMessage());
        } catch (IOException ex) {
            throw new userException("some exception occured while fetching friends:" + ex.getMessage());
        } catch (SQLException ex) {
            throw new userException("some exception occured while fetching friends:" + ex.getMessage());
        }
    }

    private JSONArray fetchAllViewed() throws userException {
        try {
            friendsTableManager sqlManager = new friendsTableManager();
            return sqlManager.getFriends(uuid, "top");
        } catch (FileNotFoundException ex) {
            throw new userException("some exception occured while fetching friends:" + ex.getMessage());
        } catch (IOException ex) {
            throw new userException("some exception occured while fetching friends:" + ex.getMessage());
        } catch (SQLException ex) {
            throw new userException("some exception occured while fetching friends:" + ex.getMessage());
        }
    }

    //API to get all users whom the current user has viewed
    public ArrayList<String> getTopViewed() throws userException {
        JSONArray topViewedFromDB = this.fetchAllViewed();
        this.sortBasis = "viewCount";
        topViewedFromDB = this.sort(topViewedFromDB);
        ArrayList<String> friends = new ArrayList<String>();
        for (int i = topViewedFromDB.size() - 1; (i--) > 0;) {
            friends.add(topViewedFromDB.getJSONObject(i).getString("uuid"));
        }
        return friends;
    }

    public ArrayList<String> getRecentlyViewed() throws userException {
        this.sortBasis = "recentlyViewed";
        JSONArray topViewedFromDB = this.fetchAllViewed();
        topViewedFromDB = this.sort(topViewedFromDB);
        ArrayList<String> friends = new ArrayList<String>();
        for (int i = 0, max = topViewedFromDB.size(); i < max; i++) {
            friends.add(topViewedFromDB.getJSONObject(i).getString("uuid"));
        }
        return friends;
    }

    //to get top friends of a user
    public ArrayList<String> getTopfriends() throws userException {
        ArrayList<String> friends = new ArrayList<String>();
        HashMap friendsMap = new HashMap(this.numTopFriends);
        JSONArray allFriendsFromDB = this.fetchAllFriends();
        //sort based on viewCount first
        this.sortBasis = "viewCount";
        allFriendsFromDB = this.sort(allFriendsFromDB);
        for (int i = allFriendsFromDB.size() - 1, max = allFriendsFromDB.size(); max - i >= this.numTopFriends / 2; i--) {
            String uuid = allFriendsFromDB.getJSONObject(i).getString("uuid");
            friendsMap.put(uuid, true);
            friends.add(uuid);
        }
        this.sortBasis = "recentlyViewed";
        allFriendsFromDB = this.sort(allFriendsFromDB);
        for (int i = allFriendsFromDB.size(); i-- >= 0;) {
            String uuid = allFriendsFromDB.getJSONObject(i).getString("uuid");
            if (friendsMap.size() == this.numTopFriends) {
                break;
            }
            if (!friendsMap.containsKey(uuid)) {
                friendsMap.put(uuid, true);
                friends.add(uuid);
            }
        }
        return friends;
    }

    /*
     * does a quickSort on friendsJSONArray
     */
    private JSONArray sort(JSONArray friendsJSONArray) throws userException {
        //sort all friends
        int low = 0, high = friendsJSONArray.size() - 1;
        this.qsort(friendsJSONArray, low, high);
        return friendsJSONArray;
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
        int size = friendsJSONArray.size();
        while (i < j) {
            JSONObject jsoni = friendsJSONArray.getJSONObject(i);
            JSONObject jsonj = friendsJSONArray.getJSONObject(j);
            JSONObject jsonp = friendsJSONArray.getJSONObject(pivot);
            if (this.sortBasis.equals("viewCount")) {
                while ((jsonp.getLong("auxViewCount") > jsoni.getLong("auxViewCount"))
                        || (jsonp.getLong("viewCount") >= jsoni.getLong("viewCount") && jsonp.getLong("auxViewCount") == jsoni.getLong("auxViewCount"))) {
                    if (i < size - 1) {
                        i++;
                    } else {
                        break;
                    }
                    jsoni = friendsJSONArray.getJSONObject(i);
                }
                while ((jsonp.getLong("auxViewCount") < jsonj.getLong("auxViewCount"))
                        || (jsonp.getLong("viewCount") < jsonj.getLong("viewCount") && jsonp.getLong("auxViewCount") == jsonj.getLong("auxViewCount"))) {
                    if (j > 0) {
                        j--;
                    } else {
                        break;
                    }
                    jsonj = friendsJSONArray.getJSONObject(j);
                }
            } else if (this.sortBasis.equals("recentlyViewed")) {
                while ((jsonp.getLong("timestamp") >= jsoni.getLong("timestamp"))) {
                    if (i < size - 1) {
                        i++;
                    } else {
                        break;
                    }
                    jsoni = friendsJSONArray.getJSONObject(i);
                }
                while ((jsonp.getLong("timestamp") < jsonj.getLong("timestamp"))) {
                    if (j > 0) {
                        j--;
                    } else {
                        break;
                    }
                    jsonj = friendsJSONArray.getJSONObject(j);
                }
            } else if (this.sortBasis.equals("friendsCount")) {
                while ((jsonp.getInt("friendsCount") >= jsoni.getInt("friendsCount"))) {
                    if (i < size - 1) {
                        i++;
                    } else {
                        break;
                    }
                    jsoni = friendsJSONArray.getJSONObject(i);
                }
                while ((jsonp.getInt("friendsCount") < jsonj.getInt("friendsCount"))) {
                    if (j > 0) {
                        j--;
                    } else {
                        break;
                    }
                    jsonj = friendsJSONArray.getJSONObject(j);
                }
            }
            if (i < j) {
                JSONObject temp = jsoni;
                friendsJSONArray.set(i, friendsJSONArray.get(j));
                friendsJSONArray.set(j, temp);
            } else {
                JSONObject temp = jsonj;
                friendsJSONArray.set(j, friendsJSONArray.get(pivot));
                friendsJSONArray.set(pivot, temp);
            }
        }
        return j;
    }

    /*
     * gets mutual friends with a friend of uuid - friendUuid @param friendUuid
     * - uuid of friend returns arraylist of uuids of mutual friends
     */
    public ArrayList<String> getMutualFriends(String friendUuid) throws userException {
        return this.chooseFromFriends(friendUuid, "mutualFriends");
    }

    public ArrayList<String> chooseFromFriends(String friend, String typeOfFriends) throws userException {
        ArrayList<String> friends = new ArrayList<String>();
        if (!typeOfFriends.equals("nonFriends") && !typeOfFriends.equals("mutualFriends")) {
            throw new userException("invalid type of friends mentioned");
        }
        try {
            friendsTableManager sqlManager = new friendsTableManager();
            JSONArray myFriends = this.fetchAllFriends();
            JSONArray friendFriends = sqlManager.getFriends(friend, "all");
            //create a hashtable for myfriends with key as uuid and value being 1
            Hashtable<String, Integer> myFriendsHashtable = new Hashtable<String, Integer>();
            for (int i = 0, max = myFriends.size(); i < max; i++) {
                myFriendsHashtable.put(myFriends.getJSONObject(i).getString("uuid"), 1);
            }
            for (int i = 0, max = friendFriends.size(); i < max; i++) {
                String uuid = friendFriends.getJSONObject(i).getString("uuid");
                if (!typeOfFriends.equals("nonFriends")) {
                    if (!myFriendsHashtable.containsKey(uuid)) {
                        friends.add(uuid);
                    }
                } else if (!typeOfFriends.equals("mutualFriends")) {
                    if (myFriendsHashtable.containsKey(uuid)) {
                        friends.add(uuid);
                    }
                }
            }
        } catch (FileNotFoundException ex) {
            throw new userException("some exception occured while fetching friend's friends:" + ex.getMessage());
        } catch (IOException ex) {
            throw new userException("some exception occured while fetching friend's friends:" + ex.getMessage());
        } catch (SQLException ex) {
            throw new userException("some exception occured while fetching friend's friends:" + ex.getMessage());
        }
        return friends;
    }

    /*
     * gets all friends of friend of uuid who are not friends of uuid @param
     * friend - uuid of the friend returns arrayList of uuids of friends fetch
     * myFriends and friend's friends and compare both to determine non friends
     */
    public ArrayList<String> getNonFriends(String friend) throws userException {
        return this.chooseFromFriends(friend, "nonFriends");
    }

    private boolean isPresent(String friend, JSONArray allFriends) {
        for (int i = allFriends.size(); (i--) > 0;) {
            String friendUuid = allFriends.getJSONObject(i).getString("uuid");
            if (friendUuid.equals(friend)) {
                return true;
            }
        }
        return false;
    }

    //algo to go upto 2nd level max
    public int getLevel(String friend) throws userException {
        int level = 0;
        String tempUuid = this.uuid;
        JSONArray allFriends = this.fetchAllFriends();
        if (this.isPresent(friend, allFriends)) {
            return level;
        }
        ArrayList<String> firstLevelUuids = new ArrayList<String>();
        for (int i = 0, max = allFriends.size(); i < max; i++) {
            firstLevelUuids.add(allFriends.getJSONObject(i).getString("uuid"));
        }
        String[] fields = {"friendsCount"};
        JSONArray friendsCount = multipleObjectsHandler.getUserFields(fields, firstLevelUuids);
        for (int i = 0, max = allFriends.size(); i < max; i++) {
            JSONObject friendObj = allFriends.getJSONObject(i);
            friendObj.put("friendsCount", friendsCount.getJSONObject(i).getString("friendsCount"));
        }
        this.sortBasis = "friendsCount";
        this.sort(allFriends);
        for (int i = allFriends.size(); i-- > 0;) {
            this.uuid = allFriends.getJSONObject(i).getString("uuid");
            JSONArray nextLevelFriends = this.fetchAllFriends();
            if (this.isPresent(friend, nextLevelFriends)) {
                this.uuid = tempUuid; //restore the uuid
                return ++level;
            }
        }
        this.uuid = tempUuid; //restore the uuid
        return -1;
    }

    private void attachChildrenToNetwork(JSONArray nextLevelFriends, networkBlob curNode) throws userException {
        for (int i = 0, max = nextLevelFriends.size(); i < max; i++) {
            user me = new user(this.uuid);
            me.fetchEntity();
            String[] fields = {"friendsCount"};
            JSONObject friend = nextLevelFriends.getJSONObject(i);
            friend.put("friendsCount", me.getFriendsCount());
        }
        this.sortBasis = "friendsCount";
        this.sort(nextLevelFriends);
        for (int i = nextLevelFriends.size(); (i--) > 0;) {
            networkBlob child = new networkBlob(nextLevelFriends.getJSONObject(i).getString("uuid"));
            child.parent = curNode;
            curNode.addChild(child);
        }
    }

    //Loads an n-ary tree with uuid as root, at 2nd level : all friends of uuid, 
    //@ 3rd level: friends of 2nd level but non friends of uuid
    protected void loadFriendNetwork() throws userException {
        if (this.myNetwork == null) {
            this.myNetwork = new networkBlob(uuid);
            networkBlob curNode = this.myNetwork;
            JSONArray nextLevelFriends = this.fetchAllFriends();
            this.attachChildrenToNetwork(nextLevelFriends, curNode);
            nextLevelFriends = new JSONArray();
            networkBlob parent = curNode;
            for (int i = 0, max = parent.childCount; i < max; i++) {
                curNode = parent.children.get(i);
                ArrayList<String> nonFriends = this.getNonFriends(curNode.uuid);
                for (int j = 0, maxj = nonFriends.size(); j < maxj; j++) {
                    JSONObject nonFriendJSONObject = new JSONObject();
                    nonFriendJSONObject.put("uuid", nonFriends.get(i));
                    nextLevelFriends.add(nonFriendJSONObject);
                    this.attachChildrenToNetwork(nextLevelFriends, curNode);
                }
            }
        }
    }

    public JSONObject searchFriendsCommon(String name) throws userException {
        JSONObject friends = new JSONObject();
        if (!trieFlag) {
            this.firstnameCommonTrie = new trie();
            this.lastnameCommonTrie = new trie();
            this.middlenameCommonTrie = new trie();
            ArrayList<String> topViewedFriends = this.getTopViewed();
            ArrayList<user> users = multipleObjectsHandler.getUsers(topViewedFriends);
            for (int i = 0, max = users.size(); i < max; i++) {
                user me = users.get(i);
                this.firstnameCommonTrie.addName(me.getFirstName(), topViewedFriends.get(i));
                this.lastnameCommonTrie.addName(me.getLastName(), topViewedFriends.get(i));
                this.middlenameCommonTrie.addName(me.getMiddleName(), topViewedFriends.get(i));
            }
            this.trieFlag = true;
        }
        trieNode firstnameTrieNode = this.firstnameCommonTrie.searchName(name);
        trieNode lastnameTrieNode = this.lastnameCommonTrie.searchName(name);
        trieNode middlenameTrieNode = this.middlenameCommonTrie.searchName(name);
        if(firstnameTrieNode != null) {
            HashMap<String,Object> firstnameHashMap = this.firstnameCommonTrie.autocomplete(name, firstnameTrieNode);
            JSONArray firstnameArray = new JSONArray();
            for(Map.Entry<String,Object> entry : firstnameHashMap.entrySet()) {
                JSONObject firstnameJSONObject = new JSONObject();
                firstnameJSONObject.put(entry.getKey(), entry.getValue());
                firstnameArray.add(firstnameJSONObject);
            }
            friends.put("firstName", firstnameArray);
        }
        
        if(lastnameTrieNode != null) {
            HashMap<String,Object> lastnameHashMap = this.lastnameCommonTrie.autocomplete(name, lastnameTrieNode);
            JSONArray lastnameArray = new JSONArray();
            for(Map.Entry<String,Object> entry : lastnameHashMap.entrySet()) {
                JSONObject lastnameJSONObject = new JSONObject();
                lastnameJSONObject.put(entry.getKey(), entry.getValue());
                lastnameArray.add(lastnameJSONObject);
            }
            friends.put("lastName", lastnameArray);
        }
        
        if(middlenameTrieNode != null) {
            HashMap<String,Object> middlenameHashMap = this.middlenameCommonTrie.autocomplete(name, middlenameTrieNode);
            JSONArray middlenameArray = new JSONArray();
            for(Map.Entry<String,Object> entry : middlenameHashMap.entrySet()) {
                JSONObject middlenameJSONObject = new JSONObject();
                middlenameJSONObject.put(entry.getKey(), entry.getValue());
                middlenameArray.add(middlenameJSONObject);
            }
            friends.put("middleName", middlenameArray);
        }
        return friends;
    }

    /**
     * API to get all friends with same company/educational institute. For now
     * these 2 fields are supported.
     *
     * @param uuid - uuid for whom friends are to be associated
     * @param field - company/education
     * @return
     */
    public static ArrayList<String> getFriendsWithSameField(final String uuid, String field) throws userException {
        try {
            ArrayList<String> friends = new ArrayList<String>();

            Callable allfriendsCallable = new Callable() {

                @Override
                public Object call() throws userException {
                    friends friends = new friends(uuid);
                    return friends.getAllFriends();
                }
            };

            Callable fetchUserCallable = new Callable() {

                @Override
                public Object call() throws userException {
                    user me = new user(uuid);
                    me.fetchEntity();
                    return me;
                }
            };

            ExecutorService executor = Executors.newFixedThreadPool(2);
            ArrayList<Callable<Object>> list = new ArrayList<Callable<Object>>();
            list.add(allfriendsCallable);
            list.add(fetchUserCallable);

            ArrayList<Future<Object>> futureList = new ArrayList<Future<Object>>();
            futureList = (ArrayList<Future<Object>>) executor.invokeAll(list);

            ArrayList<String> myFriends = (ArrayList<String>) futureList.get(0).get();
            user me = (user) futureList.get(1).get();
            ArrayList<user> friendsDetails = multipleObjectsHandler.getUsers(myFriends);
            if ("education".equals(field)) {
                String myEdu = ((basicEducationData) (me.getEducationUserdata().get(0))).getName();
                for (user friend : friendsDetails) {
                    String eduId = ((basicEducationData) (me.getEducationUserdata().get(0))).getName();
                    if (eduId.equals(myEdu)) {
                        friends.add(eduId);
                    }
                }
            } else if ("company".equals(field)) {
                String myCompany = ((basicUserData) (me.getEducationUserdata().get(0))).getName();
                for (user friend : friendsDetails) {
                    String compId = ((basicUserData) (me.getEducationUserdata().get(0))).getName();
                    if (compId.equals(myCompany)) {
                        friends.add(compId);
                    }
                }
            }
            return friends;
        } catch (ExecutionException ex) {
            throw new userException("some exception occured while excuting in thread:" + ex.getMessage());
        } catch (InterruptedException ex) {
            throw new userException("some exception occured while excuting in thread:" + ex.getMessage());
        }
    }
}

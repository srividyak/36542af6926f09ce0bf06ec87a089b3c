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

    private class friendsTrie {

        public char alphabet;
        public int childCount = 0;
        public ArrayList<friendsTrie> children = null;
        public boolean end = false;
        public String uuid = null;
        private int childPointer = 0;

        public friendsTrie(char name) {
            this.alphabet = name;
        }

        public void setEnd(String friend) {
            this.end = true;
            this.uuid = friend;
        }

        public void addChild(friendsTrie child) {
            if (this.childCount == 0) {
                this.children = new ArrayList<friendsTrie>();
            }
            this.children.add(child);
            this.childCount++;
        }

        //use this API when there is a need to iterate through the trie. Eg: while doing BFS/DFS
        public friendsTrie getNextChild() {
            if (this.childPointer < this.childCount) {
                return this.children.get(this.childPointer++);
            }
            return null;
        }

        public void resetChildPtr() {
            this.childPointer = 0;
        }
    }
    protected networkBlob myNetwork = null;
    protected friendsTrie firstNameTrie = null;
    protected friendsTrie lastNameTrie = null;
    protected friendsTrie middleNameTrie = null;

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
                if (!typeOfFriends.equals("nonFriends"))  {
                    if (!myFriendsHashtable.containsKey(uuid)) {
                        friends.add(uuid);
                    }
                } else if (!typeOfFriends.equals("mutualFriends"))  {
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
            String[] fields = {"friendsCount"};
            JSONObject friend = nextLevelFriends.getJSONObject(i);
            JSONObject friendsCountObj = me.getFields(fields);
            friend.put("friendsCount", friendsCountObj.getInt("friendsCount"));
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

    /*
     * API to load all 3 types of trie. Uses top viewed friends as the parameter
     * uses multiple users to fetch bunch of users data in one call
     */
    public void loadTries() throws userException {
        this.firstNameTrie = new friendsTrie('#');
        this.lastNameTrie = new friendsTrie('#');
        this.middleNameTrie = new friendsTrie('#');
        System.out.println("before fetch:" + new Date().getTime());
        ArrayList<String> topViewedFriends = this.getTopViewed();
        System.out.println("after fetching friends uuid:" + new Date().getTime());
        ArrayList<user> users = multipleObjectsHandler.getUsers(topViewedFriends);
        System.out.println("after fetching users:" + new Date().getTime());
        for (int i = 0, max = users.size(); i < max; i++) {
            user me = users.get(i);
            this.addNameIntoFriendsTrie(me.getFirstName(), topViewedFriends.get(i), "firstName");
            this.addNameIntoFriendsTrie(me.getLastName(), topViewedFriends.get(i), "lastName");
            this.addNameIntoFriendsTrie(me.getMiddleName(), topViewedFriends.get(i), "middleName");
        }
        this.trieFlag = true;
    }

    /*
     * populates namesArray with the matched names. When a partial name has been
     * matched, this API can be used to auto complete the search results @param
     * namesArray - this JSONArray is populated with matched results at the end
     * of the function @param prefix - partially matched string @param root -
     * the trie in which search is to be performed
     */
    private void populateSearchFriendsObject(JSONArray namesArray, String prefix, friendsTrie root) {
        ArrayList<friendsTrie> stack = new ArrayList<friendsTrie>();
        stack.add(root);
        String suffix = "";
        while (stack.size() != 0) {
            friendsTrie curNode = (stack.get(stack.size() - 1)).getNextChild();
            if (curNode != null) {
                suffix += curNode.alphabet;
                if (curNode.end) {
                    JSONObject nameObject = new JSONObject();
                    nameObject.put("name", prefix + suffix);
                    nameObject.put("uuid", curNode.uuid);
                    namesArray.add(nameObject);
                }
                stack.add(curNode);
            } else {
                if (suffix.length() > 0) {
                    suffix = suffix.substring(0, suffix.length() - 1);
                }
                //this resets the childpointer so that the entire trie is reusable after one pass
                stack.get(stack.size() - 1).resetChildPtr();
                stack.remove(stack.size() - 1);
            }
        }
    }

    /*
     * API to parse a trie - does a DFS and prints out the contents of the trie.
     * This is only for test @param trie - the trie to parse
     */
    public void parse(friendsTrie trie) {
        ArrayList<friendsTrie> stack = new ArrayList<friendsTrie>();
        stack.add(trie);
        String name = "";
        while (stack.size() != 0) {
            friendsTrie curNode = (stack.get(stack.size() - 1)).getNextChild();
            if (curNode != null) {
                name += curNode.alphabet;
                if (curNode.end) {
                    System.out.println(name);
                }
                stack.add(curNode);
            } else {
                if (name.length() > 0) {
                    name = name.substring(0, name.length() - 1);
                }
                stack.get(stack.size() - 1).resetChildPtr();
                stack.remove(stack.size() - 1);
            }
        }
    }

    /*
     * API to parse first name
     */
    public void parseFirstNameTrie() throws userException {
        this.loadTries();
        this.parse(this.firstNameTrie);
    }

    /*
     * This API can be called to search for firstName,lastname and middlename
     * starting with "name" @param name - initial characters to be searched for
     * in trie This loads the trie just once. Hence same function can be called
     * multiple times without fetching data repeatedly
     */
    public JSONObject searchFriends(String name) throws userException {
        JSONObject friends = new JSONObject();
        friends.put("firstName", new JSONArray());
        friends.put("lastName", new JSONArray());
        friends.put("middleName", new JSONArray());
        if (!this.trieFlag) {
            this.loadTries();
        }
        friendsTrie firstNameSearchNode = this.searchTrie(name, this.firstNameTrie);
        friendsTrie lastNameSearchNode = this.searchTrie(name, this.lastNameTrie);
        friendsTrie middleNameSearchNode = this.searchTrie(name, this.middleNameTrie);
        ArrayList<friendsTrie> stack = new ArrayList<friendsTrie>();
        if (firstNameSearchNode != null) {
            JSONArray firstNameJSONArray = friends.getJSONArray("firstName");
            this.populateSearchFriendsObject(firstNameJSONArray, name, firstNameSearchNode);
        }
        if (lastNameSearchNode != null) {
            JSONArray lastNameJSONArray = friends.getJSONArray("lastName");
            this.populateSearchFriendsObject(lastNameJSONArray, name, lastNameSearchNode);
        }
        if (middleNameSearchNode != null) {
            JSONArray middleNameJSONArray = friends.getJSONArray("middleName");
            this.populateSearchFriendsObject(middleNameJSONArray, name, middleNameSearchNode);
        }
        return friends;
    }

    /*
     * This API searches for name in thr trie. @param - name - name to be
     * searched @param root - trie in which search is to be performed returns
     * the pointer in the trie where the last character of name matched
     */
    private friendsTrie searchTrie(String name, friendsTrie root) {
        friendsTrie ptr = root;
        for (int i = 0, max = name.length(); i < max; i++) {
            boolean found = false;
            for (int j = 0, maxj = ptr.childCount; j < maxj; j++) {
                if (ptr.children.get(j).alphabet == name.charAt(i)) {
                    found = true;
                    ptr = ptr.children.get(j);
                    break;
                }
            }
            if (!found) {
                if (i != 0) {
                    return ptr;
                } else {
                    return null;
                }
            }
        }
        return ptr;
    }

    /*
     * API to populate the trie. Populates the trie with uuid in case end is
     * reached @param name - name to be populated @param friendUuid - uuid of
     * the user to be inserted at end @param nameType - indicates if name is
     * first/last/middle name
     *
     */
    private void addNameIntoFriendsTrie(String name, String friendUuid, String nameType) throws userException {
        friendsTrie temp = null;
        if (nameType.equals("firstName")) {
            temp = this.firstNameTrie;
        } else if (nameType.equals("lastName")) {
            temp = this.lastNameTrie;
        } else if (nameType.equals("middleName")) {
            temp = this.middleNameTrie;
        } else {
            throw new userException("Invalid nameType passed");
        }
        for (int i = 0, max = name.length(); i < max; i++) {
            int childCount = temp.childCount;
            boolean found = false;
            for (int j = 0; j < childCount; j++) {
                if (temp.children.get(j).alphabet == name.charAt(i)) {
                    found = true;
                    temp = temp.children.get(j);
                    break;
                }
            }
            if (!found) {
                temp.addChild(new friendsTrie(name.charAt(i)));
                temp = temp.children.get(temp.childCount - 1);
            }
            if (i == max - 1) {
                temp.setEnd(friendUuid);
            }
        }
    }
}

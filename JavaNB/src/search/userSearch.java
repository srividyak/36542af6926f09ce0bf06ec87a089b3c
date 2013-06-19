/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package search;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javanb.trie;
import javanb.trieNode;
import javanb.userpackage.user;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import sqlManager.userDbManager;

/**
 *
 * @author srivid
 */
public class userSearch {

    public static JSONObject searchAllUsersByName(String prefix) {
        JSONObject searchresult = new JSONObject();
        long time = (new Date()).getTime();
        userDbManager sqlDbManager = new userDbManager();
        System.out.println("time taken for db connection initialization=" + ((new Date()).getTime() - time));
        time = (new Date()).getTime();
        ArrayList<user> users = sqlDbManager.getMultipleUsers();
        System.out.println("time taken for db fetch=" + ((new Date()).getTime() - time));
        time = (new Date()).getTime();

        trie<user> searchTrie = new trie<user>();
        for (user user : users) {
            searchTrie.addName(user.getFirstName().toLowerCase(), user);
            searchTrie.addName(user.getLastName().toLowerCase(), user);
            if (!user.getMiddleName().equals("")) {
                searchTrie.addName(user.getMiddleName().toLowerCase(), user);
            }
        }
        trieNode trieNode = searchTrie.searchName(prefix.toLowerCase());
        if (trieNode != null) {
            HashMap<String, ArrayList<user>> searchHash = searchTrie.autocomplete(prefix.toLowerCase(), trieNode);
            for (Map.Entry<String, ArrayList<user>> entry : searchHash.entrySet()) {
                ArrayList<user> searchedUsers = entry.getValue();
                JSONArray value = new JSONArray();
                for (user searchedUser : searchedUsers) {
                    value.add(searchedUser.getUserDetails());
                }
                searchresult.put(entry.getKey(), value);
            }
        } else {
            return null;
        }
        System.out.println("time taken for search=" + ((new Date()).getTime() - time));
        return searchresult;
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package search;

import java.util.ArrayList;
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

    public static JSONObject searchAllUsers(String prefix) {
        JSONObject userInfo = new JSONObject();
        userDbManager sqlDbManager = new userDbManager();
        ArrayList<user> users = sqlDbManager.getMultipleUsers();

        ArrayList<String> firstNames = new ArrayList<String>();
        ArrayList<String> lastNames = new ArrayList<String>();
        ArrayList<String> middleNames = new ArrayList<String>();

        trie firstNameTrie, lastNameTrie, middleNameTrie;
        trieNode firstNameTrieNode, lastNameTrieNode, middleNameTrieNode;
        
        HashMap<String, Object> firstNameHashmap = new HashMap<String, Object>(),
                lastNameHashmap = new HashMap<String, Object>(),
                middleNameHashmap = new HashMap<String, Object>();

        for (user user : users) {
            firstNameHashmap.put(user.getFirstName(), user);
            lastNameHashmap.put(user.getLastName(), user);
            middleNameHashmap.put(user.getMiddleName(), user);
        }
        
        firstNameTrie = new trie(firstNameHashmap);
        lastNameTrie = new trie(lastNameHashmap);
        middleNameTrie = new trie(middleNameHashmap);
        
        firstNameTrieNode = firstNameTrie.searchName(prefix);
        lastNameTrieNode = lastNameTrie.searchName(prefix);
        middleNameTrieNode = middleNameTrie.searchName(prefix);
        
        if(firstNameTrieNode != null) {
            firstNameHashmap = firstNameTrie.autocomplete(prefix, firstNameTrieNode);
            JSONArray firstnameArray = new JSONArray();
            for(Map.Entry<String,Object> entry : firstNameHashmap.entrySet()) {
                JSONObject firstnameJSONObject = new JSONObject();
                user newuser = (user) entry.getValue();
                firstnameJSONObject.put(entry.getKey(), newuser.getUserDetails().toString());
                firstnameArray.add(firstnameJSONObject);
            }
            userInfo.put("firstName", firstnameArray);
        }
        
        if(lastNameTrieNode != null) {
            lastNameHashmap = lastNameTrie.autocomplete(prefix, lastNameTrieNode);
            JSONArray lastnameArray = new JSONArray();
            for(Map.Entry<String,Object> entry : firstNameHashmap.entrySet()) {
                JSONObject firstnameJSONObject = new JSONObject();
                user newuser = (user) entry.getValue();
                firstnameJSONObject.put(entry.getKey(), newuser.getUserDetails().toString());
                lastnameArray.add(firstnameJSONObject);
            }
            userInfo.put("lastName", lastnameArray);
        }
        
        if(middleNameTrieNode != null) {
            middleNameHashmap = middleNameTrie.autocomplete(prefix, middleNameTrieNode);
            JSONArray middlenameArray = new JSONArray();
            for(Map.Entry<String,Object> entry : firstNameHashmap.entrySet()) {
                JSONObject firstnameJSONObject = new JSONObject();
                user newuser = (user) entry.getValue();
                firstnameJSONObject.put(entry.getKey(), newuser.getUserDetails().toString());
                middlenameArray.add(firstnameJSONObject);
            }
            userInfo.put("middleName", middlenameArray);
        }
        return userInfo;
    }
    
}

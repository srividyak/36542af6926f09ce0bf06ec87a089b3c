/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javanb.locationpackage.location;
import javanb.trie;
import javanb.trieNode;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import sqlManager.locationTableManager;

/**
 *
 * @author srivid
 */
public class locationSearch {

    public static JSONObject searchAllLocationsByName(String prefix) {
        JSONObject resultLocations = new JSONObject();
        locationTableManager sqlManager = new locationTableManager();
        ArrayList<location> allLocations = sqlManager.getAllLocations();
        trie<location> locationTrie = new trie<location>();

        for (location location : allLocations) {
            String names[] = {location.getName(), location.getStateName(), location.getCountryName()};
            for (String name : names) {
                String nameParts[] = name.split(" ");
                for (String eachName : nameParts) {
                    locationTrie.addName(eachName.toLowerCase(), location);
                }
            }
        }
        
        //search for prefix
        trieNode locationTrieNode = locationTrie.searchName(prefix.toLowerCase());
        if(locationTrieNode == null) {
            return null;
        }
        HashMap<String,ArrayList<location>> locationHashMap = locationTrie.autocomplete(prefix.toLowerCase(), locationTrieNode);
        
        for (Map.Entry<String, ArrayList<location>> entry : locationHashMap.entrySet()) {
            allLocations = entry.getValue();
            JSONArray value = new JSONArray();
            for(location loc : allLocations) {
                value.add(loc.toJson());
            }
            resultLocations.put(entry.getKey(), value);
        }
        return resultLocations;
    }
}

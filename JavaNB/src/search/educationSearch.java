/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javanb.educationpackage.education;
import javanb.trie;
import javanb.trieNode;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import sqlManager.educationTableManager;

/**
 *
 * @author srivid
 */
public class educationSearch {

    private static String[] stopWords = {"college", "medical", "arts", "art", "medicine", "technology", "institute", "institution", "science", "commerce", "bachelor", "master"};
    private static HashMap<String, Boolean> stopWordsHashMap = null;

    private static HashMap<String, Boolean> getStopWordsHashMap() {
        if (stopWordsHashMap == null) {
            stopWordsHashMap = new HashMap<String, Boolean>();
            for (String word : stopWords) {
                stopWordsHashMap.put(word, Boolean.TRUE);
            }
        }
        return stopWordsHashMap;
    }

    public static JSONObject searchAllEducationByName(String prefix) {
        JSONObject resultEducation = new JSONObject();
        educationTableManager sqlManager = new educationTableManager();
        ArrayList<education> allEducation = sqlManager.getAllEducation();
        trie<education> educationTrie = new trie<education>();
        stopWordsHashMap = getStopWordsHashMap();
        for (education e : allEducation) {
            String name = e.getName();
            String[] nameParts = name.split(" ");
            for (String eachName : nameParts) {
                eachName = eachName.toLowerCase();
                if (!stopWordsHashMap.containsKey(eachName)) {
                    educationTrie.addName(eachName, e);
                }
            }
        }

        trieNode eduTrieNode = educationTrie.searchName(prefix.toLowerCase());
        if (eduTrieNode == null) {
            return null;
        }

        HashMap<String, ArrayList<education>> educationHashMap = educationTrie.autocomplete(prefix.toLowerCase(), eduTrieNode);
        for (Map.Entry<String, ArrayList<education>> entry : educationHashMap.entrySet()) {
            allEducation = entry.getValue();
            JSONArray value = new JSONArray();
            for (education e : allEducation) {
                value.add(e.toJson());
            }
            resultEducation.put(entry.getKey(), value);
        }
        return resultEducation;
    }
}

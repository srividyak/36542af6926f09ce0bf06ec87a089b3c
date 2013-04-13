/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javanb.companypackage.company;
import javanb.trie;
import javanb.trieNode;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import sqlManager.companyTableManager;

/**
 *
 * @author srivid
 */
public class companySearch {

    public static JSONObject searchAllCompaniesByName(String prefix) {
        JSONObject resultCompanies = new JSONObject();
        companyTableManager sqlManager = new companyTableManager();
        ArrayList<company> companies = sqlManager.getAllCompanies();
        trie<company> companyTrie = new trie<company>();
        
        //populating tries
        for (company company : companies) {
            String name = company.getName();
            String nameParts[] = name.split(" ");
            for (String eachName : nameParts) {
                companyTrie.addName(eachName.toLowerCase(), company);
            }
        }
        
        //search for prefix
        trieNode companyTrieNode = companyTrie.searchName(prefix.toLowerCase());
        if(companyTrieNode == null) {
            return null;
        }
        HashMap<String, ArrayList<company>> companySearchHashMap = companyTrie.autocomplete(prefix.toLowerCase(), companyTrieNode);
        for (Map.Entry<String, ArrayList<company>> entry : companySearchHashMap.entrySet()) {
            ArrayList<company> searchedCompanies = entry.getValue();
            JSONArray value = new JSONArray();
            for(company eachCompany : searchedCompanies) {
                value.add(eachCompany.toJson());
            }
            resultCompanies.put(entry.getKey(), value);
        }
        return resultCompanies;
    }
}

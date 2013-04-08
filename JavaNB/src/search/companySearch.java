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
import sqlManager.companyTableManager;

/**
 *
 * @author srivid
 */
public class companySearch {

    public static ArrayList<company> searchAllCompanies(String prefix) {
        ArrayList<company> resultCompanies = new ArrayList<company>();
        companyTableManager sqlManager = new companyTableManager();
        ArrayList<company> companies = sqlManager.getAllCompanies();
        trie companyTrie = new trie();
        
        //populating tries
        for (company company : companies) {
            String name = company.getName();
            String nameParts[] = name.split(" ");
            for (String eachName : nameParts) {
                companyTrie.addName(eachName, company);
            }
        }
        
        //search for prefix
        trieNode companyTrieNode = companyTrie.searchName(prefix);
        HashMap<String,Object> companySearchHashMap = companyTrie.autocomplete(prefix, companyTrieNode);
        for(Map.Entry<String,Object> entry : companySearchHashMap.entrySet()) {
            resultCompanies.add((company)entry.getValue());
        }
        return resultCompanies;
    }
}

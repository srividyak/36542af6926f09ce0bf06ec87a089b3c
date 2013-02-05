/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package miscellaneous;

import http.linkHandler;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javanb.userpackage.userException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author srivid
 */
public class misc {

    public static JSONObject jsonObj;
    public static HashSet stopWords;
    
    public misc() {
        
    }

    public static void parseJSON() throws userException {
        try {
            JSONParser parser = new JSONParser();
            jsonObj = (JSONObject) parser.parse(new FileReader("/Users/srivid/myProjects/myWorld/repository/JavaNB/src/miscellaneous/stopWords.json"));
        } catch (IOException ex) {
            throw new userException("Error reading stopWords json file:" + ex.getMessage());
        } catch (ParseException ex) {
            Logger.getLogger(misc.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static boolean isStopWord(String searchWord) throws userException {
        if(jsonObj == null) {
            parseJSON();
        }
        if(stopWords == null) {
            stopWords = new HashSet();
            JSONArray arr = (JSONArray) jsonObj.get("stopWords");
            for(int i=0,max=arr.size();i<max;i++) {
                stopWords.add(arr.get(i));
            }
        }
        if(stopWords.contains(searchWord.toLowerCase())) {
            return true;
        }
        return false;
    }
}

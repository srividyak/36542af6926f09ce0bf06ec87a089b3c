/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sqlManager;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author srivid
 */
public class tablesMetaDataHandler {
    static JSONObject allTables;
    
    public static String[] getTableInfo(String tableName) {
        if(allTables == null) {
            try {
                JSONParser parser = new JSONParser();
                allTables = (JSONObject) parser.parse(new FileReader("/Users/srivid/myProjects/myWorld/repository/JavaNB/src/sqlManager/tables.json"));
            } catch (IOException ex) {
                Logger.getLogger(tablesMetaDataHandler.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            } catch (ParseException ex) {
                Logger.getLogger(tablesMetaDataHandler.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
        }
        if(allTables.containsKey(tableName)) {
            JSONArray fields = (JSONArray) allTables.get(tableName);
            String[] columns = new String[fields.size()];
            for(int i=0,max=fields.size();i<max;i++) {
                columns[i] = (String) fields.get(i);
            }
            return columns;
        }
        return null;
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package javanb.companypackage;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import javanb.userpackage.userException;
import net.sf.json.JSONObject;
import sqlManager.companyTableManager;

/**
 *
 * @author srivid
 */
public class company {
    String id;
    String name;

    public String getName() {
        return name;
    }
    String description = "";
    String[] keywords;
    
    public company(JSONObject compData) throws FileNotFoundException, IOException, SQLException, userException {
        if(!compData.containsKey("id")) {
            compData.put("id",company.generateId(compData.getString("name")));
        }
        companyTableManager sqlManager = new companyTableManager(compData);
        this.name = compData.getString("name");
        this.id = compData.getString("id");
        this.description = compData.getString("description");
        this.extractKeys();
    }
    
    public company() {
        
    }
    
    public company(String id) {
        new companyTableManager(id, this);
        this.extractKeys();
    }

    public String getDescription() {
        return description;
    }

    public String getId() {
        return id;
    }

    public String[] getKeywords() {
        return keywords;
    }

    //TODO - extract keys from description
    private void extractKeys() {
        
    }
    
    public static String generateId(String name) {
        return name.trim().toLowerCase().replace(' ', '_');
    }
}

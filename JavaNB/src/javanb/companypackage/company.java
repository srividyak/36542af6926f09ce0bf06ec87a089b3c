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
    String description = "";
    String[] keywords;
    
    public company(JSONObject compData) throws FileNotFoundException, IOException, SQLException, userException {
        if(!compData.containsKey("id")) {
            compData.put("id",company.generateId(compData.getString("name")));
        }
        companyTableManager sqlManager = new companyTableManager(compData);
        JSONObject companyDetails = sqlManager.getCompanyDetails();
        this.name = companyDetails.getString("name");
        this.id = companyDetails.getString("id");
        this.description = companyDetails.getString("description");
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

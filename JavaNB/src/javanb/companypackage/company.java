/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package javanb.companypackage;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import net.sf.json.JSONObject;
import sqlManager.companyTableManager;

/**
 *
 * @author srivid
 */
public class company {
    String id;
    String description;
    String[] keywords;
    
    public company(String compId) throws FileNotFoundException, IOException, SQLException {
        companyTableManager sqlManager = new companyTableManager(compId);
        JSONObject companyDetails = sqlManager.getCompanyDetails();
        this.id = companyDetails.getString("id");
        this.description = companyDetails.getString("description");
        this.extractKeys();
    }
    
    public company(String compId, String description) throws FileNotFoundException, IOException, SQLException {
        companyTableManager sqlManager = new companyTableManager(compId, description);
        JSONObject companyDetails = sqlManager.getCompanyDetails();
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
}

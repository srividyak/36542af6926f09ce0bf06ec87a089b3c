/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package javanb.locationpackage;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import net.sf.json.JSONObject;
import sqlManager.locationTableManager;

/**
 *
 * @author srivid
 */
public class location {
    private String id;
    private String description = "";
    private String[] keywords;
    
    private void extractKeys() {
    }

    public location(String locId) throws FileNotFoundException, IOException, SQLException {
        locationTableManager sqlManager = new locationTableManager(locId);
        JSONObject locationDetails = sqlManager.getLocationDetails();
        this.description = locationDetails.getString("description");
        this.id = locationDetails.getString("id");
        this.extractKeys();
    }
    
    public location(String locId, String description) throws FileNotFoundException, IOException, SQLException {
        this.id = locId;
        locationTableManager sqlManager = new locationTableManager(locId,description);
        JSONObject locationDetails = sqlManager.getLocationDetails();
        this.description = locationDetails.getString("description");
        this.id = locationDetails.getString("id");
        this.extractKeys();
    }
    
    public String getDescription() {
        return description;
    }

    public String[] getKeywords() {
        return keywords;
    }

    public String getId() {
        return id;
    }
    
}

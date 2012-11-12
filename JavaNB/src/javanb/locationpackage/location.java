/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package javanb.locationpackage;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import javanb.userpackage.userException;
import net.sf.json.JSONObject;
import sqlManager.locationTableManager;

/**
 *
 * @author srivid
 */
public class location {
    private String id;
    private String name;
    private String description = "";
    private String stateId;
    private String stateName;
    private String countryId;
    private String countryName;

    public String getCountryId() {
        return countryId;
    }

    public String getCountryName() {
        return countryName;
    }

    public String getStateId() {
        return stateId;
    }

    public String getStateName() {
        return stateName;
    }
    private String[] keywords;
    
    private void extractKeys() {
    }

    public location(JSONObject locData) throws FileNotFoundException, IOException, SQLException, userException {
        if(!locData.containsKey("id")) {
            locData.put("id", location.generateId(locData.getString("name")));
        }
        if(!locData.containsKey("stateId") && locData.containsKey("stateName")) {
            locData.put("stateId", location.generateId(locData.getString("stateName")));
        }
        if(!locData.containsKey("countryId") && locData.containsKey("countryName")) {
            locData.put("countryId", location.generateId(locData.getString("countryName")));
        }
        locationTableManager sqlManager = new locationTableManager(locData);
        JSONObject locationDetails = sqlManager.getLocationDetails();
        this.name = locationDetails.getString("name");
        this.id = locationDetails.getString("id");
        this.description = locationDetails.getString("description");
        this.countryId = locationDetails.getString("countryId");
        this.countryName = locationDetails.getString("countryName");
        this.stateId = locationDetails.getString("stateId");
        this.stateName = locationDetails.getString("stateName");
        this.extractKeys();
    }
    
    //for search use case
    public location(String name) {
    }

    public String getName() {
        return name;
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
    
    public static String generateId(String name) {
        return name.trim().toLowerCase().replace(' ', '_');
    }
    
}

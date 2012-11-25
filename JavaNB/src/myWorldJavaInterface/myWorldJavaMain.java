/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package myWorldJavaInterface;

import friends.friends;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javanb.basicUserData;
import javanb.companypackage.company;
import javanb.educationpackage.education;
import javanb.locationpackage.location;
import javanb.userpackage.user;
import javanb.userpackage.userException;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

/**
 *
 * @author srivid
 */
public class myWorldJavaMain {
    public JSONObject getUser(String uuid) throws userException {
        user existingUser = new user();
        existingUser.fetchUser(uuid);
        return existingUser.getUserDetails();
    }
    
    protected void updateLocationsTable(JSONArray locations) throws userException {
        for(int i=0,max=locations.size();i<max;i++) {
            try {
                location newLoc = new location(locations.getJSONObject(i));
            } catch(Exception e) {
                throw new userException("unable to add or get specified location" + e.getMessage());
            }
        }
    
    }
    
    protected void updateCompaniesTable(JSONArray companies) throws userException {
        for(int i=0,max=companies.size();i<max;i++) {
            try {
                company newComp = new company(companies.getJSONObject(i));
            } catch(Exception e) {
                throw new userException("unable to add or get specified company" + e.getMessage());
            }
        }
    }
    
    protected void updateEducationTable(JSONArray educationJSON) throws userException {
        for(int i=0,max=educationJSON.size();i<max;i++) {
            try {
                education newEdu = new education(educationJSON.getJSONObject(i));
            } catch(Exception e) {
                throw new userException("unable to add or get specified educational institute" + e.getMessage());
            }
        }
    }
    
    public void createUser(JSONObject userInfo) throws userException, ParseException {
        //TODO: remove all info from hometown and locations field except for the name
        JSONArray locations = new JSONArray();
        JSONArray companies = new JSONArray();
        JSONArray educationJSON = new JSONArray();
        if(userInfo.containsKey("hometown")) {
            JSONObject homeTown = userInfo.getJSONObject("hometown");
            locations.add(homeTown);
        }
        
        if(userInfo.containsKey("locations")) {
            JSONArray locArray = userInfo.getJSONArray("locations");
            for(int i=0,max=locArray.size();i<max;i++) {
                locations.add(locArray.get(i));
            }
        }
        
        if(userInfo.containsKey("companies")) {
            companies = userInfo.getJSONArray("companies");
        }
        
        if(userInfo.containsKey("educations")) {
            educationJSON = userInfo.getJSONArray("educations");
        }
        user newUser = new user();
        newUser.insertUser(userInfo);
        
        //update location of the user. Here create a new user object since the calls to create new user and to update locations are parallel. Threading needs to be implemented for this
        this.updateLocationsTable(locations);
        this.updateCompaniesTable(companies);
        this.updateEducationTable(educationJSON);
    }
    
    public void updateUser(String uuid, JSONObject userDetails) throws ParseException, FileNotFoundException, IOException, SQLException, userException {
        Iterator keys = userDetails.keys();
        while(keys.hasNext()) {
            String key = (String) keys.next();
            if(key.equals("gender")) {
                user.updateGender(uuid, userDetails.getBoolean(key));
            } else if(key.equals("dob")) {
                user.updateDob(uuid, userDetails.getString(key));
            } else {
                if(key.equals("educations")) {
                    this.updateEducationTable(userDetails.getJSONArray("educations"));
                }
                if(key.equals("companies")) {
                    this.updateCompaniesTable(userDetails.getJSONArray("companies"));
                }
                if(key.equals("locations")) {
                    this.updateLocationsTable(userDetails.getJSONArray("locations"));
                }
                user.updateStringField(uuid, key, userDetails.getString(key));
            }
        }
    }
    
    public void sendRequest(String myUuid, String friendUuid) throws userException {
        user myUser;
        myUser = new user();
        myUser.fetchUser(myUuid);
        myUser.sendRequest(friendUuid);
    }
    
    public void acceptRequest(String myUuid, String friendUuid) throws userException {
        user myUser;
        myUser = new user();
        myUser.fetchUser(myUuid);
        myUser.acceptRequest(friendUuid);
    }
    
    public void getAllFriends(String uuid) throws userException {
        friends friends = new friends("06524f6541f83b817bf2f793a0a4ae04");
        
    }
    
}

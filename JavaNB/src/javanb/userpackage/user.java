/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package javanb.userpackage;

import entity.entity;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javanb.basicEducationData;
import javanb.basicUserData;
import javanb.locationpackage.location;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import org.apache.commons.codec.digest.DigestUtils;
import sqlManager.friendsTableManager;
import sqlManager.userDbManager;
import sqlManager.userTableManager;

/**
 *
 * @author srivid
 */
public class user extends entity {

    private String firstName;
    private String middleName;
    private String lastName;
    private String dob;
    private String interests;
    private String phoneNum;
    private boolean gender;
    private char relStatus;
    private String lang;
    private String about;
    private String homeTown;
    private String email;
    private long timestamp;
    private int friendsCount;
    /*
     * arraylist
     */
    private ArrayList<basicUserData> locationsUserdata;
    private ArrayList<basicEducationData> educationUserdata;
    private ArrayList<basicUserData> companyUserdata;
    private String locations;
    private String education;
    private String company;

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp.longValue();
    }

    public String getEmail() {
        return email;
    }

    public int getFriendsCount() {
        return friendsCount;
    }

    public void setFriendsCount(int friendsCount) {
        this.friendsCount = friendsCount;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public user() {
    }

    /*
     * Provides this constructor to accept an existing uuid. This can be used to
     * update/set fields in user table
     */
    public user(String uuidString) {
        this.setUuid(uuidString);
    }

    @Override
    public void fetchEntity() throws userException {
        new userDbManager(uuid, this);
    }

    public String insertUser(JSONObject userInfo) throws userException {
        try {
            this.initUser(userInfo);
            this.setUuid(this.generateUUID());
            userInfo.put("uuid", this.getUuid());
            new userDbManager(userInfo);
            return this.getUuid();
        } catch (ParseException ex) {
            Logger.getLogger(user.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    //API to get userDetails as a JSONObject
    public JSONObject getUserDetails() {
        JSONObject userDetails = new JSONObject();
        userDetails.put("firstName", this.firstName);
        userDetails.put("middleName", this.middleName);
        userDetails.put("lastName", this.lastName);
        userDetails.put("dob", this.dob);
        userDetails.put("interests", this.interests);
        userDetails.put("phoneNum", this.phoneNum);
        userDetails.put("gender", this.gender);
        userDetails.put("relStatus", this.relStatus);
        userDetails.put("lang", this.lang);
        userDetails.put("about", this.about);
        userDetails.put("hometown", this.homeTown);
        userDetails.put("locations", this.locations);
        userDetails.put("educations", this.education);
        userDetails.put("companies", this.company);
        userDetails.put("uuid", this.uuid);
        userDetails.put("email", this.email);
        userDetails.put("timestamp", this.timestamp);
        return userDetails;
    }

    /*
     * API which takes in a jsonobj of user details and initializes the user
     * object
     */
    public void loadUser(JSONObject userInfo) throws userException {
        try {
            this.initUser(userInfo);
        } catch (userException ex) {
            throw new userException("error occured during initializing user:" + ex.getMessage());
        } catch (ParseException ex) {
            throw new userException("error occured during initializing user:" + ex.getMessage());
        }
    }

    //init all details in local variables
    private void initUser(JSONObject userInfo) throws userException, ParseException {
        //update the user's locations with id so that only id is stored in the users table
        if (userInfo.containsKey("homeTown")) {
            JSONObject homeTown = (JSONObject) JSONSerializer.toJSON(userInfo.getString("homeTown"));
            //put only id and get rid of all other fields
            if (homeTown.containsKey("name")) {
                homeTown.put("id", location.generateId(homeTown.getString("name")));
            } else if (homeTown.containsKey("id")) {
                homeTown.put("id", homeTown.getString("id"));
            }
            homeTown.remove("name");
            homeTown.remove("stateName");
            homeTown.remove("stateId");
            homeTown.remove("countryId");
            homeTown.remove("countryName");
        } else {
            userInfo.put("homeTown", "{}");
        }
        if (userInfo.containsKey("locations")) {
            JSONArray locations = userInfo.getJSONArray("locations");
            for (int i = 0, max = locations.size(); i < max; i++) {
                JSONObject loc = locations.getJSONObject(i);
                if (loc.containsKey("name")) {
                    loc.put("id", location.generateId(loc.getString("name")));
                } else if (loc.containsKey("id")) {
                    loc.put("id", loc.getString("id"));
                }
                //get rid of all other fields
                loc.remove("name");
                loc.remove("stateName");
                loc.remove("stateId");
                loc.remove("countryId");
                loc.remove("countryName");
            }
        } else {
            userInfo.put("locations", "[]");
        }
        //update user's company with id
        if (userInfo.containsKey("company")) {
            JSONArray companies = userInfo.getJSONArray("companies");
            for (int i = 0, max = companies.size(); i < max; i++) {
                JSONObject comp = companies.getJSONObject(i);
                if (comp.containsKey("name")) {
                    comp.put("id", location.generateId(comp.getString("name")));
                } else if (comp.containsKey("id")) {
                    comp.put("id", comp.getString("id"));
                }
                //get rid of all other fields
                comp.remove("name");
            }
        } else {
            userInfo.put("company", "[]");
        }
        //update user education with id
        if (userInfo.containsKey("education")) {
            JSONArray educations = userInfo.getJSONArray("educations");
            for (int i = 0, max = educations.size(); i < max; i++) {
                JSONObject edu = educations.getJSONObject(i);
                if (edu.containsKey("name")) {
                    edu.put("id", location.generateId(edu.getString("name")));
                } else if (edu.containsKey("id")) {
                    edu.put("id", edu.getString("id"));
                }
                //get rid of name
                edu.remove("name");
            }
        } else {
            userInfo.put("education", "[]");
        }

        if (!userInfo.containsKey("firstName")) {
            throw new userException("firstName is mandatory");
        } else if (!userInfo.containsKey("lastName")) {
            throw new userException("LastName is mandatory");
        } else if (!userInfo.containsKey("dob")) {
            throw new userException("DOB is mandatory");
        } else if (!userInfo.containsKey("gender")) {
            throw new userException("Gender is mandatory");
        } else if (!userInfo.containsKey("email")) {
            throw new userException("email is mandatory");
        }
        this.setFirstName(userInfo.getString("firstName"));
        this.setLastName(userInfo.getString("lastName"));
        this.setDob(userInfo.getString("dob"));
        this.setTimestamp((new Date()).getTime());
        userInfo.put("timestamp", this.getTimestamp());
        String gender = userInfo.getString("gender");
        if (gender.equalsIgnoreCase("1")) {
            this.setGender(true);
        } else {
            this.setGender(false);
        }
        this.setEmail(userInfo.getString("email"));

        this.setAbout(userInfo.containsKey("about") ? userInfo.getString("about") : "");
        this.setMiddleName(userInfo.containsKey("middleName") ? userInfo.getString("middleName") : "");
        this.setInterests(userInfo.containsKey("interests") ? userInfo.getString("interests") : "");
        this.setHomeTown(userInfo.containsKey("homeTown") ? userInfo.getString("homeTown") : "{}");
        this.setLang(userInfo.containsKey("lang") ? userInfo.getString("lang") : "");
        this.setPhoneNum(userInfo.containsKey("phoneNum") ? userInfo.getString("phoneNum") : "");
        this.setLocations(userInfo.containsKey("locations") ? userInfo.getString("locations") : "[]");
        this.setCompany(userInfo.containsKey("companies") ? userInfo.getString("company") : "[]");
        this.setEducation(userInfo.containsKey("educations") ? userInfo.getString("education") : "[]");

        char relStatus = userInfo.containsKey("relStatus") ? userInfo.getString("relStatus").charAt(0) : '1';
        if ((int) relStatus >= (int) ('1') && (int) relStatus <= (int) ('9')) {
            this.setRelStatus(relStatus);
        } else {
            throw new userException("invalid relationship status");
        }

    }

    //generate a unique id for every user
    public String generateUUID() {
        long now = new Date().getTime();
        String currentTime = String.valueOf(now);
        String dobString = String.valueOf(this.getDob());
        String comboString = currentTime + this.getFirstName() + this.getLastName() + dobString;
        return DigestUtils.md5Hex(comboString);
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public void setCompanyUserdata(ArrayList companies) {
        //TODO - check if assigning directly works correctly
        this.companyUserdata = companies;
    }

    public void setCompany(String companyString) throws userException {
        this.company = companyString;
        this.companyUserdata = new ArrayList<basicUserData>();
        if (companyString != null) {
            this.setArrayLists(companyString, this.companyUserdata);
        }
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public void setEducationUserdata(ArrayList education) {
        //TODO - check if assigning directly works correctly
        this.educationUserdata = education;
    }

    public void setEducation(String educationString) throws userException {
        this.education = educationString;
        this.educationUserdata = new ArrayList<basicEducationData>();
        if (educationString != null) {
            this.setbasicEducationData(educationString, this.educationUserdata);
        }
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setGender(boolean gender) {
        this.gender = gender;
    }

    public void setGender(Boolean gender) {
        this.gender = gender.booleanValue();
    }

    public void setHomeTown(String hometown) {
        this.homeTown = hometown;
    }

    public void setInterests(String interests) {
        this.interests = interests;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setLocationsUserdata(ArrayList locations) {
        //TODO - check if assigning directly works correctly
        this.locationsUserdata = locations;
    }

    //company or location should be a JSONArray: each JSONObject of the form: name,startDate,endDate
    private void setArrayLists(String target, ArrayList<basicUserData> targetArrayList) throws userException {
        JSONArray basicJSONArray = (JSONArray) JSONSerializer.toJSON(target);
        for (int i = 0, max = basicJSONArray.size(); i < max; i++) {
            targetArrayList.add(new basicUserData((JSONObject) basicJSONArray.get(i)));
        }
    }

    //education shd be a JSONArray with each element being a JSONObject with keys: type,major,name,startDate,endDate
    private void setbasicEducationData(String target, ArrayList<basicEducationData> targetArrayList) throws userException {
        JSONArray eduJSONArray = (JSONArray) JSONSerializer.toJSON(target);
        for (int i = 0, max = eduJSONArray.size(); i < max; i++) {
            targetArrayList.add(new basicEducationData((JSONObject) eduJSONArray.get(i)));
        }
    }

    /*
     * locations should be of the format:
     * startDate,endDate,place;startDate,endDate,place;....
     */
    public void setLocations(String locations) throws userException {
        this.locations = locations;
        this.locationsUserdata = new ArrayList<basicUserData>();
        if (locations != null) {
            this.setArrayLists(this.locations, this.locationsUserdata);
        }
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public void setRelStatus(char relStatus) {
        this.relStatus = relStatus;
    }

    public String getAbout() {
        return about;
    }

    public ArrayList getCompanyUserdata() {
        return companyUserdata;
    }

    public String getDob() {
        return dob;
    }

    public ArrayList getEducationUserdata() {
        return educationUserdata;
    }

    public String getFirstName() {
        return firstName;
    }

    public boolean getGender() {
        return gender;
    }

    public String getHomeTown() {
        return homeTown;
    }

    public String getInterests() {
        return interests;
    }

    public String getLang() {
        return lang;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public ArrayList getLocationsUserdata() {
        return locationsUserdata;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public char getRelStatus() {
        return relStatus;
    }

    public void sendRequest(String friendUuid) throws userException {
        try {
            friendsTableManager sqlManager = new friendsTableManager();
            sqlManager.sendRequest(uuid, friendUuid);
        } catch (FileNotFoundException ex) {
            throw new userException("some exception occured during sending friend request:" + ex.getMessage());
        } catch (IOException ex) {
            throw new userException("some exception occured during sending friend request:" + ex.getMessage());
        } catch (SQLException ex) {
            throw new userException("some exception occured during sending friend request:" + ex.getMessage());
        }
    }

    public void acceptRequest(String friendUuid) throws userException {
        try {
            friendsTableManager sqlManager = new friendsTableManager();
            sqlManager.acceptRequest(uuid, friendUuid);
        } catch (FileNotFoundException ex) {
            throw new userException("some exception occured during sending friend request:" + ex.getMessage());
        } catch (IOException ex) {
            throw new userException("some exception occured during sending friend request:" + ex.getMessage());
        } catch (SQLException ex) {
            throw new userException("some exception occured during sending friend request:" + ex.getMessage());
        }
    }

    public void updateUser(JSONObject userDetails) throws userException {
        if (this.uuid == null) {
            if (!userDetails.containsKey("uuid")) {
                throw new userException("You are trying to update user without uuid. Update user of particular uuid");
            } else {
                this.setUuid(userDetails.getString("uuid"));
            }
        }
        if (userDetails.containsKey("timestamp")) {
            userDetails.remove("timestamp");
        }
        if (userDetails.containsKey("uuid")) {
            userDetails.remove("uuid");
        }
        userDbManager sqlManager = new userDbManager();
        sqlManager.updateUser(uuid, userDetails);
    }

    public void deleteUser() throws userException {
        try {
            userTableManager sqlManager = new userTableManager();
            JSONObject obj = new JSONObject();
            obj.put("disabled", "1");
            sqlManager.updateUser(uuid, obj);
        } catch (FileNotFoundException ex) {
            throw new userException("error while deleting/disabling user:" + ex.getMessage());
        } catch (IOException ex) {
            throw new userException("error while deleting/disabling user:" + ex.getMessage());
        } catch (SQLException ex) {
            throw new userException("error while deleting/disabling user:" + ex.getMessage());
        }
    }
}

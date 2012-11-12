/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package javanb.userpackage;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javanb.basicEducationData;
import javanb.basicUserData;
import javanb.companypackage.company;
import javanb.educationpackage.education;
import javanb.locationpackage.location;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import sqlManager.userTableManager;
import org.apache.commons.codec.digest.DigestUtils;
import sqlManager.friendsTableManager;

/**
 *
 * @author srivid
 */
public class user {

    private String uuid;
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
    private String hometown;
    private String email;
    private long timestamp;

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    /*
     * arraylist
     */
    private ArrayList<basicUserData> locations;
    private ArrayList<basicEducationData> educations;
    private ArrayList<basicUserData> companies;
    private String locationString;
    private String educationString;
    private String companyString;

    public user() {
    }

    //Constructor to get an existing user (getUser by uuid)
    public user(String _uuidString) throws userException {
        try {
            userTableManager tableMgr = new userTableManager(_uuidString);
            JSONObject userDetails = tableMgr.getUserDetails();
            this.setUuid(_uuidString);
            String userInfoString = userDetails.toString();
            this.initUser((JSONObject) JSONSerializer.toJSON(userInfoString));
        } catch (ParseException ex) {
            throw new userException("error while finding user with uuid:"+_uuidString+" :" +ex.getMessage());
        } catch (FileNotFoundException ex) {
            throw new userException("error while finding user with uuid:"+_uuidString+" :" +ex.getMessage());
        } catch (IOException ex) {
            throw new userException("error while finding user with uuid:"+_uuidString+" :" +ex.getMessage());
        } catch (SQLException ex) {
            throw new userException("error while finding user with uuid:"+_uuidString+" :" +ex.getMessage());
        }
    }

    //Contrustor for adding new user into db (createUser)
    public user(JSONObject userInfo) throws userException, ParseException {
        //serialize userInfo and then pass to initUser since we dont want original object to be manipulated
        String userInfoString = userInfo.toString();
        this.initUser((JSONObject) JSONSerializer.toJSON(userInfoString));
        this.setUuid(this.generateUUID());
        userInfo.put("uuid", this.getUuid());
        try {
            userTableManager newUser = new userTableManager(userInfo);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(user.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(user.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(user.class.getName()).log(Level.SEVERE, null, ex);
        }
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
        userDetails.put("hometown", this.hometown);
        userDetails.put("locations", this.locationString);
        userDetails.put("educations", this.educationString);
        userDetails.put("companies", this.companyString);
        userDetails.put("uuid", this.uuid);
        userDetails.put("email", this.email);
        userDetails.put("timestamp", this.timestamp);
        return userDetails;
    }
    
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
        if (userInfo.containsKey("hometown")) {
            JSONObject hometown = (JSONObject) JSONSerializer.toJSON(userInfo.getString("hometown"));
            //put only id and get rid of all other fields
            if (hometown.containsKey("name")) {
                hometown.put("id", location.generateId(hometown.getString("name")));
            } else if (hometown.containsKey("id")) {
                hometown.put("id", hometown.getString("id"));
            }
            hometown.remove("name");
            hometown.remove("stateName");
            hometown.remove("stateId");
            hometown.remove("countryId");
            hometown.remove("countryName");
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
        }
        //update user's company with id
        if (userInfo.containsKey("companies")) {
            JSONArray companies = userInfo.getJSONArray("companies");
            for (int i = 0, max = companies.size(); i < max; i++) {
                JSONObject comp = companies.getJSONObject(i);
                if (comp.containsKey("name")) {
                    comp.put("id", company.generateId(comp.getString("name")));
                } else if (comp.containsKey("id")) {
                    comp.put("id", comp.getString("id"));
                }
                //get rid of all other fields
                comp.remove("name");
            }
        }
        //update user education with id
        if (userInfo.containsKey("educations")) {
            JSONArray educations = userInfo.getJSONArray("educations");
            for (int i = 0, max = educations.size(); i < max; i++) {
                JSONObject edu = educations.getJSONObject(i);
                if (edu.containsKey("name")) {
                    edu.put("id", education.generateId(edu.getString("name")));
                } else if (edu.containsKey("id")) {
                    edu.put("id", edu.getString("id"));
                }
                //get rid of name
                edu.remove("name");
            }
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
        String gender = userInfo.getString("gender");
        if (gender.equalsIgnoreCase("1")) {
            this.setGender(true);
        } else {
            this.setGender(false);
        }
        this.setEmail(userInfo.getString("email"));
        try {
            this.setAbout(userInfo.containsKey("about") ? userInfo.getString("about") : "");
            this.setMiddleName(userInfo.containsKey("middleName") ? userInfo.getString("middleName") : "");
            this.setInterests(userInfo.containsKey("interests") ? userInfo.getString("interests") : "");
            this.setHometown(userInfo.containsKey("hometown") ? userInfo.getString("hometown") : "");
            this.setLang(userInfo.containsKey("lang") ? userInfo.getString("lang") : "");
            this.setPhoneNum(userInfo.containsKey("phoneNum") ? userInfo.getString("phoneNum") : "");
            this.setLocations(userInfo.containsKey("locations") ? userInfo.getString("locations") : "");
            this.setCompany(userInfo.containsKey("companies") ? userInfo.getString("companies") : "");
            this.setEducation(userInfo.containsKey("educations") ? userInfo.getString("educations") : "");

            char relStatus = userInfo.containsKey("relStatus") ? userInfo.getString("relStatus").charAt(0) : '1';
            if ((int) relStatus >= (int) ('1') && (int) relStatus <= (int) ('9')) {
                this.setRelStatus(relStatus);
            } else {
                throw new userException("invalid relationship status");
            }
        } catch (Exception e) {
            System.out.println("Could not find some field");
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

    public void setCompany(ArrayList companies) {
        //TODO - check if assigning directly works correctly
        this.companies = companies;
    }

    public void setCompany(String companyString) throws userException {
        this.companyString = companyString;
        this.companies = new ArrayList<basicUserData>();
        if (companyString != null) {
            this.setArrayLists(companyString, this.companies);
        }
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public void setEducation(ArrayList education) {
        //TODO - check if assigning directly works correctly
        this.educations = education;
    }

    public void setEducation(String educationString) throws userException {
        this.educationString = educationString;
        this.educations = new ArrayList<basicEducationData>();
        if (educationString != null) {
            this.setbasicEducationData(educationString, this.educations);
        }
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setGender(boolean gender) {
        this.gender = gender;
    }

    public void setHometown(String hometown) {
        this.hometown = hometown;
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

    public void setLocations(ArrayList locations) {
        //TODO - check if assigning directly works correctly
        this.locations = locations;
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
        this.locationString = locations;
        this.locations = new ArrayList<basicUserData>();
        if (locations != null) {
            this.setArrayLists(locations, this.locations);
        }
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public void setRelStatus(char relStatus) {
        this.relStatus = relStatus;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getAbout() {
        return about;
    }

    public ArrayList getCompany() {
        return companies;
    }

    public String getDob() {
        return dob;
    }

    public ArrayList getEducation() {
        return educations;
    }

    public String getFirstName() {
        return firstName;
    }

    public boolean getGender() {
        return gender;
    }

    public String getHometown() {
        return hometown;
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

    public ArrayList getLocations() {
        return locations;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public char getRelStatus() {
        return relStatus;
    }

    public String getUuid() {
        return uuid;
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

    public JSONObject getFields(String[] fields) throws userException {
        try {
            userTableManager sqlManager = new userTableManager();
            return sqlManager.getFields(fields, uuid);
        } catch (FileNotFoundException ex) {
            throw new userException("some exception occured while fetching fields" + ex.getMessage());
        } catch (IOException ex) {
            throw new userException("some exception occured while fetching fields" + ex.getMessage());
        } catch (SQLException ex) {
            throw new userException("some exception occured while fetching fields" + ex.getMessage());
        }
    }
    
    public JSONObject getFields(String uuid, String[] fields) throws userException {
        try {
            userTableManager sqlManager = new userTableManager();
            return sqlManager.getFields(fields, uuid);
        } catch (FileNotFoundException ex) {
            throw new userException("some exception occured while fetching fields" + ex.getMessage());
        } catch (IOException ex) {
            throw new userException("some exception occured while fetching fields" + ex.getMessage());
        } catch (SQLException ex) {
            throw new userException("some exception occured while fetching fields" + ex.getMessage());
        }
    }

    //to update any field use static methods
    public static void updateStringField(String uuidString, String field, String value) throws FileNotFoundException, IOException, SQLException {
        field = field.trim();
        userTableManager sqlManager = new userTableManager();
        sqlManager.updateStringField(uuidString, field, value);
    }

    public static void updateDob(String uuid, String date) throws ParseException, FileNotFoundException, IOException, SQLException {
        userTableManager sqlManager = new userTableManager();
        sqlManager.updateDob(uuid, date);
    }

    public static void updateGender(String uuid, boolean gender) throws ParseException, FileNotFoundException, IOException, SQLException {
        userTableManager sqlManager = new userTableManager();
        sqlManager.updateGender(uuid, gender);
    }
}

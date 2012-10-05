/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package javanb.userpackage;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javanb.basicUserData;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import sqlManager.userTableManager;
import org.apache.commons.codec.digest.DigestUtils;

/**
 *
 * @author srivid
 */
public class user {
    private String uuid;
    private String firstName;
    private String lastName;
    private String dob;
    private String interests;
    private String phoneNum;
    private boolean gender;
    private char relStatus;
    private String lang;
    private String about;
    private String hometown;
    
    /*arraylist*/
    private ArrayList<basicUserData> locations;
    private ArrayList<basicUserData> education;
    private ArrayList<basicUserData> company;
    
    private String locationString;
    private String educationString;
    private String companyString;
    
    //Constructor to get an existing user (getUser by uuid)
    public user(String _uuidString) throws FileNotFoundException, SQLException, IOException, userException, ParseException {
        userTableManager tableMgr = new userTableManager(_uuidString);
        JSONObject userDetails = tableMgr.getUserDetails();
        this.setUuid(_uuidString);
        this.initUser(userDetails);
    }
    
    //Contrustor for adding new user into db (createUser)
    public user(JSONObject userInfo) throws userException, ParseException {
        this.initUser(userInfo);
        this.setUuid(this.generateUUID());
        userInfo.put("uuid",this.getUuid());
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
        userDetails.put("firstName",this.firstName);
        userDetails.put("lastName",this.lastName);
        userDetails.put("dob",this.dob);
        userDetails.put("interests",this.interests);
        userDetails.put("phoneNum",this.phoneNum);
        userDetails.put("gender",this.gender);
        userDetails.put("relStatus",this.relStatus);
        userDetails.put("lang",this.lang);
        userDetails.put("about",this.about);
        userDetails.put("hometown",this.hometown);
        userDetails.put("location",this.locationString);
        userDetails.put("education",this.educationString);
        userDetails.put("company",this.companyString);
        userDetails.put("uuid",this.uuid);
        return userDetails;
    }
    
    //init all details in local variables
    private void initUser(JSONObject userInfo) throws userException, ParseException {
        if(userInfo.get("firstName") == null) {
            throw new userException("firstName is mandatory");
        } else if(userInfo.get("lastName") == null) {
            throw new userException("LastName is mandatory");
        } else if(userInfo.get("dob") == null) {
            throw new userException("DOB is mandatory");
        } else if(userInfo.get("gender") == null) {
            throw new userException("Gender is mandatory");
        }
        this.setFirstName(userInfo.getString("firstName"));
        this.setLastName(userInfo.getString("lastName"));
        this.setDob(userInfo.getString("dob"));
        String gender = userInfo.getString("gender");
        if(gender.equalsIgnoreCase("1")) {
            this.setGender(true);
        } else {
            this.setGender(false);
        }
        
        try {
            this.setAbout(userInfo.containsKey("about") ? userInfo.getString("about") : null);
            this.setInterests(userInfo.containsKey("interests") ? userInfo.getString("interests") : null);
            this.setHometown(userInfo.containsKey("homeTown") ? userInfo.getString("homeTown") : null);
            this.setLang(userInfo.containsKey("lang") ? userInfo.getString("lang") : null);
            this.setPhoneNum(userInfo.containsKey("phoneNum") ? userInfo.getString("phoneNum") : null);
            this.setLocations(userInfo.containsKey("locations") ? userInfo.getString("locations") : null);
            this.setCompany(userInfo.containsKey("company") ? userInfo.getString("company") : null);
            this.setEducation(userInfo.containsKey("educations") ? userInfo.getString("education") : null);

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

    public void setCompany(ArrayList company) {
        //TODO - check if assigning directly works correctly
        this.company = company;
    }
    
    public void setCompany(String companyString) throws userException {
        this.companyString = companyString;
        this.company = new ArrayList<basicUserData>();
        if(companyString != null) {
            this.setArrayLists(companyString, this.company);
        }
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public void setEducation(ArrayList education) {
        //TODO - check if assigning directly works correctly
        this.education = education;
    }
    
    public void setEducation(String educationString) throws userException {
        this.educationString = educationString;
        this.education = new ArrayList<basicUserData>();
        if(educationString != null) {
            this.setArrayLists(educationString, this.education);
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
    
    private void setArrayLists(String target, ArrayList<basicUserData> targetArrayList) throws userException {
        String splitLocations[] = target.trim().split(";");
        for(int i=0,maxLoc=splitLocations.length;i<maxLoc;i++) {
            String individualLocation[] = splitLocations[i].trim().split(",");
            int locLength = individualLocation.length;
            if(locLength <=0 || locLength > 3) {
                throw new userException("Invalid location format specified");
            }
            if(locLength == 1) {
                targetArrayList.add(new basicUserData(individualLocation[0]));
            } else if(locLength == 2) {
                targetArrayList.add(new basicUserData(new Date(Long.parseLong(individualLocation[0],10)), individualLocation[1]));
            } else if(locLength == 3) {
                targetArrayList.add(new basicUserData(new Date(Long.parseLong(individualLocation[0],10)), new Date(Long.parseLong(individualLocation[1],10)),individualLocation[2]));
            }
        }
    }
    
    /*
     * locations should be of the format: startDate,endDate,place;startDate,endDate,place;....
     */
    public void setLocations(String locations) throws userException {
        this.locationString = locations;
        this.locations = new ArrayList<basicUserData>();
        if(locations != null) {
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
        return company;
    }

    public String getDob() {
        return dob;
    }

    public ArrayList getEducation() {
        return education;
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

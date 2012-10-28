/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package javanb.educationpackage;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javanb.userpackage.userException;
import net.sf.json.JSONObject;
import sqlManager.educationTableManager;

/**
 *
 * @author srivid
 */
public class education {
    String name;//institute name
    String id;
    String description = "";
    String[] type;
    String[] major;
    String[] keywords;
    
    public education(JSONObject eduDetails) throws userException {
        if (eduDetails.containsKey("name")) {
            if (!eduDetails.containsKey("id")) {
                eduDetails.put("id", education.generateId(eduDetails.getString("name")));
            }
            try {
                if(eduDetails.containsKey("type")) {
                    eduDetails.put("type",eduDetails.getString("type").trim().toLowerCase());
                }
                if(eduDetails.containsKey("major")) {
                    eduDetails.put("major",eduDetails.getString("major").trim().toLowerCase());
                }
                educationTableManager sqlManager = new educationTableManager(eduDetails);
                JSONObject eduData = sqlManager.getEducationDetails();
                this.id = eduData.getString("id");
                this.name = eduData.getString("name");
                this.type = eduData.getString("type").split(",");
                this.major = eduData.getString("major").split(",");
                this.description = eduData.getString("description");
            } catch (SQLException ex) {
                Logger.getLogger(education.class.getName()).log(Level.SEVERE, null, ex);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(education.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(education.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            throw new userException("no name for educational institute provided");
        }
    }

    public String[] getMajor() {
        return major;
    }

    public String[] getType() {
        return type;
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

    public String getName() {
        return name;
    }
    
    public static String generateId(String name) {
        return name.trim().toLowerCase().replace(' ', '_');
    }
    
    private void extractKeys() {
        
    }

}

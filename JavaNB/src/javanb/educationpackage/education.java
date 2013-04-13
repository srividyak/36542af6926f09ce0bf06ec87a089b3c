/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package javanb.educationpackage;

import javanb.userpackage.userException;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
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
    
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("name", this.getName());
        json.put("id", this.getId());
        json.put("description", this.getDescription());
        json.put("type", StringUtils.join(this.getType(), ","));
        json.put("major", StringUtils.join(this.getMajor(), ","));
        return json;
    }

    public education(JSONObject eduDetails) throws userException {
        if (eduDetails.containsKey("name")) {
            if (!eduDetails.containsKey("id")) {
                eduDetails.put("id", education.generateId(eduDetails.getString("name")));
            }
            if (eduDetails.containsKey("type")) {
                eduDetails.put("type", eduDetails.getString("type").trim().toLowerCase());
            }
            if (eduDetails.containsKey("major")) {
                eduDetails.put("major", eduDetails.getString("major").trim().toLowerCase());
            }
            new educationTableManager(eduDetails);
        } else {
            throw new userException("no name for educational institute provided");
        }
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setKeywords(String[] keywords) {
        this.keywords = keywords;
    }

    public void setMajor(String major) {
        this.major = major.split(",");
        for(int i=0;i<this.major.length;i++) {
            this.major[i] = this.major[i].trim();
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
        this.type = type.split(",");
        for(int i=0;i<this.type.length;i++) {
            this.type[i] = this.type[i].trim();
        }
    }
    
    public void setType(String[] type) {
        this.type = type;
    }
    
    public void setMajor(String[] major) {
        this.major = major;
    }

    public education() {
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

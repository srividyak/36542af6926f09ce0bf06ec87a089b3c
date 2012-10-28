/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package javanb;

import java.util.Date;
import net.sf.json.JSONObject;

/**
 *
 * @author srivid
 */
public class basicEducationData {
    private String name;
    private Date startDate = null;
    private Date endDate = null;
    private String type;
    private String major;
    
    public basicEducationData(JSONObject eduObj) {
        if(eduObj.containsKey("id")) {
            this.setName(eduObj.getString("id"));
        }
        if(eduObj.containsKey("major")) {
            this.setMajor(eduObj.getString("major"));
        }
        if(eduObj.containsKey("type")) {
            this.setType(eduObj.getString("type"));
        }
        if(eduObj.containsKey("startDate")) {
            this.setStartDate(new Date(Long.parseLong(eduObj.getString("startDate"),10)));
        }
        if(eduObj.containsKey("endDate")) {
            this.setStartDate(new Date(Long.parseLong(eduObj.getString("endDate"),10)));
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}

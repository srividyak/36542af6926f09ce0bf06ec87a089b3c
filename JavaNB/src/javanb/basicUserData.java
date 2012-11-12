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
public class basicUserData {
    private Date startDate;
    private Date endDate;
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public basicUserData(JSONObject basicData) {
        if(basicData.containsKey("id")) {
            this.setName(basicData.getString("id"));
        }
        if(basicData.containsKey("startDate")) {
            this.setStartDate(new Date(Long.parseLong(basicData.getString("startDate"),10)));
        }
        if(basicData.containsKey("endDate")) {
            this.setEndDate(new Date(Long.parseLong(basicData.getString("endDate"),10)));
        }
    }
    
    public Date getEndDate() {
        return endDate;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }
    
    
}

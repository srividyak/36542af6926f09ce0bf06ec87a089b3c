/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package javanb;

import java.util.Date;

/**
 *
 * @author srivid
 */
public class basicUserData {
    private Date startDate;
    private Date endDate;
    private String id;
    
    public basicUserData(Date start, Date end,String basicId) {
        this.endDate = end;
        this.startDate = start;
        this.id = basicId;
    }
    
    public basicUserData(Date start,String basicId) {
        this.startDate = start;
        this.endDate = new Date();
        this.id = basicId;
    }
    
    public basicUserData(String basicId) {
        this.id = basicId;
        this.startDate = this.endDate = null;
    }

    public Date getEndDate() {
        return endDate;
    }

    public String getId() {
        return id;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }
    
    
}

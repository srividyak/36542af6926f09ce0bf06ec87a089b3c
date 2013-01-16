/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package UGCThreads;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import javanb.userpackage.userException;
import net.sf.json.JSONObject;
import sqlManager.UGCThreads.groupManager;

/**
 *
 * @author srivid
 */
public class group extends groupThread {
    private long numMembers;
    private String[] moderators;
    
    public group() {
        super();
    }
    
    public group(String id) {
        super(id);
    }
    
    @Override
    public void fetchEntity() throws userException {
        try {
            groupManager sqlManager = new groupManager();
            JSONObject result = sqlManager.getGroup(this.getUuid());
            this.initGroup(result);
        } catch (FileNotFoundException ex) {
            throw new userException("error occured while fetching group:" + ex.getMessage());
        } catch (IOException ex) {
            throw new userException("error occured while fetching group:" + ex.getMessage());
        } catch (SQLException ex) {
            throw new userException("error occured while fetching group:" + ex.getMessage());
        }
    }
    
    public void insertGroup(JSONObject groupDetails) throws userException {
        try {
            this.initGroup(groupDetails);
            groupDetails.put("groupId", this.getUuid());
            groupManager sqlManager = new groupManager();
            sqlManager.insertGroup(groupDetails);
        } catch (FileNotFoundException ex) {
            throw new userException("error occured while inserting group:" + ex.getMessage());
        } catch (IOException ex) {
            throw new userException("error occured while inserting group:" + ex.getMessage());
        } catch (SQLException ex) {
            throw new userException("error occured while inserting group:" + ex.getMessage());
        }
    }
    
    public void initGroup(JSONObject groupDetails) throws userException {
        this.initGroupThread(groupDetails);
        if(groupDetails.containsKey("groupId")) {
            this.setUuid(groupDetails.getString("groupId"));
        } else {
            this.generateGroupThreadId("group_");
        }
        if(groupDetails.containsKey("moderators")) {
            this.setModerators(groupDetails.getString("moderators"));
        } else {
            groupDetails.put("moderators", "");
        }
        if(groupDetails.containsKey("numMembers")) {
            this.setNumMembers(groupDetails.getLong("numMembers"));
        } else {
            this.setNumMembers((long)(1 + ((this.moderators != null) ? this.moderators.length : 0)));
        }
    }

    public String[] getModerators() {
        return moderators;
    }

    public void setModerators(String moderatorString) {
        this.moderators = moderatorString.split(",");
        for(String moderator:moderators) {
            moderator = moderator.trim().toLowerCase();
        }
    }

    public long getNumMembers() {
        return numMembers;
    }

    public void setNumMembers(long numMembers) {
        this.numMembers = numMembers;
    }
    
    public void updateGroup() {
        
    }
}

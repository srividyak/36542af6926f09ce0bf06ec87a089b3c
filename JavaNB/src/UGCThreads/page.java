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
import sqlManager.UGCThreads.pageManager;

/**
 *
 * @author srivid
 */
public class page extends groupThread {
    private long shareCount;
    private long upRatingsCount;
    private long downRatingsCount;
    
    public JSONObject toJSON() {
        JSONObject json = super.toJSON();
        json.put("shareCount", shareCount);
        json.put("upRatingsCount", upRatingsCount);
        json.put("downRatingsCount", downRatingsCount);
        return json;
    }
    
    @Override
    public String toString() {
        return this.toJSON().toString();
    }

    public long getDownRatingsCount() {
        return downRatingsCount;
    }

    public void setDownRatingsCount(long downRatingsCount) {
        this.downRatingsCount = downRatingsCount;
    }

    public long getShareCount() {
        return shareCount;
    }

    public void setShareCount(long shareCount) {
        this.shareCount = shareCount;
    }

    public long getUpRatingsCount() {
        return upRatingsCount;
    }

    public void setUpRatingsCount(long upRatingsCount) {
        this.upRatingsCount = upRatingsCount;
    }
    
    public page() {
        super();
    }
    
    public page(String id) {
        super(id);
    }
    
    public void insertPage(JSONObject pageDetails) throws userException {
        try {
            this.initPage(pageDetails);
            pageDetails.put("pageId", this.getUuid());
            pageManager sqlManager = new pageManager();
            sqlManager.insertPage(pageDetails);
        } catch (FileNotFoundException ex) {
            throw new userException("error occured while inserting page:" + ex.getMessage());
        } catch (IOException ex) {
            throw new userException("error occured while inserting page:" + ex.getMessage());
        } catch (SQLException ex) {
            throw new userException("error occured while inserting page:" + ex.getMessage());
        }
    }
    
    @Override
    public void fetchEntity() throws userException {
        try {
            pageManager sqlManager = new pageManager();
            JSONObject result = sqlManager.getPage(this.getUuid());
            this.initPage(result);
        } catch (FileNotFoundException ex) {
            throw new userException("error occured while fetching page:" + ex.getMessage());
        } catch (IOException ex) {
            throw new userException("error occured while fetching page:" + ex.getMessage());
        } catch (SQLException ex) {
            throw new userException("error occured while fetching page:" + ex.getMessage());
        }
    }
    
    public void initPage(JSONObject pageDetails) throws userException {
        this.initGroupThread(pageDetails);
        if(pageDetails.containsKey("pageId")) {
            this.setUuid(pageDetails.getString("pageId"));
        } else {
            this.generateGroupThreadId("page_");
        }
        if(pageDetails.containsKey("shareCount")) {
            this.setShareCount(pageDetails.getLong("shareCount"));
        } else {
            this.setShareCount(0L);
        }
        if(pageDetails.containsKey("upRatingsCount")) {
            this.setUpRatingsCount(pageDetails.getLong("upRatingsCount"));
        } else {
            this.setUpRatingsCount(0L);
        }
        if(pageDetails.containsKey("downRatingsCount")) {
            this.setDownRatingsCount(pageDetails.getLong("downRatingsCount"));
        } else {
            this.setDownRatingsCount(0L);
        }
    }
}

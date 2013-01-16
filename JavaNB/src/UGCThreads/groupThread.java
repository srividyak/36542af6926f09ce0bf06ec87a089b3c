/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package UGCThreads;

import entity.entity;
import java.util.Date;
import java.util.Hashtable;
import javanb.userpackage.userException;
import net.sf.json.JSONObject;
import org.apache.commons.codec.digest.DigestUtils;

/**
 *
 * @author srivid
 */
public class groupThread extends entity {

    protected String owner;
    protected String title;
    protected String description;
    protected String[] tags;
    protected String privacyLevel;
    protected long timestamp;
    protected boolean abuse;
    protected boolean disabled;
    private Hashtable privacyLevelMap;

    public groupThread() {
        this.privacyLevelMap.put("all", "0");
        this.privacyLevelMap.put("membersOnly", "1");
        this.privacyLevelMap.put("ownersOnly", "2");
    }

    public groupThread(String id) {
        this.setUuid(id);
    }

    public boolean isAbuse() {
        return abuse;
    }

    public void setAbuse(boolean abuse) {
        this.abuse = abuse;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getPrivacyLevel() {
        return privacyLevel;
    }

    public void setPrivacyLevel(String privacyLevel) {
        this.privacyLevel = privacyLevel;
    }

    public String[] getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags.split(",");
        for(String tag: this.tags) {
            tag = tag.trim().toLowerCase();
        }
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    protected void generateGroupThreadId(String prefix) {
        this.setUuid(DigestUtils.md5Hex(this.owner + (new Date()).getTime() + this.title + this.description));
    }

    protected void initGroupThread(JSONObject threadDetails) throws userException {
        if (threadDetails.containsKey("groupThreadId")) {
            this.setUuid(threadDetails.getString("groupThreadId"));
        }
        if (threadDetails.containsKey("title")) {
            this.setTitle(threadDetails.getString("title"));
        } else {
            throw new userException("title of a page/group is mandatory");
        }
        if (threadDetails.containsKey("description")) {
            this.setDescription(threadDetails.getString("description"));
        } else {
            this.setDescription("");
        }
        if (threadDetails.containsKey("owner")) {
            this.setOwner(threadDetails.getString("owner"));
        } else {
            throw new userException("owner of page/group is mandatory");
        }
        if (threadDetails.containsKey("privacyLevel")) {
            threadDetails.put("privacyLevel", (String) this.privacyLevelMap.get(threadDetails.getString("privacyLevel")));
            this.setPrivacyLevel(threadDetails.getString("privacyLevel"));
        } else {
            this.setPrivacyLevel("all");
            threadDetails.put("privacyLevel", (String) this.privacyLevelMap.get("all"));
        }
        this.setTimestamp((new Date()).getTime());
        if(threadDetails.containsKey("abuse")) {
            this.setAbuse(threadDetails.getString("abuse").equals("true"));
        } else {
            this.setAbuse(false);
        }
        if(threadDetails.containsKey("disabled")) {
            this.setAbuse(threadDetails.getString("disabled").equals("true"));
        } else {
            this.setDisabled(false);
        }
    }
}

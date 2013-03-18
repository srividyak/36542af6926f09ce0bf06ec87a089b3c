/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package UGCThreads;

import java.util.Date;
import java.util.List;
import javanb.userpackage.userException;
import net.sf.json.JSONObject;
import org.apache.commons.codec.digest.DigestUtils;

/**
 *
 * @author srivid
 */
public class thread {

    protected String threadId;
    protected long timestamp;
    protected long shareCount;
    protected long upRatingsCount;
    protected long downRatingsCount;
    protected List<String> tags;
    protected String title;
    protected String description;
    protected long commentsCount;
    protected boolean abuseFlag;
    protected String creatorUuid;

    public boolean isAbuseFlag() {
        return abuseFlag;
    }

    public void setAbuseFlag(boolean abuseFlag) {
        this.abuseFlag = abuseFlag;
    }

    public long getCommentsCount() {
        return commentsCount;
    }

    public void setCommentsCount(long commentsCount) {
        this.commentsCount = commentsCount;
    }

    public String getCreatorUuid() {
        return creatorUuid;
    }

    public void setCreatorUuid(String creatorUuid) {
        this.creatorUuid = creatorUuid;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public List<String> getTags() {
        return tags;
    }

    public void setTags(String[] tags) {
        for (String tag : tags) {
            this.tags.add(tag);
        }
    }

    public String getThreadId() {
        return threadId;
    }

    public void setThreadId(String threadId) {
        this.threadId = threadId;
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

    public long getUpRatingsCount() {
        return upRatingsCount;
    }

    public void setUpRatingsCount(long upRatingsCount) {
        this.upRatingsCount = upRatingsCount;
    }

    public thread() {
    }

    public thread(String id) {
        this.setThreadId(id);
    }

    /**
     * generates md5sum of timestamp,title and description
     *
     * @param prefix
     * @return
     */
    public String generateThreadId(String prefix) {
        String comboStr = this.creatorUuid + (new Date()).getTime() + this.title + this.description;
        return DigestUtils.md5Hex(comboStr);
    }

    /**
     * calls setters accordingly and extracts tags from title and description
     *
     * @param threadDetails
     * @throws userException
     */
    public void initThread(JSONObject threadDetails) throws userException {
        if (threadDetails.containsKey("shareCount")) {
            this.setShareCount(threadDetails.getLong("shareCount"));
        } else {
            this.setShareCount(0);
        }
        if (threadDetails.containsKey("commentsCount")) {
            this.setCommentsCount(threadDetails.getLong("commentsCount"));
        } else {
            this.setCommentsCount(0);
        }
        if (threadDetails.containsKey("upRatingsCount")) {
            this.setUpRatingsCount(threadDetails.getLong("upRatingsCount"));
        } else {
            this.setUpRatingsCount(0);
        }
        if (threadDetails.containsKey("downRatingsCount")) {
            this.setDownRatingsCount(threadDetails.getLong("downRatingsCount"));
        } else {
            this.setDownRatingsCount(0);
        }
        if (threadDetails.containsKey("title")) {
            this.setTitle(threadDetails.getString("title"));
        } else {
            throw new userException("title of board is mandatory");
        }
        if (threadDetails.containsKey("abuseFlag")) {
            this.setAbuseFlag(threadDetails.getString("abuseFlag") == "1");
        } else {
            this.setAbuseFlag(false);
        }
        if (threadDetails.containsKey("description")) {
            this.setDescription(threadDetails.getString("description"));
        } else {
            this.setDescription("");
        }
        this.setTimestamp((new Date()).getTime());
        if (threadDetails.containsKey("uuid")) {
            this.setCreatorUuid(threadDetails.getString("uuid"));
        } else {
            throw new userException("uuid of creator is mandatory");
        }
        this.extractTags();
    }

    //for now extracting very common words
    public void extractTags() {
        String words[] = this.title.split(" ");
        for (String word : words) {
            word = word.trim().toLowerCase();
            if (word.startsWith("#")) {
                word = word.substring(1);
                tags.add(word);
            } else if (word.equals("bday") || word.equals("birthday") || word.equals("b'day")) {
                tags.add("birthday");
            } else if (word.equals("new year") || word.equals("new yr")) {
                tags.add("new year");
            }
        }
    }

    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        if (this.threadId != null) {
            json.put("threadId", threadId);
        }
        json.put("timestamp", timestamp);
        json.put("shareCount", shareCount);
        json.put("upRatingsCount", upRatingsCount);
        json.put("downRatingsCount", downRatingsCount);
        json.put("commentsCount", commentsCount);
        json.put("abuseFlag", abuseFlag);
        json.put("creatorUuid", creatorUuid);
        if (this.tags != null) {
            json.put("tags", this.tags.toArray());
        }
        json.put("title", this.title);
        json.put("description", this.description);
        return json;
    }
    
    @Override
    public String toString() {
        return this.toJSON().toString();
    }
}

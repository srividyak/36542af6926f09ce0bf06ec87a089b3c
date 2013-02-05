/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package UGCThreads;

import http.linkHandler;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javanb.userpackage.userException;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author srivid
 */
public class links {
    
    public links(URL url, String uuid) {
        try {
            linkHandler linkHandler = new linkHandler(url);
            JSONObject boardObj = new JSONObject();
            boardObj.put("uuid", uuid);
            boardObj.put("type", "link");
            boardObj.put("title", linkHandler.getTitle());
            boardObj.put("description", linkHandler.getDescription());
            boardObj.put("tags", linkHandler.getTags());
            JSONObject content = new JSONObject();
            content.put("url", url.toString());
            content.put("images", StringUtils.join(linkHandler.getImages().toArray(), ","));
            boardObj.put("content", content);
            board b = new board();
            b.insertBoard(boardObj);
        } catch (userException ex) {
            Logger.getLogger(links.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public links(URL url, String uuid, String targetUuid) {
        try {
            linkHandler linkHandler = new linkHandler(url);
            JSONObject boardObj = new JSONObject();
            boardObj.put("uuid", uuid);
            boardObj.put("type", "link");
            boardObj.put("title", linkHandler.getTitle());
            boardObj.put("description", linkHandler.getDescription());
            boardObj.put("tags", linkHandler.getTags());
            JSONObject content = new JSONObject();
            content.put("url", url.toString());
            content.put("images", StringUtils.join(linkHandler.getImages().toArray(), ","));
            boardObj.put("content", content);
            boardObj.put("targetUuid", targetUuid);
            board b = new board();
            b.insertBoard(boardObj);
        } catch (userException ex) {
            Logger.getLogger(links.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}

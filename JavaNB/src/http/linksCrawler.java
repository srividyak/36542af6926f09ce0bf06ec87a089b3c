/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package http;

import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javanb.customQueue;
import net.sf.json.JSONObject;

/**
 *
 * @author srivid
 */
public class linksCrawler {

    private Hashtable<URL, JSONObject> links;

    public linksCrawler(URL[] urls) {
        customQueue q = new customQueue();
        links = new Hashtable<URL, JSONObject>();
        for (URL url : urls) {
            q.enqueue(url);
        }
        while (true) {
            try {
                if (q.isEmpty()) {
                    break;
                }
                URL url = (URL) q.dequeue();
                linkHandler l = new linkHandler(url, true);
                JSONObject obj = new JSONObject();
                obj.put("title", l.getTitle());
                obj.put("description", l.getDescription());
                obj.put("tags", l.getTags());
                obj.put("images", l.getImages());
                links.put(url, obj);
                System.out.println("url=" + url + ",data:" + obj);
                HashSet<URL> anchors = l.getAnchors();
                for (Iterator it = anchors.iterator(); it.hasNext();) {
                    URL anchor = (URL) it.next();
                    if (!links.containsKey(anchor)) {
                        q.enqueue(anchor);
                    }
                }
            } catch (Exception ex) {
                Logger.getLogger(linksCrawler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}

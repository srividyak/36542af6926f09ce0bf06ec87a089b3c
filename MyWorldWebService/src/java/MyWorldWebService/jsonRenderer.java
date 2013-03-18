/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package MyWorldWebService;

import java.util.Collection;
import java.util.Iterator;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

/**
 *
 * @author srivid
 */
public class jsonRenderer {

    public static String toJSON(Object obj) {
        return obj.toString();
    }
    
    public static String toJSON(Collection<Object> collection) {
        JSONArray result = new JSONArray();
        for(Iterator it = collection.iterator();it.hasNext();) {
            Object o = it.next();
            String json = o.toString();
            result.add(JSONSerializer.toJSON(json));
        }
        return result.toString();
    }
}

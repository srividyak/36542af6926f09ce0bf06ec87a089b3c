/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package myWorldJavaInterface;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javanb.userpackage.user;
import javanb.userpackage.userException;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

/**
 *
 * @author srivid
 */
public class myWorldJavaMain {
    public JSONObject getUser(String uuid) {
        try {
            user existingUser = new user(uuid);
            return existingUser.getUserDetails();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(myWorldJavaMain.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(myWorldJavaMain.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(myWorldJavaMain.class.getName()).log(Level.SEVERE, null, ex);
        } catch (userException ex) {
            Logger.getLogger(myWorldJavaMain.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(myWorldJavaMain.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public void createUser(JSONObject userInfo) throws userException, ParseException {
        user newUser = new user(userInfo);
    }
    
    public void updateUser(String uuid, JSONObject userDetails) throws ParseException, FileNotFoundException, IOException, SQLException {
        Iterator keys = userDetails.keys();
        while(keys.hasNext()) {
            String key = (String) keys.next();
            if(key.equals("gender")) {
                user.updateGender(uuid, userDetails.getBoolean(key));
            } else if(key.equals("dob")) {
                user.updateDob(uuid, userDetails.getString(key));
            } else {
                user.updateStringField(uuid, key, userDetails.getString(key));
            }
        }
    }
    
}

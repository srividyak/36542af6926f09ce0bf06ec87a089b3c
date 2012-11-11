/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package javanb.userpackage;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.json.JSONArray;
import sqlManager.userTableManager;

/**
 *
 * @author srivid
 */
public class multipleUsers {
    ArrayList<user> multipleUsers;

    public multipleUsers(ArrayList<String> uuids) throws userException {
        try {
            this.multipleUsers = new ArrayList<user>();
            userTableManager sqlManager = new userTableManager();
            JSONArray users = sqlManager.getMultipleUsers(uuids);
            for (int i = 0, max = users.size(); i < max; i++) {
                user me = new user();
                me.loadUser(users.getJSONObject(i));
                multipleUsers.add(me);
            }
        } catch (userException ex) {
            throw new userException("error occured during fetching multiple users:" + ex.getMessage());
        } catch (FileNotFoundException ex) {
            throw new userException("error occured during fetching multiple users:" + ex.getMessage());
        } catch (IOException ex) {
            throw new userException("error occured during fetching multiple users:" + ex.getMessage());
        } catch (SQLException ex) {
            throw new userException("error occured during fetching multiple users:" + ex.getMessage());
        }
    }

    public ArrayList<user> getMultipleUsers() {
        return multipleUsers;
    }
}

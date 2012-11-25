/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package javanb;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javanb.userpackage.user;
import javanb.userpackage.userException;
import myWorldJavaInterface.myWorldJavaMain;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

/**
 *
 * @author srivid
 */
public class JavaNB {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws userException, FileNotFoundException, SQLException, IOException, ParseException {
        // TODO code application logic here
        myWorldJavaMain myWorld = new myWorldJavaMain();
        myWorld.getAllFriends("0fccd71a31ac5da989948b822ed8846b");
    }
}

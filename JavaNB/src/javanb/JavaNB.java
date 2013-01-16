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
        user u = new user("31f8570f305a0c69872e1f6a96066db1");
        String info = "{uuid:'31f8570f305a0c69872e1f6a96066db1',firstName:'Srividya',lastName:'krishnamurthy',middleName:'',dob:'1989-03-19',phoneNum:'9900810028',lang:'kannada,english,hindi',locations:[{id:'bengaluru'}],homeTown:'bengaluru',email:'vidya.vasishtha5@gmail.com'}";
        JSONObject userDetails = (JSONObject) JSONSerializer.toJSON(info);
        u.updateUser(userDetails);
    }
}

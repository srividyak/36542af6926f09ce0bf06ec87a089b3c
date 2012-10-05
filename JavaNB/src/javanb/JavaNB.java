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
import java.util.logging.Level;
import java.util.logging.Logger;
import javanb.userpackage.user;
import javanb.userpackage.userException;
import myWorldJavaInterface.myWorldJavaMain;
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
        String JSONString = "{'firstName':'vidya','lastName':'krishnamurthy','dob':'1989-03-19','interests':'movies,cricket,english thriller and sitcoms,tennis','phoneNum':'9900810028','gender':true,'relStatus':'1','lang':'kannada,english,hindi,sanskrit','locations':'606268800,bangalore','hometown':'bangalore','about':'introvert,love watching cricket,tennis,movies and serials.'}";
        myWorldJavaMain myWorld = new myWorldJavaMain();
        JSONObject user = myWorld.getUser("fa5bcb7692a87bd7b9ff9bab8958d5b2");
        System.out.println(user);
    }
}

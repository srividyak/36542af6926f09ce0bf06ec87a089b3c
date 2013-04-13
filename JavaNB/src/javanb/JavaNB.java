/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package javanb;

import UGCThreads.board;
import UGCThreads.links;
import UGCThreads.page;
import com.mysql.jdbc.Connection;
import com.mysql.jdbc.PreparedStatement;
import entity.entity;
import friends.friends;
import http.linkHandler;
import http.linksCrawler;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javanb.userpackage.user;
import javanb.userpackage.userException;
import misc.sqlUtils;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import org.apache.commons.lang.StringUtils;
import search.companySearch;
import search.educationSearch;
import search.locationSearch;
import search.userSearch;
import sqlManager.companyTableManager;
import sqlManager.connectionManager;
import sqlManager.userDbManager;

/**
 *
 * @author srivid
 */
public class JavaNB {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws userException, FileNotFoundException, SQLException, IOException, ParseException, Exception {
        // TODO code application logic here
        long time = (new Date()).getTime();
        System.out.println(companySearch.searchAllCompaniesByName("s"));
        System.out.println("for location srch = " + ((new Date()).getTime() - time));
        time = (new Date()).getTime();
        JSONObject eduRes = (educationSearch.searchAllEducationByName("a"));
        System.out.println(eduRes.size());
        System.out.println("for location srch = " + ((new Date()).getTime() - time));
    }
}

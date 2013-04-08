/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package javanb;

import UGCThreads.board;
import UGCThreads.links;
import UGCThreads.page;
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
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import org.apache.commons.lang.StringUtils;
import search.userSearch;
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
        userSearch.searchAllUsers("vi");
        System.out.println((new Date()).getTime() - time);
    }
}

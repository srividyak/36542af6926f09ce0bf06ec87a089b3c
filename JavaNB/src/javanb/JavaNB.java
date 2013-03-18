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
import java.util.Iterator;
import java.util.List;
import javanb.userpackage.userException;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;

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
        friends f = new friends("06524f6541f83b817bf2f793a0a4ae04");
        System.out.println(f.searchFriendsCommon("vid"));
    }
}

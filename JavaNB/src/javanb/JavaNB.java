/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package javanb;

import UGCThreads.links;
import http.linkHandler;
import http.linksCrawler;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.text.ParseException;
import javanb.userpackage.userException;
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
        links l = new links(new URL("http://www.vogella.com/articles/JavaRegularExpressions/article.html"), "6a05b8829cbad91c53796337002b83c6");
    }
}

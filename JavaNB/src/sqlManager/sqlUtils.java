/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sqlManager;

import com.mysql.jdbc.Connection;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;

/**
 *
 * @author srivid
 */
public class sqlUtils {
    public String dbms;
    public String jarFile;
    public String dbName;
    public String userName;
    public String urlString;
    
    private String driver;
    private String serverName;
    private String password;
    private int port;
    private Properties prop;
    
    public sqlUtils() throws FileNotFoundException, IOException, InvalidPropertiesFormatException{
        this.prop = new Properties();
        FileInputStream fis = new FileInputStream("/Users/srivid/myProjects/myWorld/repository/JavaNB/src/sqlManager/sql-properties.xml");
        prop.loadFromXML(fis);
        
        this.dbms = this.prop.getProperty("dbms");
        this.jarFile = this.prop.getProperty("jar_file");
        this.driver = this.prop.getProperty("driver");
        this.dbName = this.prop.getProperty("database_name");
        this.userName = this.prop.getProperty("user_name");
        this.password = this.prop.getProperty("password");
        this.serverName = this.prop.getProperty("server_name");
        this.port = Integer.parseInt(this.prop.getProperty("port_number"));
        
    }
    
    public Connection getConnection() throws SQLException {
        Connection conn = null;
        Properties connectionProps = new Properties();
        connectionProps.put("user", this.userName);
        connectionProps.put("password", this.password);
        String currentUrlString = null;
        currentUrlString = "jdbc:" + this.dbms + "://" + this.serverName + ":" + this.port + "/";
        conn = (Connection) DriverManager.getConnection(currentUrlString, connectionProps);
        this.urlString = currentUrlString + this.dbName;
        conn.setCatalog(this.dbName);
        return conn;
    }
    
    public void closeConnection(Connection connArg) {
        try {
            if (connArg != null) {
                connArg.close();
                connArg = null;
            }
        } catch (SQLException sqle) {
        }
    }
}

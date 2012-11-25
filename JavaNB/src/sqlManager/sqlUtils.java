/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sqlManager;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.PreparedStatement;
import com.sun.rowset.CachedRowSetImpl;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javanb.userpackage.userException;
import net.spy.memcached.MemcachedClient;
import org.apache.commons.codec.binary.Hex;

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
    private int memcachedClientPort;
    private MemcachedClient memcachedClient;
    private final int expirationMemcached = 3600;
    
    protected class setMemcache implements Runnable {
        private MemcachedClient memcachedClient;
        private String key;
        private CachedRowSetImpl cache;
        private int expirationMemcached;
        
        public setMemcache(MemcachedClient client, String key, CachedRowSetImpl cache, int exp) {
            this.memcachedClient = client;
            this.key = key;
            this.cache = cache;
            this.expirationMemcached = exp;
            Thread t = new Thread(this);
            t.start();
        }
        
        @Override
        public void run() {
            this.memcachedClient.set(this.key,this.expirationMemcached, this.cache);
        }
    }
    
    protected class deleteMemcache implements Runnable {
        private MemcachedClient memcachedClient;
        private String key;
        
        public deleteMemcache(MemcachedClient client, String key) {
            this.memcachedClient = client;
            this.key = key;
            Thread t = new Thread(this);
            t.start();
        }
        
        @Override
        public void run() {
            this.memcachedClient.delete(this.key);
        }
        
    }

    public sqlUtils() throws FileNotFoundException, IOException, InvalidPropertiesFormatException {
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

        this.memcachedClientPort = 11211;
        this.memcachedClient = new MemcachedClient(new InetSocketAddress("localhost", this.memcachedClientPort));
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

    /**
     * executes the sql query. First checks if the result is cached in memcache. If not issues a db call and caches the result
     * @param stmt
     * @return
     * @throws userException
     * @throws SQLException 
     */
    protected ResultSet executeQuery(PreparedStatement stmt) throws userException, SQLException {
        String key = "";
        if ((key = this.generateKeyForMemcached(stmt)) != null) {
            CachedRowSetImpl resultCachedRowSetImpl = (CachedRowSetImpl) this.memcachedClient.get(key);
            if (resultCachedRowSetImpl != null) {
                System.out.println("Read from cache");
                return (ResultSet) resultCachedRowSetImpl;
            }
        }
        ResultSet rs = stmt.executeQuery();
        CachedRowSetImpl cachedRowSetImpl = new CachedRowSetImpl();
        cachedRowSetImpl.populate(rs);
        rs.beforeFirst();
        this.cacheResult(key, cachedRowSetImpl);
        System.out.println("Read from db");
        return rs;
    }

    /**
     * generates md5sum of the sql query
     * @param stmt - prepared statement for which unique key is to be generated
     * @return md5sum as a string
     * @throws userException 
     */
    protected String generateKeyForMemcached(PreparedStatement stmt) throws userException {
        String query = stmt.toString();
        String[] splits = query.split(":");
        if (splits.length > 0) {
            try {
                query = splits[splits.length - 1].trim();
                MessageDigest md = MessageDigest.getInstance("MD5");
                byte[] input = query.getBytes();
                md.update(input);
                byte[] md5sum = md.digest();
                return new String(Hex.encodeHex(md5sum));
            } catch (NoSuchAlgorithmException ex) {
                throw new userException("Error encountered while initializing messagedigest for md5:" + ex.getMessage());
            }
        } else {
            return null;
        }
    }

    /**
     * Spawns a thread to set the value in memcached
     * @param key - key in memcached
     * @param rs - value in memcached
     */
    protected void cacheResult(String key, CachedRowSetImpl rs) {
//        this.memcachedClient.set(key,this.expirationMemcached,rs);
        new setMemcache(this.memcachedClient, key, rs, this.expirationMemcached);
    }

    /**
     * Spawna a thread to delete the key in memcached
     * @param stmt - prepared stmt whose corresponding key is to be deleted from memcached
     * @throws userException 
     */
    protected void invalidateCache(PreparedStatement stmt) throws userException {
        String key = this.generateKeyForMemcached(stmt);
        new deleteMemcache(this.memcachedClient, key);
//        this.memcachedClient.delete(key);
    }
    
    /**
     * API to invalidate cache for a bulk of preparedstatements. Calls invalidateCache API for individual statements
     * @param stmt - array of preparedStatements
     * @throws userException 
     */
    protected void invalidateCache(PreparedStatement[] stmt) throws userException {
        for(int i=0,max=stmt.length;i<max;i++) {
            this.invalidateCache(stmt[i]);
        }
    }
    
    /**
     * API that constructs preparedstatement assuming all replacements are strings. DO NOT USE this in case
     * replacements are of other type
     * @param c - connection to db
     * @param query - sql query
     * @param replacements - replacements e.g for where clause
     * @return 
     */
    protected PreparedStatement getPreparedStatement(Connection c, String query, String replacements) {
        try {
            String[] replacementsArray = replacements.split(",");
            PreparedStatement stmt = (PreparedStatement) c.prepareStatement(query);
            for(int i=0,max=replacementsArray.length;i<max;i++) {
                stmt.setString(i+1, replacementsArray[i]);
            }
            return stmt;
        } catch (SQLException ex) {
            Logger.getLogger(sqlUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}

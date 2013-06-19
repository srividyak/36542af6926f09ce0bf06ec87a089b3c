/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sqlManager;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.Driver;
import com.mysql.jdbc.PreparedStatement;
import com.sun.rowset.CachedRowSetImpl;
import com.sun.tools.internal.xjc.api.S2JJAXBModel;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javanb.userpackage.user;
import javanb.userpackage.userException;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.spy.memcached.MemcachedClient;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;

/**
 *
 * @author srivid
 */
public class sqlUtils<T> {

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
    protected ExecutorService executorService = null;
    protected Connection dbConnection = null;

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
            this.memcachedClient.set(this.key, this.expirationMemcached, this.cache);
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

    public sqlUtils() {
        this.prop = new Properties();
        FileInputStream fis = null;
        try {
            fis = new FileInputStream("/Users/srivid/myProjects/myWorld/repository/JavaNB/src/sqlManager/sql-properties.xml");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(sqlUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            prop.loadFromXML(fis);
        } catch (IOException ex) {
            Logger.getLogger(sqlUtils.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.dbms = this.prop.getProperty("dbms");
        this.jarFile = this.prop.getProperty("jar_file");
        this.driver = this.prop.getProperty("driver");
        this.dbName = this.prop.getProperty("database_name");
        this.userName = this.prop.getProperty("user_name");
        this.password = this.prop.getProperty("password");
        this.serverName = this.prop.getProperty("server_name");
        this.port = Integer.parseInt(this.prop.getProperty("port_number"));

        this.memcachedClientPort = 11211;
        try {
            this.memcachedClient = new MemcachedClient(new InetSocketAddress("localhost", this.memcachedClientPort));
        } catch (IOException ex) {
            Logger.getLogger(sqlUtils.class.getName()).log(Level.SEVERE, null, ex);
        }

        this.executorService = Executors.newFixedThreadPool(16); // should be configurable but limiting to 16 for now
    }

    public Connection getConnection() throws SQLException {
        Connection conn = null;
        String currentUrlString = null;
        currentUrlString = "jdbc:" + this.dbms + "://" + this.serverName + ":" + this.port + "/" + this.dbName;
//        new Driver();
//        conn = (Connection) DriverManager.getConnection(currentUrlString, this.userName, this.password);
        this.urlString = currentUrlString + this.dbName;
//        return conn;
        return connectionManager.getConnection(currentUrlString, this.userName, this.password);
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
     * executes the sql query. First checks if the result is cached in memcache.
     * If not issues a db call and caches the result
     *
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
     *
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
     *
     * @param key - key in memcached
     * @param rs - value in memcached
     */
    protected void cacheResult(String key, CachedRowSetImpl rs) {
        new setMemcache(this.memcachedClient, key, rs, this.expirationMemcached);
    }

    /**
     * Spawns a thread to delete the key in memcached
     *
     * @param stmt - prepared stmt whose corresponding key is to be deleted from
     * memcached
     * @throws userException
     */
    protected void invalidateCache(PreparedStatement stmt) throws userException {
        String key = this.generateKeyForMemcached(stmt);
        new deleteMemcache(this.memcachedClient, key);
    }

    /**
     * API to invalidate cache for a bulk of preparedstatements. Calls
     * invalidateCache API for individual statements
     *
     * @param stmt - array of preparedStatements
     * @throws userException
     */
    protected void invalidateCache(PreparedStatement[] stmt) throws userException {
        for (int i = 0, max = stmt.length; i < max; i++) {
            if (stmt[i] != null) {
                this.invalidateCache(stmt[i]);
            }
        }
    }

    /**
     * API that constructs preparedstatement assuming all replacements are
     * strings. DO NOT USE this in case replacements are of other type
     *
     * @param c - connection to db
     * @param query - sql query
     * @param replacements - replacements e.g for where clause
     * @return
     */
    protected PreparedStatement getPreparedStatement(Connection c, String query, String replacements) {
        try {
            String[] replacementsArray = replacements.split(",");
            PreparedStatement stmt = (PreparedStatement) c.prepareStatement(query);
            for (int i = 0, max = replacementsArray.length; i < max; i++) {
                stmt.setString(i + 1, replacementsArray[i]);
            }
            return stmt;
        } catch (SQLException ex) {
            Logger.getLogger(sqlUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public Hashtable<String, Integer> executeMultipleUpdates(ArrayList<PreparedStatement> stmts) {
        List<Callable<Hashtable<String, Integer>>> callables = Collections.synchronizedList(new LinkedList<Callable<Hashtable<String, Integer>>>());
        for (PreparedStatement stmt : stmts) {
            final PreparedStatement statement = stmt;
            Callable<Hashtable<String, Integer>> callable = new Callable<Hashtable<String, Integer>>() {

                @Override
                public Hashtable<String, Integer> call() throws Exception {
                    int result = statement.executeUpdate();
                    Hashtable<String, Integer> res = new Hashtable<String, Integer>();
                    res.put(statement.toString(), new Integer(result));
                    return res;
                }
            };
            callables.add(callable);
        }

        List<Future<Hashtable<String, Integer>>> futures = Collections.synchronizedList(new LinkedList<Future<Hashtable<String, Integer>>>());
        try {
            futures = (List<Future<Hashtable<String, Integer>>>) this.executorService.invokeAll(callables);
            Hashtable<String, Integer> result = new Hashtable<String, Integer>();
            for (Future<Hashtable<String, Integer>> future : futures) {
                try {
                    Hashtable r = future.get();
                    Enumeration keys = r.keys();
                    while (keys.hasMoreElements()) {
                        String key = (String) keys.nextElement();
                        Integer integer = (Integer) r.get(key);
                        result.put(key, integer);
                    }
                } catch (ExecutionException ex) {
                    Logger.getLogger(sqlUtils.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            return result;
        } catch (InterruptedException ex) {
            Logger.getLogger(sqlUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * API to execute multiple queries parallely. It uses the thread pool to
     * execute queries in threads
     *
     * @param stmts - arraylist of prepared statements to be executed
     * @return hashtable of results with keys being the query
     * @throws userException
     */
    public Hashtable<String, ResultSet> executeMultipleQueries(ArrayList<PreparedStatement> stmts) throws userException {
        try {
            ArrayList<Callable<Hashtable<String, ResultSet>>> callables = new ArrayList<Callable<Hashtable<String, ResultSet>>>();
            for (PreparedStatement stmt : stmts) {
                final PreparedStatement statement = stmt;
                Callable<Hashtable<String, ResultSet>> callable = new Callable<Hashtable<String, ResultSet>>() {

                    @Override
                    public Hashtable<String, ResultSet> call() throws userException, SQLException {
                        ResultSet rs = sqlUtils.this.executeQuery(statement);
                        Hashtable<String, ResultSet> result = new Hashtable<String, ResultSet>();
                        result.put(statement.toString(), rs);
                        return result;
                    }
                };
                callables.add(callable);
            }

            ArrayList<Future<Hashtable<String, ResultSet>>> futuresList = new ArrayList<Future<Hashtable<String, ResultSet>>>();
            futuresList = (ArrayList<Future<Hashtable<String, ResultSet>>>) this.executorService.invokeAll(callables);
            Hashtable<String, ResultSet> multipleQueryResult = new Hashtable<String, ResultSet>();
            for (Future<Hashtable<String, ResultSet>> future : futuresList) {
                try {
                    Hashtable result = future.get();
                    Enumeration keys = result.keys();
                    while (keys.hasMoreElements()) {
                        String key = (String) keys.nextElement();
                        ResultSet rs = (ResultSet) result.get(key);
                        multipleQueryResult.put(key, rs);
                    }
                } catch (ExecutionException ex) {
                    throw new userException("error occured while executing query : " + ex.getMessage());
                }
            }
            return multipleQueryResult;
        } catch (InterruptedException ex) {
            throw new userException("error occured while executing query : " + ex.getMessage());
        }
    }

    private String getSetterMethod(String param) {
        return "set" + WordUtils.capitalize(param);
    }

    public T resultSetToJavaObject(T requiredObject, ResultSet resultSet) {
        try {
            java.sql.ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            Class clazz = requiredObject.getClass();
            for (int i = 1; i <= columnCount; i++) {
                try {
                    String columnName = metaData.getColumnName(i);
                    int sqlType = metaData.getColumnType(i);
                    String methodString = this.getSetterMethod(columnName);
                    try {
                        try {
                            switch (sqlType) {
                                case java.sql.Types.ARRAY:
                                case java.sql.Types.LONGVARBINARY:
                                case java.sql.Types.BINARY:
                                case java.sql.Types.BIT:
                                case java.sql.Types.BLOB:
                                case java.sql.Types.CLOB:
                                case java.sql.Types.DATALINK:
                                case java.sql.Types.NCLOB:
                                case java.sql.Types.REF:
                                case java.sql.Types.VARBINARY:
                                case java.sql.Types.NVARCHAR:
                                case java.sql.Types.NULL:
                                case java.sql.Types.STRUCT:
                                    break;
                                case java.sql.Types.BIGINT:
                                    clazz.getMethod(methodString, long.class).invoke(requiredObject, resultSet.getLong(i));
                                    break;
                                case java.sql.Types.VARCHAR:
                                    clazz.getMethod(methodString, String.class).invoke(requiredObject, resultSet.getString(i));
                                    break;
                                case java.sql.Types.BOOLEAN:
                                    System.out.println(columnName + ",type=" + sqlType + ",boolean");
                                    break;
                                case java.sql.Types.CHAR:
                                    clazz.getMethod(methodString, char.class).invoke(requiredObject, resultSet.getString(i).charAt(0));
                                    break;
                                case java.sql.Types.DATE:
                                    clazz.getMethod(methodString, String.class).invoke(requiredObject, resultSet.getString(i));
                                    break;
                                case java.sql.Types.DECIMAL:
                                    try {
                                        clazz.getMethod(methodString, float.class).invoke(requiredObject, resultSet.getFloat(i));
                                    } catch (NoSuchMethodException e) {
                                        clazz.getMethod(methodString, double.class).invoke(requiredObject, resultSet.getDouble(i));
                                    }
                                    break;
                                case java.sql.Types.DOUBLE:
                                    clazz.getMethod(methodString, double.class).invoke(requiredObject, resultSet.getDouble(i));
                                    break;
                                case java.sql.Types.FLOAT:
                                    clazz.getMethod(methodString, float.class).invoke(requiredObject, resultSet.getFloat(i));
                                    break;
                                case java.sql.Types.INTEGER:
                                    clazz.getMethod(methodString, int.class).invoke(requiredObject, resultSet.getInt(i));
                                    break;
                                case java.sql.Types.JAVA_OBJECT:
                                    clazz.getMethod(methodString, Object.class).invoke(requiredObject, resultSet.getObject(i));
                                    break;
                                case java.sql.Types.LONGNVARCHAR:
                                    clazz.getMethod(methodString, String.class).invoke(requiredObject, resultSet.getString(i));
                                    break;
                                case java.sql.Types.LONGVARCHAR:
                                    clazz.getMethod(methodString, String.class).invoke(requiredObject, resultSet.getString(i));
                                    break;
                                case java.sql.Types.NCHAR:
                                    clazz.getMethod(methodString, char.class).invoke(requiredObject, resultSet.getString(i).charAt(0));
                                    break;
                                case java.sql.Types.NUMERIC:
                                    clazz.getMethod(methodString, int.class).invoke(requiredObject, resultSet.getInt(i));
                                    break;
                                case java.sql.Types.OTHER:
                                    clazz.getMethod(methodString, Object.class).invoke(requiredObject, resultSet.getObject(i));
                                    break;
                                case java.sql.Types.REAL:
                                    clazz.getMethod(methodString, float.class).invoke(requiredObject, resultSet.getFloat(i));
                                    break;
                                case java.sql.Types.SMALLINT:
                                    clazz.getMethod(methodString, int.class).invoke(requiredObject, resultSet.getInt(i));
                                    break;
                                case java.sql.Types.SQLXML:
                                    clazz.getMethod(methodString, SQLXML.class).invoke(requiredObject, resultSet.getSQLXML(i));
                                    break;
                                case java.sql.Types.TIME:
                                case java.sql.Types.TIMESTAMP:
                                    clazz.getMethod(methodString, long.class).invoke(requiredObject, resultSet.getLong(i));
                                    break;
                                case java.sql.Types.TINYINT:
                                    clazz.getMethod(methodString, int.class).invoke(requiredObject, resultSet.getInt(i));
                                    break;
                                default:
                                    break;
                            }
                        } catch (IllegalAccessException ex) {
                            Logger.getLogger(userDbManager.class.getName() + ",columnName" + columnName).log(Level.SEVERE, null, ex);
                        } catch (IllegalArgumentException ex) {
                            Logger.getLogger(userDbManager.class.getName() + ",columnName" + columnName).log(Level.SEVERE, null, ex);
                        } catch (InvocationTargetException ex) {
                            Logger.getLogger(userDbManager.class.getName() + ",columnName" + columnName).log(Level.SEVERE, null, ex);
                        }
                    } catch (NoSuchMethodException ex) {
                        Logger.getLogger(userDbManager.class.getName() + ",columnName" + columnName).log(Level.SEVERE, null, ex);
                    } catch (SecurityException ex) {
                        Logger.getLogger(userDbManager.class.getName() + ",columnName" + columnName).log(Level.SEVERE, null, ex);
                    }
                } catch (SecurityException ex) {
                    Logger.getLogger(userDbManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        } catch (SQLException ex) {
            Logger.getLogger(userDbManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return requiredObject;
    }

    protected PreparedStatement getFormattedQuery(String tableName, JSONObject tbInserted, String action) {
        try {
            String query = "";
            if ("insert".equals(action.toLowerCase())) {
                query = "insert into " + tableName + " ";
            } else if ("update".equals(action.toLowerCase())) {
                query = "update " + tableName + " set ";
            } else {
                return null;
            }
            org.json.simple.JSONObject tableInfo = tablesMetaDataHandler.getTableInfo(tableName);
            String keys = "";
            String values = "";
            PreparedStatement insertRow;
            Set<String> tableSet = tableInfo.keySet();
            Iterator<String> tableIt = tableSet.iterator();
            /*
             * populating keys and values
             */
            while (tableIt.hasNext()) {
                String column = tableIt.next();
                if (tbInserted.containsKey(column)) {
                    if ("insert".equals(action.toLowerCase())) {
                        keys += column + ",";
                        values += "?,";
                    } else {
                        if (!((String) tableInfo.get(column)).equals("String")) {
                            if (tbInserted.getString(column).equals("increment")) {
                                query += column + "=" + column + "+1";
                            } else if (tbInserted.getString(column).equals("decrement")) {
                                query += column + "=" + column + "-1";
                            } else {
                                query += column + "=?,";
                            }
                        } else {
                            query += column + "=?,";
                        }

                    }
                }
            }

            if ("insert".equals(action.toLowerCase())) {
                keys = StringUtils.strip(keys, ",");
                keys = "(" + keys + ")";

                values = StringUtils.strip(values, ",");
                values = "values(" + values + ")";

                query += " " + keys + " " + values;
            } else {
                query = StringUtils.strip(query, ",");
                int numColsUpdated = tbInserted.size();
                if (tbInserted.containsKey("identifiers")) {
                    numColsUpdated--;
                    query += " where ";
                    JSONObject identifiers = tbInserted.getJSONObject("identifiers");
                    Iterator<String> identifierCols = identifiers.keys();
                    while (identifierCols.hasNext()) {
                        query += identifierCols.next() + "?,";
                    }
                    query = StringUtils.strip(query, ",");
                }
            }
            tableIt = tableSet.iterator();
            insertRow = (PreparedStatement) this.dbConnection.prepareStatement(query);
            Class clazz = insertRow.getClass();
            int j = 1;
            for (int i = 0, max = tableSet.size(); i < max; i++) {
                String column;
                if (tableIt.hasNext()) {
                    column = tableIt.next();
                    if (tbInserted.containsKey(column)) {
                        String type = (String) tableInfo.get(column), methodName = "set" + type;
                        Class paramClass = String.class;
                        if ("String".equals(type)) {
                            paramClass = String.class;
                        } else if ("Int".equals(type)) {
                            paramClass = int.class;
                        } else if ("Long".equals(type)) {
                            paramClass = long.class;
                        } else if ("Date".equals(type)) {
                            paramClass = java.sql.Date.class;
                        } else if ("Char".equals(type)) {
                            paramClass = char.class;
                        } else if ("Boolean".equals(type)) {
                            paramClass = boolean.class;
                        }

                        try {
                            try {
                                if ("Date".equals(type)) {
                                    SimpleDateFormat simpleDob = new SimpleDateFormat("yyyy-mm-dd", Locale.ENGLISH);
                                    try {
                                        java.util.Date date = simpleDob.parse(tbInserted.getString(column));
                                        java.sql.Date paramDate = new java.sql.Date(date.getTime());
                                        insertRow.setDate(j, paramDate);
                                    } catch (ParseException ex) {
                                        Logger.getLogger(sqlUtils.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                } else {
                                    clazz.getMethod(methodName, int.class, paramClass).invoke(insertRow, j,
                                            tbInserted.getClass().getMethod("get" + type, String.class).invoke(tbInserted, column));
                                }

                            } catch (NoSuchMethodException ex) {
                                Logger.getLogger(sqlUtils.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (SecurityException ex) {
                                Logger.getLogger(sqlUtils.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        } catch (IllegalAccessException ex) {
                            Logger.getLogger(sqlUtils.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (IllegalArgumentException ex) {
                            Logger.getLogger(sqlUtils.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (InvocationTargetException ex) {
                            Logger.getLogger(sqlUtils.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        j++;
                    }
                }
            }
            return insertRow;
        } catch (SQLException ex) {
            Logger.getLogger(sqlUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}

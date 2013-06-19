/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sqlManager;

import com.mysql.jdbc.Connection;
import com.mysql.jdbc.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author srivid
 */
public class connectionManager {

    public static Connection connection;

    public static Connection getConnection(String url, String username, String password) {
        try {
            if (connection == null) {
                System.out.println("new connection");
                new Driver();
                connection = (Connection) DriverManager.getConnection(url, username, password);
            }
            System.out.println("old connection");
            return connection;
        } catch (SQLException ex) {
            Logger.getLogger(connectionManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return connection;
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ex) {
                Logger.getLogger(connectionManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}

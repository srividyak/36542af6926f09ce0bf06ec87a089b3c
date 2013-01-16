/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package javanb.userLogsPackage;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javanb.userpackage.userException;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import sqlManager.userLogsManager;

/**
 *
 * @author srivid
 */
public class logs {

    private long avgUsers = 0; //avg users per day
    private long totalUsers = 0; //total distinct users over span of numDays
    private long avgTimeSpent = 0; //avg timespent per user per day
    private userLogsManager sqlManager;
    public static int numDays = 30; //this is for benchmarking

    public logs() {
        try {
            sqlManager = new userLogsManager();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(logs.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(logs.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(logs.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void mineLogs() throws userException {
        JSONArray allLogs = this.sqlManager.getAllLogs();
        Hashtable<String, Double> logTbl = new Hashtable<String, Double>();
        for (Iterator it = allLogs.iterator(); it.hasNext();) {
            JSONObject log = (JSONObject) it.next();
            String uuid = log.getString("uuid");
            long timeSpent = log.getLong("timestampLogout") - log.getLong("timestampLogin");
            if (logTbl.containsKey(uuid)) {
                long curTime = (logTbl.get(uuid)).longValue();
                double avg = (curTime + timeSpent) / logs.numDays;
                this.avgTimeSpent += avg;
                logTbl.put(uuid, new Double(avg));
            } else {
                double avg = timeSpent / logs.numDays;
                this.avgTimeSpent += avg;
                logTbl.put(uuid, new Double(avg));
            }
        }
        this.totalUsers = logTbl.size();
        this.avgUsers = this.totalUsers / logs.numDays;
        this.avgTimeSpent /= logs.numDays;
    }

    public long getAvgTimeSpent() {
        return avgTimeSpent;
    }

    public void setAvgTimeSpent(long avgTimeSpent) {
        this.avgTimeSpent = avgTimeSpent;
    }

    public long getAvgUsers() {
        return avgUsers;
    }

    public void setAvgUsers(long avgUsers) {
        this.avgUsers = avgUsers;
    }

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }
}

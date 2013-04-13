/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sqlManager;

import com.mysql.jdbc.PreparedStatement;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javanb.companypackage.company;
import javanb.userpackage.userException;
import net.sf.json.JSONObject;

/**
 *
 * @author srivid
 */
public class companyTableManager extends sqlUtils {

    private company company;

    public companyTableManager() {
        try {
            this.dbConnection = this.getConnection();
        } catch (SQLException ex) {
            Logger.getLogger(companyTableManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public companyTableManager(company company) {
        this.company = company;
    }

    public companyTableManager(String id, company company) {
        try {
            this.dbConnection = this.getConnection();
            this.company = company;
            String query = "select * from companies where id=?";
            PreparedStatement stmt = (PreparedStatement) this.dbConnection.prepareStatement(query);
            stmt.setString(1, id);
            ResultSet resultSet = this.executeQuery(stmt);
            if (resultSet.next()) {
                this.resultSetToJavaObject(this.company, resultSet);
            }
        } catch (userException ex) {
            Logger.getLogger(companyTableManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(companyTableManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public ArrayList<company> getAllCompanies() {
        String query = "select * from companies";
        ArrayList<company> companies = new ArrayList<company>();
        try {
            PreparedStatement stmt = (PreparedStatement) this.dbConnection.prepareStatement(query);
            ResultSet rs = this.executeQuery(stmt);
            while (rs.next()) {
                company company = new company();
                this.resultSetToJavaObject(company, rs);
                companies.add(company);
            }
            return companies;
        } catch (userException ex) {
            Logger.getLogger(companyTableManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(companyTableManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * DONT use this API since location/education/company tables are frozen and
     * read only
     *
     * @param compData
     * @throws FileNotFoundException
     * @throws IOException
     * @throws SQLException
     * @throws userException
     */
    public companyTableManager(JSONObject compData) throws FileNotFoundException, IOException, SQLException, userException {
        this.dbConnection = this.getConnection();
        if (compData.containsKey("id")) {
            //first do a search for company
            String id = compData.getString("id");
            String selectString = "select * from companies where id=?";
            PreparedStatement selectQuery = (PreparedStatement) this.dbConnection.prepareStatement(selectString);
            selectQuery.setString(1, id);
            boolean noComp = true;
            ResultSet selectResultSet = selectQuery.executeQuery();
            while (selectResultSet.next()) {
                noComp = false;
            }
            if (noComp) {
                try {
                    String insertString = "insert into companies (id,name,description) values (?,?,?)";
                    PreparedStatement insertQuery = (PreparedStatement) this.dbConnection.prepareStatement(insertString);
                    insertQuery.setString(1, id);
                    insertQuery.setString(2, compData.getString("name"));
                    insertQuery.setString(3, compData.containsKey("description") ? compData.getString("description") : "");
                    insertQuery.executeUpdate();
                } catch (SQLException sqle) {
                    System.out.println("Some error inserting into company table:" + sqle.getMessage());
                }
            }
        } else {
            throw new userException("id of company missing");
        }
    }
}

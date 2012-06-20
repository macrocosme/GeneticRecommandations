/*
 * Created by Dany Vohl
 * Copyright 2012
 */
package planner;
import com.mysql.jdbc.Driver;
import java.sql.*;

/**
 *
 * @author Macrocosme / Dany Vohl
 */
public class Connect {
    public Connect() throws SQLException{
        makeConnection();
    } 

    private Connection connection;  

     public  Connection makeConnection() throws SQLException {
        if (connection == null) {
             new Driver();
            // buat koneksi
             connection = DriverManager.getConnection(
                       "jdbc:mysql://localhost:3306/ia",
                       "root",
                       "");
         }
         return connection;
     }  

     public static void main(String args[]) {
         try {
             Connect c = new Connect();
             System.out.println("Connection established");
         }
         catch (SQLException e) {
             e.printStackTrace();
             System.err.println("Connection Failure");
         }  

    }
}

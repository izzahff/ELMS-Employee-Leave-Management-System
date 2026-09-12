package elms.connection;
/*
 * Author: FES (March 2024)
 * Updated with debug information
 */
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionManager {
    static Connection con;
    //define and initialize database driver
    private static final String DB_DRIVER = "oracle.jdbc.driver.OracleDriver";
    //define and initialize database url
    private static final String DB_CONNECTION = "jdbc:oracle:thin:@//localhost:1521/freepdb1";
    //define and initialize database user
    private static final String DB_USER = "YOUR_USERNAME_HERE";
    //define and initialize database password
    private static final String DB_PASSWORD = "YOUR_PASSWORD_HERE";
    
    public static Connection getConnection() {
        System.out.println("DEBUG: Attempting to create database connection...");
        System.out.println("DEBUG: Driver: " + DB_DRIVER);
        System.out.println("DEBUG: URL: " + DB_CONNECTION);
        System.out.println("DEBUG: User: " + DB_USER);
        
        try {
            //1. load the driver
            System.out.println("DEBUG: Loading Oracle driver...");
            Class.forName(DB_DRIVER);
            System.out.println("DEBUG: Oracle driver loaded successfully");
            
            try {
                //2. create connection
                System.out.println("DEBUG: Attempting to connect to database...");
                con = DriverManager.getConnection(DB_CONNECTION, DB_USER, DB_PASSWORD);
                
                if (con != null) {
                    System.out.println("DEBUG: Database connection successful!");
                    System.out.println("DEBUG: Connection object: " + con.getClass().getName());
                    System.out.println("DEBUG: AutoCommit: " + con.getAutoCommit());
                    System.out.println("DEBUG: Connection valid: " + con.isValid(5));
                } else {
                    System.err.println("ERROR: Connection object is null!");
                }
                
            } catch(SQLException e) {
                System.err.println("ERROR: SQLException during connection:");
                System.err.println("ERROR: Message: " + e.getMessage());
                System.err.println("ERROR: SQL State: " + e.getSQLState());
                System.err.println("ERROR: Error Code: " + e.getErrorCode());
                e.printStackTrace();
                con = null;
            }
        } catch(ClassNotFoundException e) {
            System.err.println("ERROR: Oracle JDBC driver not found!");
            System.err.println("ERROR: Make sure ojdbc jar is in your classpath");
            e.printStackTrace();
            con = null;
        }        
        
        return con;
    }
    
    /**
     * Test the database connection
     * @return true if connection successful, false otherwise
     */
    public static boolean testConnection() {
        System.out.println("=== CONNECTION TEST START ===");
        
        try (Connection testCon = getConnection()) {
            if (testCon != null && !testCon.isClosed()) {
                System.out.println("SUCCESS: Database connection test passed!");
                
                // Test a simple query
                try (java.sql.Statement stmt = testCon.createStatement();
                     java.sql.ResultSet rs = stmt.executeQuery("SELECT 1 FROM DUAL")) {
                    
                    if (rs.next()) {
                        System.out.println("SUCCESS: Simple query test passed!");
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("ERROR: Connection test failed!");
            System.err.println("ERROR: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("=== CONNECTION TEST END ===");
        return false;
    }
}
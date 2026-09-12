package elms.DAO;

import elms.connection.ConnectionManager;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PublicHolidayDAO {
    
    /**
     * Get public holidays for specified years
     * @param years Variable number of years to fetch holidays for
     * @return List of holiday dates in YYYY-MM-DD format
     */
    public List<String> getPublicHolidaysForYears(int... years) {
        List<String> holidays = new ArrayList<>();
        
        if (years == null || years.length == 0) {
            System.out.println("⚠️ No years provided for holiday lookup");
            return holidays;
        }
        
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            System.out.println("=== Getting public holidays for years: " + java.util.Arrays.toString(years) + " ===");
            
            conn = ConnectionManager.getConnection();
            
            // Build SQL query dynamically based on number of years
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT TO_CHAR(holiday_date, 'YYYY-MM-DD') as holiday_date ");
            sql.append("FROM public_holidays WHERE is_active = 1 AND (");
            
            for (int i = 0; i < years.length; i++) {
                if (i > 0) sql.append(" OR ");
                sql.append("EXTRACT(YEAR FROM holiday_date) = ?");
            }
            
            sql.append(") ORDER BY holiday_date");
            
            System.out.println("SQL Query: " + sql.toString());
            
            ps = conn.prepareStatement(sql.toString());
            
            // Set year parameters
            for (int i = 0; i < years.length; i++) {
                ps.setInt(i + 1, years[i]);
                System.out.println("Parameter " + (i + 1) + ": " + years[i]);
            }
            
            rs = ps.executeQuery();
            
            while (rs.next()) {
                String holidayDate = rs.getString("holiday_date");
                holidays.add(holidayDate);
                System.out.println("  Added holiday: " + holidayDate);
            }
            
            System.out.println("✅ Loaded " + holidays.size() + " public holidays");
            
        } catch (SQLException e) {
            System.err.println("❌ Error loading public holidays: " + e.getMessage());
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            e.printStackTrace();
        } finally {
            // Close resources in reverse order
            try {
                if (rs != null) {
                    rs.close();
                    System.out.println("ResultSet closed");
                }
                if (ps != null) {
                    ps.close();
                    System.out.println("PreparedStatement closed");
                }
                if (conn != null) {
                    conn.close();
                    System.out.println("Database connection closed");
                }
            } catch (SQLException e) {
                System.err.println("Error closing database resources: " + e.getMessage());
            }
        }
        
        return holidays;
    }
    
    /**
     * Test database connection and table existence
     * @return true if connection successful and table exists, false otherwise
     */
    public boolean testConnection() {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            System.out.println("=== Testing PublicHolidayDAO database connection ===");
            conn = ConnectionManager.getConnection();
            
            if (conn != null && !conn.isClosed()) {
                System.out.println("Database connection successful!");
                
                // Test if table exists
                String sql = "SELECT COUNT(*) as count FROM public_holidays";
                ps = conn.prepareStatement(sql);
                rs = ps.executeQuery();
                
                if (rs.next()) {
                    int count = rs.getInt("count");
                    System.out.println("✅ public_holidays table exists with " + count + " records");
                    return true;
                }
            } else {
                System.out.println("❌ Database connection is null or closed");
                return false;
            }
        } catch (SQLException e) {
            System.err.println("❌ SQLException during connection test: " + e.getMessage());
            System.err.println("This might mean the public_holidays table doesn't exist yet.");
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                    System.out.println("Test connection closed successfully");
                }
            } catch (SQLException e) {
                System.err.println("Error closing test connection: " + e.getMessage());
            }
        }
        
        return false;
    }
}
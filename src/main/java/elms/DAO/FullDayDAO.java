package elms.DAO;

import elms.model.FullDay;
import elms.connection.ConnectionManager;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FullDayDAO {
    
    /**
     * Create a new full day record in the database
     * @param fullDay FullDay object containing the data
     * @return true if successfully created, false otherwise
     */
    public boolean createFullDay(FullDay fullDay) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            System.out.println("=== FullDayDAO.createFullDay called ===");
            System.out.println("Getting database connection...");
            
            conn = ConnectionManager.getConnection();
            System.out.println("Database connection obtained successfully");
            
            // SQL query to insert new full day record
            String sql = "INSERT INTO fullday (leaveTypeId, standardDuration) VALUES (?, ?)";
            
            System.out.println("Preparing SQL statement: " + sql);
            System.out.println("Parameters:");
            System.out.println("1. leaveTypeId: " + fullDay.getLeaveTypeId());
            System.out.println("2. standardDuration: " + fullDay.getStandardDuration());
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, fullDay.getLeaveTypeId());
            pstmt.setInt(2, fullDay.getStandardDuration());
            
            System.out.println("Executing SQL statement...");
            int rowsAffected = pstmt.executeUpdate();
            System.out.println("Rows affected: " + rowsAffected);
            
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("SQLException in createFullDay: " + e.getMessage());
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            System.err.println("General Exception in createFullDay: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (pstmt != null) {
                    pstmt.close();
                    System.out.println("PreparedStatement closed");
                }
                if (conn != null) {
                    conn.close();
                    System.out.println("Database connection closed");
                }
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            }
        }
    }
    
    /**
     * Get full day details by leave type ID
     * @param leaveTypeId ID of the leave type
     * @return FullDay object or null if not found
     */
    public FullDay getFullDayByLeaveTypeId(String leaveTypeId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            System.out.println("=== Getting FullDay details for leaveTypeId: " + leaveTypeId + " ===");
            
            conn = ConnectionManager.getConnection();
            String sql = "SELECT leaveTypeId, standardDuration FROM fullday WHERE leaveTypeId = ?";
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, leaveTypeId);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                FullDay fullDay = new FullDay();
                fullDay.setLeaveTypeId(rs.getString("leaveTypeId"));
                fullDay.setStandardDuration(rs.getInt("standardDuration"));
                
                System.out.println("Found FullDay record: " + fullDay.toString());
                return fullDay;
            } else {
                System.out.println("No FullDay record found for leaveTypeId: " + leaveTypeId);
            }
            
        } catch (SQLException e) {
            System.err.println("Error retrieving full day details: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            }
        }
        
        return null;
    }
    
    /**
     * Get all full day records
     * @return List of FullDay objects
     */
    public List<FullDay> getAllFullDays() {
        List<FullDay> fullDays = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = ConnectionManager.getConnection();
            String sql = "SELECT leaveTypeId, standardDuration FROM fullday ORDER BY leaveTypeId";
            
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                FullDay fullDay = new FullDay();
                fullDay.setLeaveTypeId(rs.getString("leaveTypeId"));
                fullDay.setStandardDuration(rs.getInt("standardDuration"));
                
                fullDays.add(fullDay);
            }
            
            System.out.println("Retrieved " + fullDays.size() + " full day records");
            
        } catch (SQLException e) {
            System.err.println("Error retrieving all full day records: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            }
        }
        
        return fullDays;
    }
    
    /**
     * Update full day standard duration
     * @param fullDay FullDay object with updated data
     * @return true if successfully updated, false otherwise
     */
    public boolean updateFullDay(FullDay fullDay) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            System.out.println("=== Updating FullDay record ===");
            
            conn = ConnectionManager.getConnection();
            String sql = "UPDATE fullday SET standardDuration = ? WHERE leaveTypeId = ?";
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, fullDay.getStandardDuration());
            pstmt.setString(2, fullDay.getLeaveTypeId());
            
            System.out.println("Updating leaveTypeId: " + fullDay.getLeaveTypeId() + 
                             " with standardDuration: " + fullDay.getStandardDuration());
            
            int rowsAffected = pstmt.executeUpdate();
            System.out.println("Rows affected: " + rowsAffected);
            
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating full day record: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            }
        }
    }
    
    /**
     * Delete a full day record by leave type ID
     * @param leaveTypeId ID of the leave type
     * @return true if successfully deleted, false otherwise
     */
    public boolean deleteFullDay(String leaveTypeId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            System.out.println("=== Deleting FullDay record for leaveTypeId: " + leaveTypeId + " ===");
            
            conn = ConnectionManager.getConnection();
            String sql = "DELETE FROM fullday WHERE leaveTypeId = ?";
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, leaveTypeId);
            
            int rowsAffected = pstmt.executeUpdate();
            System.out.println("Rows affected: " + rowsAffected);
            
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting full day record: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            }
        }
    }
    
    /**
     * Test database connection
     * @return true if connection successful, false otherwise
     */
    public boolean testConnection() {
        Connection conn = null;
        try {
            System.out.println("=== Testing FullDayDAO database connection ===");
            conn = ConnectionManager.getConnection();
            if (conn != null && !conn.isClosed()) {
                System.out.println("FullDayDAO database connection test successful!");
                return true;
            } else {
                System.out.println("FullDayDAO database connection is null or closed");
                return false;
            }
        } catch (SQLException e) {
            System.err.println("SQLException during FullDayDAO connection test: " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            System.err.println("General exception during FullDayDAO connection test: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                    System.out.println("FullDayDAO test connection closed successfully");
                }
            } catch (SQLException e) {
                System.err.println("Error closing FullDayDAO test connection: " + e.getMessage());
            }
        }
    }
}

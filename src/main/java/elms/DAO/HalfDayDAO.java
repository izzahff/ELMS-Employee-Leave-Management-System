package elms.DAO;

import elms.model.HalfDay;
import elms.connection.ConnectionManager;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HalfDayDAO {
    
    /**
     * Create a new half day record in the database
     * @param halfDay HalfDay object containing the data
     * @return true if successfully created, false otherwise
     */
    public boolean createHalfDay(HalfDay halfDay) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            System.out.println("=== HalfDayDAO.createHalfDay called ===");
            System.out.println("Getting database connection...");
            
            conn = ConnectionManager.getConnection();
            System.out.println("Database connection obtained successfully");
            
            // SQL query to insert new half day record
            String sql = "INSERT INTO halfday (leaveTypeId, shift) VALUES (?, ?)";
            
            System.out.println("Preparing SQL statement: " + sql);
            System.out.println("Parameters:");
            System.out.println("1. leaveTypeId: " + halfDay.getLeaveTypeId());
            System.out.println("2. shift: " + halfDay.getShift());
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, halfDay.getLeaveTypeId());
            pstmt.setString(2, halfDay.getShift());
            
            System.out.println("Executing SQL statement...");
            int rowsAffected = pstmt.executeUpdate();
            System.out.println("Rows affected: " + rowsAffected);
            
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("SQLException in createHalfDay: " + e.getMessage());
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            System.err.println("General Exception in createHalfDay: " + e.getMessage());
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
     * Get half day details by leave type ID
     * @param leaveTypeId ID of the leave type
     * @return HalfDay object or null if not found
     */
    public HalfDay getHalfDayByLeaveTypeId(String leaveTypeId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            System.out.println("=== Getting HalfDay details for leaveTypeId: " + leaveTypeId + " ===");
            
            conn = ConnectionManager.getConnection();
            String sql = "SELECT leaveTypeId, shift FROM halfday WHERE leaveTypeId = ?";
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, leaveTypeId);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                HalfDay halfDay = new HalfDay();
                halfDay.setLeaveTypeId(rs.getString("leaveTypeId"));
                halfDay.setShift(rs.getString("shift"));
                
                System.out.println("Found HalfDay record: leaveTypeId=" + halfDay.getLeaveTypeId() + 
                                 ", shift=" + halfDay.getShift());
                return halfDay;
            } else {
                System.out.println("No HalfDay record found for leaveTypeId: " + leaveTypeId);
            }
            
        } catch (SQLException e) {
            System.err.println("Error retrieving half day details: " + e.getMessage());
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
     * Get all half day records
     * @return List of HalfDay objects
     */
    public List<HalfDay> getAllHalfDays() {
        List<HalfDay> halfDays = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = ConnectionManager.getConnection();
            String sql = "SELECT leaveTypeId, shift FROM halfday ORDER BY leaveTypeId";
            
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                HalfDay halfDay = new HalfDay();
                halfDay.setLeaveTypeId(rs.getString("leaveTypeId"));
                halfDay.setShift(rs.getString("shift"));
                
                halfDays.add(halfDay);
            }
            
            System.out.println("Retrieved " + halfDays.size() + " half day records");
            
        } catch (SQLException e) {
            System.err.println("Error retrieving all half day records: " + e.getMessage());
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
        
        return halfDays;
    }
    
    /**
     * Update half day shift
     * @param halfDay HalfDay object with updated data
     * @return true if successfully updated, false otherwise
     */
    public boolean updateHalfDay(HalfDay halfDay) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            System.out.println("=== Updating HalfDay record ===");
            
            conn = ConnectionManager.getConnection();
            String sql = "UPDATE halfday SET shift = ? WHERE leaveTypeId = ?";
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, halfDay.getShift());
            pstmt.setString(2, halfDay.getLeaveTypeId());
            
            System.out.println("Updating leaveTypeId: " + halfDay.getLeaveTypeId() + 
                             " with shift: " + halfDay.getShift());
            
            int rowsAffected = pstmt.executeUpdate();
            System.out.println("Rows affected: " + rowsAffected);
            
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating half day record: " + e.getMessage());
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
     * Delete a half day record by leave type ID
     * @param leaveTypeId ID of the leave type
     * @return true if successfully deleted, false otherwise
     */
    public boolean deleteHalfDay(String leaveTypeId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            System.out.println("=== Deleting HalfDay record for leaveTypeId: " + leaveTypeId + " ===");
            
            conn = ConnectionManager.getConnection();
            String sql = "DELETE FROM halfday WHERE leaveTypeId = ?";
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, leaveTypeId);
            
            int rowsAffected = pstmt.executeUpdate();
            System.out.println("Rows affected: " + rowsAffected);
            
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting half day record: " + e.getMessage());
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
            System.out.println("=== Testing HalfDayDAO database connection ===");
            conn = ConnectionManager.getConnection();
            if (conn != null && !conn.isClosed()) {
                System.out.println("HalfDayDAO database connection test successful!");
                return true;
            } else {
                System.out.println("HalfDayDAO database connection is null or closed");
                return false;
            }
        } catch (SQLException e) {
            System.err.println("SQLException during HalfDayDAO connection test: " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            System.err.println("General exception during HalfDayDAO connection test: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                    System.out.println("HalfDayDAO test connection closed successfully");
                }
            } catch (SQLException e) {
                System.err.println("Error closing HalfDayDAO test connection: " + e.getMessage());
            }
        }
    }
}
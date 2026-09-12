package elms.DAO;

import elms.model.FullDay;
import elms.model.HalfDay;
import elms.model.LeaveType;
import elms.connection.ConnectionManager;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class LeaveTypeDAO {
    
    /**
     * Get leave types by duration type using proper table joins
     * UPDATED: Now includes affectsBalance and fixedDuration fields
     * @param durationType Duration type to filter by ("Full Day" or "Half Day")
     * @return List of LeaveType objects
     */
    public List<LeaveType> getLeaveTypesByDurationType(String durationType) throws SQLException {
        System.out.println("=== Getting Leave Types by Duration Type: " + durationType + " ===");
        
        List<LeaveType> leaveTypes = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = ConnectionManager.getConnection();
            String sql;
            
            if ("Full Day".equalsIgnoreCase(durationType)) {
                sql = "SELECT lt.leaveTypeId, lt.adminId, lt.leaveTypeName, lt.leaveTypeDescription, " +
                      "lt.requiresDocument, lt.affectsBalance, lt.fixedDuration, fd.standardDuration " +
                      "FROM leavetype lt " +
                      "INNER JOIN fullday fd ON lt.leaveTypeId = fd.leaveTypeId " +
                      "ORDER BY lt.leaveTypeName";
                      
            } else if ("Half Day".equalsIgnoreCase(durationType)) {
                sql = "SELECT lt.leaveTypeId, lt.adminId, lt.leaveTypeName, lt.leaveTypeDescription, " +
                      "lt.requiresDocument, lt.affectsBalance, lt.fixedDuration, hd.shift " +
                      "FROM leavetype lt " +
                      "INNER JOIN halfday hd ON lt.leaveTypeId = hd.leaveTypeId " +
                      "ORDER BY lt.leaveTypeName";
                      
            } else {
                System.err.println("Invalid duration type: " + durationType);
                return leaveTypes;
            }
            
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                LeaveType leaveType = new LeaveType();
                leaveType.setLeaveTypeId(rs.getString("leaveTypeId"));
                leaveType.setAdminId(rs.getString("adminId"));
                leaveType.setLeaveTypeCategory(durationType);
                leaveType.setLeaveTypeName(rs.getString("leaveTypeName"));
                leaveType.setLeaveTypeDescription(rs.getString("leaveTypeDescription"));
                leaveType.setRequiresDocument(rs.getInt("requiresDocument") == 1);
                leaveType.setAffectsBalance(rs.getInt("affectsBalance") == 1);
                leaveType.setFixedDuration(rs.getInt("fixedDuration") == 1);
                
                leaveTypes.add(leaveType);
            }
            
            System.out.println("Found " + leaveTypes.size() + " leave types for duration type: " + durationType);
            
        } catch (SQLException e) {
            System.err.println("Error retrieving leave types by duration type: " + e.getMessage());
            e.printStackTrace();
            throw e;
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            }
        }
        
        return leaveTypes;
    }
    
    /**
     * Get full day details for multiple leave type IDs
     * @param leaveTypeIds List of leave type IDs
     * @return Map of leave type ID to FullDay object
     */
    public Map<String, FullDay> getFullDayDetailsByLeaveTypeIds(List<String> leaveTypeIds) throws SQLException {
        System.out.println("=== Getting Full Day Details for Leave Types ===");
        
        Map<String, FullDay> fullDayDetails = new HashMap<>();
        
        if (leaveTypeIds == null || leaveTypeIds.isEmpty()) {
            System.out.println("No leave type IDs provided");
            return fullDayDetails;
        }
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = ConnectionManager.getConnection();
            
            StringBuilder sqlBuilder = new StringBuilder();
            sqlBuilder.append("SELECT leaveTypeId, standardDuration FROM fullday WHERE leaveTypeId IN (");
            for (int i = 0; i < leaveTypeIds.size(); i++) {
                if (i > 0) sqlBuilder.append(",");
                sqlBuilder.append("?");
            }
            sqlBuilder.append(")");
            
            pstmt = conn.prepareStatement(sqlBuilder.toString());
            
            for (int i = 0; i < leaveTypeIds.size(); i++) {
                pstmt.setString(i + 1, leaveTypeIds.get(i));
            }
            
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                String leaveTypeId = rs.getString("leaveTypeId");
                int standardDuration = rs.getInt("standardDuration");
                
                FullDay fullDayLeaveType = new FullDay();
                fullDayLeaveType.setLeaveTypeId(leaveTypeId);
                fullDayLeaveType.setStandardDuration(standardDuration);
                
                fullDayDetails.put(leaveTypeId, fullDayLeaveType);
            }
            
            System.out.println("Found full day details for " + fullDayDetails.size() + " leave types");
            
        } catch (SQLException e) {
            System.err.println("Error retrieving full day details: " + e.getMessage());
            e.printStackTrace();
            throw e;
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            }
        }
        
        return fullDayDetails;
    }
    
    /**
     * Create a new leave type in the database and return the generated ID
     * UPDATED: Now includes requiresDocument, affectsBalance, and fixedDuration fields
     * @param leaveType LeaveType object containing the data
     * @return the generated leave type ID if successful, null otherwise
     */
    public String createLeaveTypeAndReturnId(LeaveType leaveType) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            System.out.println("=== LeaveTypeDAO.createLeaveTypeAndReturnId called ===");
            System.out.println("Requires Document: " + leaveType.isRequiresDocument());
            System.out.println("Affects Balance: " + leaveType.isAffectsBalance());
            System.out.println("Fixed Duration: " + leaveType.isFixedDuration());
            
            conn = ConnectionManager.getConnection();
            
            String insertSql = "INSERT INTO leavetype (adminId, leaveTypeCategory, leaveTypeName, leaveTypeDescription, requiresDocument, affectsBalance, fixedDuration) " +
                              "VALUES (?, ?, ?, ?, ?, ?, ?)";
            
            pstmt = conn.prepareStatement(insertSql);
            pstmt.setString(1, leaveType.getAdminId());
            pstmt.setString(2, leaveType.getLeaveTypeCategory());
            pstmt.setString(3, leaveType.getLeaveTypeName());
            pstmt.setString(4, leaveType.getLeaveTypeDescription());
            pstmt.setInt(5, leaveType.isRequiresDocument() ? 1 : 0);
            pstmt.setInt(6, leaveType.isAffectsBalance() ? 1 : 0);
            pstmt.setInt(7, leaveType.isFixedDuration() ? 1 : 0);
            
            int rowsAffected = pstmt.executeUpdate();
            System.out.println("Rows affected: " + rowsAffected);
            
            if (rowsAffected > 0) {
                pstmt.close();
                String selectSql = "SELECT leaveTypeId FROM leavetype WHERE leaveTypeName = ? AND adminId = ? ORDER BY leaveTypeId DESC";
                pstmt = conn.prepareStatement(selectSql);
                pstmt.setString(1, leaveType.getLeaveTypeName());
                pstmt.setString(2, leaveType.getAdminId());
                
                rs = pstmt.executeQuery();
                if (rs.next()) {
                    String generatedId = rs.getString("leaveTypeId");
                    System.out.println("Generated leave type ID: " + generatedId);
                    return generatedId;
                }
            }
            
            return null;
            
        } catch (SQLException e) {
            System.err.println("SQLException in createLeaveTypeAndReturnId: " + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            }
        }
    }
    
    /**
     * Get all leave types from the database
     * UPDATED: Now includes requiresDocument, affectsBalance, and fixedDuration fields
     * @return List of LeaveType objects
     */
    public List<LeaveType> getAllLeaveTypes() {
        List<LeaveType> leaveTypes = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = ConnectionManager.getConnection();
            String sql = "SELECT leaveTypeId, adminId, leaveTypeCategory, leaveTypeName, leaveTypeDescription, requiresDocument, affectsBalance, fixedDuration " +
                        "FROM leavetype " +
                        "ORDER BY leaveTypeId";
            
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                LeaveType leaveType = new LeaveType();
                leaveType.setLeaveTypeId(rs.getString("leaveTypeId"));
                leaveType.setAdminId(rs.getString("adminId"));
                leaveType.setLeaveTypeCategory(rs.getString("leaveTypeCategory"));
                leaveType.setLeaveTypeName(rs.getString("leaveTypeName"));
                leaveType.setLeaveTypeDescription(rs.getString("leaveTypeDescription"));
                leaveType.setRequiresDocument(rs.getInt("requiresDocument") == 1);
                leaveType.setAffectsBalance(rs.getInt("affectsBalance") == 1);
                leaveType.setFixedDuration(rs.getInt("fixedDuration") == 1);
                
                leaveTypes.add(leaveType);
            }
            
        } catch (SQLException e) {
            System.err.println("Error retrieving leave types: " + e.getMessage());
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
        
        return leaveTypes;
    }
    
    /**
     * Get a specific leave type by ID
     * UPDATED: Now includes requiresDocument, affectsBalance, and fixedDuration fields
     * @param leaveTypeId ID of the leave type
     * @return LeaveType object or null if not found
     */
    public LeaveType getLeaveTypeById(String leaveTypeId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = ConnectionManager.getConnection();
            String sql = "SELECT leaveTypeId, adminId, leaveTypeCategory, leaveTypeName, leaveTypeDescription, requiresDocument, affectsBalance, fixedDuration " +
                        "FROM leavetype " +
                        "WHERE leaveTypeId = ?";
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, leaveTypeId);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                LeaveType leaveType = new LeaveType();
                leaveType.setLeaveTypeId(rs.getString("leaveTypeId"));
                leaveType.setAdminId(rs.getString("adminId"));
                leaveType.setLeaveTypeCategory(rs.getString("leaveTypeCategory"));
                leaveType.setLeaveTypeName(rs.getString("leaveTypeName"));
                leaveType.setLeaveTypeDescription(rs.getString("leaveTypeDescription"));
                leaveType.setRequiresDocument(rs.getInt("requiresDocument") == 1);
                leaveType.setAffectsBalance(rs.getInt("affectsBalance") == 1);
                leaveType.setFixedDuration(rs.getInt("fixedDuration") == 1);
                
                return leaveType;
            }
            
        } catch (SQLException e) {
            System.err.println("Error retrieving leave type: " + e.getMessage());
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
     * Get leave type by name (for duplicate checking during edit)
     * Case-insensitive search with trimming
     * @param leaveTypeName Leave type name to search for
     * @return LeaveType object or null if not found
     */
    public LeaveType getLeaveTypeByName(String leaveTypeName) {
        System.out.println("=== Checking for Duplicate Leave Type Name: " + leaveTypeName + " ===");
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        LeaveType leaveType = null;
        
        try {
            conn = ConnectionManager.getConnection();
            if (conn == null) {
                System.err.println("❌ Failed to establish database connection");
                return null;
            }
            
            // Case-insensitive search using UPPER and TRIM
            String sql = "SELECT leaveTypeId, adminId, leaveTypeCategory, leaveTypeName, leaveTypeDescription, " +
                        "requiresDocument, affectsBalance, fixedDuration " +
                        "FROM leavetype " +
                        "WHERE UPPER(TRIM(leaveTypeName)) = UPPER(TRIM(?))";
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, leaveTypeName);
            
            System.out.println("🔍 Searching for: '" + leaveTypeName + "'");
            
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                leaveType = new LeaveType();
                leaveType.setLeaveTypeId(rs.getString("leaveTypeId"));
                leaveType.setAdminId(rs.getString("adminId"));
                leaveType.setLeaveTypeCategory(rs.getString("leaveTypeCategory"));
                leaveType.setLeaveTypeName(rs.getString("leaveTypeName"));
                leaveType.setLeaveTypeDescription(rs.getString("leaveTypeDescription"));
                leaveType.setRequiresDocument(rs.getInt("requiresDocument") == 1);
                leaveType.setAffectsBalance(rs.getInt("affectsBalance") == 1);
                leaveType.setFixedDuration(rs.getInt("fixedDuration") == 1);
                
                System.out.println("⚠️ DUPLICATE FOUND!");
                System.out.println("   Leave Type ID: " + leaveType.getLeaveTypeId());
                System.out.println("   Leave Type Name: " + leaveType.getLeaveTypeName());
            } else {
                System.out.println("✅ No duplicate found - name is unique");
            }
            
        } catch (SQLException e) {
            System.err.println("❌ SQL Error in getLeaveTypeByName: " + e.getMessage());
            e.printStackTrace();
            
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
        
        return leaveType;
    }
    
    /**
     * Update an existing leave type
     * UPDATED: Now includes requiresDocument, affectsBalance, and fixedDuration fields
     * @param leaveType LeaveType object with updated data
     * @return true if successfully updated, false otherwise
     */
    public boolean updateLeaveType(LeaveType leaveType) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = ConnectionManager.getConnection();
            String sql = "UPDATE leavetype SET leaveTypeCategory = ?, leaveTypeName = ?, " +
                        "leaveTypeDescription = ?, requiresDocument = ?, affectsBalance = ?, fixedDuration = ? " +
                        "WHERE leaveTypeId = ?";
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, leaveType.getLeaveTypeCategory());
            pstmt.setString(2, leaveType.getLeaveTypeName());
            pstmt.setString(3, leaveType.getLeaveTypeDescription());
            pstmt.setInt(4, leaveType.isRequiresDocument() ? 1 : 0);
            pstmt.setInt(5, leaveType.isAffectsBalance() ? 1 : 0);
            pstmt.setInt(6, leaveType.isFixedDuration() ? 1 : 0);
            pstmt.setString(7, leaveType.getLeaveTypeId());
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating leave type: " + e.getMessage());
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
     * Delete a leave type by ID
     * @param leaveTypeId ID of the leave type to delete
     * @return true if successfully deleted, false otherwise
     */
    public boolean deleteLeaveType(String leaveTypeId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = ConnectionManager.getConnection();
            String sql = "DELETE FROM leavetype WHERE leaveTypeId = ?";
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, leaveTypeId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting leave type: " + e.getMessage());
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
     * Check if a leave type name already exists
     * Handles multiple spaces, leading/trailing spaces, and case-insensitive comparison
     * @param leaveTypeName Name to check
     * @return true if exists, false otherwise
     */
    public boolean leaveTypeNameExists(String leaveTypeName) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = ConnectionManager.getConnection();
            
            // Normalize the input: trim and collapse multiple spaces to single space
            String normalizedInput = leaveTypeName.trim().replaceAll("\\s+", " ");
            
            System.out.println("🔍 Checking duplicate for: '" + normalizedInput + "'");
            
            // Use REGEXP_REPLACE to normalize spaces in database values for comparison
            // Oracle: REGEXP_REPLACE(string, pattern, replacement)
            String sql = "SELECT COUNT(*) FROM leavetype " +
                        "WHERE LOWER(REGEXP_REPLACE(TRIM(leavetypename), '\\s+', ' ')) = LOWER(?)";
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, normalizedInput);
            
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                int count = rs.getInt(1);
                if (count > 0) {
                    System.out.println("❌ DUPLICATE FOUND - '" + normalizedInput + "' already exists");
                } else {
                    System.out.println("✅ AVAILABLE - '" + normalizedInput + "' is unique");
                }
                return count > 0;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error checking leave type name existence: " + e.getMessage());
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
        
        return false;
    }
    
    /**
     * Get all leave types including those with NULL adminId
     * UPDATED: Now includes requiresDocument, affectsBalance, and fixedDuration fields
     * @return List of LeaveType objects
     */
    public List<LeaveType> getAllLeaveTypesIncludingOrphaned() {
        List<LeaveType> leaveTypes = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = ConnectionManager.getConnection();
            String sql = "SELECT leaveTypeId, adminId, leaveTypeCategory, leaveTypeName, leaveTypeDescription, requiresDocument, affectsBalance, fixedDuration " +
                        "FROM leavetype " +
                        "ORDER BY " +
                        "CASE WHEN adminId IS NULL THEN 1 ELSE 0 END, " +
                        "leaveTypeName";
            
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                LeaveType leaveType = new LeaveType();
                leaveType.setLeaveTypeId(rs.getString("leaveTypeId"));
                leaveType.setAdminId(rs.getString("adminId"));
                leaveType.setLeaveTypeCategory(rs.getString("leaveTypeCategory"));
                leaveType.setLeaveTypeName(rs.getString("leaveTypeName"));
                leaveType.setLeaveTypeDescription(rs.getString("leaveTypeDescription"));
                leaveType.setRequiresDocument(rs.getInt("requiresDocument") == 1);
                leaveType.setAffectsBalance(rs.getInt("affectsBalance") == 1);
                leaveType.setFixedDuration(rs.getInt("fixedDuration") == 1);
                
                leaveTypes.add(leaveType);
            }
            
        } catch (SQLException e) {
            System.err.println("Error retrieving leave types: " + e.getMessage());
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
        
        return leaveTypes;
    }

    /**
     * Update leave type to assign to a new admin
     * @param leaveTypeId ID of the leave type
     * @param newAdminId New admin ID to assign
     * @return true if successfully updated, false otherwise
     */
    public boolean assignLeaveTypeToAdmin(String leaveTypeId, String newAdminId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = ConnectionManager.getConnection();
            String sql = "UPDATE leavetype SET adminId = ? WHERE leaveTypeId = ?";
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, newAdminId);
            pstmt.setString(2, leaveTypeId);
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("✅ Leave type " + leaveTypeId + " assigned to admin " + newAdminId);
                return true;
            }
            
            return false;
            
        } catch (SQLException e) {
            System.err.println("Error assigning leave type to admin: " + e.getMessage());
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
     * Check if a leave type is orphaned (has NULL adminId)
     * @param leaveTypeId ID of the leave type
     * @return true if orphaned, false otherwise
     */
    public boolean isOrphanedLeaveType(String leaveTypeId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = ConnectionManager.getConnection();
            String sql = "SELECT adminId FROM leavetype WHERE leaveTypeId = ?";
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, leaveTypeId);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String adminId = rs.getString("adminId");
                return adminId == null;
            }
            
        } catch (SQLException e) {
            System.err.println("Error checking if leave type is orphaned: " + e.getMessage());
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
        
        return false;
    }
    
    /**
     * Test database connection
     * @return true if connection successful, false otherwise
     */
    public boolean testConnection() {
        Connection conn = null;
        try {
            System.out.println("=== Testing database connection ===");
            conn = ConnectionManager.getConnection();
            if (conn != null && !conn.isClosed()) {
                System.out.println("Database connection test successful!");
                return true;
            } else {
                System.out.println("Database connection is null or closed");
                return false;
            }
        } catch (SQLException e) {
            System.err.println("SQLException during connection test: " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            System.err.println("General exception during connection test: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                    System.out.println("Test connection closed successfully");
                }
            } catch (SQLException e) {
                System.err.println("Error closing test connection: " + e.getMessage());
            }
        }
    }
}
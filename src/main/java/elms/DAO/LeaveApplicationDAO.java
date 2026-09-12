package elms.DAO;

import elms.model.LeaveApplication;
import elms.connection.ConnectionManager;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO for managing leave applications - Updated with attachment support and additional methods
 */
public class LeaveApplicationDAO {
    
    /**
     * Create a new leave application with attachment support
     * @param leaveApplication The leave application object
     * @param shift The shift (for half-day leaves only) - collected but not stored
     * @return Success message with application ID or error message
     */
    public String createLeaveApplication(LeaveApplication leaveApplication, String shift) throws SQLException {
        System.out.println("=== Creating Leave Application with Attachment Support ===");
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String generatedApplicationId = null;
        
        try {
            conn = ConnectionManager.getConnection();
            if (conn == null) {
                throw new SQLException("Failed to establish database connection");
            }
            
            // Start transaction
            conn.setAutoCommit(false);
            
            // Step 1: Get the next sequence value
            System.out.println("🔢 Getting next value from leaveapplication_id_seq...");
            String getSequenceSql = "SELECT leaveapplication_id_seq.NEXTVAL FROM DUAL";
            
            pstmt = conn.prepareStatement(getSequenceSql);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                long sequenceValue = rs.getLong(1);
                generatedApplicationId = String.valueOf(sequenceValue);
                System.out.println("✅ Generated Application ID: " + generatedApplicationId);
            } else {
                throw new SQLException("Failed to get next sequence value");
            }
            
            rs.close();
            pstmt.close();
            
            // Step 2: Check if this is a half-day leave type and set duration accordingly
            boolean isHalfDay = isHalfDayLeaveType(conn, leaveApplication.getLeavetypeid());
            
            if (isHalfDay) {
                // For half-day leave types, always set duration to 0.5
                leaveApplication.setLeaveduration(0.5); // Note: This requires your LeaveApplication model to support double/decimal
                System.out.println("✅ Half-day leave detected - setting duration to 0.5 days");
            } else {
                System.out.println("✅ Full-day leave - using original duration: " + leaveApplication.getLeaveduration());
            }
            
            // Step 3: Insert the leave application with attachment
            System.out.println("💾 Inserting leave application with ID: " + generatedApplicationId);
            
            String currentTimestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
            
            // UPDATED: Added attachment column to INSERT statement
            String insertSql = 
                "INSERT INTO leaveapplication (applicationid, employeeid, leavetypeid, leavestartdate, " +
                "leaveenddate, leavestatus, leavereason, appliedon, leaveduration, attachment) " +
                "VALUES (?, ?, ?, TO_DATE(?, 'YYYY-MM-DD'), TO_DATE(?, 'YYYY-MM-DD'), ?, ?, " +
                "TO_TIMESTAMP(?, 'YYYY-MM-DD HH24:MI:SS'), ?, ?)";
            
            pstmt = conn.prepareStatement(insertSql);
            pstmt.setString(1, generatedApplicationId);
            pstmt.setString(2, leaveApplication.getEmployeeid());
            pstmt.setString(3, leaveApplication.getLeavetypeid());
            pstmt.setString(4, leaveApplication.getLeavestartdate());
            pstmt.setString(5, leaveApplication.getLeaveenddate());
            pstmt.setString(6, leaveApplication.getLeavestatus());
            pstmt.setString(7, leaveApplication.getLeavereason());
            pstmt.setString(8, currentTimestamp);
            
            // UPDATED: Handle both integer and decimal duration
            if (isHalfDay) {
                pstmt.setDouble(9, 0.5);
                System.out.println("⏱️ Duration set to: 0.5 days (half-day)");
            } else {
                pstmt.setDouble(9, leaveApplication.getLeaveduration());
                System.out.println("⏱️ Duration set to: " + leaveApplication.getLeaveduration() + " days (full-day)");
            }
            
            // UPDATED: Handle attachment - set to null if not provided
            if (leaveApplication.getAttachment() != null && !leaveApplication.getAttachment().trim().isEmpty()) {
                pstmt.setString(10, leaveApplication.getAttachment());
                System.out.println("📎 Attachment included: " + leaveApplication.getAttachment());
            } else {
                pstmt.setNull(10, Types.VARCHAR);
                System.out.println("📎 No attachment provided");
            }
            
            System.out.println("📋 Inserting application for employee: " + leaveApplication.getEmployeeid());
            System.out.println("📅 Leave dates: " + leaveApplication.getLeavestartdate() + " to " + leaveApplication.getLeaveenddate());
            
            if (shift != null && !shift.trim().isEmpty()) {
                System.out.println("🔄 Shift information collected: " + shift + " (not stored in database)");
            }
            
            int rowsAffected = pstmt.executeUpdate();
            pstmt.close();
            
            if (rowsAffected > 0) {
                System.out.println("✅ Leave application inserted successfully");
                
                // Commit transaction
                conn.commit();
                System.out.println("✅ Leave application created successfully with ID: " + generatedApplicationId);
                
                return "success: Leave application created with ID " + generatedApplicationId;
                
            } else {
                conn.rollback();
                System.err.println("❌ Insert failed - no rows affected");
                return "error: Failed to create leave application";
            }
            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                    System.out.println("🔄 Transaction rolled back due to error");
                } catch (SQLException ex) {
                    System.err.println("❌ Error during rollback: " + ex.getMessage());
                }
            }
            System.err.println("❌ SQL Error in createLeaveApplication: " + e.getMessage());
            e.printStackTrace();
            throw e;
            
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                System.err.println("❌ Error closing resources: " + e.getMessage());
            }
        }
    }
    
   
    
    /**
     * Check if a leave type is half-day by checking the leavetype table
     * @param conn Database connection
     * @param leaveTypeId Leave type ID to check
     * @return true if half-day, false if full-day
     */
    private boolean isHalfDayLeaveType(Connection conn, String leaveTypeId) throws SQLException {
        System.out.println("🔍 Checking if leave type " + leaveTypeId + " is half-day...");
        
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            String sql = "SELECT leaveTypeCategory FROM leavetype WHERE leaveTypeId = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, leaveTypeId);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String leaveType = rs.getString("leaveTypeCategory");
                boolean isHalfDay = "Half Day".equalsIgnoreCase(leaveType);
                System.out.println("✅ Leave type " + leaveTypeId + " is: " + leaveType + " (Half-day: " + isHalfDay + ")");
                return isHalfDay;
            } else {
                System.err.println("⚠️ Leave type " + leaveTypeId + " not found, defaulting to full-day");
                return false;
            }
            
        } finally {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
        }
    }
    
    /**
     * Update employee leave balance
     * @param employeeId Employee ID
     * @param newBalance New leave balance
     * @return Success or error message
     */
    public String updateEmployeeLeaveBalance(String employeeId, int newBalance) throws SQLException {
        System.out.println("=== Updating Employee Leave Balance ===");
        System.out.println("Employee ID: " + employeeId + ", New Balance: " + newBalance);
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = ConnectionManager.getConnection();
            if (conn == null) {
                throw new SQLException("Failed to establish database connection");
            }
            
            String updateSql = "UPDATE employee SET employeeleavebalance = ? WHERE employeeid = ?";
            pstmt = conn.prepareStatement(updateSql);
            pstmt.setInt(1, newBalance);
            pstmt.setString(2, employeeId);
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("✅ Employee leave balance updated successfully");
                return "success: Leave balance updated";
            } else {
                System.err.println("❌ No employee found with ID: " + employeeId);
                return "error: Employee not found";
            }
            
        } catch (SQLException e) {
            System.err.println("❌ SQL Error in updateEmployeeLeaveBalance: " + e.getMessage());
            e.printStackTrace();
            throw e;
            
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
    }
    
    /**
     * Get leave application by ID - UPDATED with reviewdate and rejectreason support
     */
    public LeaveApplication getLeaveApplicationById(String applicationId) throws SQLException {
        System.out.println("=== Getting Leave Application by ID: " + applicationId + " ===");
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        LeaveApplication leaveApplication = null;
        
        try {
            conn = ConnectionManager.getConnection();
            if (conn == null) {
                throw new SQLException("Failed to establish database connection");
            }
            
            // UPDATED: Include reviewdate and rejectreason in SELECT query
            String selectSql = "SELECT * FROM leaveapplication WHERE applicationid = ?";
            
            pstmt = conn.prepareStatement(selectSql);
            pstmt.setString(1, applicationId);
            
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                leaveApplication = new LeaveApplication();
                leaveApplication.setApplicationid(rs.getString("applicationid"));
                leaveApplication.setEmployeeid(rs.getString("employeeid"));
                leaveApplication.setLeavetypeid(rs.getString("leavetypeid"));
                
                // Handle date conversion
                Date startDate = rs.getDate("leavestartdate");
                Date endDate = rs.getDate("leaveenddate");
                Timestamp appliedOn = rs.getTimestamp("appliedon");
                Timestamp reviewDate = rs.getTimestamp("reviewdate");
                
                if (startDate != null) {
                    leaveApplication.setLeavestartdate(new SimpleDateFormat("yyyy-MM-dd").format(startDate));
                }
                if (endDate != null) {
                    leaveApplication.setLeaveenddate(new SimpleDateFormat("yyyy-MM-dd").format(endDate));
                }
                if (appliedOn != null) {
                    leaveApplication.setAppliedon(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(appliedOn));
                }
                if (reviewDate != null) {
                    leaveApplication.setReviewdate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(reviewDate));
                }
                
                leaveApplication.setLeavestatus(rs.getString("leavestatus"));
                leaveApplication.setManagerid(rs.getString("managerid"));
                leaveApplication.setLeavereason(rs.getString("leavereason"));
                leaveApplication.setLeaveduration(rs.getDouble("leaveduration"));
                leaveApplication.setAttachment(rs.getString("attachment"));
                leaveApplication.setRejectReason(rs.getString("rejectreason"));
                
                System.out.println("✅ Leave application found: " + applicationId);
                System.out.println("📋 Manager ID: " + leaveApplication.getManagerid());
                System.out.println("📅 Review Date: " + leaveApplication.getReviewdate());
                if (leaveApplication.getAttachment() != null) {
                    System.out.println("📎 Attachment: " + leaveApplication.getAttachment());
                }
                if (leaveApplication.getRejectReason() != null) {
                    System.out.println("❌ Reject Reason: " + leaveApplication.getRejectReason());
                }
            } else {
                System.out.println("❌ No leave application found with ID: " + applicationId);
            }
            
        } catch (SQLException e) {
            System.err.println("❌ SQL Error in getLeaveApplicationById: " + e.getMessage());
            e.printStackTrace();
            throw e;
            
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
        
        return leaveApplication;
    }
    
    /**
     * Get all leave applications for an employee - UPDATED with attachment support
     * @param employeeId Employee ID
     * @return List of leave applications
     */
    public java.util.List<LeaveApplication> getLeaveApplicationsByEmployee(String employeeId) throws SQLException {
        System.out.println("=== Getting Leave Applications for Employee: " + employeeId + " ===");
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        java.util.List<LeaveApplication> applications = new java.util.ArrayList<>();
        
        try {
            conn = ConnectionManager.getConnection();
            if (conn == null) {
                throw new SQLException("Failed to establish database connection");
            }
            
            // UPDATED: Include attachment in SELECT query
            String selectSql = 
                "SELECT la.*, lt.leavetypename " +
                "FROM leaveapplication la " +
                "LEFT JOIN leavetype lt ON la.leavetypeid = lt.leavetypeid " +
                "WHERE la.employeeid = ? " +
                "ORDER BY la.appliedon DESC";
            
            pstmt = conn.prepareStatement(selectSql);
            pstmt.setString(1, employeeId);
            
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                LeaveApplication leaveApplication = new LeaveApplication();
                leaveApplication.setApplicationid(rs.getString("applicationid"));
                leaveApplication.setEmployeeid(rs.getString("employeeid"));
                leaveApplication.setLeavetypeid(rs.getString("leavetypeid"));
                
                // Handle date conversion
                Date startDate = rs.getDate("leavestartdate");
                Date endDate = rs.getDate("leaveenddate");
                Timestamp appliedOn = rs.getTimestamp("appliedon");
                
                if (startDate != null) {
                    leaveApplication.setLeavestartdate(new SimpleDateFormat("yyyy-MM-dd").format(startDate));
                }
                if (endDate != null) {
                    leaveApplication.setLeaveenddate(new SimpleDateFormat("yyyy-MM-dd").format(endDate));
                }
                if (appliedOn != null) {
                    leaveApplication.setAppliedon(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(appliedOn));
                }
                
                leaveApplication.setLeavestatus(rs.getString("leavestatus"));
                leaveApplication.setManagerid(rs.getString("managerid"));
                leaveApplication.setLeavereason(rs.getString("leavereason"));
                leaveApplication.setLeaveduration(rs.getDouble("leaveduration")); // UPDATED: Handle decimal duration
             
                
                // UPDATED: Handle attachment
                leaveApplication.setAttachment(rs.getString("attachment"));
                
                applications.add(leaveApplication);
            }
            
            System.out.println("Found " + applications.size() + " leave applications for employee " + employeeId);
            
        } catch (SQLException e) {
            System.err.println("SQL Error in getLeaveApplicationsByEmployee: " + e.getMessage());
            e.printStackTrace();
            throw e;
            
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
        
        return applications;
    }
    
    /**
     * NEW METHOD: Update leave application status (for cancelling, approving, etc.)
     * @param applicationId Application ID
     * @param newStatus New status to set
     * @return Success or error message
     */
    public String updateLeaveApplicationStatus(String applicationId, String newStatus) throws SQLException {
        System.out.println("=== Updating Leave Application Status ===");
        System.out.println("Application ID: " + applicationId + ", New Status: " + newStatus);
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = ConnectionManager.getConnection();
            if (conn == null) {
                throw new SQLException("Failed to establish database connection");
            }
            
            String updateSql = "UPDATE leaveapplication SET leavestatus = ? WHERE applicationid = ?";
            pstmt = conn.prepareStatement(updateSql);
            pstmt.setString(1, newStatus);
            pstmt.setString(2, applicationId);
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("✅ Leave application status updated successfully");
                return "success: Leave application status updated to " + newStatus;
            } else {
                System.err.println("❌ No application found with ID: " + applicationId);
                return "error: Leave application not found";
            }
            
        } catch (SQLException e) {
            System.err.println("❌ SQL Error in updateLeaveApplicationStatus: " + e.getMessage());
            e.printStackTrace();
            throw e;
            
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
    }
    
    /**
     * Check if employee has sufficient leave balance - UPDATED to handle decimal durations
     * @param employeeId Employee ID
     * @param requestedDays Requested leave days (can be decimal for half-days)
     * @return true if sufficient balance, false otherwise
     */
    public boolean checkLeaveBalance(String employeeId, double requestedDays) throws SQLException {
        System.out.println("=== Checking Leave Balance ===");
        System.out.println("Employee ID: " + employeeId + ", Requested Days: " + requestedDays);
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = ConnectionManager.getConnection();
            if (conn == null) {
                throw new SQLException("Failed to establish database connection");
            }
            
            String selectSql = "SELECT employeeleavebalance FROM employee WHERE employeeid = ?";
            pstmt = conn.prepareStatement(selectSql);
            pstmt.setString(1, employeeId);
            
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                double currentBalance = rs.getDouble("employeeleavebalance");
                System.out.println("Current balance: " + currentBalance);
                return currentBalance >= requestedDays;
            } else {
                System.err.println("Employee not found: " + employeeId);
                return false;
            }
            
        } catch (SQLException e) {
            System.err.println("SQL Error in checkLeaveBalance: " + e.getMessage());
            e.printStackTrace();
            throw e;
            
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
    }
    
    /**
     * NEW METHOD: Update attachment for existing leave application
     * @param applicationId Application ID
     * @param attachmentPath Path to attachment file
     * @return Success or error message
     */
    public String updateAttachment(String applicationId, String attachmentPath) throws SQLException {
        System.out.println("=== Updating Attachment for Application: " + applicationId + " ===");
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = ConnectionManager.getConnection();
            if (conn == null) {
                throw new SQLException("Failed to establish database connection");
            }
            
            String updateSql = "UPDATE leaveapplication SET attachment = ? WHERE applicationid = ?";
            pstmt = conn.prepareStatement(updateSql);
            
            if (attachmentPath != null && !attachmentPath.trim().isEmpty()) {
                pstmt.setString(1, attachmentPath);
            } else {
                pstmt.setNull(1, Types.VARCHAR);
            }
            pstmt.setString(2, applicationId);
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("✅ Attachment updated successfully");
                return "success: Attachment updated";
            } else {
                System.err.println("❌ No application found with ID: " + applicationId);
                return "error: Application not found";
            }
            
        } catch (SQLException e) {
            System.err.println("❌ SQL Error in updateAttachment: " + e.getMessage());
            e.printStackTrace();
            throw e;
            
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
    }
    
    /**
     * NEW METHOD: Remove attachment from leave application
     * @param applicationId Application ID
     * @return Success or error message
     */
    public String removeAttachment(String applicationId) throws SQLException {
        return updateAttachment(applicationId, null);
    }
    
    /**
     * NEW METHOD: Get all leave applications with optional filters
     * @param employeeId Employee ID (optional - null for all employees)
     * @param status Status filter (optional - null for all statuses)
     * @param leaveTypeId Leave type filter (optional - null for all types)
     * @return List of leave applications
     */
    public java.util.List<LeaveApplication> getLeaveApplicationsWithFilters(String employeeId, String status, String leaveTypeId) throws SQLException {
        System.out.println("=== Getting Leave Applications with Filters ===");
        System.out.println("Employee ID: " + employeeId + ", Status: " + status + ", Leave Type: " + leaveTypeId);
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        java.util.List<LeaveApplication> applications = new java.util.ArrayList<>();
        
        try {
            conn = ConnectionManager.getConnection();
            if (conn == null) {
                throw new SQLException("Failed to establish database connection");
            }
            
            // Build dynamic SQL based on filters
            StringBuilder sqlBuilder = new StringBuilder();
            sqlBuilder.append("SELECT la.*, lt.leavetypename ");
            sqlBuilder.append("FROM leaveapplication la ");
            sqlBuilder.append("LEFT JOIN leavetype lt ON la.leavetypeid = lt.leavetypeid ");
            sqlBuilder.append("WHERE 1=1 ");
            
            java.util.List<String> parameters = new java.util.ArrayList<>();
            
            if (employeeId != null && !employeeId.trim().isEmpty()) {
                sqlBuilder.append("AND la.employeeid = ? ");
                parameters.add(employeeId);
            }
            
            if (status != null && !status.trim().isEmpty() && !"all".equalsIgnoreCase(status)) {
                sqlBuilder.append("AND UPPER(la.leavestatus) = UPPER(?) ");
                parameters.add(status);
            }
            
            if (leaveTypeId != null && !leaveTypeId.trim().isEmpty() && !"all".equalsIgnoreCase(leaveTypeId)) {
                sqlBuilder.append("AND la.leavetypeid = ? ");
                parameters.add(leaveTypeId);
            }
            
            sqlBuilder.append("ORDER BY la.appliedon DESC");
            
            String sql = sqlBuilder.toString();
            System.out.println("Generated SQL: " + sql);
            
            pstmt = conn.prepareStatement(sql);
            
            // Set parameters
            for (int i = 0; i < parameters.size(); i++) {
                pstmt.setString(i + 1, parameters.get(i));
                System.out.println("Parameter " + (i + 1) + ": " + parameters.get(i));
            }
            
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                LeaveApplication leaveApplication = new LeaveApplication();
                leaveApplication.setApplicationid(rs.getString("applicationid"));
                leaveApplication.setEmployeeid(rs.getString("employeeid"));
                leaveApplication.setLeavetypeid(rs.getString("leavetypeid"));
                
                // Handle date conversion
                Date startDate = rs.getDate("leavestartdate");
                Date endDate = rs.getDate("leaveenddate");
                Timestamp appliedOn = rs.getTimestamp("appliedon");
                
                if (startDate != null) {
                    leaveApplication.setLeavestartdate(new SimpleDateFormat("yyyy-MM-dd").format(startDate));
                }
                if (endDate != null) {
                    leaveApplication.setLeaveenddate(new SimpleDateFormat("yyyy-MM-dd").format(endDate));
                }
                if (appliedOn != null) {
                    leaveApplication.setAppliedon(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(appliedOn));
                }
                
                leaveApplication.setLeavestatus(rs.getString("leavestatus"));
                leaveApplication.setManagerid(rs.getString("managerid"));
                leaveApplication.setLeavereason(rs.getString("leavereason"));
                leaveApplication.setLeaveduration(rs.getDouble("leaveduration")); // UPDATED: Handle decimal duration
                leaveApplication.setAttachment(rs.getString("attachment"));
                
                applications.add(leaveApplication);
            }
            
            System.out.println("Found " + applications.size() + " leave applications with filters");
            
        } catch (SQLException e) {
            System.err.println("SQL Error in getLeaveApplicationsWithFilters: " + e.getMessage());
            e.printStackTrace();
            throw e;
            
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
        
        return applications;
    }
    
    /**
     * UPDATED: Cancel leave application method - no balance restoration needed
     * since no balance was deducted on submission
     * Add this method to your LeaveApplicationDAO class (replace the existing cancelLeaveApplicationWithBalanceRestore method)
     */
    public String cancelLeaveApplication(String applicationId, String employeeId) throws SQLException {
        System.out.println("=== CANCELLING LEAVE APPLICATION (NO BALANCE CHANGES) ===");
        System.out.println("Application ID: " + applicationId + ", Employee ID: " + employeeId);
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = ConnectionManager.getConnection();
            if (conn == null) {
                throw new SQLException("Failed to establish database connection");
            }
            
            // Start transaction
            conn.setAutoCommit(false);
            
            // Step 1: Get application details and verify ownership
            System.out.println("🔍 Fetching application details...");
            String getAppSql = 
                "SELECT la.*, lt.leavetypename " +
                "FROM leaveapplication la " +
                "JOIN leavetype lt ON la.leavetypeid = lt.leavetypeid " +
                "WHERE la.applicationid = ? AND la.employeeid = ?";
            
            pstmt = conn.prepareStatement(getAppSql);
            pstmt.setString(1, applicationId);
            pstmt.setString(2, employeeId);
            
            rs = pstmt.executeQuery();
            
            if (!rs.next()) {
                conn.rollback();
                System.out.println("❌ Application not found or access denied");
                return "error: Leave application not found or access denied";
            }
            
            String currentStatus = rs.getString("leavestatus");
            String leaveTypeName = rs.getString("leavetypename");
            double leaveDuration = rs.getDouble("leaveduration");
            
            System.out.println("📋 Application Details:");
            System.out.println("  Current Status: " + currentStatus);
            System.out.println("  Leave Type: " + leaveTypeName);
            System.out.println("  Duration: " + leaveDuration + " days");
            
            // Check if application can be cancelled
            if (!"Pending".equalsIgnoreCase(currentStatus)) {
                conn.rollback();
                System.out.println("❌ Cannot cancel - status is " + currentStatus);
                return "error: Only pending applications can be cancelled. Current status: " + currentStatus;
            }
            
            rs.close();
            pstmt.close();
            
            // Step 2: Update application status to Cancelled
            System.out.println("🔄 Updating application status to Cancelled...");
            String updateStatusSql = "UPDATE leaveapplication SET leavestatus = ? WHERE applicationid = ?";
            pstmt = conn.prepareStatement(updateStatusSql);
            pstmt.setString(1, "Cancelled");
            pstmt.setString(2, applicationId);
            
            int statusUpdateRows = pstmt.executeUpdate();
            pstmt.close();
            
            if (statusUpdateRows == 0) {
                conn.rollback();
                System.out.println("❌ Failed to update application status");
                return "error: Failed to update application status";
            }
            
            System.out.println("✅ Application status updated to Cancelled");
            
            // Step 3: No balance restoration needed since no deduction was made on submission
            boolean isAnnualLeave = "Annual Leave".equalsIgnoreCase(leaveTypeName);
            String resultMessage;
            
            if (isAnnualLeave) {
                System.out.println("ℹ️ Annual Leave: No balance restoration needed since no deduction was made on submission");
                resultMessage = "Annual Leave application cancelled successfully. Your leave balance remains unchanged since no deduction was made during submission.";
            } else {
                System.out.println("ℹ️ Non-Annual Leave: No balance changes needed for " + leaveTypeName);
                resultMessage = leaveTypeName + " application cancelled successfully. No leave balance changes required.";
            }
            
            // Commit transaction
            conn.commit();
            System.out.println("✅ Transaction committed successfully");
            
            return "success: " + resultMessage;
            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                    System.out.println("🔄 Transaction rolled back due to error");
                } catch (SQLException ex) {
                    System.err.println("❌ Error during rollback: " + ex.getMessage());
                }
            }
            System.err.println("❌ SQL Error in cancelLeaveApplication: " + e.getMessage());
            e.printStackTrace();
            throw e;
            
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                System.err.println("❌ Error closing resources: " + e.getMessage());
            }
        }
    }
    
 

 /**
  * NEW METHOD: Update leave application status with rejection reason
  * @param applicationId Application ID
  * @param newStatus New status to set
  * @param rejectReason Reason for rejection (can be null for non-rejection updates)
  * @return Success or error message
  */
 public String updateLeaveApplicationStatusWithReason(String applicationId, String newStatus, String rejectReason) throws SQLException {
     System.out.println("=== Updating Leave Application Status with Reject Reason ===");
     System.out.println("Application ID: " + applicationId + ", New Status: " + newStatus);
     if (rejectReason != null) {
         System.out.println("Reject Reason: " + rejectReason);
     }
     
     Connection conn = null;
     PreparedStatement pstmt = null;
     
     try {
         conn = ConnectionManager.getConnection();
         if (conn == null) {
             throw new SQLException("Failed to establish database connection");
         }
         
         String updateSql = "UPDATE leaveapplication SET leavestatus = ?, rejectreason = ? WHERE applicationid = ?";
         pstmt = conn.prepareStatement(updateSql);
         pstmt.setString(1, newStatus);
         
         // Set reject reason only for rejected applications
         if ("Rejected".equalsIgnoreCase(newStatus) && rejectReason != null && !rejectReason.trim().isEmpty()) {
             pstmt.setString(2, rejectReason);
         } else {
             pstmt.setNull(2, Types.VARCHAR); // Clear reject reason for non-rejected status
         }
         
         pstmt.setString(3, applicationId);
         
         int rowsAffected = pstmt.executeUpdate();
         
         if (rowsAffected > 0) {
             System.out.println("✅ Leave application status updated successfully");
             return "success: Leave application status updated to " + newStatus;
         } else {
             System.err.println("❌ No application found with ID: " + applicationId);
             return "error: Leave application not found";
         }
         
     } catch (SQLException e) {
         System.err.println("❌ SQL Error in updateLeaveApplicationStatusWithReason: " + e.getMessage());
         e.printStackTrace();
         throw e;
         
     } finally {
         try {
             if (pstmt != null) pstmt.close();
             if (conn != null) conn.close();
         } catch (SQLException e) {
             System.err.println("Error closing resources: " + e.getMessage());
         }
     }
 }

 /**
  * NEW METHOD: Get leave application by ID with reject reason - Enhanced version
  * @param applicationId Application ID
  * @return LeaveApplication object or null if not found
  */
 public LeaveApplication getLeaveApplicationByIdWithReason(String applicationId) throws SQLException {
     System.out.println("=== Getting Leave Application by ID with Reject Reason: " + applicationId + " ===");
     
     Connection conn = null;
     PreparedStatement pstmt = null;
     ResultSet rs = null;
     LeaveApplication leaveApplication = null;
     
     try {
         conn = ConnectionManager.getConnection();
         if (conn == null) {
             throw new SQLException("Failed to establish database connection");
         }
         
         // Include rejectreason in SELECT query
         String selectSql = "SELECT applicationid, employeeid, leavetypeid, leavestartdate, leaveenddate, " +
                           "leavestatus, managerid, leavereason, leaveduration, attachment, appliedon, rejectreason " +
                           "FROM leaveapplication WHERE applicationid = ?";
         
         pstmt = conn.prepareStatement(selectSql);
         pstmt.setString(1, applicationId);
         
         rs = pstmt.executeQuery();
         
         if (rs.next()) {
             leaveApplication = new LeaveApplication();
             leaveApplication.setApplicationid(rs.getString("applicationid"));
             leaveApplication.setEmployeeid(rs.getString("employeeid"));
             leaveApplication.setLeavetypeid(rs.getString("leavetypeid"));
             
             // Handle date conversion
             Date startDate = rs.getDate("leavestartdate");
             Date endDate = rs.getDate("leaveenddate");
             Timestamp appliedOn = rs.getTimestamp("appliedon");
             
             if (startDate != null) {
                 leaveApplication.setLeavestartdate(new SimpleDateFormat("yyyy-MM-dd").format(startDate));
             }
             if (endDate != null) {
                 leaveApplication.setLeaveenddate(new SimpleDateFormat("yyyy-MM-dd").format(endDate));
             }
             if (appliedOn != null) {
                 leaveApplication.setAppliedon(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(appliedOn));
             }
             
             leaveApplication.setLeavestatus(rs.getString("leavestatus"));
             leaveApplication.setManagerid(rs.getString("managerid"));
             leaveApplication.setLeavereason(rs.getString("leavereason"));
             leaveApplication.setLeaveduration(rs.getDouble("leaveduration"));
             leaveApplication.setAttachment(rs.getString("attachment"));
             
             // Set reject reason
             leaveApplication.setRejectReason(rs.getString("rejectreason"));
             
             System.out.println("✅ Leave application found: " + applicationId);
             if (leaveApplication.getRejectReason() != null) {
                 System.out.println("📝 Reject reason: " + leaveApplication.getRejectReason());
             }
         } else {
             System.out.println("❌ No leave application found with ID: " + applicationId);
         }
         
     } catch (SQLException e) {
         System.err.println("❌ SQL Error in getLeaveApplicationByIdWithReason: " + e.getMessage());
         e.printStackTrace();
         throw e;
         
     } finally {
         try {
             if (rs != null) rs.close();
             if (pstmt != null) pstmt.close();
             if (conn != null) conn.close();
         } catch (SQLException e) {
             System.err.println("Error closing resources: " + e.getMessage());
         }
     }
     
     return leaveApplication;
 }

 /**
  * NEW METHOD: Get all leave applications for an employee with reject reason
  * @param employeeId Employee ID
  * @return List of leave applications with reject reasons
  */
 public java.util.List<LeaveApplication> getLeaveApplicationsByEmployeeWithReason(String employeeId) throws SQLException {
     System.out.println("=== Getting Leave Applications for Employee with Reject Reason: " + employeeId + " ===");
     
     Connection conn = null;
     PreparedStatement pstmt = null;
     ResultSet rs = null;
     java.util.List<LeaveApplication> applications = new java.util.ArrayList<>();
     
     try {
         conn = ConnectionManager.getConnection();
         if (conn == null) {
             throw new SQLException("Failed to establish database connection");
         }
         
         // Include rejectreason in SELECT query
         String selectSql = 
             "SELECT la.applicationid, la.employeeid, la.leavetypeid, la.leavestartdate, la.leaveenddate, " +
             "la.leavestatus, la.managerid, la.leavereason, la.leaveduration, la.attachment, la.appliedon, " +
             "la.rejectreason, lt.leavetypename " +
             "FROM leaveapplication la " +
             "LEFT JOIN leavetype lt ON la.leavetypeid = lt.leavetypeid " +
             "WHERE la.employeeid = ? " +
             "ORDER BY la.appliedon DESC";
         
         pstmt = conn.prepareStatement(selectSql);
         pstmt.setString(1, employeeId);
         
         rs = pstmt.executeQuery();
         
         while (rs.next()) {
             LeaveApplication leaveApplication = new LeaveApplication();
             leaveApplication.setApplicationid(rs.getString("applicationid"));
             leaveApplication.setEmployeeid(rs.getString("employeeid"));
             leaveApplication.setLeavetypeid(rs.getString("leavetypeid"));
             
             // Handle date conversion
             Date startDate = rs.getDate("leavestartdate");
             Date endDate = rs.getDate("leaveenddate");
             Timestamp appliedOn = rs.getTimestamp("appliedon");
             
             if (startDate != null) {
                 leaveApplication.setLeavestartdate(new SimpleDateFormat("yyyy-MM-dd").format(startDate));
             }
             if (endDate != null) {
                 leaveApplication.setLeaveenddate(new SimpleDateFormat("yyyy-MM-dd").format(endDate));
             }
             if (appliedOn != null) {
                 leaveApplication.setAppliedon(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(appliedOn));
             }
             
             leaveApplication.setLeavestatus(rs.getString("leavestatus"));
             leaveApplication.setManagerid(rs.getString("managerid"));
             leaveApplication.setLeavereason(rs.getString("leavereason"));
             leaveApplication.setLeaveduration(rs.getDouble("leaveduration"));
             leaveApplication.setAttachment(rs.getString("attachment"));
             
             // Set reject reason
             leaveApplication.setRejectReason(rs.getString("rejectreason"));
             
             applications.add(leaveApplication);
         }
         
         System.out.println("Found " + applications.size() + " leave applications for employee " + employeeId);
         
     } catch (SQLException e) {
         System.err.println("SQL Error in getLeaveApplicationsByEmployeeWithReason: " + e.getMessage());
         e.printStackTrace();
         throw e;
         
     } finally {
         try {
             if (rs != null) rs.close();
             if (pstmt != null) pstmt.close();
             if (conn != null) conn.close();
         } catch (SQLException e) {
             System.err.println("Error closing resources: " + e.getMessage());
         }
     }
     
     return applications;
 }

 /**
  * NEW METHOD: Get all leave applications with optional filters including reject reason
  * @param employeeId Employee ID (optional - null for all employees)
  * @param status Status filter (optional - null for all statuses)
  * @param leaveTypeId Leave type filter (optional - null for all types)
  * @return List of leave applications with reject reasons
  */
 public java.util.List<LeaveApplication> getLeaveApplicationsWithFiltersAndReason(String employeeId, String status, String leaveTypeId) throws SQLException {
     System.out.println("=== Getting Leave Applications with Filters and Reject Reason ===");
     System.out.println("Employee ID: " + employeeId + ", Status: " + status + ", Leave Type: " + leaveTypeId);
     
     Connection conn = null;
     PreparedStatement pstmt = null;
     ResultSet rs = null;
     java.util.List<LeaveApplication> applications = new java.util.ArrayList<>();
     
     try {
         conn = ConnectionManager.getConnection();
         if (conn == null) {
             throw new SQLException("Failed to establish database connection");
         }
         
         // Include rejectreason in SELECT query
         StringBuilder sqlBuilder = new StringBuilder();
         sqlBuilder.append("SELECT la.applicationid, la.employeeid, la.leavetypeid, la.leavestartdate, la.leaveenddate, ");
         sqlBuilder.append("la.leavestatus, la.managerid, la.leavereason, la.leaveduration, la.attachment, la.appliedon, ");
         sqlBuilder.append("la.rejectreason, lt.leavetypename ");
         sqlBuilder.append("FROM leaveapplication la ");
         sqlBuilder.append("LEFT JOIN leavetype lt ON la.leavetypeid = lt.leavetypeid ");
         sqlBuilder.append("WHERE 1=1 ");
         
         java.util.List<String> parameters = new java.util.ArrayList<>();
         
         if (employeeId != null && !employeeId.trim().isEmpty()) {
             sqlBuilder.append("AND la.employeeid = ? ");
             parameters.add(employeeId);
         }
         
         if (status != null && !status.trim().isEmpty() && !"all".equalsIgnoreCase(status)) {
             sqlBuilder.append("AND UPPER(la.leavestatus) = UPPER(?) ");
             parameters.add(status);
         }
         
         if (leaveTypeId != null && !leaveTypeId.trim().isEmpty() && !"all".equalsIgnoreCase(leaveTypeId)) {
             sqlBuilder.append("AND la.leavetypeid = ? ");
             parameters.add(leaveTypeId);
         }
         
         sqlBuilder.append("ORDER BY la.appliedon DESC");
         
         String sql = sqlBuilder.toString();
         System.out.println("Generated SQL: " + sql);
         
         pstmt = conn.prepareStatement(sql);
         
         // Set parameters
         for (int i = 0; i < parameters.size(); i++) {
             pstmt.setString(i + 1, parameters.get(i));
             System.out.println("Parameter " + (i + 1) + ": " + parameters.get(i));
         }
         
         rs = pstmt.executeQuery();
         
         while (rs.next()) {
             LeaveApplication leaveApplication = new LeaveApplication();
             leaveApplication.setApplicationid(rs.getString("applicationid"));
             leaveApplication.setEmployeeid(rs.getString("employeeid"));
             leaveApplication.setLeavetypeid(rs.getString("leavetypeid"));
             
             // Handle date conversion
             Date startDate = rs.getDate("leavestartdate");
             Date endDate = rs.getDate("leaveenddate");
             Timestamp appliedOn = rs.getTimestamp("appliedon");
             
             if (startDate != null) {
                 leaveApplication.setLeavestartdate(new SimpleDateFormat("yyyy-MM-dd").format(startDate));
             }
             if (endDate != null) {
                 leaveApplication.setLeaveenddate(new SimpleDateFormat("yyyy-MM-dd").format(endDate));
             }
             if (appliedOn != null) {
                 leaveApplication.setAppliedon(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(appliedOn));
             }
             
             leaveApplication.setLeavestatus(rs.getString("leavestatus"));
             leaveApplication.setManagerid(rs.getString("managerid"));
             leaveApplication.setLeavereason(rs.getString("leavereason"));
             leaveApplication.setLeaveduration(rs.getDouble("leaveduration"));
             leaveApplication.setAttachment(rs.getString("attachment"));
             
             // Set reject reason
             leaveApplication.setRejectReason(rs.getString("rejectreason"));
             
             applications.add(leaveApplication);
         }
         
         System.out.println("Found " + applications.size() + " leave applications with filters");
         
     } catch (SQLException e) {
         System.err.println("SQL Error in getLeaveApplicationsWithFiltersAndReason: " + e.getMessage());
         e.printStackTrace();
         throw e;
         
     } finally {
         try {
             if (rs != null) rs.close();
             if (pstmt != null) pstmt.close();
             if (conn != null) conn.close();
         } catch (SQLException e) {
             System.err.println("Error closing resources: " + e.getMessage());
         }
     }
     
     return applications;
 }
 
 /**
  * Check if employee has overlapping leave applications for the given date range
  * @param employeeId Employee ID
  * @param startDate Start date of the leave
  * @param endDate End date of the leave
  * @param excludeApplicationId Application ID to exclude from check (for updates, can be null)
  * @return List of overlapping applications (empty if no overlap)
  */
 public List<LeaveApplication> checkOverlappingLeaveApplications(String employeeId, String startDate, 
                                                                 String endDate, String excludeApplicationId) throws SQLException {
     System.out.println("=== CHECKING FOR OVERLAPPING LEAVE APPLICATIONS ===");
     System.out.println("Employee ID: " + employeeId);
     System.out.println("Requested Start Date: " + startDate);
     System.out.println("Requested End Date: " + endDate);
     
     Connection conn = null;
     PreparedStatement pstmt = null;
     ResultSet rs = null;
     List<LeaveApplication> overlappingApplications = new ArrayList<>();
     
     try {
         conn = ConnectionManager.getConnection();
         if (conn == null) {
             throw new SQLException("Failed to establish database connection");
         }
         
         // Query to find overlapping leave applications
         // Two date ranges overlap if: (StartA <= EndB) AND (EndA >= StartB)
         StringBuilder sql = new StringBuilder();
         sql.append("SELECT la.*, lt.leaveTypeName ");
         sql.append("FROM leaveapplication la ");
         sql.append("JOIN leavetype lt ON la.leavetypeid = lt.leavetypeid ");
         sql.append("WHERE la.employeeid = ? ");
         sql.append("AND la.leavestatus IN ('Pending', 'Approved') "); // Only check pending or approved
         sql.append("AND ( ");
         sql.append("  (TO_DATE(?, 'YYYY-MM-DD') <= la.leaveenddate AND TO_DATE(?, 'YYYY-MM-DD') >= la.leavestartdate) ");
         sql.append(") ");
         
         // Exclude specific application if provided (for updates)
         if (excludeApplicationId != null && !excludeApplicationId.trim().isEmpty()) {
             sql.append("AND la.applicationid != ? ");
         }
         
         sql.append("ORDER BY la.leavestartdate");
         
         pstmt = conn.prepareStatement(sql.toString());
         pstmt.setString(1, employeeId);
         pstmt.setString(2, startDate);
         pstmt.setString(3, endDate);
         
         if (excludeApplicationId != null && !excludeApplicationId.trim().isEmpty()) {
             pstmt.setString(4, excludeApplicationId);
         }
         
         System.out.println("Executing overlap check query...");
         rs = pstmt.executeQuery();
         
         while (rs.next()) {
             LeaveApplication app = new LeaveApplication();
             app.setApplicationid(rs.getString("applicationid"));
             app.setEmployeeid(rs.getString("employeeid"));
             app.setLeavetypeid(rs.getString("leavetypeid"));
             
             // Handle date conversion
             Date appStartDate = rs.getDate("leavestartdate");
             Date appEndDate = rs.getDate("leaveenddate");
             
             if (appStartDate != null) {
                 app.setLeavestartdate(new SimpleDateFormat("yyyy-MM-dd").format(appStartDate));
             }
             if (appEndDate != null) {
                 app.setLeaveenddate(new SimpleDateFormat("yyyy-MM-dd").format(appEndDate));
             }
             
             app.setLeavestatus(rs.getString("leavestatus"));
             app.setLeaveduration(rs.getDouble("leaveduration"));
             
             overlappingApplications.add(app);
             
             System.out.println("⚠️ Found overlapping application:");
             System.out.println("   Application ID: " + app.getApplicationid());
             System.out.println("   Status: " + app.getLeavestatus());
             System.out.println("   Dates: " + app.getLeavestartdate() + " to " + app.getLeaveenddate());
             System.out.println("   Duration: " + app.getLeaveduration() + " days");
         }
         
         if (overlappingApplications.isEmpty()) {
             System.out.println("✅ No overlapping leave applications found");
         } else {
             System.out.println("⚠️ Found " + overlappingApplications.size() + " overlapping application(s)");
         }
         
     } catch (SQLException e) {
         System.err.println("❌ SQL Error in checkOverlappingLeaveApplications: " + e.getMessage());
         e.printStackTrace();
         throw e;
         
     } finally {
         try {
             if (rs != null) rs.close();
             if (pstmt != null) pstmt.close();
             if (conn != null) conn.close();
         } catch (SQLException e) {
             System.err.println("Error closing resources: " + e.getMessage());
         }
     }
     
     return overlappingApplications;
 }
 
 /**
  * Update leave application status with manager ID, rejection reason AND review date
  * REPLACE the existing updateLeaveApplicationStatusWithManagerAndReason method with this
  */
 public String updateLeaveApplicationStatusWithManagerAndReason(String applicationId, String newStatus, 
                                                                String managerId, String rejectionReason) throws SQLException {
     System.out.println("=== UPDATING APPLICATION STATUS WITH MANAGER, REASON AND REVIEW DATE ===");
     System.out.println("Application ID: " + applicationId);
     System.out.println("New Status: " + newStatus);
     System.out.println("Manager ID: " + managerId);
     if (rejectionReason != null) {
         System.out.println("Rejection Reason: " + rejectionReason);
     }
     
     Connection conn = null;
     PreparedStatement pstmt = null;
     
     try {
         conn = ConnectionManager.getConnection();
         if (conn == null) {
             throw new SQLException("Failed to establish database connection");
         }
         
         // Update status, manager ID, rejection reason AND review date
         String updateSql = "UPDATE leaveapplication SET leavestatus = ?, managerid = ?, rejectreason = ?, reviewdate = CURRENT_TIMESTAMP WHERE applicationid = ?";
         
         pstmt = conn.prepareStatement(updateSql);
         pstmt.setString(1, newStatus);
         pstmt.setString(2, managerId); // Store manager who made the decision
         
         // Set rejection reason only for rejected applications
         if ("Rejected".equalsIgnoreCase(newStatus) && rejectionReason != null && !rejectionReason.trim().isEmpty()) {
             pstmt.setString(3, rejectionReason);
         } else {
             pstmt.setNull(3, Types.VARCHAR); // Clear rejection reason for non-rejected status
         }
         
         pstmt.setString(4, applicationId);
         
         int rowsAffected = pstmt.executeUpdate();
         
         if (rowsAffected > 0) {
             System.out.println("✅ Application status updated successfully with manager ID, reason and review date");
             return "success: Application status updated to " + newStatus + " by manager " + managerId + " at " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
         } else {
             System.err.println("❌ No application found with ID: " + applicationId);
             return "error: Application not found or could not be updated";
         }
         
     } catch (SQLException e) {
         System.err.println("❌ Database error updating application status: " + e.getMessage());
         e.printStackTrace();
         throw e;
     } finally {
         try {
             if (pstmt != null) pstmt.close();
             if (conn != null) conn.close();
         } catch (SQLException e) {
             System.err.println("Error closing resources: " + e.getMessage());
         }
     }
 }
 
//Add these methods to LeaveApplicationDAO.java

/**
* Approve leave application with balance deduction
* UPDATED: Properly handles 0.5 day deduction for half day leaves
* @param applicationId Application ID
* @param managerId Manager ID approving the leave
* @return Success or error message
*/
public String approveLeaveApplicationWithBalanceDeduction(String applicationId, String managerId) throws SQLException {
  System.out.println("=== APPROVING LEAVE APPLICATION WITH BALANCE DEDUCTION ===");
  System.out.println("Application ID: " + applicationId + ", Manager ID: " + managerId);
  
  Connection conn = null;
  PreparedStatement pstmt = null;
  ResultSet rs = null;
  
  try {
      conn = ConnectionManager.getConnection();
      if (conn == null) {
          throw new SQLException("Failed to establish database connection");
      }
      
      // Start transaction
      conn.setAutoCommit(false);
      
      // Step 1: Get application details with leave type info
      System.out.println("🔍 Fetching application and leave type details...");
      String getAppSql = 
          "SELECT la.*, lt.leavetypename, lt.affectsBalance, e.employeeleavebalance " +
          "FROM leaveapplication la " +
          "JOIN leavetype lt ON la.leavetypeid = lt.leavetypeid " +
          "JOIN employee e ON la.employeeid = e.employeeid " +
          "WHERE la.applicationid = ?";
      
      pstmt = conn.prepareStatement(getAppSql);
      pstmt.setString(1, applicationId);
      rs = pstmt.executeQuery();
      
      if (!rs.next()) {
          conn.rollback();
          System.out.println("❌ Application not found");
          return "error: Leave application not found";
      }
      
      String currentStatus = rs.getString("leavestatus");
      String employeeId = rs.getString("employeeid");
      String leaveTypeName = rs.getString("leavetypename");
      boolean affectsBalance = rs.getInt("affectsBalance") == 1;
      double leaveDuration = rs.getDouble("leaveduration");
      double currentBalance = rs.getDouble("employeeleavebalance");
      
      System.out.println("📋 Application Details:");
      System.out.println("  Current Status: " + currentStatus);
      System.out.println("  Employee ID: " + employeeId);
      System.out.println("  Leave Type: " + leaveTypeName);
      System.out.println("  Affects Balance: " + affectsBalance);
      System.out.println("  Leave Duration: " + leaveDuration + " days");
      System.out.println("  Current Balance: " + currentBalance + " days");
      
      // Check if application can be approved
      if (!"Pending".equalsIgnoreCase(currentStatus)) {
          conn.rollback();
          System.out.println("❌ Cannot approve - status is " + currentStatus);
          return "error: Only pending applications can be approved. Current status: " + currentStatus;
      }
      
      rs.close();
      pstmt.close();
      
      // Step 2: Update application status to Approved with manager ID and review date
      System.out.println("🔄 Updating application status to Approved...");
      String updateStatusSql = "UPDATE leaveapplication SET leavestatus = ?, managerid = ?, reviewdate = CURRENT_TIMESTAMP WHERE applicationid = ?";
      pstmt = conn.prepareStatement(updateStatusSql);
      pstmt.setString(1, "Approved");
      pstmt.setString(2, managerId);
      pstmt.setString(3, applicationId);
      
      int statusUpdateRows = pstmt.executeUpdate();
      pstmt.close();
      
      if (statusUpdateRows == 0) {
          conn.rollback();
          System.out.println("❌ Failed to update application status");
          return "error: Failed to update application status";
      }
      
      System.out.println("✅ Application status updated to Approved");
      
      // Step 3: Deduct leave balance if leave type affects balance
      String resultMessage;
      
      if (affectsBalance) {
          System.out.println("💰 Processing balance deduction...");
          
          // Check if sufficient balance
          if (currentBalance < leaveDuration) {
              conn.rollback();
              System.out.println("❌ Insufficient balance");
              return "error: Insufficient leave balance. Employee has " + currentBalance + " days, but " + leaveDuration + " days requested";
          }
          
          // Calculate new balance
          double newBalance = currentBalance - leaveDuration;
          
          System.out.println("💰 Balance Calculation:");
          System.out.println("  Current Balance: " + currentBalance);
          System.out.println("  Deduction Amount: " + leaveDuration);
          System.out.println("  New Balance: " + newBalance);
          
          // Update employee balance
          String updateBalanceSql = "UPDATE employee SET employeeleavebalance = ? WHERE employeeid = ?";
          pstmt = conn.prepareStatement(updateBalanceSql);
          pstmt.setDouble(1, newBalance);
          pstmt.setString(2, employeeId);
          
          int balanceUpdateRows = pstmt.executeUpdate();
          pstmt.close();
          
          if (balanceUpdateRows == 0) {
              conn.rollback();
              System.out.println("❌ Failed to update employee balance");
              return "error: Failed to update employee leave balance";
          }
          
          System.out.println("✅ Employee balance updated successfully");
          
          if (leaveDuration == 0.5) {
              resultMessage = leaveTypeName + " application approved successfully. 0.5 days deducted from employee's leave balance. New balance: " + newBalance + " days.";
          } else {
              resultMessage = leaveTypeName + " application approved successfully. " + leaveDuration + " days deducted from employee's leave balance. New balance: " + newBalance + " days.";
          }
          
      } else {
          System.out.println("ℹ️ Leave does not affect balance: No deduction for " + leaveTypeName);
          resultMessage = leaveTypeName + " application approved successfully. This leave type does not affect the employee's annual leave balance.";
      }
      
      // Commit transaction
      conn.commit();
      System.out.println("✅ Transaction committed successfully");
      
      return "success: " + resultMessage;
      
  } catch (SQLException e) {
      if (conn != null) {
          try {
              conn.rollback();
              System.out.println("🔄 Transaction rolled back due to error");
          } catch (SQLException ex) {
              System.err.println("❌ Error during rollback: " + ex.getMessage());
          }
      }
      System.err.println("❌ SQL Error in approveLeaveApplicationWithBalanceDeduction: " + e.getMessage());
      e.printStackTrace();
      throw e;
      
  } finally {
      try {
          if (rs != null) rs.close();
          if (pstmt != null) pstmt.close();
          if (conn != null) {
              conn.setAutoCommit(true);
              conn.close();
          }
      } catch (SQLException e) {
          System.err.println("❌ Error closing resources: " + e.getMessage());
      }
  }
}

/**
* Reject leave application with reason (NO balance changes)
* @param applicationId Application ID
* @param managerId Manager ID rejecting the leave
* @param rejectionReason Reason for rejection
* @return Success or error message
*/
public String rejectLeaveApplicationWithReason(String applicationId, String managerId, String rejectionReason) throws SQLException {
  System.out.println("=== REJECTING LEAVE APPLICATION ===");
  System.out.println("Application ID: " + applicationId + ", Manager ID: " + managerId);
  System.out.println("Rejection Reason: " + rejectionReason);
  
  Connection conn = null;
  PreparedStatement pstmt = null;
  ResultSet rs = null;
  
  try {
      conn = ConnectionManager.getConnection();
      if (conn == null) {
          throw new SQLException("Failed to establish database connection");
      }
      
      // Start transaction
      conn.setAutoCommit(false);
      
      // Step 1: Get application details
      System.out.println("🔍 Fetching application details...");
      String getAppSql = 
          "SELECT la.*, lt.leavetypename " +
          "FROM leaveapplication la " +
          "JOIN leavetype lt ON la.leavetypeid = lt.leavetypeid " +
          "WHERE la.applicationid = ?";
      
      pstmt = conn.prepareStatement(getAppSql);
      pstmt.setString(1, applicationId);
      rs = pstmt.executeQuery();
      
      if (!rs.next()) {
          conn.rollback();
          System.out.println("❌ Application not found");
          return "error: Leave application not found";
      }
      
      String currentStatus = rs.getString("leavestatus");
      String leaveTypeName = rs.getString("leavetypename");
      double leaveDuration = rs.getDouble("leaveduration");
      
      System.out.println("📋 Application Details:");
      System.out.println("  Current Status: " + currentStatus);
      System.out.println("  Leave Type: " + leaveTypeName);
      System.out.println("  Duration: " + leaveDuration + " days");
      
      // Check if application can be rejected
      if (!"Pending".equalsIgnoreCase(currentStatus)) {
          conn.rollback();
          System.out.println("❌ Cannot reject - status is " + currentStatus);
          return "error: Only pending applications can be rejected. Current status: " + currentStatus;
      }
      
      rs.close();
      pstmt.close();
      
      // Step 2: Update application status to Rejected with manager ID, reason, and review date
      System.out.println("🔄 Updating application status to Rejected...");
      String updateStatusSql = "UPDATE leaveapplication SET leavestatus = ?, managerid = ?, rejectreason = ?, reviewdate = CURRENT_TIMESTAMP WHERE applicationid = ?";
      pstmt = conn.prepareStatement(updateStatusSql);
      pstmt.setString(1, "Rejected");
      pstmt.setString(2, managerId);
      pstmt.setString(3, rejectionReason);
      pstmt.setString(4, applicationId);
      
      int statusUpdateRows = pstmt.executeUpdate();
      pstmt.close();
      
      if (statusUpdateRows == 0) {
          conn.rollback();
          System.out.println("❌ Failed to update application status");
          return "error: Failed to update application status";
      }
      
      System.out.println("✅ Application status updated to Rejected");
      
      // NO BALANCE CHANGES for rejection (no deduction was made on submission)
      System.out.println("ℹ️ No balance restoration needed - no deduction was made on submission");
      
      // Commit transaction
      conn.commit();
      System.out.println("✅ Transaction committed successfully");
      
      String resultMessage = leaveTypeName + " application rejected successfully. No balance changes made.";
      
      return "success: " + resultMessage;
      
  } catch (SQLException e) {
      if (conn != null) {
          try {
              conn.rollback();
              System.out.println("🔄 Transaction rolled back due to error");
          } catch (SQLException ex) {
              System.err.println("❌ Error during rollback: " + ex.getMessage());
          }
      }
      System.err.println("❌ SQL Error in rejectLeaveApplicationWithReason: " + e.getMessage());
      e.printStackTrace();
      throw e;
      
  } finally {
      try {
          if (rs != null) rs.close();
          if (pstmt != null) pstmt.close();
          if (conn != null) {
              conn.setAutoCommit(true);
              conn.close();
          }
      } catch (SQLException e) {
          System.err.println("❌ Error closing resources: " + e.getMessage());
      }
  }
}

/**
* UPDATED: Cancel leave application with balance restoration if already approved
* Handles 0.5 day restoration for approved half day leaves
* @param applicationId Application ID
* @param employeeId Employee ID
* @return Success or error message
*/
public String cancelLeaveApplicationWithBalanceCheck(String applicationId, String employeeId) throws SQLException {
  System.out.println("=== CANCELLING LEAVE APPLICATION WITH BALANCE CHECK ===");
  System.out.println("Application ID: " + applicationId + ", Employee ID: " + employeeId);
  
  Connection conn = null;
  PreparedStatement pstmt = null;
  ResultSet rs = null;
  
  try {
      conn = ConnectionManager.getConnection();
      if (conn == null) {
          throw new SQLException("Failed to establish database connection");
      }
      
      // Start transaction
      conn.setAutoCommit(false);
      
      // Step 1: Get application details
      System.out.println("🔍 Fetching application details...");
      String getAppSql = 
          "SELECT la.*, lt.leavetypename, lt.affectsBalance, e.employeeleavebalance " +
          "FROM leaveapplication la " +
          "JOIN leavetype lt ON la.leavetypeid = lt.leavetypeid " +
          "JOIN employee e ON la.employeeid = e.employeeid " +
          "WHERE la.applicationid = ? AND la.employeeid = ?";
      
      pstmt = conn.prepareStatement(getAppSql);
      pstmt.setString(1, applicationId);
      pstmt.setString(2, employeeId);
      
      rs = pstmt.executeQuery();
      
      if (!rs.next()) {
          conn.rollback();
          System.out.println("❌ Application not found or access denied");
          return "error: Leave application not found or access denied";
      }
      
      String currentStatus = rs.getString("leavestatus");
      String leaveTypeName = rs.getString("leavetypename");
      boolean affectsBalance = rs.getInt("affectsBalance") == 1;
      double leaveDuration = rs.getDouble("leaveduration");
      double currentBalance = rs.getDouble("employeeleavebalance");
      
      System.out.println("📋 Application Details:");
      System.out.println("  Current Status: " + currentStatus);
      System.out.println("  Leave Type: " + leaveTypeName);
      System.out.println("  Affects Balance: " + affectsBalance);
      System.out.println("  Duration: " + leaveDuration + " days");
      System.out.println("  Current Balance: " + currentBalance + " days");
      
      // Check if application can be cancelled
      if (!"Pending".equalsIgnoreCase(currentStatus) && !"Approved".equalsIgnoreCase(currentStatus)) {
          conn.rollback();
          System.out.println("❌ Cannot cancel - status is " + currentStatus);
          return "error: Only pending or approved applications can be cancelled. Current status: " + currentStatus;
      }
      
      rs.close();
      pstmt.close();
      
      // Step 2: Update application status to Cancelled
      System.out.println("🔄 Updating application status to Cancelled...");
      String updateStatusSql = "UPDATE leaveapplication SET leavestatus = ? WHERE applicationid = ?";
      pstmt = conn.prepareStatement(updateStatusSql);
      pstmt.setString(1, "Cancelled");
      pstmt.setString(2, applicationId);
      
      int statusUpdateRows = pstmt.executeUpdate();
      pstmt.close();
      
      if (statusUpdateRows == 0) {
          conn.rollback();
          System.out.println("❌ Failed to update application status");
          return "error: Failed to update application status";
      }
      
      System.out.println("✅ Application status updated to Cancelled");
      
      // Step 3: Restore balance if application was approved and affects balance
      String resultMessage;
      
      if ("Approved".equalsIgnoreCase(currentStatus) && affectsBalance) {
          System.out.println("💰 Restoring leave balance...");
          
          double newBalance = currentBalance + leaveDuration;
          
          System.out.println("💰 Balance Restoration:");
          System.out.println("  Current Balance: " + currentBalance);
          System.out.println("  Restoration Amount: " + leaveDuration);
          System.out.println("  New Balance: " + newBalance);
          
          String updateBalanceSql = "UPDATE employee SET employeeleavebalance = ? WHERE employeeid = ?";
          pstmt = conn.prepareStatement(updateBalanceSql);
          pstmt.setDouble(1, newBalance);
          pstmt.setString(2, employeeId);
          
          int balanceUpdateRows = pstmt.executeUpdate();
          pstmt.close();
          
          if (balanceUpdateRows == 0) {
              conn.rollback();
              System.out.println("❌ Failed to restore balance");
              return "error: Failed to restore leave balance";
          }
          
          System.out.println("✅ Balance restored successfully");
          
          if (leaveDuration == 0.5) {
              resultMessage = leaveTypeName + " application cancelled successfully. 0.5 days restored to your leave balance. New balance: " + newBalance + " days.";
          } else {
              resultMessage = leaveTypeName + " application cancelled successfully. " + leaveDuration + " days restored to your leave balance. New balance: " + newBalance + " days.";
          }
          
      } else if ("Pending".equalsIgnoreCase(currentStatus)) {
          System.out.println("ℹ️ Pending application cancelled - no balance restoration needed");
          resultMessage = leaveTypeName + " application cancelled successfully. No balance changes made.";
          
      } else {
          System.out.println("ℹ️ Leave does not affect balance - no restoration needed");
          resultMessage = leaveTypeName + " application cancelled successfully.";
      }
      
      // Commit transaction
      conn.commit();
      System.out.println("✅ Transaction committed successfully");
      
      return "success: " + resultMessage;
      
  } catch (SQLException e) {
      if (conn != null) {
          try {
              conn.rollback();
              System.out.println("🔄 Transaction rolled back due to error");
          } catch (SQLException ex) {
              System.err.println("❌ Error during rollback: " + ex.getMessage());
          }
      }
      System.err.println("❌ SQL Error in cancelLeaveApplicationWithBalanceCheck: " + e.getMessage());
      e.printStackTrace();
      throw e;
      
  } finally {
      try {
          if (rs != null) rs.close();
          if (pstmt != null) pstmt.close();
          if (conn != null) {
              conn.setAutoCommit(true);
              conn.close();
          }
      } catch (SQLException e) {
          System.err.println("❌ Error closing resources: " + e.getMessage());
      }
  }
}

/**
 * Get all dates that are unavailable for an employee (Pending or Approved leaves)
 * @param employeeId Employee ID
 * @return List of unavailable date ranges
 */
public List<Map<String, String>> getUnavailableDatesForEmployee(String employeeId) throws SQLException {
    System.out.println("=== GETTING UNAVAILABLE DATES FOR EMPLOYEE: " + employeeId + " ===");
    
    Connection conn = null;
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    List<Map<String, String>> unavailableDates = new ArrayList<>();
    
    try {
        conn = ConnectionManager.getConnection();
        if (conn == null) {
            throw new SQLException("Failed to establish database connection");
        }
        
        // Get all Pending or Approved leave applications
        String sql = "SELECT applicationid, leavestartdate, leaveenddate, leavestatus, leaveduration " +
                    "FROM leaveapplication " +
                    "WHERE employeeid = ? " +
                    "AND leavestatus IN ('Pending', 'Approved') " +
                    "ORDER BY leavestartdate";
        
        pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, employeeId);
        
        rs = pstmt.executeQuery();
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        
        while (rs.next()) {
            Map<String, String> dateRange = new HashMap<>();
            
            Date startDate = rs.getDate("leavestartdate");
            Date endDate = rs.getDate("leaveenddate");
            
            if (startDate != null && endDate != null) {
                dateRange.put("startDate", sdf.format(startDate));
                dateRange.put("endDate", sdf.format(endDate));
                dateRange.put("status", rs.getString("leavestatus"));
                dateRange.put("applicationId", rs.getString("applicationid"));
                dateRange.put("duration", String.valueOf(rs.getDouble("leaveduration")));
                
                unavailableDates.add(dateRange);
                
                System.out.println("  Unavailable: " + sdf.format(startDate) + " to " + sdf.format(endDate) + 
                                 " (" + rs.getString("leavestatus") + ")");
            }
        }
        
        System.out.println("✅ Found " + unavailableDates.size() + " unavailable date ranges");
        
    } catch (SQLException e) {
        System.err.println("❌ SQL Error in getUnavailableDatesForEmployee: " + e.getMessage());
        e.printStackTrace();
        throw e;
        
    } finally {
        try {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            System.err.println("Error closing resources: " + e.getMessage());
        }
    }
    
    return unavailableDates;
}
 
/**
 * Count total applications for a specific leave type
 * Used to determine if a leave type has existing applications
 * This helps admins understand the impact of editing leave type settings
 * 
 * @param leaveTypeId The leave type ID to count applications for
 * @return Total number of applications (all statuses) for this leave type
 */
public int countApplicationsByLeaveType(String leaveTypeId) throws SQLException {
    System.out.println("=== COUNTING APPLICATIONS FOR LEAVE TYPE: " + leaveTypeId + " ===");
    
    Connection conn = null;
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    int count = 0;
    
    try {
        conn = ConnectionManager.getConnection();
        if (conn == null) {
            throw new SQLException("Failed to establish database connection");
        }
        
        // Count all applications regardless of status
        String sql = "SELECT COUNT(*) AS total FROM leaveapplication WHERE leavetypeid = ?";
        
        pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, leaveTypeId);
        
        rs = pstmt.executeQuery();
        
        if (rs.next()) {
            count = rs.getInt("total");
        }
        
        System.out.println("✅ Found " + count + " total applications for leave type: " + leaveTypeId);
        
    } catch (SQLException e) {
        System.err.println("❌ SQL Error in countApplicationsByLeaveType: " + e.getMessage());
        e.printStackTrace();
        throw e;
        
    } finally {
        try {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            System.err.println("Error closing resources: " + e.getMessage());
        }
    }
    
    return count;
}
 
 
    
}
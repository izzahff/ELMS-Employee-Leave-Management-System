package elms.DAO;

import elms.connection.ConnectionManager;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * REVISED: DAO for calculating employee leave balances dynamically
 * 
 * KEY CONCEPT: Each leave type with affectsBalance=1 has its OWN balance
 * - Annual Leave: 14 days allocated, tracks usage separately
 * - Personal Leave: 5 days allocated, tracks usage separately
 * - They DO NOT share a balance pool - each is independent
 * 
 * For leave types with affectsBalance=0:
 * - No balance tracking (e.g., Maternity 90 days, Medical Leave)
 * - Standard duration is just informational
 */
public class LeaveBalanceDAO {
    
    /**
     * Calculate the available balance for a SPECIFIC leave type
     * 
     * Formula: Standard Duration - Sum of Approved Applications for THIS leave type
     * 
     * Example:
     *   Leave Type: Annual Leave (14 days, affectsBalance=1)
     *   Approved Applications: 3 days + 2 days = 5 days used
     *   Available: 14 - 5 = 9 days
     * 
     * @param employeeId The employee ID
     * @param leaveTypeId The specific leave type ID
     * @return The available balance for this specific leave type, or 0 if no tracking
     */
    public static double calculateBalanceForLeaveType(String employeeId, String leaveTypeId) {
        System.out.println("=== CALCULATING BALANCE FOR SPECIFIC LEAVE TYPE ===");
        System.out.println("Employee ID: " + employeeId);
        System.out.println("Leave Type ID: " + leaveTypeId);
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        double standardDuration = 0;
        double usedDays = 0;
        boolean affectsBalance = false;
        
        try {
            conn = ConnectionManager.getConnection();
            
            // Step 1: Get leave type info (standard duration and whether it affects balance)
            String leaveTypeSQL = 
                "SELECT lt.affectsbalance, fd.standardduration " +
                "FROM leavetype lt " +
                "LEFT JOIN fullday fd ON lt.leavetypeid = fd.leavetypeid " +
                "WHERE lt.leavetypeid = ?";
            
            pstmt = conn.prepareStatement(leaveTypeSQL);
            pstmt.setString(1, leaveTypeId);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                affectsBalance = rs.getBoolean("affectsbalance");
                standardDuration = rs.getDouble("standardduration");
                
                System.out.println("Leave type affects balance: " + affectsBalance);
                System.out.println("Standard duration: " + standardDuration + " days");
            } else {
                System.out.println("⚠️ Leave type not found");
                return 0.0;
            }
            
            rs.close();
            pstmt.close();
            
            // If this leave type doesn't affect balance, return 0 (no tracking)
            if (!affectsBalance) {
                System.out.println("ℹ️ This leave type does not track balance");
                System.out.println("=== END CALCULATION ===");
                return 0.0;
            }
            
            // If no standard duration defined, return 0
            if (standardDuration <= 0) {
                System.out.println("⚠️ No standard duration defined for this leave type");
                System.out.println("=== END CALCULATION ===");
                return 0.0;
            }
            
            // Step 2: Calculate used days for THIS specific leave type only
            String usedSQL = 
                "SELECT COALESCE(SUM(leaveduration), 0) as used_days " +
                "FROM leaveapplication " +
                "WHERE employeeid = ? " +
                "AND leavetypeid = ? " +
                "AND leavestatus = 'Approved'";
            
            pstmt = conn.prepareStatement(usedSQL);
            pstmt.setString(1, employeeId);
            pstmt.setString(2, leaveTypeId);
            
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                usedDays = rs.getDouble("used_days");
            }
            
            System.out.println("Used days for this leave type: " + usedDays);
            
        } catch (SQLException e) {
            System.err.println("Error calculating balance for leave type: " + e.getMessage());
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
        
        double availableBalance = standardDuration - usedDays;
        System.out.println("Available balance: " + availableBalance + " days");
        System.out.println("=== END CALCULATION ===");
        
        return availableBalance;
    }
    
    /**
     * Get all leave types that track balance (affectsBalance = 1)
     * Returns a map of leave type ID -> leave type name
     * 
     * @return Map of leave type IDs to names that track balance
     */
    public static Map<String, String> getBalanceTrackingLeaveTypes() {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Map<String, String> balanceLeaveTypes = new HashMap<>();
        
        try {
            conn = ConnectionManager.getConnection();
            
            String sql = "SELECT leavetypeid, leavetypename " +
                        "FROM leavetype " +
                        "WHERE affectsbalance = 1 " +
                        "ORDER BY leavetypename";
            
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                String id = rs.getString("leavetypeid");
                String name = rs.getString("leavetypename");
                balanceLeaveTypes.put(id, name);
                System.out.println("Found balance-tracking leave type: " + name + " (ID: " + id + ")");
            }
            
            if (balanceLeaveTypes.isEmpty()) {
                System.out.println("ℹ️ No leave types configured to track balance");
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting balance-tracking leave types: " + e.getMessage());
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
        
        return balanceLeaveTypes;
    }
    
    /**
     * Check if any leave types are configured to track balance
     * @return true if at least one leave type has affectsBalance = 1
     */
    public static boolean hasBalanceTrackingLeaveTypes() {
        return !getBalanceTrackingLeaveTypes().isEmpty();
    }
    
    /**
     * Get detailed balance information for a specific leave type
     * 
     * @param employeeId The employee ID
     * @param leaveTypeId The leave type ID
     * @return BalanceInfo object with allocation, used, and available days
     */
    public static BalanceInfo getBalanceInfoForLeaveType(String employeeId, String leaveTypeId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        double standardDuration = 0;
        double usedDays = 0;
        boolean affectsBalance = false;
        
        try {
            conn = ConnectionManager.getConnection();
            
            // Get leave type configuration
            String leaveTypeSQL = 
                "SELECT lt.affectsbalance, fd.standardduration " +
                "FROM leavetype lt " +
                "LEFT JOIN fullday fd ON lt.leavetypeid = fd.leavetypeid " +
                "WHERE lt.leavetypeid = ?";
            
            pstmt = conn.prepareStatement(leaveTypeSQL);
            pstmt.setString(1, leaveTypeId);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                affectsBalance = rs.getBoolean("affectsbalance");
                standardDuration = rs.getDouble("standardduration");
            }
            
            rs.close();
            pstmt.close();
            
            if (!affectsBalance || standardDuration <= 0) {
                return new BalanceInfo(0, 0, 0);
            }
            
            // Get used days
            String usedSQL = 
                "SELECT COALESCE(SUM(leaveduration), 0) as used_days " +
                "FROM leaveapplication " +
                "WHERE employeeid = ? " +
                "AND leavetypeid = ? " +
                "AND leavestatus = 'Approved'";
            
            pstmt = conn.prepareStatement(usedSQL);
            pstmt.setString(1, employeeId);
            pstmt.setString(2, leaveTypeId);
            
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                usedDays = rs.getDouble("used_days");
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting balance info: " + e.getMessage());
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
        
        double availableBalance = standardDuration - usedDays;
        return new BalanceInfo(standardDuration, usedDays, availableBalance);
    }
    
    /**
     * Get balance summary for ALL balance-tracking leave types for an employee
     * Returns a map of leave type name -> BalanceInfo
     * 
     * @param employeeId The employee ID
     * @return Map of leave type names to their balance information
     */
    public static Map<String, BalanceInfo> getAllBalances(String employeeId) {
        Map<String, BalanceInfo> allBalances = new HashMap<>();
        Map<String, String> balanceLeaveTypes = getBalanceTrackingLeaveTypes();
        
        for (Map.Entry<String, String> entry : balanceLeaveTypes.entrySet()) {
            String leaveTypeId = entry.getKey();
            String leaveTypeName = entry.getValue();
            BalanceInfo info = getBalanceInfoForLeaveType(employeeId, leaveTypeId);
            allBalances.put(leaveTypeName, info);
        }
        
        return allBalances;
    }
    
    /**
     * Inner class to hold balance information
     */
    public static class BalanceInfo {
        private double standardAllocation;
        private double usedDays;
        private double availableDays;
        
        public BalanceInfo(double standardAllocation, double usedDays, double availableDays) {
            this.standardAllocation = standardAllocation;
            this.usedDays = usedDays;
            this.availableDays = availableDays;
        }
        
        public double getStandardAllocation() {
            return standardAllocation;
        }
        
        public double getUsedDays() {
            return usedDays;
        }
        
        public double getAvailableDays() {
            return availableDays;
        }
        
        @Override
        public String toString() {
            return String.format("Balance[Allocation: %.1f, Used: %.1f, Available: %.1f]", 
                               standardAllocation, usedDays, availableDays);
        }
    }
}
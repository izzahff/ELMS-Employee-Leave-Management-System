package elms.controller;

import elms.DAO.LeaveTypeDAO;
import elms.DAO.FullDayDAO;
import elms.DAO.HalfDayDAO;
import elms.model.LeaveType;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.*;

@WebServlet(urlPatterns = {
    "/AdminDeleteLeaveTypeController", 
    "/Admin/AdminDeleteLeaveTypeController"
})
public class AdminDeleteLeaveTypeController extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private LeaveTypeDAO leaveTypeDAO;
    private FullDayDAO fullDayDAO;
    private HalfDayDAO halfDayDAO;
    
    @Override
    public void init() throws ServletException {
        super.init();
        leaveTypeDAO = new LeaveTypeDAO();
        fullDayDAO = new FullDayDAO();
        halfDayDAO = new HalfDayDAO();
        System.out.println("✅ AdminDeleteLeaveTypeController initialized with enhanced deletion features");
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String action = request.getParameter("action");
        
        if ("checkDelete".equals(action)) {
            // AJAX request to check deletion eligibility
            handleDeleteCheck(request, response);
        } else {
            // Regular GET - redirect to leave type list
            response.sendRedirect("/ELMS_3.0/AdminLeaveTypeListController");
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Check if admin is logged in
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("adminId") == null) {
            response.sendRedirect(request.getContextPath() + "/Admin/AdminLogin.jsp");
            return;
        }
        
        String action = request.getParameter("action");
        String leaveTypeId = request.getParameter("leaveTypeId");
        String leaveTypeName = request.getParameter("leaveTypeName");
        String adminId = (String) session.getAttribute("adminId");
        String adminPassword = request.getParameter("adminPassword");
        
        try {
            if ("delete".equals(action)) {
                // Verify admin password before deletion
                if (adminPassword == null || adminPassword.trim().isEmpty()) {
                    session.setAttribute("errorMessage", "Password is required to confirm deletion.");
                    response.sendRedirect("/ELMS_3.0/AdminLeaveTypeListController");
                    return;
                }
                
                // Use existing authenticateAdmin method to verify password
                elms.DAO.AdminDAO adminDAO = new elms.DAO.AdminDAO();
                elms.model.Admin verifiedAdmin = adminDAO.authenticateAdmin(adminId, adminPassword);
                
                if (verifiedAdmin == null) {
                    session.setAttribute("errorMessage", "Invalid password. Deletion cancelled for security reasons.");
                    response.sendRedirect("/ELMS_3.0/AdminLeaveTypeListController");
                    return;
                }
                
                System.out.println("✅ Password verified for admin: " + adminId);
                
                // Password verified - proceed with enhanced deletion
                handleEnhancedDeletion(request, response, leaveTypeId, leaveTypeName, adminId);
                
            } else {
                session.setAttribute("errorMessage", "Invalid action specified.");
                response.sendRedirect("/ELMS_3.0/AdminLeaveTypeListController");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Error in AdminDeleteLeaveTypeController: " + e.getMessage());
            session.setAttribute("errorMessage", "An unexpected error occurred: " + e.getMessage());
            response.sendRedirect("/ELMS_3.0/AdminLeaveTypeListController");
        }
    }
    /**
     * Handle AJAX request to check deletion eligibility
     */
    private void handleDeleteCheck(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String leaveTypeId = request.getParameter("leaveTypeId");
        
        if (leaveTypeId == null || leaveTypeId.trim().isEmpty()) {
            response.getWriter().write("{\"error\": \"Invalid leave type ID\"}");
            return;
        }
        
        try {
            // Get leave type info
            LeaveType leaveType = leaveTypeDAO.getLeaveTypeById(leaveTypeId.trim());
            if (leaveType == null) {
                response.getWriter().write("{\"error\": \"Leave type not found\"}");
                return;
            }
            
            // Get application counts
            int totalApplications = getLeaveApplicationCountForLeaveType(leaveTypeId);
            int pendingApplications = getPendingApplicationCountForLeaveType(leaveTypeId);
            int approvedApplications = getApprovedApplicationCountForLeaveType(leaveTypeId);
            int rejectedApplications = getRejectedApplicationCountForLeaveType(leaveTypeId);
            
            boolean canDelete = pendingApplications == 0; // Can delete only if no pending applications
            String message;
            
            if (!canDelete) {
                message = "Cannot delete leave type with " + pendingApplications + " pending applications. Please process all pending applications first.";
            } else if (totalApplications > 0) {
                message = "This leave type has " + totalApplications + " historical applications that will also be permanently deleted.";
            } else {
                message = "Leave type can be safely deleted (no applications found).";
            }
            
            // Build JSON response
            StringBuilder jsonResponse = new StringBuilder();
            jsonResponse.append("{");
            jsonResponse.append("\"canDelete\": ").append(canDelete).append(",");
            jsonResponse.append("\"message\": \"").append(escapeJson(message)).append("\",");
            jsonResponse.append("\"pendingApplications\": ").append(pendingApplications).append(",");
            jsonResponse.append("\"totalApplications\": ").append(totalApplications).append(",");
            jsonResponse.append("\"approvedApplications\": ").append(approvedApplications).append(",");
            jsonResponse.append("\"rejectedApplications\": ").append(rejectedApplications).append(",");
            jsonResponse.append("\"leaveTypeName\": \"").append(escapeJson(leaveType.getLeaveTypeName())).append("\"");
            jsonResponse.append("}");
            
            response.getWriter().write(jsonResponse.toString());
            
        } catch (Exception e) {
            System.err.println("Error in delete check: " + e.getMessage());
            e.printStackTrace();
            response.getWriter().write("{\"error\": \"" + escapeJson(e.getMessage()) + "\"}");
        }
    }
    
    /**
     * Handle enhanced deletion (removes leave type and all associated applications)
     */
    private void handleEnhancedDeletion(HttpServletRequest request, HttpServletResponse response, 
            String leaveTypeId, String leaveTypeName, String adminId) 
            throws IOException, SQLException {

			HttpSession session = request.getSession();
			
			if (leaveTypeId == null || leaveTypeId.trim().isEmpty()) {
			session.setAttribute("errorMessage", "Invalid leave type ID provided.");
			response.sendRedirect("/ELMS_3.0/AdminLeaveTypeListController");
			return;
			}
			
			// Check if leave type exists
			LeaveType leaveType = leaveTypeDAO.getLeaveTypeById(leaveTypeId.trim());
			if (leaveType == null) {
			session.setAttribute("errorMessage", "Leave type not found.");
			response.sendRedirect("/ELMS_3.0/AdminLeaveTypeListController");
			return;
			}
			
			String actualLeaveTypeName = leaveType.getLeaveTypeName();
			String leaveTypeCategory = leaveType.getLeaveTypeCategory();
			
			// Verify the leave type name matches (security check)
			if (leaveTypeName == null || !leaveTypeName.equals(actualLeaveTypeName)) {
			session.setAttribute("errorMessage", 
			"Leave type name verification failed. Deletion aborted for security reasons.");
			response.sendRedirect("/ELMS_3.0/AdminLeaveTypeListController");
			return;
			}
			
			// Check for pending applications
			int pendingCount = getPendingApplicationCountForLeaveType(leaveTypeId);
			if (pendingCount > 0) {
			session.setAttribute("errorMessage", 
			"Cannot delete leave type '" + actualLeaveTypeName + "'. There are " + pendingCount + 
			" pending application(s). Please process all pending applications first.");
			response.sendRedirect("/ELMS_3.0/AdminLeaveTypeListController");
			return;
			}
			
			// Log the deletion attempt
			System.out.println("=== ENHANCED LEAVE TYPE DELETION ===");
			System.out.println("Admin ID: " + adminId);
			System.out.println("Leave Type: " + actualLeaveTypeName + " (" + leaveTypeId + ")");
			System.out.println("Category: " + leaveTypeCategory);
			
			int totalApps = getLeaveApplicationCountForLeaveType(leaveTypeId);
			System.out.println("Applications to delete: " + totalApps);
			
			// Perform enhanced deletion (everything including applications)
			boolean isDeleted = deleteLeaveTypeWithApplications(leaveTypeId.trim(), leaveTypeCategory);
			
			if (isDeleted) {
			System.out.println("✅ Enhanced deletion successful");
			String message = "Leave type '" + actualLeaveTypeName + "' has been permanently deleted.";
			if (totalApps > 0) {
			message += " " + totalApps + " associated applications were also permanently removed.";
			}
			session.setAttribute("successMessage", message);
			} else {
			System.out.println("❌ Enhanced deletion failed");
			session.setAttribute("errorMessage", 
			"Failed to delete leave type '" + actualLeaveTypeName + "'. Please try again.");
			}
			
			response.sendRedirect("/ELMS_3.0/AdminLeaveTypeListController");
			}
    
    /**
     * Delete leave type with all associated applications and configurations
     */
    private boolean deleteLeaveTypeWithApplications(String leaveTypeId, String leaveTypeCategory) throws SQLException {
        Connection conn = null;
        boolean success = false;
        
        try {
            conn = elms.connection.ConnectionManager.getConnection();
            conn.setAutoCommit(false);
            
            System.out.println("🗑️ Starting enhanced deletion (removing everything)");
            
            // Step 1: Delete all leave applications first to handle foreign key constraints
            PreparedStatement deleteApps = conn.prepareStatement("DELETE FROM leaveapplication WHERE leavetypeid = ?");
            deleteApps.setString(1, leaveTypeId);
            int appsDeleted = deleteApps.executeUpdate();
            deleteApps.close();
            System.out.println("🗑️ Deleted " + appsDeleted + " leave applications");
            
         // Step 2: Delete child table records (FullDay or HalfDay configurations)
            if ("Full Day".equals(leaveTypeCategory)) {
                try {
                    boolean fullDayDeleted = fullDayDAO.deleteFullDay(leaveTypeId);
                    System.out.println(fullDayDeleted ? "✅ FullDay record deleted" : "ℹ️ No FullDay record found");
                } catch (Exception e) {
                    System.out.println("⚠️ Error deleting FullDay record: " + e.getMessage());
                }
            } else if ("Half Day".equals(leaveTypeCategory)) {
                try {
                    boolean halfDayDeleted = halfDayDAO.deleteHalfDay(leaveTypeId);
                    System.out.println(halfDayDeleted ? "✅ HalfDay record deleted" : "ℹ️ No HalfDay record found");
                } catch (Exception e) {
                    System.out.println("⚠️ Error deleting HalfDay record: " + e.getMessage());
                }
            } else if ("Both".equals(leaveTypeCategory)) {
                // For "Both" category, delete from BOTH tables
                try {
                    boolean fullDayDeleted = fullDayDAO.deleteFullDay(leaveTypeId);
                    System.out.println(fullDayDeleted ? "✅ FullDay record deleted" : "ℹ️ No FullDay record found");
                } catch (Exception e) {
                    System.out.println("⚠️ Error deleting FullDay record: " + e.getMessage());
                }
                
                try {
                    boolean halfDayDeleted = halfDayDAO.deleteHalfDay(leaveTypeId);
                    System.out.println(halfDayDeleted ? "✅ HalfDay record deleted" : "ℹ️ No HalfDay record found");
                } catch (Exception e) {
                    System.out.println("⚠️ Error deleting HalfDay record: " + e.getMessage());
                }
            
            } else if ("Half Day".equals(leaveTypeCategory)) {
                try {
                    boolean halfDayDeleted = halfDayDAO.deleteHalfDay(leaveTypeId);
                    System.out.println(halfDayDeleted ? "✅ HalfDay record deleted" : "ℹ️ No HalfDay record found");
                } catch (Exception e) {
                    System.out.println("⚠️ Error deleting HalfDay record: " + e.getMessage());
                    // Continue with deletion even if child record fails
                }
            }
            
            // Step 3: Delete the leave type
            PreparedStatement deleteLeaveType = conn.prepareStatement("DELETE FROM leavetype WHERE leaveTypeId = ?");
            deleteLeaveType.setString(1, leaveTypeId);
            int leaveTypeDeleted = deleteLeaveType.executeUpdate();
            deleteLeaveType.close();
            
            if (leaveTypeDeleted > 0) {
                conn.commit();
                success = true;
                System.out.println("✅ Enhanced deletion completed - everything removed");
            } else {
                conn.rollback();
                System.out.println("❌ No leave type rows affected");
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
            System.err.println("❌ SQL error during deletion: " + e.getMessage());
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("❌ Error closing connection: " + e.getMessage());
                }
            }
        }
        
        return success;
    }
    
    // Helper methods for counting applications
    private int getLeaveApplicationCountForLeaveType(String leaveTypeId) throws SQLException {
        return getApplicationCountByStatus(leaveTypeId, null);
    }
    
    private int getPendingApplicationCountForLeaveType(String leaveTypeId) throws SQLException {
        return getApplicationCountByStatus(leaveTypeId, "PENDING");
    }
    
    private int getApprovedApplicationCountForLeaveType(String leaveTypeId) throws SQLException {
        return getApplicationCountByStatus(leaveTypeId, "APPROVED");
    }
    
    private int getRejectedApplicationCountForLeaveType(String leaveTypeId) throws SQLException {
        return getApplicationCountByStatus(leaveTypeId, "REJECTED");
    }
    
    /**
     * Get count of applications by status for a specific leave type
     */
    private int getApplicationCountByStatus(String leaveTypeId, String status) throws SQLException {
        String sql = "SELECT COUNT(*) FROM leaveapplication WHERE leavetypeid = ?";
        if (status != null) {
            sql += " AND UPPER(leavestatus) = UPPER(?)";
        }
        
        try (Connection conn = elms.connection.ConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, leaveTypeId);
            if (status != null) {
                pstmt.setString(2, status);
            }
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error counting applications: " + e.getMessage());
            throw e;
        }
        return 0;
    }
    
    /**
     * Escape special characters for JSON output
     */
    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t")
                  .replace("\\", "\\\\");
    }
}
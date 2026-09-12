package elms.controller;

import elms.DAO.ManagerDAO;
import elms.DAO.AdminDAO;
import elms.DAO.EmployeeDAO;
import elms.DAO.LeaveApplicationDAO;
import elms.model.Manager;
import elms.model.Admin;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

@WebServlet("/AdminDeleteManagerController")
public class AdminDeleteManagerController extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private ManagerDAO managerDAO;
    private AdminDAO adminDAO;
    private EmployeeDAO employeeDAO;
    private LeaveApplicationDAO leaveApplicationDAO;
    
    @Override
    public void init() throws ServletException {
        super.init();
        managerDAO = new ManagerDAO();
        adminDAO = new AdminDAO();
        employeeDAO = new EmployeeDAO();
        leaveApplicationDAO = new LeaveApplicationDAO();
        
        // Ensure system manager exists for historical records
        managerDAO.ensureSystemManagerExists();
        System.out.println("✅ AdminDeleteManagerController initialized with system manager check");
    }
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Check if admin is logged in
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("adminId") == null) {
            response.setContentType("text/plain");
            response.getWriter().write("error: Session expired. Please login again.");
            return;
        }
        
        String adminId = (String) session.getAttribute("adminId");
        String managerId = request.getParameter("managerId");
        String action = request.getParameter("action");
        String adminPassword = request.getParameter("adminPassword");
        
        System.out.println("=== Manager Deletion Request with History Preservation ===");
        System.out.println("Admin ID: " + adminId);
        System.out.println("Manager ID: " + managerId);
        System.out.println("Action: " + action);
        System.out.println("Password provided: " + (adminPassword != null && !adminPassword.isEmpty()));
        
        // Set response type for AJAX
        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        // Verify this is a delete action
        if (!"delete".equals(action)) {
            out.write("error: Invalid action specified.");
            return;
        }
        
        if (managerId == null || managerId.trim().isEmpty()) {
            out.write("error: Invalid manager ID provided.");
            return;
        }
        
        if (adminPassword == null || adminPassword.trim().isEmpty()) {
            out.write("error: Admin password is required.");
            return;
        }
        
        try {
            // 1. Verify admin password first
            System.out.println("Verifying admin password for admin ID: " + adminId);
            Admin admin = adminDAO.getAdminById(adminId);
            if (admin == null) {
                System.err.println("Admin not found with ID: " + adminId);
                out.write("error: Admin account not found.");
                return;
            }
            
            // Hash the provided password and compare with stored hash
            String hashedInputPassword = AdminDAO.hashPassword(adminPassword);
            if (!hashedInputPassword.equals(admin.getAdminPassword())) {
                System.out.println("Password verification failed for admin: " + adminId);
                out.write("invalid_password");
                return;
            }
            
            System.out.println("✅ Admin password verified successfully");
            
            // 2. Check if manager exists
            Manager manager = managerDAO.getManagerById(managerId.trim());
            if (manager == null) {
                out.write("error: Manager not found.");
                return;
            }
            
            String managerName = manager.getManagername();
            String managerPosition = manager.getManagerposition();
            
            System.out.println("Manager to delete: " + managerName + " (" + managerPosition + ")");
            
            // 3. Business logic checks:
            
            // Prevent deletion if it's the last Executive Director
            if ("Executive Director".equals(managerPosition)) {
                try {
                    int executiveCount = managerDAO.getManagerCountByPosition("Executive Director");
                    System.out.println("Executive Director count: " + executiveCount);
                    if (executiveCount <= 1) {
                        out.write("error: Cannot delete the last Executive Director. At least one Executive Director must remain in the system.");
                        return;
                    }
                } catch (Exception e) {
                    System.out.println("Warning: Could not check Executive Director count: " + e.getMessage());
                }
            }
            
            // Check if manager has pending leave requests to approve
            try {
                int pendingRequests = getPendingRequestsForManager(managerId);
                System.out.println("Pending requests for manager: " + pendingRequests);
                if (pendingRequests > 0) {
                    out.write("error: Cannot delete manager '" + managerName + "'. There are " + pendingRequests + 
                        " pending leave requests assigned to this manager. Please reassign or process these requests first.");
                    return;
                }
            } catch (Exception e) {
                System.out.println("Warning: Could not check pending requests: " + e.getMessage());
            }
            
            // 4. Check leave application dependencies and show what will happen
            try {
                int leaveAppCount = managerDAO.countDependentLeaveApplications(managerId);
                System.out.println("Leave applications for manager: " + leaveAppCount);
                if (leaveAppCount > 0) {
                    System.out.println("ℹ️ Manager has " + leaveAppCount + " leave applications - will preserve history using SYSTEM_MGR");
                }
            } catch (Exception e) {
                System.out.println("Warning: Could not check leave application count: " + e.getMessage());
            }
            
            // 5. Use safe deletion method that preserves leave application history
            System.out.println("Attempting to safely delete manager (preserving leave history): " + managerId);
            boolean isDeleted = managerDAO.deleteManagerAccountSafe(managerId.trim());
            
            if (isDeleted) {
                System.out.println("✅ Manager safely deleted with leave application history preserved: " + managerName);
                out.write("success");
            } else {
                System.err.println("❌ Failed to delete manager: " + managerName);
                out.write("error: Failed to delete manager '" + managerName + "'. Please try again.");
            }
            
        } catch (SQLException e) {
            System.err.println("Database error during manager deletion: " + e.getMessage());
            e.printStackTrace();
            out.write("error: Database error occurred while deleting the manager. Please try again.");
        } catch (Exception e) {
            System.err.println("Unexpected error during manager deletion: " + e.getMessage());
            e.printStackTrace();
            out.write("error: An unexpected error occurred while deleting the manager. Please try again or contact system administrator.");
        }
    }
    
    /**
     * Helper method to check pending leave requests for a manager
     */
    private int getPendingRequestsForManager(String managerId) throws SQLException {
        try (Connection conn = elms.connection.ConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                 "SELECT COUNT(*) FROM leaveapplication WHERE managerid = ? AND UPPER(leavestatus) = 'PENDING'")) {
            
            pstmt.setString(1, managerId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }
    
    /**
     * Helper method to check employees assigned to a manager
     * Updated for correct table structure
     */
    private int getEmployeeCountForManager(String managerId) throws SQLException {
        // Since your employee table doesn't have a managerid column, return 0
        // This prevents the check from blocking manager deletion
        System.out.println("ℹ️ Employee table has no managerid column - skipping employee assignment check");
        return 0;
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Redirect GET requests to the manager list
        response.sendRedirect("/ELMS_3.0/Admin/AdminViewManagerListController");
    }
}
package elms.controller;

import elms.DAO.EmployeeDAO;
import elms.DAO.LeaveApplicationDAO;
import elms.DAO.AdminDAO;
import elms.model.Employee;
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

@WebServlet(urlPatterns = {
    "/AdminDeleteEmployeeController", 
    "/Admin/AdminDeleteEmployeeController"
})
public class AdminDeleteEmployeeController extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private EmployeeDAO employeeDAO;
    private LeaveApplicationDAO leaveApplicationDAO;
    private AdminDAO adminDAO;
    
    @Override
    public void init() throws ServletException {
        super.init();
        employeeDAO = new EmployeeDAO();
        leaveApplicationDAO = new LeaveApplicationDAO();
        adminDAO = new AdminDAO();
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
        String employeeId = request.getParameter("employeeId");
        String action = request.getParameter("action");
        String adminPassword = request.getParameter("adminPassword");
        
        // Set response content type for AJAX
        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");
        
        // Verify this is a delete action
        if (!"delete".equals(action)) {
            response.getWriter().write("error: Invalid action specified.");
            return;
        }
        
        if (employeeId == null || employeeId.trim().isEmpty()) {
            response.getWriter().write("error: Invalid employee ID provided.");
            return;
        }
        
        if (adminPassword == null || adminPassword.trim().isEmpty()) {
            response.getWriter().write("error: Admin password is required.");
            return;
        }
        
        try {
            // Step 1: Verify admin password
            System.out.println("=== ADMIN PASSWORD VERIFICATION ===");
            System.out.println("Admin ID: " + adminId);
            System.out.println("Attempting to verify admin password...");
            
            boolean isAdminPasswordValid = verifyAdminPassword(adminId, adminPassword.trim());
            
            if (!isAdminPasswordValid) {
                System.out.println("❌ Admin password verification failed");
                response.getWriter().write("invalid_password: Invalid admin password");
                return;
            }
            
            System.out.println("✅ Admin password verified successfully");
            
            // Step 2: Check if employee exists
            Employee employee = employeeDAO.getEmployeeByIdOnly(employeeId.trim());
            if (employee == null) {
                response.getWriter().write("error: Employee not found.");
                return;
            }
            
            String employeeName = employee.getEmployeeName();
            String employeeEmail = employee.getEmployeeEmail();
            
            // Step 3: Log what will be deleted due to CASCADE
            try {
                int leaveApplicationCount = getLeaveApplicationCountForEmployee(employeeId);
                if (leaveApplicationCount > 0) {
                    System.out.println("⚠️  CASCADE DELETE: Employee " + employeeName + " has " + 
                        leaveApplicationCount + " leave application(s) that will be automatically deleted.");
                }
            } catch (Exception e) {
                System.out.println("Warning: Could not check leave applications: " + e.getMessage());
            }
            
            // Step 4: Log the deletion attempt
            System.out.println("=== ADMIN EMPLOYEE DELETION ===");
            System.out.println("Admin ID: " + session.getAttribute("adminId"));
            System.out.println("Admin Name: " + session.getAttribute("adminName"));
            System.out.println("Employee to delete: " + employeeName + " (" + employeeId + ")");
            System.out.println("Employee Email: " + employeeEmail);
            
            // Step 5: Attempt to delete the employee (CASCADE DELETE will handle related records)
            boolean isDeleted = deleteEmployee(employeeId.trim());
            
            if (isDeleted) {
                System.out.println("✅ Employee deletion successful (with CASCADE DELETE)");
                
                // Log successful deletion for audit trail
                
                
                response.getWriter().write("success: Employee '" + employeeName + "' (" + employeeId + 
                    ") and all associated leave applications have been successfully deleted from the system.");
            } else {
                System.out.println("❌ Employee deletion failed");
                response.getWriter().write("error: Failed to delete employee '" + employeeName + "'. Please try again.");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Error during employee deletion: " + e.getMessage());
            
            // Log the error for debugging
           
            
            response.getWriter().write("error: An unexpected error occurred while deleting the employee. Please try again or contact system administrator.");
        }
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Redirect GET requests to the employee list
        response.sendRedirect("/ELMS_3.0/Admin/AdminViewEmployeeListController");
    }
    
    /**
     * Verify admin password using your existing AdminDAO
     */
    private boolean verifyAdminPassword(String adminId, String password) {
        System.out.println("=== VERIFYING ADMIN PASSWORD ===");
        System.out.println("Admin ID: " + adminId);
        
        try {
            // Use your existing AdminDAO method to authenticate admin
            Admin admin = adminDAO.authenticateAdmin(adminId, password);
            
            if (admin != null) {
                System.out.println("✅ Admin password verified successfully");
                return true;
            } else {
                System.out.println("❌ Admin password verification failed");
                return false;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error verifying admin password: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Hash password using SHA-256 (same method as your AdminDAO)
     */
    private String hashPassword(String password) {
        return AdminDAO.hashPassword(password);
    }
    
    /**
     * Helper method to delete employee from database
     */
    private boolean deleteEmployee(String employeeId) throws SQLException {
        try (Connection conn = elms.connection.ConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement("DELETE FROM employee WHERE employeeid = ?")) {
            
            pstmt.setString(1, employeeId);
            int rowsAffected = pstmt.executeUpdate();
            
            System.out.println("Employee deletion - rows affected: " + rowsAffected);
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("❌ SQL Error during employee deletion: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Helper method to check total leave applications for an employee
     */
    private int getLeaveApplicationCountForEmployee(String employeeId) throws SQLException {
        try (Connection conn = elms.connection.ConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                 "SELECT COUNT(*) FROM leaveapplication WHERE employeeid = ?")) {
            
            pstmt.setString(1, employeeId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                int count = rs.getInt(1);
                System.out.println("Employee " + employeeId + " has " + count + " total leave applications");
                return count;
            }
        } catch (SQLException e) {
            System.err.println("❌ Error checking leave applications: " + e.getMessage());
            throw e;
        }
        return 0;
    }
    
    /**
     * Helper method to check pending leave applications for an employee
     */
    private int getPendingLeaveApplicationsForEmployee(String employeeId) throws SQLException {
        try (Connection conn = elms.connection.ConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                 "SELECT COUNT(*) FROM leaveapplication WHERE employeeid = ? AND UPPER(leavestatus) = 'PENDING'")) {
            
            pstmt.setString(1, employeeId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                int count = rs.getInt(1);
                System.out.println("Employee " + employeeId + " has " + count + " pending leave applications");
                return count;
            }
        } catch (SQLException e) {
            System.err.println("❌ Error checking pending applications: " + e.getMessage());
            throw e;
        }
        return 0;
    }
    
    /**
     * Helper method to check active/future approved leave applications for an employee
     */
    private int getActiveFutureLeaveApplicationsForEmployee(String employeeId) throws SQLException {
        try (Connection conn = elms.connection.ConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                 "SELECT COUNT(*) FROM leaveapplication WHERE employeeid = ? AND UPPER(leavestatus) = 'APPROVED' AND leaveenddate >= CURRENT_DATE")) {
            
            pstmt.setString(1, employeeId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                int count = rs.getInt(1);
                System.out.println("Employee " + employeeId + " has " + count + " active/future approved leaves");
                return count;
            }
        } catch (SQLException e) {
            System.err.println("❌ Error checking active/future leaves: " + e.getMessage());
            throw e;
        }
        return 0;
    }
    
    
}
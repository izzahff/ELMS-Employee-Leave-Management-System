package elms.controller;

import elms.DAO.ManagerDAO;
import elms.model.Manager;
import elms.connection.ConnectionManager;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

@WebServlet("/executive-director-delete-manager")
public class DirectorDeleteManagerController extends HttpServlet {
    
    private static final long serialVersionUID = 1L;
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Redirect GET requests to the manager list
        response.sendRedirect(request.getContextPath() + "/executive-director-project-managers");
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        System.out.println("=== Director Delete Manager Controller - POST ===");
        
        HttpSession session = request.getSession();
        Manager director = (Manager) session.getAttribute("manager");
        String userType = (String) session.getAttribute("userType");
        
        // Check if user is logged in and is executive director
        if (director == null || !"executive_director".equals(userType)) {
            System.out.println("❌ Unauthorized access attempt - redirecting to login");
            response.sendRedirect(request.getContextPath() + "/Manager/ManagerLogin.jsp");
            return;
        }
        
        System.out.println("✅ Executive Director authorized: " + director.getManagername());
        
        // Get parameters
        String action = request.getParameter("action");
        String managerId = request.getParameter("managerId");
        String directorPassword = request.getParameter("directorPassword");
        
        System.out.println("📋 Request Parameters:");
        System.out.println("  Action: " + action);
        System.out.println("  Manager ID: " + managerId);
        System.out.println("  Password provided: " + (directorPassword != null && !directorPassword.isEmpty()));
        
        // Handle AJAX request with password verification
        if ("delete_with_password".equals(action)) {
            handlePasswordVerificationAndDelete(request, response, director, managerId, directorPassword);
            return;
        }
        
        // Handle regular deletion (fallback for original functionality)
        if (!"delete".equals(action) || managerId == null || managerId.trim().isEmpty()) {
            System.out.println("❌ Invalid parameters");
            session.setAttribute("errorMessage", "Invalid request parameters");
            response.sendRedirect(request.getContextPath() + "/executive-director-project-managers");
            return;
        }
        
        // Continue with regular deletion process
        performRegularDeletion(request, response, session, director, managerId);
    }
    
    /**
     * Handle AJAX password verification and deletion
     */
    private void handlePasswordVerificationAndDelete(HttpServletRequest request, HttpServletResponse response,
            Manager director, String managerId, String directorPassword) throws IOException {
        
        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        try {
            // Validate parameters
            if (managerId == null || managerId.trim().isEmpty()) {
                System.out.println("❌ Missing manager ID");
                out.print("error: Manager ID is required");
                return;
            }
            
            if (directorPassword == null || directorPassword.trim().isEmpty()) {
                System.out.println("❌ Missing director password");
                out.print("error: Password is required");
                return;
            }
            
            managerId = managerId.trim();
            directorPassword = directorPassword.trim();
            
            // Validate that the manager ID is a Project Manager (starts with PM)
            if (!managerId.toUpperCase().startsWith("PM")) {
                System.out.println("❌ Attempt to delete non-project manager: " + managerId);
                out.print("error: You can only delete Project Managers");
                return;
            }
            
            // Verify Executive Director's password
            if (!verifyDirectorPassword(director.getManagerid(), directorPassword)) {
                System.out.println("❌ Invalid director password for: " + director.getManagername());
                out.print("invalid_password");
                return;
            }
            
            System.out.println("✅ Director password verified successfully");
            
            // Check if the manager exists and is indeed a Project Manager
            Manager managerToDelete = getManagerById(managerId);
            
            if (managerToDelete == null) {
                System.out.println("❌ Manager not found: " + managerId);
                out.print("error: Project Manager not found");
                return;
            }
            
            // Check for dependent records (leave applications)
            int dependentRecords = countDependentLeaveApplications(managerId);
            
            if (dependentRecords > 0) {
                System.out.println("❌ Cannot delete - manager has dependent leave applications: " + dependentRecords);
                out.print("error: Cannot delete Project Manager. There are " + dependentRecords + 
                         " leave application(s) assigned to this manager. Please reassign these applications first.");
                return;
            }
            
            // Perform the deletion
            boolean deleted = deleteManager(managerId);
            
            if (deleted) {
                System.out.println("✅ Project Manager deleted successfully: " + managerId);
                
                // Log the deletion for audit purposes
                logManagerDeletion(director.getManagerid(), managerId, managerToDelete.getManagername());
                
                out.print("success");
                
            } else {
                System.out.println("❌ Failed to delete Project Manager: " + managerId);
                out.print("error: Failed to delete Project Manager from database");
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Database error in password verification: " + e.getMessage());
            e.printStackTrace();
            out.print("error: Database error occurred during deletion");
        } catch (Exception e) {
            System.err.println("❌ Unexpected error in password verification: " + e.getMessage());
            e.printStackTrace();
            out.print("error: An unexpected error occurred");
        } finally {
            out.flush();
            out.close();
        }
    }
    
    /**
     * Verify Executive Director's password
     */
    private boolean verifyDirectorPassword(String directorId, String inputPassword) throws SQLException {
        String sql = "SELECT managerpassword FROM manager WHERE managerid = ?";
        
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, directorId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String storedHashedPassword = rs.getString("managerpassword");
                    String inputHashedPassword = hashPassword(inputPassword);
                    
                    boolean passwordMatch = inputHashedPassword.equals(storedHashedPassword);
                    System.out.println("Password verification result: " + passwordMatch);
                    return passwordMatch;
                } else {
                    System.out.println("❌ Director not found in database: " + directorId);
                    return false;
                }
            }
        }
    }
    
    /**
     * Hash password using SHA-256 (same method as in your AdminDAO)
     */
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }
    
    /**
     * Handle regular deletion (fallback for original functionality)
     */
    private void performRegularDeletion(HttpServletRequest request, HttpServletResponse response,
            HttpSession session, Manager director, String managerId) throws IOException {
        
        managerId = managerId.trim();
        
        // Validate that the manager ID is a Project Manager (starts with PM)
        if (!managerId.toUpperCase().startsWith("PM")) {
            System.out.println("❌ Attempt to delete non-project manager: " + managerId);
            session.setAttribute("errorMessage", "You can only delete Project Managers");
            response.sendRedirect(request.getContextPath() + "/executive-director-project-managers");
            return;
        }
        
        try {
            // First, check if the manager exists and is indeed a Project Manager
            Manager managerToDelete = getManagerById(managerId);
            
            if (managerToDelete == null) {
                System.out.println("❌ Manager not found: " + managerId);
                session.setAttribute("errorMessage", "Project Manager not found");
                response.sendRedirect(request.getContextPath() + "/executive-director-project-managers");
                return;
            }
            
            // Verify it's a Project Manager
            if (!managerId.toUpperCase().startsWith("PM")) {
                System.out.println("❌ Not a Project Manager: " + managerId);
                session.setAttribute("errorMessage", "You can only delete Project Managers");
                response.sendRedirect(request.getContextPath() + "/executive-director-project-managers");
                return;
            }
            
            // Check for dependent records (leave applications)
            int dependentRecords = countDependentLeaveApplications(managerId);
            
            if (dependentRecords > 0) {
                System.out.println("❌ Cannot delete - manager has dependent leave applications: " + dependentRecords);
                session.setAttribute("errorMessage", 
                    String.format("Cannot delete Project Manager '%s'. There are %d leave application(s) assigned to this manager. " +
                    "Please reassign or handle these applications first.", 
                    managerToDelete.getManagername(), dependentRecords));
                response.sendRedirect(request.getContextPath() + "/executive-director-project-managers");
                return;
            }
            
            // Perform the deletion
            boolean deleted = deleteManager(managerId);
            
            if (deleted) {
                System.out.println("✅ Project Manager deleted successfully: " + managerId);
                session.setAttribute("successMessage", 
                    "Project Manager '" + managerToDelete.getManagername() + "' has been deleted successfully");
                
                // Log the deletion for audit purposes
                logManagerDeletion(director.getManagerid(), managerId, managerToDelete.getManagername());
                
            } else {
                System.out.println("❌ Failed to delete Project Manager: " + managerId);
                session.setAttribute("errorMessage", "Failed to delete Project Manager. Please try again.");
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Database error in DirectorDeleteManagerController: " + e.getMessage());
            e.printStackTrace();
            session.setAttribute("errorMessage", "Database error occurred while deleting Project Manager");
        } catch (Exception e) {
            System.err.println("❌ Unexpected error in DirectorDeleteManagerController: " + e.getMessage());
            e.printStackTrace();
            session.setAttribute("errorMessage", "An unexpected error occurred while deleting Project Manager");
        }
        
        // Redirect back to the manager list
        response.sendRedirect(request.getContextPath() + "/executive-director-project-managers");
    }
    
    /**
     * Get manager by ID
     */
    private Manager getManagerById(String managerId) throws SQLException {
        String sql = "SELECT managerid, managername, manageremail, managernophone, " +
                    "managerposition, profile_picture_path " +
                    "FROM manager WHERE managerid = ?";
        
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, managerId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Manager manager = new Manager();
                    manager.setManagerid(rs.getString("managerid"));
                    manager.setManagername(rs.getString("managername"));
                    manager.setManageremail(rs.getString("manageremail"));
                    manager.setManagernophone(rs.getString("managernophone"));
                    manager.setManagerposition(rs.getString("managerposition"));
                    manager.setProfilePicturePath(rs.getString("profile_picture_path"));
                    return manager;
                }
            }
        }
        
        return null;
    }
    
    /**
     * Count dependent leave applications for a manager
     */
    private int countDependentLeaveApplications(String managerId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM leaveapplication WHERE managerid = ?";
        
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, managerId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        
        return 0;
    }
    
    /**
     * Delete manager from database
     */
    private boolean deleteManager(String managerId) throws SQLException {
        String sql = "DELETE FROM manager WHERE managerid = ? AND UPPER(managerid) LIKE 'PM%'";
        
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, managerId);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }
    
    /**
     * Log manager deletion for audit purposes
     */
    private void logManagerDeletion(String executiveDirectorId, String deletedManagerId, String deletedManagerName) {
        try {
            String sql = "INSERT INTO audit_log (action_type, performed_by, target_id, target_name, action_timestamp, details) " +
                        "VALUES ('DELETE_MANAGER', ?, ?, ?, SYSDATE, ?)";
            
            String details = String.format("Executive Director deleted Project Manager. ID: %s, Name: %s", 
                                          deletedManagerId, deletedManagerName);
            
            try (Connection conn = ConnectionManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                pstmt.setString(1, executiveDirectorId);
                pstmt.setString(2, deletedManagerId);
                pstmt.setString(3, deletedManagerName);
                pstmt.setString(4, details);
                
                pstmt.executeUpdate();
                System.out.println("✅ Deletion logged for audit: " + deletedManagerId);
                
            }
        } catch (SQLException e) {
            // Log the error but don't fail the deletion process
            System.err.println("⚠️ Failed to log deletion for audit: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
package elms.controller;

import elms.DAO.AdminDAO;
import elms.model.Admin;
import java.io.IOException;
import java.sql.SQLException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * AdminChangePasswordController - Handles admin password change functionality
 * 
 * Features:
 * - Validates current password
 * - Ensures new password meets requirements
 * - Confirms new password matches confirmation
 * - Updates password in database with proper hashing
 * - Provides user feedback via JSON responses for AJAX handling
 */
@WebServlet("/adminChangePassword")
public class AdminChangePasswordController extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private AdminDAO adminDAO;

    /**
     * Constructor
     */
    public AdminChangePasswordController() {
        super();
        this.adminDAO = new AdminDAO();
    }

    /**
     * Handle GET requests - redirect to change password page
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        System.out.println("=== ADMIN CHANGE PASSWORD CONTROLLER - GET ===");
        
        // Check if admin is logged in
        HttpSession session = request.getSession(false); // Don't create new session
        
        // Debug session information
        if (session == null) {
            System.out.println("DEBUG: No session found");
        } else {
            System.out.println("DEBUG: Session ID: " + session.getId());
            System.out.println("DEBUG: Session creation time: " + new java.util.Date(session.getCreationTime()));
            System.out.println("DEBUG: Session last accessed: " + new java.util.Date(session.getLastAccessedTime()));
            System.out.println("DEBUG: Session max inactive interval: " + session.getMaxInactiveInterval());
            
            // Debug all session attributes
            java.util.Enumeration<String> attributeNames = session.getAttributeNames();
            System.out.println("DEBUG: Session attributes:");
            while (attributeNames.hasMoreElements()) {
                String attributeName = attributeNames.nextElement();
                Object attributeValue = session.getAttribute(attributeName);
                System.out.println("  - " + attributeName + " = " + attributeValue);
            }
        }
        
        String adminId = null;
        String adminName = null;
        
        if (session != null) {
            adminId = (String) session.getAttribute("adminId");
            adminName = (String) session.getAttribute("adminName");
            
            // Try alternative attribute names that might be used
            if (adminId == null) {
                adminId = (String) session.getAttribute("admin_id");
            }
            if (adminId == null) {
                adminId = (String) session.getAttribute("adminid");
            }
            if (adminName == null) {
                adminName = (String) session.getAttribute("admin_name");
            }
            if (adminName == null) {
                adminName = (String) session.getAttribute("adminname");
            }
        }
        
        System.out.println("DEBUG: Admin ID from session: " + adminId);
        System.out.println("DEBUG: Admin Name from session: " + adminName);
        
        if (adminId == null || adminId.trim().isEmpty()) {
            System.out.println("ERROR: No admin logged in, redirecting to login");
            System.out.println("DEBUG: Redirecting to: /ELMS_3.0/Admin/AdminLogin.jsp");
            response.sendRedirect("/ELMS_3.0/Admin/AdminLogin.jsp");
            return;
        }
        
        System.out.println("SUCCESS: Admin " + adminId + " (" + adminName + ") accessing change password page");
        
        // Forward to change password page
        request.getRequestDispatcher("/Admin/AdminChangePassword.jsp").forward(request, response);
    }

    /**
     * Handle POST requests - process password change
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        System.out.println("=== ADMIN CHANGE PASSWORD CONTROLLER - POST ===");
        
        // Check if admin is logged in
        HttpSession session = request.getSession(false); // Don't create new session
        
        // Debug session information
        System.out.println("DEBUG: Checking session for POST request...");
        if (session == null) {
            System.out.println("DEBUG: No session found for POST request");
        } else {
            System.out.println("DEBUG: Session found for POST request");
            System.out.println("DEBUG: Session attributes:");
            java.util.Enumeration<String> attributeNames = session.getAttributeNames();
            while (attributeNames.hasMoreElements()) {
                String attributeName = attributeNames.nextElement();
                Object attributeValue = session.getAttribute(attributeName);
                System.out.println("  - " + attributeName + " = " + attributeValue);
            }
        }
        
        String adminId = null;
        
        if (session != null) {
            adminId = (String) session.getAttribute("adminId");
            
            // Also try alternative attribute names if the first one doesn't work
            if (adminId == null) {
                adminId = (String) session.getAttribute("admin_id");
            }
            if (adminId == null) {
                adminId = (String) session.getAttribute("adminid");
            }
        }
        
        System.out.println("DEBUG: Admin ID extracted from session: '" + adminId + "'");
        
        if (adminId == null || adminId.trim().isEmpty()) {
            System.out.println("ERROR: No admin logged in during password change");
            
            // Send JSON response for AJAX handling
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"success\": false, \"message\": \"Session expired. Please login again.\", \"redirect\": \"/ELMS_3.0/Admin/AdminLogin.jsp\"}");
            return;
        }
        
        // Get form parameters
        String currentPassword = request.getParameter("currentPassword");
        String newPassword = request.getParameter("newPassword");
        String confirmPassword = request.getParameter("confirmPassword");
        
        System.out.println("DEBUG: Processing password change for admin: " + adminId);
        System.out.println("DEBUG: Raw parameters received:");
        System.out.println("  - currentPassword: " + (currentPassword != null ? "[RECEIVED-" + currentPassword.length() + " chars]" : "null"));
        System.out.println("  - newPassword: " + (newPassword != null ? "[RECEIVED-" + newPassword.length() + " chars]" : "null"));
        System.out.println("  - confirmPassword: " + (confirmPassword != null ? "[RECEIVED-" + confirmPassword.length() + " chars]" : "null"));
        
        // Debug: Print all parameters
        System.out.println("DEBUG: All request parameters:");
        request.getParameterMap().forEach((key, values) -> {
            System.out.println("  " + key + " = " + (key.toLowerCase().contains("password") ? "[HIDDEN-" + values[0].length() + " chars]" : String.join(",", values)));
        });
        
        System.out.println("DEBUG: Current password provided: " + (currentPassword != null && !currentPassword.isEmpty()));
        System.out.println("DEBUG: New password provided: " + (newPassword != null && !newPassword.isEmpty()));
        System.out.println("DEBUG: Confirm password provided: " + (confirmPassword != null && !confirmPassword.isEmpty()));
        
        // Validate input parameters
        if (currentPassword == null || currentPassword.trim().isEmpty()) {
            System.out.println("ERROR: Current password is empty");
            sendErrorResponse(response, "Current password is required");
            return;
        }
        
        if (newPassword == null || newPassword.trim().isEmpty()) {
            System.out.println("ERROR: New password is empty");
            sendErrorResponse(response, "New password is required");
            return;
        }
        
        if (confirmPassword == null || confirmPassword.trim().isEmpty()) {
            System.out.println("ERROR: Confirm password is empty");
            sendErrorResponse(response, "Password confirmation is required");
            return;
        }
        
        // Validate new password requirements
        String passwordValidationError = validatePassword(newPassword);
        if (passwordValidationError != null) {
            System.out.println("ERROR: Password validation failed: " + passwordValidationError);
            sendErrorResponse(response, passwordValidationError);
            return;
        }
        
        // Check if new password matches confirmation
        if (!newPassword.equals(confirmPassword)) {
            System.out.println("ERROR: New passwords do not match");
            sendErrorResponse(response, "New passwords do not match");
            return;
        }
        
        // Check if new password is different from current password
        if (currentPassword.equals(newPassword)) {
            System.out.println("ERROR: New password is same as current password");
            sendErrorResponse(response, "New password must be different from current password");
            return;
        }
        
        try {
            // Step 1: Validate current password by attempting authentication
            System.out.println("DEBUG: Validating current password...");
            Admin admin = adminDAO.authenticateAdmin(adminId, currentPassword);
            
            if (admin == null) {
                System.out.println("ERROR: Current password is incorrect");
                sendErrorResponse(response, "Current password is incorrect");
                return;
            }
            
            System.out.println("SUCCESS: Current password validated for admin: " + admin.getAdminName());
            
            // Step 2: Update password in database (AdminDAO.updateAdminPassword handles hashing)
            System.out.println("DEBUG: Updating password in database for admin ID: " + adminId);
            System.out.println("DEBUG: Admin name: " + admin.getAdminName());
            
            boolean updateSuccess = adminDAO.updateAdminPassword(adminId, newPassword);
            System.out.println("DEBUG: Update result: " + updateSuccess);
            
            if (updateSuccess) {
                System.out.println("SUCCESS: Password updated successfully in database");
                
                // Step 3: Verify the password was actually updated by testing login
                System.out.println("DEBUG: Verifying password was updated...");
                Admin updatedAdmin = adminDAO.authenticateAdmin(adminId, newPassword);
                
                if (updatedAdmin != null) {
                    System.out.println("✅ VERIFICATION SUCCESS: Can authenticate with new password");
                    
                    // Log the successful password change
                    System.out.println("PASSWORD CHANGE SUCCESS: Admin " + adminId + " (" + admin.getAdminName() + ") changed password successfully");
                    
                    // Set success message in SESSION for popup modal (not in JSON response)
                    session.setAttribute("passwordChangeSuccess", "Your password has been changed successfully! You can now use your new password to login.");
                    
                    // Send success response with redirect for AJAX
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write("{\"success\": true, \"redirect\": \"/ELMS_3.0/Admin/AdminSettings.jsp\"}");
                    return;
                    
                } else {
                    System.out.println("❌ VERIFICATION FAILED: Cannot authenticate with new password");
                    sendErrorResponse(response, "Password update verification failed. Please try again.");
                }
                
            } else {
                System.out.println("ERROR: Failed to update password in database");
                sendErrorResponse(response, "Failed to update password. Please try again.");
            }
            
        } catch (SQLException e) {
            System.err.println("ERROR: SQL Exception during password change: " + e.getMessage());
            e.printStackTrace();
            
            sendErrorResponse(response, "Database error occurred. Please try again later.");
            
        } catch (Exception e) {
            System.err.println("ERROR: Unexpected exception during password change: " + e.getMessage());
            e.printStackTrace();
            
            sendErrorResponse(response, "An unexpected error occurred. Please try again later.");
        }
        
        System.out.println("=== ADMIN CHANGE PASSWORD CONTROLLER - END ===");
    }
    
    /**
     * Send success response as JSON
     */
    private void sendSuccessResponse(HttpServletResponse response, String message) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"success\": true, \"message\": \"" + message + "\"}");
    }
    
    /**
     * Send error response as JSON
     */
    private void sendErrorResponse(HttpServletResponse response, String message) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"success\": false, \"message\": \"" + message + "\"}");
    }
    
    /**
     * Validate password requirements
     * @param password The password to validate
     * @return Error message if invalid, null if valid
     */
    private String validatePassword(String password) {
        System.out.println("DEBUG: Validating password requirements...");
        
        if (password == null) {
            return "Password cannot be null";
        }
        
        password = password.trim();
        
        if (password.length() < 8) {
            System.out.println("ERROR: Password too short: " + password.length() + " characters");
            return "Password must be at least 8 characters long";
        }
        
        if (password.length() > 128) {
            System.out.println("ERROR: Password too long: " + password.length() + " characters");
            return "Password must be less than 128 characters long";
        }
        
        if (!password.matches(".*[A-Z].*")) {
            System.out.println("ERROR: Password missing uppercase letter");
            return "Password must contain at least one uppercase letter";
        }
        
        if (!password.matches(".*[a-z].*")) {
            System.out.println("ERROR: Password missing lowercase letter");
            return "Password must contain at least one lowercase letter";
        }
        
        if (!password.matches(".*[0-9].*")) {
            System.out.println("ERROR: Password missing number");
            return "Password must contain at least one number";
        }
        
        // Optional: Check for special characters (uncomment if needed)
        /*
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) {
            System.out.println("ERROR: Password missing special character");
            return "Password must contain at least one special character";
        }
        */
        
        System.out.println("SUCCESS: Password meets all requirements");
        return null; // Password is valid
    }
    
    /**
     * Additional utility method to check password strength
     * @param password The password to check
     * @return Strength level: "weak", "medium", "strong"
     */
    private String getPasswordStrength(String password) {
        if (password == null || password.length() < 8) {
            return "weak";
        }
        
        int score = 0;
        
        // Length check
        if (password.length() >= 8) score++;
        if (password.length() >= 12) score++;
        
        // Character variety checks
        if (password.matches(".*[A-Z].*")) score++;  // Uppercase
        if (password.matches(".*[a-z].*")) score++;  // Lowercase  
        if (password.matches(".*[0-9].*")) score++;  // Numbers
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) score++; // Special chars
        
        // Avoid common patterns
        if (!password.matches(".*(123|abc|qwe|password|admin).*")) score++;
        
        if (score <= 3) return "weak";
        if (score <= 5) return "medium";
        return "strong";
    }
}
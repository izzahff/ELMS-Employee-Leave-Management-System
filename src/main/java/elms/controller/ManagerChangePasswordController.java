package elms.controller;

import elms.DAO.ManagerDAO;
import elms.model.Manager;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * ManagerChangePasswordController - Handles password change functionality for both Manager and Executive Director
 * 
 * Features:
 * - Validates current password
 * - Ensures new password meets requirements
 * - Confirms new password matches confirmation
 * - Updates password in database with proper hashing
 * - Provides user feedback via JSON responses for AJAX handling
 * - Supports both Manager and Executive Director roles
 */
@WebServlet(urlPatterns = {"/managerChangePassword", "/executiveDirectorChangePassword"})
public class ManagerChangePasswordController extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private ManagerDAO managerDAO;

    /**
     * Constructor
     */
    public ManagerChangePasswordController() {
        super();
        this.managerDAO = new ManagerDAO();
    }

    /**
     * Handle GET requests - redirect to appropriate change password page
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        System.out.println("=== MANAGER CHANGE PASSWORD CONTROLLER - GET ===");
        
        // Check if manager is logged in
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
        
        Manager manager = null;
        
        if (session != null) {
            manager = (Manager) session.getAttribute("manager");
        }
        
        System.out.println("DEBUG: Manager from session: " + (manager != null ? manager.getManagername() : "null"));
        
        if (manager == null) {
            System.out.println("ERROR: No manager logged in, redirecting to login");
            System.out.println("DEBUG: Redirecting to: /ELMS_3.0/Manager/ManagerLogin.jsp");
            response.sendRedirect("/ELMS_3.0/Manager/ManagerLogin.jsp");
            return;
        }
        
        System.out.println("SUCCESS: Manager " + manager.getManagerid() + " (" + manager.getManagername() + ") accessing change password page");
        
        // Determine which page to forward to based on the URL pattern
        String servletPath = request.getServletPath();
        if (servletPath.contains("executiveDirector")) {
            // Forward to Executive Director change password page
            request.getRequestDispatcher("/Manager/DirectorChangePassword.jsp").forward(request, response);
        } else {
            // Forward to Manager change password page
            request.getRequestDispatcher("/Manager/ManagerChangePassword.jsp").forward(request, response);
        }
    }

    /**
     * Handle POST requests - process password change
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        System.out.println("=== MANAGER CHANGE PASSWORD CONTROLLER - POST ===");
        
        // Check if manager is logged in
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
        
        Manager manager = null;
        
        if (session != null) {
            manager = (Manager) session.getAttribute("manager");
        }
        
        System.out.println("DEBUG: Manager extracted from session: " + (manager != null ? manager.getManagername() : "null"));
        
        if (manager == null) {
            System.out.println("ERROR: No manager logged in during password change");
            
            // Send JSON response for AJAX handling
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"success\": false, \"message\": \"Session expired. Please login again.\", \"redirect\": \"/ELMS_3.0/Manager/ManagerLogin.jsp\"}");
            return;
        }
        
        // Get form parameters
        String currentPassword = request.getParameter("currentPassword");
        String newPassword = request.getParameter("newPassword");
        String confirmPassword = request.getParameter("confirmPassword");
        
        System.out.println("DEBUG: Processing password change for manager: " + manager.getManagerid());
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
            
            // First, get the current manager from database to get the stored hashed password
            Manager currentManager = managerDAO.getManagerById(manager.getManagerid());
            if (currentManager == null) {
                System.out.println("ERROR: Manager not found in database");
                sendErrorResponse(response, "Manager not found. Please login again.");
                return;
            }
            
            // Hash the current password input and compare with stored hash
            String hashedCurrentPassword = hashPassword(currentPassword);
            if (hashedCurrentPassword == null) {
                System.out.println("ERROR: Failed to hash current password");
                sendErrorResponse(response, "Failed to process current password. Please try again.");
                return;
            }
            
            String storedPasswordHash = currentManager.getManagerpassword();
            System.out.println("DEBUG: Stored password hash: " + (storedPasswordHash != null ? "[HASH-" + storedPasswordHash.length() + " chars]" : "null"));
            System.out.println("DEBUG: Input password hash: " + (hashedCurrentPassword != null ? "[HASH-" + hashedCurrentPassword.length() + " chars]" : "null"));
            
            if (!hashedCurrentPassword.equals(storedPasswordHash)) {
                System.out.println("ERROR: Current password is incorrect");
                sendErrorResponse(response, "Current password is incorrect");
                return;
            }
            
            System.out.println("SUCCESS: Current password validated for manager: " + currentManager.getManagername());
            
            // Step 2: Hash the new password
            System.out.println("DEBUG: Hashing new password...");
            String hashedNewPassword = hashPassword(newPassword);
            
            if (hashedNewPassword == null) {
                System.out.println("ERROR: Failed to hash new password");
                sendErrorResponse(response, "Failed to process new password. Please try again.");
                return;
            }
            
            // Step 3: Update password in database
            System.out.println("DEBUG: Updating password in database for manager ID: " + manager.getManagerid());
            System.out.println("DEBUG: Manager name: " + manager.getManagername());
            
            // Create updated manager object with new password
            Manager updatedManager = new Manager();
            updatedManager.setManagerid(manager.getManagerid());
            updatedManager.setManagername(manager.getManagername());
            updatedManager.setManageremail(manager.getManageremail());
            updatedManager.setManagernophone(manager.getManagernophone());
            updatedManager.setManagerposition(manager.getManagerposition());
            updatedManager.setManagerpassword(hashedNewPassword);
            updatedManager.setProfilePicturePath(manager.getProfilePicturePath());
            
            boolean updateSuccess = managerDAO.updateManager(updatedManager);
            System.out.println("DEBUG: Update result: " + updateSuccess);
            
            if (updateSuccess) {
                System.out.println("SUCCESS: Password updated successfully in database");
                
                // Step 4: Verify the password was actually updated by getting the updated manager
                System.out.println("DEBUG: Verifying password was updated...");
                Manager verifiedManager = managerDAO.getManagerById(manager.getManagerid());
                
                if (verifiedManager != null) {
                    // Check if the stored password hash matches our new hashed password
                    String verifiedStoredHash = verifiedManager.getManagerpassword();
                    if (hashedNewPassword.equals(verifiedStoredHash)) {
                        System.out.println("✅ VERIFICATION SUCCESS: Password hash matches in database");
                        
                        // Update session with new manager data (with hashed password)
                        updatedManager.setManagerpassword(hashedNewPassword); // Keep hashed password in session
                        session.setAttribute("manager", updatedManager);
                        
                        // Log the successful password change
                        System.out.println("PASSWORD CHANGE SUCCESS: Manager " + manager.getManagerid() + " (" + manager.getManagername() + ") changed password successfully");
                        
                        // Set success message in SESSION for popup modal (not in JSON response)
                        session.setAttribute("passwordChangeSuccess", "Your password has been changed successfully! You can now use your new password to login.");
                        
                        // Send success response with redirect for AJAX
                        response.setContentType("application/json");
                        response.setCharacterEncoding("UTF-8");
                        response.getWriter().write("{\"success\": true, \"redirect\": \"/ELMS_3.0/Manager/ManagerSettings.jsp\"}");
                        return;
                        
                    } else {
                        System.out.println("❌ VERIFICATION FAILED: Password hash does not match in database");
                        sendErrorResponse(response, "Password update verification failed. Please try again.");
                    }
                
            } else {
                System.out.println("ERROR: Failed to update password in database");
                sendErrorResponse(response, "Failed to update password. Please try again.");
            }
            }
        } catch (Exception e) {
            System.err.println("ERROR: Unexpected exception during password change: " + e.getMessage());
            e.printStackTrace();
            
            sendErrorResponse(response, "An unexpected error occurred. Please try again later.");
        }
        
        System.out.println("=== MANAGER CHANGE PASSWORD CONTROLLER - END ===");
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
        
        System.out.println("SUCCESS: Password meets all requirements");
        return null; // Password is valid
    }
    
    /**
     * Hash password using SHA-256 (consistent with AdminDAO)
     * @param password The password to hash
     * @return Hashed password or null if hashing fails
     */
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes("UTF-8"));
            
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            
            return sb.toString();
            
        } catch (NoSuchAlgorithmException | java.io.UnsupportedEncodingException e) {
            System.err.println("ERROR: Failed to hash password: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
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
    
    /**
     * Log user activity for audit purposes
     */
    private void logUserActivity(HttpServletRequest request, String action, String details) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            Manager manager = (Manager) session.getAttribute("manager");
            if (manager != null) {
                String clientIP = getClientIP(request);
                
                System.out.println("[AUDIT LOG] " + 
                    "Manager: " + manager.getManagername() + " (" + manager.getManagerid() + ") " +
                    "Action: " + action + " " +
                    "Details: " + details + " " +
                    "IP: " + clientIP + " " +
                    "Time: " + new java.util.Date());
            }
        }
    }
    
    /**
     * Gets client IP address considering proxy headers
     */
    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIP = request.getHeader("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty()) {
            return xRealIP;
        }
        
        return request.getRemoteAddr();
    }
}
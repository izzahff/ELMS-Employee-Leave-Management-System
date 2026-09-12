package elms.controller;

import elms.DAO.EmployeeDAO;
import elms.model.Employee;
import java.io.IOException;
import java.sql.SQLException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;


/**
 * EmployeeChangePasswordController - Handles employee password change functionality
 * 
 * Features:
 * - Validates current password
 * - Ensures new password meets requirements
 * - Confirms new password matches confirmation
 * - Updates password in database with proper hashing
 * - Provides user feedback
 */
@WebServlet("/employeeChangePassword")
public class EmployeeChangePasswordController extends HttpServlet {
    private static final long serialVersionUID = 1L;

    /**
     * Constructor
     */
    public EmployeeChangePasswordController() {
        super();
    }

    /**
     * Handle GET requests - redirect to change password page
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        System.out.println("=== EMPLOYEE CHANGE PASSWORD CONTROLLER - GET ===");
        
        // Check if user is logged in
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
        
        String employeeId = null;
        String employeeName = null;
        
        if (session != null) {
            employeeId = (String) session.getAttribute("employeeid"); // Note: lowercase 'i'
            employeeName = (String) session.getAttribute("employeename"); // Note: lowercase 'n'
            
            // Also try alternative attribute names that might be used
            if (employeeId == null) {
                employeeId = (String) session.getAttribute("employeeId"); // Camelcase
            }
            if (employeeId == null) {
                employeeId = (String) session.getAttribute("employee_id");
            }
            if (employeeId == null) {
                employeeId = (String) session.getAttribute("empId");
            }
            if (employeeName == null) {
                employeeName = (String) session.getAttribute("employeeName");
            }
            if (employeeName == null) {
                employeeName = (String) session.getAttribute("employee_name");
            }
        }
        
        System.out.println("DEBUG: Employee ID from session: " + employeeId);
        System.out.println("DEBUG: Employee Name from session: " + employeeName);
        
        if (employeeId == null || employeeId.trim().isEmpty()) {
            System.out.println("ERROR: No employee logged in, redirecting to login");
            System.out.println("DEBUG: Redirecting to: /ELMS_3.0/Employee/EmployeeLogin.jsp");
            response.sendRedirect("/ELMS_3.0/Employee/EmployeeLogin.jsp");
            return;
        }
        
        System.out.println("SUCCESS: Employee " + employeeId + " (" + employeeName + ") accessing change password page");
        
        // Forward to change password page
        request.getRequestDispatcher("/Employee/EmployeeChangePassword.jsp").forward(request, response);
    }

    /**
     * Handle POST requests - process password change
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        System.out.println("=== EMPLOYEE CHANGE PASSWORD CONTROLLER - POST ===");
        
        // Check if user is logged in
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
        
        String employeeId = null;
        
        if (session != null) {
            employeeId = (String) session.getAttribute("employeeid"); // Note: lowercase 'i'
            
            // Also try alternative attribute names if the first one doesn't work
            if (employeeId == null) {
                employeeId = (String) session.getAttribute("employeeId"); // Camelcase
            }
            if (employeeId == null) {
                employeeId = (String) session.getAttribute("employee_id");
            }
            if (employeeId == null) {
                employeeId = (String) session.getAttribute("empId");
            }
        }
        
        System.out.println("DEBUG: Employee ID extracted from session: '" + employeeId + "'");
        
        if (employeeId == null || employeeId.trim().isEmpty()) {
            System.out.println("ERROR: No employee logged in during password change");
            
            // Instead of redirecting, send JSON response for AJAX handling
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"success\": false, \"message\": \"Session expired. Please login again.\", \"redirect\": \"/ELMS_3.0/Employee/EmployeeLogin.jsp\"}");
            return;
        }
        
        // Get form parameters
        String currentPassword = request.getParameter("currentPassword");
        String newPassword = request.getParameter("newPassword");
        String confirmPassword = request.getParameter("confirmPassword");
        
        System.out.println("DEBUG: Processing password change for employee: " + employeeId);
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
            // Step 1: Validate current password by attempting login
            System.out.println("DEBUG: Validating current password...");
            Employee employee = EmployeeDAO.validateLogin(employeeId, currentPassword);
            
            if (employee == null) {
                System.out.println("ERROR: Current password is incorrect");
                sendErrorResponse(response, "Current password is incorrect");
                return;
            }
            
            System.out.println("SUCCESS: Current password validated for employee: " + employee.getEmployeeName());
            
            // Step 2: Hash the new password
            System.out.println("DEBUG: Hashing new password...");
            String hashedNewPassword = EmployeeDAO.hashPassword(newPassword);
            System.out.println("DEBUG: New password hashed successfully. Hash length: " + hashedNewPassword.length());
            System.out.println("DEBUG: Hash starts with: " + hashedNewPassword.substring(0, Math.min(10, hashedNewPassword.length())) + "...");
            
            // Step 3: Verify employee exists before updating
            System.out.println("DEBUG: Verifying employee exists before password update...");
            Employee existingEmployee = EmployeeDAO.getEmployeeByIdOnly(employeeId);
            if (existingEmployee == null) {
                System.out.println("ERROR: Employee not found in database: " + employeeId);
                sendErrorResponse(response, "Employee account not found. Please contact administrator.");
                return;
            }
            
            // Step 4: Update password in database
            System.out.println("DEBUG: Updating password in database for employee ID: " + employeeId);
            System.out.println("DEBUG: Employee name: " + existingEmployee.getEmployeeName());
            System.out.println("DEBUG: Current hashed password in DB: " + existingEmployee.getEmployeePassword().substring(0, 10) + "...");
            
            String updateResult = EmployeeDAO.updateEmployeePassword(employeeId, hashedNewPassword);
            System.out.println("DEBUG: Update result: " + updateResult);
            
            if (updateResult != null && updateResult.startsWith("success")) {
                System.out.println("SUCCESS: Password updated successfully in database");
                
                // Step 5: Verify the password was actually updated
                System.out.println("DEBUG: Verifying password was updated...");
                Employee updatedEmployee = EmployeeDAO.getEmployeeByIdOnly(employeeId);
                if (updatedEmployee != null) {
                    String newHashInDB = updatedEmployee.getEmployeePassword();
                    System.out.println("DEBUG: New hash in DB: " + newHashInDB.substring(0, 10) + "...");
                    
                    if (newHashInDB.equals(hashedNewPassword)) {
                        System.out.println("✅ VERIFICATION SUCCESS: Password hash matches in database");
                        
                        // Test if we can login with new password
                        Employee loginTest = EmployeeDAO.validateLogin(employeeId, newPassword);
                        if (loginTest != null) {
                            System.out.println("✅ LOGIN TEST SUCCESS: Can login with new password");
                        } else {
                            System.out.println("❌ LOGIN TEST FAILED: Cannot login with new password");
                        }
                        
                    } else {
                        System.out.println("❌ VERIFICATION FAILED: Password hash does NOT match in database");
                        System.out.println("Expected: " + hashedNewPassword);
                        System.out.println("Actual:   " + newHashInDB);
                    }
                } else {
                    System.out.println("❌ VERIFICATION FAILED: Could not retrieve updated employee");
                }
                
                // Log the successful password change
                System.out.println("PASSWORD CHANGE SUCCESS: Employee " + employeeId + " (" + employee.getEmployeeName() + ") changed password successfully");
                
                // Set success message in SESSION for popup modal (not in JSON response)
                session.setAttribute("passwordChangeSuccess", "Your password has been changed successfully! You can now use your new password to login.");
                
                // Send success response with redirect for AJAX
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("{\"success\": true, \"redirect\": \"/ELMS_3.0/Employee/EmployeeSettings.jsp\"}");
                return;
                
            } else {
                System.out.println("ERROR: Failed to update password. Update result: " + updateResult);
                sendErrorResponse(response, "Failed to update password: " + (updateResult != null ? updateResult.replace("error: ", "") : "Unknown error"));
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
        
        System.out.println("=== EMPLOYEE CHANGE PASSWORD CONTROLLER - END ===");
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
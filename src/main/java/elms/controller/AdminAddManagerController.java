package elms.controller;

import elms.DAO.ManagerDAO;
import elms.model.Manager;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;

@WebServlet(name = "AdminAddManagerController", urlPatterns = {"/AdminAddManagerController", "/Admin/AdminAddManagerController"})
public class AdminAddManagerController extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    // Validation patterns
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );
    
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^[\\+]?[\\d\\s\\-\\(\\)]{10,}$"
    );
    
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$"
    );
    
    private static final Pattern NAME_PATTERN = Pattern.compile(
        "^[a-zA-Z\\s]{2,50}$"
    );
    
    private ManagerDAO managerDAO;
    
    @Override
    public void init() throws ServletException {
        super.init();
        try {
            managerDAO = new ManagerDAO();
            System.out.println("AdminAddManagerController initialized successfully");
        } catch (Exception e) {
            System.err.println("Error initializing AdminAddManagerController: " + e.getMessage());
            e.printStackTrace();
            throw new ServletException("Failed to initialize controller", e);
        }
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        System.out.println("AdminAddManagerController doGet called");
        
        // Check admin session
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("adminId") == null) {
            System.out.println("No admin session found, redirecting to login");
            response.sendRedirect(request.getContextPath() + "/Admin/AdminLogin.jsp");
            return;
        }
        
        System.out.println("Admin session valid, forwarding to add manager form");
        
        // Forward to the add manager form
        request.getRequestDispatcher("/Admin/AdminAddManager.jsp").forward(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        System.out.println("AdminAddManagerController doPost called");
        
        // Check admin session
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("adminId") == null) {
            response.sendRedirect(request.getContextPath() + "/Admin/AdminLogin.jsp");
            return;
        }
        
        try {
            // Get form parameters
            String managerName = request.getParameter("managername");
            String managerEmail = request.getParameter("manageremail");
            String managerPhone = request.getParameter("managerphone");
            String managerPosition = request.getParameter("managerposition");
            String managerPassword = request.getParameter("managerpassword");
            String confirmPassword = request.getParameter("confirmPassword");
            
            System.out.println("Processing manager registration for: " + managerEmail);
            System.out.println("Manager position: " + managerPosition);
            
            // Trim whitespace
            managerName = (managerName != null) ? managerName.trim() : "";
            managerEmail = (managerEmail != null) ? managerEmail.trim().toLowerCase() : "";
            managerPhone = (managerPhone != null) ? managerPhone.trim() : "";
            managerPosition = (managerPosition != null) ? managerPosition.trim() : "";
            managerPassword = (managerPassword != null) ? managerPassword : "";
            confirmPassword = (confirmPassword != null) ? confirmPassword : "";
            
            // Validate input
            String validationError = validateInput(managerName, managerEmail, managerPhone, 
                                                 managerPosition, managerPassword, confirmPassword);
            
            if (validationError != null) {
                System.out.println("Validation error: " + validationError);
                request.setAttribute("errorMessage", validationError);
                request.getRequestDispatcher("/Admin/AdminAddManager.jsp").forward(request, response);
                return;
            }
            
            // Check if email already exists
            if (managerDAO.managerEmailExists(managerEmail)) {
                System.out.println("Email already exists: " + managerEmail);
                request.setAttribute("errorMessage", 
                    "A manager with this email address already exists. Please use a different email.");
                request.getRequestDispatcher("/Admin/AdminAddManager.jsp").forward(request, response);
                return;
            }
            
            // Hash the password
            String hashedPassword = hashPassword(managerPassword);
            if (hashedPassword == null) {
                System.out.println("Password hashing failed");
                request.setAttribute("errorMessage", 
                    "An error occurred while processing the password. Please try again.");
                request.getRequestDispatcher("/Admin/AdminAddManager.jsp").forward(request, response);
                return;
            }
            
            // Create manager object
            Manager newManager = new Manager();
            newManager.setManagername(managerName);
            newManager.setManageremail(managerEmail);
            newManager.setManagernophone(managerPhone.isEmpty() ? null : managerPhone);
            newManager.setManagerposition(managerPosition);
            newManager.setManagerpassword(hashedPassword);
            
            // Add manager to database
            String generatedId = managerDAO.addManagerAndReturnId(newManager);
            
            if (generatedId != null) {
                System.out.println("Manager created successfully with ID: " + generatedId);
                
                // Success - set manager details for success page
                request.setAttribute("managerId", generatedId);
                request.setAttribute("managerName", managerName);
                request.setAttribute("managerEmail", managerEmail);
                request.setAttribute("managerPhone", managerPhone);
                request.setAttribute("managerPosition", managerPosition);
                
                // Log successful manager creation
                logUserActivity(request, "CREATE_MANAGER", 
                    "Created new manager: " + managerName + " (" + generatedId + ") - Position: " + managerPosition);
                
                // FIXED: Forward to success page instead of redirecting to manager list
                System.out.println("Forwarding to success page with manager details");
                request.getRequestDispatcher("/Admin/AdminManagerRegistrationSuccess.jsp").forward(request, response);
                
            } else {
                System.out.println("Failed to add manager to database");
                // Failed to add manager
                request.setAttribute("errorMessage", 
                    "Failed to add the manager. Please check your input and try again.");
                request.getRequestDispatcher("/Admin/AdminAddManager.jsp").forward(request, response);
            }
            
        } catch (Exception e) {
            // Log the error
            System.err.println("Error in AdminAddManagerController: " + e.getMessage());
            e.printStackTrace();
            
            // Set error message
            request.setAttribute("errorMessage", 
                "An unexpected error occurred while adding the manager. Please try again.");
            request.getRequestDispatcher("/Admin/AdminAddManager.jsp").forward(request, response);
        }
    }
    
    /**
     * Logs user activity for audit purposes
     */
    private void logUserActivity(HttpServletRequest request, String action, String details) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            String adminId = (String) session.getAttribute("adminId");
            String adminName = (String) session.getAttribute("adminName");
            String clientIP = getClientIP(request);
            
            System.out.println("[AUDIT LOG] " + 
                "Admin: " + adminName + " (" + adminId + ") " +
                "Action: " + action + " " +
                "Details: " + details + " " +
                "IP: " + clientIP + " " +
                "Time: " + new java.util.Date());
        }
    }
    
    /**
     * Validates all input parameters
     */
    private String validateInput(String name, String email, String phone, 
                               String position, String password, String confirmPassword) {
        
        // Check required fields
        if (name == null || name.isEmpty()) {
            return "Manager name is required.";
        }
        
        if (email == null || email.isEmpty()) {
            return "Manager email is required.";
        }
        
        if (position == null || position.isEmpty()) {
            return "Manager position is required.";
        }
        
        if (password == null || password.isEmpty()) {
            return "Password is required.";
        }
        
        if (confirmPassword == null || confirmPassword.isEmpty()) {
            return "Password confirmation is required.";
        }
        
        // Validate name format
        if (!NAME_PATTERN.matcher(name).matches()) {
            return "Manager name must contain only letters and spaces, and be between 2-50 characters long.";
        }
        
        // Validate email format
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return "Please enter a valid email address.";
        }
        
        // Validate phone if provided
        if (phone != null && !phone.isEmpty() && !PHONE_PATTERN.matcher(phone).matches()) {
            return "Please enter a valid phone number (minimum 10 digits).";
        }
        
        // Validate position
        if (!position.equals("Executive Director") && !position.equals("Project Manager")) {
            return "Please select a valid manager position.";
        }
        
        // Validate password strength
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            return "Password must be at least 8 characters long and contain at least one uppercase letter, one lowercase letter, and one number.";
        }
        
        // Check password confirmation
        if (!password.equals(confirmPassword)) {
            return "Passwords do not match. Please ensure both password fields are identical.";
        }
        
        // Check password length (additional safety check)
        if (password.length() > 100) {
            return "Password is too long. Maximum length is 100 characters.";
        }
        
        // Check email length
        if (email.length() > 100) {
            return "Email address is too long. Maximum length is 100 characters.";
        }
        
        // Check name length
        if (name.length() > 100) {
            return "Manager name is too long. Maximum length is 100 characters.";
        }
        
        return null; // No validation errors
    }
    
    /**
     * Hashes the password using SHA-256
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
            e.printStackTrace();
            return null;
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
package elms.controller;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.regex.Pattern;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import elms.DAO.AdminDAO;
import elms.model.Admin;

@WebServlet("/AdminAddAdminController")
public class AdminAddAdminController extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private AdminDAO adminDAO;
    
    // Email validation pattern
    private static final String EMAIL_PATTERN = 
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
    
    private static final Pattern pattern = Pattern.compile(EMAIL_PATTERN);
    
    public AdminAddAdminController() {
        super();
        this.adminDAO = new AdminDAO();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Check session
        HttpSession session = request.getSession(false);
        
        // OPTION 1: For testing without login - UNCOMMENT if needed
        if (session == null || session.getAttribute("adminId") == null) {
            session = request.getSession(true);
            session.setAttribute("adminId", "TEMP_ADMIN_123");
            session.setAttribute("adminName", "Test Admin");
        }
        
        // OPTION 2: For production - UNCOMMENT when ready for production
        /*
        if (session == null || session.getAttribute("adminId") == null) {
            response.sendRedirect("/ELMS_3.0/Admin/AdminLogin.jsp");
            return;
        }
        */
        
        // Forward to JSP
        request.getRequestDispatcher("/Admin/AdminAddAdmin.jsp").forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Check session
        HttpSession session = request.getSession(false);
        
        if (session == null || session.getAttribute("adminId") == null) {
            response.sendRedirect("/ELMS_3.0/Admin/AdminLogin.jsp");
            return;
        }
        
        try {
            // Get form parameters - INCLUDING PHONE!
            String adminName = request.getParameter("adminName");
            String adminEmail = request.getParameter("adminEmail");
            String adminPassword = request.getParameter("adminPassword");
            String confirmPassword = request.getParameter("confirmPassword");
            String adminPhone = request.getParameter("adminphone"); // THIS WAS MISSING!
            
            // DEBUG: Print all parameters
            System.out.println("=== CONTROLLER DEBUG ===");
            System.out.println("adminName: " + adminName);
            System.out.println("adminEmail: " + adminEmail);
            System.out.println("adminPhone: " + adminPhone);
            System.out.println("adminPassword: " + (adminPassword != null ? "[PROVIDED]" : "[NULL]"));
            
            // Validate input
            if (adminName == null || adminName.trim().isEmpty() ||
                adminEmail == null || adminEmail.trim().isEmpty() ||
                adminPassword == null || adminPassword.trim().isEmpty() ||
                confirmPassword == null || confirmPassword.trim().isEmpty() ||
                adminPhone == null || adminPhone.trim().isEmpty()) { // Add phone validation
                
                request.setAttribute("errorMessage", "All fields are required.");
                request.getRequestDispatcher("/Admin/AdminAddAdmin.jsp").forward(request, response);
                return;
            }
            
            // Trim inputs
            adminName = adminName.trim();
            adminEmail = adminEmail.trim().toLowerCase();
            adminPhone = adminPhone.trim(); // Trim phone
            
            // Validate email format
            if (!isValidEmail(adminEmail)) {
                request.setAttribute("errorMessage", "Please enter a valid email address.");
                request.getRequestDispatcher("/Admin/AdminAddAdmin.jsp").forward(request, response);
                return;
            }
            
            // Validate phone format (basic validation)
            if (!isValidPhone(adminPhone)) {
                request.setAttribute("errorMessage", "Please enter a valid phone number.");
                request.getRequestDispatcher("/Admin/AdminAddAdmin.jsp").forward(request, response);
                return;
            }
            
            // Validate password match
            if (!adminPassword.equals(confirmPassword)) {
                request.setAttribute("errorMessage", "Passwords do not match.");
                request.getRequestDispatcher("/Admin/AdminAddAdmin.jsp").forward(request, response);
                return;
            }
            
            // Validate password strength
            if (!isValidPassword(adminPassword)) {
                request.setAttribute("errorMessage", 
                    "Password must be at least 8 characters long and contain at least one uppercase letter, one lowercase letter, and one digit.");
                request.getRequestDispatcher("/Admin/AdminAddAdmin.jsp").forward(request, response);
                return;
            }
            
            // Check if email already exists
            if (adminDAO.isEmailExists(adminEmail)) {
                request.setAttribute("errorMessage", "An admin with this email already exists.");
                request.getRequestDispatcher("/Admin/AdminAddAdmin.jsp").forward(request, response);
                return;
            }
            
            // Create new admin object
            Admin newAdmin = new Admin();
            newAdmin.setAdminName(adminName);
            newAdmin.setAdminEmail(adminEmail);
            newAdmin.setAdminNoPhone(adminPhone); // SET THE PHONE NUMBER!
            
            // Hash the password using SHA-256
            String hashedPassword = hashPassword(adminPassword);
            newAdmin.setAdminPassword(hashedPassword);
            
            System.out.println("Admin object before DAO:");
            System.out.println("Name: " + newAdmin.getAdminName());
            System.out.println("Email: " + newAdmin.getAdminEmail());
            System.out.println("Phone: " + newAdmin.getAdminNoPhone());
            
            // Create admin and get generated ID
            String generatedAdminId = adminDAO.createAdminWithGeneratedId(newAdmin);
            
            if (generatedAdminId != null) {
                // Success - forward to success page
                request.setAttribute("adminName", adminName);
                request.setAttribute("adminId", generatedAdminId);
                request.setAttribute("adminEmail", adminEmail);
                request.setAttribute("adminPhone", adminPhone); // Include phone in success page
                request.getRequestDispatcher("/Admin/AdminRegistrationSuccess.jsp").forward(request, response);
            } else {
                request.setAttribute("errorMessage", "Failed to create admin account. Please try again.");
                request.getRequestDispatcher("/Admin/AdminAddAdmin.jsp").forward(request, response);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            
            String userFriendlyMessage = "Database error: " + e.getMessage();
            if (e.getMessage().contains("admin_id_seq")) {
                userFriendlyMessage = "Database sequence error. Please contact administrator.";
            }
            
            request.setAttribute("errorMessage", userFriendlyMessage);
            request.getRequestDispatcher("/Admin/AdminAddAdmin.jsp").forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "An unexpected error occurred. Please try again.");
            request.getRequestDispatcher("/Admin/AdminAddAdmin.jsp").forward(request, response);
        }
    }
    
    /**
     * Validate email format
     */
    private boolean isValidEmail(String email) {
        return pattern.matcher(email).matches();
    }
    
    /**
     * Validate phone format
     */
    private boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        
        // Remove spaces and check if it's a valid Malaysian phone format
        phone = phone.trim().replaceAll("\\s+", "");
        
        // Valid formats: +60123456789, 60123456789, 0123456789, 123456789
        return phone.matches("^(\\+60|60|0)?[0-9]{9,10}$");
    }
    
    /**
     * Validate password strength
     */
    private boolean isValidPassword(String password) {
        if (password.length() < 8) {
            return false;
        }
        
        boolean hasUpper = false;
        boolean hasLower = false;
        boolean hasDigit = false;
        
        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) {
                hasUpper = true;
            } else if (Character.isLowerCase(c)) {
                hasLower = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            }
        }
        
        return hasUpper && hasLower && hasDigit;
    }
    
    /**
     * Hash password using SHA-256
     */
    public static String hashPassword(String password) {
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
}
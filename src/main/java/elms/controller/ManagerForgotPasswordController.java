package elms.controller;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import elms.DAO.ManagerDAO;
import elms.model.Manager;
import elms.service.ManagerForgotPasswordEmailService;

/**
 * Controller for handling manager forgot password functionality with OTP verification
 * Includes email sending, OTP generation, validation, and password reset for managers
 */
@WebServlet("/ManagerForgotPasswordController")
public class ManagerForgotPasswordController extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    // OTP Configuration
    private static final int OTP_LENGTH = 6;
    private static final long OTP_EXPIRY_TIME = 5 * 60 * 1000; // 5 minutes in milliseconds
    private static final int MAX_OTP_ATTEMPTS = 3;
    
    // In-memory storage for OTP data (in production, use database or Redis)
    private static final ConcurrentHashMap<String, OTPData> otpStorage = new ConcurrentHashMap<>();
    
    // OTP Data structure
    private static class OTPData {
        String otp;
        long expiryTime;
        int attempts;
        String email;
        
        public OTPData(String otp, String email) {
            this.otp = otp;
            this.email = email;
            this.expiryTime = System.currentTimeMillis() + OTP_EXPIRY_TIME;
            this.attempts = 0;
        }
        
        public boolean isExpired() {
            return System.currentTimeMillis() > expiryTime;
        }
        
        public boolean isMaxAttemptsReached() {
            return attempts >= MAX_OTP_ATTEMPTS;
        }
        
        public void incrementAttempts() {
            attempts++;
        }
    }
    
    public ManagerForgotPasswordController() {
        super();
    }
    
    /**
     * GET - Show manager forgot password page
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        System.out.println("=== MANAGER FORGOT PASSWORD CONTROLLER: GET ===");
        
        String action = request.getParameter("action");
        
        if ("resendOTP".equals(action)) {
            handleResendOTP(request, response);
        } else {
            // Show manager forgot password page
            request.getRequestDispatcher("Manager/ManagerForgotPassword.jsp").forward(request, response);
        }
    }
    
    /**
     * POST - Handle manager forgot password actions
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        System.out.println("=== MANAGER FORGOT PASSWORD CONTROLLER: POST ===");
        
        String action = request.getParameter("action");
        
        if ("sendOTP".equals(action)) {
            handleSendOTP(request, response);
        } else if ("verifyOTP".equals(action)) {
            handleVerifyOTP(request, response);
        } else if ("resetPassword".equals(action)) {
            handleResetPassword(request, response);
        } else {
            // Default action - show manager forgot password page
            request.getRequestDispatcher("Manager/ManagerForgotPassword.jsp").forward(request, response);
        }
    }
    
    /**
     * Handle sending OTP to manager's email
     */
    private void handleSendOTP(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        System.out.println("=== HANDLING MANAGER SEND OTP ===");
        
        String email = request.getParameter("email");
        
        if (email == null || email.trim().isEmpty()) {
            request.setAttribute("errorMessage", "Please enter your manager email address");
            request.getRequestDispatcher("Manager/ManagerForgotPassword.jsp").forward(request, response);
            return;
        }
        
        email = email.trim().toLowerCase();
        System.out.println("Manager email for OTP: " + email);
        
        try {
            // Create ManagerDAO instance
            ManagerDAO managerDAO = new ManagerDAO();
            
            // Check if manager exists with this email
            Manager manager = managerDAO.getManagerByEmail(email);
            
            if (manager == null) {
                System.out.println("No manager found with email: " + email);
                request.setAttribute("errorMessage", "No manager account found with this email address");
                request.setAttribute("email", email);
                request.getRequestDispatcher("Manager/ManagerForgotPassword.jsp").forward(request, response);
                return;
            }
            
            System.out.println("Manager found: " + manager.getManagername());
            
            // Generate OTP
            String otp = generateOTP();
            System.out.println("Generated manager OTP: " + otp);
            
            // Store OTP data
            otpStorage.put(email, new OTPData(otp, email));
            
            // Send OTP via email
            boolean emailSent = ManagerForgotPasswordEmailService.sendOTPEmail(
                manager.getManageremail(), 
                manager.getManagername(), 
                otp
            );
            
            if (emailSent) {
                System.out.println("✅ Manager OTP email sent successfully");
                
                // Redirect to OTP verification step
                response.sendRedirect(request.getContextPath() + 
                    "/Manager/ManagerForgotPassword.jsp?step=2&email=" + 
                    java.net.URLEncoder.encode(email, "UTF-8") + 
                    "&message=otp_sent");
                
            } else {
                System.out.println("❌ Failed to send manager OTP email");
                request.setAttribute("errorMessage", "Failed to send OTP email. Please try again.");
                request.setAttribute("email", email);
                request.getRequestDispatcher("Manager/ManagerForgotPassword.jsp").forward(request, response);
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error in manager handleSendOTP: " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("errorMessage", "System error occurred. Please try again later.");
            request.setAttribute("email", email);
            request.getRequestDispatcher("Manager/ManagerForgotPassword.jsp").forward(request, response);
        }
    }
    
    /**
     * Handle manager OTP verification
     */
    private void handleVerifyOTP(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        System.out.println("=== HANDLING MANAGER VERIFY OTP ===");
        
        String email = request.getParameter("email");
        String inputOTP = request.getParameter("otp");
        
        if (email == null || email.trim().isEmpty() || inputOTP == null || inputOTP.trim().isEmpty()) {
            request.setAttribute("errorMessage", "Please enter the OTP");
            response.sendRedirect(request.getContextPath() + 
                "/Manager/ManagerForgotPassword.jsp?step=2&email=" + 
                java.net.URLEncoder.encode(email != null ? email : "", "UTF-8"));
            return;
        }
        
        email = email.trim().toLowerCase();
        inputOTP = inputOTP.trim();
        
        System.out.println("Verifying manager OTP for email: " + email);
        System.out.println("Input OTP: " + inputOTP);
        
        try {
            // Get stored OTP data
            OTPData otpData = otpStorage.get(email);
            
            if (otpData == null) {
                System.out.println("❌ No manager OTP data found for email: " + email);
                request.setAttribute("errorMessage", "OTP session expired. Please request a new OTP.");
                response.sendRedirect(request.getContextPath() + "/Manager/ManagerForgotPassword.jsp");
                return;
            }
            
            // Check if OTP is expired
            if (otpData.isExpired()) {
                System.out.println("❌ Manager OTP expired for email: " + email);
                otpStorage.remove(email); // Clean up expired OTP
                request.setAttribute("errorMessage", "OTP has expired. Please request a new OTP.");
                response.sendRedirect(request.getContextPath() + "/Manager/ManagerForgotPassword.jsp");
                return;
            }
            
            // Check if max attempts reached
            if (otpData.isMaxAttemptsReached()) {
                System.out.println("❌ Max attempts reached for manager email: " + email);
                otpStorage.remove(email); // Clean up
                request.setAttribute("errorMessage", "Maximum OTP attempts reached. Please request a new OTP.");
                response.sendRedirect(request.getContextPath() + "/Manager/ManagerForgotPassword.jsp");
                return;
            }
            
            // Verify OTP
            if (inputOTP.equals(otpData.otp)) {
                System.out.println("✅ Manager OTP verified successfully for email: " + email);
                
                // Store verification status in session
                HttpSession session = request.getSession();
                session.setAttribute("managerOtpVerified", true);
                session.setAttribute("managerVerifiedEmail", email);
                session.setAttribute("managerVerifiedOTP", inputOTP);
                session.setMaxInactiveInterval(10 * 60); // 10 minutes for password reset
                
                // Redirect to password reset step
                response.sendRedirect(request.getContextPath() + 
                    "/Manager/ManagerForgotPassword.jsp?step=3&email=" + 
                    java.net.URLEncoder.encode(email, "UTF-8"));
                
            } else {
                System.out.println("❌ Invalid manager OTP for email: " + email);
                otpData.incrementAttempts();
                
                int attemptsLeft = MAX_OTP_ATTEMPTS - otpData.attempts;
                request.setAttribute("errorMessage", 
                    "Invalid OTP. " + attemptsLeft + " attempt(s) remaining.");
                
                response.sendRedirect(request.getContextPath() + 
                    "/Manager/ManagerForgotPassword.jsp?step=2&email=" + 
                    java.net.URLEncoder.encode(email, "UTF-8"));
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error in manager handleVerifyOTP: " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("errorMessage", "System error occurred. Please try again.");
            response.sendRedirect(request.getContextPath() + 
                "/Manager/ManagerForgotPassword.jsp?step=2&email=" + 
                java.net.URLEncoder.encode(email, "UTF-8"));
        }
    }
    
    /**
     * Handle manager password reset
     */
    private void handleResetPassword(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        System.out.println("=== HANDLING MANAGER RESET PASSWORD ===");
        
        String email = request.getParameter("email");
        String newPassword = request.getParameter("newPassword");
        String confirmPassword = request.getParameter("confirmPassword");
        
        if (email == null || email.trim().isEmpty() || 
            newPassword == null || newPassword.trim().isEmpty() ||
            confirmPassword == null || confirmPassword.trim().isEmpty()) {
            
            request.setAttribute("errorMessage", "Please fill in all fields");
            response.sendRedirect(request.getContextPath() + 
                "/Manager/ManagerForgotPassword.jsp?step=3&email=" + 
                java.net.URLEncoder.encode(email != null ? email : "", "UTF-8"));
            return;
        }
        
        email = email.trim().toLowerCase();
        
        // Check if passwords match
        if (!newPassword.equals(confirmPassword)) {
            request.setAttribute("errorMessage", "Passwords do not match");
            response.sendRedirect(request.getContextPath() + 
                "/Manager/ManagerForgotPassword.jsp?step=3&email=" + 
                java.net.URLEncoder.encode(email, "UTF-8"));
            return;
        }
        
        // Validate password strength (minimum 6 characters for manager)
        if (newPassword.length() < 6) {
            request.setAttribute("errorMessage", "Manager password must be at least 6 characters long");
            response.sendRedirect(request.getContextPath() + 
                "/Manager/ManagerForgotPassword.jsp?step=3&email=" + 
                java.net.URLEncoder.encode(email, "UTF-8"));
            return;
        }
        
        try {
            // Verify session - ensure OTP was verified
            HttpSession session = request.getSession();
            Boolean otpVerified = (Boolean) session.getAttribute("managerOtpVerified");
            String verifiedEmail = (String) session.getAttribute("managerVerifiedEmail");
            
            if (otpVerified == null || !otpVerified || !email.equals(verifiedEmail)) {
                System.out.println("❌ Invalid manager session or OTP not verified");
                request.setAttribute("errorMessage", "Session expired or OTP not verified. Please start over.");
                response.sendRedirect(request.getContextPath() + "/Manager/ManagerForgotPassword.jsp");
                return;
            }
            
            // Get manager to update password
            ManagerDAO managerDAO = new ManagerDAO();
            Manager manager = managerDAO.getManagerByEmail(email);
            
            if (manager == null) {
                System.out.println("❌ Manager not found for email: " + email);
                request.setAttribute("errorMessage", "Manager account not found");
                response.sendRedirect(request.getContextPath() + "/Manager/ManagerForgotPassword.jsp");
                return;
            }
            
            // Hash the new password (assuming managers use plain text passwords like in the DAO)
            // Note: In production, you should hash manager passwords too
            String hashedPassword = hashPassword(newPassword);
            
            // Update the manager's password
            manager.setManagerpassword(hashedPassword);
            boolean passwordUpdated = managerDAO.updateManager(manager);
            
            if (passwordUpdated) {
                System.out.println("✅ Manager password updated successfully for: " + manager.getManagerid());
                
                // Clean up OTP data and session
                otpStorage.remove(email);
                session.invalidate();
                
                // Send confirmation email
                ManagerForgotPasswordEmailService.sendPasswordResetConfirmationEmail(
                    manager.getManageremail(), 
                    manager.getManagername()
                );
                
                // Redirect to manager login with success message
                response.sendRedirect(request.getContextPath() + 
                    "/Manager/ManagerLogin.jsp?message=passwordReset");
                
            } else {
                System.out.println("❌ Failed to update manager password");
                request.setAttribute("errorMessage", "Failed to update password. Please try again.");
                response.sendRedirect(request.getContextPath() + 
                    "/Manager/ManagerForgotPassword.jsp?step=3&email=" + 
                    java.net.URLEncoder.encode(email, "UTF-8"));
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error in manager handleResetPassword: " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("errorMessage", "System error occurred. Please try again.");
            response.sendRedirect(request.getContextPath() + 
                "/Manager/ManagerForgotPassword.jsp?step=3&email=" + 
                java.net.URLEncoder.encode(email, "UTF-8"));
        }
    }
    
    /**
     * Handle manager resend OTP request
     */
    private void handleResendOTP(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        System.out.println("=== HANDLING MANAGER RESEND OTP ===");
        
        String email = request.getParameter("email");
        
        if (email == null || email.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/Manager/ManagerForgotPassword.jsp");
            return;
        }
        
        email = email.trim().toLowerCase();
        
        try {
            // Remove existing OTP data
            otpStorage.remove(email);
            
            // Get manager details
            ManagerDAO managerDAO = new ManagerDAO();
            Manager manager = managerDAO.getManagerByEmail(email);
            
            if (manager == null) {
                request.setAttribute("errorMessage", "Manager account not found");
                response.sendRedirect(request.getContextPath() + "/Manager/ManagerForgotPassword.jsp");
                return;
            }
            
            // Generate new OTP
            String newOTP = generateOTP();
            System.out.println("Generated new manager OTP: " + newOTP);
            
            // Store new OTP data
            otpStorage.put(email, new OTPData(newOTP, email));
            
            // Send new OTP via email
            boolean emailSent = ManagerForgotPasswordEmailService.sendOTPEmail(
                manager.getManageremail(), 
                manager.getManagername(), 
                newOTP
            );
            
            if (emailSent) {
                System.out.println("✅ New manager OTP email sent successfully");
                request.setAttribute("successMessage", "New OTP has been sent to your manager email");
            } else {
                System.out.println("❌ Failed to send new manager OTP email");
                request.setAttribute("errorMessage", "Failed to send new OTP. Please try again.");
            }
            
            // Redirect back to OTP verification step
            response.sendRedirect(request.getContextPath() + 
                "/Manager/ManagerForgotPassword.jsp?step=2&email=" + 
                java.net.URLEncoder.encode(email, "UTF-8") + 
                "&message=otp_resent");
            
        } catch (Exception e) {
            System.err.println("❌ Error in manager handleResendOTP: " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("errorMessage", "System error occurred. Please try again.");
            response.sendRedirect(request.getContextPath() + 
                "/Manager/ManagerForgotPassword.jsp?step=2&email=" + 
                java.net.URLEncoder.encode(email, "UTF-8"));
        }
    }
    
    /**
     * Hash password using SHA-256 (consistent with other systems)
     */
    private String hashPassword(String password) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }
    
    /**
     * Generate a random 6-digit OTP
     */
    private String generateOTP() {
        SecureRandom random = new SecureRandom();
        StringBuilder otp = new StringBuilder();
        
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(random.nextInt(10));
        }
        
        return otp.toString();
    }
    
    /**
     * Clean up expired OTP data (should be called periodically)
     */
    public static void cleanupExpiredOTPs() {
        long currentTime = System.currentTimeMillis();
        
        otpStorage.entrySet().removeIf(entry -> {
            boolean expired = entry.getValue().expiryTime < currentTime;
            if (expired) {
                System.out.println("Cleaning up expired manager OTP for: " + entry.getKey());
            }
            return expired;
        });
    }
}
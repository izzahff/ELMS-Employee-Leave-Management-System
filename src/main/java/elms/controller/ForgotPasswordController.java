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

import elms.DAO.EmployeeDAO;
import elms.model.Employee;
import elms.service.ForgotPasswordEmailService;

/**
 * Controller for handling forgot password functionality with OTP verification
 * Includes email sending, OTP generation, validation, and password reset
 */
@WebServlet("/ForgotPasswordController")
public class ForgotPasswordController extends HttpServlet {
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
    
    public ForgotPasswordController() {
        super();
    }
    
    /**
     * GET - Show forgot password page
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        System.out.println("=== FORGOT PASSWORD CONTROLLER: GET ===");
        
        String action = request.getParameter("action");
        
        if ("resendOTP".equals(action)) {
            handleResendOTP(request, response);
        } else {
            // Show forgot password page
            request.getRequestDispatcher("Employee/EmployeeForgotPassword.jsp").forward(request, response);
        }
    }
    
    /**
     * POST - Handle forgot password actions
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        System.out.println("=== FORGOT PASSWORD CONTROLLER: POST ===");
        
        String action = request.getParameter("action");
        
        if ("sendOTP".equals(action)) {
            handleSendOTP(request, response);
        } else if ("verifyOTP".equals(action)) {
            handleVerifyOTP(request, response);
        } else if ("resetPassword".equals(action)) {
            handleResetPassword(request, response);
        } else {
            // Default action - show forgot password page
            request.getRequestDispatcher("Employee/EmployeeForgotPassword.jsp").forward(request, response);
        }
    }
    
    /**
     * Handle sending OTP to user's email
     */
    private void handleSendOTP(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        System.out.println("=== HANDLING SEND OTP ===");
        
        String email = request.getParameter("email");
        
        if (email == null || email.trim().isEmpty()) {
            request.setAttribute("errorMessage", "Please enter your email address");
            request.getRequestDispatcher("Employee/EmployeeForgotPassword.jsp").forward(request, response);
            return;
        }
        
        email = email.trim().toLowerCase();
        System.out.println("Email for OTP: " + email);
        
        try {
            // Check if employee exists with this email
            Employee employee = EmployeeDAO.getEmployeeByEmail(email);
            
            if (employee == null) {
                System.out.println("No employee found with email: " + email);
                request.setAttribute("errorMessage", "No account found with this email address");
                request.setAttribute("email", email);
                request.getRequestDispatcher("Employee/EmployeeForgotPassword.jsp").forward(request, response);
                return;
            }
            
            System.out.println("Employee found: " + employee.getEmployeeName());
            
            // Generate OTP
            String otp = generateOTP();
            System.out.println("Generated OTP: " + otp);
            
            // Store OTP data
            otpStorage.put(email, new OTPData(otp, email));
            
            // Send OTP via email
            boolean emailSent = ForgotPasswordEmailService.sendOTPEmail(
                employee.getEmployeeEmail(), 
                employee.getEmployeeName(), 
                otp
            );
            
            if (emailSent) {
                System.out.println("✅ OTP email sent successfully");
                
                // Redirect to OTP verification step
                response.sendRedirect(request.getContextPath() + 
                    "/Employee/EmployeeForgotPassword.jsp?step=2&email=" + 
                    java.net.URLEncoder.encode(email, "UTF-8"));
                
            } else {
                System.out.println("❌ Failed to send OTP email");
                request.setAttribute("errorMessage", "Failed to send OTP email. Please try again.");
                request.setAttribute("email", email);
                request.getRequestDispatcher("Employee/EmployeeForgotPassword.jsp").forward(request, response);
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error in handleSendOTP: " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("errorMessage", "System error occurred. Please try again later.");
            request.setAttribute("email", email);
            request.getRequestDispatcher("Employee/EmployeeForgotPassword.jsp").forward(request, response);
        }
    }
    
    /**
     * Handle OTP verification
     */
    private void handleVerifyOTP(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        System.out.println("=== HANDLING VERIFY OTP ===");
        
        String email = request.getParameter("email");
        String inputOTP = request.getParameter("otp");
        
        if (email == null || email.trim().isEmpty() || inputOTP == null || inputOTP.trim().isEmpty()) {
            request.setAttribute("errorMessage", "Please enter the OTP");
            response.sendRedirect(request.getContextPath() + 
                "/Employee/EmployeeForgotPassword.jsp?step=2&email=" + 
                java.net.URLEncoder.encode(email != null ? email : "", "UTF-8"));
            return;
        }
        
        email = email.trim().toLowerCase();
        inputOTP = inputOTP.trim();
        
        System.out.println("Verifying OTP for email: " + email);
        System.out.println("Input OTP: " + inputOTP);
        
        try {
            // Get stored OTP data
            OTPData otpData = otpStorage.get(email);
            
            if (otpData == null) {
                System.out.println("❌ No OTP data found for email: " + email);
                request.setAttribute("errorMessage", "OTP session expired. Please request a new OTP.");
                response.sendRedirect(request.getContextPath() + "/Employee/EmployeeForgotPassword.jsp");
                return;
            }
            
            // Check if OTP is expired
            if (otpData.isExpired()) {
                System.out.println("❌ OTP expired for email: " + email);
                otpStorage.remove(email); // Clean up expired OTP
                request.setAttribute("errorMessage", "OTP has expired. Please request a new OTP.");
                response.sendRedirect(request.getContextPath() + "/Employee/EmployeeForgotPassword.jsp");
                return;
            }
            
            // Check if max attempts reached
            if (otpData.isMaxAttemptsReached()) {
                System.out.println("❌ Max attempts reached for email: " + email);
                otpStorage.remove(email); // Clean up
                request.setAttribute("errorMessage", "Maximum OTP attempts reached. Please request a new OTP.");
                response.sendRedirect(request.getContextPath() + "/Employee/EmployeeForgotPassword.jsp");
                return;
            }
            
            // Verify OTP
            if (inputOTP.equals(otpData.otp)) {
                System.out.println("✅ OTP verified successfully for email: " + email);
                
                // Store verification status in session
                HttpSession session = request.getSession();
                session.setAttribute("otpVerified", true);
                session.setAttribute("verifiedEmail", email);
                session.setAttribute("verifiedOTP", inputOTP);
                session.setMaxInactiveInterval(10 * 60); // 10 minutes for password reset
                
                // Redirect to password reset step
                response.sendRedirect(request.getContextPath() + 
                    "/Employee/EmployeeForgotPassword.jsp?step=3&email=" + 
                    java.net.URLEncoder.encode(email, "UTF-8"));
                
            } else {
                System.out.println("❌ Invalid OTP for email: " + email);
                otpData.incrementAttempts();
                
                int attemptsLeft = MAX_OTP_ATTEMPTS - otpData.attempts;
                request.setAttribute("errorMessage", 
                    "Invalid OTP. " + attemptsLeft + " attempt(s) remaining.");
                
                response.sendRedirect(request.getContextPath() + 
                    "/Employee/EmployeeForgotPassword.jsp?step=2&email=" + 
                    java.net.URLEncoder.encode(email, "UTF-8"));
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error in handleVerifyOTP: " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("errorMessage", "System error occurred. Please try again.");
            response.sendRedirect(request.getContextPath() + 
                "/Employee/EmployeeForgotPassword.jsp?step=2&email=" + 
                java.net.URLEncoder.encode(email, "UTF-8"));
        }
    }
    
    /**
     * Handle password reset
     */
    private void handleResetPassword(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        System.out.println("=== HANDLING RESET PASSWORD ===");
        
        String email = request.getParameter("email");
        String newPassword = request.getParameter("newPassword");
        String confirmPassword = request.getParameter("confirmPassword");
        
        if (email == null || email.trim().isEmpty() || 
            newPassword == null || newPassword.trim().isEmpty() ||
            confirmPassword == null || confirmPassword.trim().isEmpty()) {
            
            request.setAttribute("errorMessage", "Please fill in all fields");
            response.sendRedirect(request.getContextPath() + 
                "/Employee/EmployeeForgotPassword.jsp?step=3&email=" + 
                java.net.URLEncoder.encode(email != null ? email : "", "UTF-8"));
            return;
        }
        
        email = email.trim().toLowerCase();
        
        // Check if passwords match
        if (!newPassword.equals(confirmPassword)) {
            request.setAttribute("errorMessage", "Passwords do not match");
            response.sendRedirect(request.getContextPath() + 
                "/Employee/ForgotPassword.jsp?step=3&email=" + 
                java.net.URLEncoder.encode(email, "UTF-8"));
            return;
        }
        
        // Validate password strength
        if (newPassword.length() < 6) {
            request.setAttribute("errorMessage", "Password must be at least 6 characters long");
            response.sendRedirect(request.getContextPath() + 
                "/Employee/EmployeeForgotPassword.jsp?step=3&email=" + 
                java.net.URLEncoder.encode(email, "UTF-8"));
            return;
        }
        
        try {
            // Verify session - ensure OTP was verified
            HttpSession session = request.getSession();
            Boolean otpVerified = (Boolean) session.getAttribute("otpVerified");
            String verifiedEmail = (String) session.getAttribute("verifiedEmail");
            
            if (otpVerified == null || !otpVerified || !email.equals(verifiedEmail)) {
                System.out.println("❌ Invalid session or OTP not verified");
                request.setAttribute("errorMessage", "Session expired or OTP not verified. Please start over.");
                response.sendRedirect(request.getContextPath() + "/Employee/EmployeeForgotPassword.jsp");
                return;
            }
            
            // Get employee to update password
            Employee employee = EmployeeDAO.getEmployeeByEmail(email);
            
            if (employee == null) {
                System.out.println("❌ Employee not found for email: " + email);
                request.setAttribute("errorMessage", "Employee account not found");
                response.sendRedirect(request.getContextPath() + "/Employee/EmployeeForgotPassword.jsp");
                return;
            }
            
            // Hash the new password
            String hashedPassword = EmployeeDAO.hashPassword(newPassword);
            System.out.println("New password hashed for employee: " + employee.getEmployeeId());
            
            // Update password in database
            String result = EmployeeDAO.updateEmployeePassword(employee.getEmployeeId(), hashedPassword);
            
            if (result.startsWith("success")) {
                System.out.println("✅ Password updated successfully for: " + employee.getEmployeeId());
                
                // Clean up OTP data and session
                otpStorage.remove(email);
                session.invalidate();
                
                // Send confirmation email
                ForgotPasswordEmailService.sendPasswordResetConfirmationEmail(
                    employee.getEmployeeEmail(), 
                    employee.getEmployeeName()
                );
                
                // Redirect to login with success message
                response.sendRedirect(request.getContextPath() + 
                    "/Employee/EmployeeLogin.jsp?message=passwordReset");
                
            } else {
                System.out.println("❌ Failed to update password: " + result);
                request.setAttribute("errorMessage", "Failed to update password. Please try again.");
                response.sendRedirect(request.getContextPath() + 
                    "/Employee/EmployeeForgotPassword.jsp?step=3&email=" + 
                    java.net.URLEncoder.encode(email, "UTF-8"));
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error in handleResetPassword: " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("errorMessage", "System error occurred. Please try again.");
            response.sendRedirect(request.getContextPath() + 
                "/Employee/EmployeeForgotPassword.jsp?step=3&email=" + 
                java.net.URLEncoder.encode(email, "UTF-8"));
        }
    }
    
    /**
     * Handle resend OTP request
     */
    private void handleResendOTP(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        System.out.println("=== HANDLING RESEND OTP ===");
        
        String email = request.getParameter("email");
        
        if (email == null || email.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/Employee/EmployeeForgotPassword.jsp");
            return;
        }
        
        email = email.trim().toLowerCase();
        
        try {
            // Remove existing OTP data
            otpStorage.remove(email);
            
            // Get employee details
            Employee employee = EmployeeDAO.getEmployeeByEmail(email);
            
            if (employee == null) {
                request.setAttribute("errorMessage", "Employee account not found");
                response.sendRedirect(request.getContextPath() + "/Employee/EmployeeForgotPassword.jsp");
                return;
            }
            
            // Generate new OTP
            String newOTP = generateOTP();
            System.out.println("Generated new OTP: " + newOTP);
            
            // Store new OTP data
            otpStorage.put(email, new OTPData(newOTP, email));
            
            // Send new OTP via email
            boolean emailSent = ForgotPasswordEmailService.sendOTPEmail(
                employee.getEmployeeEmail(), 
                employee.getEmployeeName(), 
                newOTP
            );
            
            if (emailSent) {
                System.out.println("✅ New OTP email sent successfully");
                request.setAttribute("successMessage", "New OTP has been sent to your email");
            } else {
                System.out.println("❌ Failed to send new OTP email");
                request.setAttribute("errorMessage", "Failed to send new OTP. Please try again.");
            }
            
            // Redirect back to OTP verification step
            response.sendRedirect(request.getContextPath() + 
                "/Employee/EmployeeForgotPassword.jsp?step=2&email=" + 
                java.net.URLEncoder.encode(email, "UTF-8"));
            
        } catch (Exception e) {
            System.err.println("❌ Error in handleResendOTP: " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("errorMessage", "System error occurred. Please try again.");
            response.sendRedirect(request.getContextPath() + 
                "/Employee/EmployeeForgotPassword.jsp?step=2&email=" + 
                java.net.URLEncoder.encode(email, "UTF-8"));
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
                System.out.println("Cleaning up expired OTP for: " + entry.getKey());
            }
            return expired;
        });
    }
}
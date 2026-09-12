package elms.controller;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import elms.DAO.AdminDAO;
import elms.model.Admin;

@WebServlet("/AdminLoginController")
public class AdminLoginController extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private AdminDAO adminDAO;

    public AdminLoginController() {
        super();
        this.adminDAO = new AdminDAO();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        
        if (action == null) {
            action = "login";
        }
        
        switch (action) {
            case "login":
                showLoginForm(request, response);
                break;
            case "logout":
                logout(request, response);
                break;
            default:
                showLoginForm(request, response);
                break;
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        
        if (action == null) {
            action = "authenticate";
        }
        
        switch (action) {
            case "authenticate":
                authenticateAdmin(request, response);
                break;
            default:
                authenticateAdmin(request, response);
                break;
        }
    }

    private void showLoginForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/ELMS_3.0/Admin/AdminLogin.jsp").forward(request, response);
    }

    private void authenticateAdmin(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        String adminIdOrEmail = request.getParameter("adminId");
        String adminPassword = request.getParameter("adminPassword");
        String errorMessage = "";

        // Debug output
        System.out.println("Authentication attempt for adminId/Email: " + adminIdOrEmail);
        System.out.println("Password provided: " + (adminPassword != null && !adminPassword.isEmpty() ? "Yes" : "No"));

        try {
            if (adminIdOrEmail != null && adminPassword != null && 
                !adminIdOrEmail.trim().isEmpty() && !adminPassword.trim().isEmpty()) {
                
                System.out.println("Creating AdminDAO...");
                AdminDAO adminDAO = new AdminDAO();
                
                Admin admin = null;
                
                // Check if input contains @ symbol to determine if it's an email
                if (adminIdOrEmail.trim().contains("@")) {
                    System.out.println("Login attempt using email: " + adminIdOrEmail.trim());
                    // Try to authenticate using email
                    admin = adminDAO.authenticateAdminByEmail(adminIdOrEmail.trim(), adminPassword);
                } else {
                    System.out.println("Login attempt using admin ID: " + adminIdOrEmail.trim());
                    // Authenticate using admin ID (existing method)
                    admin = adminDAO.authenticateAdmin(adminIdOrEmail.trim(), adminPassword);
                }
                
                if (admin != null) {
                    // Authentication successful
                    System.out.println("Authentication successful for: " + admin.getAdminName());
                    System.out.println("Admin details retrieved:");
                    System.out.println("- ID: " + admin.getAdminId());
                    System.out.println("- Name: " + admin.getAdminName());
                    System.out.println("- Email: " + admin.getAdminEmail());
                    System.out.println("- Phone: " + admin.getAdminNoPhone());
                    System.out.println("- Profile Picture: " + admin.getProfile_picture_path());
                    
                    HttpSession session = request.getSession();
                    
                    // Clear any existing session data to prevent conflicts
                    session.invalidate();
                    session = request.getSession(true);
                    
                    // Store admin details in session including profile picture
                    session.setAttribute("adminId", admin.getAdminId());
                    session.setAttribute("adminName", admin.getAdminName());
                    session.setAttribute("adminEmail", admin.getAdminEmail());
                    session.setAttribute("adminPhone", admin.getAdminNoPhone());
                    session.setAttribute("adminProfilePicturePath", admin.getProfile_picture_path()); // Added profile picture
                    session.setAttribute("userType", "admin");
                    session.setAttribute("loggedInAdmin", admin); // Store complete admin object
                    
                    // Set session timeout (30 minutes)
                    session.setMaxInactiveInterval(30 * 60);
                    
                    System.out.println("Session created successfully:");
                    System.out.println("- Session ID: " + session.getId());
                    System.out.println("- Admin stored in session: " + session.getAttribute("adminName"));
                    System.out.println("- Profile picture path: " + session.getAttribute("adminProfilePicturePath"));
                    
                    // Redirect to admin dashboard controller (updated)
                    response.sendRedirect(request.getContextPath() + "/admin-dashboard");
                    return;
                } else {
                    System.out.println("Authentication failed - invalid credentials for: " + adminIdOrEmail);
                    errorMessage = "Invalid Admin ID/Email or Password";
                }
            } else {
                System.out.println("Authentication failed - empty fields");
                errorMessage = "Please enter both Admin ID/Email and Password";
            }
        } catch (Exception e) {
            System.err.println("Exception during authentication: " + e.getMessage());
            errorMessage = "Login failed. Please try again.";
            e.printStackTrace();
        }
        
        // If we reach here, authentication failed
        request.setAttribute("errorMessage", errorMessage);
        request.setAttribute("adminId", adminIdOrEmail); // Preserve the entered admin ID/Email
        request.getRequestDispatcher("Admin/AdminLogin.jsp").forward(request, response);
    }

    private void logout(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            System.out.println("Logging out admin: " + session.getAttribute("adminName"));
            session.invalidate();
        }
        response.sendRedirect("Admin/AdminLogin.jsp?message=logout");
    }
}
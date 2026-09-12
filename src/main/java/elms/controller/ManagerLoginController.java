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

@WebServlet("/ManagerLoginController")
public class ManagerLoginController extends HttpServlet {
    
    private static final long serialVersionUID = 1L;
    private ManagerDAO managerDAO;
    
    @Override
    public void init() throws ServletException {
        super.init();
        managerDAO = new ManagerDAO();
        
        initializeSystemManager();
    }
    
    private void initializeSystemManager() {
        System.out.println("🔄 Initializing System Manager...");
        
        try {
            managerDAO.ensureSystemManagerExists();
            System.out.println("✅ System Manager initialization complete");
            
        } catch (Exception e) {
            System.err.println("❌ Failed to initialize System Manager: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Handle logout
        String action = request.getParameter("action");
        if ("logout".equals(action)) {
            handleLogout(request, response);
            return;
        }
        
        // Forward to login page
        request.getRequestDispatcher("/Manager/ManagerLogin.jsp").forward(request, response);
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String managerId = request.getParameter("managerId");
        String managerPassword = request.getParameter("managerPassword");
        
        // Validate input
        if (managerId == null || managerId.trim().isEmpty() || 
            managerPassword == null || managerPassword.trim().isEmpty()) {
            
            request.setAttribute("errorMessage", "Manager ID and Password are required.");
            request.getRequestDispatcher("/Manager/ManagerLogin.jsp").forward(request, response);
            return;
        }
        
        // Clean input
        managerId = managerId.trim();
        managerPassword = managerPassword.trim();
        
        try {
            Manager manager = authenticateManager(managerId, managerPassword);
            
            if (manager != null) {
                // Successful login
                handleSuccessfulLogin(request, response, manager);
            } else {
                // Failed login
                handleFailedLogin(request, response);
            }
            
        } catch (Exception e) {
            // Handle any unexpected errors
            System.err.println("Error during manager login: " + e.getMessage());
            e.printStackTrace();
            
            request.setAttribute("errorMessage", "An error occurred during login. Please try again.");
            request.getRequestDispatcher("/Manager/ManagerLogin.jsp").forward(request, response);
        }
    }
    
    /**
     * Hash password using SHA-256
     */
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }
    
    /**
     * Authenticate manager using either Manager ID or Email with SHA-256 password hashing
     */
    private Manager authenticateManager(String managerId, String password) {
        Manager manager = null;
        String hashedPassword = hashPassword(password);
        
        // First, try to get manager by ID and check hashed password
        manager = managerDAO.getManagerById(managerId);
        if (manager != null && manager.getManagerpassword().equals(hashedPassword)) {
            return manager;
        }
        
        // If not found by ID, try to authenticate by email
        // (assuming managerId field might contain email)
        if (managerId.contains("@")) {
            manager = managerDAO.getManagerByEmail(managerId);
            if (manager != null && manager.getManagerpassword().equals(hashedPassword)) {
                return manager;
            }
        }
        
        // If still not found, try to get manager by email using the managerId parameter
        // This handles cases where user enters email in managerId field
        manager = managerDAO.getManagerByEmail(managerId);
        if (manager != null && manager.getManagerpassword().equals(hashedPassword)) {
            return manager;
        }
        
        return null; // Authentication failed
    }
    
    /**
     * Determine user type based on Manager ID
     * ED = Executive Director, PM = Project Manager
     */
    private String determineUserType(String managerId) {
        if (managerId == null || managerId.trim().isEmpty()) {
            return "manager"; // default
        }
        
        String id = managerId.trim().toUpperCase();
        
        // Check if ID starts with "ED" for Executive Director
        if (id.startsWith("ED")) {
            return "executive_director";
        }
        // Check if ID starts with "PM" for Project Manager
        else if (id.startsWith("PM")) {
            return "project_manager";
        }
        // Default to regular manager for any other pattern
        else {
            return "manager";
        }
    }
    
    /**
     * Handle successful login with role-based redirection
     */
    private void handleSuccessfulLogin(HttpServletRequest request, HttpServletResponse response, Manager manager) 
            throws ServletException, IOException {
        
        // Create session
        HttpSession session = request.getSession(true);
        
        // Determine user type based on manager ID
        String userType = determineUserType(manager.getManagerid());
        
        // Store manager information in session
        session.setAttribute("manager", manager);
        session.setAttribute("managerId", manager.getManagerid());
        session.setAttribute("managerName", manager.getManagername());
        session.setAttribute("managerEmail", manager.getManageremail());
        session.setAttribute("managerPosition", manager.getManagerposition());
        session.setAttribute("userType", userType);
        
        // Set session timeout (30 minutes)
        session.setMaxInactiveInterval(30 * 60);
        
        // Log successful login
        System.out.println("Manager logged in successfully: " + manager.getManagerid() + " - " + manager.getManagername() + " (Type: " + userType + ")");
        
        // Redirect based on user type
        String redirectUrl;
        switch (userType) {
            case "executive_director":
                redirectUrl = request.getContextPath() + "/Manager/ExecutiveDirectorDashboard.jsp";
                break;
            case "project_manager":
                redirectUrl = request.getContextPath() + "/Manager/ManagerDashboard.jsp";
                break;
            default:
                redirectUrl = request.getContextPath() + "/Manager/ManagerDashboard.jsp";
                break;
        }
        
        response.sendRedirect(redirectUrl);
    }
    
    /**
     * Handle failed login
     */
    private void handleFailedLogin(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Log failed login attempt
        String attemptedId = request.getParameter("managerId");
        System.out.println("Failed login attempt for manager ID: " + attemptedId);
        
        // Set error message
        request.setAttribute("errorMessage", "Invalid Manager ID or Password. Please try again.");
        
        // Forward back to login page
        request.getRequestDispatcher("/Manager/ManagerLogin.jsp").forward(request, response);
    }
    
    /**
     * Handle logout
     */
    private void handleLogout(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        if (session != null) {
            // Log logout
            String managerId = (String) session.getAttribute("managerId");
            String userType = (String) session.getAttribute("userType");
            if (managerId != null) {
                System.out.println("Manager logged out: " + managerId + " (Type: " + userType + ")");
            }
            
            // Invalidate session
            session.invalidate();
        }
        
        // Redirect to login page with logout message
        response.sendRedirect(request.getContextPath() + "/Manager/ManagerLogin.jsp?message=logout");
    }
    
    /**
     * Utility method to check if manager is already logged in
     */
    public static boolean isManagerLoggedIn(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null && session.getAttribute("manager") != null;
    }
    
    /**
     * Utility method to get logged in manager from session
     */
    public static Manager getLoggedInManager(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            return (Manager) session.getAttribute("manager");
        }
        return null;
    }
    
    /**
     * Utility method to get user type from session
     */
    public static String getUserType(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            return (String) session.getAttribute("userType");
        }
        return null;
    }
    
    /**
     * Utility method to check if logged in user is Executive Director
     */
    public static boolean isExecutiveDirector(HttpServletRequest request) {
        return "executive_director".equals(getUserType(request));
    }
    
    /**
     * Utility method to check if logged in user is Project Manager
     */
    public static boolean isProjectManager(HttpServletRequest request) {
        return "project_manager".equals(getUserType(request));
    }
}
package elms.controller;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import elms.DAO.EmployeeDAO;
import elms.model.Employee;

/**
 * Complete Login Controller - Handles employee login functionality with proper session management
 */
@WebServlet("/LoginController")
public class EmployeeLoginController extends HttpServlet {
    private static final long serialVersionUID = 1L;

    public EmployeeLoginController() {
        super();
    }

    /**
     * GET - Show login page
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        System.out.println("=== LOGIN CONTROLLER: Showing login page ===");
        request.getRequestDispatcher("Employee/EmployeeLogin.jsp").forward(request, response);
    }

    /**
     * POST - Process login with enhanced session management
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        System.out.println("=== LOGIN CONTROLLER: Processing login ===");
        
        // Get form parameters
        String employeeIdOrEmail = request.getParameter("employeeid");
        String password = request.getParameter("employeepassword");
        
        System.out.println("Login attempt for Employee ID/Email: " + employeeIdOrEmail);
        
        // Validate input
        if (employeeIdOrEmail == null || employeeIdOrEmail.trim().isEmpty() || 
            password == null || password.trim().isEmpty()) {
            
            System.out.println("ERROR: Missing credentials");
            request.setAttribute("errorMessage", "Please enter both Employee ID/Email and Password");
            request.setAttribute("employeeid", employeeIdOrEmail);
            request.getRequestDispatcher("Employee/EmployeeLogin.jsp").forward(request, response);
            return;
        }
        
        try {
            // Test password hashing for debugging
            System.out.println("=== PASSWORD DEBUG TEST ===");
            String testPassword = "password123";
            String testHash = EmployeeDAO.hashPassword(testPassword);
            System.out.println("Test password: " + testPassword);
            System.out.println("Test hash: " + testHash);
            System.out.println("Hash length: " + testHash.length());
            System.out.println("=== END TEST ===");
            
            Employee employee = null;
            
            // Check if input contains @ symbol to determine if it's an email
            if (employeeIdOrEmail.trim().contains("@")) {
                System.out.println("Login attempt using email: " + employeeIdOrEmail.trim());
                // Try to validate using email (you'll need to add this method to EmployeeDAO)
                employee = EmployeeDAO.validateLoginByEmail(employeeIdOrEmail.trim(), password);
            } else {
                System.out.println("Login attempt using employee ID: " + employeeIdOrEmail.trim());
                // Validate using employee ID (existing method)
                employee = EmployeeDAO.validateLogin(employeeIdOrEmail.trim(), password);
            }
            
            if (employee != null) {
                // Login successful
                System.out.println("SUCCESS: Login successful for " + employee.getEmployeeName());
                System.out.println("Employee details retrieved:");
                System.out.println("- ID: " + employee.getEmployeeId());
                System.out.println("- Name: " + employee.getEmployeeName());
                System.out.println("- Email: " + employee.getEmployeeEmail());
                System.out.println("- Phone: " + employee.getEmployeeNoPhone());    
                System.out.println("- Profile Picture: " + employee.getProfilePicturePath());
                
                // Create session and store complete employee data
                HttpSession session = request.getSession();
                
                // Clear any existing session data to prevent conflicts
                session.invalidate();
                session = request.getSession(true);
                
                // Store complete employee object
                session.setAttribute("loggedInEmployee", employee);
                
                // Also store individual attributes for backward compatibility
                session.setAttribute("employeeid", employee.getEmployeeId());
                session.setAttribute("employeename", employee.getEmployeeName());
                session.setAttribute("employeeemail", employee.getEmployeeEmail());
                session.setAttribute("employeenophone", employee.getEmployeeNoPhone());
                session.setAttribute("profile_picture_path", employee.getProfilePicturePath());
                
                // Set session timeout (30 minutes)
                session.setMaxInactiveInterval(30 * 60);
                
                System.out.println("Session created successfully:");
                System.out.println("- Session ID: " + session.getId());
                System.out.println("- Employee stored in session: " + ((Employee) session.getAttribute("loggedInEmployee")).getEmployeeName());
                System.out.println("- Profile picture in session: " + ((Employee) session.getAttribute("loggedInEmployee")).getProfilePicturePath());
                
                // Redirect to dashboard
                response.sendRedirect(request.getContextPath() + "/Employee/EmployeeDashboard.jsp");
                
            } else {
                // Login failed
                System.out.println("ERROR: Invalid credentials for Employee ID/Email: " + employeeIdOrEmail);
                request.setAttribute("errorMessage", "Invalid Employee ID/Email or Password");
                request.setAttribute("employeeid", employeeIdOrEmail);
                request.getRequestDispatcher("Employee/EmployeeLogin.jsp").forward(request, response);
            }
            
        } catch (Exception e) {
            System.err.println("ERROR: Exception during login: " + e.getMessage());
            e.printStackTrace();
            
            request.setAttribute("errorMessage", "System error. Please try again later.");
            request.getRequestDispatcher("Employee/EmployeeLogin.jsp").forward(request, response);
        }
        
        System.out.println("=== LOGIN CONTROLLER END ===");
    }
}
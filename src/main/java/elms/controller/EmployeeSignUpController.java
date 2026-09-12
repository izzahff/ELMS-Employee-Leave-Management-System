package elms.controller;
/*
 * Author: Your Name
 * June 2025
 */
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import elms.DAO.EmployeeDAO;
import elms.model.Employee;
import java.io.IOException;
import java.sql.SQLException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;

/**
 * Servlet implementation class EmployeeSignUpController
 */
@WebServlet("/employeeSignUp")
public class EmployeeSignUpController extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // Malaysian phone number pattern: +60 1x-xxx xxxx
    private static final Pattern MALAYSIA_PHONE_PATTERN = Pattern.compile("^\\+60\\s1[0-9]-[0-9]{3}\\s[0-9]{4}$");
    
    /**
     * @see HttpServlet#HttpServlet()
     */
    public EmployeeSignUpController() {
        super();
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Forward to the signup form (JSP)
        RequestDispatcher req = request.getRequestDispatcher("/Employee/EmployeeSignUp.jsp");
        req.forward(request, response);
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
        System.out.println("INFO: EmployeeSignUpController doPost called");
        System.out.println("=== CONTROLLER DEBUG START ===");
        
        try {
            // Debug: Print all form parameters
            System.out.println("Employee Name: " + request.getParameter("employeename"));
            System.out.println("Employee Email: " + request.getParameter("employeeemail"));
            System.out.println("Employee Phone: " + request.getParameter("employeenophone"));
            System.out.println("Password received: " + (request.getParameter("employeepassword") != null ? "YES" : "NO"));
            System.out.println("Confirm Password received: " + (request.getParameter("confirmPassword") != null ? "YES" : "NO"));
            
            // Get form parameters
            String employeeName = request.getParameter("employeename");
            String employeeEmail = request.getParameter("employeeemail");
            String employeePhone = request.getParameter("employeenophone");
            String password = request.getParameter("employeepassword");
            String confirmPassword = request.getParameter("confirmPassword");
            
            // Validate required fields
            if (employeeName == null || employeeName.trim().isEmpty()) {
                handleError(request, response, "Employee name is required!");
                return;
            }
            
            if (employeeEmail == null || employeeEmail.trim().isEmpty()) {
                handleError(request, response, "Email is required!");
                return;
            }
            
            if (employeePhone == null || employeePhone.trim().isEmpty()) {
                handleError(request, response, "Phone number is required!");
                return;
            }
            
            if (password == null || password.trim().isEmpty()) {
                handleError(request, response, "Password is required!");
                return;
            }
            
            if (confirmPassword == null || confirmPassword.trim().isEmpty()) {
                handleError(request, response, "Password confirmation is required!");
                return;
            }

            // Validate employee name (letters, spaces, hyphens, apostrophes only)
            if (!isValidName(employeeName.trim())) {
                handleError(request, response, "Employee name can only contain letters, spaces, hyphens, and apostrophes!");
                return;
            }

            // Validate email format
            if (!isValidEmail(employeeEmail.trim())) {
                handleError(request, response, "Please enter a valid email address!");
                return;
            }

            // Validate Malaysian phone number format (flexible)
            String formattedPhone = cleanPhoneNumber(employeePhone.trim());
            if (formattedPhone == null) {
                handleError(request, response, "Please enter a valid Malaysian mobile number (e.g., 0123456789 or 123456789)!");
                return;
            }
            
            System.out.println("DEBUG: Phone formatted from '" + employeePhone + "' to '" + formattedPhone + "'");

            // Validate password confirmation
            if (!password.equals(confirmPassword)) {
                System.err.println("ERROR: Passwords do not match!");
                handleError(request, response, "Passwords do not match!");
                return;
            }

            // Validate password strength
            if (!isValidPassword(password)) {
                handleError(request, response, "Password must be at least 8 characters long and contain uppercase, lowercase, and number!");
                return;
            }

            // Check if email already exists
            System.out.println("DEBUG: Checking if email already exists...");
            Employee existingEmployee = EmployeeDAO.getEmployeeByEmail(employeeEmail);
            if (existingEmployee != null) {
                handleError(request, response, "An account with this email already exists!");
                return;
            }

            // Clean and format phone number (use the formatted version)
            System.out.println("DEBUG: Using formatted phone number: " + formattedPhone);

            // Create Employee object
            Employee employee = new Employee();
            employee.setEmployeeName(employeeName.trim());
            employee.setEmployeeEmail(employeeEmail.trim().toLowerCase()); // Store email in lowercase
            employee.setEmployeeNoPhone(formattedPhone);

            // Hash the password before storing
            String hashedPassword = EmployeeDAO.hashPassword(password);
            employee.setEmployeePassword(hashedPassword);

            System.out.println("DEBUG: About to call EmployeeDAO.addEmployee()");
            System.out.println("DEBUG: Employee object created - Name: " + employee.getEmployeeName());
            System.out.println("DEBUG: Employee phone (formatted): " + employee.getEmployeeNoPhone());
            
            // Call addEmployee() from EmployeeDAO class and get the generated employee ID
            String generatedEmployeeId = EmployeeDAO.addEmployee(employee);
            
            if (generatedEmployeeId != null && !generatedEmployeeId.trim().isEmpty()) {
                System.out.println("SUCCESS: Employee created with ID: " + generatedEmployeeId);
                
                // Set attributes for success page
                request.setAttribute("employeeid", generatedEmployeeId);
                request.setAttribute("employeename", employee.getEmployeeName());
                request.setAttribute("employeeemail", employee.getEmployeeEmail());
                request.setAttribute("employeenophone", employee.getEmployeeNoPhone());
                request.setAttribute("successmessage", "Registration successful! Your Employee ID is: " + generatedEmployeeId);

                // Forward to success page
                RequestDispatcher req = request.getRequestDispatcher("/Employee/registrationsuccess.jsp");
                req.forward(request, response);
            } else {
                // Registration failed
                System.err.println("ERROR: Employee registration failed - no ID generated");
                handleError(request, response, "Registration failed. No employee ID was generated. Please check your database trigger.");
            }

        } catch (SQLException e) {
            // Handle database errors with DETAILED information
            System.err.println("=== SQL EXCEPTION DETAILS ===");
            System.err.println("Message: " + e.getMessage());
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            System.err.println("Class: " + e.getClass().getName());
            e.printStackTrace();
            System.err.println("=== END SQL EXCEPTION ===");
            
            // Create user-friendly error message
            String userError = "Registration failed due to a database error. ";
            
            if (e.getErrorCode() == 942) {
                userError += "Database table not found. Please contact system administrator.";
            } else if (e.getErrorCode() == 1) {
                userError += "This email address is already registered. Please use a different email.";
            } else if (e.getErrorCode() == 904) {
                userError += "Database configuration error. Please contact system administrator.";
            } else if (e.getErrorCode() == 1400) {
                userError += "Required information is missing. Please fill all fields.";
            } else {
                userError += "Please try again later or contact system administrator.";
            }
            
            handleError(request, response, userError);
            
        } catch (Exception e) {
            // Handle other errors
            System.err.println("=== GENERAL EXCEPTION DETAILS ===");
            System.err.println("Message: " + e.getMessage());
            System.err.println("Class: " + e.getClass().getName());
            e.printStackTrace();
            System.err.println("=== END GENERAL EXCEPTION ===");
            
            handleError(request, response, "An unexpected error occurred. Please try again later.");
        }
        
        System.out.println("=== CONTROLLER DEBUG END ===");
    }
    
    /**
     * Validate employee name (letters, spaces, hyphens, apostrophes only)
     */
    private boolean isValidName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        // Allow letters, spaces, hyphens, and apostrophes
        return name.matches("^[a-zA-Z\\s'-]+$") && name.length() >= 2 && name.length() <= 100;
    }
    
    /**
     * Validate email format
     */
    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }
    
    /**
     * Validate Malaysian phone number format: +60 1x-xxx xxxx
     */
    private boolean isValidMalaysianPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        return MALAYSIA_PHONE_PATTERN.matcher(phone.trim()).matches();
    }
    
    /**
     * Validate password strength
     */
    private boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        
        // Check for at least one uppercase, one lowercase, and one number
        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasNumber = password.matches(".*[0-9].*");
        
        return hasUpper && hasLower && hasNumber;
    }
    
    /**
     * Clean phone number format (ensure consistent formatting)
     */
    private String cleanPhoneNumber(String phone) {
        if (phone == null) {
            return null;
        }
        // Remove any extra spaces and ensure consistent formatting
        return phone.replaceAll("\\s+", " ").trim();
    }
    
    /**
     * Helper method to handle errors consistently
     */
    private void handleError(HttpServletRequest request, HttpServletResponse response, String errorMessage) 
            throws ServletException, IOException {
        System.err.println("HANDLING ERROR: " + errorMessage);
        
        // Preserve form data for user convenience
        request.setAttribute("errorMessage", errorMessage);
        request.setAttribute("employeename", request.getParameter("employeename"));
        request.setAttribute("employeeemail", request.getParameter("employeeemail"));
        request.setAttribute("employeenophone", request.getParameter("employeenophone"));
        
        RequestDispatcher req = request.getRequestDispatcher("/Employee/EmployeeSignUp.jsp");
        req.forward(request, response);
    }
}
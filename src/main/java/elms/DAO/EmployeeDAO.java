package elms.DAO;

import elms.model.Employee;
import elms.connection.ConnectionManager;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.io.File;

/**
 * Complete EmployeeDAO with employeeleavebalance removed
 * All method names preserved exactly as original
 * All balance calculations are now done dynamically via LeaveBalanceDAO
 */
public class EmployeeDAO {
    
    /**
     * Add new employee with proper transaction handling for triggers
     */
    public static String addEmployee(Employee employee) throws SQLException {
        System.out.println("=== DAO DEBUG START ===");
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        String generatedEmployeeId = null;
        
        // Set default profile picture path
        String defaultProfilePicture = "defaultprofilepicture.jpg";
        employee.setProfilePicturePath(defaultProfilePicture);
        
        try {
            conn = ConnectionManager.getConnection();
            if (conn == null) {
                throw new SQLException("Failed to establish database connection");
            }
            
            // Method 1: Try using RETURNING clause (works with triggers)
            System.out.println("DEBUG: Trying Method 1 - RETURNING clause");
            try {
                String insertWithReturning = 
                    "INSERT INTO employee (employeename, employeenophone, employeeemail, employeepassword, profile_picture_path) " +
                    "VALUES (?, ?, ?, ?, ?) RETURNING employeeid";
                
                pstmt = conn.prepareStatement(insertWithReturning);
                pstmt.setString(1, employee.getEmployeeName());
                pstmt.setString(2, employee.getEmployeeNoPhone());
                pstmt.setString(3, employee.getEmployeeEmail());
                pstmt.setString(4, employee.getEmployeePassword());
                pstmt.setString(5, defaultProfilePicture);
                
                ResultSet rs = pstmt.executeQuery();
                
                if (rs.next()) {
                    generatedEmployeeId = rs.getString(1);
                    System.out.println("SUCCESS: Method 1 worked! Generated ID: " + generatedEmployeeId);
                    System.out.println("Default profile picture set to: " + defaultProfilePicture);
                    rs.close();
                    pstmt.close();
                    return generatedEmployeeId;
                }
                rs.close();
                pstmt.close();
                
            } catch (SQLException e) {
                System.out.println("DEBUG: Method 1 failed: " + e.getMessage());
                if (pstmt != null) {
                    try { pstmt.close(); } catch (SQLException ex) {}
                }
            }
            
            // Method 2: Traditional insert + immediate select in same transaction
            System.out.println("DEBUG: Trying Method 2 - Insert then immediate select");
            try {
                boolean originalAutoCommit = conn.getAutoCommit();
                conn.setAutoCommit(true);
                
                String insertSql = "INSERT INTO employee (employeename, employeenophone, employeeemail, employeepassword, profile_picture_path) " +
                                  "VALUES (?, ?, ?, ?, ?)";
                pstmt = conn.prepareStatement(insertSql);
                pstmt.setString(1, employee.getEmployeeName());
                pstmt.setString(2, employee.getEmployeeNoPhone());
                pstmt.setString(3, employee.getEmployeeEmail());
                pstmt.setString(4, employee.getEmployeePassword());
                pstmt.setString(5, defaultProfilePicture);
                
                int rowsAffected = pstmt.executeUpdate();
                pstmt.close();
                
                if (rowsAffected > 0) {
                    System.out.println("DEBUG: Insert successful, now retrieving ID...");
                    System.out.println("Default profile picture set to: " + defaultProfilePicture);
                    
                    PreparedStatement selectStmt = conn.prepareStatement(
                        "SELECT employeeid FROM employee WHERE employeeemail = ? ORDER BY employeeid DESC"
                    );
                    selectStmt.setString(1, employee.getEmployeeEmail());
                    ResultSet rs = selectStmt.executeQuery();
                    
                    if (rs.next()) {
                        generatedEmployeeId = rs.getString("employeeid");
                        System.out.println("SUCCESS: Method 2 worked! Generated ID: " + generatedEmployeeId);
                    } else {
                        System.err.println("ERROR: Could not find inserted record");
                    }
                    
                    rs.close();
                    selectStmt.close();
                }
                
                conn.setAutoCommit(originalAutoCommit);
                
            } catch (SQLException e) {
                System.err.println("ERROR: Method 2 failed: " + e.getMessage());
                if (pstmt != null) {
                    try { pstmt.close(); } catch (SQLException ex) {}
                }
                throw e;
            }
            
            if (generatedEmployeeId == null) {
                // Method 3: Check what was actually inserted for debugging
                System.out.println("DEBUG: Method 3 - Checking what was actually inserted");
                try {
                    PreparedStatement debugStmt = conn.prepareStatement(
                        "SELECT * FROM employee WHERE employeeemail = ?"
                    );
                    debugStmt.setString(1, employee.getEmployeeEmail());
                    ResultSet debugRs = debugStmt.executeQuery();
                    
                    if (debugRs.next()) {
                        ResultSetMetaData rsmd = debugRs.getMetaData();
                        System.out.println("DEBUG: Inserted record contains:");
                        
                        for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                            String columnName = rsmd.getColumnName(i);
                            String value = debugRs.getString(i);
                            System.out.println("  " + columnName + " = " + value);
                            
                            if (columnName.equalsIgnoreCase("employeeid") || 
                                columnName.equalsIgnoreCase("employee_id") ||
                                columnName.equalsIgnoreCase("empid")) {
                                if (value != null && !value.trim().isEmpty()) {
                                    generatedEmployeeId = value;
                                    System.out.println("SUCCESS: Found employee ID in column '" + columnName + "': " + value);
                                }
                            }
                        }
                    }
                    
                    debugRs.close();
                    debugStmt.close();
                    
                } catch (SQLException e) {
                    System.err.println("ERROR: Debug query failed: " + e.getMessage());
                }
            }
            
        } catch (SQLException e) {
            System.err.println("ERROR: SQL Exception in addEmployee: " + e.getMessage());
            throw e;
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("ERROR: Failed to close resources: " + e.getMessage());
            }
        }
        
        if (generatedEmployeeId == null) {
            throw new SQLException("Failed to retrieve generated employee ID after successful insert. Trigger may not be working properly.");
        }
        
        System.out.println("=== DAO DEBUG END ===");
        return generatedEmployeeId;
    }
    
    /**
     * Get employee by email
     */
    public static Employee getEmployeeByEmail(String email) {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                 "SELECT employeeid, employeename, employeeemail, employeenophone, " +
                 "employeepassword, profile_picture_path FROM employee WHERE employeeemail = ?")) {
            
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Employee employee = new Employee();
                employee.setEmployeeName(rs.getString("employeename"));
                employee.setEmployeeEmail(rs.getString("employeeemail"));
                employee.setEmployeeNoPhone(rs.getString("employeenophone"));
                employee.setEmployeePassword(rs.getString("employeepassword"));
                
                // Try to get employee ID
                try {
                    employee.setEmployeeId(rs.getString("employeeid"));
                } catch (SQLException e) {
                    // Column might have different name
                }
                
                // Try to get profile picture path
                try {
                    employee.setProfilePicturePath(rs.getString("profile_picture_path"));
                } catch (SQLException e) {
                    // Column might not exist in older versions
                }
                
                return employee;
            }
            
        } catch (SQLException e) {
            System.err.println("ERROR: Failed to get employee by email: " + e.getMessage());
        }
        
        return null;
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
    
    /**
     * Validate employee login using employee ID
     */
    public static Employee validateLogin(String employeeId, String password) {
        System.out.println("=== LOGIN VALIDATION DEBUG ===");
        System.out.println("Input Employee ID: '" + employeeId + "'");
        System.out.println("Input Password: '" + password + "'");
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Employee employee = null;
        
        try {
            conn = ConnectionManager.getConnection();
            
            String sql = "SELECT employeeid, employeename, employeeemail, employeenophone, " +
                        "employeepassword, profile_picture_path FROM employee WHERE employeeid = ?";
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, employeeId.trim());
            
            System.out.println("DEBUG: Executing query with Employee ID: '" + employeeId.trim() + "'");
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String storedHash = rs.getString("employeepassword");
                System.out.println("Employee found!");
                System.out.println("Employee Name: " + rs.getString("employeename"));
                System.out.println("Profile Picture Path: " + rs.getString("profile_picture_path"));
                System.out.println("Stored hash: " + storedHash);
                System.out.println("Stored hash length: " + storedHash.length());
                
                // Hash the input password
                String inputHash = hashPassword(password);
                System.out.println("Input hash: " + inputHash);
                System.out.println("Input hash length: " + inputHash.length());
                
                // Compare hashes
                boolean matches = inputHash.equals(storedHash);
                System.out.println("Hashes match: " + matches);
                
                if (matches) {
                    // Create employee object
                    employee = new Employee();
                    employee.setEmployeeId(rs.getString("employeeid"));
                    employee.setEmployeeName(rs.getString("employeename"));
                    employee.setEmployeeEmail(rs.getString("employeeemail"));
                    employee.setEmployeeNoPhone(rs.getString("employeenophone"));
                    employee.setEmployeePassword(rs.getString("employeepassword"));
                    
                    // Set profile picture path
                    String profilePicturePath = rs.getString("profile_picture_path");
                    employee.setProfilePicturePath(profilePicturePath);
                    System.out.println("Set profile picture path to: " + profilePicturePath);
                    
                    System.out.println("SUCCESS: Login validated!");
                } else {
                    System.out.println("FAILED: Password hash mismatch");
                }
            } else {
                System.out.println("FAILED: No employee found with ID: " + employeeId);
            }
            
        } catch (SQLException e) {
            System.err.println("SQL ERROR: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("ERROR closing resources: " + e.getMessage());
            }
        }
        
        return employee;
    }
    
    /**
     * Update employee information
     */
    public static String updateEmployee(Employee employee) throws SQLException {
        System.out.println("=== UPDATING EMPLOYEE: " + employee.getEmployeeId() + " ===");
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        String result = null;
        String defaultProfilePicture = "defaultprofilepicture.jpg";
        
        try {
            conn = ConnectionManager.getConnection();
            if (conn == null) {
                return "error: Database connection failed";
            }
            
            conn.setAutoCommit(false);
            
            // Check for email duplicates (excluding current employee)
            if (isEmailDuplicate(conn, employee.getEmployeeEmail(), employee.getEmployeeId())) {
                conn.rollback();
                return "error: Email address is already in use by another employee";
            }
            
            // If profile picture path is null or empty, set to default
            String profilePicturePath = employee.getProfilePicturePath();
            if (profilePicturePath == null || profilePicturePath.trim().isEmpty()) {
                profilePicturePath = defaultProfilePicture;
                System.out.println("✅ Profile picture path was null/empty, setting to default: " + defaultProfilePicture);
            }
            
            String sql = "UPDATE employee SET employeename = ?, employeenophone = ?, " +
                        "employeeemail = ?, profile_picture_path = ? WHERE employeeid = ?";
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, employee.getEmployeeName());
            pstmt.setString(2, employee.getEmployeeNoPhone());
            pstmt.setString(3, employee.getEmployeeEmail());
            pstmt.setString(4, profilePicturePath);
            pstmt.setString(5, employee.getEmployeeId());
            
            System.out.println("Updating profile_picture_path to: " + profilePicturePath);
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                conn.commit();
                result = "success: Employee updated successfully";
                System.out.println("Employee updated successfully");
            } else {
                conn.rollback();
                result = "error: No employee found with the given ID";
            }
            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Rollback error: " + ex.getMessage());
                }
            }
            System.err.println("Error updating employee: " + e.getMessage());
            throw e;
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
        
        return result;
    }
    
    /**
     * Check if email is duplicate (excluding specific employee ID)
     */
    private static boolean isEmailDuplicate(Connection conn, String email, String excludeEmployeeId) throws SQLException {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            String sql = "SELECT COUNT(*) FROM employee WHERE employeeemail = ? AND employeeid != ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, email);
            pstmt.setString(2, excludeEmployeeId);
            
            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } finally {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
        }
        
        return false;
    }
    
    /**
     * Update employee profile picture path
     */
    public static boolean updateProfilePicture(String employeeId, String profilePicturePath) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = ConnectionManager.getConnection();
            
            String sql = "UPDATE employee SET profile_picture_path = ? WHERE employeeid = ?";
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, profilePicturePath);
            pstmt.setString(2, employeeId);
            
            int rowsAffected = pstmt.executeUpdate();
            
            System.out.println("Profile picture update - Rows affected: " + rowsAffected);
            
            return rowsAffected > 0;
            
        } finally {
            if (pstmt != null) pstmt.close();
            if (conn != null) conn.close();
        }
    }
    
    /**
     * Get employee by ID only (without password validation)
     */
    public static Employee getEmployeeByIdOnly(String employeeId) {
        System.out.println("=== FETCHING EMPLOYEE BY ID: " + employeeId + " ===");
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Employee employee = null;
        
        try {
            conn = ConnectionManager.getConnection();
            
            String sql = "SELECT employeeid, employeename, employeeemail, employeenophone, " +
                        "employeepassword, profile_picture_path FROM employee WHERE employeeid = ?";
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, employeeId.trim());
            
            System.out.println("DEBUG: Fetching employee with ID: '" + employeeId.trim() + "'");
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                employee = new Employee();
                employee.setEmployeeId(rs.getString("employeeid"));
                employee.setEmployeeName(rs.getString("employeename"));
                employee.setEmployeeEmail(rs.getString("employeeemail"));
                employee.setEmployeeNoPhone(rs.getString("employeenophone"));
                employee.setEmployeePassword(rs.getString("employeepassword"));
                
                String profilePicturePath = rs.getString("profile_picture_path");
                employee.setProfilePicturePath(profilePicturePath);
                
                System.out.println("✅ Employee found:");
                System.out.println("- Name: " + employee.getEmployeeName());
                System.out.println("- Email: " + employee.getEmployeeEmail());
                System.out.println("- Profile Picture: " + profilePicturePath);
                
            } else {
                System.out.println("❌ No employee found with ID: " + employeeId);
            }
            
        } catch (SQLException e) {
            System.err.println("❌ SQL ERROR: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("ERROR closing resources: " + e.getMessage());
            }
        }
        
        System.out.println("=== FETCH EMPLOYEE BY ID COMPLETE ===");
        return employee;
    }
    
    /**
     * Update employee password separately (for change password functionality)
     */
    public static String updateEmployeePassword(String employeeId, String newHashedPassword) throws SQLException {
        System.out.println("=== SIMPLE UPDATE EMPLOYEE PASSWORD START ===");
        System.out.println("Employee ID: " + employeeId);
        System.out.println("New hashed password (first 10 chars): " + newHashedPassword.substring(0, 10) + "...");
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = ConnectionManager.getConnection();
            if (conn == null) {
                System.out.println("❌ ERROR: Cannot get database connection");
                return "error: Database connection failed";
            }
            
            System.out.println("✅ Database connection successful");
            
            String sql = "UPDATE employee SET employeepassword = ? WHERE employeeid = ?";
            System.out.println("SQL: " + sql);
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, newHashedPassword);
            pstmt.setString(2, employeeId);
            
            System.out.println("Executing update...");
            int rowsUpdated = pstmt.executeUpdate();
            
            System.out.println("Rows updated: " + rowsUpdated);
            
            if (rowsUpdated > 0) {
                System.out.println("✅ SUCCESS: Password updated in database");
                return "success: Password updated successfully";
            } else {
                System.out.println("❌ ERROR: No rows updated - employee might not exist");
                return "error: Employee not found";
            }
            
        } catch (SQLException e) {
            System.err.println("❌ SQL ERROR: " + e.getMessage());
            System.err.println("SQL State: " + e.getSQLState());
            System.err.println("Error Code: " + e.getErrorCode());
            e.printStackTrace();
            return "error: Database error - " + e.getMessage();
            
        } finally {
            try {
                if (pstmt != null) {
                    pstmt.close();
                    System.out.println("PreparedStatement closed");
                }
                if (conn != null) {
                    conn.close();
                    System.out.println("Connection closed");
                }
            } catch (SQLException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
    }
    
    /**
     * Validate employee login using email
     */
    public static Employee validateLoginByEmail(String email, String password) {
        System.out.println("=== VALIDATING LOGIN BY EMAIL ===");
        System.out.println("Email: " + email);
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Employee employee = null;
        
        try {
            conn = ConnectionManager.getConnection();
            
            String sql = "SELECT employeeid, employeename, employeeemail, employeenophone, " +
                        "employeepassword, profile_picture_path " +
                        "FROM employee WHERE employeeemail = ?";
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, email);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String storedHash = rs.getString("employeepassword");
                String inputHash = hashPassword(password);
                
                if (inputHash.equals(storedHash)) {
                    employee = new Employee();
                    employee.setEmployeeId(rs.getString("employeeid"));
                    employee.setEmployeeName(rs.getString("employeename"));
                    employee.setEmployeeEmail(rs.getString("employeeemail"));
                    employee.setEmployeeNoPhone(rs.getString("employeenophone"));
                    employee.setProfilePicturePath(rs.getString("profile_picture_path"));
                    
                    System.out.println("Email login successful for: " + employee.getEmployeeName());
                } else {
                    System.out.println("Password verification failed for email: " + email);
                }
            } else {
                System.out.println("No employee found with email: " + email);
            }
            
        } catch (SQLException e) {
            System.err.println("Database error during email login validation: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        
        return employee;
    }
    
    /**
     * Debugging method to help track down issues
     */
    public static void debugEmployeeData(String employeeId, String context) {
        System.out.println("=== DEBUG EMPLOYEE DATA (" + context + ") ===");
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = ConnectionManager.getConnection();
            
            String sql = "SELECT employeeid, employeename, employeeemail, employeenophone, " +
                        "employeepassword, profile_picture_path FROM employee WHERE employeeid = ?";
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, employeeId);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                System.out.println("DATABASE CONTENTS:");
                System.out.println("- Employee ID: " + rs.getString("employeeid"));
                System.out.println("- Name: " + rs.getString("employeename"));
                System.out.println("- Email: " + rs.getString("employeeemail"));
                System.out.println("- Phone: " + rs.getString("employeenophone"));
                System.out.println("- Profile Picture Path: " + rs.getString("profile_picture_path"));
                System.out.println("- Profile Picture Path Length: " + (rs.getString("profile_picture_path") != null ? rs.getString("profile_picture_path").length() : "null"));
            } else {
                System.out.println("❌ No employee found with ID: " + employeeId);
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Debug query failed: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing debug resources: " + e.getMessage());
            }
        }
        
        System.out.println("=== END DEBUG ===");
    }
    
    /**
     * Method to verify the profile picture file exists on disk
     */
    public static boolean verifyProfilePictureFile(String profilePicturePath, String contextPath) {
        if (profilePicturePath == null || profilePicturePath.isEmpty()) {
            System.out.println("Profile picture path is null or empty");
            return false;
        }
        
        try {
            String fullPath = contextPath + File.separator + profilePicturePath;
            File file = new File(fullPath);
            
            System.out.println("Verifying profile picture file:");
            System.out.println("- Relative path: " + profilePicturePath);
            System.out.println("- Full path: " + fullPath);
            System.out.println("- File exists: " + file.exists());
            System.out.println("- File size: " + (file.exists() ? file.length() + " bytes" : "N/A"));
            System.out.println("- File readable: " + (file.exists() ? file.canRead() : "N/A"));
            
            return file.exists() && file.canRead();
            
        } catch (Exception e) {
            System.err.println("Error verifying profile picture file: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get all employees from the database
     * @return List of Employee objects
     */
    public static List<Employee> getAllEmployees() {
        System.out.println("=== GETTING ALL EMPLOYEES ===");
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Employee> employees = new ArrayList<>();
        
        try {
            conn = ConnectionManager.getConnection();
            
            String sql = "SELECT employeeid, employeename, employeeemail, employeenophone, " +
                        "employeepassword, profile_picture_path " +
                        "FROM employee ORDER BY employeename";
            
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Employee employee = new Employee();
                employee.setEmployeeId(rs.getString("employeeid"));
                employee.setEmployeeName(rs.getString("employeename"));
                employee.setEmployeeEmail(rs.getString("employeeemail"));
                employee.setEmployeeNoPhone(rs.getString("employeenophone"));
                employee.setEmployeePassword(rs.getString("employeepassword"));
                employee.setProfilePicturePath(rs.getString("profile_picture_path"));
                
                employees.add(employee);
            }
            
            System.out.println("✅ Found " + employees.size() + " employees");
            
        } catch (SQLException e) {
            System.err.println("❌ SQL ERROR: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("ERROR closing resources: " + e.getMessage());
            }
        }
        
        return employees;
    }
}
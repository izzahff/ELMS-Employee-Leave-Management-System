package elms.DAO;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import elms.model.Admin;
import elms.model.LeaveType;
import elms.connection.ConnectionManager; 

public class AdminDAO {
    
    private ConnectionManager connectionManager;
    
    public AdminDAO() {
        this.connectionManager = new ConnectionManager();
    }
    
    /**
     * Authenticate admin with adminId and password (with SHA-256 verification)
     * @param adminId
     * @param adminPassword
     * @return Admin object if authentication successful, null otherwise
     */
    public Admin authenticateAdmin(String adminId, String adminPassword) throws SQLException {
        Admin admin = null;
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        
        String sql = "SELECT adminId, adminPassword, adminName, adminNoPhone, adminEmail, profile_picture_path " +
                     "FROM admin WHERE adminId = ?";
        
        try {
            connection = ConnectionManager.getConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, adminId);
            
            resultSet = preparedStatement.executeQuery();
            
            if (resultSet.next()) {
                String storedHashedPassword = resultSet.getString("adminPassword");
                
                // Hash the input password and compare with stored hash
                String inputHashedPassword = hashPassword(adminPassword);
                
                if (inputHashedPassword.equals(storedHashedPassword)) {
                    admin = new Admin();
                    admin.setAdminId(resultSet.getString("adminId"));
                    admin.setAdminPassword(storedHashedPassword); // Keep hashed password
                    admin.setAdminName(resultSet.getString("adminName"));
                    admin.setAdminNoPhone(resultSet.getString("adminNoPhone"));
                    admin.setAdminEmail(resultSet.getString("adminEmail"));
                    admin.setProfile_picture_path(resultSet.getString("profile_picture_path")); // ADD THIS LINE
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        } finally {
            closeResources(connection, preparedStatement, resultSet);
        }
        
        return admin;
    }
    
    /**
     * Authenticate admin with email and password (with SHA-256 verification)
     * @param email
     * @param adminPassword
     * @return Admin object if authentication successful, null otherwise
     */
    public Admin authenticateAdminByEmail(String email, String adminPassword) throws SQLException {
        Admin admin = null;
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        
        String sql = "SELECT adminId, adminPassword, adminName, adminNoPhone, adminEmail, profile_picture_path " +
                     "FROM admin WHERE LOWER(adminEmail) = LOWER(?)";
        
        try {
            connection = ConnectionManager.getConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, email);
            
            resultSet = preparedStatement.executeQuery();
            
            if (resultSet.next()) {
                String storedHashedPassword = resultSet.getString("adminPassword");
                
                // Hash the input password and compare with stored hash
                String inputHashedPassword = hashPassword(adminPassword);
                
                if (inputHashedPassword.equals(storedHashedPassword)) {
                    admin = new Admin();
                    admin.setAdminId(resultSet.getString("adminId"));
                    admin.setAdminPassword(storedHashedPassword); // Keep hashed password
                    admin.setAdminName(resultSet.getString("adminName"));
                    admin.setAdminNoPhone(resultSet.getString("adminNoPhone"));
                    admin.setAdminEmail(resultSet.getString("adminEmail"));
                    admin.setProfile_picture_path(resultSet.getString("profile_picture_path")); // ADD THIS LINE
                    
                    System.out.println("Email login successful for: " + admin.getAdminName());
                } else {
                    System.out.println("Password verification failed for email: " + email);
                }
            } else {
                System.out.println("No admin found with email: " + email);
            }
            
        } catch (SQLException e) {
            System.err.println("Database error during email login validation: " + e.getMessage());
            e.printStackTrace();
            throw e;
        } finally {
            closeResources(connection, preparedStatement, resultSet);
        }
        
        return admin;
    }
    
    /**
     * Check if email already exists
     * @param email
     * @return true if email exists, false otherwise
     */
    public boolean isEmailExists(String email) throws SQLException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        
        String sql = "SELECT COUNT(*) FROM admin WHERE LOWER(adminEmail) = LOWER(?)";
        
        try {
            connection = ConnectionManager.getConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, email);
            
            resultSet = preparedStatement.executeQuery();
            
            if (resultSet.next()) {
                return resultSet.getInt(1) > 0;
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        } finally {
            closeResources(connection, preparedStatement, resultSet);
        }
        
        return false;
    }
    
    /**
     * Create new admin with auto-generated ID and default profile picture
     * @param admin
     * @return generated adminId if successful, null otherwise
     */
    public String createAdminWithGeneratedId(Admin admin) throws SQLException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet generatedKeys = null;
        String generatedAdminId = null;
        
        // Set default profile picture path for new admins
        String defaultProfilePicture = "defaultprofilepicture.jpg";
        
        // Use Oracle sequence for auto-generating ID - UPDATED to include profile picture
        String sql = "INSERT INTO admin (adminId, adminPassword, adminName, adminNoPhone, adminEmail, profile_picture_path) " +
                     "VALUES (admin_id_seq.NEXTVAL, ?, ?, ?, ?, ?)";
        
        try {
            connection = ConnectionManager.getConnection();
            preparedStatement = connection.prepareStatement(sql, new String[]{"adminId"});
            preparedStatement.setString(1, admin.getAdminPassword()); // Already hashed
            preparedStatement.setString(2, admin.getAdminName());
            
            // Handle optional phone number - set to empty string if null
            String phoneNumber = admin.getAdminNoPhone();
            if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
                preparedStatement.setString(3, ""); // Set empty string instead of null
            } else {
                preparedStatement.setString(3, phoneNumber.trim());
            }
            
            preparedStatement.setString(4, admin.getAdminEmail());
            preparedStatement.setString(5, defaultProfilePicture); // Set default profile picture
            
            System.out.println("Creating admin with default profile picture: " + defaultProfilePicture);
            
            int rowsAffected = preparedStatement.executeUpdate();
            
            if (rowsAffected > 0) {
                generatedKeys = preparedStatement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    generatedAdminId = generatedKeys.getString(1);
                    System.out.println("✅ Admin created successfully with ID: " + generatedAdminId);
                    System.out.println("✅ Default profile picture set: " + defaultProfilePicture);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error creating admin: " + e.getMessage());
            e.printStackTrace();
            throw e;
        } finally {
            closeResources(connection, preparedStatement, generatedKeys);
        }
        
        return generatedAdminId;
    }
    
    /**
     * Get admin by adminId
     * @param adminId
     * @return Admin object if found, null otherwise
     */
    public Admin getAdminById(String adminId) throws SQLException {
        Admin admin = null;
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        
        String sql = "SELECT adminId, adminPassword, adminName, adminNoPhone, adminEmail, profile_picture_path " +
                     "FROM admin WHERE adminId = ?";
        
        try {
            connection = ConnectionManager.getConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, adminId);
            
            resultSet = preparedStatement.executeQuery();
            
            if (resultSet.next()) {
                admin = new Admin();
                admin.setAdminId(resultSet.getString("adminId"));
                admin.setAdminPassword(resultSet.getString("adminPassword"));
                admin.setAdminName(resultSet.getString("adminName"));
                admin.setAdminNoPhone(resultSet.getString("adminNoPhone"));
                admin.setAdminEmail(resultSet.getString("adminEmail"));
                admin.setProfile_picture_path(resultSet.getString("profile_picture_path"));
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        } finally {
            closeResources(connection, preparedStatement, resultSet);
        }
        
        return admin;
    }
    
    /**
     * Update admin password (with SHA-256 hashing)
     * @param adminId
     * @param newPassword
     * @return true if update successful, false otherwise
     */
    public boolean updateAdminPassword(String adminId, String newPassword) throws SQLException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        boolean result = false;
        
        String sql = "UPDATE admin SET adminPassword = ? WHERE adminId = ?";
        
        try {
            connection = ConnectionManager.getConnection();
            preparedStatement = connection.prepareStatement(sql);
            
            // Hash the new password using SHA-256
            String hashedPassword = hashPassword(newPassword);
            preparedStatement.setString(1, hashedPassword);
            preparedStatement.setString(2, adminId);
            
            int rowsAffected = preparedStatement.executeUpdate();
            result = rowsAffected > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        } finally {
            closeResources(connection, preparedStatement, null);
        }
        
        return result;
    }
    
    /**
     * Update admin phone number only
     * @param adminId
     * @param phoneNumber
     * @return true if update successful, false otherwise
     */
    public boolean updateAdminPhone(String adminId, String phoneNumber) throws SQLException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        boolean result = false;
        
        String sql = "UPDATE admin SET adminNoPhone = ? WHERE adminId = ?";
        
        try {
            connection = ConnectionManager.getConnection();
            preparedStatement = connection.prepareStatement(sql);
            
            // Handle phone number - allow empty string or actual number
            if (phoneNumber == null) {
                preparedStatement.setString(1, "");
            } else {
                preparedStatement.setString(1, phoneNumber.trim());
            }
            
            preparedStatement.setString(2, adminId);
            
            int rowsAffected = preparedStatement.executeUpdate();
            result = rowsAffected > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        } finally {
            closeResources(connection, preparedStatement, null);
        }
        
        return result;
    }
    
    /**
     * Create new admin with default profile picture (original method kept for compatibility)
     * @param admin
     * @return true if creation successful, false otherwise
     */
    public boolean createAdmin(Admin admin) throws SQLException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        boolean result = false;
        
        // Set default profile picture path for new admins
        String defaultProfilePicture = "defaultprofilepicture.jpg";
        
        String sql = "INSERT INTO admin (adminId, adminPassword, adminName, adminNoPhone, adminEmail, profile_picture_path) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        
        try {
            connection = ConnectionManager.getConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, admin.getAdminId());
            preparedStatement.setString(2, admin.getAdminPassword());
            preparedStatement.setString(3, admin.getAdminName());
            
            // Handle optional phone number
            String phoneNumber = admin.getAdminNoPhone();
            if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
                preparedStatement.setString(4, ""); // Set empty string instead of null
            } else {
                preparedStatement.setString(4, phoneNumber.trim());
            }
            
            preparedStatement.setString(5, admin.getAdminEmail());
            preparedStatement.setString(6, defaultProfilePicture); // Set default profile picture
            
            System.out.println("Creating admin with default profile picture: " + defaultProfilePicture);
            
            int rowsAffected = preparedStatement.executeUpdate();
            result = rowsAffected > 0;
            
            if (result) {
                System.out.println("✅ Admin created successfully");
                System.out.println("✅ Default profile picture set: " + defaultProfilePicture);
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error creating admin: " + e.getMessage());
            e.printStackTrace();
            throw e;
        } finally {
            closeResources(connection, preparedStatement, null);
        }
        
        return result;
    }
    
    /**
     * Update admin details including profile picture
     * @param admin
     * @return true if update successful, false otherwise
     */
    public boolean updateAdmin(Admin admin) throws SQLException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        boolean result = false;
        
        String sql = "UPDATE admin SET adminName = ?, adminNoPhone = ?, adminEmail = ?, profile_picture_path = ? " +
                     "WHERE adminId = ?";
        
        try {
            connection = ConnectionManager.getConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, admin.getAdminName());
            preparedStatement.setString(2, admin.getAdminNoPhone());
            preparedStatement.setString(3, admin.getAdminEmail());
         // If profile picture path is null or empty, set to default
            String profilePicturePath = admin.getProfile_picture_path();
            if (profilePicturePath == null || profilePicturePath.trim().isEmpty()) {
                profilePicturePath = "defaultprofilepicture.jpg";
                System.out.println("✅ Setting to default: defaultprofilepicture.jpg");
            }

            preparedStatement.setString(4, profilePicturePath);
            preparedStatement.setString(5, admin.getAdminId());
            
            int rowsAffected = preparedStatement.executeUpdate();
            result = rowsAffected > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        } finally {
            closeResources(connection, preparedStatement, null);
        }
        
        return result;
    }
    
    /**
     * Get all admins
     * @return List of Admin objects
     */
    public List<Admin> getAllAdmins() throws SQLException {
        List<Admin> adminList = new ArrayList<>();
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        
        String sql = "SELECT adminId, adminPassword, adminName, adminNoPhone, adminEmail, profile_picture_path " +
                     "FROM admin ORDER BY adminId";
        
        try {
            connection = ConnectionManager.getConnection();
            preparedStatement = connection.prepareStatement(sql);
            resultSet = preparedStatement.executeQuery();
            
            while (resultSet.next()) {
                Admin admin = new Admin();
                admin.setAdminId(resultSet.getString("adminId"));
                admin.setAdminPassword(resultSet.getString("adminPassword"));
                admin.setAdminName(resultSet.getString("adminName"));
                admin.setAdminNoPhone(resultSet.getString("adminNoPhone"));
                admin.setAdminEmail(resultSet.getString("adminEmail"));
                admin.setProfile_picture_path(resultSet.getString("profile_picture_path"));
                adminList.add(admin);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        } finally {
            closeResources(connection, preparedStatement, resultSet);
        }
        
        return adminList;
    }
    
    /**
     * Update admin profile picture path
     * @param adminId
     * @param profilePicturePath
     * @return true if update successful, false otherwise
     */
    public boolean updateAdminProfilePicture(String adminId, String profilePicturePath) throws SQLException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        boolean result = false;
        
        String sql = "UPDATE admin SET profile_picture_path = ? WHERE adminId = ?";
        
        try {
            connection = ConnectionManager.getConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, profilePicturePath);
            preparedStatement.setString(2, adminId);
            
            int rowsAffected = preparedStatement.executeUpdate();
            result = rowsAffected > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        } finally {
            closeResources(connection, preparedStatement, null);
        }
        
        return result;
    }
    /**
     * Delete admin by adminId
     * @param adminId
     * @return true if deletion successful, false otherwise
     */
    public boolean deleteAdmin(String adminId) throws SQLException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        boolean result = false;
        
        String sql = "DELETE FROM admin WHERE adminId = ?";
        
        try {
            connection = ConnectionManager.getConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, adminId);
            
            int rowsAffected = preparedStatement.executeUpdate();
            result = rowsAffected > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        } finally {
            closeResources(connection, preparedStatement, null);
        }
        
        return result;
    }
    
    /**
     * Test database connection
     * @return true if connection successful, false otherwise
     */
    public boolean testConnection() {
        Connection connection = null;
        try {
            connection = ConnectionManager.getConnection();
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (connection != null) {
                try { 
                    connection.close(); 
                } catch (SQLException e) { 
                    e.printStackTrace(); 
                }
            }
        }
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
     * Get admin by email (for forgot password functionality)
     * @param email
     * @return Admin object if found, null otherwise
     */
    public Admin getAdminByEmail(String email) throws SQLException {
        Admin admin = null;
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        
        String sql = "SELECT adminId, adminPassword, adminName, adminNoPhone, adminEmail, profile_picture_path " +
                     "FROM admin WHERE LOWER(adminEmail) = LOWER(?)";
        
        try {
            connection = ConnectionManager.getConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, email);
            
            resultSet = preparedStatement.executeQuery();
            
            if (resultSet.next()) {
                admin = new Admin();
                admin.setAdminId(resultSet.getString("adminId"));
                admin.setAdminPassword(resultSet.getString("adminPassword"));
                admin.setAdminName(resultSet.getString("adminName"));
                admin.setAdminNoPhone(resultSet.getString("adminNoPhone"));
                admin.setAdminEmail(resultSet.getString("adminEmail"));
                admin.setProfile_picture_path(resultSet.getString("profile_picture_path"));
                
                System.out.println("Admin found by email: " + admin.getAdminName());
            } else {
                System.out.println("No admin found with email: " + email);
            }
            
        } catch (SQLException e) {
            System.err.println("Database error during admin email lookup: " + e.getMessage());
            e.printStackTrace();
            throw e;
        } finally {
            closeResources(connection, preparedStatement, resultSet);
        }
        
        return admin;
    }
    
 // Add these methods to your AdminDAO class

    /**
     * Get count of leave types that have NULL adminId (orphaned leave types)
     * @return number of orphaned leave types
     */
    public int getOrphanedLeaveTypesCount() throws SQLException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        
        String sql = "SELECT COUNT(*) FROM leavetype WHERE adminId IS NULL";
        
        try {
            connection = ConnectionManager.getConnection();
            preparedStatement = connection.prepareStatement(sql);
            resultSet = preparedStatement.executeQuery();
            
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        } finally {
            closeResources(connection, preparedStatement, resultSet);
        }
        
        return 0;
    }

    /**
     * Assign orphaned leave types to a specific admin
     * @param targetAdminId Admin ID to assign orphaned leave types to
     * @return number of leave types reassigned
     */
    public int assignOrphanedLeaveTypes(String targetAdminId) throws SQLException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        
        String sql = "UPDATE leavetype SET adminId = ? WHERE adminId IS NULL";
        
        try {
            connection = ConnectionManager.getConnection();
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, targetAdminId);
            
            int rowsUpdated = preparedStatement.executeUpdate();
            
            if (rowsUpdated > 0) {
                System.out.println("✅ Assigned " + rowsUpdated + " orphaned leave types to admin: " + targetAdminId);
            }
            
            return rowsUpdated;
            
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        } finally {
            closeResources(connection, preparedStatement, null);
        }
    }

    /**
     * Get all orphaned leave types (adminId is NULL)
     * @return List of orphaned leave types
     */
    public List<LeaveType> getOrphanedLeaveTypes() throws SQLException {
        List<LeaveType> leaveTypes = new ArrayList<>();
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        
        String sql = "SELECT leaveTypeId, adminId, leaveTypeCategory, leaveTypeName, leaveTypeDescription " +
                     "FROM leavetype WHERE adminId IS NULL ORDER BY leaveTypeName";
        
        try {
            connection = ConnectionManager.getConnection();
            preparedStatement = connection.prepareStatement(sql);
            resultSet = preparedStatement.executeQuery();
            
            while (resultSet.next()) {
                LeaveType leaveType = new LeaveType();
                leaveType.setLeaveTypeId(resultSet.getString("leaveTypeId"));
                leaveType.setAdminId(resultSet.getString("adminId")); // Will be null
                leaveType.setLeaveTypeCategory(resultSet.getString("leaveTypeCategory"));
                leaveType.setLeaveTypeName(resultSet.getString("leaveTypeName"));
                leaveType.setLeaveTypeDescription(resultSet.getString("leaveTypeDescription"));
                
                leaveTypes.add(leaveType);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        } finally {
            closeResources(connection, preparedStatement, resultSet);
        }
        
        return leaveTypes;
    }
    
    /**
     * Close database resources
     * @param connection
     * @param preparedStatement
     * @param resultSet
     */
    private void closeResources(Connection connection, PreparedStatement preparedStatement, ResultSet resultSet) {
        try {
            if (resultSet != null) {
                resultSet.close();
            }
            if (preparedStatement != null) {
                preparedStatement.close();
            }
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
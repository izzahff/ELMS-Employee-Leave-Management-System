package elms.DAO;

import elms.model.Manager;
import elms.connection.ConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ManagerDAO {
    
	// Get all managers
	public List<Manager> getAllManagers() {
	    List<Manager> managers = new ArrayList<>();
	    String sql = "SELECT managerid, managername, managerpassword, managernophone, manageremail, managerposition, profile_picture_path " +
	                 "FROM manager ORDER BY managername ASC";
	    
	    try (Connection connection = ConnectionManager.getConnection();
	         PreparedStatement statement = connection.prepareStatement(sql);
	         ResultSet resultSet = statement.executeQuery()) {
	        
	        while (resultSet.next()) {
	            Manager manager = new Manager();
	            manager.setManagerid(resultSet.getString("managerid"));
	            manager.setManagername(resultSet.getString("managername"));
	            manager.setManagerpassword(resultSet.getString("managerpassword"));
	            manager.setManagernophone(resultSet.getString("managernophone"));
	            manager.setManageremail(resultSet.getString("manageremail"));
	            manager.setManagerposition(resultSet.getString("managerposition"));
	            manager.setProfilePicturePath(resultSet.getString("profile_picture_path")); 
	            managers.add(manager);
	        }
	        System.out.println("Found " + managers.size() + " managers in database");
	    } catch (SQLException e) {
	        e.printStackTrace();
	        System.out.println("ERROR in getAllManagers: " + e.getMessage());
	    }
	    
	    return managers;
	}
    
	public String addManagerAndReturnId(Manager manager) {
	    // Set default profile picture path for new managers
	    String defaultProfilePicture = "defaultprofilepicture.jpg";
	    
	    String sql = "INSERT INTO manager (managername, managerpassword, " +
	                "managernophone, manageremail, managerposition, profile_picture_path) " +
	                "VALUES (?, ?, ?, ?, ?, ?)";
	    
	    try (Connection connection = ConnectionManager.getConnection();
	         PreparedStatement statement = connection.prepareStatement(sql)) {
	        
	        statement.setString(1, manager.getManagername());
	        statement.setString(2, manager.getManagerpassword());
	        statement.setString(3, manager.getManagernophone());
	        statement.setString(4, manager.getManageremail());
	        statement.setString(5, manager.getManagerposition());
	        statement.setString(6, defaultProfilePicture); 
	        System.out.println("Creating manager with default profile picture: " + defaultProfilePicture);
	        
	        int rowsAffected = statement.executeUpdate();
	        
	        if (rowsAffected > 0) {
	           
	            String selectSql = "SELECT managerid FROM manager WHERE manageremail = ?";
	            try (PreparedStatement selectStmt = connection.prepareStatement(selectSql)) {
	                selectStmt.setString(1, manager.getManageremail());
	                ResultSet rs = selectStmt.executeQuery();
	                if (rs.next()) {
	                    String generatedManagerId = rs.getString("managerid");
	                    System.out.println("✅ Manager created successfully with ID: " + generatedManagerId);
	                    System.out.println("✅ Default profile picture set: " + defaultProfilePicture);
	                    return generatedManagerId;
	                }
	            }
	        }
	        
	    } catch (SQLException e) {
	        System.err.println("❌ Error creating manager: " + e.getMessage());
	        e.printStackTrace();
	    }
	    
	    return null;
	}
    
    // Update manager
    public boolean updateManager(Manager manager) {
        String sql = "UPDATE manager SET managername = ?, managerpassword = ?, " +
                    "managernophone = ?, manageremail = ?, managerposition = ?, " +
                    "profile_picture_path = ? WHERE managerid = ?";
        
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setString(1, manager.getManagername());
            statement.setString(2, manager.getManagerpassword());
            statement.setString(3, manager.getManagernophone());
            statement.setString(4, manager.getManageremail());
            statement.setString(5, manager.getManagerposition());
            statement.setString(6, manager.getProfilePicturePath());
            statement.setString(7, manager.getManagerid());
            
            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Update manager without password
    public boolean updateManagerWithoutPassword(Manager manager) {
        String sql = "UPDATE manager SET managername = ?, managernophone = ?, " +
                    "manageremail = ?, managerposition = ?, " +
                    "profile_picture_path = ? WHERE managerid = ?";
        
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setString(1, manager.getManagername());
            statement.setString(2, manager.getManagernophone());
            statement.setString(3, manager.getManageremail());
            statement.setString(4, manager.getManagerposition());
            statement.setString(5, manager.getProfilePicturePath());
            statement.setString(6, manager.getManagerid());
            
            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Delete manager
    public boolean deleteManager(String managerId) {
        String sql = "DELETE FROM manager WHERE managerid = ?";
        
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setString(1, managerId);
            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Get manager by ID
    public Manager getManagerById(String managerId) {
        Manager manager = null;
        String sql = "SELECT * FROM manager WHERE managerid = ?";
        
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setString(1, managerId);
            ResultSet resultSet = statement.executeQuery();
            
            if (resultSet.next()) {
                manager = new Manager();
                manager.setManagerid(resultSet.getString("managerid"));
                manager.setManagername(resultSet.getString("managername"));
                manager.setManagerpassword(resultSet.getString("managerpassword"));
                manager.setManagernophone(resultSet.getString("managernophone"));
                manager.setManageremail(resultSet.getString("manageremail"));
                manager.setManagerposition(resultSet.getString("managerposition"));
                manager.setProfilePicturePath(resultSet.getString("profile_picture_path"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return manager;
    }
    
    // NEW METHOD: Get manager name by ID (lightweight version)
    public String getManagerNameById(String managerId) throws SQLException {
        System.out.println("=== Getting Manager Name by ID: " + managerId + " ===");
        
        String managerName = null;
        String sql = "SELECT managername FROM manager WHERE managerid = ?";
        
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setString(1, managerId);
            ResultSet resultSet = statement.executeQuery();
            
            if (resultSet.next()) {
                managerName = resultSet.getString("managername");
                System.out.println("✅ Manager found: " + managerName);
            } else {
                System.out.println("❌ No manager found with ID: " + managerId);
            }
            
        } catch (SQLException e) {
            System.err.println("❌ SQL Error in getManagerNameById: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
        
        return managerName;
    }
    
    // Get manager by email
    public Manager getManagerByEmail(String email) {
        Manager manager = null;
        String sql = "SELECT * FROM manager WHERE manageremail = ?";
        
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setString(1, email);
            ResultSet resultSet = statement.executeQuery();
            
            if (resultSet.next()) {
                manager = new Manager();
                manager.setManagerid(resultSet.getString("managerid"));
                manager.setManagername(resultSet.getString("managername"));
                manager.setManagerpassword(resultSet.getString("managerpassword"));
                manager.setManagernophone(resultSet.getString("managernophone"));
                manager.setManageremail(resultSet.getString("manageremail"));
                manager.setManagerposition(resultSet.getString("managerposition"));
                manager.setProfilePicturePath(resultSet.getString("profile_picture_path"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return manager;
    }
    
    // Check if manager ID exists
    public boolean managerIdExists(String managerId) {
        String sql = "SELECT COUNT(*) FROM manager WHERE managerid = ?";
        
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setString(1, managerId);
            ResultSet resultSet = statement.executeQuery();
            
            if (resultSet.next()) {
                return resultSet.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    // Check if manager email exists
    public boolean managerEmailExists(String email) {
        String sql = "SELECT COUNT(*) FROM manager WHERE manageremail = ?";
        
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setString(1, email);
            ResultSet resultSet = statement.executeQuery();
            
            if (resultSet.next()) {
                return resultSet.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    // Check if manager email exists for different manager ID (for updates)
    public boolean managerEmailExistsForDifferentId(String email, String managerId) {
        String sql = "SELECT COUNT(*) FROM manager WHERE manageremail = ? AND managerid != ?";
        
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setString(1, email);
            statement.setString(2, managerId);
            ResultSet resultSet = statement.executeQuery();
            
            if (resultSet.next()) {
                return resultSet.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return false;
    }
    
    // Get total count of managers
    public int getTotalManagerCount() {
        String sql = "SELECT COUNT(*) FROM manager";
        
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return 0;
    }
    
    // Get managers with pagination - FIXED for Oracle
 // Get managers with pagination - FIXED for Oracle
    public List<Manager> getManagersWithPagination(int offset, int limit) {
        List<Manager> managers = new ArrayList<>();
        
        // Oracle pagination syntax
        String sql = "SELECT * FROM (" +
                     "SELECT ROW_NUMBER() OVER (ORDER BY managername ASC) AS rn, " +
                     "managerid, managername, managerpassword, managernophone, manageremail, managerposition, profile_picture_path " +
                     "FROM manager" +
                     ") WHERE rn > ? AND rn <= ?";
        
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setInt(1, offset);
            statement.setInt(2, offset + limit);
            ResultSet resultSet = statement.executeQuery();
            
            while (resultSet.next()) {
                Manager manager = new Manager();
                manager.setManagerid(resultSet.getString("managerid"));
                manager.setManagername(resultSet.getString("managername"));
                manager.setManagerpassword(resultSet.getString("managerpassword"));
                manager.setManagernophone(resultSet.getString("managernophone"));
                manager.setManageremail(resultSet.getString("manageremail"));
                manager.setManagerposition(resultSet.getString("managerposition"));
                manager.setProfilePicturePath(resultSet.getString("profile_picture_path")); // ADD THIS LINE
                managers.add(manager);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("ERROR in getManagersWithPagination: " + e.getMessage());
        }
        
        return managers;
    }
    
    // Validate manager login
    public Manager validateManagerLogin(String email, String password) {
        Manager manager = null;
        String sql = "SELECT * FROM manager WHERE manageremail = ? AND managerpassword = ?";
        
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setString(1, email);
            statement.setString(2, password);
            ResultSet resultSet = statement.executeQuery();
            
            if (resultSet.next()) {
                manager = new Manager();
                manager.setManagerid(resultSet.getString("managerid"));
                manager.setManagername(resultSet.getString("managername"));
                manager.setManagerpassword(resultSet.getString("managerpassword"));
                manager.setManagernophone(resultSet.getString("managernophone"));
                manager.setManageremail(resultSet.getString("manageremail"));
                manager.setManagerposition(resultSet.getString("managerposition"));
                manager.setProfilePicturePath(resultSet.getString("profile_picture_path"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return manager;
    }
    
    // Get managers by position (renamed from getManagersByRole)
    public List<Manager> getManagersByPosition(String position) {
        List<Manager> managers = new ArrayList<>();
        String sql = "SELECT * FROM manager WHERE managerposition = ? ORDER BY managername ASC";
        
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setString(1, position);
            ResultSet resultSet = statement.executeQuery();
            
            while (resultSet.next()) {
                Manager manager = new Manager();
                manager.setManagerid(resultSet.getString("managerid"));
                manager.setManagername(resultSet.getString("managername"));
                manager.setManagerpassword(resultSet.getString("managerpassword"));
                manager.setManagernophone(resultSet.getString("managernophone"));
                manager.setManageremail(resultSet.getString("manageremail"));
                manager.setManagerposition(resultSet.getString("managerposition"));     
                manager.setProfilePicturePath(resultSet.getString("profile_picture_path"));
                managers.add(manager);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return managers;
    }
    
    // Get count of managers by position (renamed from getManagerCountByRole)
    public int getManagerCountByPosition(String position) {
        String sql = "SELECT COUNT(*) FROM manager WHERE managerposition = ?";
        
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setString(1, position);
            ResultSet resultSet = statement.executeQuery();
            
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return 0;
    }
    
    // Search managers by name, email, or position
 // Search managers by name, email, or position
    public List<Manager> searchManagers(String searchTerm) {
        List<Manager> managers = new ArrayList<>();
        String sql = "SELECT managerid, managername, managerpassword, managernophone, manageremail, managerposition, profile_picture_path " +
                     "FROM manager WHERE " +
                     "LOWER(managername) LIKE ? OR " +
                     "LOWER(manageremail) LIKE ? OR " +
                     "LOWER(managerposition) LIKE ? OR " +
                     "LOWER(managerid) LIKE ? " +
                     "ORDER BY managername ASC";
        
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            String searchPattern = "%" + searchTerm.toLowerCase() + "%";
            statement.setString(1, searchPattern);
            statement.setString(2, searchPattern);
            statement.setString(3, searchPattern);
            statement.setString(4, searchPattern);
            
            ResultSet resultSet = statement.executeQuery();
            
            while (resultSet.next()) {
                Manager manager = new Manager();
                manager.setManagerid(resultSet.getString("managerid"));
                manager.setManagername(resultSet.getString("managername"));
                manager.setManagerpassword(resultSet.getString("managerpassword"));
                manager.setManagernophone(resultSet.getString("managernophone"));
                manager.setManageremail(resultSet.getString("manageremail"));
                manager.setManagerposition(resultSet.getString("managerposition"));
                manager.setProfilePicturePath(resultSet.getString("profile_picture_path")); // ADD THIS LINE
                managers.add(manager);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("ERROR in searchManagers: " + e.getMessage());
        }
        
        return managers;
    }

    // NEW METHOD: Get basic manager info for leave application reviews
    public Manager getManagerForLeaveReview(String managerId) {
        System.out.println("=== Getting Manager Info for Leave Review: " + managerId + " ===");
        
        Manager manager = null;
        String sql = "SELECT managerid, managername, managerposition FROM manager WHERE managerid = ?";
        
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setString(1, managerId);
            ResultSet resultSet = statement.executeQuery();
            
            if (resultSet.next()) {
                manager = new Manager();
                manager.setManagerid(resultSet.getString("managerid"));
                manager.setManagername(resultSet.getString("managername"));
                manager.setManagerposition(resultSet.getString("managerposition"));
                
                System.out.println("✅ Manager found for review: " + manager.getManagername() + 
                                 " (" + manager.getManagerposition() + ")");
            } else {
                System.out.println("❌ No manager found with ID: " + managerId);
            }
            
        } catch (SQLException e) {
            System.err.println("❌ SQL Error in getManagerForLeaveReview: " + e.getMessage());
            e.printStackTrace();
        }
        
        return manager;
    }
    
    /**
     * Delete manager account and all related data
     * This method handles the complete deletion of a manager account
     * @param managerId The manager ID to delete
     * @return true if deletion successful, false otherwise
     */
    public boolean deleteManagerAccount(String managerId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = ConnectionManager.getConnection();
            if (conn == null) {
                return false;
            }
            
            // Start transaction
            conn.setAutoCommit(false);
            
            // Delete related records first (leave application approvals, etc.)
            String deleteApprovals = "DELETE FROM leaveapproval WHERE managerid = ?";
            pstmt = conn.prepareStatement(deleteApprovals);
            pstmt.setString(1, managerId);
            pstmt.executeUpdate();
            pstmt.close();
            
            // Delete any other related records here as needed
            // For example, if there are manager-employee relationships, etc.
            
            // Delete manager record
            String deleteManager = "DELETE FROM manager WHERE managerid = ?";
            pstmt = conn.prepareStatement(deleteManager);
            pstmt.setString(1, managerId);
            int rowsDeleted = pstmt.executeUpdate();
            
            if (rowsDeleted > 0) {
                conn.commit();
                System.out.println("Manager account deleted successfully: " + managerId);
                return true;
            } else {
                conn.rollback();
                return false;
            }
            
        } catch (SQLException e) {
            System.err.println("Database error during account deletion: " + e.getMessage());
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException rollbackEx) {
                System.err.println("Error during rollback: " + rollbackEx.getMessage());
            }
            return false;
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
    }
    
    /**
     * Verify manager password (for Executive Director authentication)
     * @param managerId
     * @param inputPassword
     * @return true if password matches, false otherwise
     */
    public boolean verifyManagerPassword(String managerId, String inputPassword) throws SQLException {
        System.out.println("=== Verifying Manager Password for ID: " + managerId + " ===");
        
        String sql = "SELECT managerpassword FROM manager WHERE managerid = ?";
        
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setString(1, managerId);
            ResultSet resultSet = statement.executeQuery();
            
            if (resultSet.next()) {
                String storedHashedPassword = resultSet.getString("managerpassword");
                String inputHashedPassword = hashPassword(inputPassword);
                
                boolean passwordMatch = inputHashedPassword.equals(storedHashedPassword);
                System.out.println("Password verification result: " + passwordMatch);
                return passwordMatch;
            } else {
                System.out.println("❌ Manager not found with ID: " + managerId);
                return false;
            }
        } catch (SQLException e) {
            System.err.println("❌ SQL Error in verifyManagerPassword: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Hash password using SHA-256 (consistent with AdminDAO)
     * @param password
     * @return hashed password
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
     * Get project managers under Executive Director supervision
     * @return List of project managers
     */
    public List<Manager> getProjectManagers() throws SQLException {
        System.out.println("=== Getting Project Managers ===");
        
        List<Manager> projectManagers = new ArrayList<>();
        String sql = "SELECT managerid, managername, manageremail, managernophone, " +
                    "managerposition, profile_picture_path " +
                    "FROM manager WHERE UPPER(managerid) LIKE 'PM%' " +
                    "ORDER BY managername ASC";
        
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            
            while (resultSet.next()) {
                Manager manager = new Manager();
                manager.setManagerid(resultSet.getString("managerid"));
                manager.setManagername(resultSet.getString("managername"));
                manager.setManageremail(resultSet.getString("manageremail"));
                manager.setManagernophone(resultSet.getString("managernophone"));
                manager.setManagerposition(resultSet.getString("managerposition"));
                manager.setProfilePicturePath(resultSet.getString("profile_picture_path"));
                projectManagers.add(manager);
            }
            
            System.out.println("✅ Found " + projectManagers.size() + " project managers");
            
        } catch (SQLException e) {
            System.err.println("❌ SQL Error in getProjectManagers: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
        
        return projectManagers;
    }

    /**
     * Delete project manager (only allows deletion of Project Managers)
     * @param managerId
     * @return true if deletion successful, false otherwise
     */
    public boolean deleteProjectManager(String managerId) throws SQLException {
        System.out.println("=== Deleting Project Manager: " + managerId + " ===");
        
        // Verify it's a Project Manager before deletion
        if (!managerId.toUpperCase().startsWith("PM")) {
            System.out.println("❌ Attempt to delete non-project manager: " + managerId);
            throw new SQLException("Only Project Managers can be deleted through this method");
        }
        
        String sql = "DELETE FROM manager WHERE managerid = ? AND UPPER(managerid) LIKE 'PM%'";
        
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setString(1, managerId);
            
            int rowsAffected = statement.executeUpdate();
            boolean success = rowsAffected > 0;
            
            if (success) {
                System.out.println("✅ Project Manager deleted successfully: " + managerId);
            } else {
                System.out.println("❌ Failed to delete Project Manager: " + managerId);
            }
            
            return success;
            
        } catch (SQLException e) {
            System.err.println("❌ SQL Error in deleteProjectManager: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Count dependent leave applications for a project manager
     * @param managerId
     * @return number of dependent leave applications
     */
    public int countDependentLeaveApplications(String managerId) throws SQLException {
        System.out.println("=== Counting Dependent Leave Applications for Manager: " + managerId + " ===");
        
        String sql = "SELECT COUNT(*) FROM leaveapplication WHERE managerid = ?";
        
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setString(1, managerId);
            ResultSet resultSet = statement.executeQuery();
            
            if (resultSet.next()) {
                int count = resultSet.getInt(1);
                System.out.println("✅ Found " + count + " dependent leave applications");
                return count;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ SQL Error in countDependentLeaveApplications: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
        
        return 0;
    }

    /**
     * Update manager password with SHA-256 hashing
     * @param managerId
     * @param newPassword
     * @return true if update successful, false otherwise
     */
    public boolean updateManagerPassword(String managerId, String newPassword) throws SQLException {
        System.out.println("=== Updating Manager Password for ID: " + managerId + " ===");
        
        String sql = "UPDATE manager SET managerpassword = ? WHERE managerid = ?";
        
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            String hashedPassword = hashPassword(newPassword);
            statement.setString(1, hashedPassword);
            statement.setString(2, managerId);
            
            int rowsAffected = statement.executeUpdate();
            boolean success = rowsAffected > 0;
            
            if (success) {
                System.out.println("✅ Manager password updated successfully");
            } else {
                System.out.println("❌ Failed to update manager password");
            }
            
            return success;
            
        } catch (SQLException e) {
            System.err.println("❌ SQL Error in updateManagerPassword: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Get Executive Directors (for administrative purposes)
     * @return List of Executive Directors
     */
    public List<Manager> getExecutiveDirectors() throws SQLException {
        System.out.println("=== Getting Executive Directors ===");
        
        List<Manager> executiveDirectors = new ArrayList<>();
        String sql = "SELECT managerid, managername, manageremail, managernophone, " +
                    "managerposition, profile_picture_path " +
                    "FROM manager WHERE UPPER(managerposition) = 'EXECUTIVE DIRECTOR' " +
                    "ORDER BY managername ASC";
        
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            
            while (resultSet.next()) {
                Manager manager = new Manager();
                manager.setManagerid(resultSet.getString("managerid"));
                manager.setManagername(resultSet.getString("managername"));
                manager.setManageremail(resultSet.getString("manageremail"));
                manager.setManagernophone(resultSet.getString("managernophone"));
                manager.setManagerposition(resultSet.getString("managerposition"));
                manager.setProfilePicturePath(resultSet.getString("profile_picture_path"));
                executiveDirectors.add(manager);
            }
            
            System.out.println("✅ Found " + executiveDirectors.size() + " executive directors");
            
        } catch (SQLException e) {
            System.err.println("❌ SQL Error in getExecutiveDirectors: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
        
        return executiveDirectors;
    }

    /**
     * Check if manager has authority to delete other managers
     * (Currently only Executive Directors can delete Project Managers)
     * @param managerId
     * @param targetManagerId
     * @return true if authorized, false otherwise
     */
    public boolean hasDeleteAuthority(String managerId, String targetManagerId) throws SQLException {
        System.out.println("=== Checking Delete Authority: " + managerId + " -> " + targetManagerId + " ===");
        
        // Get the requesting manager's position
        String sql = "SELECT managerposition FROM manager WHERE managerid = ?";
        
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setString(1, managerId);
            ResultSet resultSet = statement.executeQuery();
            
            if (resultSet.next()) {
                String position = resultSet.getString("managerposition");
                
                // Only Executive Directors can delete other managers
                if ("Executive Director".equalsIgnoreCase(position)) {
                    // Executive Directors can only delete Project Managers
                    if (targetManagerId.toUpperCase().startsWith("PM")) {
                        System.out.println("✅ Executive Director authorized to delete Project Manager");
                        return true;
                    } else {
                        System.out.println("❌ Executive Director cannot delete non-Project Manager");
                        return false;
                    }
                } else {
                    System.out.println("❌ Only Executive Directors can delete other managers");
                    return false;
                }
            } else {
                System.out.println("❌ Requesting manager not found: " + managerId);
                return false;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ SQL Error in hasDeleteAuthority: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Validate manager credentials with SHA-256 hashing
     * Enhanced version of existing validateManagerLogin with proper hashing
     * @param email
     * @param password
     * @return Manager object if valid, null otherwise
     */
    public Manager validateManagerLoginWithHash(String email, String password) throws SQLException {
        System.out.println("=== Validating Manager Login with Hash for: " + email + " ===");
        
        Manager manager = null;
        String sql = "SELECT managerid, managername, managerpassword, managernophone, " +
                    "manageremail, managerposition, profile_picture_path " +
                    "FROM manager WHERE manageremail = ?";
        
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setString(1, email);
            ResultSet resultSet = statement.executeQuery();
            
            if (resultSet.next()) {
                String storedHashedPassword = resultSet.getString("managerpassword");
                String inputHashedPassword = hashPassword(password);
                
                if (inputHashedPassword.equals(storedHashedPassword)) {
                    manager = new Manager();
                    manager.setManagerid(resultSet.getString("managerid"));
                    manager.setManagername(resultSet.getString("managername"));
                    manager.setManagerpassword(storedHashedPassword); // Keep hashed
                    manager.setManagernophone(resultSet.getString("managernophone"));
                    manager.setManageremail(resultSet.getString("manageremail"));
                    manager.setManagerposition(resultSet.getString("managerposition"));
                    manager.setProfilePicturePath(resultSet.getString("profile_picture_path"));
                    
                    System.out.println("✅ Manager login successful: " + manager.getManagername());
                } else {
                    System.out.println("❌ Invalid password for manager: " + email);
                }
            } else {
                System.out.println("❌ Manager not found with email: " + email);
            }
            
        } catch (SQLException e) {
            System.err.println("❌ SQL Error in validateManagerLoginWithHash: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
        
        return manager;
    }
    
    public boolean deleteManagerAccountSafe(String managerId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = ConnectionManager.getConnection();
            if (conn == null) {
                System.err.println("❌ Failed to get database connection");
                return false;
            }
            
            System.out.println("🔄 Starting safe deletion process for manager ID: " + managerId);
            
            // Start transaction
            conn.setAutoCommit(false);
            
            // Step 1: Check for dependent leave applications
            int leaveAppCount = countDependentLeaveApplications(managerId);
            System.out.println("📊 Found " + leaveAppCount + " dependent leave applications");
            
            if (leaveAppCount > 0) {
                // Preserve historical data by updating leave applications to reference system manager
                // Use leavereason field to store historical info (this field exists in your table)
                String updateLeaveApps = "UPDATE leaveapplication SET " +
                    "managerid = 'SYSTEM_MGR', " +
                    "leavereason = COALESCE(leavereason, '') || " +
                    "CASE WHEN COALESCE(leavereason, '') != '' THEN ' ' ELSE '' END || " +
                    "'[Originally processed by Manager ID: ' || ? || ']' " +
                    "WHERE managerid = ?";
                    
                pstmt = conn.prepareStatement(updateLeaveApps);
                pstmt.setString(1, managerId);
                pstmt.setString(2, managerId);
                int leaveAppsUpdated = pstmt.executeUpdate();
                System.out.println("✅ Preserved history for " + leaveAppsUpdated + " leave applications");
                pstmt.close();
            }
            
            // Step 2: No employee table updates needed (employee table has no managerid column)
            System.out.println("ℹ️ Employee table has no managerid column - no updates needed");
            
            // Step 3: Delete manager record
            String deleteManager = "DELETE FROM manager WHERE managerid = ?";
            pstmt = conn.prepareStatement(deleteManager);
            pstmt.setString(1, managerId);
            int rowsDeleted = pstmt.executeUpdate();
            
            if (rowsDeleted > 0) {
                conn.commit();
                System.out.println("✅ Manager account safely deleted: " + managerId);
                return true;
            } else {
                conn.rollback();
                System.err.println("❌ No manager record found to delete for ID: " + managerId);
                return false;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Database error during safe account deletion: " + e.getMessage());
            e.printStackTrace();
            
            try {
                if (conn != null) {
                    conn.rollback();
                    System.out.println("🔄 Transaction rolled back");
                }
            } catch (SQLException rollbackEx) {
                System.err.println("❌ Error during rollback: " + rollbackEx.getMessage());
            }
            return false;
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                System.err.println("❌ Error closing resources: " + e.getMessage());
            }
        }
    }


    /**
     * Check if manager can be deleted without affecting data integrity
     * @param managerId Manager ID to check
     * @return true if safe to delete, false if has dependencies
     */
    public boolean canManagerBeDeletedSafely(String managerId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM leaveapplication WHERE managerid = ?";
        
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setString(1, managerId);
            ResultSet resultSet = statement.executeQuery();
            
            if (resultSet.next()) {
                int count = resultSet.getInt(1);
                System.out.println("Manager " + managerId + " has " + count + " dependent leave applications");
                return count == 0;
            }
            
        } catch (SQLException e) {
            System.err.println("Error checking manager dependencies: " + e.getMessage());
            throw e;
        }
        
        return true;
    }

    /**
     * Create system manager entry if it doesn't exist (run once)
     */
    public void ensureSystemManagerExists() {
        System.out.println("=== Ensuring System Manager Exists ===");
        
        String checkSql = "SELECT COUNT(*) FROM manager WHERE managerid = 'SYSTEM_MGR'";
        String insertSql = "INSERT INTO manager (managerid, managername, managerpassword, " +
                          "manageremail, managerposition, managernophone, profile_picture_path) " +
                          "VALUES ('SYSTEM_MGR', 'System Manager (Historical)', ?, " +
                          "'system@company.com', 'System Account', 'N/A', 'defaultprofilepicture.jpg')";
        
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement checkStmt = connection.prepareStatement(checkSql)) {
            
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt(1) == 0) {
                // System manager doesn't exist, create it
                System.out.println("🔄 Creating system manager for historical records...");
                
                try (PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {
                    // Use a hashed dummy password
                    insertStmt.setString(1, hashPassword("SYSTEM_ACCOUNT_NO_LOGIN"));
                    
                    int rowsInserted = insertStmt.executeUpdate();
                    if (rowsInserted > 0) {
                        System.out.println("✅ System manager created for historical records");
                    } else {
                        System.out.println("❌ Failed to create system manager");
                    }
                }
            } else {
                System.out.println("✅ System manager already exists");
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error ensuring system manager exists: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Get manager name for leave applications (handles historical records)
     * @param managerId Manager ID
     * @return Manager name or placeholder for historical records
     */
    public String getManagerNameForLeaveDisplay(String managerId) throws SQLException {
        if ("SYSTEM_MGR".equals(managerId)) {
            return "Former Manager (Historical)";
        }
        
        String sql = "SELECT managername FROM manager WHERE managerid = ?";
        
        try (Connection connection = ConnectionManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setString(1, managerId);
            ResultSet resultSet = statement.executeQuery();
            
            if (resultSet.next()) {
                return resultSet.getString("managername");
            } else {
                // Manager not found, return placeholder
                return "Former Manager (" + managerId + ")";
            }
            
        } catch (SQLException e) {
            System.err.println("Error getting manager name for display: " + e.getMessage());
            throw e;
        }
    }

    // 3. USAGE IN YOUR APPLICATION

    // Before allowing manager deletion, check dependencies:
    public String getManagerDeletionWarning(String managerId) {
        try {
            ManagerDAO managerDAO = new ManagerDAO();
            int leaveAppCount = managerDAO.countDependentLeaveApplications(managerId);
            
            if (leaveAppCount > 0) {
                return "This manager has " + leaveAppCount + " leave applications. " +
                       "Historical approval data will be preserved but transferred to a system account.";
            } else {
                return "This manager has no dependent records and can be safely deleted.";
            }
        } catch (SQLException e) {
            return "Unable to check dependencies. Please contact support.";
        }
    }

    
    // Deprecated methods for backward compatibility
    
    /**
     * @deprecated Use getManagersByPosition() instead
     */
    @Deprecated
    public List<Manager> getManagersByRole(String role) {
        return getManagersByPosition(role);
    }
    
    /**
     * @deprecated Use getManagerCountByPosition() instead
     */
    @Deprecated
    public int getManagerCountByRole(String role) {
        return getManagerCountByPosition(role);
    }
}
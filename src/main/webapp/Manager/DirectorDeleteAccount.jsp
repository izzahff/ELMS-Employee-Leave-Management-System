<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="elms.model.Manager" %>
<%@ page import="elms.DAO.ManagerDAO" %>
<%@ page import="java.sql.*" %>
<%@ page import="java.security.MessageDigest" %>
<%@ page import="java.security.NoSuchAlgorithmException" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
    // Check if user is logged in
    Manager manager = (Manager) session.getAttribute("manager");
    if (manager == null) {
        response.sendRedirect("/ELMS_3.0/Manager/ManagerLogin.jsp");
        return;
    }

    // Handle form submission
    String deleteResult = "";
    String errorMessage = "";
    
    if ("POST".equals(request.getMethod())) {
        String password = request.getParameter("password");
        String confirmDelete = request.getParameter("confirmDelete");
        
        if (password != null && confirmDelete != null) {
            // Validate password by checking directly against database
            boolean passwordValid = validateManagerPassword(manager.getManagerid(), password);
            
            if (passwordValid) {
                // Password is correct, proceed with account deletion
                try {
                    boolean deleted = deleteDirectorAccount(manager.getManagerid());
                    if (deleted) {
                        // Clear session and redirect to login with success message
                        session.invalidate();
                        response.sendRedirect("/ELMS_3.0/Manager/ManagerLogin.jsp?message=Account deleted successfully");
                        return;
                    } else {
                        errorMessage = "Failed to delete account. Please try again.";
                    }
                } catch (Exception e) {
                    System.err.println("Error deleting director account: " + e.getMessage());
                    errorMessage = "An error occurred while deleting your account. Please contact support.";
                }
            } else {
                errorMessage = "Incorrect password. Please try again.";
            }
        } else {
            errorMessage = "Please provide your password and confirm the deletion.";
        }
    }
%>

<%!
    // Helper method to validate manager password
    private boolean validateManagerPassword(String managerId, String password) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = elms.connection.ConnectionManager.getConnection();
            if (conn == null) {
                System.err.println("❌ Failed to get database connection");
                return false;
            }
            
            System.out.println("🔄 Validating password for manager ID: " + managerId);
            
            String sql = "SELECT managerpassword FROM manager WHERE managerid = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, managerId);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String storedPassword = rs.getString("managerpassword");
                System.out.println("✅ Manager found, comparing passwords...");
                System.out.println("🔍 Stored password length: " + storedPassword.length());
                
                // First try direct comparison (plain text)
                if (password.equals(storedPassword)) {
                    System.out.println("✅ Password match (plain text)");
                    return true;
                }
                
                // If that fails, try hashing the input password (like Employee does)
                String hashedPassword = hashPassword(password);
                System.out.println("🔍 Hashed input password: " + hashedPassword.substring(0, 10) + "...");
                
                if (hashedPassword.equals(storedPassword)) {
                    System.out.println("✅ Password match (hashed)");
                    return true;
                }
                
                System.out.println("❌ No password match found");
                return false;
                
            } else {
                System.err.println("❌ No manager found with ID: " + managerId);
                return false;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Database error during password validation: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("❌ Error closing resources: " + e.getMessage());
            }
        }
    }
    
    // Helper method to hash password (same as Employee DAO)
    private String hashPassword(String password) {
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

    // Helper method to delete director account
    private boolean deleteDirectorAccount(String managerId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = elms.connection.ConnectionManager.getConnection();
            if (conn == null) {
                System.err.println("❌ Failed to get database connection");
                return false;
            }
            
            System.out.println("🔄 Starting deletion process for director ID: " + managerId);
            
            // Start transaction
            conn.setAutoCommit(false);
            
            // Step 1: Check if director has any leave applications
            String checkLeaveApps = "SELECT COUNT(*) FROM leaveapplication WHERE managerid = ?";
            pstmt = conn.prepareStatement(checkLeaveApps);
            pstmt.setString(1, managerId);
            ResultSet rs = pstmt.executeQuery();
            
            int leaveAppCount = 0;
            if (rs.next()) {
                leaveAppCount = rs.getInt(1);
            }
            pstmt.close();
            rs.close();
            
            System.out.println("📊 Found " + leaveAppCount + " leave applications for this director");
            
            if (leaveAppCount > 0) {
                // PRESERVE HISTORICAL DATA: Update leave applications to reference system manager
                // Add historical info to the leavereason field (which exists in your table)
                String updateLeaveApps = "UPDATE leaveapplication SET " +
                    "managerid = 'SYSTEM_MGR', " +
                    "leavereason = COALESCE(leavereason, '') || " +
                    "CASE WHEN COALESCE(leavereason, '') != '' THEN ' ' ELSE '' END || " +
                    "'[Originally processed by Executive Director: ' || ? || ']' " +
                    "WHERE managerid = ?";
                
                pstmt = conn.prepareStatement(updateLeaveApps);
                pstmt.setString(1, managerId);
                pstmt.setString(2, managerId);
                int leaveAppsUpdated = pstmt.executeUpdate();
                System.out.println("✅ Updated " + leaveAppsUpdated + " leave applications to preserve history");
                pstmt.close();
            }
            
            // Step 2: No employee table updates needed (employee table has no managerid column)
            System.out.println("ℹ️ Employee table has no managerid column - skipping employee updates");
            
            // Step 3: Handle project manager relationships (if they exist)
            try {
                String updateProjectManagers = "UPDATE manager SET director_id = NULL WHERE director_id = ?";
                pstmt = conn.prepareStatement(updateProjectManagers);
                pstmt.setString(1, managerId);
                int managerUpdates = pstmt.executeUpdate();
                System.out.println("✅ Updated " + managerUpdates + " project manager records to remove director reference");
                pstmt.close();
            } catch (SQLException e) {
                System.out.println("ℹ️ No director_id column or project manager relationships to update: " + e.getMessage());
            }
            
            // Step 4: Finally, delete the director record
            String deleteManager = "DELETE FROM manager WHERE managerid = ?";
            pstmt = conn.prepareStatement(deleteManager);
            pstmt.setString(1, managerId);
            int rowsDeleted = pstmt.executeUpdate();
            
            System.out.println("🔄 Attempting to delete director record...");
            System.out.println("📊 Rows affected: " + rowsDeleted);
            
            if (rowsDeleted > 0) {
                conn.commit();
                System.out.println("✅ Director account deleted successfully: " + managerId);
                return true;
            } else {
                conn.rollback();
                System.err.println("❌ No director record found to delete for ID: " + managerId);
                return false;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Database error during account deletion: " + e.getMessage());
            System.err.println("❌ SQL State: " + e.getSQLState());
            System.err.println("❌ Error Code: " + e.getErrorCode());
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
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Delete Account - IMNSB Employee Leave Management System</title>
    <link rel="icon" href="/ELMS_3.0/imnsb_logo.png">
    <link rel="stylesheet" href="/ELMS_3.0/css/styles.css">
    <link rel="stylesheet" href="/ELMS_3.0/css/dashboard.css">
    <link rel="stylesheet" href="/ELMS_3.0/css/settings.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    
    <style>
    .settings-container {
        max-width: 600px;
        margin: 0 auto;
    }
    
    .settings-form-card.danger {
        border: none;
        border-radius: 12px;
        padding: 2rem;
        background: #fff;
        box-shadow: 0 4px 20px rgba(220, 53, 69, 0.1);
    }
    
    .delete-warning {
        text-align: center;
        margin-bottom: 2rem;
        padding: 1.5rem;
        background: #f8d7da;
        border-radius: 8px;
    }
    
    .delete-warning i {
        font-size: 3rem;
        color: #dc3545;
        margin-bottom: 1rem;
    }
    
    .delete-warning h3 {
        color: #721c24;
        margin-bottom: 1rem;
    }
    
    .delete-warning p {
        color: #721c24;
        margin-bottom: 1rem;
    }
    
    .delete-warning ul {
        text-align: left;
        color: #721c24;
        margin: 1rem 0;
    }
    
    .delete-warning li {
        margin-bottom: 0.5rem;
    }
    
    .form-group {
        margin-bottom: 1.5rem;
    }
    
    .form-group label {
        display: block;
        margin-bottom: 0.5rem;
        font-weight: 600;
        color: #333;
    }
    
    .form-group input[type="password"] {
        width: 100%;
        padding: 0.75rem;
        border: 2px solid #ddd;
        border-radius: 6px;
        font-size: 1rem;
        transition: border-color 0.3s ease;
    }
    
    .form-group input[type="password"]:focus {
        outline: none;
        border-color: #dc3545;
        box-shadow: 0 0 0 3px rgba(220, 53, 69, 0.1);
    }
    
    .checkbox-label {
        display: flex;
        align-items: flex-start;
        gap: 0.5rem;
        cursor: pointer;
        font-size: 0.95rem;
        line-height: 1.4;
    }
    
    .checkbox-label input[type="checkbox"] {
        margin-top: 0.2rem;
        transform: scale(1.2);
    }
    
    .form-buttons {
        display: flex;
        gap: 1rem;
        justify-content: center;
        margin-top: 2rem;
    }
    
    .btn-secondary:hover {
        color: white;
    }
    
    .btn-danger {
        background: #dc3545;
        color: white;
    }
    
    .btn-danger:hover {
        background: #c82333;
        color: white;
    }
    
    .btn-danger:disabled {
        background: #6c757d;
        cursor: not-allowed;
        transform: none;
        color: white;
    }
    
    .error-message {
        background: #f8d7da;
        color: #721c24;
        padding: 1rem;
        border-radius: 6px;
        border-left: 4px solid #dc3545;
        margin-bottom: 1rem;
    }
    
    .error-message i {
        margin-right: 0.5rem;
    }
    
    /* Modal Styles - Matching consistent design with danger colors */
    .popup-overlay {
        position: fixed;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background-color: rgba(0, 0, 0, 0.5);
        display: flex;
        justify-content: center;
        align-items: center;
        z-index: 10000;
        animation: fadeIn 0.3s ease-in-out;
    }
    
    .popup-content {
        background: white;
        border-radius: 10px;
        max-width: 500px;
        width: 90%;
        box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
        animation: slideIn 0.3s ease-in-out;
        overflow: hidden;
    }
    
    .popup-body {
        padding: 2rem 1.5rem;
    }
    
    .popup-footer {
        padding: 1rem 1.5rem;
        border-top: 1px solid #dee2e6;
        display: flex;
        gap: 1rem;
        justify-content: center;
        background: #f8f9fa;
    }
    
    .btn-modal {
        padding: 0.5rem 1.5rem;
        border: none;
        outline: none;
        border-radius: 4px;
        cursor: pointer;
        text-decoration: none;
        display: inline-flex;
        align-items: center;
        gap: 0.5rem;
        font-size: 0.9rem;
        font-weight: 500;
        transition: all 0.3s ease;
        min-width: 120px;
        justify-content: center;
        color: white;
    }
    
    .btn-modal:focus {
        outline: none;
    }
    
    .btn-modal-danger {
        background: #dc3545;
        border: none;
    }
    
    .btn-modal-danger:hover {
        background: #c82333;
        transform: translateY(-1px);
    }
    
    .btn-modal-danger:focus {
        box-shadow: 0 0 0 3px rgba(220, 53, 69, 0.25);
    }
    
    .btn-modal-secondary {
        background: #6c757d;
        border: none;
    }
    
    .btn-modal-secondary:hover {
        background: #5a6268;
        transform: translateY(-1px);
    }
    
    .btn-modal-secondary:focus {
        box-shadow: 0 0 0 3px rgba(108, 117, 125, 0.25);
    }
    
    .consequences-list {
        background: #f8f9fa;
        padding: 15px;
        border-radius: 8px;
        margin: 15px 0;
        text-align: left;
    }
    
    .consequences-list h4 {
        color: #dc3545;
        margin-bottom: 10px;
        font-size: 1rem;
        font-weight: 600;
    }
    
    .consequences-list ul {
        margin: 0;
        padding-left: 20px;
    }
    
    .consequences-list li {
        margin-bottom: 8px;
        color: #495057;
        line-height: 1.4;
    }
    
    .warning-text {
        background: #fff3cd;
        color: #856404;
        padding: 10px;
        border-radius: 5px;
        text-align: center;
        font-weight: 600;
        margin: 15px 0;
        border: 1px solid #ffeaa7;
    }
    
    @keyframes fadeIn {
        from { opacity: 0; }
        to { opacity: 1; }
    }
    
    @keyframes slideIn {
        from { 
            transform: translateY(-50px);
            opacity: 0;
        }
        to { 
            transform: translateY(0);
            opacity: 1;
        }
    }
    
    @media (max-width: 600px) {
        .form-buttons {
            flex-direction: column;
        }
        
        .btn {
            justify-content: center;
        }
        
        .popup-content {
            margin: 1rem;
            width: calc(100% - 2rem);
            min-width: auto;
        }
        
        .popup-footer {
            flex-direction: column;
        }
    }
</style>
</head>
<body>
    <div class="dashboard-container">
        <!-- Sidebar Navigation -->
        <nav class="sidebar" id="sidebar">
            <div class="sidebar-header">
                <div class="logo-container">
                    <img src="../imnsb_logowbg.png" alt="IMNSB Logo" class="sidebar-logo">
                </div>
                <h3>IMNSB Executive Director</h3><br>
            </div>
            <ul class="sidebar-menu">
                <li><a href="/ELMS_3.0/Manager/ExecutiveDirectorDashboard.jsp"><i class="fas fa-user"></i> <span>Profile</span></a></li>
                <li><a href="<%= request.getContextPath() %>/executive-director-project-managers"><i class="fas fa-users"></i> <span>Project Managers</span></a></li>
                <li><a href="<%= request.getContextPath() %>/manager-pending-requests"><i class="fas fa-clock"></i> <span>Pending Requests</span></a></li>
                <li class="active"><a href="/ELMS_3.0/Manager/DirectorSettings.jsp"><i class="fas fa-cog"></i> <span>Settings</span></a></li>
                <li class="logout"><a href="/ELMS_3.0/ManagerLoginController?action=logout" id="logoutBtn"><i class="fas fa-sign-out-alt"></i> <span>Logout</span></a></li>
            </ul>
        </nav>
        
        <!-- Main Content -->
        <main class="main-content" id="mainContent">
            <header class="content-header">
                <div class="header-left">
                    <button id="sidebarToggle" class="sidebar-toggle">
                        <i class="fas fa-bars"></i>
                    </button>
                    <h2>Delete Account</h2>
                </div>
                <div class="header-right">
                    <span id="userName"><%= manager.getManagername() %></span>
                    <div class="user-avatar">
                        <%
                            String headerProfilePicPath = manager.getProfilePicturePath();
                            if (headerProfilePicPath != null && !headerProfilePicPath.isEmpty()) {
                        %>
                        <img src="/ELMS_3.0/<%= headerProfilePicPath %>" alt="User Avatar" id="headerAvatar">
                        <% } else { %>
                        <img src="/ELMS_3.0/defaultprofilepicture.jpg" alt="User Avatar" id="headerAvatar">
                        <% } %>
                    </div>
                </div>
            </header>
            
            <div class="content-body">
                <div class="settings-container">
                    <div class="settings-form-card danger">
                        <div class="delete-warning">
                            <i class="fas fa-exclamation-triangle"></i>
                            <h3>Warning: This action cannot be undone</h3>
                            <p>Deleting your executive director account will permanently remove all your data, including:</p>
                            <ul>
                                <li>Personal information and profile</li>
                                <li>All leave application approvals and history</li>
                                <li>Project manager oversight records</li>
                                <li>Executive director permissions and access</li>
                                <li>Administrative records and system configurations</li>
                            </ul>
                            <p><strong>This action is irreversible!</strong></p>
                        </div>
                        
                        <%
						// Add this code right after the </div> that closes the delete-warning and before the <form>
						ManagerDAO managerDAO = new ManagerDAO();
						String deletionWarning = "";
						
						try {
						    int leaveAppCount = managerDAO.countDependentLeaveApplications(manager.getManagerid());
						    if (leaveAppCount > 0) {
						        deletionWarning = "ℹ️ You have " + leaveAppCount + " leave application(s). " +
						                        "Your approval history will be preserved for audit purposes.";
						    } else {
						        deletionWarning = "✅ You have no leave applications - account can be safely deleted.";
						    }
						} catch (Exception e) {
						    deletionWarning = "Unable to check dependencies.";
						}
						%>

						<!-- Add this HTML right after the Java code and before the form -->
						<% if (!deletionWarning.isEmpty()) { %>
						<div class="info-message" style="background: #d1ecf1; color: #0c5460; padding: 1rem; border-radius: 6px; margin-bottom: 1rem;">
						    <%= deletionWarning %>
						</div>
						<% } %>
                        
                        <form id="deleteAccountForm" method="POST" action="">
                            <div class="form-group">
                                <label for="password">
                                    <i class="fas fa-lock"></i> Enter your current password to confirm
                                </label>
                                <input type="password" id="password" name="password" required 
                                       placeholder="Enter your password" autocomplete="current-password">
                            </div>
                            
                            <div class="form-group">
                                <label class="checkbox-label">
                                    <input type="checkbox" id="confirmDelete" name="confirmDelete" value="true" required>
                                    I understand that this action is permanent and cannot be undone. I want to permanently delete my executive director account and all associated data.
                                </label>
                            </div>
                            
                            <div class="form-buttons">
                                <a href="/ELMS_3.0/Manager/DirectorSettings.jsp" class="btn btn-secondary">Cancel
                                </a>
                                <button type="submit" class="btn btn-danger" id="deleteBtn" disabled>
                                    <i class="fas fa-trash-alt"></i> Delete Account
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </main>
    </div>

   <!-- Error Modal -->
	<div id="errorModal" class="popup-overlay" style="display: none;">
	    <div class="popup-content" style="border-top: 5px solid #dc3545;">
	        <div class="popup-body">
	            <div style="text-align: center; padding: 1rem 0;">
	                <i class="fas fa-exclamation-circle" style="font-size: 4rem; color: #dc3545; margin-bottom: 1rem;"></i>
	                <h3 style="color: #dc3545; font-size: 1.5rem; margin: 0 0 0.5rem 0;">Account Deletion Failed</h3>
	                <p id="errorMessage" style="color: #495057; font-size: 1rem; margin: 0 0 1rem 0; line-height: 1.5;"></p>
	                <p style="font-size: 0.9rem; color: #dc3545; margin: 0;">
	                    Please check your password and try again.
	                </p>
	            </div>
	        </div>
	        <div class="popup-footer">
	            <button type="button" class="btn-modal btn-modal-secondary" onclick="closeErrorModal()">
	                <i class="fas fa-times"></i> Close
	            </button>
	        </div>
	    </div>
	</div>

    <!-- Confirmation Modal -->
	<div id="confirmModal" class="popup-overlay" style="display: none;">
	    <div class="popup-content" style="border-top: 5px solid #dc3545;">
	        <div class="popup-body">
	            <div style="text-align: center; padding: 1rem 0;">
	                <i class="fas fa-exclamation-triangle" style="font-size: 4rem; color: #dc3545; margin-bottom: 1rem;"></i>
	                <h3 style="color: #dc3545; font-size: 1.5rem; margin: 0 0 1rem 0;">Confirm Account Deletion</h3>
	                <p style="color: #495057; font-size: 1rem; margin: 0 0 1rem 0; line-height: 1.5;">
	                    Are you absolutely sure you want to delete your executive director account?
	                </p>
	                
	                <div class="consequences-list">
	                    <h4>This action will permanently:</h4>
	                    <ul>
	                        <li>Delete all your personal information and profile</li>
	                        <li>Remove all your leave approval history</li>
	                        <li>Revoke access to the executive director portal</li>
	                        <li>Remove all administrative permissions</li>
	                        <li>Delete project manager oversight records</li>
	                        <li>Remove all system configurations and settings</li>
	                    </ul>
	                </div>
	                
	                <div class="warning-text">
	                    ⚠️ This action CANNOT be undone! ⚠️
	                </div>
	                
	                <p style="text-align: center; margin: 0; color: #495057;">
	                    Click "Delete Account" to proceed, or "Cancel" to keep your account.
	                </p>
	            </div>
	        </div>
	        <div class="popup-footer">
	            <button type="button" class="btn-modal btn-modal-secondary" onclick="closeConfirmModal()">
	                <i class="fas fa-times"></i> Cancel
	            </button>
	            <button type="button" class="btn-modal btn-modal-danger" onclick="proceedWithDeletion()">
	                <i class="fas fa-trash-alt"></i> Delete Account
	            </button>
	        </div>
	    </div>
	</div>
    <script src="/ELMS_3.0/Manager/Manager.js"></script>
    <script>
        // Check for error message and show modal
        <% if (!errorMessage.isEmpty()) { %>
        document.addEventListener('DOMContentLoaded', function() {
            showErrorModal('<%= errorMessage.replace("'", "\\'") %>');
        });
        <% } %>
        
        // Error Modal functions
        function showErrorModal(message) {
            document.getElementById('errorMessage').textContent = message;
            document.getElementById('errorModal').style.display = 'flex';
        }
        
        function closeErrorModal() {
            document.getElementById('errorModal').style.display = 'none';
        }
        
        // Confirmation Modal functions
        function showConfirmModal() {
            document.getElementById('confirmModal').style.display = 'flex';
        }
        
        function closeConfirmModal() {
            document.getElementById('confirmModal').style.display = 'none';
        }
        
        function proceedWithDeletion() {
            // Close confirmation modal
            closeConfirmModal();
            
            // Show loading state
            const deleteBtn = document.getElementById('deleteBtn');
            deleteBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Deleting Account...';
            deleteBtn.disabled = true;
            
            // Submit the form
            document.getElementById('deleteAccountForm').submit();
        }
        
        // Close modals when clicking outside
        document.addEventListener('click', function(event) {
            const errorModal = document.getElementById('errorModal');
            const confirmModal = document.getElementById('confirmModal');
            
            if (event.target === errorModal) {
                closeErrorModal();
            }
            
            if (event.target === confirmModal) {
                closeConfirmModal();
            }
        });
        
        // Close modals with Escape key
        document.addEventListener('keydown', function(event) {
            if (event.key === 'Escape') {
                closeErrorModal();
                closeConfirmModal();
            }
        });
        
        // Enable/disable delete button based on form validation
        function validateForm() {
            const password = document.getElementById('password').value;
            const confirmDelete = document.getElementById('confirmDelete').checked;
            const deleteBtn = document.getElementById('deleteBtn');
            
            if (password.length > 0 && confirmDelete) {
                deleteBtn.disabled = false;
                deleteBtn.style.opacity = '1';
            } else {
                deleteBtn.disabled = true;
                deleteBtn.style.opacity = '0.6';
            }
        }
        
        // Add event listeners
        document.getElementById('password').addEventListener('input', validateForm);
        document.getElementById('confirmDelete').addEventListener('change', validateForm);
        
        // Replace confirm dialog with modal
        document.getElementById('deleteAccountForm').addEventListener('submit', function(e) {
            e.preventDefault(); // Always prevent default submission
            
            const password = document.getElementById('password').value;
            
            if (!password) {
                showErrorModal('Please enter your password to confirm account deletion.');
                return;
            }
            
            // Show confirmation modal instead of dialog
            showConfirmModal();
        });
        
        // Auto-focus password field
        document.addEventListener('DOMContentLoaded', function() {
            document.getElementById('password').focus();
        });
        
        // Add show/hide password button
        document.addEventListener('DOMContentLoaded', function() {
            const passwordField = document.getElementById('password');
            const toggleBtn = document.createElement('button');
            toggleBtn.type = 'button';
            toggleBtn.innerHTML = '<i class="fas fa-eye"></i>';
            toggleBtn.className = 'password-toggle';
            toggleBtn.style.cssText = 
                'position: absolute;' +
                'right: 10px;' +
                'top: 50%;' +
                'transform: translateY(-50%);' +
                'background: none;' +
                'border: none;' +
                'cursor: pointer;' +
                'color: #666;';
            
            // Wrap password field in relative container
            const wrapper = document.createElement('div');
            wrapper.style.position = 'relative';
            passwordField.parentNode.insertBefore(wrapper, passwordField);
            wrapper.appendChild(passwordField);
            wrapper.appendChild(toggleBtn);
            
            toggleBtn.addEventListener('click', function() {
                const type = passwordField.getAttribute('type') === 'password' ? 'text' : 'password';
                passwordField.setAttribute('type', type);
                this.innerHTML = type === 'password' ? '<i class="fas fa-eye"></i>' : '<i class="fas fa-eye-slash"></i>';
            });
        });
    </script>
</body>
</html>
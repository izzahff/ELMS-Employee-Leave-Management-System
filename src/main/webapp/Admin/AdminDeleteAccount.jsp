<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="elms.model.Admin" %>
<%@ page import="elms.DAO.AdminDAO" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
    // Check if admin is logged in
    String adminId = (String) session.getAttribute("adminId");
    String adminName = (String) session.getAttribute("adminName");
    String adminEmail = (String) session.getAttribute("adminEmail");
    String adminPhone = (String) session.getAttribute("adminPhone");
    
    if (adminId == null) {
        response.sendRedirect("/ELMS_3.0/Admin/AdminLogin.jsp");
        return;
    }
    
    // Set default values if session attributes are null
    if (adminName == null) adminName = "Admin User";
    if (adminEmail == null) adminEmail = "admin@imnsb.com";
    if (adminPhone == null) adminPhone = "+1234567890";

    // Handle form submission
    String deleteResult = "";
    String errorMessage = "";
    
    if ("POST".equals(request.getMethod())) {
        String password = request.getParameter("password");
        String confirmDelete = request.getParameter("confirmDelete");
        
        if (password != null && confirmDelete != null) {
            // Validate password by attempting authentication
            AdminDAO adminDAO = new AdminDAO();
            Admin validatedAdmin = adminDAO.authenticateAdmin(adminId, password);
            
            if (validatedAdmin != null) {
                // Password is correct, proceed with account deletion
                try {
                    boolean deleted = deleteAdminAccount(adminId);
                    if (deleted) {
                        // Clear session and redirect to login with success message
                        session.invalidate();
                        response.sendRedirect("/ELMS_3.0/Admin/AdminLogin.jsp?message=Account deleted successfully");
                        return;
                    } else {
                        errorMessage = "Failed to delete account. Please try again.";
                    }
                } catch (Exception e) {
                    System.err.println("Error deleting admin account: " + e.getMessage());
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
    // Simplified helper method to delete admin account
    // The database trigger will automatically handle leave type cleanup
    private boolean deleteAdminAccount(String adminId) {
        java.sql.Connection conn = null;
        java.sql.PreparedStatement pstmt = null;
        
        try {
            conn = elms.connection.ConnectionManager.getConnection();
            if (conn == null) {
                return false;
            }
            
            // Start transaction
            conn.setAutoCommit(false);
            
            // Check if this is the last admin account
            String countAdmins = "SELECT COUNT(*) FROM admin";
            pstmt = conn.prepareStatement(countAdmins);
            java.sql.ResultSet rs = pstmt.executeQuery();
            int adminCount = 0;
            if (rs.next()) {
                adminCount = rs.getInt(1);
            }
            rs.close();
            pstmt.close();
            
            // Prevent deletion if this is the last admin
            if (adminCount <= 1) {
                conn.rollback();
                throw new RuntimeException("Cannot delete the last admin account. At least one admin must remain in the system.");
            }
            
            // Delete admin record
            // The trigger will automatically set adminId to NULL in leavetype table
            String deleteAdmin = "DELETE FROM admin WHERE adminId = ?";
            pstmt = conn.prepareStatement(deleteAdmin);
            pstmt.setString(1, adminId);
            int rowsDeleted = pstmt.executeUpdate();
            
            if (rowsDeleted > 0) {
                conn.commit();
                System.out.println("Admin account deleted successfully: " + adminId);
                System.out.println("Database trigger handled leave type cleanup automatically");
                return true;
            } else {
                conn.rollback();
                return false;
            }
            
        } catch (java.sql.SQLException e) {
            System.err.println("Database error during admin account deletion: " + e.getMessage());
            try {
                if (conn != null) conn.rollback();
            } catch (java.sql.SQLException rollbackEx) {
                System.err.println("Error during rollback: " + rollbackEx.getMessage());
            }
            return false;
        } catch (RuntimeException e) {
            System.err.println("Business logic error: " + e.getMessage());
            try {
                if (conn != null) conn.rollback();
            } catch (java.sql.SQLException rollbackEx) {
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
            } catch (java.sql.SQLException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
    }
%>



<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Delete Account - IMNSB Admin Leave Management System</title>
    <link rel="icon" type="image/png" href="/ELMS_3.0/imnsb_logo.png">
    <link rel="stylesheet" href="/ELMS_3.0/css/styles.css">
    <link rel="stylesheet" href="/ELMS_3.0/css/dashboard.css">
    <link rel="stylesheet" href="/ELMS_3.0/css/settings.css">
    <link rel="stylesheet" href="/ELMS_3.0/css/admin.css">
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
            margin-bottom: 8px;
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
        
        .admin-specific-warning {
            background: #fff3cd;
            color: #856404;
            padding: 1rem;
            margin-bottom: 1.5rem;
            border-radius: 6px;
        }
        
        .admin-specific-warning i {
            color: #ffc107;
            font-size: 1.2rem;
            margin-right: 0.5rem;
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
            color: black;
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
        
        /* Modal Styles */
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
            padding: 30px;
            border-radius: 10px;
            text-align: center;
            min-width: 300px;
            max-width: 500px;
            box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
            animation: slideIn 0.3s ease-in-out;
        }
        
        .popup-icon {
            font-size: 3rem;
            margin-bottom: 15px;
        }
        
        .popup-content h3 {
            margin: 0 0 15px 0;
            color: #333;
        }
        
        .popup-content p {
            margin: 0 0 20px 0;
            color: #666;
            line-height: 1.5;
        }
        
        .popup-btn {
            background-color: #007bff;
            color: white;
            border: none;
            padding: 10px 30px;
            border-radius: 5px;
            cursor: pointer;
            font-size: 1rem;
            font-weight: 500;
            transition: background-color 0.3s ease;
            margin-top: 10px;
        }
        
        .popup-btn:hover {
            background-color: #0056b3;
        }
        
        .error-popup .popup-content {
            border-top: 5px solid #dc3545;
        }
        
        /* Confirmation Modal Styles */
        .confirm-popup .popup-content {
            border-top: 5px solid #dc3545;
            text-align: left;
        }
        
        .confirm-popup .popup-icon {
            color: #dc3545;
            text-align: center;
        }
        
        .confirm-popup h3 {
            color: #dc3545;
            text-align: center;
            margin-bottom: 20px;
        }
        
        .confirm-popup .consequences-list {
            background: #f8f9fa;
            padding: 15px;
            border-radius: 8px;
            margin: 15px 0;
        }
        
        .confirm-popup .consequences-list h4 {
            color: #dc3545;
            margin-bottom: 10px;
            font-size: 1rem;
        }
        
        .confirm-popup .consequences-list ul {
            margin: 0;
            padding-left: 20px;
        }
        
        .confirm-popup .consequences-list li {
            margin-bottom: 5px;
            color: #495057;
        }
        
        .confirm-popup .warning-text {
            background: #fff3cd;
            color: #856404;
            padding: 10px;
            border-radius: 5px;
            text-align: center;
            font-weight: 600;
            margin: 15px 0;
            border: 1px solid #ffeaa7;
        }
        
        .confirm-popup .modal-buttons {
            display: flex;
            gap: 15px;
            justify-content: center;
            margin-top: 20px;
        }
        
        .confirm-popup .btn-cancel {
            background: #007bff;
            color: white;
            border: none;
            padding: 12px 25px;
            border-radius: 5px;
            cursor: pointer;
            font-size: 1rem;
            font-weight: 500;
            transition: background-color 0.3s ease;
        }
        
        .confirm-popup .btn-cancel:hover {
            background: #0056b3;
        }
        
        .confirm-popup .btn-confirm {
            background: #dc3545;
            color: white;
            border: none;
            padding: 12px 25px;
            border-radius: 5px;
            cursor: pointer;
            font-size: 1rem;
            font-weight: 500;
            transition: background-color 0.3s ease;
        }
        
        .confirm-popup .btn-confirm:hover {
            background: #c82333;
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
            
            .confirm-popup .modal-buttons {
                flex-direction: column;
            }
        }
    </style>
</head>
<body>
    <div class="dashboard-container">
        <!-- Admin Sidebar Navigation -->
        <nav class="sidebar" id="sidebar">
            <div class="sidebar-header">
                <div class="logo-container">
                    <img src="/ELMS_3.0/imnsb_logowbg.png" alt="IMNSB Logo" class="sidebar-logo">
                </div>
                <h3>IMNSB Admin</h3>
            </div>
            <ul class="sidebar-menu">
                <li>
                    <a href="<%= request.getContextPath() %>/admin-dashboard"><i class="fas fa-user"></i> <span>Admin Officer</span></a>
                </li>
                <li>
                    <a href="/ELMS_3.0/Admin/AdminViewManagerListController"><i class="fas fa-user-tie"></i> <span>Approval Managers</span></a>
                </li>
                <li>
                    <a href="/ELMS_3.0/Admin/AdminViewEmployeeListController"><i class="fas fa-users"></i> <span>Employees</span></a>
                </li>
                <li>
                    <a href="/ELMS_3.0/AdminLeaveTypeListController"><i class="fas fa-list-alt"></i> <span>Leave Types</span></a>
                </li>
                <li>
                    <a href="<%= request.getContextPath() %>/admin-leave-requests"><i class="fas fa-clipboard-list"></i> <span>Leave Requests</span></a>
                </li>
                <li>
                    <a href="/ELMS_3.0/admin-leave-reports"><i class="fas fa-chart-bar"></i> <span>View Report</span></a>
                </li>
                <li class="active">
                    <a href="/ELMS_3.0/Admin/AdminSettings.jsp"><i class="fas fa-cog"></i> <span>Settings</span></a>
                </li>
                <li class="logout">
                    <a href="/ELMS_3.0/Admin/AdminLogin.jsp" id="logoutBtn"><i class="fas fa-sign-out-alt"></i> <span>Logout</span></a>
                </li>
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
                    <span id="userName"><%= adminName %></span>
                    <div class="user-avatar">
                        <% 
                            String headerProfilePicPath = (String) session.getAttribute("adminProfilePicturePath");
                            if (headerProfilePicPath != null && !headerProfilePicPath.isEmpty()) {
                        %>
                            <img src="/ELMS_3.0/<%= headerProfilePicPath %>" alt="Admin Avatar" id="headerAvatar" 
                                 onerror="this.src='/ELMS_3.0/defaultprofilepicture.jpg';">
                        <% } else { %>
                            <img src="/ELMS_3.0/defaultprofilepicture.jpg" alt="Admin Avatar" id="headerAvatar">
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
                            <p>Deleting your admin account will permanently remove all your administrative data, including:</p>
                            <ul>
                                <li>Personal information and admin profile</li>
                                <li>Administrative privileges and access rights</li>
                                <li>All associated admin records in the system</li>
                                <li>Access to the admin dashboard and management tools</li>
                                <li>Profile picture and other uploaded files</li>
                            </ul>
                            <p><strong>This action is irreversible!</strong></p>
                        </div>
                        
                        <div class="admin-specific-warning">
                            <i class="fas fa-shield-alt"></i>
                            <strong>Admin Account Deletion:</strong> Please note that deleting an admin account may affect system administration capabilities. Ensure there are other admin accounts available to manage the system.
                        </div>
                        
                        <form id="deleteAccountForm" method="POST" action="">
                            <div class="form-group">
                                <label for="password">
                                    <i class="fas fa-lock"></i> Enter your current password to confirm
                                </label>
                                <input type="password" id="password" name="password" required 
                                       placeholder="Enter your admin password" autocomplete="current-password">
                            </div>
                            
                            <div class="form-group">
                                <label class="checkbox-label">
                                    <input type="checkbox" id="confirmDelete" name="confirmDelete" value="true" required>
                                    I understand that this action is permanent and cannot be undone. I want to permanently delete my admin account and all associated administrative data.
                                </label>
                            </div>
                            
                            <div class="form-buttons">
                                <a href="/ELMS_3.0/Admin/AdminSettings.jsp" class="btn btn-secondary">Cancel
                                </a>
                                <button type="submit" class="btn btn-danger" id="deleteBtn" disabled>
                                    <i class="fas fa-trash-alt"></i> Delete Admin Account
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </main>
    </div>

    <!-- Error Modal -->
    <div id="errorModal" class="popup-overlay error-popup" style="display: none;">
        <div class="popup-content">
            <div class="popup-icon">🚫</div>
            <h3>Account Deletion Failed</h3>
            <p id="errorMessage"></p>
            <div style="font-size: 0.9rem; color: #dc3545; margin-top: 10px;">
                Please check your password and try again.
            </div>
            <button class="popup-btn" onclick="closeErrorModal()">Try Again</button>
        </div>
    </div>

    <!-- Confirmation Modal -->
    <div id="confirmModal" class="popup-overlay confirm-popup" style="display: none;">
        <div class="popup-content">
            <div class="popup-icon">
                <i class="fas fa-exclamation-triangle"></i>
            </div>
            <h3>Confirm Admin Account Deletion</h3>
            <p>Are you absolutely sure you want to delete your admin account?</p>
            
            <div class="consequences-list">
                <h4>This action will permanently:</h4>
                <ul>
                    <li>Delete all your personal information and admin profile</li>
                    <li>Remove all administrative privileges and access rights</li>
                    <li>Revoke access to the admin dashboard and management tools</li>
                    <li>Delete all associated admin records in the system</li>
                    <li>Remove your profile picture and uploaded files</li>
                </ul>
            </div>
            
            <div class="warning-text">
                ⚠️ This action CANNOT be undone! ⚠️
            </div>
            
            <p style="text-align: center; margin-bottom: 0;">
                Click "Delete Account" to proceed, or "Cancel" to keep your account.
            </p>
            
            <div class="modal-buttons">
                <button class="btn-cancel" onclick="closeConfirmModal()">
                    <i class="fas fa-times"></i> Cancel
                </button>
                <button class="btn-confirm" onclick="proceedWithDeletion()">
                    <i class="fas fa-trash-alt"></i> Delete Account
                </button>
            </div>
        </div>
    </div>

    <script src="/ELMS_3.0/Admin/Admin.js"></script>
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
                showErrorModal('Please enter your admin password to confirm account deletion.');
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
            toggleBtn.style.cssText = `
                position: absolute;
                right: 10px;
                top: 50%;
                transform: translateY(-50%);
                background: none;
                border: none;
                cursor: pointer;
                color: #666;
            `;
            
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
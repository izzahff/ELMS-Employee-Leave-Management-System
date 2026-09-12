<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page import="elms.model.Manager" %>
<%
    // Check if manager is logged in
    Manager manager = (Manager) session.getAttribute("manager");
    if (manager == null) {
        response.sendRedirect(request.getContextPath() + "/Manager/ManagerLogin.jsp");
        return;
    }
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="icon" type="image/png" href="../imnsb_logo.png">
    <title>Settings - IMNSB Employee Leave Management System</title>
    <link rel="stylesheet" href="/ELMS_3.0/css/styles.css">
    <link rel="stylesheet" href="/ELMS_3.0/css/dashboard.css">
    <link rel="stylesheet" href="/ELMS_3.0/css/settings.css">
    <link rel="stylesheet" href="/ELMS_3.0/css/admin.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    
    <style>
    /* Success Popup Styles - Matching Employee/Admin Modal */
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

    .btn-success {
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
        background: #28a745;
    }

    .btn-success:focus {
        outline: none;
        box-shadow: 0 0 0 3px rgba(40, 167, 69, 0.25);
    }

    .btn-success:hover {
        background: #218838;
        transform: translateY(-1px);
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
        .popup-content {
            margin: 1rem;
            width: calc(100% - 2rem);
        }
    }
    </style>
</head>
<body>
    <div class="dashboard-container">
        <!-- Manager Sidebar Navigation -->
        <nav class="sidebar" id="sidebar">
            <div class="sidebar-header">
                <div class="logo-container">
                    <img src="/ELMS_3.0/imnsb_logowbg.png" alt="IMNSB Logo" class="sidebar-logo">
                </div>
                <h3>IMNSB Manager</h3>
            </div>
            <ul class="sidebar-menu">
                <li><a href="/ELMS_3.0/Manager/ManagerDashboard.jsp"><i class="fas fa-user"></i> <span>Profile</span></a></li>
                <li><a href="<%= request.getContextPath() %>/manager-pending-requests"><i class="fas fa-clock"></i> <span>Pending Requests</span></a></li>
                <li class="active"><a href="/ELMS_3.0/Manager/ManagerSettings.jsp"><i class="fas fa-cog"></i> <span>Settings</span></a></li>
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
                    <h2>Settings</h2>
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
                    <div class="settings-menu">
                        <a href="ManagerEditAccount.jsp" class="settings-item">
                            <div class="settings-icon">
                                <i class="fas fa-user-edit"></i>
                            </div>
                            <div class="settings-info">
                                <h3>Edit Account Information</h3>
                                <p>Update your personal details and profile information</p>
                            </div>
                            <i class="fas fa-chevron-right"></i>
                        </a>
                        
                        <a href="ManagerChangePassword.jsp" class="settings-item">
                            <div class="settings-icon">
                                <i class="fas fa-key"></i>
                            </div>
                            <div class="settings-info">
                                <h3>Change Password</h3>
                                <p>Update your account password</p>
                            </div>
                            <i class="fas fa-chevron-right"></i>
                        </a>
                        
                        <a href="ManagerDeleteAccount.jsp" class="settings-item danger" id="deleteAccountBtn">
                            <div class="settings-icon">
                                <i class="fas fa-user-times"></i>
                            </div>
                            <div class="settings-info">
                                <h3>Delete Account</h3>
                                <p>Permanently delete your manager account and all data</p>
                            </div>
                            <i class="fas fa-chevron-right"></i>
                        </a>
                    </div>
                </div>
            </div>
        </main>
    </div>

   <!-- Success Popup Modal - Only shows when success message exists -->
<%
    // Consolidate all success messages
    String displaySuccessMessage = null;
    
    // Check for account update success
    String accountUpdateSuccess = (String) session.getAttribute("accountUpdateSuccess");
    if (accountUpdateSuccess != null) {
        displaySuccessMessage = accountUpdateSuccess;
        session.removeAttribute("accountUpdateSuccess");
    }
    
    // Check for password change success
    String passwordChangeSuccess = (String) session.getAttribute("passwordChangeSuccess");
    if (passwordChangeSuccess != null) {
        displaySuccessMessage = passwordChangeSuccess;
        session.removeAttribute("passwordChangeSuccess");
    }
    
    if (displaySuccessMessage != null) {
%>
<div id="successPopup" class="popup-overlay">
    <div class="popup-content" style="border-top: 5px solid #28a745;">
        <div class="popup-body">
            <div style="text-align: center; padding: 1rem 0;">
                <i class="fas fa-check-circle" style="font-size: 4rem; color: #28a745; margin-bottom: 1rem;"></i>
                <h3 style="color: #28a745; font-size: 1.5rem; margin: 0 0 0.5rem 0;">Success!</h3>
                <p style="color: #495057; font-size: 1rem; margin: 0; line-height: 1.5;">
                    <%= displaySuccessMessage %>
                </p>
            </div>
        </div>
        <div class="popup-footer">
            <button type="button" class="btn-success" onclick="closeSuccessPopup()">
                <i class="fas fa-check"></i> OK
            </button>
        </div>
    </div>
</div>

<script>
// Show success popup and setup handlers
window.addEventListener('DOMContentLoaded', function() {
    const successPopup = document.getElementById('successPopup');
    
    if (successPopup) {
        successPopup.style.display = 'flex';
        console.log('✅ Showing account update success popup');
        
        // Auto-close after 5 seconds
        setTimeout(function() {
            closeSuccessPopup();
        }, 5000);
    }
});
</script>
<% } %>

<script src="/ELMS_3.0/Manager/Manager.js"></script>
<script>
// Close success popup function
function closeSuccessPopup() {
    const popup = document.getElementById('successPopup');
    if (popup) {
        popup.style.display = 'none';
    }
}

// Close popup on outside click
document.addEventListener('click', function(event) {
    if (event.target.id === 'successPopup') {
        closeSuccessPopup();
    }
});

// Close popup on Escape key
document.addEventListener('keydown', function(event) {
    if (event.key === 'Escape') {
        closeSuccessPopup();
    }
});

// Main initialization
document.addEventListener('DOMContentLoaded', function() {
    console.log('✅ ManagerSettings.jsp loaded');

    // Logout confirmation
    const logoutBtn = document.getElementById('logoutBtn');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', function(e) {
            const confirmLogout = confirm('Are you sure you want to logout?');
            if (!confirmLogout) {
                e.preventDefault();
                return false;
            }
            
            this.innerHTML = '<i class="fas fa-spinner fa-spin"></i> <span>Logging out...</span>';
            console.log('Manager logout initiated');
            return true;
        });
    }
});
</script>
</body>
</html>
</body>
</html>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="elms.model.Employee" %>
<%@ page import="elms.controller.ManagerApproveLeaveController" %>
<%@ page import="java.util.Set" %>
<%
    Employee employee = (Employee) session.getAttribute("loggedInEmployee");
    if (employee == null) {
        response.sendRedirect("/ELMS_3.0/Employee/EmployeeLogin.jsp");
        return;
    }

    // Get newly updated applications count for notification badge
    Set<String> newlyUpdatedApps = ManagerApproveLeaveController.getNewlyUpdatedApplications(employee.getEmployeeId());
    int newUpdatesCount = newlyUpdatedApps.size();
    
    // Get success/error messages from session
    String successMessage = (String) session.getAttribute("successMessage");
    String errorMessage = (String) session.getAttribute("errorMessage");
    
    // Remove messages from session after reading (one-time display)
    if (successMessage != null) {
        session.removeAttribute("successMessage");
    }
    if (errorMessage != null) {
        session.removeAttribute("errorMessage");
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="icon" type="image/png" href="/ELMS_3.0/imnsb_logo.png">
    <title>Settings - IMNSB Employee Leave Management System</title>
    <link rel="stylesheet" href="/ELMS_3.0/css/styles.css">
    <link rel="stylesheet" href="/ELMS_3.0/css/dashboard.css">
    <link rel="stylesheet" href="/ELMS_3.0/css/settings.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    
    <style>
    /* Navigation Notification Badge */
    .nav-notification-badge {
        background: #dc3545;
        color: white;
        border-radius: 10px;
        padding: 2px 6px;
        font-size: 0.7rem;
        font-weight: bold;
        margin-left: 8px;
        min-width: 18px;
        text-align: center;
        display: inline-block;
        animation: pulse-nav 2s infinite;
    }
    
    @keyframes pulse-nav {
        0%, 100% { transform: scale(1); }
        50% { transform: scale(1.1); }
    }
    
    /* Modal Styles - Simplified to match Admin */
    .modal {
        position: fixed;
        z-index: 10000;
        left: 0;
        top: 0;
        width: 100%;
        height: 100%;
        background-color: rgba(0, 0, 0, 0.5);
        animation: fadeIn 0.3s ease-in;
        display: flex;
        align-items: center;
        justify-content: center;
    }
    
    .modal-content {
        background-color: #ffffff;
        border: none;
        border-radius: 10px;
        width: 90%;
        max-width: 500px;
        box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
        animation: slideIn 0.3s ease-out;
        overflow: hidden;
    }
    
    .modal-body {
        padding: 2rem 1.5rem;
    }
    
    .btn {
        padding: 0.5rem 1.5rem;
        border-radius: 4px;
        font-weight: 500;
        text-decoration: none;
        border: none;
        outline: none;
        cursor: pointer;
        transition: all 0.3s ease;
        display: inline-flex;
        align-items: center;
        gap: 0.5rem;
        font-size: 0.9rem;
        min-width: 120px;
        justify-content: center;
        color: white;
    }
    
    .btn:focus {
	    outline: none;  /* ✅ Remove focus outline */
	    box-shadow: 0 0 0 3px rgba(40, 167, 69, 0.25);  /* ✅ Optional: add subtle glow on focus */
	}
    
    .btn-primary {
        background: #28a745;
        border: none;
    }
    
    .btn-primary:hover {
        background: #218838;
        transform: translateY(-1px);
    }
    
    .btn-secondary {
        background: #6c757d;
        border: none;
    }
    
    .btn-secondary:hover {
        background: #545b62;
        transform: translateY(-1px);
    }
    
    /* Animation keyframes */
    @keyframes fadeIn {
        from { opacity: 0; }
        to { opacity: 1; }
    }
    
    @keyframes slideIn {
        from { 
            opacity: 0;
            transform: translateY(-50px);
        }
        to { 
            opacity: 1;
            transform: translateY(0);
        }
    }
    
    @keyframes fadeOut {
        from { opacity: 1; }
        to { opacity: 0; }
    }
    
    /* Modal hide animation */
    .modal.hiding {
        animation: fadeOut 0.3s ease-out;
    }
    
    .modal.hiding .modal-content {
        animation: fadeOut 0.3s ease-out;
    }
    
    /* Mobile responsiveness */
    @media (max-width: 768px) {
        .modal-content {
            width: 95%;
            margin: 1rem;
        }
    }
    
    @media (max-width: 600px) {
        .modal-content {
            margin: 1rem;
            width: calc(100% - 2rem);
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
                <h3>IMNSB Employee</h3>
            </div>
            <ul class="sidebar-menu">
                <li><a href="EmployeeDashboard.jsp"><i class="fas fa-user"></i> <span>Profile</span></a></li>
                <li><a href="/ELMS_3.0/LeaveApplicationController"><i class="fas fa-calendar-plus"></i> <span>Apply Leave</span></a></li>
                <li>
                    <a href="<%= request.getContextPath() %>/leave-history">
                        <i class="fas fa-history"></i> <span>Leave History</span>
                        <% if (newUpdatesCount > 0) { %>
                        <span class="nav-notification-badge"><%= newUpdatesCount %></span>
                        <% } %>
                    </a>
                </li>
                <li class="active"><a href="EmployeeSettings.jsp"><i class="fas fa-cog"></i> <span>Settings</span></a></li>
                <li class="logout"><a href="EmployeeLogin.jsp" id="logoutBtn"><i class="fas fa-sign-out-alt"></i> <span>Logout</span></a></li>
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
                    <span id="userName"><%= employee.getEmployeeName() %></span>
                    <div class="user-avatar">
                        <% 
                            String headerProfilePicPath = employee.getProfilePicturePath();
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
                        <a href="EmployeeEditAccount.jsp" class="settings-item">
                            <div class="settings-icon">
                                <i class="fas fa-user-edit"></i>
                            </div>
                            <div class="settings-info">
                                <h3>Edit Account Information</h3>
                                <p>Update your personal details and profile information</p>
                            </div>
                            <i class="fas fa-chevron-right"></i>
                        </a>
                        
                        <a href="/ELMS_3.0/employeeChangePassword" class="settings-item">
                            <div class="settings-icon">
                                <i class="fas fa-key"></i>
                            </div>
                            <div class="settings-info">
                                <h3>Change Password</h3>
                                <p>Update your account password</p>
                            </div>
                            <i class="fas fa-chevron-right"></i>
                        </a>
                        
                        <a href="EmployeeDeleteAccount.jsp" class="settings-item danger" id="deleteAccountBtn">
                            <div class="settings-icon">
                                <i class="fas fa-user-times"></i>
                            </div>
                            <div class="settings-info">
                                <h3>Delete Account</h3>
                                <p>Permanently delete your account and all data</p>
                            </div>
                            <i class="fas fa-chevron-right"></i>
                        </a>
                    </div>
                </div>
            </div>
        </main>
    </div>

	<!-- Success/Error Modal -->
<%
    // Consolidate all success messages
    String displaySuccessMessage = null;
    
    // Check for account update success
    if (successMessage != null) {
        displaySuccessMessage = successMessage;
    }
    
    // Check for password change success
    String passwordChangeSuccess = (String) session.getAttribute("passwordChangeSuccess");
    if (passwordChangeSuccess != null) {
        displaySuccessMessage = passwordChangeSuccess;
        session.removeAttribute("passwordChangeSuccess");
    }
%>

<% if (displaySuccessMessage != null) { %>
<div id="successModal" class="modal">
    <div class="modal-content" style="border-top: 5px solid #28a745; border-radius: 10px; overflow: hidden;">
        <div class="modal-body" style="padding: 2rem 1.5rem;">
            <div style="text-align: center; padding: 1rem 0;">
                <i class="fas fa-check-circle" style="font-size: 4rem; color: #28a745; margin-bottom: 1rem;"></i>
                <h3 style="color: #28a745; font-size: 1.5rem; margin: 0 0 0.5rem 0;">Success!</h3>
                <p style="color: #495057; font-size: 1rem; margin: 0; line-height: 1.5;"><%= displaySuccessMessage %></p>
            </div>
        </div>
        <div style="padding: 1rem 1.5rem; border-top: 1px solid #dee2e6; display: flex; gap: 1rem; justify-content: center; background: #f8f9fa;">
            <button type="button" class="btn btn-primary" id="okButton" 
                style="background: #28a745; min-width: 120px; border: none; outline: none;">
                <i class="fas fa-check"></i> OK
            </button>
        </div>
    </div>
</div>
<% } %>

<% if (errorMessage != null) { %>
<div id="errorModal" class="modal">
    <div class="modal-content" style="border-top: 5px solid #dc3545; border-radius: 10px; overflow: hidden;">
        <div class="modal-body" style="padding: 2rem 1.5rem;">
            <div style="text-align: center; padding: 1rem 0;">
                <i class="fas fa-exclamation-circle" style="font-size: 4rem; color: #dc3545; margin-bottom: 1rem;"></i>
                <h3 style="color: #dc3545; font-size: 1.5rem; margin: 0 0 0.5rem 0;">Error!</h3>
                <p style="color: #495057; font-size: 1rem; margin: 0; line-height: 1.5;"><%= errorMessage %></p>
            </div>
        </div>
        <div style="padding: 1rem 1.5rem; border-top: 1px solid #dee2e6; display: flex; gap: 1rem; justify-content: center; background: #f8f9fa;">
            <button type="button" class="btn btn-secondary" id="okErrorButton" style="background: #6c757d; min-width: 120px;">
                <i class="fas fa-times"></i> Close
            </button>
        </div>
    </div>
</div>
<% } %>

<script src="/ELMS_3.0/Employee/Employee.js?v=2.0"></script>

<script>
    // Modal functionality
    document.addEventListener('DOMContentLoaded', function() {
        console.log('🎭 Settings page with modal system loaded');
        
        // Get modal elements
        const successModal = document.getElementById('successModal');
        const errorModal = document.getElementById('errorModal');
        
        // OK button elements
        const okButton = document.getElementById('okButton');
        const okErrorButton = document.getElementById('okErrorButton');
        
        // Function to close modal with animation
        function closeModal(modal) {
            if (modal) {
                console.log('🚪 Closing modal with animation');
                modal.classList.add('hiding');
                
                setTimeout(function() {
                    modal.style.display = 'none';
                    modal.classList.remove('hiding');
                    console.log('✅ Modal closed successfully');
                }, 300);
            }
        }
        
        // Success modal event listeners
        if (successModal) {
            console.log('✅ Success modal found, setting up events');
            
            // Show modal immediately
            successModal.style.display = 'flex';
            
            // OK button
            if (okButton) {
                okButton.addEventListener('click', function() {
                    console.log('👍 Success modal OK button clicked');
                    closeModal(successModal);
                });
                
                // Focus on OK button for accessibility
                okButton.focus();
            }
            
            // Auto-close after 5 seconds
            setTimeout(function() {
                if (successModal.style.display !== 'none') {
                    console.log('⏰ Auto-closing success modal after 5 seconds');
                    closeModal(successModal);
                }
            }, 5000);
        }

        // Error modal event listeners
        if (errorModal) {
            console.log('❌ Error modal found, setting up events');
            
            // Show modal immediately
            errorModal.style.display = 'flex';
            
            // OK button
            if (okErrorButton) {
                okErrorButton.addEventListener('click', function() {
                    console.log('👍 Error modal OK button clicked');
                    closeModal(errorModal);
                });
                
                // Focus on OK button for accessibility
                okErrorButton.focus();
            }
            
            // Auto-close after 7 seconds (longer for errors)
            setTimeout(function() {
                if (errorModal.style.display !== 'none') {
                    console.log('⏰ Auto-closing error modal after 7 seconds');
                    closeModal(errorModal);
                }
            }, 7000);
        }
        
        // Close modal when clicking outside of it
        window.addEventListener('click', function(event) {
            if (event.target === successModal) {
                console.log('🖱️ Clicked outside success modal, closing');
                closeModal(successModal);
            } else if (event.target === errorModal) {
                console.log('🖱️ Clicked outside error modal, closing');
                closeModal(errorModal);
            }
        });
        
        // Close modal on Escape key
        document.addEventListener('keydown', function(event) {
            if (event.key === 'Escape') {
                if (successModal && successModal.style.display === 'flex') {
                    console.log('⌨️ Escape key pressed, closing success modal');
                    closeModal(successModal);
                } else if (errorModal && errorModal.style.display === 'flex') {
                    console.log('⌨️ Escape key pressed, closing error modal');
                    closeModal(errorModal);
                }
            }
        });
        
        console.log('🎭 Modal system ready');
    });
</script>
</body>
</html>
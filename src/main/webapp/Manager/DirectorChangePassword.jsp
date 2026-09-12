<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="elms.model.Manager" %>
<%
    // Check if executive director is logged in
    Manager manager = (Manager) session.getAttribute("manager");
    if (manager == null) {
        response.sendRedirect("/ELMS_3.0/Manager/ManagerLogin.jsp");
        return;
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="icon" type="image/png" href="/ELMS_3.0/imnsb_logo.png">
    <title>Change Password - IMNSB Employee Leave Management System</title>
    <link rel="stylesheet" href="/ELMS_3.0/css/styles.css">
    <link rel="stylesheet" href="/ELMS_3.0/css/dashboard.css">
    <link rel="stylesheet" href="/ELMS_3.0/css/settings.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <style>
        /* Password field with eye icon styling */
        .password-field-container {
            position: relative;
        }
        
        .password-field-container input {
            padding-right: 40px;
        }
        
        .password-toggle {
            position: absolute;
            right: 10px;
            top: 50%;
            transform: translateY(-50%);
            cursor: pointer;
            color: #6c757d;
            font-size: 1rem;
            z-index: 10;
        }
        
        .password-toggle:hover {
            color: #007bff;
        }
        
        /* Password requirements styling */
        .password-requirements {
            font-size: 0.875rem;
            color: #6c757d;
            margin-top: 10px;
            font-weight: 300;
            background-color: #f8f9fa;
            padding: 15px;
            border-radius: 6px;
            border: 1px solid #dee2e6;
        }
        
        .password-requirements h4 {
            margin: 0 0 10px 0;
            font-size: 0.95rem;
            font-weight: 500;
            color: #495057;
        }
        
        .password-requirements ul {
            margin: 0;
            padding-left: 20px;
        }
        
        .password-requirements li {
            margin: 5px 0;
            font-weight: 300;
            transition: color 0.3s ease;
        }
        
        .password-requirements li.valid {
            color: #28a745;
        }
        
        .password-requirements li.valid::before {
            content: "✓ ";
            font-weight: bold;
        }
        
        .password-requirements li.invalid {
            color: #dc3545;
        }
        
        .password-requirements li.invalid::before {
            content: "✗ ";
            font-weight: bold;
        }
        
        /* Form error styling */
        .form-error {
            color: #dc3545;
            font-size: 0.875rem;
            margin-top: 5px;
            display: none;
            font-weight: 400;
        }
        
        .form-group input.error {
            border-color: #dc3545;
            box-shadow: 0 0 0 0.2rem rgba(220, 53, 69, 0.25);
        }
        
        .form-group input.valid {
            border-color: #28a745;
            box-shadow: 0 0 0 0.2rem rgba(40, 167, 69, 0.25);
        }
        
        /* Success message styling */
        .success-message {
            color: #28a745;
            background-color: #d4edda;
            border: 1px solid #c3e6cb;
            padding: 10px;
            border-radius: 4px;
            margin-bottom: 15px;
        }
        
        .error-message {
            color: #dc3545;
            background-color: #f8d7da;
            border: 1px solid #f5c6cb;
            padding: 10px;
            border-radius: 4px;
            margin-bottom: 15px;
        }
        
        /* Loading state for submit button */
        .btn.loading {
            opacity: 0.7;
            cursor: not-allowed;
        }
        
        .btn.loading::after {
            content: "...";
            animation: dots 1s steps(5, end) infinite;
        }
        
        @keyframes dots {
            0%, 20% { color: rgba(0,0,0,0); text-shadow: .25em 0 0 rgba(0,0,0,0), .5em 0 0 rgba(0,0,0,0); }
            40% { color: white; text-shadow: .25em 0 0 rgba(0,0,0,0), .5em 0 0 rgba(0,0,0,0); }
            60% { text-shadow: .25em 0 0 white, .5em 0 0 rgba(0,0,0,0); }
            80%, 100% { text-shadow: .25em 0 0 white, .5em 0 0 white; }
        }
        
        /* Enhance form styling */
        .settings-form-card {
            background: white;
            border-radius: 8px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
            padding: 30px;
            max-width: 500px;
            margin: 0 auto;
        }
        
        .form-group {
            margin-bottom: 20px;
        }
        
        .form-group label {
            display: block;
            margin-bottom: 8px;
            font-weight: 500;
            color: #495057;
        }
        
        .form-group input {
            width: 100%;
            padding: 12px;
            border: 1px solid #ced4da;
            border-radius: 6px;
            font-size: 1rem;
            transition: border-color 0.3s ease, box-shadow 0.3s ease;
        }
        
        .form-group input:focus {
            outline: none;
            border-color: #007bff;
            box-shadow: 0 0 0 0.2rem rgba(0, 123, 255, 0.25);
        }
        
        /* Popup styles */
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
            max-width: 400px;
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
        }
        
        .popup-btn:hover {
            background-color: #0056b3;
        }
        
        .success-popup .popup-content {
            border-top: 5px solid #28a745;
        }
        
        .error-popup .popup-content {
            border-top: 5px solid #dc3545;
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

        /* Additional styling for better consistency with executive director dashboard */
        body {
            font-family: 'Segoe UI', sans-serif;
            background-color: #f4f6f9;
            margin: 0;
            padding: 0;
        }

        .content-body {
            padding: 2rem;
        }

        .settings-container {
            max-width: 1200px;
            margin: 0 auto;
        }

        .form-buttons {
            display: flex;
            gap: 1rem;
            justify-content: center;
            margin-top: 2rem;
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
                    <h2>Change Password</h2>
                </div>
                <div class="header-right">
                    <span id="userName"><%= manager.getManagername() %></span>
                    <div class="user-avatar">
                        <%
                            String headerProfilePicPath = manager.getProfilePicturePath();
                            if (headerProfilePicPath != null && !headerProfilePicPath.isEmpty()) {
                        %>
                            <img src="/ELMS_3.0/<%= headerProfilePicPath %>" alt="User Avatar" id="headerAvatar" 
                                 onerror="this.src='/ELMS_3.0/defaultprofilepicture.jpg';">
                        <% } else { %>
                            <img src="/ELMS_3.0/defaultprofilepicture.jpg" alt="User Avatar" id="headerAvatar">
                        <% } %>
                    </div>
                </div>
            </header>
            
            <div class="content-body">
                <div class="settings-container">
                    <div class="settings-form-card">
                        
                        <!-- Display success message if present -->
                        <% if (request.getAttribute("successMessage") != null) { %>
                            <div class="success-message">
                                <strong>Success:</strong> <%= request.getAttribute("successMessage") %>
                            </div>
                        <% } %>
                        
                        <!-- Display error message if present -->
                        <% if (request.getAttribute("errorMessage") != null) { %>
                            <div class="error-message">
                                <strong>Error:</strong> <%= request.getAttribute("errorMessage") %>
                            </div>
                        <% } %>
                        
                        <form id="changePasswordForm" action="/ELMS_3.0/executiveDirectorChangePassword" method="post">
                            <div class="form-group">
                                <label for="currentPassword">Current Password</label>
                                <div class="password-field-container">
                                    <input type="password" id="currentPassword" name="currentPassword" required>
                                    <i class="fas fa-eye password-toggle" data-target="currentPassword"></i>
                                </div>
                                <div class="form-error" id="currentPasswordError"></div>
                            </div>
                            
                            <div class="form-group">
                                <label for="newPassword">New Password</label>
                                <div class="password-field-container">
                                    <input type="password" id="newPassword" name="newPassword" required>
                                    <i class="fas fa-eye password-toggle" data-target="newPassword"></i>
                                </div>
                                <div class="form-error" id="newPasswordError"></div>
                            </div>
                            
                            <div class="form-group">
                                <label for="confirmPassword">Confirm New Password</label>
                                <div class="password-field-container">
                                    <input type="password" id="confirmPassword" name="confirmPassword" required>
                                    <i class="fas fa-eye password-toggle" data-target="confirmPassword"></i>
                                </div>
                                <div class="form-error" id="confirmPasswordError"></div>
                            </div>
                            
                            <div class="password-requirements">
                                <h4>Password Requirements:</h4>
                                <ul>
                                    <li id="length">At least 8 characters long</li>
                                    <li id="uppercase">Contains at least one uppercase letter</li>
                                    <li id="lowercase">Contains at least one lowercase letter</li>
                                    <li id="number">Contains at least one number</li>
                                </ul>
                            </div>
                            
                            <div class="form-buttons">
                                <a href="/ELMS_3.0/Manager/DirectorSettings.jsp" class="btn btn-secondary">Cancel</a>
                                <button type="submit" class="btn btn-primary" id="submitBtn">Change Password</button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </main>
    </div>
    
    <script src="/ELMS_3.0/Manager/Manager.js"></script>
    <script>
        document.addEventListener('DOMContentLoaded', function() {
            const form = document.getElementById('changePasswordForm');
            const currentPasswordInput = document.getElementById('currentPassword');
            const newPasswordInput = document.getElementById('newPassword');
            const confirmPasswordInput = document.getElementById('confirmPassword');
            const submitBtn = document.getElementById('submitBtn');
            
            // Password requirements elements
            const lengthReq = document.getElementById('length');
            const uppercaseReq = document.getElementById('uppercase');
            const lowercaseReq = document.getElementById('lowercase');
            const numberReq = document.getElementById('number');
            
            // Password toggle functionality
            document.querySelectorAll('.password-toggle').forEach(toggle => {
                toggle.addEventListener('click', function() {
                    const targetId = this.getAttribute('data-target');
                    const targetInput = document.getElementById(targetId);
                    
                    if (targetInput.type === 'password') {
                        targetInput.type = 'text';
                        this.classList.remove('fa-eye');
                        this.classList.add('fa-eye-slash');
                    } else {
                        targetInput.type = 'password';
                        this.classList.remove('fa-eye-slash');
                        this.classList.add('fa-eye');
                    }
                });
            });
            
            // Current password validation
            currentPasswordInput.addEventListener('input', function() {
                const currentPassword = this.value;
                const errorElement = document.getElementById('currentPasswordError');
                
                if (currentPassword.length > 0) {
                    this.classList.remove('error');
                    errorElement.style.display = 'none';
                }
            });
            
            // New password validation with real-time feedback
            newPasswordInput.addEventListener('input', function() {
                const password = this.value;
                const errorElement = document.getElementById('newPasswordError');
                
                // Check length
                if (password.length >= 8) {
                    lengthReq.classList.add('valid');
                    lengthReq.classList.remove('invalid');
                } else {
                    lengthReq.classList.add('invalid');
                    lengthReq.classList.remove('valid');
                }
                
                // Check uppercase
                if (/[A-Z]/.test(password)) {
                    uppercaseReq.classList.add('valid');
                    uppercaseReq.classList.remove('invalid');
                } else {
                    uppercaseReq.classList.add('invalid');
                    uppercaseReq.classList.remove('valid');
                }
                
                // Check lowercase
                if (/[a-z]/.test(password)) {
                    lowercaseReq.classList.add('valid');
                    lowercaseReq.classList.remove('invalid');
                } else {
                    lowercaseReq.classList.add('invalid');
                    lowercaseReq.classList.remove('valid');
                }
                
                // Check number
                if (/[0-9]/.test(password)) {
                    numberReq.classList.add('valid');
                    numberReq.classList.remove('invalid');
                } else {
                    numberReq.classList.add('invalid');
                    numberReq.classList.remove('valid');
                }
                
                // Overall validation
                if (password && isValidPassword(password)) {
                    this.classList.remove('error');
                    this.classList.add('valid');
                    errorElement.style.display = 'none';
                } else if (password) {
                    this.classList.add('error');
                    this.classList.remove('valid');
                } else {
                    this.classList.remove('error', 'valid');
                }
                
                // Re-validate confirm password if it has content
                if (confirmPasswordInput.value) {
                    validateConfirmPassword();
                }
            });
            
            // Confirm password validation
            confirmPasswordInput.addEventListener('input', validateConfirmPassword);
            
            function validateConfirmPassword() {
                const newPassword = newPasswordInput.value;
                const confirmPassword = confirmPasswordInput.value;
                const errorElement = document.getElementById('confirmPasswordError');
                
                if (confirmPassword && newPassword !== confirmPassword) {
                    confirmPasswordInput.classList.add('error');
                    confirmPasswordInput.classList.remove('valid');
                    errorElement.textContent = 'Passwords do not match';
                    errorElement.style.display = 'block';
                } else if (confirmPassword && newPassword === confirmPassword) {
                    confirmPasswordInput.classList.remove('error');
                    confirmPasswordInput.classList.add('valid');
                    errorElement.style.display = 'none';
                } else {
                    confirmPasswordInput.classList.remove('error', 'valid');
                    errorElement.style.display = 'none';
                }
            }
            
            // Form submission validation
            form.addEventListener('submit', function(e) {
                e.preventDefault(); // Always prevent default form submission
                
                let isValid = true;
                
                // Clear previous errors
                document.querySelectorAll('.form-error').forEach(error => {
                    error.style.display = 'none';
                });
                document.querySelectorAll('input').forEach(input => {
                    input.classList.remove('error');
                });
                
                const currentPassword = currentPasswordInput.value.trim();
                const newPassword = newPasswordInput.value.trim();
                const confirmPassword = confirmPasswordInput.value.trim();
                
                // Current password validation
                if (!currentPassword) {
                    showError('currentPasswordError', 'Please enter your current password');
                    currentPasswordInput.classList.add('error');
                    isValid = false;
                }
                
                // New password validation
                if (!newPassword) {
                    showError('newPasswordError', 'Please enter a new password');
                    newPasswordInput.classList.add('error');
                    isValid = false;
                } else if (!isValidPassword(newPassword)) {
                    showError('newPasswordError', 'Password must meet all requirements below');
                    newPasswordInput.classList.add('error');
                    isValid = false;
                } else if (currentPassword === newPassword) {
                    showError('newPasswordError', 'New password must be different from current password');
                    newPasswordInput.classList.add('error');
                    isValid = false;
                }
                
                // Confirm password validation
                if (!confirmPassword) {
                    showError('confirmPasswordError', 'Please confirm your new password');
                    confirmPasswordInput.classList.add('error');
                    isValid = false;
                } else if (newPassword !== confirmPassword) {
                    showError('confirmPasswordError', 'Passwords do not match');
                    confirmPasswordInput.classList.add('error');
                    isValid = false;
                }
                
                if (!isValid) {
                    return false;
                }
                
                // Show loading state
                submitBtn.disabled = true;
                submitBtn.classList.add('loading');
                submitBtn.textContent = 'Changing Password';
                
                // Submit form via AJAX
                const params = new URLSearchParams();
                params.append('currentPassword', currentPassword);
                params.append('newPassword', newPassword);
                params.append('confirmPassword', confirmPassword);
                
                fetch('/ELMS_3.0/executiveDirectorChangePassword', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded',
                    },
                    body: params
                })
                .then(response => {
                    console.log('Response status:', response.status);
                    
                    // Check if response is JSON
                    const contentType = response.headers.get('content-type');
                    if (!contentType || !contentType.includes('application/json')) {
                        return response.text().then(text => {
                            throw new Error('Server returned non-JSON response');
                        });
                    }
                    
                    return response.json();
                })
                .then(data => {
				    console.log('Response data:', data);
				    
				    // Hide loading state
				    submitBtn.disabled = false;
				    submitBtn.classList.remove('loading');
				    submitBtn.textContent = 'Change Password';
				    
				    if (data.success) {
				        // Redirect to settings page where popup will be shown
				        if (data.redirect) {
				            window.location.href = data.redirect;
				        } else {
				            window.location.href = '/ELMS_3.0/Manager/DirectorSettings.jsp';
				        }
				        
				    } else {
				        // Show error popup
				        showErrorPopup(data.message || 'Unknown error occurred');
				        
				        // If session expired, redirect to login
				        if (data.redirect) {
				            setTimeout(() => {
				                window.location.href = data.redirect;
				            }, 2000);
				        }
				    }
				})
                .catch(error => {
                    console.error('Error:', error);
                    
                    // Hide loading state
                    submitBtn.disabled = false;
                    submitBtn.classList.remove('loading');
                    submitBtn.textContent = 'Change Password';
                    
                    // Show error popup
                    showErrorPopup('Network error occurred. Please try again.');
                });
            });
            
            function showError(elementId, message) {
                const errorElement = document.getElementById(elementId);
                errorElement.textContent = message;
                errorElement.style.display = 'block';
            }
            
            function isValidPassword(password) {
                return password.length >= 8 && 
                       /[A-Z]/.test(password) && 
                       /[a-z]/.test(password) && 
                       /[0-9]/.test(password);
            }
            
            // Popup functions
            function showSuccessPopup(message) {
                removeExistingPopups();
                
                const popup = document.createElement('div');
                popup.className = 'popup-overlay success-popup';
                popup.innerHTML = `
                    <div class="popup-content">
                        <div class="popup-icon">🔐✅</div>
                        <h3>Password Changed Successfully!</h3>
                        <p>${message}</p>
                        <div style="font-size: 0.9rem; color: #28a745; margin-top: 10px;">
                            <strong>Security Tip:</strong> Make sure to remember your new password and keep it secure.
                        </div>
                        <button class="popup-btn" onclick="closePopup()" style="margin-top: 15px">Got it!</button>
                    </div>
                `;
                
                document.body.appendChild(popup);
                
                // Auto close after 5 seconds
                setTimeout(() => {
                    closePopup();
                }, 5000);
            }
            
            function showErrorPopup(message) {
                removeExistingPopups();
                
                const popup = document.createElement('div');
                popup.className = 'popup-overlay error-popup';
                popup.innerHTML = `
                    <div class="popup-content">
                        <div class="popup-icon">🔐❌</div>
                        <h3>Password Change Failed</h3>
                        <p>${message}</p>
                        <div style="font-size: 0.9rem; color: #dc3545; margin-top: 10px;">
                            Please check your current password and try again.
                        </div>
                        <button class="popup-btn" onclick="closePopup()" style="margin-top: 15px">Try Again</button>
                    </div>
                `;
                
                document.body.appendChild(popup);
            }
            
            function removeExistingPopups() {
                const existingPopups = document.querySelectorAll('.popup-overlay');
                existingPopups.forEach(popup => popup.remove());
            }
            
            // Make closePopup function global
            window.closePopup = function() {
                const popup = document.querySelector('.popup-overlay');
                if (popup) {
                    popup.remove();
                }
            };
        });
    </script>
</body>
</html>
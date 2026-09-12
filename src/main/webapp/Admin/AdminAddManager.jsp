<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page session="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%
    //Check if user is logged in and is admin
    if (session.getAttribute("adminId") == null) {
       response.sendRedirect("/ELMS_3.0/Admin/AdminLogin.jsp");
       return;
    }
    
    // Get admin information from session
    String adminName = (String) session.getAttribute("adminName");
    if (adminName == null) adminName = "Admin User";
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="icon" type="image/png" href="../imnsb_logo.png">
    <title>Add Manager - IMNSB Employee Leave Management System</title>
    <link rel="stylesheet" href="/ELMS_3.0/css/styles.css">
    <link rel="stylesheet" href="/ELMS_3.0/css/dashboard.css">
    <link rel="stylesheet" href="/ELMS_3.0/css/admin.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <style>
        .form-error {
            color: #dc3545;
            font-size: 0.875rem;
            margin-top: 5px;
            display: none;
        }
        
        .form-group input.error,
        .form-group select.error {
            border-color: #dc3545;
            box-shadow: 0 0 0 0.2rem rgba(220, 53, 69, 0.25);
        }
        
        .password-requirements {
            font-size: 0.875rem;
            color: #6c757d;
            margin-top: 5px;
        }
        
        .password-requirements ul {
            margin: 5px 0;
            padding-left: 20px;
        }
        
        .password-requirements li {
            margin: 2px 0;
        }
        
        .password-requirements li.valid {
            color: #28a745;
        }
        
        .password-requirements li.invalid {
            color: #dc3545;
        }
        
        .alert {
            padding: 15px;
            margin-bottom: 20px;
            border: 1px solid transparent;
            border-radius: 4px;
        }
        
        .alert-danger {
            color: #721c24;
            background-color: #f8d7da;
            border-color: #f5c6cb;
        }
        
        .alert-success {
            color: #155724;
            background-color: #d4edda;
            border-color: #c3e6cb;
        }

        .role-info {
            background: #f8f9fa;
            border-left: 4px solid #007bff;
            padding: 1rem;
            margin-top: 0.5rem;
            border-radius: 0 4px 4px 0;
        }

        .role-info h5 {
            margin: 0 0 0.5rem 0;
            color: #495057;
            font-size: 0.9rem;
            font-weight: 600;
        }

        .role-info p {
            margin: 0;
            font-size: 0.8rem;
            color: #6c757d;
        }

        .role-option {
            display: flex;
            align-items: center;
            padding: 0.75rem;
            margin: 0.5rem 0;
            border: 2px solid #e9ecef;
            border-radius: 8px;
            cursor: pointer;
            transition: all 0.3s ease;
        }

        .role-option:hover {
            border-color: #007bff;
            background: #f8f9fa;
        }

        .role-option.selected {
            border-color: #007bff;
            background: rgba(0, 123, 255, 0.1);
        }

        .role-option input[type="radio"] {
            margin-right: 1rem;
            transform: scale(1.2);
        }

        .role-details {
            flex: 1;
        }

        .role-title {
            font-weight: 600;
            color: #495057;
            margin-bottom: 0.25rem;
            display: flex;
            align-items: center;
            gap: 0.5rem;
        }

        .role-description {
            font-size: 0.85rem;
            color: #6c757d;
            margin: 0;
        }

        .role-icon {
            font-size: 1.5rem;
            margin-right: 1rem;
        }

        .role-executive .role-icon {
            color: #ff6b6b;
        }

        .role-project .role-icon {
            color: #4ecdc4;
        }

        /* Phone input styling - simplified like admin form */
        .phone-format-help {
            font-size: 0.875rem;
            color: #6c757d;
            margin-top: 5px;
            font-style: italic;
            min-height: 20px;
        }

        /* Password input container with toggle button */
        .password-input-container {
            position: relative;
        }

        .password-toggle {
            position: absolute;
            right: 12px;
            top: 50%;
            transform: translateY(-50%);
            background: none;
            border: none;
            cursor: pointer;
            color: #6c757d;
            font-size: 1rem;
            padding: 4px;
            transition: color 0.3s ease;
            z-index: 10;
        }

        .password-toggle:hover {
            color: #495057;
        }

        .password-toggle:focus {
            outline: none;
            color: #007bff;
        }

        /* Adjust padding for password inputs to make room for toggle button */
        .password-input-container input[type="password"],
        .password-input-container input[type="text"] {
            padding-right: 45px;
        }

        /* Form styling improvements */
        .form-group select {
            appearance: none;
            background-image: url("data:image/svg+xml,%3csvg xmlns='http://www.w3.org/2000/svg' fill='none' viewBox='0 0 20 20'%3e%3cpath stroke='%236b7280' stroke-linecap='round' stroke-linejoin='round' stroke-width='1.5' d='m6 8 4 4 4-4'/%3e%3c/svg%3e");
            background-position: right 0.5rem center;
            background-repeat: no-repeat;
            background-size: 1.5em 1.5em;
            padding-right: 2.5rem;
        }

        /* Equal button sizing */
        .form-buttons {
            display: flex;
            gap: 1rem;
            justify-content: center;
            margin-top: 2rem;
        }

        .form-buttons .btn {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            gap: 0.5rem;
            padding: 0.75rem 1.5rem;
            font-size: 1rem;
            font-weight: 500;
            text-decoration: none;
            border-radius: 4px;
            border: none;
            cursor: pointer;
            transition: var(--transition);
            min-width: 140px;
            flex: 1;
            max-width: 180px;
        }

        .btn-secondary {
            background-color: #6c757d;
            color: white;
            border: 1px solid #6c757d;
        }

        .btn-secondary:hover {
            background-color: #545b62;
            color: white;
            border-color: #545b62;
        }

        .btn-primary {
            background-color: var(--primary-color);
            color: white;
            border: 1px solid var(--primary-color);
        }

        .btn-primary:hover {
            background-color: var(--primary-dark);
            color: white;
            border-color: var(--primary-dark);
        }

        .btn-primary:disabled {
            background-color: #6c757d;
            border-color: #6c757d;
            opacity: 0.8;
            cursor: not-allowed;
        }
        
        .form-group input.valid,
		.form-group select.valid {
		    border-color: #28a745;
		    box-shadow: 0 0 0 0.2rem rgba(40, 167, 69, 0.25);
		}
		
		/* Ensure form-group has relative positioning */
		.form-group {
		    position: relative;
		}
		
		/* Button hover tooltip */
		#add-manager-button-tooltip {
		    position: fixed;
		    background: #000000;
		    color: #ffffff;
		    padding: 14px 18px;
		    border-radius: 8px;
		    font-size: 0.9rem;
		    font-weight: 600;
		    text-align: left;
		    line-height: 1.6;
		    opacity: 0;
		    visibility: hidden;
		    pointer-events: none;
		    transition: opacity 0.2s ease, visibility 0.2s ease;
		    z-index: 999999;
		    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.95);
		    border: 2px solid rgba(255, 255, 255, 0.3);
		    max-width: 280px;
		    white-space: pre-line;
		}
		
		#add-manager-button-tooltip.show {
		    opacity: 1;
		    visibility: visible;
		}
		
		#add-manager-button-tooltip::after {
		    content: '';
		    position: absolute;
		    top: 100%;
		    left: 50%;
		    transform: translateX(-50%);
		    width: 0;
		    height: 0;
		    border-left: 8px solid transparent;
		    border-right: 8px solid transparent;
		    border-top: 8px solid #000000;
		}

        /* Mobile responsiveness */
        @media (max-width: 768px) {
            .form-buttons {
                flex-direction: column;
                gap: 0.75rem;
            }
            
            .form-buttons .btn {
                max-width: none;
                width: 100%;
            }

            .role-option {
                flex-direction: column;
                text-align: center;
            }

            .role-icon {
                margin-right: 0;
                margin-bottom: 0.5rem;
            }

            .password-toggle {
                right: 10px;
            }
        }
    </style>
</head>
<body>
    <div class="dashboard-container">
        <!-- Admin Sidebar Navigation -->
        <nav class="sidebar" id="sidebar">
            <div class="sidebar-header">
                <img src="/ELMS_3.0/imnsb_logowbg.png" alt="IMNSB Logo" class="sidebar-logo">
                <h3>IMNSB Admin</h3>
            </div>
           <ul class="sidebar-menu">
                <li>
	                <a href="<%= request.getContextPath() %>/admin-dashboard"><i class="fas fa-user"></i> <span>Admin Officer</span></a>
	            </li>
	            <li class="active">
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
	            <li>
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
                    <h2>Add Manager</h2>
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
                <!-- Display error message if any -->
                <c:if test="${not empty requestScope.errorMessage}">
                    <div class="alert alert-danger">
                        <i class="fas fa-exclamation-circle"></i>
                        ${requestScope.errorMessage}
                    </div>
                </c:if>
                
                <!-- Display success message if any -->
                <c:if test="${not empty requestScope.successMessage}">
                    <div class="alert alert-success">
                        <i class="fas fa-check-circle"></i>
                        ${requestScope.successMessage}
                    </div>
                </c:if>
                
                <div class="admin-card">
                    <div class="admin-card-header">
                        <h3><i class="fas fa-user-plus"></i> New Manager Details</h3>
                        <p>Create a new manager account for the system</p>
                    </div>
                    <div class="admin-card-content">
                        <form id="addManagerForm" class="admin-form" action="${pageContext.request.contextPath}/AdminAddManagerController" method="post">
                            
                            <div class="form-group">
                                <label for="managerName">
                                    <i class="fas fa-user"></i> Manager Name <span style="color: red;">*</span>
                                </label>
                                <input type="text" id="managerName" name="managername" required 
                                       value="${param.managername}" placeholder="Enter full name">
                                <div class="form-error" id="nameError"></div>
                            </div>
                            
                            <div class="form-group">
                                <label for="managerEmail">
                                    <i class="fas fa-envelope"></i> Manager Email <span style="color: red;">*</span>
                                </label>
                                <input type="email" id="managerEmail" name="manageremail" required 
                                       value="${param.manageremail}" placeholder="Enter email address">
                                <div class="form-error" id="emailError"></div>
                            </div>
                            
							<div class="form-group">
							    <label for="managerPhone">
							        <i class="fas fa-phone"></i> Mobile Number <span style="color: red;">*</span>
							    </label>
							    <input type="tel" id="managerPhone" name="managerphone"
							           value="${param.managerphone != null && param.managerphone != '' ? param.managerphone : '+60 '}" 
							           placeholder="+60 12-345 6789" 
							           maxlength="17" required>
							    <div class="phone-format-help" id="phoneHelp">
							        Format: +60 1x-xxx xxxx (Malaysian mobile number)
							    </div>
							    <div class="form-error" id="phoneError"></div>
							</div>
                            
                            <div class="form-group">
                                <label>
                                    <i class="fas fa-user-tie"></i> Manager Position <span style="color: red;">*</span>
                                </label>
                                
                                <div class="role-option role-executive" onclick="selectPosition('Executive Director')">
                                    <input type="radio" name="managerposition" value="Executive Director" id="positionExecutive" 
                                           ${param.managerposition eq 'Executive Director' ? 'checked' : ''}>
                                    <div class="role-icon">
                                        <i class="fas fa-crown"></i>
                                    </div>
                                    <div class="role-details">
                                        <div class="role-title">Executive Director</div>
                                        <p class="role-description">
                                            Has full management authority, can approve all leave requests, 
                                            and can manage other managers including Project Managers.
                                        </p>
                                    </div>
                                </div>
                                
                                <div class="role-option role-project" onclick="selectPosition('Project Manager')">
                                    <input type="radio" name="managerposition" value="Project Manager" id="positionProject"
                                           ${param.managerposition eq 'Project Manager' ? 'checked' : ''}>
                                    <div class="role-icon">
                                        <i class="fas fa-user-tie"></i>
                                    </div>
                                    <div class="role-details">
                                        <div class="role-title">Project Manager</div>
                                        <p class="role-description">
                                            Can approve leave requests for assigned employees. 
                                            Has limited administrative privileges compared to Executive Directors.
                                        </p>
                                    </div>
                                </div>
                                
                                <div class="form-error" id="positionError"></div>
                                
                                <div class="role-info">
                                    <h5><i class="fas fa-info-circle"></i> Position Hierarchy</h5>
                                    <p>Executive Directors have higher authority and can manage Project Manager accounts. 
                                       Project Managers focus on their assigned teams and projects.</p>
                                </div>
                            </div>
                            
                            <div class="form-group">
                                <label for="managerPassword">
                                    <i class="fas fa-lock"></i> Password <span style="color: red;">*</span>
                                </label>
                                <div class="password-input-container">
                                    <input type="password" id="managerPassword" name="managerpassword" required 
                                           placeholder="Enter password">
                                    <button type="button" class="password-toggle" id="passwordToggle">
                                        <i class="fas fa-eye"></i>
                                    </button>
                                </div>
                                <div class="form-error" id="passwordError"></div>
                                <div class="password-requirements">
                                    <strong>Password Requirements:</strong>
                                    <ul>
                                        <li id="length">At least 8 characters long</li>
                                        <li id="uppercase">Contains uppercase letter (A-Z)</li>
                                        <li id="lowercase">Contains lowercase letter (a-z)</li>
                                        <li id="number">Contains at least one number (0-9)</li>
                                    </ul>
                                </div>
                            </div>
                            
                            <div class="form-group">
                                <label for="confirmPassword">
                                    <i class="fas fa-lock"></i> Confirm Password <span style="color: red;">*</span>
                                </label>
                                <div class="password-input-container">
                                    <input type="password" id="confirmPassword" name="confirmPassword" required 
                                           placeholder="Confirm password">
                                    <button type="button" class="password-toggle" id="confirmPasswordToggle">
                                        <i class="fas fa-eye"></i>
                                    </button>
                                </div>
                                <div class="form-error" id="confirmPasswordError"></div>
                            </div>
                            
                            <div class="form-buttons">
                                <a href="AdminViewManagerListController" class="btn btn-secondary">
                                    <i class="fas fa-times"></i> Cancel
                                </a>
                                <button type="submit" class="btn btn-primary" id="submitBtn" disabled>
							    <i class="fas fa-user-plus"></i> Add Manager
							</button>
                            </div>
                        </form>
                    </div>
                </div>
                
                <div id="add-manager-button-tooltip"></div>
                
            </div>
        </main>
    </div>

    <!-- JavaScript -->
    <script src="/ELMS_3.0/Admin/Admin.js"></script>
    <script>
        document.addEventListener('DOMContentLoaded', function() {
            const form = document.getElementById('addManagerForm');
            const nameInput = document.getElementById('managerName');
            const emailInput = document.getElementById('managerEmail');
            const phoneInput = document.getElementById('managerPhone');
            const passwordInput = document.getElementById('managerPassword');
            const confirmPasswordInput = document.getElementById('confirmPassword');
            const submitBtn = document.getElementById('submitBtn');
            
            // Password toggle elements
            const passwordToggle = document.getElementById('passwordToggle');
            const confirmPasswordToggle = document.getElementById('confirmPasswordToggle');
            
            // Password requirements elements
            const lengthReq = document.getElementById('length');
            const uppercaseReq = document.getElementById('uppercase');
            const lowercaseReq = document.getElementById('lowercase');
            const numberReq = document.getElementById('number');
            
            function isValidName(name) {
                return /^[a-zA-Z\s'-]+$/.test(name) && name.length >= 2 && name.length <= 100;
            }

            function isValidEmail(email) {
                const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
                return emailRegex.test(email);
            }

            function isValidMalaysianPhone(value) {
                const malaysianPhonePattern = /^\+60\s1[0-9]-[0-9]{3}\s[0-9]{4}$/;
                return malaysianPhonePattern.test(value);
            }

            function isValidPassword(password) {
                return password.length >= 8 && 
                       /[A-Z]/.test(password) && 
                       /[a-z]/.test(password) && 
                       /[0-9]/.test(password);
            }

            function checkFormValidity() {
                const name = nameInput.value.trim();
                const email = emailInput.value.trim();
                const phone = phoneInput.value.trim();
                const password = passwordInput.value;
                const confirmPassword = confirmPasswordInput.value;
                const selectedPosition = document.querySelector('input[name="managerposition"]:checked');
                
                const isNameValid = isValidName(name);
                const isEmailValid = isValidEmail(email);
                const isPhoneValid = isValidMalaysianPhone(phone);
                const isPasswordValid = isValidPassword(password);
                const isConfirmPasswordValid = confirmPassword && password === confirmPassword;
                const isPositionSelected = selectedPosition !== null;
                
                const tooltip = document.getElementById('add-manager-button-tooltip');
                
                if (isNameValid && isEmailValid && isPhoneValid && isPasswordValid && isConfirmPasswordValid && isPositionSelected) {
                    submitBtn.disabled = false;
                    
                    if (tooltip) {
                        tooltip.classList.remove('show');
                        tooltip.innerHTML = '';
                    }
                } else {
                    submitBtn.disabled = true;
                    
                    const incompleteFields = [];
                    if (!isNameValid) incompleteFields.push('Name');
                    if (!isEmailValid) incompleteFields.push('Email');
                    if (!isPhoneValid) incompleteFields.push('Mobile Number');
                    if (!isPositionSelected) incompleteFields.push('Position');
                    if (!isPasswordValid) incompleteFields.push('Password');
                    if (!isConfirmPasswordValid && password) incompleteFields.push('Confirm Password');
                    
                    if (tooltip && incompleteFields.length > 0) {
                        tooltip.innerHTML = 'Please complete:<br>• ' + incompleteFields.join('<br>• ');
                    } else if (tooltip) {
                        tooltip.textContent = 'Please complete all required fields';
                    }
                }
            }
            
            nameInput.addEventListener('input', function() {
                const name = this.value;
                const namePattern = /^[a-zA-Z\s'-]+$/;
                const errorElement = document.getElementById('nameError');
                
                if (name.length === 0) {
                    this.classList.remove('error', 'valid');
                    errorElement.style.display = 'none';
                } else if (!namePattern.test(name) || name.length < 2) {
                    this.classList.add('error');
                    this.classList.remove('valid');
                    errorElement.textContent = 'Name can only contain letters, spaces, hyphens, and apostrophes (min 2 characters)';
                    errorElement.style.display = 'block';
                } else {
                    this.classList.remove('error');
                    this.classList.add('valid');
                    errorElement.style.display = 'none';
                }
                
                checkFormValidity();
            });
            
            emailInput.addEventListener('input', function() {
                const email = this.value.trim();
                const errorElement = document.getElementById('emailError');
                const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
                
                if (email.length === 0) {
                    this.classList.remove('error', 'valid');
                    errorElement.style.display = 'none';
                } else if (!emailRegex.test(email)) {
                    this.classList.add('error');
                    this.classList.remove('valid');
                    errorElement.textContent = 'Please enter a valid email address';
                    errorElement.style.display = 'block';
                } else {
                    this.classList.remove('error');
                    this.classList.add('valid');
                    errorElement.style.display = 'none';
                }
                
                checkFormValidity();
            });

            
            // Role selection functionality
            const roleOptions = document.querySelectorAll('.role-option');
            const positionRadios = document.querySelectorAll('input[name="managerposition"]');
            
            // Initialize position selection on page load
            positionRadios.forEach(radio => {
                if (radio.checked) {
                    radio.closest('.role-option').classList.add('selected');
                }
            });
            
            // Password visibility toggle functionality
            passwordToggle.addEventListener('click', function() {
                const type = passwordInput.getAttribute('type') === 'password' ? 'text' : 'password';
                passwordInput.setAttribute('type', type);
                
                const icon = this.querySelector('i');
                if (type === 'password') {
                    icon.classList.remove('fa-eye-slash');
                    icon.classList.add('fa-eye');
                } else {
                    icon.classList.remove('fa-eye');
                    icon.classList.add('fa-eye-slash');
                }
            });
            
            confirmPasswordToggle.addEventListener('click', function() {
                const type = confirmPasswordInput.getAttribute('type') === 'password' ? 'text' : 'password';
                confirmPasswordInput.setAttribute('type', type);
                
                const icon = this.querySelector('i');
                if (type === 'password') {
                    icon.classList.remove('fa-eye-slash');
                    icon.classList.add('fa-eye');
                } else {
                    icon.classList.remove('fa-eye');
                    icon.classList.add('fa-eye-slash');
                }
            });
            
            phoneInput.addEventListener('input', function(e) {
                let value = e.target.value;
                
                // Extract only digits (remove +, spaces, dashes)
                let digitsOnly = value.replace(/\D/g, '');
                
                console.log('📱 Raw value:', value);
                console.log('📱 Digits only:', digitsOnly);
                
                // Handle empty field
                if (digitsOnly === '' || digitsOnly === '6' || digitsOnly === '60') {
                    e.target.value = '+60 ';
                    this.classList.remove('error', 'valid');
                    document.getElementById('phoneHelp').innerHTML = 'Format: +60 1x-xxx xxxx (Malaysian mobile number)';
                    document.getElementById('phoneHelp').style.color = '#6c757d';
                    document.getElementById('phoneError').style.display = 'none';
                    checkFormValidity(); // ✅ CHECK VALIDITY IMMEDIATELY WHEN EMPTY
                    return;
                }
                
                // Remove country code prefix if present (we'll add it back)
                if (digitsOnly.startsWith('60')) {
                    digitsOnly = digitsOnly.substring(2);
                }
                
                // Remove leading 0 if present
                if (digitsOnly.startsWith('0')) {
                    digitsOnly = digitsOnly.substring(1);
                }
                
                // Limit to 9 local digits
                if (digitsOnly.length > 9) {
                    digitsOnly = digitsOnly.substring(0, 9);
                }
                
                console.log('📱 Local digits:', digitsOnly);
                
                // Build formatted string: +60 1x-xxx xxxx
                let formatted = '+60';
                
                if (digitsOnly.length > 0) {
                    formatted += ' ' + digitsOnly.substring(0, Math.min(2, digitsOnly.length));
                }
                if (digitsOnly.length > 2) {
                    formatted += '-' + digitsOnly.substring(2, Math.min(5, digitsOnly.length));
                }
                if (digitsOnly.length > 5) {
                    formatted += ' ' + digitsOnly.substring(5);
                }
                
                console.log('📱 Final formatted:', formatted);
                
                e.target.value = formatted;
                
                const malaysianPhonePattern = /^\+60\s1[0-9]-[0-9]{3}\s[0-9]{4}$/;
                const errorElement = document.getElementById('phoneError');
                const phoneHelp = document.getElementById('phoneHelp');
                
                // Validate the formatted number
                if (formatted && malaysianPhonePattern.test(formatted)) {
                    this.classList.remove('error');
                    this.classList.add('valid');
                    phoneHelp.innerHTML = '✓ Valid Malaysian mobile number format';
                    phoneHelp.style.color = '#28a745';
                    errorElement.style.display = 'none';
                } else if (formatted.length > 4) {
                    // Check if number doesn't start with 1 (not a mobile number)
                    if (digitsOnly.length > 0 && !digitsOnly.startsWith('1')) {
                        this.classList.add('error');
                        this.classList.remove('valid');
                        phoneHelp.innerHTML = 'Invalid format';
                        phoneHelp.style.color = '#dc3545';
                        errorElement.textContent = 'Mobile number must start with 1 (e.g., +60 12-345 6789)';
                        errorElement.style.display = 'block';
                    }
                    // Number starts with 1 but incomplete (less than 9 digits)
                    else if (digitsOnly.startsWith('1') && digitsOnly.length < 9) {
                        this.classList.add('error');
                        this.classList.remove('valid');
                        phoneHelp.innerHTML = 'Continue typing... Format: +60 1x-xxx xxxx';
                        phoneHelp.style.color = '#6c757d';
                        errorElement.style.display = 'none';
                    }
                    // Number is 9+ digits but invalid format
                    else {
                        this.classList.add('error');
                        this.classList.remove('valid');
                        phoneHelp.innerHTML = 'Invalid format';
                        phoneHelp.style.color = '#dc3545';
                        errorElement.textContent = 'Invalid mobile number format';
                        errorElement.style.display = 'block';
                    }
                } else {
                    this.classList.remove('error', 'valid');
                    phoneHelp.innerHTML = 'Format: +60 1x-xxx xxxx (Malaysian mobile number)';
                    phoneHelp.style.color = '#6c757d';
                    errorElement.style.display = 'none';
                }
                
                checkFormValidity(); // ✅ CHECK VALIDITY AFTER EVERY INPUT
            });

            phoneInput.addEventListener('keydown', function(e) {
                const cursorPosition = this.selectionStart;
                if (e.key === 'Backspace' && cursorPosition <= 4) {
                    e.preventDefault();
                    this.value = '+60 ';
                    this.setSelectionRange(4, 4);
                    checkFormValidity(); // ✅ CHECK VALIDITY AFTER BACKSPACE
                }
            });

            phoneInput.addEventListener('focus', function() {
                if (this.value === '+60 ' || this.value === '+60' || this.value === '') {
                    this.value = '+60 ';
                    setTimeout(() => {
                        this.setSelectionRange(4, 4);
                    }, 0);
                }
            });

            
            passwordInput.addEventListener('input', function() {
                const password = this.value;
                
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
                
                const errorElement = document.getElementById('passwordError');
                if (password.length === 0) {
                    this.classList.remove('error', 'valid');
                    errorElement.style.display = 'none';
                } else if (isValidPassword(password)) {
                    this.classList.remove('error');
                    this.classList.add('valid'); // ✅ GREEN BORDER
                    errorElement.style.display = 'none';
                } else {
                    this.classList.add('error');
                    this.classList.remove('valid');
                }
                
                checkFormValidity(); // ✅ ADD THIS
            });
            
            // Real-time confirm password validation
           confirmPasswordInput.addEventListener('input', function() {
			    const password = passwordInput.value;
			    const confirmPassword = this.value;
			    const errorElement = document.getElementById('confirmPasswordError');
			    
			    if (confirmPassword.length === 0) {
			        this.classList.remove('error', 'valid');
			        errorElement.style.display = 'none';
			    } else if (password !== confirmPassword) {
			        this.classList.add('error');
			        this.classList.remove('valid');
			        errorElement.textContent = 'Passwords do not match';
			        errorElement.style.display = 'block';
			    } else {
			        this.classList.remove('error');
			        this.classList.add('valid'); // ✅ GREEN BORDER
			        errorElement.style.display = 'none';
			    }
			    
			    checkFormValidity(); // ✅ ADD THIS
			});
            
           positionRadios.forEach(radio => {
        	    radio.addEventListener('change', function() {
        	        checkFormValidity();
        	    });
        	});
           
           if (!phoneInput.value || phoneInput.value.trim() === '') {
        	    phoneInput.value = '+60 ';
        	}

        	// ✅ INITIALIZE TOOLTIP
        	checkFormValidity();

        	// ✅ TOOLTIP SHOW/HIDE EVENT LISTENERS
        	submitBtn.addEventListener('mouseenter', function() {
        	    if (this.disabled) {
        	        const tooltip = document.getElementById('add-manager-button-tooltip');
        	        if (tooltip) {
        	            const rect = this.getBoundingClientRect();
        	            tooltip.style.left = (rect.left + rect.width / 2) + 'px';
        	            tooltip.style.top = (rect.top - 10) + 'px';
        	            tooltip.style.transform = 'translate(-50%, -100%)';
        	            tooltip.classList.add('show');
        	        }
        	    }
        	});

        	submitBtn.addEventListener('mouseleave', function() {
        	    const tooltip = document.getElementById('add-manager-button-tooltip');
        	    if (tooltip) {
        	        tooltip.classList.remove('show');
        	    }
        	});

            
        	form.addEventListener('submit', function(e) {
        	    let isValid = true;
        	    
        	    document.querySelectorAll('.form-error').forEach(error => {
        	        error.style.display = 'none';
        	    });
        	    document.querySelectorAll('input').forEach(input => {
        	        input.classList.remove('error');
        	    });
        	    
        	    // Validate name with pattern
        	    if (!nameInput.value.trim()) {
        	        showError('nameError', 'Name is required');
        	        nameInput.classList.add('error');
        	        isValid = false;
        	    } else if (!isValidName(nameInput.value)) {
        	        showError('nameError', 'Name can only contain letters, spaces, hyphens, and apostrophes (min 2 characters)');
        	        nameInput.classList.add('error');
        	        isValid = false;
        	    }
        	    
        	    // Validate email
        	    if (!emailInput.value.trim()) {
        	        showError('emailError', 'Email is required');
        	        emailInput.classList.add('error');
        	        isValid = false;
        	    } else if (!isValidEmail(emailInput.value)) {
        	        showError('emailError', 'Please enter a valid email address');
        	        emailInput.classList.add('error');
        	        isValid = false;
        	    }
        	    
        	    // Validate phone
        	    if (!phoneInput.value.trim() || phoneInput.value.trim() === '+60 ' || phoneInput.value.trim() === '+60') {
        	        showError('phoneError', 'Phone number is required');
        	        phoneInput.classList.add('error');
        	        isValid = false;
        	    } else if (!isValidMalaysianPhone(phoneInput.value)) {
        	        showError('phoneError', 'Enter valid Malaysian phone in format: +60 1x-xxx xxxx');
        	        phoneInput.classList.add('error');
        	        isValid = false;
        	    }
        	    
        	    // Validate position
        	    const selectedPosition = document.querySelector('input[name="managerposition"]:checked');
        	    if (!selectedPosition) {
        	        showError('positionError', 'Please select a position');
        	        isValid = false;
        	    }
        	    
        	    // Validate password
        	    if (!passwordInput.value) {
        	        showError('passwordError', 'Password is required');
        	        passwordInput.classList.add('error');
        	        isValid = false;
        	    } else if (!isValidPassword(passwordInput.value)) {
        	        showError('passwordError', 'Password does not meet requirements');
        	        passwordInput.classList.add('error');
        	        isValid = false;
        	    }
        	    
        	    // Validate confirm password
        	    if (!confirmPasswordInput.value) {
        	        showError('confirmPasswordError', 'Please confirm your password');
        	        confirmPasswordInput.classList.add('error');
        	        isValid = false;
        	    } else if (passwordInput.value !== confirmPasswordInput.value) {
        	        showError('confirmPasswordError', 'Passwords do not match');
        	        confirmPasswordInput.classList.add('error');
        	        isValid = false;
        	    }
        	    
        	    if (!isValid) {
        	        e.preventDefault();
        	    } else {
        	        // Format phone for database (remove spaces/hyphens)
        	        if (phoneInput.value.trim() !== '' && phoneInput.value.trim() !== '+60 ') {
        	            phoneInput.value = phoneInput.value.replace(/[\s-]/g, '');
        	        }
        	        
        	        submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Creating Manager...';
        	        submitBtn.disabled = true;
        	    }
        	});
            
            function showError(elementId, message) {
                const errorElement = document.getElementById(elementId);
                errorElement.textContent = message;
                errorElement.style.display = 'block';
            }
            
        });
            
           
        function selectPosition(position) {
            // Clear all selections
            document.querySelectorAll('.role-option').forEach(option => {
                option.classList.remove('selected');
            });
            
            // Select the clicked position
            const radioButton = document.querySelector(`input[value="${position}"]`);
            if (radioButton) {
                radioButton.checked = true;
                radioButton.closest('.role-option').classList.add('selected');
            }
        }
    </script>
</body>
</html>
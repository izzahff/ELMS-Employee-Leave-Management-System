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
    <title>Add Admin - IMNSB Employee Leave Management System</title>
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
        
        .form-group input.error {
            border-color: #dc3545;
            box-shadow: 0 0 0 0.2rem rgba(220, 53, 69, 0.25);
        }
        
        .form-group input.valid {
		    border-color: #28a745;
		    box-shadow: 0 0 0 0.2rem rgba(40, 167, 69, 0.25);
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

        /* Phone input styling */
        .phone-format-help {
            font-size: 0.875rem;
            color: #6c757d;
            margin-top: 5px;
            font-style: italic;
            min-height: 20px;
        }

        .no-phone-option {
            display: flex;
            align-items: center;
            margin-top: 0.5rem;
            padding: 0.5rem;
            background: #f8f9fa;
            border-radius: 4px;
            border: 1px solid #dee2e6;
        }

        .no-phone-option input[type="checkbox"] {
            margin-right: 0.5rem;
            transform: scale(1.1);
        }

        .no-phone-option label {
            margin: 0;
            color: #6c757d;
            font-size: 0.875rem;
            cursor: pointer;
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

        /* Back button styling */
        .back-button {
            margin-bottom: 1rem;
        }

        .back-button .btn {
            display: inline-flex;
            align-items: center;
            gap: 0.5rem;
            padding: 0.6rem 1.2rem;
            font-size: 0.9rem;
            text-decoration: none;
            border-radius: 4px;
            transition: var(--transition);
        }

        .btn-back {
            background-color: var(--text-light);
            color: white;
            border: 1px solid var(--text-light);
        }

        .btn-back:hover {
            background-color: #545b62;
            color: white;
            border-color: #545b62;
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

        .form-buttons .btn i {
            font-size: 0.9rem;
        }

        /* Ensure consistent button styling */
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
		    background-color: #6c757d !important;
		    border-color: #6c757d !important;
		    opacity: 0.6;
		    cursor: not-allowed;
		}
		
		.btn-primary:disabled:hover {
		    background-color: #6c757d !important;
		    border-color: #6c757d !important;
		    transform: none;
		}
		
		.btn-primary:disabled {
		    background-color: #6c757d !important;
		    border-color: #6c757d !important;
		    opacity: 0.6;
		    cursor: not-allowed;
		}
		
		.btn-primary:disabled:hover {
		    background-color: #6c757d !important;
		    border-color: #6c757d !important;
		    transform: none;
		}
		
		/* ✅ ADD THIS - Tooltip for disabled Add Admin button */
		#add-admin-button-tooltip {
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
		
		#add-admin-button-tooltip.show {
		    opacity: 1;
		    visibility: visible;
		}
		
		/* Arrow for tooltip */
		#add-admin-button-tooltip::after {
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
                <li class="active">
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
                    <h2>Add Admin</h2>
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
                        <h3><i class="fas fa-user-plus"></i> New Admin Details</h3>
                        <p>Create a new administrator account for the system</p>
                    </div>
                    <div class="admin-card-content">
                        <form id="addAdminForm" class="admin-form" action="/ELMS_3.0/AdminAddAdminController" method="post">
                            
                            <div class="form-group">
                                <label for="adminName">
                                    <i class="fas fa-user"></i> Admin Name <span style="color: red;">*</span>
                                </label>
                                <input type="text" id="adminName" name="adminName" required 
                                       value="${param.adminName}" placeholder="Enter full name">
                                <div class="form-error" id="nameError"></div>
                            </div>
                            
                            <div class="form-group">
                                <label for="adminEmail">
                                    <i class="fas fa-envelope"></i> Admin Email <span style="color: red;">*</span>
                                </label>
                                <input type="email" id="adminEmail" name="adminEmail" required 
                                       value="${param.adminEmail}" placeholder="Enter email address">
                                <div class="form-error" id="emailError"></div>
                            </div>
                            
                            <div class="form-group">
							    <label for="adminPhone">
							        <i class="fas fa-phone"></i> Mobile Number <span style="color: red;">*</span>
							    </label>
							    <input type="tel" id="adminPhone" name="adminphone"
							           value="${param.adminphone != null && param.adminphone != '' ? param.adminphone : '+60 '}" 
							           placeholder="+60 12-345 6789" 
							           maxlength="17" required>
							    <div class="phone-format-help" id="phoneHelp">
							        Format: +60 1x-xxx xxxx (Malaysian mobile number)
							    </div>
							    <div class="form-error" id="phoneError"></div>
							</div>
                            
                            <div class="form-group">
                                <label for="adminPassword">
                                    <i class="fas fa-lock"></i> Password <span style="color: red;">*</span>
                                </label>
                                <div class="password-input-container">
                                    <input type="password" id="adminPassword" name="adminPassword" required 
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
                                <a href="<%= request.getContextPath() %>/admin-dashboard" class="btn btn-secondary">
                                    <i class="fas fa-times"></i> Cancel
                                </a>
                               <button type="submit" class="btn btn-primary" id="submitBtn" disabled>
								    <i class="fas fa-user-plus"></i> Add Admin
								</button>
                            </div>
                        </form>
                    </div>
                </div>
                <div id="add-admin-button-tooltip"></div>
            </div>
        </main>
    </div>

    <!-- JavaScript -->
    <script src="/ELMS_3.0/Admin/Admin.js"></script>
    <script>
        document.addEventListener('DOMContentLoaded', function() {
            const form = document.getElementById('addAdminForm');
            const nameInput = document.getElementById('adminName');
            const emailInput = document.getElementById('adminEmail');
            const phoneInput = document.getElementById('adminPhone');
            const passwordInput = document.getElementById('adminPassword');
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
                if (value === '') return true;
                
                if (value.startsWith('+60')) {
                    return value.length >= 12 && value.length <= 13;
                } else if (value.startsWith('60')) {
                    return value.length >= 11 && value.length <= 12;
                } else if (value.startsWith('0')) {
                    return value.length >= 10 && value.length <= 11;
                } else {
                    return value.length >= 9 && value.length <= 10;
                }
            }

            function isValidPassword(password) {
                return password.length >= 8 && 
                       /[A-Z]/.test(password) && 
                       /[a-z]/.test(password) && 
                       /[0-9]/.test(password);
            }

            // ✅ ADD FORM VALIDITY CHECK FUNCTION
            function checkFormValidity() {
			    const name = nameInput.value.trim();
			    const email = emailInput.value.trim();
			    const phone = phoneInput.value.trim();
			    const password = passwordInput.value;
			    const confirmPassword = confirmPasswordInput.value;
			    
			    const isNameValid = isValidName(name);
			    const isEmailValid = isValidEmail(email);
			    const isPhoneValid = isValidMalaysianPhone(phone) && phone !== '';
			    const isPasswordValid = isValidPassword(password);
			    const isConfirmPasswordValid = confirmPassword && password === confirmPassword;
			    
			    // Get tooltip element
			    const tooltip = document.getElementById('add-admin-button-tooltip');
			    
			    // Enable button only if ALL fields are valid
			    if (isNameValid && isEmailValid && isPhoneValid && isPasswordValid && isConfirmPasswordValid) {
			        submitBtn.disabled = false;
			        
			        // Hide tooltip
			        if (tooltip) {
			            tooltip.classList.remove('show');
			            tooltip.innerHTML = '';
			        }
			        
			        console.log('✅ All fields valid - Add Admin button ENABLED');
			    } else {
			        submitBtn.disabled = true;
			        
			        // ✅ BUILD LIST OF INCOMPLETE FIELDS
			        const incompleteFields = [];
			        if (!isNameValid) incompleteFields.push('Name');
			        if (!isEmailValid) incompleteFields.push('Email');
			        if (!isPhoneValid) incompleteFields.push('Mobile Number');
			        if (!isPasswordValid) incompleteFields.push('Password');
			        if (!isConfirmPasswordValid && password) incompleteFields.push('Confirm Password');
			        
			        // ✅ UPDATE TOOLTIP CONTENT
			        if (tooltip && incompleteFields.length > 0) {
			            tooltip.innerHTML = 'Please complete:<br>• ' + incompleteFields.join('<br>• ');
			        } else if (tooltip) {
			            tooltip.textContent = 'Please complete all required fields';
			        }
			        
			        console.log('❌ Some fields invalid - Add Admin button DISABLED');
			        console.log('  Name Valid:', isNameValid);
			        console.log('  Email Valid:', isEmailValid);
			        console.log('  Phone Valid:', isPhoneValid);
			        console.log('  Password Valid:', isPasswordValid);
			        console.log('  Confirm Password Valid:', isConfirmPasswordValid);
			    }
			}
            
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
            
            // Phone number formatting and validation (simplified like working edit page)
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
		
		// Prevent backspace from deleting +60
		phoneInput.addEventListener('keydown', function(e) {
		    const cursorPosition = this.selectionStart;
		    if (e.key === 'Backspace' && cursorPosition <= 4) {
		        e.preventDefault();
		        this.value = '+60 ';
		        this.setSelectionRange(4, 4);
		        checkFormValidity(); // ✅ CHECK VALIDITY AFTER BACKSPACE
		    }
		});
		// Set cursor to end of +60 when field is focused
		phoneInput.addEventListener('focus', function() {
		    if (this.value === '+60 ' || this.value === '+60') {
		        this.value = '+60 ';
		        setTimeout(() => {
		            this.setSelectionRange(4, 4);
		        }, 0);
		    }
		});

            
            function validatePhoneNumber(value) {
                const phoneError = document.getElementById('phoneError');
                const phoneHelp = document.getElementById('phoneHelp');
                
                if (value === '') {
                    phoneError.style.display = 'none';
                    phoneInput.classList.remove('error');
                    phoneHelp.innerHTML = '';
                    return true;
                }
                
                let isValid = false;
                let helpText = '';
                
                if (value.startsWith('+60')) {
                    if (value.length >= 12 && value.length <= 13) {
                        isValid = true;
                        helpText = '✓ Valid Malaysian number format';
                    } else {
                        helpText = 'Format: +60123456789 (12-13 digits)';
                    }
                } else if (value.startsWith('60')) {
                    if (value.length >= 11 && value.length <= 12) {
                        isValid = true;
                        helpText = '✓ Valid Malaysian number format';
                    } else {
                        helpText = 'Format: 60123456789 (11-12 digits)';
                    }
                } else if (value.startsWith('0')) {
                    if (value.length >= 10 && value.length <= 11) {
                        isValid = true;
                        helpText = '✓ Valid Malaysian number format';
                    } else {
                        helpText = 'Format: 0123456789 (10-11 digits)';
                    }
                } else {
                    if (value.length >= 9 && value.length <= 10) {
                        isValid = true;
                        helpText = '✓ Valid Malaysian number format';
                    } else {
                        helpText = 'Format: 123456789 (9-10 digits)';
                    }
                }
                
                if (isValid) {
                    phoneError.style.display = 'none';
                    phoneInput.classList.remove('error');
                    phoneHelp.innerHTML = helpText;
                    phoneHelp.style.color = '#28a745';
                } else {
                    phoneHelp.innerHTML = helpText;
                    phoneHelp.style.color = '#6c757d';
                }
                
                return isValid;
            }
            function formatPhoneForSubmission(phoneValue) {
                if (!phoneValue || phoneValue.trim() === '' || phoneValue === '+60 ') {
                    return '';
                }
                
                // Remove spaces and hyphens for database storage
                // Format: +60 1x-xxx xxxx → +60123456789
                return phoneValue.replace(/[\s-]/g, '');
            }
            
            function isValidMalaysianPhone(value) {
                // Must start with +60 and match pattern: +60 1x-xxx xxxx
                const malaysianPhonePattern = /^\+60\s1[0-9]-[0-9]{3}\s[0-9]{4}$/;
                return malaysianPhonePattern.test(value);
            }
            
            // Real-time password validation
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
                
                checkFormValidity();
                
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
			    
			    checkFormValidity(); // ✅ CHECK FORM VALIDITY
			});
            
            
            
        // ✅ INITIALIZE PHONE FIELD WITH +60 ON PAGE LOAD
           if (!phoneInput.value || phoneInput.value.trim() === '') {
               phoneInput.value = '+60 ';
           }

           // ✅ INITIALIZE TOOLTIP WITH DEFAULT MESSAGE ON PAGE LOAD
           checkFormValidity();

           // ✅ TOOLTIP SHOW/HIDE EVENT LISTENERS
           submitBtn.addEventListener('mouseenter', function() {
               if (this.disabled) {
                   const tooltip = document.getElementById('add-admin-button-tooltip');
                   if (tooltip) {
                       // Position tooltip above button
                       const rect = this.getBoundingClientRect();
                       tooltip.style.left = (rect.left + rect.width / 2) + 'px';
                       tooltip.style.top = (rect.top - 10) + 'px';
                       tooltip.style.transform = 'translate(-50%, -100%)';
                       
                       // Show tooltip
                       tooltip.classList.add('show');
                   }
               }
           });

           submitBtn.addEventListener('mouseleave', function() {
               const tooltip = document.getElementById('add-admin-button-tooltip');
               if (tooltip) {
                   tooltip.classList.remove('show');
               }
           });
            
            // Form submission validation - UPDATED VERSION
            form.addEventListener('submit', function(e) {
                let isValid = true;
                
                // Clear previous errors
                document.querySelectorAll('.form-error').forEach(error => {
                    error.style.display = 'none';
                });
                document.querySelectorAll('input').forEach(input => {
                    input.classList.remove('error');
                });
                
             // Validate name
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
                
             // Validate phone - must be in +60 1x-xxx xxxx format
                if (!phoneInput.value.trim() || phoneInput.value.trim() === '+60 ' || phoneInput.value.trim() === '+60') {
                    showError('phoneError', 'Phone number is required');
                    phoneInput.classList.add('error');
                    isValid = false;
                } else if (!isValidMalaysianPhone(phoneInput.value)) {
                    showError('phoneError', 'Enter valid Malaysian phone in format: +60 1x-xxx xxxx');
                    phoneInput.classList.add('error');
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
                    // Format phone number for database (remove spaces/hyphens)
                    if (phoneInput.value.trim() !== '' && phoneInput.value.trim() !== '+60 ') {
                        phoneInput.value = formatPhoneForSubmission(phoneInput.value);
                        console.log('📱 Phone formatted for database:', phoneInput.value);
                    }
                    
                    // Show loading state
                    submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Creating Admin...';
                    submitBtn.disabled = true;
                }

            });
            
            function showError(elementId, message) {
                const errorElement = document.getElementById(elementId);
                errorElement.textContent = message;
                errorElement.style.display = 'block';
            }
            
          
        });
    </script>
</body>
</html>
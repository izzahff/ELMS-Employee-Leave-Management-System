<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="icon" type="image/png" href="/ELMS_3.0/imnsb_logo.png">
    <title>Sign Up - IMNSB Employee Leave Management System</title>
    <!-- Fixed CSS paths to be absolute -->
    <link rel="stylesheet" href="/ELMS_3.0/css/styles.css">
    <link rel="stylesheet" href="/ELMS_3.0/css/auth.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <style>
        /* Background fix for full page coverage */
        body {
            background-attachment: fixed;
            min-height: 100vh;
        }
        
        /* Disabled button style */
		.btn:disabled {
		    cursor: not-allowed;
		    background-color: #6c757d !important;
		    color: rgba(255, 255, 255, 0.6) !important;
		    /* NO opacity! */
		}
		
		.btn:disabled:hover {
		    background-color: #6c757d !important;
		    transform: none;
		}
        
        /* Password requirements styling with thin font */
        .password-requirements {
            font-size: 0.875rem;
            color: #6c757d;
            margin-top: 5px;
            font-weight: 300;
        }
        
        .password-requirements strong {
            font-weight: 500;
        }
        
        .password-requirements ul {
            margin: 5px 0;
            padding-left: 20px;
        }
        
        .password-requirements li {
            margin: 2px 0;
            font-weight: 300;
        }
        
        .password-requirements li.valid {
            color: #28a745;
        }
        
        .password-requirements li.invalid {
            color: #dc3545;
        }

        /* Phone number formatting help */
        .phone-format-help {
            font-size: 0.875rem;
            color: #6c757d;
            margin-top: 5px;
            font-weight: 300;
        }

        .phone-format-help.valid {
            color: #28a745;
        }

        .phone-format-help.invalid {
            color: #dc3545;
        }
        
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
            40% { color: black; text-shadow: .25em 0 0 rgba(0,0,0,0), .5em 0 0 rgba(0,0,0,0); }
            60% { text-shadow: .25em 0 0 black, .5em 0 0 rgba(0,0,0,0); }
            80%, 100% { text-shadow: .25em 0 0 black, .5em 0 0 black; }
        }

        /* Password toggle styles */
        .password-group {
            position: relative;
        }

        .password-toggle {
            position: absolute;
            right: 15px;
            top: 12px; /* Fixed positioning to align with input field */
            cursor: pointer;
            color: #666;
            font-size: 16px;
            user-select: none;
            transition: color 0.3s ease;
            z-index: 10; /* Ensure it stays above other elements */
        }

        .password-toggle:hover {
            color: #333;
        }

        .password-group input {
            padding-right: 45px;
        }

        /* Ensure password requirements don't interfere with icon positioning */
        .password-requirements {
            position: relative;
            z-index: 1;
        }
        
        @keyframes shake {
		    0%, 100% { transform: translateX(0); }
		    25% { transform: translateX(-10px); }
		    75% { transform: translateX(10px); }
		}
		
		.shake {
		    animation: shake 0.4s ease-in-out;
		    border-color: #dc3545 !important;
		}
		
		/* Tooltip-style error message */
		.field-reminder {
		    position: absolute;
		    top: -40px;
		    left: 0;
		    background: #dc3545;
		    color: white;
		    padding: 8px 12px;
		    border-radius: 4px;
		    font-size: 0.85rem;
		    font-weight: 400;
		    white-space: nowrap;
		    z-index: 1000;
		    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
		    animation: fadeInDown 0.3s ease-in-out;
		}
		
		@keyframes fadeInDown {
		    from {
		        opacity: 0;
		        transform: translateY(-10px);
		    }
		    to {
		        opacity: 1;
		        transform: translateY(0);
		    }
		}
		
		.field-reminder::after {
		    content: '';
		    position: absolute;
		    bottom: -5px;
		    left: 20px;
		    width: 0;
		    height: 0;
		    border-left: 5px solid transparent;
		    border-right: 5px solid transparent;
		    border-top: 5px solid #dc3545;
		}
		
		/* Ensure form-group has relative positioning for tooltip */
		.form-group {
		    position: relative;
		}
		
		#signup-button-tooltip {
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
		
		#signup-button-tooltip.show {
		    opacity: 1;
		    visibility: visible;
		}
		
		/* Arrow for tooltip */
		#signup-button-tooltip::after {
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
		
        
        
    </style>
</head>
<body>
    <div class="container">
        <div class="auth-card">
            <div class="logo-container">
                <img src="/ELMS_3.0/imnsb_logo.png" alt="IMNSB Logo" class="logo">
                <div class="logo-text">
                    <h1>Welcome to IMNSB</h1>
                    <p>Employee Leave Management System</p>
                    <p>Employee Portal</p>
                </div>
            </div>

            <div class="auth-form">
                <h2>Employee Sign Up</h2>
                <div class="auth-links">
                    <a href="#" class="auth-link active">Sign Up</a>
                    <a href="/ELMS_3.0/Employee/EmployeeLogin.jsp" class="auth-link">Log In</a>
                </div>

                <!-- Display error message if present -->
                <% if (request.getAttribute("errorMessage") != null) { %>
                    <div class="error-message" style="color: red; margin-bottom: 15px; padding: 10px; border: 1px solid red; border-radius: 4px; background-color: #ffe6e6;">
                        <strong>Error:</strong> <%= request.getAttribute("errorMessage") %>
                    </div>
                <% } %>

                <!-- Fixed form action to point to servlet URL -->
                <form action="/ELMS_3.0/employeeSignUp" method="post" id="signupForm">
               
                    <!-- All fields in one step for now -->
                    <div id="step1" class="form-step active">
                        <div class="form-group">
                            <input type="text" id="employeename" name="employeename" placeholder="Employee Name" 
                                   value="<%= request.getAttribute("employeename") != null ? request.getAttribute("employeename") : "" %>" required>
                            <div class="form-error" id="nameError"></div>
                        </div>
                        
                        <div class="form-group">
                            <input type="email" id="email" name="employeeemail" placeholder="Employee Email" 
                                   value="<%= request.getAttribute("employeeemail") != null ? request.getAttribute("employeeemail") : "" %>" required>
                            <div class="form-error" id="emailError"></div>
                        </div>
                        
                        <div class="form-group">
						    <input type="tel" id="phone" name="employeenophone" 
						           value="${param.employeenophone != null && param.employeenophone != '' ? param.employeenophone : '+60 '}" 
						           placeholder="+60 12-345 6789" 
						           maxlength="17" required>
						    <div class="phone-format-help" id="phoneHelp">Format: +60 1x-xxx xxxx (Malaysian mobile number)</div>
						    <div class="form-error" id="phoneError"></div>
						</div>
                        
                        <div class="form-group password-group">
                            <input type="password" id="password" name="employeepassword" placeholder="Password" required>
                            <span class="password-toggle" onclick="togglePassword('password', 'toggleIconPassword')">
                                <i id="toggleIconPassword" class="fas fa-eye"></i>
                            </span>
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
                        
                        <div class="form-group password-group">
                            <input type="password" id="confirmPassword" name="confirmPassword" placeholder="Confirm Password" required>
                            <span class="password-toggle" onclick="togglePassword('confirmPassword', 'toggleIconConfirm')">
                                <i id="toggleIconConfirm" class="fas fa-eye"></i>
                            </span>
                            <div class="form-error" id="confirmPasswordError"></div>
                        </div>
                        
                        <!-- Buttons inside the step div -->
                        <div class="form-group buttons-group">
                         <button type="submit" class="btn btn-primary" id="submitBtn" disabled>
						    Sign Up
						</button>

                        </div>
                    </div>
                </form>
                
                 <div id="signup-button-tooltip"></div>

                <div class="back-link">
                    <a href="/ELMS_3.0/">&larr; Change Role</a>
                </div>

               
            </div>
        </div>
    </div>

    <script>
        // Password toggle functionality
        function togglePassword(passwordFieldId, iconId) {
            const passwordInput = document.getElementById(passwordFieldId);
            const toggleIcon = document.getElementById(iconId);
            
            if (passwordInput.type === 'password') {
                passwordInput.type = 'text';
                toggleIcon.classList.remove('fa-eye');
                toggleIcon.classList.add('fa-eye-slash');
            } else {
                passwordInput.type = 'password';
                toggleIcon.classList.remove('fa-eye-slash');
                toggleIcon.classList.add('fa-eye');
            }
        }

        document.addEventListener('DOMContentLoaded', function() {
            const form = document.getElementById('signupForm');
            const nameInput = document.getElementById('employeename');
            const emailInput = document.getElementById('email');
            const phoneInput = document.getElementById('phone');
            const passwordInput = document.getElementById('password');
            const confirmPasswordInput = document.getElementById('confirmPassword');
            const submitBtn = document.getElementById('submitBtn');
            
            // Password requirements elements
            const lengthReq = document.getElementById('length');
            const uppercaseReq = document.getElementById('uppercase');
            const lowercaseReq = document.getElementById('lowercase');
            const numberReq = document.getElementById('number');
            
            // Phone help element
            const phoneHelp = document.getElementById('phoneHelp');
			
            function isValidName(name) {
                return /^[a-zA-Z\s'-]+$/.test(name) && name.length >= 2 && name.length <= 100;
            }
            
            function isValidEmail(email) {
                return /^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/.test(email);
            }
            
            function isValidMalaysianPhone(phone) {
                // Must match exact format: +60 1x-xxx xxxx
                return /^\+60\s1[0-9]-[0-9]{3}\s[0-9]{4}$/.test(phone);
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
                
                const isNameValid = name && isValidName(name);
                const isEmailValid = email && isValidEmail(email);
                const isPhoneValid = phone && isValidMalaysianPhone(phone);
                const isPasswordValid = password && isValidPassword(password);
                const isConfirmPasswordValid = confirmPassword && password === confirmPassword;
                
                // Get tooltip element
                const tooltip = document.getElementById('signup-button-tooltip');
                
                // Enable button only if ALL fields are valid
                if (isNameValid && isEmailValid && isPhoneValid && isPasswordValid && isConfirmPasswordValid) {
                    submitBtn.disabled = false;
                    submitBtn.style.cursor = 'pointer';
                    
                    // Hide tooltip
                    if (tooltip) {
                        tooltip.classList.remove('show');
                        tooltip.innerHTML = '';
                    }
                    
                    console.log('✅ All fields valid - Sign Up button ENABLED');
                } else {
                    submitBtn.disabled = true;
                    submitBtn.style.cursor = 'not-allowed';
                    
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
                    
                    console.log('❌ Some fields invalid - Sign Up button DISABLED');
                    console.log('  Name Valid:', isNameValid);
                    console.log('  Email Valid:', isEmailValid);
                    console.log('  Phone Valid:', isPhoneValid);
                    console.log('  Password Valid:', isPasswordValid);
                    console.log('  Confirm Password Valid:', isConfirmPasswordValid);
                }
            }
            
            submitBtn.addEventListener('mouseenter', function() {
                if (this.disabled) {
                    const tooltip = document.getElementById('signup-button-tooltip');
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
                const tooltip = document.getElementById('signup-button-tooltip');
                if (tooltip) {
                    tooltip.classList.remove('show');
                }
            });

            function checkPreviousFields(currentField) {
                const fields = [nameInput, emailInput, phoneInput, passwordInput, confirmPasswordInput];
                const currentIndex = fields.indexOf(currentField);
                
                for (let i = 0; i < currentIndex; i++) {
                    const field = fields[i];
                    const value = field.value.trim();
                    let shouldRemind = false;
                    let message = '';
                    
                    // Check if previous field is empty or invalid
                    if (field === nameInput && (!value || !isValidName(value))) {
                        shouldRemind = true;
                        message = 'Please fill in Name';
                    } else if (field === emailInput && (!value || !isValidEmail(value))) {
                        shouldRemind = true;
                        message = 'Please fill in Email';
                    } else if (field === phoneInput && (!value || !isValidMalaysianPhone(value))) {
                        shouldRemind = true;
                        message = 'Please fill in Mobile Number';
                    } else if (field === passwordInput && (!value || !isValidPassword(value))) {
                        shouldRemind = true;
                        message = 'Please fill in Password';
                    }
                    
                    if (shouldRemind) {
                        // Add shake animation to the incomplete field
                        field.classList.add('shake', 'error');
                        setTimeout(() => field.classList.remove('shake'), 400);
                        
                        // Show tooltip reminder on the incomplete field
                        showFieldReminder(field, message);
                        
                        // Don't prevent focus, just show reminder
                        // Break after first incomplete field found
                        break;
                    }
                }
            }

            // ✅ ADD THIS - Helper function to show tooltip reminder
            function showFieldReminder(field, message) {
                // Remove existing reminder if any
                const existingReminder = field.parentElement.querySelector('.field-reminder');
                if (existingReminder) {
                    existingReminder.remove();
                }
                
                // Create new reminder tooltip
                const reminder = document.createElement('div');
                reminder.className = 'field-reminder';
                reminder.textContent = message;
                field.parentElement.appendChild(reminder);
                
                // Auto-remove after 3 seconds
                setTimeout(() => {
                    if (reminder.parentElement) {
                        reminder.remove();
                    }
                }, 3000);
            }
            
            emailInput.addEventListener('focus', function() {
                checkPreviousFields(this);
                // Don't blur - just show reminder if needed
            });

            phoneInput.addEventListener('focus', function() {
                checkPreviousFields(this);
                // Don't blur - just show reminder if needed
            });

            passwordInput.addEventListener('focus', function() {
                checkPreviousFields(this);
                // Don't blur - just show reminder if needed
            });

            confirmPasswordInput.addEventListener('focus', function() {
                checkPreviousFields(this);
                // Don't blur - just show reminder if needed
            });

            
            // Name validation
            nameInput.addEventListener('input', function() {
                const name = this.value;
                const namePattern = /^[a-zA-Z\s'-]+$/;
                const errorElement = document.getElementById('nameError');
                
                if (name && (!namePattern.test(name) || name.length < 2)) {
                    this.classList.add('error');
                    this.classList.remove('valid');
                    errorElement.textContent = 'Name can only contain letters, spaces, hyphens, and apostrophes (min 2 characters)';
                    errorElement.style.display = 'block';
                } else if (name) {
                    this.classList.remove('error');
                    this.classList.add('valid');
                    errorElement.style.display = 'none';
                } else {
                    this.classList.remove('error', 'valid');
                    errorElement.style.display = 'none';
                }
                
                checkFormValidity(); //
            });

            // Email validation
            emailInput.addEventListener('input', function() {
                const email = this.value;
                const emailPattern = /^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/;
                const errorElement = document.getElementById('emailError');
                
                if (email && !emailPattern.test(email)) {
                    this.classList.add('error');
                    this.classList.remove('valid');
                    errorElement.textContent = 'Please enter a valid email address';
                    errorElement.style.display = 'block';
                } else if (email) {
                    this.classList.remove('error');
                    this.classList.add('valid');
                    errorElement.style.display = 'none';
                } else {
                    this.classList.remove('error', 'valid');
                    errorElement.style.display = 'none';
                }
                
                checkFormValidity();
            });

         // Phone number formatting and validation with LOCKED +60
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
                    phoneHelp.classList.remove('valid', 'invalid');
                    phoneHelp.textContent = 'Format: +60 1x-xxx xxxx (Malaysian mobile number)';
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
                
                // Validate format
                const malaysianPhonePattern = /^\+60\s1[0-9]-[0-9]{3}\s[0-9]{4}$/;
                const errorElement = document.getElementById('phoneError');
                
                if (formatted && malaysianPhonePattern.test(formatted)) {
                    this.classList.remove('error');
                    this.classList.add('valid');
                    phoneHelp.classList.add('valid');
                    phoneHelp.classList.remove('invalid');
                    phoneHelp.textContent = '✓ Valid Malaysian mobile number format';
                    errorElement.style.display = 'none';
                } else if (formatted.length > 4) {
                    // Check if number doesn't start with 1 (not a mobile number)
                    if (digitsOnly.length > 0 && !digitsOnly.startsWith('1')) {
                        this.classList.add('error');
                        this.classList.remove('valid');
                        phoneHelp.classList.add('invalid');
                        phoneHelp.classList.remove('valid');
                        phoneHelp.textContent = 'Invalid format';
                        errorElement.textContent = 'Mobile number must start with 1 (e.g., +60 12-345 6789)';
                        errorElement.style.display = 'block';
                    } 
                    // Number starts with 1 but incomplete
                    else if (digitsOnly.startsWith('1') && digitsOnly.length < 9) {
                        this.classList.add('error');
                        this.classList.remove('valid');
                        phoneHelp.classList.add('invalid');
                        phoneHelp.classList.remove('valid');
                        phoneHelp.textContent = 'Continue typing... Format: +60 1x-xxx xxxx';
                        errorElement.style.display = 'none';
                    }
                    // Number is complete but invalid format
                    else {
                        this.classList.add('error');
                        this.classList.remove('valid');
                        phoneHelp.classList.add('invalid');
                        phoneHelp.classList.remove('valid');
                        phoneHelp.textContent = 'Invalid format';
                        errorElement.textContent = 'Invalid mobile number format';
                        errorElement.style.display = 'block';
                    }
                } else {
                    this.classList.remove('error', 'valid');
                    phoneHelp.classList.remove('valid', 'invalid');
                    phoneHelp.textContent = 'Format: +60 1x-xxx xxxx (Malaysian mobile number)';
                    errorElement.style.display = 'none';
                }
                
                checkFormValidity(); // ✅ CHECK VALIDITY AFTER EVERY INPUT
            });
         
         // ✅ Prevent backspace from deleting +60 prefix
            phoneInput.addEventListener('keydown', function(e) {
                const cursorPosition = this.selectionStart;
                
                if (e.key === 'Backspace' && cursorPosition <= 4) {
                    e.preventDefault();
                    this.value = '+60 ';
                    this.setSelectionRange(4, 4);
                    checkFormValidity(); // ✅ CHECK VALIDITY AFTER BACKSPACE
                }
            });

         // ✅ Set cursor to end of +60 when field is focused
            phoneInput.addEventListener('focus', function() {
                if (this.value === '+60 ' || this.value === '+60' || this.value === '') {
                    this.value = '+60 ';
                    setTimeout(() => {
                        this.setSelectionRange(4, 4);
                    }, 0);
                }
            });

         
            // Prevent non-digit input in phone (except +, space, -)
            phoneInput.addEventListener('keypress', function(e) {
                const char = String.fromCharCode(e.which);
                if (!/[\d+\s-]/.test(char)) {
                    e.preventDefault();
                }
            });
            
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

                // Overall password validation
                const errorElement = document.getElementById('passwordError');
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
                
                checkFormValidity();
            });
            
            // Real-time confirm password validation
            confirmPasswordInput.addEventListener('input', function() {
                const password = passwordInput.value;
                const confirmPassword = this.value;
                const errorElement = document.getElementById('confirmPasswordError');
                
                if (confirmPassword && password !== confirmPassword) {
                    this.classList.add('error');
                    this.classList.remove('valid');
                    errorElement.textContent = 'Passwords do not match';
                    errorElement.style.display = 'block';
                } else if (confirmPassword && password === confirmPassword) {
                    this.classList.remove('error');
                    this.classList.add('valid');
                    errorElement.style.display = 'none';
                } else {
                    this.classList.remove('error', 'valid');
                    errorElement.style.display = 'none';
                }
                
                checkFormValidity();
            });
            
            // ✅ INITIALIZE PHONE FIELD WITH +60 ON PAGE LOAD
            if (!phoneInput.value || phoneInput.value.trim() === '') {
                phoneInput.value = '+60 ';
            }
            
            if (!phoneInput.value || phoneInput.value.trim() === '') {
                phoneInput.value = '+60 ';
            }

            // ✅ INITIALIZE TOOLTIP WITH DEFAULT MESSAGE ON PAGE LOAD
            const tooltip = document.getElementById('signup-button-tooltip');
            if (tooltip) {
                tooltip.innerHTML = 'Please complete:<br>• Name<br>• Email<br>• Mobile Number<br>• Password<br>• Confirm Password';
            }

            
            // Form submission validation
            form.addEventListener('submit', function(e) {
                console.log('Form submitted!');
                console.log('Employee Name:', nameInput.value);
                console.log('Email:', emailInput.value);
                console.log('Phone:', phoneInput.value);
                console.log('Password:', passwordInput.value ? 'Provided' : 'Missing');
                console.log('Confirm Password:', confirmPasswordInput.value ? 'Provided' : 'Missing');
                
                let isValid = true;
                
                // Clear previous errors
                document.querySelectorAll('.form-error').forEach(error => {
                    error.style.display = 'none';
                });
                document.querySelectorAll('input').forEach(input => {
                    input.classList.remove('error');
                });
                
                // Validate all fields
                const name = nameInput.value.trim();
                const email = emailInput.value.trim();
                const phone = phoneInput.value.trim();
                const password = passwordInput.value;
                const confirmPassword = confirmPasswordInput.value;
                
                // Name validation
                if (!name || !isValidName(name)) {
                    showError('nameError', 'Please enter a valid name (letters, spaces, hyphens, apostrophes only, min 2 characters)');
                    nameInput.classList.add('error');
                    isValid = false;
                }
                
                // Email validation
                if (!email || !isValidEmail(email)) {
                    showError('emailError', 'Please enter a valid email address');
                    emailInput.classList.add('error');
                    isValid = false;
                }
                
                // Phone validation
                if (!phone || !isValidMalaysianPhone(phone)) {
                    showError('phoneError', 'Please enter a valid Malaysian phone number in format: +60 1x-xxx xxxx');
                    phoneInput.classList.add('error');
                    isValid = false;
                }
                
                // Password validation
                if (!password || !isValidPassword(password)) {
                    showError('passwordError', 'Password must be at least 8 characters with uppercase, lowercase, and number');
                    passwordInput.classList.add('error');
                    isValid = false;
                }
                
                // Confirm password validation
                if (password !== confirmPassword) {
                    showError('confirmPasswordError', 'Passwords do not match');
                    confirmPasswordInput.classList.add('error');
                    isValid = false;
                }
                
                if (!isValid) {
                    e.preventDefault();
                    return false;
                }

                // Show loading state
                submitBtn.disabled = true;
                submitBtn.classList.add('loading');
                submitBtn.textContent = 'Creating Account';
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
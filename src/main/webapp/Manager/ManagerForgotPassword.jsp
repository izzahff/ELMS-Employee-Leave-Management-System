<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<link rel="icon" type="image/png" href="/ELMS_3.0/imnsb_logo.png">
<title>Manager Forgot Password - IMNSB Employee Leave Management System</title>
<link rel="stylesheet" href="/ELMS_3.0/css/styles.css">
<link rel="stylesheet" href="/ELMS_3.0/css/auth.css">
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
</head>
<body>
	<div class="container">
		<div class="auth-card">
			<div class="logo-container">
				<img src="/ELMS_3.0/imnsb_logo.png" alt="IMNSB Logo" class="logo">
				<div class="logo-text">
					<h1>Welcome to IMNSB</h1>
					<p>Employee Leave Management System</p>
					<p>Manager Password Recovery</p>
				</div>
			</div>
			
			<div class="auth-form">
				<h2 id="form-title">Manager Forgot Password</h2>
				
				<div class="auth-links">
					<a href="/ELMS_3.0/Manager/ManagerLogin.jsp" class="auth-link">← Back to Manager Login</a>
				</div>

				<!-- Display dynamic messages -->
				<% 
				    String errorMessage = (String) request.getAttribute("errorMessage");
				    String successMessage = (String) request.getAttribute("successMessage");
				    String infoMessage = (String) request.getAttribute("infoMessage");
				    
				    if (errorMessage != null && !errorMessage.trim().isEmpty()) {
				%>
				    <div class="error-message">
				         <%= errorMessage %>
				    </div>
				<% 
				    }
				    if (successMessage != null && !successMessage.trim().isEmpty()) {
				%>
				    <div class="success-message">
				         <%= successMessage %>
				    </div>
				<% 
				    }
				    if (infoMessage != null && !infoMessage.trim().isEmpty()) {
				%>
				    <div class="info-message">
				        <%= infoMessage %>
				    </div>
				<% } %>

				<!-- Step 1: Email Input Form -->
				<div id="email-form" class="step-form">
					<div class="step-indicator">
						<div class="step active">
							<div class="step-number">1</div>
							<div class="step-label">Enter Email</div>
						</div>
						<div class="step-divider"></div>
						<div class="step">
							<div class="step-number">2</div>
							<div class="step-label">Verify OTP</div>
						</div>
						<div class="step-divider"></div>
						<div class="step">
							<div class="step-number">3</div>
							<div class="step-label">New Password</div>
						</div>
					</div>
					
					<div class="form-description">
						<p>Enter your manager email address and we'll send you an OTP to reset your password.</p>
					</div>
					
					<form action="/ELMS_3.0/ManagerForgotPasswordController" method="post" id="emailForm">
						<input type="hidden" name="action" value="sendOTP">
						<div class="form-group">
							<label for="email">Manager Email Address</label>
							<input type="email" 
							       id="email" 
							       name="email" 
							       placeholder="Enter your registered manager email" 
							       value="<%= request.getAttribute("email") != null ? request.getAttribute("email") : "" %>"
							       required>
						</div>
						<div class="form-group">
							<button type="submit" class="btn btn-primary">
								<i class="fas fa-paper-plane"></i> Send OTP
							</button>
						</div>
					</form>
				</div>

				<!-- Step 2: OTP Verification Form -->
				<div id="otp-form" class="step-form" style="display: none;">
					<div class="step-indicator">
						<div class="step completed">
							<div class="step-number"><i class="fas fa-check"></i></div>
							<div class="step-label">Enter Email</div>
						</div>
						<div class="step-divider"></div>
						<div class="step active">
							<div class="step-number">2</div>
							<div class="step-label">Verify OTP</div>
						</div>
						<div class="step-divider"></div>
						<div class="step">
							<div class="step-number">3</div>
							<div class="step-label">New Password</div>
						</div>
					</div>
					
					<div class="form-description">
						<p>Enter the 6-digit OTP sent to your manager email address.</p>
						<p class="email-sent">OTP sent to: <span id="sent-email"></span></p>
					</div>
					
					<form action="/ELMS_3.0/ManagerForgotPasswordController" method="post" id="otpForm">
						<input type="hidden" name="action" value="verifyOTP">
						<input type="hidden" name="email" id="hidden-email" value="">
						<div class="form-group">
							<label for="otp">Enter OTP</label>
							<div class="otp-input-group">
								<input type="text" 
								       id="otp" 
								       name="otp" 
								       placeholder="Enter 6-digit OTP" 
								       maxlength="6"
								       pattern="[0-9]{6}"
								       required>
								<button type="button" class="btn btn-secondary" id="resendOTP">
									<i class="fas fa-redo"></i> Resend OTP
								</button>
							</div>
						</div>
						<div class="form-group">
							<button type="submit" class="btn btn-primary">
								<i class="fas fa-shield-alt"></i> Verify OTP
							</button>
						</div>
					</form>
					
					
				</div>

				<!-- Step 3: New Password Form -->
				<div id="password-form" class="step-form" style="display: none;">
					<div class="step-indicator">
						<div class="step completed">
							<div class="step-number"><i class="fas fa-check"></i></div>
							<div class="step-label">Enter Email</div>
						</div>
						<div class="step-divider"></div>
						<div class="step completed">
							<div class="step-number"><i class="fas fa-check"></i></div>
							<div class="step-label">Verify OTP</div>
						</div>
						<div class="step-divider"></div>
						<div class="step active">
							<div class="step-number">3</div>
							<div class="step-label">New Password</div>
						</div>
					</div>
					
					<div class="form-description">
						<p>Create a new password for your manager account.</p>
					</div>
					
					<form action="/ELMS_3.0/ManagerForgotPasswordController" method="post" id="passwordForm">
						<input type="hidden" name="action" value="resetPassword">
						<input type="hidden" name="email" id="hidden-email-2" value="">
						<input type="hidden" name="otp" id="hidden-otp" value="">
						
						<div class="form-group">
					    <label for="newPassword">New Password</label>
					    <div class="password-input-container">
					        <input type="password" 
					               id="newPassword" 
					               name="newPassword" 
					               placeholder="Enter new password" 
					               minlength="8"
					               required>
					        <span class="password-toggle" onclick="togglePassword('newPassword', 'toggleIcon1')">
					            <i id="toggleIcon1" class="fas fa-eye"></i>
					        </span>
					    </div>
					</div>
					
					<div class="form-group">
					    <label for="confirmPassword">Confirm Password</label>
					    <div class="password-input-container">
					        <input type="password" 
					               id="confirmPassword" 
					               name="confirmPassword" 
					               placeholder="Confirm new password" 
					               minlength="8"
					               required>
					        <span class="password-toggle" onclick="togglePassword('confirmPassword', 'toggleIcon2')">
					            <i id="toggleIcon2" class="fas fa-eye"></i>
					        </span>
					    </div>
					</div>
						
						<div class="password-strength">
							<div class="strength-bar">
								<div class="strength-fill" id="strength-fill"></div>
							</div>
							<p class="strength-text" id="strength-text">Password strength: <span id="strength-level">Weak</span></p>
						</div>
						
						
						
						<div class="form-group">
							<button type="submit" class="btn btn-primary">
								<i class="fas fa-key"></i> Reset Manager Password
							</button>
						</div>
					</form>
				</div>
			</div>
		</div>
	</div>

	<!-- Add CSS for manager forgot password specific styling -->
	<style>
	
	 body {
            background-attachment: fixed;
            min-height: 100vh;
        }
        
         .password-input-container {
		    position: relative;
		    display: block;
		    width: 100%;
		}

	/* Input field styling */
	.password-input-container input {
	    width: 100%;
	    padding: 12px 50px 12px 15px !important; /* Extra right padding for the icon */
	    border: 2px solid #e1e5e9;
	    border-radius: 8px;
	    font-size: 16px;
	    transition: all 0.3s ease;
	    box-sizing: border-box;
	    background: white;
	}
	
	.password-input-container input:focus {
	    outline: none;
	    border-color: #dc3545;
	    box-shadow: 0 0 0 3px rgba(220, 53, 69, 0.1);
	}
	
	/* Eye icon positioning - FIXED */
	.password-toggle {
	    position: absolute;
	    right: 15px;
	    top: 50%;
	    transform: translateY(-50%);
	    cursor: pointer;
	    color: #666;
	    font-size: 18px;
	    user-select: none;
	    transition: all 0.3s ease;
	    z-index: 10;
	    display: flex;
	    align-items: center;
	    justify-content: center;
	    width: 24px;
	    height: 24px;
	    background: transparent;
	    border: none;
	    padding: 0;
	    pointer-events: auto; /* Ensure it's clickable */
	}
	
	.password-toggle:hover {
	    color: #333;
	    transform: translateY(-50%) scale(1.1);
	}
	
	.password-toggle:active {
	    transform: translateY(-50%) scale(0.95);
	}
	
	/* Base message styling */
	.error-message {
	    background: #ffe6e6;
	    color: #d63031;
	    padding: 15px;
	    border-radius: 8px;
	    margin-bottom: 20px;
	    border: 1px solid #fab1a0;
	    font-size: 14px;
	    text-align: center;
	    border-left: 4px solid #d63031;
	}

	.success-message {
	    background: #e8f5e8;
	    color: #00b894;
	    padding: 15px;
	    border-radius: 8px;
	    margin-bottom: 20px;
	    border: 1px solid #81ecec;
	    font-size: 14px;
	    text-align: center;
	    border-left: 4px solid #00b894;
	}

	.info-message {
	    background: #e8f4fd;
	    color: #0984e3;
	    padding: 15px;
	    border-radius: 8px;
	    margin-bottom: 20px;
	    border: 1px solid #74b9ff;
	    font-size: 14px;
	    text-align: center;
	    border-left: 4px solid #0984e3;
	}

	/* Step indicator styling - Manager theme (Orange) */
	.step-indicator {
	    display: flex;
	    justify-content: space-between;
	    align-items: center;
	    margin-bottom: 30px;
	    padding: 0 20px;
	}

	.step {
	    display: flex;
	    flex-direction: column;
	    align-items: center;
	    flex: 1;
	}

	.step-number {
	    width: 40px;
	    height: 40px;
	    border-radius: 50%;
	    display: flex;
	    align-items: center;
	    justify-content: center;
	    font-weight: bold;
	    font-size: 16px;
	    margin-bottom: 8px;
	    border: 2px solid #ddd;
	    color: #666;
	    background: white;
	}

	.step.active .step-number {
	    background: #fd7e14;
	    color: white;
	    border-color: #fd7e14;
	}

	.step.completed .step-number {
	    background: #28a745;
	    color: white;
	    border-color: #28a745;
	}

	.step-label {
	    font-size: 12px;
	    color: #666;
	    text-align: center;
	}

	.step.active .step-label {
	    color: #fd7e14;
	    font-weight: bold;
	}

	.step.completed .step-label {
	    color: #28a745;
	}

	.step-divider {
	    flex: 1;
	    height: 2px;
	    background: #ddd;
	    margin: 0 10px;
	    margin-top: -20px;
	}

	/* Form description */
	.form-description {
	    text-align: center;
	    margin-bottom: 25px;
	}

	.form-description p {
	    color: #666;
	    margin-bottom: 10px;
	}

	.email-sent {
	    background: #e8f4fd;
	    color: #0984e3;
	    padding: 10px;
	    border-radius: 6px;
	    border: 1px solid #74b9ff;
	    font-size: 14px;
	}

	.email-sent span {
	    font-weight: bold;
	}

	/* OTP input styling */
	.otp-input-group {
	    display: flex;
	    gap: 10px;
	    align-items: center;
	}

	.otp-input-group input {
	    flex: 1;
	    text-align: center;
	    font-size: 18px;
	    letter-spacing: 2px;
	    font-weight: bold;
	}

	.otp-input-group .btn {
	    padding: 12px 16px;
	    font-size: 14px;
	    white-space: nowrap;
	    transition: all 0.3s ease;
	}
	
	.otp-input-group .btn:disabled {
	    opacity: 0.5;
	    cursor: not-allowed;
	}

	/* Timer styling */
	.otp-timer {
	    text-align: center;
	    margin-top: 20px;
	    padding: 15px;
	    background: #f8f9fa;
	    border-radius: 8px;
	    border: 1px solid #dee2e6;
	}

	.otp-timer p {
	    color: #495057;
	    font-size: 16px;
	    margin: 0;
	    font-weight: 500;
	}

	#timer {
	    font-weight: bold;
	    font-size: 20px;
	    color: #28a745;
	    transition: color 0.3s ease;
	    display: inline !important;
	    visibility: visible !important;
	    font-family: 'Courier New', monospace;
	    letter-spacing: 1px;
	}
	
	/* Timer message styling */
	.timer-message {
	    margin-top: 10px;
	    padding: 10px;
	    border-radius: 5px;
	    font-size: 14px;
	    text-align: center;
	}
	
	.timer-message.error-message {
	    background: #f8d7da;
	    color: #721c24;
	    border: 1px solid #f5c6cb;
	}
	
	.timer-message.warning-message {
	    background: #fff3cd;
	    color: #856404;
	    border: 1px solid #ffeaa7;
	}
	
	.timer-message.success-message {
	    background: #d4edda;
	    color: #155724;
	    border: 1px solid #c3e6cb;
	}

	/* Password field styling */
	.password-field {
	    position: relative;
	}

	.password-toggle {
	    position: absolute;
	    right: 15px;
	    top: 50%;
	    transform: translateY(-50%);
	    cursor: pointer;
	    color: #666;
	    font-size: 16px;
	    user-select: none;
	    transition: color 0.3s ease;
	}

	.password-toggle:hover {
	    color: #333;
	}

	.password-field input {
	    padding-right: 45px !important;
	}

	/* Password strength indicator */
	.password-strength {
	    margin-top: 10px;
	    margin-bottom: 20px;
	}

	.strength-bar {
	    height: 4px;
	    background: #e9ecef;
	    border-radius: 2px;
	    overflow: hidden;
	    margin-bottom: 8px;
	}

	.strength-fill {
	    height: 100%;
	    transition: width 0.3s ease, background-color 0.3s ease;
	    width: 0%;
	    background: #dc3545;
	}

	.strength-text {
	    font-size: 12px;
	    color: #666;
	    margin: 0;
	}

	#strength-level {
	    font-weight: bold;
	}

	/* Manager security notice */
	.manager-security-notice {
	    margin: 20px 0;
	}

	.security-notice {
	    background: #fff3cd;
	    border: 1px solid #ffeaa7;
	    border-left: 4px solid #fd7e14;
	    padding: 15px;
	    border-radius: 8px;
	    display: flex;
	    align-items: flex-start;
	    gap: 10px;
	}

	.security-notice i {
	    color: #fd7e14;
	    font-size: 18px;
	    margin-top: 2px;
	}

	.security-notice p {
	    margin: 0;
	    color: #856404;
	    font-size: 14px;
	}

	/* Form labels */
	.form-group label {
	    display: block;
	    margin-bottom: 8px;
	    font-weight: 600;
	    color: #333;
	    font-size: 14px;
	}

	/* Responsive design */
	@media (max-width: 768px) {
	    .step-indicator {
	        padding: 0 10px;
	    }
	    
	    .step-number {
	        width: 35px;
	        height: 35px;
	        font-size: 14px;
	    }
	    
	    .step-label {
	        font-size: 11px;
	    }
	    
	    .otp-input-group {
	        flex-direction: column;
	        gap: 15px;
	    }
	    
	    .otp-input-group .btn {
	        width: 100%;
	    }
	    
	    .security-notice {
	        flex-direction: column;
	        text-align: center;
	    }
	}
	</style>

	<script>
	// Password visibility toggle
	function togglePassword(fieldId, iconId) {
	    const passwordField = document.getElementById(fieldId);
	    const toggleIcon = document.getElementById(iconId);
	    
	    if (passwordField.type === 'password') {
	        passwordField.type = 'text';
	        toggleIcon.classList.remove('fa-eye');
	        toggleIcon.classList.add('fa-eye-slash');
	    } else {
	        passwordField.type = 'password';
	        toggleIcon.classList.remove('fa-eye-slash');
	        toggleIcon.classList.add('fa-eye');
	    }
	}

	// Password strength checker
	function checkPasswordStrength(password) {
	    let strength = 0;
	    const checks = [
	        password.length >= 6,
	        /[a-z]/.test(password),
	        /[A-Z]/.test(password),
	        /[0-9]/.test(password),
	        /[^A-Za-z0-9]/.test(password)
	    ];
	    
	    strength = checks.filter(Boolean).length;
	    
	    const strengthFill = document.getElementById('strength-fill');
	    const strengthLevel = document.getElementById('strength-level');
	    
	    if (strength === 0) {
	        strengthFill.style.width = '0%';
	        strengthFill.style.backgroundColor = '#dc3545';
	        strengthLevel.textContent = 'Weak';
	        strengthLevel.style.color = '#dc3545';
	    } else if (strength <= 2) {
	        strengthFill.style.width = '25%';
	        strengthFill.style.backgroundColor = '#dc3545';
	        strengthLevel.textContent = 'Weak';
	        strengthLevel.style.color = '#dc3545';
	    } else if (strength <= 3) {
	        strengthFill.style.width = '50%';
	        strengthFill.style.backgroundColor = '#ffc107';
	        strengthLevel.textContent = 'Fair';
	        strengthLevel.style.color = '#ffc107';
	    } else if (strength <= 4) {
	        strengthFill.style.width = '75%';
	        strengthFill.style.backgroundColor = '#17a2b8';
	        strengthLevel.textContent = 'Good';
	        strengthLevel.style.color = '#17a2b8';
	    } else {
	        strengthFill.style.width = '100%';
	        strengthFill.style.backgroundColor = '#28a745';
	        strengthLevel.textContent = 'Strong';
	        strengthLevel.style.color = '#28a745';
	    }
	}

	// OTP Timer
	let timerInterval;
	let isTimerRunning = false;
	
	function startOTPTimer(duration) {
	    console.log('Starting manager OTP timer with duration:', duration);
	    
	    const timer = document.getElementById('timer');
	    const resendButton = document.getElementById('resendOTP');
	    
	    if (!timer) {
	        console.error('Timer element not found!');
	        return;
	    }
	    
	    // Prevent multiple timers
	    if (isTimerRunning) {
	        console.log('Timer already running, stopping previous timer');
	        clearInterval(timerInterval);
	    }
	    
	    isTimerRunning = true;
	    let time = duration;
	    
	    // Enable/disable resend button based on timer
	    if (resendButton) {
	        resendButton.disabled = true;
	        resendButton.style.opacity = '0.5';
	    }
	    
	    // Update timer display immediately
	    updateTimerDisplay(time, timer);
	    
	    // Create interval that updates every second
	    timerInterval = setInterval(() => {
	        time--;
	        
	        // Check if timer element still exists
	        const currentTimer = document.getElementById('timer');
	        if (!currentTimer) {
	            console.error('Timer element disappeared!');
	            clearInterval(timerInterval);
	            isTimerRunning = false;
	            return;
	        }
	        
	        updateTimerDisplay(time, currentTimer);
	        
	        if (time <= 0) {
	            clearInterval(timerInterval);
	            isTimerRunning = false;
	            
	            currentTimer.textContent = 'Expired';
	            currentTimer.style.color = '#dc3545';
	            currentTimer.style.fontWeight = 'bold';
	            
	            // Enable resend button
	            const currentResendButton = document.getElementById('resendOTP');
	            if (currentResendButton) {
	                currentResendButton.disabled = false;
	                currentResendButton.style.opacity = '1';
	                currentResendButton.innerHTML = '<i class="fas fa-redo"></i> Resend OTP';
	            }
	            
	            // Show expiry message
	            showTimerMessage('Manager OTP has expired. Please request a new one.', 'error');
	        }
	    }, 1000);
	    
	    console.log('Manager timer started successfully');
	}
	
	function stopTimer() {
	    if (timerInterval) {
	        clearInterval(timerInterval);
	        timerInterval = null;
	        isTimerRunning = false;
	        console.log('Manager timer stopped');
	    }
	}

	function updateTimerDisplay(time, timer) {
	    if (!timer) {
	        console.error('Timer element not found in updateTimerDisplay');
	        return;
	    }
	    
	    // Ensure time is not negative
	    if (time < 0) time = 0;
	    
	    const minutes = Math.floor(time / 60);
	    const seconds = time % 60;
	    
	    // Format the display
	    const displayTime = `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
	    
	    // Set the timer text
	    timer.textContent = displayTime;
	    
	    // Force visibility
	    timer.style.display = 'inline';
	    timer.style.visibility = 'visible';
	    
	    console.log('Manager timer updated:', displayTime, 'Element visible:', timer.offsetParent !== null);
	    
	    // Change color based on remaining time
	    if (time <= 60) {
	        timer.style.color = '#dc3545'; // Red for last minute
	    } else if (time <= 120) {
	        timer.style.color = '#ffc107'; // Yellow for last 2 minutes
	    } else {
	        timer.style.color = '#28a745'; // Green for more than 2 minutes
	    }
	}

	function showTimerMessage(message, type) {
	    // Remove any existing timer message
	    const existingMessage = document.querySelector('.timer-message');
	    if (existingMessage) {
	        existingMessage.remove();
	    }
	    
	    // Create new message
	    const messageDiv = document.createElement('div');
	    messageDiv.className = `timer-message ${type}-message`;
	    messageDiv.textContent = message;
	    
	    // Insert after timer
	    const timerDiv = document.querySelector('.otp-timer');
	    if (timerDiv) {
	        timerDiv.appendChild(messageDiv);
	    }
	}

	// Show different form steps
	function showStep(stepNumber) {
	    console.log('Showing manager step:', stepNumber);
	    
	    // Stop any existing timer first
	    stopTimer();
	    
	    // Hide all forms
	    document.querySelectorAll('.step-form').forEach(form => {
	        form.style.display = 'none';
	    });
	    
	    const forms = ['email-form', 'otp-form', 'password-form'];
	    const targetForm = document.getElementById(forms[stepNumber - 1]);
	    
	    if (targetForm) {
	        targetForm.style.display = 'block';
	        console.log('Manager form displayed:', forms[stepNumber - 1]);
	    } else {
	        console.error('Target form not found:', forms[stepNumber - 1]);
	    }
	    
	    // Update form title
	    const titles = ['Manager Forgot Password', 'Verify Manager OTP', 'Create New Manager Password'];
	    const titleElement = document.getElementById('form-title');
	    if (titleElement) {
	        titleElement.textContent = titles[stepNumber - 1];
	    }
	    
	    // Start timer when showing OTP form
	    if (stepNumber === 2) {
	        console.log('Manager step 2 - preparing to start timer');
	        
	        // Wait for DOM to be fully rendered
	        setTimeout(() => {
	            const timer = document.getElementById('timer');
	            if (timer) {
	                console.log('Manager timer element found, starting timer');
	                
	                // Ensure timer is visible
	                timer.style.display = 'inline';
	                timer.style.visibility = 'visible';
	                
	                // Start the timer
	                startOTPTimer(300); // 5 minutes in seconds
	            } else {
	                console.error('Manager timer element not found after DOM render');
	            }
	        }, 200);
	    }
	}

	// Form validation and submission
	document.addEventListener('DOMContentLoaded', function() {
	    // Check URL parameters to determine which step to show
	    const urlParams = new URLSearchParams(window.location.search);
	    const step = urlParams.get('step');
	    const email = urlParams.get('email');
	    const message = urlParams.get('message');
	    
	    if (step === '2' && email) {
	        console.log('Loading manager OTP step with email:', email);
	        showStep(2);
	        
	        // Set email display and hidden field
	        const sentEmailElement = document.getElementById('sent-email');
	        const hiddenEmailElement = document.getElementById('hidden-email');
	        
	        if (sentEmailElement) {
	            sentEmailElement.textContent = email;
	        }
	        if (hiddenEmailElement) {
	            hiddenEmailElement.value = email;
	        }
	        
	        // Show success message based on URL parameter
	        setTimeout(() => {
	            if (message === 'otp_sent') {
	                showTimerMessage('Manager OTP has been sent to your email address', 'success');
	            } else if (message === 'otp_resent') {
	                showTimerMessage('New manager OTP has been sent to your email address', 'success');
	            }
	        }, 500);
	        
	    } else if (step === '3' && email) {
	        showStep(3);
	        const hiddenEmailElement2 = document.getElementById('hidden-email-2');
	        if (hiddenEmailElement2) {
	            hiddenEmailElement2.value = email;
	        }
	    }
	    
	    // Manual timer start button for testing
	    if (window.location.search.includes('step=2')) {
	        // Ensure timer starts even if automatic detection fails
	        setTimeout(() => {
	            const timer = document.getElementById('timer');
	            if (timer && timer.textContent === '05:00') {
	                console.log('Manager timer not started automatically, starting manually');
	                startOTPTimer(300);
	            }
	        }, 1000);
	    }
	    
	    // Password strength checking
	    const newPasswordField = document.getElementById('newPassword');
	    if (newPasswordField) {
	        newPasswordField.addEventListener('input', function() {
	            checkPasswordStrength(this.value);
	        });
	    }
	    
	    // Password confirmation validation
	    const confirmPasswordField = document.getElementById('confirmPassword');
	    if (confirmPasswordField) {
	        confirmPasswordField.addEventListener('input', function() {
	            const newPassword = document.getElementById('newPassword').value;
	            if (this.value !== newPassword) {
	                this.setCustomValidity('Passwords do not match');
	            } else {
	                this.setCustomValidity('');
	            }
	        });
	    }
	    
	    // OTP input formatting
	    const otpField = document.getElementById('otp');
	    if (otpField) {
	        otpField.addEventListener('input', function() {
	            // Only allow numbers
	            this.value = this.value.replace(/[^0-9]/g, '');
	            
	            // Limit to 6 digits
	            if (this.value.length > 6) {
	                this.value = this.value.slice(0, 6);
	            }
	        });
	        
	        // Focus on OTP field when it's visible
	        if (otpField.offsetParent !== null) {
	            otpField.focus();
	        }
	    }
	    
	    // Resend OTP functionality
	    const resendButton = document.getElementById('resendOTP');
	    if (resendButton) {
	        resendButton.addEventListener('click', function() {
	            const email = document.getElementById('hidden-email').value;
	            if (email) {
	                // Show loading state
	                this.disabled = true;
	                this.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Sending...';
	                
	                // Redirect to resend OTP
	                window.location.href = '/ELMS_3.0/ManagerForgotPasswordController?action=resendOTP&email=' + encodeURIComponent(email);
	            }
	        });
	    }
	    
	    // Form submission enhancements
	    const otpForm = document.getElementById('otpForm');
	    if (otpForm) {
	        otpForm.addEventListener('submit', function(e) {
	            const otp = document.getElementById('otp').value;
	            if (otp.length !== 6) {
	                e.preventDefault();
	                showTimerMessage('Please enter a valid 6-digit OTP', 'error');
	                return false;
	            }
	            
	            // Show loading state
	            const submitButton = this.querySelector('button[type="submit"]');
	            if (submitButton) {
	                submitButton.disabled = true;
	                submitButton.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Verifying...';
	            }
	        });
	    }
	    
	    // Password form validation
	    const passwordForm = document.getElementById('passwordForm');
	    if (passwordForm) {
	        passwordForm.addEventListener('submit', function(e) {
	            const newPassword = document.getElementById('newPassword').value;
	            const confirmPassword = document.getElementById('confirmPassword').value;
	            
	            if (newPassword.length < 6) {
	                e.preventDefault();
	                alert('Manager password must be at least 6 characters long');
	                return false;
	            }
	            
	            if (newPassword !== confirmPassword) {
	                e.preventDefault();
	                alert('Passwords do not match');
	                return false;
	            }
	            
	            // Show loading state
	            const submitButton = this.querySelector('button[type="submit"]');
	            if (submitButton) {
	                submitButton.disabled = true;
	                submitButton.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Resetting...';
	            }
	        });
	    }
	});
	
	// Debug functions for manager
	function debugManagerTimer() {
	    console.log('=== MANAGER TIMER DEBUG ===');
	    const timer = document.getElementById('timer');
	    console.log('Timer element found:', !!timer);
	    if (timer) {
	        console.log('Timer content:', timer.textContent);
	        console.log('Timer visible:', timer.offsetParent !== null);
	        console.log('Timer display:', timer.style.display);
	        console.log('Timer visibility:', timer.style.visibility);
	        console.log('Timer color:', timer.style.color);
	    }
	    
	    const otpForm = document.getElementById('otp-form');
	    console.log('OTP form found:', !!otpForm);
	    if (otpForm) {
	        console.log('OTP form display:', otpForm.style.display);
	        console.log('OTP form visible:', otpForm.offsetParent !== null);
	    }
	    
	    console.log('Current URL:', window.location.href);
	    console.log('URL params:', Object.fromEntries(new URLSearchParams(window.location.search)));
	    console.log('Timer running:', isTimerRunning);
	    
	    // Test timer start
	    if (timer) {
	        console.log('Testing manager timer start...');
	        startOTPTimer(10); // 10 second test
	    }
	}
	
	// Force timer to show
	function forceShowManagerTimer() {
	    const timer = document.getElementById('timer');
	    if (timer) {
	        timer.style.display = 'inline';
	        timer.style.visibility = 'visible';
	        timer.textContent = '05:00';
	        timer.style.color = '#28a745';
	        console.log('Manager timer forced to show');
	    }
	}
	
	// Make functions available globally
	window.debugManagerTimer = debugManagerTimer;
	window.forceShowManagerTimer = forceShowManagerTimer;
	window.startOTPTimer = startOTPTimer;
	</script>
</body>
</html>
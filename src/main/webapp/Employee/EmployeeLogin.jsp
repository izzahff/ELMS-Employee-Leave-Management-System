<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<link rel="icon" type="image/png" href="/ELMS_3.0/imnsb_logo.png">
<title>Login - IMNSB Employee Leave Management System</title>
<link rel="stylesheet" href="/ELMS_3.0/css/styles.css">
<link rel="stylesheet" href="/ELMS_3.0/css/auth.css">
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
</head>


<body>
	<div class="container">
	<div class="auth-card">
	<div class="logo-container">
	<img src="../imnsb_logo.png" alt="IMNSB Logo" class="logo">
	<div class="logo-text">
	<h1>Welcome to IMNSB</h1>
		<p>Employee Leave Management System</p>
		<p>Employee Portal<p>
		</div>
		</div>
		
			<div class="auth-form">
			<h2>Employee Log In</h2>
			<div class="auth-links">
			<a href="/ELMS_3.0/Employee/EmployeeSignUp.jsp" class="auth-link">Sign Up</a>
			<a href="#" class="auth-link active">Log In</a>
		</div>

<!-- Display dynamic messages from controller -->
<% 
    String errorMessage = (String) request.getAttribute("errorMessage");
    String successMessage = (String) request.getAttribute("successMessage");
    String infoMessage = (String) request.getAttribute("infoMessage");
    String urlMessage = request.getParameter("message");
    
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
<% 
    }
    if ("logout".equals(urlMessage)) {
%>
    <div class="success-message">
         You have been successfully logged out.
    </div>
<% 
    } else if ("passwordReset".equals(urlMessage)) {
%>
    <div class="success-message">
         🎉 Password reset successful! You can now log in with your new password.
    </div>
<% } %>

<form action="/ELMS_3.0/LoginController" id="loginForm" method="post">
<div class="form-group">
<input type="text" 
       id="employeeid" 
       name="employeeid" 
       placeholder="Employee ID or Email" 
       value="<%= request.getAttribute("employeeid") != null ? request.getAttribute("employeeid") : "" %>"
       required>
</div>
<div class="form-group password-field">
<input type="password" 
       id="employeepassword" 
       name="employeepassword" 
       placeholder="Password" 
       required>
<span class="password-toggle" onclick="togglePassword()">
    <i id="toggleIcon" class="fas fa-eye"></i>
</span>
</div>
<div class="form-group forgot-password">
<a href="/ELMS_3.0/Employee/EmployeeForgotPassword.jsp" class="forgot-link">Forgot Password?</a>
</div>
<div class="form-group">
<button type="submit" class="btn btn-primary">Log In</button>
</div>
</form>

<div class="back-link">
<a href="/ELMS_3.0/index.html">&larr; Change Role</a>
</div>
</div>
</div>
</div>

<!-- Add CSS for message styling and password toggle -->
<style>

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

/* Password field with eye icon */
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

.password-toggle svg {
    width: 20px;
    height: 20px;
}

/* Ensure input has enough padding for the icon */
.password-field input {
    padding-right: 45px !important;
}

/* Forgot password link styling */
.forgot-password {
    text-align: center;
    margin-top: -10px;
    margin-bottom: 20px;
}

.forgot-link {
    color: #007bff;
    text-decoration: none;
    font-size: 14px;
    transition: color 0.3s ease;
}

.forgot-link:hover {
    color: #0056b3;
    text-decoration: underline;
}
</style>

<script>
// Password visibility toggle
function togglePassword() {
    const passwordField = document.getElementById('employeepassword');
    const toggleIcon = document.getElementById('toggleIcon');
    
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

// Form validation
document.getElementById('loginForm').addEventListener('submit', function(e) {
	const employeeid = document.getElementById('employeeid').value.trim();
	const employeepassword = document.getElementById('employeepassword').value.trim();

	if (!employeeid || !employeepassword) {
		e.preventDefault();
		alert('Please enter both Employee ID/Email and Password');
		return false;
	}
});

// Auto-focus on employee ID field
window.onload = function() {
	document.getElementById('employeeid').focus();
};
</script>
</body>
</html>
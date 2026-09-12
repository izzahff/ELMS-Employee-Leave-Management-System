<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="icon" type="image/png" href="/ELMS_3.0/imnsb_logo.png">
    <title>Manager Login - IMNSB Employee Leave Management System</title>
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
                    <p>Manager Portal</p>
                </div>
            </div>

            <div class="auth-form">
                <h2>Manager Log In</h2>
                <div class="auth-links">
                    <a href="#" class="auth-link active">Log In</a>
                </div>

                <!-- Uniform message display -->
                <%
                    String errorMessage = (String) request.getAttribute("errorMessage");
                    String successMessage = (String) request.getAttribute("successMessage");
                    String infoMessage = (String) request.getAttribute("infoMessage");
                    String urlMessage = request.getParameter("message");

                    if (errorMessage != null && !errorMessage.trim().isEmpty()) {
                %>
                    <div class="error-message"><%= errorMessage %></div>
                <%
                    }
                    if (successMessage != null && !successMessage.trim().isEmpty()) {
                %>
                    <div class="success-message"><%= successMessage %></div>
                <%
                    }
                    if (infoMessage != null && !infoMessage.trim().isEmpty()) {
                %>
                    <div class="info-message"><%= infoMessage %></div>
                <%
                    }
                    if ("logout".equals(urlMessage)) {
                %>
                    <div class="success-message">You have been successfully logged out.</div>
                <% 
                    } else if ("passwordReset".equals(urlMessage)) {
                %>
                    <div class="success-message">
                         🎉 Manager password reset successful! You can now log in with your new password.
                    </div>
                <% } %>

                <!-- Login Form -->
                <form id="loginForm" method="post" action="/ELMS_3.0/ManagerLoginController">
                    <div class="form-group">
                        <input type="text" id="managerId" name="managerId" placeholder="Manager ID or Email" required>
                    </div>
                    <div class="form-group password-group">
                        <input type="password" id="managerPassword" name="managerPassword" placeholder="Password" required>
                        <span class="password-toggle" onclick="togglePassword()">
                            <i id="toggleIcon" class="fas fa-eye"></i>
                        </span>
                    </div>
                    <div class="form-group forgot-password">
                        <a href="/ELMS_3.0/Manager/ManagerForgotPassword.jsp" class="forgot-link">Forgot Password?</a>
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

    <!-- Shared message CSS -->
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

        /* Password toggle styles */
        .password-group {
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

        .password-group input {
            padding-right: 45px;
        }

        /* Forgot password link styling */
        .forgot-password {
            text-align: center;
            margin-top: -10px;
            margin-bottom: 20px;
        }

        .forgot-link {
            color: #fd7e14;
            text-decoration: none;
            font-size: 14px;
            transition: color 0.3s ease;
        }

        .forgot-link:hover {
            color: #e8590c;
            text-decoration: underline;
        }
    </style>

    <!-- Optional: Form Validation & Autofocus -->
    <script src="/ELMS_3.0/Manager/Manager.js"></script>
    <script>
        // Password toggle functionality
        function togglePassword() {
            const passwordInput = document.getElementById('managerPassword');
            const toggleIcon = document.getElementById('toggleIcon');
            
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

        document.getElementById('loginForm').addEventListener('submit', function(e) {
            const managerId = document.getElementById('managerId').value.trim();
            const managerPassword = document.getElementById('managerPassword').value.trim();

            if (!managerId || !managerPassword) {
                e.preventDefault();
                alert('Please enter both Manager ID/Email and Password');
                return false;
            }
        });

        window.onload = function() {
            document.getElementById('managerId').focus();
        };
    </script>
</body>
</html>
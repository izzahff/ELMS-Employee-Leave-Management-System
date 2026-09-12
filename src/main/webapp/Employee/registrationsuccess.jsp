<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="icon" type="image/png" href="/ELMS_3.0/imnsb_logo.png">
    <title>Registration Successful - Employee Management System</title>
    <!-- Fixed CSS paths to be absolute -->
    <link rel="stylesheet" href="/ELMS_3.0/css/styles.css">
    <link rel="stylesheet" href="/ELMS_3.0/css/auth.css">
    <style>
        .success-card {
            background: white;
            border-radius: 10px;
            padding: 40px;
            box-shadow: 0 4px 20px rgba(0,0,0,0.1);
            text-align: center;
            max-width: 500px;
            margin: 0 auto;
        }
        
        .success-icon {
            margin-bottom: 20px;
        }
        
        .success-info {
            margin: 30px 0;
        }
        
        .generated-id {
            background: #f8f9fa;
            border: 2px solid #4CAF50;
            border-radius: 8px;
            padding: 20px;
            margin: 20px 0;
        }
        
        .id-display {
            font-size: 24px;
            font-weight: bold;
            color: #4CAF50;
            margin: 10px 0;
            letter-spacing: 2px;
        }
        
        .action-buttons {
            margin-top: 30px;
        }
        
        .btn-primary, .btn-secondary {
            display: inline-block;
            padding: 12px 24px;
            margin: 0 10px;
            text-decoration: none;
            border-radius: 5px;
            font-weight: bold;
            transition: all 0.3s ease;
            margin-bottom: 1rem;
        }
        
        .btn-primary {
            background-color: #4CAF50;
            color: white;
        }
        
        .btn-primary:hover {
            background-color: #45a049;
        }
        
        .btn-secondary {
            background-color: #6c757d;
            color: white;
        }
        
        .btn-secondary:hover {
            background-color: #5a6268;
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="success-card">
            <div class="success-icon">
                <svg width="80" height="80" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <circle cx="12" cy="12" r="10" stroke="#4CAF50" stroke-width="2"/>
                    <path d="m9 12 2 2 4-4" stroke="#4CAF50" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
            </div>

            <h2>Registration Successful!</h2>

            <div class="success-info">
                <p><strong>Welcome, <%= request.getAttribute("employeename") != null ? request.getAttribute("employeename") : "Employee" %>!</strong></p>
                <p>Your employee account has been created successfully.</p>

                <div class="generated-id">
                    <p><strong>Your Employee ID:</strong></p>
                    <div class="id-display">
                        <%= request.getAttribute("employeeid") != null ? request.getAttribute("employeeid") : "N/A" %>
                    </div>
                    <small>Please save this ID for future reference</small>
                </div>
            </div>

            <div class="action-buttons">
                <a href="/ELMS_3.0/Employee/EmployeeLogin.jsp" class="btn-primary">Go to Login</a>
            </div>
        </div>
    </div>
</body>
</html>
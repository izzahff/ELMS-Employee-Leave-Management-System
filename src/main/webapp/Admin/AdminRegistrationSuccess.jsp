<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="icon" type="image/png" href="../imnsb_logo.png">
    <title>Admin Registration Successful - IMNSB Employee Leave Management System</title>
    <!-- Load your system CSS -->
    <link rel="stylesheet" href="/ELMS_3.0/css/styles.css">
    <link rel="stylesheet" href="/ELMS_3.0/css/auth.css">
    <link rel="stylesheet" href="/ELMS_3.0/Admin/AdminRegistrationSuccess.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
</head>
<body>
    <div class="container">
        <div class="success-card">
            <!-- Compact Header -->
            <div class="success-header">
                <div class="success-icon">
                    <i class="fas fa-check-circle"></i>
                </div>
                <h2>Registration Successful!</h2>
                <p>New administrator account created</p>
            </div>

            <!-- Admin Info Grid -->
            <div class="admin-info-grid">
                <div class="admin-details" style="grid-column: 1 / -1;">
                    <h4><i class="fas fa-user"></i> Admin Details</h4>
                    <div class="detail-item">
                        <span class="detail-label" style="display: inline; margin-right: 5px;">Name:</span>
                        <span class="detail-value">
                            <%= request.getAttribute("adminName") != null ? request.getAttribute("adminName") : "N/A" %>
                        </span>
                    </div>
                    <div class="detail-item">
                        <span class="detail-label" style="display: inline; margin-right: 5px;">Email:</span>
                        <span class="detail-value">
                            <%= request.getAttribute("adminEmail") != null ? request.getAttribute("adminEmail") : "N/A" %>
                        </span>
                    </div>
                    <div class="detail-item">
                        <span class="detail-label" style="display: inline; margin-right: 5px;">Phone:</span>
                        <span class="detail-value">
                            <%
                                String adminPhone = (String) request.getAttribute("adminPhone");
                                if (adminPhone != null && !adminPhone.trim().isEmpty()) {
                                    out.print(adminPhone);
                                } else {
                                    out.print("<em style='color: #6c757d; font-size: 0.75rem;'>Add after login</em>");
                                }
                            %>
                        </span>
                    </div>
                </div>
            </div>

            <!-- Compact ID Display -->
            <div class="generated-id">
                <p><strong><i class="fas fa-id-card"></i> Admin ID</strong></p>
                <div class="id-display">
                    <%= request.getAttribute("adminId") != null ? request.getAttribute("adminId") : "N/A" %>
                </div>
                <small><i class="fas fa-info-circle"></i> Required for login</small>
            </div>

            <!-- Compact Notes -->
            <div class="note">
                <strong><i class="fas fa-info-circle"></i> Quick Steps:</strong>
                <ul>
                    <li>Save Admin ID for login</li>
                    <li>Share login credentials securely</li>
                    <li>Recommend password change on first login</li>
                </ul>
            </div>

            <!-- Compact Action Buttons -->
            <div class="action-buttons">
                <a href="<%= request.getContextPath() %>/admin-dashboard" class="btn btn-primary">
                    <i class="fas fa-tachometer-alt"></i>Back to Admins Page
                </a>
                <a href="/ELMS_3.0/Admin/AdminAddAdmin.jsp" class="btn btn-success">
                    <i class="fas fa-user-plus"></i> Add Another
                </a>
               
            </div>
        </div>
    </div>
</body>
</html>
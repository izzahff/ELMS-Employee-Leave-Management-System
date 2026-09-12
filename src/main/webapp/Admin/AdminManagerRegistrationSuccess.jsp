<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="icon" type="image/png" href="../imnsb_logo.png">
    <title>Manager Registration Successful - IMNSB Employee Leave Management System</title>
    <!-- Load your system CSS -->
    <link rel="stylesheet" href="/ELMS_3.0/css/styles.css">
    <link rel="stylesheet" href="/ELMS_3.0/css/auth.css">
    <link rel="stylesheet" href="/ELMS_3.0/Admin/AdminRegistrationSuccess.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <style>
        /* Manager-specific styling overrides */
        .success-card {
            border-top: 3px solid #2ecc71;
        }
        
        .success-icon i {
            color: #2ecc71;
        }
        
        .generated-id {
            background: #2ecc71;
            background-color: #2ecc71 !important;
            border: 1px solid #27ae60;
        }
        
        .btn-primary {
            background-color: #4ecdc4;
            background: #4ecdc4 !important;
            border: 1px solid #4ecdc4;
        }
        
        .btn-primary:hover {
            background-color: #44a08d;
            background: #44a08d !important;
            border-color: #44a08d;
        }
        
        .position-badge {
            background-color: #e8f5e9;
            color: #2e7d32;
            padding: 0.2rem 0.6rem;
            border-radius: 12px;
            font-size: 0.7rem;
            font-weight: 600;
            display: inline-block;
            text-transform: uppercase;
            letter-spacing: 0.5px;
        }
        
        .position-badge i {
            margin-right: 0.4rem;
            font-size: 0.75rem;
        }
        
        .role-executive {
            background: linear-gradient(135deg, #ff6b6b, #ee5a52);
            color: white;
        }
        
        .role-project {
            background: linear-gradient(135deg, #4ecdc4, #44a08d);
            color: white;
        }

        .role-default {
            background-color: #6c757d;
            color: white;
        }

        /* Enhanced detail item styling - more compact */
        .detail-item {
            display: flex;
            align-items: center;
            margin-bottom: 6px;
            padding: 0.3rem 0;
            border-bottom: 1px solid #f0f0f0;
        }

        .detail-item:last-child {
            border-bottom: none;
            margin-bottom: 0;
        }

        .detail-label {
            font-size: 0.75rem;
            color: #6c757d;
            font-weight: 600;
            min-width: 70px;
            display: inline-flex;
            align-items: center;
        }

        .detail-value {
            font-size: 0.8rem;
            color: #333;
            font-weight: 500;
            flex: 1;
        }

        /* Position row special styling - more compact */
        .position-row {
            background: #f8f9fa;
            border-radius: 4px;
            padding: 0.5rem;
            margin: 0.3rem 0;
        }

        /* Viewport-constrained container - ensure no scrolling */
        .container {
            max-width: 420px;
            max-height: 95vh;
            margin: 2vh auto;
            padding: 0.5rem;
            overflow-y: auto;
            background: transparent;
        }

        .success-card {
            background: white;
            background-color: white !important;
            border: 1px solid #e0e0e0;
            border-radius: 10px;
            padding: 15px;
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
            border-top: 3px solid #2ecc71;
            height: auto;
            position: relative;
        }

        /* Compact everything for better fit */
        .success-header {
            text-align: center;
            margin-bottom: 12px;
        }

        .success-icon i {
            font-size: 2rem;
            color: #2ecc71;
            margin-bottom: 8px;
        }

        .success-header h2 {
            color: var(--text-color, #333);
            font-size: 1.2rem;
            margin-bottom: 4px;
        }

        .success-header p {
            color: var(--text-light, #6c757d);
            font-size: 0.85rem;
            margin: 0;
        }

        .admin-info-grid {
            margin: 12px 0;
        }

        .admin-details {
            background: #f8f9fa;
            background-color: #f8f9fa !important;
            border: 1px solid #dee2e6;
            border-radius: 6px;
            padding: 10px;
            position: relative;
        }

        .admin-details h4 {
            font-size: 0.75rem;
            color: var(--text-color, #333);
            margin-bottom: 6px;
            font-weight: 600;
        }

        .generated-id {
            background: #2ecc71;
            background-color: #2ecc71 !important;
            color: white;
            border-radius: 6px;
            padding: 12px 10px;
            text-align: center;
            margin: 12px 0;
            border: 1px solid #27ae60;
        }

        .generated-id p {
            font-size: 0.75rem;
            margin-bottom: 6px;
            opacity: 0.9;
            color: white;
        }

        .id-display {
            font-size: 1.1rem;
            font-weight: bold;
            letter-spacing: 1px;
            margin-bottom: 4px;
            color: white;
        }

        .generated-id small {
            font-size: 0.65rem;
            opacity: 0.8;
            color: white;
        }

        .note {
            background: #fff3cd;
            background-color: #fff3cd !important;
            border: 1px solid #ffeaa7;
            border-radius: 4px;
            padding: 8px;
            margin: 12px 0;
            font-size: 0.7rem;
            color: #856404;
        }

        .note strong {
            display: block;
            margin-bottom: 4px;
            font-size: 0.75rem;
        }

        .note ul {
            margin: 0;
            padding-left: 12px;
        }

        .note li {
            margin: 1px 0;
            line-height: 1.2;
            font-size: 0.65rem;
        }

        .action-buttons {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 6px;
            margin-top: 12px;
        }

        .action-buttons .btn-primary {
            grid-column: 1 / -1;
        }

        .btn {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            gap: 4px;
            padding: 6px 10px;
            font-weight: 500;
            text-align: center;
            border: none;
            border-radius: 4px;
            cursor: pointer;
            text-decoration: none;
            transition: var(--transition, all 0.3s ease);
            font-size: 0.75rem;
        }

        .btn i {
            font-size: 0.7rem;
        }

        /* Mobile optimization */
        @media (max-width: 768px) {
            .container {
                max-width: 95%;
                max-height: 97vh;
                margin: 1vh auto;
                padding: 0.25rem;
            }
            
            .success-card {
                padding: 12px;
            }
            
            .action-buttons {
                grid-template-columns: 1fr;
                gap: 4px;
            }
            
            .success-icon i {
                font-size: 1.8rem;
            }
            
            .success-header h2 {
                font-size: 1.1rem;
            }
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="success-card">
            <!-- Compact Header -->
            <div class="success-header">
                <div class="success-icon">
                    <i class="fas fa-check-circle"></i>
                </div>
                <h2>Manager Registration Successful!</h2>
                <p>New manager account created</p>
            </div>

            <!-- Manager Info Grid -->
            <div class="admin-info-grid">
                <div class="admin-details" style="grid-column: 1 / -1;">
                    <h4><i class="fas fa-user-tie"></i> Manager Details</h4>
                    
                    <div class="detail-item">
                        <span class="detail-label">Name:</span>
                        <span class="detail-value">
                            <%= request.getAttribute("managerName") != null ? request.getAttribute("managerName") : "N/A" %>
                        </span>
                    </div>
                    
                    <div class="detail-item">
                        <span class="detail-label">Email:</span>
                        <span class="detail-value">
                            <%= request.getAttribute("managerEmail") != null ? request.getAttribute("managerEmail") : "N/A" %>
                        </span>
                    </div>
                    
                    <div class="detail-item">
                        <span class="detail-label">Phone:</span>
                        <span class="detail-value">
                            <%
                                String managerPhone = (String) request.getAttribute("managerPhone");
                                if (managerPhone != null && !managerPhone.trim().isEmpty()) {
                                    out.print(managerPhone);
                                } else {
                                    out.print("<em style='color: #6c757d; font-size: 0.75rem;'>Add after login</em>");
                                }
                            %>
                        </span>
                    </div>
                    
                    <div class="detail-item position-row">
                        <span class="detail-label">Position:</span>
                        <span class="detail-value">
                            <%
                                String managerPosition = (String) request.getAttribute("managerPosition");
                                String positionClass = "";
                                String positionIcon = "";
                                String displayText = "";
                                
                                if ("Executive Director".equals(managerPosition)) {
                                    positionClass = "role-executive";
                                    positionIcon = "fas fa-crown";
                                    displayText = "Executive Director";
                                } else if ("Project Manager".equals(managerPosition)) {
                                    positionClass = "role-project";
                                    positionIcon = "fas fa-user-tie";
                                    displayText = "Project Manager";
                                } else {
                                    positionClass = "role-default";
                                    positionIcon = "fas fa-user";
                                    displayText = managerPosition != null ? managerPosition : "Manager";
                                }
                            %>
                            <span class="position-badge <%= positionClass %>">
                                <i class="<%= positionIcon %>"></i>
                                <%= displayText %>
                            </span>
                        </span>
                    </div>
                </div>
            </div>

            <!-- Compact ID Display -->
            <div class="generated-id">
                <p><strong><i class="fas fa-id-card"></i> Manager ID</strong></p>
                <div class="id-display">
                    <%= request.getAttribute("managerId") != null ? request.getAttribute("managerId") : "N/A" %>
                </div>
                <small><i class="fas fa-info-circle"></i> Required for login</small>
            </div>

            <!-- Compact Notes -->
            <div class="note">
                <strong><i class="fas fa-info-circle"></i> Quick Steps:</strong>
                <ul>
                    <li><i class="fas fa-save" style="margin-right: 0.5rem; color: #007bff;"></i>Save Manager ID for login</li>
                    <li><i class="fas fa-share" style="margin-right: 0.5rem; color: #28a745;"></i>Share login credentials securely with the manager</li>
                    <li><i class="fas fa-key" style="margin-right: 0.5rem; color: #ffc107;"></i>Recommend password change on first login</li>
                    <li><i class="fas fa-desktop" style="margin-right: 0.5rem; color: #6f42c1;"></i>Manager can access their portal with ID and password</li>
                </ul>
            </div>

            <!-- Compact Action Buttons -->
            <div class="action-buttons">
                <a href="/ELMS_3.0/Admin/AdminAddManager.jsp" class="btn btn-success">
                    <i class="fas fa-user-plus"></i> Add Another Manager
                </a>
                <a href="/ELMS_3.0/Admin/AdminViewManagerListController" class="btn btn-secondary">
                    <i class="fas fa-list"></i> View All Managers
                </a>
            </div>
        </div>
    </div>
</body>
</html>
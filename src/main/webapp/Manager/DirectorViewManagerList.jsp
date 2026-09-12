<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page session="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="elms.model.Manager" %>

<%
    // Check if manager is logged in and is executive director
    Manager manager = (Manager) session.getAttribute("manager");
    String userType = (String) session.getAttribute("userType");
    
    if (manager == null || !"executive_director".equals(userType)) {
        response.sendRedirect("/ELMS_3.0/Manager/ManagerLogin.jsp");
        return;
    }
    
    // Get manager information from session
    String managerName = manager.getManagername();
    if (managerName == null) managerName = "Executive Director";
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="icon" type="image/png" href="/ELMS_3.0/imnsb_logo.png">
    <title>Project Managers - IMNSB Executive Director Portal</title>
    <link rel="stylesheet" href="/ELMS_3.0/css/styles.css">
    <link rel="stylesheet" href="/ELMS_3.0/css/dashboard.css">
    <link rel="stylesheet" href="/ELMS_3.0/css/admin.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <style>
        .manager-list-container {
            background: white;
            border-radius: 8px;
            box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
            overflow: hidden;
        }

        .manager-list-header {
            background: linear-gradient(135deg, var(--primary-color), var(--primary-dark));
            color: white;
            padding: 1.5rem;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }

        .manager-list-header h3 {
            margin: 0;
            font-size: 1.5rem;
            display: flex;
            align-items: center;
            gap: 0.5rem;
        }

        .manager-list-header p {
            margin: 0.5rem 0 0 0;
            opacity: 0.9;
            font-size: 0.9rem;
        }

        .header-actions {
            display: flex;
            gap: 1rem;
            align-items: center;
        }

        .search-box {
            position: relative;
            display: flex;
            align-items: center;
        }

        .search-box input {
            padding: 0.5rem 1rem 0.5rem 2.5rem;
            border: 1px solid rgba(255, 255, 255, 0.3);
            border-radius: 20px;
            background: rgba(255, 255, 255, 0.1);
            color: white;
            font-size: 0.9rem;
            width: 250px;
            backdrop-filter: blur(10px);
        }

        .search-box input::placeholder {
            color: rgba(255, 255, 255, 0.7);
        }

        .search-box i {
            position: absolute;
            left: 0.8rem;
            color: rgba(255, 255, 255, 0.7);
            z-index: 10;
        }

        .manager-table-container {
            overflow-x: auto;
        }

        .manager-table {
            width: 100%;
            border-collapse: collapse;
            font-size: 0.9rem;
        }

        .manager-table th {
            background: #f8f9fa;
            color: #495057;
            padding: 1rem;
            text-align: left;
            font-weight: 600;
            border-bottom: 2px solid #dee2e6;
            white-space: nowrap;
        }

        .manager-table td {
            padding: 1rem;
            border-bottom: 1px solid #dee2e6;
            vertical-align: middle;
        }

        .manager-table tr {
            transition: all 0.3s ease;
        }

        .manager-table tr:hover {
            background: #f8f9fa;
        }

        .manager-avatar {
            width: 45px;
            height: 45px;
            border-radius: 50%;
            object-fit: cover;
            border: 2px solid #e9ecef;
        }

        .manager-info {
            display: flex;
            align-items: center;
            gap: 1rem;
        }

        .manager-details h4 {
            margin: 0;
            color: #495057;
            font-size: 1rem;
        }

        .manager-details p {
            margin: 0.2rem 0 0 0;
            color: #6c757d;
            font-size: 0.85rem;
        }

        .action-buttons {
            display: flex;
            gap: 0.5rem;
        }

        .btn-action {
            padding: 0.4rem 0.8rem;
            border: none;
            border-radius: 4px;
            cursor: pointer;
            font-size: 0.8rem;
            text-decoration: none;
            display: inline-flex;
            align-items: center;
            gap: 0.3rem;
            transition: all 0.3s ease;
        }

        .btn-delete {
            background: #dc3545;
            color: white;
        }

        .btn-delete:hover {
            background: #c82333;
            color: white;
        }

        .empty-state {
            text-align: center;
            padding: 3rem;
            color: #6c757d;
        }

        .empty-state i {
            font-size: 4rem;
            margin-bottom: 1rem;
            opacity: 0.5;
        }

        .empty-state h3 {
            margin: 0 0 0.5rem 0;
            color: #495057;
        }

        .empty-state p {
            margin: 0;
            font-size: 0.9rem;
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

        .alert-info {
            color: #0c5460;
            background-color: #d1ecf1;
            border-color: #bee5eb;
        }

        /* Modal Styles - Matching Admin Modal */
		.modal {
		    display: none;
		    position: fixed;
		    z-index: 1000;
		    left: 0;
		    top: 0;
		    width: 100%;
		    height: 100%;
		    background-color: rgba(0, 0, 0, 0.5);
		    align-items: center;
		    justify-content: center;
		}
		
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
		    border-radius: 10px;
		    max-width: 500px;
		    width: 90%;
		    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
		    animation: slideIn 0.3s ease-in-out;
		    overflow: hidden;
		}
		
		.popup-body {
		    padding: 2rem 1.5rem;
		}
		
		.manager-info-display {
		    background: #f8f9fa;
		    padding: 1rem;
		    border-radius: 6px;
		    margin: 1rem 0;
		    text-align: left;
		}
		
		.manager-info-display p {
		    margin: 0.5rem 0;
		    color: #495057;
		}
		
		.form-group {
		    margin-bottom: 1.5rem;
		}
		
		.form-group label {
		    display: block;
		    margin-bottom: 0.5rem;
		    font-weight: 600;
		    color: #333;
		}
		
		.form-group input[type="password"] {
		    width: 100%;
		    padding: 0.75rem;
		    border: 2px solid #ddd;
		    border-radius: 6px;
		    font-size: 1rem;
		    transition: border-color 0.3s ease;
		}
		
		.form-group input[type="password"]:focus {
		    outline: none;
		    border-color: #dc3545;
		    box-shadow: 0 0 0 3px rgba(220, 53, 69, 0.1);
		}
		
		.checkbox-label {
		    display: flex;
		    align-items: flex-start;
		    gap: 0.5rem;
		    cursor: pointer;
		    font-size: 0.95rem;
		    line-height: 1.4;
		}
		
		.checkbox-label input[type="checkbox"] {
		    margin-top: 0.2rem;
		    transform: scale(1.2);
		}
		
		.popup-footer {
		    padding: 1rem 1.5rem;
		    border-top: 1px solid #dee2e6;
		    display: flex;
		    gap: 1rem;
		    justify-content: center;
		    background: #f8f9fa;
		}

        .manager-info-card {
            background: #f8f9fa;
            padding: 1rem;
            margin-bottom: 1.5rem;
            border-radius: 0 4px 4px 0;
        }

        .manager-info-card h4 {
            margin: 0 0 0.5rem 0;
            color: #495057;
            font-size: 1.1rem;
        }

        .manager-info-card p {
            margin: 0.2rem 0;
            color: #6c757d;
            font-size: 0.9rem;
        }
        
        .warning-message {
            background: #fff3cd;
            border: 1px solid #ffeaa7;
            color: #856404;
            padding: 1rem;
            border-radius: 4px;
            margin-bottom: 1.5rem;
            text-align: center;
            display: block;
        }
        
        .warning-message i {
            display: block;
            font-size: 1.5rem;
            margin: 0 auto 0.5rem auto;
            color: #856404;
        }
          
        .confirmation-icon {
            text-align: center;
            margin: 1rem 0 1.5rem 0;
        }
        
        .password-input-group {
            margin-bottom: 1rem;
        }

        .password-input-group label {
            display: block;
            margin-bottom: 0.5rem;
            font-weight: 600;
            color: #495057;
        }

        .password-input-container {
            position: relative;
            display: flex;
            align-items: center;
        }

        .password-input {
            width: 100%;
            padding: 0.75rem 2.5rem 0.75rem 1rem;
            border: 2px solid #dee2e6;
            border-radius: 4px;
            font-size: 1rem;
            transition: border-color 0.3s ease;
        }

        .password-input:focus {
            outline: none;
            border-color: #dc3545;
            box-shadow: 0 0 0 0.2rem rgba(220, 53, 69, 0.25);
        }

        .password-toggle {
            position: absolute;
            right: 0.75rem;
            background: none;
            border: none;
            color: #6c757d;
            cursor: pointer;
            padding: 0.25rem;
            transition: color 0.3s ease;
        }

        .password-toggle:hover {
            color: #495057;
        }

        .error-message {
            color: #dc3545;
            font-size: 0.875rem;
            margin-top: 0.5rem;
            display: none;
            align-items: center;
            gap: 0.3rem;
        }

       

        .btn {
            padding: 0.5rem 1.5rem;
            border: none;
            border-radius: 4px;
            cursor: pointer;
            text-decoration: none;
            display: inline-flex;
            align-items: center;
            gap: 0.5rem;
            font-size: 0.9rem;
            font-weight: 500;
            transition: all 0.3s ease;
            min-width: 100px;
            justify-content: center;
        }

        .btn-secondary {
            background: #6c757d;
            color: white;
        }

        .btn-secondary:hover {
            background: #545b62;
            transform: translateY(-1px);
        }

        .btn-danger {
            background: #dc3545;
            color: white;
        }

        .btn-danger:hover {
            background: #c82333;
            transform: translateY(-1px);
        }

        .btn-danger:disabled {
            background: #e9ecef;
            color: #6c757d;
            cursor: not-allowed;
            transform: none;
        }

        .loading-spinner {
            display: none;
            width: 16px;
            height: 16px;
            border: 2px solid #ffffff;
            border-top: 2px solid transparent;
            border-radius: 50%;
            animation: spin 1s linear infinite;
        }

        @keyframes spin {
            0% { transform: rotate(0deg); }
            100% { transform: rotate(360deg); }
        }

        /* Animation classes */
        .modal.show {
            display: flex;
            animation: modalFadeIn 0.3s ease;
        }

        .modal.hide {
            animation: modalFadeOut 0.3s ease;
        }

        @keyframes modalFadeIn {
            from { opacity: 0; }
            to { opacity: 1; }
        }

        @keyframes modalFadeOut {
            from { opacity: 1; }
            to { opacity: 0; }
        }

        .modal-content.show {
            animation: modalSlideIn 0.3s ease;
        }

        @keyframes modalSlideIn {
            from {
                transform: translateY(-50px);
                opacity: 0;
            }
            to {
                transform: translateY(0);
                opacity: 1;
            }
        }

        .confirmation-step {
            text-align: center;
        }

        .password-step {
            animation: slideInUp 0.3s ease;
        }

        @keyframes slideInUp {
            from {
                transform: translateY(20px);
                opacity: 0;
            }
            to {
                transform: translateY(0);
                opacity: 1;
            }
        }

        /* Row animation for deletion */
        .fade-out-row {
            transition: all 0.4s ease !important;
            opacity: 0 !important;
            transform: translateX(-20px) scale(0.95) !important;
            background-color: #f8d7da !important;
        }

        @keyframes fadeInUp {
            from {
                transform: translateY(20px);
                opacity: 0;
            }
            to {
                transform: translateY(0);
                opacity: 1;
            }
        }

        /* Success Modal Styles */
        .success-modal {
            display: none;
            position: fixed;
            z-index: 1001;
            left: 0;
            top: 0;
            width: 100%;
            height: 100%;
            background-color: rgba(0, 0, 0, 0.5);
            align-items: center;
            justify-content: center;
        }

        .success-modal-content {
            background: white;
            border-radius: 8px;
            box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
            max-width: 500px;
            width: 90%;
            max-height: 90vh;
            overflow-y: auto;
        }

        .success-modal-header {
            padding: 1.5rem;
            border-bottom: 1px solid #dee2e6;
            background: linear-gradient(135deg, #28a745, #20c997);
            color: white;
            border-radius: 8px 8px 0 0;
        }

        .success-modal-header h3 {
            margin: 0;
            display: flex;
            align-items: center;
            gap: 0.5rem;
            font-size: 1.3rem;
        }

        .success-modal-body {
            padding: 2rem 1.5rem;
        }

        .success-message {
            font-size: 1rem;
            color: #495057;
            margin: 0;
            line-height: 1.5;
            text-align: center;
        }

        .success-manager-name {
            font-weight: 600;
            color: #2c3e50;
        }

        .success-modal-footer {
            padding: 1rem 1.5rem;
            border-top: 1px solid #dee2e6;
            display: flex;
            gap: 1rem;
            justify-content: center;
            background: #f8f9fa;
            border-radius: 0 0 8px 8px;
        }

        .btn-success {
            background: #28a745;
            color: white;
            border: none;
            padding: 0.5rem 1.5rem;
            border-radius: 4px;
            cursor: pointer;
            text-decoration: none;
            display: inline-flex;
            align-items: center;
            gap: 0.5rem;
            font-size: 0.9rem;
            font-weight: 500;
            transition: all 0.3s ease;
            min-width: 100px;
            justify-content: center;
        }

        .btn-success:hover {
            background: #218838;
            transform: translateY(-1px);
        }

        /* Mobile responsiveness */
        @media (max-width: 768px) {
            .manager-list-header {
                flex-direction: column;
                gap: 1rem;
                align-items: stretch;
            }

            .header-actions {
                flex-direction: column;
                gap: 0.75rem;
            }

            .search-box input {
                width: 100%;
                color: rgba(255, 255, 255, 1);
                z-index: 10;
            }

            .manager-table {
                font-size: 0.8rem;
            }

            .manager-table th,
            .manager-table td {
                padding: 0.5rem;
            }

            .action-buttons {
                flex-direction: column;
                gap: 0.25rem;
            }

            .btn-action {
                padding: 0.3rem 0.6rem;
                font-size: 0.75rem;
            }
        }
    </style>
</head>
<body>
    <div class="dashboard-container">
        <!-- Executive Director Sidebar Navigation -->
        <nav class="sidebar" id="sidebar">
            <div class="sidebar-header">
                <img src="/ELMS_3.0/imnsb_logowbg.png" alt="IMNSB Logo" class="sidebar-logo">
                <h3>IMNSB Executive Director</h3>
            </div>
           <ul class="sidebar-menu">
                <li><a href="/ELMS_3.0/Manager/ExecutiveDirectorDashboard.jsp"><i class="fas fa-user"></i> <span>Profile</span></a></li>
	            <li class="active"><a href="<%= request.getContextPath() %>/executive-director-project-managers"><i class="fas fa-users"></i> <span>Project Managers</span></a></li>
	            <li><a href="<%= request.getContextPath() %>/manager-pending-requests"><i class="fas fa-clock"></i> <span>Pending Requests</span></a></li>
	            <li><a href="/ELMS_3.0/Manager/DirectorSettings.jsp"><i class="fas fa-cog"></i> <span>Settings</span></a></li>
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
                    <h2>Project Managers</h2>
                </div>
                <div class="header-right">
                        <span id="userName"><%= managerName %></span>
                        <div class="user-avatar">
                            <%
                                String headerProfilePicPath = manager.getProfilePicturePath();
                                if (headerProfilePicPath != null && !headerProfilePicPath.isEmpty()) {
                            %>
                            <img src="/ELMS_3.0/<%= headerProfilePicPath %>" alt="Executive Director Avatar">
                            <% } else { %>
                            <img src="/ELMS_3.0/defaultprofilepicture.jpg" alt="Executive Director Avatar">
                            <% } %>
                        </div>
                </div>
            </header>
            
            <div class="content-body">
                <!-- Display messages -->
                <c:if test="${not empty requestScope.errorMessage}">
                    <div class="alert alert-danger">
                        <i class="fas fa-exclamation-circle"></i>
                        ${requestScope.errorMessage}
                    </div>
                </c:if>
                
                <c:if test="${not empty requestScope.successMessage}">
                    <div class="alert alert-success">
                        <i class="fas fa-check-circle"></i>
                        ${requestScope.successMessage}
                    </div>
                </c:if>
                
                <c:if test="${not empty requestScope.infoMessage}">
                    <div class="alert alert-info">
                        <i class="fas fa-info-circle"></i>
                        ${requestScope.infoMessage}
                    </div>
                </c:if>

                <div class="manager-list-container">
                    <div class="manager-list-header">
                        <div>
                            <h3><i class="fas fa-users"></i> Project Managers</h3>
                            <p>Manage project managers under your supervision</p>
                        </div>
                        <div class="header-actions">
                            <div class="search-box">
                                <i class="fas fa-search"></i>
                                <input type="text" id="searchInput" placeholder="Search project managers..." 
                                       value="${param.search}">
                            </div>
                        </div>
                    </div>

                    <div class="manager-table-container">
                        <c:choose>
                            <c:when test="${empty requestScope.managerList}">
                                <div class="empty-state">
                                    <i class="fas fa-users"></i>
                                    <h3>No Project Managers Found</h3>
                                    <p>There are currently no project managers under your supervision.</p>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <table class="manager-table">
                                    <thead>
                                        <tr>
                                            <th>Project Manager</th>
                                            <th>Contact Information</th>
                                            <th>Manager ID</th>
                                            <th>Actions</th>
                                        </tr>
                                    </thead>
                                    <tbody id="managerTableBody">
                                        <c:forEach var="projectManager" items="${requestScope.managerList}">
                                            <tr data-manager-id="${projectManager.managerid}">
                                                <td>
                                                    <div class="manager-info">
                                                        <img src="${empty projectManager.profilePicturePath ? '/ELMS_3.0/defaultprofilepicture.jpg' : pageContext.request.contextPath.concat('/').concat(projectManager.profilePicturePath)}" 
														     alt="Project Manager Avatar" class="manager-avatar"
														     onerror="this.src='/ELMS_3.0/defaultprofilepicture.jpg';">
                                                        <div class="manager-details">
                                                            <h4>${projectManager.managername}</h4>
                                                            <p>${projectManager.manageremail}</p>
                                                        </div>
                                                    </div>
                                                </td>
                                                <td>
                                                    <div>
                                                        <i class="fas fa-envelope" style="color: #6c757d; margin-right: 0.5rem;"></i>
                                                        ${projectManager.manageremail}
                                                    </div>
                                                    <div style="margin-top: 0.3rem;">
                                                        <i class="fas fa-phone" style="color: #6c757d; margin-right: 0.5rem;"></i>
                                                        ${empty projectManager.managernophone ? 'Not provided' : projectManager.managernophone}
                                                    </div>
                                                </td>
                                                <td>
                                                    <code style="background: #f8f9fa; padding: 0.2rem 0.5rem; border-radius: 3px; font-size: 0.85rem;">
                                                        ${projectManager.managerid}
                                                    </code>
                                                </td>
                                                <td>
                                                    <div class="action-buttons">
                                                        <button type="button" class="btn-action btn-delete" 
                                                                onclick="confirmDeleteManager('${projectManager.managerid}', '${projectManager.managername}', '${projectManager.manageremail}')"
                                                                title="Delete Project Manager">
                                                            <i class="fas fa-trash"></i> Delete
                                                        </button>
                                                    </div>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </tbody>
                                </table>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>
        </main>
    </div>

    <!-- Delete Manager Confirmation Modal -->
<div id="passwordModal" class="popup-overlay" style="display: none;">
    <div class="popup-content" style="border-top: 5px solid #dc3545;">
        <div class="popup-body">
            <div style="text-align: center; padding: 1rem 0;">
                <i class="fas fa-exclamation-triangle" style="font-size: 4rem; color: #dc3545; margin-bottom: 1rem;"></i>
                <h3 style="color: #dc3545; font-size: 1.5rem; margin: 0 0 1rem 0;">Confirm Project Manager Deletion</h3>
                <p style="color: #495057; font-size: 1rem; margin: 0 0 1rem 0; line-height: 1.5;">
                    Are you sure you want to delete this project manager?
                </p>
                
                <div class="manager-info-display" style="background: #f8f9fa; padding: 15px; border-radius: 8px; margin: 15px 0; text-align: left;">
                    <p style="margin: 0.5rem 0;"><strong>Manager:</strong> <span id="deleteManagerName">John Doe</span></p>
                    <p style="margin: 0.5rem 0;"><strong>ID:</strong> <span id="deleteManagerId">PM001</span></p>
                    <p style="margin: 0.5rem 0;"><strong>Email:</strong> <span id="deleteManagerEmail">manager@example.com</span></p>
                </div>
                
                <div style="background: linear-gradient(135deg, #fff5f5 0%, #ffe5e5 100%); padding: 15px; border-radius: 8px; margin-bottom: 20px; border-left: 4px solid #dc3545;">
                    <p style="margin: 0; color: #721c24; font-size: 0.9rem; line-height: 1.5;">
                        <i class="fas fa-exclamation-triangle"></i>
                        <strong>Warning:</strong> This action will permanently delete the project manager account and may affect leave request approvals. This cannot be undone.
                    </p>
                </div>
            </div>
            
            <form id="directorPasswordForm">
                <div class="form-group" style="margin-bottom: 20px; text-align: left;">
                    <label for="directorPassword" style="display: block; margin-bottom: 8px; font-weight: 500; color: #495057; font-size: 0.9rem;">
                        <i class="fas fa-lock"></i> Confirm Your Password <span style="color: red;">*</span>
                    </label>
                    <div style="position: relative;">
                        <input type="password" 
                               id="directorPassword" 
                               name="directorPassword" 
                               placeholder="Enter your password to confirm deletion"
                               required
                               autocomplete="current-password"
                               style="width: 100%; padding: 12px 45px 12px 12px; border: 1px solid #ced4da; border-radius: 6px; font-size: 0.9rem; box-sizing: border-box;">
                        <button type="button" 
                                id="toggleDirectorPassword" 
                                onclick="togglePasswordVisibility()"
                                style="position: absolute; right: 12px; top: 50%; transform: translateY(-50%); background: none; border: none; color: #6c757d; cursor: pointer; padding: 0; width: 24px; height: 24px; display: flex; align-items: center; justify-content: center;">
                            <i class="fas fa-eye" id="passwordToggleIcon"></i>
                        </button>
                    </div>
                    <div id="passwordError" style="color: #dc3545; font-size: 0.875rem; margin-top: 8px; display: none;">
                        <i class="fas fa-exclamation-circle"></i>
                        <span id="passwordErrorText">Invalid password. Please try again.</span>
                    </div>
                    <div style="font-size: 0.875rem; color: #6c757d; margin-top: 8px; font-style: italic;">
                        <i class="fas fa-info-circle"></i>
                        Enter your executive director password to authorize this deletion.
                    </div>
                </div>
                
                <div class="form-group" style="margin-bottom: 20px; text-align: left;">
                    <label class="checkbox-label" style="display: flex; align-items: flex-start; gap: 0.5rem; cursor: pointer; font-size: 0.95rem; line-height: 1.4;">
                        <input type="checkbox" id="confirmDelete" name="confirmDelete" value="true" required 
                               style="margin-top: 0.2rem; transform: scale(1.2);">
                        <span>I understand this action is permanent and want to delete this project manager account.</span>
                    </label>
                </div>
            </form>
        </div>
        <div class="popup-footer">
            <button type="button" class="btn btn-secondary" onclick="closePasswordModal()">
                <i class="fas fa-times"></i> Cancel
            </button>
            <button type="button" class="btn btn-danger" id="confirmDeleteBtn" disabled onclick="confirmPasswordAndDelete()">
                <i class="fas fa-trash-alt" id="deleteIcon"></i>
                <div class="loading-spinner" id="deleteSpinner" style="display: none;"></div>
                <span id="deleteButtonText">Delete Manager</span>
            </button>
        </div>
    </div>
</div>

    <!-- Success Modal -->
    <div id="successModal" class="success-modal">
        <div class="success-modal-content">
            <div class="success-modal-header">
                <h3>
                    <i class="fas fa-check-circle"></i>
                    Project Manager Deleted Successfully
                </h3>
            </div>
            <div class="success-modal-body">
                <p class="success-message">
                    Project Manager <span class="success-manager-name" id="successManagerName">John Doe</span> has been permanently deleted from the system.
                </p>
            </div>
            <div class="success-modal-footer">
                <button type="button" class="btn-success" onclick="closeSuccessModal()">
                    <i class="fas fa-check"></i> OK
                </button>
            </div>
        </div>
    </div>

    <!-- JavaScript -->
    <script src="/ELMS_3.0/Manager/Manager.js"></script>
    <script>
        // Global variables
        let currentManagerToDelete = null;

        // Document ready initialization
        document.addEventListener('DOMContentLoaded', function() {
    console.log('✅ Executive Director Project Manager management script loaded');
    
    // Initialize form validation
    initializeFormValidation();
    
    // Add smooth animations
    addSmoothAnimations();
    
    // Verify table structure
    const tableBody = document.getElementById('managerTableBody');
    if (tableBody) {
        const rows = tableBody.querySelectorAll('tr[data-manager-id]');
        console.log(`✅ Found ${rows.length} project manager rows in table`);
        
        // Debug: Log all manager IDs found
        rows.forEach((row, index) => {
            const managerId = row.getAttribute('data-manager-id');
            console.log(`Manager row ${index}: ID = "${managerId}"`);
        });
    } else {
        console.warn('⚠️ Project manager table body not found');
    }
    
    // Search functionality
    const searchInput = document.getElementById('searchInput');
    const managerTableBody = document.getElementById('managerTableBody');
    
    if (searchInput && managerTableBody) {
        searchInput.addEventListener('input', function() {
            const searchTerm = this.value.toLowerCase();
            const rows = managerTableBody.getElementsByTagName('tr');
            
            Array.from(rows).forEach(row => {
                const managerName = row.querySelector('.manager-details h4')?.textContent.toLowerCase() || '';
                const managerEmail = row.querySelector('.manager-details p')?.textContent.toLowerCase() || '';
                const managerId = row.cells[2]?.textContent.toLowerCase() || '';
                
                if (managerName.includes(searchTerm) || 
                    managerEmail.includes(searchTerm) || 
                    managerId.includes(searchTerm)) {
                    row.style.display = '';
                } else {
                    row.style.display = 'none';
                }
            });
        });
    }

		    // Close modals when clicking outside
		    window.onclick = function(event) {
		        const modal = document.getElementById('passwordModal');
		        const successModal = document.getElementById('successModal');
		        
		        if (event.target === modal) {
		            closePasswordModal();
		        }
		        
		        if (event.target === successModal) {
		            closeSuccessModal();
		        }
		    }
		    
		    // Auto-hide alert messages after 5 seconds
		    const alerts = document.querySelectorAll('.alert');
		    alerts.forEach(function(alert) {
		        setTimeout(function() {
		            alert.style.transition = 'opacity 0.5s ease';
		            alert.style.opacity = '0';
		            setTimeout(function() {
		                if (alert.parentNode) {
		                    alert.parentNode.removeChild(alert);
		                }
		            }, 500);
		        }, 5000);
		    });
		});

        // Add CSS animations for smooth transitions
        function addSmoothAnimations() {
            const style = document.createElement('style');
            style.textContent = `
                @keyframes slideInUp {
                    from {
                        transform: translateY(10px);
                        opacity: 0.7;
                    }
                    to {
                        transform: translateY(0);
                        opacity: 1;
                    }
                }
                
                @keyframes fadeInUp {
                    from {
                        transform: translateY(20px);
                        opacity: 0;
                    }
                    to {
                        transform: translateY(0);
                        opacity: 1;
                    }
                }
                
                .manager-table tr {
                    transition: all 0.3s ease;
                }
            `;
            document.head.appendChild(style);
        }

        function confirmDeleteManager(managerId, managerName, managerEmail) {
            console.log('🎯 Opening deletion confirmation for:', managerId, managerName);
            
            // Validate inputs
            if (!managerId || !managerName) {
                showPasswordError('Invalid manager data provided');
                return;
            }
            
            // Store manager data
            currentManagerToDelete = {
                id: String(managerId).trim(),
                name: String(managerName).trim(),
                email: String(managerEmail || 'N/A').trim()
            };
            
            console.log('📝 Stored manager data:', currentManagerToDelete);
            
            // Get modal elements
            const modal = document.getElementById('passwordModal');
            const nameEl = document.getElementById('deleteManagerName');
            const idEl = document.getElementById('deleteManagerId');
            const emailEl = document.getElementById('deleteManagerEmail');
            const form = document.getElementById('directorPasswordForm');
            const deleteBtn = document.getElementById('confirmDeleteBtn');
            
            // Validate elements exist
            if (!modal || !nameEl || !idEl || !emailEl || !form || !deleteBtn) {
                alert('Modal elements not found. Please refresh the page.');
                return;
            }
            
            // Populate modal
            nameEl.textContent = currentManagerToDelete.name;
            idEl.textContent = currentManagerToDelete.id;
            emailEl.textContent = currentManagerToDelete.email;
            
            // Reset form
            form.reset();
            deleteBtn.disabled = true;
            deleteBtn.style.opacity = '0.6';
            
            // Hide error
            const errorDiv = document.getElementById('passwordError');
            if (errorDiv) errorDiv.style.display = 'none';
            
            // Show modal
            modal.style.display = 'flex';
            modal.classList.add('show');
            
            // Focus password input
            setTimeout(() => {
                const passwordInput = document.getElementById('directorPassword');
                if (passwordInput) passwordInput.focus();
            }, 100);
        }
        
        function initializeFormValidation() {
            const passwordInput = document.getElementById('directorPassword');
            const confirmCheckbox = document.getElementById('confirmDelete');
            const deleteBtn = document.getElementById('confirmDeleteBtn');

            function validateForm() {
                if (passwordInput && confirmCheckbox && deleteBtn) {
                    const hasPassword = passwordInput.value.trim().length > 0;
                    const isConfirmed = confirmCheckbox.checked;
                    
                    deleteBtn.disabled = !(hasPassword && isConfirmed);
                    deleteBtn.style.opacity = deleteBtn.disabled ? '0.6' : '1';
                }
            }

            if (passwordInput) passwordInput.addEventListener('input', validateForm);
            if (confirmCheckbox) confirmCheckbox.addEventListener('change', validateForm);
            
            // Enter key to submit
            if (passwordInput) {
                passwordInput.addEventListener('keypress', function(e) {
                    if (e.key === 'Enter' && deleteBtn && !deleteBtn.disabled) {
                        e.preventDefault();
                        confirmPasswordAndDelete();
                    }
                });
            }
        }

       
        function closePasswordModal() {
            const modal = document.getElementById('passwordModal');
            if (modal) {
                modal.classList.remove('show');
                modal.classList.add('hide');
                
                setTimeout(() => {
                    modal.style.display = 'none';
                    modal.classList.remove('hide');
                    currentManagerToDelete = null;
                }, 300);
            }
        }

        function togglePasswordVisibility() {
            const passwordInput = document.getElementById('directorPassword');
            const toggleIcon = document.getElementById('passwordToggleIcon');
            
            if (passwordInput && toggleIcon) {
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
        }

        // Main deletion function with immediate row removal
        function confirmPasswordAndDelete() {
            const passwordInput = document.getElementById('directorPassword');
            const errorDiv = document.getElementById('passwordError');
            
            if (!passwordInput || !errorDiv) {
                console.error('Password input or error div not found');
                return;
            }
            
            const password = passwordInput.value.trim();

            // Reset error state
            errorDiv.style.display = 'none';
            passwordInput.style.borderColor = '#dee2e6';

            if (!password) {
                showPasswordError('Please enter your password');
                return;
            }

            if (!currentManagerToDelete) {
                showPasswordError('Manager information not found');
                return;
            }

            // Show loading state
            setDeleteButtonLoading(true);

            // Send AJAX request to verify password and delete manager
            fetch('<%= request.getContextPath() %>/executive-director-delete-manager', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: new URLSearchParams({
                    'action': 'delete_with_password',
                    'managerId': currentManagerToDelete.id,
                    'directorPassword': password
                })
            })
            .then(response => {
                console.log('Response status:', response.status);
                if (!response.ok) {
                    throw new Error(`HTTP error! status: ${response.status}`);
                }
                return response.text();
            })
            .then(result => {
                console.log('Raw server response:', result);
                
                // Clean the response text
                const cleanResult = result.trim();
                console.log('Clean server response:', cleanResult);
                
                if (cleanResult === 'success') {
                    // IMMEDIATE SUCCESS ACTIONS
                    console.log('✅ Delete successful, removing row immediately');
                    
                    // 1. Close password modal immediately
                    closePasswordModal();
                    
                    // 2. Remove the manager row from table IMMEDIATELY
                    setTimeout(() => {
                        removeManagerFromTableImmediate(currentManagerToDelete.id);
                    }, 100);
                    
                    // 3. Show success modal
                    setTimeout(() => {
                        showSuccessModal(currentManagerToDelete.name);
                    }, 400);
                    
                } else if (cleanResult === 'invalid_password') {
                    // Invalid password
                    console.log('❌ Invalid password');
                    setDeleteButtonLoading(false);
                    showPasswordError('Invalid password. Please try again.');
                    
                } else if (cleanResult.startsWith('error:')) {
                    // Other error
                    console.log('❌ Server error:', cleanResult);
                    setDeleteButtonLoading(false);
                    const errorMessage = cleanResult.replace('error:', '').trim();
                    showPasswordError(errorMessage || 'An error occurred while deleting the project manager.');
                    
                } else {
                    // Unexpected response
                    console.log('❌ Unexpected response:', cleanResult);
                    setDeleteButtonLoading(false);
                    showPasswordError('Unexpected server response. Please try again.');
                }
            })
            .catch(error => {
                console.error('❌ Network error during deletion:', error);
                setDeleteButtonLoading(false);
                showPasswordError('Network error. Please check your connection and try again.');
            });
        }

        function showPasswordError(message) {
            const errorDiv = document.getElementById('passwordError');
            const errorText = document.getElementById('passwordErrorText');
            const passwordInput = document.getElementById('directorPassword');
            
            if (errorDiv && errorText && passwordInput) {
                errorText.textContent = message;
                errorDiv.style.display = 'flex';
                errorDiv.style.alignItems = 'center';
                errorDiv.style.gap = '0.3rem';
                passwordInput.style.borderColor = '#dc3545';
                passwordInput.focus();
            }
        }

        function setDeleteButtonLoading(isLoading) {
            const deleteBtn = document.getElementById('confirmDeleteBtn');
            const deleteIcon = document.getElementById('deleteIcon');
            const deleteSpinner = document.getElementById('deleteSpinner');
            const deleteText = document.getElementById('deleteButtonText');

            if (deleteBtn && deleteIcon && deleteSpinner && deleteText) {
                if (isLoading) {
                    deleteBtn.disabled = true;
                    deleteIcon.style.display = 'none';
                    deleteSpinner.style.display = 'block';
                    deleteText.textContent = 'Deleting...';
                } else {
                    resetDeleteButton();
                }
            }
        }

        function resetDeleteButton() {
            const deleteBtn = document.getElementById('confirmDeleteBtn');
            const deleteIcon = document.getElementById('deleteIcon');
            const deleteSpinner = document.getElementById('deleteSpinner');
            const deleteText = document.getElementById('deleteButtonText');

            if (deleteBtn && deleteIcon && deleteSpinner && deleteText) {
                deleteBtn.disabled = false;
                deleteIcon.style.display = 'inline';
                deleteSpinner.style.display = 'none';
                deleteText.textContent = 'Delete Manager';
            }
        }

        // Immediate row removal function
        function removeManagerFromTableImmediate(managerId) {
            console.log('🔄 IMMEDIATE: Removing project manager from table:', managerId);
            
            // Find the row using the data attribute
            const row = document.querySelector(`tr[data-manager-id="${managerId}"]`);
            
            if (!row) {
                console.error('❌ Project manager row not found in table:', managerId);
                // Debug: Log all available rows
                const allRows = document.querySelectorAll('#managerTableBody tr[data-manager-id]');
                console.log('Available manager rows:', allRows.length);
                allRows.forEach((r, index) => {
                    const id = r.getAttribute('data-manager-id');
                    console.log(`Row ${index}: data-manager-id="${id}"`);
                });
                
                // Try alternative removal method
                removeManagerRowAlternative(managerId);
                return;
            }

            console.log('✅ Found project manager row to delete:', row);
            console.log('Row manager ID attribute:', row.getAttribute('data-manager-id'));
            
            // Disable row interactions immediately
            row.style.pointerEvents = 'none';
            
            // Add immediate visual feedback
            row.style.transition = 'all 0.4s ease';
            row.style.backgroundColor = '#f8d7da';
            row.style.opacity = '0.7';
            
            console.log('🎨 Applied initial styling to row');
            
            // Start fade out animation after a short delay
            setTimeout(() => {
                console.log('🎬 Starting fade out animation');
                row.classList.add('fade-out-row');
            }, 150);
            
            // Remove from DOM after animation completes
            setTimeout(() => {
                if (row && row.parentNode) {
                    console.log('🗑️ Removing row from DOM');
                    row.remove();
                    console.log('✅ Project manager row successfully removed from DOM');
                    
                    // Check if table is empty and update UI accordingly
                    checkAndUpdateEmptyState();
                    
                    // Re-animate remaining rows for smooth transition
                    animateRemainingRows();
                } else {
                    console.warn('⚠️ Row or parent not found during removal');
                }
            }, 500);
        }

        // Alternative removal function using different selectors (backup)
        function removeManagerRowAlternative(managerId) {
            console.log('Trying alternative removal method for:', managerId);
            
            // Try multiple selector approaches
            const selectors = [
                `tr[data-manager-id="${managerId}"]`,
                `#managerTableBody tr[data-manager-id="${managerId}"]`,
                `.manager-table tr[data-manager-id="${managerId}"]`
            ];
            
            let row = null;
            for (const selector of selectors) {
                row = document.querySelector(selector);
                if (row) {
                    console.log(`Found row using selector: ${selector}`);
                    break;
                }
            }
            
            if (!row) {
                // Last resort: find by manager ID in the table cells
                const allRows = document.querySelectorAll('#managerTableBody tr');
                for (const r of allRows) {
                    const idCell = r.querySelector('code');
                    if (idCell && idCell.textContent.trim() === managerId) {
                        row = r;
                        console.log('Found row by searching cell content');
                        break;
                    }
                }
            }
            
            if (row) {
                row.style.transition = 'all 0.4s ease';
                row.style.backgroundColor = '#f8d7da';
                row.style.opacity = '0.7';
                
                setTimeout(() => {
                    row.classList.add('fade-out-row');
                }, 150);
                
                setTimeout(() => {
                    row.remove();
                    console.log('✅ Project manager row removed using alternative method');
                    checkAndUpdateEmptyState();
                    animateRemainingRows();
                }, 500);
            } else {
                console.error('❌ Could not find project manager row to remove with any method');
                // Force page reload as last resort
                console.log('🔄 Forcing page reload as fallback');
                setTimeout(() => {
                    window.location.reload();
                }, 1000);
            }
        }

        // Check if table is empty and show empty state
        function checkAndUpdateEmptyState() {
            const tableBody = document.getElementById('managerTableBody');
            
            if (!tableBody) {
                console.error('Table body not found');
                return;
            }
            
            const remainingRows = tableBody.children.length;
            console.log('Remaining project managers after deletion:', remainingRows);
            
            if (remainingRows === 0) {
                console.log('Table is now empty, showing empty state');
                showEmptyStateImmediate();
            }
        }

        // Animate remaining rows for smooth transition
        function animateRemainingRows() {
            const tableBody = document.getElementById('managerTableBody');
            if (!tableBody) return;
            
            const remainingRows = Array.from(tableBody.children);
            remainingRows.forEach((row, index) => {
                row.style.animation = `slideInUp 0.3s ease ${index * 0.05}s both`;
            });
        }

        // Show empty state immediately when no managers left
        function showEmptyStateImmediate() {
            const tableContainer = document.querySelector('.manager-table-container');
            if (!tableContainer) {
                console.error('Table container not found');
                return;
            }
            
            // Fade out current table
            const currentTable = tableContainer.querySelector('.manager-table');
            if (currentTable) {
                currentTable.style.transition = 'opacity 0.3s ease';
                currentTable.style.opacity = '0';
                
                setTimeout(() => {
                    // Replace with empty state
                    tableContainer.innerHTML = `
                        <div class="empty-state" style="animation: fadeInUp 0.5s ease;">
                            <i class="fas fa-users"></i>
                            <h3>No Project Managers Found</h3>
                            <p>There are currently no project managers under your supervision.</p>
                        </div>
                    `;
                }, 300);
            }
        }

        // Success modal functions
        function showSuccessModal(managerName) {
            console.log('Showing success modal for:', managerName);
            
            // Set manager name in the success message
            const successManagerNameEl = document.getElementById('successManagerName');
            if (successManagerNameEl) {
                successManagerNameEl.textContent = managerName;
            }
            
            // Show success modal with animation
            const successModal = document.getElementById('successModal');
            if (successModal) {
                const successModalContent = successModal.querySelector('.success-modal-content');
                
                successModal.style.display = 'flex';
                successModal.classList.add('show');
                if (successModalContent) {
                    successModalContent.classList.add('show');
                }
                
                console.log('Success modal displayed');
                
                // Auto-close after 5 seconds
                setTimeout(() => {
                    closeSuccessModal();
                }, 5000);
            } else {
                console.error('Success modal not found!');
            }
        }

        function closeSuccessModal() {
            console.log('Closing success modal');
            
            const successModal = document.getElementById('successModal');
            if (successModal) {
                successModal.classList.remove('show');
                successModal.classList.add('hide');
                
                setTimeout(() => {
                    successModal.style.display = 'none';
                    successModal.classList.remove('hide');
                    console.log('Success modal closed');
                }, 300);
            }
        }
    </script>
</body>
</html>
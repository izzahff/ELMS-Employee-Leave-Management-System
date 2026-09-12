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
    <title>Manager List - IMNSB Employee Leave Management System</title>
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

    .btn-add {
        background: rgba(255, 255, 255, 0.2);
        color: white;
        border: 1px solid rgba(255, 255, 255, 0.3);
        padding: 0.5rem 1rem;
        border-radius: 6px;
        text-decoration: none;
        display: flex;
        align-items: center;
        gap: 0.5rem;
        font-size: 0.9rem;
        transition: all 0.3s ease;
        backdrop-filter: blur(10px);
    }

    .btn-add:hover {
        background: rgba(255, 255, 255, 0.3);
        color: white;
        transform: translateY(-1px);
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

    .status-badge {
        display: inline-block;
        padding: 0.25rem 0.75rem;
        border-radius: 12px;
        font-size: 0.75rem;
        font-weight: 500;
        text-transform: uppercase;
        letter-spacing: 0.5px;
    }

    .status-active {
        background: #d4edda;
        color: #155724;
    }

    .role-badge {
        display: inline-block;
        padding: 0.25rem 0.75rem;
        border-radius: 12px;
        font-size: 0.75rem;
        font-weight: 500;
        text-transform: uppercase;
        letter-spacing: 0.5px;
        margin-left: 0.5rem;
    }
    
    .role-executive {
        background: linear-gradient(135deg, #f39c12, #e67e22);
        color: white;
    }
            
    .role-project {
        background: linear-gradient(135deg, #4ecdc4, #44a08d);
        color: white;
    }
    
    .role-unknown {
        background: #6c757d;
        color: white;
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

    .btn-view {
        background: #007bff;
        color: white;
    }

    .btn-view:hover {
        background: #0056b3;
        color: white;
    }

    .btn-edit {
        background: #28a745;
        color: white;
    }

    .btn-edit:hover {
        background: #1e7e34;
        color: white;
    }

    .btn-delete {
        background: #dc3545;
        color: white;
    }

    .btn-delete:hover {
        background: #c82333;
        color: white;
    }

    .fade-out-row {
        transition: all 0.4s ease !important;
        opacity: 0 !important;
        transform: translateX(-20px) scale(0.95) !important;
        background-color: #f8d7da !important;
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

    .pagination {
        display: flex;
        justify-content: center;
        padding: 1rem;
        gap: 0.5rem;
    }

    .pagination a, .pagination span {
        padding: 0.5rem 0.75rem;
        border: 1px solid #dee2e6;
        color: #007bff;
        text-decoration: none;
        border-radius: 4px;
    }

    .pagination .current {
        background: #007bff;
        color: white;
        border-color: #007bff;
    }

    .pagination a:hover {
        background: #e9ecef;
    }

    /* Modal Styles - Matching Employee Modal */
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
	    border-top: 5px solid #dc3545;  
	}

    .popup-body {
        padding: 2rem 1.5rem;
    }

    .delete-warning {
        text-align: center;
        margin-bottom: 1.5rem;
    }

    .delete-warning i {
        font-size: 3rem;
        color: #dc3545;
        margin-bottom: 1rem;
    }

    .delete-warning h4 {
        color: #495057;
        margin-bottom: 1rem;
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

    .warning-text {
        color: #856404;
        background: #fff3cd;
        padding: 0.75rem;
        border-radius: 4px;
        border: 1px solid #ffeaa7;
        font-size: 0.9rem;
        margin: 1rem 0;
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
        min-width: 120px;
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

    .btn-success {
        background: #28a745;
        color: white;
    }

    .btn-success:hover {
        background: #218838;
        transform: translateY(-1px);
    }

    /* Animations */
    @keyframes fadeIn {
        from { opacity: 0; }
        to { opacity: 1; }
    }

    @keyframes slideIn {
        from { 
            transform: translateY(-50px);
            opacity: 0;
        }
        to { 
            transform: translateY(0);
            opacity: 1;
        }
    }

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

    @media (max-width: 600px) {
        .popup-footer {
            flex-direction: column;
        }
        
        .popup-content {
            margin: 1rem;
            width: calc(100% - 2rem);
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
                    <h2>Approval Managers</h2>
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
                            <h3><i class="fas fa-user-tie"></i> Approval Managers</h3>
                            <p>Manage system approval managers</p>
                        </div>
                        <div class="header-actions">
                            <div class="search-box">
                                <i class="fas fa-search"></i>
                                <input type="text" id="searchInput" placeholder="Search managers..." 
                                       value="${param.search}">
                            </div>
                            <a href="AdminAddManagerController" class="btn-add">
                                <i class="fas fa-plus"></i> Add Manager
                            </a>
                        </div>
                    </div>

                    <div class="manager-table-container">
                        <c:choose>
                            <c:when test="${empty requestScope.managerList}">
                                <div class="empty-state">
                                    <i class="fas fa-user-tie"></i>
                                    <h3>No Managers Found</h3>
                                    <p>There are currently no approval managers in the system.</p>
                                    <a href="/ELMS_3.0/Admin/AdminAddManager.jsp" class="btn btn-primary" style="margin-top: 1rem;">
                                        <i class="fas fa-plus"></i> Add First Manager
                                    </a>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <table class="manager-table">
                                    <thead>
                                        <tr>
                                            <th>Manager</th>
                                            <th>Contact</th>
                                            <th>Position & Role</th>
                                            <th>Manager ID</th>
                                            <th>Actions</th>
                                        </tr>
                                    </thead>
                                 <tbody id="managerTableBody">
								    <c:forEach var="manager" items="${requestScope.managerList}">
								        <%-- Hide the system manager from display --%>
								        <c:if test="${manager.managerid != 'SYSTEM_MGR'}">
								            <tr data-manager-id="${manager.managerid}">
								               <td>
								                    <div class="manager-info">
								                        <img src="${empty manager.profilePicturePath ? '/ELMS_3.0/defaultprofilepicture.jpg' : pageContext.request.contextPath.concat('/').concat(manager.profilePicturePath)}" 
								                             alt="Manager Avatar" class="manager-avatar"
								                             onerror="this.src='/ELMS_3.0/defaultprofilepicture.jpg';">
								                        <div class="manager-details">
								                            <h4>${manager.managername}</h4>
								                            <p>${manager.manageremail}</p>
								                        </div>
								                    </div>
								                </td>
								                <td>
								                    <div>
								                        <i class="fas fa-envelope" style="color: #6c757d; margin-right: 0.5rem;"></i>
								                        ${manager.manageremail}
								                    </div>
								                    <div style="margin-top: 0.3rem;">
								                        <i class="fas fa-phone" style="color: #6c757d; margin-right: 0.5rem;"></i>
								                        ${empty manager.managernophone ? 'Not provided' : manager.managernophone}
								                    </div>
								                </td>
								                <td>
								                    <div>
								                        <span style="color: #495057; font-weight: 500;">
								                            ${empty manager.managerposition ? 'Manager' : manager.managerposition}
								                        </span>
								                        <br>
								                        <span class="role-badge ${manager.positionBadgeClass}">
								                            <i class="fas ${manager.managerposition eq 'Executive Director' ? 'fa-crown' : 'fa-user-tie'}"></i>
								                            ${manager.positionDisplayName}
								                        </span>
								                    </div>
								                </td>
								                <td>
								                    <code style="background: #f8f9fa; padding: 0.2rem 0.5rem; border-radius: 3px; font-size: 0.85rem;">
								                        ${manager.managerid}
								                    </code>
								                </td>
								                <td>
								                    <div class="action-buttons">
								                        <button type="button" class="btn-action btn-delete" 
								                                onclick="confirmDeleteManager('${manager.managerid}', '${manager.managername}', '${manager.manageremail}', '${manager.managerposition}')"
								                                title="Delete Manager">
								                            <i class="fas fa-trash"></i> Delete
								                        </button>
								                    </div>
								                </td>
								            </tr>
								        </c:if>
								    </c:forEach>
								</tbody>
                                </table>

                                <!-- Pagination (if needed) -->
                                <c:if test="${requestScope.totalPages > 1}">
                                    <div class="pagination">
                                        <c:if test="${requestScope.currentPage > 1}">
                                            <a href="?page=${requestScope.currentPage - 1}&search=${param.search}">
                                                <i class="fas fa-chevron-left"></i> Previous
                                            </a>
                                        </c:if>
                                        
                                        <c:forEach var="i" begin="1" end="${requestScope.totalPages}">
                                            <c:choose>
                                                <c:when test="${i == requestScope.currentPage}">
                                                    <span class="current">${i}</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <a href="?page=${i}&search=${param.search}">${i}</a>
                                                </c:otherwise>
                                            </c:choose>
                                        </c:forEach>
                                        
                                        <c:if test="${requestScope.currentPage < requestScope.totalPages}">
                                            <a href="?page=${requestScope.currentPage + 1}&search=${param.search}">
                                                Next <i class="fas fa-chevron-right"></i>
                                            </a>
                                        </c:if>
                                    </div>
                                </c:if>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>
        </main>
    </div>

 <!-- Delete Manager Confirmation Modal -->
<div id="deleteManagerModal" class="popup-overlay" style="display: none;">
    <div class="popup-content" style="border-top: 5px solid #dc3545;">
        <div class="popup-body">
            <div style="text-align: center; padding: 1rem 0;">
                <i class="fas fa-exclamation-triangle" style="font-size: 4rem; color: #dc3545; margin-bottom: 1rem;"></i>
                <h3 style="color: #dc3545; font-size: 1.5rem; margin: 0 0 1rem 0;">Confirm Manager Deletion</h3>
                <p style="color: #495057; font-size: 1rem; margin: 0 0 1rem 0; line-height: 1.5;">
                    Are you sure you want to delete this manager?
                </p>
                
                <div class="manager-info-display" style="background: #f8f9fa; padding: 15px; border-radius: 8px; margin: 15px 0; text-align: left;">
                    <p style="margin: 0.5rem 0;"><strong>Manager:</strong> <span id="deleteManagerDisplayName">John Doe</span></p>
                    <p style="margin: 0.5rem 0;"><strong>ID:</strong> <span id="deleteManagerDisplayId">PM001</span></p>
                    <p style="margin: 0.5rem 0;"><strong>Position:</strong> <span id="deleteManagerDisplayPosition">Project Manager</span></p>
                </div>
                
                <div style="background: linear-gradient(135deg, #fff5f5 0%, #ffe5e5 100%); padding: 15px; border-radius: 8px; margin-bottom: 20px; border-left: 4px solid #dc3545;">
                    <p style="margin: 0; color: #721c24; font-size: 0.9rem; line-height: 1.5;">
                        <i class="fas fa-exclamation-triangle"></i>
                        <strong>Warning:</strong> This action will permanently delete the manager account. Leave application history will be preserved for audit purposes.
                    </p>
                </div>
            </div>
            
            <form id="deleteManagerForm">
                <div class="form-group" style="margin-bottom: 20px; text-align: left;">
                    <label for="adminPasswordConfirm" style="display: block; margin-bottom: 8px; font-weight: 500; color: #495057; font-size: 0.9rem;">
                        <i class="fas fa-lock"></i> Confirm Your Password <span style="color: red;">*</span>
                    </label>
                    <div style="position: relative;">
                        <input type="password" 
                               id="adminPasswordConfirm" 
                               name="adminPassword" 
                               placeholder="Enter your admin password to confirm deletion"
                               required
                               style="width: 100%; padding: 12px 45px 12px 12px; border: 1px solid #ced4da; border-radius: 6px; font-size: 0.9rem; box-sizing: border-box;">
                        <button type="button" 
                                id="toggleManagerDeletePassword" 
                                onclick="toggleManagerDeletePasswordVisibility()"
                                style="position: absolute; right: 12px; top: 50%; transform: translateY(-50%); background: none; border: none; color: #6c757d; cursor: pointer; padding: 0; width: 24px; height: 24px; display: flex; align-items: center; justify-content: center;">
                            <i class="fas fa-eye" id="managerDeletePasswordIcon"></i>
                        </button>
                    </div>
                    <div style="font-size: 0.875rem; color: #6c757d; margin-top: 8px; font-style: italic;">
                        <i class="fas fa-info-circle"></i>
                        Enter your admin password to authorize this deletion.
                    </div>
                </div>
                
                <div class="form-group" style="margin-bottom: 20px; text-align: left;">
                    <label class="checkbox-label" style="display: flex; align-items: flex-start; gap: 0.5rem; cursor: pointer; font-size: 0.95rem; line-height: 1.4;">
                        <input type="checkbox" id="confirmManagerDelete" name="confirmDelete" value="true" required 
                               style="margin-top: 0.2rem; transform: scale(1.2);">
                        <span>I understand this action is permanent and want to delete this manager account.</span>
                    </label>
                </div>
            </form>
        </div>
        <div class="popup-footer">
            <button type="button" class="btn btn-secondary" onclick="closeDeleteManagerModal()">
                <i class="fas fa-times"></i> Cancel
            </button>
            <button type="button" class="btn btn-danger" id="confirmManagerDeleteBtn" disabled onclick="executeManagerDeletion()">
                <i class="fas fa-trash-alt"></i> Delete Manager
            </button>
        </div>
    </div>
</div>

<!-- Manager Success Modal -->
<div id="deleteSuccessModal" class="popup-overlay" style="display: none;">
    <div class="popup-content" style="border-top: 5px solid #28a745;">
        <div class="popup-body">
            <div style="text-align: center; padding: 1rem 0;">
                <i class="fas fa-check-circle" style="font-size: 4rem; color: #28a745; margin-bottom: 1rem;"></i>
                <h3 style="color: #28a745; font-size: 1.5rem; margin: 0 0 0.5rem 0;">Manager Deleted Successfully!</h3>
                <p style="color: #495057; font-size: 1rem; margin: 0 0 0.5rem 0;">
                    Manager <strong id="deletedManagerName">John Doe</strong> has been successfully deleted from the system.
                </p>
                <p style="color: #6c757d; font-size: 0.9rem; margin: 0;">
                    Leave application history has been preserved for audit purposes.
                </p>
            </div>
        </div>
        <div class="popup-footer">
            <button type="button" class="btn btn-success" onclick="closeDeleteSuccessModal()">
                <i class="fas fa-check"></i> OK
            </button>
        </div>
    </div>
</div>

<!-- Manager Error Modal -->
<div id="deleteErrorModal" class="popup-overlay" style="display: none;">
    <div class="popup-content" style="border-top: 5px solid #dc3545;">
        <div class="popup-body">
            <div style="text-align: center; padding: 1rem 0;">
                <i class="fas fa-exclamation-circle" style="font-size: 4rem; color: #dc3545; margin-bottom: 1rem;"></i>
                <h3 style="color: #dc3545; font-size: 1.5rem; margin: 0 0 0.5rem 0;">Deletion Failed</h3>
                <p id="deleteErrorMessage" style="color: #495057; font-size: 1rem; margin: 0 0 0.5rem 0;"></p>
                <p style="color: #6c757d; font-size: 0.9rem; margin: 0;">
                    Please try again or contact support if the problem persists.
                </p>
            </div>
        </div>
        <div class="popup-footer">
            <button type="button" class="btn btn-secondary" onclick="closeDeleteErrorModal()">
                <i class="fas fa-times"></i> Close
            </button>
        </div>
    </div>
</div>

    <!-- JavaScript -->
    <script src="/ELMS_3.0/Admin/Admin.js"></script>
    <script>
    let currentManagerToDelete = null;

 // Make functions globally available
 window.confirmDeleteManager = confirmDeleteManager;
 window.closeDeleteManagerModal = closeDeleteManagerModal;
 window.executeManagerDeletion = executeManagerDeletion;
 window.closeDeleteSuccessModal = closeDeleteSuccessModal;
 window.closeDeleteErrorModal = closeDeleteErrorModal;

 document.addEventListener('DOMContentLoaded', function() {
     console.log('✅ Admin manager deletion loaded');
     
     // Search functionality
     initializeSearch();
     
     // Form validation
     initializeFormValidation();
     
     // Modal handlers
     initializeModalHandlers();
 });

 function initializeSearch() {
     const searchInput = document.getElementById('searchInput');
     const managerTableBody = document.getElementById('managerTableBody');
     
     if (searchInput && managerTableBody) {
         searchInput.addEventListener('input', function() {
             const searchTerm = this.value.toLowerCase();
             const rows = managerTableBody.getElementsByTagName('tr');
             
             Array.from(rows).forEach(row => {
                 const text = row.textContent.toLowerCase();
                 row.style.display = text.includes(searchTerm) ? '' : 'none';
             });
         });
     }
 }

 function initializeFormValidation() {
     const passwordInput = document.getElementById('adminPasswordConfirm');
     const confirmCheckbox = document.getElementById('confirmManagerDelete');
     const deleteBtn = document.getElementById('confirmManagerDeleteBtn');

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
                 executeManagerDeletion();
             }
         });
     }
 }

 function initializeModalHandlers() {
     // Close on outside click
     document.addEventListener('click', function(event) {
         if (event.target.classList.contains('popup-overlay')) {
             const modalId = event.target.id;
             if (modalId === 'deleteManagerModal') closeDeleteManagerModal();
             if (modalId === 'deleteSuccessModal') closeDeleteSuccessModal();
             if (modalId === 'deleteErrorModal') closeDeleteErrorModal();
         }
     });

     // Close on Escape key
     document.addEventListener('keydown', function(event) {
         if (event.key === 'Escape') {
             closeDeleteManagerModal();
             closeDeleteSuccessModal();
             closeDeleteErrorModal();
         }
     });
 }

 // MAIN DELETION FUNCTIONS

 function confirmDeleteManager(managerId, managerName, managerEmail, managerPosition) {
     console.log('🎯 Opening deletion confirmation for:', managerId, managerName);
     
     // Validate inputs
     if (!managerId || !managerName) {
         showDeleteError('Invalid manager data provided');
         return;
     }
     
     // Store manager data
     currentManagerToDelete = {
         id: String(managerId).trim(),
         name: String(managerName).trim(),
         email: String(managerEmail || 'N/A').trim(),
         position: String(managerPosition || 'Manager').trim()
     };
     
     console.log('📝 Stored manager data:', currentManagerToDelete);
     
     // Get modal elements
     const modal = document.getElementById('deleteManagerModal');
     const nameEl = document.getElementById('deleteManagerDisplayName');
     const idEl = document.getElementById('deleteManagerDisplayId');
     const positionEl = document.getElementById('deleteManagerDisplayPosition');
     const form = document.getElementById('deleteManagerForm');
     const deleteBtn = document.getElementById('confirmManagerDeleteBtn');
     
     // Validate elements exist
     if (!modal || !nameEl || !idEl || !positionEl || !form || !deleteBtn) {
         showDeleteError('Modal elements not found. Please refresh the page.');
         return;
     }
     
     // Populate modal
     nameEl.textContent = currentManagerToDelete.name;
     idEl.textContent = currentManagerToDelete.id;
     positionEl.textContent = currentManagerToDelete.position;
     
     // Reset form
     form.reset();
     deleteBtn.disabled = true;
     deleteBtn.style.opacity = '0.6';
     
     // Show modal
     modal.style.display = 'flex';
     
     // Focus password input
     setTimeout(() => {
         const passwordInput = document.getElementById('adminPasswordConfirm');
         if (passwordInput) passwordInput.focus();
     }, 100);
 }

 function closeDeleteManagerModal() {
     const modal = document.getElementById('deleteManagerModal');
     if (modal) modal.style.display = 'none';
     // Keep currentManagerToDelete for potential retry
 }

 function executeManagerDeletion() {
     console.log('🚀 Starting deletion process...');
     
     // Validate manager data exists
     if (!currentManagerToDelete || !currentManagerToDelete.id) {
         console.error('❌ No manager data found');
         showDeleteError('No manager selected. Please try again.');
         return;
     }
     
     // Get form elements
     const passwordInput = document.getElementById('adminPasswordConfirm');
     const deleteBtn = document.getElementById('confirmManagerDeleteBtn');
     
     if (!passwordInput) {
         showDeleteError('Password input not found');
         return;
     }
     
     const password = passwordInput.value.trim();
     if (!password) {
         showDeleteError('Please enter your admin password');
         passwordInput.focus();
         return;
     }
     
     // Create a secure copy of manager data
     const managerToDelete = {
         id: currentManagerToDelete.id,
         name: currentManagerToDelete.name,
         email: currentManagerToDelete.email,
         position: currentManagerToDelete.position
     };
     
     console.log('🔐 Processing deletion for:', managerToDelete.id);
     
     // Show loading state
     if (deleteBtn) {
         deleteBtn.disabled = true;
         deleteBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Deleting...';
     }
     
     // Create form data
     const formData = new URLSearchParams();
     formData.append('action', 'delete');
     formData.append('managerId', managerToDelete.id);
     formData.append('adminPassword', password);
     
     // Send request
     fetch('/ELMS_3.0/AdminDeleteManagerController', {
         method: 'POST',
         headers: {
             'Content-Type': 'application/x-www-form-urlencoded',
             'X-Requested-With': 'XMLHttpRequest'
         },
         body: formData,
         credentials: 'same-origin'
     })
     .then(response => {
         console.log('📥 Response status:', response.status);
         
         if (!response.ok) {
             throw new Error(`HTTP ${response.status}: ${response.statusText}`);
         }
         
         return response.text();
     })
     .then(responseText => {
         console.log('📄 Server response:', `"${responseText}"`);
         
         const result = responseText.trim();
         
         // Handle different response types
         if (result === 'success') {
             console.log('✅ Deletion successful');
             handleDeletionSuccess(managerToDelete);
             
         } else if (result === 'invalid_password') {
             console.log('❌ Invalid password');
             showDeleteError('Invalid admin password. Please try again.');
             passwordInput.focus();
             passwordInput.select();
             
         } else if (result.startsWith('error:')) {
             console.log('❌ Server error');
             const errorMsg = result.replace('error:', '').trim();
             showDeleteError(errorMsg || 'Server error occurred');
             
         } else if (result.includes('<!DOCTYPE') || result.includes('<html')) {
             console.log('⚠️ HTML response received');
             // This might indicate success with a redirect
             handleDeletionSuccess(managerToDelete);
             
         } else {
             console.log('❓ Unexpected response');
             showDeleteError('Unexpected server response');
         }
     })
     .catch(error => {
         console.error('💥 Request error:', error);
         showDeleteError('Network error. Please try again.');
     })
     .finally(() => {
         // Reset button
         if (deleteBtn) {
             deleteBtn.disabled = false;
             deleteBtn.innerHTML = '<i class="fas fa-trash-alt"></i> Delete Manager';
         }
     });
 }

 function handleDeletionSuccess(managerData) {
     console.log('🎉 Handling successful deletion for:', managerData.name);
     
     // Close delete modal
     closeDeleteManagerModal();
     
     // Remove from table
     removeManagerFromTable(managerData.id);
     
     // Show success modal
     showDeleteSuccess(managerData.name);
     
     // Clear the global variable
     currentManagerToDelete = null;
 }

 function removeManagerFromTable(managerId) {
     console.log('🗑️ Removing manager row:', managerId);
     
     // Find the row by data attribute
     let row = document.querySelector(`tr[data-manager-id="${managerId}"]`);
     
     // Fallback: search by cell content
     if (!row) {
         const rows = document.querySelectorAll('#managerTableBody tr');
         for (const r of rows) {
             const codeEl = r.querySelector('code');
             if (codeEl && codeEl.textContent.trim() === managerId) {
                 row = r;
                 break;
             }
         }
     }
     
     if (row) {
         // Animate removal
         row.style.transition = 'all 0.4s ease';
         row.style.backgroundColor = '#f8d7da';
         row.style.opacity = '0.7';
         
         setTimeout(() => {
             row.style.opacity = '0';
             row.style.transform = 'translateX(-20px) scale(0.95)';
         }, 100);
         
         setTimeout(() => {
             row.remove();
             checkIfTableEmpty();
         }, 500);
         
     } else {
         console.warn('⚠️ Row not found, refreshing page');
         setTimeout(() => window.location.reload(), 1000);
     }
 }

 function checkIfTableEmpty() {
     const tableBody = document.getElementById('managerTableBody');
     if (tableBody && tableBody.children.length === 0) {
         const container = document.querySelector('.manager-table-container');
         if (container) {
             container.innerHTML = `
                 <div class="empty-state">
                     <i class="fas fa-user-tie"></i>
                     <h3>No Managers Found</h3>
                     <p>There are currently no approval managers in the system.</p>
                     <a href="/ELMS_3.0/Admin/AdminAddManager.jsp" class="btn btn-primary" style="margin-top: 1rem;">
                         <i class="fas fa-plus"></i> Add First Manager
                     </a>
                 </div>
             `;
         }
     }
 }

 // MODAL FUNCTIONS

 function showDeleteSuccess(managerName) {
     const nameEl = document.getElementById('deletedManagerName');
     const modal = document.getElementById('deleteSuccessModal');
     
     if (nameEl && modal) {
         nameEl.textContent = managerName;
         modal.style.display = 'flex';
         
         // Auto-close after 3 seconds
         setTimeout(closeDeleteSuccessModal, 3000);
     }
 }

 function closeDeleteSuccessModal() {
     const modal = document.getElementById('deleteSuccessModal');
     if (modal) modal.style.display = 'none';
 }

 function showDeleteError(message) {
     const messageEl = document.getElementById('deleteErrorMessage');
     const modal = document.getElementById('deleteErrorModal');
     
     if (messageEl && modal) {
         messageEl.textContent = message;
         modal.style.display = 'flex';
     } else {
         alert('Error: ' + message);
     }
 }

 function closeDeleteErrorModal() {
     const modal = document.getElementById('deleteErrorModal');
     if (modal) modal.style.display = 'none';
 }
 
//Add this function to your manager page JavaScript
 function toggleManagerDeletePasswordVisibility() {
     const passwordInput = document.getElementById('adminPasswordConfirm');
     const passwordIcon = document.getElementById('managerDeletePasswordIcon');
     
     if (passwordInput && passwordIcon) {
         if (passwordInput.type === 'password') {
             passwordInput.type = 'text';
             passwordIcon.classList.remove('fa-eye');
             passwordIcon.classList.add('fa-eye-slash');
         } else {
             passwordInput.type = 'password';
             passwordIcon.classList.remove('fa-eye-slash');
             passwordIcon.classList.add('fa-eye');
         }
     }
 }
    </script>
</body>
</html>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page import="elms.model.Manager" %>
<%
    // Check if manager is logged in
    Manager manager = (Manager) session.getAttribute("manager");
    if (manager == null) {
        response.sendRedirect(request.getContextPath() + "/Manager/ManagerLogin.jsp");
        return;
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="icon" type="image/png" href="<%= request.getContextPath() %>/imnsb_logo.png">
    <title>Pending Leave Requests - IMNSB Executive Director Portal</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/styles.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/dashboard.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <style>
        .leave-requests-container {
            max-width: 1400px;
            margin: 0 auto;
        }
        
        .filters-section {
            background: white;
            border-radius: 8px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
            margin-bottom: 20px;
            overflow: hidden;
        }
        
        .filters-header {
            background: linear-gradient(135deg, var(--primary-color), var(--primary-dark));
            color: white;
            padding: 15px 20px;
            display: flex;
            align-items: center;
            gap: 10px;
        }
        
        .filters-content {
            padding: 20px;
        }
        
        .filter-row {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 15px;
            margin-bottom: 15px;
        }
        
        .filter-group {
            display: flex;
            flex-direction: column;
        }
        
        .filter-group label {
            font-weight: 600;
            margin-bottom: 5px;
            color: #333;
        }
        
        .filter-group select, .filter-group input {
            padding: 8px 12px;
            border: 1px solid #ddd;
            border-radius: 4px;
            font-size: 14px;
        }
        
        .filter-buttons {
            display: flex;
            gap: 10px;
            justify-content: flex-end;
            margin-top: 15px;
        }
        
        .btn {
            padding: 8px 16px;
            border: none;
            border-radius: 4px;
            cursor: pointer;
            font-size: 14px;
            font-weight: 500;
            text-decoration: none;
            display: inline-flex;
            align-items: center;
            gap: 5px;
            transition: all 0.3s ease;
        }
        
        .btn-primary {
            background: #007bff;
            color: white;
        }
        
        .btn-primary:hover {
            background: #0056b3;
        }
        
        .btn-secondary {
            background: #6c757d;
            color: white;
        }
        
        .btn-secondary:hover {
            background: #545b62;
        }
        
        .stats-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
            gap: 20px;
            margin-bottom: 30px;
        }
        
        .stat-card {
            background: white;
            border-radius: 8px;
            padding: 20px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
            text-align: center;
        }
        
        .stat-card .stat-number {
            font-size: 2rem;
            font-weight: bold;
            margin-bottom: 5px;
        }
        
        .stat-card .stat-label {
            color: #666;
            font-size: 0.9rem;
        }
        
        .stat-pending { border-left: 4px solid #ffc107; }
        .stat-pending .stat-number { color: #ffc107; }
        
        .stat-urgent { border-left: 4px solid #dc3545; }
        .stat-urgent .stat-number { color: #dc3545; }
        
        .stat-today { border-left: 4px solid #007bff; }
        .stat-today .stat-number { color: #007bff; }
        
        .requests-table {
            background: white;
            border-radius: 8px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
            overflow: hidden;
        }
        
        .table-header {
            background: linear-gradient(135deg, var(--primary-color), var(--primary-dark));
            color: white;
            padding: 20px;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        
        .table-header h3 {
            margin: 0;
            display: flex;
            align-items: center;
            gap: 10px;
        }
        
        .page-size-selector {
            display: flex;
            align-items: center;
            gap: 10px;
            color: white;
        }
        
        .page-size-selector select {
            padding: 5px 8px;
            border: 1px solid white;
            border-radius: 4px;
            background: white;
            color: #333;
        }
        
        .table-content {
            overflow-x: auto;
        }
        
        table {
            width: 100%;
            border-collapse: collapse;
        }
        
        th, td {
            padding: 12px;
            text-align: left;
            border-bottom: 1px solid #ddd;
        }
        
        th {
            background: #f8f9fa;
            font-weight: 600;
            color: #333;
            position: sticky;
            top: 0;
        }
        
        .employee-info {
            display: flex;
            align-items: center;
            gap: 8px;
        }
        
        .employee-avatar {
            width: 32px;
            height: 32px;
            border-radius: 50%;
            object-fit: cover;
            border: 2px solid #ddd;
        }
        
        .employee-details {
            display: flex;
            flex-direction: column;
        }
        
        .employee-name {
            font-weight: 600;
            color: #333;
        }
        
        .employee-id {
            font-size: 0.8rem;
            color: #666;
        }
        
        .status-badge {
            padding: 4px 8px;
            border-radius: 12px;
            font-size: 0.8rem;
            font-weight: 500;
        }
        
        .status-pending { background: #fff3cd; color: #856404; }
        .status-approved { background: #d4edda; color: #155724; }
        .status-rejected { background: #f8d7da; color: #721c24; }
        .status-cancelled { background: #e2e3e5; color: #383d41; }
        
        .priority-badge {
            display: inline-flex;
            align-items: center;
            gap: 4px;
            padding: 2px 6px;
            border-radius: 8px;
            font-size: 0.75rem;
            font-weight: 500;
        }
        
        .priority-urgent {
            background: #ffebee;
            color: #c62828;
        }
        
        .priority-normal {
            background: #f3e5f5;
            color: #7b1fa2;
        }
        
        .action-buttons {
            display: flex;
            gap: 5px;
            flex-wrap: wrap;
        }
        
        .btn-sm {
            padding: 4px 8px;
            font-size: 0.75rem;
            border-radius: 4px;
            border: none;
            cursor: pointer;
            text-decoration: none;
            display: inline-flex;
            align-items: center;
            gap: 4px;
            transition: all 0.3s ease;
        }
        
        .btn-view {
            background: #17a2b8;
            color: white;
        }
        
        .btn-view:hover {
            background: #138496;
        }
        
        .btn-attachment {
            background: #28a745;
            color: white;
        }
        
        .btn-attachment:hover {
            background: #218838;
        }
        
        .btn-review {
            background: #007bff;
            color: white;
        }
        
        .btn-review:hover {
            background: #0056b3;
        }
        
        .btn-approve {
            background: #28a745;
            color: white;
        }
        
        .btn-approve:hover {
            background: #218838;
        }
        
        .btn-reject {
            background: #dc3545;
            color: white;
        }
        
        .btn-reject:hover {
            background: #c82333;
        }
        
        /* Pagination Styles */
        .pagination-container {
            padding: 20px;
            background: white;
            border-top: 1px solid #ddd;
            display: flex;
            justify-content: space-between;
            align-items: center;
            flex-wrap: wrap;
            gap: 15px;
        }
        
        .pagination-info {
            color: #666;
            font-size: 14px;
        }
        
        .pagination {
            display: flex;
            align-items: center;
            gap: 5px;
        }
        
        .pagination a, .pagination span {
            padding: 8px 12px;
            border: 1px solid #ddd;
            text-decoration: none;
            color: #007bff;
            border-radius: 4px;
            transition: all 0.3s ease;
            min-width: 40px;
            text-align: center;
        }
        
        .pagination a:hover {
            background: #f8f9fa;
            border-color: #007bff;
        }
        
        .pagination .current {
            background: #007bff;
            color: white;
            border-color: #007bff;
        }
        
        .pagination .disabled {
            color: #6c757d;
            cursor: not-allowed;
            background: #f8f9fa;
        }
        
        .pagination .disabled:hover {
            background: #f8f9fa;
            border-color: #ddd;
        }
        
        .pagination .ellipsis {
            color: #6c757d;
            cursor: default;
        }
        
        .empty-state {
            text-align: center;
            padding: 40px;
            color: #666;
        }
        
        .alert {
            padding: 15px;
            margin-bottom: 20px;
            border: 1px solid transparent;
            border-radius: 4px;
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
        
        .duration-badge {
            background: #e9ecef;
            color: #495057;
            padding: 2px 6px;
            border-radius: 10px;
            font-size: 0.75rem;
            font-weight: 500;
        }
        
        .leave-type-tag {
            background: #f8f9fa;
            color: #495057;
            padding: 2px 8px;
            border-radius: 4px;
            font-size: 0.8rem;
            border: 1px solid #dee2e6;
        }
        
        /* Modal styles for leave details */
        .modal {
            display: none;
            position: fixed;
            z-index: 1000;
            left: 0;
            top: 0;
            width: 100%;
            height: 100%;
            background-color: rgba(0,0,0,0.5);
        }
        
        .modal-content {
            background-color: white;
            margin: 5% auto;
            padding: 0;
            border-radius: 8px;
            width: 90%;
            max-width: 800px;
            max-height: 90vh;
            overflow-y: auto;
            box-shadow: 0 4px 20px rgba(0,0,0,0.3);
        }
        
        .modal-header {
            background: linear-gradient(135deg, #007bff, #0056b3);
            color: white;
            padding: 20px;
            border-radius: 8px 8px 0 0;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        
        .modal-header h3 {
            margin: 0;
            display: flex;
            align-items: center;
            gap: 10px;
        }
        
        .close {
            color: white;
            font-size: 28px;
            font-weight: bold;
            cursor: pointer;
            border: none;
            background: none;
        }
        
        .close:hover {
            opacity: 0.7;
        }
        
        .modal-body {
            padding: 20px;
        }
        
        .modal-footer {
            padding: 20px;
            border-top: 1px solid #ddd;
            display: flex;
            justify-content: flex-end;
            gap: 10px;
        }

        /* Enhanced Modal Styles - Similar to Employee Application Page */
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
		    padding: 30px;
		    border-radius: 10px;
		    min-width: 300px;
		    max-width: 500px;
		    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
		    animation: slideIn 0.3s ease-in-out;
		    text-align: center; /* This centers all text by default */
		}
        
        .popup-icon {
            font-size: 3rem;
		    margin-bottom: 15px;
		    display: flex;
		    justify-content: center;
		    align-items: center;
		    width: 100%;
        }
        
        .popup-content h3 {
		    margin: 0 0 15px 0;
		    color: #333;
		    text-align: center; /* Explicitly center headings */
		}
        
        .popup-content p {
		    margin: 0 0 20px 0;
		    color: #666;
		    line-height: 1.5;
		    text-align: center; /* Explicitly center paragraphs */
		}
        
        .popup-btn {
            background-color: #007bff;
            color: white;
            border: none;
            padding: 10px 30px;
            border-radius: 5px;
            cursor: pointer;
            font-size: 1rem;
            font-weight: 500;
            transition: background-color 0.3s ease;
            margin: 5px;
        }
        
        .popup-btn:hover {
            background-color: #0056b3;
        }
        
        .popup-btn.btn-secondary {
            background-color: #6c757d;
        }
        
        .popup-btn.btn-secondary:hover {
            background-color: #545b62;
        }
        
        .popup-btn.btn-success {
            background-color: #28a745;
        }
        
        .popup-btn.btn-success:hover {
            background-color: #1e7e34;
        }
        
        .popup-btn.btn-danger {
            background-color: #dc3545;
        }
        
        .popup-btn.btn-danger:hover {
            background-color: #c82333;
        }
        
        .success-popup .popup-content {
            border-top: 5px solid #28a745;
        }
        
        .success-popup .popup-icon {
            color: #28a745;
        }
        
        .error-popup .popup-content {
            border-top: 5px solid #dc3545;
        }
        
        .error-popup .popup-icon {
            color: #dc3545;
        }
        
        .warning-popup .popup-content {
            border-top: 5px solid #ffc107;
        }
        
        .warning-popup .popup-icon {
            color: #ffc107;
        }
        
        .info-popup .popup-content {
            border-top: 5px solid #17a2b8;
        }
        
        .info-popup .popup-icon {
            color: #17a2b8;
        }

        /* Rejection Modal with Text Area */
        .rejection-popup .popup-content {
		    max-width: 600px;
		    text-align: center; /* Center the main content */
		}

        .rejection-form {
            margin: 20px 0;
            text-align: left;
        }

        .rejection-form label {
            display: block;
            margin-bottom: 8px;
            font-weight: 600;
            color: #333;
            text-align: left;
        }

        .rejection-form textarea {
            width: 100%;
            padding: 12px;
            border: 1px solid #ddd;
            border-radius: 4px;
            font-size: 14px;
            resize: vertical;
            min-height: 100px;
            box-sizing: border-box;
        }

        .rejection-form textarea:focus {
            outline: none;
            border-color: #007bff;
            box-shadow: 0 0 0 3px rgba(0,123,255,0.1);
        }

        .rejection-buttons {
            display: flex;
            gap: 10px;
            justify-content: center;
            margin-top: 20px;
        }
        
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
        
        @media (max-width: 768px) {
            .filter-row {
                grid-template-columns: 1fr;
            }
            
            .stats-grid {
                grid-template-columns: repeat(2, 1fr);
            }
            
            .action-buttons {
                flex-direction: column;
            }
            
            .btn-sm {
                justify-content: center;
            }
            
            .pagination-container {
                flex-direction: column;
                text-align: center;
            }
            
            .pagination {
                flex-wrap: wrap;
                justify-content: center;
            }
            
            .table-header {
                flex-direction: column;
                gap: 10px;
                text-align: center;
            }
            
            .employee-info {
                flex-direction: column;
                text-align: center;
                gap: 4px;
            }
            
            .employee-details {
                align-items: center;
            }
            
            .modal-content {
                width: 95%;
                margin: 2% auto;
            }

            .popup-content {
                margin: 1rem;
                width: calc(100% - 2rem);
                min-width: auto;
            }
        }
    </style>
</head>
<body>
    <div class="dashboard-container">
        <!-- Executive Director Sidebar -->
        <nav class="sidebar" id="sidebar">
            <div class="sidebar-header">
                <div class="logo-container">
                    <img src="<%= request.getContextPath() %>/imnsb_logowbg.png" alt="IMNSB Logo" class="sidebar-logo">
                </div>
                <h3>IMNSB Executive Director</h3><br>
            </div>
            <ul class="sidebar-menu">
               <li><a href="/ELMS_3.0/Manager/ExecutiveDirectorDashboard.jsp"><i class="fas fa-user"></i> <span>Profile</span></a></li>
	            <li><a href="<%= request.getContextPath() %>/executive-director-project-managers"><i class="fas fa-users"></i> <span>Project Managers</span></a></li>
	            <li class="active"><a href="<%= request.getContextPath() %>/manager-pending-requests"><i class="fas fa-clock"></i> <span>Pending Requests</span></a></li>
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
                    <h2>Pending Leave Requests</h2>
                </div>
                <div class="header-right">
                    <span id="userName"><%= manager.getManagername() %></span>
                    <div class="user-avatar">
                        <%
                            String headerProfilePicPath = manager.getProfilePicturePath();
                            if (headerProfilePicPath != null && !headerProfilePicPath.isEmpty()) {
                        %>
                        <img src="/ELMS_3.0/<%= headerProfilePicPath %>" alt="Executive Director Avatar" id="headerAvatar">
                        <% } else { %>
                        <img src="<%= request.getContextPath() %>/defaultprofilepicture.jpg" alt="Executive Director Avatar" id="headerAvatar">
                        <% } %>
                    </div>
                </div>
            </header>
            
            <div class="content-body">
                <!-- Success/Info Messages -->
                <c:if test="${not empty successMessage}">
                    <div class="alert alert-success">
                        <i class="fas fa-check-circle"></i> ${successMessage}
                    </div>
                </c:if>
                
                <c:if test="${not empty infoMessage}">
                    <div class="alert alert-info">
                        <i class="fas fa-info-circle"></i> ${infoMessage}
                    </div>
                </c:if>
                
                <div class="leave-requests-container">

                    <!-- Statistics Cards - Pending Applications Only -->
                    <div class="stats-grid">
                        <div class="stat-card stat-pending">
                            <div class="stat-number">${totalPendingRequests}</div>
                            <div class="stat-label">Total Pending Requests</div>
                        </div>
                        <div class="stat-card stat-urgent">
                            <div class="stat-number">${urgentRequests}</div>
                            <div class="stat-label">Urgent (Within 3 Days)</div>
                        </div>
                        <div class="stat-card stat-today">
                            <div class="stat-number">${todayRequests}</div>
                            <div class="stat-label">Submitted Today</div>
                        </div>
                    </div>
                    
                     <!-- Filters Section -->
                    <div class="filters-section">
                        <div class="filters-header">
                            <i class="fas fa-filter"></i>
                            <h3>Filter Pending Applications</h3>
                        </div>
                        <div class="filters-content">
                            <form id="filterForm" method="GET" action="<%= request.getContextPath() %>/manager-pending-requests">
                                <!-- Hidden field to maintain page size -->
                                <input type="hidden" name="size" value="${pageSize}">
                                
                                <div class="filter-row">
                                   <div class="filter-group">
									    <label for="leaveTypeFilter">Leave Type</label>
									    <select id="leaveTypeFilter" name="leaveType">
									        <option value="">All Leave Types</option>
									        <!-- Options will be populated by JavaScript -->
									    </select>
									</div>
									                                    
                                    <div class="filter-group">
                                        <label for="employeeFilter">Employee</label>
                                        <input type="text" id="employeeFilter" name="employee" 
                                               placeholder="Search by name or ID..." 
                                               value="${param.employee}">
                                    </div>
                                    
                                    <div class="filter-group">
									    <label for="priorityFilter">Status/Priority</label>
									    <select id="priorityFilter" name="priority">
									        <option value="">All Status</option>
									        <optgroup label="Critical Priority">
									            <option value="overdue" ${param.priority == 'overdue' ? 'selected' : ''}>
									                🔴 Overdue (Already Started)
									            </option>
									        </optgroup>
									        <optgroup label="High Priority">
									            <option value="backdated" ${param.priority == 'backdated' ? 'selected' : ''}>
									                🟠 Backdated Applications
									            </option>
									            <option value="urgent" ${param.priority == 'urgent' ? 'selected' : ''}>
									                🟡 Urgent (Within 3 Days)
									            </option>
									        </optgroup>
									        <optgroup label="Normal Priority">
									            <option value="normal" ${param.priority == 'normal' ? 'selected' : ''}>
									                🟢 Normal (More than 3 Days)
									            </option>
									        </optgroup>
									    </select>
									</div>
                                    
                                    <div class="filter-group">
                                        <label for="fromDate">Applied From</label>
                                        <input type="date" id="fromDate" name="fromDate" value="${param.fromDate}">
                                    </div>
                                    
                                    <div class="filter-group">
                                        <label for="toDate">Applied To</label>
                                        <input type="date" id="toDate" name="toDate" value="${param.toDate}">
                                    </div>
                                </div>
                                
                                <div class="filter-buttons">
								    <button type="button" id="clearFilters" class="btn btn-secondary">
								        <i class="fas fa-eraser"></i> Clear Filters
								    </button>
								    <button type="submit" class="btn btn-primary">
								        <i class="fas fa-search"></i> Apply Filters
								    </button>
								</div>
                            </form>
                        </div>
                    </div>
                    
                    <!-- Pending Requests Table -->
                    <div class="requests-table">
                        <div class="table-header">
                            <h3><i class="fas fa-clock"></i> Pending Approval Requests</h3>
                            <div class="page-size-selector">
                                <label for="pageSizeSelect">Show:</label>
                                <select id="pageSizeSelect" onchange="changePageSize(this.value)">
                                    <c:forEach var="size" items="${allowedPageSizes}">
                                        <option value="${size}" ${pageSize == size ? 'selected' : ''}>${size}</option>
                                    </c:forEach>
                                </select>
                                <span>per page</span>
                            </div>
                        </div>
                        <div class="table-content">
                            <c:choose>
                                <c:when test="${not empty pendingRequests}">
                                    <table>
                                        <thead>
										    <tr>
										        <th>Status</th>
										        <th>Employee</th>
										        <th>Application ID</th>
										        <th>Leave Type</th>
										        <th>Start Date</th>
										        <th>End Date</th>
										        <th>Duration</th>
										        <th>Applied On</th>
										        <th>Actions</th>
										    </tr>
										</thead>
                                        <tbody>
                                            <c:forEach var="request" items="${pendingRequests}">
                                                <tr>
                                                    <td>
													    <%-- Calculate application status based on dates --%>
													    <fmt:parseDate value="${request.leavestartdate}" pattern="yyyy-MM-dd" var="startDate"/>
													    <fmt:parseDate value="${request.appliedon}" pattern="yyyy-MM-dd HH:mm:ss" var="appliedDate"/>
													    <c:set var="currentDate" value="<%= new java.util.Date() %>" />
													    
													    <%-- Compare dates (date only, ignore time) --%>
													    <fmt:formatDate value="${startDate}" pattern="yyyy-MM-dd" var="startDateStr"/>
													    <fmt:formatDate value="${appliedDate}" pattern="yyyy-MM-dd" var="appliedDateStr"/>
													    <fmt:formatDate value="${currentDate}" pattern="yyyy-MM-dd" var="currentDateStr"/>
													    
													    <c:choose>
													        <%-- CRITICAL: Overdue (most urgent) --%>
													        <c:when test="${startDate.time < currentDate.time && request.leavestatus == 'Pending'}">
													            <span class="priority-badge" style="background: #ffebee; color: #c62828; font-weight: 700; border: 1px solid #c62828;">
													                <i class="fas fa-exclamation-circle"></i> OVERDUE
													            </span>
													        </c:when>
													        <%-- HIGH: Backdated --%>
													        <c:when test="${startDate.time < appliedDate.time}">
													            <span class="priority-badge" style="background: #fff3cd; color: #856404; font-weight: 600;">
													                <i class="fas fa-history"></i> Backdated
													            </span>
													        </c:when>
													        <%-- MEDIUM: Urgent (within 3 days) --%>
													        <c:when test="${(startDate.time - currentDate.time) / (1000*60*60*24) <= 3 && (startDate.time - currentDate.time) / (1000*60*60*24) >= 0}">
													            <span class="priority-badge" style="background: #ffe5e5; color: #d32f2f; font-weight: 600;">
													                <i class="fas fa-exclamation-triangle"></i> Urgent
													            </span>
													        </c:when>
													        <%-- LOW: Normal --%>
													        <c:otherwise>
													            <span class="priority-badge priority-normal">
													                <i class="fas fa-check-circle"></i> Normal
													            </span>
													        </c:otherwise>
													    </c:choose>
													</td>
                                                   
                                                    <td>
                                                        <div class="employee-info">
                                                            <img src="${empty employeeProfilePictures[request.employeeid] ? pageContext.request.contextPath.concat('/defaultprofilepicture.jpg') : pageContext.request.contextPath.concat('/').concat(employeeProfilePictures[request.employeeid])}" 
                                                                 alt="Employee Avatar" class="employee-avatar"
                                                                 onerror="this.src='${pageContext.request.contextPath}/defaultprofilepicture.jpg';">
                                                            <div class="employee-details">
                                                                <div class="employee-name">${employeeNames[request.employeeid]}</div>
                                                                <div class="employee-id">${request.employeeid}</div>
                                                            </div>
                                                        </div>
                                                    </td>
                                                    <td>
                                                        <strong>${request.applicationid}</strong>
                                                    </td>
                                                    <td>
                                                        <span class="leave-type-tag">
                                                            <c:choose>
                                                                <c:when test="${not empty leaveTypeNames[request.leavetypeid]}">
                                                                    ${leaveTypeNames[request.leavetypeid]}
                                                                </c:when>
                                                                <c:otherwise>
                                                                    Unknown Type
                                                                </c:otherwise>
                                                            </c:choose>
                                                        </span>
                                                    </td>
                                                    <td>
                                                        <fmt:parseDate value="${request.leavestartdate}" pattern="yyyy-MM-dd" var="startDate"/>
                                                        <fmt:formatDate value="${startDate}" pattern="MMM dd, yyyy"/>
                                                    </td>
                                                    <td>
                                                        <fmt:parseDate value="${request.leaveenddate}" pattern="yyyy-MM-dd" var="endDate"/>
                                                        <fmt:formatDate value="${endDate}" pattern="MMM dd, yyyy"/>
                                                    </td>
                                                    <td>
                                                        <span class="duration-badge">${request.leaveduration} day(s)</span>
                                                    </td>
                                                    <td>
                                                        <fmt:parseDate value="${request.appliedon}" pattern="yyyy-MM-dd HH:mm:ss" var="appliedDate"/>
                                                        <fmt:formatDate value="${appliedDate}" pattern="MMM dd, yyyy"/>
                                                    </td>
                                                    <td>
                                                        <div class="action-buttons">
                                                            <!-- Review Application Button (Primary Action) -->
                                                            <button onclick="viewLeaveDetails('${request.applicationid}')" 
                                                                    class="btn-sm btn-review" title="Review Application">
                                                                <i class="fas fa-eye"></i> Review
                                                            </button>
                                                            
                                                            <!-- View Attachment Button -->
                                                            <c:if test="${not empty request.attachment}">
                                                                <a href="<%= request.getContextPath() %>/manager-file-download?file=${request.attachment}&applicationId=${request.applicationid}" 
                                                                   target="_blank" class="btn-sm btn-attachment" title="View Attachment">
                                                                    <i class="fas fa-paperclip"></i> File
                                                                </a>
                                                            </c:if>
                                                        </div>
                                                    </td>
                                                </tr>
                                            </c:forEach>
                                        </tbody>
                                    </table>
                                </c:when>
                                <c:otherwise>
                                    <div class="empty-state">
                                        <i class="fas fa-calendar-check" style="font-size: 3rem; color: #ccc; margin-bottom: 20px;"></i>
                                        <h3>No Pending Requests</h3>
                                        <p>There are no pending leave requests that require your approval at this time.</p>
                                        <c:if test="${not empty param.leaveType or not empty param.employee or not empty param.priority or not empty param.fromDate or not empty param.toDate}">
								            <button onclick="clearAllFilters()" class="btn btn-secondary" style="margin-top: 15px;">
								                <i class="fas fa-eraser"></i> Clear All Filters
								            </button>
                                        </c:if>
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </div>
                        
                        <!-- Pagination Controls -->
						<c:if test="${totalPages > 1}">
						    <div class="pagination-container">
						        <div class="pagination-info">
						            ${showingInfo}
						        </div>
						        <div class="pagination">
						            <!-- First Page -->
						            <c:if test="${currentPage > 1}">
						                <a href="?page=1&size=${pageSize}&leaveType=${param.leaveType}&employee=${param.employee}&priority=${param.priority}&fromDate=${param.fromDate}&toDate=${param.toDate}" title="First Page">
						                    <i class="fas fa-angle-double-left"></i>
						                </a>
						            </c:if>
						            <c:if test="${currentPage <= 1}">
						                <span class="disabled">
						                    <i class="fas fa-angle-double-left"></i>
						                </span>
						            </c:if>
						            
						            <!-- Previous Page -->
						            <c:if test="${currentPage > 1}">
						                <a href="?page=${currentPage - 1}&size=${pageSize}&leaveType=${param.leaveType}&employee=${param.employee}&priority=${param.priority}&fromDate=${param.fromDate}&toDate=${param.toDate}" title="Previous Page">
						                    <i class="fas fa-angle-left"></i>
						                </a>
						            </c:if>
						            <c:if test="${currentPage <= 1}">
						                <span class="disabled">
						                    <i class="fas fa-angle-left"></i>
						                </span>
						            </c:if>
						            
						            <!-- Page Numbers -->
						            <c:forEach var="pageNum" begin="${startPage}" end="${endPage}">
						                <c:choose>
						                    <c:when test="${pageNum == currentPage}">
						                        <span class="current">${pageNum}</span>
						                    </c:when>
						                    <c:otherwise>
						                        <a href="?page=${pageNum}&size=${pageSize}&leaveType=${param.leaveType}&employee=${param.employee}&priority=${param.priority}&fromDate=${param.fromDate}&toDate=${param.toDate}">${pageNum}</a>
						                    </c:otherwise>
						                </c:choose>
						            </c:forEach>
						            
						            <!-- Show ellipsis if there are more pages -->
						            <c:if test="${endPage < totalPages}">
						                <c:if test="${endPage < totalPages - 1}">
						                    <span class="ellipsis">...</span>
						                </c:if>
						                <a href="?page=${totalPages}&size=${pageSize}&leaveType=${param.leaveType}&employee=${param.employee}&priority=${param.priority}&fromDate=${param.fromDate}&toDate=${param.toDate}">${totalPages}</a>
						            </c:if>
						            
						            <!-- Next Page -->
						            <c:if test="${currentPage < totalPages}">
						                <a href="?page=${currentPage + 1}&size=${pageSize}&leaveType=${param.leaveType}&employee=${param.employee}&priority=${param.priority}&fromDate=${param.fromDate}&toDate=${param.toDate}" title="Next Page">
						                    <i class="fas fa-angle-right"></i>
						                </a>
						            </c:if>
						            <c:if test="${currentPage >= totalPages}">
						                <span class="disabled">
						                    <i class="fas fa-angle-right"></i>
						                </span>
						            </c:if>
						            
						            <!-- Last Page -->
						            <c:if test="${currentPage < totalPages}">
						                <a href="?page=${totalPages}&size=${pageSize}&leaveType=${param.leaveType}&employee=${param.employee}&priority=${param.priority}&fromDate=${param.fromDate}&toDate=${param.toDate}" title="Last Page">
						                    <i class="fas fa-angle-double-right"></i>
						                </a>
						            </c:if>
						            <c:if test="${currentPage >= totalPages}">
						                <span class="disabled">
						                    <i class="fas fa-angle-double-right"></i>
						                </span>
						            </c:if>
						        </div>
						    </div>
						</c:if>
                    </div>
                </div>
            </div>
        </main>
    </div>

    <!-- Leave Details Modal -->
    <div id="leaveDetailsModal" class="modal">
        <div class="modal-content">
            <div class="modal-header">
                <h3><i class="fas fa-file-alt"></i> Leave Application Details</h3>
                <span class="close" onclick="closeModal()">&times;</span>
            </div>
            <div class="modal-body" id="modalBody">
                <!-- Application details will be loaded here -->
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" onclick="closeModal()">
                    <i class="fas fa-times"></i> Close
                </button>
                <button type="button" class="btn btn-reject" onclick="showRejectModal()" id="rejectBtn">
                    <i class="fas fa-times-circle"></i> Reject
                </button>
                <button type="button" class="btn btn-approve" onclick="showApproveModal()" id="approveBtn">
                    <i class="fas fa-check-circle"></i> Approve
                </button>
            </div>
        </div>
    </div>

    <!-- Success Modal -->
    <div id="successModal" class="popup-overlay success-popup" style="display: none;">
        <div class="popup-content">
            <div class="popup-icon">✅</div>
            <h3>Action Completed Successfully!</h3>
            <p id="successMessage"></p>
            <button class="popup-btn btn-success" onclick="closeSuccessModal()">Continue</button>
        </div>
    </div>

    <!-- Error Modal -->
    <div id="errorModal" class="popup-overlay error-popup" style="display: none;">
        <div class="popup-content">
            <div class="popup-icon">❌</div>
            <h3>Action Failed</h3>
            <p id="errorMessage"></p>
            <button class="popup-btn" onclick="closeErrorModal()">Try Again</button>
        </div>
    </div>

    <!-- Approval Confirmation Modal -->
    <div id="approveConfirmModal" class="popup-overlay info-popup" style="display: none;">
        <div class="popup-content">
            <div class="popup-icon">❓</div>
            <h3>Confirm Approval</h3>
            <p id="approveConfirmMessage">Are you sure you want to approve this leave application?</p>
            <div>
                <button class="popup-btn btn-secondary" onclick="closeApproveConfirmModal()">Cancel</button>
                <button class="popup-btn btn-success" onclick="confirmApproval()">
                    <i class="fas fa-check-circle"></i> Yes, Approve
                </button>
            </div>
        </div>
    </div>

    <!-- Rejection Modal with Reason -->
    <div id="rejectModal" class="popup-overlay warning-popup rejection-popup" style="display: none;">
        <div class="popup-content">
            <div class="popup-icon">⚠️</div>
            <h3>Reject Leave Application</h3>
            <p>Please provide a reason for rejecting this leave application:</p>
            
            <div class="rejection-form">
                <label for="rejectionReason">Rejection Reason <span style="color: red;">*</span></label>
                <textarea id="rejectionReason" placeholder="Enter the reason for rejection..." required></textarea>
            </div>
            
            <div class="rejection-buttons">
                <button class="popup-btn btn-secondary" onclick="closeRejectModal()">Cancel</button>
                <button class="popup-btn btn-danger" onclick="confirmRejection()">
                    <i class="fas fa-times-circle"></i> Reject Application
                </button>
            </div>
        </div>
    </div>

    <!-- Processing Modal -->
    <div id="processingModal" class="popup-overlay info-popup" style="display: none;">
        <div class="popup-content">
            <div class="popup-icon">
                <i class="fas fa-spinner fa-spin"></i>
            </div>
            <h3>Processing...</h3>
            <p id="processingMessage">Please wait while we process your request.</p>
        </div>
    </div>

<script src="/ELMS_3.0/Manager/Manager.js"></script>
<script>
    let currentApplicationId = null;
    
    // Change page size functionality
    function changePageSize(newSize) {
        const currentUrl = new URL(window.location);
        currentUrl.searchParams.set('size', newSize);
        currentUrl.searchParams.set('page', '1'); // Reset to first page
        window.location.href = currentUrl.toString();
    }
    
    // Clear filters functionality
    function clearAllFilters() {
        // Get the current servlet path to maintain correct URL
        const currentPath = window.location.pathname;
        const url = new URL(window.location);
        url.search = '?size=' + ${pageSize}; // Keep current page size
        window.location.href = url.toString();
    }
    
    // View leave details modal
    function viewLeaveDetails(applicationId) {
        currentApplicationId = applicationId;
        // AJAX call to fetch application details
        fetch('<%= request.getContextPath() %>/manager-leave-details?applicationId=' + applicationId)
            .then(response => response.text())
            .then(data => {
                document.getElementById('modalBody').innerHTML = data;
                document.getElementById('leaveDetailsModal').style.display = 'block';
            })
            .catch(error => {
                console.error('Error:', error);
                showErrorModal('Error loading application details');
            });
    }
    
    // Close modal
    function closeModal() {
        document.getElementById('leaveDetailsModal').style.display = 'none';
        currentApplicationId = null;
    }

    // Enhanced Modal Functions
    function showSuccessModal(message) {
        document.getElementById('successMessage').textContent = message;
        document.getElementById('successModal').style.display = 'flex';
    }

    function closeSuccessModal() {
        document.getElementById('successModal').style.display = 'none';
        // Reload the page to reflect changes
        window.location.reload();
    }

    function showErrorModal(message) {
        document.getElementById('errorMessage').textContent = message;
        document.getElementById('errorModal').style.display = 'flex';
    }

    function closeErrorModal() {
        document.getElementById('errorModal').style.display = 'none';
    }

    function showProcessingModal(message) {
        document.getElementById('processingMessage').textContent = message;
        document.getElementById('processingModal').style.display = 'flex';
    }

    function closeProcessingModal() {
        document.getElementById('processingModal').style.display = 'none';
    }

    // Approval Modal Functions
    function showApproveModal() {
        if (!currentApplicationId) return;
        
        const message = `Are you sure you want to approve leave application ${currentApplicationId}?`;
        document.getElementById('approveConfirmMessage').textContent = message;
        document.getElementById('approveConfirmModal').style.display = 'flex';
    }

    function closeApproveConfirmModal() {
        document.getElementById('approveConfirmModal').style.display = 'none';
    }

    function confirmApproval() {
        if (!currentApplicationId) return;
        
        closeApproveConfirmModal();
        showProcessingModal('Approving leave application...');
        
        // AJAX call to approve
        fetch('<%= request.getContextPath() %>/manager-approve-leave', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: 'applicationId=' + currentApplicationId + '&action=approve'
        })
        .then(response => response.json())
        .then(data => {
            closeProcessingModal();
            if (data.success) {
                closeModal();
                showSuccessModal(`Leave application ${currentApplicationId} has been approved successfully!`);
            } else {
                showErrorModal('Error: ' + (data.message || 'Failed to approve application'));
            }
        })
        .catch(error => {
            closeProcessingModal();
            console.error('Error:', error);
            showErrorModal('Network error occurred while approving the application');
        });
    }

    // Rejection Modal Functions
    function showRejectModal() {
        if (!currentApplicationId) return;
        
        document.getElementById('rejectionReason').value = '';
        document.getElementById('rejectModal').style.display = 'flex';
        // Focus on the textarea
        setTimeout(() => {
            document.getElementById('rejectionReason').focus();
        }, 300);
    }

    function closeRejectModal() {
        document.getElementById('rejectModal').style.display = 'none';
        document.getElementById('rejectionReason').value = '';
    }

    function confirmRejection() {
        if (!currentApplicationId) return;
        
        const reason = document.getElementById('rejectionReason').value.trim();
        
        if (!reason) {
            // Highlight the textarea to show it's required
            const textarea = document.getElementById('rejectionReason');
            textarea.style.borderColor = '#dc3545';
            textarea.style.boxShadow = '0 0 0 0.2rem rgba(220, 53, 69, 0.25)';
            textarea.focus();
            return;
        }
        
        closeRejectModal();
        showProcessingModal('Rejecting leave application...');
        
        // AJAX call to reject
        fetch('<%= request.getContextPath() %>/manager-approve-leave', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: 'applicationId=' + currentApplicationId + '&action=reject&reason=' + encodeURIComponent(reason)
        })
        .then(response => response.json())
        .then(data => {
            closeProcessingModal();
            if (data.success) {
                closeModal();
                showSuccessModal(`Leave application has been rejected.`);
            } else {
                showErrorModal('Error: ' + (data.message || 'Failed to reject application'));
            }
        })
        .catch(error => {
            closeProcessingModal();
            console.error('Error:', error);
            showErrorModal('Network error occurred while rejecting the application');
        });
    }

    // Clear textarea border color on input
    document.addEventListener('DOMContentLoaded', function() {
        const rejectionReason = document.getElementById('rejectionReason');
        if (rejectionReason) {
            rejectionReason.addEventListener('input', function() {
                this.style.borderColor = '';
                this.style.boxShadow = '';
            });
        }
    });
    
    // Close modals when clicking outside
    window.onclick = function(event) {
        const modals = [
            'leaveDetailsModal', 
            'successModal', 
            'errorModal', 
            'approveConfirmModal', 
            'rejectModal', 
            'processingModal'
        ];
        
        modals.forEach(modalId => {
            const modal = document.getElementById(modalId);
            if (event.target === modal) {
                if (modalId === 'leaveDetailsModal') {
                    closeModal();
                } else if (modalId === 'successModal') {
                    closeSuccessModal();
                } else if (modalId === 'errorModal') {
                    closeErrorModal();
                } else if (modalId === 'approveConfirmModal') {
                    closeApproveConfirmModal();
                } else if (modalId === 'rejectModal') {
                    closeRejectModal();
                } else if (modalId === 'processingModal') {
                    closeProcessingModal();
                }
            }
        });
    }

    // Close modals with Escape key
    document.addEventListener('keydown', function(event) {
        if (event.key === 'Escape') {
            // Close all modals
            closeModal();
            closeSuccessModal();
            closeErrorModal();
            closeApproveConfirmModal();
            closeRejectModal();
            closeProcessingModal();
        }
    });
    
    document.getElementById('clearFilters').addEventListener('click', clearAllFilters);
    
    // DOMContentLoaded event
    document.addEventListener('DOMContentLoaded', function() {
        console.log('Pending requests page loaded');
        console.log('Current page:', ${currentPage});
        console.log('Total pages:', ${totalPages});
        console.log('Page size:', ${pageSize});
        console.log('Total pending records:', ${totalRecords});
		
        populateLeaveTypeFilter();
        
        // Check for server messages and show appropriate modals
        <c:if test="${not empty successMessage}">
        showSuccessModal('${successMessage}');
        </c:if>
        
        <c:if test="${not empty errorMessage}">
        showErrorModal('${errorMessage}');
        </c:if>
    });
    
 // ========================================
 // POPULATE AND SORT LEAVE TYPE FILTER
 // ========================================

 function populateLeaveTypeFilter() {
     const leaveTypeSelect = document.getElementById('leaveTypeFilter');
     
     if (!leaveTypeSelect) {
         console.log('Leave type filter not found');
         return;
     }
     
     // Get the current selected value (if any)
     const currentSelection = '${param.leaveType}';
     
     // Collect all leave types from the JSP
     const leaveTypes = [];
     <c:forEach var="entry" items="${leaveTypeNames}">
         leaveTypes.push({
             key: '${entry.key}',
             value: '${entry.value}'
         });
     </c:forEach>
     
     // Sort alphabetically by leave type name
     leaveTypes.sort((a, b) => a.value.localeCompare(b.value));
     
     // Clear existing options except the first one ("All Leave Types")
     while (leaveTypeSelect.options.length > 1) {
         leaveTypeSelect.remove(1);
     }
     
     // Populate with sorted leave types
     leaveTypes.forEach(function(leaveType) {
         const option = document.createElement('option');
         option.value = leaveType.key;
         option.textContent = leaveType.value;
         
         // Restore previous selection if it exists
         if (currentSelection && currentSelection === leaveType.key) {
             option.selected = true;
         }
         
         leaveTypeSelect.appendChild(option);
     });
     
     console.log('✅ Manager leave type filter populated and sorted (' + leaveTypes.length + ' types)');
 }
</script>
</body>
</html>
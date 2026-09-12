<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="elms.model.Employee" %>
<%@ page import="elms.controller.ManagerApproveLeaveController" %>
<%@ page import="java.util.Set" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%
    Employee employee = (Employee) session.getAttribute("loggedInEmployee");
    if (employee == null) {
        response.sendRedirect(request.getContextPath() + "/Employee/EmployeeLogin.jsp");
        return;
    }

    // Get newly updated applications for alert notifications
    Set<String> newlyUpdatedApps = ManagerApproveLeaveController.getNewlyUpdatedApplications(employee.getEmployeeId());
    int newUpdatesCount = newlyUpdatedApps.size();
    
    // Make newlyUpdatedApps available to JSP
    request.setAttribute("newlyUpdatedApps", newlyUpdatedApps);
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="icon" type="image/png" href="<%= request.getContextPath() %>/imnsb_logo.png">
    <title>Leave History - IMNSB Employee Leave Management System</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/styles.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/dashboard.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
   

<style>
    .leave-history-container {
        max-width: 1200px;
        margin: 0 auto;
    }
    
  
    .table-row-new-update {
        background: linear-gradient(90deg, #f8f9fa, #ffffff) !important;
        border-left: 3px solid #007bff !important;
        position: relative;
        animation: professional-highlight 0.5s ease-in-out;
        transition: all 0.3s ease;
    }
    
    @keyframes professional-highlight {
        0% { 
            background: linear-gradient(90deg, #e3f2fd, #f8f9fa);
            transform: translateX(-2px);
        }
        100% { 
            background: linear-gradient(90deg, #f8f9fa, #ffffff);
            transform: translateX(0);
        }
    }
    
    /* Subtle fade-out animation */
    .table-row-highlight-fadeout {
        background: linear-gradient(90deg, #ffffff, #ffffff) !important;
        border-left: none !important;
        animation: highlight-fadeout 1s ease-out;
    }
    
    @keyframes highlight-fadeout {
        0% { 
            background: linear-gradient(90deg, #f8f9fa, #ffffff);
            border-left: 3px solid #007bff;
        }
        100% { 
            background: linear-gradient(90deg, #ffffff, #ffffff);
            border-left: none;
        }
    }
    
    /* Enhanced Table Row Styling */
    table {
        width: 100%;
        border-collapse: collapse;
        table-layout: fixed;
    }
    
    th, td {
        padding: 12px;
        text-align: left;
        border-bottom: 1px solid #ddd;
        vertical-align: middle;
        position: relative;
        word-wrap: break-word;
        overflow: hidden;
    }
    
    /* Fixed column widths to prevent shifting */
    th:nth-child(1), td:nth-child(1) { width: 12%; } /* Application ID */
    th:nth-child(2), td:nth-child(2) { width: 15%; } /* Leave Type */
    th:nth-child(3), td:nth-child(3) { width: 12%; } /* Start Date */
    th:nth-child(4), td:nth-child(4) { width: 12%; } /* End Date */
    th:nth-child(5), td:nth-child(5) { width: 8%; }  /* Duration */
    th:nth-child(6), td:nth-child(6) { width: 15%; } /* Status */
    th:nth-child(7), td:nth-child(7) { width: 12%; } /* Applied On */
    th:nth-child(8), td:nth-child(8) { width: 14%; } /* Actions */
    
    /* Clean NEW badge - More professional */
   .table-row-new-update {
    background-color: #f0f8ff !important; /* Very light blue background */
    transition: background-color 0.3s ease;
}

/* Simple fade-out to normal */
.table-row-highlight-fadeout {
    background-color: #ffffff !important;
    transition: background-color 1s ease-out;
}

/* Keep the NEW badge simple */
	.new-update-badge {
	    display: inline-flex;
	    align-items: center;
	    gap: 4px;
	    background: #007bff;
	    color: white;
	    padding: 2px 8px;
	    border-radius: 12px;
	    font-size: 0.65rem;
	    font-weight: 600;
	    margin-left: 8px;
	    vertical-align: middle;
	}

/* Remove the glow effects - keep status badges normal */
	.status-badge {
	    padding: 4px 8px;
	    border-radius: 12px;
	    font-size: 0.8rem;
	    font-weight: 500;
	    position: relative;
	    display: inline-flex;
	    align-items: center;
	    gap: 4px;
	    transition: all 0.3s ease;
	}
    
   
    
    @keyframes clean-glow {
        0% { box-shadow: 0 0 0 2px rgba(0, 123, 255, 0.3); }
        50% { box-shadow: 0 0 0 2px rgba(0, 123, 255, 0.6); }
        100% { box-shadow: 0 0 0 2px rgba(0, 123, 255, 0.3); }
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
        grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
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
    
    .stat-approved { border-left: 4px solid #28a745; }
    .stat-approved .stat-number { color: #28a745; }
    
    .stat-rejected { border-left: 4px solid #dc3545; }
    .stat-rejected .stat-number { color: #dc3545; }
    
    .stat-cancelled { border-left: 4px solid #6c757d; }
    .stat-cancelled .stat-number { color: #6c757d; }
    
    .stat-total { border-left: 4px solid #007bff; }
    .stat-total .stat-number { color: #007bff; }
    
    .applications-table {
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
    
    th {
        background: #f8f9fa;
        font-weight: 600;
        color: #333;
    }
    
    .status-pending { background: #fff3cd; color: #856404; }
    .status-approved { background: #d4edda; color: #155724; }
    .status-rejected { 
        background: #f8d7da; 
        color: #721c24; 
        cursor: pointer;
        transition: all 0.3s ease;
    }
    .status-rejected:hover {
        opacity: 0.8;
        transform: scale(1.02);
    }
    .status-cancelled { background: #e2e3e5; color: #383d41; }
    
    /* Rejection reason modal styling */
    .rejection-modal {
        display: none;
        position: fixed;
        z-index: 1050;
        left: 0;
        top: 0;
        width: 100%;
        height: 100%;
        background-color: rgba(0,0,0,0.5);
    }
    
    .rejection-modal-content {
        background-color: #fefefe;
        margin: 15% auto;
        padding: 0;
        border: none;
        border-radius: 8px;
        width: 500px;
        max-width: 90%;
        box-shadow: 0 4px 20px rgba(0,0,0,0.3);
    }
    
    .rejection-modal-header {
        background: #dc3545;
        color: white;
        padding: 15px 20px;
        border-radius: 8px 8px 0 0;
        display: flex;
        align-items: center;
        gap: 10px;
    }
    
    .rejection-modal-header h3 {
        margin: 0;
        font-size: 1.2rem;
    }
    
    .rejection-modal-body {
        padding: 20px;
    }
    
    .rejection-reason-text {
        color: #333;
        font-size: 14px;
        line-height: 1.6;
        word-wrap: break-word;
        background: #f8f9fa;
        padding: 15px;
        border-radius: 6px;
        border-left: 4px solid #dc3545;
    }
    
    .rejection-modal-footer {
        padding: 15px 20px;
        border-top: 1px solid #ddd;
        display: flex;
        justify-content: flex-end;
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
    
    .btn-cancel {
        background: #dc3545;
        color: white;
    }
    
    .btn-cancel:hover {
        background: #c82333;
    }
    
    .empty-state {
        text-align: center;
        padding: 40px;
        color: #666;
    }
    
    .alert {
        padding: 12px;
        margin-bottom: 15px;
        border: 1px solid transparent;
        border-radius: 4px;
        font-size: 0.9rem;
    }
    
    .alert-success {
        color: #155724;
        background-color: #d4edda;
        border-color: #c3e6cb;
    }
    
    .alert-danger {
        color: #721c24;
        background-color: #f8d7da;
        border-color: #f5c6cb;
    }
    
    /* Modal Styles */
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
        text-align: center;
        min-width: 300px;
        max-width: 500px;
        box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
        animation: slideIn 0.3s ease-in-out;
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
        text-align: center;
    }
    
    .popup-content p {
        margin: 0 0 20px 0;
        color: #666;
        line-height: 1.5;
        text-align: center;
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
    
    /* Pagination Styles */
    .pagination-container {
        padding: 20px;
        display: flex;
        justify-content: space-between;
        align-items: center;
        border-top: 1px solid #ddd;
        background: #f8f9fa;
    }
    
    .pagination-info {
        color: #666;
        font-size: 0.9rem;
    }
    
    .pagination {
        display: flex;
        gap: 5px;
        align-items: center;
    }
    
    .pagination a, .pagination span {
        padding: 8px 12px;
        border: 1px solid #ddd;
        border-radius: 4px;
        text-decoration: none;
        color: #007bff;
        background: white;
        transition: all 0.3s ease;
    }
    
    .pagination a:hover {
        background: #007bff;
        color: white;
    }
    
    .pagination .current {
        background: #007bff;
        color: white;
        border-color: #007bff;
    }
    
    .pagination .disabled {
        color: #6c757d;
        background: #f8f9fa;
        cursor: not-allowed;
    }
    
    .pagination .ellipsis {
        border: none;
        background: none;
        color: #6c757d;
    }
    
    /* Mobile responsive */
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
        
        .modal-content {
            width: 90%;
            margin: 10% auto;
        }
        
        .table-header {
            flex-direction: column;
            gap: 10px;
            text-align: center;
        }
        
        .rejection-modal-content {
            margin: 10% auto;
            width: 90%;
        }
        
        .pagination-container {
            flex-direction: column;
            gap: 10px;
        }
        
         .popup-content {
            margin: 1rem;
            width: calc(100% - 2rem);
            min-width: auto;
            padding: 20px;
        }
        
        .popup-icon {
            font-size: 2.5rem;
        }
        
        .popup-btn {
            width: 100%;
            margin: 5px 0;
        }
        
        #cancelDetails {
            padding: 10px;
            font-size: 0.9rem;
        }
    }
    
</style>
</head>
<body>
    <div class="dashboard-container">
        <!-- Sidebar -->
        <nav class="sidebar" id="sidebar">
            <div class="sidebar-header">
                <div class="logo-container">
                    <img src="<%= request.getContextPath() %>/imnsb_logowbg.png" alt="IMNSB Logo" class="sidebar-logo">
                </div>
                <h3>IMNSB Employee</h3><br>
            </div>
            <ul class="sidebar-menu">
                <li><a href="<%= request.getContextPath() %>/Employee/EmployeeDashboard.jsp"><i class="fas fa-user"></i> <span>Profile</span></a></li>
                <li><a href="<%= request.getContextPath() %>/LeaveApplicationController"><i class="fas fa-calendar-plus"></i> <span>Apply Leave</span></a></li>
                <li class="active"><a href="<%= request.getContextPath() %>/leave-history"><i class="fas fa-history"></i> <span>Leave History</span></a></li>
                <li><a href="<%= request.getContextPath() %>/Employee/EmployeeSettings.jsp"><i class="fas fa-cog"></i> <span>Settings</span></a></li>
                <li class="logout"><a href="<%= request.getContextPath() %>/Employee/EmployeeLogin.jsp" id="logoutBtn"><i class="fas fa-sign-out-alt"></i> <span>Logout</span></a></li>
            </ul>
        </nav>

        <!-- Main Content -->
        <main class="main-content" id="mainContent">
            <header class="content-header">
                <div class="header-left">
                    <button id="sidebarToggle" class="sidebar-toggle">
                        <i class="fas fa-bars"></i>
                    </button>
                    <h2>Leave History</h2>
                </div>
                <div class="header-right">
                    <span id="userName"><%= employee.getEmployeeName() %></span>
                    <div class="user-avatar">
                        <% 
                            String headerProfilePicPath = employee.getProfilePicturePath();
                            if (headerProfilePicPath != null && !headerProfilePicPath.isEmpty()) {
                        %>
                            <img src="<%= request.getContextPath() %>/<%= headerProfilePicPath %>" alt="User Avatar" id="headerAvatar">
                        <% } else { %>
                            <img src="<%= request.getContextPath() %>/defaultprofilepicture.jpg" alt="User Avatar" id="headerAvatar">
                        <% } %>
                    </div>
                </div>
            </header>
            
           <div class="content-body">
    		<div class="leave-history-container">

                    <!-- Statistics Cards -->
                    <div class="stats-grid">
                        <div class="stat-card stat-total">
                            <div class="stat-number">${totalApplications}</div>
                            <div class="stat-label">Total Applications</div>
                        </div>
                        <div class="stat-card stat-pending">
                            <div class="stat-number">${pendingCount}</div>
                            <div class="stat-label">Pending</div>
                        </div>
                        <div class="stat-card stat-approved">
                            <div class="stat-number">${approvedCount}</div>
                            <div class="stat-label">Approved</div>
                        </div>
                        <div class="stat-card stat-rejected">
                            <div class="stat-number">${rejectedCount}</div>
                            <div class="stat-label">Rejected</div>
                        </div>
                        <div class="stat-card stat-cancelled">
                            <div class="stat-number">${cancelledCount}</div>
                            <div class="stat-label">Cancelled</div>
                        </div>
                    </div>
                    
                     <!-- Filters Section -->
                    <div class="filters-section">
                        <div class="filters-header">
                            <i class="fas fa-filter"></i>
                            <h3>Filter Leave Applications</h3>
                        </div>
                        <div class="filters-content">
                            <form id="filterForm" method="GET" action="<%= request.getContextPath() %>/leave-history">
                                <!-- Hidden field to maintain page size -->
                                <input type="hidden" name="size" value="${pageSize}">
                                
                                <div class="filter-row">
                                    <div class="filter-group">
                                        <label for="statusFilter">Status</label>
                                        <select id="statusFilter" name="status">
                                            <option value="">All Status</option>
                                            <option value="Pending" ${param.status == 'Pending' ? 'selected' : ''}>Pending</option>
                                            <option value="Approved" ${param.status == 'Approved' ? 'selected' : ''}>Approved</option>
                                            <option value="Rejected" ${param.status == 'Rejected' ? 'selected' : ''}>Rejected</option>
                                            <option value="Cancelled" ${param.status == 'Cancelled' ? 'selected' : ''}>Cancelled</option>
                                        </select>
                                    </div>
                                    
                                    <div class="filter-group">
									    <label for="leaveTypeFilter">Leave Type</label>
									    <select id="leaveTypeFilter" name="leaveType">
									        <option value="">All Leave Types</option>
									        <!-- Options will be populated by JavaScript -->
									    </select>
									</div>
                                    
                                    <div class="filter-group">
                                        <label for="fromDate">From Date</label>
                                        <input type="date" id="fromDate" name="fromDate" value="${param.fromDate}">
                                    </div>
                                    
                                    <div class="filter-group">
                                        <label for="toDate">To Date</label>
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
                    
                    <!-- Applications Table -->
                    <div class="applications-table">
                        <div class="table-header">
                            <h3><i class="fas fa-history"></i> Leave Applications History</h3>
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
                                <c:when test="${not empty allApplications}">
                                    <table>
                                        <thead>
                                            <tr>
                                                <th>Application ID</th>
                                                <th>Leave Type</th>
                                                <th>Start Date</th>
                                                <th>End Date</th>
                                                <th>Duration</th>
                                                <th>Status</th>
                                                <th>Applied On</th>
                                                <th>Actions</th>
                                            </tr>
                                        </thead>
                                        <tbody>
                                            <c:forEach var="application" items="${allApplications}">
                                                <!-- Check if this application is newly updated -->
                                                <c:set var="isNewlyUpdated" value="false" />
                                                <c:forEach var="newAppId" items="${newlyUpdatedApps}">
                                                    <c:if test="${newAppId == application.applicationid}">
                                                        <c:set var="isNewlyUpdated" value="true" />
                                                    </c:if>
                                                </c:forEach>
                                                
                                                <tr class="${isNewlyUpdated ? 'table-row-new-update' : ''}" 
                                                    data-app-id="${application.applicationid}" 
                                                    data-newly-updated="${isNewlyUpdated}">
                                                    <td>
                                                        ${application.applicationid}
                                                        <c:if test="${isNewlyUpdated}">
                                                            <span class="new-update-badge">
                                                                <i class="fas fa-star"></i> NEW
                                                            </span>
                                                        </c:if>
                                                    </td>
                                                    <td>
                                                        <c:choose>
                                                            <c:when test="${not empty leaveTypeNames[application.leavetypeid]}">
                                                                ${leaveTypeNames[application.leavetypeid]}
                                                            </c:when>
                                                            <c:otherwise>
                                                                Unknown Type
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </td>
                                                    <td>
                                                        <fmt:parseDate value="${application.leavestartdate}" pattern="yyyy-MM-dd" var="startDate"/>
                                                        <fmt:formatDate value="${startDate}" pattern="MMM dd, yyyy"/>
                                                    </td>
                                                    <td>
                                                        <fmt:parseDate value="${application.leaveenddate}" pattern="yyyy-MM-dd" var="endDate"/>
                                                        <fmt:formatDate value="${endDate}" pattern="MMM dd, yyyy"/>
                                                    </td>
                                                    <td>${application.leaveduration} day(s)</td>
                                                   <td>
													    <c:choose>
													        <c:when test="${application.leavestatus == 'Pending'}">
													            <span class="status-badge status-pending">Pending</span>
													        </c:when>
													        <c:when test="${application.leavestatus == 'Approved'}">
													            <span class="status-badge status-approved ${isNewlyUpdated ? 'new-update' : ''}">
													                Approved
													                <c:if test="${isNewlyUpdated}">
													                    <i class="fas fa-check-circle" style="margin-left: 4px; animation: subtle-pulse 2s infinite;"></i>
													                </c:if>
													            </span>
													        </c:when>
													        <c:when test="${application.leavestatus == 'Rejected'}">
													            <c:choose>
													                <c:when test="${not empty application.rejectReason}">
													                    <span class="status-badge status-rejected ${isNewlyUpdated ? 'new-update' : ''}" 
													                          onclick="showRejectionReason('${application.applicationid}', '${fn:escapeXml(application.rejectReason)}')"
													                          title="Click to view rejection reason">
													                        Rejected <i class="fas fa-info-circle"></i>
													                        <!-- NO WARNING SIGN - Clean professional look -->
													                    </span>
													                </c:when>
													                <c:otherwise>
													                    <span class="status-badge status-rejected ${isNewlyUpdated ? 'new-update' : ''}">Rejected</span>
													                </c:otherwise>
													            </c:choose>
													        </c:when>
													        <c:when test="${application.leavestatus == 'Cancelled'}">
													            <span class="status-badge status-cancelled">Cancelled</span>
													        </c:when>
													        <c:otherwise>
													            <span class="status-badge">${application.leavestatus}</span>
													        </c:otherwise>
													    </c:choose>
													</td>
                                                    <td>
                                                        <fmt:parseDate value="${application.appliedon}" pattern="yyyy-MM-dd HH:mm:ss" var="appliedDate"/>
                                                        <fmt:formatDate value="${appliedDate}" pattern="MMM dd, yyyy"/>
                                                    </td>
                                                    <td>
														 <div class="action-buttons">
														    <!-- Enhanced View Application PDF Button with immediate highlight removal -->
														    <a href="<%= request.getContextPath() %>/leave-application-pdf?applicationId=${application.applicationid}" 
														       target="_blank" 
														       class="btn-sm btn-view" 
														       title="View Application Details"
														       onclick="markApplicationAsViewed('${application.applicationid}'); return true;">
														        <i class="fas fa-eye"></i> View
														    </a>
                                                            
                                                            <!-- View Attachment Button -->
														    <c:if test="${not empty application.attachment}">
														        <a href="<%= request.getContextPath() %>/file-download?file=${application.attachment}&applicationId=${application.applicationid}" 
														           target="_blank" class="btn-sm btn-attachment" title="View Attachment">
														            <i class="fas fa-paperclip"></i> Attachment
														        </a>
														    </c:if>
                                                            
                                                            <!-- Cancel Button (only for pending applications) -->
														    <c:if test="${application.leavestatus == 'Pending'}">
														        <button class="btn-sm btn-cancel" 
														                onclick="confirmCancel('${application.applicationid}', '${leaveTypeNames[application.leavetypeid]}', ${application.leaveduration})" 
														                title="Cancel Application">
														            <i class="fas fa-times"></i> Cancel
														        </button>
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
                                        <i class="fas fa-calendar-times" style="font-size: 3rem; color: #ccc; margin-bottom: 20px;"></i>
                                        <h3>No Leave Applications Found</h3>
                                        <p>No applications match your current filter criteria.</p>
                                        <button onclick="clearAllFilters()" class="btn btn-secondary">
                                            <i class="fas fa-eraser"></i> Clear All Filters
                                        </button>
                                        <a href="<%= request.getContextPath() %>/LeaveApplicationController" class="btn btn-primary">
                                            <i class="fas fa-plus"></i> Apply for Leave
                                        </a>
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
                                        <a href="?page=1&size=${pageSize}&status=${param.status}&leaveType=${param.leaveType}&fromDate=${param.fromDate}&toDate=${param.toDate}" title="First Page">
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
                                        <a href="?page=${currentPage - 1}&size=${pageSize}&status=${param.status}&leaveType=${param.leaveType}&fromDate=${param.fromDate}&toDate=${param.toDate}" title="Previous Page">
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
                                                <a href="?page=${pageNum}&size=${pageSize}&status=${param.status}&leaveType=${param.leaveType}&fromDate=${param.fromDate}&toDate=${param.toDate}">${pageNum}</a>
                                            </c:otherwise>
                                        </c:choose>
                                    </c:forEach>
                                    
                                    <!-- Show ellipsis if there are more pages -->
                                    <c:if test="${endPage < totalPages}">
                                        <c:if test="${endPage < totalPages - 1}">
                                            <span class="ellipsis">...</span>
                                        </c:if>
                                        <a href="?page=${totalPages}&size=${pageSize}&status=${param.status}&leaveType=${param.leaveType}&fromDate=${param.fromDate}&toDate=${param.toDate}">${totalPages}</a>
                                    </c:if>
                                    
                                    <!-- Next Page -->
                                    <c:if test="${currentPage < totalPages}">
                                        <a href="?page=${currentPage + 1}&size=${pageSize}&status=${param.status}&leaveType=${param.leaveType}&fromDate=${param.fromDate}&toDate=${param.toDate}" title="Next Page">
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
                                        <a href="?page=${totalPages}&size=${pageSize}&status=${param.status}&leaveType=${param.leaveType}&fromDate=${param.fromDate}&toDate=${param.toDate}" title="Last Page">
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

    <!-- Rejection Reason Modal -->
    <div id="rejectionModal" class="rejection-modal">
        <div class="rejection-modal-content">
            <div class="rejection-modal-header">
                <i class="fas fa-exclamation-triangle"></i>
                <h3>Rejection Reason</h3>
            </div>
            <div class="rejection-modal-body">
                <div id="rejectionReasonText" class="rejection-reason-text">
                    <!-- Rejection reason will be inserted here -->
                </div>
            </div>
            <div class="rejection-modal-footer">
                <button onclick="closeRejectionModal()" class="btn btn-secondary">
                    <i class="fas fa-times"></i> Close
                </button>
            </div>
        </div>
    </div>

     <!-- Modern Cancel Confirmation Modal -->
    <div id="cancelModal" class="popup-overlay warning-popup" style="display: none;">
        <div class="popup-content">
            <div class="popup-icon">⚠️</div>
            <h3>Cancel Leave Application</h3>
            <p id="cancelMessage"><strong>Are you sure you want to cancel this leave application?</strong></p>
            <div id="cancelDetails" style="margin: 15px 0; padding: 15px; background: #fff3cd; border-radius: 8px; border-left: 4px solid #ffc107;">
                <!-- Details will be shown here -->
            </div>
         <p style="font-size: 0.9rem; color: #856404; margin-bottom: 20px;">
		    <i class="fas fa-info-circle"></i> <strong>Note:</strong> This action cannot be undone. Leave balances will be restored for balance-tracked leave types.
		</p>
                <button class="popup-btn btn-secondary" onclick="closeModal()">
                    <i class="fas fa-times"></i> No, Keep It
                </button>
                <button class="popup-btn btn-danger" onclick="cancelApplication()">
                    <i class="fas fa-check"></i> Yes, Cancel It
                </button>
            </div>
        </div>
    </div>
    
    <!-- Success Modal -->
    <div id="successModal" class="popup-overlay success-popup" style="display: none;">
        <div class="popup-content">
            <div class="popup-icon">✅</div>
            <h3>Success!</h3>
            <p id="successMessage"></p>
            <button class="popup-btn btn-success" onclick="closeSuccessModal()">Continue</button>
        </div>
    </div>

    <!-- Error Modal -->
    <div id="errorModal" class="popup-overlay error-popup" style="display: none;">
        <div class="popup-content">
            <div class="popup-icon">❌</div>
            <h3>Error</h3>
            <p id="errorMessage"></p>
            <button class="popup-btn" onclick="closeErrorModal()">Close</button>
        </div>
    </div>

    <script src="<%= request.getContextPath() %>/Employee/Employee.js"></script>
<script>



function markApplicationAsViewed(applicationId) {
    console.log('🔄 Marking application as viewed:', applicationId);
    
    // Immediately remove highlighting for instant feedback
    const row = document.querySelector(`tr[data-app-id="${applicationId}"]`);
    if (row && row.classList.contains('table-row-new-update')) {
        console.log('📋 Removing highlight from row:', applicationId);
        removeHighlightingFromRowProfessional(row);
    }
    
    // Send request to server
    fetch('<%= request.getContextPath() %>/mark-applications-viewed', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: 'action=markViewed&applicationId=' + encodeURIComponent(applicationId)
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            console.log('✅ Application marked as viewed on server:', applicationId);
        } else {
            console.error('❌ Failed to mark application as viewed:', data.message);
        }
    })
    .catch(error => {
        console.error('❌ Error marking application as viewed:', error);
    });
}

function removeHighlightingFromRowProfessional(row) {
    if (!row) return;
    
    console.log('🎨 Professionally removing highlighting from row');
    
    // Add fade-out class for smooth transition
    row.classList.add('table-row-highlight-fadeout');
    row.classList.remove('table-row-new-update');
    
    // Remove the NEW badge with smooth animation
    const badge = row.querySelector('.new-update-badge');
    if (badge) {
        badge.style.transition = 'opacity 0.5s ease, transform 0.5s ease';
        badge.style.opacity = '0';
        badge.style.transform = 'scale(0.8)';
        setTimeout(() => {
            if (badge.parentNode) {
                badge.parentNode.removeChild(badge);
            }
        }, 500);
    }
    
    // Remove status glow effects
    const statusBadge = row.querySelector('.status-badge.new-update');
    if (statusBadge) {
        statusBadge.classList.remove('new-update');
        statusBadge.style.animation = 'none';
        statusBadge.style.boxShadow = '';
    }
    
    // Remove any animated icons (NO WARNING SIGNS for rejection)
    const animatedIcons = row.querySelectorAll('i[style*="animation"]');
    animatedIcons.forEach(icon => {
        icon.style.animation = 'none';
        icon.style.removeProperty('animation');
        // For rejection reasons, remove the warning icon completely
        if (icon.classList.contains('fa-exclamation-triangle')) {
            icon.remove();
        }
    });
    
    // Clean up after animation completes
    setTimeout(() => {
        row.classList.remove('table-row-highlight-fadeout');
        row.style.background = '';
        row.style.borderLeft = '';
    }, 1000);
    
    console.log('✅ Professional highlighting removed from row');
}

// Auto-remove highlighting after 10 seconds
function setupAutoHighlightRemoval() {
    const highlightedRows = document.querySelectorAll('.table-row-new-update');
    
    highlightedRows.forEach(row => {
        const applicationId = row.getAttribute('data-app-id');
        
        // Set 10-second timer for each highlighted row
        setTimeout(() => {
            if (row.classList.contains('table-row-new-update')) {
                console.log('⏰ Auto-removing highlight after 10 seconds for:', applicationId);
                removeHighlightingFromRowProfessional(row);
            }
        }, 10000); // 10 seconds
    });
    
    if (highlightedRows.length > 0) {
        console.log(`⏰ Set up auto-removal for ${highlightedRows.length} highlighted rows (10 seconds)`);
    }
}

// Existing functions remain the same
function showRejectionReason(applicationId, reason) {
    document.getElementById('rejectionReasonText').innerHTML = reason;
    document.getElementById('rejectionModal').style.display = 'block';
}

function closeRejectionModal() {
    document.getElementById('rejectionModal').style.display = 'none';
}

let applicationIdToCancel = null;

function confirmCancel(applicationId, leaveTypeName, duration) {
    console.log('🔔 confirmCancel called with:');
    console.log('  Application ID: "' + applicationId + '"');
    console.log('  Leave Type: "' + leaveTypeName + '"');
    console.log('  Duration: ' + duration);
    
    // Store the application ID
    applicationIdToCancel = applicationId;
    console.log('✅ Stored applicationIdToCancel: "' + applicationIdToCancel + '"');
    
    const detailsElement = document.getElementById('cancelDetails');
    detailsElement.innerHTML = 
        '<p style="margin: 5px 0; color: #333;"><strong>Application ID:</strong> ' + applicationId + '</p>' +
        '<p style="margin: 5px 0; color: #333;"><strong>Leave Type:</strong> ' + leaveTypeName + '</p>' +
        '<p style="margin: 5px 0; color: #333;"><strong>Duration:</strong> ' + duration + ' day(s)</p>';
    
    document.getElementById('cancelModal').style.display = 'flex';
}

function closeModal() {
    console.log('🚫 Closing modal, clearing applicationIdToCancel');
    document.getElementById('cancelModal').style.display = 'none';
    applicationIdToCancel = null;
}

function cancelApplication() {
    console.log('🔄 cancelApplication called');
    console.log('  applicationIdToCancel value: "' + applicationIdToCancel + '"');
    
    if (!applicationIdToCancel) {
        console.error('❌ ERROR: applicationIdToCancel is empty!');
        showErrorModal('Cannot cancel: Application ID is missing. Please refresh the page and try again.');
        return;
    }
    
    console.log('✅ Application ID is valid, proceeding with cancellation');
    
    // ✅ CREATE FORM FIRST (while applicationIdToCancel still has value)
    const form = document.createElement('form');
    form.method = 'POST';
    form.action = '<%= request.getContextPath() %>/leave-history';
    
    const actionInput = document.createElement('input');
    actionInput.type = 'hidden';
    actionInput.name = 'action';
    actionInput.value = 'cancel';
    
    const idInput = document.createElement('input');
    idInput.type = 'hidden';
    idInput.name = 'applicationId';
    idInput.value = applicationIdToCancel;  // ✅ Gets value BEFORE closeModal() clears it
    
    console.log('📋 Form inputs created:');
    console.log('  action: "' + actionInput.value + '"');
    console.log('  applicationId: "' + idInput.value + '"');
    
    form.appendChild(actionInput);
    form.appendChild(idInput);
    document.body.appendChild(form);
    
    console.log('📤 Submitting form to cancel application ' + applicationIdToCancel);
    
    // ✅ THEN close modal and submit (order matters less now since form already has the ID)
    closeModal();
    form.submit();
}



// Change page size functionality
function changePageSize(newSize) {
    const currentUrl = new URL(window.location);
    currentUrl.searchParams.set('size', newSize);
    currentUrl.searchParams.set('page', '1');
    window.location.href = currentUrl.toString();
}

// Clear filters functionality
function clearAllFilters() {
    const url = new URL(window.location);
    url.search = '?size=<%= request.getAttribute("pageSize") %>';
    window.location.href = url.toString();
}

document.getElementById('clearFilters').addEventListener('click', clearAllFilters);

//Close modals with Escape key
document.addEventListener('keydown', function(event) {
    if (event.key === 'Escape') {
        closeModal();
        closeRejectionModal();
        closeSuccessModal();
        closeErrorModal();
    }
});


//Close modals when clicking outside
window.onclick = function(event) {
    const cancelModal = document.getElementById('cancelModal');
    const rejectionModal = document.getElementById('rejectionModal');
    const successModal = document.getElementById('successModal');
    const errorModal = document.getElementById('errorModal');
    
    if (event.target == cancelModal) {
        closeModal();
    }
    if (event.target == rejectionModal) {
        closeRejectionModal();
    }
    if (event.target == successModal) {
        closeSuccessModal();
    }
    if (event.target == errorModal) {
        closeErrorModal();
    }
}
document.addEventListener('DOMContentLoaded', function() {
    console.log('✅ Professional Leave History page loaded');
    
    // ✅ CHECK FOR SESSION MESSAGES
    <c:if test="${not empty sessionScope.successMessage}">
        console.log('📋 Success message from session');
        showSuccessModal('${sessionScope.successMessage}');
        <c:remove var="successMessage" scope="session" />
    </c:if>
    
    <c:if test="${not empty sessionScope.errorMessage}">
        console.log('❌ Error message from session');
        showErrorModal('${sessionScope.errorMessage}');
        <c:remove var="errorMessage" scope="session" />
    </c:if>
    
    // Set up auto-removal for highlighted rows (10 seconds)
    setupAutoHighlightRemoval();
    
    // Log current highlighted applications
    const highlightedCount = document.querySelectorAll('.table-row-new-update').length;
    if (highlightedCount > 0) {
        console.log(`📋 ${highlightedCount} applications are highlighted and will auto-remove after 10 seconds`);
    }
});

//Populate and sort leave types filter on page load
document.addEventListener('DOMContentLoaded', function() {
    console.log('✅ Professional Leave History page loaded');
    
    // ... existing DOMContentLoaded code ...
    
    // ✅ POPULATE AND SORT LEAVE TYPE FILTER
    populateLeaveTypeFilter();
});

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
    
    console.log('✅ Leave type filter populated and sorted (' + leaveTypes.length + ' types)');
}

function showSuccessModal(message) {
    document.getElementById('successMessage').textContent = message;
    document.getElementById('successModal').style.display = 'flex';
}

function closeSuccessModal() {
    document.getElementById('successModal').style.display = 'none';
    // Reload page to refresh the leave history
    window.location.reload();
}

function showErrorModal(message) {
    document.getElementById('errorMessage').textContent = message;
    document.getElementById('errorModal').style.display = 'flex';
}

function closeErrorModal() {
    document.getElementById('errorModal').style.display = 'none';
}
</script>
</body>
</html>
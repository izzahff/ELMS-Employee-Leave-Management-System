<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="elms.model.Employee" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%
    Employee employee = (Employee) session.getAttribute("loggedInEmployee");
    if (employee == null) {
        response.sendRedirect(request.getContextPath() + "/Employee/EmployeeLogin.jsp");
        return;
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="icon" type="image/png" href="<%= request.getContextPath() %>/imnsb_logo.png">
    <title>Apply Leave - IMNSB Employee Leave Management System</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/styles.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/dashboard.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <!-- Flatpickr CSS -->
	<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/flatpickr/dist/flatpickr.min.css">
	
	<!-- Flatpickr JS -->
	<script src="https://cdn.jsdelivr.net/npm/flatpickr"></script>
    <style>
        .leave-application-container {
            max-width: 800px;
            margin: 0 auto;
        }
        
        .leave-application-card {
            background: white;
            border-radius: 8px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
            overflow: hidden;
        }
        
        .leave-application-header {
            background: linear-gradient(135deg, var(--primary-color), var(--primary-dark));
            color: white;
            padding: 20px;
            text-align: center;
        }
        
        .leave-application-header h3 {
            margin: 0;
            font-size: 1.5rem;
        }
        
        .leave-application-content {
            padding: 40px 50px;
        }
        
        /* Responsive padding for mobile */
		@media (max-width: 768px) {
		    .leave-application-content {
		        padding: 25px 30px;
		    }
		}
        
        .form-row {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 20px;
            margin-bottom: 20px;
        }
        
        .form-group {
            margin-bottom: 20px;
        }
        
        .form-group label {
            display: block;
            margin-bottom: 8px;
            font-weight: 600;
            color: #333;
        }
        
        .form-group input, 
        .form-group select, 
        .form-group textarea {
            width: 100%;
            padding: 12px;
            border: 1px solid #ddd;
            border-radius: 4px;
            font-size: 14px;
            transition: border-color 0.3s ease;
            box-sizing: border-box;
        }
        
        .form-group input:focus, 
        .form-group select:focus, 
        .form-group textarea:focus {
            outline: none;
            border-color: #007bff;
            box-shadow: 0 0 0 3px rgba(0,123,255,0.1);
        }
        
        .form-group select:disabled {
            background-color: #f8f9fa;
            color: #6c757d;
            cursor: not-allowed;
        }
        
        .form-buttons {
            display: flex;
            gap: 10px;
            justify-content: center;
            margin-top: 30px;
        }
        
        .btn {
            padding: 0.5rem 1rem;
            font-size: 0.875rem;
            font-weight: 500;
            text-decoration: none;
            border-radius: 4px;
            border: none;
            cursor: pointer;
            transition: all 0.3s ease;
            display: inline-flex;
            align-items: center;
            gap: 0.5rem;
            min-width: 100px;
            justify-content: center;
        }
        
        .btn-primary {
            background-color: #007bff;
            color: white;
            border: 1px solid #007bff;
        }
        
        .btn-primary:hover:not(:disabled) {
            background-color: #0056b3;
            border-color: #0056b3;
        }
        
        .btn-primary:disabled {
            background-color: #6c757d;
            border-color: #6c757d;
            cursor: not-allowed;
        }
        
        .btn-secondary {
            background-color: #6c757d;
            color: white;
            border: 1px solid #6c757d;
        }
        
        .btn-secondary:hover {
            background-color: #545b62;
            border-color: #545b62;
        }
        
        .btn-info {
            background-color: #17a2b8;
            color: white;
            border: 1px solid #17a2b8;
        }
        
        .btn-info:hover {
            background-color: #138496;
            border-color: #117a8b;
        }
        
        .file-upload {
            position: relative;
        }
        
        .file-upload input[type="file"] {
            display: none;
        }
        
        .file-upload-label {
            display: inline-flex;
            align-items: center;
            gap: 8px;
            padding: 12px 20px;
            background: #f8f9fa;
            border: 2px dashed #dee2e6;
            border-radius: 4px;
            cursor: pointer;
            transition: all 0.3s ease;
            width: 100%;
            justify-content: center;
            box-sizing: border-box;
        }
        
        .file-upload-label:hover {
            background: #e9ecef;
            border-color: #007bff;
        }
        
        .file-list {
            margin-top: 10px;
        }
        
        .file-item {
            padding: 8px;
            background: #f8f9fa;
            border-radius: 4px;
            margin-bottom: 5px;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        
        .hidden {
            display: none !important;
        }
        
        .form-error {
            color: #dc3545;
            font-size: 0.875rem;
            margin-top: 5px;
            display: none;
        }
        
        .form-group input.error,
        .form-group select.error,
        .form-group textarea.error {
            border-color: #dc3545;
            box-shadow: 0 0 0 0.2rem rgba(220, 53, 69, 0.25);
        }
        
        /* Uniform info box styling */
        .info-box {
            background: #e3f2fd;
            border: 1px solid #90caf9;
            border-left: 4px solid #2196f3;
            padding: 12px;
            border-radius: 4px;
            margin-bottom: 15px;
        }
        
        .info-box p {
            margin: 0;
            color: #1565c0;
            font-size: 0.9rem;
        }
        
        .step-indicator {
            background: #e3f2fd;
            border: 1px solid #90caf9;
            border-left: 4px solid #2196f3;
            padding: 10px;
            border-radius: 4px;
            margin-bottom: 15px;
            font-size: 0.9rem;
            color: #1565c0;
        }
        
        .shift-info {
            background: #e3f2fd;
            border: 1px solid #90caf9;
            border-left: 4px solid #2196f3;
            padding: 8px 12px;
            border-radius: 4px;
            margin-top: 8px;
            font-size: 0.85rem;
            color: #1565c0;
            display: none;
        }
        
        .shift-info.show {
            display: block;
        }
        
        #balanceWarning {
            background: #e3f2fd;
            border: 1px solid #90caf9;
            border-left: 4px solid #2196f3;
            color: #1565c0;
        }
        
        .warning-style {
            background: #fff3cd !important;
            border-color: #ffeaa7 !important;
            border-left-color: #ffc107 !important;
            color: #856404 !important;
        }
        
        .success-style {
            background: #d4edda !important;
            border-color: #c3e6cb !important;
            border-left-color: #28a745 !important;
            color: #155724 !important;
        }
        
        .danger-style {
            background: #f8d7da !important;
            border-color: #f5c6cb !important;
            border-left-color: #dc3545 !important;
            color: #721c24 !important;
        }
        
        /* Leave Types Info Section Styles */
        .leave-types-grid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
            gap: 15px;
        }
        
        .leave-type-card {
            background: #f8f9fa;
            border: 1px solid #dee2e6;
            border-radius: 8px;
            padding: 15px;
            transition: all 0.3s ease;
            position: relative;
        }
        
        .leave-type-card:hover {
            box-shadow: 0 4px 12px rgba(0,0,0,0.1);
            transform: translateY(-2px);
            border-color: #667eea;
        }
        
        .leave-type-card-header {
            display: flex;
            align-items: flex-start;
            justify-content: space-between;
            margin-bottom: 10px;
        }
        
        .leave-type-name {
            font-weight: 600;
            color: #2d3748;
            font-size: 0.95rem;
            margin: 0;
            flex: 1;
        }
        
        .leave-type-badge {
            display: inline-block;
            padding: 3px 8px;
            border-radius: 12px;
            font-size: 0.7rem;
            font-weight: 600;
            margin-left: 8px;
            white-space: nowrap;
        }
        
        .badge-annual {
            background: #fef3c7;
            color: #92400e;
        }
        
        .badge-document {
            background: #fee2e2;
            color: #991b1b;
        }
        
        .badge-duration {
            background: #dbeafe;
            color: #1e40af;
        }
        
        .leave-type-description {
            color: #64748b;
            font-size: 0.85rem;
            margin: 8px 0;
            line-height: 1.4;
        }
        
        .leave-type-details {
            display: flex;
            flex-wrap: wrap;
            gap: 8px;
            margin-top: 10px;
        }
        
        .leave-type-detail-item {
            display: flex;
            align-items: center;
            gap: 5px;
            font-size: 0.8rem;
            color: #475569;
            background: white;
            padding: 4px 10px;
            border-radius: 4px;
            border: 1px solid #e2e8f0;
        }
        
        .leave-type-detail-item i {
            color: #667eea;
            font-size: 0.75rem;
        }
        
        #confirmMessage {
		    margin: 0 0 20px 0;
		    color: #666;
		    line-height: 1.5;
		    text-align: center;
		}
		
		#confirmMessage p {
		    margin: 10px 0;
		}
		
		#confirmMessage strong {
		    color: #333;
		}
        
        /* Table styles for leave types */
		.leave-types-grid {
		    display: block; /* Changed from grid to block for table layout */
		}
		
		@media (max-width: 768px) {
		    .leave-types-grid table {
		        font-size: 0.75rem;
		    }
		    
		    .leave-types-grid th,
		    .leave-types-grid td {
		        padding: 8px 6px !important;
		    }
		    
		    .leave-types-grid th {
		        font-size: 0.75rem;
		    }
		}
        
        #leaveTypesToggle:hover {
            background: linear-gradient(135deg, #5a67d8 0%, #6b46a1 100%);
        }
        
        #toggleIcon {
            transition: transform 0.3s ease;
        }
        
        #toggleIcon.rotated {
            transform: rotate(180deg);
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
        
        #endDate:disabled {
	    background-color: #f8f9fa;
	    color: #6c757d;
	    cursor: not-allowed;
	    border-color: #dee2e6;
	}
	
	/* Make leave type table rows clickable */
		.leave-types-grid table tbody tr {
		    cursor: pointer;
		    transition: all 0.2s ease;
		}
		
		.leave-types-grid table tbody tr:hover {
		    background-color: #f0f7ff !important;
		    transform: scale(1.01);
		    box-shadow: 0 2px 4px rgba(0,0,0,0.05);
		}
		
		.leave-types-grid table tbody tr:active {
		    background-color: #e3f2fd !important;
		    transform: scale(0.99);
		}
		
		/* Add a subtle "click to select" hint */
		.leave-types-grid table tbody tr::after {
		    content: '👆 Click to select';
		    position: absolute;
		    right: 10px;
		    font-size: 0.7rem;
		    color: #667eea;
		    opacity: 0;
		    transition: opacity 0.2s;
		    pointer-events: none;
		}
		
		.leave-types-grid table tbody tr:hover::after {
		    opacity: 1;
		}
	
	
			/* Override Flatpickr's default grey disabled styling */
		.flatpickr-day.flatpickr-disabled {
		    background-color: #ffebee !important;
		    color: #c62828 !important;
		    cursor: not-allowed !important;
		    border-color: #c62828 !important;
		}
		
		.flatpickr-day.flatpickr-disabled:hover {
		    background-color: #ffcdd2 !important;
		    border-color: #c62828 !important;
		}
		
		/* Our custom unavailable date styling (same red) */
		.flatpickr-day.unavailable-date {
		    background-color: #ffebee !important;
		    color: #c62828 !important;
		    cursor: not-allowed !important;
		}
		
		.flatpickr-day.unavailable-date:hover {
		    background-color: #ffcdd2 !important;
		    border-color: #c62828 !important;
		}
		
		/* Make Flatpickr match your theme */
		.flatpickr-calendar {
		    box-shadow: 0 4px 12px rgba(0,0,0,0.15);
		    border-radius: 8px;
		}
		
		.flatpickr-day.selected {
		    background: #007bff !important;
		    border-color: #007bff !important;
		}
		
		.flatpickr-day.today {
		    border-color: #007bff;
		}
		
		/* Custom tooltip styling */
		.date-tooltip {
		    position: fixed;
		    background-color: #333;
		    color: white;
		    padding: 10px 15px;
		    border-radius: 6px;
		    font-size: 13px;
		    z-index: 99999;
		    pointer-events: none;
		    white-space: nowrap;
		    box-shadow: 0 4px 12px rgba(0,0,0,0.3);
		    max-width: 300px;
		}
		
		.date-tooltip::before {
		    content: '';
		    position: absolute;
		    top: 100%;
		    left: 50%;
		    transform: translateX(-50%);
		    border: 6px solid transparent;
		    border-top-color: #333;
		}
		
		/* Public holiday styling in calendar */
		.flatpickr-day.unavailable-date {
		    background-color: #ffebee !important;
		    color: #c62828 !important;
		    cursor: not-allowed !important;
		}
		
		.flatpickr-day.unavailable-date:hover {
		    background-color: #ffcdd2 !important;
		    border-color: #c62828 !important;
		}
		
		/* Specific styling for public holidays */
		.flatpickr-day[title*="Public Holiday"] {
		    background-color: #fff0f0 !important;
		    font-weight: bold !important;
		}
        
        @media (max-width: 768px) {
            .form-row {
                grid-template-columns: 1fr;
            }
            
            .form-buttons {
                flex-direction: column;
            }
            
            .btn {
                width: 100%;
            }
            
            .popup-content {
                margin: 1rem;
                width: calc(100% - 2rem);
                min-width: auto;
            }
        }
        
        /* Weekend styling in calendar */
		.flatpickr-day.weekend-date {
		    background-color: #f5f5f5 !important;
		    color: #bbb !important;
		    cursor: not-allowed !important;
		    border: none !important;
		}
		
		.flatpickr-day.weekend-date:hover {
		    background-color: #e9ecef !important;
		    color: #bbb !important;
		    border: none !important;
		}
		
		/* Submit button tooltip */
		#submitButtonTooltip {
		    position: fixed;
		    background-color: #333;
		    color: white;
		    padding: 12px 16px;
		    border-radius: 6px;
		    font-size: 13px;
		    z-index: 99999;
		    pointer-events: none;
		    white-space: pre-line;
		    box-shadow: 0 4px 12px rgba(0,0,0,0.3);
		    max-width: 350px;
		    display: none;
		    line-height: 1.5;
		}
		
		#submitButtonTooltip::before {
		    content: '';
		    position: absolute;
		    bottom: 100%;
		    left: 50%;
		    transform: translateX(-50%);
		    border: 6px solid transparent;
		    border-bottom-color: #333;
		}
		
		.btn-primary:disabled {
		    opacity: 0.65;
		}
		
		@media (max-width: 768px) {
		
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
                <li class="active"><a href="<%= request.getContextPath() %>/LeaveApplicationController"><i class="fas fa-calendar-plus"></i> <span>Apply Leave</span></a></li>
                <li><a href="<%= request.getContextPath() %>/leave-history"><i class="fas fa-history"></i> <span>Leave History</span></a></li>
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
                    <h2>Apply Leave</h2>
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
                <div class="leave-application-container">
                    <div class="leave-application-card">
                        <div class="leave-application-header">
                            <h3><i class="fas fa-calendar-plus"></i> Leave Application Form</h3>
                        </div>
                        <div class="leave-application-content">
                            <!-- Current Leave Balance Display -->
	                            <%
							    // Get all leave types that track balance
							    java.util.Map<String, String> balanceLeaveTypes = elms.DAO.LeaveBalanceDAO.getBalanceTrackingLeaveTypes();
							    java.util.Map<String, elms.DAO.LeaveBalanceDAO.BalanceInfo> allBalances = null;
							    
							    if (!balanceLeaveTypes.isEmpty()) {
							        allBalances = elms.DAO.LeaveBalanceDAO.getAllBalances(employee.getEmployeeId());
							    }
							%>
							
							<!-- Show ALL balance-tracking leave types -->
							<% if (allBalances != null && !allBalances.isEmpty()) { %>
							    <div class="info-box" id="leaveBalancesDisplay" style="display: block;">
							        <p><strong><i class="fas fa-calendar-check"></i> Your Leave Balances:</strong></p>
							        <% for (java.util.Map.Entry<String, elms.DAO.LeaveBalanceDAO.BalanceInfo> entry : allBalances.entrySet()) { %>
							            <p style="margin-left: 20px; margin-top: 5px;">
							                <strong><%= entry.getKey() %>:</strong> 
							                <%= String.format("%.1f", entry.getValue().getAvailableDays()) %> days available
							            </p>
							        <% } %>
							    </div>
							<% } %>
													    <p style="margin-top: 8px; font-size: 0.85rem; color: #1565c0;">
							        <i class="fas fa-info-circle"></i> Your balance will only be deducted when a manager approves your request.
							    </p>
							
                            
                           <!-- Leave Types Information Section -->
							<div id="leaveTypesInfoSection" style="margin-bottom: 20px;">
							    <div style="background: white; border: 1px solid #dee2e6; border-radius: 8px; overflow: hidden;">
							        <div style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 15px; cursor: pointer;" id="leaveTypesToggle">
							            <h4 style="margin: 0; display: flex; align-items: center; justify-content: space-between; font-size: 1.1rem;">
							                <span><i class="fas fa-info-circle"></i> Available Leave Types & Information</span>
							                <i class="fas fa-chevron-down" id="toggleIcon"></i>
							            </h4>
							        </div>
							        <div id="leaveTypesContent" style="padding: 20px; display: none;">
							            
							            <!-- Full Day Leave Types Section -->
							            <div id="fullDayLeaveTypesInfo" style="margin-bottom: 20px;">
							                <div style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 12px; border-radius: 8px; cursor: pointer; display: flex; align-items: center; justify-content: space-between;" id="fullDayToggle">
							                    <h5 style="margin: 0; font-size: 1rem;">
							                        <i class="fas fa-sun"></i> Full Day Leave Types
							                        <span id="fullDayCount" style="background: rgba(255,255,255,0.3); padding: 2px 8px; border-radius: 12px; font-size: 0.8rem; margin-left: 8px;">0</span>
							                    </h5>
							                    <i class="fas fa-chevron-down" id="fullDayIcon"></i>
							                </div>
							                <div id="fullDayTypesList" class="leave-types-grid" style="margin-top: 15px; display: none;">
							                    <p style="color: #94a3b8; font-style: italic;">Loading...</p>
							                </div>
							            </div>
							            
							            <!-- Half Day Leave Types Section -->
							            <div id="halfDayLeaveTypesInfo" style="margin-bottom: 20px;">
							                <div style="background: linear-gradient(135deg, #764ba2 0%, #667eea 100%); color: white; padding: 12px; border-radius: 8px; cursor: pointer; display: flex; align-items: center; justify-content: space-between;" id="halfDayToggle">
							                    <h5 style="margin: 0; font-size: 1rem;">
							                        <i class="fas fa-clock"></i> Half Day Leave Types
							                        <span id="halfDayCount" style="background: rgba(255,255,255,0.3); padding: 2px 8px; border-radius: 12px; font-size: 0.8rem; margin-left: 8px;">0</span>
							                    </h5>
							                    <i class="fas fa-chevron-down" id="halfDayIcon"></i>
							                </div>
							                <div id="halfDayTypesList" class="leave-types-grid" style="margin-top: 15px; display: none;">
							                    <p style="color: #94a3b8; font-style: italic;">Loading...</p>
							                </div>
							            </div>
							            
							            <!-- Both (Flexible) Leave Types Section -->
							            <div id="bothLeaveTypesInfo">
							                <div style="background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); color: white; padding: 12px; border-radius: 8px; cursor: pointer; display: flex; align-items: center; justify-content: space-between;" id="bothToggle">
							                    <h5 style="margin: 0; font-size: 1rem;">
							                        <i class="fas fa-exchange-alt"></i> Flexible Leave Types (Full Day or Half Day)
							                        <span id="bothCount" style="background: rgba(255,255,255,0.3); padding: 2px 8px; border-radius: 12px; font-size: 0.8rem; margin-left: 8px;">0</span>
							                    </h5>
							                    <i class="fas fa-chevron-down" id="bothIcon"></i>
							                </div>
							                <div id="bothTypesList" class="leave-types-grid" style="margin-top: 15px; display: none;">
							                    <p style="color: #94a3b8; font-style: italic;">Loading...</p>
							                </div>
							            </div>
							            
							        </div>
							    </div>
							</div>
							                            
                           
                            
                            
                            <form id="leaveApplicationForm" action="<%= request.getContextPath() %>/LeaveApplicationController" method="post" enctype="multipart/form-data">
							    <input type="hidden" name="employeeid" value="<%= employee.getEmployeeId() %>">
							    
							    <!-- Step 1: Leave Type Selection (MOVED TO FIRST) -->
							    <div class="form-group">
							        <label for="leaveTypeId">
							            <i class="fas fa-list"></i>Leave Type <span style="color: red;">*</span>
							        </label>
							        <select id="leaveTypeId" name="leavetypeid" required>
							            <option value="">Select Leave Type</option>
							        </select>
							        <div class="form-error" id="leaveTypeIdError"></div>
							    </div>
							
							    <!-- Step 2: Duration Type Selection (MOVED TO SECOND, CONDITIONALLY SHOWN) -->
							    <div class="form-group hidden" id="durationTypeGroup">
							        <label for="durationType">
							            <i class="fas fa-clock"></i> Duration Type <span style="color: red;">*</span>
							        </label>
							        <select id="durationType" name="leavetype" required disabled>
							            <option value="">Please select leave type first</option>
							        </select>
							        <div class="form-error" id="durationTypeError"></div>
							    </div>
							
							    <!-- Shift Selection (for Half Day only) -->
							    <div class="form-group hidden" id="shiftGroup">
							        <label for="shift">
							            <i class="fas fa-sun"></i> Shift <span style="color: red;">*</span>
							        </label>
							        <select id="shift" name="shift">
							            <option value="">Select Shift</option>
							        </select>
							        <div class="shift-info" id="shiftInfo">
							            <i class="fas fa-info-circle"></i> <span id="shiftInfoText"></span>
							        </div>
							        <div class="form-error" id="shiftError"></div>
							    </div>
							    
							    <!-- Step 3: Date Selection -->
							    <div class="form-row">
							        <div class="form-group">
							            <label for="startDate">
							                <i class="fas fa-calendar-alt"></i> Start Date <span style="color: red;">*</span>
							            </label>
							            <input type="date" id="startDate" name="leavestartdate" required value="${param.leavestartdate}">
							            <div class="form-error" id="startDateError"></div>
							        </div>
							        <div class="form-group" id="endDateGroup">
							            <label for="endDate">
							                <i class="fas fa-calendar-alt"></i> End Date <span style="color: red;">*</span>
							            </label>
							            <input type="date" id="endDate" name="leaveenddate" required value="${param.leaveenddate}">
							            <div class="form-error" id="endDateError"></div>
							        </div>
							    </div>
							    
							    <!-- Duration Display -->
							    <div class="form-group">
							        <label><i class="fas fa-calculator"></i> Calculated Leave Duration</label>
							        <input type="text" id="calculatedDuration" readonly placeholder="Select dates to calculate duration">
							        <input type="hidden" id="leaveDuration" name="leaveduration" value="0">
							    </div>
							    
							    <!-- Leave Reason (REQUIRED) -->
							    <div class="form-group">
							        <label for="leaveDescription">
							            <i class="fas fa-align-left"></i> Leave Reason <span style="color: red;">*</span>
							        </label>
							        <textarea id="leaveDescription" name="leavereason" rows="4" required
							                  placeholder="Provide details about your leave request (e.g., reason, purpose, emergency details)">${param.leavereason}</textarea>
							        
							        <div id="leaveReasonCharCount" style="margin-top: 5px; font-size: 0.85rem; color: #6c757d;">
								        <i class="fas fa-info-circle"></i> Minimum 10 characters required (0/10)
								    </div>
							        
							        <div class="form-error" id="leaveDescriptionError"></div>
							    </div>
							    
							    <!-- File Attachments -->
							    <div class="form-group" id="attachmentGroup">
							        <label for="attachments">
							            <i class="fas fa-paperclip"></i> Attachments <span id="attachmentRequiredIndicator" style="color: red; display: none;">*</span>
							        </label>
							        <div class="file-upload">
							            <input type="file" id="attachments" name="attachments" accept=".pdf,.doc,.docx,.jpg,.jpeg,.png">
							            <label for="attachments" class="file-upload-label">
							                <i class="fas fa-cloud-upload-alt"></i> Choose File
							            </label>
							            <div id="fileList" class="file-list"></div>
							        </div>
							        <div class="form-error" id="attachmentError"></div>
							    </div>
							    
							    <!-- Form Buttons -->
							    <div class="form-buttons">
							        <button type="button" id="clearBtn" class="btn btn-info">
							            <i class="fas fa-eraser"></i> Clear Form
							        </button>
							        <button type="button" id="cancelBtn" class="btn btn-secondary">
							            <i class="fas fa-times"></i> Cancel
							        </button>
							      
							        <button type="submit" class="btn btn-primary" id="submitBtn">
							            <i class="fas fa-paper-plane"></i> Submit Application
							        </button>
							    </div>
							</form>
                        </div>
                    </div>
                </div>
            </div>
        </main>
    </div>

    <!-- Success Modal -->
    <div id="successModal" class="popup-overlay success-popup" style="display: none;">
        <div class="popup-content">
            <div class="popup-icon">✅</div>
            <h3>Application Submitted Successfully!</h3>
            <p id="successMessage"></p>
            <button class="popup-btn btn-success" onclick="closeSuccessModal()">Continue</button>
        </div>
    </div>

    <!-- Error Modal -->
    <div id="errorModal" class="popup-overlay error-popup" style="display: none;">
        <div class="popup-content">
            <div class="popup-icon">❌</div>
            <h3>Application Failed</h3>
            <p id="errorMessage"></p>
            <button class="popup-btn" onclick="closeErrorModal()">Try Again</button>
        </div>
    </div>

    <!-- Warning Modal -->
    <div id="warningModal" class="popup-overlay warning-popup" style="display: none;">
        <div class="popup-content">
            <div class="popup-icon">⚠️</div>
            <h3>Warning</h3>
            <p id="warningMessage"></p>
            <button class="popup-btn" onclick="closeWarningModal()">Understood</button>
        </div>
    </div>

    <!-- Confirmation Modal -->
    <div id="confirmModal" class="popup-overlay info-popup" style="display: none;">
        <div class="popup-content">
            <div class="popup-icon">❓</div>
            <h3 id="confirmTitle">Confirm Action</h3>
            <p id="confirmMessage">Are you sure you want to proceed?</p>
            <div>
                <button class="popup-btn btn-secondary" onclick="closeConfirmModal()">Cancel</button>
                <button class="popup-btn" id="confirmBtn" onclick="confirmAction()">Confirm</button>
            </div>
        </div>
    </div>
    
    <!-- Submit Button Tooltip -->
	<div id="submitButtonTooltip"></div>

    <!-- Pre-loaded leave types data -->
    <script type="text/javascript">
        window.fullDayLeaveTypes = ${fullDayTypesJson != null ? fullDayTypesJson : '[]'};
        window.halfDayLeaveTypes = ${halfDayTypesJson != null ? halfDayTypesJson : '[]'};
        window.bothLeaveTypes = ${bothTypesJson != null ? bothTypesJson : '[]'};
        window.unavailableDates = ${unavailableDatesJson != null ? unavailableDatesJson : '[]'};
        window.malaysianPublicHolidays = ${publicHolidaysJson != null ? publicHolidaysJson : '[]'};
        window.contextPath = '<%= request.getContextPath() %>';
        
        console.log('=== LOADED DATA ===');
        console.log('Unavailable Dates:', window.unavailableDates);
        
        // DEBUG: Log the loaded leave types
        console.log('=== LOADED LEAVE TYPES DATA ===');
        console.log('Full Day Leave Types:', window.fullDayLeaveTypes);
        console.log('Half Day Leave Types:', window.halfDayLeaveTypes);
        console.log('Both Leave Types:', window.bothLeaveTypes);
        console.log('Full Day count:', window.fullDayLeaveTypes ? window.fullDayLeaveTypes.length : 0);
        console.log('Half Day count:', window.halfDayLeaveTypes ? window.halfDayLeaveTypes.length : 0);
        console.log('Both count:', window.bothLeaveTypes ? window.bothLeaveTypes.length : 0);
        
        // Check if any leave type has requiresDocument = true
        if (window.fullDayLeaveTypes && window.fullDayLeaveTypes.length > 0) {
            window.fullDayLeaveTypes.forEach(function(lt) {
                console.log('Full Day - ' + lt.leaveTypeName + ': requiresDocument = ' + lt.requiresDocument + ' (type: ' + typeof lt.requiresDocument + ')');
            });
        }
        
        if (window.halfDayLeaveTypes && window.halfDayLeaveTypes.length > 0) {
            window.halfDayLeaveTypes.forEach(function(lt) {
                console.log('Half Day - ' + lt.leaveTypeName + ': requiresDocument = ' + lt.requiresDocument + ' (type: ' + typeof lt.requiresDocument + ')');
            });
        }
        
     // Store balances per leave type ID
        window.leaveBalances = {};
        <% if (allBalances != null && !allBalances.isEmpty()) { %>
            <% for (java.util.Map.Entry<String, String> entry : balanceLeaveTypes.entrySet()) { 
                String leaveTypeId = entry.getKey();
                String leaveTypeName = entry.getValue();
                elms.DAO.LeaveBalanceDAO.BalanceInfo info = allBalances.get(leaveTypeName);
                if (info != null) {
            %>
                window.leaveBalances['<%= leaveTypeId %>'] = {
                    name: '<%= leaveTypeName %>',
                    available: <%= info.getAvailableDays() %>,
                    allocated: <%= info.getStandardAllocation() %>,
                    used: <%= info.getUsedDays() %>
                };
            <% } } %>
        <% } %>

        console.log('Leave Balances:', window.leaveBalances);
        // Check for server messages and show modals
        <c:if test="${not empty successMessage}">
        document.addEventListener('DOMContentLoaded', function() {
            showSuccessModal('${successMessage}');
        });
        </c:if>
        
        <c:if test="${not empty errorMessage}">
        document.addEventListener('DOMContentLoaded', function() {
            showErrorModal('${errorMessage}');
        });
        </c:if>
        
        <c:if test="${not empty warningMessage}">
        document.addEventListener('DOMContentLoaded', function() {
            showWarningModal('${warningMessage}');
        });
        </c:if>
        
        
    </script>

   
		<script src="<%= request.getContextPath() %>/Employee/Employee.js"></script>
		<script>
		
		let startDatePicker = null;
		let endDatePicker = null;
		let disabledDateStrings = new Set();
		
		window.onerror = function(msg, url, lineNo, columnNo, error) {
		    if (msg.includes('getTime')) {
		        console.error('🔴 ERROR CAUGHT AT:');
		        console.error('Line:', lineNo);
		        console.error('Column:', columnNo);
		        console.error('Stack:', error?.stack);
		        return false; // Let browser show the error too
		    }
		};
		//========================================
		//MODAL FUNCTIONS
		//========================================
		
		function showSuccessModal(message) {
		 document.getElementById('successMessage').textContent = message;
		 document.getElementById('successModal').style.display = 'flex';
		}
		
		function closeSuccessModal() {
		    document.getElementById('successModal').style.display = 'none';
		    
		    // Redirect to leave history page after a short delay
		    setTimeout(function() {
		        window.location.href = window.contextPath + '/leave-history';
		    }, 500);
		}
		
		function showErrorModal(message) {
		 document.getElementById('errorMessage').textContent = message;
		 document.getElementById('errorModal').style.display = 'flex';
		}
		
		function closeErrorModal() {
		 document.getElementById('errorModal').style.display = 'none';
		}
		
		function showWarningModal(message) {
		 document.getElementById('warningMessage').textContent = message;
		 document.getElementById('warningModal').style.display = 'flex';
		}
		
		function closeWarningModal() {
		 document.getElementById('warningModal').style.display = 'none';
		}
		
		function showConfirmModal(title, message, callback) {
		    document.getElementById('confirmTitle').textContent = title;
		    document.getElementById('confirmMessage').innerHTML = message; 
		    document.getElementById('confirmModal').style.display = 'flex';
		    window.confirmCallback = callback;
		}
		
		function closeConfirmModal() {
		 document.getElementById('confirmModal').style.display = 'none';
		 window.confirmCallback = null;
		}
		
		function confirmAction() {
		 if (window.confirmCallback) {
		     window.confirmCallback();
		 }
		 closeConfirmModal();
		}
		
		//Close modals when clicking outside
		document.addEventListener('click', function(event) {
		 const modals = ['successModal', 'errorModal', 'warningModal', 'confirmModal'];
		 modals.forEach(modalId => {
		     const modal = document.getElementById(modalId);
		     if (event.target === modal) {
		         modal.style.display = 'none';
		     }
		 });
		});
		
		//Close modals with Escape key
		document.addEventListener('keydown', function(event) {
		 if (event.key === 'Escape') {
		     const modals = ['successModal', 'errorModal', 'warningModal', 'confirmModal'];
		     modals.forEach(modalId => {
		         document.getElementById(modalId).style.display = 'none';
		     });
		 }
		});
		
		//========================================
		//POPULATE LEAVE TYPES INFO
		//========================================
		
		function populateLeaveTypesInfoImmediate() {
		    console.log('=== POPULATING LEAVE TYPES INFO ===');
		    
		    const fullDayList = document.getElementById('fullDayTypesList');
		    const halfDayList = document.getElementById('halfDayTypesList');
		    const bothList = document.getElementById('bothTypesList');
		    const infoSection = document.getElementById('leaveTypesInfoSection');
		    
		    if (!fullDayList || !halfDayList || !bothList || !infoSection) {
		        console.error('Required elements not found!');
		        return;
		    }
		    
		    fullDayList.innerHTML = '';
		    halfDayList.innerHTML = '';
		    bothList.innerHTML = '';
		    
		    let hasAnyLeaveTypes = false;
		    
		    // Populate Full Day Leave Types
		    if (window.fullDayLeaveTypes && window.fullDayLeaveTypes.length > 0) {
		        console.log('Populating', window.fullDayLeaveTypes.length, 'full day leave types');
		        
		        // Sort alphabetically
		        const sortedFullDay = [...window.fullDayLeaveTypes].sort((a, b) => 
		            a.leaveTypeName.localeCompare(b.leaveTypeName)
		        );
		        
		        const table = createLeaveTypeTable(sortedFullDay, 'Full Day');
		        fullDayList.appendChild(table);
		        document.getElementById('fullDayCount').textContent = sortedFullDay.length;
		        hasAnyLeaveTypes = true;
		    } else {
		        fullDayList.innerHTML = '<p style="color: #64748b; font-style: italic; padding: 10px;">No full day leave types available</p>';
		        document.getElementById('fullDayCount').textContent = '0';
		    }
		    
		    // Populate Half Day Leave Types
		    if (window.halfDayLeaveTypes && window.halfDayLeaveTypes.length > 0) {
		        console.log('Populating', window.halfDayLeaveTypes.length, 'half day leave types');
		        
		        // Sort alphabetically
		        const sortedHalfDay = [...window.halfDayLeaveTypes].sort((a, b) => 
		            a.leaveTypeName.localeCompare(b.leaveTypeName)
		        );
		        
		        const table = createLeaveTypeTable(sortedHalfDay, 'Half Day');
		        halfDayList.appendChild(table);
		        document.getElementById('halfDayCount').textContent = sortedHalfDay.length;
		        hasAnyLeaveTypes = true;
		    } else {
		        halfDayList.innerHTML = '<p style="color: #64748b; font-style: italic; padding: 10px;">No half day leave types available</p>';
		        document.getElementById('halfDayCount').textContent = '0';
		    }
		    
		    // Populate Both (Flexible) Leave Types
		    if (window.bothLeaveTypes && window.bothLeaveTypes.length > 0) {
		        console.log('Populating', window.bothLeaveTypes.length, 'flexible leave types');
		        
		        // Sort alphabetically
		        const sortedBoth = [...window.bothLeaveTypes].sort((a, b) => 
		            a.leaveTypeName.localeCompare(b.leaveTypeName)
		        );
		        
		        const table = createLeaveTypeTable(sortedBoth, 'Both');
		        bothList.appendChild(table);
		        document.getElementById('bothCount').textContent = sortedBoth.length;
		        hasAnyLeaveTypes = true;
		    } else {
		        bothList.innerHTML = '<p style="color: #64748b; font-style: italic; padding: 10px;">No flexible leave types available</p>';
		        document.getElementById('bothCount').textContent = '0';
		    }
		    
		    if (hasAnyLeaveTypes) {
		        infoSection.style.display = 'block';
		        console.log('✅ Leave types info section displayed');
		    }
		}
		
		function createLeaveTypeTable(leaveTypes, durationType) {
		    const tableWrapper = document.createElement('div');
		    tableWrapper.style.overflowX = 'auto';
		    
		    const table = document.createElement('table');
		    table.style.width = '100%';
		    table.style.borderCollapse = 'collapse';
		    table.style.backgroundColor = 'white';
		    table.style.boxShadow = '0 1px 3px rgba(0,0,0,0.1)';
		    table.style.borderRadius = '8px';
		    table.style.overflow = 'hidden';
		    
		    // Create table header
		    const thead = document.createElement('thead');
		    
		    // Set header color based on duration type
		    if (durationType === 'Full Day') {
		        thead.style.backgroundColor = '#667eea';
		    } else if (durationType === 'Half Day') {
		        thead.style.backgroundColor = '#764ba2';
		    } else if (durationType === 'Both') {
		        thead.style.background = 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)';
		    }
		    
		    thead.style.color = 'white';
		    
		    const headerRow = document.createElement('tr');
		    
		    // Set headers based on duration type
		    let headers;
		    if (durationType === 'Full Day') {
		        headers = ['Leave Type', 'Description', 'Max Duration', 'Document', 'Balance Impact'];
		    } else if (durationType === 'Half Day') {
		        headers = ['Leave Type', 'Description', 'Available Shifts', 'Document', 'Balance Impact'];
		    } else if (durationType === 'Both') {
		        headers = ['Leave Type', 'Description', 'Max Duration (Full Day)', 'Available Shifts (Half Day)', 'Document', 'Balance Impact'];
		    }
		    
		    headers.forEach(headerText => {
		        const th = document.createElement('th');
		        th.textContent = headerText;
		        th.style.padding = '12px';
		        th.style.textAlign = 'left';
		        th.style.fontWeight = '600';
		        th.style.fontSize = '0.875rem';
		        th.style.borderBottom = '2px solid rgba(255,255,255,0.2)';
		        headerRow.appendChild(th);
		    });
		    
		    thead.appendChild(headerRow);
		    table.appendChild(thead);
		    
		    // Create table body
		    const tbody = document.createElement('tbody');
		    
		    leaveTypes.forEach((leaveType, index) => {
		        const row = document.createElement('tr');
		        row.style.borderBottom = '1px solid #e2e8f0';
		        row.style.transition = 'background-color 0.2s, transform 0.2s';
		        row.style.cursor = 'pointer'; // Show it's clickable
		        
		        // Make the row clickable
		        row.addEventListener('click', function() {
		            // Select the leave type in the dropdown
		            const leaveTypeSelect = document.getElementById('leaveTypeId');
		            leaveTypeSelect.value = leaveType.leaveTypeId;
		            
		            // Trigger the change event to update the form
		            const event = new Event('change', { bubbles: true });
		            leaveTypeSelect.dispatchEvent(event);
		            
		            // Scroll to the form
		            document.getElementById('leaveApplicationForm').scrollIntoView({ 
		                behavior: 'smooth', 
		                block: 'start' 
		            });
		            
		            // Close the leave types info section
		            const leaveTypesContent = document.getElementById('leaveTypesContent');
		            const toggleIcon = document.getElementById('toggleIcon');
		            if (leaveTypesContent) {
		                leaveTypesContent.style.display = 'none';
		                if (toggleIcon) toggleIcon.classList.remove('rotated');
		            }
		            
		            // Show a subtle feedback
		            this.style.backgroundColor = '#e3f2fd';
		            setTimeout(() => {
		                this.style.backgroundColor = '';
		            }, 300);
		        });
		        
		        // Hover effect
		        row.addEventListener('mouseenter', function() {
		            this.style.backgroundColor = '#f0f7ff';
		            this.style.transform = 'scale(1.01)';
		        });
		        row.addEventListener('mouseleave', function() {
		            this.style.backgroundColor = index % 2 === 0 ? 'white' : '#fafafa';
		            this.style.transform = 'scale(1)';
		        });
		        
		        if (index % 2 === 0) {
		            row.style.backgroundColor = 'white';
		        } else {
		            row.style.backgroundColor = '#fafafa';
		        }
		        
		        // Leave Type Name Column
		        const nameCell = document.createElement('td');
		        nameCell.style.padding = '12px';
		        nameCell.style.fontWeight = '600';
		        nameCell.style.color = '#2d3748';
		        nameCell.style.fontSize = '0.875rem';
		        
		        const nameText = document.createElement('div');
		        nameText.textContent = leaveType.leaveTypeName;
		        
		        // Add badges
		        const badgesDiv = document.createElement('div');
		        badgesDiv.style.marginTop = '4px';
		        badgesDiv.style.display = 'flex';
		        badgesDiv.style.gap = '4px';
		        badgesDiv.style.flexWrap = 'wrap';
		        
		        const affectsBalance = leaveType.affectsBalance === true || leaveType.affectsBalance === 'true';
		        const requiresDoc = leaveType.requiresDocument === true || leaveType.requiresDocument === 'true';
		        const isFixedDuration = leaveType.fixedDuration === true || leaveType.fixedDuration === 'true';
		        
		        // Only show Fixed Duration badge (Balance Impact is already in its own column)
		        if (isFixedDuration) {
		            const badge = createBadge('Fixed Duration', '#dbeafe', '#1e40af');
		            badgesDiv.appendChild(badge);
		        }
		        
		        nameCell.appendChild(nameText);
		        nameCell.appendChild(badgesDiv);
		        row.appendChild(nameCell);
		        
		        // Description Column
		        const descCell = document.createElement('td');
		        descCell.style.padding = '12px';
		        descCell.style.color = '#64748b';
		        descCell.style.fontSize = '0.8rem';
		        descCell.style.lineHeight = '1.4';
		        descCell.textContent = leaveType.leaveTypeDescription || 'No description available';
		        row.appendChild(descCell);
		        
		        // Duration/Shift Column(s) - depends on type
		        if (durationType === 'Full Day') {
		            // Single column: Max Duration
		            const durationCell = document.createElement('td');
		            durationCell.style.padding = '12px';
		            durationCell.style.fontSize = '0.8rem';
		            durationCell.style.textAlign = 'center';
		            
		            const standardDuration = leaveType.standardDuration || 0;
		            if (standardDuration > 0) {
		                durationCell.innerHTML = '<strong style="color: #2d3748;">' + standardDuration + ' day(s)</strong>';
		            } else {
		                durationCell.innerHTML = '<span style="color: #64748b;">No limit</span>';
		            }
		            row.appendChild(durationCell);
		            
		        } else if (durationType === 'Half Day') {
		            // Single column: Available Shifts
		            const durationCell = document.createElement('td');
		            durationCell.style.padding = '12px';
		            durationCell.style.fontSize = '0.8rem';
		            durationCell.style.textAlign = 'center';
		            
		            const shifts = leaveType.availableShifts || 'Both';
		            const shiftText = shifts === 'Both' ? 'Morning / Afternoon' : shifts;
		            durationCell.innerHTML = '<span style="color: #2d3748;">' + shiftText + '</span>';
		            row.appendChild(durationCell);
		            
		        } else if (durationType === 'Both') {
		            // Two columns: Max Duration (Full Day) AND Available Shifts (Half Day)
		            
		            // Max Duration Column (for full day)
		            const maxDurationCell = document.createElement('td');
		            maxDurationCell.style.padding = '12px';
		            maxDurationCell.style.fontSize = '0.8rem';
		            maxDurationCell.style.textAlign = 'center';
		            
		            const standardDuration = leaveType.standardDuration || 0;
		            if (standardDuration > 0) {
		                maxDurationCell.innerHTML = '<strong style="color: #2d3748;">' + standardDuration + ' day(s)</strong>';
		            } else {
		                maxDurationCell.innerHTML = '<span style="color: #64748b;">No limit</span>';
		            }
		            row.appendChild(maxDurationCell);
		            
		            // Available Shifts Column (for half day)
		            const shiftsCell = document.createElement('td');
		            shiftsCell.style.padding = '12px';
		            shiftsCell.style.fontSize = '0.8rem';
		            shiftsCell.style.textAlign = 'center';
		            
		            const shifts = leaveType.availableShifts || 'Both';
		            const shiftText = shifts === 'Both' ? 'Morning / Afternoon' : shifts;
		            shiftsCell.innerHTML = '<span style="color: #2d3748;">' + shiftText + '</span>';
		            row.appendChild(shiftsCell);
		        }
		        
		        // Document Required Column
		        const docCell = document.createElement('td');
		        docCell.style.padding = '12px';
		        docCell.style.textAlign = 'center';
		        docCell.style.fontSize = '0.875rem';
		        
		        if (requiresDoc) {
		            docCell.innerHTML = '<span style="color: #dc3545; font-weight: 600;">✓ Required</span>';
		        } else {
		            docCell.innerHTML = '<span style="color: #6c757d;">Optional</span>';
		        }
		        
		        row.appendChild(docCell);
		        
		        // Balance Impact Column
		        const balanceCell = document.createElement('td');
		        balanceCell.style.padding = '12px';
		        balanceCell.style.textAlign = 'center';
		        balanceCell.style.fontSize = '0.875rem';
		        
		        if (affectsBalance) {
		            balanceCell.innerHTML = '<span style="color: #dc3545; font-weight: 600;">Deducts</span>';
		        } else {
		            balanceCell.innerHTML = '<span style="color: #28a745; font-weight: 600;">No Impact</span>';
		        }
		        
		        row.appendChild(balanceCell);
		        
		        tbody.appendChild(row);
		    });
		    
		    table.appendChild(tbody);
		    tableWrapper.appendChild(table);
		    
		    return tableWrapper;
		}
		
		function createBadge(text, bgColor, textColor) {
		    const badge = document.createElement('span');
		    badge.textContent = text;
		    badge.style.display = 'inline-block';
		    badge.style.padding = '2px 8px';
		    badge.style.borderRadius = '12px';
		    badge.style.fontSize = '0.7rem';
		    badge.style.fontWeight = '600';
		    badge.style.backgroundColor = bgColor;
		    badge.style.color = textColor;
		    badge.style.whiteSpace = 'nowrap';
		    return badge;
		}
		
		function populateLeaveTypesInfo() {
		 populateLeaveTypesInfoImmediate();
		}
		
		function createLeaveTypeCard(leaveType, durationType) {
		 const card = document.createElement('div');
		 card.className = 'leave-type-card';
		 
		 const affectsBalance = leaveType.affectsBalance === true || leaveType.affectsBalance === 'true';
		 const requiresDoc = leaveType.requiresDocument === true || leaveType.requiresDocument === 'true';
		 const standardDuration = leaveType.standardDuration || 0;
		 
		 let badges = '';
		 if (affectsBalance) {
		     badges += '<span class="leave-type-badge badge-annual">Affects Balance</span>';
		 }
		 if (requiresDoc) {
		     badges += '<span class="leave-type-badge badge-document"><i class="fas fa-paperclip"></i> Doc Required</span>';
		 }
		 
		 let details = '';
		 
		 if (durationType === 'Full Day') {
		     if (standardDuration > 0) {
		         details += '<div class="leave-type-detail-item">';
		         details += '<i class="fas fa-calendar-day"></i>';
		         details += '<span>Max: ' + standardDuration + ' day(s)</span>';
		         details += '</div>';
		     } else {
		         details += '<div class="leave-type-detail-item">';
		         details += '<i class="fas fa-infinity"></i>';
		         details += '<span>No duration limit</span>';
		         details += '</div>';
		     }
		 } else {
		     details += '<div class="leave-type-detail-item">';
		     details += '<i class="fas fa-clock"></i>';
		     details += '<span>0.5 days</span>';
		     details += '</div>';
		     
		     if (leaveType.availableShifts) {
		         let shiftText = leaveType.availableShifts;
		         if (shiftText === 'Both') {
		             shiftText = 'Morning or Afternoon';
		         }
		         details += '<div class="leave-type-detail-item">';
		         details += '<i class="fas fa-sun"></i>';
		         details += '<span>' + shiftText + '</span>';
		         details += '</div>';
		     }
		 }
		 
		 if (affectsBalance) {
		     details += '<div class="leave-type-detail-item">';
		     details += '<i class="fas fa-minus-circle" style="color: #dc3545;"></i>';
		     details += '<span>Deducts from balance</span>';
		     details += '</div>';
		 } else {
		     details += '<div class="leave-type-detail-item">';
		     details += '<i class="fas fa-check-circle" style="color: #28a745;"></i>';
		     details += '<span>No balance deduction</span>';
		     details += '</div>';
		 }
		 
		 let descriptionHtml = '';
		 if (leaveType.leaveTypeDescription) {
		     descriptionHtml = '<p class="leave-type-description">' + leaveType.leaveTypeDescription + '</p>';
		 } else {
		     descriptionHtml = '<p class="leave-type-description" style="font-style: italic; color: #94a3b8;">No description available</p>';
		 }
		 
		 let cardHtml = '<div class="leave-type-card-header">';
		 cardHtml += '<h6 class="leave-type-name">' + leaveType.leaveTypeName + '</h6>';
		 cardHtml += badges;
		 cardHtml += '</div>';
		 cardHtml += descriptionHtml;
		 cardHtml += '<div class="leave-type-details">';
		 cardHtml += details;
		 cardHtml += '</div>';
		 
		 card.innerHTML = cardHtml;
		 
		 return card;
		}




		//========================================
		//MAIN FORM LOGIC
		//========================================
		
		document.addEventListener('DOMContentLoaded', function() {
		 const form = document.getElementById('leaveApplicationForm');
		 const leaveTypeSelect = document.getElementById('leaveTypeId');
		 const durationTypeSelect = document.getElementById('durationType');
		 const durationTypeGroup = document.getElementById('durationTypeGroup');
		 const shiftGroup = document.getElementById('shiftGroup');
		 const shiftSelect = document.getElementById('shift');
		 const shiftInfo = document.getElementById('shiftInfo');
		 const shiftInfoText = document.getElementById('shiftInfoText');
		 const startDateInput = document.getElementById('startDate');
		 const endDateInput = document.getElementById('endDate');
		 const endDateGroup = document.getElementById('endDateGroup');
		 const calculatedDurationInput = document.getElementById('calculatedDuration');
		 const leaveDurationInput = document.getElementById('leaveDuration');
		 const submitBtn = document.getElementById('submitBtn');
		 const attachmentsInput = document.getElementById('attachments');
		 const attachmentRequiredIndicator = document.getElementById('attachmentRequiredIndicator');
		 const leaveDescriptionTextarea = document.getElementById('leaveDescription');
		 
		 
		 startDateInput.removeAttribute('min');
		 endDateInput.removeAttribute('min');
		 
		 initializeDatePickers();
		 
		 function checkFormValidity() {
		     const missingFields = [];
		     
		     // Check leave type
		     if (!leaveTypeSelect.value.trim()) {
		         missingFields.push('Leave Type');
		     }
		     
		     const selectedLeaveTypeOption = leaveTypeSelect.options[leaveTypeSelect.selectedIndex];
		     
		     if (selectedLeaveTypeOption && selectedLeaveTypeOption.value) {
		         const leaveTypeCategory = selectedLeaveTypeOption.dataset.leaveTypeCategory;
		         const currentDurationType = durationTypeSelect.value;
		         const requiresDocument = selectedLeaveTypeOption.dataset.requiresDocument === 'true';
		         
		         // Check duration type for "Both" category
		         if (leaveTypeCategory === 'Both' && !currentDurationType.trim()) {
		             missingFields.push('Duration Type');
		         }
		         
		         // Check shift for Half Day
		         const isHalfDay = leaveTypeCategory === 'Half Day' || currentDurationType === 'Half Day';
		         if (isHalfDay && (!shiftSelect.value || shiftSelect.value.trim() === '')) {
		             missingFields.push('Shift');
		         }
		         
		         // Check document requirement
		         const hasFile = attachmentsInput.files && attachmentsInput.files.length > 0;
		         if (requiresDocument && !hasFile) {
		             missingFields.push('Supporting Document (Required)');
		         
		         
		         const fileUploadLabel = document.querySelector('.file-upload-label');
		         if (fileUploadLabel) {
		             fileUploadLabel.style.borderColor = '#dc3545';
		             fileUploadLabel.style.backgroundColor = '#fff5f5';
		         }
		     } else if (requiresDocument && hasFile) {
		         // Document is required and uploaded - show success state
		         const fileUploadLabel = document.querySelector('.file-upload-label');
		         if (fileUploadLabel) {
		             fileUploadLabel.style.borderColor = '#28a745';
		             fileUploadLabel.style.backgroundColor = '#f0fff4';
		         }
		     }
		     }
		     
		  // Check dates - be more specific about what's missing
		     if (!startDateInput.value) {
		         missingFields.push('Start Date');
		     }
		     
		  // Check end date for Full Day
		     if (selectedLeaveTypeOption && selectedLeaveTypeOption.value) {
		         const leaveTypeCategory = selectedLeaveTypeOption.dataset.leaveTypeCategory;
		         const currentDurationType = durationTypeSelect.value;
		         const isFullDay = leaveTypeCategory === 'Full Day' || currentDurationType === 'Full Day';
		         
		         // Show "End Date" missing if it's a full day leave type and end date is empty
		         if (isFullDay && !endDateInput.value) {
		             missingFields.push('End Date');
		         }
		     }
		     
		     // Check leave reason
		     const leaveReason = leaveDescriptionTextarea.value.trim();
		     if (!leaveReason) {
		         missingFields.push('Leave Reason');
		     } else if (leaveReason.length < 10) {
		         missingFields.push('Leave Reason (minimum 10 characters)');
		     }
		     
		  // Check document requirement
		     if (selectedLeaveTypeOption && selectedLeaveTypeOption.value) {
		         const requiresDocument = selectedLeaveTypeOption.dataset.requiresDocument === 'true';
		         const hasFile = attachmentsInput.files && attachmentsInput.files.length > 0;
		         
		         if (requiresDocument && !hasFile) {
		             missingFields.push('Supporting Document (Required)');
		             
		             const fileUploadLabel = document.querySelector('.file-upload-label');
		             if (fileUploadLabel) {
		                 fileUploadLabel.style.borderColor = '#dc3545';
		                 fileUploadLabel.style.backgroundColor = '#fff5f5';
		             }
		         } else if (requiresDocument && hasFile) {
		             // Document is required and uploaded - show success state
		             const fileUploadLabel = document.querySelector('.file-upload-label');
		             if (fileUploadLabel) {
		                 fileUploadLabel.style.borderColor = '#28a745';
		                 fileUploadLabel.style.backgroundColor = '#f0fff4';
		             }
		         }
		     }
		     
		     // Check for any visible errors
		     const visibleErrors = document.querySelectorAll('.form-error[style*="display: block"]');
		     const hasErrors = visibleErrors.length > 0;
		     
		     // Update submit button state
		     const submitBtn = document.getElementById('submitBtn');
		     const tooltip = document.getElementById('submitButtonTooltip');
		     
		     if (missingFields.length > 0 || hasErrors) {
		         submitBtn.disabled = true;
		         
		         // Build tooltip message - organize by importance
		         let tooltipMessage = '⚠️ Required fields:\n\n';
		         
		         // Prioritize the order
		         const orderedFields = [];
		         const fieldOrder = [
		             'Leave Type',
		             'Duration Type',
		             'Start Date',
		             'End Date',
		             'Shift',
		             'Leave Reason',
		             'Leave Reason (minimum 10 characters)',
		             'Supporting Document (Required)'
		         ];
		         
		         // Add fields in priority order
		         fieldOrder.forEach(priorityField => {
		             if (missingFields.includes(priorityField)) {
		                 orderedFields.push(priorityField);
		             }
		         });
		         
		         // Add any remaining fields not in the priority list
		         missingFields.forEach(field => {
		             if (!orderedFields.includes(field)) {
		                 orderedFields.push(field);
		             }
		         });
		         
		         // Build the message
		         orderedFields.forEach(field => {
		             tooltipMessage += '• ' + field + '\n';
		         });
		         
		         if (hasErrors) {
		             tooltipMessage += '\n⚠️ Fix validation errors above';
		         }
		         
		         tooltip.textContent = tooltipMessage;
		     } else {
		         submitBtn.disabled = false;
		         tooltip.textContent = '';
		     }
		 }
		 
		 function checkAndEnableSubmitButton() {
		     checkFormValidity();
		 }
		 
		 function clearAllErrors() {
		     document.querySelectorAll('.form-error').forEach(function(error) {
		         error.style.display = 'none';
		     });
		     
		     document.querySelectorAll('input, select, textarea').forEach(function(input) {
		         input.classList.remove('error');
		     });
		 }
 
		 // ========================================
		 // LOAD ALL LEAVE TYPES ON PAGE LOAD
		 // ========================================
		 
		 function loadAllLeaveTypes() {
		    console.log('=== Loading All Leave Types ===');
		    
		    leaveTypeSelect.innerHTML = '<option value="">Select Leave Type</option>';
		    
		    // Combine both full day and half day leave types
		    const allLeaveTypes = [
		        ...(window.fullDayLeaveTypes || []),
		        ...(window.halfDayLeaveTypes || []),
		        ...(window.bothLeaveTypes || [])
		        
		    ];
		    
		    // Remove duplicates by leaveTypeId
		    const uniqueLeaveTypes = Array.from(
		        new Map(allLeaveTypes.map(lt => [lt.leaveTypeId, lt])).values()
		    );
		    
		    uniqueLeaveTypes.sort((a, b) => {
		        return a.leaveTypeName.localeCompare(b.leaveTypeName);
		    });
		    
		    console.log('Total unique leave types:', uniqueLeaveTypes.length);
		    
		    if (uniqueLeaveTypes.length > 0) {
		        uniqueLeaveTypes.forEach((leaveType) => {
		            const option = document.createElement('option');
		            option.value = leaveType.leaveTypeId;
		            option.textContent = leaveType.leaveTypeName;
		            option.dataset.leaveTypeCategory = leaveType.leaveTypeCategory || '';
		            option.dataset.standardDuration = leaveType.standardDuration || '';
		            option.dataset.availableShifts = leaveType.availableShifts || 'Both';
		            
		            const requiresDoc = leaveType.requiresDocument === true || leaveType.requiresDocument === 'true';
		            option.dataset.requiresDocument = requiresDoc ? 'true' : 'false';
		            
		            const affectsBalance = leaveType.affectsBalance === true || leaveType.affectsBalance === 'true';
		            option.dataset.affectsBalance = affectsBalance ? 'true' : 'false';
		            
		            // ✅ ADD FIXED DURATION HERE
		            const isFixedDuration = leaveType.fixedDuration === true || leaveType.fixedDuration === 'true';
		            option.dataset.fixedDuration = isFixedDuration ? 'true' : 'false';
		            console.log('Creating option:', leaveType.leaveTypeName, '- fixedDuration:', isFixedDuration);
		            
		            option.title = leaveType.leaveTypeDescription || '';
		            
		            // Add visual indicators
		            let indicators = '';
		            if (affectsBalance) {
		                indicators += ' 💰';
		            }
		            if (requiresDoc) {
		                indicators += ' 📎';
		            }
		            if (isFixedDuration) {
		                indicators += ' 🔒'; // Lock icon for fixed duration
		            }
		            
		            if (indicators) {
		                option.textContent += indicators;
		            }
		            
		            leaveTypeSelect.appendChild(option);
		        });
		    } else {
		        leaveTypeSelect.innerHTML = '<option value="">No leave types available</option>';
		    }
		    
		    console.log('✅ Leave types loaded');
		}
		 
		 // Load leave types on page load
		 loadAllLeaveTypes();
		 
 
		//========================================
		//UNAVAILABLE DATE CHECKING
		//========================================
		
		function isDateUnavailable(dateString) {
		  if (!window.unavailableDates || window.unavailableDates.length === 0) {
		      return false;
		  }
		  
		  const checkDate = new Date(dateString);
		  
		  for (let range of window.unavailableDates) {
		      const startDate = new Date(range.startDate);
		      const endDate = new Date(range.endDate);
		      
		      // Check if checkDate falls within this range (inclusive)
		      if (checkDate >= startDate && checkDate <= endDate) {
		          console.log('❌ Date ' + dateString + ' is unavailable (' + range.status + ')');
		          return true;
		      }
		  }
		  
		  return false;
		}

		function getUnavailableDatesArray() {
		  if (!window.unavailableDates || window.unavailableDates.length === 0) {
		      return [];
		  }
		  
		  const unavailableDatesArray = [];
		  
		  window.unavailableDates.forEach(range => {
		      const startDate = new Date(range.startDate);
		      const endDate = new Date(range.endDate);
		      
		      // Generate all dates in the range
		      let currentDate = new Date(startDate);
		      while (currentDate <= endDate) {
		          unavailableDatesArray.push(currentDate.toISOString().split('T')[0]);
		          currentDate.setDate(currentDate.getDate() + 1);
		      }
		  });
		  
		  return unavailableDatesArray;
		}

		
		//========================================
		//INITIALIZE FLATPICKR WITH DISABLED DATES
		//========================================
		
		
		function initializeDatePickers() {
		    console.log('=== Initializing Date Pickers ===');
		    
		    const startDateInput = document.getElementById('startDate');
		    const endDateInput = document.getElementById('endDate');
		    
		    if (!startDateInput || !endDateInput) {
		        console.error('Date inputs not found!');
		        return;
		    }
		    
		    // Destroy existing pickers
		    if (startDatePicker) startDatePicker.destroy();
		    if (endDatePicker) endDatePicker.destroy();
		    
		    // Clear and rebuild disabled dates
		    disabledDateStrings.clear();
		    const disabledDates = [];
		    
		    // Function to disable weekends
		    const disableWeekends = function(date) {
		        return (date.getDay() === 0 || date.getDay() === 6);
		    };
		    
		    // Add unavailable dates from existing leave applications
		    if (window.unavailableDates && window.unavailableDates.length > 0) {
		        window.unavailableDates.forEach(range => {
		            const start = new Date(range.startDate + 'T00:00:00Z');
		            const end = new Date(range.endDate + 'T00:00:00Z');
		            
		            let current = new Date(start);
		            while (current <= end) {
		                const dateStr = current.toISOString().split('T')[0];
		                disabledDates.push(new Date(current));
		                disabledDateStrings.add(dateStr);
		                console.log('  Disabling (leave):', dateStr);
		                current.setUTCDate(current.getUTCDate() + 1);
		            }
		        });
		    }
		    
		    // Add public holidays to disabled dates
		    if (window.malaysianPublicHolidays && window.malaysianPublicHolidays.length > 0) {
		        window.malaysianPublicHolidays.forEach(holidayStr => {
		            disabledDates.push(new Date(holidayStr + 'T00:00:00'));
		            disabledDateStrings.add(holidayStr);
		            console.log('  Disabling (holiday):', holidayStr);
		        });
		    }
		    
		    console.log('Total dates to disable:', disabledDates.length);
		    console.log('Disabled date strings:', Array.from(disabledDateStrings));
		    
		    // Combine weekend disabling with other disabled dates
		    const combinedDisable = [
		        disableWeekends,
		        ...disabledDates
		    ];
		    
		    // Initialize Start Date Picker
		    startDatePicker = flatpickr(startDateInput, {
		        dateFormat: "Y-m-d",
		        disable: combinedDisable,
		        onReady: function(selectedDates, dateStr, instance) {
		            addTooltipsToCalendar();
		        },
		        onOpen: function(selectedDates, dateStr, instance) {
		            setTimeout(addTooltipsToCalendar, 50);
		        },
		        onMonthChange: function(selectedDates, dateStr, instance) {
		            setTimeout(addTooltipsToCalendar, 50);
		        },
		        onYearChange: function(selectedDates, dateStr, instance) {
		            setTimeout(addTooltipsToCalendar, 50);
		        },
		        onChange: function(selectedDates, dateStr, instance) {
		            console.log('Start date changed to:', dateStr);
		            
		            if (!endDatePicker) return;
		            
		            if (dateStr && dateStr.trim() !== '') {
		                endDatePicker.set('minDate', dateStr);
		                
		                if (endDateInput.value && endDateInput.value < dateStr) {
		                    endDateInput.value = '';
		                }
		                
		                autoSetEndDateForLongDuration();
		                calculateDuration();
		            } else {
		                endDatePicker.set('minDate', null);
		            }
		        },
		        onDayCreate: function(dObj, dStr, fp, dayElem) {
		            const year = dayElem.dateObj.getFullYear();
		            const month = String(dayElem.dateObj.getMonth() + 1).padStart(2, '0');
		            const day = String(dayElem.dateObj.getDate()).padStart(2, '0');
		            const dateStr = `${year}-${month}-${day}`;
		            
		            const isInSet = disabledDateStrings.has(dateStr);
		            const isWeekend = dayElem.dateObj.getDay() === 0 || dayElem.dateObj.getDay() === 6;
		            
		            // ✅ WEEKEND HAS HIGHEST PRIORITY
		            if (isWeekend) {
		                dayElem.classList.add('weekend-date');
		                dayElem.style.backgroundColor = '#f5f5f5';
		                dayElem.style.color = '#bbb';
		                
		                dayElem.addEventListener('mouseenter', function(e) {
		                    showCustomTooltip(e, '📅 Weekend - Not Available');
		                });
		                
		                dayElem.addEventListener('mousemove', function(e) {
		                    const tooltip = document.getElementById('customDateTooltip');
		                    if (tooltip && tooltip.style.display === 'block') {
		                        tooltip.style.left = (e.clientX + 10) + 'px';
		                        tooltip.style.top = (e.clientY - 40) + 'px';
		                    }
		                });
		                
		                dayElem.addEventListener('mouseleave', hideCustomTooltip);
		            }
		            else if (isInSet) {
		                dayElem.classList.add('unavailable-date');
		                
		                if (isPublicHoliday(dateStr)) {
		                    dayElem.classList.add('public-holiday-date');
		                    dayElem.style.backgroundColor = '#fff0f0';
		                    dayElem.style.fontWeight = 'bold';
		                    
		                    dayElem.addEventListener('mouseenter', function(e) {
		                        showCustomTooltip(e, '🏖️ Public Holiday - Not Available for Leave');
		                    });
		                    
		                    dayElem.addEventListener('mousemove', function(e) {
		                        const tooltip = document.getElementById('customDateTooltip');
		                        if (tooltip && tooltip.style.display === 'block') {
		                            tooltip.style.left = (e.clientX + 10) + 'px';
		                            tooltip.style.top = (e.clientY - 40) + 'px';
		                        }
		                    });
		                    
		                    dayElem.addEventListener('mouseleave', hideCustomTooltip);
		                } else {
		                    dayElem.addEventListener('mouseenter', function(e) {
		                        showCustomTooltip(e, '📅 Unavailable - Existing Leave Application');
		                    });
		                    
		                    dayElem.addEventListener('mousemove', function(e) {
		                        const tooltip = document.getElementById('customDateTooltip');
		                        if (tooltip && tooltip.style.display === 'block') {
		                            tooltip.style.left = (e.clientX + 10) + 'px';
		                            tooltip.style.top = (e.clientY - 40) + 'px';
		                        }
		                    });
		                    
		                    dayElem.addEventListener('mouseleave', hideCustomTooltip);
		                }
		            } else {
		                dayElem.classList.remove('unavailable-date');
		                
		                const today = new Date();
		                today.setHours(0, 0, 0, 0);
		                const dayDate = new Date(dayElem.dateObj);
		                dayDate.setHours(0, 0, 0, 0);
		                
		                if (dayDate < today) {
		                    dayElem.style.color = '#94a3b8';
		                    
		                    dayElem.addEventListener('mouseenter', function(e) {
		                        showCustomTooltip(e, '⏮️ Past Date - Backdated Application');
		                    });
		                    
		                    dayElem.addEventListener('mousemove', function(e) {
		                        const tooltip = document.getElementById('customDateTooltip');
		                        if (tooltip && tooltip.style.display === 'block') {
		                            tooltip.style.left = (e.clientX + 10) + 'px';
		                            tooltip.style.top = (e.clientY - 40) + 'px';
		                        }
		                    });
		                    
		                    dayElem.addEventListener('mouseleave', hideCustomTooltip);
		                }
		            }
		        }
		    });
		
		    // Initialize End Date Picker
		    endDatePicker = flatpickr(endDateInput, {
		        dateFormat: "Y-m-d",
		        disable: combinedDisable,
		        onReady: function(selectedDates, dateStr, instance) {
		            addTooltipsToCalendar();
		        },
		        onOpen: function(selectedDates, dateStr, instance) {
		            setTimeout(addTooltipsToCalendar, 50);
		        },
		        onMonthChange: function(selectedDates, dateStr, instance) {
		            setTimeout(addTooltipsToCalendar, 50);
		        },
		        onYearChange: function(selectedDates, dateStr, instance) {
		            setTimeout(addTooltipsToCalendar, 50);
		        },
		        onChange: function(selectedDates, dateStr, instance) {
		            console.log('End date changed to:', dateStr);
		            calculateDuration();
		        },
		        onDayCreate: function(dObj, dStr, fp, dayElem) {
		            const year = dayElem.dateObj.getFullYear();
		            const month = String(dayElem.dateObj.getMonth() + 1).padStart(2, '0');
		            const day = String(dayElem.dateObj.getDate()).padStart(2, '0');
		            const dateStr = `${year}-${month}-${day}`;
		            
		            const isInSet = disabledDateStrings.has(dateStr);
		            const isWeekend = dayElem.dateObj.getDay() === 0 || dayElem.dateObj.getDay() === 6;
		            
		            // ✅ WEEKEND HAS HIGHEST PRIORITY
		            if (isWeekend) {
		                dayElem.classList.add('weekend-date');
		                dayElem.style.backgroundColor = '#f5f5f5';
		                dayElem.style.color = '#bbb';
		                
		                dayElem.addEventListener('mouseenter', function(e) {
		                    showCustomTooltip(e, '📅 Weekend - Not Available');
		                });
		                
		                dayElem.addEventListener('mousemove', function(e) {
		                    const tooltip = document.getElementById('customDateTooltip');
		                    if (tooltip && tooltip.style.display === 'block') {
		                        tooltip.style.left = (e.clientX + 10) + 'px';
		                        tooltip.style.top = (e.clientY - 40) + 'px';
		                    }
		                });
		                
		                dayElem.addEventListener('mouseleave', hideCustomTooltip);
		            }
		            else if (isInSet) {
		                dayElem.classList.add('unavailable-date');
		                
		                if (isPublicHoliday(dateStr)) {
		                    dayElem.classList.add('public-holiday-date');
		                    dayElem.style.backgroundColor = '#fff0f0';
		                    dayElem.style.fontWeight = 'bold';
		                    
		                    dayElem.addEventListener('mouseenter', function(e) {
		                        showCustomTooltip(e, '🏖️ Public Holiday - Not Available for Leave');
		                    });
		                    
		                    dayElem.addEventListener('mousemove', function(e) {
		                        const tooltip = document.getElementById('customDateTooltip');
		                        if (tooltip && tooltip.style.display === 'block') {
		                            tooltip.style.left = (e.clientX + 10) + 'px';
		                            tooltip.style.top = (e.clientY - 40) + 'px';
		                        }
		                    });
		                    
		                    dayElem.addEventListener('mouseleave', hideCustomTooltip);
		                } else {
		                    dayElem.addEventListener('mouseenter', function(e) {
		                        showCustomTooltip(e, '📅 Unavailable - Existing Leave Application');
		                    });
		                    
		                    dayElem.addEventListener('mousemove', function(e) {
		                        const tooltip = document.getElementById('customDateTooltip');
		                        if (tooltip && tooltip.style.display === 'block') {
		                            tooltip.style.left = (e.clientX + 10) + 'px';
		                            tooltip.style.top = (e.clientY - 40) + 'px';
		                        }
		                    });
		                    
		                    dayElem.addEventListener('mouseleave', hideCustomTooltip);
		                }
		            } else {
		                dayElem.classList.remove('unavailable-date');
		                
		                const today = new Date();
		                today.setHours(0, 0, 0, 0);
		                const dayDate = new Date(dayElem.dateObj);
		                dayDate.setHours(0, 0, 0, 0);
		                
		                if (dayDate < today) {
		                    dayElem.style.color = '#94a3b8';
		                    
		                    dayElem.addEventListener('mouseenter', function(e) {
		                        showCustomTooltip(e, '⏮️ Past Date - Backdated Application');
		                    });
		                    
		                    dayElem.addEventListener('mousemove', function(e) {
		                        const tooltip = document.getElementById('customDateTooltip');
		                        if (tooltip && tooltip.style.display === 'block') {
		                            tooltip.style.left = (e.clientX + 10) + 'px';
		                            tooltip.style.top = (e.clientY - 40) + 'px';
		                        }
		                    });
		                    
		                    dayElem.addEventListener('mouseleave', hideCustomTooltip);
		                }
		            }
		        }
		    });
		    
		    console.log('✅ Date pickers initialized with weekend priority');
		}


		//========================================
		//HELPER FUNCTIONS FOR PUBLIC HOLIDAYS
		//========================================
		
		//Helper function to check if a date is a public holiday
		function isPublicHoliday(dateString) {
		 return window.malaysianPublicHolidays && window.malaysianPublicHolidays.includes(dateString);
		}
		
		//Helper function to check if a date is a working day (not weekend, not holiday)
		function isWorkingDay(date) {
		 const dayOfWeek = date.getDay();
		 const dateString = formatDateToString(date);
		 
		 // Check if it's a weekend
		 if (dayOfWeek === 0 || dayOfWeek === 6) {
		     return false;
		 }
		 
		 // Check if it's a public holiday
		 if (isPublicHoliday(dateString)) {
		     return false;
		 }
		 
		 return true;
		}

		//Helper function to format date to YYYY-MM-DD string
		function formatDateToString(date) {
		 const year = date.getFullYear();
		 const month = String(date.getMonth() + 1).padStart(2, '0');
		 const day = String(date.getDate()).padStart(2, '0');
		 return year + '-' + month + '-' + day;
		}
		
		//========================================
		//ENHANCED TOOLTIP FOR PUBLIC HOLIDAYS
		//========================================
		
		function createCustomTooltip() {
		 let tooltip = document.getElementById('customDateTooltip');
		 if (!tooltip) {
		     tooltip = document.createElement('div');
		     tooltip.id = 'customDateTooltip';
		     tooltip.className = 'date-tooltip';
		     tooltip.style.display = 'none';
		     document.body.appendChild(tooltip);
		 }
		 return tooltip;
		}

		function showCustomTooltip(event, message) {
		 const tooltip = createCustomTooltip();
		 tooltip.textContent = message;
		 tooltip.style.display = 'block';
		 
		 // Position tooltip near the cursor
		 const x = event.clientX;
		 const y = event.clientY;
		 
		 tooltip.style.left = (x + 10) + 'px';
		 tooltip.style.top = (y - 40) + 'px';
		}
		
		function hideCustomTooltip() {
		 const tooltip = document.getElementById('customDateTooltip');
		 if (tooltip) {
		     tooltip.style.display = 'none';
		 }
		}

		//Add event listeners to Flatpickr calendar days after they're created
		
		function addTooltipsToCalendar() {
		    const allDays = document.querySelectorAll('.flatpickr-calendar .flatpickr-day');
		    
		    allDays.forEach(function(dayElem) {
		        const dateObj = dayElem.dateObj;
		        if (!dateObj) return;
		        
		        const dateString = formatDateToString(dateObj);
		        const today = new Date();
		        today.setHours(0, 0, 0, 0);
		        const dayDate = new Date(dateObj);
		        dayDate.setHours(0, 0, 0, 0);
		        
		        // Skip if we already added listeners
		        if (dayElem.dataset.tooltipAdded === 'true') {
		            return;
		        }
		        
		        // Mark as processed
		        dayElem.dataset.tooltipAdded = 'true';
		        
		        // ✅ CHECK WEEKEND FIRST - HIGHEST PRIORITY
		        const isWeekend = dateObj.getDay() === 0 || dateObj.getDay() === 6;
		        
		        if (isWeekend) {
		            // Weekend has highest priority
		            dayElem.addEventListener('mouseenter', function(e) {
		                showCustomTooltip(e, '📅 Weekend - Not Available');
		            });
		            
		            dayElem.addEventListener('mousemove', function(e) {
		                const tooltip = document.getElementById('customDateTooltip');
		                if (tooltip && tooltip.style.display === 'block') {
		                    tooltip.style.left = (e.clientX + 10) + 'px';
		                    tooltip.style.top = (e.clientY - 40) + 'px';
		                }
		            });
		            
		            dayElem.addEventListener('mouseleave', hideCustomTooltip);
		        }
		        // Only check other conditions if NOT a weekend
		        else if (isPublicHoliday(dateString)) {
		            dayElem.addEventListener('mouseenter', function(e) {
		                showCustomTooltip(e, '🏖️ Public Holiday - Not Available for Leave');
		            });
		            
		            dayElem.addEventListener('mousemove', function(e) {
		                const tooltip = document.getElementById('customDateTooltip');
		                if (tooltip && tooltip.style.display === 'block') {
		                    tooltip.style.left = (e.clientX + 10) + 'px';
		                    tooltip.style.top = (e.clientY - 40) + 'px';
		                }
		            });
		            
		            dayElem.addEventListener('mouseleave', hideCustomTooltip);
		        }
		        else if (disabledDateStrings.has(dateString)) {
		            dayElem.addEventListener('mouseenter', function(e) {
		                showCustomTooltip(e, '📅 Unavailable - Existing Leave Application');
		            });
		            
		            dayElem.addEventListener('mousemove', function(e) {
		                const tooltip = document.getElementById('customDateTooltip');
		                if (tooltip && tooltip.style.display === 'block') {
		                    tooltip.style.left = (e.clientX + 10) + 'px';
		                    tooltip.style.top = (e.clientY - 40) + 'px';
		                }
		            });
		            
		            dayElem.addEventListener('mouseleave', hideCustomTooltip);
		        }
		        else if (dayDate < today) {
		            dayElem.addEventListener('mouseenter', function(e) {
		                showCustomTooltip(e, '⏮️ Past Date - Backdated Application');
		            });
		            
		            dayElem.addEventListener('mousemove', function(e) {
		                const tooltip = document.getElementById('customDateTooltip');
		                if (tooltip && tooltip.style.display === 'block') {
		                    tooltip.style.left = (e.clientX + 10) + 'px';
		                    tooltip.style.top = (e.clientY - 40) + 'px';
		                }
		            });
		            
		            dayElem.addEventListener('mouseleave', hideCustomTooltip);
		        }
		    });
		}

		function checkDateRangeOverlap(startDate, endDate) {
		    if (!startDate || !window.unavailableDates || window.unavailableDates.length === 0) {
		        return { hasOverlap: false, overlappingDates: [] };
		    }
		    
		    const start = new Date(startDate + 'T00:00:00Z');
		    const end = new Date((endDate || startDate) + 'T00:00:00Z');
		    
		    const overlappingDates = [];
		    
		    // Check each unavailable date range
		    for (let range of window.unavailableDates) {
		        const unavailStart = new Date(range.startDate + 'T00:00:00Z');
		        const unavailEnd = new Date(range.endDate + 'T00:00:00Z');
		        
		        // Check if ranges overlap
		        // Overlap occurs if: start <= unavailEnd AND end >= unavailStart
		        if (start <= unavailEnd && end >= unavailStart) {
		            overlappingDates.push({
		                startDate: range.startDate,
		                endDate: range.endDate,
		                status: range.status,
		                duration: range.duration
		            });
		        }
		    }
		    
		    return {
		        hasOverlap: overlappingDates.length > 0,
		        overlappingDates: overlappingDates
		    };
		}

		function checkIfPastDates(startDate, endDate) {
		    const today = new Date();
		    today.setHours(0, 0, 0, 0);
		    
		    const start = new Date(startDate);
		    start.setHours(0, 0, 0, 0);
		    
		    const end = endDate ? new Date(endDate) : start;
		    end.setHours(0, 0, 0, 0);
		    
		    return start < today || end < today;
		}

		function reloadUnavailableDates() {
		    console.log('=== Reloading Unavailable Dates ===');
		    
		    // Fetch updated unavailable dates from server
		    fetch(window.contextPath + '/LeaveApplicationController?action=getUnavailableDates')
		        .then(response => response.json())
		        .then(data => {
		            console.log('Updated unavailable dates:', data);
		            
		            // Update the global unavailable dates
		            window.unavailableDates = data;
		            
		            // Re-initialize the date pickers with new data
		            initializeDatePickers();
		            
		            console.log('✅ Date pickers reloaded with updated unavailable dates');
		        })
		        .catch(error => {
		            console.error('Error reloading unavailable dates:', error);
		        });
		}

		function clearError(errorId) {
		 const errorElement = document.getElementById(errorId);
		 if (errorElement) {
		     errorElement.textContent = '';
		     errorElement.style.display = 'none';
		 }
		}



		//========================================
		//AUTO-SET END DATE FOR LONG DURATION (BUT KEEP IT EDITABLE)
		//========================================
		
		 
		function autoSetEndDateForLongDuration() {
		    const selectedOption = leaveTypeSelect.options[leaveTypeSelect.selectedIndex];
		    
		    if (!selectedOption || !selectedOption.value) {
		        // Remove info box if it exists and make end date editable
		        const autoDateInfo = document.getElementById('autoDateInfo');
		        if (autoDateInfo) {
		            autoDateInfo.remove();
		        }
		        endDateInput.readOnly = false;
		        endDateInput.disabled = false;
		        endDateInput.style.backgroundColor = '';
		        endDateInput.style.cursor = '';
		        endDateInput.style.color = '';
		        endDateInput.removeAttribute('data-locked');
		        
		        if (endDatePicker) {
		            endDatePicker.set('clickOpens', true);
		        }
		        
		        return;
		    }
		    
		    const standardDuration = parseFloat(selectedOption.dataset.standardDuration);
		    const leaveTypeCategory = selectedOption.dataset.leaveTypeCategory;
		    const currentDurationType = durationTypeSelect.value;
		    const isFixedDuration = selectedOption.dataset.fixedDuration === 'true';
		    
		    const isFullDay = leaveTypeCategory === 'Full Day' || currentDurationType === 'Full Day';
		    
		    console.log('=== AUTO-SET END DATE CHECK ===');
		    console.log('  Fixed Duration:', isFixedDuration);
		    console.log('  Standard Duration:', standardDuration);
		    console.log('  Is Full Day:', isFullDay);
		    console.log('  Start Date:', startDateInput.value);
		    
		    // ONLY auto-set end date for FIXED DURATION leave types
		    if (isFullDay && isFixedDuration && standardDuration > 0 && startDateInput.value) {
		        
		        console.log('✅ Calculating fixed duration end date...');
		        
		        // Calculate end date by adding business days (excluding weekends AND public holidays)
		        let businessDaysToAdd = standardDuration;
		        let currentDate = new Date(startDateInput.value + 'T00:00:00');
		        
		        console.log('  Starting from:', currentDate.toDateString());
		        console.log('  Need to add:', businessDaysToAdd, 'business days (excluding weekends & holidays)');
		        
		        // Count the first day if it's a working day
		        const firstDayOfWeek = currentDate.getDay();
		        const firstDateString = formatDateToString(currentDate);
		        const isFirstDayWorkingDay = firstDayOfWeek !== 0 && firstDayOfWeek !== 6 && !isPublicHoliday(firstDateString);
		        
		        if (isFirstDayWorkingDay) {
		            businessDaysToAdd--;
		            console.log('  First day is a working day, counting it. Remaining:', businessDaysToAdd);
		        }
		        
		        // Now add the remaining business days
		        while (businessDaysToAdd > 0) {
		            currentDate.setDate(currentDate.getDate() + 1);
		            const dayOfWeek = currentDate.getDay();
		            const dateString = formatDateToString(currentDate);
		            
		            // ✅ Only count if NOT a weekend AND NOT a public holiday
		            if (dayOfWeek !== 0 && dayOfWeek !== 6 && !isPublicHoliday(dateString)) {
		                businessDaysToAdd--;
		                console.log('  Added:', dateString, '- Remaining:', businessDaysToAdd);
		            } else {
		                if (dayOfWeek === 0 || dayOfWeek === 6) {
		                    console.log('  Skipped weekend:', dateString);
		                } else {
		                    console.log('  Skipped public holiday:', dateString);
		                }
		            }
		        }
		        
		        // Format date without timezone issues
		        const year = currentDate.getFullYear();
		        const month = String(currentDate.getMonth() + 1).padStart(2, '0');
		        const day = String(currentDate.getDate()).padStart(2, '0');
		        const endDateValue = year + '-' + month + '-' + day;
		        
		        console.log('  Final End Date:', endDateValue);
		        console.log('  Calculated End Date:', currentDate.toDateString());
		        
		        // Set the end date value BEFORE disabling/locking
		        endDateInput.value = endDateValue;
		        
		        // Update Flatpickr's internal value
		        if (endDatePicker) {
		            endDatePicker.setDate(endDateValue, false); // false = don't trigger onChange
		            endDatePicker.set('clickOpens', false); // Disable calendar
		        }
		        
		        // LOCK the end date for fixed duration
		        endDateInput.disabled = false; // Keep enabled for form submission
		        endDateInput.readOnly = true;
		        endDateInput.style.backgroundColor = '#e9ecef';
		        endDateInput.style.cursor = 'not-allowed';
		        endDateInput.style.color = '#6c757d';
		        endDateInput.setAttribute('data-locked', 'true');
		        
		        console.log('✅ End date LOCKED:', endDateInput.value);
		        
		        // Show locked info message with calculated duration
		        const infoBox = document.createElement('div');
		        infoBox.className = 'info-box';
		        infoBox.style.marginTop = '10px';
		        infoBox.style.background = '#fff3cd';
		        infoBox.style.borderColor = '#ffc107';
		        infoBox.style.borderLeftColor = '#ffc107';
		        infoBox.style.color = '#856404';
		        infoBox.innerHTML = '<i class="fas fa-lock"></i> <strong>Fixed Duration Leave:</strong> ' +
		            'This leave type requires exactly <strong>' + standardDuration + ' working day(s)</strong>. ' +
		            'End date automatically set to <strong>' + endDateValue + '</strong> (excluding weekends & public holidays). ' +
		            '<strong style="color: #dc3545;">End date cannot be modified.</strong>';
		        
		        const existingInfo = document.getElementById('autoDateInfo');
		        if (existingInfo) {
		            existingInfo.remove();
		        }
		        
		        infoBox.id = 'autoDateInfo';
		        endDateGroup.appendChild(infoBox);
		        
		        // IMPORTANT: Force recalculation after a short delay to ensure end date is set
		        setTimeout(function() {
		            console.log('🔄 Forcing duration recalculation...');
		            console.log('  Start Date:', startDateInput.value);
		            console.log('  End Date:', endDateInput.value);
		            calculateDuration();
		            console.log('✅ Duration calculated:', leaveDurationInput.value);
		        }, 100);
		        
		    } else {
		        // NOT a fixed duration leave type - make end date fully editable
		        const autoDateInfo = document.getElementById('autoDateInfo');
		        if (autoDateInfo) {
		            autoDateInfo.remove();
		        }
		        endDateInput.readOnly = false;
		        endDateInput.disabled = false;
		        endDateInput.style.backgroundColor = '';
		        endDateInput.style.cursor = '';
		        endDateInput.style.color = '';
		        endDateInput.removeAttribute('data-locked');
		        
		        // RE-ENABLE FLATPICKR
		        if (endDatePicker) {
		            endDatePicker.set('clickOpens', true);
		        }
		        
		        console.log('✅ End date is EDITABLE (not a fixed duration leave type)');
		    }
		}
		 // ========================================
		 // LEAVE TYPE CHANGE HANDLER
		 // ========================================
		 
		leaveTypeSelect.addEventListener('change', function() {
		    const selectedOption = this.options[this.selectedIndex];
		    
		    // RESET FORM FIELDS when leave type changes
		    durationTypeSelect.value = '';
		    shiftSelect.value = '';
		    calculatedDurationInput.value = '';
		    leaveDurationInput.value = '0';
		    
		    const charCountDisplay = document.getElementById('leaveReasonCharCount');
		    if (charCountDisplay) {
		        charCountDisplay.innerHTML = '<i class="fas fa-info-circle"></i> Minimum 10 characters required (0/10)';
		        charCountDisplay.style.color = '#6c757d';
		    }
		    
		    attachmentsInput.value = '';
		    document.getElementById('fileList').innerHTML = '';
		    
		    // ✅ Clear input values first
		    startDateInput.value = '';
		    endDateInput.value = '';
		    
		    // ✅ Then clear and reset Flatpickr
		     if (startDatePicker) {
		        startDatePicker.clear(false, false); // false = don't trigger onChange
		        startDatePicker.set('minDate', null);
		    }
		    if (endDatePicker) {
		        endDatePicker.clear(false, false); // false = don't trigger onChange
		        endDatePicker.set('minDate', null);
		    }
		    
		    // Reset end date field styling
		    endDateInput.disabled = false;
		    endDateInput.readOnly = false;
		    endDateInput.style.backgroundColor = '';
		    endDateInput.style.cursor = '';
		    endDateInput.style.color = '';
		    endDateInput.removeAttribute('data-locked');
		    
		    leaveDescriptionTextarea.value = '';
		    attachmentsInput.value = '';
		    document.getElementById('fileList').innerHTML = '';
		     
		     // Remove any auto-date info
		     const autoDateInfo = document.getElementById('autoDateInfo');
		     if (autoDateInfo) {
		         autoDateInfo.remove();
		     }
		     
		     // Clear all errors
		     clearAllErrors();
		     
		     
		  // Show/hide balance info based on affectsBalance
		     const balanceInfoBox = document.getElementById('annualLeaveBalanceInfo');
		     if (balanceInfoBox) {
		         const affectsBalance = selectedOption.dataset.affectsBalance === 'true';
		         if (affectsBalance) {
		             balanceInfoBox.classList.remove('hidden');
		         } else {
		             balanceInfoBox.classList.add('hidden');
		         }
		     }
		     
		     if (!this.value) {
		         // Reset everything if no leave type selected
		         durationTypeGroup.classList.add('hidden');
		         durationTypeSelect.disabled = true;
		         durationTypeSelect.value = '';
		         shiftGroup.classList.add('hidden');
		         shiftSelect.value = '';
		         endDateGroup.classList.remove('hidden');
		         endDateInput.required = true;
		         return;
		     }
		     
		     const leaveTypeCategory = selectedOption.dataset.leaveTypeCategory;
		     const requiresDocument = selectedOption.dataset.requiresDocument === 'true';
		     const fileUploadLabel = document.querySelector('.file-upload-label');
		     const leaveTypeGroup = this.parentNode;
		     
		     console.log('Leave type selected:', this.value);
		     console.log('Category:', leaveTypeCategory);
		     console.log('Requires document:', requiresDocument);
		     
		  // Handle document requirement
		     if (requiresDocument) {
		         attachmentsInput.setAttribute('data-required', 'true');
		         attachmentRequiredIndicator.style.display = 'inline';
		         fileUploadLabel.style.borderColor = '#dc3545';
		         fileUploadLabel.style.backgroundColor = '#fff5f5';
		         
		         let docNotice = document.getElementById('documentRequirementNotice');
		         if (!docNotice) {
		             docNotice = document.createElement('div');
		             docNotice.id = 'documentRequirementNotice';
		             docNotice.className = 'danger-style';
		             docNotice.style.marginTop = '10px';
		             docNotice.style.padding = '8px';
		             docNotice.style.borderRadius = '4px';
		             docNotice.style.fontSize = '0.9rem';
		             leaveTypeGroup.appendChild(docNotice);
		         }
		         docNotice.innerHTML = '<i class="fas fa-exclamation-circle"></i> <strong>Document Required:</strong> You must attach a supporting document for this leave type.';
		         docNotice.style.display = 'block';
		     } else {
		         attachmentsInput.removeAttribute('data-required');
		         attachmentRequiredIndicator.style.display = 'none';
		         fileUploadLabel.style.borderColor = '#dee2e6';
		         fileUploadLabel.style.backgroundColor = '#f8f9fa';
		         
		         const docNotice = document.getElementById('documentRequirementNotice');
		         if (docNotice) {
		             docNotice.style.display = 'none';
		         }
		     }
		     
		  
		     
		     // Handle duration type selection based on category
		     if (leaveTypeCategory === 'Both') {
		         // Show duration type selector for "Both" category
		         durationTypeGroup.classList.remove('hidden');
		         durationTypeSelect.disabled = false;
		         durationTypeSelect.required = true;
		         durationTypeSelect.innerHTML = '<option value="">Select Duration Type</option>' +
		                                       '<option value="Full Day">Full Day</option>' +
		                                       '<option value="Half Day">Half Day</option>';
		         
		         // Reset other fields until duration is selected
		         shiftGroup.classList.add('hidden');
		         shiftSelect.required = false;
		         endDateGroup.classList.remove('hidden');
		         endDateInput.required = true;
		         
		     } else if (leaveTypeCategory === 'Full Day') {
		         // Auto-select Full Day
		         durationTypeGroup.classList.add('hidden');
		         durationTypeSelect.value = 'Full Day';
		         durationTypeSelect.disabled = true;
		         durationTypeSelect.required = false;
		         
		         // Full day settings
		         shiftGroup.classList.add('hidden');
		         shiftSelect.required = false;
		         shiftSelect.value = '';
		         endDateGroup.classList.remove('hidden');
		         endDateInput.required = true;
		         
		     } else if (leaveTypeCategory === 'Half Day') {
		         // Auto-select Half Day
		         durationTypeGroup.classList.add('hidden');
		         durationTypeSelect.value = 'Half Day';
		         durationTypeSelect.disabled = true;
		         durationTypeSelect.required = false;
		         
		         // Half day settings
		         shiftGroup.classList.remove('hidden');
		         shiftSelect.required = true;
		         endDateGroup.classList.add('hidden');
		         endDateInput.required = false;
		         
		         if (startDateInput.value) {
		             endDateInput.value = startDateInput.value;
		         }
		         
		         // Update shift options
		         const availableShifts = selectedOption.dataset.availableShifts || 'Both';
		         updateShiftOptions(availableShifts);
		     }
		     
		    // validateStandardDuration();
		     //calculateDuration();
		     //checkLeaveBalanceAndUpdateUI();
		     
		     // Auto-set end date if standard duration > 15 (but keep it editable)
		     //if (startDateInput.value) {
		       //  autoSetEndDateForLongDuration();
		     //}
		     checkFormValidity();
		     
		 });
		 
		 // ========================================
		 // DURATION TYPE CHANGE HANDLER
		 // ========================================
		 
		 durationTypeSelect.addEventListener('change', function() {
		     const selectedDurationType = this.value;
		     const selectedLeaveTypeOption = leaveTypeSelect.options[leaveTypeSelect.selectedIndex];
		     
		     if (!selectedDurationType) {
		         shiftGroup.classList.add('hidden');
		         shiftSelect.required = false;
		         endDateGroup.classList.remove('hidden');
		         endDateInput.required = true;
		         return;
		     }
		     
		     // Show/hide shift and end date based on duration type
		     if (selectedDurationType === 'Half Day') {
		         shiftGroup.classList.remove('hidden');
		         shiftSelect.required = true;
		         endDateGroup.classList.add('hidden');
		         endDateInput.required = false;
		         
		         if (startDateInput.value) {
		             endDateInput.value = startDateInput.value;
		             calculateDuration();
		         }
		         
		         // Update shift options
		         const availableShifts = selectedLeaveTypeOption.dataset.availableShifts || 'Both';
		         updateShiftOptions(availableShifts);
		         
		     } else {
		         shiftGroup.classList.add('hidden');
		         shiftSelect.required = false;
		         shiftSelect.value = '';
		         shiftInfo.classList.remove('show');
		         endDateGroup.classList.remove('hidden');
		         endDateInput.required = true;
		     }
		     
		     //calculateDuration();
		     
		     // Auto-set end date if standard duration > 15 (but keep it editable)
		    // if (startDateInput.value) {
		      //   autoSetEndDateForLongDuration();
		     //}
		     checkFormValidity();
		 });
		 
		 // ========================================
		 // UPDATE SHIFT OPTIONS FUNCTION
		 // ========================================
		 
		 function updateShiftOptions(availableShifts) {
		     shiftSelect.innerHTML = '';
		     shiftSelect.disabled = false;
		     shiftSelect.required = true;
		     shiftInfo.classList.remove('show');
		     
		     const normalizedShifts = (availableShifts || '').trim();
		     
		     if (normalizedShifts === 'Morning') {
		         const option = document.createElement('option');
		         option.value = 'Morning';
		         option.textContent = 'Morning Shift (Auto-Selected)';
		         option.selected = true;
		         shiftSelect.appendChild(option);
		         
		         shiftSelect.style.backgroundColor = '#f8f9fa';
		         shiftSelect.style.color = '#6c757d';
		         shiftSelect.style.pointerEvents = 'none';
		         shiftSelect.required = false;
		         
		         shiftInfoText.innerHTML = ' This leave type is restricted to <strong>Morning Shift</strong> only.';
		         shiftInfo.classList.add('show');
		         
		     } else if (normalizedShifts === 'Afternoon') {
		         const option = document.createElement('option');
		         option.value = 'Afternoon';
		         option.textContent = 'Afternoon Shift (Auto-Selected)';
		         option.selected = true;
		         shiftSelect.appendChild(option);
		         
		         shiftSelect.style.backgroundColor = '#f8f9fa';
		         shiftSelect.style.color = '#6c757d';
		         shiftSelect.style.pointerEvents = 'none';
		         shiftSelect.required = false;
		         
		         shiftInfoText.innerHTML = ' This leave type is restricted to <strong>Afternoon Shift</strong> only.';
		         shiftInfo.classList.add('show');
		         
		     } else {
		         const defaultOption = document.createElement('option');
		         defaultOption.value = '';
		         defaultOption.textContent = 'Please select your preferred shift';
		         shiftSelect.appendChild(defaultOption);
		         
		         const morningOption = document.createElement('option');
		         morningOption.value = 'Morning';
		         morningOption.textContent = 'Morning Shift';
		         shiftSelect.appendChild(morningOption);
		         
		         const afternoonOption = document.createElement('option');
		         afternoonOption.value = 'Afternoon';
		         afternoonOption.textContent = 'Afternoon Shift';
		         shiftSelect.appendChild(afternoonOption);
		         
		         shiftSelect.style.backgroundColor = '';
		         shiftSelect.style.color = '';
		         shiftSelect.style.pointerEvents = '';
		         shiftSelect.required = true;
		         
		         shiftInfoText.innerHTML = ' You can choose either <strong>Morning</strong> or <strong>Afternoon</strong> shift.';
		         shiftInfo.classList.add('show');
		     }
		 }
		 
		 shiftSelect.addEventListener('change', function() {
			    checkFormValidity();
			});
		 
		//========================================
		//DATE CHANGE HANDLERS
		//========================================
		
		// This is now handled by Flatpickr's onChange callback
		// Keep this for half-day weekend validation only
		startDateInput.addEventListener('change', function() {
		    const selectedLeaveTypeOption = leaveTypeSelect.options[leaveTypeSelect.selectedIndex];
		    const leaveTypeCategory = selectedLeaveTypeOption ? selectedLeaveTypeOption.dataset.leaveTypeCategory : '';
		    const currentDurationType = durationTypeSelect.value;
		    
		    // Check if it's half day
		    if (leaveTypeCategory === 'Half Day' || currentDurationType === 'Half Day') {
		        const selectedDate = new Date(this.value);
		        const dayOfWeek = selectedDate.getDay();
		        
		        // Check if selected date is a weekend
		        if (dayOfWeek === 0 || dayOfWeek === 6) {
		            showError('startDateError', 'Half day leave cannot be applied for weekends. Please select a weekday.');
		            startDateInput.classList.add('error');
		            this.value = '';
		            endDateInput.value = '';
		            calculatedDurationInput.value = '';
		            leaveDurationInput.value = '0';
		            return;
		        } else {
		            hideError('startDateError');
		            startDateInput.classList.remove('error');
		            endDateInput.value = this.value;
		        }
		    }
		    checkFormValidity();
		});
		 
		 endDateInput.addEventListener('change', calculateDuration);
		 
		 endDateInput.addEventListener('change', function() {
			    checkFormValidity();
			});
 
 // ========================================
 // CALCULATE DURATION FUNCTION
 // ========================================
 
 function calculateDuration() {
    console.log('=== CALCULATE DURATION CALLED ===');
    console.log('  Start Date:', startDateInput.value);
    console.log('  End Date:', endDateInput.value);
    
    if (!startDateInput.value) {
        calculatedDurationInput.value = '';
        leaveDurationInput.value = '0';
        return;
    }
    
    const selectedLeaveTypeOption = leaveTypeSelect.options[leaveTypeSelect.selectedIndex];
    const leaveTypeCategory = selectedLeaveTypeOption ? selectedLeaveTypeOption.dataset.leaveTypeCategory : '';
    const currentDurationType = durationTypeSelect.value;
    
    // Determine if it's half day
    const isHalfDay = leaveTypeCategory === 'Half Day' || currentDurationType === 'Half Day';
    
    if (isHalfDay) {
        calculatedDurationInput.value = '0.5 days (Half Day)';
        leaveDurationInput.value = '0.5';
        console.log('✅ Half day duration set');
    } else if (endDateInput.value) {
        const startDate = new Date(startDateInput.value + 'T00:00:00');
        const endDate = new Date(endDateInput.value + 'T00:00:00');
        
        console.log('  Calculating business days between:', startDate, 'and', endDate);
        
        // Calculate actual business days (excluding weekends AND public holidays)
        let businessDays = 0;
        let currentDate = new Date(startDate);
        
        while (currentDate <= endDate) {
            const dayOfWeek = currentDate.getDay();
            const dateString = formatDateToString(currentDate);
            
            // ✅ Count only if it's NOT a weekend AND NOT a public holiday
            if (dayOfWeek !== 0 && dayOfWeek !== 6 && !isPublicHoliday(dateString)) {
                businessDays++;
                console.log('  ✓ Counted:', dateString);
            } else {
                if (dayOfWeek === 0 || dayOfWeek === 6) {
                    console.log('  ✗ Skipped (weekend):', dateString);
                } else {
                    console.log('  ✗ Skipped (public holiday):', dateString);
                }
            }
            
            currentDate.setDate(currentDate.getDate() + 1);
        }
        
        console.log('  Business days calculated (excluding weekends & public holidays):', businessDays);
        
        if (businessDays > 0) {
            // Check if this is a fixed duration leave type
            const isFixedDuration = selectedLeaveTypeOption && selectedLeaveTypeOption.dataset.fixedDuration === 'true';
            const standardDuration = selectedLeaveTypeOption ? parseFloat(selectedLeaveTypeOption.dataset.standardDuration) : 0;
            
            console.log('  Is Fixed Duration:', isFixedDuration);
            console.log('  Standard Duration:', standardDuration);
            
            if (isFixedDuration && standardDuration > 0) {
                const displayText = businessDays + ' working day(s) (Fixed Duration: ' + standardDuration + ' day(s) required)';
                calculatedDurationInput.value = displayText;
                console.log('✅ Fixed duration display:', displayText);
            } else {
                calculatedDurationInput.value = businessDays + ' working day(s) (excluding weekends & public holidays)';
                console.log('✅ Regular duration display:', calculatedDurationInput.value);
            }
            leaveDurationInput.value = businessDays.toString();
            
            console.log('📊 Final calculated duration:', businessDays, 'working days');
        } else {
            calculatedDurationInput.value = 'No working days in selected range (all weekends/holidays)';
            leaveDurationInput.value = '0';
            console.log('⚠️ No working days in range');
        }
    } else {
        calculatedDurationInput.value = '';
        leaveDurationInput.value = '0';
        console.log('⚠️ End date not set yet');
    }
    
    // Continue with overlap check and other validations...
    const endDate = endDateInput.value || startDateInput.value;
    const overlapCheck = checkDateRangeOverlap(startDateInput.value, endDate);

    if (overlapCheck.hasOverlap) {
        showError('startDateError', 'Selected dates overlap with an existing leave application. Please choose different dates.');
        showError('endDateError', 'Selected dates overlap with an existing leave application.');
        
        startDateInput.classList.add('error');
        endDateInput.classList.add('error');
        
        // checkFormValidity() will handle the submit button state
        
        return;
    } else {
        const startError = document.getElementById('startDateError');
        const endError = document.getElementById('endDateError');
        
        if (startError && startError.textContent.includes('overlap')) {
            hideError('startDateError');
            startDateInput.classList.remove('error');
        }
        if (endError && endError.textContent.includes('overlap')) {
            hideError('endDateError');
            endDateInput.classList.remove('error');
        }
    }
 
    validateStandardDuration();
    checkLeaveBalanceAndUpdateUI();
    checkFormValidity(); // ADD THIS LINE
}


//Check if a date is unavailable
 function isDateUnavailable(dateString) {
     if (!window.unavailableDates || window.unavailableDates.length === 0) {
         return false;
     }
     
     const checkDate = new Date(dateString);
     
     for (let range of window.unavailableDates) {
         const rangeStart = new Date(range.startDate);
         const rangeEnd = new Date(range.endDate);
         
         if (checkDate >= rangeStart && checkDate <= rangeEnd) {
             return true;
         }
     }
     
     return false;
 }
 
 // Get list of unavailable date ranges for display
 function getUnavailableDatesArray() {
     return window.unavailableDates || [];
 }
 

 // ========================================
 // VALIDATE STANDARD DURATION
 // ========================================
 
 function validateStandardDuration() {
     const selectedOption = leaveTypeSelect.options[leaveTypeSelect.selectedIndex];
     const standardDuration = selectedOption ? selectedOption.dataset.standardDuration : '';
     const affectsBalance = selectedOption ? selectedOption.dataset.affectsBalance === 'true' : false;
     const requestedDuration = parseFloat(leaveDurationInput.value);
     
     if (standardDuration && requestedDuration > 0) {
         const maxDuration = parseFloat(standardDuration);
         if (maxDuration > 0 && requestedDuration > maxDuration) {
             let errorMessage = '';
             if (affectsBalance) {
                 errorMessage = `Requested duration exceeds annual allocation for this leave type`;
             } else {
                 errorMessage = `Requested duration exceeds maximum days allowed per application for this leave type`;
             }
             showError('startDateError', errorMessage);
             showError('endDateError', errorMessage);
             
            
             
             return false;
         } else {
             hideError('startDateError');
             hideError('endDateError');
         }
     }
     
     checkFormValidity(); // REPLACE WITH THIS
     return true;
 }
 
 function checkLeaveBalanceAndUpdateUI() {
     const selectedOption = leaveTypeSelect.options[leaveTypeSelect.selectedIndex];
     
     if (!selectedOption || !selectedOption.value) {
         // Re-enable submit if no leave type selected yet
         checkAndEnableSubmitButton();
         return true;
     }
     
     const affectsBalance = selectedOption.dataset.affectsBalance === 'true';
     const leaveTypeId = selectedOption.value;
     const requestedDuration = parseFloat(leaveDurationInput.value) || 0;
     
     if (affectsBalance && window.leaveBalances && window.leaveBalances[leaveTypeId]) {
         const availableBalance = window.leaveBalances[leaveTypeId].available;
         
         if (requestedDuration > availableBalance) {
             const errorMessage = 'Insufficient leave balance. Available: ' + availableBalance + ' day(s), Requested: ' + requestedDuration + ' day(s)';
             showError('startDateError', errorMessage);
             showError('endDateError', errorMessage);
             
             
             
             return false;
         } else {
             // Clear balance-related errors only
             const startError = document.getElementById('startDateError');
             const endError = document.getElementById('endDateError');
             
             // Only hide if the error is about balance
             if (startError && startError.textContent.includes('Insufficient leave balance')) {
                 hideError('startDateError');
             }
             if (endError && endError.textContent.includes('Insufficient leave balance')) {
                 hideError('endDateError');
             }
         }
     }
     
     checkFormValidity(); // REPLACE WITH THIS
     return true;
 }
 
 
 
 // ========================================
 // FILE UPLOAD DISPLAY
 // ========================================
 
 attachmentsInput.addEventListener('change', function() {
     const fileList = document.getElementById('fileList');
     const fileUploadLabel = document.querySelector('.file-upload-label');
     fileList.innerHTML = '';
     
     fileUploadLabel.style.borderColor = '#dee2e6';
     fileUploadLabel.style.backgroundColor = '#f8f9fa';
     
     hideError('attachmentError');
     attachmentsInput.classList.remove('error');
     
     if (this.files.length > 0) {
         for (let i = 0; i < this.files.length; i++) {
             const file = this.files[i];
             const fileItem = document.createElement('div');
             fileItem.className = 'file-item';
             fileItem.innerHTML = 
                 '<span><i class="fas fa-file"></i> ' + file.name + '</span>' +
                 '<span>' + Math.round(file.size / 1024 * 10) / 10 + ' KB</span>';
             fileList.appendChild(fileItem);
         }
         
         const isRequired = attachmentsInput.getAttribute('data-required') === 'true';
         
         if (isRequired) {
             fileUploadLabel.style.borderColor = '#28a745';
             fileUploadLabel.style.backgroundColor = '#f0fff4';
         }
     } else {
         const isRequired = attachmentsInput.getAttribute('data-required') === 'true';
         if (isRequired) {
             fileUploadLabel.style.borderColor = '#dc3545';
             fileUploadLabel.style.backgroundColor = '#fff5f5';
         }
     }
     checkFormValidity();
 });
 
 leaveDescriptionTextarea.addEventListener('input', function() {
	    const charCount = this.value.trim().length;
	    const charCountDisplay = document.getElementById('leaveReasonCharCount');
	    const minChars = 10;
	    
	    if (charCountDisplay) {
	        if (charCount === 0) {
	            charCountDisplay.innerHTML = '<i class="fas fa-info-circle"></i> Minimum 10 characters required (0/10)';
	            charCountDisplay.style.color = '#6c757d';
	        } else if (charCount < minChars) {
	            charCountDisplay.innerHTML = '<i class="fas fa-exclamation-circle"></i> ' + (minChars - charCount) + ' more character(s) needed (' + charCount + '/' + minChars + ')';
	            charCountDisplay.style.color = '#dc3545';
	        } else {
	            charCountDisplay.innerHTML = '<i class="fas fa-check-circle"></i> Character requirement met (' + charCount + ' characters)';
	            charCountDisplay.style.color = '#28a745';
	        }
	    }
	    
	    checkFormValidity();
	});
 
 // ========================================
 // FORM SUBMISSION VALIDATION
 // ========================================
 
 form.addEventListener('submit', function(e) {
    e.preventDefault();
    
    let isValid = true;
    
    console.log('=== FORM SUBMISSION VALIDATION ===');
    
    document.querySelectorAll('.form-error').forEach(error => error.style.display = 'none');
    document.querySelectorAll('input, select, textarea').forEach(input => input.classList.remove('error'));
    
    // Validate leave type selection
    if (!leaveTypeSelect.value.trim()) {
        showError('leaveTypeIdError', 'Please select a leave type');
        leaveTypeSelect.classList.add('error');
        isValid = false;
    }
    
    // ✅ DECLARE ONCE AT THE TOP
    const selectedLeaveTypeOption = leaveTypeSelect.options[leaveTypeSelect.selectedIndex];
    
    if (selectedLeaveTypeOption && selectedLeaveTypeOption.value) {
        const leaveTypeCategory = selectedLeaveTypeOption.dataset.leaveTypeCategory;
        
        // Validate duration type for "Both" category
        if (leaveTypeCategory === 'Both' && !durationTypeSelect.value.trim()) {
            showError('durationTypeError', 'Please select a duration type');
            durationTypeSelect.classList.add('error');
            isValid = false;
        }
        
        // Validate document requirement
        const requiresDocumentDataset = selectedLeaveTypeOption.dataset.requiresDocument;
        const requiresDocument = requiresDocumentDataset === 'true';
        const hasFile = attachmentsInput.files && attachmentsInput.files.length > 0;
        
        console.log('Document validation check:');
        console.log('  - Selected leave type:', selectedLeaveTypeOption.textContent);
        console.log('  - Requires document:', requiresDocument);
        console.log('  - Has file uploaded:', hasFile);
        
        if (requiresDocument && !hasFile) {
            console.log('  ❌ NO FILE UPLOADED - BLOCKING SUBMISSION');
            showError('attachmentError', 'Supporting document is required for this leave type. Please attach a file.');
            const fileUploadLabel = document.querySelector('.file-upload-label');
            fileUploadLabel.style.borderColor = '#dc3545';
            fileUploadLabel.style.backgroundColor = '#fff5f5';
            attachmentsInput.classList.add('error');
            isValid = false;
            
            document.getElementById('attachmentGroup').scrollIntoView({ behavior: 'smooth', block: 'center' });
        }
        
        // Validate shift for Half Day
        const currentDurationType = durationTypeSelect.value;
        const isHalfDay = leaveTypeCategory === 'Half Day' || currentDurationType === 'Half Day';
        
        if (isHalfDay) {
            if (!shiftSelect.value || shiftSelect.value.trim() === '') {
                showError('shiftError', 'Shift selection is required for half day leave');
                shiftSelect.classList.add('error');
                isValid = false;
            } else if (shiftSelect.disabled) {
                shiftSelect.disabled = false;
            }
        }
    }
    
    // Validate start date
    if (!startDateInput.value) {
        showError('startDateError', 'Please select a start date');
        startDateInput.classList.add('error');
        isValid = false;
    } else {
        // ✅ Validate start date is not a public holiday
        if (isPublicHoliday(startDateInput.value)) {
            showError('startDateError', 'Cannot apply for leave on a public holiday. Please select a working day.');
            startDateInput.classList.add('error');
            isValid = false;
        }
    }
    
    // ✅ REUSE selectedLeaveTypeOption (already declared above)
    // Validate end date for Full Day
    if (selectedLeaveTypeOption && selectedLeaveTypeOption.value) {
        const leaveTypeCategory = selectedLeaveTypeOption.dataset.leaveTypeCategory;
        const currentDurationType = durationTypeSelect.value;
        const isFullDay = leaveTypeCategory === 'Full Day' || currentDurationType === 'Full Day';
        
        if (isFullDay) {
            if (!endDateInput.value) {
                showError('endDateError', 'Please select an end date');
                endDateInput.classList.add('error');
                isValid = false;
            } else if (isPublicHoliday(endDateInput.value)) {
                showError('endDateError', 'Cannot apply for leave ending on a public holiday. Please select a working day.');
                endDateInput.classList.add('error');
                isValid = false;
            }
        }
    }
    
    // Check if duration is 0 (all weekends selected)
    const duration = parseFloat(leaveDurationInput.value) || 0;
    if (duration === 0 && startDateInput.value && endDateInput.value) {
        showError('startDateError', 'Selected date range contains only weekends. Please select working days.');
        showError('endDateError', 'Selected date range contains only weekends. Please select working days.');
        isValid = false;
    }
    
    // Validate leave reason (REQUIRED)
    const leaveReason = leaveDescriptionTextarea.value.trim();
    if (!leaveReason) {
        showError('leaveDescriptionError', 'Leave reason is required');
        leaveDescriptionTextarea.classList.add('error');
        isValid = false;
    } else if (leaveReason.length < 10) {
        showError('leaveDescriptionError', 'Leave reason must be at least 10 characters long');
        leaveDescriptionTextarea.classList.add('error');
        isValid = false;
    } else {
        const hasLetters = /[a-zA-Z]/.test(leaveReason);
        if (!hasLetters) {
            showError('leaveDescriptionError', 'Leave reason must contain meaningful text with at least one letter');
            leaveDescriptionTextarea.classList.add('error');
            isValid = false;
        }
    }
    
   
    
    // Validate duration and balance
    const durationValidation = validateStandardDuration();
    if (!durationValidation) {
        isValid = false;
    }
    
    const balanceValidation = checkLeaveBalanceAndUpdateUI();
    if (!balanceValidation) {
        isValid = false;
    }
    
    console.log('Form validation result:', isValid ? 'PASS ✅' : 'FAIL ❌');
    
    if (!isValid) {
        console.log('❌ FORM SUBMISSION BLOCKED - Validation Failed');
        const firstError = document.querySelector('.error');
        if (firstError) {
            firstError.scrollIntoView({ behavior: 'smooth', block: 'center' });
            firstError.focus();
        }
        return;
    }
    
    // Check for past dates
    const endDate = endDateInput.value || startDateInput.value;
    const hasPastDates = checkIfPastDates(startDateInput.value, endDate);
    
    if (hasPastDates) {
        const startDate = startDateInput.value;
        const endDateStr = endDate;
        
        let dateRangeText;
        if (startDate === endDateStr) {
            dateRangeText = `<strong>${startDate}</strong>`;
        } else {
            dateRangeText = `<strong>${startDate}</strong> to <strong>${endDateStr}</strong>`;
        }
        
        showConfirmModal(
            'Past Date Application',
            `<div style="text-align: left;">
                <p><i class="fas fa-exclamation-triangle" style="color: #ffc107;"></i> You are submitting a leave application for past date(s):</p>
                <p style="margin: 15px 0; text-align: center; font-size: 1.1rem;">${dateRangeText}</p>
                <p style="margin-top: 15px;">Do you want to proceed with this application?</p>
            </div>`,
            function() {
                console.log('✅ User confirmed past date submission');
                submitForm();
            }
        );
        return;
    }
    
    console.log('✅ FORM SUBMISSION ALLOWED - No past dates');
    submitForm();
});

// Helper function to actually submit the form
function submitForm() {
    submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Submitting...';
    submitBtn.disabled = true;
    
    // Actually submit the form
    form.submit();
}
 
 // ========================================
 // BUTTON HANDLERS
 // ========================================
 
// ========================================
// BUTTON HANDLERS
// ========================================

document.getElementById('clearBtn').addEventListener('click', function() {
    showConfirmModal(
        'Clear Form',
        'Are you sure you want to clear all form data?',
        function() {
            clearEntireForm();
            // Just close the modal, stay on page
            closeConfirmModal();
        }
    );
});

document.getElementById('cancelBtn').addEventListener('click', function() {
    showConfirmModal(
        'Cancel Application',
        'Are you sure you want to cancel? All entered data will be lost.',
        function() {
            clearEntireForm();
            // Redirect to dashboard
            window.location.href = window.contextPath + '/Employee/EmployeeDashboard.jsp';
        }
    );
});
 
 // ========================================
 // HELPER FUNCTIONS
 // ========================================
 
 function showError(elementId, message) {
     const errorElement = document.getElementById(elementId);
     if (errorElement) {
         errorElement.textContent = message;
         errorElement.style.display = 'block';
     }
 }
 
 function hideError(elementId) {
     const errorElement = document.getElementById(elementId);
     if (errorElement) {
         errorElement.style.display = 'none';
     }
 }
 
 function clearAllErrors() {
	    document.querySelectorAll('.form-error').forEach(function(error) {
	        error.style.display = 'none';
	    });
	    
	    document.querySelectorAll('input, select, textarea').forEach(function(input) {
	        input.classList.remove('error');
	    });
	}
 
 function clearEntireForm() {
	    console.log('=== CLEARING ENTIRE FORM ===');
	    
	    // Reset form
	    form.reset();
	    
	    // Clear leave type selection
	    leaveTypeSelect.value = '';
	    
	    // Reset and hide duration type
	    durationTypeSelect.value = '';
	    durationTypeSelect.disabled = true;
	    durationTypeGroup.classList.add('hidden');
	    
	    // Reset and hide shift
	    shiftSelect.value = '';
	    shiftGroup.classList.add('hidden');
	    shiftInfo.classList.remove('show');
	    
	    // Clear dates
	    startDateInput.value = '';
	    endDateInput.value = '';
	    
	    // Clear Flatpickr dates
	    if (startDatePicker) {
	        startDatePicker.clear();
	    }
	    if (endDatePicker) {
	        endDatePicker.clear();
	    }
	    
	    // Clear duration
	    calculatedDurationInput.value = '';
	    leaveDurationInput.value = '0';
	    
	    // Clear leave reason
	    leaveDescriptionTextarea.value = '';
	    
	    // Reset character count display
	    const charCountDisplay = document.getElementById('leaveReasonCharCount');
	    if (charCountDisplay) {
	        charCountDisplay.innerHTML = '<i class="fas fa-info-circle"></i> Minimum 10 characters required (0/10)';
	        charCountDisplay.style.color = '#6c757d';
	    }
	    
	    // Clear file upload
	    attachmentsInput.value = '';
	    document.getElementById('fileList').innerHTML = '';
	    
	    // Reset file upload styling
	    const fileUploadLabel = document.querySelector('.file-upload-label');
	    if (fileUploadLabel) {
	        fileUploadLabel.style.borderColor = '#dee2e6';
	        fileUploadLabel.style.backgroundColor = '#f8f9fa';
	    }
	    
	    // Hide attachment required indicator
	    if (attachmentRequiredIndicator) {
	        attachmentRequiredIndicator.style.display = 'none';
	    }
	    
	    // Remove document requirement notice
	    const docNotice = document.getElementById('documentRequirementNotice');
	    if (docNotice) {
	        docNotice.style.display = 'none';
	    }
	    
	    // Remove auto-date info
	    const autoDateInfo = document.getElementById('autoDateInfo');
	    if (autoDateInfo) {
	        autoDateInfo.remove();
	    }
	    
	    // Reset end date field styling
	    endDateInput.disabled = false;
	    endDateInput.readOnly = false;
	    endDateInput.style.backgroundColor = '';
	    endDateInput.style.cursor = '';
	    endDateInput.style.color = '';
	    endDateInput.removeAttribute('data-locked');
	    
	    // Show end date group
	    endDateGroup.classList.remove('hidden');
	    endDateInput.required = true;
	    
	    // Clear all errors
	    clearAllErrors();
	    
	    // Re-check form validity
	    checkFormValidity();
	    
	    console.log('✅ Form cleared successfully');
	}
 
 function checkFormValidity() {
	    const missingFields = [];
	    
	    // Check leave type
	    if (!leaveTypeSelect.value.trim()) {
	        missingFields.push('Leave Type');
	    }
	    
	    const selectedLeaveTypeOption = leaveTypeSelect.options[leaveTypeSelect.selectedIndex];
	    
	    if (selectedLeaveTypeOption && selectedLeaveTypeOption.value) {
	        const leaveTypeCategory = selectedLeaveTypeOption.dataset.leaveTypeCategory;
	        const currentDurationType = durationTypeSelect.value;
	        const requiresDocument = selectedLeaveTypeOption.dataset.requiresDocument === 'true';
	        
	        // Check duration type for "Both" category
	        if (leaveTypeCategory === 'Both' && !currentDurationType.trim()) {
	            missingFields.push('Duration Type');
	        }
	        
	        // Check shift for Half Day
	        const isHalfDay = leaveTypeCategory === 'Half Day' || currentDurationType === 'Half Day';
	        if (isHalfDay && (!shiftSelect.value || shiftSelect.value.trim() === '')) {
	            missingFields.push('Shift');
	        }
	        
	        // Check document requirement
	        const hasFile = attachmentsInput.files && attachmentsInput.files.length > 0;
	        if (requiresDocument && !hasFile) {
	            missingFields.push('Supporting Document (Required)');
	            
	            const fileUploadLabel = document.querySelector('.file-upload-label');
	            if (fileUploadLabel) {
	                fileUploadLabel.style.borderColor = '#dc3545';
	                fileUploadLabel.style.backgroundColor = '#fff5f5';
	            }
	        } else if (requiresDocument && hasFile) {
	            // Document is required and uploaded - show success state
	            const fileUploadLabel = document.querySelector('.file-upload-label');
	            if (fileUploadLabel) {
	                fileUploadLabel.style.borderColor = '#28a745';
	                fileUploadLabel.style.backgroundColor = '#f0fff4';
	            }
	        }
	        
	        // Check end date for Full Day
	        const isFullDay = leaveTypeCategory === 'Full Day' || currentDurationType === 'Full Day';
	        if (isFullDay && !endDateInput.value) {
	            missingFields.push('End Date');
	        }
	    }
	    
	    // Check dates - start date
	    if (!startDateInput.value) {
	        missingFields.push('Start Date');
	    }
	    
	    // Check leave reason
	    const leaveReason = leaveDescriptionTextarea.value.trim();
	    if (!leaveReason) {
	        missingFields.push('Leave Reason');
	    } else if (leaveReason.length < 10) {
	        missingFields.push('Leave Reason (minimum 10 characters)');
	    }
	    
	    // Check for any visible errors
	    const visibleErrors = document.querySelectorAll('.form-error[style*="display: block"]');
	    const hasErrors = visibleErrors.length > 0;
	    
	    // Update submit button state
	    const submitBtn = document.getElementById('submitBtn');
	    const tooltip = document.getElementById('submitButtonTooltip');
	    
	    if (missingFields.length > 0 || hasErrors) {
	        submitBtn.disabled = true;
	        
	        let tooltipMessage = '';
	        
	        // If there are validation errors, prioritize showing that message
	        if (hasErrors && missingFields.length === 0) {
	            // Only validation errors, no missing fields
	            tooltipMessage = '⚠️ Please fix the validation errors above before submitting';
	        } else if (hasErrors && missingFields.length > 0) {
	            // Both validation errors AND missing fields
	            tooltipMessage = '⚠️ Required fields:\n\n';
	            
	            // Prioritize the order
	            const orderedFields = [];
	            const fieldOrder = [
	                'Leave Type',
	                'Duration Type',
	                'Start Date',
	                'End Date',
	                'Shift',
	                'Leave Reason',
	                'Leave Reason (minimum 10 characters)',
	                'Supporting Document (Required)'
	            ];
	            
	            // Add fields in priority order
	            fieldOrder.forEach(priorityField => {
	                if (missingFields.includes(priorityField)) {
	                    orderedFields.push(priorityField);
	                }
	            });
	            
	            // Add any remaining fields not in the priority list
	            missingFields.forEach(field => {
	                if (!orderedFields.includes(field)) {
	                    orderedFields.push(field);
	                }
	            });
	            
	            // Build the message
	            orderedFields.forEach(field => {
	                tooltipMessage += '• ' + field + '\n';
	            });
	            
	            tooltipMessage += '\n⚠️ Also fix validation errors above';
	        } else {
	            // Only missing fields, no validation errors
	            tooltipMessage = '⚠️ Required fields:\n\n';
	            
	            // Prioritize the order
	            const orderedFields = [];
	            const fieldOrder = [
	                'Leave Type',
	                'Duration Type',
	                'Start Date',
	                'End Date',
	                'Shift',
	                'Leave Reason',
	                'Leave Reason (minimum 10 characters)',
	                'Supporting Document (Required)'
	            ];
	            
	            // Add fields in priority order
	            fieldOrder.forEach(priorityField => {
	                if (missingFields.includes(priorityField)) {
	                    orderedFields.push(priorityField);
	                }
	            });
	            
	            // Add any remaining fields not in the priority list
	            missingFields.forEach(field => {
	                if (!orderedFields.includes(field)) {
	                    orderedFields.push(field);
	                }
	            });
	            
	            // Build the message
	            orderedFields.forEach(field => {
	                tooltipMessage += '• ' + field + '\n';
	            });
	        }
	        
	        tooltip.textContent = tooltipMessage;
	    } else {
	        submitBtn.disabled = false;
	        tooltip.textContent = '';
	    }
	}
 
 // ========================================
 // POPULATE LEAVE TYPES INFO ON LOAD
 // ========================================
 
 console.log('Calling populateLeaveTypesInfo from main DOMContentLoaded');
 populateLeaveTypesInfo();
 
 // Setup toggle for leave types info section
 const toggleButton = document.getElementById('leaveTypesToggle');
if (toggleButton) {
    toggleButton.addEventListener('click', function() {
        const content = document.getElementById('leaveTypesContent');
        const icon = document.getElementById('toggleIcon');
        
        if (content.style.display === 'none') {
            content.style.display = 'block';
            icon.classList.add('rotated');
        } else {
            content.style.display = 'none';
            icon.classList.remove('rotated');
        }
    });
    
 // Setup toggle for Full Day section
    const fullDayToggle = document.getElementById('fullDayToggle');
    if (fullDayToggle) {
        fullDayToggle.addEventListener('click', function() {
            const content = document.getElementById('fullDayTypesList');
            const icon = document.getElementById('fullDayIcon');
            
            if (content.style.display === 'none') {
                content.style.display = 'block';
                icon.style.transform = 'rotate(180deg)';
            } else {
                content.style.display = 'none';
                icon.style.transform = 'rotate(0deg)';
            }
        });
    }

    // Setup toggle for Half Day section
    const halfDayToggle = document.getElementById('halfDayToggle');
    if (halfDayToggle) {
        halfDayToggle.addEventListener('click', function() {
            const content = document.getElementById('halfDayTypesList');
            const icon = document.getElementById('halfDayIcon');
            
            if (content.style.display === 'none') {
                content.style.display = 'block';
                icon.style.transform = 'rotate(180deg)';
            } else {
                content.style.display = 'none';
                icon.style.transform = 'rotate(0deg)';
            }
        });
    }

    // Setup toggle for Both section
    const bothToggle = document.getElementById('bothToggle');
    if (bothToggle) {
        bothToggle.addEventListener('click', function() {
            const content = document.getElementById('bothTypesList');
            const icon = document.getElementById('bothIcon');
            
            if (content.style.display === 'none') {
                content.style.display = 'block';
                icon.style.transform = 'rotate(180deg)';
            } else {
                content.style.display = 'none';
                icon.style.transform = 'rotate(0deg)';
            }
        });
    }
    
    // Start with section CLOSED by default
    const content = document.getElementById('leaveTypesContent');
    const icon = document.getElementById('toggleIcon');
    content.style.display = 'none';
    icon.classList.remove('rotated');
}

//Submit button hover tooltip
submitBtn.addEventListener('mouseenter', function(e) {
    const tooltip = document.getElementById('submitButtonTooltip');
    if (this.disabled && tooltip.textContent.trim() !== '') {
        tooltip.style.display = 'block';
        
        // Position tooltip above button
        const rect = this.getBoundingClientRect();
        tooltip.style.left = (rect.left + rect.width / 2) + 'px';
        tooltip.style.top = (rect.top - 10) + 'px';
        tooltip.style.transform = 'translate(-50%, -100%)';
    }
});

submitBtn.addEventListener('mouseleave', function() {
    const tooltip = document.getElementById('submitButtonTooltip');
    tooltip.style.display = 'none';
});

// Initial form validity check
checkFormValidity();
});

</script>
</body>
</html>

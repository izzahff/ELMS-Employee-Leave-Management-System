<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="elms.model.Employee" %>
<%@ page import="elms.model.LeaveApplication" %>
<%@ page import="elms.model.LeaveType" %>
<%@ page import="elms.model.FullDay" %>
<%@ page import="elms.model.HalfDay" %>
<%@ page import="elms.DAO.LeaveApplicationDAO" %>
<%@ page import="elms.DAO.LeaveTypeDAO" %>
<%@ page import="elms.DAO.FullDayDAO" %>
<%@ page import="elms.DAO.HalfDayDAO" %>
<%@ page import="elms.controller.ManagerApproveLeaveController" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Set" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
    Employee employee = (Employee) session.getAttribute("loggedInEmployee");
    String profilePicturePath = null;

    if (employee == null) {
        response.sendRedirect("/ELMS_3.0/Employee/EmployeeLogin.jsp");
        return;
    }
    
    if (employee != null && employee.getProfilePicturePath() != null && !employee.getProfilePicturePath().trim().isEmpty()) {
        profilePicturePath = employee.getProfilePicturePath();
    } else {
        profilePicturePath = "defaultprofilepicture.jpg";
    }

    LeaveApplicationDAO leaveAppDAO = new LeaveApplicationDAO();
    LeaveTypeDAO leaveTypeDAO = new LeaveTypeDAO();
    FullDayDAO fullDayDAO = new FullDayDAO();
    HalfDayDAO halfDayDAO = new HalfDayDAO();
    
    List<LeaveApplication> allApplications = null;
    int pendingCount = 0;
    int approvedCount = 0;
    
    // Map to store leave type balances dynamically calculated
    List<Map<String, Object>> leaveBalances = new ArrayList<>();

    try {
        // Get all applications for this employee
        allApplications = leaveAppDAO.getLeaveApplicationsByEmployee(employee.getEmployeeId());
        
        // Count pending and approved
        for (LeaveApplication app : allApplications) {
            if ("Pending".equals(app.getLeavestatus())) {
                pendingCount++;
            } else if ("Approved".equals(app.getLeavestatus())) {
                approvedCount++;
            }
        }
        
        // Get all leave types that affect balance
        List<LeaveType> allLeaveTypes = leaveTypeDAO.getAllLeaveTypes();
        
        System.out.println("=== Processing Leave Balances ===");
        System.out.println("Total leave types: " + allLeaveTypes.size());
        
        for (LeaveType leaveType : allLeaveTypes) {
            System.out.println("Processing: " + leaveType.getLeaveTypeName() + 
                             " | Category: " + leaveType.getLeaveTypeCategory() + 
                             " | Affects Balance: " + leaveType.isAffectsBalance());
            
            if (leaveType.isAffectsBalance()) {
                Map<String, Object> balanceInfo = new HashMap<>();
                String leaveTypeId = leaveType.getLeaveTypeId();
                String leaveTypeName = leaveType.getLeaveTypeName();
                String category = leaveType.getLeaveTypeCategory();
                
                // Get standard duration based on category
                double standardDuration = 0;
                
                if ("Full Day".equalsIgnoreCase(category)) {
                    FullDay fullDay = fullDayDAO.getFullDayByLeaveTypeId(leaveTypeId);
                    if (fullDay != null) {
                        standardDuration = fullDay.getStandardDuration();
                        System.out.println("  -> Full Day standard duration: " + standardDuration);
                    } else {
                        System.err.println("  -> WARNING: No FullDay record found for " + leaveTypeName);
                    }
                } else if ("Half Day".equalsIgnoreCase(category)) {
                    // Half Day types don't track annual balances in this system
                    System.out.println("  -> Skipping Half Day type - balance tracking not supported for this category");
                    continue;
                } else if ("Both".equalsIgnoreCase(category)) {
                    // For "Both" category, try to get from FullDay table
                    FullDay fullDay = fullDayDAO.getFullDayByLeaveTypeId(leaveTypeId);
                    if (fullDay != null) {
                        standardDuration = fullDay.getStandardDuration();
                        System.out.println("  -> Both category standard duration: " + standardDuration);
                    }
                }
                // Skip if no standard duration is defined
                if (standardDuration == 0) {
                    System.out.println("  -> Skipping " + leaveTypeName + " - no standard duration defined");
                    continue;
                }
                
                // Calculate used days for this specific leave type (approved applications only)
                double usedDays = 0;
                for (LeaveApplication app : allApplications) {
                    if ("Approved".equals(app.getLeavestatus()) && 
                        leaveTypeId.equals(app.getLeavetypeid())) {
                        usedDays += app.getLeaveduration();
                    }
                }
                
                // Calculate remaining balance
                double remainingDays = standardDuration - usedDays;
                
                balanceInfo.put("leaveTypeId", leaveTypeId);
                balanceInfo.put("leaveTypeName", leaveTypeName);
                balanceInfo.put("category", category);
                balanceInfo.put("standardDuration", standardDuration);
                balanceInfo.put("usedDays", usedDays);
                balanceInfo.put("remainingDays", remainingDays);
                
                leaveBalances.add(balanceInfo);
                
                System.out.println("  -> Balance calculated: " + remainingDays + "/" + standardDuration);
            } else {
                System.out.println("  -> Skipped (does not affect balance)");
            }
        }
        
        System.out.println("Total balances calculated: " + leaveBalances.size());
        System.out.println("=================================");
        
    } catch (Exception e) {
        System.err.println("Error fetching leave data: " + e.getMessage());
        e.printStackTrace();
        allApplications = new java.util.ArrayList<>();
    }

    Set<String> newlyUpdatedApps = ManagerApproveLeaveController.getNewlyUpdatedApplications(employee.getEmployeeId());
    int newUpdatesCount = newlyUpdatedApps.size();
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Profile - IMNSB Employee Leave Management System</title>
    <link rel="icon" href="/ELMS_3.0/imnsb_logo.png">
    <link rel="stylesheet" href="/ELMS_3.0/css/styles.css">
    <link rel="stylesheet" href="/ELMS_3.0/css/dashboard.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">

    <style>
        body {
            font-family: 'Segoe UI', sans-serif;
            background-color: #f4f6f9;
            margin: 0;
            padding: 0;
        }

        .content-body {
            padding: 2rem;
        }

        .profile-container {
            background-color: white;
            border-radius: 12px;
            box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
            padding: 2rem;
            max-width: 800px;
            margin: 0 auto;
        }

        .profile-header {
            text-align: center;
            margin-bottom: 2rem;
        }

        .profile-avatar img {
            width: 120px;
            height: 120px;
            border-radius: 50%;
            object-fit: cover;
            border: 3px solid #dee2e6;
        }

        .profile-details {
            margin-top: 1.5rem;
        }

        .profile-item {
            margin-bottom: 1rem;
            display: flex;
            justify-content: space-between;
            border-bottom: 1px solid #eee;
            padding-bottom: 0.5rem;
        }

        .profile-label {
            font-weight: 600;
            color: #555;
        }

        .profile-value {
            color: #333;
        }

        .leave-balance-section {
            margin-top: 2rem;
            background: linear-gradient(135deg, #f0fdf4 0%, #dcfce7 100%);
            border: 1px solid #c6f6d5;
            border-radius: 10px;
            padding: 1.5rem;
        }

        .leave-balance-section h4 {
            margin-bottom: 1rem;
            font-size: 1.2rem;
            color: #2f855a;
            text-align: center;
        }

        .leave-balance-section i.main-icon {
            font-size: 2rem;
            color: #38a169;
            display: block;
            text-align: center;
            margin-bottom: 0.5rem;
        }

        .balance-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
            gap: 1rem;
            margin-bottom: 1rem;
        }

        .balance-card {
            background: white;
            border: 2px solid #c6f6d5;
            border-radius: 8px;
            padding: 1.2rem;
            text-align: center;
            transition: transform 0.3s ease, box-shadow 0.3s ease;
        }

        .balance-card:hover {
            transform: translateY(-4px);
            box-shadow: 0 6px 20px rgba(56, 161, 105, 0.25);
        }

        .balance-card h5 {
            margin: 0 0 1rem 0;
            font-size: 1rem;
            color: #2f855a;
            font-weight: 600;
        }

        .balance-display {
            display: flex;
            justify-content: center;
            align-items: baseline;
            gap: 0.3rem;
            margin: 0.8rem 0;
        }

        .balance-card .balance-value {
            font-size: 2.5rem;
            font-weight: bold;
            color: #276749;
        }

        .balance-card .balance-total {
            font-size: 1.2rem;
            color: #6b7280;
        }

        .balance-card .balance-unit {
            font-size: 0.9rem;
            color: #2f855a;
            margin-top: 0.3rem;
        }

        .balance-breakdown {
            margin-top: 0.8rem;
            padding-top: 0.8rem;
            border-top: 1px solid #e5e7eb;
            font-size: 0.85rem;
            color: #6b7280;
        }

        .balance-breakdown div {
            margin: 0.3rem 0;
        }

        .used-days {
            color: #dc2626;
        }

        .remaining-days {
            color: #16a34a;
            font-weight: 600;
        }

        .balance-info {
            margin-top: 15px;
            padding-top: 15px;
            border-top: 1px solid rgba(47, 133, 90, 0.2);
        }

        .balance-info p {
            font-size: 0.85rem;
            line-height: 1.6;
            margin: 0 0 8px 0;
            color: #2f855a;
            text-align: center;
        }

        /* Email Alert Notification Styles */
        .email-alert-section {
            background: linear-gradient(135deg, #17a2b8, #138496);
            color: white;
            border-radius: 8px;
            box-shadow: 0 2px 10px rgba(23, 162, 184, 0.2);
            margin-bottom: 1.5rem;
            overflow: hidden;
        }

        .email-alert-header {
            padding: 1rem;
            display: flex;
            align-items: center;
            gap: 0.75rem;
        }

        .email-alert-icon {
            font-size: 1.2rem;
            animation: pulse-email 2s infinite;
        }

        @keyframes pulse-email {
            0%, 100% { transform: scale(1); opacity: 1; }
            50% { transform: scale(1.1); opacity: 0.8; }
        }

        .email-alert-content h3 {
            margin: 0 0 0.25rem 0;
            font-size: 1rem;
        }

        .email-alert-content p {
            margin: 0;
            opacity: 0.9;
            line-height: 1.4;
            font-size: 0.85rem;
        }

        .email-alert-actions {
            padding: 0 1rem 1rem 1rem;
            display: flex;
            gap: 0.5rem;
            flex-wrap: wrap;
        }

        .email-alert-btn {
            background: rgba(255, 255, 255, 0.2);
            color: white;
            border: 1px solid rgba(255, 255, 255, 0.3);
            padding: 0.4rem 0.8rem;
            border-radius: 15px;
            cursor: pointer;
            font-size: 0.75rem;
            transition: all 0.3s ease;
            text-decoration: none;
            display: inline-flex;
            align-items: center;
            gap: 0.4rem;
        }

        .email-alert-btn:hover {
            background: rgba(255, 255, 255, 0.3);
            transform: translateY(-1px);
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
        }

        .nav-notification-badge {
            background: #dc3545;
            color: white;
            border-radius: 10px;
            padding: 2px 6px;
            font-size: 0.7rem;
            font-weight: bold;
            margin-left: 8px;
            min-width: 18px;
            text-align: center;
            display: inline-block;
            animation: pulse-nav 2s infinite;
        }
        
        @keyframes pulse-nav {
            0%, 100% { transform: scale(1); }
            50% { transform: scale(1.1); }
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
        
        .alert-info {
            color: #0c5460;
            background-color: #d1ecf1;
            border-color: #bee5eb;
        }
        
        .alert-error {
            color: #721c24;
            background-color: #f8d7da;
            border-color: #f5c6cb;
        }

        .alert-warning {
            color: #856404;
            background-color: #fff3cd;
            border-color: #ffeeba;
        }

        @media (max-width: 600px) {
            .profile-item {
                flex-direction: column;
                align-items: flex-start;
            }
            
            .email-alert-actions {
                flex-direction: column;
            }
            
            .email-alert-btn {
                justify-content: center;
            }

            .balance-grid {
                grid-template-columns: 1fr;
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
                <img src="../imnsb_logowbg.png" alt="IMNSB Logo" class="sidebar-logo">
            </div>
            <h3>IMNSB Employee</h3><br>
        </div>
        <ul class="sidebar-menu">
            <li class="active"><a href="EmployeeDashboard.jsp"><i class="fas fa-user"></i> <span>Profile</span></a></li>
            <li><a href="/ELMS_3.0/LeaveApplicationController"><i class="fas fa-calendar-plus"></i> <span>Apply Leave</span></a></li>
            <li>
                <a href="<%= request.getContextPath() %>/leave-history">
                    <i class="fas fa-history"></i> <span>Leave History</span>
                    <% if (newUpdatesCount > 0) { %>
                    <span class="nav-notification-badge"><%= newUpdatesCount %></span>
                    <% } %>
                </a>
            </li>
            <li><a href="/ELMS_3.0/Employee/EmployeeSettings.jsp"><i class="fas fa-cog"></i> <span>Settings</span></a></li>
            <li class="logout"><a href="/ELMS_3.0/Employee/EmployeeLogin.jsp" id="logoutBtn"><i class="fas fa-sign-out-alt"></i> <span>Logout</span></a></li>
        </ul>
    </nav>

    <!-- Main Content -->
    <main class="main-content" id="mainContent">
        <header class="content-header">
            <div class="header-left">
                <button id="sidebarToggle" class="sidebar-toggle">
                    <i class="fas fa-bars"></i>
                </button>
                <h2>Profile</h2>
            </div>
            <div class="header-right">
                <span id="userName"><%= employee.getEmployeeName() %></span>
                <div class="user-avatar">
                    <%
                        String headerProfilePicPath = employee.getProfilePicturePath();
                        if (headerProfilePicPath != null && !headerProfilePicPath.isEmpty()) {
                    %>
                    <img src="/ELMS_3.0/<%= headerProfilePicPath %>" alt="User Avatar" id="headerAvatar">
                    <% } else { %>
                    <img src="/ELMS_3.0/defaultprofilepicture.jpg" alt="User Avatar" id="headerAvatar">
                    <% } %>
                </div>
            </div>
        </header>

        <div class="content-body">

            <!-- Flash Messages -->
            <c:if test="${not empty sessionScope.successMessage}">
                <div class="alert alert-success">
                    <i class="fas fa-check-circle"></i> ${sessionScope.successMessage}
                </div>
                <c:remove var="successMessage" scope="session" />
            </c:if>

            <c:if test="${not empty sessionScope.infoMessage}">
                <div class="alert alert-info">
                    <i class="fas fa-info-circle"></i> ${sessionScope.infoMessage}
                </div>
                <c:remove var="infoMessage" scope="session" />
            </c:if>

            <!-- Email Alert Notification -->
            <% if (newUpdatesCount > 0) { %>
            <div class="email-alert-section" id="emailNotificationAlert">
                <div class="email-alert-header">
                    <div class="email-alert-icon">
                        <i class="fas fa-envelope"></i>
                    </div>
                    <div class="email-alert-content">
                        <h3>Leave Status Update<%= newUpdatesCount > 1 ? "s" : "" %> Received</h3>
                        <p>
                            <%= newUpdatesCount %> new update<%= newUpdatesCount > 1 ? "s" : "" %> available. Check your email for details.
                        </p>
                    </div>
                </div>
                <div class="email-alert-actions">
                    <a href="<%= request.getContextPath() %>/leave-history" class="email-alert-btn">
                        <i class="fas fa-history"></i> View History
                    </a>
                    <button onclick="markAllAsViewedFromDashboard()" class="email-alert-btn">
                        <i class="fas fa-check"></i> Mark Viewed
                    </button>
                    <button onclick="dismissEmailAlert()" class="email-alert-btn">
                        <i class="fas fa-times"></i> Dismiss
                    </button>
                </div>
            </div>
            <% } %>

            <!-- Profile Card -->
            <div class="profile-container">
               <div class="profile-header">
			    <div style="display: flex; justify-content: center; margin-bottom: 1rem;">
			        <div class="profile-avatar">
			            <%
			                String profilePicPath = employee.getProfilePicturePath();
			                if (profilePicPath != null && !profilePicPath.isEmpty()) {
			            %>
			            <img src="/ELMS_3.0/<%= profilePicPath %>" alt="Profile Picture">
			            <% } else { %>
			            <img src="/ELMS_3.0/defaultprofilepicture.jpg" alt="Profile Picture">
			            <% } %>
			        </div>
			    </div>
		    <h2 style="text-align: center;"><%= employee.getEmployeeName() %></h2>
		</div>

                <div class="profile-details">
                    <div class="profile-item">
                        <span class="profile-label">Employee ID:</span>
                        <span class="profile-value"><%= employee.getEmployeeId() %></span>
                    </div>
                    <div class="profile-item">
                        <span class="profile-label">Email:</span>
                        <span class="profile-value"><%= employee.getEmployeeEmail() %></span>
                    </div>
                    <div class="profile-item">
                        <span class="profile-label">Mobile:</span>
                        <span class="profile-value"><%= employee.getEmployeeNoPhone() %></span>
                    </div>
                </div>

               <div class="leave-balance-section">
			    <i class="fas fa-calendar-check main-icon"></i>
			    <h4>Leave Balance Overview</h4>
			    
			    <% if (leaveBalances != null && !leaveBalances.isEmpty()) { %>
			        <div class="balance-grid">
			            <% for (Map<String, Object> balance : leaveBalances) { 
			                String leaveTypeName = (String) balance.get("leaveTypeName");
			                String category = (String) balance.get("category");
			                double standardDuration = ((Number) balance.get("standardDuration")).doubleValue();
			                double usedDays = (Double) balance.get("usedDays");
			                double remainingDays = (Double) balance.get("remainingDays");
			                String remainingUnit = (remainingDays == 1) ? "day" : "days";
			            %>
			            <div class="balance-card">
			                <h5><%= leaveTypeName %></h5>
			                <div class="balance-display">
			                    <span class="balance-value"><%= String.format("%.1f", remainingDays) %></span>
			                    <span class="balance-total">/ <%= String.format("%.1f", standardDuration) %></span>
			                </div>
			                <div class="balance-unit"><%= remainingUnit %> remaining</div>
			                <div class="balance-breakdown">
			                    <div>Total: <strong><%= String.format("%.0f", standardDuration) %> days</strong></div>
			                    <div class="used-days">Used: <%= String.format("%.1f", usedDays) %> days</div>
			                    
			                </div>
			            </div>
			            <% } %>
			        </div>
			        
			        <div class="balance-info">
			            <p>
			                <i class="fas fa-info-circle"></i> <strong>Balance Information:</strong> 
			                Your leave balance will be deducted when a manager approves your application for these leave types.
			            </p>
			            <p>
			                Other leave types (e.g., medical leave, emergency leave) do not affect these balances.
			            </p>
			        </div>
			    <% } else { %>
			        <p style="text-align: center; color: #2f855a; font-size: 1rem; margin: 1rem 0;">
			            <i class="fas fa-info-circle"></i> No leave types currently track balances.
			        </p>
			        <div class="balance-info">
			            <p>
			                All available leave types do not deduct from a balance allocation.
			            </p>
			        </div>
			    <% } %>
			</div>
            </div>
        </div>
    </main>
</div>

<script src="/ELMS_3.0/Employee/Employee.js"></script>
<script>
    function goToLeaveHistory() {
        window.location.href = '<%= request.getContextPath() %>/leave-history';
    }
    
    function markAllAsViewedFromDashboard() {
        fetch('<%= request.getContextPath() %>/mark-applications-viewed', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            },
            body: 'action=markAllViewed'
        })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                const alert = document.getElementById('emailNotificationAlert');
                if (alert) {
                    alert.style.animation = 'fadeOut 0.5s ease-out';
                    setTimeout(() => {
                        alert.remove();
                        const badge = document.querySelector('.nav-notification-badge');
                        if (badge) {
                            badge.style.display = 'none';
                        }
                    }, 500);
                }
                showTempMessage('All notifications marked as viewed!', 'success');
            }
        })
        .catch(error => {
            console.error('Error marking applications as viewed:', error);
            showTempMessage('Error updating notifications. Please try again.', 'error');
        });
    }
    
    function dismissEmailAlert() {
        const alert = document.getElementById('emailNotificationAlert');
        if (alert) {
            alert.style.animation = 'fadeOut 0.5s ease-out';
            setTimeout(() => {
                alert.remove();
            }, 500);
        }
    }
    
    function showTempMessage(message, type) {
        const alertDiv = document.createElement('div');
        alertDiv.className = 'alert alert-' + type;
        alertDiv.innerHTML = '<i class="fas fa-' + (type === 'success' ? 'check' : 'exclamation') + '-circle"></i> ' + message;
        
        const contentBody = document.querySelector('.content-body');
        contentBody.insertBefore(alertDiv, contentBody.firstChild);
        
        setTimeout(() => {
            alertDiv.style.transition = 'opacity 0.5s ease';
            alertDiv.style.opacity = '0';
            setTimeout(() => {
                if (alertDiv.parentNode) {
                    alertDiv.parentNode.removeChild(alertDiv);
                }
            }, 500);
        }, 3000);
    }
    
    const style = document.createElement('style');
    style.textContent = `
        @keyframes fadeOut {
            from { opacity: 1; transform: translateY(0); }
            to { opacity: 0; transform: translateY(-20px); }
        }
    `;
    document.head.appendChild(style);
    
    document.addEventListener('DOMContentLoaded', function() {
        const newUpdatesCount = <%= newUpdatesCount %>;
        console.log('Dashboard loaded with', newUpdatesCount, 'new email notifications');
        
        if (newUpdatesCount > 0) {
            console.log('Employee should check their email for detailed leave status updates');
        }
        
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
</script>
</body>
</html>
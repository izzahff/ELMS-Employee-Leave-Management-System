<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page session="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<%
    // Check if user is logged in and is admin
    if (session.getAttribute("adminId") == null) {
        response.sendRedirect("/ELMS_3.0/Admin/AdminLogin.jsp");
        return;
    }
    
    // Get admin information from session
    String adminId = (String) session.getAttribute("adminId");
    String adminName = (String) session.getAttribute("adminName");
    String adminEmail = (String) session.getAttribute("adminEmail");
    String adminPhone = (String) session.getAttribute("adminPhone");
    
    // Set default values if session attributes are null
    if (adminName == null) adminName = "Admin User";
    if (adminEmail == null) adminEmail = "admin@imnsb.com";
    if (adminPhone == null) adminPhone = "+1234567890";
    if (adminId == null) adminId = "1234";
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Profile - IMNSB Employee Leave Management System</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="icon" href="/ELMS_3.0/imnsb_logo.png">
    <link rel="stylesheet" href="/ELMS_3.0/css/styles.css">
    <link rel="stylesheet" href="/ELMS_3.0/css/dashboard.css">
    <link rel="stylesheet" href="/ELMS_3.0/css/admin.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">

    <style>

.admin-officers-header {
    background: linear-gradient(135deg, var(--primary-color), var(--primary-dark)); /* Blue gradient like Employee List */
    color: white;
    padding: 1.5rem;
    display: flex;
    justify-content: space-between;
    align-items: center;
    flex-wrap: wrap;
    gap: 1rem;
}

.admin-officers-header h3 {
    margin: 0;
    font-size: 1.2rem;
    display: flex;
    align-items: center;
    gap: 0.5rem;
}

.add-admin-btn {
    background: rgba(255, 255, 255, 0.2); /* Keep the semi-transparent white for contrast */
    color: white;
    border: 1px solid rgba(255, 255, 255, 0.3);
    padding: 0.5rem 1rem;
    border-radius: 6px;
    text-decoration: none;
    font-size: 0.9rem;
    display: inline-flex;
    align-items: center;
    gap: 0.5rem;
    transition: all 0.3s ease;
    font-weight: 500;
    backdrop-filter: blur(10px);
}

.add-admin-btn:hover {
    background: rgba(255, 255, 255, 0.3);
    color: white;
    border-color: rgba(255, 255, 255, 0.4);
    transform: translateY(-1px);
    box-shadow: 0 2px 8px rgba(255, 255, 255, 0.15);
    text-decoration: none;
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

.admin-officers-section {
    margin-top: 2rem;
    background-color: white;
    border-radius: 12px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
    overflow: hidden;
    max-width: 800px;
    margin-left: auto;
    margin-right: auto;
}



.admin-officers-list {
    padding: 1.5rem;
    max-height: 400px;
    overflow-y: auto;
}

.admin-officer-card {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 1rem;
    border: 1px solid #e9ecef;
    border-radius: 8px;
    margin-bottom: 1rem;
    transition: all 0.3s ease;
}

.admin-officer-info {
    display: flex;
    align-items: center;
    gap: 1rem;
}

.admin-officer-avatar {
    width: 50px;
    height: 50px;
}

.admin-officer-avatar img {
    width: 100%;
    height: 100%;
    border-radius: 50%;
    object-fit: cover;
    border: 2px solid #dee2e6;
}

/* Navigation Notification Badge */
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
</style>
</head>
<body>
<div class="dashboard-container">

    <!-- Admin Sidebar -->
    <nav class="sidebar" id="sidebar">
        <div class="sidebar-header">
            <div class="logo-container">
                <img src="/ELMS_3.0/imnsb_logowbg.png" alt="IMNSB Logo" class="sidebar-logo">
            </div>
            <h3>IMNSB Admin</h3><br>
        </div>
        <ul class="sidebar-menu">
            <li class="active">
                <a href="<%= request.getContextPath() %>/admin-dashboard"><i class="fas fa-user"></i> <span>Admin Officer</span></a>
            </li>
            <li>
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
                <h2>Admin Officer</h2>
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

            <c:if test="${not empty sessionScope.errorMessage}">
                <div class="alert alert-error">
                    <i class="fas fa-exclamation-circle"></i> ${sessionScope.errorMessage}
                </div>
                <c:remove var="errorMessage" scope="session" />
            </c:if>

            <!-- Profile Card -->
            <div class="profile-container">
               <div class="profile-header">
                <div style="display: flex; justify-content: center; margin-bottom: 1rem;">
                    <div class="profile-avatar">
                        <%
                            String adminProfilePicPath = (String) session.getAttribute("adminProfilePicturePath");
                            if (adminProfilePicPath != null && !adminProfilePicPath.isEmpty()) {
                        %>
                        <img src="/ELMS_3.0/<%= adminProfilePicPath %>" alt="Admin Profile Picture">
                        <% } else { %>
                        <img src="${pageContext.request.contextPath}/defaultprofilepicture.jpg" alt="Admin Profile Picture">
                        <% } %>
                    </div>
                </div>
                    <h2 style="text-align: center;"><%= adminName %></h2>
                </div>

                <div class="profile-details">
                    <div class="profile-item">
                        <span class="profile-label">Admin ID:</span>
                        <span class="profile-value"><%= adminId %></span>
                    </div>
                    <div class="profile-item">
                        <span class="profile-label">Email:</span>
                        <span class="profile-value"><%= adminEmail %></span>
                    </div>
                    <div class="profile-item">
                        <span class="profile-label">Mobile:</span>
                        <span class="profile-value"><%= adminPhone %></span>
                    </div>
                    <div class="profile-item">
                        <span class="profile-label">Role:</span>
                        <span class="profile-value">System Administrator</span>
                    </div>
                </div>

                </div>

                <!-- Admin Officers Section -->
                <div class="admin-officers-section">
                    <div class="admin-officers-header">
                        <h3><i class="fas fa-users-cog"></i> Other Admin Officers</h3>
                        <a href="/ELMS_3.0/Admin/AdminAddAdmin.jsp" class="btn btn-primary add-admin-btn">
                            <i class="fas fa-plus"></i> Add New Admin
                        </a>
                    </div>
                    
                    <div class="admin-officers-list">
                        <c:choose>
                            <c:when test="${not empty adminOfficers}">
                                <c:forEach var="admin" items="${adminOfficers}">
                                    <div class="admin-officer-card">
                                        <div class="admin-officer-info">
                                            <div class="admin-officer-avatar">
                                                <c:choose>
                                                    <c:when test="${not empty admin.profilePicturePath}">
                                                        <img src="/ELMS_3.0/${admin.profilePicturePath}" alt="Admin Avatar" 
                                                             onerror="this.src='${pageContext.request.contextPath}/defaultprofilepicture.jpg';">
                                                    </c:when>
                                                    <c:otherwise>
                                                        <img src="${pageContext.request.contextPath}/defaultprofilepicture.jpg" alt="Admin Avatar">
                                                    </c:otherwise>
                                                </c:choose>
                                            </div>
                                            <div class="admin-officer-details">
                                                <h4>${admin.name}</h4>
                                                <p class="admin-officer-id">ID: ${admin.adminId}</p>
                                                <p class="admin-officer-email">${admin.email}</p>
                                                <c:if test="${not empty admin.phone}">
                                                    <p class="admin-officer-phone"><i class="fas fa-phone"></i> ${admin.phone}</p>
                                                </c:if>
                                                <c:if test="${empty admin.phone}">
                                                    <p class="admin-officer-phone"><i class="fas fa-phone"></i> No phone</p>
                                                </c:if>
                                            </div>
                                        </div>
                                        <div class="admin-officer-status">
                                            <!-- Removed status badge - no active/inactive display -->
                                        </div>
                                    </div>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <div class="no-admin-officers">
                                    <i class="fas fa-user-plus"></i>
                                    <p>No other admin officers found</p>
                                    <p style="font-size: 0.9rem; color: #666;">Click "Add New Admin" to create additional admin accounts</p>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>
        </div>
    </main>
</div>

<script src="/ELMS_3.0/Admin/Admin.js"></script>

</body>
</html>
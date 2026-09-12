<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="elms.model.Manager" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%
    Manager manager = (Manager) session.getAttribute("manager");
    if (manager == null) {
        response.sendRedirect("/ELMS_3.0/Manager/ManagerLogin.jsp");
        return;
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Executive Director Profile - IMNSB Employee Leave Management System</title>
    <link rel="icon" href="/ELMS_3.0/imnsb_logo.png">
    <link rel="stylesheet" href="/ELMS_3.0/css/styles.css">
    <link rel="stylesheet" href="/ELMS_3.0/css/dashboard.css">
    <link rel="stylesheet" href="/ELMS_3.0/css/manager.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">

    
</head>
<body>
<div class="dashboard-container">

    <!-- Sidebar -->
    <nav class="sidebar" id="sidebar">
        <div class="sidebar-header">
            <div class="logo-container">
                <img src="../imnsb_logowbg.png" alt="IMNSB Logo" class="sidebar-logo">
            </div>
            <h3>IMNSB Executive Director</h3><br>
        </div>
        <ul class="sidebar-menu">
            <li class="active"><a href="/ELMS_3.0/Manager/ExecutiveDirectorDashboard.jsp"><i class="fas fa-user"></i> <span>Profile</span></a></li>
            <li><a href="<%= request.getContextPath() %>/executive-director-project-managers"><i class="fas fa-users"></i> <span>Project Managers</span></a></li>
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
                <h2>Executive Director Profile</h2>
            </div>
            <div class="header-right">
                <span id="userName"><%= manager.getManagername() %></span>
                <div class="user-avatar">
                    <%
                        String headerProfilePicPath = manager.getProfilePicturePath();
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

        
          

            <!-- Manager Profile Card -->
            <div class="profile-container">
                <div class="profile-header">
                    <div style="display: flex; justify-content: center; margin-bottom: 1rem;">
                        <div class="profile-avatar">
                            <%
                                String profilePicPath = manager.getProfilePicturePath();
                                if (profilePicPath != null && !profilePicPath.isEmpty()) {
                            %>
                            <img src="/ELMS_3.0/<%= profilePicPath %>" alt="Profile Picture">
                            <% } else { %>
                            <img src="/ELMS_3.0/defaultprofilepicture.jpg" alt="Profile Picture">
                            <% } %>
                        </div>
                    </div>
                    <h2 style="text-align: center;"><%= manager.getManagername() %></h2>
                    <div style="text-align: center; margin-top: 0.5rem;">
                        <span class="position-badge"><%= manager.getManagerposition() %></span>
                    </div>
                </div>

                <div class="profile-details">
                    <div class="profile-item">
                        <span class="profile-label">Manager ID:</span>
                        <span class="profile-value"><%= manager.getManagerid() %></span>
                    </div>
                    <div class="profile-item">
                        <span class="profile-label">Email:</span>
                        <span class="profile-value"><%= manager.getManageremail() %></span>
                    </div>
                    <div class="profile-item">
                        <span class="profile-label">Mobile:</span>
                        <span class="profile-value"><%= manager.getManagernophone() %></span>
                    </div>
                    <div class="profile-item">
                        <span class="profile-label">Position:</span>
                        <span class="profile-value"><%= manager.getManagerposition() %></span>
                    </div>
                </div>
            </div>

            <!-- Welcome Message -->
            <div class="recent-applications">
                <h3><i class="fas fa-info-circle"></i> Getting Started</h3>
                <p>Use the navigation menu to access different sections of the executive director portal. You can review pending leave applications, manage project managers, and view all submitted applications from the sidebar.</p>
            </div>
        </div>
    </main>
</div>

<script src="/ELMS_3.0/Manager/Manager.js"></script>
   
</body>
</html>
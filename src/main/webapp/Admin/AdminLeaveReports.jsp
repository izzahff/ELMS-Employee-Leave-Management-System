<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%
    // Check if admin is logged in
    String adminId = (String) session.getAttribute("adminId");
    String adminName = (String) session.getAttribute("adminName");
    
    if (adminId == null) {
        response.sendRedirect(request.getContextPath() + "/Admin/AdminLogin.jsp");
        return;
    }
    
    // Set default value if adminName is null
    if (adminName == null || adminName.trim().isEmpty()) {
        adminName = "Admin User";
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link rel="icon" type="image/png" href="<%= request.getContextPath() %>/imnsb_logo.png">
    <title>Leave Application Reports - IMNSB Admin Panel</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/styles.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/admin.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/dashboard.css">
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/leavereport.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
   
</head>
<body>
    <div class="dashboard-container">
        <!-- Admin Sidebar -->
        <nav class="sidebar" id="sidebar">
            <div class="sidebar-header">
                <div class="logo-container">
                    <img src="${pageContext.request.contextPath}/imnsb_logowbg.png" alt="IMNSB Logo" class="sidebar-logo">
                </div>
                <h3>IMNSB Admin</h3>
            </div>
            <ul class="sidebar-menu">
                <li>
                    <a href="${pageContext.request.contextPath}/admin-dashboard"><i class="fas fa-user"></i> <span>Admin Officer</span></a>
                </li>
                <li>
                    <a href="${pageContext.request.contextPath}/Admin/AdminViewManagerListController"><i class="fas fa-user-tie"></i> <span>Approval Managers</span></a>
                </li>
                <li>
                    <a href="${pageContext.request.contextPath}/Admin/AdminViewEmployeeListController"><i class="fas fa-users"></i> <span>Employees</span></a>
                </li>
                <li>
                    <a href="${pageContext.request.contextPath}/AdminLeaveTypeListController"><i class="fas fa-list-alt"></i> <span>Leave Types</span></a>
                </li>
                <li>
                    <a href="${pageContext.request.contextPath}/admin-leave-requests"><i class="fas fa-clipboard-list"></i> <span>Leave Requests</span></a>
                </li>
                <li class="active">
                    <a href="${pageContext.request.contextPath}/admin-leave-reports"><i class="fas fa-chart-bar"></i> <span>View Report</span></a>
                </li>
                <li>
                    <a href="${pageContext.request.contextPath}/Admin/AdminSettings.jsp"><i class="fas fa-cog"></i> <span>Settings</span></a>
                </li>
                <li class="logout">
                    <a href="${pageContext.request.contextPath}/Admin/AdminLogin.jsp" id="logoutBtn"><i class="fas fa-sign-out-alt"></i> <span>Logout</span></a>
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
                    <h2>Leave Application Reports</h2>
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
                <!-- Success/Info/Error Messages -->
                <c:if test="${not empty successMessage}">
                    <div class="alert alert-success">
                        <i class="fas fa-check-circle"></i> ${successMessage}
                    </div>
                </c:if>
                
                <c:if test="${not empty errorMessage}">
                    <div class="alert alert-danger">
                        <i class="fas fa-exclamation-circle"></i> ${errorMessage}
                    </div>
                </c:if>
                
                <!-- Summary Statistics -->
                <div class="summary-grid">
                    <div class="summary-card total">
                        <div class="summary-number">${totalApplications != null ? totalApplications : 0}</div>
                        <div class="summary-label">Total Applications</div>
                        <div class="summary-description">All leave applications in system</div>
                    </div>
                    <div class="summary-card pending">
                        <div class="summary-number">${pendingApplications != null ? pendingApplications : 0}</div>
                        <div class="summary-label">Pending Reviews</div>
                        <div class="summary-description">Awaiting manager approval</div>
                    </div>
                    <div class="summary-card approved">
                        <div class="summary-number">${approvedApplications != null ? approvedApplications : 0}</div>
                        <div class="summary-label">Approved</div>
                        <div class="summary-description">Successfully approved leaves</div>
                    </div>
                    <div class="summary-card rejected">
                        <div class="summary-number">${rejectedApplications != null ? rejectedApplications : 0}</div>
                        <div class="summary-label">Rejected</div>
                        <div class="summary-description">Declined leave requests</div>
                    </div>
                    <div class="summary-card cancelled">
                        <div class="summary-number">${cancelledApplications != null ? cancelledApplications : 0}</div>
                        <div class="summary-label">Cancelled</div>
                        <div class="summary-description">User cancelled requests</div>
                    </div>
                </div>
                
                <div class="reports-container">
                   <!-- Report Filters Section - Modified -->
				<div class="report-filters-section">
				    <div class="report-filters-header">
				        <i class="fas fa-filter"></i>
				        <h3>Report Filters & Settings</h3>
				    </div>
				    <div class="report-filters-content">
				    
				        <c:if test="${not empty currentReportId}">
				            <div class="alert alert-info" style="margin-bottom: 15px; padding: 12px 16px; border-radius: 6px; background-color: #e3f2fd; border-color: #bbdefb; color: #1976d2;">
				                <div style="display: flex; justify-content: space-between; align-items: center;">
				                    <div>
				                        <strong><i class="fas fa-file-alt"></i> Current Report:</strong> 
				                        <span style="font-family: monospace; background: rgba(255,255,255,0.7); padding: 2px 6px; border-radius: 3px;">
				                            ${currentReportId}
				                        </span>
				                    </div>
				                    <div style="font-size: 0.9em; color: #666;">
				                        <i class="fas fa-clock"></i> Generated: <span id="currentTime"></span>
				                    </div>
				                </div>
				            </div>
				        </c:if>
				        
				        <c:if test="${empty currentReportId}">
				            <div class="alert alert-warning" style="margin-bottom: 15px; padding: 12px 16px; border-radius: 6px; background-color: #fff3cd; border-color: #ffeaa7; color: #856404;">
				                <div style="display: flex; align-items: center; gap: 10px;">
				                    <i class="fas fa-info-circle"></i>
				                    <span>No report generated yet. Configure your filters and click "Generate Report" to create a new report.</span>
				                </div>
				            </div>
				        </c:if>
				        
				        <form id="reportForm" method="GET" action="${pageContext.request.contextPath}/admin-leave-reports">
				            <!-- Hidden field to track when Generate Report button is clicked -->
				            <input type="hidden" id="generateFlag" name="generate" value="false">
				            
				            <div class="filter-row">
				                <div class="filter-group">
				                    <label for="reportType">Report Type</label>
				                    <select id="reportType" name="reportType">
				                        <option value="summary" ${selectedReportType == 'summary' ? 'selected' : ''}>Summary Report</option>
				                        <option value="detailed" ${selectedReportType == 'detailed' ? 'selected' : ''}>Detailed Report</option>
				                        <option value="trends" ${selectedReportType == 'trends' ? 'selected' : ''}>Trend Analysis</option>
				                    </select>
				                </div>
				                
				                <div class="filter-group">
				                    <label for="dateRange">Date Range</label>
				                    <select id="dateRange" name="dateRange">
				                        <option value="" ${empty selectedDateRange ? 'selected' : ''}>All Time</option>
				                        <option value="last30" ${selectedDateRange == 'last30' ? 'selected' : ''}>Last 30 Days</option>
				                        <option value="last90" ${selectedDateRange == 'last90' ? 'selected' : ''}>Last 90 Days</option>
				                        <option value="thisMonth" ${selectedDateRange == 'thisMonth' ? 'selected' : ''}>This Month</option>
				                        <option value="lastMonth" ${selectedDateRange == 'lastMonth' ? 'selected' : ''}>Last Month</option>
				                        <option value="thisQuarter" ${selectedDateRange == 'thisQuarter' ? 'selected' : ''}>This Quarter</option>
				                        <option value="thisYear" ${selectedDateRange == 'thisYear' ? 'selected' : ''}>This Year</option>
				                        <option value="custom" ${selectedDateRange == 'custom' ? 'selected' : ''}>Custom Range</option>
				                    </select>
				                </div>
				                
				                <div class="filter-group">
				                    <label for="fromDate">From Date</label>
				                    <input type="date" id="fromDate" name="fromDate" value="${selectedFromDate}">
				                </div>
				                
				                <div class="filter-group">
				                    <label for="toDate">To Date</label>
				                    <input type="date" id="toDate" name="toDate" value="${selectedToDate}">
				                </div>
				            </div>
				            
				            <div class="filter-row">
				                <div class="filter-group">
				                    <label for="statusFilter">Status Filter</label>
				                    <select id="statusFilter" name="status">
				                       <option value="">All Status</option>
				                        <option value="Pending" ${selectedStatus == 'Pending' ? 'selected' : ''}>Pending</option>
				                        <option value="Approved" ${selectedStatus == 'Approved' ? 'selected' : ''}>Approved</option>
				                        <option value="Rejected" ${selectedStatus == 'Rejected' ? 'selected' : ''}>Rejected</option>
				                        <option value="Cancelled" ${selectedStatus == 'Cancelled' ? 'selected' : ''}>Cancelled</option>
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
				                    <label for="employeeFilter">Employee</label>
				                    <input type="text" id="employeeFilter" name="employee" 
				                           placeholder="Search by name or ID..." 
				                           value="${selectedEmployee}">
				                </div>
				            </div>
				            
							<div class="filter-buttons">
							    <button type="button" id="clearFilters" class="btn btn-secondary">
							        <i class="fas fa-eraser"></i> Clear Filters
							    </button>
							    <button type="button" id="applyFiltersBtn" class="btn btn-primary" style="background: #17a2b8; border-color: #17a2b8;">
							        <i class="fas fa-filter"></i> Apply Filters
							    </button>
							    <button type="button" id="generateReportBtn" class="btn btn-primary">
							        <i class="fas fa-chart-bar"></i> Generate Report
							    </button>
							    <!-- Export buttons - ALWAYS show like before -->
							    <button type="button" id="exportCSV" class="btn btn-success">
							        <i class="fas fa-file-csv"></i> Export CSV
							        <c:if test="${not empty currentReportType}">
							            <span style="font-size: 0.8em; opacity: 0.8;">(${currentReportType.toUpperCase()})</span>
							        </c:if>
							    </button>
							    <button type="button" id="exportPDF" class="btn btn-info">
							        <i class="fas fa-file-pdf"></i> Export PDF
							        <c:if test="${not empty currentReportType}">
							            <span style="font-size: 0.8em; opacity: 0.8;">(${currentReportType.toUpperCase()})</span>
							        </c:if>
							    </button>
							</div>
				        </form>
				    </div>
				</div>
                    
				<!-- Charts Section for Trend Analysis -->
				<c:if test="${selectedReportType == 'trends'}">
				    <div class="charts-section">
				        <div class="charts-header">
				            <h3><i class="fas fa-chart-bar"></i> Data Visualization</h3>
				        </div>
				        <div class="charts-content">
				            <c:choose>
				                <c:when test="${not empty chartData && totalApplications > 0}">
				                    <div class="chart-grid">
				                        <!-- Monthly Trends Chart -->
				                        <div class="chart-container">
				                            <h4><i class="fas fa-calendar-alt"></i> Monthly Application Trends</h4>
				                            <canvas id="monthlyTrendsChart" class="chart-canvas"></canvas>
				                        </div>
				                        
				                        <!-- Status Distribution Chart -->
				                        <div class="chart-container">
				                            <h4><i class="fas fa-chart-pie"></i> Status Distribution</h4>
				                            <canvas id="statusDistributionChart" class="chart-canvas"></canvas>
				                        </div>
				                        
				                        <!-- Day of Week Trends Chart -->
				                        <div class="chart-container">
				                            <h4><i class="fas fa-chart-bar"></i> Day of Week Patterns</h4>
				                            <canvas id="dayOfWeekChart" class="chart-canvas"></canvas>
				                        </div>
				                        
				                        <!-- Leave Type Distribution Chart -->
				                        <div class="chart-container">
				                            <h4><i class="fas fa-list-alt"></i> Leave Type Distribution</h4>
				                            <canvas id="leaveTypeChart" class="chart-canvas"></canvas>
				                        </div>
				                    </div>
				                </c:when>
				                <c:otherwise>
				                    <!-- ✅ CONSISTENT EMPTY STATE -->
				                    <div style="text-align: center; padding: 60px 20px;">
				                        <i class="fas fa-chart-bar" style="font-size: 64px; color: #cbd5e0; margin-bottom: 20px;"></i>
				                        <h3 style="color: #495057; margin-bottom: 15px;">No Visualization Data Available</h3>
				                        <p style="color: #6c757d; margin-bottom: 25px; max-width: 500px; margin-left: auto; margin-right: auto;">
				                            No leave applications match your current filter criteria. Adjust your filters and click "Generate Report" to view charts.
				                        </p>
				                        <div style="background: #f8f9fa; border-left: 4px solid #17a2b8; padding: 15px; margin: 20px auto; max-width: 600px; text-align: left;">
				                            <p style="margin: 0; color: #495057; font-size: 0.95em;">
				                                <i class="fas fa-lightbulb" style="color: #ffc107;"></i> 
				                                <strong>Suggestions:</strong>
				                            </p>
				                            <ul style="margin: 10px 0 0 0; padding-left: 40px; color: #6c757d;">
				                                <li>Try expanding your date range to include more months</li>
				                                <li>Remove leave type filters to see overall trends</li>
				                                <li>Clear all filters to see all historical patterns</li>
				                            </ul>
				                        </div>
				                    </div>
				                </c:otherwise>
				            </c:choose>
				        </div>
				    </div>
				</c:if>
					                    
					 <!-- Summary Report Section -->
					<c:if test="${selectedReportType == 'summary' || empty selectedReportType}">
					    <!-- Summary Report Content -->
					    <div class="detailed-report-section">
					        <div class="detailed-report-header">
					            <h3><i class="fas fa-chart-pie"></i> Summary Report</h3>
					        </div>
					        <div class="detailed-report-content" style="padding: 20px;">
					            
					            <!-- ✅ CHECK IF THERE'S DATA FIRST -->
					            <c:choose>
					                <c:when test="${totalApplications > 0}">
					                    <!-- Leave Type Summary -->
					                    <c:if test="${not empty leaveTypeSummary}">
					                        <div class="summary-section" style="margin-bottom: 30px;">
					                            <h4><i class="fas fa-list-alt"></i> Leave Type Summary</h4>
					                            <div class="summary-table-container">
					                                <table class="report-table">
					                                    <thead>
					                                        <tr>
					                                            <th>Leave Type</th>
					                                            <th>Total Applications</th>
					                                            <th>Total Days</th>
					                                            <th>Pending</th>
					                                            <th>Approved</th>
					                                            <th>Rejected</th>
					                                            <th>Cancelled</th>
					                                        </tr>
					                                    </thead>
					                                    <tbody>
					                                        <c:forEach var="entry" items="${leaveTypeSummary}">
					                                            <tr>
					                                                <td><strong>${entry.key}</strong></td>
					                                                <td>${entry.value.totalApplications}</td>
					                                                <td><fmt:formatNumber value="${entry.value.totalDays}" maxFractionDigits="1"/> days</td>
					                                                <td><span class="status-badge status-pending">${entry.value.pending}</span></td>
					                                                <td><span class="status-badge status-approved">${entry.value.approved}</span></td>
					                                                <td><span class="status-badge status-rejected">${entry.value.rejected}</span></td>
					                                                <td><span class="status-badge status-cancelled">${entry.value.cancelled}</span></td>
					                                            </tr>
					                                        </c:forEach>
					                                    </tbody>
					                                </table>
					                            </div>
					                        </div>
					                    </c:if>
					                    
					                    <!-- Top Employees -->
					                    <c:if test="${not empty topEmployees}">
					                        <div class="summary-section" style="margin-bottom: 30px;">
					                            <h4><i class="fas fa-users"></i> Most Active Employees</h4>
					                            <div class="summary-table-container">
					                                <table class="report-table">
					                                    <thead>
					                                        <tr>
					                                            <th>Employee</th>
					                                            <th>Employee ID</th>
					                                            <th>Total Applications</th>
					                                            <th>Total Days</th>
					                                            <th>Pending</th>
					                                            <th>Approved</th>
					                                        </tr>
					                                    </thead>
					                                    <tbody>
					                                        <c:forEach var="entry" items="${topEmployees}">
					                                            <tr>
					                                                <td><strong>${entry.key}</strong></td>
					                                                <td>${entry.value.employeeId}</td>
					                                                <td>${entry.value.totalApplications}</td>
					                                                <td><fmt:formatNumber value="${entry.value.totalDays}" maxFractionDigits="1"/> days</td>
					                                                <td>${entry.value.pending}</td>
					                                                <td>${entry.value.approved}</td>
					                                            </tr>
					                                        </c:forEach>
					                                    </tbody>
					                                </table>
					                            </div>
					                        </div>
					                    </c:if>
					                </c:when>
					                <c:otherwise>
					                    <!-- ✅ EMPTY STATE FOR SUMMARY REPORT -->
					                    <div style="text-align: center; padding: 60px 20px;">
					                        <i class="fas fa-chart-pie" style="font-size: 64px; color: #cbd5e0; margin-bottom: 20px;"></i>
					                        <h3 style="color: #495057; margin-bottom: 15px;">No Summary Data Available</h3>
					                        <p style="color: #6c757d; margin-bottom: 25px; max-width: 500px; margin-left: auto; margin-right: auto;">
					                            No leave applications match your current filter criteria. Adjust your filters and click "Generate Report" to view summary statistics.
					                        </p>
					                        <div style="background: #f8f9fa; border-left: 4px solid #17a2b8; padding: 15px; margin: 20px auto; max-width: 600px; text-align: left;">
					                            <p style="margin: 0; color: #495057; font-size: 0.95em;">
					                                <i class="fas fa-lightbulb" style="color: #ffc107;"></i> 
					                                <strong>Suggestions:</strong>
					                            </p>
					                            <ul style="margin: 10px 0 0 0; padding-left: 40px; color: #6c757d;">
					                                <li>Try selecting "All Leave Types" or a different leave type</li>
					                                <li>Expand your date range to include more data</li>
					                                <li>Clear all filters to see all applications</li>
					                            </ul>
					                        </div>
					                    </div>
					                </c:otherwise>
					            </c:choose>
					        </div>
					    </div>
					</c:if>

                    <!-- Detailed Report Section -->
                    <c:if test="${selectedReportType == 'detailed'}">
                        <div class="detailed-report-section">
                            <div class="detailed-report-header">
                                <h3><i class="fas fa-table"></i> Detailed Report</h3>
                                <div>
                                    <select id="reportSort" class="btn btn-secondary" style="background: rgba(255,255,255,0.2); border: 1px solid rgba(255,255,255,0.3);">
                                        <option value="date">Sort by Date</option>
                                        <option value="status">Sort by Status</option>
                                        <option value="employee">Sort by Employee</option>
                                        <option value="duration">Sort by Duration</option>
                                    </select>
                                </div>
                            </div>
                            <div class="detailed-report-content">
                                
                                <!-- Detailed Analytics Summary -->
                                <c:if test="${not empty detailedAnalytics}">
                                    <div style="padding: 15px; background: #f8f9fa; border-bottom: 1px solid #dee2e6;">
                                        <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 15px;">
                                            <div>
                                                <strong>Urgent Applications:</strong> 
                                                <span class="status-badge" style="background: #fff3cd; color: #856404;">
                                                    ${detailedAnalytics.urgentApplications}
                                                </span>
                                            </div>
                                            <div>
                                                <strong>With Attachments:</strong> 
                                                <span class="status-badge" style="background: #d1ecf1; color: #0c5460;">
                                                    ${detailedAnalytics.applicationsWithAttachments}
                                                </span>
                                            </div>
                                            <div>
                                                <strong>Average Duration:</strong> 
                                                <span class="duration-badge">
                                                    <fmt:formatNumber value="${detailedAnalytics.averageLeaveDuration}" maxFractionDigits="1"/> days
                                                </span>
                                            </div>
                                        </div>
                                    </div>
                                </c:if>
                                
                                <c:choose>
                                    <c:when test="${not empty enhancedApplications}">
                                        <table class="report-table">
                                            <thead>
                                                <tr>
                                                    <th>Employee</th>
                                                    <th>Application ID</th>
                                                    <th>Leave Type</th>
                                                    
                                                    <th>Start Date</th>
                                                    <th>End Date</th>
                                                    <th>Duration</th>
                                                    <th>Business Days</th>
                                                    <th>Status</th>
                                                    <th>Processing Time</th>
                                                    <th>Applied On</th>
                                                    <th>Flags</th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                <c:forEach var="enhanced" items="${enhancedApplications}">
                                                    <c:set var="app" value="${enhanced.application}" />
                                                    <tr>
                                                        <td>
                                                            <div class="employee-info">
                                                                <div class="employee-details">
                                                                    <div class="employee-name">${enhanced.employeeName}</div>
                                                                    <div class="employee-id">${app.employeeid}</div>
                                                                </div>
                                                            </div>
                                                        </td>
                                                        <td><strong>${app.applicationid}</strong></td>
                                                        <td>
                                                            <span class="leave-type-tag">${enhanced.leaveTypeName}</span>
                                                        </td>
                                                       
                                                        <td>
                                                            <fmt:parseDate value="${app.leavestartdate}" pattern="yyyy-MM-dd" var="startDate"/>
                                                            <fmt:formatDate value="${startDate}" pattern="MMM dd, yyyy"/>
                                                        </td>
                                                        <td>
                                                            <fmt:parseDate value="${app.leaveenddate}" pattern="yyyy-MM-dd" var="endDate"/>
                                                            <fmt:formatDate value="${endDate}" pattern="MMM dd, yyyy"/>
                                                        </td>
                                                        <td>
                                                            <span class="duration-badge">${app.leaveduration} day(s)</span>
                                                        </td>
                                                        <td>
                                                            <span class="duration-badge">${enhanced.businessDays} days</span>
                                                        </td>
                                                        <td>
                                                            <c:choose>
                                                                <c:when test="${app.leavestatus == 'Pending'}">
                                                                    <span class="status-badge status-pending">Pending</span>
                                                                </c:when>
                                                                <c:when test="${app.leavestatus == 'Approved'}">
                                                                    <span class="status-badge status-approved">Approved</span>
                                                                </c:when>
                                                                <c:when test="${app.leavestatus == 'Rejected'}">
                                                                    <span class="status-badge status-rejected">Rejected</span>
                                                                </c:when>
                                                                <c:when test="${app.leavestatus == 'Cancelled'}">
                                                                    <span class="status-badge status-cancelled">Cancelled</span>
                                                                </c:when>
                                                                <c:otherwise>
                                                                    <span class="status-badge">${app.leavestatus}</span>
                                                                </c:otherwise>
                                                            </c:choose>
                                                        </td>
                                                        <td>
                                                            <c:choose>
                                                                <c:when test="${enhanced.processingTime > 0}">
                                                                    <fmt:formatNumber value="${enhanced.processingTime}" maxFractionDigits="1"/> days
                                                                </c:when>
                                                                <c:otherwise>-</c:otherwise>
                                                            </c:choose>
                                                        </td>
                                                        <td>
                                                            <fmt:parseDate value="${app.appliedon}" pattern="yyyy-MM-dd HH:mm:ss" var="appliedDate"/>
                                                            <fmt:formatDate value="${appliedDate}" pattern="MMM dd, yyyy"/>
                                                        </td>
                                                       <td>
													    <div style="display: flex; gap: 4px; flex-wrap: wrap;">
													        <c:if test="${enhanced.isUrgent}">
													            <span class="status-badge" style="background: #f8d7da; color: #721c24; font-size: 0.7rem;">
													                <i class="fas fa-exclamation-triangle"></i> Urgent
													            </span>
													        </c:if>
													        <c:if test="${not empty app.attachment}">
													            <span class="status-badge" style="background: #d1ecf1; color: #0c5460; font-size: 0.7rem;">
													                <i class="fas fa-paperclip"></i> File
													            </span>
													        </c:if>
													    </div>
													</td>
                                                    </tr>
                                                </c:forEach>
                                            </tbody>
                                        </table>
                                    </c:when>
                                    <c:otherwise>
								    <!-- ✅ CONSISTENT EMPTY STATE -->
								    <div style="text-align: center; padding: 60px 20px;">
								        <i class="fas fa-table" style="font-size: 64px; color: #cbd5e0; margin-bottom: 20px;"></i>
								        <h3 style="color: #495057; margin-bottom: 15px;">No Detailed Data Available</h3>
								        <p style="color: #6c757d; margin-bottom: 25px; max-width: 500px; margin-left: auto; margin-right: auto;">
								            No leave applications match your current filter criteria for detailed analysis.
								        </p>
								        <div style="background: #f8f9fa; border-left: 4px solid #17a2b8; padding: 15px; margin: 20px auto; max-width: 600px; text-align: left;">
								            <p style="margin: 0; color: #495057; font-size: 0.95em;">
								                <i class="fas fa-lightbulb" style="color: #ffc107;"></i> 
								                <strong>Suggestions:</strong>
								            </p>
								            <ul style="margin: 10px 0 0 0; padding-left: 40px; color: #6c757d;">
								                <li>Try selecting "All Status" to see all applications</li>
								                <li>Expand your date range to include more data</li>
								                <li>Clear all filters to see all applications</li>
								            </ul>
								        </div>
								    </div>
								</c:otherwise>
                                </c:choose>
                            </div>
                        </div>
                    </c:if>

                   <!-- Trend Analysis Section -->
					<c:if test="${selectedReportType == 'trends'}">
					    <div class="detailed-report-section">
					        <div class="detailed-report-header">
					            <h3><i class="fas fa-chart-line"></i> Trend Analysis</h3>
					        </div>
					        <div class="detailed-report-content" style="padding: 20px;">
					            
					            <!-- ✅ CHECK IF THERE'S DATA FIRST -->
					            <c:choose>
					                <c:when test="${totalApplications > 0}">
                   
                             
                              
                                
                                <!-- Monthly Trends -->
                                <c:if test="${not empty monthlyTrends}">
                                    <div class="trend-section" style="margin-bottom: 30px;">
                                        <h4><i class="fas fa-calendar-alt"></i> Monthly Trends</h4>
                                        <div class="summary-table-container">
                                            <table class="report-table">
                                                <thead>
                                                    <tr>
                                                        <th>Month</th>
                                                        <th>Total Applications</th>
                                                        <th>Pending</th>
                                                        <th>Approved</th>
                                                        <th>Rejected</th>
                                                        <th>Cancelled</th>
                                                    </tr>
                                                </thead>
                                                <tbody>
                                                    <c:forEach var="entry" items="${monthlyTrends}">
                                                        <tr>
                                                            <td><strong>${entry.key}</strong></td>
                                                            <td>${entry.value.total}</td>
                                                            <td><span class="status-badge status-pending">${entry.value.pending}</span></td>
                                                            <td><span class="status-badge status-approved">${entry.value.approved}</span></td>
                                                            <td><span class="status-badge status-rejected">${entry.value.rejected}</span></td>
                                                            <td><span class="status-badge status-cancelled">${entry.value.cancelled}</span></td>
                                                        </tr>
                                                    </c:forEach>
                                                </tbody>
                                            </table>
                                        </div>
                                    </div>
                                </c:if>
                                
                                <!-- Day of Week Trends -->
                                <c:if test="${not empty dayOfWeekTrends}">
                                    <div class="trend-section" style="margin-bottom: 30px;">
                                        <h4><i class="fas fa-chart-bar"></i> Day of Week Trends</h4>
                                        <div class="summary-table-container">
                                            <table class="report-table">
                                                <thead>
                                                    <tr>
                                                        <th>Day of Week</th>
                                                        <th>Applications</th>
                                                        <th>Percentage</th>
                                                    </tr>
                                                </thead>
                                                <tbody>
                                                    <c:set var="totalDayApplications" value="0" />
                                                    <c:forEach var="entry" items="${dayOfWeekTrends}">
                                                        <c:set var="totalDayApplications" value="${totalDayApplications + entry.value}" />
                                                    </c:forEach>
                                                    
                                                    <c:forEach var="entry" items="${dayOfWeekTrends}">
                                                        <tr>
                                                            <td><strong>${entry.key}</strong></td>
                                                            <td>${entry.value}</td>
                                                            <td>
                                                                <c:choose>
                                                                    <c:when test="${totalDayApplications > 0}">
                                                                        <fmt:formatNumber value="${(entry.value * 100) / totalDayApplications}" maxFractionDigits="1"/>%
                                                                    </c:when>
                                                                    <c:otherwise>0%</c:otherwise>
                                                                </c:choose>
                                                            </td>
                                                        </tr>
                                                    </c:forEach>
                                                </tbody>
                                            </table>
                                        </div>
                                    </div>
                                </c:if>
                                
                                <!-- Peak Analysis -->
                                <c:if test="${not empty peakAnalysis}">
                                    <div class="alert alert-info">
                                        <h5><i class="fas fa-chart-line"></i> Peak Period Analysis</h5>
                                        <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(250px, 1fr)); gap: 15px; margin-top: 15px;">
                                            <div>
                                                <strong>Peak Month:</strong><br>
                                                <span class="text-muted">
                                                    ${peakAnalysis.peakMonth} (${peakAnalysis.peakMonthApplications} applications)
                                                </span>
                                            </div>
                                            <div>
                                                <strong>Peak Day of Week:</strong><br>
                                                <span class="text-muted">
                                                    ${peakAnalysis.peakDay} (${peakAnalysis.peakDayApplications} applications)
                                                </span>
                                            </div>
                                           
                                            <div>
                                                <strong>Recommendations:</strong><br>
                                                <span class="text-muted">
                                                    <c:choose>
                                                        <c:when test="${trendIndicators.monthOverMonthGrowth > 10}">
                                                            Monitor capacity planning
                                                        </c:when>
                                                        <c:when test="${pendingApplications > 10}">
                                                            Review approval processes
                                                        </c:when>
                                                        <c:otherwise>
                                                            Continue current practices
                                                        </c:otherwise>
                                                    </c:choose>
                                                </span>
                                            </div>
                                        </div>
                                    </div>
                                </c:if>
                                
                                <!-- Leave Type Trends Over Time -->
                                <c:if test="${not empty leaveTypeTrends}">
                                    <div class="trend-section">
                                        <h4><i class="fas fa-chart-area"></i> Leave Type Trends Over Time</h4>
                                        <div class="summary-table-container">
                                            <table class="report-table">
                                                <thead>
                                                    <tr>
                                                        <th>Leave Type</th>
                                                        <th>Total Applications</th>
                                                        <th>Monthly Distribution</th>
                                                    </tr>
                                                </thead>
                                                <tbody>
                                                    <c:forEach var="entry" items="${leaveTypeTrends}">
                                                        <c:set var="typeTotal" value="0" />
                                                        <c:forEach var="monthEntry" items="${entry.value}">
                                                            <c:set var="typeTotal" value="${typeTotal + monthEntry.value}" />
                                                        </c:forEach>
                                                        
                                                        <tr>
                                                            <td><strong>${entry.key}</strong></td>
                                                            <td>${typeTotal}</td>
                                                            <td>
                                                                <div style="display: flex; gap: 5px; align-items: center;">
                                                                    <c:forEach var="monthEntry" items="${entry.value}" varStatus="status">
                                                                        <c:if test="${status.count <= 6}"> <!-- Show only last 6 months -->
                                                                            <div style="background: #e9ecef; border-radius: 3px; padding: 2px 4px; font-size: 0.7rem;">
                                                                                ${fn:substring(monthEntry.key, 5, 7)}: ${monthEntry.value}
                                                                            </div>
                                                                        </c:if>
                                                                    </c:forEach>
                                                                </div>
                                                            </td>
                                                        </tr>
                                                    </c:forEach>
                                                </tbody>
                                            </table>
                                        </div>
                                    </div>
                                </c:if>
                                </c:when>
                                <c:otherwise>
                                    <!-- ✅ EMPTY STATE FOR TRENDS -->
                                    <div style="text-align: center; padding: 60px 20px;">
                                        <i class="fas fa-chart-line" style="font-size: 64px; color: #cbd5e0; margin-bottom: 20px;"></i>
                                        <h3 style="color: #495057; margin-bottom: 15px;">No Trend Data Available</h3>
                                        <p style="color: #6c757d; margin-bottom: 25px; max-width: 500px; margin-left: auto; margin-right: auto;">
                                            No leave applications match your current filter criteria. Adjust your filters and click "Generate Report" to view trend analysis.
                                        </p>
                                        <div style="background: #f8f9fa; border-left: 4px solid #17a2b8; padding: 15px; margin: 20px auto; max-width: 600px; text-align: left;">
                                            <p style="margin: 0; color: #495057; font-size: 0.95em;">
                                                <i class="fas fa-lightbulb" style="color: #ffc107;"></i> 
                                                <strong>Suggestions:</strong>
                                            </p>
                                            <ul style="margin: 10px 0 0 0; padding-left: 40px; color: #6c757d;">
                                                <li>Try expanding your date range to include more months</li>
                                                <li>Remove leave type filters to see overall trends</li>
                                                <li>Clear all filters to see all historical patterns</li>
                                            </ul>
                                        </div>
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>
                </c:if>

                    <!-- Default Detailed Report Table -->
                    <c:if test="${selectedReportType != 'summary' && selectedReportType != 'detailed' && selectedReportType != 'trends'}">
                        <div class="detailed-report-section">
                            <div class="detailed-report-header">
                                <h3><i class="fas fa-table"></i> Leave Applications</h3>
                            </div>
                            <div class="detailed-report-content">
                                <c:choose>
                                    <c:when test="${not empty reportData}">
                                        <table class="report-table">
                                            <thead>
                                                <tr>
                                                    <th>Employee</th>
                                                    <th>Application ID</th>
                                                    <th>Leave Type</th>
                                                    <th>Start Date</th>
                                                    <th>End Date</th>
                                                    <th>Duration</th>
                                                    <th>Status</th>
                                                    <th>Applied On</th>
                                                    <th>Manager</th>
                                                </tr>
                                            </thead>
                                            <tbody>
                                                <c:forEach var="application" items="${reportData}">
                                                    <tr>
                                                        <td>
                                                            <div class="employee-info">
                                                                <div class="employee-details">
                                                                    <div class="employee-name">
                                                                        <c:choose>
                                                                            <c:when test="${not empty employeeNames[application.employeeid]}">
                                                                                ${employeeNames[application.employeeid]}
                                                                            </c:when>
                                                                            <c:otherwise>
                                                                                Unknown Employee
                                                                            </c:otherwise>
                                                                        </c:choose>
                                                                    </div>
                                                                    <div class="employee-id">${application.employeeid}</div>
                                                                </div>
                                                            </div>
                                                        </td>
                                                        <td><strong>${application.applicationid}</strong></td>
                                                        <td>
                                                            <span class="leave-type-tag">
                                                                <c:choose>
                                                                    <c:when test="${not empty leaveTypeNames[application.leavetypeid]}">
                                                                        ${leaveTypeNames[application.leavetypeid]}
                                                                    </c:when>
                                                                    <c:otherwise>
                                                                        Unknown Type
                                                                    </c:otherwise>
                                                                </c:choose>
                                                            </span>
                                                        </td>
                                                        <td>
                                                            <fmt:parseDate value="${application.leavestartdate}" pattern="yyyy-MM-dd" var="startDate"/>
                                                            <fmt:formatDate value="${startDate}" pattern="MMM dd, yyyy"/>
                                                        </td>
                                                        <td>
                                                            <fmt:parseDate value="${application.leaveenddate}" pattern="yyyy-MM-dd" var="endDate"/>
                                                            <fmt:formatDate value="${endDate}" pattern="MMM dd, yyyy"/>
                                                        </td>
                                                        <td>
                                                            <span class="duration-badge">${application.leaveduration} day(s)</span>
                                                        </td>
                                                        <td>
                                                            <c:choose>
                                                                <c:when test="${application.leavestatus == 'Pending'}">
                                                                    <span class="status-badge status-pending">Pending</span>
                                                                </c:when>
                                                                <c:when test="${application.leavestatus == 'Approved'}">
                                                                    <span class="status-badge status-approved">Approved</span>
                                                                </c:when>
                                                                <c:when test="${application.leavestatus == 'Rejected'}">
                                                                    <span class="status-badge status-rejected">Rejected</span>
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
                                                            <c:choose>
                                                                <c:when test="${not empty application.managerid}">
                                                                    ${application.managerid}
                                                                </c:when>
                                                                <c:otherwise>
                                                                    Not Assigned
                                                                </c:otherwise>
                                                            </c:choose>
                                                        </td>
                                                    </tr>
                                                </c:forEach>
                                            </tbody>
                                        </table>
                                    </c:when>
                                    <c:otherwise>
                                        <div class="empty-state">
                                            <i class="fas fa-chart-bar"></i>
                                            <h3>No Report Data Available</h3>
                                            <p>No leave applications match your current filter criteria.</p>
                                            <div class="alert alert-info" style="margin-top: 20px; text-align: left;">
                                                <strong>Suggestions:</strong>
                                                <ul style="margin: 10px 0; padding-left: 20px;">
                                                    <li>Try expanding your date range</li>
                                                    <li>Remove some filters to see more data</li>
                                                    <li>Check if employees have submitted leave applications</li>
                                                </ul>
                                            </div>
                                            <button onclick="clearAllFilters()" class="btn btn-primary" style="margin-top: 15px;">
                                                <i class="fas fa-eraser"></i> Clear All Filters
                                            </button>
                                        </div>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </div>
                    </c:if>
                    
<!-- Additional Insights Section - REAL DATA FOR ALL REPORT TYPES -->
<c:if test="${totalApplications > 0}">
    <div class="charts-section" style="margin-top: 20px;">
        <div class="charts-header">
            <h3><i class="fas fa-lightbulb"></i> Key Insights & Recommendations</h3>
        </div>
        <div class="charts-content">
            <div class="alert alert-info">
                <h5><i class="fas fa-info-circle"></i> Report Summary</h5>
                <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(250px, 1fr)); gap: 15px; margin-top: 15px;">
                    
                    <!-- Only show Average Processing Time if there's data -->
                    <c:if test="${avgProcessingTime > 0}">
                        <div>
                            <strong>Average Processing Time:</strong><br>
                            <span class="text-muted">
                                <fmt:formatNumber value="${avgProcessingTime}" maxFractionDigits="1"/> days for reviewed applications
                            </span>
                        </div>
                    </c:if>
                    
                    <!-- Only show Most Common Leave Type if NOT filtered by leave type -->
                    <c:if test="${not empty mostPopularLeaveType && !hasLeaveTypeFilter}">
                        <div>
                            <strong>Most Common Leave Type:</strong><br>
                            <span class="text-muted">
                                ${mostPopularLeaveType}
                            </span>
                        </div>
                    </c:if>
                    
                    <!-- Always show Approval Rate -->
                    <div>
                        <strong>Approval Rate:</strong><br>
                        <span class="text-muted">
                            <c:choose>
                                <c:when test="${totalApplications > 0 && approvedApplications > 0}">
                                    <fmt:formatNumber value="${(approvedApplications * 100) / totalApplications}" maxFractionDigits="1"/>% of applications approved
                                </c:when>
                                <c:when test="${totalApplications > 0 && approvedApplications == 0}">
                                    0% approved (${pendingApplications} pending)
                                </c:when>
                                <c:otherwise>
                                    No data available
                                </c:otherwise>
                            </c:choose>
                        </span>
                    </div>
                    
                    <!-- Show Total Days if available -->
                    <c:if test="${totalApplications > 0}">
                        <div>
                            <strong>Total Leave Days:</strong><br>
                            <span class="text-muted">
                                <c:set var="totalDays" value="0" />
                                <c:forEach var="app" items="${reportData}">
                                    <c:set var="totalDays" value="${totalDays + app.leaveduration}" />
                                </c:forEach>
                                <fmt:formatNumber value="${totalDays}" maxFractionDigits="1"/> days requested
                            </span>
                        </div>
                    </c:if>
                </div>
            </div>
            
            <div class="alert alert-warning">
                <h5><i class="fas fa-exclamation-triangle"></i> Action Items</h5>
                <ul style="margin: 10px 0; padding-left: 20px;">
                    <li>
                        <strong>Pending Applications:</strong> 
                        ${pendingApplications != null ? pendingApplications : 0} 
                        application<c:if test="${pendingApplications != 1}">s</c:if> require manager review
                    </li>
                    <c:if test="${pendingApplications > 10}">
                        <li><strong>High Pending Count:</strong> Consider reviewing approval processes to reduce backlog</li>
                    </c:if>
                    <c:if test="${avgProcessingTime > 5}">
                        <li><strong>Slow Processing:</strong> Average processing time is high - consider streamlining approval workflow</li>
                    </c:if>
                    <c:if test="${pendingApplications <= 5 && avgProcessingTime > 0 && avgProcessingTime <= 3}">
                        <li><strong>System Health:</strong> Leave management processes are operating efficiently</li>
                    </c:if>
                    <c:if test="${totalApplications > 0}">
                        <li><strong>Monitoring:</strong> Regularly review employee leave balances to ensure adequate coverage</li>
                    </c:if>
                </ul>
            </div>
        </div>
    </div>
</c:if>

    <script src="<%= request.getContextPath() %>/Admin/Admin.js"></script>
   <script>
	document.addEventListener('DOMContentLoaded', function() {
    console.log('Leave Application Reports page loaded successfully');
    
    populateLeaveTypeFilter();
    
    const sortSelect = document.getElementById('reportSort');
    if (sortSelect) {
        // Style the dropdown properly
        sortSelect.style.background = '#ffffff';
        sortSelect.style.color = '#333333';
        sortSelect.style.border = '1px solid #ced4da';
        sortSelect.style.borderRadius = '4px';
        sortSelect.style.padding = '6px 10px';
        sortSelect.style.fontSize = '13px';
        sortSelect.style.minWidth = '140px';
        
        sortSelect.addEventListener('change', function() {
            const sortBy = this.value;
            console.log('Sort changed to:', sortBy);
            sortTable(sortBy);
        });
    }
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
	    const currentSelection = '${selectedLeaveType}';
	    
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
	    
	    console.log('✅ Admin leave reports - leave type filter populated and sorted (' + leaveTypes.length + ' types)');
	}
	
    // Set current time
    const currentTimeElement = document.getElementById('currentTime');
    if (currentTimeElement) {
        const now = new Date();
        currentTimeElement.textContent = now.toLocaleString();
    }
    
    // Auto-hide alerts immediately when page loads
    autoHideAlerts();
    
    // Initialize charts if trend analysis is selected
    const reportType = '${selectedReportType}';
    if (reportType === 'trends') {
        initializeCharts();
    }
    
    // Date range selector functionality
    const dateRangeSelect = document.getElementById('dateRange');
    const fromDateInput = document.getElementById('fromDate');
    const toDateInput = document.getElementById('toDate');
    
    // Set initial values from server if available
    const selectedDateRange = '${selectedDateRange}' || '';
    if (selectedDateRange && selectedDateRange !== 'null') {
        dateRangeSelect.value = selectedDateRange;
    }
    
    // Set initial date values
    const selectedFromDate = '${selectedFromDate}';
    const selectedToDate = '${selectedToDate}';
    if (selectedFromDate && selectedFromDate !== 'null') {
        fromDateInput.value = selectedFromDate;
    }
    if (selectedToDate && selectedToDate !== 'null') {
        toDateInput.value = selectedToDate;
    }
    
    dateRangeSelect.addEventListener('change', function() {
        const selectedRange = this.value;
        const today = new Date();
        let fromDate, toDate;
        
        switch(selectedRange) {
            case 'last30':
                fromDate = new Date(today.getTime() - 30 * 24 * 60 * 60 * 1000);
                toDate = today;
                break;
            case 'last90':
                fromDate = new Date(today.getTime() - 90 * 24 * 60 * 60 * 1000);
                toDate = today;
                break;
            case 'thisMonth':
                fromDate = new Date(today.getFullYear(), today.getMonth(), 1);
                toDate = today;
                break;
            case 'lastMonth':
                fromDate = new Date(today.getFullYear(), today.getMonth() - 1, 1);
                toDate = new Date(today.getFullYear(), today.getMonth(), 0);
                break;
            case 'thisQuarter':
                const quarterMonth = Math.floor(today.getMonth() / 3) * 3;
                fromDate = new Date(today.getFullYear(), quarterMonth, 1);
                toDate = today;
                break;
            case 'thisYear':
                fromDate = new Date(today.getFullYear(), 0, 1);
                toDate = today;
                break;
            case 'custom':
                return;
            case '':
                fromDateInput.value = '';
                toDateInput.value = '';
                return;
        }
        
        if (fromDate && toDate) {
            fromDateInput.value = formatDate(fromDate);
            toDateInput.value = formatDate(toDate);
        }
    });
    
 // Manual date input change handlers
    fromDateInput.addEventListener('change', function() {
        // When user manually changes From Date, set Date Range to "custom"
        if (this.value) {
            dateRangeSelect.value = 'custom';
        }
    });

    toDateInput.addEventListener('change', function() {
        // When user manually changes To Date, set Date Range to "custom"
        if (this.value) {
            dateRangeSelect.value = 'custom';
        }
    });
    
    function formatDate(date) {
        return date.getFullYear() + '-' + 
               String(date.getMonth() + 1).padStart(2, '0') + '-' + 
               String(date.getDate()).padStart(2, '0');
    }
    
    // Generate Report Button
    const generateReportBtn = document.getElementById('generateReportBtn');
    if (generateReportBtn) {
        generateReportBtn.addEventListener('click', function() {
            document.getElementById('generateFlag').value = 'true';
            console.log('Generating new report...');
            document.getElementById('reportForm').submit();
        });
    }
    
 // Apply Filters Button (view data without generating new report)
    const applyFiltersBtn = document.getElementById('applyFiltersBtn');
    if (applyFiltersBtn) {
        applyFiltersBtn.addEventListener('click', function() {
            document.getElementById('generateFlag').value = 'false';
            console.log('Applying filters to view data...');
            document.getElementById('reportForm').submit();
        });
    }
    
    // Report type change handler
    const reportTypeSelect = document.getElementById('reportType');
    if (reportTypeSelect) {
        reportTypeSelect.addEventListener('change', function() {
            console.log('Report type changed to:', this.value);
            document.getElementById('generateFlag').value = 'false';
            document.getElementById('reportForm').submit();
        });
    }
    
    // Export functionality
    const exportCSVBtn = document.getElementById('exportCSV');
    const exportPDFBtn = document.getElementById('exportPDF');
    
    if (exportCSVBtn) {
        exportCSVBtn.addEventListener('click', function() {
            exportReport('csv');
        });
    }
    
    if (exportPDFBtn) {
        exportPDFBtn.addEventListener('click', function() {
            exportReport('pdf');
        });
    }
    
    // Clear filters functionality
    document.getElementById('clearFilters').addEventListener('click', function() {
        clearAllFilters();
    });
    
    function clearAllFilters() {
        document.getElementById('reportForm').reset();
        fromDateInput.value = '';
        toDateInput.value = '';
        document.getElementById('generateFlag').value = 'false';
        window.location.href = window.location.pathname;
    }
    
    // Make clearAllFilters global for the empty state button
    window.clearAllFilters = clearAllFilters;
    
    // Table sorting functionality
    const sortSelect = document.getElementById('reportSort');
    if (sortSelect) {
        sortSelect.addEventListener('change', function() {
            const sortBy = this.value;
            sortTable(sortBy);
        });
    }
    
    function sortTable(sortBy) {
        const table = document.querySelector('.report-table tbody');
        if (!table) {
            console.log('No table found for sorting');
            return;
        }
        
        const rows = Array.from(table.querySelectorAll('tr'));
        if (rows.length === 0) {
            console.log('No rows found for sorting');
            return;
        }
        
        console.log(`Sorting by: ${sortBy}, found ${rows.length} rows`);
        
        rows.sort((a, b) => {
            let aVal, bVal;
            
            try {
                switch(sortBy) {
                    case 'date':
                        // For detailed report, applied date is usually in column 9 (0-indexed)
                        // Check if it's detailed report (has more columns) or regular report
                        const isDetailedReport = a.cells.length > 10;
                        const dateColIndex = isDetailedReport ? 9 : 7; // Applied On column
                        
                        aVal = a.cells[dateColIndex] ? a.cells[dateColIndex].textContent.trim() : '';
                        bVal = b.cells[dateColIndex] ? b.cells[dateColIndex].textContent.trim() : '';
                        
                        // Parse dates - handle "MMM dd, yyyy" format
                        const dateA = parseDisplayDate(aVal);
                        const dateB = parseDisplayDate(bVal);
                        
                        return dateB - dateA; // Most recent first
                        
                    case 'status':
                        // Status column is at index 7 for detailed report, 6 for regular
                        const statusColIndex = a.cells.length > 10 ? 7 : 6;
                        aVal = a.cells[statusColIndex] ? a.cells[statusColIndex].textContent.trim() : '';
                        bVal = b.cells[statusColIndex] ? b.cells[statusColIndex].textContent.trim() : '';
                        
                        // Define status priority (Pending first, then others alphabetically)
                        const statusPriority = { 'Pending': 0, 'Approved': 1, 'Rejected': 2, 'Cancelled': 3 };
                        const priorityA = statusPriority[aVal] !== undefined ? statusPriority[aVal] : 4;
                        const priorityB = statusPriority[bVal] !== undefined ? statusPriority[bVal] : 4;
                        
                        return priorityA - priorityB;
                        
                    case 'employee':
                        // Employee name is in the first column
                        const empCellA = a.cells[0] ? a.cells[0].querySelector('.employee-name') : null;
                        const empCellB = b.cells[0] ? b.cells[0].querySelector('.employee-name') : null;
                        
                        aVal = empCellA ? empCellA.textContent.trim() : 
                               (a.cells[0] ? a.cells[0].textContent.trim() : '');
                        bVal = empCellB ? empCellB.textContent.trim() : 
                               (b.cells[0] ? b.cells[0].textContent.trim() : '');
                        
                        return aVal.localeCompare(bVal);
                        
                    case 'duration':
                        // Duration column - check for detailed vs regular report
                        let durationColIndex;
                        if (a.cells.length > 10) {
                            // Detailed report - Duration is at index 5, Business Days at index 6
                            durationColIndex = 5;
                        } else {
                            // Regular report - Duration is at index 5
                            durationColIndex = 5;
                        }
                        
                        const durationCellA = a.cells[durationColIndex];
                        const durationCellB = b.cells[durationColIndex];
                        
                        if (!durationCellA || !durationCellB) {
                            return 0;
                        }
                        
                        // Extract numeric value from text like "5 day(s)" or "5 days"
                        const durationTextA = durationCellA.textContent.trim();
                        const durationTextB = durationCellB.textContent.trim();
                        
                        const numA = parseFloat(durationTextA.match(/[\d.]+/)?.[0] || 0);
                        const numB = parseFloat(durationTextB.match(/[\d.]+/)?.[0] || 0);
                        
                        return numB - numA; // Longest duration first
                        
                    default:
                        return 0;
                }
            } catch (error) {
                console.error('Error sorting table:', error);
                return 0;
            }
        });
        
        // Clear and rebuild table
        table.innerHTML = '';
        rows.forEach(row => table.appendChild(row));
        
        console.log(`Table sorted by ${sortBy}`);
    }
    
   

// Fixed auto-hide alerts function
function autoHideAlerts() {
    const successAlert = document.querySelector('.alert-success');
    const errorAlert = document.querySelector('.alert-danger');
    const infoAlerts = document.querySelectorAll('.alert-info');
    
    if (successAlert) {
        console.log('Found success alert, hiding in 2 seconds...');
        setTimeout(() => {
            hideAlert(successAlert);
        }, 2000);
    }

    if (errorAlert) {
        console.log('Found error alert, hiding in 3 seconds...');
        setTimeout(() => {
            hideAlert(errorAlert);
        }, 3000);
    }

 // Only hide info alerts that are temporary messages (not permanent sections)
    infoAlerts.forEach(infoAlert => {
        if (infoAlert && 
            !infoAlert.textContent.includes('Key Insights') && 
            !infoAlert.textContent.includes('Current Report:') &&
            !infoAlert.textContent.includes('No report generated yet') &&
            !infoAlert.textContent.includes('Report Summary') &&
            !infoAlert.textContent.includes('Action Items') &&
            !infoAlert.textContent.includes('No Report Data Available') &&
            !infoAlert.textContent.includes('No Summary Data Available') &&
            !infoAlert.textContent.includes('Peak Period Analysis')) {  // ✅ ADD THIS LINE
            console.log('Found temporary info alert, hiding in 2 seconds...');
            setTimeout(() => {
                hideAlert(infoAlert);
            }, 2000);
        }
    });
}

function hideAlert(alert) {
    if (alert && alert.parentNode) {
        console.log('Hiding alert element');
        alert.style.transition = 'all 0.5s ease';
        alert.style.opacity = '0';
        alert.style.transform = 'translateY(-20px)';
        
        setTimeout(() => {
            if (alert.parentNode) {
                alert.parentNode.removeChild(alert);
                console.log('Alert removed from DOM');
            }
        }, 500);
    }
}

// Simplified export function
function exportReport(type) {
    const reportType = '${selectedReportType}' || 'summary';
    const reportId = '${currentReportId}' || '';
    
    console.log(`Exporting ${type.toUpperCase()} report for ${reportType.toUpperCase()} view...`);
    
    const form = document.createElement('form');
    form.method = 'POST';
    form.action = window.location.pathname;
    form.style.display = 'none';
    
    const actionInput = document.createElement('input');
    actionInput.type = 'hidden';
    actionInput.name = 'action';
    actionInput.value = 'export';
    form.appendChild(actionInput);
    
    const typeInput = document.createElement('input');
    typeInput.type = 'hidden';
    typeInput.name = 'exportType';
    typeInput.value = type;
    form.appendChild(typeInput);
    
    const reportTypeInput = document.createElement('input');
    reportTypeInput.type = 'hidden';
    reportTypeInput.name = 'reportTypeForExport';
    reportTypeInput.value = reportType;
    form.appendChild(reportTypeInput);
    
    if (reportId) {
        const reportIdInput = document.createElement('input');
        reportIdInput.type = 'hidden';
        reportIdInput.name = 'reportId';
        reportIdInput.value = reportId;
        form.appendChild(reportIdInput);
    }
    
    const reportForm = document.getElementById('reportForm');
    const formData = new FormData(reportForm);
    for (const [key, value] of formData) {
        if (value && key !== 'generate') {
            const input = document.createElement('input');
            input.type = 'hidden';
            input.name = key;
            input.value = value;
            form.appendChild(input);
        }
    }
    
    document.body.appendChild(form);
    form.submit();
    document.body.removeChild(form);
}

// Chart initialization function (keep as is)
function initializeCharts() {
    <c:if test="${not empty chartData}">
        const chartData = {
            monthlyLabels: [
                <c:forEach var="label" items="${chartData.monthlyLabels}" varStatus="status">
                    "${label}"<c:if test="${!status.last}">,</c:if>
                </c:forEach>
            ],
            monthlyValues: [
                <c:forEach var="value" items="${chartData.monthlyValues}" varStatus="status">
                    ${value}<c:if test="${!status.last}">,</c:if>
                </c:forEach>
            ],
            dayLabels: [
                <c:forEach var="label" items="${chartData.dayLabels}" varStatus="status">
                    "${label}"<c:if test="${!status.last}">,</c:if>
                </c:forEach>
            ],
            dayValues: [
                <c:forEach var="value" items="${chartData.dayValues}" varStatus="status">
                    ${value}<c:if test="${!status.last}">,</c:if>
                </c:forEach>
            ],
            statusLabels: [
                <c:forEach var="label" items="${chartData.statusLabels}" varStatus="status">
                    "${label}"<c:if test="${!status.last}">,</c:if>
                </c:forEach>
            ],
            statusValues: [
                <c:forEach var="value" items="${chartData.statusValues}" varStatus="status">
                    ${value}<c:if test="${!status.last}">,</c:if>
                </c:forEach>
            ],
            leaveTypeLabels: [
                <c:forEach var="label" items="${chartData.leaveTypeLabels}" varStatus="status">
                    "${label}"<c:if test="${!status.last}">,</c:if>
                </c:forEach>
            ],
            leaveTypeValues: [
                <c:forEach var="value" items="${chartData.leaveTypeValues}" varStatus="status">
                    ${value}<c:if test="${!status.last}">,</c:if>
                </c:forEach>
            ]
        };

        // Monthly Trends Chart
        const monthlyCtx = document.getElementById('monthlyTrendsChart');
        if (monthlyCtx && chartData.monthlyLabels.length > 0) {
            new Chart(monthlyCtx, {
                type: 'line',
                data: {
                    labels: chartData.monthlyLabels,
                    datasets: [{
                        label: 'Applications per Month',
                        data: chartData.monthlyValues,
                        borderColor: '#007bff',
                        backgroundColor: 'rgba(0, 123, 255, 0.1)',
                        borderWidth: 3,
                        fill: true,
                        tension: 0.4,
                        pointBackgroundColor: '#007bff',
                        pointBorderColor: '#ffffff',
                        pointBorderWidth: 2,
                        pointRadius: 5
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: { display: false },
                        tooltip: {
                            backgroundColor: 'rgba(0, 0, 0, 0.8)',
                            titleColor: '#ffffff',
                            bodyColor: '#ffffff',
                            borderColor: '#007bff',
                            borderWidth: 1
                        }
                    },
                    scales: {
                        y: {
                            beginAtZero: true,
                            ticks: { stepSize: 1 },
                            grid: { color: 'rgba(0, 0, 0, 0.1)' }
                        },
                        x: { grid: { display: false } }
                    }
                }
            });
        }

        // Status Distribution Chart
        const statusCtx = document.getElementById('statusDistributionChart');
        if (statusCtx && chartData.statusLabels.length > 0) {
            new Chart(statusCtx, {
                type: 'doughnut',
                data: {
                    labels: chartData.statusLabels,
                    datasets: [{
                        data: chartData.statusValues,
                        backgroundColor: chartData.statusLabels.map(label => {
                            switch(label.toLowerCase()) {
                                case 'approved': return '#28a745';  // Green
                                case 'pending': return '#ffc107';   // Yellow
                                case 'rejected': return '#dc3545';  // Red
                                case 'cancelled': return '#6c757d'; // Grey
                                default: return '#6c757d';          // Default grey
                            }
                        }),
                        borderWidth: 2,
                        borderColor: '#ffffff'
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: {
                            position: 'bottom',
                            labels: { padding: 20, usePointStyle: true }
                        },
                        tooltip: {
                            backgroundColor: 'rgba(0, 0, 0, 0.8)',
                            titleColor: '#ffffff',
                            bodyColor: '#ffffff',
                            callbacks: {
                                label: function(context) {
                                    const value = context.raw;
                                    const total = context.dataset.data.reduce((a, b) => a + b, 0);
                                    const percentage = total > 0 ? ((value / total) * 100).toFixed(1) : '0';
                                    return context.label + ': ' + value + ' (' + percentage + '%)';
                                }
                            }
                        }
                    }
                }
            });
        }

        // Day of Week Chart
        const dayCtx = document.getElementById('dayOfWeekChart');
        if (dayCtx && chartData.dayLabels.length > 0) {
            new Chart(dayCtx, {
                type: 'bar',
                data: {
                    labels: chartData.dayLabels,
                    datasets: [{
                        label: 'Applications by Day',
                        data: chartData.dayValues,
                        backgroundColor: ['#007bff', '#28a745', '#ffc107', '#17a2b8', '#dc3545', '#6f42c1', '#fd7e14'],
                        borderColor: '#ffffff',
                        borderWidth: 2,
                        borderRadius: 4
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: { display: false },
                        tooltip: {
                            backgroundColor: 'rgba(0, 0, 0, 0.8)',
                            titleColor: '#ffffff',
                            bodyColor: '#ffffff',
                            borderColor: '#007bff',
                            borderWidth: 1
                        }
                    },
                    scales: {
                        y: {
                            beginAtZero: true,
                            ticks: { stepSize: 1 },
                            grid: { color: 'rgba(0, 0, 0, 0.1)' }
                        },
                        x: { grid: { display: false } }
                    }
                }
            });
        }

        // Leave Type Distribution Chart
        const leaveTypeCtx = document.getElementById('leaveTypeChart');
        if (leaveTypeCtx && chartData.leaveTypeLabels.length > 0) {
            new Chart(leaveTypeCtx, {
                type: 'bar',
                data: {
                    labels: chartData.leaveTypeLabels,
                    datasets: [{
                        label: 'Applications by Leave Type',
                        data: chartData.leaveTypeValues,
                        backgroundColor: ['#007bff', '#28a745', '#ffc107', '#17a2b8', '#dc3545', '#6f42c1', '#fd7e14', '#20c997'],
                        borderColor: '#ffffff',
                        borderWidth: 2,
                        borderRadius: 4
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    indexAxis: 'y',
                    plugins: {
                        legend: { display: false },
                        tooltip: {
                            backgroundColor: 'rgba(0, 0, 0, 0.8)',
                            titleColor: '#ffffff',
                            bodyColor: '#ffffff',
                            borderColor: '#007bff',
                            borderWidth: 1
                        }
                    },
                    scales: {
                        x: {
                            beginAtZero: true,
                            ticks: { stepSize: 1 },
                            grid: { color: 'rgba(0, 0, 0, 0.1)' }
                        },
                        y: { grid: { display: false } }
                    }
                }
            });
        }
    </c:if>
}

function parseDisplayDate(dateStr) {
    const months = {
        'Jan': 0, 'Feb': 1, 'Mar': 2, 'Apr': 3, 'May': 4, 'Jun': 5,
        'Jul': 6, 'Aug': 7, 'Sep': 8, 'Oct': 9, 'Nov': 10, 'Dec': 11
    };
    
    try {
        const parts = dateStr.split(' ');
        if (parts.length === 3) {
            const month = months[parts[0]];
            const day = parseInt(parts[1].replace(',', ''));
            const year = parseInt(parts[2]);
            return new Date(year, month, day);
        }
    } catch (e) {
        console.error('Error parsing date:', dateStr, e);
    }
    
    return new Date(0);
}
</script>
</body>
</html>
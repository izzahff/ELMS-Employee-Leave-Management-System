<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<!-- Application Statistics -->
<div class="stats-grid">
    <div class="stat-card">
        <h3>${applicationStats.total}</h3>
        <p>Total Applications</p>
    </div>
    <div class="stat-card pending">
        <h3>${applicationStats.pending}</h3>
        <p>Pending</p>
    </div>
    <div class="stat-card approved">
        <h3>${applicationStats.approved}</h3>
        <p>Approved</p>
    </div>
    <div class="stat-card rejected">
        <h3>${applicationStats.rejected}</h3>
        <p>Rejected</p>
    </div>
    <div class="stat-card cancelled">
        <h3>${applicationStats.cancelled}</h3>
        <p>Cancelled</p>
    </div>
</div>

<!-- Leave Types and Applications -->
<div class="balance-section">
    <h3><i class="fas fa-calendar-check"></i> Leave Applications by Type</h3>
    
    <c:choose>
        <c:when test="${not empty leaveBalances}">
            <!-- Check if we have tracked and non-tracked leave types -->
            <c:set var="hasTrackedLeaves" value="false" />
            <c:set var="hasNonTrackedLeaves" value="false" />
            
            <c:forEach var="balance" items="${leaveBalances}">
                <c:if test="${balance.affectsBalance == 1 && balance.standardDuration != null}">
                    <c:set var="hasTrackedLeaves" value="true" />
                </c:if>
                <c:if test="${balance.affectsBalance == 0 || balance.standardDuration == null}">
                    <c:set var="hasNonTrackedLeaves" value="true" />
                </c:if>
            </c:forEach>
            
            <!-- Tracked Leave Types (with balance) -->
            <c:if test="${hasTrackedLeaves}">
                <div class="subsection">
                    <h4><i class="fas fa-balance-scale"></i> Leave Types with Balance Tracking</h4>
                    <div class="info-box">
                        <p>
                            <i class="fas fa-info-circle"></i> <strong>Note:</strong> 
                            Balances: <strong>Standard Allocation - Approved = Available</strong>.
                            To modify allocations, visit 
                            <a href="<%= request.getContextPath() %>/AdminLeaveTypeListController" target="_blank">Manage Leave Types <i class="fas fa-external-link-alt"></i></a>.
                        </p>
                    </div>
                    
                    <table class="balance-table">
                        <thead>
                            <tr>
                                <th>Leave Type</th>
                                <th>Category</th>
                                <th>Total Applications</th>
                                <th>Standard Allocation</th>
                                <th>Used (Approved)</th>
                                <th>Pending</th>
                                <th>Available</th>
                                <th>Status</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="balance" items="${leaveBalances}">
                                <c:if test="${balance.affectsBalance == 1 && balance.standardDuration != null}">
                                    <tr>
                                        <td><strong>${balance.leaveTypeName}</strong></td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${balance.category == 'Full Day'}">
                                                    <i class="fas fa-calendar-day"></i> Full Day
                                                </c:when>
                                                <c:when test="${balance.category == 'Half Day'}">
                                                    <i class="fas fa-calendar-minus"></i> Half Day
                                                </c:when>
                                                <c:otherwise>
                                                    <i class="fas fa-calendar"></i> ${balance.category}
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td style="text-align: center;">
                                            <span class="application-count ${balance.totalApplications == 0 ? 'zero' : ''}">
                                                <i class="fas fa-file-alt"></i> ${balance.totalApplications}
                                            </span>
                                        </td>
                                        <td>
                                            <span class="balance-value">${String.format("%.1f", balance.standardDuration)}</span> days
                                            <br><small style="color: #666;">(Standard)</small>
                                        </td>
                                        <td>
                                            <fmt:formatNumber value="${balance.usedDays}" maxFractionDigits="1" minFractionDigits="1"/> days
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${balance.pendingDays > 0}">
                                                    <span style="color: #ffc107; font-weight: 600;">
                                                        <fmt:formatNumber value="${balance.pendingDays}" maxFractionDigits="1" minFractionDigits="1"/> days
                                                    </span>
                                                </c:when>
                                                <c:otherwise>
                                                    <fmt:formatNumber value="${balance.pendingDays}" maxFractionDigits="1" minFractionDigits="1"/> days
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <span class="balance-value ${balance.availableDays <= 2 ? 'low' : ''}">
                                                <fmt:formatNumber value="${balance.availableDays}" maxFractionDigits="1" minFractionDigits="1"/>
                                            </span> days
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${balance.availableDays < 0}">
                                                    <span class="status-badge status-exceeded">
                                                        <i class="fas fa-exclamation-triangle"></i> Exceeded
                                                    </span>
                                                </c:when>
                                                <c:when test="${balance.availableDays <= 2}">
                                                    <span class="status-badge status-low">
                                                        <i class="fas fa-exclamation-circle"></i> Low
                                                    </span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="status-badge status-good">
                                                        <i class="fas fa-check-circle"></i> Good
                                                    </span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                    </tr>
                                </c:if>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:if>
            
            <!-- Non-Tracked Leave Types (no balance tracking) -->
            <c:if test="${hasNonTrackedLeaves}">
                <div class="subsection" style="margin-top: ${hasTrackedLeaves ? '40px' : '0'};">
                    <h4><i class="fas fa-calendar-alt"></i> Leave Types without Balance Tracking</h4>
                    <div class="info-box info-box-neutral">
                        <p>
                            <i class="fas fa-info-circle"></i> 
                            These leave types do not track balances. Applications are unlimited.
                        </p>
                    </div>
                    
                    <table class="balance-table">
                        <thead>
                            <tr>
                                <th>Leave Type</th>
                                <th>Category</th>
                                <th>Total Applications</th>
                                <th>Approved</th>
                                <th>Pending</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="balance" items="${leaveBalances}">
                                <c:if test="${balance.affectsBalance == 0 || balance.standardDuration == null}">
                                    <tr>
                                        <td><strong>${balance.leaveTypeName}</strong></td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${balance.category == 'Full Day'}">
                                                    <i class="fas fa-calendar-day"></i> Full Day
                                                </c:when>
                                                <c:when test="${balance.category == 'Half Day'}">
                                                    <i class="fas fa-calendar-minus"></i> Half Day
                                                </c:when>
                                                <c:otherwise>
                                                    <i class="fas fa-calendar"></i> ${balance.category}
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td style="text-align: center;">
                                            <span class="application-count ${balance.totalApplications == 0 ? 'zero' : ''}">
                                                <i class="fas fa-file-alt"></i> ${balance.totalApplications}
                                            </span>
                                        </td>
                                        <td>
                                            <fmt:formatNumber value="${balance.usedDays}" maxFractionDigits="1" minFractionDigits="1"/> days
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${balance.pendingDays > 0}">
                                                    <span style="color: #ffc107; font-weight: 600;">
                                                        <fmt:formatNumber value="${balance.pendingDays}" maxFractionDigits="1" minFractionDigits="1"/> days
                                                    </span>
                                                </c:when>
                                                <c:otherwise>
                                                    <fmt:formatNumber value="${balance.pendingDays}" maxFractionDigits="1" minFractionDigits="1"/> days
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                    </tr>
                                </c:if>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </c:if>
            
        </c:when>
        <c:otherwise>
            <div style="text-align: center; padding: 60px; color: #666; background-color: #f8f9fa; border-radius: 8px;">
                <i class="fas fa-inbox" style="font-size: 4rem; margin-bottom: 20px; color: #ccc;"></i>
                <h3 style="margin: 0 0 10px 0; color: #666;">No Leave Types Found</h3>
                <p style="margin: 0;">No leave types are configured in the system.</p>
            </div>
        </c:otherwise>
    </c:choose>
</div>

<!-- Employee Information -->
<div class="balance-section">
    <h3><i class="fas fa-user"></i> Employee Information</h3>
    <table class="balance-table employee-info-table">
        <tbody>
            <tr>
                <th><i class="fas fa-id-badge"></i> Employee ID</th>
                <td><strong>${employee.employeeId}</strong></td>
            </tr>
            <tr>
                <th><i class="fas fa-user"></i> Full Name</th>
                <td>${employee.employeeName}</td>
            </tr>
            <tr>
                <th><i class="fas fa-envelope"></i> Email</th>
                <td><a href="mailto:${employee.employeeEmail}">${employee.employeeEmail}</a></td>
            </tr>
            <tr>
                <th><i class="fas fa-phone"></i> Mobile</th>
                <td><a href="tel:${employee.employeeNoPhone}">${employee.employeeNoPhone}</a></td>
            </tr>
        </tbody>
    </table>
</div>

<style>
/* Stats Grid */
.stats-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
    gap: 20px;
    margin-bottom: 40px;
}

.stat-card {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    padding: 25px;
    border-radius: 12px;
    text-align: center;
    box-shadow: 0 4px 15px rgba(0,0,0,0.1);
    transition: transform 0.3s;
}

.stat-card:hover {
    transform: translateY(-5px);
    box-shadow: 0 8px 25px rgba(0,0,0,0.2);
}

.stat-card.pending { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); }
.stat-card.approved { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); }
.stat-card.rejected { background: linear-gradient(135deg, #fa709a 0%, #fee140 100%); }
.stat-card.cancelled { background: linear-gradient(135deg, #a8edea 0%, #fed6e3 100%); }

.stat-card h3 {
    margin: 0 0 10px 0;
    font-size: 2.5rem;
    font-weight: bold;
}

.stat-card p {
    margin: 0;
    font-size: 0.95rem;
    opacity: 0.95;
    text-transform: uppercase;
    letter-spacing: 0.5px;
}

/* Subsections */
.subsection h4 {
    margin-bottom: 15px;
    color: #495057;
    font-size: 1.1rem;
    display: flex;
    align-items: center;
    gap: 10px;
}

/* Balance Section */
.balance-section {
    margin-top: 40px;
}

.balance-section h3 {
    margin-bottom: 20px;
    color: #333;
    border-bottom: 3px solid #667eea;
    padding-bottom: 12px;
    font-size: 1.3rem;
    display: flex;
    align-items: center;
    gap: 10px;
}

.info-box {
    padding: 18px;
    background-color: #e7f3ff;
    border-left: 4px solid #2196F3;
    border-radius: 8px;
    margin-bottom: 25px;
}

.info-box-neutral {
    background-color: #f8f9fa;
    border-left: 4px solid #6c757d;
}

.info-box p {
    margin: 0;
    color: #0c5460;
    line-height: 1.6;
}

.info-box-neutral p {
    color: #495057;
}

.info-box strong {
    color: #004085;
}

.info-box a {
    color: #007bff;
    text-decoration: underline;
    font-weight: 600;
}

.info-box a:hover {
    color: #0056b3;
}

/* Application Count */
.application-count {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    font-size: 1.3rem;
    font-weight: 700;
    color: #667eea;
    background: #f0f3ff;
    padding: 8px 16px;
    border-radius: 20px;
    border: 2px solid #667eea;
}

.application-count.zero {
    color: #6c757d;
    background: #f8f9fa;
    border: 2px solid #dee2e6;
}

.application-count i {
    font-size: 1.1rem;
}

/* Balance Table */
.balance-table {
    width: 100%;
    border-collapse: collapse;
    margin-top: 20px;
    box-shadow: 0 2px 10px rgba(0,0,0,0.05);
    border-radius: 8px;
    overflow: hidden;
}

.balance-table th,
.balance-table td {
    padding: 16px;
    text-align: left;
    border-bottom: 1px solid #e9ecef;
}

.balance-table th {
    background-color: #f8f9fa;
    font-weight: 600;
    color: #495057;
    text-transform: uppercase;
    font-size: 0.85rem;
    letter-spacing: 0.5px;
}

.balance-table tbody tr {
    transition: background-color 0.2s;
}

.balance-table tbody tr:hover {
    background-color: #f8f9fa;
}

.balance-table tbody tr:last-child td {
    border-bottom: none;
}

.balance-value {
    font-weight: bold;
    font-size: 1.1rem;
    color: #28a745;
}

.balance-value.low {
    color: #dc3545;
}

.status-badge {
    padding: 6px 12px;
    border-radius: 20px;
    font-size: 0.85rem;
    font-weight: 600;
    display: inline-block;
}

.status-exceeded {
    background-color: #f8d7da;
    color: #721c24;
}

.status-low {
    background-color: #fff3cd;
    color: #856404;
}

.status-good {
    background-color: #d4edda;
    color: #155724;
}

.employee-info-table {
    background-color: #f8f9fa;
    border-radius: 8px;
    overflow: hidden;
}

.employee-info-table th {
    width: 200px;
    background-color: #e9ecef;
    font-weight: 600;
}

.loading {
    text-align: center;
    padding: 60px;
    color: #666;
    font-size: 1.1rem;
}

.loading i {
    font-size: 2rem;
    margin-bottom: 15px;
    display: block;
    animation: spin 1s linear infinite;
}

@keyframes spin {
    0% { transform: rotate(0deg); }
    100% { transform: rotate(360deg); }
}
</style>
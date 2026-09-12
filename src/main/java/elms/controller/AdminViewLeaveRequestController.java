package elms.controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import elms.model.LeaveApplication;
import elms.connection.ConnectionManager;
import elms.service.NudgeNotificationService;
import elms.service.NudgeNotificationService.NudgeResult;

@WebServlet("/admin-leave-requests")
public class AdminViewLeaveRequestController extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final List<Integer> ALLOWED_PAGE_SIZES = Arrays.asList(5, 10, 25, 50, 100);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        System.out.println("=== ADMIN VIEW LEAVE REQUESTS ===");
        
        String action = request.getParameter("action");
        if ("checkFailures".equals(action)) {
            handleCheckFailuresRequest(request, response);
            return; // Exit early - don't continue to normal page rendering
        }
        
        
        // Check if admin is logged in
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("adminId") == null) {
            response.sendRedirect("/ELMS_3.0/Admin/AdminLogin.jsp");
            return;
        }

        String adminId = (String) session.getAttribute("adminId");
        System.out.println("Admin ID: " + adminId);

        try {
            // Get filter parameters
            String statusFilter = request.getParameter("status");
            String leaveTypeFilter = request.getParameter("leaveType");
            String employeeFilter = request.getParameter("employee");
            String fromDateFilter = request.getParameter("fromDate");
            String toDateFilter = request.getParameter("toDate");
            
            // Get pagination parameters
            int currentPage = 1;
            String pageParam = request.getParameter("page");
            if (pageParam != null && !pageParam.isEmpty()) {
                try {
                    currentPage = Integer.parseInt(pageParam);
                    if (currentPage < 1) currentPage = 1;
                } catch (NumberFormatException e) {
                    currentPage = 1;
                }
            }
            
            int pageSize = DEFAULT_PAGE_SIZE;
            String sizeParam = request.getParameter("size");
            if (sizeParam != null && !sizeParam.isEmpty()) {
                try {
                    int requestedSize = Integer.parseInt(sizeParam);
                    if (ALLOWED_PAGE_SIZES.contains(requestedSize)) {
                        pageSize = requestedSize;
                    }
                } catch (NumberFormatException e) {
                    pageSize = DEFAULT_PAGE_SIZE;
                }
            }
            
            int offset = (currentPage - 1) * pageSize;
            
            System.out.println("Filters - Status: " + statusFilter + ", LeaveType: " + leaveTypeFilter + 
                              ", Employee: " + employeeFilter + ", FromDate: " + fromDateFilter + 
                              ", ToDate: " + toDateFilter);
            System.out.println("Pagination - Page: " + currentPage + ", Size: " + pageSize + ", Offset: " + offset);

            // Set current request in thread local for employee info access
            setCurrentRequest(request);

            // Get leave requests with filters and pagination
            List<LeaveApplication> allRequests = getLeaveRequests(statusFilter, leaveTypeFilter, 
                                                                employeeFilter, fromDateFilter, 
                                                                toDateFilter, offset, pageSize);
            
            // Get total count for pagination
            int totalRecords = getTotalRequestCount(statusFilter, leaveTypeFilter, employeeFilter, 
                                                  fromDateFilter, toDateFilter);
            int totalPages = (int) Math.ceil((double) totalRecords / pageSize);
            
            // Get statistics
            Map<String, Integer> stats = getLeaveRequestStatistics();
            
            // Get leave type names for display
            Map<String, String> leaveTypeNames = getLeaveTypeNames();
            
            // Get overdue applications count for nudge feature
            List<LeaveApplication> overdueApplications = NudgeNotificationService.getOverdueApplications();
            int overdueCount = overdueApplications.size();

            // Load employee names for overdue applications first (independent of pagination)
            loadEmployeeNamesForOverdueApplications(overdueApplications, request);

           
            
            // Calculate pagination display range
            int startPage = Math.max(1, currentPage - 2);
            int endPage = Math.min(totalPages, currentPage + 2);
            
            // Create showing info text
            int startRecord = totalRecords > 0 ? offset + 1 : 0;
            int endRecord = Math.min(offset + pageSize, totalRecords);
            String showingInfo = String.format("Showing %d to %d of %d entries", 
                                             startRecord, endRecord, totalRecords);
            
            // Set request attributes
            request.setAttribute("allRequests", allRequests);
            request.setAttribute("currentPage", currentPage);
            request.setAttribute("totalPages", totalPages);
            request.setAttribute("totalRecords", totalRecords);
            request.setAttribute("pageSize", pageSize);
            request.setAttribute("allowedPageSizes", ALLOWED_PAGE_SIZES);
            request.setAttribute("startPage", startPage);
            request.setAttribute("endPage", endPage);
            request.setAttribute("showingInfo", showingInfo);
            
            // Statistics
            request.setAttribute("totalRequests", stats.get("total"));
            request.setAttribute("pendingCount", stats.get("pending"));
            request.setAttribute("approvedCount", stats.get("approved"));
            request.setAttribute("rejectedCount", stats.get("rejected"));
            request.setAttribute("cancelledCount", stats.get("cancelled"));
            
            // Leave type names
            request.setAttribute("leaveTypeNames", leaveTypeNames);
            
            // Nudge feature attributes
            request.setAttribute("overdueCount", overdueCount);
            request.setAttribute("overdueApplications", overdueApplications);
            
            System.out.println("Found " + allRequests.size() + " requests for display");
            System.out.println("Total records: " + totalRecords + ", Total pages: " + totalPages);
            System.out.println("Overdue applications: " + overdueCount);
            
            // Forward to JSP
            request.getRequestDispatcher("/Admin/AdminViewLeaveRequest.jsp").forward(request, response);
            
        } catch (SQLException e) {
            System.err.println("❌ SQL Error: " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("errorMessage", "Database error occurred while retrieving leave requests: " + e.getMessage());
            request.getRequestDispatcher("/Admin/AdminViewLeaveRequest.jsp").forward(request, response);
        } catch (Exception e) {
            System.err.println("❌ Unexpected Error: " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("errorMessage", "An unexpected error occurred: " + e.getMessage());
            request.getRequestDispatcher("/Admin/AdminViewLeaveRequest.jsp").forward(request, response);
        }
        
        System.out.println("=== ADMIN VIEW LEAVE REQUESTS COMPLETE ===");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        System.out.println("=== ADMIN LEAVE REQUEST POST ACTION ===");
        
        // ✅ FORCE parameter parsing before anything else
        request.setCharacterEncoding("UTF-8");
        
        // Check if admin is logged in
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("adminId") == null) {
            response.sendRedirect("/ELMS_3.0/Admin/AdminLogin.jsp");
            return;
        }

        String adminId = (String) session.getAttribute("adminId");
        String adminName = (String) session.getAttribute("adminName");
        
        // ✅ Read parameters directly
        String action = request.getParameter("action");
        
        System.out.println("Admin ID: " + adminId);
        System.out.println("Admin Name: " + adminName);
        System.out.println("Action: " + action);
        System.out.println("Content-Type: " + request.getContentType());
        System.out.println("Method: " + request.getMethod());
        
        // ✅ DEBUG: List ALL parameters
        System.out.println("=== ALL REQUEST PARAMETERS ===");
        java.util.Enumeration<String> paramNames = request.getParameterNames();
        while (paramNames.hasMoreElements()) {
            String paramName = paramNames.nextElement();
            String[] paramValues = request.getParameterValues(paramName);
            System.out.println("Parameter: " + paramName + " = " + String.join(", ", paramValues));
        }
        System.out.println("=== END PARAMETERS ===");
        
        // ✅ Handle null action case
        if (action == null || action.trim().isEmpty()) {
            System.err.println("❌ No action parameter found in POST request!");
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"success\":false,\"message\":\"No action specified\"}");
            return;
        }
        
        // ✅ Handle clear nudge failures
        if ("clearNudgeFailures".equals(action)) {
            if (adminName != null) {
                NudgeNotificationService.clearFailedEmails(adminName);
            }
            response.setContentType("application/jhson");
            response.setCharacterEncoding("UTF-8");
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("{\"success\":true}");
            return;
        }
        
        // ✅ Handle nudge action
        if ("nudge".equals(action)) {
            handleNudgeAction(request, response, adminId, adminName, session);
            return;
        }
        
        // Handle other actions
        if ("delete".equals(action)) {
            handleDeleteAction(request, response, adminId, session);
            return;
        }
        
        if ("bulkNudge".equals(action)) {
            handleBulkNudgeAction(request, response, adminId, adminName, session);
            return;
        }
        
        // Unknown action
        System.err.println("❌ Unknown action: " + action);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().write("{\"success\":false,\"message\":\"Unknown action: " + escapeJson(action) + "\"}");
        
        System.out.println("=== ADMIN LEAVE REQUEST POST ACTION COMPLETE ===");
    }
    
    
    /**
     * Handle individual nudge - JSON RESPONSE VERSION
     */
    private void handleNudgeAction(HttpServletRequest request, HttpServletResponse response, 
            String adminId, String adminName, HttpSession session) throws IOException {

        System.out.println("=== HANDLING INDIVIDUAL NUDGE ACTION ===");

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String applicationId = request.getParameter("applicationId");

        if (applicationId == null || applicationId.trim().isEmpty()) {
            response.getWriter().write("{\"success\":false,\"message\":\"Invalid application ID provided for nudge.\"}");
            return;
        }

        try {
            System.out.println("Sending nudge for application: " + applicationId + " and waiting for email completion...");

            // ✅ This now waits for email completion automatically
            NudgeNotificationService.NudgeResult result = 
                NudgeNotificationService.sendNudgeNotification(
                    applicationId, 
                    adminName != null ? adminName : adminId
                );

            // ✅ RETURN JSON BASED ON ACTUAL EMAIL RESULT
            StringBuilder jsonResponse = new StringBuilder();
            jsonResponse.append("{");
            jsonResponse.append("\"success\":").append(result.isSuccess()).append(",");
            jsonResponse.append("\"message\":\"").append(escapeJson(result.getMessage())).append("\",");
            jsonResponse.append("\"applicationId\":\"").append(escapeJson(applicationId)).append("\",");
            jsonResponse.append("\"emailsSent\":").append(result.getEmailsSent()).append(",");
            jsonResponse.append("\"emailsFailed\":").append(result.getEmailsFailed());
            jsonResponse.append("}");

            System.out.println("✅ Returning JSON: " + jsonResponse.toString());
            response.getWriter().write(jsonResponse.toString());

        } catch (Exception e) {
            System.err.println("❌ Error sending nudge: " + e.getMessage());
            e.printStackTrace();
            response.getWriter().write("{\"success\":false,\"message\":\"An error occurred while sending nudge notification: " + escapeJson(e.getMessage()) + "\"}");
        }
    }


/**
 * Helper method to escape JSON strings
 */
private String escapeJson(String input) {
    if (input == null) return "";
    return input.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
}
    
/**
 * Handle bulk nudge action - JSON RESPONSE VERSION
 */
private void handleBulkNudgeAction(HttpServletRequest request, HttpServletResponse response, 
                                  String adminId, String adminName, HttpSession session) throws IOException {
    
    System.out.println("=== HANDLING BULK NUDGE ACTION ===");
    
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");
    
    try {
        System.out.println("Sending bulk nudge notifications and waiting for email completion...");
        
        // ✅ This waits for all emails to complete
        NudgeNotificationService.BulkNudgeResult result = 
            NudgeNotificationService.sendBulkNudgeNotifications(adminName != null ? adminName : adminId);
        
        // ✅ RETURN JSON BASED ON ACTUAL EMAIL RESULT
        StringBuilder jsonResponse = new StringBuilder();
        jsonResponse.append("{");
        jsonResponse.append("\"success\":").append(result.isActualSuccess()).append(",");
        jsonResponse.append("\"message\":\"").append(escapeJson(result.getMessage())).append("\",");
        jsonResponse.append("\"totalApplications\":").append(result.getTotalApplications()).append(",");
        jsonResponse.append("\"successfulNudges\":").append(result.getSuccessfulNudges()).append(",");
        jsonResponse.append("\"failedNudges\":").append(result.getFailedNudges()).append(",");
        jsonResponse.append("\"skippedDueToRateLimit\":").append(result.getSkippedDueToRateLimit());
        jsonResponse.append("}");
        
        System.out.println("✅ Returning JSON: " + jsonResponse.toString());
        response.getWriter().write(jsonResponse.toString());
        
    } catch (Exception e) {
        System.err.println("❌ Error sending bulk nudge: " + e.getMessage());
        e.printStackTrace();
        response.getWriter().write("{\"success\":false,\"message\":\"An error occurred while sending bulk nudge notifications: " + escapeJson(e.getMessage()) + "\"}");
    }
}
    
    /**
     * Handle delete action (existing functionality)
     */
    private void handleDeleteAction(HttpServletRequest request, HttpServletResponse response, 
                                   String adminId, HttpSession session) throws IOException {
        
        String applicationId = request.getParameter("applicationId");
        
        if (applicationId == null || applicationId.trim().isEmpty()) {
            session.setAttribute("errorMessage", "Invalid application ID provided.");
            response.sendRedirect(request.getContextPath() + "/admin-leave-requests");
            return;
        }
        
        try {
            boolean deleted = deleteLeaveApplication(applicationId, adminId);
            
            if (deleted) {
                session.setAttribute("successMessage", "Leave application " + applicationId + " has been successfully deleted.");
                System.out.println("✅ Leave application " + applicationId + " deleted successfully by admin " + adminId);
            } else {
                session.setAttribute("errorMessage", "Failed to delete leave application. The application may not exist or cannot be deleted.");
                System.out.println("❌ Failed to delete leave application " + applicationId);
            }
            
        } catch (SQLException e) {
            System.err.println("❌ SQL Error while deleting application: " + e.getMessage());
            e.printStackTrace();
            session.setAttribute("errorMessage", "Database error occurred while deleting the leave application: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ Unexpected error while deleting application: " + e.getMessage());
            e.printStackTrace();
            session.setAttribute("errorMessage", "An unexpected error occurred while deleting the leave application.");
        }
        
        // Redirect back to the leave requests page
        response.sendRedirect(request.getContextPath() + "/admin-leave-requests");
    }

    /**
     * Deletes a leave application from the database
     * @param applicationId The ID of the application to delete
     * @param adminId The ID of the admin performing the deletion
     * @return true if deletion was successful, false otherwise
     * @throws SQLException if a database error occurs
     */
    private boolean deleteLeaveApplication(String applicationId, String adminId) throws SQLException {
        
        // First check if the application exists and get its details
        String checkSql = "SELECT applicationid, employeeid, leavestatus FROM leaveapplication WHERE applicationid = ?";
        
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            
            checkStmt.setString(1, applicationId);
            
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("❌ Application " + applicationId + " not found");
                    return false;
                }
                
                String employeeId = rs.getString("employeeid");
                String leaveStatus = rs.getString("leavestatus");
                
                System.out.println("📋 Deleting application - ID: " + applicationId + 
                                 ", Employee: " + employeeId + ", Status: " + leaveStatus);
                
                // Prevent deletion of pending applications (optional business rule)
                if ("Pending".equalsIgnoreCase(leaveStatus)) {
                    System.out.println("⚠️ Cannot delete pending application " + applicationId);
                    return false;
                }
            }
        }
        
        // Perform the deletion
        String deleteSql = "DELETE FROM leaveapplication WHERE applicationid = ?";
        
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
            
            deleteStmt.setString(1, applicationId);
            
            int rowsAffected = deleteStmt.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("✅ Successfully deleted application " + applicationId);
                return true;
            } else {
                System.out.println("❌ No rows affected when deleting application " + applicationId);
                return false;
            }
        }
    }
    
    /**
     * Retrieves leave requests with filtering and pagination
     */
    private List<LeaveApplication> getLeaveRequests(String statusFilter, String leaveTypeFilter, 
                                                   String employeeFilter, String fromDateFilter, 
                                                   String toDateFilter, int offset, int limit) throws SQLException {
        
        List<LeaveApplication> requests = new ArrayList<>();
        
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM ( ");
        sql.append("  SELECT ROWNUM rnum, req.* FROM ( ");
        sql.append("  SELECT la.applicationid, la.employeeid, la.leavetypeid, la.leavestartdate, ");
        sql.append("         la.leaveenddate, la.leaveduration, la.leavereason, la.leavestatus, ");
        sql.append("         la.appliedon, la.managerid, la.attachment, la.rejectreason, ");
        sql.append("         e.employeename, e.profile_picture_path as employeeProfilePicture ");
        sql.append("    FROM leaveapplication la ");
        sql.append("    JOIN employee e ON la.employeeid = e.employeeid ");
        sql.append("    WHERE 1=1 ");

        List<Object> parameters = new ArrayList<>();
        
        // Add filters
        if (statusFilter != null && !statusFilter.trim().isEmpty()) {
            sql.append("    AND LOWER(la.leavestatus) = LOWER(?) ");
            parameters.add(statusFilter.trim());
        }
        
        if (leaveTypeFilter != null && !leaveTypeFilter.trim().isEmpty()) {
            sql.append("    AND la.leavetypeid = ? ");
            parameters.add(leaveTypeFilter.trim());
        }
        
        if (employeeFilter != null && !employeeFilter.trim().isEmpty()) {
            sql.append("    AND (LOWER(e.employeename) LIKE LOWER(?) OR LOWER(la.employeeid) LIKE LOWER(?)) ");
            String employeePattern = "%" + employeeFilter.trim() + "%";
            parameters.add(employeePattern);
            parameters.add(employeePattern);
        }
        
        if (fromDateFilter != null && !fromDateFilter.trim().isEmpty()) {
            sql.append("    AND la.leavestartdate >= TO_DATE(?, 'YYYY-MM-DD') ");
            parameters.add(fromDateFilter.trim());
        }
        
        if (toDateFilter != null && !toDateFilter.trim().isEmpty()) {
            sql.append("    AND la.leaveenddate <= TO_DATE(?, 'YYYY-MM-DD') ");
            parameters.add(toDateFilter.trim());
        }
        
        sql.append("    ORDER BY la.appliedon DESC ");
        sql.append("  ) req ");
        sql.append("  WHERE ROWNUM <= ? ");
        sql.append(") ");
        sql.append("WHERE rnum > ?");
        
        // Add pagination parameters
        parameters.add(offset + limit);
        parameters.add(offset);
        
        System.out.println("SQL Query: " + sql.toString());
        System.out.println("Parameters: " + parameters);
        
        // Maps to store employee information
        Map<String, String> employeeNames = new HashMap<>();
        Map<String, String> employeeProfilePictures = new HashMap<>();
        
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            
            // Set parameters
            for (int i = 0; i < parameters.size(); i++) {
                pstmt.setObject(i + 1, parameters.get(i));
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    LeaveApplication request = new LeaveApplication();
                    request.setApplicationid(rs.getString("applicationid"));
                    request.setEmployeeid(rs.getString("employeeid"));
                    request.setLeavetypeid(rs.getString("leavetypeid"));
                    request.setLeavestartdate(rs.getString("leavestartdate"));
                    request.setLeaveenddate(rs.getString("leaveenddate"));
                    request.setLeaveduration(rs.getDouble("leaveduration"));
                    request.setLeavereason(rs.getString("leavereason"));
                    request.setLeavestatus(rs.getString("leavestatus"));
                    request.setAppliedon(rs.getString("appliedon"));
                    request.setManagerid(rs.getString("managerid"));
                    request.setAttachment(rs.getString("attachment"));
                    request.setRejectReason(rs.getString("rejectreason"));
                    
                    // Store employee information in maps
                    String employeeId = rs.getString("employeeid");
                    String employeeName = rs.getString("employeename");
                    String employeeProfilePicture = rs.getString("employeeProfilePicture");
                    
                    employeeNames.put(employeeId, employeeName != null ? employeeName : "Unknown Employee");
                    employeeProfilePictures.put(employeeId, employeeProfilePicture);
                    
                    requests.add(request);
                }
            }
        }
        
        // Store employee information in request scope for JSP access
        HttpServletRequest currentRequest = getCurrentRequest();
        if (currentRequest != null) {
            currentRequest.setAttribute("employeeNames", employeeNames);
            currentRequest.setAttribute("employeeProfilePictures", employeeProfilePictures);
        }
        
        System.out.println("Retrieved " + requests.size() + " leave requests");
        return requests;
    }
    
    // Thread-local to store current request
    private static final ThreadLocal<HttpServletRequest> currentRequestThreadLocal = new ThreadLocal<>();
    
    private HttpServletRequest getCurrentRequest() {
        return currentRequestThreadLocal.get();
    }
    
    private void setCurrentRequest(HttpServletRequest request) {
        currentRequestThreadLocal.set(request);
    }
    
    /**
     * Gets total count of leave requests for pagination
     */
    private int getTotalRequestCount(String statusFilter, String leaveTypeFilter, String employeeFilter, 
                                   String fromDateFilter, String toDateFilter) throws SQLException {
        
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(*) FROM leaveapplication la ");
        sql.append("JOIN employee e ON la.employeeid = e.employeeid ");
        sql.append("WHERE 1=1 ");
        
        List<Object> parameters = new ArrayList<>();
        
        // Add same filters as main query
        if (statusFilter != null && !statusFilter.trim().isEmpty()) {
            sql.append("AND LOWER(la.leavestatus) = LOWER(?) ");
            parameters.add(statusFilter.trim());
        }
        
        if (leaveTypeFilter != null && !leaveTypeFilter.trim().isEmpty()) {
            sql.append("AND la.leavetypeid = ? ");
            parameters.add(leaveTypeFilter.trim());
        }
        
        if (employeeFilter != null && !employeeFilter.trim().isEmpty()) {
            sql.append("AND (LOWER(e.employeename) LIKE LOWER(?) OR LOWER(la.employeeid) LIKE LOWER(?)) ");
            String employeePattern = "%" + employeeFilter.trim() + "%";
            parameters.add(employeePattern);
            parameters.add(employeePattern);
        }
        
        if (fromDateFilter != null && !fromDateFilter.trim().isEmpty()) {
            sql.append("AND la.leavestartdate >= TO_DATE(?, 'YYYY-MM-DD') ");
            parameters.add(fromDateFilter.trim());
        }
        
        if (toDateFilter != null && !toDateFilter.trim().isEmpty()) {
            sql.append("AND la.leaveenddate <= TO_DATE(?, 'YYYY-MM-DD') ");
            parameters.add(toDateFilter.trim());
        }
        
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            
            // Set parameters
            for (int i = 0; i < parameters.size(); i++) {
                pstmt.setObject(i + 1, parameters.get(i));
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        
        return 0;
    }
    
    /**
     * Gets leave request statistics for dashboard cards
     */
    private Map<String, Integer> getLeaveRequestStatistics() throws SQLException {
        Map<String, Integer> stats = new HashMap<>();
        
        String sql = "SELECT leavestatus, COUNT(*) as count FROM leaveapplication GROUP BY leavestatus " +
                    "UNION ALL SELECT 'TOTAL', COUNT(*) FROM leaveapplication";
        
        // Initialize with zeros
        stats.put("total", 0);
        stats.put("pending", 0);
        stats.put("approved", 0);
        stats.put("rejected", 0);
        stats.put("cancelled", 0);
        
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                String status = rs.getString("leavestatus");
                int count = rs.getInt("count");
                
                if ("TOTAL".equalsIgnoreCase(status)) {
                    stats.put("total", count);
                } else if ("Pending".equalsIgnoreCase(status)) {
                    stats.put("pending", count);
                } else if ("Approved".equalsIgnoreCase(status)) {
                    stats.put("approved", count);
                } else if ("Rejected".equalsIgnoreCase(status)) {
                    stats.put("rejected", count);
                } else if ("Cancelled".equalsIgnoreCase(status)) {
                    stats.put("cancelled", count);
                }
            }
        }
        
        System.out.println("Leave Request Statistics: " + stats);
        return stats;
    }
    
    /**
     * Load employee names specifically for overdue applications (independent of pagination)
     */
    private void loadEmployeeNamesForOverdueApplications(List<LeaveApplication> overdueApplications, 
                                                       HttpServletRequest request) throws SQLException {
        
        System.out.println("=== LOADING EMPLOYEE NAMES FOR OVERDUE APPLICATIONS ===");
        
        if (overdueApplications == null || overdueApplications.isEmpty()) {
            System.out.println("No overdue applications to process");
            return;
        }
        
        // Get existing employee names map or create new one
        @SuppressWarnings("unchecked")
        Map<String, String> employeeNames = (Map<String, String>) request.getAttribute("employeeNames");
        if (employeeNames == null) {
            employeeNames = new HashMap<>();
        }
        
        @SuppressWarnings("unchecked")
        Map<String, String> employeeProfilePictures = (Map<String, String>) request.getAttribute("employeeProfilePictures");
        if (employeeProfilePictures == null) {
            employeeProfilePictures = new HashMap<>();
        }
        
        // Collect employee IDs from overdue applications that don't have names
        Set<String> overdueEmployeeIds = new HashSet<>();
        for (LeaveApplication app : overdueApplications) {
            String employeeId = app.getEmployeeid();
            if (employeeId != null && 
                (!employeeNames.containsKey(employeeId) || 
                 employeeNames.get(employeeId) == null || 
                 employeeNames.get(employeeId).trim().isEmpty())) {
                overdueEmployeeIds.add(employeeId);
            }
        }
        
        System.out.println("Overdue applications needing employee names: " + overdueEmployeeIds.size());
        System.out.println("Employee IDs: " + overdueEmployeeIds);
        
        // Fetch employee information for overdue applications
        if (!overdueEmployeeIds.isEmpty()) {
            String sql = "SELECT employeeid, employeename, profile_picture_path FROM employee WHERE employeeid IN (" +
                        String.join(",", Collections.nCopies(overdueEmployeeIds.size(), "?")) + ")";
            
            try (Connection conn = ConnectionManager.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                
                int paramIndex = 1;
                for (String employeeId : overdueEmployeeIds) {
                    pstmt.setString(paramIndex++, employeeId);
                }
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        String employeeId = rs.getString("employeeid");
                        String employeeName = rs.getString("employeename");
                        String profilePicture = rs.getString("profile_picture_path");
                        
                        // Store employee name (with fallback)
                        if (employeeName != null && !employeeName.trim().isEmpty()) {
                            employeeNames.put(employeeId, employeeName.trim());
                            System.out.println("✅ Loaded overdue employee name: " + employeeId + " -> " + employeeName);
                        } else {
                            employeeNames.put(employeeId, "Employee ID: " + employeeId);
                            System.out.println("⚠️ No name found for overdue employee: " + employeeId + ", using fallback");
                        }
                        
                        // Store profile picture
                        employeeProfilePictures.put(employeeId, profilePicture);
                    }
                }
            }
            
            // Check for any employee IDs that still don't have names (deleted employees)
            for (String employeeId : overdueEmployeeIds) {
                if (!employeeNames.containsKey(employeeId) || 
                    employeeNames.get(employeeId) == null || 
                    employeeNames.get(employeeId).trim().isEmpty()) {
                    employeeNames.put(employeeId, "Deleted Employee (" + employeeId + ")");
                    employeeProfilePictures.put(employeeId, null);
                    System.out.println("❌ Overdue employee not found in database: " + employeeId + ", marked as deleted");
                }
            }
        }
        
        // Update request attributes
        request.setAttribute("employeeNames", employeeNames);
        request.setAttribute("employeeProfilePictures", employeeProfilePictures);
        
        System.out.println("=== OVERDUE EMPLOYEE NAMES LOADING COMPLETE ===");
        System.out.println("Final employee names map size: " + employeeNames.size());
        
        // Debug: Print overdue employee names
        for (LeaveApplication app : overdueApplications) {
            String employeeId = app.getEmployeeid();
            String employeeName = employeeNames.get(employeeId);
            System.out.println("Overdue App " + app.getApplicationid() + " - Employee: " + employeeId + " -> " + employeeName);
        }
    }
    
    /**
     * Gets leave type names for filtering dropdown and display
     */
    private Map<String, String> getLeaveTypeNames() throws SQLException {
        Map<String, String> leaveTypes = new HashMap<>();
        
        String sql = "SELECT leavetypeid, leavetypename FROM leavetype ORDER BY leavetypename";
        
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                leaveTypes.put(rs.getString("leavetypeid"), rs.getString("leavetypename"));
            }
        }
        
        System.out.println("Leave Types: " + leaveTypes);
        return leaveTypes;
    }
    
    /**
     * Handle AJAX request to check for email failures
     */
    private void handleCheckFailuresRequest(HttpServletRequest request, HttpServletResponse response) 
            throws IOException {
        
        System.out.println("=== CHECKING FOR EMAIL FAILURES (AJAX) ===");
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        // Get admin name from session
        HttpSession session = request.getSession(false);
        String adminName = null;
        
        if (session != null) {
            adminName = (String) session.getAttribute("adminName");
        }
        
        if (adminName == null) {
            System.out.println("⚠️ No admin name in session");
            response.getWriter().write("{\"hasFailures\":false,\"failures\":[]}");
            return;
        }
        
        // Get failed emails for this admin
        List<NudgeNotificationService.EmailFailure> failures = 
            NudgeNotificationService.getFailedEmails(adminName);
        
        if (failures != null && !failures.isEmpty()) {
            System.out.println("❌ Found " + failures.size() + " email failures");
            
            // Convert failures to JSON manually
            StringBuilder json = new StringBuilder("{\"hasFailures\":true,\"failures\":[");
            
            for (int i = 0; i < failures.size(); i++) {
                NudgeNotificationService.EmailFailure f = failures.get(i);
                
                if (i > 0) json.append(",");
                
                json.append("{")
                    .append("\"applicationId\":\"").append(escapeJson(f.getApplicationId())).append("\",")
                    .append("\"managerName\":\"").append(escapeJson(f.getManagerName())).append("\",")
                    .append("\"managerEmail\":\"").append(escapeJson(f.getManagerEmail())).append("\",")
                    .append("\"failureReason\":\"").append(escapeJson(f.getFailureReason())).append("\",")
                    .append("\"failureTime\":").append(f.getFailureTime().getTime())
                    .append("}");
            }
            
            json.append("]}");
            
            String jsonResponse = json.toString();
            System.out.println("📤 Sending JSON response: " + jsonResponse);
            response.getWriter().write(jsonResponse);
            
        } else {
            System.out.println("✅ No email failures found");
            response.getWriter().write("{\"hasFailures\":false,\"failures\":[]}");
        }
        
        System.out.println("=== CHECK FAILURES COMPLETE ===");
    }
    
}
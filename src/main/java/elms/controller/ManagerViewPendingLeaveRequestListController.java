package elms.controller;

import elms.DAO.LeaveApplicationDAO;
import elms.model.LeaveApplication;
import elms.model.Manager;
import elms.connection.ConnectionManager;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.Date;

@WebServlet({"/manager-pending-requests", "/executive-director-pending-requests"})
public class ManagerViewPendingLeaveRequestListController extends HttpServlet {
    
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final List<Integer> ALLOWED_PAGE_SIZES = Arrays.asList(5, 10, 25, 50, 100);
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        System.out.println("=== Manager/Executive Director Pending Requests Controller - GET ===");
        
        HttpSession session = request.getSession();
        Manager manager = (Manager) session.getAttribute("manager");
        
        if (manager == null) {
            System.out.println("❌ Manager not logged in");
            response.sendRedirect(request.getContextPath() + "/Manager/ManagerLogin.jsp");
            return;
        }
        
        // Determine user type from session
        String userType = (String) session.getAttribute("userType");
        boolean isExecutiveDirector = "executive_director".equals(userType);
        
        System.out.println("✅ User logged in: " + manager.getManagername() + " (Type: " + userType + ")");
        
        try {
            // Get pagination parameters
            int page = getIntParameter(request, "page", 1);
            int size = getIntParameter(request, "size", DEFAULT_PAGE_SIZE);
            
            // Validate page size
            if (!ALLOWED_PAGE_SIZES.contains(size)) {
                size = DEFAULT_PAGE_SIZE;
            }
            
            int offset = (page - 1) * size;
            
            // Get filter parameters
            String leaveTypeFilter = request.getParameter("leaveType");
            String employeeFilter = request.getParameter("employee");
            String priorityFilter = request.getParameter("priority");
            String applicationTypeFilter = request.getParameter("applicationType");
            String fromDate = request.getParameter("fromDate");
            String toDate = request.getParameter("toDate");
            
            System.out.println("📋 Filters Applied:");
            System.out.println("  Leave Type: " + leaveTypeFilter);
            System.out.println("  Employee: " + employeeFilter);
            System.out.println("  Priority: " + priorityFilter);
            System.out.println("  Application Type: " + applicationTypeFilter);
            System.out.println("  From Date: " + fromDate);
            System.out.println("  To Date: " + toDate);
            System.out.println("  Page: " + page + ", Size: " + size);

            // Get pending leave requests with filters and pagination
            PendingRequestsResult result = getPendingLeaveRequestsWithEmployeeInfo(leaveTypeFilter, 
                    employeeFilter, priorityFilter, applicationTypeFilter, fromDate, toDate, offset, size);
            
            List<LeaveApplication> pendingRequests = result.getRequests();
            Map<String, String> employeeNames = result.getEmployeeNames();
            Map<String, String> employeeProfilePictures = result.getEmployeeProfilePictures();
            
            // Get total count for pagination
            int totalRecords = getTotalPendingRequestCount(leaveTypeFilter, employeeFilter, 
                    priorityFilter, applicationTypeFilter, fromDate, toDate);
            int totalPages = (int) Math.ceil((double) totalRecords / size);
            
            // Ensure page is within valid range
            if (page < 1) page = 1;
            if (page > totalPages && totalPages > 0) page = totalPages;
            
            // Get statistics for all pending requests
            Map<String, Integer> stats = getPendingRequestStatistics();
            
            // Get leave type names for display and filters
            Map<String, String> leaveTypeNames = getLeaveTypeNames();
            
            // Calculate pagination display range
            int startPage = Math.max(1, page - 2);
            int endPage = Math.min(totalPages, page + 2);
            
            // Calculate showing info
            String showingInfo = calculateShowingInfo(offset, Math.min(offset + size, totalRecords), totalRecords);
            
            // Set attributes for JSP
            request.setAttribute("pendingRequests", pendingRequests);
            request.setAttribute("leaveTypeNames", leaveTypeNames);
            request.setAttribute("employeeNames", employeeNames);
            request.setAttribute("employeeProfilePictures", employeeProfilePictures);
            
            // Statistics
            request.setAttribute("totalPendingRequests", stats.get("total"));
            request.setAttribute("urgentRequests", stats.get("urgent"));
            request.setAttribute("todayRequests", stats.get("today"));
            
            // Pagination attributes
            request.setAttribute("currentPage", page);
            request.setAttribute("totalPages", totalPages);
            request.setAttribute("pageSize", size);
            request.setAttribute("totalRecords", totalRecords);
            request.setAttribute("showingInfo", showingInfo);
            request.setAttribute("startPage", startPage);
            request.setAttribute("endPage", endPage);
            request.setAttribute("allowedPageSizes", ALLOWED_PAGE_SIZES);
            
            System.out.println("📊 Statistics:");
            System.out.println("  Total Pending: " + stats.get("total"));
            System.out.println("  Urgent: " + stats.get("urgent"));
            System.out.println("  Today: " + stats.get("today"));
            System.out.println("  Filtered Results: " + totalRecords);
            System.out.println("  Current Page: " + page + "/" + totalPages);
            
            // Determine which JSP to forward to based on user type
            String forwardPage;
            if (isExecutiveDirector) {
                forwardPage = "/Manager/DirectorViewPendingLeaveRequestList.jsp";
                System.out.println("🎯 Forwarding to Executive Director JSP: " + forwardPage);
            } else {
                forwardPage = "/Manager/ManagerViewPendingLeaveRequestList.jsp";
                System.out.println("🎯 Forwarding to Manager JSP: " + forwardPage);
            }
            
            // Forward to appropriate JSP
            request.getRequestDispatcher(forwardPage).forward(request, response);
            
        } catch (SQLException e) {
            System.err.println("❌ Database error in ManagerViewPendingLeaveRequestListController: " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("errorMessage", "Database error occurred while loading pending requests");
            
            // Forward to appropriate error page based on user type
            String errorPage = isExecutiveDirector ? 
                "/Manager/DirectorViewPendingLeaveRequestList.jsp" : 
                "/Manager/ManagerViewPendingLeaveRequestList.jsp";
            request.getRequestDispatcher(errorPage).forward(request, response);
            
        } catch (Exception e) {
            System.err.println("❌ Unexpected error in ManagerViewPendingLeaveRequestListController: " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("errorMessage", "An unexpected error occurred");
            
            // Forward to appropriate error page based on user type
            String errorPage = isExecutiveDirector ? 
                "/Manager/DirectorViewPendingLeaveRequestList.jsp" : 
                "/Manager/ManagerViewPendingLeaveRequestList.jsp";
            request.getRequestDispatcher(errorPage).forward(request, response);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Redirect POST to GET for this controller
        doGet(request, response);
    }
    
    /**
     * Result class to hold both requests and employee information
     */
    private static class PendingRequestsResult {
        private List<LeaveApplication> requests;
        private Map<String, String> employeeNames;
        private Map<String, String> employeeProfilePictures;
        
        public PendingRequestsResult(List<LeaveApplication> requests, 
                                   Map<String, String> employeeNames, 
                                   Map<String, String> employeeProfilePictures) {
            this.requests = requests;
            this.employeeNames = employeeNames;
            this.employeeProfilePictures = employeeProfilePictures;
        }
        
        public List<LeaveApplication> getRequests() { return requests; }
        public Map<String, String> getEmployeeNames() { return employeeNames; }
        public Map<String, String> getEmployeeProfilePictures() { return employeeProfilePictures; }
    }
    
    /**
     * Get pending leave requests with employee information - Fixed version
     */
    private PendingRequestsResult getPendingLeaveRequestsWithEmployeeInfo(String leaveTypeFilter, 
            String employeeFilter, String priorityFilter, String applicationTypeFilter, 
            String fromDate, String toDate, 
            int offset, int limit) throws SQLException {
        
        List<LeaveApplication> requests = new ArrayList<>();
        Map<String, String> employeeNames = new HashMap<>();
        Map<String, String> employeeProfilePictures = new HashMap<>();
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = ConnectionManager.getConnection();
            
            StringBuilder sql = new StringBuilder();
            
            // 🔥 KEY FIX: Check if we need to fetch ALL records for in-memory filtering
            boolean needsInMemoryFilter = (priorityFilter != null && !priorityFilter.trim().isEmpty()) ||
                                          (applicationTypeFilter != null && !applicationTypeFilter.trim().isEmpty());
            
            if (needsInMemoryFilter) {
                System.out.println("🔍 Priority/AppType filter detected - fetching ALL records for in-memory filtering");
                // Fetch ALL records without pagination
                sql.append("SELECT la.applicationid, la.employeeid, la.leavetypeid, la.leavestartdate, ");
                sql.append("       la.leaveenddate, la.leaveduration, la.leavereason, la.leavestatus, ");
                sql.append("       la.appliedon, la.managerid, la.attachment, ");
                sql.append("       e.employeename, e.profile_picture_path as employeeProfilePicture ");
                sql.append("FROM leaveapplication la ");
                sql.append("JOIN employee e ON la.employeeid = e.employeeid ");
                sql.append("WHERE LOWER(la.leavestatus) = 'pending' ");
            } else {
                // Use pagination for non-priority filters
                sql.append("SELECT * FROM ( ");
                sql.append("  SELECT ROWNUM rnum, req.* FROM ( ");
                sql.append("    SELECT la.applicationid, la.employeeid, la.leavetypeid, la.leavestartdate, ");
                sql.append("           la.leaveenddate, la.leaveduration, la.leavereason, la.leavestatus, ");
                sql.append("           la.appliedon, la.managerid, la.attachment, ");
                sql.append("           e.employeename, e.profile_picture_path as employeeProfilePicture ");
                sql.append("    FROM leaveapplication la ");
                sql.append("    JOIN employee e ON la.employeeid = e.employeeid ");
                sql.append("    WHERE LOWER(la.leavestatus) = 'pending' ");
            }

            List<Object> parameters = new ArrayList<>();
            
            // Add filters
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
            
            if (fromDate != null && !fromDate.trim().isEmpty()) {
                sql.append("    AND la.appliedon >= TO_DATE(?, 'YYYY-MM-DD') ");
                parameters.add(fromDate.trim());
            }
            
            if (toDate != null && !toDate.trim().isEmpty()) {
                sql.append("    AND la.appliedon <= TO_DATE(?, 'YYYY-MM-DD') + 1 ");
                parameters.add(toDate.trim());
            }
            
            if (!needsInMemoryFilter) {
                // Add pagination only when NOT using in-memory filtering
                sql.append("    ORDER BY la.appliedon DESC ");
                sql.append("  ) req ");
                sql.append("  WHERE ROWNUM <= ? ");
                sql.append(") ");
                sql.append("WHERE rnum > ?");
                
                // Add pagination parameters
                parameters.add(offset + limit);
                parameters.add(offset);
            } else {
                // No pagination in SQL, just order by
                sql.append("ORDER BY la.appliedon DESC");
            }
            
            System.out.println("SQL Query: " + sql.toString());
            System.out.println("Parameters: " + parameters);
            
            pstmt = conn.prepareStatement(sql.toString());
            
            // Set parameters
            for (int i = 0; i < parameters.size(); i++) {
                pstmt.setObject(i + 1, parameters.get(i));
            }
            
            rs = pstmt.executeQuery();
            
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
                
                // Store employee information in maps
                String employeeId = rs.getString("employeeid");
                employeeNames.put(employeeId, rs.getString("employeename"));
                
                String profilePicPath = rs.getString("employeeProfilePicture");
                employeeProfilePictures.put(employeeId, profilePicPath);
                
                requests.add(request);
            }
            
        } finally {
            // Close resources in reverse order
            if (rs != null) {
                try { rs.close(); } catch (SQLException e) { 
                    System.err.println("Error closing ResultSet: " + e.getMessage());
                }
            }
            if (pstmt != null) {
                try { pstmt.close(); } catch (SQLException e) { 
                    System.err.println("Error closing PreparedStatement: " + e.getMessage());
                }
            }
            if (conn != null) {
                try { conn.close(); } catch (SQLException e) { 
                    System.err.println("Error closing Connection: " + e.getMessage());
                }
            }
        }
        
     // Apply priority filter if specified (done in memory since it requires date calculation)
        if (priorityFilter != null && !priorityFilter.trim().isEmpty()) {
            System.out.println("📊 Before priority filter: " + requests.size() + " records");
            requests = applyPriorityFilter(requests, priorityFilter);
            System.out.println("📊 After priority filter: " + requests.size() + " records");
            
            // 🔥 KEY FIX: Apply pagination AFTER filtering
            int totalFiltered = requests.size();
            int fromIndex = Math.min(offset, totalFiltered);
            int toIndex = Math.min(offset + limit, totalFiltered);
            
            if (fromIndex < toIndex) {
                requests = new ArrayList<>(requests.subList(fromIndex, toIndex));
                System.out.println("📊 After pagination: " + requests.size() + " records (showing " + (fromIndex + 1) + "-" + toIndex + " of " + totalFiltered + ")");
            } else {
                requests = new ArrayList<>();
                System.out.println("📊 After pagination: 0 records (offset beyond filtered results)");
            }
        }

        // ✅ Apply application type filter if specified
        if (applicationTypeFilter != null && !applicationTypeFilter.trim().isEmpty()) {
            System.out.println("📊 Before appType filter: " + requests.size() + " records");
            requests = applyApplicationTypeFilter(requests, applicationTypeFilter);
            System.out.println("📊 After appType filter: " + requests.size() + " records");
            
            // Apply pagination AFTER filtering
            int totalFiltered = requests.size();
            int fromIndex = Math.min(offset, totalFiltered);
            int toIndex = Math.min(offset + limit, totalFiltered);
            
            if (fromIndex < toIndex) {
                requests = new ArrayList<>(requests.subList(fromIndex, toIndex));
                System.out.println("📊 After pagination: " + requests.size() + " records (showing " + (fromIndex + 1) + "-" + toIndex + " of " + totalFiltered + ")");
            } else {
                requests = new ArrayList<>();
                System.out.println("📊 After pagination: 0 records (offset beyond filtered results)");
            }
        }

        System.out.println("Retrieved " + requests.size() + " pending leave requests");
        return new PendingRequestsResult(requests, employeeNames, employeeProfilePictures);
    }
 
    /**
     * Apply priority filter (overdue, backdated, urgent, normal) - DEBUGGED VERSION
     */
    private List<LeaveApplication> applyPriorityFilter(List<LeaveApplication> requests, String priorityFilter) {
        List<LeaveApplication> filtered = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat sdfWithTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        System.out.println("╔════════════════════════════════════════════════════════════════");
        System.out.println("║ PRIORITY FILTER DEBUG: " + priorityFilter);
        System.out.println("║ Total applications to filter: " + requests.size());
        System.out.println("╚════════════════════════════════════════════════════════════════");
        
        Date currentDate = new Date();
        String currentDateStr = sdf.format(currentDate);
        System.out.println("🕐 Current Date (stripped): " + currentDateStr);
        
        int matchCount = 0;
        int errorCount = 0;
        
        for (int i = 0; i < requests.size(); i++) {
            LeaveApplication app = requests.get(i);
            System.out.println("\n--- Application " + (i+1) + "/" + requests.size() + " ---");
            System.out.println("App ID: " + app.getApplicationid());
            System.out.println("Employee: " + app.getEmployeeid());
            System.out.println("Status: " + app.getLeavestatus());
            System.out.println("Raw Start Date: '" + app.getLeavestartdate() + "'");
            System.out.println("Raw Applied On: '" + app.getAppliedon() + "'");
            
            try {
                // Check for null values
                if (app.getLeavestartdate() == null || app.getAppliedon() == null) {
                    System.out.println("❌ ERROR: Null date values detected!");
                    errorCount++;
                    continue;
                }
                
                // Parse dates
                Date startDate = sdf.parse(app.getLeavestartdate());
                Date appliedDate = sdfWithTime.parse(app.getAppliedon());
                
                // Strip time to get date-only comparison
                String startDateStr = sdf.format(startDate);
                String appliedDateStr = sdf.format(appliedDate);
                
                Date startDateOnly = sdf.parse(startDateStr);
                Date appliedDateOnly = sdf.parse(appliedDateStr);
                Date currentDateOnly = sdf.parse(currentDateStr);
                
                System.out.println("📅 Parsed Start Date: " + startDateStr + " (" + startDateOnly.getTime() + ")");
                System.out.println("📅 Parsed Applied Date: " + appliedDateStr + " (" + appliedDateOnly.getTime() + ")");
                System.out.println("📅 Current Date: " + currentDateStr + " (" + currentDateOnly.getTime() + ")");
                
                // Calculate days until start
                long millisDiff = startDateOnly.getTime() - currentDateOnly.getTime();
                long daysUntilStart = millisDiff / (24 * 60 * 60 * 1000);
                
                System.out.println("⏱️ Days Until Start: " + daysUntilStart);
                
                boolean match = false;
                String reason = "";
                
                switch (priorityFilter.toLowerCase().trim()) {
                case "overdue":
	                    // OVERDUE: Start date has passed OR is today (already started or starting now)
	                    boolean isOverdue = startDateOnly.before(currentDateOnly) || startDateOnly.equals(currentDateOnly);
	                    match = isOverdue;
                        reason = String.format("Overdue Check: startDate(%s) < currentDate(%s) = %b", 
                                startDateStr, currentDateStr, isOverdue);
                        System.out.println("🔴 " + reason);
                        System.out.println("   startDateOnly.getTime() = " + startDateOnly.getTime());
                        System.out.println("   currentDateOnly.getTime() = " + currentDateOnly.getTime());
                        System.out.println("   Difference (ms) = " + (currentDateOnly.getTime() - startDateOnly.getTime()));
                        break;
                        
                    case "backdated":
                        // BACKDATED: Start date is before the date when they applied
                        boolean isBackdated = startDateOnly.before(appliedDateOnly);
                        match = isBackdated;
                        reason = String.format("Backdated Check: startDate(%s) < appliedDate(%s) = %b", 
                                startDateStr, appliedDateStr, isBackdated);
                        System.out.println("🟠 " + reason);
                        System.out.println("   startDateOnly.getTime() = " + startDateOnly.getTime());
                        System.out.println("   appliedDateOnly.getTime() = " + appliedDateOnly.getTime());
                        System.out.println("   Difference (ms) = " + (appliedDateOnly.getTime() - startDateOnly.getTime()));
                        break;
                        
                    case "urgent":
                        // URGENT: Starts within 1-3 days from now (not today, not overdue, not backdated)
                        boolean notOverdue = !startDateOnly.before(currentDateOnly) && !startDateOnly.equals(currentDateOnly);
                        boolean notBackdated = !startDateOnly.before(appliedDateOnly);
                        boolean within3Days = daysUntilStart >= 1 && daysUntilStart <= 3;
                        match = notOverdue && notBackdated && within3Days;
                        reason = String.format("Urgent Check: days=%d, notOverdue=%b, notBackdated=%b, within3Days=%b", 
                                daysUntilStart, notOverdue, notBackdated, within3Days);
                        System.out.println("🟡 " + reason);
                        break;
                        
                    case "normal":
                        // NORMAL: Starts more than 3 days from now (not overdue, not backdated, not urgent)
                        boolean normalNotOverdue = !startDateOnly.before(currentDateOnly);
                        boolean normalNotBackdated = !startDateOnly.before(appliedDateOnly);
                        boolean moreThan3Days = daysUntilStart > 3;
                        match = normalNotOverdue && normalNotBackdated && moreThan3Days;
                        reason = String.format("Normal Check: days=%d, notOverdue=%b, notBackdated=%b, moreThan3Days=%b", 
                                daysUntilStart, normalNotOverdue, normalNotBackdated, moreThan3Days);
                        System.out.println("🟢 " + reason);
                        break;
                        
                    default:
                        match = true;
                        reason = "No filter or unknown filter: '" + priorityFilter + "'";
                        System.out.println("⚪ " + reason);
                        break;
                }
                
                if (match) {
                    filtered.add(app);
                    matchCount++;
                    System.out.println("✅ MATCHED - Added to filtered list");
                } else {
                    System.out.println("❌ NOT MATCHED - Excluded from results");
                }
                
            } catch (ParseException e) {
                System.err.println("❌ ParseException for application " + app.getApplicationid());
                System.err.println("   Error: " + e.getMessage());
                System.err.println("   Start Date String: '" + app.getLeavestartdate() + "'");
                System.err.println("   Applied On String: '" + app.getAppliedon() + "'");
                e.printStackTrace();
                errorCount++;
            } catch (Exception e) {
                System.err.println("❌ Unexpected Exception for application " + app.getApplicationid());
                System.err.println("   Error: " + e.getMessage());
                e.printStackTrace();
                errorCount++;
            }
        }
        
        System.out.println("\n╔════════════════════════════════════════════════════════════════");
        System.out.println("║ FILTER RESULTS");
        System.out.println("║ Priority Filter: " + priorityFilter);
        System.out.println("║ Total Input: " + requests.size());
        System.out.println("║ Matched: " + matchCount);
        System.out.println("║ Filtered Out: " + (requests.size() - matchCount - errorCount));
        System.out.println("║ Errors: " + errorCount);
        System.out.println("║ Returned: " + filtered.size());
        System.out.println("╚════════════════════════════════════════════════════════════════");
        
        return filtered;
    }
    
    /**
     * Apply application type filter (backdated, overdue, regular)
     */
    private List<LeaveApplication> applyApplicationTypeFilter(List<LeaveApplication> requests, String applicationTypeFilter) {
        List<LeaveApplication> filtered = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date currentDate = new Date();
        
        for (LeaveApplication app : requests) {
            try {
                Date startDate = sdf.parse(app.getLeavestartdate());
                Date appliedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(app.getAppliedon());
                
                // Strip time for comparison
                String startDateStr = sdf.format(startDate);
                String appliedDateStr = sdf.format(appliedDate);
                String currentDateStr = sdf.format(currentDate);
                
                Date startDateOnly = sdf.parse(startDateStr);
                Date appliedDateOnly = sdf.parse(appliedDateStr);
                Date currentDateOnly = sdf.parse(currentDateStr);
                
                if ("backdated".equalsIgnoreCase(applicationTypeFilter)) {
                    // Backdated: start date < applied date
                    if (startDateOnly.before(appliedDateOnly)) {
                        filtered.add(app);
                    }
                } else if ("overdue".equalsIgnoreCase(applicationTypeFilter)) {
                    // Overdue: start date < current date AND still pending
                    if (startDateOnly.before(currentDateOnly) && "Pending".equalsIgnoreCase(app.getLeavestatus())) {
                        filtered.add(app);
                    }
                } else if ("regular".equalsIgnoreCase(applicationTypeFilter)) {
                    // Regular: not backdated and not overdue
                    if (!startDateOnly.before(appliedDateOnly) && !startDateOnly.before(currentDateOnly)) {
                        filtered.add(app);
                    }
                }
            } catch (Exception e) {
                System.err.println("Error filtering by application type: " + e.getMessage());
            }
        }
        
        System.out.println("🔽 After application type filter (" + applicationTypeFilter + "): " + filtered.size() + " applications");
        return filtered;
    }
    
    /**
     * Get total count of pending leave requests for pagination
     */
    private int getTotalPendingRequestCount(String leaveTypeFilter, String employeeFilter, 
            String priorityFilter, String applicationTypeFilter, String fromDate, String toDate) throws SQLException {
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = ConnectionManager.getConnection();
            
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT COUNT(*) FROM leaveapplication la ");
            sql.append("JOIN employee e ON la.employeeid = e.employeeid ");
            sql.append("WHERE LOWER(la.leavestatus) = 'pending' ");
            
            List<Object> parameters = new ArrayList<>();
            
            // Add same filters as main query (excluding priority and applicationType which are calculated in memory)
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
            
            if (fromDate != null && !fromDate.trim().isEmpty()) {
                sql.append("AND la.appliedon >= TO_DATE(?, 'YYYY-MM-DD') ");
                parameters.add(fromDate.trim());
            }
            
            if (toDate != null && !toDate.trim().isEmpty()) {
                sql.append("AND la.appliedon <= TO_DATE(?, 'YYYY-MM-DD') + 1 ");
                parameters.add(toDate.trim());
            }
            
            pstmt = conn.prepareStatement(sql.toString());
            
            // Set parameters
            for (int i = 0; i < parameters.size(); i++) {
                pstmt.setObject(i + 1, parameters.get(i));
            }
            
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                int baseCount = rs.getInt(1);
                
                // If priority or application type filter is applied, we need to get all records and filter in memory
                // This is necessary because these filters require date calculations
                if ((priorityFilter != null && !priorityFilter.trim().isEmpty()) || 
                    (applicationTypeFilter != null && !applicationTypeFilter.trim().isEmpty())) {
                    
                    // Get all matching records (without priority/appType filters)
                    PendingRequestsResult allRequestsResult = getPendingLeaveRequestsWithEmployeeInfo(
                            leaveTypeFilter, employeeFilter, null, null, 
                            fromDate, toDate, 0, Integer.MAX_VALUE);
                    
                    List<LeaveApplication> filteredRequests = allRequestsResult.getRequests();
                    
                    // Apply priority filter if specified
                    if (priorityFilter != null && !priorityFilter.trim().isEmpty()) {
                        filteredRequests = applyPriorityFilter(filteredRequests, priorityFilter);
                    }
                    
                    // Apply application type filter if specified
                    if (applicationTypeFilter != null && !applicationTypeFilter.trim().isEmpty()) {
                        filteredRequests = applyApplicationTypeFilter(filteredRequests, applicationTypeFilter);
                    }
                    
                    return filteredRequests.size();
                }
                
                return baseCount;
            }
            
        } finally {
            if (rs != null) {
                try { rs.close(); } catch (SQLException e) { 
                    System.err.println("Error closing ResultSet: " + e.getMessage());
                }
            }
            if (pstmt != null) {
                try { pstmt.close(); } catch (SQLException e) { 
                    System.err.println("Error closing PreparedStatement: " + e.getMessage());
                }
            }
            if (conn != null) {
                try { conn.close(); } catch (SQLException e) { 
                    System.err.println("Error closing Connection: " + e.getMessage());
                }
            }
        }
        
        return 0;
    }
    
    /**
     * Get pending request statistics
     */
    private Map<String, Integer> getPendingRequestStatistics() throws SQLException {
        Map<String, Integer> stats = new HashMap<>();
        
        // Initialize with zeros
        stats.put("total", 0);
        stats.put("urgent", 0);
        stats.put("today", 0);
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = ConnectionManager.getConnection();
            
            String sql = "SELECT la.leavestartdate, la.appliedon " +
                        "FROM leaveapplication la " +
                        "WHERE LOWER(la.leavestatus) = 'pending'";
            
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String today = dateFormat.format(new java.util.Date());
            
            int totalCount = 0;
            int urgentCount = 0;
            int todayCount = 0;
            
            while (rs.next()) {
                totalCount++;
                
                // Check if urgent (within 3 days of start date)
                String startDate = rs.getString("leavestartdate");
                if (startDate != null && isUrgent(startDate)) {
                    urgentCount++;
                }
                
                // Check if applied today
                String appliedDate = rs.getString("appliedon");
                if (appliedDate != null && appliedDate.startsWith(today)) {
                    todayCount++;
                }
            }
            
            stats.put("total", totalCount);
            stats.put("urgent", urgentCount);
            stats.put("today", todayCount);
            
        } finally {
            if (rs != null) {
                try { rs.close(); } catch (SQLException e) { 
                    System.err.println("Error closing ResultSet: " + e.getMessage());
                }
            }
            if (pstmt != null) {
                try { pstmt.close(); } catch (SQLException e) { 
                    System.err.println("Error closing PreparedStatement: " + e.getMessage());
                }
            }
            if (conn != null) {
                try { conn.close(); } catch (SQLException e) { 
                    System.err.println("Error closing Connection: " + e.getMessage());
                }
            }
        }
        
        System.out.println("Pending Request Statistics: " + stats);
        return stats;
    }
    
    /**
     * Get leave type names for filtering dropdown and display
     */
    private Map<String, String> getLeaveTypeNames() throws SQLException {
        Map<String, String> leaveTypes = new HashMap<>();
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = ConnectionManager.getConnection();
            
            String sql = "SELECT leavetypeid, leavetypename FROM leavetype ORDER BY leavetypename";
            
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                leaveTypes.put(rs.getString("leavetypeid"), rs.getString("leavetypename"));
            }
            
        } finally {
            if (rs != null) {
                try { rs.close(); } catch (SQLException e) { 
                    System.err.println("Error closing ResultSet: " + e.getMessage());
                }
            }
            if (pstmt != null) {
                try { pstmt.close(); } catch (SQLException e) { 
                    System.err.println("Error closing PreparedStatement: " + e.getMessage());
                }
            }
            if (conn != null) {
                try { conn.close(); } catch (SQLException e) { 
                    System.err.println("Error closing Connection: " + e.getMessage());
                }
            }
        }
        
        System.out.println("Leave Types: " + leaveTypes);
        return leaveTypes;
    }
    
    /**
     * Check if a leave start date is urgent (within 3 days)
     */
    private boolean isUrgent(String startDate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            java.util.Date leaveStart = sdf.parse(startDate);
            java.util.Date today = new java.util.Date();
            
            // Strip time component for fair comparison
            String startDateStr = sdf.format(leaveStart);
            String todayStr = sdf.format(today);
            
            java.util.Date startDateOnly = sdf.parse(startDateStr);
            java.util.Date todayOnly = sdf.parse(todayStr);
            
            long diffInMillies = startDateOnly.getTime() - todayOnly.getTime();
            long diffInDays = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
            
            // Urgent if starting today, tomorrow, or within 3 days (0-3 days)
            return diffInDays >= 0 && diffInDays <= 3;
        } catch (ParseException e) {
            System.err.println("❌ Error parsing date: " + startDate);
            return false;
        } catch (Exception e) {
            System.err.println("❌ Unexpected error in isUrgent: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Calculate showing info string for pagination
     */
    private String calculateShowingInfo(int startIndex, int endIndex, int totalRecords) {
        if (totalRecords == 0) {
            return "Showing 0 of 0 requests";
        }
        return String.format("Showing %d-%d of %d requests", startIndex + 1, endIndex, totalRecords);
    }
    
    /**
     * Safely parse integer parameter with default value
     */
    private int getIntParameter(HttpServletRequest request, String paramName, int defaultValue) {
        String paramValue = request.getParameter(paramName);
        if (paramValue == null || paramValue.trim().isEmpty()) {
            return defaultValue;
        }
        
        try {
            int value = Integer.parseInt(paramValue);
            return value > 0 ? value : defaultValue;
        } catch (NumberFormatException e) {
            System.err.println("❌ Invalid integer parameter " + paramName + ": " + paramValue);
            return defaultValue;
        }
    }
}
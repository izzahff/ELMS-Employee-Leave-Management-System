package elms.controller;

import elms.DAO.LeaveApplicationDAO;
import elms.DAO.LeaveTypeDAO;
import elms.connection.ConnectionManager;
import elms.DAO.EmployeeDAO;
import elms.model.Employee;
import elms.model.LeaveApplication;
import elms.model.LeaveType;
import elms.controller.ManagerNotificationController;
import elms.DAO.LeaveBalanceDAO;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Date;

/**
 * ENHANCED: Controller for Employee Leave History with Filters and Pagination
 */
@WebServlet("/leave-history")
public class EmployeeLeaveHistoryController extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private LeaveApplicationDAO leaveApplicationDAO;
    private LeaveTypeDAO leaveTypeDAO;
    
    // Pagination settings
    private static final int DEFAULT_PAGE_SIZE = 10; // Number of records per page
    private static final int[] ALLOWED_PAGE_SIZES = {5, 10, 20, 50}; // Allowed page sizes
    
    @Override
    public void init() throws ServletException {
        super.init();
        System.out.println("=== INITIALIZING PAGINATED LEAVE HISTORY CONTROLLER ===");
        
        try {
            leaveApplicationDAO = new LeaveApplicationDAO();
            leaveTypeDAO = new LeaveTypeDAO();
            
            System.out.println("✅ LeaveHistoryController DAOs initialized successfully");
            
        } catch (Exception e) {
            System.err.println("❌ Error initializing LeaveHistoryController DAOs: " + e.getMessage());
            e.printStackTrace();
            throw new ServletException("Failed to initialize DAOs", e);
        }
        
        System.out.println("✅ Paginated LeaveHistoryController initialized successfully");
    }
    
    /**
     * Handle GET request - Display leave history page with filters and pagination
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        System.out.println("=== PAGINATED LEAVE HISTORY CONTROLLER GET REQUEST ===");
        System.out.println("Request URI: " + request.getRequestURI());
        
        HttpSession session = request.getSession(false);
        if (session == null) {
            System.out.println("❌ No session found, redirecting to login");
            response.sendRedirect(request.getContextPath() + "/Employee/EmployeeLogin.jsp");
            return;
        }
        
        Employee employee = (Employee) session.getAttribute("loggedInEmployee");
        if (employee == null) {
            System.out.println("❌ No logged in employee found, redirecting to login");
            response.sendRedirect(request.getContextPath() + "/Employee/EmployeeLogin.jsp");
            return;
        }
        
        System.out.println("✅ Employee authenticated: " + employee.getEmployeeName() + " (ID: " + employee.getEmployeeId() + ")");
        
        // Get filter parameters
        String statusFilter = request.getParameter("status");
        String leaveTypeFilter = request.getParameter("leaveType");
        String fromDate = request.getParameter("fromDate");
        String toDate = request.getParameter("toDate");
        
        // Get pagination parameters
        int currentPage = 1;
        int pageSize = DEFAULT_PAGE_SIZE;
        
        try {
            String pageParam = request.getParameter("page");
            if (pageParam != null && !pageParam.trim().isEmpty()) {
                currentPage = Integer.parseInt(pageParam);
                if (currentPage < 1) currentPage = 1;
            }
            
            String sizeParam = request.getParameter("size");
            if (sizeParam != null && !sizeParam.trim().isEmpty()) {
                int requestedSize = Integer.parseInt(sizeParam);
                // Validate page size
                for (int allowedSize : ALLOWED_PAGE_SIZES) {
                    if (allowedSize == requestedSize) {
                        pageSize = requestedSize;
                        break;
                    }
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("⚠️ Invalid pagination parameters, using defaults");
        }
        
        System.out.println("🔍 Applied Filters:");
        System.out.println("  Status: " + statusFilter);
        System.out.println("  Leave Type: " + leaveTypeFilter);
        System.out.println("  From Date: " + fromDate);
        System.out.println("  To Date: " + toDate);
        System.out.println("📄 Pagination:");
        System.out.println("  Current Page: " + currentPage);
        System.out.println("  Page Size: " + pageSize);
        
        try {
            // Load leave history data with filters and pagination
            loadLeaveHistoryDataWithPagination(request, employee, statusFilter, leaveTypeFilter, 
                                             fromDate, toDate, currentPage, pageSize);
            
            System.out.println("✅ Leave history data loaded successfully with pagination");
            
            // Forward to JSP
            request.getRequestDispatcher("/Employee/EmployeeLeaveHistory.jsp").forward(request, response);
            
        } catch (Exception e) {
            System.err.println("❌ Error loading leave history: " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("errorMessage", "An error occurred while loading leave history: " + e.getMessage());
            request.getRequestDispatcher("/Employee/EmployeeLeaveHistory.jsp").forward(request, response);
        }
    }
    
    /**
     * Handle POST request - Process actions like canceling applications
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        System.out.println("=== PAGINATED LEAVE HISTORY CONTROLLER POST REQUEST ===");
        
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendRedirect(request.getContextPath() + "/Employee/EmployeeLogin.jsp");
            return;
        }
        
        Employee employee = (Employee) session.getAttribute("loggedInEmployee");
        if (employee == null) {
            response.sendRedirect(request.getContextPath() + "/Employee/EmployeeLogin.jsp");
            return;
        }
        
        String action = request.getParameter("action");
        String applicationId = request.getParameter("applicationId");
        
        System.out.println("📋 POST Parameters:");
        System.out.println("  Action: " + action);
        System.out.println("  Application ID: " + applicationId);
        
        try {
            if ("cancel".equals(action) && applicationId != null) {
                String result = cancelLeaveApplication(applicationId, employee.getEmployeeId());
                
                if (result.startsWith("success:")) {
                    // Refresh employee data in session
                    Employee updatedEmployee = EmployeeDAO.getEmployeeByIdOnly(employee.getEmployeeId());
                    if (updatedEmployee != null) {
                        session.setAttribute("loggedInEmployee", updatedEmployee);
                        System.out.println("✅ Updated employee data in session");
                        
                        // Log all balance-tracking leave types
                        if (LeaveBalanceDAO.hasBalanceTrackingLeaveTypes()) {
                            java.util.Map<String, elms.DAO.LeaveBalanceDAO.BalanceInfo> allBalances = 
                                LeaveBalanceDAO.getAllBalances(employee.getEmployeeId());
                            
                            System.out.println("✅ Current Leave Balances:");
                            for (java.util.Map.Entry<String, elms.DAO.LeaveBalanceDAO.BalanceInfo> entry : allBalances.entrySet()) {
                                System.out.println("   " + entry.getKey() + ": " + entry.getValue().getAvailableDays() + " days available");
                            }
                        }
                    }
                    
                    // ✅ FIXED: Store in SESSION instead of REQUEST
                    session.setAttribute("successMessage", result.substring(8));
                    System.out.println("✅ Leave application cancelled successfully - message stored in session");
                } else {
                    // ✅ FIXED: Store in SESSION instead of REQUEST
                    session.setAttribute("errorMessage", result.startsWith("error:") ? result.substring(6) : result);
                    System.out.println("❌ Failed to cancel leave application: " + result);
                }
            } else {
                // ✅ FIXED: Store in SESSION instead of REQUEST
                session.setAttribute("errorMessage", "Invalid action or missing application ID");
                System.out.println("❌ Invalid action or missing parameters");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error processing leave history action: " + e.getMessage());
            e.printStackTrace();
            // ✅ FIXED: Store in SESSION instead of REQUEST
            session.setAttribute("errorMessage", "An unexpected error occurred: " + e.getMessage());
        }
        
        // ✅ FIXED: REDIRECT instead of FORWARD (preserves session attributes and triggers modal on page load)
        System.out.println("🔄 Redirecting to leave history page to display result modal");
        response.sendRedirect(request.getContextPath() + "/leave-history");
    }
    
    /**
     * ENHANCED: Load leave history data with filter support and pagination
     */
    private void loadLeaveHistoryDataWithPagination(HttpServletRequest request, Employee employee, 
                                                   String statusFilter, String leaveTypeFilter, 
                                                   String fromDate, String toDate, 
                                                   int currentPage, int pageSize) throws SQLException {
        System.out.println("=== LOADING LEAVE HISTORY DATA WITH PAGINATION ===");
        
        try {
            String employeeId = employee.getEmployeeId();
            System.out.println("🔍 Loading data for Employee ID: " + employeeId);
            
            // Get all leave applications for this employee first
            List<LeaveApplication> allApplications = leaveApplicationDAO.getLeaveApplicationsByEmployee(employeeId);
            
            if (allApplications == null) {
                System.out.println("⚠️ DAO returned null applications list");
                allApplications = new ArrayList<>();
            }
            
            System.out.println("📊 Found " + allApplications.size() + " total applications before filtering");
            
            // Apply filters
            List<LeaveApplication> filteredApplications = applyFilters(allApplications, statusFilter, leaveTypeFilter, fromDate, toDate);
            
            System.out.println("📊 Found " + filteredApplications.size() + " applications after filtering");
            
            // Calculate pagination
            int totalRecords = filteredApplications.size();
            int totalPages = (int) Math.ceil((double) totalRecords / pageSize);
            
            // Adjust current page if it's out of bounds
            if (currentPage > totalPages && totalPages > 0) {
                currentPage = totalPages;
            }
            if (currentPage < 1) {
                currentPage = 1;
            }
            
            // Calculate start and end indices for current page
            int startIndex = (currentPage - 1) * pageSize;
            int endIndex = Math.min(startIndex + pageSize, totalRecords);
            
            // Get the current page records
            List<LeaveApplication> pageApplications = new ArrayList<>();
            if (startIndex < totalRecords) {
                pageApplications = filteredApplications.subList(startIndex, endIndex);
            }
            
            fetchRejectionReasons(pageApplications);

            System.out.println("📄 Pagination Details:");
            System.out.println("  Total Records: " + totalRecords);
            System.out.println("  Total Pages: " + totalPages);
            System.out.println("  Current Page: " + currentPage);
            System.out.println("  Page Size: " + pageSize);
            System.out.println("  Start Index: " + startIndex);
            System.out.println("  End Index: " + endIndex);
            System.out.println("  Records on Current Page: " + pageApplications.size());
            
            // Get all leave types for mapping and filter dropdown
            System.out.println("🔍 Fetching leave types...");
            List<LeaveType> leaveTypes = leaveTypeDAO.getAllLeaveTypes();
            
            if (leaveTypes == null) {
                System.out.println("⚠️ DAO returned null leave types list");
                leaveTypes = new ArrayList<>();
            }
            
            System.out.println("📊 Found " + leaveTypes.size() + " leave types");
            
            // Create leave type mapping
            Map<String, String> leaveTypeNames = new HashMap<>();
            for (LeaveType lt : leaveTypes) {
                leaveTypeNames.put(lt.getLeaveTypeId(), lt.getLeaveTypeName());
                System.out.println("  📋 Leave Type: " + lt.getLeaveTypeName() + " (ID: " + lt.getLeaveTypeId() + ")");
            }
            
            // Calculate statistics from ALL filtered results (not just current page)
            int totalApplications = filteredApplications.size();
            int pendingCount = 0;
            int approvedCount = 0;
            int rejectedCount = 0;
            int cancelledCount = 0;
            int totalDaysUsed = 0;
            
            for (LeaveApplication app : filteredApplications) {
                String status = app.getLeavestatus();
                if ("Pending".equalsIgnoreCase(status)) {
                    pendingCount++;
                } else if ("Approved".equalsIgnoreCase(status)) {
                    approvedCount++;
                    totalDaysUsed += app.getLeaveduration();
                } else if ("Rejected".equalsIgnoreCase(status)) {
                    rejectedCount++;
                } else if ("Cancelled".equalsIgnoreCase(status)) {
                    cancelledCount++;
                }
            }
            
            System.out.println("📊 Filtered Leave Statistics:");
            System.out.println("  Total Applications: " + totalApplications);
            System.out.println("  Pending: " + pendingCount);
            System.out.println("  Approved: " + approvedCount);
            System.out.println("  Rejected: " + rejectedCount);
            System.out.println("  Cancelled: " + cancelledCount);
            System.out.println("  Total Days Used: " + totalDaysUsed);
            
            // Calculate pagination navigation
            int startPage = Math.max(1, currentPage - 2);
            int endPage = Math.min(totalPages, currentPage + 2);
            
            // Set request attributes
            request.setAttribute("allApplications", pageApplications); // Only current page records
            request.setAttribute("leaveTypeNames", leaveTypeNames);
            request.setAttribute("totalApplications", totalApplications);
            request.setAttribute("pendingCount", pendingCount);
            request.setAttribute("approvedCount", approvedCount);
            request.setAttribute("rejectedCount", rejectedCount);
            request.setAttribute("cancelledCount", cancelledCount);
            request.setAttribute("totalDaysUsed", totalDaysUsed);
            
            // Pagination attributes
            request.setAttribute("currentPage", currentPage);
            request.setAttribute("totalPages", totalPages);
            request.setAttribute("pageSize", pageSize);
            request.setAttribute("totalRecords", totalRecords);
            request.setAttribute("startPage", startPage);
            request.setAttribute("endPage", endPage);
            request.setAttribute("allowedPageSizes", ALLOWED_PAGE_SIZES);
            
            // Show range info (e.g., "Showing 1-10 of 25 results")
            String showingInfo = "";
            if (totalRecords > 0) {
                showingInfo = "Showing " + (startIndex + 1) + "-" + endIndex + " of " + totalRecords + " applications";
            } else {
                showingInfo = "No applications found";
            }
            request.setAttribute("showingInfo", showingInfo);
            
            System.out.println("✅ Leave history data loaded and set as request attributes");
            System.out.println("📄 " + showingInfo);
            
        } catch (SQLException e) {
            System.err.println("❌ Database error loading leave history: " + e.getMessage());
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            System.err.println("❌ Unexpected error loading leave history: " + e.getMessage());
            e.printStackTrace();
            throw new SQLException("Unexpected error loading leave history", e);
        }
    }
    
    /**
     * Apply filters to the applications list
     */
    private List<LeaveApplication> applyFilters(List<LeaveApplication> applications, 
                                              String statusFilter, String leaveTypeFilter, 
                                              String fromDate, String toDate) {
        System.out.println("🔍 Applying filters to " + applications.size() + " applications");
        
        List<LeaveApplication> filtered = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        
        for (LeaveApplication app : applications) {
            boolean includeApp = true;
            
            // Status filter
            if (statusFilter != null && !statusFilter.trim().isEmpty()) {
                if (!statusFilter.equalsIgnoreCase(app.getLeavestatus())) {
                    includeApp = false;
                }
            }
            
            // Leave type filter
            if (includeApp && leaveTypeFilter != null && !leaveTypeFilter.trim().isEmpty()) {
                if (!leaveTypeFilter.equals(app.getLeavetypeid())) {
                    includeApp = false;
                }
            }
            
            // Date range filter
            if (includeApp && (fromDate != null && !fromDate.trim().isEmpty())) {
                try {
                    Date filterFromDate = dateFormat.parse(fromDate);
                    Date appStartDate = dateFormat.parse(app.getLeavestartdate());
                    if (appStartDate.before(filterFromDate)) {
                        includeApp = false;
                    }
                } catch (ParseException e) {
                    System.err.println("❌ Error parsing from date: " + e.getMessage());
                }
            }
            
            if (includeApp && (toDate != null && !toDate.trim().isEmpty())) {
                try {
                    Date filterToDate = dateFormat.parse(toDate);
                    Date appStartDate = dateFormat.parse(app.getLeavestartdate());
                    if (appStartDate.after(filterToDate)) {
                        includeApp = false;
                    }
                } catch (ParseException e) {
                    System.err.println("❌ Error parsing to date: " + e.getMessage());
                }
            }
            
            if (includeApp) {
                filtered.add(app);
            }
        }
        
        System.out.println("✅ Filter applied: " + filtered.size() + " applications match criteria");
        return filtered;
    }
    
    /**
     * ENHANCED: Cancel leave application with comprehensive debugging
     * Replace your existing cancelLeaveApplication method with this version
     */
    public String cancelLeaveApplication(String applicationId, String employeeId) throws SQLException {
        System.out.println("=== CANCELLING LEAVE APPLICATION (ENHANCED DEBUG VERSION) ===");
        System.out.println("  Requested Application ID: '" + applicationId + "' (length: " + applicationId.length() + ")");
        System.out.println("  Requested Employee ID: '" + employeeId + "' (length: " + employeeId.length() + ")");
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = ConnectionManager.getConnection();
            if (conn == null) {
                throw new SQLException("Failed to establish database connection");
            }
            
            // Start transaction
            conn.setAutoCommit(false);
            
            // DEBUGGING STEP 1: Check if application exists at all (without employee check)
            System.out.println("\n🔍 DEBUG STEP 1: Checking if application exists in database...");
            String checkExistsSql = "SELECT applicationid, employeeid, leavestatus FROM leaveapplication WHERE applicationid = ?";
            pstmt = conn.prepareStatement(checkExistsSql);
            pstmt.setString(1, applicationId);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String dbApplicationId = rs.getString("applicationid");
                String dbEmployeeId = rs.getString("employeeid");
                String dbStatus = rs.getString("leavestatus");
                
                System.out.println("✅ Application EXISTS in database:");
                System.out.println("   DB Application ID: '" + dbApplicationId + "'");
                System.out.println("   DB Employee ID: '" + dbEmployeeId + "'");
                System.out.println("   DB Status: '" + dbStatus + "'");
                System.out.println("\n🔍 Comparing IDs:");
                System.out.println("   Requested Employee ID: '" + employeeId + "'");
                System.out.println("   Database Employee ID:  '" + dbEmployeeId + "'");
                System.out.println("   IDs Match: " + employeeId.equals(dbEmployeeId));
                System.out.println("   IDs Match (ignoring case): " + employeeId.equalsIgnoreCase(dbEmployeeId));
                
                // Check for whitespace issues
                if (!employeeId.equals(dbEmployeeId)) {
                    System.out.println("\n⚠️ EMPLOYEE ID MISMATCH DETECTED!");
                    System.out.println("   Requested (trimmed): '" + employeeId.trim() + "'");
                    System.out.println("   Database (trimmed):  '" + dbEmployeeId.trim() + "'");
                    System.out.println("   After trim match: " + employeeId.trim().equals(dbEmployeeId.trim()));
                }
            } else {
                System.out.println("❌ Application does NOT exist in database!");
                System.out.println("   Query executed: " + checkExistsSql);
                System.out.println("   Parameter: applicationId = '" + applicationId + "'");
                
                conn.rollback();
                rs.close();
                pstmt.close();
                return "error: Leave application not found in database";
            }
            
            rs.close();
            pstmt.close();
            
            // DEBUGGING STEP 2: Now check with employee ID
            System.out.println("\n🔍 DEBUG STEP 2: Fetching application with employee ownership verification...");
            
            String getAppSql = 
                "SELECT la.*, lt.leavetypename " +
                "FROM leaveapplication la " +
                "JOIN leavetype lt ON la.leavetypeid = lt.leavetypeid " +
                "WHERE la.applicationid = ? AND la.employeeid = ?";
            
            System.out.println("   SQL Query: " + getAppSql);
            System.out.println("   Parameter 1 (applicationId): '" + applicationId + "'");
            System.out.println("   Parameter 2 (employeeId): '" + employeeId + "'");
            
            pstmt = conn.prepareStatement(getAppSql);
            pstmt.setString(1, applicationId);
            pstmt.setString(2, employeeId);
            
            rs = pstmt.executeQuery();
            
            if (!rs.next()) {
                System.out.println("\n❌ OWNERSHIP VERIFICATION FAILED!");
                System.out.println("   The application exists but does not belong to this employee");
                System.out.println("   This means the employee ID in session doesn't match the application owner");
                
                conn.rollback();
                rs.close();
                pstmt.close();
                return "error: Leave application not found or access denied - You can only cancel your own applications";
            }
            
            String currentStatus = rs.getString("leavestatus");
            String leaveTypeName = rs.getString("leavetypename");
            double leaveDuration = rs.getDouble("leaveduration");
            
            System.out.println("\n✅ APPLICATION FOUND AND OWNERSHIP VERIFIED:");
            System.out.println("   Application ID: " + rs.getString("applicationid"));
            System.out.println("   Employee ID: " + rs.getString("employeeid"));
            System.out.println("   Leave Type: " + leaveTypeName);
            System.out.println("   Current Status: " + currentStatus);
            System.out.println("   Duration: " + leaveDuration + " days");
            
            // Check if application can be cancelled
            if (!"Pending".equalsIgnoreCase(currentStatus)) {
                conn.rollback();
                System.out.println("\n❌ CANNOT CANCEL - Invalid Status");
                System.out.println("   Current Status: " + currentStatus);
                System.out.println("   Required Status: Pending");
                System.out.println("   Only pending applications can be cancelled");
                
                rs.close();
                pstmt.close();
                return "error: Only pending applications can be cancelled. Current status: " + currentStatus;
            }
            
            rs.close();
            pstmt.close();
            
            // Step 3: Update application status to Cancelled
            System.out.println("\n🔄 STEP 3: Updating application status to Cancelled...");
            String updateStatusSql = "UPDATE leaveapplication SET leavestatus = ? WHERE applicationid = ?";
            pstmt = conn.prepareStatement(updateStatusSql);
            pstmt.setString(1, "Cancelled");
            pstmt.setString(2, applicationId);
            
            int statusUpdateRows = pstmt.executeUpdate();
            pstmt.close();
            
            if (statusUpdateRows == 0) {
                conn.rollback();
                System.out.println("❌ Failed to update application status");
                return "error: Failed to update application status";
            }
            
            System.out.println("✅ Application status updated to Cancelled");
            System.out.println("   Rows affected: " + statusUpdateRows);
            
            // Step 4: Determine success message
            boolean isAnnualLeave = "Annual Leave".equalsIgnoreCase(leaveTypeName);
            String resultMessage;
            
            if (isAnnualLeave) {
                System.out.println("ℹ️ Annual Leave: No balance restoration needed (no deduction on submission)");
                resultMessage = "Annual Leave application cancelled successfully. Your leave balance remains unchanged since no deduction was made during submission.";
            } else {
                System.out.println("ℹ️ " + leaveTypeName + ": No balance changes needed");
                resultMessage = leaveTypeName + " application cancelled successfully. No leave balance changes required.";
            }
            
            // Commit transaction
            conn.commit();
            System.out.println("\n✅ TRANSACTION COMMITTED SUCCESSFULLY");
            System.out.println("✅ Leave application cancelled successfully!");
            
            return "success: " + resultMessage;
            
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                    System.out.println("\n🔄 Transaction rolled back due to error");
                } catch (SQLException ex) {
                    System.err.println("❌ Error during rollback: " + ex.getMessage());
                }
            }
            System.err.println("\n❌ SQL ERROR in cancelLeaveApplication:");
            System.err.println("   Message: " + e.getMessage());
            System.err.println("   SQL State: " + e.getSQLState());
            System.err.println("   Error Code: " + e.getErrorCode());
            e.printStackTrace();
            throw e;
            
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                System.err.println("❌ Error closing resources: " + e.getMessage());
            }
        }
    }
    
    private void fetchRejectionReasons(List<LeaveApplication> applications) throws SQLException {
        if (applications == null || applications.isEmpty()) {
            return;
        }
        
        System.out.println("🔍 Fetching rejection reasons for applications...");
        
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT applicationid, rejectreason FROM leaveapplication WHERE applicationid IN (");
        
        // Build IN clause with application IDs
        for (int i = 0; i < applications.size(); i++) {
            if (i > 0) sql.append(",");
            sql.append("?");
        }
        sql.append(") AND rejectreason IS NOT NULL");
        
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            
            // Set application IDs as parameters
            for (int i = 0; i < applications.size(); i++) {
                pstmt.setString(i + 1, applications.get(i).getApplicationid());
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                Map<String, String> rejectionReasons = new HashMap<>();
                
                while (rs.next()) {
                    rejectionReasons.put(rs.getString("applicationid"), rs.getString("rejectreason"));
                }
                
                // Set rejection reasons in applications
                for (LeaveApplication app : applications) {
                    String reason = rejectionReasons.get(app.getApplicationid());
                    if (reason != null) {
                        app.setRejectReason(reason);
                    }
                }
                
                System.out.println("✅ Fetched rejection reasons for " + rejectionReasons.size() + " applications");
            }
        }
    }
    
    @Override
    public void destroy() {
        System.out.println("=== DESTROYING PAGINATED LEAVE HISTORY CONTROLLER ===");
        super.destroy();
    }
}
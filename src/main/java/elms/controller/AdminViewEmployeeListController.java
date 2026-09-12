package elms.controller;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import elms.model.Employee;
import elms.connection.ConnectionManager;

@WebServlet("/Admin/AdminViewEmployeeListController")
public class AdminViewEmployeeListController extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final int RECORDS_PER_PAGE = 10;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Check if admin is logged in
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("adminId") == null) {
            response.sendRedirect("/ELMS_3.0/Admin/AdminLogin.jsp");
            return;
        }

        // Check if this is a request for employee details (AJAX call)
        String action = request.getParameter("action");
        if ("getEmployeeDetails".equals(action)) {
            handleGetEmployeeDetails(request, response);
            return;
        }

        // Otherwise, handle the normal employee list view
        try {
            // Get search parameter
            String searchTerm = request.getParameter("search");
            if (searchTerm != null) {
                searchTerm = searchTerm.trim();
                if (searchTerm.isEmpty()) {
                    searchTerm = null;
                }
            }

            // Get page parameter
            int currentPage = 1;
            String pageParam = request.getParameter("page");
            if (pageParam != null && !pageParam.isEmpty()) {
                try {
                    currentPage = Integer.parseInt(pageParam);
                    if (currentPage < 1) {
                        currentPage = 1;
                    }
                } catch (NumberFormatException e) {
                    currentPage = 1;
                }
            }

            // Calculate offset for pagination
            int offset = (currentPage - 1) * RECORDS_PER_PAGE;

            // Get employee list and total count
            List<Employee> employeeList = getEmployeeListWithRobustPagination(searchTerm, offset, RECORDS_PER_PAGE);
            int totalRecords = getTotalEmployeeCount(searchTerm);
            int totalPages = (int) Math.ceil((double) totalRecords / RECORDS_PER_PAGE);

            // Set attributes for JSP
            request.setAttribute("employeeList", employeeList);
            request.setAttribute("currentPage", currentPage);
            request.setAttribute("totalPages", totalPages);
            request.setAttribute("totalRecords", totalRecords);
            request.setAttribute("searchTerm", searchTerm);

            // Set info message if no employees found
            if (employeeList.isEmpty()) {
                if (searchTerm != null) {
                    request.setAttribute("infoMessage", 
                        "No employees found matching your search criteria: \"" + searchTerm + "\"");
                } else {
                    request.setAttribute("infoMessage", 
                        "No employees found in the system. Add employees to get started.");
                }
            }

            // Forward to JSP
            request.getRequestDispatcher("/Admin/AdminViewEmployeeList.jsp")
                   .forward(request, response);

        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", 
                "Database error occurred while retrieving employee list: " + e.getMessage());
            request.getRequestDispatcher("/Admin/AdminViewEmployeeList.jsp")
                   .forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", 
                "An unexpected error occurred: " + e.getMessage());
            request.getRequestDispatcher("/Admin/AdminViewEmployeeList.jsp")
                   .forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        doGet(request, response);
    }

    /**
     * NEW METHOD: Handle AJAX request for employee details
     */
    private void handleGetEmployeeDetails(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String employeeId = request.getParameter("employeeId");
        
        if (employeeId == null || employeeId.trim().isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Employee ID is required");
            return;
        }

        try {
            // Get employee details
            Employee employee = getEmployeeDetails(employeeId);
            
            if (employee == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Employee not found");
                return;
            }

            // Get leave balance information
            List<Map<String, Object>> leaveBalances = getEmployeeLeaveBalances(employeeId);
            
            // Get application statistics
            Map<String, Integer> applicationStats = getApplicationStatistics(employeeId);

            // Set attributes
            request.setAttribute("employee", employee);
            request.setAttribute("leaveBalances", leaveBalances);
            request.setAttribute("applicationStats", applicationStats);

            // Forward to modal JSP
            request.getRequestDispatcher("/Admin/EmployeeDetailsModal.jsp")
                   .forward(request, response);

        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                "Database error: " + e.getMessage());
        }
    }

    /**
     * NEW METHOD: Get employee details
     */
    private Employee getEmployeeDetails(String employeeId) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = ConnectionManager.getConnection();
            String sql = "SELECT employeeid, employeename, employeeemail, employeenophone, " +
                        "profile_picture_path FROM employee WHERE employeeid = ?";
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, employeeId);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Employee employee = new Employee();
                employee.setEmployeeId(rs.getString("employeeid"));
                employee.setEmployeeName(rs.getString("employeename"));
                employee.setEmployeeEmail(rs.getString("employeeemail"));
                employee.setEmployeeNoPhone(rs.getString("employeenophone"));
                employee.setProfilePicturePath(rs.getString("profile_picture_path"));
                return employee;
            }
            
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { }
            if (pstmt != null) try { pstmt.close(); } catch (SQLException e) { }
            if (conn != null) try { conn.close(); } catch (SQLException e) { }
        }
        
        return null;
    }

    /**
     * NEW METHOD: Get employee leave balances
     */
    /**
     * Get employee leave balances - FIXED VERSION
     */
    /**
     * Get employee leave balances for ALL leave types (tracked and non-tracked)
     */
    private List<Map<String, Object>> getEmployeeLeaveBalances(String employeeId) throws SQLException {
        List<Map<String, Object>> balances = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = ConnectionManager.getConnection();
            
            // Get ALL leave types, not just tracked ones
            String sql = 
                "SELECT lt.leavetypeid, lt.leavetypename, lt.leavetypecategory, lt.affectsbalance, " +
                "fd.standardduration, " +
                "COALESCE(SUM(CASE WHEN la.leavestatus = 'Approved' THEN la.leaveduration ELSE 0 END), 0) as used_days, " +
                "COALESCE(SUM(CASE WHEN la.leavestatus = 'Pending' THEN la.leaveduration ELSE 0 END), 0) as pending_days, " +
                "COUNT(la.leavetypeid) as total_applications " +
                "FROM leavetype lt " +
                "LEFT JOIN fullday fd ON lt.leavetypeid = fd.leavetypeid " +
                "LEFT JOIN leaveapplication la ON lt.leavetypeid = la.leavetypeid AND la.employeeid = ? " +
                "GROUP BY lt.leavetypeid, lt.leavetypename, lt.leavetypecategory, lt.affectsbalance, fd.standardduration " +
                "ORDER BY lt.affectsbalance DESC, lt.leavetypename";
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, employeeId);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> balance = new HashMap<>();
                balance.put("leaveTypeId", rs.getString("leavetypeid"));
                balance.put("leaveTypeName", rs.getString("leavetypename"));
                balance.put("category", rs.getString("leavetypecategory"));
                balance.put("affectsBalance", rs.getInt("affectsbalance"));
                balance.put("totalApplications", rs.getInt("total_applications"));
                balance.put("usedDays", rs.getDouble("used_days"));
                balance.put("pendingDays", rs.getDouble("pending_days"));
                
                // Only set balance-related fields for tracked leave types
                if (rs.getInt("affectsbalance") == 1 && rs.getObject("standardduration") != null) {
                    balance.put("standardDuration", rs.getDouble("standardduration"));
                    balance.put("availableDays", rs.getDouble("standardduration") - rs.getDouble("used_days"));
                } else {
                    balance.put("standardDuration", null);
                    balance.put("availableDays", null);
                }
                
                balances.add(balance);
            }
            
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { }
            if (pstmt != null) try { pstmt.close(); } catch (SQLException e) { }
            if (conn != null) try { conn.close(); } catch (SQLException e) { }
        }
        
        return balances;
    }

    /**
     * NEW METHOD: Get application statistics
     */
    private Map<String, Integer> getApplicationStatistics(String employeeId) throws SQLException {
        Map<String, Integer> stats = new HashMap<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = ConnectionManager.getConnection();
            
            String sql = 
                "SELECT " +
                "COUNT(*) as total_applications, " +
                "SUM(CASE WHEN leavestatus = 'Pending' THEN 1 ELSE 0 END) as pending, " +
                "SUM(CASE WHEN leavestatus = 'Approved' THEN 1 ELSE 0 END) as approved, " +
                "SUM(CASE WHEN leavestatus = 'Rejected' THEN 1 ELSE 0 END) as rejected, " +
                "SUM(CASE WHEN leavestatus = 'Cancelled' THEN 1 ELSE 0 END) as cancelled " +
                "FROM leaveapplication WHERE employeeid = ?";
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, employeeId);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                stats.put("total", rs.getInt("total_applications"));
                stats.put("pending", rs.getInt("pending"));
                stats.put("approved", rs.getInt("approved"));
                stats.put("rejected", rs.getInt("rejected"));
                stats.put("cancelled", rs.getInt("cancelled"));
            }
            
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { }
            if (pstmt != null) try { pstmt.close(); } catch (SQLException e) { }
            if (conn != null) try { conn.close(); } catch (SQLException e) { }
        }
        
        return stats;
    }

    /**
     * Robust pagination that prevents duplicates and missing records
     */
    private List<Employee> getEmployeeListWithRobustPagination(String searchTerm, int offset, int limit) throws SQLException {
        List<Employee> employeeList = new ArrayList<>();
        
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM ( ");
        sql.append("  SELECT emp.*, ROW_NUMBER() OVER ( ");
        sql.append("    ORDER BY employeeid ASC, employeename ASC, ROWID ASC ");
        sql.append("  ) AS rn FROM ( ");
        sql.append("    SELECT DISTINCT employeeid, employeename, employeeemail, employeenophone, ");
        sql.append("           employeepassword, profile_picture_path ");
        sql.append("    FROM employee ");
        sql.append("    WHERE 1=1 ");

        if (searchTerm != null && !searchTerm.isEmpty()) {
            sql.append("    AND (UPPER(employeename) LIKE UPPER(?) ");
            sql.append("    OR UPPER(employeeemail) LIKE UPPER(?) ");
            sql.append("    OR UPPER(employeeid) LIKE UPPER(?) ");
            sql.append("    OR UPPER(employeenophone) LIKE UPPER(?)) ");
        }

        sql.append("  ) emp ");
        sql.append(") ");
        sql.append("WHERE rn > ? AND rn <= ?");

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = ConnectionManager.getConnection();
            pstmt = conn.prepareStatement(sql.toString());
            
            int paramIndex = 1;
            
            if (searchTerm != null && !searchTerm.isEmpty()) {
                String searchPattern = "%" + searchTerm + "%";
                pstmt.setString(paramIndex++, searchPattern);
                pstmt.setString(paramIndex++, searchPattern);
                pstmt.setString(paramIndex++, searchPattern);
                pstmt.setString(paramIndex++, searchPattern);
            }
            
            pstmt.setInt(paramIndex++, offset);
            pstmt.setInt(paramIndex, offset + limit);

            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Employee employee = new Employee();
                employee.setEmployeeId(rs.getString("employeeid"));
                employee.setEmployeeName(rs.getString("employeename"));
                employee.setEmployeeEmail(rs.getString("employeeemail"));
                employee.setEmployeeNoPhone(rs.getString("employeenophone"));
                employee.setEmployeePassword(rs.getString("employeepassword"));
                employee.setProfilePicturePath(rs.getString("profile_picture_path"));
                
                employeeList.add(employee);
            }
            
        } catch (SQLException e) {
            return getEmployeeListWithRownum(searchTerm, offset, limit);
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { }
            if (pstmt != null) try { pstmt.close(); } catch (SQLException e) { }
            if (conn != null) try { conn.close(); } catch (SQLException e) { }
        }
        
        return employeeList;
    }

    /**
     * Fallback method using ROWNUM
     */
    private List<Employee> getEmployeeListWithRownum(String searchTerm, int offset, int limit) throws SQLException {
        List<Employee> employeeList = new ArrayList<>();
        
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM ( ");
        sql.append("  SELECT emp.*, ROWNUM rnum FROM ( ");
        sql.append("    SELECT DISTINCT employeeid, employeename, employeeemail, employeenophone, ");
        sql.append("           employeepassword,  profile_picture_path ");
        sql.append("    FROM employee ");
        sql.append("    WHERE 1=1 ");

        if (searchTerm != null && !searchTerm.isEmpty()) {
            sql.append("    AND (UPPER(employeename) LIKE UPPER(?) ");
            sql.append("    OR UPPER(employeeemail) LIKE UPPER(?) ");
            sql.append("    OR UPPER(employeeid) LIKE UPPER(?) ");
            sql.append("    OR UPPER(employeenophone) LIKE UPPER(?)) ");
        }

        sql.append("    ORDER BY employeeid ASC, employeename ASC ");
        sql.append("  ) emp ");
        sql.append("  WHERE ROWNUM <= ? ");
        sql.append(") ");
        sql.append("WHERE rnum > ?");

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = ConnectionManager.getConnection();
            pstmt = conn.prepareStatement(sql.toString());
            
            int paramIndex = 1;
            
            if (searchTerm != null && !searchTerm.isEmpty()) {
                String searchPattern = "%" + searchTerm + "%";
                pstmt.setString(paramIndex++, searchPattern);
                pstmt.setString(paramIndex++, searchPattern);
                pstmt.setString(paramIndex++, searchPattern);
                pstmt.setString(paramIndex++, searchPattern);
            }
            
            pstmt.setInt(paramIndex++, offset + limit);
            pstmt.setInt(paramIndex, offset);

            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Employee employee = new Employee();
                employee.setEmployeeId(rs.getString("employeeid"));
                employee.setEmployeeName(rs.getString("employeename"));
                employee.setEmployeeEmail(rs.getString("employeeemail"));
                employee.setEmployeeNoPhone(rs.getString("employeenophone"));
                employee.setEmployeePassword(rs.getString("employeepassword"));
                employee.setProfilePicturePath(rs.getString("profile_picture_path"));
                
                employeeList.add(employee);
            }
            
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { }
            if (pstmt != null) try { pstmt.close(); } catch (SQLException e) { }
            if (conn != null) try { conn.close(); } catch (SQLException e) { }
        }
        
        return employeeList;
    }

    /**
     * Gets total count of employees
     */
    private int getTotalEmployeeCount(String searchTerm) throws SQLException {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(DISTINCT employeeid) FROM employee WHERE 1=1 ");

        if (searchTerm != null && !searchTerm.isEmpty()) {
            sql.append("AND (UPPER(employeename) LIKE UPPER(?) ");
            sql.append("OR UPPER(employeeemail) LIKE UPPER(?) ");
            sql.append("OR UPPER(employeeid) LIKE UPPER(?) ");
            sql.append("OR UPPER(employeenophone) LIKE UPPER(?)) ");
        }

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = ConnectionManager.getConnection();
            pstmt = conn.prepareStatement(sql.toString());
            
            if (searchTerm != null && !searchTerm.isEmpty()) {
                String searchPattern = "%" + searchTerm + "%";
                pstmt.setString(1, searchPattern);
                pstmt.setString(2, searchPattern);
                pstmt.setString(3, searchPattern);
                pstmt.setString(4, searchPattern);
            }

            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
            
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { }
            if (pstmt != null) try { pstmt.close(); } catch (SQLException e) { }
            if (conn != null) try { conn.close(); } catch (SQLException e) { }
        }
        
        return 0;
    }
}
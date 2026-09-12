package elms.controller;

import elms.DAO.ManagerDAO;
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
import java.util.*;

@WebServlet("/executive-director-project-managers")
public class DirectorViewManagerListController extends HttpServlet {
    
    private static final long serialVersionUID = 1L;
    private static final int DEFAULT_PAGE_SIZE = 10;
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        System.out.println("=== Director View Manager List Controller - GET ===");
        
        HttpSession session = request.getSession();
        Manager director = (Manager) session.getAttribute("manager");
        String userType = (String) session.getAttribute("userType");
        
        // Check if user is logged in and is executive director
        if (director == null || !"executive_director".equals(userType)) {
            System.out.println("❌ Unauthorized access attempt - redirecting to login");
            response.sendRedirect(request.getContextPath() + "/Manager/ManagerLogin.jsp");
            return;
        }
        
        System.out.println("✅ Executive Director logged in: " + director.getManagername());
        
        try {
            // Get search parameter
            String search = request.getParameter("search");
            if (search != null) {
                search = search.trim();
                if (search.isEmpty()) {
                    search = null;
                }
            }
            
            // Get pagination parameters
            int page = getIntParameter(request, "page", 1);
            int pageSize = DEFAULT_PAGE_SIZE;
            
            System.out.println("📋 Request Parameters:");
            System.out.println("  Search: " + search);
            System.out.println("  Page: " + page);
            System.out.println("  Page Size: " + pageSize);
            
            // Get project managers with search and pagination
            List<Manager> projectManagers = getProjectManagers(search, page, pageSize);
            
            // Get total count for pagination
            int totalRecords = getTotalProjectManagerCount(search);
            int totalPages = (int) Math.ceil((double) totalRecords / pageSize);
            
            // Ensure page is within valid range
            if (page < 1) page = 1;
            if (page > totalPages && totalPages > 0) page = totalPages;
            
            // Enhance manager objects with display properties
            enhanceManagerDisplayProperties(projectManagers);
            
            System.out.println("📊 Results:");
            System.out.println("  Total Project Managers: " + totalRecords);
            System.out.println("  Current Page: " + page + "/" + totalPages);
            System.out.println("  Managers on this page: " + projectManagers.size());
            
            // Set attributes for JSP
            request.setAttribute("managerList", projectManagers);
            request.setAttribute("currentPage", page);
            request.setAttribute("totalPages", totalPages);
            request.setAttribute("totalRecords", totalRecords);
            
            // Add info message if search was performed
            if (search != null && !search.isEmpty()) {
                String message = String.format("Found %d project manager(s) matching '%s'", totalRecords, search);
                request.setAttribute("infoMessage", message);
            }
            
            // Forward to JSP
            request.getRequestDispatcher("/Manager/DirectorViewManagerList.jsp").forward(request, response);
            
        } catch (SQLException e) {
            System.err.println("❌ Database error in DirectorViewManagerListController: " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("errorMessage", "Database error occurred while loading project managers");
            request.getRequestDispatcher("/ExecutiveDirector/DirectorViewManagerList.jsp").forward(request, response);
        } catch (Exception e) {
            System.err.println("❌ Unexpected error in DirectorViewManagerListController: " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("errorMessage", "An unexpected error occurred");
            request.getRequestDispatcher("/Manager/DirectorViewManagerList.jsp").forward(request, response);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        // Redirect POST to GET for this controller
        doGet(request, response);
    }
    
    /**
     * Get project managers with search and pagination
     */
    private List<Manager> getProjectManagers(String search, int page, int pageSize) throws SQLException {
        List<Manager> managers = new ArrayList<>();
        
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM ( ");
        sql.append("  SELECT ROWNUM rnum, m.* FROM ( ");
        sql.append("    SELECT managerid, managername, manageremail, managernophone, ");
        sql.append("           managerposition, profile_picture_path ");
        sql.append("    FROM manager ");
        sql.append("    WHERE UPPER(managerid) LIKE 'PM%' "); // Only Project Managers
        
        List<Object> parameters = new ArrayList<>();
        
        // Add search filter
        if (search != null && !search.isEmpty()) {
            sql.append("    AND (LOWER(managername) LIKE LOWER(?) ");
            sql.append("         OR LOWER(manageremail) LIKE LOWER(?) ");
            sql.append("         OR LOWER(managerid) LIKE LOWER(?) ");
            sql.append("         OR LOWER(managerposition) LIKE LOWER(?)) ");
            
            String searchPattern = "%" + search + "%";
            parameters.add(searchPattern);
            parameters.add(searchPattern);
            parameters.add(searchPattern);
            parameters.add(searchPattern);
        }
        
        sql.append("    ORDER BY managername ASC ");
        sql.append("  ) m ");
        sql.append("  WHERE ROWNUM <= ? ");
        sql.append(") ");
        sql.append("WHERE rnum > ?");
        
        // Add pagination parameters
        int offset = (page - 1) * pageSize;
        parameters.add(offset + pageSize);
        parameters.add(offset);
        
        System.out.println("SQL Query: " + sql.toString());
        System.out.println("Parameters: " + parameters);
        
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            
            // Set parameters
            for (int i = 0; i < parameters.size(); i++) {
                pstmt.setObject(i + 1, parameters.get(i));
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Manager manager = new Manager();
                    manager.setManagerid(rs.getString("managerid"));
                    manager.setManagername(rs.getString("managername"));
                    manager.setManageremail(rs.getString("manageremail"));
                    manager.setManagernophone(rs.getString("managernophone"));
                    manager.setManagerposition(rs.getString("managerposition"));
                    manager.setProfilePicturePath(rs.getString("profile_picture_path"));
                    
                    managers.add(manager);
                }
            }
        }
        
        System.out.println("Retrieved " + managers.size() + " project managers");
        return managers;
    }
    
    /**
     * Get total count of project managers for pagination
     */
    private int getTotalProjectManagerCount(String search) throws SQLException {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(*) FROM manager ");
        sql.append("WHERE UPPER(managerid) LIKE 'PM%' "); // Only Project Managers
        
        List<Object> parameters = new ArrayList<>();
        
        // Add search filter
        if (search != null && !search.isEmpty()) {
            sql.append("AND (LOWER(managername) LIKE LOWER(?) ");
            sql.append("     OR LOWER(manageremail) LIKE LOWER(?) ");
            sql.append("     OR LOWER(managerid) LIKE LOWER(?) ");
            sql.append("     OR LOWER(managerposition) LIKE LOWER(?)) ");
            
            String searchPattern = "%" + search + "%";
            parameters.add(searchPattern);
            parameters.add(searchPattern);
            parameters.add(searchPattern);
            parameters.add(searchPattern);
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
     * Enhance manager objects with display properties for JSP
     */
    private void enhanceManagerDisplayProperties(List<Manager> managers) {
        System.out.println("=== Enhancing Manager Display Properties ===");
        
        for (Manager manager : managers) {
            // Set default position if null or empty
            if (manager.getManagerposition() == null || manager.getManagerposition().trim().isEmpty()) {
                manager.setManagerposition("Project Manager");
            }
            
            // DEBUG: Log current profile picture path
            String profilePath = manager.getProfilePicturePath();
            System.out.println("Manager: " + manager.getManagername() + 
                             ", Profile Path: '" + profilePath + "'");
            
            // DON'T modify the profile picture path here!
            // Let the JSP handle path construction with pageContext.request.contextPath
        }
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
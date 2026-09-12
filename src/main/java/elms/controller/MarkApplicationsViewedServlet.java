package elms.controller;

import elms.model.Employee;
import elms.controller.ManagerApproveLeaveController;

import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Servlet to handle marking applications as viewed (removing the "new update" highlighting)
 */
@WebServlet("/mark-applications-viewed")
public class MarkApplicationsViewedServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        System.out.println("=== MARK APPLICATIONS AS VIEWED ===");
        
        // Set response type to JSON
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        try {
            // Check if employee is logged in
            HttpSession session = request.getSession(false);
            if (session == null) {
                System.out.println("❌ No session found");
                out.print("{\"success\": false, \"message\": \"Session expired. Please login again.\"}");
                return;
            }
            
            Employee employee = (Employee) session.getAttribute("loggedInEmployee");
            if (employee == null) {
                System.out.println("❌ No employee found in session");
                out.print("{\"success\": false, \"message\": \"Employee not found. Please login again.\"}");
                return;
            }
            
            String action = request.getParameter("action");
            String employeeId = employee.getEmployeeId();
            
            System.out.println("Action: " + action);
            System.out.println("Employee ID: " + employeeId);
            
            switch (action) {
                case "markViewed":
                    handleMarkSingleAsViewed(request, employeeId, out);
                    break;
                    
                case "markAllViewed":
                    handleMarkAllAsViewed(employeeId, out);
                    break;
                    
                case "acknowledgeUpdate":
                    handleAcknowledgeUpdate(request, employeeId, out);
                    break;
                    
                default:
                    System.out.println("❌ Invalid action: " + action);
                    out.print("{\"success\": false, \"message\": \"Invalid action specified\"}");
                    break;
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error in mark applications viewed handler: " + e.getMessage());
            e.printStackTrace();
            out.print("{\"success\": false, \"message\": \"System error occurred\"}");
        }
        
        out.flush();
    }
    
    /**
     * Mark a specific application as viewed (when employee clicks View button)
     */
    private void handleMarkSingleAsViewed(HttpServletRequest request, String employeeId, PrintWriter out) {
        String applicationId = request.getParameter("applicationId");
        
        if (applicationId == null || applicationId.trim().isEmpty()) {
            System.out.println("❌ Application ID is required");
            out.print("{\"success\": false, \"message\": \"Application ID is required\"}");
            return;
        }
        
        applicationId = applicationId.trim();
        System.out.println("📋 Marking application as viewed: " + applicationId + " for employee: " + employeeId);
        
        // Remove the application from the newly updated list
        boolean removed = ManagerApproveLeaveController.markApplicationAsViewed(employeeId, applicationId);
        
        if (removed) {
            System.out.println("✅ Application " + applicationId + " marked as viewed and removed from new updates");
            
            // Get updated count after removal
            int remainingCount = ManagerApproveLeaveController.getNewlyUpdatedCount(employeeId);
            
            out.print("{\"success\": true, \"message\": \"Application marked as viewed\", \"remainingCount\": " + remainingCount + "}");
        } else {
            System.out.println("⚠️ Application " + applicationId + " was not in the newly updated list");
            out.print("{\"success\": true, \"message\": \"Application was already viewed\", \"remainingCount\": 0}");
        }
    }
    
    /**
     * Mark all applications as viewed for the employee (when employee clicks "Mark All Viewed")
     */
    private void handleMarkAllAsViewed(String employeeId, PrintWriter out) {
        System.out.println("📋 Marking all applications as viewed for employee: " + employeeId);
        
        // Get count before clearing
        int countBeforeClear = ManagerApproveLeaveController.getNewlyUpdatedCount(employeeId);
        
        // Clear all newly updated applications for this employee
        ManagerApproveLeaveController.clearAllNewlyUpdatedForEmployee(employeeId);
        
        System.out.println("✅ " + countBeforeClear + " applications marked as viewed for employee: " + employeeId);
        out.print("{\"success\": true, \"message\": \"All applications marked as viewed\", \"clearedCount\": " + countBeforeClear + "}");
    }
    
    /**
     * Handle acknowledgment of a specific update (alternative to markViewed)
     */
    private void handleAcknowledgeUpdate(HttpServletRequest request, String employeeId, PrintWriter out) {
        String applicationId = request.getParameter("applicationId");
        
        if (applicationId == null || applicationId.trim().isEmpty()) {
            System.out.println("❌ Application ID is required for acknowledgment");
            out.print("{\"success\": false, \"message\": \"Application ID is required\"}");
            return;
        }
        
        applicationId = applicationId.trim();
        System.out.println("📋 Acknowledging update for application: " + applicationId + " by employee: " + employeeId);
        
        // Remove the application from the newly updated list
        boolean acknowledged = ManagerApproveLeaveController.markApplicationAsViewed(employeeId, applicationId);
        
        if (acknowledged) {
            System.out.println("✅ Update acknowledged for application: " + applicationId);
            
            // Get updated count
            int remainingCount = ManagerApproveLeaveController.getNewlyUpdatedCount(employeeId);
            
            out.print("{\"success\": true, \"message\": \"Update acknowledged\", \"remainingCount\": " + remainingCount + "}");
        } else {
            System.out.println("⚠️ Update was already acknowledged for application: " + applicationId);
            out.print("{\"success\": true, \"message\": \"Update was already acknowledged\", \"remainingCount\": 0}");
        }
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // GET requests can be used to retrieve newly updated application data
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        try {
            HttpSession session = request.getSession(false);
            if (session == null) {
                System.out.println("❌ No session found for GET request");
                out.print("{\"success\": false, \"message\": \"Session expired\"}");
                return;
            }
            
            Employee employee = (Employee) session.getAttribute("loggedInEmployee");
            if (employee == null) {
                System.out.println("❌ No employee found in session for GET request");
                out.print("{\"success\": false, \"message\": \"Employee not found\"}");
                return;
            }
            
            String action = request.getParameter("action");
            String employeeId = employee.getEmployeeId();
            
            System.out.println("📋 GET request - Action: " + action + ", Employee: " + employeeId);
            
            switch (action) {
                case "getNewUpdatesCount":
                    handleGetNewUpdatesCount(employeeId, out);
                    break;
                    
                case "getNewUpdatedApplications":
                    handleGetNewUpdatedApplications(employeeId, out);
                    break;
                    
                case "checkApplicationStatus":
                    handleCheckApplicationStatus(request, employeeId, out);
                    break;
                    
                default:
                    System.out.println("❌ Invalid GET action: " + action);
                    out.print("{\"success\": false, \"message\": \"Invalid action\"}");
                    break;
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error in GET request handler: " + e.getMessage());
            e.printStackTrace();
            out.print("{\"success\": false, \"message\": \"System error occurred\"}");
        }
        
        out.flush();
    }
    
    /**
     * Get the count of newly updated applications for the employee
     */
    private void handleGetNewUpdatesCount(String employeeId, PrintWriter out) {
        int newUpdatesCount = ManagerApproveLeaveController.getNewlyUpdatedCount(employeeId);
        System.out.println("📊 New updates count for " + employeeId + ": " + newUpdatesCount);
        out.print("{\"success\": true, \"newUpdatesCount\": " + newUpdatesCount + "}");
    }
    
    /**
     * Get the list of newly updated application IDs for the employee
     */
    private void handleGetNewUpdatedApplications(String employeeId, PrintWriter out) {
        java.util.Set<String> newlyUpdatedApps = ManagerApproveLeaveController.getNewlyUpdatedApplications(employeeId);
        
        StringBuilder json = new StringBuilder();
        json.append("{\"success\": true, \"applicationIds\": [");
        
        boolean first = true;
        for (String appId : newlyUpdatedApps) {
            if (!first) json.append(",");
            json.append("\"").append(appId).append("\"");
            first = false;
        }
        
        json.append("], \"count\": ").append(newlyUpdatedApps.size()).append("}");
        
        System.out.println("📊 Newly updated applications for " + employeeId + ": " + newlyUpdatedApps.size() + " applications");
        out.print(json.toString());
    }
    
    /**
     * Check if a specific application is in the newly updated list
     */
    private void handleCheckApplicationStatus(HttpServletRequest request, String employeeId, PrintWriter out) {
        String applicationId = request.getParameter("applicationId");
        
        if (applicationId == null || applicationId.trim().isEmpty()) {
            out.print("{\"success\": false, \"message\": \"Application ID is required\"}");
            return;
        }
        
        applicationId = applicationId.trim();
        java.util.Set<String> newlyUpdatedApps = ManagerApproveLeaveController.getNewlyUpdatedApplications(employeeId);
        boolean isNewlyUpdated = newlyUpdatedApps.contains(applicationId);
        
        System.out.println("📋 Application " + applicationId + " newly updated status: " + isNewlyUpdated);
        out.print("{\"success\": true, \"isNewlyUpdated\": " + isNewlyUpdated + ", \"applicationId\": \"" + applicationId + "\"}");
    }
    
    /**
     * Utility method to validate employee session and return employee ID
     */
    private String validateEmployeeSession(HttpServletRequest request, PrintWriter out) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            out.print("{\"success\": false, \"message\": \"Session expired. Please login again.\"}");
            return null;
        }
        
        Employee employee = (Employee) session.getAttribute("loggedInEmployee");
        if (employee == null) {
            out.print("{\"success\": false, \"message\": \"Employee not found. Please login again.\"}");
            return null;
        }
        
        return employee.getEmployeeId();
    }
    
    /**
     * Log newly updated applications state for debugging
     */
    private void logNewlyUpdatedState(String employeeId) {
        try {
            java.util.Set<String> apps = ManagerApproveLeaveController.getNewlyUpdatedApplications(employeeId);
            int count = apps.size();
            
            System.out.println("🔍 Current newly updated state for " + employeeId + ":");
            System.out.println("   Count: " + count);
            System.out.println("   Applications: " + apps);
        } catch (Exception e) {
            System.err.println("❌ Error logging newly updated state: " + e.getMessage());
        }
    }
}
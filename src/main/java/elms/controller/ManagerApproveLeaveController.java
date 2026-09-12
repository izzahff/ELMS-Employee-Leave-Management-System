package elms.controller;

import elms.DAO.LeaveApplicationDAO;
import elms.DAO.LeaveBalanceDAO;
import elms.DAO.EmployeeDAO;
import elms.DAO.LeaveTypeDAO;
import elms.DAO.ManagerDAO;
import elms.model.LeaveApplication;
import elms.model.Employee;
import elms.model.Manager;
import elms.model.LeaveType;
import elms.service.EmailNotificationService;
import elms.controller.ManagerNotificationController;


import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Controller for managers to approve or reject leave applications with email notifications
 * UPDATED: Balance deduction/restoration now happens here on manager decision
 */
@WebServlet("/manager-approve-leave")
public class ManagerApproveLeaveController extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    // Track newly updated applications per employee (application-wide)
    // Key: employeeId, Value: Set of application IDs that have been recently updated
    private static final Map<String, Set<String>> newlyUpdatedApplications = new ConcurrentHashMap<>();
    
    private LeaveApplicationDAO leaveApplicationDAO;
    private LeaveTypeDAO leaveTypeDAO;
    private ManagerDAO managerDAO;
    
    @Override
    public void init() throws ServletException {
        super.init();
        try {
            leaveApplicationDAO = new LeaveApplicationDAO();
            leaveTypeDAO = new LeaveTypeDAO();
            managerDAO = new ManagerDAO();
        } catch (Exception e) {
            throw new ServletException("Failed to initialize DAOs", e);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        System.out.println("=== MANAGER APPROVE/REJECT LEAVE REQUEST ===");
        
        // Set response type to JSON
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        try {
            // Check if manager is logged in
            HttpSession session = request.getSession(false);
            if (session == null) {
                out.print("{\"success\": false, \"message\": \"Session expired. Please login again.\"}");
                return;
            }
            
            Manager manager = (Manager) session.getAttribute("manager");
            if (manager == null) {
                out.print("{\"success\": false, \"message\": \"Manager not found. Please login again.\"}");
                return;
            }
            
            // Get request parameters
            String applicationId = request.getParameter("applicationId");
            String action = request.getParameter("action"); // "approve" or "reject"
            String reason = request.getParameter("reason"); // For rejection
            
            System.out.println("Application ID: " + applicationId);
            System.out.println("Action: " + action);
            System.out.println("Manager: " + manager.getManagerid() + " (" + manager.getManagername() + ")");
            
            // Validate input
            if (applicationId == null || applicationId.trim().isEmpty()) {
                out.print("{\"success\": false, \"message\": \"Application ID is required\"}");
                return;
            }
            
            if (action == null || (!action.equals("approve") && !action.equals("reject"))) {
                out.print("{\"success\": false, \"message\": \"Invalid action specified\"}");
                return;
            }
            
            if (action.equals("reject") && (reason == null || reason.trim().isEmpty())) {
                out.print("{\"success\": false, \"message\": \"Reason is required for rejection\"}");
                return;
            }
            
            // Get application details
            LeaveApplication application = leaveApplicationDAO.getLeaveApplicationById(applicationId);
            if (application == null) {
                out.print("{\"success\": false, \"message\": \"Leave application not found\"}");
                return;
            }
            
            // Check if application is still pending
            if (!"Pending".equalsIgnoreCase(application.getLeavestatus())) {
                out.print("{\"success\": false, \"message\": \"Application is already " + application.getLeavestatus().toLowerCase() + " and cannot be modified\"}");
                return;
            }
            
            // Process the action with balance handling
            String result = processLeaveDecisionWithBalanceHandling(application, action, manager, reason);
            
            if (result.startsWith("success:")) {
                String message = result.substring(8); // Remove "success:" prefix
                
                // Mark application as newly updated for the employee
                markApplicationAsNewlyUpdated(application.getEmployeeid(), applicationId);
                
                // Send email notification (async to avoid blocking the response)
                sendEmailNotificationAsync(application, action, manager, reason);
               
                // Escape quotes in message for JSON
                message = message.replace("\"", "\\\"");
                out.print("{\"success\": true, \"message\": \"" + message + "\"}");
                System.out.println("✅ Leave application " + action + "d successfully");
                System.out.println("✅ Application removed from ALL managers' notifications: " + applicationId);
            } else {
                String message = result.startsWith("error:") ? result.substring(6) : result;
                // Escape quotes in message for JSON
                message = message.replace("\"", "\\\"");
                out.print("{\"success\": false, \"message\": \"" + message + "\"}");
                System.err.println("❌ Failed to " + action + " leave application: " + result);
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error processing leave decision: " + e.getMessage());
            e.printStackTrace();
            out.print("{\"success\": false, \"message\": \"System error occurred while processing your request\"}");
        }
        
        out.flush();
    }
    
    /**
     * Mark application as newly updated for highlighting in employee view
     */
    private void markApplicationAsNewlyUpdated(String employeeId, String applicationId) {
        newlyUpdatedApplications.computeIfAbsent(employeeId, k -> new ConcurrentSkipListSet<>())
                                .add(applicationId);
        System.out.println("✅ Application " + applicationId + " marked as newly updated for employee " + employeeId);
    }
    
    /**
     * Send email notification asynchronously
     */
    private void sendEmailNotificationAsync(LeaveApplication application, String action, 
                                          Manager manager, String rejectionReason) {
        // Run email sending in a separate thread to avoid blocking the response
        new Thread(() -> {
            try {
                boolean emailSent = EmailNotificationService.sendLeaveStatusNotification(
                    application, action, manager, rejectionReason);
                
                if (emailSent) {
                    System.out.println("✅ Email notification sent successfully for application: " + application.getApplicationid());
                } else {
                    System.err.println("⚠️ Failed to send email notification for application: " + application.getApplicationid());
                }
                
            } catch (Exception e) {
                System.err.println("❌ Error in async email sending: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }
    
    /**
     * Process the manager's decision (approve or reject) with balance handling
     * UPDATED: Now handles balance deduction/restoration based on manager decision
     */
    private String processLeaveDecisionWithBalanceHandling(LeaveApplication application, String action, Manager manager, String reason) 
            throws SQLException {
        
        System.out.println("=== PROCESSING LEAVE DECISION WITH BALANCE HANDLING ===");
        System.out.println("Action: " + action);
        System.out.println("Application ID: " + application.getApplicationid());
        System.out.println("Employee ID: " + application.getEmployeeid());
        
        try {
            if ("approve".equals(action)) {
                return approveLeaveApplicationWithBalanceDeduction(application, manager);
            } else if ("reject".equals(action)) {
                return rejectLeaveApplication(application, manager, reason);
            } else {
                return "error: Invalid action specified";
            }
        } catch (Exception e) {
            System.err.println("❌ Error in processLeaveDecisionWithBalanceHandling: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    /**
     * Approve leave application and deduct balance NOW (not during submission)
     * UPDATED: Balance deduction happens here on approval
     */
    private String approveLeaveApplicationWithBalanceDeduction(LeaveApplication application, Manager manager) throws SQLException {
        System.out.println("=== APPROVING LEAVE APPLICATION WITH BALANCE DEDUCTION ===");
        
        try {
            // Get leave type details
        	// Check if THIS leave type affects balance (not just "Annual Leave"!)
        	LeaveType leaveType = leaveTypeDAO.getLeaveTypeById(application.getLeavetypeid());
        	boolean affectsBalance = leaveType != null && leaveType.isAffectsBalance();

        	if (affectsBalance) {
        	    // Calculate balance for THIS SPECIFIC leave type
        	    String employeeId = application.getEmployeeid();
        	    String leaveTypeId = application.getLeavetypeid();
        	    
        	    double currentBalance = LeaveBalanceDAO.calculateBalanceForLeaveType(employeeId, leaveTypeId);
        	    double requestedDuration = application.getLeaveduration();
        	    
        	    if (requestedDuration > currentBalance) {
        	        return "error: Insufficient balance for " + leaveType.getLeaveTypeName();
        	    }
        	    
        	    // ✅ No manual deduction - balance updates automatically!
        	}

        	// Approve the application
        	String statusResult = updateApplicationStatusWithManager(
        	    application.getApplicationid(), "Approved", manager.getManagerid(), null
        	);

        	// Success message with updated balance
        	String successMessage = "Leave application approved successfully";
        	if (affectsBalance) {
        	    double newBalance = LeaveBalanceDAO.calculateBalanceForLeaveType(
        	        application.getEmployeeid(), application.getLeavetypeid()
        	    );
        	    successMessage += ". Remaining: " + String.format("%.1f", newBalance) + " days.";
        	}

        	return "success: " + successMessage;
            
        } catch (Exception e) {
            System.err.println("❌ Error approving leave application: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    /**
     * Reject leave application - no balance changes needed since no deduction was made on submission
     * UPDATED: No balance restoration needed since no deduction was made on submission
     */
    private String rejectLeaveApplication(LeaveApplication application, Manager manager, String reason) throws SQLException {
        System.out.println("=== REJECTING LEAVE APPLICATION ===");
        System.out.println("Rejection reason: " + reason);
        
        try {
            // Get leave type details for messaging
            LeaveType leaveType = leaveTypeDAO.getLeaveTypeById(application.getLeavetypeid());
            boolean isAnnualLeave = (leaveType != null && "Annual Leave".equalsIgnoreCase(leaveType.getLeaveTypeName()));
            
            System.out.println("Leave Type: " + (leaveType != null ? leaveType.getLeaveTypeName() : "Unknown"));
            System.out.println("Is Annual Leave: " + isAnnualLeave);
            
            // No balance restoration needed since no deduction was made on submission
            System.out.println("ℹ️ No balance restoration needed - balance was not deducted on submission");
            
            // Update application status to Rejected and set manager ID with reason and review date
            String statusResult = updateApplicationStatusWithManager(
                application.getApplicationid(), 
                "Rejected", 
                manager.getManagerid(),
                reason // Include rejection reason
            );
            
            if (!statusResult.startsWith("success:")) {
                System.err.println("❌ Failed to update application status: " + statusResult);
                return "error: Failed to update application status";
            }
            
            System.out.println("✅ Application rejected successfully");
            
            // Create success message
            String successMessage = "Leave application has been rejected. Reason: " + reason;
            if (isAnnualLeave) {
                successMessage += ". Employee's leave balance remains unchanged.";
            }
            
            return "success: " + successMessage;
            
        } catch (Exception e) {
            System.err.println("❌ Error rejecting leave application: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    /**
     * Update application status and manager ID with rejection reason and review date
     */
    private String updateApplicationStatusWithManager(String applicationId, String newStatus, 
                                                    String managerId, String rejectionReason) throws SQLException {
        System.out.println("=== UPDATING APPLICATION STATUS WITH MANAGER ===");
        System.out.println("Application ID: " + applicationId);
        System.out.println("New Status: " + newStatus);
        System.out.println("Manager ID: " + managerId);
        if (rejectionReason != null) {
            System.out.println("Rejection Reason: " + rejectionReason);
        }
        
        try {
            // Use the updated method that handles manager ID, rejection reason, and review date
            String result = leaveApplicationDAO.updateLeaveApplicationStatusWithManagerAndReason(
                applicationId, newStatus, managerId, rejectionReason);
            
            return result;
            
        } catch (Exception e) {
            System.err.println("❌ Error updating application status with manager: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    
    /**
     * Static methods for accessing newly updated applications from other servlets/JSPs
     */
    public static Set<String> getNewlyUpdatedApplications(String employeeId) {
        return newlyUpdatedApplications.getOrDefault(employeeId, new ConcurrentSkipListSet<>());
    }
    
    /**
     * Mark a specific application as viewed by the employee
     * Returns true if the application was in the newly updated list and was removed
     */
    public static boolean markApplicationAsViewed(String employeeId, String applicationId) {
        Set<String> applications = newlyUpdatedApplications.get(employeeId);
        if (applications != null) {
            boolean removed = applications.remove(applicationId);
            
            // If the set becomes empty, remove the employee entry entirely to save memory
            if (applications.isEmpty()) {
                newlyUpdatedApplications.remove(employeeId);
                System.out.println("✅ Removed employee from newly updated list (no more updates): " + employeeId);
            }
            
            if (removed) {
                System.out.println("✅ Application " + applicationId + " marked as viewed and removed for employee " + employeeId);
            } else {
                System.out.println("⚠️ Application " + applicationId + " was not in newly updated list for employee " + employeeId);
            }
            
            return removed;
        }
        
        System.out.println("⚠️ Employee " + employeeId + " has no newly updated applications");
        return false;
    }
    
    /**
     * Clear all newly updated applications for a specific employee
     */
    public static void clearAllNewlyUpdatedForEmployee(String employeeId) {
        Set<String> removed = newlyUpdatedApplications.remove(employeeId);
        int count = removed != null ? removed.size() : 0;
        System.out.println("✅ Cleared " + count + " newly updated applications for employee " + employeeId);
    }
    
    /**
     * Get count of newly updated applications for a specific employee
     */
    public static int getNewlyUpdatedCount(String employeeId) {
        Set<String> applications = newlyUpdatedApplications.get(employeeId);
        return applications != null ? applications.size() : 0;
    }
    
    /**
     * Check if a specific application is in the newly updated list for an employee
     */
    public static boolean isApplicationNewlyUpdated(String employeeId, String applicationId) {
        Set<String> applications = newlyUpdatedApplications.get(employeeId);
        return applications != null && applications.contains(applicationId);
    }
    
    /**
     * Add an application to the newly updated list for an employee
     * This is already handled by the markApplicationAsNewlyUpdated method above
     */
    public static void addNewlyUpdatedApplication(String employeeId, String applicationId) {
        newlyUpdatedApplications.computeIfAbsent(employeeId, k -> new ConcurrentSkipListSet<>())
                                .add(applicationId);
        System.out.println("✅ Application " + applicationId + " added to newly updated list for employee " + employeeId);
    }
    
    /**
     * Get all employees who have newly updated applications
     */
    public static Set<String> getEmployeesWithNewUpdates() {
        return newlyUpdatedApplications.keySet();
    }
    
    /**
     * Get total count of all newly updated applications across all employees
     */
    public static int getTotalNewlyUpdatedCount() {
        return newlyUpdatedApplications.values().stream()
                .mapToInt(Set::size)
                .sum();
    }
    
    /**
     * Remove a specific application from all employees' newly updated lists
     * Useful when an application is deleted or cancelled
     */
    public static void removeApplicationFromAllNewlyUpdated(String applicationId) {
        System.out.println("📋 Removing application from all newly updated lists: " + applicationId);
        
        newlyUpdatedApplications.entrySet().removeIf(entry -> {
            Set<String> apps = entry.getValue();
            apps.remove(applicationId);
            return apps.isEmpty(); // Remove employee entry if no more applications
        });
        
        System.out.println("✅ Application " + applicationId + " removed from all newly updated lists");
    }
    
    /**
     * Debug method to print current state of newly updated applications
     */
    public static void printNewlyUpdatedState() {
        System.out.println("🔍 Current newly updated applications state:");
        if (newlyUpdatedApplications.isEmpty()) {
            System.out.println("   No newly updated applications");
        } else {
            for (Map.Entry<String, Set<String>> entry : newlyUpdatedApplications.entrySet()) {
                System.out.println("   Employee " + entry.getKey() + ": " + entry.getValue());
            }
            System.out.println("   Total employees with updates: " + newlyUpdatedApplications.size());
            System.out.println("   Total applications: " + getTotalNewlyUpdatedCount());
        }
    }
}
package elms.controller;

import elms.service.ManagerNotificationService;

/**
 * Updated Manager Notification Controller that uses the new ManagerNotificationService
 * This replaces the existing notifyAllManagersOfNewApplication method
 */
public class ManagerNotificationController {

    /**
     * Notify ALL managers about a new leave application (SYNCHRONOUS - blocks until complete)
     * This method should be called from EmployeeLeaveApplicationController
     * @param applicationId The leave application ID
     * @param employeeId The employee who submitted the application (for logging purposes)
     */
    public static void notifyAllManagersOfNewApplication(String applicationId, String employeeId) {
        System.out.println("=== MANAGER NOTIFICATION CONTROLLER (SYNC) ===");
        System.out.println("Application ID: " + applicationId);
        System.out.println("Employee ID: " + employeeId);

        try {
            // Use the new ManagerNotificationService to send emails
            boolean emailsSent = ManagerNotificationService.notifyAllManagersOfNewApplication(applicationId);

            if (emailsSent) {
                System.out.println("✅ Manager notifications sent successfully");
            } else {
                System.err.println("❌ Failed to send manager notifications");
            }

        } catch (Exception e) {
            System.err.println("❌ Error in notifyAllManagersOfNewApplication: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * ✅ NEW: Notify ALL managers about a new leave application (ASYNCHRONOUS - returns immediately)
     * This method starts a background thread and returns immediately without blocking
     * @param applicationId The leave application ID
     * @param employeeId The employee who submitted the application (for logging purposes)
     */
    public static void notifyAllManagersOfNewApplicationAsync(String applicationId, String employeeId) {
        System.out.println("=== MANAGER NOTIFICATION CONTROLLER (ASYNC) ===");
        System.out.println("Application ID: " + applicationId);
        System.out.println("Employee ID: " + employeeId);
        System.out.println("🔔 Starting background email thread...");

        // ✅ Run in background thread - returns immediately
        new Thread(() -> {
            try {
                System.out.println("📧 [Background Thread] Sending manager notifications for app " + applicationId);
                
                long startTime = System.currentTimeMillis();
                boolean emailsSent = ManagerNotificationService.notifyAllManagersOfNewApplication(applicationId);
                long endTime = System.currentTimeMillis();

                if (emailsSent) {
                    System.out.println("✅ [Background Thread] Manager notifications sent successfully in " + (endTime - startTime) + "ms");
                } else {
                    System.err.println("❌ [Background Thread] Failed to send manager notifications");
                }

            } catch (Exception e) {
                System.err.println("❌ [Background Thread] Error sending manager notifications: " + e.getMessage());
                e.printStackTrace();
                // Don't throw - we're in a background thread, failing silently is acceptable
            }
        }, "ManagerNotification-" + applicationId).start(); // Give thread a descriptive name for debugging

        System.out.println("✅ Background notification thread started - returning immediately");
    }
}
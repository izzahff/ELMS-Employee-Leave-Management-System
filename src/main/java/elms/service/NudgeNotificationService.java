package elms.service;

import elms.model.LeaveApplication;
import elms.model.Employee;
import elms.model.Manager;
import elms.model.LeaveType;
import elms.DAO.EmployeeDAO;
import elms.DAO.ManagerDAO;
import elms.DAO.LeaveTypeDAO;
import elms.DAO.LeaveApplicationDAO;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.*;
import java.text.SimpleDateFormat;
import java.text.ParseException;

/**
 * HIGH-PERFORMANCE service class for sending nudge notifications
 * Features:
 * - Synchronous email sending with progress tracking
 * - Rate limiting (1 nudge per application per day)
 * - Accurate success/failure feedback
 */
public class NudgeNotificationService {
    
    // ============================================================================
    // EMAIL CONFIGURATION
    // ============================================================================
    
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    
    private static final String EMAIL_USERNAME = "intanarina15@gmail.com";
    private static final String EMAIL_PASSWORD = "hgcf huzk jvnx goib";
    
    private static final String FROM_EMAIL = "noreply@imnsb.com";
    private static final String COMPANY_NAME = "Iktisas Management Network Sdn. Bhd. (IMNSB)";
    
    private static final int OVERDUE_DAYS_THRESHOLD = 2;
    private static final int MAX_NUDGES_PER_APPLICATION_PER_DAY = 1;
    
    // In-memory tracking (will reset on server restart)
    private static final Map<String, Integer> dailyNudgeCount = new ConcurrentHashMap<>();
    private static final Map<String, String> lastNudgeDate = new ConcurrentHashMap<>();
    private static final Map<String, List<EmailFailure>> failedEmails = new ConcurrentHashMap<>();
    
    // Executor for async emails
    private static final ExecutorService emailExecutor = Executors.newFixedThreadPool(5);
    private static final Map<String, CompletableFuture<EmailSendResult>> pendingEmails = new ConcurrentHashMap<>();
    
    // ============================================================================
    // OPTIMIZED NUDGE NOTIFICATION METHODS
    // ============================================================================
    
    /**
     * Send nudge notification and WAIT for email completion
     * @param applicationId The leave application ID
     * @param adminName The admin who triggered the nudge
     * @return NudgeResult with actual email delivery status
     */
    public static NudgeResult sendNudgeNotification(String applicationId, String adminName) {
        NudgeResult result = new NudgeResult();
        result.setApplicationId(applicationId);
        
        try {
            // Check rate limiting first (instant check)
            if (!canSendNudge(applicationId)) {
                int todayCount = getDailyNudgeCount(applicationId);
                result.setSuccess(false);
                result.setMessage("Application " + applicationId + " has already been nudged " + todayCount + 
                                " time today. Maximum " + MAX_NUDGES_PER_APPLICATION_PER_DAY + " nudge per day allowed.");
                return result;
            }
            
            // Validate configuration (instant check)
            if (!isConfigurationValid()) {
                result.setSuccess(false);
                result.setMessage("Email configuration is not valid. Please contact system administrator.");
                return result;
            }
            
            // Get leave application details (fast DB query)
            LeaveApplicationDAO leaveApplicationDAO = new LeaveApplicationDAO();
            LeaveApplication application = leaveApplicationDAO.getLeaveApplicationById(applicationId);
            
            if (application == null) {
                result.setSuccess(false);
                result.setMessage("Leave application not found: " + applicationId);
                return result;
            }
            
            // Check if application is still pending (instant check)
            if (!"Pending".equalsIgnoreCase(application.getLeavestatus())) {
                result.setSuccess(false);
                result.setMessage("Application " + applicationId + " is no longer pending (Status: " + application.getLeavestatus() + ")");
                return result;
            }
            
            // Get employee details (fast DB query)
            Employee employee = EmployeeDAO.getEmployeeByIdOnly(application.getEmployeeid());
            if (employee == null) {
                result.setSuccess(false);
                result.setMessage("Employee not found for application: " + applicationId);
                return result;
            }
            
            // Get leave type details (fast DB query)
            LeaveTypeDAO leaveTypeDAO = new LeaveTypeDAO();
            LeaveType leaveType = leaveTypeDAO.getLeaveTypeById(application.getLeavetypeid());
            String leaveTypeName = (leaveType != null) ? leaveType.getLeaveTypeName() : "Leave";
            
            // Calculate days pending (instant calculation)
            int daysPending = calculateDaysPending(application.getAppliedon());
            
            // Get all managers (fast DB query)
            ManagerDAO managerDAO = new ManagerDAO();
            List<Manager> allManagers = managerDAO.getAllManagers();
            
            if (allManagers == null || allManagers.isEmpty()) {
                result.setSuccess(false);
                result.setMessage("No managers found in the system");
                return result;
            }
            
         // ✅ Record nudge OPTIMISTICALLY
            recordNudgeAttempt(applicationId);
            System.out.println("✅ Nudge recorded optimistically for application " + applicationId);

            // ✅ SEND EMAILS SYNCHRONOUSLY AND WAIT FOR COMPLETION
            final String finalLeaveTypeName = leaveTypeName;
            final int finalDaysPending = daysPending;
            final String finalApplicationId = applicationId;
            final long emailStartTime = System.currentTimeMillis();

            CompletableFuture<EmailSendResult> emailTask = CompletableFuture.supplyAsync(() -> {
                EmailSendResult emailResult = new EmailSendResult();
                List<EmailFailure> failures = new ArrayList<>();
                
                for (Manager manager : allManagers) {
                    try {
                        boolean sent = sendNudgeEmailToManager(application, employee, finalLeaveTypeName, 
                                                              manager, finalDaysPending, adminName);
                        
                        if (sent) {
                            emailResult.incrementSent();
                            System.out.println("✅ Email sent to manager: " + manager.getManagername());
                        } else {
                            emailResult.incrementFailed();
                            System.out.println("❌ Email failed to manager: " + manager.getManagername());
                            failures.add(new EmailFailure(
                                finalApplicationId,
                                manager.getManageremail(),
                                manager.getManagername(),
                                "Email sending failed - please check SMTP configuration"
                            ));
                        }
                        
                    } catch (Exception e) {
                        emailResult.incrementFailed();
                        failures.add(new EmailFailure(
                            finalApplicationId,
                            manager.getManageremail(),
                            manager.getManagername(),
                            "Error: " + e.getMessage()
                        ));
                        System.err.println("Error sending nudge to manager " + manager.getManagername() + ": " + e.getMessage());
                    }
                }
                
                // ✅ ROLLBACK if ALL emails failed
                if (emailResult.getSent() == 0) {
                    rollbackNudgeAttempt(finalApplicationId);
                    System.out.println("❌ ALL EMAILS FAILED - Rolled back nudge count for " + finalApplicationId);
                }
                
                // Store failures if any occurred
                if (!failures.isEmpty()) {
                    failedEmails.put(adminName, failures);
                    System.out.println("⚠️ Recorded " + failures.size() + " email failures for admin: " + adminName);
                }
                
                long emailDuration = System.currentTimeMillis() - emailStartTime;
                System.out.println("📧 Email task completed in " + emailDuration + "ms");
                
                return emailResult;
            }, emailExecutor);

         // ✅ ULTRA-FAST DETECTION: Check if task completes early
            long maxTimeoutMs = 10000 + (allManagers.size() * 5000);
            System.out.println("⏳ Waiting for " + allManagers.size() + " emails (max timeout: " + (maxTimeoutMs/1000) + " seconds)");

            EmailSendResult emailResult = null;
            long startTime = System.currentTimeMillis();
            long elapsedMs = 0;
            boolean timedOut = false;

            // Poll every 100ms for completion (instant detection)
            while (elapsedMs < maxTimeoutMs) {
                if (emailTask.isDone()) {
                    // ✅ Task completed! Get the result immediately
                    try {
                        emailResult = emailTask.get();
                        elapsedMs = System.currentTimeMillis() - startTime;
                        System.out.println("✅ Email task completed after " + elapsedMs + "ms (" + (elapsedMs/1000) + "s)");
                        break;
                    } catch (Exception e) {
                        System.err.println("❌ Email task failed with exception: " + e.getMessage());
                        rollbackNudgeAttempt(finalApplicationId);
                        break;
                    }
                }
                
                // Not done yet, sleep briefly
                try {
                    Thread.sleep(100); // Check every 100ms (very fast)
                } catch (InterruptedException e) {
                    System.err.println("❌ Wait interrupted: " + e.getMessage());
                    rollbackNudgeAttempt(finalApplicationId);
                    break;
                }
                
                elapsedMs = System.currentTimeMillis() - startTime;
                
                // Log progress every 5 seconds (less spam in console)
                if (elapsedMs % 5000 < 100) {
                    System.out.println("⏳ Still waiting... (" + (elapsedMs/1000) + "s elapsed)");
                }
            }

            // Check if we timed out
            if (emailResult == null && elapsedMs >= maxTimeoutMs) {
                System.out.println("⏳ Email task timeout after " + elapsedMs + "ms - treating as failure");
                rollbackNudgeAttempt(finalApplicationId);
                timedOut = true;
            }

            // ✅ SET RESULT BASED ON ACTUAL EMAIL DELIVERY
            if (emailResult != null) {
                result.setTotalManagers(allManagers.size());
                result.setEmailsSent(emailResult.getSent());
                result.setEmailsFailed(emailResult.getFailed());
                
                System.out.println("📊 Email results - Sent: " + emailResult.getSent() + ", Failed: " + emailResult.getFailed());
                
                if (emailResult.getSent() == 0) {
                    // ALL FAILED
                    result.setSuccess(false);
                    result.setMessage("❌ Failed to send nudge emails to all " + allManagers.size() + 
                                    " managers. Please check email configuration.");
                } else if (emailResult.getFailed() > 0) {
                    // PARTIAL SUCCESS
                    result.setSuccess(true);
                    result.setMessage("⚠️ Nudge sent to " + emailResult.getSent() + " manager(s), but " + 
                                    emailResult.getFailed() + " email(s) failed. Please check email configuration.");
                } else {
                    // ALL SUCCEEDED
                    result.setSuccess(true);
                    result.setMessage("✅ Nudge notification sent successfully to " + emailResult.getSent() + " manager(s).");
                }
            } else if (timedOut) {
                // ❌ TIMEOUT - TREAT AS FAILURE
                result.setSuccess(false);
                result.setTotalManagers(allManagers.size());
                result.setEmailsSent(0);
                result.setEmailsFailed(allManagers.size());
                result.setMessage("⏳ Email sending timed out. Please check email configuration and try again.");
            } else {
                // ❌ EXCEPTION OR INTERRUPTION
                result.setSuccess(false);
                result.setTotalManagers(allManagers.size());
                result.setEmailsSent(0);
                result.setEmailsFailed(allManagers.size());
                result.setMessage("❌ An error occurred while sending emails. Please try again.");
            }
           
            
            System.out.println("✅ Nudge result for " + applicationId + ": " + result.toString());
            return result;
            
        } catch (Exception e) {
            System.err.println("❌ Error sending nudge: " + e.getMessage());
            result.setSuccess(false);
            result.setMessage("System error occurred while sending nudge: " + e.getMessage());
            return result;
        }
    }
   
    
    /**
     * Send bulk nudge notifications with progress tracking
     * @param adminName The admin who triggered the bulk nudge
     * @param callback Optional progress callback (can be null)
     * @return BulkNudgeResult with detailed statistics
     */
    public static BulkNudgeResult sendBulkNudgeNotifications(String adminName, BulkNudgeProgressCallback callback) {
        BulkNudgeResult result = new BulkNudgeResult();
        
        try {
            List<LeaveApplication> overdueApplications = getOverdueApplications();
            
            if (overdueApplications.isEmpty()) {
                result.setTotalApplications(0);
                result.setSuccessfulNudges(0);
                result.setFailedNudges(0);
                result.setSkippedDueToRateLimit(0);
                result.setMessage("No overdue applications found (older than " + OVERDUE_DAYS_THRESHOLD + " days)");
                return result;
            }
            
            result.setTotalApplications(overdueApplications.size());
            
            int successCount = 0;
            int failCount = 0;
            int skippedCount = 0;
            int totalEmailsSent = 0;
            int totalEmailsFailed = 0;
            int currentIndex = 0;
            List<String> skippedApplications = new ArrayList<>();
            
            // ✅ Notify start
            if (callback != null) {
                callback.onProgress(0, overdueApplications.size(), "Starting bulk nudge...");
            }
            
            // Process each application
            for (LeaveApplication app : overdueApplications) {
                currentIndex++;
                
                try {
                    // ✅ Update progress
                    if (callback != null) {
                        callback.onProgress(currentIndex, overdueApplications.size(), 
                            "Processing application " + app.getApplicationid() + "...");
                    }
                    
                    // Check rate limiting
                    if (!canSendNudge(app.getApplicationid())) {
                        skippedCount++;
                        skippedApplications.add(app.getApplicationid());
                        System.out.println("⏭️ Skipped " + app.getApplicationid() + " (already nudged today)");
                        continue;
                    }
                    
                    // ✅ Send nudge and WAIT for email result
                    NudgeResult nudgeResult = sendNudgeNotification(app.getApplicationid(), adminName);
                    
                    if (nudgeResult.isSuccess()) {
                        successCount++;
                        totalEmailsSent += nudgeResult.getEmailsSent();
                        totalEmailsFailed += nudgeResult.getEmailsFailed();
                        System.out.println("✅ Application " + app.getApplicationid() + " - " + 
                                         nudgeResult.getEmailsSent() + " emails sent");
                    } else {
                        failCount++;
                        System.out.println("❌ Application " + app.getApplicationid() + " - failed");
                    }
                    
                } catch (Exception e) {
                    failCount++;
                    System.err.println("Error sending nudge for " + app.getApplicationid() + ": " + e.getMessage());
                }
            }
            
            // ✅ Notify completion
            if (callback != null) {
                callback.onProgress(overdueApplications.size(), overdueApplications.size(), "Finalizing...");
            }
            
            result.setSuccessfulNudges(successCount);
            result.setFailedNudges(failCount);
            result.setSkippedDueToRateLimit(skippedCount);
            result.setSkippedApplications(skippedApplications);
            
            // ✅ BUILD ACCURATE MESSAGE
            StringBuilder message = new StringBuilder();
            
            if (totalEmailsSent > 0) {
                message.append("✅ Successfully sent ").append(totalEmailsSent).append(" nudge email(s) for ")
                       .append(successCount).append(" application(s)");
            }
            
            if (totalEmailsFailed > 0) {
                if (message.length() > 0) message.append(". ");
                message.append("❌ ").append(totalEmailsFailed).append(" email(s) failed to send");
            }
            
            if (skippedCount > 0) {
                if (message.length() > 0) message.append(". ");
                message.append("⚠️ ").append(skippedCount).append(" application(s) skipped (already nudged today)");
            }
            
            if (failCount > 0 && totalEmailsSent == 0) {
                if (message.length() > 0) message.append(". ");
                message.append("❌ All ").append(failCount).append(" application(s) failed to send nudges");
            }
            
            if (message.length() == 0) {
                message.append("No nudges were sent");
            }
            
            message.append(".");
            
            result.setMessage(message.toString());
            result.setActualSuccess(totalEmailsSent > 0);
            
            System.out.println("✅ Bulk nudge final result: " + result.toString());
            return result;
            
        } catch (Exception e) {
            System.err.println("❌ Error in bulk nudge: " + e.getMessage());
            result.setMessage("System error occurred during bulk nudge: " + e.getMessage());
            result.setActualSuccess(false);
            return result;
        }
    }
    
    /**
     * Backwards compatibility - no progress callback
     */
    public static BulkNudgeResult sendBulkNudgeNotifications(String adminName) {
        return sendBulkNudgeNotifications(adminName, null);
    }
    
    /**
     * Wait for email task to complete and return the result
     * @param applicationId The application ID
     * @param timeoutMs Maximum time to wait in milliseconds
     * @return EmailSendResult or null if timeout/not found
     */
    public static EmailSendResult waitForEmailCompletion(String applicationId, long timeoutMs) {
        CompletableFuture<EmailSendResult> emailTask = pendingEmails.get(applicationId);
        
        if (emailTask != null) {
            try {
                EmailSendResult result = emailTask.get(timeoutMs, TimeUnit.MILLISECONDS);
                pendingEmails.remove(applicationId);
                System.out.println("✅ Email task completed for application: " + applicationId + 
                                 " - Sent: " + result.getSent() + ", Failed: " + result.getFailed());
                return result;
                
            } catch (TimeoutException e) {
                System.out.println("⚠️ Email task timeout after " + timeoutMs + "ms - emails still sending in background");
                return null;
            } catch (Exception e) {
                System.err.println("❌ Error waiting for email task: " + e.getMessage());
                return null;
            }
        }
        
        return null;
    }
    
    // ============================================================================
    // RATE LIMITING METHODS
    // ============================================================================
    
    /**
     * Check if an application can be nudged (rate limiting)
     */
    private static boolean canSendNudge(String applicationId) {
        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String lastDate = lastNudgeDate.get(applicationId);
        
        if (!today.equals(lastDate)) {
            dailyNudgeCount.put(applicationId, 0);
            lastNudgeDate.put(applicationId, today);
        }
        
        int currentCount = dailyNudgeCount.getOrDefault(applicationId, 0);
        return currentCount < MAX_NUDGES_PER_APPLICATION_PER_DAY;
    }
    
    /**
     * Record a nudge attempt
     */
    private static void recordNudgeAttempt(String applicationId) {
        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        lastNudgeDate.put(applicationId, today);
        
        int currentCount = dailyNudgeCount.getOrDefault(applicationId, 0);
        dailyNudgeCount.put(applicationId, currentCount + 1);
    }
    
    /**
     * Rollback a nudge attempt (used when all emails fail)
     */
    private static void rollbackNudgeAttempt(String applicationId) {
        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String lastDate = lastNudgeDate.get(applicationId);
        
        if (today.equals(lastDate)) {
            int currentCount = dailyNudgeCount.getOrDefault(applicationId, 0);
            if (currentCount > 0) {
                dailyNudgeCount.put(applicationId, currentCount - 1);
                System.out.println("🔄 Rolled back nudge count for " + applicationId + ": " + currentCount + " → " + (currentCount - 1));
            }
        }
    }
    
    /**
     * Get daily nudge count for an application
     */
    public static int getDailyNudgeCount(String applicationId) {
        String today = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String lastDate = lastNudgeDate.get(applicationId);
        
        if (!today.equals(lastDate)) {
            return 0;
        }
        
        return dailyNudgeCount.getOrDefault(applicationId, 0);
    }
    
    /**
     * Get remaining nudges for an application today
     */
    public static int getRemainingNudges(String applicationId) {
        return Math.max(0, MAX_NUDGES_PER_APPLICATION_PER_DAY - getDailyNudgeCount(applicationId));
    }
    
    // ============================================================================
    // HELPER METHODS
    // ============================================================================
    
    /**
     * Get list of applications that are overdue
     */
    public static List<LeaveApplication> getOverdueApplications() {
        List<LeaveApplication> overdueApplications = new ArrayList<>();
        
        try {
            LeaveApplicationDAO leaveApplicationDAO = new LeaveApplicationDAO();
            List<LeaveApplication> pendingApplications = leaveApplicationDAO.getLeaveApplicationsWithFilters(null, "Pending", null);
            
            for (LeaveApplication app : pendingApplications) {
                try {
                    int daysPending = calculateDaysPending(app.getAppliedon());
                    
                    if (daysPending >= OVERDUE_DAYS_THRESHOLD) {
                        overdueApplications.add(app);
                    }
                    
                } catch (Exception e) {
                    // Silent error handling
                }
            }
            
        } catch (Exception e) {
            // Silent error handling
        }
        
        return overdueApplications;
    }
    
    /**
     * Calculate days pending from application date
     */
    private static int calculateDaysPending(String appliedOnStr) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date appliedDate = dateFormat.parse(appliedOnStr);
            Date currentDate = new Date();
            
            long diffInMillies = currentDate.getTime() - appliedDate.getTime();
            long daysPending = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);
            
            return (int) daysPending;
            
        } catch (ParseException e) {
            return 0;
        }
    }
    
    /**
     * Send nudge email to a specific manager
     */
    private static boolean sendNudgeEmailToManager(LeaveApplication application, Employee employee, 
                                                  String leaveTypeName, Manager manager, int daysPending, String adminName) {
        try {
            String subject = createNudgeEmailSubject(employee, leaveTypeName, application.getApplicationid(), daysPending);
            String emailBody = createNudgeEmailBody(application, employee, leaveTypeName, manager, daysPending, adminName);
            
            if (emailBody == null || emailBody.trim().isEmpty() || subject == null || subject.trim().isEmpty()) {
                return false;
            }
            
            return sendEmail(manager.getManageremail(), manager.getManagername(), subject, emailBody);
            
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Create email subject for nudge notification
     */
    private static String createNudgeEmailSubject(Employee employee, String leaveTypeName, String applicationId, int daysPending) {
        return String.format("🔔 [%s] URGENT REMINDER: Leave Application Pending %d Days - %s (ID: %s)", 
                           COMPANY_NAME, daysPending, employee.getEmployeeName(), applicationId);
    }
    
    /**
     * Create HTML email body for nudge notification
     */
    private static String createNudgeEmailBody(LeaveApplication application, Employee employee, 
                                             String leaveTypeName, Manager manager, int daysPending, String adminName) {
        
        if (application == null || employee == null || manager == null) {
            return null;
        }
        
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("MMMM dd, yyyy 'at' hh:mm a");
        
        StringBuilder emailBody = new StringBuilder();
        
        emailBody.append("<!DOCTYPE html>")
                .append("<html><head>")
                .append("<meta charset='UTF-8'>")
                .append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>")
                .append("<title>URGENT: Leave Application Reminder</title>")
                .append("<style>")
                .append("body{font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Arial,sans-serif;line-height:1.6;color:#333;margin:0;padding:0;background-color:#f4f6f9;}")
                .append(".email-container{max-width:600px;margin:20px auto;background:white;border-radius:12px;overflow:hidden;box-shadow:0 4px 20px rgba(0,0,0,0.1);}")
                .append(".header{background:linear-gradient(135deg,#dc3545,#c82333);color:white;padding:40px 30px;text-align:center;}")
                .append(".header h1{margin:0;font-size:28px;font-weight:600;}")
                .append(".header p{margin:10px 0 0 0;font-size:16px;opacity:0.9;}")
                .append(".content{padding:40px 30px;}")
                .append(".urgent-section{background:#fff3cd;padding:25px;border-radius:12px;margin:25px 0;border-left:6px solid #ffc107;}")
                .append(".urgent-badge{background:#dc3545;color:white;padding:8px 16px;border-radius:20px;font-weight:600;display:inline-block;margin-bottom:15px;font-size:14px;}")
                .append(".details-grid{display:grid;grid-template-columns:1fr 1fr;gap:20px;margin:30px 0;}")
                .append(".detail-item{background:#f8f9fa;padding:20px;border-radius:8px;border-left:4px solid #dc3545;}")
                .append(".detail-label{font-weight:600;color:#495057;font-size:14px;text-transform:uppercase;letter-spacing:0.5px;}")
                .append(".detail-value{margin-top:8px;font-size:16px;color:#212529;}")
                .append(".employee-info{background:#e3f2fd;border:2px solid #90caf9;color:#1565c0;padding:25px;border-radius:12px;margin:25px 0;}")
                .append(".overdue-warning{background:#f8d7da;border:2px solid #f5c6cb;color:#721c24;padding:25px;border-radius:12px;margin:25px 0;}")
                .append(".footer{background:#f8f9fa;padding:30px;text-align:center;color:#6c757d;border-top:1px solid #dee2e6;}")
                .append("@media (max-width:600px){.email-container{margin:10px;border-radius:0;}.content{padding:20px;}.details-grid{grid-template-columns:1fr;gap:15px;}}")
                .append("</style>")
                .append("</head><body>");
        
        emailBody.append("<div class='email-container'>");
        
        emailBody.append("<div class='header'>")
                .append("<h1>🔔 URGENT REMINDER</h1>")
                .append("<p>").append(COMPANY_NAME).append(" - Leave Management System</p>")
                .append("</div>");
        
        emailBody.append("<div class='content'>")
                .append("<h2 style='color:#2d3748;margin-bottom:20px;'>Dear ").append(safeString(manager.getManagername())).append(",</h2>")
                .append("<p style='font-size:16px;color:#4a5568;margin-bottom:30px;'>")
                .append("This is an <strong style='color:#dc3545;'>urgent reminder</strong> about a leave application that has been pending for ")
                .append("<strong>").append(daysPending).append(" days</strong> and requires your immediate attention.")
                .append("</p>");
        
        emailBody.append("<div class='urgent-section'>")
                .append("<div class='urgent-badge'>⚠️ OVERDUE - ").append(daysPending).append(" DAYS</div>")
                .append("<p style='margin:0;font-size:16px;'><strong>Status:</strong> Still awaiting manager review</p>")
                .append("<p style='margin:10px 0 0 0;font-size:16px;'><strong>Original Submission:</strong> ").append(formatDateString(application.getAppliedon())).append("</p>")
                .append("<p style='margin:10px 0 0 0;font-size:16px;'><strong>Nudge Sent By:</strong> Admin ").append(safeString(adminName)).append("</p>")
                .append("</div>");
        
        emailBody.append("<div class='overdue-warning'>")
                .append("<h4 style='margin:0 0 15px 0;color:#721c24;'>⏰ Action Required</h4>")
                .append("<p style='margin:0;font-size:16px;'>")
                .append("This application has exceeded the standard review timeframe. Please review and make a decision as soon as possible.")
                .append("</p>")
                .append("</div>");
        
        emailBody.append("<div class='employee-info'>")
                .append("<h4 style='margin:0 0 15px 0;color:#1565c0;'>👤 Employee Information</h4>")
                .append("<p style='margin:0 0 8px 0;'><strong>Name:</strong> ").append(safeString(employee.getEmployeeName())).append("</p>")
                .append("<p style='margin:0 0 8px 0;'><strong>Employee ID:</strong> ").append(safeString(employee.getEmployeeId())).append("</p>")
                .append("<p style='margin:0;'><strong>Email:</strong> ").append(safeString(employee.getEmployeeEmail())).append("</p>")
                .append("</div>");
        
        emailBody.append("<h3 style='color:#2d3748;margin:40px 0 20px 0;'>📋 Leave Application Details</h3>")
                .append("<div class='details-grid'>")
                .append("<div class='detail-item'>")
                .append("<div class='detail-label'>Application ID</div>")
                .append("<div class='detail-value'>").append(safeString(application.getApplicationid())).append("</div>")
                .append("</div>")
                .append("<div class='detail-item'>")
                .append("<div class='detail-label'>Leave Type</div>")
                .append("<div class='detail-value'>").append(safeString(leaveTypeName)).append("</div>")
                .append("</div>")
                .append("<div class='detail-item'>")
                .append("<div class='detail-label'>Start Date</div>")
                .append("<div class='detail-value'>").append(formatDateString(application.getLeavestartdate())).append("</div>")
                .append("</div>")
                .append("<div class='detail-item'>")
                .append("<div class='detail-label'>End Date</div>")
                .append("<div class='detail-value'>").append(formatDateString(application.getLeaveenddate())).append("</div>")
                .append("</div>")
                .append("<div class='detail-item'>")
                .append("<div class='detail-label'>Duration</div>")
                .append("<div class='detail-value'>").append(application.getLeaveduration()).append(" day(s)</div>")
                .append("</div>")
                .append("<div class='detail-item'>")
                .append("<div class='detail-label'>Days Pending</div>")
                .append("<div class='detail-value' style='color:#dc3545;font-weight:600;'>").append(daysPending).append(" days</div>")
                .append("</div>")
                .append("</div>");
        
        if (application.getLeavereason() != null && !application.getLeavereason().trim().isEmpty()) {
            emailBody.append("<h3 style='color:#2d3748;margin:40px 0 20px 0;'>📝 Leave Reason</h3>")
                    .append("<div style='background:#f8f9fa;padding:20px;border-radius:8px;border-left:4px solid #17a2b8;margin:20px 0;'>")
                    .append("<p style='margin:0;font-size:16px;color:#212529;'>").append(safeString(application.getLeavereason())).append("</p>")
                    .append("</div>");
        }
        
        if (application.getAttachment() != null && !application.getAttachment().trim().isEmpty()) {
            emailBody.append("<div style='background:#d4edda;border:2px solid #c3e6cb;color:#155724;padding:20px;border-radius:8px;margin:20px 0;'>")
                    .append("<h4 style='margin:0 0 10px 0;'>📎 Attachment Included</h4>")
                    .append("<p style='margin:0;'>This application includes a file attachment. Please review it when processing the application.</p>")
                    .append("</div>");
        }
        
        emailBody.append("<div style='margin:40px 0 0 0;padding:30px 0;border-top:1px solid #e2e8f0;'>")
                .append("<p style='margin:0 0 10px 0;font-size:16px;color:#4a5568;'>Thank you for your immediate attention to this overdue application.</p>")
                .append("<p style='margin:0;font-size:16px;color:#4a5568;'>")
                .append("<strong>Best regards,</strong><br>")
                .append("System Administrator (").append(safeString(adminName)).append(")<br>")
                .append("<span style='color:#dc3545;'>").append(COMPANY_NAME).append("</span>")
                .append("</p>")
                .append("</div>");
        
        emailBody.append("</div>");
        
        emailBody.append("<div class='footer'>")
                .append("<p style='margin:0 0 10px 0;font-size:14px;'>🔔 This is an automated urgent reminder from the Employee Leave Management System.</p>")
                .append("<p style='margin:0 0 20px 0;font-size:14px;'>This nudge was triggered by an administrator due to the overdue status.</p>")
                .append("<p style='margin:0;font-size:12px;color:#9ca3af;'>")
                .append("© ").append(new SimpleDateFormat("yyyy").format(new Date())).append(" ").append(COMPANY_NAME).append(". All rights reserved.<br>")
                .append("Nudge sent on: ").append(dateTimeFormat.format(new Date()))
                .append("</p>")
                .append("</div>");
        
        emailBody.append("</div>");
        emailBody.append("</body></html>");
        
        return emailBody.toString();
    }
    
    /**
     * Safe string method
     */
    private static String safeString(String input) {
        return input != null ? input : "N/A";
    }
    
    /**
     * Format date string for display
     */
    private static String formatDateString(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return "N/A";
        }
        
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM dd, yyyy");
            Date date = inputFormat.parse(dateStr);
            return outputFormat.format(date);
        } catch (Exception e) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM dd, yyyy 'at' hh:mm a");
                Date date = inputFormat.parse(dateStr);
                return outputFormat.format(date);
            } catch (Exception ex) {
                return dateStr;
            }
        }
    }
    
    /**
     * Email sending using JavaMail API
     */
    private static boolean sendEmail(String toEmail, String toName, String subject, String htmlContent) {
        try {
            if (toEmail == null || toEmail.trim().isEmpty() || 
                subject == null || subject.trim().isEmpty() || 
                htmlContent == null || htmlContent.trim().isEmpty()) {
                return false;
            }
            
            Properties props = new Properties();
            props.put("mail.smtp.host", SMTP_HOST);
            props.put("mail.smtp.port", SMTP_PORT);
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.ssl.trust", SMTP_HOST);
            props.put("mail.smtp.ssl.protocols", "TLSv1.2");
            props.put("mail.smtp.connectiontimeout", "8000");
            props.put("mail.smtp.timeout", "8000");
            
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(EMAIL_USERNAME, EMAIL_PASSWORD);
                }
            });
            
            session.setDebug(false);
            
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL, COMPANY_NAME));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail, toName));
            message.setSubject(subject);
            message.setContent(htmlContent, "text/html; charset=utf-8");
            message.setSentDate(new Date());
            message.setHeader("X-Mailer", "ELMS-Nudge-Notification-System");
            message.setHeader("X-Priority", "1");
            
            Transport.send(message);
            
            return true;
            
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Validate email configuration
     */
    private static boolean isConfigurationValid() {
        if (EMAIL_USERNAME.equals("your-email@gmail.com") || EMAIL_PASSWORD.equals("your-app-password")) {
            return false;
        }
        return true;
    }
    
    // ============================================================================
    // MEMORY MANAGEMENT
    // ============================================================================
    
    public static List<EmailFailure> getFailedEmails(String adminId) {
        return failedEmails.getOrDefault(adminId, new ArrayList<>());
    }

    public static void clearFailedEmails(String adminId) {
        failedEmails.remove(adminId);
    }
    
    /**
     * Shutdown hook for graceful cleanup
     */
    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down email executor...");
            emailExecutor.shutdown();
            try {
                if (!emailExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                    emailExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                emailExecutor.shutdownNow();
            }
        }));
    }
    
    // ============================================================================
    // ✅ PROGRESS CALLBACK INTERFACE
    // ============================================================================
    
    /**
     * Progress callback interface for bulk nudge operations
     */
    public interface BulkNudgeProgressCallback {
        void onProgress(int current, int total, String message);
    }
    
    // ============================================================================
    // RESULT CLASSES
    // ============================================================================
    
    public static class NudgeResult {
        private String applicationId;
        private boolean success;
        private String message;
        private int emailsSent;
        private int emailsFailed;
        private int totalManagers;
        
        public NudgeResult() {
            this.success = false;
            this.emailsSent = 0;
            this.emailsFailed = 0;
            this.totalManagers = 0;
        }
        
        public String getApplicationId() { return applicationId; }
        public void setApplicationId(String applicationId) { this.applicationId = applicationId; }
        
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public int getEmailsSent() { return emailsSent; }
        public void setEmailsSent(int emailsSent) { this.emailsSent = emailsSent; }
        
        public int getEmailsFailed() { return emailsFailed; }
        public void setEmailsFailed(int emailsFailed) { this.emailsFailed = emailsFailed; }
        
        public int getTotalManagers() { return totalManagers; }
        public void setTotalManagers(int totalManagers) { this.totalManagers = totalManagers; }
        
        @Override
        public String toString() {
            return String.format("NudgeResult{appId='%s', success=%s, sent=%d, failed=%d, total=%d, message='%s'}", 
                               applicationId, success, emailsSent, emailsFailed, totalManagers, message);
        }
    }
    
    public static class BulkNudgeResult {
        private int totalApplications;
        private int successfulNudges;
        private int failedNudges;
        private int skippedDueToRateLimit;
        private String message;
        private List<String> skippedApplications;
        private List<String> failedApplications;
        private boolean actualSuccess;
        
        public BulkNudgeResult() {
            this.totalApplications = 0;
            this.successfulNudges = 0;
            this.failedNudges = 0;
            this.skippedDueToRateLimit = 0;
            this.message = "";
            this.skippedApplications = new ArrayList<>();
            this.failedApplications = new ArrayList<>();
            this.actualSuccess = false;
        }
        
        public int getTotalApplications() { return totalApplications; }
        public void setTotalApplications(int totalApplications) { this.totalApplications = totalApplications; }
        
        public int getSuccessfulNudges() { return successfulNudges; }
        public void setSuccessfulNudges(int successfulNudges) { this.successfulNudges = successfulNudges; }
        
        public int getFailedNudges() { return failedNudges; }
        public void setFailedNudges(int failedNudges) { this.failedNudges = failedNudges; }
        
        public int getSkippedDueToRateLimit() { return skippedDueToRateLimit; }
        public void setSkippedDueToRateLimit(int skippedDueToRateLimit) { this.skippedDueToRateLimit = skippedDueToRateLimit; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public List<String> getSkippedApplications() { return skippedApplications; }
        public void setSkippedApplications(List<String> skippedApplications) { this.skippedApplications = skippedApplications; }
        
        public List<String> getFailedApplications() { return failedApplications; }
        public void setFailedApplications(List<String> failedApplications) { this.failedApplications = failedApplications; }
        
        public boolean isActualSuccess() { return actualSuccess; }
        public void setActualSuccess(boolean actualSuccess) { this.actualSuccess = actualSuccess; }
        
        public boolean isSuccess() { 
            return actualSuccess;
        }
        
        @Override
        public String toString() {
            return String.format("BulkNudgeResult{total=%d, successful=%d, failed=%d, skipped=%d, actualSuccess=%s, message='%s'}", 
                               totalApplications, successfulNudges, failedNudges, skippedDueToRateLimit, actualSuccess, message);
        }
    }
    
    public static class EmailSendResult {
        private int sent = 0;
        private int failed = 0;
        
        public synchronized void incrementSent() { sent++; }
        public synchronized void incrementFailed() { failed++; }
        
        public int getSent() { return sent; }
        public int getFailed() { return failed; }
    }
    
    public static class EmailFailure {
        private String applicationId;
        private String managerEmail;
        private String managerName;
        private String failureReason;
        private Date failureTime;
        
        public EmailFailure(String applicationId, String managerEmail, String managerName, String failureReason) {
            this.applicationId = applicationId;
            this.managerEmail = managerEmail;
            this.managerName = managerName;
            this.failureReason = failureReason;
            this.failureTime = new Date();
        }
        
        public String getApplicationId() { return applicationId; }
        public String getManagerEmail() { return managerEmail; }
        public String getManagerName() { return managerName; }
        public String getFailureReason() { return failureReason; }
        public Date getFailureTime() { return failureTime; }
    }
}
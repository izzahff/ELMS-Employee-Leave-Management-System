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
import java.text.SimpleDateFormat;

/**
 * Optimized service class for sending email notifications to managers about new leave applications
 * All debug statements removed for better performance
 */
public class ManagerNotificationService {
    
   
    
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    
    // TODO: Replace with your actual Gmail credentials
    private static final String EMAIL_USERNAME = "intanarina15@gmail.com";        // ← CHANGE THIS
    private static final String EMAIL_PASSWORD = "hgcf huzk jvnx goib";           // ← CHANGE THIS (16-char App Password)
    
    // Email display settings
    private static final String FROM_EMAIL = "noreply@imnsb.com";              // ← CHANGE THIS
    private static final String COMPANY_NAME = "Iktisas Management Network Sdn. Bhd. (IMNSB)";
    
 
    
    /**
     * Send email notifications to ALL managers about a new leave application
     * Optimized version with minimal logging for better performance
     * @param applicationId The leave application ID
     * @return true if at least one email sent successfully, false otherwise
     */
    public static boolean notifyAllManagersOfNewApplication(String applicationId) {
        try {
            // Validate configuration
            if (!isConfigurationValid()) {
                System.err.println("Email configuration invalid");
                return false;
            }
            
            // Get leave application details
            LeaveApplicationDAO leaveApplicationDAO = new LeaveApplicationDAO();
            LeaveApplication application = leaveApplicationDAO.getLeaveApplicationById(applicationId);
            
            if (application == null) {
                System.err.println("Leave application not found: " + applicationId);
                return false;
            }
            
            // Get employee details
            Employee employee = EmployeeDAO.getEmployeeByIdOnly(application.getEmployeeid());
            if (employee == null) {
                System.err.println("Employee not found for application: " + applicationId);
                return false;
            }
            
            // Get leave type details
            LeaveTypeDAO leaveTypeDAO = new LeaveTypeDAO();
            LeaveType leaveType = leaveTypeDAO.getLeaveTypeById(application.getLeavetypeid());
            String leaveTypeName = (leaveType != null) ? leaveType.getLeaveTypeName() : "Leave";
            
            // Get all managers
            ManagerDAO managerDAO = new ManagerDAO();
            List<Manager> allManagers = managerDAO.getAllManagers();
            
            if (allManagers == null || allManagers.isEmpty()) {
                System.err.println("No managers found in the system");
                return false;
            }
            
            // Send emails to all managers with minimal logging
            int emailsSent = 0;
            int emailsFailed = 0;
            
            for (Manager manager : allManagers) {
                try {
                    boolean sent = sendNewApplicationNotificationToManager(application, employee, leaveTypeName, manager);
                    
                    if (sent) {
                        emailsSent++;
                    } else {
                        emailsFailed++;
                    }
                    
                    // Reduced delay between emails
                    Thread.sleep(100);
                    
                } catch (Exception e) {
                    emailsFailed++;
                    // Silent error handling for performance
                }
            }
            
            // Only log summary for essential monitoring
            if (emailsFailed > 0) {
                System.err.println("Manager notifications: " + emailsSent + " sent, " + emailsFailed + " failed");
            }
            
            return emailsSent > 0;
            
        } catch (Exception e) {
            System.err.println("Error in manager notification service: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Send email notification to a specific manager about a new leave application
     * Optimized for performance
     */
    private static boolean sendNewApplicationNotificationToManager(LeaveApplication application, Employee employee, 
                                                                  String leaveTypeName, Manager manager) {
        try {
            // Create email subject
            String subject = createManagerNotificationSubject(employee, leaveTypeName, application.getApplicationid());
            
            // Create email body
            String emailBody = createManagerNotificationEmailBody(application, employee, leaveTypeName, manager);
            
            // Send email
            return sendEmail(manager.getManageremail(), manager.getManagername(), subject, emailBody);
            
        } catch (Exception e) {
            // Silent error handling for performance
            return false;
        }
    }
    
    /**
     * Create email subject for manager notification
     */
    private static String createManagerNotificationSubject(Employee employee, String leaveTypeName, String applicationId) {
        return String.format("[%s] New Leave Application - %s requesting %s (ID: %s)", 
                           COMPANY_NAME, employee.getEmployeeName(), leaveTypeName, applicationId);
    }
    
    /**
     * Create professional HTML email body for manager notification
     * Optimized version with streamlined HTML generation
     */
    private static String createManagerNotificationEmailBody(LeaveApplication application, Employee employee, 
                                                            String leaveTypeName, Manager manager) {
        
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("MMMM dd, yyyy 'at' hh:mm a");
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy");
        
        StringBuilder emailBody = new StringBuilder();
        
        // Streamlined HTML Email Template
        emailBody.append("<!DOCTYPE html>")
                .append("<html><head>")
                .append("<meta charset='UTF-8'>")
                .append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>")
                .append("<title>New Leave Application - Manager Notification</title>")
                .append("<style>")
                // Optimized CSS Styles
                .append("body{font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Arial,sans-serif;line-height:1.6;color:#333;margin:0;padding:0;background-color:#f4f6f9;}")
                .append(".email-container{max-width:600px;margin:20px auto;background:white;border-radius:12px;overflow:hidden;box-shadow:0 4px 20px rgba(0,0,0,0.1);}")
                .append(".header{background:linear-gradient(135deg,#28a745,#20c997);color:white;padding:40px 30px;text-align:center;}")
                .append(".header h1{margin:0;font-size:28px;font-weight:600;}")
                .append(".header p{margin:10px 0 0 0;font-size:16px;opacity:0.9;}")
                .append(".content{padding:40px 30px;}")
                .append(".priority-section{background:#fff3cd;padding:25px;border-radius:12px;margin:25px 0;border-left:6px solid #ffc107;}")
                .append(".priority-badge{background:#ffc107;color:#212529;padding:8px 16px;border-radius:20px;font-weight:600;display:inline-block;margin-bottom:15px;font-size:14px;}")
                .append(".details-grid{display:grid;grid-template-columns:1fr 1fr;gap:20px;margin:30px 0;}")
                .append(".detail-item{background:#f8f9fa;padding:20px;border-radius:8px;border-left:4px solid #28a745;}")
                .append(".detail-label{font-weight:600;color:#495057;font-size:14px;text-transform:uppercase;letter-spacing:0.5px;}")
                .append(".detail-value{margin-top:8px;font-size:16px;color:#212529;}")
                .append(".employee-info{background:#e3f2fd;border:2px solid #90caf9;color:#1565c0;padding:25px;border-radius:12px;margin:25px 0;}")
                .append(".action-buttons{text-align:center;margin:30px 0;}")
                .append(".btn{display:inline-block;padding:12px 24px;margin:0 10px;text-decoration:none;border-radius:6px;font-weight:600;font-size:16px;transition:all 0.3s ease;}")
                .append(".btn-primary{background:#007bff;color:white;}")
                .append(".btn-success{background:#28a745;color:white;}")
                .append(".footer{background:#f8f9fa;padding:30px;text-align:center;color:#6c757d;border-top:1px solid #dee2e6;}")
                .append("@media (max-width:600px){.email-container{margin:10px;border-radius:0;}.content{padding:20px;}.details-grid{grid-template-columns:1fr;gap:15px;}}")
                .append("</style>")
                .append("</head><body>");
        
        // Email Container
        emailBody.append("<div class='email-container'>");
        
        // Header
        emailBody.append("<div class='header'>")
                .append("<h1>🆕 New Leave Application</h1>")
                .append("<p>").append(COMPANY_NAME).append("</p>")
                .append("</div>");
        
        // Content
        emailBody.append("<div class='content'>")
                .append("<h2 style='color:#2d3748;margin-bottom:20px;'>Dear ").append(manager.getManagername()).append(",</h2>")
                .append("<p style='font-size:16px;color:#4a5568;margin-bottom:30px;'>")
                .append("A new leave application has been submitted and requires your review. Please find the details below:")
                .append("</p>");
        
        // Priority Section
        emailBody.append("<div class='priority-section'>")
                .append("<div class='priority-badge'>⏳ PENDING APPROVAL</div>")
                .append("<p style='margin:0;font-size:16px;'><strong>Status:</strong> Awaiting manager review</p>")
                .append("<p style='margin:10px 0 0 0;font-size:16px;'><strong>Submitted:</strong> ").append(dateTimeFormat.format(new Date())).append("</p>")
                .append("</div>");
        
        // Employee Information
        emailBody.append("<div class='employee-info'>")
                .append("<h4 style='margin:0 0 15px 0;color:#1565c0;'>👤 Employee Information</h4>")
                .append("<p style='margin:0 0 8px 0;'><strong>Name:</strong> ").append(employee.getEmployeeName()).append("</p>")
                .append("<p style='margin:0 0 8px 0;'><strong>Employee ID:</strong> ").append(employee.getEmployeeId()).append("</p>")
                .append("<p style='margin:0;'><strong>Email:</strong> ").append(employee.getEmployeeEmail()).append("</p>")
                .append("</div>");
        
        // Application Details Grid
        emailBody.append("<h3 style='color:#2d3748;margin:40px 0 20px 0;'>📋 Leave Application Details</h3>")
                .append("<div class='details-grid'>")
                .append("<div class='detail-item'>")
                .append("<div class='detail-label'>Application ID</div>")
                .append("<div class='detail-value'>").append(application.getApplicationid()).append("</div>")
                .append("</div>")
                .append("<div class='detail-item'>")
                .append("<div class='detail-label'>Leave Type</div>")
                .append("<div class='detail-value'>").append(leaveTypeName).append("</div>")
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
                .append("<div class='detail-label'>Current Status</div>")
                .append("<div class='detail-value' style='color:#ffc107;font-weight:600;'>").append(application.getLeavestatus()).append("</div>")
                .append("</div>")
                .append("</div>");
        
        // Leave Reason (if provided)
        if (application.getLeavereason() != null && !application.getLeavereason().trim().isEmpty()) {
            emailBody.append("<h3 style='color:#2d3748;margin:40px 0 20px 0;'>📝 Leave Reason</h3>")
                    .append("<div style='background:#f8f9fa;padding:20px;border-radius:8px;border-left:4px solid #17a2b8;margin:20px 0;'>")
                    .append("<p style='margin:0;font-size:16px;color:#212529;'>").append(application.getLeavereason()).append("</p>")
                    .append("</div>");
        }
        
        // Attachment notice (if any)
        if (application.getAttachment() != null && !application.getAttachment().trim().isEmpty()) {
            emailBody.append("<div style='background:#d4edda;border:2px solid #c3e6cb;color:#155724;padding:20px;border-radius:8px;margin:20px 0;'>")
                    .append("<h4 style='margin:0 0 10px 0;'>📎 Attachment Included</h4>")
                    .append("<p style='margin:0;'>This application includes a file attachment. Please review it when processing the application.</p>")
                    .append("</div>");
        }
        
        // Action Buttons
        emailBody.append("<div class='action-buttons'>")
                .append("<h3 style='color:#2d3748;margin:40px 0 20px 0;'>🎯 Next Steps</h3>")
                .append("<p style='margin-bottom:25px;color:#6c757d;'>Please log in to the Manager Portal to review and process this application:</p>")
                .append("<a href='#' class='btn btn-primary'>📱 Open Manager Portal</a>")
                .append("<a href='#' class='btn btn-success'>✅ Review Application</a>")
                .append("</div>");
        
        // Important Notes
        emailBody.append("<div style='background:#e3f2fd;border:2px solid #90caf9;color:#1565c0;padding:20px;border-radius:8px;margin:25px 0;'>")
                .append("<h4 style='margin:0 0 15px 0;'>📌 Important Notes</h4>")
                .append("<ul style='margin:0;padding-left:20px;'>")
                .append("<li>This application is currently in <strong>PENDING</strong> status</li>")
                .append("<li>Please review and approve/reject within your standard timeframe</li>")
                .append("<li>The employee will be notified once you make a decision</li>");
        
        // Special note for Annual Leave
        if ("Annual Leave".equalsIgnoreCase(leaveTypeName)) {
            emailBody.append("<li><strong>Annual Leave:</strong> Employee's balance will be updated upon approval</li>");
        }
        
        emailBody.append("</ul>")
                .append("</div>");
        
        // Closing
        emailBody.append("<div style='margin:40px 0 0 0;padding:30px 0;border-top:1px solid #e2e8f0;'>")
                .append("<p style='margin:0 0 10px 0;font-size:16px;color:#4a5568;'>Thank you for your prompt attention to this matter.</p>")
                .append("<p style='margin:0;font-size:16px;color:#4a5568;'>")
                .append("<strong>Best regards,</strong><br>")
                .append("Employee Leave Management System<br>")
                .append("<span style='color:#28a745;'>").append(COMPANY_NAME).append("</span>")
                .append("</p>")
                .append("</div>");
        
        emailBody.append("</div>"); // Close content
        
        // Footer
        emailBody.append("<div class='footer'>")
                .append("<p style='margin:0 0 10px 0;font-size:14px;'>🔔 This is an automated notification from the Employee Leave Management System.</p>")
                .append("<p style='margin:0 0 20px 0;font-size:14px;'>Please do not reply to this email.</p>")
                .append("<p style='margin:0;font-size:12px;color:#9ca3af;'>")
                .append("© ").append(new SimpleDateFormat("yyyy").format(new Date())).append(" ").append(COMPANY_NAME).append(". All rights reserved.<br>")
                .append("Email sent on: ").append(dateTimeFormat.format(new Date()))
                .append("</p>")
                .append("</div>");
        
        emailBody.append("</div>"); // Close email container
        emailBody.append("</body></html>");
        
        return emailBody.toString();
    }
    
    /**
     * Format date string for display
     */
    private static String formatDateString(String dateStr) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM dd, yyyy");
            Date date = inputFormat.parse(dateStr);
            return outputFormat.format(date);
        } catch (Exception e) {
            return dateStr; // Return original if parsing fails
        }
    }
    
    /**
     * Optimized email sending using JavaMail API
     * Debug mode disabled for production performance
     */
    private static boolean sendEmail(String toEmail, String toName, String subject, String htmlContent) {
        try {
            // Email properties
            Properties props = new Properties();
            props.put("mail.smtp.host", SMTP_HOST);
            props.put("mail.smtp.port", SMTP_PORT);
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.ssl.trust", SMTP_HOST);
            props.put("mail.smtp.ssl.protocols", "TLSv1.2");
            props.put("mail.smtp.connectiontimeout", "10000"); // 10 seconds
            props.put("mail.smtp.timeout", "10000"); // 10 seconds
            
            // Create session with authentication
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(EMAIL_USERNAME, EMAIL_PASSWORD);
                }
            });
            
            // Disable debug mode for production performance
            session.setDebug(false);
            
            // Create message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL, COMPANY_NAME));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail, toName));
            message.setSubject(subject);
            message.setContent(htmlContent, "text/html; charset=utf-8");
            message.setSentDate(new Date());
            
            // Add headers for better deliverability
            message.setHeader("X-Mailer", "ELMS-Manager-Notification-System");
            message.setHeader("X-Priority", "3");
            
            // Send message
            Transport.send(message);
            
            return true;
            
        } catch (AuthenticationFailedException e) {
            System.err.println("Email authentication failed. Check your Gmail App Password.");
            return false;
        } catch (MessagingException e) {
            System.err.println("Email messaging error: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("Unexpected error sending manager notification email: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Validate email configuration
     */
    private static boolean isConfigurationValid() {
        boolean isValid = true;
        
        if (EMAIL_USERNAME.equals("your-email@gmail.com")) {
            System.err.println("Please update EMAIL_USERNAME in ManagerNotificationService.java");
            isValid = false;
        }
        
        if (EMAIL_PASSWORD.equals("your-app-password")) {
            System.err.println("Please update EMAIL_PASSWORD in ManagerNotificationService.java");
            isValid = false;
        }
        
        if (EMAIL_PASSWORD.length() != 16 && !EMAIL_PASSWORD.equals("your-app-password")) {
            System.err.println("Warning: Gmail App Passwords are usually 16 characters long");
        }
        
        return isValid;
    }
    
    /**
     * Test manager notification email configuration
     * Optimized version with minimal logging
     */
    public static boolean testManagerNotificationConfiguration() {
        try {
            // Check configuration
            if (!isConfigurationValid()) {
                return false;
            }
            
            String testSubject = "[" + COMPANY_NAME + "] 🧪 Manager Notification System Test";
            String testBody = createTestManagerNotificationEmailBody();
            
            boolean result = sendEmail(EMAIL_USERNAME, "System Administrator", testSubject, testBody);
            
            if (result) {
                System.out.println("Manager notification email configuration test PASSED");
            } else {
                System.err.println("Manager notification email configuration test FAILED");
            }
            
            return result;
            
        } catch (Exception e) {
            System.err.println("Manager notification email configuration test failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Create test email body for manager notifications
     * Optimized version
     */
    private static String createTestManagerNotificationEmailBody() {
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("MMMM dd, yyyy 'at' hh:mm a");
        
        return "<!DOCTYPE html><html><head><meta charset='UTF-8'><style>" +
               "body{font-family:Arial,sans-serif;line-height:1.6;color:#333;margin:0;padding:0;background:#f4f6f9;}" +
               ".container{max-width:600px;margin:20px auto;background:white;border-radius:12px;overflow:hidden;box-shadow:0 4px 20px rgba(0,0,0,0.1);}" +
               ".header{background:linear-gradient(135deg,#28a745,#20c997);color:white;padding:40px 30px;text-align:center;}" +
               ".content{padding:40px 30px;}" +
               ".success-box{background:#d4edda;border:2px solid #c3e6cb;color:#155724;padding:25px;border-radius:12px;margin:25px 0;}" +
               ".footer{background:#f8f9fa;padding:20px;text-align:center;color:#6c757d;}" +
               "</style></head><body>" +
               "<div class='container'>" +
               "<div class='header'>" +
               "<h1>✅ Manager Notification System Test</h1>" +
               "<p>" + COMPANY_NAME + "</p>" +
               "</div>" +
               "<div class='content'>" +
               "<h2>Congratulations!</h2>" +
               "<p>This is a test email to verify that your ELMS manager notification system is working correctly.</p>" +
               "<div class='success-box'>" +
               "<h4>🎉 Test Results: PASSED</h4>" +
               "<p>If you receive this email, your manager notification configuration is working perfectly!</p>" +
               "</div>" +
               "<h3>📋 Configuration Details:</h3>" +
               "<ul>" +
               "<li><strong>SMTP Server:</strong> " + SMTP_HOST + ":" + SMTP_PORT + "</li>" +
               "<li><strong>From Email:</strong> " + FROM_EMAIL + "</li>" +
               "<li><strong>Test Sent:</strong> " + dateTimeFormat.format(new Date()) + "</li>" +
               "</ul>" +
               "<h3>🚀 Next Steps:</h3>" +
               "<ol>" +
               "<li>Test the complete employee leave submission flow</li>" +
               "<li>Verify managers receive email notifications for new applications</li>" +
               "<li>Check that existing employee approval/rejection emails still work</li>" +
               "</ol>" +
               "<p>Your ELMS manager notification system is ready for production use!</p>" +
               "</div>" +
               "<div class='footer'>" +
               "<p>This is an automated test message from the ELMS Manager Notification System.</p>" +
               "<p>© " + new SimpleDateFormat("yyyy").format(new Date()) + " " + COMPANY_NAME + "</p>" +
               "</div>" +
               "</div></body></html>";
    }
}
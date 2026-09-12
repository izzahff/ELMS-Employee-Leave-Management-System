package elms.service;

import elms.model.LeaveApplication;
import elms.model.Employee;
import elms.model.Manager;
import elms.model.LeaveType;
import elms.DAO.EmployeeDAO;
import elms.DAO.LeaveTypeDAO;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;
import java.util.Date;
import java.text.SimpleDateFormat;

/**
 * Service class for sending email notifications to employees when their leave status is updated
 * Compatible with Jakarta EE applications using direct configuration
 */
public class EmailNotificationService {
    
    // ============================================================================
    // EMAIL CONFIGURATION - UPDATE THESE WITH YOUR ACTUAL VALUES
    // ============================================================================
    
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    
    // TODO: Replace with your actual Gmail credentials
    private static final String EMAIL_USERNAME = "intanarina15@gmail.com";        // ← CHANGE THIS
    private static final String EMAIL_PASSWORD = "hgcf huzk jvnx goib";           // ← CHANGE THIS (16-char App Password)
    
    // Email display settings
    private static final String FROM_EMAIL = "noreply@imnsb.com";              // ← CHANGE THIS
    private static final String COMPANY_NAME = "Iktisas Management Network Sdn. Bhd. (IMNSB)";
    
    // ============================================================================
    // EMAIL NOTIFICATION METHODS
    // ============================================================================
    
    /**
     * Send email notification when leave application status is updated
     */
    public static boolean sendLeaveStatusNotification(LeaveApplication application, String action, 
                                                    Manager manager, String rejectionReason) {
        try {
            System.out.println("=== SENDING EMAIL NOTIFICATION ===");
            
            // Validate configuration
            if (!isConfigurationValid()) {
                System.err.println("❌ Email configuration is not valid. Please update EmailNotificationService.java");
                return false;
            }
            
            // Get employee details
            Employee employee = EmployeeDAO.getEmployeeByIdOnly(application.getEmployeeid());
            if (employee == null) {
                System.err.println("❌ Employee not found for notification");
                return false;
            }
            
            // Get leave type details
            LeaveTypeDAO leaveTypeDAO = new LeaveTypeDAO();
            LeaveType leaveType = leaveTypeDAO.getLeaveTypeById(application.getLeavetypeid());
            String leaveTypeName = (leaveType != null) ? leaveType.getLeaveTypeName() : "Leave";
            
            // Create email content
            String subject = createEmailSubject(action, leaveTypeName, application.getApplicationid());
            String emailBody = createEmailBody(employee, application, leaveTypeName, action, manager, rejectionReason);
            
            // Send email
            boolean sent = sendEmail(employee.getEmployeeEmail(), employee.getEmployeeName(), subject, emailBody);
            
            if (sent) {
                System.out.println("✅ Email notification sent successfully to: " + employee.getEmployeeEmail());
            } else {
                System.err.println("❌ Failed to send email notification");
            }
            
            return sent;
            
        } catch (Exception e) {
            System.err.println("❌ Error sending email notification: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Validate email configuration
     */
    private static boolean isConfigurationValid() {
        boolean isValid = true;
        
        if (EMAIL_USERNAME.equals("your-email@gmail.com")) {
            System.err.println("❌ Please update EMAIL_USERNAME in EmailNotificationService.java");
            isValid = false;
        }
        
        if (EMAIL_PASSWORD.equals("your-app-password")) {
            System.err.println("❌ Please update EMAIL_PASSWORD in EmailNotificationService.java");
            isValid = false;
        }
        
        if (EMAIL_PASSWORD.length() != 16 && !EMAIL_PASSWORD.equals("your-app-password")) {
            System.err.println("⚠️ Warning: Gmail App Passwords are usually 16 characters long");
        }
        
        return isValid;
    }
    
    /**
     * Create email subject based on action
     */
    private static String createEmailSubject(String action, String leaveTypeName, String applicationId) {
        if ("approve".equals(action)) {
            return String.format("[%s] Leave Application APPROVED - %s (ID: %s)", 
                               COMPANY_NAME, leaveTypeName, applicationId);
        } else if ("reject".equals(action)) {
            return String.format("[%s] Leave Application REJECTED - %s (ID: %s)", 
                               COMPANY_NAME, leaveTypeName, applicationId);
        } else {
            return String.format("[%s] Leave Application Status Updated - %s (ID: %s)", 
                               COMPANY_NAME, leaveTypeName, applicationId);
        }
    }
    
    /**
     * Create professional HTML email body
     */
    private static String createEmailBody(Employee employee, LeaveApplication application, 
                                        String leaveTypeName, String action, Manager manager, String rejectionReason) {
        
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("MMMM dd, yyyy 'at' hh:mm a");
        
        String statusColor = "approve".equals(action) ? "#28a745" : "#dc3545";
        String statusIcon = "approve".equals(action) ? "✅" : "❌";
        String statusText = "approve".equals(action) ? "APPROVED" : "REJECTED";
        
        StringBuilder emailBody = new StringBuilder();
        
        // Professional HTML Email Template
        emailBody.append("<!DOCTYPE html>")
                .append("<html><head>")
                .append("<meta charset='UTF-8'>")
                .append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>")
                .append("<title>Leave Application Status Update</title>")
                .append("<style>")
                // CSS Styles
                .append("body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Arial, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; background-color: #f4f6f9; }")
                .append(".email-container { max-width: 600px; margin: 20px auto; background: white; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 20px rgba(0,0,0,0.1); }")
                .append(".header { background: linear-gradient(135deg, #007bff, #0056b3); color: white; padding: 40px 30px; text-align: center; }")
                .append(".header h1 { margin: 0; font-size: 28px; font-weight: 600; }")
                .append(".header p { margin: 10px 0 0 0; font-size: 16px; opacity: 0.9; }")
                .append(".content { padding: 40px 30px; }")
                .append(".status-section { background: ").append("approve".equals(action) ? "#f0fff4" : "#fff5f5").append("; padding: 30px; border-radius: 12px; margin: 30px 0; border-left: 6px solid ").append(statusColor).append("; }")
                .append(".status-badge { background: ").append(statusColor).append("; color: white; padding: 12px 20px; border-radius: 25px; font-weight: 600; display: inline-block; margin-bottom: 20px; font-size: 16px; }")
                .append(".details-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; margin: 30px 0; }")
                .append(".detail-item { background: #f8f9fa; padding: 20px; border-radius: 8px; border-left: 4px solid #007bff; }")
                .append(".detail-label { font-weight: 600; color: #495057; font-size: 14px; text-transform: uppercase; letter-spacing: 0.5px; }")
                .append(".detail-value { margin-top: 8px; font-size: 16px; color: #212529; }")
                .append(".rejection-reason { background: #fff5f5; border: 2px solid #fed7d7; color: #c53030; padding: 25px; border-radius: 12px; margin: 25px 0; }")
                .append(".approval-notice { background: #f0fff4; border: 2px solid #c6f6d5; color: #2f855a; padding: 25px; border-radius: 12px; margin: 25px 0; }")
                .append(".action-list { list-style: none; padding: 0; }")
                .append(".action-list li { padding: 8px 0; display: flex; align-items: center; }")
                .append(".action-list li::before { content: '✓'; color: #28a745; font-weight: bold; margin-right: 12px; }")
                .append(".highlight-box { background: #fff3cd; border: 2px solid #ffeaa7; color: #856404; padding: 25px; border-radius: 12px; margin: 25px 0; }")
                .append(".footer { background: #f8f9fa; padding: 30px; text-align: center; color: #6c757d; border-top: 1px solid #dee2e6; }")
                .append(".footer-links { margin: 20px 0; }")
                .append(".footer-links a { color: #007bff; text-decoration: none; margin: 0 15px; }")
                .append("@media (max-width: 600px) { .email-container { margin: 10px; border-radius: 0; } .content { padding: 20px; } .details-grid { grid-template-columns: 1fr; gap: 15px; } }")
                .append("</style>")
                .append("</head><body>");
        
        // Email Container
        emailBody.append("<div class='email-container'>");
        
        // Header
        emailBody.append("<div class='header'>")
                .append("<h1>").append(statusIcon).append(" Leave Application ").append(statusText).append("</h1>")
                .append("<p>").append(COMPANY_NAME).append("</p>")
                .append("</div>");
        
        // Content
        emailBody.append("<div class='content'>")
                .append("<h2 style='color: #2d3748; margin-bottom: 20px;'>Dear ").append(employee.getEmployeeName()).append(",</h2>")
                .append("<p style='font-size: 16px; color: #4a5568; margin-bottom: 30px;'>")
                .append("We are writing to inform you that your leave application has been <strong style='color: ").append(statusColor).append(";'>").append(statusText).append("</strong> by your manager.")
                .append("</p>");
        
        // Status Section
        emailBody.append("<div class='status-section'>")
                .append("<div class='status-badge'>").append(statusText).append("</div>")
                .append("<p style='margin: 0; font-size: 16px;'><strong>Reviewed by:</strong> ").append(manager.getManagername()).append("</p>")
                .append("<p style='margin: 10px 0 0 0; font-size: 16px;'><strong>Review Date:</strong> ").append(dateTimeFormat.format(new Date())).append("</p>")
                .append("</div>");
        
        // Application Details Grid
        emailBody.append("<h3 style='color: #2d3748; margin: 40px 0 20px 0;'>📋 Application Details</h3>")
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
                .append("<div class='detail-label'>Reason</div>")
                .append("<div class='detail-value'>").append(application.getLeavereason() != null ? application.getLeavereason() : "N/A").append("</div>")
                .append("</div>")
                .append("</div>");
        
        // Rejection Reason (if applicable)
        if ("reject".equals(action) && rejectionReason != null && !rejectionReason.trim().isEmpty()) {
            emailBody.append("<h3 style='color: #c53030; margin: 40px 0 20px 0;'>❌ Rejection Reason</h3>")
                    .append("<div class='rejection-reason'>")
                    .append("<p style='margin: 0; font-size: 16px; font-weight: 600;'>").append(rejectionReason).append("</p>")
                    .append("</div>");
        }
        
        // Action-specific content
        if ("approve".equals(action)) {
            emailBody.append("<div class='approval-notice'>")
                    .append("<h4 style='margin: 0 0 15px 0; color: #2f855a;'>🎉 Your leave application has been approved!</h4>")
                    .append("<p style='margin: 0 0 15px 0;'><strong>What to do next:</strong></p>")
                    .append("<ul class='action-list'>")
                    .append("<li>Coordinate with your team regarding your absence</li>")
                    .append("<li>Complete any pending tasks before your leave</li>")
                    .append("<li>Ensure proper handover if necessary</li>");
            
            // Check if it's Annual Leave for balance update
            if ("Annual Leave".equalsIgnoreCase(leaveTypeName)) {
                emailBody.append("<li>Your annual leave balance has been updated</li>");
            }
            
            emailBody.append("</ul>")
                    .append("</div>");
        } else {
            emailBody.append("<div class='highlight-box'>")
                    .append("<h4 style='margin: 0 0 15px 0; color: #856404;'>📞 Need clarification?</h4>")
                    .append("<p style='margin: 0;'>If you have any questions about this decision, please contact:</p>")
                    .append("<ul style='margin: 15px 0 0 20px;'>")
                    .append("<li><strong>Your Manager:</strong> ").append(manager.getManagername()).append("</li>")
                    .append("<li><strong>HR Department:</strong> Contact for further assistance</li>")
                    .append("</ul>")
                    .append("</div>");
        }
        
        // Next Steps
        emailBody.append("<h3 style='color: #2d3748; margin: 40px 0 20px 0;'>🔗 Access Your Account</h3>")
                .append("<p style='font-size: 16px; color: #4a5568;'>")
                .append("You can view your complete leave history and apply for new leave through the Employee Portal. ")
                .append("Log in with your employee credentials to access all features.")
                .append("</p>");
        
        // Closing
        emailBody.append("<div style='margin: 40px 0 0 0; padding: 30px 0; border-top: 1px solid #e2e8f0;'>")
                .append("<p style='margin: 0 0 10px 0; font-size: 16px; color: #4a5568;'>Thank you for using the ").append(COMPANY_NAME).append(" Leave Management System.</p>")
                .append("<p style='margin: 0; font-size: 16px; color: #4a5568;'>")
                .append("<strong>Best regards,</strong><br>")
                .append("Human Resources Department<br>")
                .append("<span style='color: #007bff;'>").append(COMPANY_NAME).append("</span>")
                .append("</p>")
                .append("</div>");
        
        emailBody.append("</div>"); // Close content
        
        // Footer
        emailBody.append("<div class='footer'>")
                .append("<p style='margin: 0 0 10px 0; font-size: 14px;'>🔒 This is an automated message from the Employee Leave Management System.</p>")
                .append("<p style='margin: 0 0 20px 0; font-size: 14px;'>Please do not reply to this email.</p>")
                .append("<div class='footer-links'>")
                .append("<a href='#'>Employee Portal</a>")
                .append("<a href='#'>HR Contact</a>")
                .append("<a href='#'>Company Policy</a>")
                .append("</div>")
                .append("<p style='margin: 20px 0 0 0; font-size: 12px; color: #9ca3af;'>")
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
     * Send email using JavaMail API
     */
    private static boolean sendEmail(String toEmail, String toName, String subject, String htmlContent) {
        try {
            System.out.println("📧 Sending email to: " + toEmail);
            System.out.println("📧 Subject: " + subject);
            System.out.println("📧 Using SMTP: " + SMTP_HOST + ":" + SMTP_PORT);
            
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
            
            // Enable debug mode for troubleshooting (set to false in production)
            session.setDebug(false);
            
            // Create message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL, COMPANY_NAME));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail, toName));
            message.setSubject(subject);
            message.setContent(htmlContent, "text/html; charset=utf-8");
            message.setSentDate(new Date());
            
            // Add headers for better deliverability
            message.setHeader("X-Mailer", "ELMS-LeaveManagement-System");
            message.setHeader("X-Priority", "3");
            
            // Send message
            Transport.send(message);
            
            System.out.println("✅ Email sent successfully to: " + toEmail);
            return true;
            
        } catch (AuthenticationFailedException e) {
            System.err.println("❌ Email authentication failed. Check your Gmail App Password.");
            System.err.println("❌ Error: " + e.getMessage());
            return false;
        } catch (MessagingException e) {
            System.err.println("❌ Email messaging error: " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (Exception e) {
            System.err.println("❌ Unexpected error sending email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Test email configuration
     */
    public static boolean testEmailConfiguration() {
        try {
            System.out.println("=== TESTING EMAIL CONFIGURATION ===");
            
            // Check configuration
            if (!isConfigurationValid()) {
                return false;
            }
            
            System.out.println("📧 Email Configuration:");
            System.out.println("  SMTP Host: " + SMTP_HOST);
            System.out.println("  SMTP Port: " + SMTP_PORT);
            System.out.println("  Username: " + EMAIL_USERNAME);
            System.out.println("  From Email: " + FROM_EMAIL);
            System.out.println("  Company: " + COMPANY_NAME);
            
            String testSubject = "[" + COMPANY_NAME + "] 🧪 Email Configuration Test";
            String testBody = createTestEmailBody();
            
            boolean result = sendEmail(EMAIL_USERNAME, "System Administrator", testSubject, testBody);
            
            if (result) {
                System.out.println("✅ Email configuration test PASSED");
                System.out.println("📧 Check your email inbox for the test message");
            } else {
                System.err.println("❌ Email configuration test FAILED");
            }
            
            return result;
            
        } catch (Exception e) {
            System.err.println("❌ Email configuration test failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Create test email body
     */
    private static String createTestEmailBody() {
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
               "<h1>✅ Email Configuration Test</h1>" +
               "<p>" + COMPANY_NAME + "</p>" +
               "</div>" +
               "<div class='content'>" +
               "<h2>Congratulations!</h2>" +
               "<p>This is a test email to verify that your ELMS email configuration is working correctly.</p>" +
               "<div class='success-box'>" +
               "<h4>🎉 Test Results: PASSED</h4>" +
               "<p>If you receive this email, your configuration is working perfectly!</p>" +
               "</div>" +
               "<h3>📋 Configuration Details:</h3>" +
               "<ul>" +
               "<li><strong>SMTP Server:</strong> " + SMTP_HOST + ":" + SMTP_PORT + "</li>" +
               "<li><strong>From Email:</strong> " + FROM_EMAIL + "</li>" +
               "<li><strong>Test Sent:</strong> " + dateTimeFormat.format(new Date()) + "</li>" +
               "</ul>" +
               "<h3>🚀 Next Steps:</h3>" +
               "<ol>" +
               "<li>Test the complete leave approval/rejection flow</li>" +
               "<li>Verify employees receive email notifications</li>" +
               "<li>Check visual alerts appear in the employee portal</li>" +
               "</ol>" +
               "<p>Your ELMS email notification system is ready for production use!</p>" +
               "</div>" +
               "<div class='footer'>" +
               "<p>This is an automated test message from the ELMS system.</p>" +
               "<p>© " + new SimpleDateFormat("yyyy").format(new Date()) + " " + COMPANY_NAME + "</p>" +
               "</div>" +
               "</div></body></html>";
    }
    
    /**
     * Send welcome email to new employees
     */
    public static boolean sendWelcomeEmail(Employee employee, String tempPassword) {
        try {
            if (!isConfigurationValid()) {
                return false;
            }
            
            String subject = "Welcome to " + COMPANY_NAME + " - Employee Portal Access";
            String emailBody = createWelcomeEmailBody(employee, tempPassword);
            
            return sendEmail(employee.getEmployeeEmail(), employee.getEmployeeName(), subject, emailBody);
            
        } catch (Exception e) {
            System.err.println("❌ Error sending welcome email: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Create welcome email body
     */
    private static String createWelcomeEmailBody(Employee employee, String tempPassword) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy");
        
        return "<!DOCTYPE html><html><head><meta charset='UTF-8'><style>" +
               "body{font-family:Arial,sans-serif;line-height:1.6;color:#333;margin:0;padding:0;background:#f4f6f9;}" +
               ".container{max-width:600px;margin:20px auto;background:white;border-radius:12px;overflow:hidden;box-shadow:0 4px 20px rgba(0,0,0,0.1);}" +
               ".header{background:linear-gradient(135deg,#007bff,#0056b3);color:white;padding:40px 30px;text-align:center;}" +
               ".content{padding:40px 30px;}" +
               ".credentials{background:#f8f9fa;padding:25px;border-radius:12px;margin:25px 0;border-left:6px solid #007bff;}" +
               ".warning{background:#fff3cd;border:2px solid #ffeaa7;color:#856404;padding:20px;border-radius:8px;margin:20px 0;}" +
               ".footer{background:#f8f9fa;padding:20px;text-align:center;color:#6c757d;}" +
               "</style></head><body>" +
               "<div class='container'>" +
               "<div class='header'>" +
               "<h1>Welcome to " + COMPANY_NAME + "</h1>" +
               "</div>" +
               "<div class='content'>" +
               "<h2>Dear " + employee.getEmployeeName() + ",</h2>" +
               "<p>Welcome to " + COMPANY_NAME + "! Your employee account has been created successfully.</p>" +
               "<div class='credentials'>" +
               "<h3>🔑 Your Login Credentials</h3>" +
               "<p><strong>Employee ID:</strong> " + employee.getEmployeeId() + "</p>" +
               "<p><strong>Temporary Password:</strong> " + tempPassword + "</p>" +
               "<p><strong>Email:</strong> " + employee.getEmployeeEmail() + "</p>" +
               "</div>" +
               "<div class='warning'>" +
               "<p><strong>⚠️ Important:</strong> Please change your password upon first login for security purposes.</p>" +
               "</div>" +
               "<h3>🚀 What you can do with your account:</h3>" +
               "<ul>" +
               "<li>Apply for leave through the Employee Portal</li>" +
               "<li>View your leave history and current balance</li>" +
               "<li>Update your profile information</li>" +
               "<li>Track your leave application status</li>" +
               "<li>Receive email notifications for status updates</li>" +
               "</ul>" +
               "<p>If you have any questions or need assistance, please contact the HR department.</p>" +
               "<p>We're excited to have you as part of our team!</p>" +
               "<p><strong>Best regards,</strong><br>HR Department<br>" + COMPANY_NAME + "</p>" +
               "</div>" +
               "<div class='footer'>" +
               "<p>This is an automated welcome message.</p>" +
               "<p>© " + new SimpleDateFormat("yyyy").format(new Date()) + " " + COMPANY_NAME + "</p>" +
               "</div>" +
               "</div></body></html>";
    }
}
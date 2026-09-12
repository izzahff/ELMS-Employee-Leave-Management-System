package elms.service;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;
import java.util.Date;
import java.text.SimpleDateFormat;

/**
 * Email service for forgot password functionality
 * Handles OTP sending and password reset confirmation emails
 */
public class ForgotPasswordEmailService {
    
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
    
    /**
     * Send OTP email to user
     */
    public static boolean sendOTPEmail(String toEmail, String toName, String otp) {
        try {
            System.out.println("=== SENDING OTP EMAIL ===");
            
            // Validate configuration
            if (!isConfigurationValid()) {
                System.err.println("❌ Email configuration is not valid. Please update ForgotPasswordEmailService.java");
                return false;
            }
            
            String subject = "[" + COMPANY_NAME + "] Password Reset OTP - " + otp;
            String emailBody = createOTPEmailBody(toName, otp);
            
            boolean sent = sendEmail(toEmail, toName, subject, emailBody);
            
            if (sent) {
                System.out.println("✅ OTP email sent successfully to: " + toEmail);
            } else {
                System.err.println("❌ Failed to send OTP email");
            }
            
            return sent;
            
        } catch (Exception e) {
            System.err.println("❌ Error sending OTP email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Send password reset confirmation email
     */
    public static boolean sendPasswordResetConfirmationEmail(String toEmail, String toName) {
        try {
            System.out.println("=== SENDING PASSWORD RESET CONFIRMATION EMAIL ===");
            
            if (!isConfigurationValid()) {
                System.err.println("❌ Email configuration is not valid");
                return false;
            }
            
            String subject = "[" + COMPANY_NAME + "] Password Reset Successful";
            String emailBody = createPasswordResetConfirmationEmailBody(toName);
            
            boolean sent = sendEmail(toEmail, toName, subject, emailBody);
            
            if (sent) {
                System.out.println("✅ Password reset confirmation email sent successfully to: " + toEmail);
            } else {
                System.err.println("❌ Failed to send password reset confirmation email");
            }
            
            return sent;
            
        } catch (Exception e) {
            System.err.println("❌ Error sending password reset confirmation email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Create OTP email body
     */
    private static String createOTPEmailBody(String employeeName, String otp) {
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("MMMM dd, yyyy 'at' hh:mm a");
        
        StringBuilder emailBody = new StringBuilder();
        
        emailBody.append("<!DOCTYPE html>")
                .append("<html><head>")
                .append("<meta charset='UTF-8'>")
                .append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>")
                .append("<title>Password Reset OTP</title>")
                .append("<style>")
                .append("body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Arial, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; background-color: #f4f6f9; }")
                .append(".email-container { max-width: 600px; margin: 20px auto; background: white; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 20px rgba(0,0,0,0.1); }")
                .append(".header { background: linear-gradient(135deg, #dc3545, #c82333); color: white; padding: 40px 30px; text-align: center; }")
                .append(".header h1 { margin: 0; font-size: 28px; font-weight: 600; }")
                .append(".header p { margin: 10px 0 0 0; font-size: 16px; opacity: 0.9; }")
                .append(".content { padding: 40px 30px; }")
                .append(".otp-section { background: #fff3cd; padding: 30px; border-radius: 12px; margin: 30px 0; border: 2px solid #ffeaa7; text-align: center; }")
                .append(".otp-code { font-size: 48px; font-weight: bold; color: #dc3545; letter-spacing: 8px; margin: 20px 0; font-family: 'Courier New', monospace; }")
                .append(".warning-box { background: #f8d7da; border: 2px solid #f5c6cb; color: #721c24; padding: 25px; border-radius: 12px; margin: 25px 0; }")
                .append(".info-box { background: #d4edda; border: 2px solid #c3e6cb; color: #155724; padding: 25px; border-radius: 12px; margin: 25px 0; }")
                .append(".footer { background: #f8f9fa; padding: 30px; text-align: center; color: #6c757d; border-top: 1px solid #dee2e6; }")
                .append("@media (max-width: 600px) { .email-container { margin: 10px; border-radius: 0; } .content { padding: 20px; } .otp-code { font-size: 36px; letter-spacing: 4px; } }")
                .append("</style>")
                .append("</head><body>");
        
        emailBody.append("<div class='email-container'>")
                .append("<div class='header'>")
                .append("<h1>🔐 Password Reset OTP</h1>")
                .append("<p>").append(COMPANY_NAME).append("</p>")
                .append("</div>");
        
        emailBody.append("<div class='content'>")
                .append("<h2 style='color: #2d3748; margin-bottom: 20px;'>Dear ").append(employeeName).append(",</h2>")
                .append("<p style='font-size: 16px; color: #4a5568; margin-bottom: 30px;'>")
                .append("You have requested to reset your password for your ").append(COMPANY_NAME).append(" employee account. ")
                .append("Please use the following One-Time Password (OTP) to proceed with your password reset.")
                .append("</p>");
        
        emailBody.append("<div class='otp-section'>")
                .append("<h3 style='margin: 0 0 20px 0; color: #dc3545;'>🔑 Your OTP Code</h3>")
                .append("<div class='otp-code'>").append(otp).append("</div>")
                .append("<p style='margin: 20px 0 0 0; color: #856404; font-weight: 600;'>Valid for 5 minutes only</p>")
                .append("</div>");
        
        emailBody.append("<div class='warning-box'>")
                .append("<h4 style='margin: 0 0 15px 0; color: #721c24;'>⚠️ Important Security Notice</h4>")
                .append("<ul style='margin: 0; padding-left: 20px;'>")
                .append("<li>This OTP will expire in <strong>5 minutes</strong></li>")
                .append("<li>Do not share this OTP with anyone</li>")
                .append("<li>If you didn't request this reset, please ignore this email</li>")
                .append("<li>You have <strong>3 attempts</strong> to enter the correct OTP</li>")
                .append("</ul>")
                .append("</div>");
        
        emailBody.append("<div class='info-box'>")
                .append("<h4 style='margin: 0 0 15px 0; color: #155724;'>📝 Next Steps</h4>")
                .append("<ol style='margin: 0; padding-left: 20px;'>")
                .append("<li>Return to the password reset page</li>")
                .append("<li>Enter the OTP code: <strong>").append(otp).append("</strong></li>")
                .append("<li>Create your new password</li>")
                .append("<li>Log in with your new credentials</li>")
                .append("</ol>")
                .append("</div>");
        
        emailBody.append("<h3 style='color: #2d3748; margin: 40px 0 20px 0;'>🔗 Need Help?</h3>")
                .append("<p style='font-size: 16px; color: #4a5568;'>")
                .append("If you're having trouble with the password reset process or didn't request this change, ")
                .append("please contact your system administrator or IT support team immediately.")
                .append("</p>");
        
        emailBody.append("<div style='margin: 40px 0 0 0; padding: 30px 0; border-top: 1px solid #e2e8f0;'>")
                .append("<p style='margin: 0 0 10px 0; font-size: 16px; color: #4a5568;'>")
                .append("This password reset request was initiated on ").append(dateTimeFormat.format(new Date())).append(".</p>")
                .append("<p style='margin: 0; font-size: 16px; color: #4a5568;'>")
                .append("<strong>Best regards,</strong><br>")
                .append("IT Security Team<br>")
                .append("<span style='color: #dc3545;'>").append(COMPANY_NAME).append("</span>")
                .append("</p>")
                .append("</div>");
        
        emailBody.append("</div>");
        
        emailBody.append("<div class='footer'>")
                .append("<p style='margin: 0 0 10px 0; font-size: 14px;'>🔒 This is an automated security message from the Employee Leave Management System.</p>")
                .append("<p style='margin: 0 0 20px 0; font-size: 14px;'>Please do not reply to this email.</p>")
                .append("<p style='margin: 20px 0 0 0; font-size: 12px; color: #9ca3af;'>")
                .append("© ").append(new SimpleDateFormat("yyyy").format(new Date())).append(" ").append(COMPANY_NAME).append(". All rights reserved.<br>")
                .append("Email sent on: ").append(dateTimeFormat.format(new Date()))
                .append("</p>")
                .append("</div>");
        
        emailBody.append("</div></body></html>");
        
        return emailBody.toString();
    }
    
    /**
     * Create password reset confirmation email body
     */
    private static String createPasswordResetConfirmationEmailBody(String employeeName) {
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("MMMM dd, yyyy 'at' hh:mm a");
        
        StringBuilder emailBody = new StringBuilder();
        
        emailBody.append("<!DOCTYPE html>")
                .append("<html><head>")
                .append("<meta charset='UTF-8'>")
                .append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>")
                .append("<title>Password Reset Successful</title>")
                .append("<style>")
                .append("body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Arial, sans-serif; line-height: 1.6; color: #333; margin: 0; padding: 0; background-color: #f4f6f9; }")
                .append(".email-container { max-width: 600px; margin: 20px auto; background: white; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 20px rgba(0,0,0,0.1); }")
                .append(".header { background: linear-gradient(135deg, #28a745, #20c997); color: white; padding: 40px 30px; text-align: center; }")
                .append(".header h1 { margin: 0; font-size: 28px; font-weight: 600; }")
                .append(".header p { margin: 10px 0 0 0; font-size: 16px; opacity: 0.9; }")
                .append(".content { padding: 40px 30px; }")
                .append(".success-section { background: #d4edda; padding: 30px; border-radius: 12px; margin: 30px 0; border: 2px solid #c3e6cb; text-align: center; }")
                .append(".warning-box { background: #fff3cd; border: 2px solid #ffeaa7; color: #856404; padding: 25px; border-radius: 12px; margin: 25px 0; }")
                .append(".info-box { background: #d1ecf1; border: 2px solid #bee5eb; color: #0c5460; padding: 25px; border-radius: 12px; margin: 25px 0; }")
                .append(".footer { background: #f8f9fa; padding: 30px; text-align: center; color: #6c757d; border-top: 1px solid #dee2e6; }")
                .append("@media (max-width: 600px) { .email-container { margin: 10px; border-radius: 0; } .content { padding: 20px; } }")
                .append("</style>")
                .append("</head><body>");
        
        emailBody.append("<div class='email-container'>")
                .append("<div class='header'>")
                .append("<h1>✅ Password Reset Successful</h1>")
                .append("<p>").append(COMPANY_NAME).append("</p>")
                .append("</div>");
        
        emailBody.append("<div class='content'>")
                .append("<h2 style='color: #2d3748; margin-bottom: 20px;'>Dear ").append(employeeName).append(",</h2>")
                .append("<p style='font-size: 16px; color: #4a5568; margin-bottom: 30px;'>")
                .append("Your password has been successfully reset for your ").append(COMPANY_NAME).append(" employee account. ")
                .append("You can now log in using your new password.")
                .append("</p>");
        
        emailBody.append("<div class='success-section'>")
                .append("<h3 style='margin: 0 0 20px 0; color: #28a745;'>🎉 Password Reset Complete!</h3>")
                .append("<p style='margin: 0; font-size: 16px; color: #155724;'>")
                .append("Your password was successfully updated on ").append(dateTimeFormat.format(new Date()))
                .append("</p>")
                .append("</div>");
        
        emailBody.append("<div class='info-box'>")
                .append("<h4 style='margin: 0 0 15px 0; color: #0c5460;'>🔐 Security Recommendations</h4>")
                .append("<ul style='margin: 0; padding-left: 20px;'>")
                .append("<li>Keep your new password secure and confidential</li>")
                .append("<li>Use a unique password that you don't use elsewhere</li>")
                .append("<li>Consider using a password manager</li>")
                .append("<li>Log out of shared computers after use</li>")
                .append("</ul>")
                .append("</div>");
        
        emailBody.append("<div class='warning-box'>")
                .append("<h4 style='margin: 0 0 15px 0; color: #856404;'>⚠️ Didn't Reset Your Password?</h4>")
                .append("<p style='margin: 0;'>")
                .append("If you didn't request this password reset, your account may be compromised. ")
                .append("Please contact your system administrator or IT support team immediately.")
                .append("</p>")
                .append("</div>");
        
        emailBody.append("<h3 style='color: #2d3748; margin: 40px 0 20px 0;'>🔗 Access Your Account</h3>")
                .append("<p style='font-size: 16px; color: #4a5568;'>")
                .append("You can now log in to the Employee Portal using your email address and new password. ")
                .append("All your leave applications and account information remain unchanged.")
                .append("</p>");
        
        emailBody.append("<div style='margin: 40px 0 0 0; padding: 30px 0; border-top: 1px solid #e2e8f0;'>")
                .append("<p style='margin: 0 0 10px 0; font-size: 16px; color: #4a5568;'>Thank you for using the ").append(COMPANY_NAME).append(" Employee Leave Management System.</p>")
                .append("<p style='margin: 0; font-size: 16px; color: #4a5568;'>")
                .append("<strong>Best regards,</strong><br>")
                .append("IT Security Team<br>")
                .append("<span style='color: #28a745;'>").append(COMPANY_NAME).append("</span>")
                .append("</p>")
                .append("</div>");
        
        emailBody.append("</div>");
        
        emailBody.append("<div class='footer'>")
                .append("<p style='margin: 0 0 10px 0; font-size: 14px;'>🔒 This is an automated security confirmation from the Employee Leave Management System.</p>")
                .append("<p style='margin: 0 0 20px 0; font-size: 14px;'>Please do not reply to this email.</p>")
                .append("<p style='margin: 20px 0 0 0; font-size: 12px; color: #9ca3af;'>")
                .append("© ").append(new SimpleDateFormat("yyyy").format(new Date())).append(" ").append(COMPANY_NAME).append(". All rights reserved.<br>")
                .append("Email sent on: ").append(dateTimeFormat.format(new Date()))
                .append("</p>")
                .append("</div>");
        
        emailBody.append("</div></body></html>");
        
        return emailBody.toString();
    }
    
    /**
     * Validate email configuration
     */
    private static boolean isConfigurationValid() {
        boolean isValid = true;
        
        if (EMAIL_USERNAME.equals("your-email@gmail.com")) {
            System.err.println("❌ Please update EMAIL_USERNAME in ForgotPasswordEmailService.java");
            isValid = false;
        }
        
        if (EMAIL_PASSWORD.equals("your-app-password")) {
            System.err.println("❌ Please update EMAIL_PASSWORD in ForgotPasswordEmailService.java");
            isValid = false;
        }
        
        if (EMAIL_PASSWORD.length() != 16 && !EMAIL_PASSWORD.equals("your-app-password")) {
            System.err.println("⚠️ Warning: Gmail App Passwords are usually 16 characters long");
        }
        
        return isValid;
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
            message.setHeader("X-Mailer", "ELMS-ForgotPassword-System");
            message.setHeader("X-Priority", "1"); // High priority for security emails
            
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
     * Test email configuration for forgot password functionality
     */
    public static boolean testEmailConfiguration() {
        try {
            System.out.println("=== TESTING FORGOT PASSWORD EMAIL CONFIGURATION ===");
            
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
            
            // Test OTP email
            String testOTP = "123456";
            String testSubject = "[" + COMPANY_NAME + "] 🧪 Password Reset OTP Test - " + testOTP;
            String testBody = createOTPEmailBody("Test User", testOTP);
            
            boolean result = sendEmail(EMAIL_USERNAME, "System Administrator", testSubject, testBody);
            
            if (result) {
                System.out.println("✅ Forgot password email configuration test PASSED");
                System.out.println("📧 Check your email inbox for the test OTP message");
                
                // Also test password reset confirmation email
                String confirmationSubject = "[" + COMPANY_NAME + "] 🧪 Password Reset Confirmation Test";
                String confirmationBody = createPasswordResetConfirmationEmailBody("Test User");
                
                boolean confirmationResult = sendEmail(EMAIL_USERNAME, "System Administrator", confirmationSubject, confirmationBody);
                
                if (confirmationResult) {
                    System.out.println("✅ Password reset confirmation email test PASSED");
                    System.out.println("📧 Check your email inbox for the confirmation message");
                } else {
                    System.err.println("❌ Password reset confirmation email test FAILED");
                }
                
                return confirmationResult;
            } else {
                System.err.println("❌ Forgot password email configuration test FAILED");
            }
            
            return result;
            
        } catch (Exception e) {
            System.err.println("❌ Email configuration test failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
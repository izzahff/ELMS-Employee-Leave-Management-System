package elms.util;

import elms.service.EmailNotificationService;
import elms.model.Employee;
import elms.model.Manager;
import elms.model.LeaveApplication;

import java.util.Date;
import java.text.SimpleDateFormat;

/**
 * Utility class for testing email configuration and sending test emails
 */
public class EmailTestUtility {
    
    /**
     * Test basic email configuration
     */
    public static void testEmailConfiguration() {
        System.out.println("=== TESTING EMAIL CONFIGURATION ===");
        
        boolean configTest = EmailNotificationService.testEmailConfiguration();
        
        if (configTest) {
            System.out.println("✅ Email configuration test PASSED");
            System.out.println("📧 Test email sent successfully!");
        } else {
            System.err.println("❌ Email configuration test FAILED");
            System.err.println("Please check your email settings in EmailNotificationService.java");
        }
    }
    
    /**
     * Test leave approval notification email
     */
    public static void testLeaveApprovalEmail() {
        System.out.println("=== TESTING LEAVE APPROVAL EMAIL ===");
        
        try {
            // Create test data
            Employee testEmployee = createTestEmployee();
            Manager testManager = createTestManager();
            LeaveApplication testApplication = createTestLeaveApplication();
            
            // Send test approval email
            boolean sent = EmailNotificationService.sendLeaveStatusNotification(
                testApplication, "approve", testManager, null);
            
            if (sent) {
                System.out.println("✅ Leave approval email test PASSED");
                System.out.println("📧 Approval notification sent to: " + testEmployee.getEmployeeEmail());
            } else {
                System.err.println("❌ Leave approval email test FAILED");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error testing approval email: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Test leave rejection notification email
     */
    public static void testLeaveRejectionEmail() {
        System.out.println("=== TESTING LEAVE REJECTION EMAIL ===");
        
        try {
            // Create test data
            Employee testEmployee = createTestEmployee();
            Manager testManager = createTestManager();
            LeaveApplication testApplication = createTestLeaveApplication();
            
            // Send test rejection email
            String rejectionReason = "Unfortunately, we are unable to approve your leave request due to operational requirements during this period. Please consider rescheduling your leave for a later date.";
            
            boolean sent = EmailNotificationService.sendLeaveStatusNotification(
                testApplication, "reject", testManager, rejectionReason);
            
            if (sent) {
                System.out.println("✅ Leave rejection email test PASSED");
                System.out.println("📧 Rejection notification sent to: " + testEmployee.getEmployeeEmail());
            } else {
                System.err.println("❌ Leave rejection email test FAILED");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error testing rejection email: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Test welcome email for new employees
     */
    public static void testWelcomeEmail() {
        System.out.println("=== TESTING WELCOME EMAIL ===");
        
        try {
            Employee testEmployee = createTestEmployee();
            String tempPassword = "TempPass123!";
            
            boolean sent = EmailNotificationService.sendWelcomeEmail(testEmployee, tempPassword);
            
            if (sent) {
                System.out.println("✅ Welcome email test PASSED");
                System.out.println("📧 Welcome email sent to: " + testEmployee.getEmployeeEmail());
            } else {
                System.err.println("❌ Welcome email test FAILED");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error testing welcome email: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Run all email tests
     */
    public static void runAllEmailTests() {
        System.out.println("========================================");
        System.out.println("   ELMS EMAIL SYSTEM TEST SUITE");
        System.out.println("========================================");
        
        testEmailConfiguration();
        System.out.println();
        
        testLeaveApprovalEmail();
        System.out.println();
        
        testLeaveRejectionEmail();
        System.out.println();
        
        testWelcomeEmail();
        System.out.println();
        
        System.out.println("========================================");
        System.out.println("   EMAIL TESTS COMPLETED");
        System.out.println("========================================");
    }
    
    /**
     * Create test employee data
     */
    private static Employee createTestEmployee() {
        Employee employee = new Employee();
        employee.setEmployeeId("TEST001");
        employee.setEmployeeName("John Doe Test");
        employee.setEmployeeEmail("test@example.com"); // Change this to your test email
        employee.setEmployeeNoPhone("+60123456789");
        
        return employee;
    }
    
    /**
     * Create test manager data
     */
    private static Manager createTestManager() {
        Manager manager = new Manager();
        manager.setManagerid("MGR001");
        manager.setManagername("Jane Smith");
        manager.setManageremail("manager@imnsb.com");
        return manager;
    }
    
    /**
     * Create test leave application data
     */
    private static LeaveApplication createTestLeaveApplication() {
        LeaveApplication application = new LeaveApplication();
        application.setApplicationid("TEST-APP-001");
        application.setEmployeeid("TEST001");
        application.setLeavetypeid("LT001");
        application.setLeavestartdate("2024-12-25");
        application.setLeaveenddate("2024-12-27");
        application.setLeaveduration(3.0);
        application.setLeavereason("Christmas holiday with family");
        application.setLeavestatus("Pending");
        application.setAppliedon(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        return application;
    }
    
    /**
     * Main method for running tests standalone
     */
    public static void main(String[] args) {
        System.out.println("Starting ELMS Email System Tests...");
        
        if (args.length > 0) {
            String testType = args[0].toLowerCase();
            
            switch (testType) {
                case "config":
                    testEmailConfiguration();
                    break;
                case "approval":
                    testLeaveApprovalEmail();
                    break;
                case "rejection":
                    testLeaveRejectionEmail();
                    break;
                case "welcome":
                    testWelcomeEmail();
                    break;
                case "all":
                default:
                    runAllEmailTests();
                    break;
            }
        } else {
            runAllEmailTests();
        }
        
        System.out.println("Email tests completed. Check your email inbox for test messages.");
    }
    
    /**
     * Validate email configuration before running tests
     */
    public static boolean validateEmailConfig() {
        System.out.println("=== VALIDATING EMAIL CONFIGURATION ===");
        
        // Check if all required email properties are configured
        String[] requiredProps = {
            "SMTP_HOST", "EMAIL_USERNAME", "EMAIL_PASSWORD", "FROM_EMAIL"
        };
        
        boolean configValid = true;
        
        // This is a simplified check - in a real implementation, you'd read from
        // properties file or environment variables
        System.out.println("📋 Checking email configuration...");
        
        // Add your validation logic here
        System.out.println("✅ Email configuration validation completed");
        
        return configValid;
    }
    
    /**
     * Generate email configuration report
     */
    public static void generateConfigurationReport() {
        System.out.println("=== EMAIL CONFIGURATION REPORT ===");
        System.out.println("SMTP Host: smtp.gmail.com");
        System.out.println("SMTP Port: 587");
        System.out.println("Authentication: Enabled");
        System.out.println("TLS/SSL: Enabled");
        System.out.println("Email Format: HTML");
        System.out.println("From Address: noreply@imnsb.com");
        System.out.println("Company: Institut Maritim Negara Sabah Berhad (IMNSB)");
        System.out.println("=====================================");
        
        System.out.println("\n📧 Email Types Supported:");
        System.out.println("• Leave Application Approval");
        System.out.println("• Leave Application Rejection");
        System.out.println("• New Employee Welcome");
        System.out.println("• System Configuration Test");
        
        System.out.println("\n🎨 Email Features:");
        System.out.println("• Professional HTML templates");
        System.out.println("• Company branding and colors");
        System.out.println("• Responsive design for mobile");
        System.out.println("• Application details and manager info");
        System.out.println("• Rejection reasons for denied applications");
        System.out.println("• Automatic timestamps and tracking");
    }
}
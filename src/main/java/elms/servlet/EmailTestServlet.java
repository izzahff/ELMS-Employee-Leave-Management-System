package elms.servlet;

import elms.service.EmailNotificationService;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Test servlet to verify email configuration
 * Access: http://localhost:8080/YourProject/test-email
 */
@WebServlet("/test-email")
public class EmailTestServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        out.println("<!DOCTYPE html>");
        out.println("<html><head>");
        out.println("<title>ELMS Email Test</title>");
        out.println("<style>");
        out.println("body { font-family: Arial, sans-serif; margin: 40px; background: #f4f4f4; }");
        out.println(".container { max-width: 800px; margin: 0 auto; background: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }");
        out.println(".success { color: #28a745; background: #d4edda; padding: 15px; border-radius: 6px; border-left: 4px solid #28a745; }");
        out.println(".error { color: #dc3545; background: #f8d7da; padding: 15px; border-radius: 6px; border-left: 4px solid #dc3545; }");
        out.println(".info { color: #0c5460; background: #d1ecf1; padding: 15px; border-radius: 6px; border-left: 4px solid #17a2b8; }");
        out.println("button { background: #007bff; color: white; padding: 10px 20px; border: none; border-radius: 4px; cursor: pointer; font-size: 16px; }");
        out.println("button:hover { background: #0056b3; }");
        out.println("</style>");
        out.println("</head><body>");
        
        out.println("<div class='container'>");
        out.println("<h1>📧 ELMS Email Configuration Test</h1>");
        
        // Check if test button was clicked
        String testAction = request.getParameter("test");
        
        if ("send".equals(testAction)) {
            out.println("<h2>🧪 Testing Email Configuration...</h2>");
            
            try {
                boolean result = EmailNotificationService.testEmailConfiguration();
                
                if (result) {
                    out.println("<div class='success'>");
                    out.println("<h3>✅ Email Test Successful!</h3>");
                    out.println("<p>A test email has been sent successfully. Check your email inbox to confirm.</p>");
                    out.println("</div>");
                } else {
                    out.println("<div class='error'>");
                    out.println("<h3>❌ Email Test Failed!</h3>");
                    out.println("<p>There was an error sending the test email. Please check your configuration and server logs.</p>");
                    out.println("</div>");
                }
                
            } catch (Exception e) {
                out.println("<div class='error'>");
                out.println("<h3>❌ Email Test Failed!</h3>");
                out.println("<p>Exception occurred: " + e.getMessage() + "</p>");
                out.println("</div>");
            }
            
        } else {
            out.println("<div class='info'>");
            out.println("<h3>📋 Email Configuration Check</h3>");
            out.println("<p>This page allows you to test your ELMS email notification system.</p>");
            out.println("<p><strong>Before testing:</strong></p>");
            out.println("<ul>");
            out.println("<li>✅ Make sure you have updated your Gmail credentials in EmailNotificationService.java</li>");
            out.println("<li>✅ Ensure you're using an App Password (not your regular Gmail password)</li>");
            out.println("<li>✅ Check that the JAR files (javax.mail and activation) are in WEB-INF/lib</li>");
            out.println("</ul>");
            out.println("</div>");
        }
        
        out.println("<hr style='margin: 30px 0;'>");
        
        // Always show the test button
        out.println("<h3>🚀 Ready to Test?</h3>");
        out.println("<p>Click the button below to send a test email:</p>");
        out.println("<form method='GET'>");
        out.println("<input type='hidden' name='test' value='send'>");
        out.println("<button type='submit'>📧 Send Test Email</button>");
        out.println("</form>");
        
        out.println("<hr style='margin: 30px 0;'>");
        
        out.println("<h3>🔧 Configuration Checklist:</h3>");
        out.println("<div style='background: #f8f9fa; padding: 20px; border-radius: 6px;'>");
        out.println("<h4>1. Gmail Setup:</h4>");
        out.println("<ul>");
        out.println("<li>Gmail account with 2-Factor Authentication enabled</li>");
        out.println("<li>App Password generated (16 characters)</li>");
        out.println("</ul>");
        
        out.println("<h4>2. Project Setup:</h4>");
        out.println("<ul>");
        out.println("<li>javax.mail-1.6.2.jar in WEB-INF/lib</li>");
        out.println("<li>activation-1.1.1.jar in WEB-INF/lib</li>");
        out.println("<li>EmailNotificationService.java with your credentials</li>");
        out.println("</ul>");
        
        out.println("<h4>3. Next Steps:</h4>");
        out.println("<ul>");
        out.println("<li>Test manager approval/rejection flow</li>");
        out.println("<li>Check employee receives email notifications</li>");
        out.println("<li>Verify visual alerts appear in leave history</li>");
        out.println("</ul>");
        out.println("</div>");
        
        out.println("<p style='margin-top: 30px; color: #666; font-size: 14px;'>");
        out.println("<strong>Note:</strong> After successful testing, you can remove this test servlet for security.");
        out.println("</p>");
        
        out.println("</div>");
        out.println("</body></html>");
    }
}
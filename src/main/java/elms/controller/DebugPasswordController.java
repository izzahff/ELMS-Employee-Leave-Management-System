package elms.controller;

import java.io.IOException;
import java.sql.SQLException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;


/**
 * DEBUG SERVLET - Create this temporarily to test if the controller is being called
 * Map it to /debugPasswordChange and test it first
 */
@WebServlet("/debugPasswordChange")
public class DebugPasswordController extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        System.out.println("=== DEBUG PASSWORD CONTROLLER CALLED ===");
        
        // Log all parameters
        System.out.println("Parameters received:");
        request.getParameterMap().forEach((key, values) -> {
            System.out.println("  " + key + " = " + (key.toLowerCase().contains("password") ? "[HIDDEN]" : String.join(",", values)));
        });
        
        // Check session
        HttpSession session = request.getSession(false);
        System.out.println("Session exists: " + (session != null));
        
        if (session != null) {
            System.out.println("Session attributes:");
            session.getAttributeNames().asIterator().forEachRemaining(name -> {
                System.out.println("  " + name + " = " + session.getAttribute(name));
            });
        }
        
        // Send simple JSON response
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String jsonResponse = "{\"success\": true, \"message\": \"Debug controller called successfully! Check console logs.\"}";
        response.getWriter().write(jsonResponse);
        
        System.out.println("Debug response sent: " + jsonResponse);
        System.out.println("=== DEBUG PASSWORD CONTROLLER END ===");
    }
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        doPost(request, response);
    }
}
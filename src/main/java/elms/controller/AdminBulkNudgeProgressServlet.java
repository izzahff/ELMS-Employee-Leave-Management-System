package elms.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.CompletableFuture;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import elms.service.NudgeNotificationService;

/**
 * Servlet for handling bulk nudge with Server-Sent Events (SSE) progress
 */
@WebServlet("/admin-bulk-nudge-progress")
public class AdminBulkNudgeProgressServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("adminId") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String adminName = (String) session.getAttribute("adminName");
        if (adminName == null || adminName.trim().isEmpty()) {
            adminName = "Admin User";
        }

        // Set up Server-Sent Events
        response.setContentType("text/event-stream");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-cache");
        response.setHeader("Connection", "keep-alive");

        PrintWriter writer = response.getWriter();
        
        final String finalAdminName = adminName;

        try {
            // ✅ Run bulk nudge with progress updates
            CompletableFuture.runAsync(() -> {
                try {
                    NudgeNotificationService.BulkNudgeResult result = 
                        NudgeNotificationService.sendBulkNudgeNotifications(finalAdminName, 
                            new NudgeNotificationService.BulkNudgeProgressCallback() {
                                @Override
                                public void onProgress(int current, int total, String message) {
                                    try {
                                        // Send progress update as SSE
                                        writer.write("data: {");
                                        writer.write("\"current\": " + current + ",");
                                        writer.write("\"total\": " + total + ",");
                                        writer.write("\"message\": \"" + escapeJson(message) + "\"");
                                        writer.write("}\n\n");
                                        writer.flush();
                                    } catch (Exception e) {
                                        System.err.println("Error sending progress: " + e.getMessage());
                                    }
                                }
                            });
                    
                    // ✅ Send final result
                    writer.write("data: {");
                    writer.write("\"done\": true,");
                    writer.write("\"success\": " + result.isActualSuccess() + ",");
                    writer.write("\"message\": \"" + escapeJson(result.getMessage()) + "\"");
                    writer.write("}\n\n");
                    writer.flush();
                    
                } catch (Exception e) {
                    writer.write("data: {");
                    writer.write("\"done\": true,");
                    writer.write("\"success\": false,");
                    writer.write("\"message\": \"Error: " + escapeJson(e.getMessage()) + "\"");
                    writer.write("}\n\n");
                    writer.flush();
                }
            }).join(); // Wait for completion
            
        } catch (Exception e) {
            System.err.println("Error in bulk nudge progress: " + e.getMessage());
        } finally {
            writer.close();
        }
    }
    
    private String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
    }
}

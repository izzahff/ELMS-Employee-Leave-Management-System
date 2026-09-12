package elms.servlet;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Debug servlet to help locate upload directory and files
 */
@WebServlet("/debug-uploads")
public class DebugUploadServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        
        out.println("<html><head><title>Upload Directory Debug</title></head><body>");
        out.println("<h1>Upload Directory Debug Information</h1>");
        
        // Check various possible paths
        out.println("<h2>Path Information:</h2>");
        out.println("<ul>");
        
        // Web application path
        String webappPath = request.getServletContext().getRealPath("/");
        out.println("<li><strong>Web App Root:</strong> " + webappPath + "</li>");
        
        String webappUploads = request.getServletContext().getRealPath("/uploads/attachments/");
        out.println("<li><strong>Web App Uploads:</strong> " + webappUploads + "</li>");
        if (webappUploads != null) {
            File webappDir = new File(webappUploads);
            out.println("<li><strong>Web App Uploads Exists:</strong> " + webappDir.exists() + "</li>");
        }
        
        // System properties
        out.println("<li><strong>User Home:</strong> " + System.getProperty("user.home") + "</li>");
        out.println("<li><strong>Java Temp Dir:</strong> " + System.getProperty("java.io.tmpdir") + "</li>");
        out.println("<li><strong>Current Working Dir:</strong> " + System.getProperty("user.dir") + "</li>");
        
        // Common upload paths
        String[] testPaths = {
            "C:/uploads/attachments/",
            "/var/uploads/attachments/",
            webappPath + "uploads/attachments/",
            System.getProperty("user.home") + "/uploads/attachments/",
            "/tmp/uploads/attachments/"
        };
        
        out.println("</ul>");
        
        out.println("<h2>Testing Common Upload Paths:</h2>");
        out.println("<table border='1' cellpadding='5'>");
        out.println("<tr><th>Path</th><th>Exists</th><th>Is Directory</th><th>Can Read</th><th>Files Count</th></tr>");
        
        for (String path : testPaths) {
            if (path != null) {
                File dir = new File(path);
                boolean exists = dir.exists();
                boolean isDir = dir.isDirectory();
                boolean canRead = dir.canRead();
                int fileCount = 0;
                
                if (exists && isDir && canRead) {
                    File[] files = dir.listFiles();
                    fileCount = files != null ? files.length : 0;
                }
                
                out.println("<tr>");
                out.println("<td>" + path + "</td>");
                out.println("<td>" + exists + "</td>");
                out.println("<td>" + isDir + "</td>");
                out.println("<td>" + canRead + "</td>");
                out.println("<td>" + fileCount + "</td>");
                out.println("</tr>");
                
                // List files if directory exists
                if (exists && isDir && canRead && fileCount > 0) {
                    out.println("<tr><td colspan='5'>");
                    out.println("<strong>Files in " + path + ":</strong><br>");
                    File[] files = dir.listFiles();
                    if (files != null) {
                        for (File file : files) {
                            out.println("&nbsp;&nbsp;- " + file.getName() + " (" + file.length() + " bytes)<br>");
                        }
                    }
                    out.println("</td></tr>");
                }
            }
        }
        
        out.println("</table>");
        
        // Check for specific file if provided
        String testFile = request.getParameter("file");
        if (testFile != null && !testFile.trim().isEmpty()) {
            out.println("<h2>Testing Specific File: " + testFile + "</h2>");
            out.println("<table border='1' cellpadding='5'>");
            out.println("<tr><th>Full Path</th><th>Exists</th><th>Size</th><th>Can Read</th></tr>");
            
            for (String path : testPaths) {
                if (path != null) {
                    File file = new File(path + testFile);
                    out.println("<tr>");
                    out.println("<td>" + file.getAbsolutePath() + "</td>");
                    out.println("<td>" + file.exists() + "</td>");
                    out.println("<td>" + (file.exists() ? file.length() + " bytes" : "N/A") + "</td>");
                    out.println("<td>" + file.canRead() + "</td>");
                    out.println("</tr>");
                }
            }
            out.println("</table>");
            
            // Try to find the file using search utility
            try {
                out.println("<h3>Search Results:</h3>");
                // This would use the FileFinderUtility if you implement it
                out.println("<p>Searching for file: " + testFile + "</p>");
                
                // Manual search in common locations
                String[] manualSearchPaths = {
                    "C:/",
                    "C:/temp/",
                    "C:/uploads/",
                    "/var/",
                    "/tmp/",
                    System.getProperty("user.home"),
                    System.getProperty("java.io.tmpdir")
                };
                
                out.println("<p>Manual search results:</p><ul>");
                for (String searchRoot : manualSearchPaths) {
                    File rootDir = new File(searchRoot);
                    if (rootDir.exists()) {
                        searchForFile(rootDir, testFile, out, 0, 3); // Max 3 levels deep
                    }
                }
                out.println("</ul>");
                
            } catch (Exception e) {
                out.println("<p>Error during search: " + e.getMessage() + "</p>");
            }
        }
        
        out.println("<h2>How to Use:</h2>");
        out.println("<p>Add <code>?file=filename.jpg</code> to test a specific file.</p>");
        out.println("<p>Example: <a href='?file=emp_EMP037_20250625_044938_britney_spears_.jpg'>Test your file</a></p>");
        
        out.println("</body></html>");
    }
    
    private void searchForFile(File directory, String fileName, PrintWriter out, int currentDepth, int maxDepth) {
        if (currentDepth > maxDepth || !directory.canRead()) {
            return;
        }
        
        try {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().equals(fileName)) {
                        out.println("<li><strong>FOUND:</strong> " + file.getAbsolutePath() + " (" + file.length() + " bytes)</li>");
                    } else if (file.isDirectory() && currentDepth < maxDepth) {
                        searchForFile(file, fileName, out, currentDepth + 1, maxDepth);
                    }
                }
            }
        } catch (SecurityException e) {
            // Skip directories we can't access
        }
    }
}
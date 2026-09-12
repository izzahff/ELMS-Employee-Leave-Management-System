package elms.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import elms.model.Employee;
import elms.DAO.LeaveApplicationDAO;
import elms.model.LeaveApplication;

/**
 * Servlet to handle file downloads for leave application attachments
 * FIXED: Now searches through date-organized directory structure
 */
@WebServlet("/file-download")
public class FileDownloadServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private LeaveApplicationDAO leaveApplicationDAO;
    
    @Override
    public void init() throws ServletException {
        super.init();
        try {
            leaveApplicationDAO = new LeaveApplicationDAO();
        } catch (Exception e) {
            throw new ServletException("Failed to initialize LeaveApplicationDAO", e);
        }
    }
    
    // Get the base upload directory
    private String getUploadDirectory(HttpServletRequest request) {
        String webappPath = request.getServletContext().getRealPath("/uploads/attachments/");
        if (webappPath != null) {
            File webappDir = new File(webappPath);
            if (webappDir.exists()) {
                System.out.println("✅ Found webapp upload directory: " + webappPath);
                return webappPath;
            } else {
                if (webappDir.mkdirs()) {
                    System.out.println("✅ Created webapp upload directory: " + webappPath);
                    return webappPath;
                }
            }
        }
        
        // Fallback options
        String[] fallbackPaths = {
            request.getServletContext().getRealPath("/") + "uploads" + File.separator + "attachments" + File.separator,
            "C:/uploads/attachments/",
            "/var/uploads/attachments/",
            System.getProperty("user.home") + "/uploads/attachments/",
            "/tmp/uploads/attachments/"
        };
        
        for (String path : fallbackPaths) {
            if (path != null) {
                File dir = new File(path);
                if (dir.exists()) {
                    System.out.println("✅ Found fallback upload directory: " + path);
                    return path;
                }
            }
        }
        
        System.err.println("❌ No upload directory found! Using webapp path: " + webappPath);
        return webappPath;
    }
    
    /**
     * FIXED: Search for file in date-organized directory structure
     */
    private File findAttachmentFile(String baseDir, String fileName) {
        System.out.println("🔍 Searching for file: " + fileName + " in base directory: " + baseDir);
        
        // First try direct path (legacy files)
        File directFile = new File(baseDir + fileName);
        if (directFile.exists() && directFile.isFile()) {
            System.out.println("✅ Found file at direct path: " + directFile.getAbsolutePath());
            return directFile;
        }
        
        // Search through year/month directories
        File baseDirectory = new File(baseDir);
        if (!baseDirectory.exists()) {
            System.out.println("❌ Base directory doesn't exist: " + baseDir);
            return null;
        }
        
        // Get all year directories
        File[] yearDirs = baseDirectory.listFiles(File::isDirectory);
        if (yearDirs != null) {
            for (File yearDir : yearDirs) {
                System.out.println("🔍 Searching year directory: " + yearDir.getName());
                
                // Get all month directories within year
                File[] monthDirs = yearDir.listFiles(File::isDirectory);
                if (monthDirs != null) {
                    for (File monthDir : monthDirs) {
                        System.out.println("🔍 Searching month directory: " + monthDir.getName());
                        
                        // Look for the file in this month directory
                        File possibleFile = new File(monthDir, fileName);
                        if (possibleFile.exists() && possibleFile.isFile()) {
                            System.out.println("✅ Found file at: " + possibleFile.getAbsolutePath());
                            return possibleFile;
                        }
                    }
                }
            }
        }
        
        System.out.println("❌ File not found anywhere: " + fileName);
        return null;
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        System.out.println("=== FILE DOWNLOAD REQUEST ===");
        
        // Check if user is logged in
        HttpSession session = request.getSession(false);
        if (session == null) {
            System.out.println("❌ No session found, redirecting to login");
            response.sendRedirect(request.getContextPath() + "/Employee/EmployeeLogin.jsp");
            return;
        }
        
        Employee employee = (Employee) session.getAttribute("loggedInEmployee");
        if (employee == null) {
            System.out.println("❌ No logged in employee found, redirecting to login");
            response.sendRedirect(request.getContextPath() + "/Employee/EmployeeLogin.jsp");
            return;
        }
        
        String fileName = request.getParameter("file");
        String applicationId = request.getParameter("applicationId");
        
        if (fileName == null || fileName.trim().isEmpty()) {
            System.out.println("❌ No file parameter provided");
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "File parameter is required");
            return;
        }
        
        try {
            // Decode the filename in case it was URL encoded
            fileName = URLDecoder.decode(fileName, "UTF-8");
            System.out.println("📁 Requested file: " + fileName);
            System.out.println("📁 Application ID: " + applicationId);
            
            // Security check - prevent directory traversal attacks
            if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
                System.out.println("❌ Invalid file path detected: " + fileName);
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid file path");
                return;
            }
            
            // Additional security: Verify the file belongs to an application the user can access
            if (applicationId != null && !applicationId.trim().isEmpty()) {
                try {
                    LeaveApplication app = leaveApplicationDAO.getLeaveApplicationById(applicationId);
                    if (app == null || !employee.getEmployeeId().equals(app.getEmployeeid())) {
                        System.out.println("❌ User doesn't have access to this application");
                        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
                        return;
                    }
                } catch (Exception e) {
                    System.err.println("❌ Error verifying application access: " + e.getMessage());
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error verifying access");
                    return;
                }
            }
            
            // Get the upload directory
            String uploadDir = getUploadDirectory(request);
            System.out.println("📁 Upload directory: " + uploadDir);
            
            // FIXED: Search for file in date-organized structure
            File file = findAttachmentFile(uploadDir, fileName);
            
            if (file == null || !file.exists() || !file.isFile()) {
                System.out.println("❌ File not found: " + fileName);
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "File not found: " + fileName);
                return;
            }
            
            // Additional security check - ensure file is within allowed directory
            String canonicalFilePath = file.getCanonicalPath();
            String canonicalBasePath = new File(uploadDir).getCanonicalPath();
            
            if (!canonicalFilePath.startsWith(canonicalBasePath)) {
                System.out.println("❌ File access denied - outside allowed directory");
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
                return;
            }
            
            // Determine content type based on file extension
            String contentType = getContentType(fileName);
            response.setContentType(contentType);
            
            // Set content length
            response.setContentLengthLong(file.length());
            
            // FIXED: Set content disposition to 'inline' to view in browser
            response.setHeader("Content-Disposition", "inline; filename=\"" + fileName + "\"");
            
            // Add cache headers for better performance
            response.setHeader("Cache-Control", "public, max-age=31536000"); // Cache for 1 year
            response.setDateHeader("Expires", System.currentTimeMillis() + (365L * 24 * 60 * 60 * 1000)); // 1 year
            
            System.out.println("📤 Serving file: " + fileName + " (" + file.length() + " bytes, " + contentType + ")");
            
            // Stream the file to the response
            try (FileInputStream fileInputStream = new FileInputStream(file);
                 OutputStream outputStream = response.getOutputStream()) {
                
                byte[] buffer = new byte[8192];
                int bytesRead;
                
                while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                
                outputStream.flush();
            }
            
            System.out.println("✅ File served successfully: " + fileName);
            
        } catch (Exception e) {
            System.err.println("❌ Error serving file: " + e.getMessage());
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error serving file: " + e.getMessage());
        }
    }
    
    /**
     * Determine content type based on file extension
     */
    private String getContentType(String fileName) {
        String extension = "";
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot > 0) {
            extension = fileName.substring(lastDot + 1).toLowerCase();
        }
        
        switch (extension) {
            case "pdf":
                return "application/pdf";
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "gif":
                return "image/gif";
            case "bmp":
                return "image/bmp";
            case "doc":
                return "application/msword";
            case "docx":
                return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls":
                return "application/vnd.ms-excel";
            case "xlsx":
                return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "txt":
                return "text/plain";
            case "csv":
                return "text/csv";
            default:
                return "application/octet-stream";
        }
    }
}
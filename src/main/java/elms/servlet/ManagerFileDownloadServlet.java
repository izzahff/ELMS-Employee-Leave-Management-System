package elms.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import elms.model.Manager;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Manager servlet for downloading leave application attachments
 */
@WebServlet("/manager-file-download")
public class ManagerFileDownloadServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    // Define allowed file extensions for security
    private static final String[] ALLOWED_EXTENSIONS = {
        ".pdf", ".doc", ".docx", ".jpg", ".jpeg", ".png", ".gif", ".txt", ".zip", ".rar"
    };
    
    // Get the base upload directory (same as employee servlet)
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
     * Search for file in date-organized directory structure (same as employee servlet)
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
    
    /**
     * Determine content type based on file extension (same as employee servlet)
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
    };
    
    // Maximum file size (50MB)
    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024;
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        System.out.println("=== MANAGER FILE DOWNLOAD REQUEST ===");
        
        // Check if manager is logged in
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendRedirect(request.getContextPath() + "/Manager/ManagerLogin.jsp");
            return;
        }
        
        Manager manager = (Manager) session.getAttribute("manager");
        if (manager == null) {
            response.sendRedirect(request.getContextPath() + "/Manager/ManagerLogin.jsp");
            return;
        }
        
        String managerId = manager.getManagerid();
        String managerName = manager.getManagername();
        System.out.println("Manager ID: " + managerId + " (" + managerName + ")");
        
        String fileName = request.getParameter("file");
        String applicationId = request.getParameter("applicationId");
        
        if (fileName == null || fileName.trim().isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "File parameter is required");
            return;
        }
        
        if (applicationId == null || applicationId.trim().isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Application ID is required");
            return;
        }
        
        System.out.println("Requested file: " + fileName);
        System.out.println("Application ID: " + applicationId);
        
        try {
            // Security check: Validate file name
            if (!isValidFileName(fileName)) {
                System.out.println("❌ Invalid file name: " + fileName);
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid file name");
                return;
            }
            
            // Get the upload directory using the same approach as employee servlet
            String uploadDir = getUploadDirectory(request);
            System.out.println("📁 Upload directory: " + uploadDir);
            
            // Search for file in date-organized structure (same as employee servlet)
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
            
            // Check file size
            if (file.length() > MAX_FILE_SIZE) {
                System.out.println("❌ File too large: " + file.length() + " bytes");
                response.sendError(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE, "File too large");
                return;
            }
            
            // Determine content type
            String contentType = getContentType(fileName);
            
            // Set response headers
            response.setContentType(contentType);
            response.setContentLengthLong(file.length());
            
            // Set filename for download - use inline to view in browser
            response.setHeader("Content-Disposition", "inline; filename=\"" + fileName + "\"");
            response.setHeader("Cache-Control", "public, max-age=3600"); // Cache for 1 hour
            
            // Log manager access
            System.out.println("✅ Manager " + managerId + " (" + managerName + ") accessing file: " + fileName + " for application: " + applicationId);
            System.out.println("📤 Serving file: " + fileName + " (" + file.length() + " bytes, " + contentType + ")");
            
            // Stream file to response
            try (FileInputStream fileInputStream = new FileInputStream(file);
                 OutputStream outputStream = response.getOutputStream()) {
                
                byte[] buffer = new byte[8192];
                int bytesRead;
                long totalBytesRead = 0;
                
                while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;
                }
                
                outputStream.flush();
                System.out.println("✅ File download completed. Bytes served: " + totalBytesRead);
            }
            
        } catch (SecurityException e) {
            System.err.println("❌ Security error: " + e.getMessage());
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied");
        } catch (IOException e) {
            System.err.println("❌ IO error while serving file: " + e.getMessage());
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error serving file");
        } catch (Exception e) {
            System.err.println("❌ Unexpected error: " + e.getMessage());
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Server error");
        }
        
        System.out.println("=== MANAGER FILE DOWNLOAD COMPLETE ===");
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Redirect POST to GET
        doGet(request, response);
    }
    
    /**
     * Validates file name for security
     */
    private boolean isValidFileName(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            return false;
        }
        
        // Allow underscores and letters/numbers for your naming convention
        // emp_EMP037_20250625_152203_TUTORIAL_LECTURE_10.pdf
        if (!fileName.matches("^[a-zA-Z0-9._-]+$")) {
            System.out.println("❌ Invalid characters in filename: " + fileName);
            return false;
        }
        
        // Check for path traversal attempts (shouldn't have directory separators)
        if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
            System.out.println("❌ Path traversal attempt in filename: " + fileName);
            return false;
        }
        
        // Check file extension
        String lowerFileName = fileName.toLowerCase();
        boolean hasValidExtension = false;
        for (String ext : ALLOWED_EXTENSIONS) {
            if (lowerFileName.endsWith(ext)) {
                hasValidExtension = true;
                break;
            }
        }
        
        if (!hasValidExtension) {
            System.out.println("❌ File extension not allowed: " + fileName);
            return false;
        }
        
        // Check file name length
        if (fileName.length() > 255) {
            System.out.println("❌ Filename too long: " + fileName.length() + " characters");
            return false;
        }
        
        System.out.println("✅ Filename validation passed: " + fileName);
        return true;
    }
}
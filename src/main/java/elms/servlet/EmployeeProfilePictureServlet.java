package elms.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/profile-pictures/*")
public class EmployeeProfilePictureServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        
        // Remove leading slash
        String fileName = pathInfo.substring(1);
        
        // Get the upload directory path
        String uploadPath = getServletContext().getRealPath("") + File.separator + "profile-pictures";
        File imageFile = new File(uploadPath, fileName);
        
        if (!imageFile.exists() || !imageFile.isFile()) {
            // If image doesn't exist, serve default avatar
            String defaultAvatarPath = getServletContext().getRealPath("") + File.separator + "img" + File.separator + "defaultprofilepicture.jpg";
            imageFile = new File(defaultAvatarPath);
            
            if (!imageFile.exists()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
        }
        
        // Set content type based on file extension
        String contentType = getContentType(fileName);
        response.setContentType(contentType);
        
        // Set cache headers
        response.setHeader("Cache-Control", "public, max-age=31536000"); // 1 year
        response.setDateHeader("Expires", System.currentTimeMillis() + 31536000000L);
        
        // Stream the file
        try (FileInputStream fis = new FileInputStream(imageFile);
             OutputStream os = response.getOutputStream()) {
            
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
        }
    }
    
    private String getContentType(String fileName) {
        String extension = getFileExtension(fileName).toLowerCase();
        switch (extension) {
            case ".jpg":
            case ".jpeg":
                return "image/jpeg";
            case ".png":
                return "image/png";
            case ".gif":
                return "image/gif";
            default:
                return "application/octet-stream";
        }
    }
    
    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return fileName.substring(lastDotIndex);
    }
}
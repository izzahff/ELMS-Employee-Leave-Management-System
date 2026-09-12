package elms.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.regex.Pattern;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import elms.DAO.ManagerDAO;
import elms.model.Manager;

@WebServlet({"/update-director-account", "/update-manager-account"})
@MultipartConfig(
    maxFileSize = 1024 * 1024 * 5,      // 5MB max file size
    maxRequestSize = 1024 * 1024 * 10,  // 10MB max request size
    fileSizeThreshold = 1024 * 1024 * 1 // 1MB threshold
)
public class ManagerUpdateAccountController extends HttpServlet {

    private static final String UPLOAD_DIR = "profile-pictures";
    private static final String[] ALLOWED_EXTENSIONS = {".jpg", ".jpeg", ".png", ".gif"};
    
    // Malaysian phone number pattern: +60123456789 (compact format)
    private static final Pattern MALAYSIA_PHONE_PATTERN = Pattern.compile("^\\+60\\d{9}$");
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        System.out.println("=== UPDATE MANAGER ACCOUNT CONTROLLER START ===");
        long startTime = System.currentTimeMillis();
        
        HttpSession session = request.getSession();
        Manager currentManager = (Manager) session.getAttribute("manager");
        String userType = (String) session.getAttribute("userType");
        
        // Check if manager is logged in
        if (currentManager == null) {
            System.out.println("No manager in session, redirecting to login");
            response.sendRedirect("/ELMS_3.0/Manager/ManagerLogin.jsp");
            return;
        }
        
        System.out.println("BEFORE UPDATE - Current manager in session:");
        System.out.println("- ID: " + currentManager.getManagerid());
        System.out.println("- Name: " + currentManager.getManagername());
        System.out.println("- Position: " + currentManager.getManagerposition());
        System.out.println("- Current Profile Picture: " + currentManager.getProfilePicturePath());
        
        try {
            // Get form parameters
            String managerId = request.getParameter("managerid");
            String fullName = request.getParameter("managername");
            String email = request.getParameter("manageremail");
            String phone = request.getParameter("managernophone");
            String position = request.getParameter("managerposition");
            
            // Check if profile picture should be removed
            String removeProfilePicture = request.getParameter("remove_profile_picture");
            boolean shouldRemoveProfilePicture = "true".equals(removeProfilePicture);
            
            System.out.println("Processing update for manager: " + managerId);
            System.out.println("Form data:");
            System.out.println("- Name: " + fullName);
            System.out.println("- Email: " + email);
            System.out.println("- Phone: " + phone);
            System.out.println("- Position: " + position);
            System.out.println("- Remove Profile Picture: " + shouldRemoveProfilePicture);
            
            // Quick validation
            if (fullName == null || fullName.trim().isEmpty()) {
                setErrorAndForward(request, response, "Full name is required");
                return;
            }
            
            if (email == null || email.trim().isEmpty()) {
                setErrorAndForward(request, response, "Email is required");
                return;
            }
            
            // Validate email format
            if (!isValidEmail(email.trim())) {
                setErrorAndForward(request, response, "Please enter a valid email address");
                return;
            }
            
            // Validate phone number format (more flexible - accept various formats)
            if (phone != null && !phone.trim().isEmpty()) {
                String cleanedPhone = formatPhoneNumber(phone.trim());
                if (cleanedPhone == null) {
                    setErrorAndForward(request, response, "Please enter a valid Malaysian mobile number (e.g., 0123456789 or 123456789)");
                    return;
                }
                // Use the cleaned/formatted phone number
                phone = cleanedPhone;
                System.out.println("DEBUG: Phone formatted to: " + phone);
            }
            
            // Validate manager name format
            if (!isValidName(fullName.trim())) {
                setErrorAndForward(request, response, "Manager name can only contain letters, spaces, hyphens, and apostrophes");
                return;
            }
            
            // Handle profile picture removal FIRST (before upload handling)
            String finalProfilePicturePath = currentManager.getProfilePicturePath();
            
            if (shouldRemoveProfilePicture) {
                System.out.println("=== PROFILE PICTURE REMOVAL REQUESTED ===");
                
                // Delete the current profile picture file if it exists and is not default
                if (currentManager.getProfilePicturePath() != null && 
                    !currentManager.getProfilePicturePath().isEmpty() && 
                    !currentManager.getProfilePicturePath().equals("/defaultprofilepicture.jpg") && 
                    !currentManager.getProfilePicturePath().contains("defaultprofilepicture.jpg")) {
                    
                    try {
                        String currentFilePath = request.getServletContext().getRealPath("") + 
                                               File.separator + currentManager.getProfilePicturePath();
                        File currentFile = new File(currentFilePath);
                        
                        if (currentFile.exists() && currentFile.delete()) {
                            System.out.println("✅ Successfully deleted current profile picture: " + currentFilePath);
                        } else {
                            System.out.println("⚠️ Could not delete current profile picture file: " + currentFilePath);
                        }
                    } catch (Exception e) {
                        System.out.println("⚠️ Error deleting current profile picture: " + e.getMessage());
                        // Continue anyway, this is not critical
                    }
                }
                
                // Set profile picture path to null (will use default avatar)
                finalProfilePicturePath = null;
                System.out.println("✅ Profile picture will be removed (set to null)");
            }
            
            // Handle profile picture upload (only if not removing)
            String newProfilePicturePath = null;
            if (!shouldRemoveProfilePicture) {
                Part profilePicturePart = request.getPart("profile_picture_path");
                
                if (profilePicturePart != null && profilePicturePart.getSize() > 0) {
                    System.out.println("Processing profile picture upload...");
                    newProfilePicturePath = handleProfilePictureUpload(profilePicturePart, managerId, request, currentManager.getProfilePicturePath());
                    if (newProfilePicturePath != null) {
                        System.out.println("✅ Profile picture uploaded successfully: " + newProfilePicturePath);
                        finalProfilePicturePath = newProfilePicturePath;
                    } else {
                        System.out.println("❌ Profile picture upload failed");
                        setErrorAndForward(request, response, "Failed to upload profile picture. Please try again.");
                        return;
                    }
                } else {
                    System.out.println("No new profile picture uploaded, keeping existing: " + currentManager.getProfilePicturePath());
                    // Only keep existing if we're not removing it
                    if (!shouldRemoveProfilePicture) {
                        finalProfilePicturePath = currentManager.getProfilePicturePath();
                    }
                }
            }
            
            // Create updated manager object with all current data
            Manager updatedManager = new Manager();
            updatedManager.setManagerid(managerId);
            updatedManager.setManagername(fullName.trim());
            updatedManager.setManageremail(email.trim());
            updatedManager.setManagernophone(phone != null ? phone.trim() : "");
            updatedManager.setManagerposition(currentManager.getManagerposition()); // Position cannot be changed
            updatedManager.setManagerpassword(currentManager.getManagerpassword()); // Keep existing password
            
            // Set the final profile picture path (could be new upload, existing, or null for removal)
            updatedManager.setProfilePicturePath(finalProfilePicturePath);
            
            System.out.println("BEFORE DATABASE UPDATE - Manager object to save:");
            System.out.println("- ID: " + updatedManager.getManagerid());
            System.out.println("- Name: " + updatedManager.getManagername());
            System.out.println("- Email: " + updatedManager.getManageremail());
            System.out.println("- Phone: " + updatedManager.getManagernophone());
            System.out.println("- Position: " + updatedManager.getManagerposition());
            System.out.println("- Profile Picture: " + updatedManager.getProfilePicturePath());
            
            // Check if email already exists for a different manager
            ManagerDAO managerDAO = new ManagerDAO();
            if (managerDAO.managerEmailExistsForDifferentId(email.trim(), managerId)) {
                System.out.println("❌ Email already exists for different manager");
                setErrorAndForward(request, response, "Email address is already in use by another manager.");
                return;
            }
            
            // Update the database
            boolean updateSuccess = managerDAO.updateManagerWithoutPassword(updatedManager);
            
            if (updateSuccess) {
                System.out.println("✅ Database update successful");
                
                // CRITICAL: Update the session manager object immediately
                Manager updatedSessionManager = new Manager();
                updatedSessionManager.setManagerid(managerId);
                updatedSessionManager.setManagername(fullName.trim());
                updatedSessionManager.setManageremail(email.trim());
                updatedSessionManager.setManagernophone(phone != null ? phone.trim() : "");
                updatedSessionManager.setManagerposition(currentManager.getManagerposition());
                updatedSessionManager.setManagerpassword(currentManager.getManagerpassword());
                updatedSessionManager.setProfilePicturePath(finalProfilePicturePath);
                
                // Update session
                session.setAttribute("manager", updatedSessionManager);
                
                System.out.println("✅ Session updated with:");
                System.out.println("- ID: " + updatedSessionManager.getManagerid());
                System.out.println("- Name: " + updatedSessionManager.getManagername());
                System.out.println("- Position: " + updatedSessionManager.getManagerposition());
                System.out.println("- Profile Picture: " + updatedSessionManager.getProfilePicturePath());
                
                 // Set success message in SESSION for popup (not request)
                if (shouldRemoveProfilePicture) {
                    session.setAttribute("accountUpdateSuccess", "Account information updated successfully! Profile picture has been removed.");
                } else if (newProfilePicturePath != null) {
                    session.setAttribute("accountUpdateSuccess", "Account information and profile picture updated successfully!");
                } else {
                    session.setAttribute("accountUpdateSuccess", "Account information updated successfully!");
                }
                
                long endTime = System.currentTimeMillis();
                System.out.println("✅ Update completed in " + (endTime - startTime) + "ms");
                
             // REDIRECT to settings page to show popup
                String redirectPath = "/ELMS_3.0/Manager/DirectorSettings.jsp"; // Default
                if (userType != null && "executive_director".equals(userType)) {
                    redirectPath = "/ELMS_3.0/Manager/DirectorSettings.jsp";
                } else {
                    redirectPath = "/ELMS_3.0/Manager/ManagerSettings.jsp";
                }
                
                response.sendRedirect(redirectPath);
                return;
                
            } else {
                System.out.println("❌ Database update failed");
                request.setAttribute("message", "Failed to update account. Please try again.");
                request.setAttribute("messageType", "error");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Exception: " + e.getMessage());
            e.printStackTrace();
            
            String errorMessage = "An unexpected error occurred. Please try again.";
            if (e.getMessage() != null && (e.getMessage().contains("duplicate") || e.getMessage().contains("unique"))) {
                errorMessage = "Email address is already in use.";
            }
            
            request.setAttribute("message", errorMessage);
            request.setAttribute("messageType", "error");
        }
        
	     // ONLY FORWARD ON ERROR (success path already redirected above)
	     // Determine which JSP to forward to based on user type
	     String forwardPath = "/Manager/DirectorEditAccount.jsp"; // Default for executive director
	     if (userType != null && "executive_director".equals(userType)) {
	         forwardPath = "/Manager/DirectorEditAccount.jsp";
	     } else {
	         forwardPath = "/Manager/ManagerEditAccount.jsp";
	     }
        
	  // FORWARD to edit page (only happens on error now)
	     request.getRequestDispatcher(forwardPath).forward(request, response);
	     System.out.println("=== UPDATE MANAGER ACCOUNT CONTROLLER END ===");
    }
    
    /**
     * Validate email format
     */
    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }
    
    /**
     * Set error message and forward to appropriate edit page
     */
    private void setErrorAndForward(HttpServletRequest request, HttpServletResponse response, String errorMessage) 
            throws ServletException, IOException {
        System.out.println("❌ Error: " + errorMessage);
        request.setAttribute("message", errorMessage);
        request.setAttribute("messageType", "error");
        
        // Determine which JSP to forward to based on user type
        HttpSession session = request.getSession();
        String userType = (String) session.getAttribute("userType");
        String forwardPath = "/Manager/DirectorEditAccount.jsp"; // Default
        
        if (userType != null && "executive_director".equals(userType)) {
            forwardPath = "/Manager/DirectorEditAccount.jsp";
        } else {
            forwardPath = "/Manager/ManagerEditAccount.jsp";
        }
        
        request.getRequestDispatcher(forwardPath).forward(request, response);
    }
    
    /**
     * Handle profile picture upload with comprehensive validation and error handling
     */
    private String handleProfilePictureUpload(Part filePart, String managerId, HttpServletRequest request, String oldProfilePath) {
        try {
            String originalFileName = filePart.getSubmittedFileName();
            if (originalFileName == null || originalFileName.trim().isEmpty()) {
                System.out.println("No file selected for upload");
                return null;
            }
            
            System.out.println("Processing file upload:");
            System.out.println("- Original filename: " + originalFileName);
            System.out.println("- File size: " + filePart.getSize() + " bytes");
            System.out.println("- Content type: " + filePart.getContentType());
            
            // Validate file extension
            String fileExtension = getFileExtension(originalFileName).toLowerCase();
            if (!isValidImageExtension(fileExtension)) {
                System.out.println("❌ Invalid file extension: " + fileExtension);
                return null;
            }
            
            // Validate file size (5MB max)
            if (filePart.getSize() > (5 * 1024 * 1024)) {
                System.out.println("❌ File too large: " + filePart.getSize() + " bytes");
                return null;
            }
            
            // Create unique filename with timestamp
            String uniqueFileName = "manager_" + managerId + "_" + System.currentTimeMillis() + fileExtension;
            System.out.println("Generated unique filename: " + uniqueFileName);
            
            // Get upload directory
            String uploadPath = getUploadDirectory(request);
            System.out.println("Upload directory: " + uploadPath);
            
            // Create directory if needed
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                boolean created = uploadDir.mkdirs();
                System.out.println("Upload directory created: " + created);
                if (!created) {
                    System.err.println("❌ Failed to create upload directory: " + uploadPath);
                    return null;
                }
            }
            
            // Delete old profile picture if it exists and is not the default avatar
            if (oldProfilePath != null && !oldProfilePath.isEmpty() && 
                !oldProfilePath.equals("defaultprofilepicture.jpg") && 
                !oldProfilePath.contains("defaultprofilepicture.jpg")) {
                try {
                    String oldFilePath = request.getServletContext().getRealPath("") + File.separator + oldProfilePath;
                    File oldFile = new File(oldFilePath);
                    if (oldFile.exists() && oldFile.delete()) {
                        System.out.println("✅ Deleted old profile picture: " + oldFilePath);
                    }
                } catch (Exception e) {
                    System.out.println("⚠️ Could not delete old profile picture: " + e.getMessage());
                    // Continue anyway, this is not critical
                }
            }
            
            // Save new file
            String filePath = uploadPath + File.separator + uniqueFileName;
            Path targetPath = Paths.get(filePath);
            
            System.out.println("Saving file to: " + filePath);
            Files.copy(filePart.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            
            // Verify file was saved
            File savedFile = new File(filePath);
            if (savedFile.exists()) {
                System.out.println("✅ File saved successfully:");
                System.out.println("- Path: " + filePath);
                System.out.println("- Size: " + savedFile.length() + " bytes");
                
                // Return relative path for database storage
                String relativePath = UPLOAD_DIR + "/" + uniqueFileName;
                System.out.println("✅ Returning relative path: " + relativePath);
                return relativePath;
                
            } else {
                System.err.println("❌ File was not saved successfully");
                return null;
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error uploading profile picture: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Get the upload directory path
     */
    private String getUploadDirectory(HttpServletRequest request) {
        String applicationPath = request.getServletContext().getRealPath("");
        return applicationPath + File.separator + UPLOAD_DIR;
    }
    
    /**
     * Extract file extension from filename
     */
    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        return lastDotIndex == -1 ? "" : fileName.substring(lastDotIndex);
    }
    
    /**
     * Check if file extension is valid for images
     */
    private boolean isValidImageExtension(String extension) {
        for (String allowed : ALLOWED_EXTENSIONS) {
            if (allowed.equals(extension)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Validate manager name (letters, spaces, hyphens, apostrophes only)
     */
    private boolean isValidName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        // Allow letters, spaces, hyphens, and apostrophes
        return name.matches("^[a-zA-Z\\s'-]+$") && name.length() >= 2 && name.length() <= 100;
    }
    
    /**
     * Flexible phone number formatter - accepts various formats and converts to compact Malaysian standard
     * Accepts: 0123456789, 123456789, 60123456789, +60123456789, +60 12-345 6789, etc.
     * Returns: +60123456789 format (12 chars) or null if invalid
     */
    private String formatPhoneNumber(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return null;
        }
        
        System.out.println("DEBUG: Formatting phone input: '" + phone + "'");
        
        // Remove all non-digit characters except +
        String digitsOnly = phone.replaceAll("[^\\d+]", "");
        System.out.println("DEBUG: Digits only: '" + digitsOnly + "'");
        
        // Handle different input patterns
        String normalizedDigits = "";
        
        if (digitsOnly.startsWith("+60")) {
            // +60123456789 or +60 12-345 6789 format
            normalizedDigits = digitsOnly.substring(3); // Remove +60
        } else if (digitsOnly.startsWith("60")) {
            // 60123456789 format
            normalizedDigits = digitsOnly.substring(2); // Remove 60
        } else if (digitsOnly.startsWith("01")) {
            // 0123456789 format (Malaysian local with leading 0)
            normalizedDigits = digitsOnly.substring(1); // Remove leading 0
        } else if (digitsOnly.startsWith("1") && digitsOnly.length() >= 9) {
            // 123456789 format (Malaysian local without leading 0)
            normalizedDigits = digitsOnly;
        } else {
            // Invalid format
            System.out.println("DEBUG: Invalid phone format - unrecognized pattern");
            return null;
        }
        
        System.out.println("DEBUG: Normalized digits: '" + normalizedDigits + "'");
        
        // Validate length and format
        if (normalizedDigits.length() < 9 || normalizedDigits.length() > 10) {
            System.out.println("DEBUG: Invalid phone length: " + normalizedDigits.length());
            return null;
        }
        
        // Ensure it's 9 digits (remove extra leading digits if any)
        if (normalizedDigits.length() == 10 && normalizedDigits.startsWith("1")) {
            normalizedDigits = normalizedDigits.substring(1);
        } else if (normalizedDigits.length() > 9) {
            normalizedDigits = normalizedDigits.substring(normalizedDigits.length() - 9);
        }
        
        // Must start with 1 (Malaysian mobile numbers start with 1x)
        if (!normalizedDigits.startsWith("1") || normalizedDigits.length() != 9) {
            System.out.println("DEBUG: Invalid Malaysian mobile format - must start with 1 and be 9 digits");
            return null;
        }
        
        // Format as +60123456789 (compact format for 12 char limit)
        String formatted = "+60" + normalizedDigits;
        
        System.out.println("DEBUG: Final formatted phone: '" + formatted + "'");
        return formatted;
    }
    
    /**
     * Validate Malaysian phone number format: +60123456789
     */
    private boolean isValidMalaysianPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        return MALAYSIA_PHONE_PATTERN.matcher(phone.trim()).matches();
    }
    
    /**
     * Clean phone number format (ensure consistent formatting)
     */
    private String cleanPhoneNumber(String phone) {
        if (phone == null) {
            return null;
        }
        // Remove any extra spaces and ensure consistent formatting
        return phone.replaceAll("\\s+", " ").trim();
    }
    
    /**
     * Handle GET request - show edit account page
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        System.out.println("=== GET: Showing edit account page ===");
        
        HttpSession session = request.getSession();
        Manager manager = (Manager) session.getAttribute("manager");
        String userType = (String) session.getAttribute("userType");
        
        // Check if manager is logged in
        if (manager == null) {
            System.out.println("No manager in session, redirecting to login");
            response.sendRedirect("/ELMS_3.0/Manager/ManagerLogin.jsp");
            return;
        }
        
        // Determine which JSP to forward to based on user type
        String forwardPath = "/Manager/DirectorEditAccount.jsp"; // Default
        if (userType != null && "executive_director".equals(userType)) {
            forwardPath = "/Manager/DirectorEditAccount.jsp";
            System.out.println("Showing edit page for executive director: " + manager.getManagername());
        } else {
            forwardPath = "/Manager/ManagerEditAccount.jsp";
            System.out.println("Showing edit page for manager: " + manager.getManagername());
        }
        
        request.getRequestDispatcher(forwardPath).forward(request, response);
    }
}
package elms.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.regex.Pattern;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import elms.DAO.EmployeeDAO;
import elms.model.Employee;

@WebServlet("/update-account")
@MultipartConfig(
    maxFileSize = 1024 * 1024 * 5,      // 5MB max file size
    maxRequestSize = 1024 * 1024 * 10,  // 10MB max request size
    fileSizeThreshold = 1024 * 1024 * 1 // 1MB threshold
)
public class EmployeeUpdateAccountController extends HttpServlet {

    private static final String UPLOAD_DIR = "profile-pictures";
    private static final String[] ALLOWED_EXTENSIONS = {".jpg", ".jpeg", ".png", ".gif"};
    
    // Malaysian phone number pattern: +60 1x-xxx xxxx
    private static final Pattern MALAYSIA_PHONE_PATTERN = Pattern.compile("^\\\\+60\\\\d{9}$");
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        System.out.println("=== UPDATE ACCOUNT CONTROLLER START ===");
        long startTime = System.currentTimeMillis();
        
        HttpSession session = request.getSession();
        Employee currentEmployee = (Employee) session.getAttribute("loggedInEmployee");
        
        if (currentEmployee == null) {
            System.out.println("No employee in session, redirecting to login");
            response.sendRedirect("/ELMS_3.0/Employee/EmployeeLogin.jsp");
            return;
        }
        
        System.out.println("BEFORE UPDATE - Current employee in session:");
        System.out.println("- ID: " + currentEmployee.getEmployeeId());
        System.out.println("- Name: " + currentEmployee.getEmployeeName());

        System.out.println("- Current Profile Picture: " + currentEmployee.getProfilePicturePath());
        
        try {
            // Get form parameters
            String employeeId = request.getParameter("employeeid");
            String fullName = request.getParameter("employeename");
            String email = request.getParameter("employeeemail");
            String phone = request.getParameter("employeenophone");
            
            // Check if profile picture should be removed
            String removeProfilePicture = request.getParameter("remove_profile_picture");
            boolean shouldRemoveProfilePicture = "true".equals(removeProfilePicture);
            
            System.out.println("Processing update for employee: " + employeeId);
            System.out.println("Form data:");
            System.out.println("- Name: " + fullName);
            System.out.println("- Email: " + email);
            System.out.println("- Phone: " + phone);
            System.out.println("- Remove Profile Picture: " + shouldRemoveProfilePicture);
            System.out.println("- Phone will be formatted server-side");
            
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
            
            // Validate employee name format
            if (!isValidName(fullName.trim())) {
                setErrorAndForward(request, response, "Employee name can only contain letters, spaces, hyphens, and apostrophes");
                return;
            }
            
            // Handle profile picture removal FIRST (before upload handling)
            String finalProfilePicturePath = currentEmployee.getProfilePicturePath();
            
            if (shouldRemoveProfilePicture) {
                System.out.println("=== PROFILE PICTURE REMOVAL REQUESTED ===");
                
                // Delete the current profile picture file if it exists and is not default
                if (currentEmployee.getProfilePicturePath() != null && 
                    !currentEmployee.getProfilePicturePath().isEmpty() && 
                    !currentEmployee.getProfilePicturePath().equals("defaultprofilepicture.jpg") && 
                    !currentEmployee.getProfilePicturePath().contains("defaultprofilepicture")) {
                    
                    try {
                        String currentFilePath = request.getServletContext().getRealPath("") + 
                                               File.separator + currentEmployee.getProfilePicturePath();
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
                    newProfilePicturePath = handleProfilePictureUpload(profilePicturePart, employeeId, request, currentEmployee.getProfilePicturePath());
                    if (newProfilePicturePath != null) {
                        System.out.println("✅ Profile picture uploaded successfully: " + newProfilePicturePath);
                        finalProfilePicturePath = newProfilePicturePath;
                    } else {
                        System.out.println("❌ Profile picture upload failed");
                        setErrorAndForward(request, response, "Failed to upload profile picture. Please try again.");
                        return;
                    }
                } else {
                    System.out.println("No new profile picture uploaded, keeping existing: " + currentEmployee.getProfilePicturePath());
                    // Only keep existing if we're not removing it
                    if (!shouldRemoveProfilePicture) {
                        finalProfilePicturePath = currentEmployee.getProfilePicturePath();
                    }
                }
            }
            
            // Create updated employee object with all current data
            Employee updatedEmployee = new Employee();
            updatedEmployee.setEmployeeId(employeeId);
            updatedEmployee.setEmployeeName(fullName.trim());
            updatedEmployee.setEmployeeEmail(email.trim());
            updatedEmployee.setEmployeeNoPhone(phone != null ? phone.trim() : "");
            updatedEmployee.setEmployeePassword(currentEmployee.getEmployeePassword());
            
            
            // Set the final profile picture path (could be new upload, existing, or null for removal)
            updatedEmployee.setProfilePicturePath(finalProfilePicturePath);
            
            System.out.println("BEFORE DATABASE UPDATE - Employee object to save:");
            System.out.println("- ID: " + updatedEmployee.getEmployeeId());
            System.out.println("- Name: " + updatedEmployee.getEmployeeName());
            System.out.println("- Email: " + updatedEmployee.getEmployeeEmail());
            System.out.println("- Phone: " + updatedEmployee.getEmployeeNoPhone());
           
            System.out.println("- Profile Picture: " + updatedEmployee.getProfilePicturePath());
            
            // Update the database
            String result = EmployeeDAO.updateEmployee(updatedEmployee);
            
            if (result != null && result.contains("success")) {
                System.out.println("✅ Database update successful");
                
                // CRITICAL: Update the session employee object immediately
                Employee updatedSessionEmployee = new Employee();
                updatedSessionEmployee.setEmployeeId(employeeId);
                updatedSessionEmployee.setEmployeeName(fullName.trim());
                updatedSessionEmployee.setEmployeeEmail(email.trim());
                updatedSessionEmployee.setEmployeeNoPhone(phone != null ? phone.trim() : "");
                updatedSessionEmployee.setEmployeePassword(currentEmployee.getEmployeePassword());
               
                updatedSessionEmployee.setProfilePicturePath(finalProfilePicturePath);
                
                // Update session
                session.setAttribute("loggedInEmployee", updatedSessionEmployee);
                
                System.out.println("✅ Session updated with:");
                System.out.println("- ID: " + updatedSessionEmployee.getEmployeeId());
                System.out.println("- Name: " + updatedSessionEmployee.getEmployeeName());
                
                System.out.println("- Profile Picture: " + updatedSessionEmployee.getProfilePicturePath());
                
                // Set success message in session for modal display on settings page
                String successMessage;
                if (shouldRemoveProfilePicture) {
                    successMessage = "Account information updated successfully! Profile picture has been removed.";
                } else if (newProfilePicturePath != null) {
                    successMessage = "Account information and profile picture updated successfully!";
                } else {
                    successMessage = "Account information updated successfully!";
                }
                
                // Store success message in session for modal display
                session.setAttribute("successMessage", successMessage);
                
                long endTime = System.currentTimeMillis();
                System.out.println("✅ Update completed in " + (endTime - startTime) + "ms");
                System.out.println("🔄 Redirecting to settings page with success modal...");
                
                // Redirect to settings page instead of forwarding to edit page
                response.sendRedirect(request.getContextPath() + "/Employee/EmployeeSettings.jsp");
                return;
                
            } else {
                System.out.println("❌ Database update failed: " + result);
                String errorMessage = "Failed to update account. Please try again.";
                if (result != null && result.contains("Email address is already in use")) {
                    errorMessage = "Email address is already in use by another employee.";
                }
                request.setAttribute("message", errorMessage);
                request.setAttribute("messageType", "error");
            }
            
        } catch (SQLException e) {
            System.err.println("❌ SQL Exception: " + e.getMessage());
            e.printStackTrace();
            
            String errorMessage = "Database error occurred. Please try again later.";
            if (e.getMessage().contains("duplicate") || e.getMessage().contains("unique")) {
                errorMessage = "Email address is already in use.";
            }
            
            request.setAttribute("message", errorMessage);
            request.setAttribute("messageType", "error");
            
        } catch (Exception e) {
            System.err.println("❌ General Exception: " + e.getMessage());
            e.printStackTrace();
            
            request.setAttribute("message", "An unexpected error occurred. Please try again.");
            request.setAttribute("messageType", "error");
        }
        
        // Only forward to edit page if there was an error (success cases redirect above)
        System.out.println("⚠️ Forwarding to edit page due to error");
        request.getRequestDispatcher("/Employee/EmployeeEditAccount.jsp").forward(request, response);
        System.out.println("=== UPDATE ACCOUNT CONTROLLER END ===");
    }
    
    /**
     * Validate email format
     */
    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }
    
    /**
     * Set error message and forward to edit page
     */
    private void setErrorAndForward(HttpServletRequest request, HttpServletResponse response, String errorMessage) 
            throws ServletException, IOException {
        System.out.println("❌ Error: " + errorMessage);
        request.setAttribute("message", errorMessage);
        request.setAttribute("messageType", "error");
        request.getRequestDispatcher("/Employee/EmployeeEditAccount.jsp").forward(request, response);
    }
    
    /**
     * Handle profile picture upload with comprehensive validation and error handling
     */
    private String handleProfilePictureUpload(Part filePart, String employeeId, HttpServletRequest request, String oldProfilePath) {
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
            String uniqueFileName = employeeId + "_" + System.currentTimeMillis() + fileExtension;
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
     * Validate employee name (letters, spaces, hyphens, apostrophes only)
     */
    private boolean isValidName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        // Allow letters, spaces, hyphens, and apostrophes
        return name.matches("^[a-zA-Z\\s'-]+$") && name.length() >= 2 && name.length() <= 100;
    }
    
    /**
     * Flexible phone number formatter - accepts various formats and converts to Malaysian standard
     * Accepts: 0123456789, 123456789, 60123456789, +60123456789, +60 12-345 6789, etc.
     * Returns: +60 1x-xxx xxxx format or null if invalid
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
        
        // Format as +60 1x-xxx xxxx
        String formatted = "+60" + normalizedDigits;
        
        System.out.println("DEBUG: Final formatted phone: '" + formatted + "'");
        return formatted;
    }
    
    /**
     * Validate Malaysian phone number format: +60 1x-xxx xxxx
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
        Employee employee = (Employee) session.getAttribute("loggedInEmployee");
        
        if (employee == null) {
            System.out.println("No employee in session, redirecting to login");
            response.sendRedirect("/ELMS_3.0/Employee/EmployeeLogin.jsp");
            return;
        }
        
        System.out.println("Showing edit page for employee: " + employee.getEmployeeName());
        request.getRequestDispatcher("/Employee/EmployeeEditAccount.jsp").forward(request, response);
    }
}
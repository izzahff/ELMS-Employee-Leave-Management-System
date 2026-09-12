package elms.controller;

import elms.DAO.LeaveTypeDAO;
import elms.DAO.FullDayDAO;
import elms.DAO.HalfDayDAO;
import elms.model.LeaveType;
import elms.model.FullDay;
import elms.model.HalfDay;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/CreateLeaveTypeController")
public class AdminCreateLeaveTypeController extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private LeaveTypeDAO leaveTypeDAO;
    private FullDayDAO fullDayDAO;
    private HalfDayDAO halfDayDAO;
    
    @Override
    public void init() throws ServletException {
        super.init();
        leaveTypeDAO = new LeaveTypeDAO();
        fullDayDAO = new FullDayDAO();
        halfDayDAO = new HalfDayDAO();
        System.out.println("=== AdminCreateLeaveTypeController initialized ===");
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // ============================================
        // HANDLE AJAX DUPLICATE NAME CHECK
        // ============================================
        String action = request.getParameter("action");
        
        if ("checkDuplicate".equals(action)) {
            System.out.println("=== DUPLICATE CHECK REQUEST (GET) ===");
            String name = request.getParameter("name");
            System.out.println("Checking name: '" + name + "'");
            
            boolean exists = false;
            
            try {
                if (name != null && !name.trim().isEmpty()) {
                    exists = leaveTypeDAO.leaveTypeNameExists(name.trim());
                    System.out.println("🔍 Result: " + (exists ? "EXISTS ❌" : "AVAILABLE ✅"));
                }
            } catch (Exception e) {
                System.err.println("Error checking duplicate: " + e.getMessage());
                e.printStackTrace();
            }
            
            // Clear any existing response content
            response.reset();
            
            // Set response headers BEFORE writing anything
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Cache-Control", "no-cache");
            
            // Write ONLY JSON
            String jsonResponse = "{\"exists\":" + exists + "}";
            System.out.println("Sending JSON: " + jsonResponse);
            
            response.getWriter().write(jsonResponse);
            response.getWriter().flush();
            
            return; // Exit immediately
        }
        
        // ============================================
        // REGULAR GET REQUEST - REDIRECT TO JSP
        // ============================================
        System.out.println("=== doGet called - redirecting to AdminCreateLeaveType.jsp ===");
        String redirectPath = request.getContextPath() + "/Admin/AdminCreateLeaveType.jsp";
        System.out.println("*** DOGET REDIRECT PATH: " + redirectPath + " ***");
        response.sendRedirect(redirectPath);
    }
    
   
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // ============================================
        // HANDLE AJAX DUPLICATE NAME CHECK FIRST
        // ============================================
        String action = request.getParameter("action");
        System.out.println("Action parameter: " + action);
        
        if ("checkDuplicate".equals(action)) {
            System.out.println("=== DUPLICATE CHECK REQUEST ===");
            String name = request.getParameter("name");
            System.out.println("Checking name: '" + name + "'");
            
            boolean exists = false;
            
            try {
                if (name != null && !name.trim().isEmpty()) {
                    exists = leaveTypeDAO.leaveTypeNameExists(name.trim());
                    System.out.println("🔍 Result: " + (exists ? "EXISTS ❌" : "AVAILABLE ✅"));
                }
            } catch (Exception e) {
                System.err.println("Error checking duplicate: " + e.getMessage());
                e.printStackTrace();
            }
            
            // Clear any existing response content
            response.reset();
            
            // Set response headers BEFORE writing anything
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Cache-Control", "no-cache");
            
            // Write ONLY JSON - no whitespace, no HTML
            String jsonResponse = "{\"exists\":" + exists + "}";
            System.out.println("Sending: " + jsonResponse);
            
            response.getWriter().write(jsonResponse);
            response.getWriter().flush();
            
            return; // Exit immediately - don't continue to form processing
        }
        
        // ============================================
        // REGULAR FORM SUBMISSION
        // ============================================
        
        // Check if admin is logged in
        HttpSession session = request.getSession(false);
        
        // Add null check for session
        if (session == null) {
            System.out.println("❌ NO SESSION - redirecting to login");
            response.sendRedirect(request.getContextPath() + "/Admin/AdminLogin.jsp");
            return;
        }
        
        String adminId = (String) session.getAttribute("adminId");
        
        if (adminId == null) {
            System.out.println("❌ NO ADMIN ID IN SESSION - redirecting to login");
            response.sendRedirect(request.getContextPath() + "/Admin/AdminLogin.jsp");
            return;
        }
        
        System.out.println("✅ ADMIN SESSION VALID - Admin ID: " + adminId);
        
        try {
            // Get form parameters
            String leaveType = request.getParameter("leaveType");
            String leaveTypeName = request.getParameter("leaveTypeName");
            String leaveTypeDescription = request.getParameter("leaveTypeDescription");
            String standardDurationStr = request.getParameter("standardDuration");
            String halfDayShift = request.getParameter("halfDayShift");
            String requiresDocumentStr = request.getParameter("requiresDocument");
            String affectsBalanceStr = request.getParameter("affectsBalance");
            String fixedDurationStr = request.getParameter("fixedDuration");
            
            System.out.println("=== Form Parameters Received ===");
            System.out.println("Leave Type: " + leaveType);
            System.out.println("Leave Type Name: " + leaveTypeName);
            System.out.println("Description Length: " + (leaveTypeDescription != null ? leaveTypeDescription.length() : 0));
            System.out.println("Standard Duration: " + standardDurationStr);
            System.out.println("Half Day Shift: " + halfDayShift);
            System.out.println("Requires Document: " + requiresDocumentStr);
            System.out.println("Affects Balance: " + affectsBalanceStr);
            System.out.println("Fixed Duration: " + fixedDurationStr);
            
            // Parse checkboxes
            boolean requiresDocument = "on".equals(requiresDocumentStr) || "true".equals(requiresDocumentStr);
            boolean affectsBalance = "on".equals(affectsBalanceStr) || "true".equals(affectsBalanceStr);
            boolean fixedDuration = "on".equals(fixedDurationStr) || "true".equals(fixedDurationStr);
            
            System.out.println("Requires Document (parsed): " + requiresDocument);
            System.out.println("Affects Balance (parsed): " + affectsBalance);
            System.out.println("Fixed Duration (parsed): " + fixedDuration);
            
            // ============================================
            // COMPREHENSIVE VALIDATION - COLLECT ALL ERRORS
            // ============================================
            List<String> validationErrors = new ArrayList<>();
            Integer standardDuration = null;

            // 1. Validate leave type selection
            if (leaveType == null || leaveType.trim().isEmpty()) {
                validationErrors.add("Please select a leave duration type");
            } else {
                leaveType = leaveType.trim();
                if (!leaveType.equals("Full Day") && !leaveType.equals("Half Day") && !leaveType.equals("Both")) {
                    validationErrors.add("Invalid leave type selection");
                }
            }

            // 2. Validate standard duration for Full Day and Both
            if ("Full Day".equals(leaveType) || "Both".equals(leaveType)) {
                if (standardDurationStr == null || standardDurationStr.trim().isEmpty()) {
                    validationErrors.add("Standard duration is required for Full Day or Both leave types");
                } else {
                    try {
                        standardDuration = Integer.parseInt(standardDurationStr.trim());
                        if (standardDuration < 1) {
                            validationErrors.add("Standard duration must be at least 1 day");
                        } else if (standardDuration > 365) {
                            validationErrors.add("Standard duration cannot exceed 365 days");
                        }
                    } catch (NumberFormatException e) {
                        validationErrors.add("Invalid standard duration - please enter a valid number");
                    }
                }
            }

            // 3. Validate half day shift
            if ("Half Day".equals(leaveType) || "Both".equals(leaveType)) {
                if (halfDayShift == null || halfDayShift.trim().isEmpty()) {
                    validationErrors.add("Please select available shifts for half day leave");
                } else {
                    halfDayShift = halfDayShift.trim();
                    if (!halfDayShift.equals("Morning") && !halfDayShift.equals("Afternoon") && !halfDayShift.equals("Both")) {
                        validationErrors.add("Invalid shift selection - must be Morning, Afternoon, or Both");
                    }
                }
            }

            // 4. Validate leave type name
            if (leaveTypeName == null || leaveTypeName.trim().isEmpty()) {
                validationErrors.add("Leave type name is required");
            } else {
                String trimmedName = leaveTypeName.trim();
                
                // Collect all name-related errors
                boolean nameHasErrors = false;
                
                if (trimmedName.length() < 3) {
                    validationErrors.add("Leave type name must be at least 3 characters long");
                    nameHasErrors = true;
                }
                
                if (trimmedName.length() > 100) {
                    validationErrors.add("Leave type name cannot exceed 100 characters");
                    nameHasErrors = true;
                }
                
                if (!trimmedName.matches(".*[a-zA-Z].*")) {
                    validationErrors.add("Leave type name must contain at least one letter");
                    nameHasErrors = true;
                }
                
                if (!trimmedName.matches("^[a-zA-Z0-9\\s]+$")) {
                    validationErrors.add("Leave type name can only contain letters, numbers, and spaces (no special characters)");
                    nameHasErrors = true;
                }
                
                // Only check for duplicates if other name validations passed
                if (!nameHasErrors && leaveTypeDAO.leaveTypeNameExists(trimmedName)) {
                    validationErrors.add("A leave type with the name '" + trimmedName + "' already exists - please choose a different name");
                }
            }

            // 5. Validate description
            if (leaveTypeDescription == null || leaveTypeDescription.trim().isEmpty()) {
                validationErrors.add("Description is required");
            } else {
                String trimmedDesc = leaveTypeDescription.trim();
                
                if (trimmedDesc.length() < 10) {
                    validationErrors.add("Description must be at least 10 characters long");
                }
                
                if (trimmedDesc.length() > 500) {
                    validationErrors.add("Description cannot exceed 500 characters");
                }
                
                if (!trimmedDesc.matches(".*[a-zA-Z].*")) {
                    validationErrors.add("Description must contain at least one letter (not just numbers or symbols)");
                }
            }

            // If there are validation errors, send them all back
            if (!validationErrors.isEmpty()) {
                System.out.println("❌ Validation failed with " + validationErrors.size() + " error(s):");
                for (int i = 0; i < validationErrors.size(); i++) {
                    System.out.println("  " + (i + 1) + ". " + validationErrors.get(i));
                }
                request.setAttribute("validationErrors", validationErrors);
                request.getRequestDispatcher("/Admin/AdminCreateLeaveType.jsp").forward(request, response);
                return;
            }
            
            // ============================================
            // VALIDATION PASSED - PROCEED WITH CREATION
            // ============================================
            
            // Trim whitespace
            leaveType = leaveType.trim();
            leaveTypeName = leaveTypeName.trim();
            leaveTypeDescription = leaveTypeDescription.trim();
            
            System.out.println("✅ All validation passed - creating LeaveType object");
            
            // Create LeaveType object
            LeaveType newLeaveType = new LeaveType();
            newLeaveType.setAdminId(adminId);
            newLeaveType.setLeaveTypeCategory(leaveType);
            newLeaveType.setLeaveTypeName(leaveTypeName);
            newLeaveType.setLeaveTypeDescription(leaveTypeDescription);
            newLeaveType.setRequiresDocument(requiresDocument);
            newLeaveType.setAffectsBalance(affectsBalance);
            newLeaveType.setFixedDuration(fixedDuration);
            
            System.out.println("Attempting to save LeaveType to database");
            
            // Save LeaveType to database
            String leaveTypeId = leaveTypeDAO.createLeaveTypeAndReturnId(newLeaveType);
            
            System.out.println("LeaveType save result - ID: " + leaveTypeId);
            
            if (leaveTypeId != null) {
                boolean subTypeCreated = true;
                
                // Build success message with bullet points
                StringBuilder successMessage = new StringBuilder();
                successMessage.append("<strong>Leave type '").append(leaveTypeName).append("' created successfully!</strong><br><br>");
                successMessage.append("<ul style='text-align: left; padding-left: 20px; margin: 0;'>");
                
                // Document requirement
                if (requiresDocument) {
                    successMessage.append("<li>Supporting documents <strong>required</strong></li>");
                } else {
                    successMessage.append("<li>Supporting documents <strong>optional</strong></li>");
                }
                
                // Balance tracking
                if (affectsBalance) {
                    successMessage.append("<li>Will <strong>deduct</strong> from leave balance</li>");
                } else {
                    successMessage.append("<li>Will <strong>NOT</strong> affect leave balance</li>");
                }
                
                // Duration mode
                if (fixedDuration) {
                    successMessage.append("<li>Fixed duration: Employees must take exact amount</li>");
                } else {
                    successMessage.append("<li>Flexible duration: Up to maximum allowed</li>");
                }
                
                // Create corresponding records based on leave type category
                if ("Full Day".equals(leaveType)) {
                    System.out.println("Creating FullDay record for leave type ID: " + leaveTypeId);
                    
                    FullDay fullDay = new FullDay();
                    fullDay.setLeaveTypeId(leaveTypeId);
                    
                    if (standardDuration != null && standardDuration > 0) {
                        fullDay.setStandardDuration(standardDuration);
                        System.out.println("Setting standard duration: " + standardDuration);
                        successMessage.append("<li>Standard duration: <strong>").append(standardDuration).append(" day").append(standardDuration > 1 ? "s" : "").append("</strong></li>");
                    }
                    
                    subTypeCreated = fullDayDAO.createFullDay(fullDay);
                    System.out.println("FullDay creation result: " + subTypeCreated);
                    
                } else if ("Half Day".equals(leaveType)) {
                    System.out.println("Creating HalfDay record for leave type ID: " + leaveTypeId);
                    
                    HalfDay halfDay = new HalfDay();
                    halfDay.setLeaveTypeId(leaveTypeId);
                    halfDay.setShift(halfDayShift);
                    
                    subTypeCreated = halfDayDAO.createHalfDay(halfDay);
                    System.out.println("HalfDay creation result: " + subTypeCreated);
                    
                    if ("Morning".equals(halfDayShift)) {
                        successMessage.append("<li>Available shift: <strong>Morning only</strong></li>");
                    } else if ("Afternoon".equals(halfDayShift)) {
                        successMessage.append("<li>Available shift: <strong>Afternoon only</strong></li>");
                    } else if ("Both".equals(halfDayShift)) {
                        successMessage.append("<li>Available shifts: <strong>Morning & Afternoon</strong></li>");
                    }
                    
                } else if ("Both".equals(leaveType)) {
                    System.out.println("Creating Both FullDay and HalfDay records for leave type ID: " + leaveTypeId);
                    
                    // Create FullDay record
                    FullDay fullDay = new FullDay();
                    fullDay.setLeaveTypeId(leaveTypeId);
                    
                    if (standardDuration != null && standardDuration > 0) {
                        fullDay.setStandardDuration(standardDuration);
                        System.out.println("Setting standard duration: " + standardDuration);
                        successMessage.append("<li>Full day duration: <strong>").append(standardDuration).append(" day").append(standardDuration > 1 ? "s" : "").append("</strong></li>");
                    }
                    
                    boolean fullDayCreated = fullDayDAO.createFullDay(fullDay);
                    System.out.println("FullDay creation result: " + fullDayCreated);
                    
                    // Create HalfDay record
                    HalfDay halfDay = new HalfDay();
                    halfDay.setLeaveTypeId(leaveTypeId);
                    halfDay.setShift(halfDayShift);
                    
                    boolean halfDayCreated = halfDayDAO.createHalfDay(halfDay);
                    System.out.println("HalfDay creation result: " + halfDayCreated);
                    
                    subTypeCreated = fullDayCreated && halfDayCreated;
                    
                    successMessage.append("<li>Flexible: <strong>Full day or half day</strong></li>");
                    
                    if ("Morning".equals(halfDayShift)) {
                        successMessage.append("<li>Half day shift: <strong>Morning only</strong></li>");
                    } else if ("Afternoon".equals(halfDayShift)) {
                        successMessage.append("<li>Half day shift: <strong>Afternoon only</strong></li>");
                    } else if ("Both".equals(halfDayShift)) {
                        successMessage.append("<li>Half day shifts: <strong>Morning & Afternoon</strong></li>");
                    }
                }
                
                successMessage.append("</ul>");
                
                if (subTypeCreated) {
                    // Success - redirect to list page with success message
                    System.out.println("🎉 Success! Leave type created successfully");
                    session.setAttribute("successMessage", successMessage.toString());
                    session.setAttribute("newLeaveTypeId", leaveTypeId); // Store ID for scrolling
                    response.sendRedirect(request.getContextPath() + "/AdminLeaveTypeListController");
                } else {
                    // Error creating sub-type record
                    System.out.println("Failed to create sub-type record");
                    request.setAttribute("errorMessage", "Leave type created but failed to set additional details. Please try again.");
                    request.getRequestDispatcher("/Admin/AdminCreateLeaveType.jsp").forward(request, response);
                }
            } else {
                // Error saving LeaveType to database
                System.out.println("Failed to save LeaveType to database");
                request.setAttribute("errorMessage", "Failed to create leave type. Please try again.");
                request.getRequestDispatcher("/Admin/AdminCreateLeaveType.jsp").forward(request, response);
            }
            
        } catch (Exception e) {
            System.err.println("❌ Exception in AdminCreateLeaveTypeController: " + e.getMessage());
            e.printStackTrace();
            
            request.setAttribute("errorMessage", "An unexpected error occurred: " + e.getMessage());
            request.getRequestDispatcher("/Admin/AdminCreateLeaveType.jsp").forward(request, response);
        }
    }
  
}
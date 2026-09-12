package elms.controller;

import elms.DAO.LeaveTypeDAO;
import elms.DAO.FullDayDAO;
import elms.DAO.HalfDayDAO;
import elms.DAO.LeaveApplicationDAO;
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
import java.io.PrintWriter;

@WebServlet("/AdminEditLeaveTypeController")
public class AdminEditLeaveTypeController extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private LeaveTypeDAO leaveTypeDAO;
    private FullDayDAO fullDayDAO;
    private HalfDayDAO halfDayDAO;
    private LeaveApplicationDAO leaveApplicationDAO;
    
    @Override
    public void init() throws ServletException {
        super.init();
        leaveTypeDAO = new LeaveTypeDAO();
        fullDayDAO = new FullDayDAO();
        halfDayDAO = new HalfDayDAO();
        leaveApplicationDAO = new LeaveApplicationDAO();
        System.out.println("=== AdminEditLeaveTypeController initialized ===");
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String action = request.getParameter("action");
        
        if ("getLeaveTypeData".equals(action)) {
            getLeaveTypeData(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid action");
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String action = request.getParameter("action");
        
        if ("updateLeaveType".equals(action)) {
            updateLeaveType(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid action");
        }
    }
    
    /**
     * Get leave type data for editing (AJAX request)
     * Returns JSON response with support for "Both" category
     * Includes totalApplications count for fixed duration warning
     */
    private void getLeaveTypeData(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        try {
            String leaveTypeId = request.getParameter("leaveTypeId");
            
            System.out.println("=== Getting Leave Type Data ===");
            System.out.println("Leave Type ID: " + leaveTypeId);
            
            if (leaveTypeId == null || leaveTypeId.trim().isEmpty()) {
                out.print("{\"success\":false,\"error\":\"Leave type ID is required\"}");
                return;
            }
            
            // Get leave type data
            LeaveType leaveType = leaveTypeDAO.getLeaveTypeById(leaveTypeId);
            
            if (leaveType == null) {
                System.err.println("Leave type not found for ID: " + leaveTypeId);
                out.print("{\"success\":false,\"error\":\"Leave type not found\"}");
                return;
            }
            
            System.out.println("Retrieved Leave Type:");
            System.out.println("  - ID: " + leaveType.getLeaveTypeId());
            System.out.println("  - Name: " + leaveType.getLeaveTypeName());
            System.out.println("  - Category: " + leaveType.getLeaveTypeCategory());
            System.out.println("  - Requires Document: " + leaveType.isRequiresDocument());
            System.out.println("  - Affects Balance: " + leaveType.isAffectsBalance());
            System.out.println("  - Fixed Duration: " + leaveType.isFixedDuration());
            
            // Get total applications count for this leave type
            int totalApplications = leaveApplicationDAO.countApplicationsByLeaveType(leaveTypeId);
            System.out.println("  - Total Applications: " + totalApplications);
            
            // Build JSON response manually
            StringBuilder json = new StringBuilder();
            json.append("{");
            json.append("\"success\":true,");
            json.append("\"leaveTypeId\":\"").append(escapeJson(leaveType.getLeaveTypeId())).append("\",");
            json.append("\"leaveType\":\"").append(escapeJson(leaveType.getLeaveTypeCategory())).append("\",");
            json.append("\"leaveTypeName\":\"").append(escapeJson(leaveType.getLeaveTypeName())).append("\",");
            json.append("\"leaveTypeDescription\":\"").append(escapeJson(leaveType.getLeaveTypeDescription())).append("\",");
            json.append("\"requiresDocument\":").append(leaveType.isRequiresDocument()).append(",");
            json.append("\"affectsBalance\":").append(leaveType.isAffectsBalance()).append(",");
            json.append("\"fixedDuration\":").append(leaveType.isFixedDuration()).append(",");
            json.append("\"totalApplications\":").append(totalApplications);
            
            // Get additional details based on leave type category
            String category = leaveType.getLeaveTypeCategory();
            
            if ("Full Day".equalsIgnoreCase(category)) {
                // Full Day Only - get standard duration
                FullDay fullDay = fullDayDAO.getFullDayByLeaveTypeId(leaveTypeId);
                if (fullDay != null) {
                    json.append(",\"standardDuration\":").append(fullDay.getStandardDuration());
                    System.out.println("  - Standard Duration: " + fullDay.getStandardDuration());
                } else {
                    json.append(",\"standardDuration\":0");
                    System.out.println("  - No FullDay record found");
                }
                
            } else if ("Half Day".equalsIgnoreCase(category)) {
                // Half Day Only - get shift configuration
                HalfDay halfDay = halfDayDAO.getHalfDayByLeaveTypeId(leaveTypeId);
                if (halfDay != null) {
                    json.append(",\"halfDayShift\":\"").append(escapeJson(halfDay.getShift())).append("\"");
                    System.out.println("  - Half Day Shift: " + halfDay.getShift());
                } else {
                    json.append(",\"halfDayShift\":\"Both\"");
                    System.out.println("  - No HalfDay record found");
                }
                
            } else if ("Both".equalsIgnoreCase(category)) {
                // Both - get both standard duration and shift configuration
                System.out.println("  - Category is 'Both', fetching both configurations");
                
                // Get Full Day details
                FullDay fullDay = fullDayDAO.getFullDayByLeaveTypeId(leaveTypeId);
                if (fullDay != null) {
                    json.append(",\"standardDuration\":").append(fullDay.getStandardDuration());
                    System.out.println("  - Standard Duration: " + fullDay.getStandardDuration());
                } else {
                    json.append(",\"standardDuration\":0");
                    System.out.println("  - No FullDay record found");
                }
                
                // Get Half Day details
                HalfDay halfDay = halfDayDAO.getHalfDayByLeaveTypeId(leaveTypeId);
                if (halfDay != null) {
                    json.append(",\"halfDayShift\":\"").append(escapeJson(halfDay.getShift())).append("\"");
                    System.out.println("  - Half Day Shift: " + halfDay.getShift());
                } else {
                    json.append(",\"halfDayShift\":\"Both\"");
                    System.out.println("  - No HalfDay record found");
                }
            }
            
            json.append("}");
            
            System.out.println("JSON Response: " + json.toString());
            out.print(json.toString());
            
        } catch (Exception e) {
            System.err.println("❌ Error getting leave type data: " + e.getMessage());
            e.printStackTrace();
            
            out.print("{\"success\":false,\"error\":\"An error occurred: " + escapeJson(e.getMessage()) + "\"}");
        }
    }
    
    /**
     * Update leave type
     * CRITICAL: Leave Type Category, Standard Duration, and Affects Balance are READ-ONLY
     * These are received as hidden fields to maintain data integrity
     */
    private void updateLeaveType(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        String adminId = (String) session.getAttribute("adminId");
        
        if (adminId == null) {
            System.err.println("❌ No admin session found");
            response.sendRedirect(request.getContextPath() + "/Admin/AdminLogin.jsp");
            return;
        }
        
        try {
            // Get form parameters
            String leaveTypeId = request.getParameter("leaveTypeId");
            
            // READ-ONLY fields (from hidden inputs to maintain data integrity)
            String leaveType = request.getParameter("leaveType");
            String standardDurationStr = request.getParameter("standardDuration");
            String affectsBalanceStr = request.getParameter("affectsBalance");
            
            // EDITABLE fields
            String leaveTypeName = request.getParameter("leaveTypeName");
            String leaveTypeDescription = request.getParameter("leaveTypeDescription");
            String halfDayShift = request.getParameter("halfDayShift");
            String requiresDocumentStr = request.getParameter("requiresDocument");
            String fixedDurationStr = request.getParameter("fixedDuration");
            
            System.out.println("=== Update Leave Type Request ===");
            System.out.println("Leave Type ID: " + leaveTypeId);
            System.out.println("--- READ-ONLY FIELDS (from hidden inputs) ---");
            System.out.println("Leave Type Category: " + leaveType);
            System.out.println("Standard Duration (raw): " + standardDurationStr);
            System.out.println("Affects Balance (raw): " + affectsBalanceStr);
            System.out.println("--- EDITABLE FIELDS ---");
            System.out.println("Leave Type Name: " + leaveTypeName);
            System.out.println("Description Length: " + (leaveTypeDescription != null ? leaveTypeDescription.length() : 0));
            System.out.println("Half Day Shift: " + halfDayShift);
            System.out.println("Requires Document (raw): " + requiresDocumentStr);
            System.out.println("Fixed Duration (raw): " + fixedDurationStr);
            
            // Validate input
            if (leaveTypeId == null || leaveTypeId.trim().isEmpty()) {
                System.err.println("❌ Invalid leave type ID");
                session.setAttribute("errorMessage", "Invalid leave type ID");
                response.sendRedirect(request.getContextPath() + "/AdminLeaveTypeListController");
                return;
            }
            
            if (leaveType == null || leaveType.trim().isEmpty()) {
                System.err.println("❌ Leave type category is required");
                session.setAttribute("errorMessage", "Leave type category is missing");
                response.sendRedirect(request.getContextPath() + "/AdminLeaveTypeListController");
                return;
            }
            
            // Validate leave type category value
            if (!leaveType.equals("Full Day") && !leaveType.equals("Half Day") && !leaveType.equals("Both")) {
                System.err.println("❌ Invalid leave type category: " + leaveType);
                session.setAttribute("errorMessage", "Invalid leave duration type");
                response.sendRedirect(request.getContextPath() + "/AdminLeaveTypeListController");
                return;
            }
            
            // Parse standard duration (READ-ONLY, comes from hidden field)
            Integer standardDuration = null;
            if (standardDurationStr != null && !standardDurationStr.trim().isEmpty()) {
                try {
                    standardDuration = Integer.parseInt(standardDurationStr.trim());
                    System.out.println("Standard duration (from hidden): " + standardDuration);
                } catch (NumberFormatException e) {
                    System.err.println("❌ Invalid standard duration format: " + standardDurationStr);
                    standardDuration = 0;
                }
            }
            
            // Validate Half Day shift if editable (for Half Day and Both types)
            if ("Half Day".equals(leaveType) || "Both".equals(leaveType)) {
                if (halfDayShift == null || halfDayShift.trim().isEmpty()) {
                    System.err.println("❌ Half day shift is required for " + leaveType);
                    session.setAttribute("errorMessage", "Please select available shifts for half day leave type");
                    response.sendRedirect(request.getContextPath() + "/AdminLeaveTypeListController");
                    return;
                }
                
                // Validate shift value
                if (!halfDayShift.equals("Morning") && !halfDayShift.equals("Afternoon") && !halfDayShift.equals("Both")) {
                    System.err.println("❌ Invalid half day shift value: " + halfDayShift);
                    session.setAttribute("errorMessage", "Invalid shift selection");
                    response.sendRedirect(request.getContextPath() + "/AdminLeaveTypeListController");
                    return;
                }
                System.out.println("Half day shift validated: " + halfDayShift);
            }
            
            // Parse checkboxes
            boolean requiresDocument = "on".equals(requiresDocumentStr) || "true".equals(requiresDocumentStr);
            boolean affectsBalance = "true".equals(affectsBalanceStr); // From hidden field, not checkbox
            boolean fixedDuration = "on".equals(fixedDurationStr) || "true".equals(fixedDurationStr);
            
            System.out.println("Requires Document (parsed): " + requiresDocument);
            System.out.println("Affects Balance (from hidden): " + affectsBalance);
            System.out.println("Fixed Duration (parsed): " + fixedDuration);
            
            // Validate leave type name and description
            if (leaveTypeName == null || leaveTypeName.trim().isEmpty()) {
                System.err.println("❌ Leave type name is required");
                session.setAttribute("errorMessage", "Leave type name is required");
                response.sendRedirect(request.getContextPath() + "/AdminLeaveTypeListController");
                return;
            }
            
         // ============================================
         // CHECK FOR DUPLICATE LEAVE TYPE NAME
         // ============================================
         System.out.println("=== Checking for Duplicate Leave Type Name ===");
         System.out.println("Current Leave Type ID: " + leaveTypeId);
         System.out.println("New/Updated Name: '" + leaveTypeName.trim() + "'");

         LeaveType existingLeaveType = leaveTypeDAO.getLeaveTypeByName(leaveTypeName.trim());

         if (existingLeaveType != null) {
             System.out.println("⚠️ Found existing leave type with this name:");
             System.out.println("   Existing ID: " + existingLeaveType.getLeaveTypeId());
             System.out.println("   Existing Name: '" + existingLeaveType.getLeaveTypeName() + "'");
             System.out.println("   Current ID being edited: " + leaveTypeId);
             
             // Check if it's a DIFFERENT leave type (not the same one being edited)
             if (!existingLeaveType.getLeaveTypeId().equals(leaveTypeId)) {
                 // This is a DIFFERENT leave type with the same name - REJECT
                 System.err.println("❌ DUPLICATE NAME DETECTED - Blocking update");
                 session.setAttribute("errorMessage", 
                     "Cannot update: A leave type named '" + leaveTypeName.trim() + 
                     "' already exists in the system. Please choose a different name.");
                 response.sendRedirect(request.getContextPath() + "/AdminLeaveTypeListController");
                 return;
             } else {
                 // Same leave type - user kept the same name (possibly changed case or added/removed spaces)
                 System.out.println("✅ Same leave type - name unchanged or formatting changed only");
             }
         } else {
             System.out.println("✅ No duplicate found - name is unique");
         }
            
            if (leaveTypeDescription == null || leaveTypeDescription.trim().isEmpty()) {
                System.err.println("❌ Leave type description is required");
                session.setAttribute("errorMessage", "Leave type description is required");
                response.sendRedirect(request.getContextPath() + "/AdminLeaveTypeListController");
                return;
            }
            
            // Update LeaveType (ONLY editable fields + read-only fields from hidden inputs)
            LeaveType updateLeaveType = new LeaveType();
            updateLeaveType.setLeaveTypeId(leaveTypeId);
            updateLeaveType.setLeaveTypeCategory(leaveType); // From hidden field
            updateLeaveType.setLeaveTypeName(leaveTypeName.trim());
            updateLeaveType.setLeaveTypeDescription(leaveTypeDescription.trim());
            updateLeaveType.setRequiresDocument(requiresDocument);
            updateLeaveType.setAffectsBalance(affectsBalance); // From hidden field
            updateLeaveType.setFixedDuration(fixedDuration);
            
            System.out.println("=== Updating LeaveType in database ===");
            boolean updated = leaveTypeDAO.updateLeaveType(updateLeaveType);
            
            if (updated) {
                System.out.println("✅ Leave type updated successfully in database");
                
                // Update child table records based on category
                // NOTE: We don't create/delete records since category is read-only
                // We only update existing records
                
                if ("Full Day".equals(leaveType)) {
                    System.out.println("=== Updating Full Day configuration (read-only duration) ===");
                    // Standard duration is read-only, but we ensure consistency
                    FullDay existingFullDay = fullDayDAO.getFullDayByLeaveTypeId(leaveTypeId);
                    if (existingFullDay != null && standardDuration != null) {
                        existingFullDay.setStandardDuration(standardDuration);
                        fullDayDAO.updateFullDay(existingFullDay);
                    }
                    
                } else if ("Half Day".equals(leaveType)) {
                    System.out.println("=== Updating Half Day configuration ===");
                    HalfDay existingHalfDay = halfDayDAO.getHalfDayByLeaveTypeId(leaveTypeId);
                    
                    if (existingHalfDay != null) {
                        existingHalfDay.setShift(halfDayShift);
                        boolean halfDayUpdated = halfDayDAO.updateHalfDay(existingHalfDay);
                        System.out.println("HalfDay shift updated: " + halfDayUpdated);
                    }
                    
                } else if ("Both".equals(leaveType)) {
                    System.out.println("=== Updating Both (Full Day and Half Day) configuration ===");
                    
                    // Update FullDay (standard duration is read-only)
                    FullDay existingFullDay = fullDayDAO.getFullDayByLeaveTypeId(leaveTypeId);
                    if (existingFullDay != null && standardDuration != null) {
                        existingFullDay.setStandardDuration(standardDuration);
                        fullDayDAO.updateFullDay(existingFullDay);
                    }
                    
                    // Update HalfDay shift (editable)
                    HalfDay existingHalfDay = halfDayDAO.getHalfDayByLeaveTypeId(leaveTypeId);
                    if (existingHalfDay != null) {
                        existingHalfDay.setShift(halfDayShift);
                        boolean halfDayUpdated = halfDayDAO.updateHalfDay(existingHalfDay);
                        System.out.println("HalfDay shift updated: " + halfDayUpdated);
                    }
                }
                
                // Build success message with bullet points
                StringBuilder successMessage = new StringBuilder();
                successMessage.append("<strong>Leave type '").append(leaveTypeName).append("' updated successfully!</strong><br><br>");
                successMessage.append("<ul style='text-align: left; padding-left: 20px; margin: 0;'>");

                // Document requirement
                if (requiresDocument) {
                    successMessage.append("<li>Supporting documents <strong>required</strong></li>");
                } else {
                    successMessage.append("<li>Supporting documents <strong>optional</strong></li>");
                }

                // Balance tracking (read-only, but shown for confirmation)
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

                // Add category-specific info (read-only)
                if ("Full Day".equals(leaveType)) {
                    if (standardDuration != null && standardDuration > 0) {
                        successMessage.append("<li>Standard duration: <strong>").append(standardDuration).append(" day").append(standardDuration > 1 ? "s" : "").append("</strong></li>");
                    }
                } else if ("Half Day".equals(leaveType)) {
                    if ("Morning".equals(halfDayShift)) {
                        successMessage.append("<li>Available shift: <strong>Morning only</strong></li>");
                    } else if ("Afternoon".equals(halfDayShift)) {
                        successMessage.append("<li>Available shift: <strong>Afternoon only</strong></li>");
                    } else if ("Both".equals(halfDayShift)) {
                        successMessage.append("<li>Available shifts: <strong>Morning & Afternoon</strong></li>");
                    }
                } else if ("Both".equals(leaveType)) {
                    successMessage.append("<li>Flexible: <strong>Full day or half day</strong></li>");
                    
                    if (standardDuration != null && standardDuration > 0) {
                        successMessage.append("<li>Full day duration: <strong>").append(standardDuration).append(" day").append(standardDuration > 1 ? "s" : "").append("</strong></li>");
                    }
                    
                    if ("Morning".equals(halfDayShift)) {
                        successMessage.append("<li>Half day shift: <strong>Morning only</strong></li>");
                    } else if ("Afternoon".equals(halfDayShift)) {
                        successMessage.append("<li>Half day shift: <strong>Afternoon only</strong></li>");
                    } else if ("Both".equals(halfDayShift)) {
                        successMessage.append("<li>Half day shifts: <strong>Morning & Afternoon</strong></li>");
                    }
                }

                successMessage.append("</ul>");

                System.out.println("✅ Success message: " + successMessage.toString());
                session.setAttribute("successMessage", successMessage.toString());
                
            } else {
                System.err.println("❌ Failed to update leave type in database");
                session.setAttribute("errorMessage", "Failed to update leave type. Please try again.");
            }
            
            response.sendRedirect(request.getContextPath() + "/AdminLeaveTypeListController");
            
        } catch (Exception e) {
            System.err.println("❌ Exception in updateLeaveType: " + e.getMessage());
            e.printStackTrace();
            session.setAttribute("errorMessage", "An error occurred while updating: " + e.getMessage());
            response.sendRedirect(request.getContextPath() + "/AdminLeaveTypeListController");
        }
    }
    
    /**
     * Helper method to escape JSON strings
     * Prevents JSON injection attacks
     */
    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        
        return value
            .replace("\\", "\\\\")  // Escape backslash
            .replace("\"", "\\\"")  // Escape double quotes
            .replace("\n", "\\n")   // Escape newline
            .replace("\r", "\\r")   // Escape carriage return
            .replace("\t", "\\t")   // Escape tab
            .replace("\b", "\\b")   // Escape backspace
            .replace("\f", "\\f");  // Escape form feed
    }
}
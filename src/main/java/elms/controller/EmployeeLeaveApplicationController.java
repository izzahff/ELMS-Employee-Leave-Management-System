package elms.controller;

import elms.DAO.LeaveTypeDAO;
import elms.DAO.PublicHolidayDAO;
import elms.DAO.FullDayDAO;
import elms.DAO.HalfDayDAO;
import elms.DAO.LeaveApplicationDAO;
import elms.DAO.LeaveBalanceDAO;
import elms.model.LeaveType;
import elms.model.FullDay;
import elms.model.HalfDay;
import elms.model.LeaveApplication;
import elms.model.Employee;

import java.io.IOException;
import java.io.File;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import java.sql.SQLException;
import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Date;
import java.util.Calendar;



/**
 * Updated Leave Application Controller with Flexible Duration Support and Fixed Duration
 * - Employees choose leave type first
 * - Duration selection shown based on leave type category
 * - Fixed duration leave types automatically set end date
 */
@WebServlet("/LeaveApplicationController")
@MultipartConfig(
    fileSizeThreshold = 1024 * 1024 * 2,
    maxFileSize = 1024 * 1024 * 10,
    maxRequestSize = 1024 * 1024 * 50
)
public class EmployeeLeaveApplicationController extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private LeaveTypeDAO leaveTypeDAO;
    private FullDayDAO fullDayDAO;
    private HalfDayDAO halfDayDAO;
    private LeaveApplicationDAO leaveApplicationDAO;
    
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    
    @Override
    public void init() throws ServletException {
        super.init();
        System.out.println("=== INITIALIZING LEAVE APPLICATION CONTROLLER ===");
        
        try {
            leaveTypeDAO = new LeaveTypeDAO();
            fullDayDAO = new FullDayDAO();
            halfDayDAO = new HalfDayDAO();
            leaveApplicationDAO = new LeaveApplicationDAO();
            
            System.out.println("✅ All DAOs initialized successfully");
            
        } catch (Exception e) {
            System.err.println("❌ Error initializing DAOs: " + e.getMessage());
            e.printStackTrace();
            throw new ServletException("Failed to initialize DAOs", e);
        }
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        System.out.println("=== LEAVE APPLICATION CONTROLLER GET REQUEST ===");
        
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendRedirect(request.getContextPath() + "/Employee/EmployeeLogin.jsp");
            return;
        }
        
        Employee employee = (Employee) session.getAttribute("loggedInEmployee");
        if (employee == null) {
            response.sendRedirect(request.getContextPath() + "/Employee/EmployeeLogin.jsp");
            return;
        }
        
        try {
            // Load ALL leave types with their configurations
            loadAllLeaveTypesForForm(request);
            
            // ✅ ADD THIS - Get unavailable dates for this employee
            List<Map<String, String>> unavailableDates = leaveApplicationDAO.getUnavailableDatesForEmployee(employee.getEmployeeId());
            
            // Convert to JSON for JavaScript
            String unavailableDatesJson = createUnavailableDatesJson(unavailableDates);
            request.setAttribute("unavailableDatesJson", unavailableDatesJson);
            
            PublicHolidayDAO holidayDAO = new PublicHolidayDAO();
            int currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
            List<String> publicHolidays = holidayDAO.getPublicHolidaysForYears(currentYear, currentYear + 1, currentYear + 2);
            String publicHolidaysJson = createPublicHolidaysJson(publicHolidays);
            request.setAttribute("publicHolidaysJson", publicHolidaysJson);
            
            System.out.println("✅ Leave types and unavailable dates loaded successfully");
            
            request.getRequestDispatcher("/Employee/EmployeeLeaveApplication.jsp").forward(request, response);
            
        } catch (Exception e) {
            System.err.println("❌ Error loading leave application form: " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("errorMessage", "An error occurred while loading the form: " + e.getMessage());
            request.getRequestDispatcher("/Employee/EmployeeLeaveApplication.jsp").forward(request, response);
        }
    }
    
    private String createPublicHolidaysJson(List<String> holidays) {
        if (holidays == null || holidays.isEmpty()) {
            return "[]";
        }
        
        StringBuilder json = new StringBuilder();
        json.append("[");
        
        for (int i = 0; i < holidays.size(); i++) {
            if (i > 0) json.append(",");
            json.append("\"").append(escapeJsonString(holidays.get(i))).append("\"");
        }
        
        json.append("]");
        return json.toString();
    }
    
    /**
     * Create JSON string for unavailable dates
     */
    private String createUnavailableDatesJson(List<Map<String, String>> unavailableDates) {
        if (unavailableDates == null || unavailableDates.isEmpty()) {
            return "[]";
        }
        
        StringBuilder json = new StringBuilder();
        json.append("[");
        
        for (int i = 0; i < unavailableDates.size(); i++) {
            if (i > 0) json.append(",");
            
            Map<String, String> dateRange = unavailableDates.get(i);
            json.append("{");
            json.append("\"startDate\":\"").append(escapeJsonString(dateRange.get("startDate"))).append("\",");
            json.append("\"endDate\":\"").append(escapeJsonString(dateRange.get("endDate"))).append("\",");
            json.append("\"status\":\"").append(escapeJsonString(dateRange.get("status"))).append("\",");
            json.append("\"applicationId\":\"").append(escapeJsonString(dateRange.get("applicationId"))).append("\",");
            json.append("\"duration\":\"").append(escapeJsonString(dateRange.get("duration"))).append("\"");
            json.append("}");
        }
        
        json.append("]");
        return json.toString();
    }

    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        System.out.println("=== LEAVE APPLICATION CONTROLLER POST REQUEST ===");
        
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendRedirect(request.getContextPath() + "/Employee/EmployeeLogin.jsp");
            return;
        }
        
        Employee employee = (Employee) session.getAttribute("loggedInEmployee");
        if (employee == null) {
            response.sendRedirect(request.getContextPath() + "/Employee/EmployeeLogin.jsp");
            return;
        }
        
        try {
            String result = processLeaveApplication(request, employee);
            
            if (result.startsWith("success:")) {
                request.setAttribute("successMessage", result.substring(8));
                System.out.println("✅ Leave application submitted successfully");
                
            } else if (result.startsWith("error:")) {
                request.setAttribute("errorMessage", result.substring(6));
                System.out.println("❌ Leave application submission failed: " + result);
                
            } else {
                request.setAttribute("warningMessage", result);
                System.out.println("⚠️ Leave application submission warning: " + result);
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error processing leave application: " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("errorMessage", "An unexpected error occurred: " + e.getMessage());
        }
        
        try {
            loadAllLeaveTypesForForm(request);
        } catch (Exception e) {
            System.err.println("❌ Error reloading leave types: " + e.getMessage());
        }
        
        request.getRequestDispatcher("/Employee/EmployeeLeaveApplication.jsp").forward(request, response);
    }
    
    /**
     * Load all leave types and prepare data for JavaScript
     * UPDATED: Now properly handles the new leaveTypeCategory structure and fixed duration
     */
    private void loadAllLeaveTypesForForm(HttpServletRequest request) throws SQLException {
        System.out.println("=== LOADING ALL LEAVE TYPES FOR FORM ===");
        
        // Get ALL leave types from database
        List<LeaveType> allLeaveTypes = leaveTypeDAO.getAllLeaveTypes();
        System.out.println("📊 Found " + allLeaveTypes.size() + " total leave types");
        
        // Separate into Full Day, Half Day, and Both lists
        List<Map<String, Object>> fullDayTypesForJson = new ArrayList<>();
        List<Map<String, Object>> halfDayTypesForJson = new ArrayList<>();
        List<Map<String, Object>> bothTypesForJson = new ArrayList<>();
        
        for (LeaveType leaveType : allLeaveTypes) {
            String category = leaveType.getLeaveTypeCategory();
            
            System.out.println("  📋 Processing: " + leaveType.getLeaveTypeName() + " - Category: " + category);
            
            Map<String, Object> typeMap = new HashMap<>();
            typeMap.put("leaveTypeId", leaveType.getLeaveTypeId());
            typeMap.put("leaveTypeName", leaveType.getLeaveTypeName());
            typeMap.put("leaveTypeDescription", leaveType.getLeaveTypeDescription());
            typeMap.put("leaveTypeCategory", category);
            typeMap.put("requiresDocument", leaveType.isRequiresDocument());
            typeMap.put("affectsBalance", leaveType.isAffectsBalance());
            typeMap.put("fixedDuration", leaveType.isFixedDuration());
            
            // Get Full Day details if applicable
            if ("Full Day".equals(category) || "Both".equals(category)) {
                FullDay fullDayDetail = fullDayDAO.getFullDayByLeaveTypeId(leaveType.getLeaveTypeId());
                if (fullDayDetail != null) {
                    typeMap.put("standardDuration", fullDayDetail.getStandardDuration());
                    System.out.println("    📅 Standard Duration: " + fullDayDetail.getStandardDuration());
                } else {
                    typeMap.put("standardDuration", 0);
                }
            }
            
            // Get Half Day details if applicable
            if ("Half Day".equals(category) || "Both".equals(category)) {
                HalfDay halfDayConfig = halfDayDAO.getHalfDayByLeaveTypeId(leaveType.getLeaveTypeId());
                if (halfDayConfig != null) {
                    String availableShifts = halfDayConfig.getShift();
                    if (availableShifts == null || availableShifts.isEmpty()) {
                        availableShifts = "Both";
                    }
                    typeMap.put("availableShifts", availableShifts);
                    System.out.println("    🌓 Available Shifts: " + availableShifts);
                } else {
                    typeMap.put("availableShifts", "Both");
                }
            }
            
            // Add to appropriate list based on category
            if ("Full Day".equals(category)) {
                fullDayTypesForJson.add(typeMap);
            } else if ("Half Day".equals(category)) {
                halfDayTypesForJson.add(typeMap);
            } else if ("Both".equals(category)) {
                bothTypesForJson.add(typeMap);
            }
        }
        
        // Convert to JSON
        String fullDayTypesJson = createLeaveTypesJson(fullDayTypesForJson);
        String halfDayTypesJson = createLeaveTypesJson(halfDayTypesForJson);
        String bothTypesJson = createLeaveTypesJson(bothTypesForJson);
        
        System.out.println("📄 Full Day Types JSON created with " + fullDayTypesForJson.size() + " items");
        System.out.println("📄 Half Day Types JSON created with " + halfDayTypesForJson.size() + " items");
        System.out.println("📄 Both Types JSON created with " + bothTypesForJson.size() + " items");
        
        request.setAttribute("fullDayTypesJson", fullDayTypesJson);
        request.setAttribute("halfDayTypesJson", halfDayTypesJson);
        request.setAttribute("bothTypesJson", bothTypesJson);
        
        System.out.println("✅ Successfully loaded leave types");
        System.out.println("   Full Day types: " + fullDayTypesForJson.size());
        System.out.println("   Half Day types: " + halfDayTypesForJson.size());
        System.out.println("   Both types: " + bothTypesForJson.size());
    }
    
    /**
     * Create JSON string for leave types
     * UPDATED: Added fixedDuration field
     */
    private String createLeaveTypesJson(List<Map<String, Object>> data) {
        if (data == null || data.isEmpty()) {
            return "[]";
        }
        
        StringBuilder json = new StringBuilder();
        json.append("[");
        
        for (int i = 0; i < data.size(); i++) {
            if (i > 0) json.append(",");
            
            Map<String, Object> item = data.get(i);
            json.append("{");
            json.append("\"leaveTypeId\":\"").append(escapeJsonString(item.get("leaveTypeId"))).append("\",");
            json.append("\"leaveTypeName\":\"").append(escapeJsonString(item.get("leaveTypeName"))).append("\",");
            json.append("\"leaveTypeDescription\":\"").append(escapeJsonString(item.get("leaveTypeDescription"))).append("\",");
            json.append("\"leaveTypeCategory\":\"").append(escapeJsonString(item.get("leaveTypeCategory"))).append("\",");
            json.append("\"requiresDocument\":").append(item.get("requiresDocument") != null && (Boolean) item.get("requiresDocument")).append(",");
            json.append("\"affectsBalance\":").append(item.get("affectsBalance") != null && (Boolean) item.get("affectsBalance"));
            
            // Add fixedDuration
            if (item.containsKey("fixedDuration")) {
                json.append(",\"fixedDuration\":").append(item.get("fixedDuration") != null && (Boolean) item.get("fixedDuration"));
            }
            
            // Add standardDuration if present
            if (item.containsKey("standardDuration")) {
                json.append(",\"standardDuration\":").append(item.get("standardDuration"));
            }
            
            // Add availableShifts if present
            if (item.containsKey("availableShifts")) {
                json.append(",\"availableShifts\":\"").append(escapeJsonString(item.get("availableShifts"))).append("\"");
            }
            
            json.append("}");
        }
        
        json.append("]");
        return json.toString();
    }
    
    private String escapeJsonString(Object value) {
        if (value == null) return "";
        
        String str = value.toString();
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
    
    /**
     * Process leave application with flexible duration support and fixed duration validation
     */
    private String processLeaveApplication(HttpServletRequest request, Employee employee) throws SQLException {
        System.out.println("=== PROCESSING LEAVE APPLICATION ===");
        
        try {
            String employeeId = request.getParameter("employeeid");
            String leaveTypeId = request.getParameter("leavetypeid");
            String durationType = request.getParameter("leavetype"); // This will be the selected duration
            String shift = request.getParameter("shift");
            String startDate = request.getParameter("leavestartdate");
            String endDate = request.getParameter("leaveenddate");
            String leaveDurationStr = request.getParameter("leaveduration");
            String leaveReason = request.getParameter("leavereason");
            
            System.out.println("📋 Form Parameters:");
            System.out.println("  Employee ID: " + employeeId);
            System.out.println("  Leave Type ID: " + leaveTypeId);
            System.out.println("  Duration Type: " + durationType);
            System.out.println("  Shift: " + shift);
            System.out.println("  Start Date: " + startDate);
            System.out.println("  End Date: " + endDate);
            System.out.println("  Leave Duration: " + leaveDurationStr);
            
            // Validate required fields
            if (leaveTypeId == null || leaveTypeId.trim().isEmpty()) {
                return "error: Please select a leave type";
            }
            
            if (startDate == null || startDate.trim().isEmpty()) {
                return "error: Please select a start date";
            }
            
            // ✅ Validate start date is not a public holiday
            PublicHolidayDAO holidayDAO = new PublicHolidayDAO();
            int currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
            List<String> publicHolidays = holidayDAO.getPublicHolidaysForYears(currentYear, currentYear + 1, currentYear + 2);
            
            if (publicHolidays.contains(startDate)) {
                return "error: Cannot apply for leave starting on a public holiday (" + startDate + "). Please select a working day.";
            }
            
            // Get leave type details
            LeaveType selectedLeaveType = leaveTypeDAO.getLeaveTypeById(leaveTypeId);
            if (selectedLeaveType == null) {
                return "error: Invalid leave type selected";
            }
            
            String leaveTypeCategory = selectedLeaveType.getLeaveTypeCategory();
            System.out.println("📋 Leave Type Category: " + leaveTypeCategory);
            
            // Determine actual duration type
            String actualDurationType;
            if ("Both".equals(leaveTypeCategory)) {
                // For "Both" types, use the selected durationType
                if (durationType == null || durationType.trim().isEmpty()) {
                    return "error: Please select full day or half day";
                }
                actualDurationType = durationType;
            } else {
                // For specific types, use the leave type category
                actualDurationType = leaveTypeCategory;
            }
            
            System.out.println("📋 Actual Duration Type: " + actualDurationType);
            
            // Validate based on actual duration type
            double leaveDuration;
            if ("Half Day".equals(actualDurationType)) {
                // Half day validation
                leaveDuration = 0.5;
                endDate = startDate; // Half day: start and end are the same
                
                if (shift == null || shift.trim().isEmpty()) {
                    return "error: Please select a shift for half day leave";
                }
                
                System.out.println("✅ Half day leave: 0.5 days");
            } else {
                // Full day validation
                if (endDate == null || endDate.trim().isEmpty()) {
                    return "error: Please select an end date for full day leave";
                }
                
                // ✅ Validate end date is not a public holiday
                if (publicHolidays.contains(endDate)) {
                    return "error: Cannot apply for leave ending on a public holiday (" + endDate + "). Please select a working day.";
                }
                
                try {
                    leaveDuration = Double.parseDouble(leaveDurationStr);
                } catch (NumberFormatException e) {
                    return "error: Invalid leave duration format";
                }
                
                if (leaveDuration <= 0) {
                    return "error: Leave duration must be greater than 0";
                }
                
                System.out.println("✅ Full day leave: " + leaveDuration + " days");
            }
            
            // Date validation
            try {
                if (!isValidDateRange(startDate, endDate)) {
                    return "error: End date must be on or after start date";
                }
                
            } catch (ParseException e) {
                return "error: Invalid date format";
            }
            
            System.out.println("🔍 Checking for overlapping leave applications...");
            try {
                List<LeaveApplication> overlappingApps = leaveApplicationDAO.checkOverlappingLeaveApplications(
                    employeeId, startDate, endDate, null
                );
                
                if (!overlappingApps.isEmpty()) {
                    System.out.println("⚠️ Overlap detected! Found " + overlappingApps.size() + " overlapping application(s)");
                    
                    // Build detailed error message
                    StringBuilder errorMsg = new StringBuilder();
                    errorMsg.append("Cannot apply for leave - you already have leave application(s) for overlapping dates:");
                    
                    for (LeaveApplication overlap : overlappingApps) {
                        LeaveType overlapLeaveType = leaveTypeDAO.getLeaveTypeById(overlap.getLeavetypeid());
                        String leaveTypeName = overlapLeaveType != null ? overlapLeaveType.getLeaveTypeName() : "Unknown";
                        
                        errorMsg.append("\n• ").append(leaveTypeName)
                                .append(" (").append(overlap.getLeavestatus()).append(")")
                                .append(" from ").append(overlap.getLeavestartdate())
                                .append(" to ").append(overlap.getLeaveenddate());
                        
                        if (overlap.getLeaveduration() == 0.5) {
                            errorMsg.append(" - Half Day");
                        }
                    }
                    
                    errorMsg.append("\n\nPlease select different dates or cancel the existing application first.");
                    
                    return "error: " + errorMsg.toString();
                }
                
                System.out.println("✅ No overlapping applications found - proceeding with submission");
                
            } catch (SQLException e) {
                System.err.println("❌ Error checking for overlaps: " + e.getMessage());
                e.printStackTrace();
                return "error: Unable to verify leave dates. Please try again.";
            }

            
            // Check if document is required
            boolean documentRequired = selectedLeaveType.isRequiresDocument();
            String attachmentPath = null;
            
            try {
                Part filePart = request.getPart("attachments");
                
                if (documentRequired && (filePart == null || filePart.getSize() == 0)) {
                    return "error: Supporting document is required for " + selectedLeaveType.getLeaveTypeName();
                }
                
                if (filePart != null && filePart.getSize() > 0) {
                    attachmentPath = saveUploadedFile(filePart, employeeId);
                    System.out.println("✅ File uploaded: " + attachmentPath);
                }
            } catch (Exception e) {
                return "error: File upload failed: " + e.getMessage();
            }
            
            // Check balance if leave affects balance
            boolean affectsBalance = selectedLeaveType.isAffectsBalance();

            if (affectsBalance) {
                // Calculate balance for THIS SPECIFIC leave type
                double currentBalance = LeaveBalanceDAO.calculateBalanceForLeaveType(employeeId, leaveTypeId);
                
                System.out.println("💰 Balance Check for Leave Type: " + leaveTypeId);
                System.out.println("  Leave Type: " + selectedLeaveType.getLeaveTypeName());
                System.out.println("  Current Balance: " + currentBalance);
                System.out.println("  Requested Days: " + leaveDuration);
                
                if (leaveDuration > currentBalance) {
                    return "error: Insufficient balance for " + selectedLeaveType.getLeaveTypeName() + 
                           ". Available: " + String.format("%.1f", currentBalance) + 
                           " days, Requested: " + leaveDuration + " days";
                }
            }
            
            // Validate fixed duration requirement
            boolean isFixedDuration = selectedLeaveType.isFixedDuration();
            if (isFixedDuration && "Full Day".equals(actualDurationType)) {
                // Get the standard duration for this leave type
                FullDay fullDayConfig = fullDayDAO.getFullDayByLeaveTypeId(leaveTypeId);
                if (fullDayConfig != null && fullDayConfig.getStandardDuration() > 0) {
                    int requiredDuration = fullDayConfig.getStandardDuration();
                    if (leaveDuration != requiredDuration) {
                        return "error: " + selectedLeaveType.getLeaveTypeName() + 
                               " requires exactly " + requiredDuration + " days. System calculated " + 
                               leaveDuration + " days. Please contact HR if you believe this is incorrect.";
                    }
                    System.out.println("✅ Fixed duration validated: " + requiredDuration + " days");
                }
            }
            
            // Create leave application
            LeaveApplication leaveApplication = new LeaveApplication();
            leaveApplication.setEmployeeid(employeeId);
            leaveApplication.setLeavetypeid(leaveTypeId);
            leaveApplication.setLeavestartdate(startDate);
            leaveApplication.setLeaveenddate(endDate);
            leaveApplication.setLeavestatus("Pending");
            leaveApplication.setLeavereason(leaveReason != null ? leaveReason.trim() : "");
            leaveApplication.setLeaveduration(leaveDuration);
            leaveApplication.setAttachment(attachmentPath);
            
            String createResult = leaveApplicationDAO.createLeaveApplication(leaveApplication, shift);
            
            if (createResult.startsWith("success:")) {
                String applicationId = extractApplicationIdFromResult(createResult);
                leaveApplication.setApplicationid(applicationId);
                
                // Notify managers
                notifyAllManagersOfNewApplication(leaveApplication);
                
                String successMessage = selectedLeaveType.getLeaveTypeName() + " application submitted successfully. ";
                
                if (affectsBalance) {
                    if (leaveDuration == 0.5) {
                        successMessage += "0.5 days will be deducted from your balance upon approval.";
                    } else {
                        successMessage += leaveDuration + " days will be deducted from your balance upon approval.";
                    }
                } else {
                    successMessage += "No balance deduction applies to this leave type.";
                }
                
                return "success: " + successMessage;
                
            } else {
                return createResult;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Database error: " + e.getMessage());
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            System.err.println("❌ Unexpected error: " + e.getMessage());
            e.printStackTrace();
            return "error: An unexpected error occurred";
        }
    }
    
    private boolean isDateInPast(String dateStr) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        sdf.setLenient(false);
        Date inputDate = sdf.parse(dateStr);
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        return inputDate.before(today.getTime());
    }
    
    private boolean isValidDateRange(String startDateStr, String endDateStr) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        sdf.setLenient(false);
        Date startDate = sdf.parse(startDateStr);
        Date endDate = sdf.parse(endDateStr);
        return !endDate.before(startDate);
    }
    
    private String saveUploadedFile(Part filePart, String employeeId) throws Exception {
        String originalFilename = getSubmittedFileName(filePart);
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String cleanOriginalName = originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_");
        String uniqueFilename = "emp_" + employeeId + "_" + timestamp + "_" + cleanOriginalName;
        
        String uploadBaseDir = getServletContext().getRealPath("/uploads/attachments");
        String yearMonth = new SimpleDateFormat("yyyy/MM").format(new Date());
        String uploadDir = uploadBaseDir + File.separator + yearMonth;
        
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        
        String fullFilePath = uploadDir + File.separator + uniqueFilename;
        filePart.write(fullFilePath);
        
        return uniqueFilename;
    }
    
    private String getSubmittedFileName(Part part) {
        String contentDisposition = part.getHeader("content-disposition");
        if (contentDisposition == null) return null;
        
        for (String content : contentDisposition.split(";")) {
            if (content.trim().startsWith("filename")) {
                String filename = content.substring(content.indexOf('=') + 1).trim();
                filename = filename.replace("\"", "");
                return filename.isEmpty() ? null : filename;
            }
        }
        return null;
    }
    
    private String extractApplicationIdFromResult(String result) {
        String[] words = result.split("\\s+");
        for (int i = words.length - 1; i >= 0; i--) {
            if (words[i].matches("\\d+")) {
                return words[i];
            }
        }
        return null;
    }
    
    private void notifyAllManagersOfNewApplication(LeaveApplication leaveApplication) {
        try {
            String employeeId = leaveApplication.getEmployeeid();
            String applicationId = leaveApplication.getApplicationid();
            
            // ✅ ASYNC: Returns immediately without waiting for emails
            ManagerNotificationController.notifyAllManagersOfNewApplicationAsync(applicationId, employeeId);
            System.out.println("✅ Manager notification queued (emails sending in background)");
            
        } catch (Exception e) {
            System.err.println("❌ Error queuing manager notifications: " + e.getMessage());
            // Don't throw - we don't want email failures to break leave submissions
        }
    }
}
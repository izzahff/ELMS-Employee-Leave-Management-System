package elms.controller;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import elms.DAO.EmployeeDAO;
import elms.DAO.LeaveApplicationDAO;
import elms.DAO.LeaveTypeDAO;
import elms.DAO.ReportDAO;
import elms.model.Employee;
import elms.model.LeaveApplication;
import elms.model.LeaveType;

@WebServlet("/admin-leave-reports")
public class AdminLeaveReportsController extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private LeaveApplicationDAO leaveApplicationDAO;
    private LeaveTypeDAO leaveTypeDAO;
    private EmployeeDAO employeeDAO;
    private ReportDAO reportDAO;
    
    @Override
    public void init() throws ServletException {
        leaveApplicationDAO = new LeaveApplicationDAO();
        leaveTypeDAO = new LeaveTypeDAO();
        employeeDAO = new EmployeeDAO();
        reportDAO = new ReportDAO();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession();
        String adminId = (String) session.getAttribute("adminId");
        
        // Check if admin is logged in
        if (adminId == null) {
            response.sendRedirect(request.getContextPath() + "/Admin/AdminLogin.jsp");
            return;
        }
        
        try {
            System.out.println("=== ADMIN LEAVE REPORTS CONTROLLER START ===");
            
            // Check if this is a report generation request (has generate parameter)
            String generateReport = request.getParameter("generate");
            boolean shouldGenerateReport = "true".equals(generateReport);
            
            // Get filter parameters
            String reportType = request.getParameter("reportType");
            String dateRange = request.getParameter("dateRange");
            String fromDateStr = request.getParameter("fromDate");
            String toDateStr = request.getParameter("toDate");
            String statusFilter = request.getParameter("status");
            String leaveTypeFilter = request.getParameter("leaveType");
            String employeeFilter = request.getParameter("employee");
            
            System.out.println("Filter Parameters:");
            System.out.println("- Generate Report: " + shouldGenerateReport);
            System.out.println("- Report Type: " + reportType);
            System.out.println("- Date Range: " + dateRange);
            System.out.println("- From Date: " + fromDateStr);
            System.out.println("- To Date: " + toDateStr);
            System.out.println("- Status Filter: " + statusFilter);
            System.out.println("- Leave Type Filter: " + leaveTypeFilter);
            System.out.println("- Employee Filter: " + employeeFilter);
            
            // Set default report type if not specified
            if (reportType == null || reportType.trim().isEmpty()) {
                reportType = "summary";
            }
            
            // Validate report type
            reportType = validateReportType(reportType);
            
            System.out.println("Validated Report Type: " + reportType);
            
            // Calculate date range - only apply if specifically requested
            Date fromDate = null;
            Date toDate = null;
            
            if ("custom".equals(dateRange)) {
                
                if (fromDateStr != null && !fromDateStr.trim().isEmpty()) {
                    fromDate = parseDate(fromDateStr);
                }
                if (toDateStr != null && !toDateStr.trim().isEmpty()) {
                    toDate = parseDate(toDateStr);
                }
            } else if (dateRange != null && !dateRange.trim().isEmpty() && !"".equals(dateRange)) {
                // Calculate predefined date ranges only if explicitly selected
                Calendar cal = Calendar.getInstance();
                toDate = cal.getTime(); // Today
                
                switch (dateRange) {
                    case "last30":
                        cal.add(Calendar.DAY_OF_MONTH, -30);
                        fromDate = cal.getTime();
                        break;
                    case "last90":
                        cal.add(Calendar.DAY_OF_MONTH, -90);
                        fromDate = cal.getTime();
                        break;
                    case "thisMonth":
                        cal.set(Calendar.DAY_OF_MONTH, 1);
                        fromDate = cal.getTime();
                        break;
                    case "lastMonth":
                        cal.add(Calendar.MONTH, -1);
                        cal.set(Calendar.DAY_OF_MONTH, 1);
                        fromDate = cal.getTime();
                        cal.add(Calendar.MONTH, 1);
                        cal.set(Calendar.DAY_OF_MONTH, 0); // Last day of previous month
                        toDate = cal.getTime();
                        break;
                    case "thisQuarter":
                        int currentMonth = cal.get(Calendar.MONTH);
                        int quarterStartMonth = (currentMonth / 3) * 3;
                        cal.set(Calendar.MONTH, quarterStartMonth);
                        cal.set(Calendar.DAY_OF_MONTH, 1);
                        fromDate = cal.getTime();
                        cal = Calendar.getInstance(); // Reset to today
                        toDate = cal.getTime();
                        break;
                    case "thisYear":
                        cal.set(Calendar.MONTH, 0);
                        cal.set(Calendar.DAY_OF_MONTH, 1);
                        fromDate = cal.getTime();
                        cal = Calendar.getInstance(); // Reset to today
                        toDate = cal.getTime();
                        break;
                }
            }
           
            
            System.out.println("Calculated Date Range: " + fromDate + " to " + toDate);
            
            // Get all leave applications
            System.out.println("=== RETRIEVING LEAVE APPLICATIONS ===");
            List<LeaveApplication> allApplications;
            
            try {
                // Use the enhanced method that includes reject reason support
                allApplications = leaveApplicationDAO.getLeaveApplicationsWithFiltersAndReason(null, null, null);
                System.out.println("Total applications retrieved from DAO: " + allApplications.size());
            } catch (Exception e) {
                System.err.println("Error retrieving applications: " + e.getMessage());
                e.printStackTrace();
                allApplications = new ArrayList<>();
            }
            
           
            for (int i = 0; i < Math.min(3, allApplications.size()); i++) {
                LeaveApplication app = allApplications.get(i);
                System.out.println("App " + i + ": ID=" + app.getApplicationid() + 
                                 ", Employee=" + app.getEmployeeid() + 
                                 ", Status=" + app.getLeavestatus() +
                                 ", Applied=" + app.getAppliedon());
            }
            
            // Apply filters
            List<LeaveApplication> filteredApplications = filterApplications(
                allApplications, fromDate, toDate, statusFilter, leaveTypeFilter, employeeFilter);
            System.out.println("Applications after filtering: " + filteredApplications.size());
            
           
            String currentReportId = null;
            
            if (shouldGenerateReport) {if (shouldGenerateReport) {
                // User clicked "Generate Report" button - create new report ID
                try {
                    // ✅ CHECK IF THERE ARE ANY APPLICATIONS FIRST
                    if (filteredApplications.isEmpty()) {
                        System.out.println("⚠️ No applications found - skipping database report generation");
                        currentReportId = generateSimpleReportId(); // Generate temporary ID without DB
                        // ✅ NO MESSAGE - silently handle empty report
                    } else {
                        // Only store in database if there are applications
                        List<String> applicationIds = filteredApplications.stream()
                            .map(LeaveApplication::getApplicationid)
                            .collect(Collectors.toList());
                        
                        currentReportId = reportDAO.generateReport(applicationIds, reportType);
                        
                        // Set success message ONLY when there are applications
                        request.setAttribute("successMessage", "Report generated successfully! Report ID: " + currentReportId);
                    }
                    
                    // Store in session for reuse during exports
                    String reportConfigKey = createReportConfigKey(reportType, dateRange, fromDateStr, toDateStr, 
                                                                  statusFilter, leaveTypeFilter, employeeFilter);
                    session.setAttribute("reportId_" + reportConfigKey, currentReportId);
                    session.setAttribute("lastGeneratedReportId", currentReportId);
                    
                    System.out.println("✅ Report ID: " + currentReportId);
                    
                } catch (Exception e) {
                    System.err.println("❌ Database storage failed: " + e.getMessage());
                    e.printStackTrace(); // Log the full stack trace for debugging
                    // ✅ DON'T show error to user - silently fail
                    currentReportId = "ERROR_" + System.currentTimeMillis();
                }
            }} else {
                
                String reportConfigKey = createReportConfigKey(reportType, dateRange, fromDateStr, toDateStr, 
                                                              statusFilter, leaveTypeFilter, employeeFilter);
                currentReportId = (String) session.getAttribute("reportId_" + reportConfigKey);
                
                if (currentReportId != null) {
                    System.out.println("✅ REUSING: Existing report ID: " + currentReportId);
                    session.setAttribute("lastGeneratedReportId", currentReportId);
                } else {
                    System.out.println("ℹ️ No report generated yet for this configuration");
                    // Don't set any success/error message for filter changes
                }
            }
            
            // Set the report ID as an attribute for the JSP (can be null if no report generated)
            request.setAttribute("currentReportId", currentReportId);
            request.setAttribute("currentReportType", reportType);
           
            
            // Get additional data maps 
            Map<String, String> leaveTypeNames = getLeaveTypeNames();
            Map<String, String> employeeNames = getEmployeeNames();
            List<LeaveType> leaveTypes = leaveTypeDAO.getAllLeaveTypes();
            
            // Calculate statistics - use FILTERED applications for stats
            calculateAndSetStatistics(request, filteredApplications);
            
            // Process data based on report type
            processReportTypeData(request, reportType, filteredApplications, leaveTypeNames, employeeNames);
            
         // Calculate additional insights for bottom section (all report types)
            double avgProcessingTime = calculateAverageProcessingTime(filteredApplications);
            String mostPopularLeaveType = findMostPopularLeaveType(filteredApplications, leaveTypeNames);

            // Only show "Most Common Leave Type" if user hasn't filtered by a specific leave type
            boolean hasLeaveTypeFilter = (leaveTypeFilter != null && !leaveTypeFilter.trim().isEmpty());
            if (hasLeaveTypeFilter) {
                mostPopularLeaveType = null; // Don't show if filtered
            }

            request.setAttribute("avgProcessingTime", avgProcessingTime);
            request.setAttribute("mostPopularLeaveType", mostPopularLeaveType);
            request.setAttribute("hasLeaveTypeFilter", hasLeaveTypeFilter);
            
            request.setAttribute("currentReportId", currentReportId);
            request.setAttribute("currentReportType", reportType);
            
            System.out.println("=== SETTING JSP ATTRIBUTES ===");
            
            if (leaveTypeNames == null) {
                leaveTypeNames = new HashMap<>();
            }
            if (employeeNames == null) {
                employeeNames = new HashMap<>();
            }
            if (leaveTypes == null) {
                leaveTypes = new ArrayList<>();
            }
            
            // Set data for JSP
            request.setAttribute("reportData", filteredApplications);
            request.setAttribute("leaveTypeNames", leaveTypeNames);
            request.setAttribute("employeeNames", employeeNames);
            request.setAttribute("leaveTypes", leaveTypes);
            
            // Set filter values for form persistence
            request.setAttribute("selectedStatus", statusFilter);
            request.setAttribute("selectedLeaveType", leaveTypeFilter);
            request.setAttribute("selectedEmployee", employeeFilter);
            request.setAttribute("selectedReportType", reportType);
            request.setAttribute("selectedDateRange", dateRange);
            
            // Format dates for form inputs
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            if (fromDate != null) {
                request.setAttribute("selectedFromDate", dateFormat.format(fromDate));
            }
            if (toDate != null) {
                request.setAttribute("selectedToDate", dateFormat.format(toDate));
            }
            
            System.out.println("Final data validation:");
            System.out.println("- Applications: " + filteredApplications.size());
            System.out.println("- Leave types map: " + leaveTypeNames.size());
            System.out.println("- Employee names map: " + employeeNames.size());
            System.out.println("- Leave types list: " + leaveTypes.size());
            System.out.println("- Report ID: " + currentReportId);
            
            System.out.println("=== FORWARDING TO JSP ===");
            
            // Forward to JSP
            request.getRequestDispatcher("/Admin/AdminLeaveReports.jsp").forward(request, response);
            
        } catch (Exception e) {
            System.err.println("=== ERROR IN CONTROLLER ===");
            e.printStackTrace();
            
            // Check if response is already committed
            if (!response.isCommitted()) {
                request.setAttribute("errorMessage", "Error loading reports: " + e.getMessage());
                // Set empty data to prevent JSP errors
                request.setAttribute("reportData", new ArrayList<>());
                request.setAttribute("leaveTypeNames", new HashMap<>());
                request.setAttribute("employeeNames", new HashMap<>());
                request.setAttribute("leaveTypes", new ArrayList<>());
                request.setAttribute("totalApplications", 0);
                request.setAttribute("pendingApplications", 0);
                request.setAttribute("approvedApplications", 0);
                request.setAttribute("rejectedApplications", 0);
                request.setAttribute("cancelledApplications", 0);
                
                try {
                    request.getRequestDispatcher("/Admin/AdminLeaveReports.jsp").forward(request, response);
                } catch (Exception forwardException) {
                    System.err.println("Error forwarding to error page: " + forwardException.getMessage());
                    // If we can't forward, try to send a redirect instead
                    if (!response.isCommitted()) {
                        response.sendRedirect(request.getContextPath() + "/admin-leave-reports");
                    }
                }
            } else {
                System.err.println("Response already committed, cannot forward to error page");
            }
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String action = request.getParameter("action");
        
        if ("export".equals(action)) {
            // Use the updated export method that reuses existing report ID
            handleExportReport(request, response);
        } else {
            // Default to GET behavior for other POST requests
            doGet(request, response);
        }
    }
    
    /**
     * Handle export report functionality - Alternative approach without DB storage
     */
    private void handleExportReportDirect(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            String exportType = request.getParameter("exportType");
            String reportTypeForExport = request.getParameter("reportTypeForExport");
            
            System.out.println("=== DIRECT EXPORT (NO DB STORAGE) ===");
            System.out.println("Export Type: " + exportType);
            System.out.println("Report Type: " + reportTypeForExport);
            
            // Get the filtered applications based on the current filters
            List<LeaveApplication> applications = getFilteredApplicationsForExport(request);
            
            // Generate simple unique report ID for file naming
            String reportId = generateSimpleReportId();
            
            if ("csv".equals(exportType)) {
                exportToCSV(response, applications, reportId, reportTypeForExport);
            } else if ("pdf".equals(exportType)) {
                exportToPDF(response, applications, reportId, reportTypeForExport);
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid export type");
            }
            
        } catch (Exception e) {
            System.err.println("Error in direct export: " + e.getMessage());
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error generating export");
        }
    }
    
    private void handleExportReport(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        try {
            String exportType = request.getParameter("exportType");
            String reportTypeForExport = request.getParameter("reportTypeForExport");
            
            System.out.println("=== EXPORT REPORT REQUEST ===");
            System.out.println("Export Type: " + exportType);
            System.out.println("Report Type: " + reportTypeForExport);
            
            // Get the filtered applications
            List<LeaveApplication> applications = getFilteredApplicationsForExport(request);
            
            String reportId = null;
            HttpSession session = request.getSession();
            
            // TRY TO REUSE THE EXISTING REPORT ID FROM THE SESSION
            reportId = (String) session.getAttribute("lastGeneratedReportId");
            
            if (reportId != null) {
                System.out.println("✅ REUSING existing report ID for export: " + reportId);
            } else {
                // Fallback: Generate temporary ID for export only (don't store in database)
                reportId = generateSimpleReportId();
                System.out.println("⚠️ No existing report ID found, using temporary ID: " + reportId);
            }
            
            // Export using the report ID (without storing in database again)
            if ("csv".equals(exportType)) {
                exportToCSV(response, applications, reportId, reportTypeForExport);
            } else if ("pdf".equals(exportType)) {
                exportToPDF(response, applications, reportId, reportTypeForExport);
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid export type");
            }
            
        } catch (Exception e) {
            System.err.println("Error exporting report: " + e.getMessage());
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error generating export");
        }
    }
    
    /**
     * Updated createReportConfigKey method to include report type
     */
    private String createReportConfigKey(String reportType, String dateRange, String fromDate, String toDate,
            String status, String leaveType, String employee) {
        StringBuilder key = new StringBuilder();
        key.append(reportType != null ? reportType : "summary");
        key.append("_").append(dateRange != null ? dateRange : "");
        key.append("_").append(fromDate != null ? fromDate : "");
        key.append("_").append(toDate != null ? toDate : "");
        key.append("_").append(status != null ? status : "");
        key.append("_").append(leaveType != null ? leaveType : "");
        key.append("_").append(employee != null ? employee : "");
        
        return String.valueOf(Math.abs(key.toString().hashCode()));
    }
    
    /**
     * Generate a guaranteed unique report ID
     */
    private String generateUniqueReportId() {
        // Use timestamp + random number + thread ID for maximum uniqueness
        long timestamp = System.currentTimeMillis();
        int random = (int) (Math.random() * 9999);
        long threadId = Thread.currentThread().getId();
        
        String reportId = String.format("RPT%d_%04d_%d", timestamp, random, threadId);
        System.out.println("Generated unique report ID: " + reportId);
        return reportId;
    }
    
    /**
     * Generate simple unique report ID without database dependency
     */
    private String generateSimpleReportId() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String timestamp = dateFormat.format(new Date());
        int random = (int) (Math.random() * 999);
        return String.format("EXPORT_%s_%03d", timestamp, random);
    }
    
    /**
     * Get filtered applications for export (reuse filter logic)
     */
    private List<LeaveApplication> getFilteredApplicationsForExport(HttpServletRequest request) throws Exception {
        // Parse filter parameters
        String dateRange = request.getParameter("dateRange");
        String fromDateStr = request.getParameter("fromDate");
        String toDateStr = request.getParameter("toDate");
        String statusFilter = request.getParameter("status");
        String leaveTypeFilter = request.getParameter("leaveType");
        String employeeFilter = request.getParameter("employee");
        
        // Calculate date range
        Date fromDate = null;
        Date toDate = null;
        
        if ("custom".equals(dateRange)) {
            if (fromDateStr != null && !fromDateStr.trim().isEmpty()) {
                fromDate = parseDate(fromDateStr);
            }
            if (toDateStr != null && !toDateStr.trim().isEmpty()) {
                toDate = parseDate(toDateStr);
            }
        } else if (dateRange != null && !dateRange.trim().isEmpty()) {
            // Apply predefined date ranges logic
            Calendar cal = Calendar.getInstance();
            toDate = cal.getTime();
            
            switch (dateRange) {
                case "last30":
                    cal.add(Calendar.DAY_OF_MONTH, -30);
                    fromDate = cal.getTime();
                    break;
                case "last90":
                    cal.add(Calendar.DAY_OF_MONTH, -90);
                    fromDate = cal.getTime();
                    break;
                case "thisMonth":
                    cal.set(Calendar.DAY_OF_MONTH, 1);
                    fromDate = cal.getTime();
                    break;
                case "lastMonth":
                    cal.add(Calendar.MONTH, -1);
                    cal.set(Calendar.DAY_OF_MONTH, 1);
                    fromDate = cal.getTime();
                    cal.add(Calendar.MONTH, 1);
                    cal.set(Calendar.DAY_OF_MONTH, 0);
                    toDate = cal.getTime();
                    break;
                case "thisQuarter":
                    int currentMonth = cal.get(Calendar.MONTH);
                    int quarterStartMonth = (currentMonth / 3) * 3;
                    cal.set(Calendar.MONTH, quarterStartMonth);
                    cal.set(Calendar.DAY_OF_MONTH, 1);
                    fromDate = cal.getTime();
                    cal = Calendar.getInstance();
                    toDate = cal.getTime();
                    break;
                case "thisYear":
                    cal.set(Calendar.MONTH, 0);
                    cal.set(Calendar.DAY_OF_MONTH, 1);
                    fromDate = cal.getTime();
                    cal = Calendar.getInstance();
                    toDate = cal.getTime();
                    break;
            }
        }
        
        // Get all applications and apply filters
        List<LeaveApplication> allApplications = leaveApplicationDAO.getLeaveApplicationsWithFiltersAndReason(null, null, null);
        return filterApplications(allApplications, fromDate, toDate, statusFilter, leaveTypeFilter, employeeFilter);
    }
    
    /**
     * Validate and normalize report type
     */
    private String validateReportType(String reportType) {
        if (reportType == null || reportType.trim().isEmpty()) {
            return "summary";
        }
        
        String normalized = reportType.toLowerCase().trim();
        switch (normalized) {
            case "summary":
            case "detailed":  
            case "trends":
                return normalized;
            default:
                System.out.println("⚠️ Unknown report type '" + reportType + "', defaulting to 'summary'");
                return "summary";
        }
    }

    
    /**
     * Export data to CSV format
     */
    private void exportToCSV(HttpServletResponse response, List<LeaveApplication> applications, 
                           String reportId, String reportType) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"leave_report_" + reportId + ".csv\"");
        
        StringBuilder csv = new StringBuilder();
        
        // CSV Header
        csv.append("Report ID,").append(reportId).append("\n");
        csv.append("Generated On,").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())).append("\n");
        csv.append("Report Type,").append(reportType).append("\n");
        csv.append("Total Records,").append(applications.size()).append("\n\n");
        
        // Data headers
        csv.append("Application ID,Employee ID,Employee Name,Leave Type,Start Date,End Date,Duration,Status,Applied On,Manager ID,Reason,Reject Reason,Review Date,Attachment\n");
        
        // Get lookup maps
        Map<String, String> leaveTypeNames = getLeaveTypeNames();
        Map<String, String> employeeNames = getEmployeeNames();
        
        // Data rows
        for (LeaveApplication app : applications) {
            csv.append("\"").append(app.getApplicationid()).append("\",");
            csv.append("\"").append(app.getEmployeeid()).append("\",");
            csv.append("\"").append(employeeNames.getOrDefault(app.getEmployeeid(), "Unknown")).append("\",");
            csv.append("\"").append(leaveTypeNames.getOrDefault(app.getLeavetypeid(), "Unknown")).append("\",");
            csv.append("\"").append(app.getLeavestartdate()).append("\",");
            csv.append("\"").append(app.getLeaveenddate()).append("\",");
            csv.append("\"").append(app.getLeaveduration()).append("\",");
            csv.append("\"").append(app.getLeavestatus()).append("\",");
            csv.append("\"").append(app.getAppliedon()).append("\",");
            csv.append("\"").append(app.getManagerid() != null ? app.getManagerid() : "").append("\",");
            csv.append("\"").append(app.getLeavereason() != null ? app.getLeavereason().replace("\"", "\"\"") : "").append("\",");
            csv.append("\"").append(app.getRejectReason() != null ? app.getRejectReason().replace("\"", "\"\"") : "").append("\",");
            csv.append("\"").append(app.getReviewdate() != null ? app.getReviewdate() : "").append("\",");
            csv.append("\"").append(app.getAttachment() != null ? app.getAttachment() : "").append("\"");
            csv.append("\n");
        }
        
        response.getWriter().write(csv.toString());
    }
    
    /**
     * Export data to PDF format
     */
    private void exportToPDF(HttpServletResponse response, List<LeaveApplication> applications, 
            String reportId, String reportType) throws IOException {
		response.setContentType("text/html; charset=UTF-8");
		response.setHeader("Content-Disposition", "inline; filename=\"leave_report_" + reportId + ".html\"");
        
        // Get lookup maps
        Map<String, String> leaveTypeNames = getLeaveTypeNames();
        Map<String, String> employeeNames = getEmployeeNames();
        
        try {
            // Generate PDF content based on report type
            StringBuilder pdfContent;
            
            switch (reportType.toLowerCase()) {
                case "summary":
                    pdfContent = generateSummaryPDFContent(applications, reportId, reportType, leaveTypeNames, employeeNames);
                    break;
                case "detailed":
                    pdfContent = generateDetailedPDFContent(applications, reportId, reportType, leaveTypeNames, employeeNames);
                    break;
                case "trends":
                    pdfContent = generateTrendsPDFContent(applications, reportId, reportType, leaveTypeNames, employeeNames);
                    break;
                default:
                    pdfContent = generateDetailedPDFContent(applications, reportId, reportType, leaveTypeNames, employeeNames);
                    break;
            }
            
            // For a simple implementation, we'll create a well-formatted HTML that browsers can convert to PDF
            response.setContentType("text/html; charset=UTF-8");
            response.setHeader("Content-Disposition", "inline; filename=\"leave_report_" + reportId + ".html\"");
            response.getWriter().write(pdfContent.toString());
            
        } catch (Exception e) {
            System.err.println("Error generating PDF: " + e.getMessage());
            e.printStackTrace();
            
            // Fallback to simple text response
            response.setContentType("text/plain");
            response.setHeader("Content-Disposition", "attachment; filename=\"leave_report_" + reportId + ".txt\"");
            response.getWriter().write("Error generating PDF report. Please try again or contact support.");
        }
    }
    
    /**
     * Generate PDF-ready content
     */
    private StringBuilder generatePDFContent(List<LeaveApplication> applicationsList, String reportId, 
                                           String reportType, Map<String, String> leaveTypeNames, 
                                           Map<String, String> employeeNames) {
        StringBuilder content = new StringBuilder();
        
        // Null safety checks with different variable name to avoid scope issues
        List<LeaveApplication> safeApplications = applicationsList;
        if (safeApplications == null) {
            safeApplications = new ArrayList<>();
        }
        Map<String, String> safeLeaveTypeNames = leaveTypeNames;
        if (safeLeaveTypeNames == null) {
            safeLeaveTypeNames = new HashMap<>();
        }
        Map<String, String> safeEmployeeNames = employeeNames;
        if (safeEmployeeNames == null) {
            safeEmployeeNames = new HashMap<>();
        }
        
        content.append("<!DOCTYPE html>\n");
        content.append("<html>\n<head>\n");
        content.append("<meta charset=\"UTF-8\">\n");
        content.append("<title>Leave Report - ").append(reportId != null ? reportId : "UNKNOWN").append("</title>\n");
        content.append("<style>\n");
        
        // Add the complete CSS for PDF generation
        content.append("@page { size: A4 landscape; margin: 0.5in; }\n");
        content.append("* { box-sizing: border-box; }\n");
        content.append("body { font-family: Arial, sans-serif; margin: 0; padding: 15px; font-size: 10px; color: #333; background: white; }\n");
        content.append(".report-header { text-align: center; margin-bottom: 20px; border-bottom: 2px solid #333; padding-bottom: 10px; }\n");
        content.append(".report-header h1 { color: #2c3e50; font-size: 18px; margin: 0; font-weight: bold; }\n");
        content.append(".report-info { display: flex; justify-content: space-between; margin-bottom: 15px; font-size: 9px; }\n");
        content.append("table { width: 100%; border-collapse: collapse; margin-bottom: 20px; font-size: 8px; }\n");
        content.append("th, td { border: 1px solid #333; padding: 4px 3px; text-align: left; vertical-align: top; }\n");
        content.append("th { background-color: #34495e; color: white; font-weight: bold; text-align: center; }\n");
        content.append("tr:nth-child(even) { background-color: #f8f9fa; }\n");
        
        // Column widths
        content.append(".col-id { width: 8%; } .col-employee { width: 15%; } .col-type { width: 12%; }\n");
        content.append(".col-date { width: 10%; } .col-duration { width: 8%; } .col-status { width: 10%; }\n");
        content.append(".col-applied { width: 10%; } .col-manager { width: 12%; } .col-reason { width: 15%; }\n");
        
        // Status styles
        content.append(".status-pending { background-color: #fff3cd !important; color: #856404 !important; font-weight: bold; text-align: center; padding: 2px; }\n");
        content.append(".status-approved { background-color: #d4edda !important; color: #155724 !important; font-weight: bold; text-align: center; padding: 2px; }\n");
        content.append(".status-rejected { background-color: #f8d7da !important; color: #721c24 !important; font-weight: bold; text-align: center; padding: 2px; }\n");
        content.append(".status-cancelled { background-color: #e2e3e5 !important; color: #383d41 !important; font-weight: bold; text-align: center; padding: 2px; }\n");
        
        content.append(".employee-info { font-size: 7px; }\n");
        content.append(".employee-name { font-weight: bold; margin-bottom: 1px; }\n");
        content.append(".employee-id { color: #666; font-size: 6px; }\n");
        content.append(".summary-section { margin-top: 25px; page-break-inside: avoid; }\n");
        content.append(".summary-table { width: 50%; margin: 0 auto; font-size: 9px; }\n");
        content.append(".footer { margin-top: 20px; text-align: center; font-size: 8px; color: #666; border-top: 1px solid #ddd; padding-top: 10px; }\n");
        
        // Print styles
        content.append("@media print {\n");
        content.append("  body { margin: 0; padding: 10px; font-size: 8px; }\n");
        content.append("  table { font-size: 7px; }\n");
        content.append("  th, td { padding: 2px; }\n");
        content.append("  @page { margin: 0.3in; }\n");
        content.append("}\n");
        
        content.append("</style>\n");
        
        // Add JavaScript for automatic PDF generation
        content.append("<script>\n");
        content.append("window.onload = function() {\n");
        content.append("  // Auto-trigger print dialog for PDF generation\n");
        content.append("  setTimeout(function() {\n");
        content.append("    window.print();\n");
        content.append("  }, 1000);\n");
        content.append("};\n");
        content.append("</script>\n");
        
        content.append("</head>\n<body>\n");
        
        // Header
        content.append("<div class=\"report-header\">\n");
        content.append("<h1>IMNSB EMPLOYEE LEAVE MANAGEMENT SYSTEM</h1>\n");
        content.append("<h2 style=\"margin: 5px 0; font-size: 14px; color: #2980b9;\">Leave Application Report</h2>\n");
        content.append("</div>\n");
        
        // Report info
        content.append("<div class=\"report-info\">\n");
        content.append("<div><strong>Report ID:</strong> ").append(reportId != null ? reportId : "N/A").append("</div>\n");
        content.append("<div><strong>Generated:</strong> ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date())).append("</div>\n");
        content.append("<div><strong>Type:</strong> ").append(reportType != null ? reportType.toUpperCase() : "GENERAL").append("</div>\n");
        content.append("<div><strong>Records:</strong> ").append(safeApplications.size()).append("</div>\n");
        content.append("</div>\n");
        
        if (safeApplications.isEmpty()) {
            content.append("<div style=\"text-align: center; margin: 50px 0; font-size: 12px;\">\n");
            content.append("<p>No leave applications found matching the selected criteria.</p>\n");
            content.append("</div>\n");
        } else {
            // Data table
            content.append("<table>\n<thead>\n<tr>\n");
            content.append("<th class=\"col-id\">App ID</th>\n");
            content.append("<th class=\"col-employee\">Employee</th>\n");
            content.append("<th class=\"col-type\">Leave Type</th>\n");
            content.append("<th class=\"col-date\">Start</th>\n");
            content.append("<th class=\"col-date\">End</th>\n");
            content.append("<th class=\"col-duration\">Days</th>\n");
            content.append("<th class=\"col-status\">Status</th>\n");
            content.append("<th class=\"col-applied\">Applied</th>\n");
            content.append("<th class=\"col-manager\">Manager</th>\n");
            content.append("<th class=\"col-reason\">Reason</th>\n");
            content.append("</tr>\n</thead>\n<tbody>\n");
            
            for (LeaveApplication app : safeApplications) {
                if (app == null) continue; // Skip null applications
                
                content.append("<tr>\n");
                content.append("<td class=\"col-id\">").append(app.getApplicationid() != null ? app.getApplicationid() : "N/A").append("</td>\n");
                
                String employeeName = safeEmployeeNames.getOrDefault(app.getEmployeeid(), "Unknown");
                content.append("<td class=\"col-employee\">");
                content.append("<div class=\"employee-info\">");
                content.append("<div class=\"employee-name\">").append(employeeName).append("</div>");
                content.append("<div class=\"employee-id\">").append(app.getEmployeeid() != null ? app.getEmployeeid() : "N/A").append("</div>");
                content.append("</div></td>\n");
                
                content.append("<td class=\"col-type\">").append(safeLeaveTypeNames.getOrDefault(app.getLeavetypeid(), "Unknown")).append("</td>\n");
                content.append("<td class=\"col-date\">").append(app.getLeavestartdate() != null ? app.getLeavestartdate() : "N/A").append("</td>\n");
                content.append("<td class=\"col-date\">").append(app.getLeaveenddate() != null ? app.getLeaveenddate() : "N/A").append("</td>\n");
                content.append("<td class=\"col-duration\" style=\"text-align: center;\">").append(String.format("%.1f", app.getLeaveduration())).append("</td>\n");
                
                String status = app.getLeavestatus() != null ? app.getLeavestatus() : "Unknown";
                String statusClass = "status-" + status.toLowerCase();
                content.append("<td class=\"col-status\"><span class=\"").append(statusClass).append("\">").append(status).append("</span></td>\n");
                
                String appliedDate = "N/A";
                if (app.getAppliedon() != null && app.getAppliedon().length() >= 10) {
                    appliedDate = app.getAppliedon().substring(0, 10);
                }
                content.append("<td class=\"col-applied\">").append(appliedDate).append("</td>\n");
                content.append("<td class=\"col-manager\">").append(app.getManagerid() != null ? app.getManagerid() : "Unassigned").append("</td>\n");
                
                String reason = app.getLeavereason() != null ? app.getLeavereason() : "N/A";
                if (reason.length() > 40) reason = reason.substring(0, 37) + "...";
                content.append("<td class=\"col-reason\">").append(reason).append("</td>\n");
                content.append("</tr>\n");
            }
            
            content.append("</tbody>\n</table>\n");
            
            // Summary
            content.append("<div class=\"summary-section\">\n");
            content.append("<h3 style=\"text-align: center; margin-bottom: 15px; font-size: 11px;\">Summary Statistics</h3>\n");
            
            int pending = 0;
            int approved = 0;
            int rejected = 0;
            int cancelled = 0;
            double totalDays = 0.0;
            
            // Calculate statistics manually to avoid stream API issues
            for (LeaveApplication app : safeApplications) {
                if (app == null || app.getLeavestatus() == null) continue;
                
                String status = app.getLeavestatus();
                if ("Pending".equals(status)) {
                    pending++;
                } else if ("Approved".equals(status)) {
                    approved++;
                } else if ("Rejected".equals(status)) {
                    rejected++;
                } else if ("Cancelled".equals(status)) {
                    cancelled++;
                }
                totalDays += app.getLeaveduration();
            }
            
            double avgDuration = safeApplications.size() > 0 ? totalDays / safeApplications.size() : 0.0;
            double approvalRate = safeApplications.size() > 0 ? (approved * 100.0) / safeApplications.size() : 0.0;
            
            content.append("<table class=\"summary-table\">\n");
            content.append("<tr><th style=\"background-color: #3498db;\">Metric</th><th style=\"background-color: #3498db;\">Value</th></tr>\n");
            content.append("<tr><td>Total Applications</td><td style=\"text-align: center;\">").append(safeApplications.size()).append("</td></tr>\n");
            content.append("<tr><td>Pending</td><td style=\"text-align: center;\">").append(pending).append("</td></tr>\n");
            content.append("<tr><td>Approved</td><td style=\"text-align: center;\">").append(approved).append("</td></tr>\n");
            content.append("<tr><td>Rejected</td><td style=\"text-align: center;\">").append(rejected).append("</td></tr>\n");
            content.append("<tr><td>Cancelled</td><td style=\"text-align: center;\">").append(cancelled).append("</td></tr>\n");
            content.append("<tr><td>Total Leave Days</td><td style=\"text-align: center;\">").append(String.format("%.1f", totalDays)).append("</td></tr>\n");
            content.append("<tr><td>Average Duration</td><td style=\"text-align: center;\">").append(String.format("%.1f", avgDuration)).append(" days</td></tr>\n");
            content.append("<tr><td>Approval Rate</td><td style=\"text-align: center;\">").append(String.format("%.1f%%", approvalRate)).append("</td></tr>\n");
            content.append("</table>\n</div>\n");
        }
        
        // Footer
        content.append("<div class=\"footer\">\n");
        content.append("<p>Generated by IMNSB Employee Leave Management System | ");
        content.append(new SimpleDateFormat("EEEE, MMMM dd, yyyy 'at' HH:mm").format(new Date()));
        content.append("</p></div>\n");
        
        content.append("</body>\n</html>");
        
        return content;
    }
    
    /**
     * Generate Summary PDF content
     */
    private StringBuilder generateSummaryPDFContent(List<LeaveApplication> applicationsList, String reportId, 
                                                  String reportType, Map<String, String> leaveTypeNames, 
                                                  Map<String, String> employeeNames) {
        StringBuilder content = new StringBuilder();
        
        // Null safety checks
        List<LeaveApplication> safeApplications = applicationsList != null ? applicationsList : new ArrayList<>();
        Map<String, String> safeLeaveTypeNames = leaveTypeNames != null ? leaveTypeNames : new HashMap<>();
        Map<String, String> safeEmployeeNames = employeeNames != null ? employeeNames : new HashMap<>();
        
        content.append(generatePDFHeader(reportId, reportType + " - SUMMARY REPORT", safeApplications.size()));
        
        if (!safeApplications.isEmpty()) {
            // Generate summary data
            Map<String, Map<String, Object>> leaveTypeSummary = calculateLeaveTypeSummary(safeApplications, safeLeaveTypeNames);
            Map<String, Map<String, Object>> employeeSummary = calculateEmployeeSummary(safeApplications, safeEmployeeNames);
            
            // Leave Type Summary Table
            content.append("<h3 style=\"color: #2c3e50; margin: 20px 0 10px 0;\">Leave Type Summary</h3>\n");
            content.append("<table style=\"width: 100%; margin-bottom: 30px;\">\n");
            content.append("<tr><th>Leave Type</th><th>Applications</th><th>Total Days</th><th>Pending</th><th>Approved</th><th>Rejected</th></tr>\n");
            
            for (Map.Entry<String, Map<String, Object>> entry : leaveTypeSummary.entrySet()) {
                Map<String, Object> stats = entry.getValue();
                content.append("<tr>");
                content.append("<td>").append(entry.getKey()).append("</td>");
                content.append("<td style=\"text-align: center;\">").append(stats.get("totalApplications")).append("</td>");
                content.append("<td style=\"text-align: center;\">").append(String.format("%.1f", (Double) stats.get("totalDays"))).append("</td>");
                content.append("<td style=\"text-align: center;\">").append(stats.get("pending")).append("</td>");
                content.append("<td style=\"text-align: center;\">").append(stats.get("approved")).append("</td>");
                content.append("<td style=\"text-align: center;\">").append(stats.get("rejected")).append("</td>");
                content.append("</tr>\n");
            }
            content.append("</table>\n");
            
            // Top Employees Table
            List<Map.Entry<String, Map<String, Object>>> topEmployees = employeeSummary.entrySet()
                .stream()
                .sorted((e1, e2) -> Integer.compare(
                    (Integer) e2.getValue().get("totalApplications"),
                    (Integer) e1.getValue().get("totalApplications")
                ))
                .limit(10)
                .collect(Collectors.toList());
            
            content.append("<h3 style=\"color: #2c3e50; margin: 20px 0 10px 0;\">Top 10 Most Active Employees</h3>\n");
            content.append("<table style=\"width: 100%; margin-bottom: 30px;\">\n");
            content.append("<tr><th>Employee</th><th>Employee ID</th><th>Applications</th><th>Total Days</th><th>Approved</th></tr>\n");
            
            for (Map.Entry<String, Map<String, Object>> entry : topEmployees) {
                Map<String, Object> stats = entry.getValue();
                content.append("<tr>");
                content.append("<td>").append(entry.getKey()).append("</td>");
                content.append("<td>").append(stats.get("employeeId")).append("</td>");
                content.append("<td style=\"text-align: center;\">").append(stats.get("totalApplications")).append("</td>");
                content.append("<td style=\"text-align: center;\">").append(String.format("%.1f", (Double) stats.get("totalDays"))).append("</td>");
                content.append("<td style=\"text-align: center;\">").append(stats.get("approved")).append("</td>");
                content.append("</tr>\n");
            }
            content.append("</table>\n");
        }
        
        content.append(generateSummaryStatistics(safeApplications));
        content.append(generatePDFFooter());
        
        return content;
    }
    
    /**
     * Generate Detailed PDF content
     */
    private StringBuilder generateDetailedPDFContent(List<LeaveApplication> applicationsList, String reportId, 
                                                   String reportType, Map<String, String> leaveTypeNames, 
                                                   Map<String, String> employeeNames) {
        // This is the original detailed PDF content we had before
        return generatePDFContent(applicationsList, reportId, reportType, leaveTypeNames, employeeNames);
    }
    
    /**
     * Process data based on report type
     */
    private void processReportTypeData(HttpServletRequest request, String reportType, 
                                     List<LeaveApplication> applications, 
                                     Map<String, String> leaveTypeNames,
                                     Map<String, String> employeeNames) {
        
        System.out.println("=== PROCESSING REPORT TYPE: " + reportType + " ===");
        
        switch (reportType.toLowerCase()) {
            case "summary":
                processSummaryReport(request, applications, leaveTypeNames, employeeNames);
                break;
            case "detailed":
                processDetailedReport(request, applications, leaveTypeNames, employeeNames);
                break;
            case "trends":
                processTrendAnalysis(request, applications, leaveTypeNames, employeeNames);
                break;
            default:
                // Default to summary
                processSummaryReport(request, applications, leaveTypeNames, employeeNames);
                break;
        }
    }
    
    /**
     * Generate Trends PDF content
     */
    private StringBuilder generateTrendsPDFContent(List<LeaveApplication> applicationsList, String reportId, 
                                                 String reportType, Map<String, String> leaveTypeNames, 
                                                 Map<String, String> employeeNames) {
        StringBuilder content = new StringBuilder();
        
        // Null safety checks
        List<LeaveApplication> safeApplications = applicationsList != null ? applicationsList : new ArrayList<>();
        Map<String, String> safeLeaveTypeNames = leaveTypeNames != null ? leaveTypeNames : new HashMap<>();
        Map<String, String> safeEmployeeNames = employeeNames != null ? employeeNames : new HashMap<>();
        
        content.append(generatePDFHeader(reportId, reportType + " - TRENDS ANALYSIS", safeApplications.size()));
        
        if (!safeApplications.isEmpty()) {
            // Calculate trends data
            Map<String, Map<String, Integer>> monthlyTrends = calculateMonthlyTrends(safeApplications);
            Map<String, Integer> dayOfWeekTrends = calculateDayOfWeekTrends(safeApplications);
            Map<String, Integer> statusTrends = calculateStatusTrends(safeApplications);
            Map<String, Integer> leaveTypeTrends = calculateLeaveTypeTrends(safeApplications, safeLeaveTypeNames);
            
            // Monthly Trends
            content.append("<h3 style=\"color: #2c3e50; margin: 20px 0 10px 0;\">Monthly Trends</h3>\n");
            content.append("<table style=\"width: 100%; margin-bottom: 30px;\">\n");
            content.append("<tr><th>Month</th><th>Total</th><th>Pending</th><th>Approved</th><th>Rejected</th></tr>\n");
            
            List<String> sortedMonths = new ArrayList<>(monthlyTrends.keySet());
            Collections.sort(sortedMonths);
            
            for (String month : sortedMonths) {
                Map<String, Integer> stats = monthlyTrends.get(month);
                content.append("<tr>");
                content.append("<td>").append(month).append("</td>");
                content.append("<td style=\"text-align: center;\">").append(stats.getOrDefault("total", 0)).append("</td>");
                content.append("<td style=\"text-align: center;\">").append(stats.getOrDefault("pending", 0)).append("</td>");
                content.append("<td style=\"text-align: center;\">").append(stats.getOrDefault("approved", 0)).append("</td>");
                content.append("<td style=\"text-align: center;\">").append(stats.getOrDefault("rejected", 0)).append("</td>");
                content.append("</tr>\n");
            }
            content.append("</table>\n");
            
            // Day of Week Trends
            content.append("<h3 style=\"color: #2c3e50; margin: 20px 0 10px 0;\">Day of Week Analysis</h3>\n");
            content.append("<table style=\"width: 60%; margin-bottom: 30px;\">\n");
            content.append("<tr><th>Day</th><th>Applications</th><th>Percentage</th></tr>\n");
            
            String[] dayOrder = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
            for (String day : dayOrder) {
                if (dayOfWeekTrends.containsKey(day)) {
                    int count = dayOfWeekTrends.get(day);
                    double percentage = safeApplications.size() > 0 ? (count * 100.0) / safeApplications.size() : 0.0;
                    content.append("<tr>");
                    content.append("<td>").append(day).append("</td>");
                    content.append("<td style=\"text-align: center;\">").append(count).append("</td>");
                    content.append("<td style=\"text-align: center;\">").append(String.format("%.1f%%", percentage)).append("</td>");
                    content.append("</tr>\n");
                }
            }
            content.append("</table>\n");
            
            // Leave Type Distribution
            content.append("<h3 style=\"color: #2c3e50; margin: 20px 0 10px 0;\">Leave Type Distribution</h3>\n");
            content.append("<table style=\"width: 80%; margin-bottom: 30px;\">\n");
            content.append("<tr><th>Leave Type</th><th>Applications</th><th>Percentage</th></tr>\n");
            
            for (Map.Entry<String, Integer> entry : leaveTypeTrends.entrySet()) {
                int count = entry.getValue();
                double percentage = safeApplications.size() > 0 ? (count * 100.0) / safeApplications.size() : 0.0;
                content.append("<tr>");
                content.append("<td>").append(entry.getKey()).append("</td>");
                content.append("<td style=\"text-align: center;\">").append(count).append("</td>");
                content.append("<td style=\"text-align: center;\">").append(String.format("%.1f%%", percentage)).append("</td>");
                content.append("</tr>\n");
            }
            content.append("</table>\n");
        }
        
        content.append(generateSummaryStatistics(safeApplications));
        content.append(generatePDFFooter());
        
        return content;
    }
    
    /**
     * Generate PDF header
     */
    private String generatePDFHeader(String reportId, String reportTitle, int recordCount) {
        StringBuilder header = new StringBuilder();
        
        header.append("<!DOCTYPE html>\n");
        header.append("<html>\n<head>\n");
        header.append("<meta charset=\"UTF-8\">\n");
        header.append("<title>").append(reportTitle).append(" - ").append(reportId).append("</title>\n");
        header.append("<style>\n");
        
        // Add CSS
        header.append("@page { size: A4 landscape; margin: 0.5in; }\n");
        header.append("* { box-sizing: border-box; }\n");
        header.append("body { font-family: Arial, sans-serif; margin: 0; padding: 15px; font-size: 10px; color: #333; background: white; }\n");
        header.append(".report-header { text-align: center; margin-bottom: 20px; border-bottom: 2px solid #333; padding-bottom: 10px; }\n");
        header.append(".report-header h1 { color: #2c3e50; font-size: 18px; margin: 0; font-weight: bold; }\n");
        header.append(".report-info { display: flex; justify-content: space-between; margin-bottom: 15px; font-size: 9px; }\n");
        header.append("table { width: 100%; border-collapse: collapse; margin-bottom: 20px; font-size: 8px; }\n");
        header.append("th, td { border: 1px solid #333; padding: 4px 3px; text-align: left; vertical-align: top; }\n");
        header.append("th { background-color: #34495e; color: white; font-weight: bold; text-align: center; }\n");
        header.append("tr:nth-child(even) { background-color: #f8f9fa; }\n");
        header.append("h3 { color: #2c3e50; font-size: 12px; }\n");
        header.append(".summary-table { width: 50%; margin: 0 auto; font-size: 9px; }\n");
        header.append(".footer { margin-top: 20px; text-align: center; font-size: 8px; color: #666; border-top: 1px solid #ddd; padding-top: 10px; }\n");
        
        // Print styles
        header.append("@media print {\n");
        header.append("  body { margin: 0; padding: 10px; font-size: 8px; }\n");
        header.append("  table { font-size: 7px; }\n");
        header.append("  th, td { padding: 2px; }\n");
        header.append("  @page { margin: 0.3in; }\n");
        header.append("}\n");
        
        header.append("</style>\n");
        
        // Add JavaScript for auto-print
        header.append("<script>\n");
        header.append("window.onload = function() {\n");
        header.append("  setTimeout(function() { window.print(); }, 1000);\n");
        header.append("};\n");
        header.append("</script>\n");
        
        header.append("</head>\n<body>\n");
        
        // Header
        header.append("<div class=\"report-header\">\n");
        header.append("<h1>IMNSB EMPLOYEE LEAVE MANAGEMENT SYSTEM</h1>\n");
        header.append("<h2 style=\"margin: 5px 0; font-size: 14px; color: #2980b9;\">").append(reportTitle).append("</h2>\n");
        header.append("</div>\n");
        
        // Report info
        header.append("<div class=\"report-info\">\n");
        header.append("<div><strong>Report ID:</strong> ").append(reportId != null ? reportId : "N/A").append("</div>\n");
        header.append("<div><strong>Generated:</strong> ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date())).append("</div>\n");
        header.append("<div><strong>Records:</strong> ").append(recordCount).append("</div>\n");
        header.append("</div>\n");
        
        return header.toString();
    }
    
    /**
     * Generate PDF footer
     */
    private String generatePDFFooter() {
        StringBuilder footer = new StringBuilder();
        
        footer.append("<div class=\"footer\">\n");
        footer.append("<p>Generated by IMNSB Employee Leave Management System | ");
        footer.append(new SimpleDateFormat("EEEE, MMMM dd, yyyy 'at' HH:mm").format(new Date()));
        footer.append("</p></div>\n");
        
        footer.append("</body>\n</html>");
        
        return footer.toString();
    }
    
    /**
     * Process Summary Report - High-level overview with aggregated data
     */
    private void processSummaryReport(HttpServletRequest request, List<LeaveApplication> applications,
                                    Map<String, String> leaveTypeNames, Map<String, String> employeeNames) {
        
        System.out.println("Processing Summary Report...");
        
        // Summary by Leave Type
        Map<String, Map<String, Object>> leaveTypeSummary = new LinkedHashMap<>();
        
        // Summary by Employee
        Map<String, Map<String, Object>> employeeSummary = new LinkedHashMap<>();
        
        // Summary by Status
        Map<String, Integer> statusSummary = new LinkedHashMap<>();
        
        for (LeaveApplication app : applications) {
            String leaveTypeId = app.getLeavetypeid();
            String employeeId = app.getEmployeeid();
            String status = app.getLeavestatus();
            double duration = app.getLeaveduration();
            
            // Leave Type Summary
            String leaveTypeName = leaveTypeNames.getOrDefault(leaveTypeId, "Unknown Type");
            leaveTypeSummary.computeIfAbsent(leaveTypeName, k -> {
                Map<String, Object> summary = new HashMap<>();
                summary.put("totalApplications", 0);
                summary.put("totalDays", 0.0);
                summary.put("pending", 0);
                summary.put("approved", 0);
                summary.put("rejected", 0);
                summary.put("cancelled", 0);
                return summary;
            });
            
            Map<String, Object> typeStats = leaveTypeSummary.get(leaveTypeName);
            typeStats.put("totalApplications", (Integer) typeStats.get("totalApplications") + 1);
            typeStats.put("totalDays", (Double) typeStats.get("totalDays") + duration);
            
            if (status != null) {
                String statusKey = status.toLowerCase();
                if (typeStats.containsKey(statusKey)) {
                    typeStats.put(statusKey, (Integer) typeStats.get(statusKey) + 1);
                }
            }
            
            // Employee Summary
            String employeeName = employeeNames.getOrDefault(employeeId, "Unknown Employee");
            employeeSummary.computeIfAbsent(employeeName, k -> {
                Map<String, Object> summary = new HashMap<>();
                summary.put("employeeId", employeeId);
                summary.put("totalApplications", 0);
                summary.put("totalDays", 0.0);
                summary.put("pending", 0);
                summary.put("approved", 0);
                summary.put("rejected", 0);
                summary.put("cancelled", 0);
                return summary;
            });
            
            Map<String, Object> empStats = employeeSummary.get(employeeName);
            empStats.put("totalApplications", (Integer) empStats.get("totalApplications") + 1);
            empStats.put("totalDays", (Double) empStats.get("totalDays") + duration);
            
            if (status != null) {
                String statusKey = status.toLowerCase();
                if (empStats.containsKey(statusKey)) {
                    empStats.put(statusKey, (Integer) empStats.get(statusKey) + 1);
                }
            }
            
            // Status Summary
            statusSummary.put(status, statusSummary.getOrDefault(status, 0) + 1);
        }
        
        // Calculate average processing time for approved/rejected applications
        double avgProcessingTime = calculateAverageProcessingTime(applications);
        
        // Most active employees (top 10)
        List<Map.Entry<String, Map<String, Object>>> topEmployees = employeeSummary.entrySet()
            .stream()
            .sorted((e1, e2) -> Integer.compare(
                (Integer) e2.getValue().get("totalApplications"),
                (Integer) e1.getValue().get("totalApplications")
            ))
            .limit(10)
            .collect(Collectors.toList());
        
        // Set attributes for JSP
        request.setAttribute("leaveTypeSummary", leaveTypeSummary);
        request.setAttribute("employeeSummary", employeeSummary);
        request.setAttribute("statusSummary", statusSummary);
        request.setAttribute("topEmployees", topEmployees);
        request.setAttribute("avgProcessingTime", avgProcessingTime);
        request.setAttribute("reportType", "summary");
        
        System.out.println("Summary Report processed:");
        System.out.println("- Leave Types: " + leaveTypeSummary.size());
        System.out.println("- Employees: " + employeeSummary.size());
        System.out.println("- Top Employees: " + topEmployees.size());
    }
    
    /**
     * Find most popular leave type from applications
     */
    private String findMostPopularLeaveType(List<LeaveApplication> applications, Map<String, String> leaveTypeNames) {
        if (applications.isEmpty()) {
            return "N/A";
        }
        
        Map<String, Integer> leaveTypeCounts = new HashMap<>();
        
        for (LeaveApplication app : applications) {
            String leaveTypeId = app.getLeavetypeid();
            String leaveTypeName = leaveTypeNames.getOrDefault(leaveTypeId, "Unknown Type");
            leaveTypeCounts.put(leaveTypeName, leaveTypeCounts.getOrDefault(leaveTypeName, 0) + 1);
        }
        
        // Find the leave type with maximum count
        String mostPopular = "N/A";
        int maxCount = 0;
        
        for (Map.Entry<String, Integer> entry : leaveTypeCounts.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                mostPopular = entry.getKey();
            }
        }
        
        // Calculate percentage
        if (maxCount > 0 && applications.size() > 0) {
            double percentage = (maxCount * 100.0) / applications.size();
            return String.format("%s (%.0f%% of applications)", mostPopular, percentage);
        }
        
        return mostPopular;
    }
    
    /**
     * Process Detailed Report - Individual application details with enhanced information
     */
    private void processDetailedReport(HttpServletRequest request, List<LeaveApplication> applications,
                                     Map<String, String> leaveTypeNames, Map<String, String> employeeNames) {
        
        System.out.println("Processing Detailed Report...");
        
        // Sort applications by applied date (most recent first)
        applications.sort((a1, a2) -> {
            try {
                String date1 = a1.getAppliedon();
                String date2 = a2.getAppliedon();
                if (date1 == null) return 1;
                if (date2 == null) return -1;
                return date2.compareTo(date1); // Descending order
            } catch (Exception e) {
                return 0;
            }
        });
        
        // Enhance applications with additional calculated fields
        List<Map<String, Object>> enhancedApplications = new ArrayList<>();
        
        for (LeaveApplication app : applications) {
            Map<String, Object> enhanced = new HashMap<>();
            
            // Basic application info
            enhanced.put("application", app);
            enhanced.put("leaveTypeName", leaveTypeNames.getOrDefault(app.getLeavetypeid(), "Unknown Type"));
            enhanced.put("employeeName", employeeNames.getOrDefault(app.getEmployeeid(), "Unknown Employee"));
            
            // Calculate additional fields
            enhanced.put("daysFromToday", calculateDaysFromToday(app.getLeavestartdate()));
            enhanced.put("processingTime", calculateProcessingTime(app));
            enhanced.put("isUrgent", isUrgentApplication(app));
            enhanced.put("leaveCategory", categorizeLeave(app, leaveTypeNames));
            
            // Weekend/Holiday information
            enhanced.put("includesWeekend", includesWeekend(app.getLeavestartdate(), app.getLeaveenddate()));
            enhanced.put("businessDays", calculateBusinessDays(app.getLeavestartdate(), app.getLeaveenddate()));
            
            enhancedApplications.add(enhanced);
        }
        
        // Additional analytics for detailed view
        Map<String, Object> detailedAnalytics = new HashMap<>();
        detailedAnalytics.put("urgentApplications", enhancedApplications.stream()
            .mapToInt(app -> (Boolean) app.get("isUrgent") ? 1 : 0).sum());
        detailedAnalytics.put("applicationsWithAttachments", applications.stream()
            .mapToInt(app -> (app.getAttachment() != null && !app.getAttachment().trim().isEmpty()) ? 1 : 0).sum());
        detailedAnalytics.put("averageLeaveDuration", applications.stream()
            .mapToDouble(LeaveApplication::getLeaveduration).average().orElse(0.0));
        
        request.setAttribute("enhancedApplications", enhancedApplications);
        request.setAttribute("detailedAnalytics", detailedAnalytics);
        request.setAttribute("reportType", "detailed");
        
        System.out.println("Detailed Report processed: " + enhancedApplications.size() + " enhanced applications");
    }
    
    /**
     * Process Trend Analysis - Time-based patterns and trends
     */
    private void processTrendAnalysis(HttpServletRequest request, List<LeaveApplication> applications,
                                    Map<String, String> leaveTypeNames, Map<String, String> employeeNames) {
        
        System.out.println("Processing Trend Analysis...");
        
        // Monthly trends
        Map<String, Map<String, Integer>> monthlyTrends = new LinkedHashMap<>();
        
        // Quarterly trends
        Map<String, Map<String, Integer>> quarterlyTrends = new LinkedHashMap<>();
        
        // Day of week trends
        Map<String, Integer> dayOfWeekTrends = new LinkedHashMap<>();
        
        // Leave type trends over time
        Map<String, Map<String, Integer>> leaveTypeTrends = new LinkedHashMap<>();
        
        // Status change trends
        Map<String, Integer> statusTrends = new LinkedHashMap<>();
        
        SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy-MM");
        SimpleDateFormat quarterFormat = new SimpleDateFormat("yyyy");
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE");
        
        for (LeaveApplication app : applications) {
            try {
                String appliedOnStr = app.getAppliedon();
                if (appliedOnStr == null || appliedOnStr.length() < 10) continue;
                
                Date appliedDate = parseDate(appliedOnStr.substring(0, 10));
                Calendar cal = Calendar.getInstance();
                cal.setTime(appliedDate);
                
                // Monthly trends
                String monthKey = monthFormat.format(appliedDate);
                monthlyTrends.computeIfAbsent(monthKey, k -> new HashMap<>());
                Map<String, Integer> monthStats = monthlyTrends.get(monthKey);
                monthStats.put("total", monthStats.getOrDefault("total", 0) + 1);
                monthStats.put(app.getLeavestatus().toLowerCase(), 
                             monthStats.getOrDefault(app.getLeavestatus().toLowerCase(), 0) + 1);
                
                // Quarterly trends
                int quarter = (cal.get(Calendar.MONTH) / 3) + 1;
                String quarterKey = quarterFormat.format(appliedDate) + "-Q" + quarter;
                quarterlyTrends.computeIfAbsent(quarterKey, k -> new HashMap<>());
                Map<String, Integer> quarterStats = quarterlyTrends.get(quarterKey);
                quarterStats.put("total", quarterStats.getOrDefault("total", 0) + 1);
                quarterStats.put(app.getLeavestatus().toLowerCase(), 
                               quarterStats.getOrDefault(app.getLeavestatus().toLowerCase(), 0) + 1);
                
                // Day of week trends
                String dayOfWeek = dayFormat.format(appliedDate);
                dayOfWeekTrends.put(dayOfWeek, dayOfWeekTrends.getOrDefault(dayOfWeek, 0) + 1);
                
                // Leave type trends
                String leaveTypeName = leaveTypeNames.getOrDefault(app.getLeavetypeid(), "Unknown Type");
                leaveTypeTrends.computeIfAbsent(leaveTypeName, k -> new HashMap<>());
                Map<String, Integer> typeStats = leaveTypeTrends.get(leaveTypeName);
                typeStats.put(monthKey, typeStats.getOrDefault(monthKey, 0) + 1);
                
                // Status trends
                statusTrends.put(app.getLeavestatus(), statusTrends.getOrDefault(app.getLeavestatus(), 0) + 1);
                
            } catch (Exception e) {
                System.err.println("Error processing date for trend analysis: " + e.getMessage());
            }
        }
        
        // Calculate trend indicators
        Map<String, Object> trendIndicators = calculateTrendIndicators(monthlyTrends, quarterlyTrends);
        
        // Peak periods analysis
        Map<String, Object> peakAnalysis = analyzePeakPeriods(monthlyTrends, dayOfWeekTrends);
        
        // Prepare chart data for visualization
        Map<String, Object> chartData = prepareChartData(monthlyTrends, dayOfWeekTrends, statusTrends, leaveTypeTrends);
        
        request.setAttribute("monthlyTrends", monthlyTrends);
        request.setAttribute("quarterlyTrends", quarterlyTrends);
        request.setAttribute("dayOfWeekTrends", dayOfWeekTrends);
        request.setAttribute("leaveTypeTrends", leaveTypeTrends);
        request.setAttribute("statusTrends", statusTrends);
        request.setAttribute("trendIndicators", trendIndicators);
        request.setAttribute("peakAnalysis", peakAnalysis);
        request.setAttribute("chartData", chartData);
        request.setAttribute("reportType", "trends");
        
        System.out.println("Trend Analysis processed:");
        System.out.println("- Monthly periods: " + monthlyTrends.size());
        System.out.println("- Quarterly periods: " + quarterlyTrends.size());
        System.out.println("- Leave types tracked: " + leaveTypeTrends.size());
    }
    
    /**
     * Prepare chart data for visualization
     */
    private Map<String, Object> prepareChartData(Map<String, Map<String, Integer>> monthlyTrends,
                                               Map<String, Integer> dayOfWeekTrends,
                                               Map<String, Integer> statusTrends,
                                               Map<String, Map<String, Integer>> leaveTypeTrends) {
        Map<String, Object> chartData = new HashMap<>();
        
        // Monthly trends chart data
        List<String> monthLabels = new ArrayList<>(monthlyTrends.keySet());
        Collections.sort(monthLabels);
        List<Integer> monthlyValues = new ArrayList<>();
        for (String month : monthLabels) {
            monthlyValues.add(monthlyTrends.get(month).getOrDefault("total", 0));
        }
        chartData.put("monthlyLabels", monthLabels);
        chartData.put("monthlyValues", monthlyValues);
        
        // Day of week chart data
        String[] dayOrder = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        List<String> dayLabels = new ArrayList<>();
        List<Integer> dayValues = new ArrayList<>();
        for (String day : dayOrder) {
            if (dayOfWeekTrends.containsKey(day)) {
                dayLabels.add(day);
                dayValues.add(dayOfWeekTrends.get(day));
            }
        }
        chartData.put("dayLabels", dayLabels);
        chartData.put("dayValues", dayValues);
        
        // Status distribution chart data
        List<String> statusLabels = new ArrayList<>(statusTrends.keySet());
        List<Integer> statusValues = new ArrayList<>();
        for (String status : statusLabels) {
            statusValues.add(statusTrends.get(status));
        }
        chartData.put("statusLabels", statusLabels);
        chartData.put("statusValues", statusValues);
        
        // Leave type distribution chart data
        Map<String, Integer> leaveTypeTotal = new HashMap<>();
        for (Map.Entry<String, Map<String, Integer>> entry : leaveTypeTrends.entrySet()) {
            int total = entry.getValue().values().stream().mapToInt(Integer::intValue).sum();
            leaveTypeTotal.put(entry.getKey(), total);
        }
        List<String> leaveTypeLabels = new ArrayList<>(leaveTypeTotal.keySet());
        List<Integer> leaveTypeValues = new ArrayList<>();
        for (String type : leaveTypeLabels) {
            leaveTypeValues.add(leaveTypeTotal.get(type));
        }
        chartData.put("leaveTypeLabels", leaveTypeLabels);
        chartData.put("leaveTypeValues", leaveTypeValues);
        
        return chartData;
    }
    
    /**
     * Calculate trend indicators for analysis
     */
    private Map<String, Object> calculateTrendIndicators(Map<String, Map<String, Integer>> monthlyTrends,
                                                        Map<String, Map<String, Integer>> quarterlyTrends) {
        Map<String, Object> indicators = new HashMap<>();
        
        // Get sorted month keys
        List<String> monthKeys = new ArrayList<>(monthlyTrends.keySet());
        Collections.sort(monthKeys);
        
        if (monthKeys.size() >= 2) {
            // Calculate month-over-month growth
            String currentMonth = monthKeys.get(monthKeys.size() - 1);
            String previousMonth = monthKeys.get(monthKeys.size() - 2);
            
            int currentMonthTotal = monthlyTrends.get(currentMonth).getOrDefault("total", 0);
            int previousMonthTotal = monthlyTrends.get(previousMonth).getOrDefault("total", 0);
            
            double growthRate = 0.0;
            if (previousMonthTotal > 0) {
                growthRate = ((double)(currentMonthTotal - previousMonthTotal) / previousMonthTotal) * 100;
            }
            
            indicators.put("monthOverMonthGrowth", growthRate);
            indicators.put("currentMonthTotal", currentMonthTotal);
            indicators.put("previousMonthTotal", previousMonthTotal);
        } else {
            indicators.put("monthOverMonthGrowth", 0.0);
            indicators.put("currentMonthTotal", 0);
            indicators.put("previousMonthTotal", 0);
        }
        
        return indicators;
    }
    
    /**
     * Analyze peak periods from trends data
     */
    private Map<String, Object> analyzePeakPeriods(Map<String, Map<String, Integer>> monthlyTrends,
                                                  Map<String, Integer> dayOfWeekTrends) {
        Map<String, Object> peakAnalysis = new HashMap<>();
        
        // Find peak month
        String peakMonth = "";
        int peakMonthApplications = 0;
        for (Map.Entry<String, Map<String, Integer>> entry : monthlyTrends.entrySet()) {
            int total = entry.getValue().getOrDefault("total", 0);
            if (total > peakMonthApplications) {
                peakMonthApplications = total;
                peakMonth = entry.getKey();
            }
        }
        
        // Find peak day of week
        String peakDay = "";
        int peakDayApplications = 0;
        for (Map.Entry<String, Integer> entry : dayOfWeekTrends.entrySet()) {
            if (entry.getValue() > peakDayApplications) {
                peakDayApplications = entry.getValue();
                peakDay = entry.getKey();
            }
        }
        
        peakAnalysis.put("peakMonth", peakMonth);
        peakAnalysis.put("peakMonthApplications", peakMonthApplications);
        peakAnalysis.put("peakDay", peakDay);
        peakAnalysis.put("peakDayApplications", peakDayApplications);
        
        return peakAnalysis;
    }
    
    /**
     * Filter applications based on provided criteria
     */
    private List<LeaveApplication> filterApplications(List<LeaveApplication> applications, 
                                                    Date fromDate, Date toDate, 
                                                    String statusFilter, String leaveTypeFilter, 
                                                    String employeeFilter) {
        
        System.out.println("=== FILTERING APPLICATIONS ===");
        System.out.println("Original count: " + applications.size());
        
        return applications.stream()
            .filter(app -> {
                // Date filter
                if (fromDate != null || toDate != null) {
                    try {
                        String appliedOnStr = app.getAppliedon();
                        if (appliedOnStr != null && appliedOnStr.length() >= 10) {
                            Date appliedDate = parseDate(appliedOnStr.substring(0, 10));
                            
                            if (fromDate != null && appliedDate.before(fromDate)) {
                                return false;
                            }
                            if (toDate != null && appliedDate.after(toDate)) {
                                return false;
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("Error parsing date for application: " + app.getApplicationid());
                        return false;
                    }
                }
                
                // Status filter
                if (statusFilter != null && !statusFilter.trim().isEmpty()) {
                    if (!statusFilter.equalsIgnoreCase(app.getLeavestatus())) {
                        return false;
                    }
                }
                
                // Leave type filter
                if (leaveTypeFilter != null && !leaveTypeFilter.trim().isEmpty()) {
                    if (!leaveTypeFilter.equals(app.getLeavetypeid())) {
                        return false;
                    }
                }
                
                // Employee filter (search by ID or name)
                if (employeeFilter != null && !employeeFilter.trim().isEmpty()) {
                    String filter = employeeFilter.toLowerCase();
                    String employeeId = app.getEmployeeid();
                    
                    // Check if filter matches employee ID
                    if (employeeId != null && employeeId.toLowerCase().contains(filter)) {
                        return true;
                    }
                    
                    // Check if filter matches employee name
                    Map<String, String> employeeNames = getEmployeeNames();
                    String employeeName = employeeNames.get(employeeId);
                    if (employeeName != null && employeeName.toLowerCase().contains(filter)) {
                        return true;
                    }
                    
                    return false;
                }
                
                return true;
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Calculate and set statistics for the applications
     */
    private void calculateAndSetStatistics(HttpServletRequest request, List<LeaveApplication> applications) {
        System.out.println("=== CALCULATING STATISTICS ===");
        
        int total = applications.size();
        int pending = 0;
        int approved = 0;
        int rejected = 0;
        int cancelled = 0;
        
        for (LeaveApplication app : applications) {
            String status = app.getLeavestatus();
            if (status != null) {
                switch (status.toLowerCase()) {
                    case "pending":
                        pending++;
                        break;
                    case "approved":
                        approved++;
                        break;
                    case "rejected":
                        rejected++;
                        break;
                    case "cancelled":
                        cancelled++;
                        break;
                }
            }
        }
        
        request.setAttribute("totalApplications", total);
        request.setAttribute("pendingApplications", pending);
        request.setAttribute("approvedApplications", approved);
        request.setAttribute("rejectedApplications", rejected);
        request.setAttribute("cancelledApplications", cancelled);
        
        System.out.println("Statistics calculated:");
        System.out.println("- Total: " + total);
        System.out.println("- Pending: " + pending);
        System.out.println("- Approved: " + approved);
        System.out.println("- Rejected: " + rejected);
        System.out.println("- Cancelled: " + cancelled);
    }
    
    /**
     * Get leave type names mapping
     */
    private Map<String, String> getLeaveTypeNames() {
        Map<String, String> leaveTypeNames = new HashMap<>();
        try {
            List<LeaveType> leaveTypes = leaveTypeDAO.getAllLeaveTypes();
            for (LeaveType type : leaveTypes) {
                leaveTypeNames.put(type.getLeaveTypeId(), type.getLeaveTypeName());
            }
            System.out.println("✅ Loaded " + leaveTypeNames.size() + " leave type names");
        } catch (Exception e) {
            System.err.println("❌ Error loading leave type names: " + e.getMessage());
            e.printStackTrace();
        }
        return leaveTypeNames;
    }
    
    /**
     * Get employee names mapping
     */
    private Map<String, String> getEmployeeNames() {
        Map<String, String> employeeNames = new HashMap<>();
        try {
            List<Employee> employees = employeeDAO.getAllEmployees();
            for (Employee emp : employees) {
                employeeNames.put(emp.getEmployeeId(), emp.getEmployeeName());
            }
            System.out.println("✅ Loaded " + employeeNames.size() + " employee names");
        } catch (Exception e) {
            System.err.println("❌ Error loading employee names: " + e.getMessage());
            e.printStackTrace();
        }
        return employeeNames;
    }
    
    /**
     * Parse date string to Date object
     */
    private Date parseDate(String dateStr) throws ParseException {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        
        SimpleDateFormat[] formats = {
            new SimpleDateFormat("yyyy-MM-dd"),
            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"),
            new SimpleDateFormat("dd/MM/yyyy"),
            new SimpleDateFormat("MM/dd/yyyy")
        };
        
        for (SimpleDateFormat format : formats) {
            try {
                return format.parse(dateStr.trim());
            } catch (ParseException e) {
                // Try next format
            }
        }
        
        throw new ParseException("Unable to parse date: " + dateStr, 0);
    }
    
    // Helper methods for report processing
    
    private double calculateAverageProcessingTime(List<LeaveApplication> applications) {
        return applications.stream()
            .filter(app -> !"Pending".equals(app.getLeavestatus()))
            .mapToDouble(this::calculateProcessingTime)
            .filter(time -> time > 0)
            .average()
            .orElse(0.0);
    }
    
    private double calculateProcessingTime(LeaveApplication app) {
        try {
            if (app.getReviewdate() != null && app.getAppliedon() != null) {
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date applied = format.parse(app.getAppliedon());
                Date reviewed = format.parse(app.getReviewdate());
                return (reviewed.getTime() - applied.getTime()) / (1000.0 * 60 * 60 * 24); // Days
            }
        } catch (Exception e) {
            // If can't calculate, estimate based on status
            if (!"Pending".equals(app.getLeavestatus())) {
                return 2.0; // Default 2 days processing time
            }
        }
        return 0.0;
    }
    
    private long calculateDaysFromToday(String startDateStr) {
        try {
            Date startDate = parseDate(startDateStr);
            Date today = new Date();
            return (startDate.getTime() - today.getTime()) / (1000 * 60 * 60 * 24);
        } catch (Exception e) {
            return 0;
        }
    }
    
    private boolean isUrgentApplication(LeaveApplication app) {
        long daysFromToday = calculateDaysFromToday(app.getLeavestartdate());
        return daysFromToday <= 3 && daysFromToday >= 0 && "Pending".equals(app.getLeavestatus());
    }
    
    private String categorizeLeave(LeaveApplication app, Map<String, String> leaveTypeNames) {
        String leaveTypeName = leaveTypeNames.getOrDefault(app.getLeavetypeid(), "Other");
        
        if (leaveTypeName.toLowerCase().contains("annual") || leaveTypeName.toLowerCase().contains("vacation")) {
            return "Planned Leave";
        } else if (leaveTypeName.toLowerCase().contains("sick") || leaveTypeName.toLowerCase().contains("medical")) {
            return "Medical Leave";
        } else if (leaveTypeName.toLowerCase().contains("emergency") || leaveTypeName.toLowerCase().contains("urgent")) {
            return "Emergency Leave";
        } else {
            return "Other Leave";
        }
    }
    
    private boolean includesWeekend(String startDateStr, String endDateStr) {
        try {
            Date startDate = parseDate(startDateStr);
            Date endDate = parseDate(endDateStr);
            
            Calendar cal = Calendar.getInstance();
            cal.setTime(startDate);
            
            while (!cal.getTime().after(endDate)) {
                int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
                if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
                    return true;
                }
                cal.add(Calendar.DAY_OF_MONTH, 1);
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }
    
    private int calculateBusinessDays(String startDateStr, String endDateStr) {
        try {
            Date startDate = parseDate(startDateStr);
            Date endDate = parseDate(endDateStr);
            
            Calendar cal = Calendar.getInstance();
            cal.setTime(startDate);
            
            int businessDays = 0;
            while (!cal.getTime().after(endDate)) {
                int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
                // Count Monday to Friday as business days
                if (dayOfWeek != Calendar.SATURDAY && dayOfWeek != Calendar.SUNDAY) {
                    businessDays++;
                }
                cal.add(Calendar.DAY_OF_MONTH, 1);
            }
            
            return businessDays;
        } catch (Exception e) {
            System.err.println("Error calculating business days: " + e.getMessage());
            return 0;
        }
    }
    
    /**
     * Calculate leave type summary
     */
    private Map<String, Map<String, Object>> calculateLeaveTypeSummary(List<LeaveApplication> applications, 
                                                                      Map<String, String> leaveTypeNames) {
        Map<String, Map<String, Object>> summary = new LinkedHashMap<>();
        
        for (LeaveApplication app : applications) {
            String leaveTypeId = app.getLeavetypeid();
            String status = app.getLeavestatus();
            double duration = app.getLeaveduration();
            
            String leaveTypeName = leaveTypeNames.getOrDefault(leaveTypeId, "Unknown Type");
            summary.computeIfAbsent(leaveTypeName, k -> {
                Map<String, Object> stats = new HashMap<>();
                stats.put("totalApplications", 0);
                stats.put("totalDays", 0.0);
                stats.put("pending", 0);
                stats.put("approved", 0);
                stats.put("rejected", 0);
                stats.put("cancelled", 0);
                return stats;
            });
            
            Map<String, Object> typeStats = summary.get(leaveTypeName);
            typeStats.put("totalApplications", (Integer) typeStats.get("totalApplications") + 1);
            typeStats.put("totalDays", (Double) typeStats.get("totalDays") + duration);
            
            if (status != null) {
                String statusKey = status.toLowerCase();
                if (typeStats.containsKey(statusKey)) {
                    typeStats.put(statusKey, (Integer) typeStats.get(statusKey) + 1);
                }
            }
        }
        
        return summary;
    }
    
    /**
     * Calculate employee summary
     */
    private Map<String, Map<String, Object>> calculateEmployeeSummary(List<LeaveApplication> applications, 
                                                                     Map<String, String> employeeNames) {
        Map<String, Map<String, Object>> summary = new LinkedHashMap<>();
        
        for (LeaveApplication app : applications) {
            String employeeId = app.getEmployeeid();
            String status = app.getLeavestatus();
            double duration = app.getLeaveduration();
            
            String employeeName = employeeNames.getOrDefault(employeeId, "Unknown Employee");
            summary.computeIfAbsent(employeeName, k -> {
                Map<String, Object> stats = new HashMap<>();
                stats.put("employeeId", employeeId);
                stats.put("totalApplications", 0);
                stats.put("totalDays", 0.0);
                stats.put("pending", 0);
                stats.put("approved", 0);
                stats.put("rejected", 0);
                stats.put("cancelled", 0);
                return stats;
            });
            
            Map<String, Object> empStats = summary.get(employeeName);
            empStats.put("totalApplications", (Integer) empStats.get("totalApplications") + 1);
            empStats.put("totalDays", (Double) empStats.get("totalDays") + duration);
            
            if (status != null) {
                String statusKey = status.toLowerCase();
                if (empStats.containsKey(statusKey)) {
                    empStats.put(statusKey, (Integer) empStats.get(statusKey) + 1);
                }
            }
        }
        
        return summary;
    }
    
    /**
     * Calculate monthly trends
     */
    private Map<String, Map<String, Integer>> calculateMonthlyTrends(List<LeaveApplication> applications) {
        Map<String, Map<String, Integer>> trends = new LinkedHashMap<>();
        SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy-MM");
        
        for (LeaveApplication app : applications) {
            try {
                String appliedOnStr = app.getAppliedon();
                if (appliedOnStr != null && appliedOnStr.length() >= 10) {
                    Date appliedDate = parseDate(appliedOnStr.substring(0, 10));
                    String monthKey = monthFormat.format(appliedDate);
                    
                    trends.computeIfAbsent(monthKey, k -> new HashMap<>());
                    Map<String, Integer> monthStats = trends.get(monthKey);
                    monthStats.put("total", monthStats.getOrDefault("total", 0) + 1);
                    monthStats.put(app.getLeavestatus().toLowerCase(), 
                                 monthStats.getOrDefault(app.getLeavestatus().toLowerCase(), 0) + 1);
                }
            } catch (Exception e) {
                System.err.println("Error processing date: " + e.getMessage());
            }
        }
        
        return trends;
    }
    
    /**
     * Calculate day of week trends
     */
    private Map<String, Integer> calculateDayOfWeekTrends(List<LeaveApplication> applications) {
        Map<String, Integer> trends = new LinkedHashMap<>();
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE");
        
        for (LeaveApplication app : applications) {
            try {
                String appliedOnStr = app.getAppliedon();
                if (appliedOnStr != null && appliedOnStr.length() >= 10) {
                    Date appliedDate = parseDate(appliedOnStr.substring(0, 10));
                    String dayOfWeek = dayFormat.format(appliedDate);
                    trends.put(dayOfWeek, trends.getOrDefault(dayOfWeek, 0) + 1);
                }
            } catch (Exception e) {
                System.err.println("Error processing date: " + e.getMessage());
            }
        }
        
        return trends;
    }
    
    /**
     * Calculate status trends
     */
    private Map<String, Integer> calculateStatusTrends(List<LeaveApplication> applications) {
        Map<String, Integer> trends = new LinkedHashMap<>();
        
        for (LeaveApplication app : applications) {
            String status = app.getLeavestatus();
            trends.put(status, trends.getOrDefault(status, 0) + 1);
        }
        
        return trends;
    }
    
    /**
     * Calculate leave type trends
     */
    private Map<String, Integer> calculateLeaveTypeTrends(List<LeaveApplication> applications, 
                                                         Map<String, String> leaveTypeNames) {
        Map<String, Integer> trends = new LinkedHashMap<>();
        
        for (LeaveApplication app : applications) {
            String leaveTypeName = leaveTypeNames.getOrDefault(app.getLeavetypeid(), "Unknown Type");
            trends.put(leaveTypeName, trends.getOrDefault(leaveTypeName, 0) + 1);
        }
        
        return trends;
    }
    
    /**
     * Generate summary statistics section
     */
    private String generateSummaryStatistics(List<LeaveApplication> applications) {
        StringBuilder stats = new StringBuilder();
        
        int pending = 0, approved = 0, rejected = 0, cancelled = 0;
        double totalDays = 0.0;
        
        for (LeaveApplication app : applications) {
            String status = app.getLeavestatus();
            if ("Pending".equals(status)) pending++;
            else if ("Approved".equals(status)) approved++;
            else if ("Rejected".equals(status)) rejected++;
            else if ("Cancelled".equals(status)) cancelled++;
            totalDays += app.getLeaveduration();
        }
        
        double avgDuration = applications.size() > 0 ? totalDays / applications.size() : 0.0;
        double approvalRate = applications.size() > 0 ? (approved * 100.0) / applications.size() : 0.0;
        
        stats.append("<h3 style=\"color: #2c3e50; margin: 30px 0 15px 0; text-align: center;\">Summary Statistics</h3>\n");
        stats.append("<table class=\"summary-table\">\n");
        stats.append("<tr><th style=\"background-color: #3498db;\">Metric</th><th style=\"background-color: #3498db;\">Value</th></tr>\n");
        stats.append("<tr><td>Total Applications</td><td style=\"text-align: center;\">").append(applications.size()).append("</td></tr>\n");
        stats.append("<tr><td>Pending</td><td style=\"text-align: center;\">").append(pending).append("</td></tr>\n");
        stats.append("<tr><td>Approved</td><td style=\"text-align: center;\">").append(approved).append("</td></tr>\n");
        stats.append("<tr><td>Rejected</td><td style=\"text-align: center;\">").append(rejected).append("</td></tr>\n");
        stats.append("<tr><td>Cancelled</td><td style=\"text-align: center;\">").append(cancelled).append("</td></tr>\n");
        stats.append("<tr><td>Total Leave Days</td><td style=\"text-align: center;\">").append(String.format("%.1f", totalDays)).append("</td></tr>\n");
        stats.append("<tr><td>Average Duration</td><td style=\"text-align: center;\">").append(String.format("%.1f", avgDuration)).append(" days</td></tr>\n");
        stats.append("<tr><td>Approval Rate</td><td style=\"text-align: center;\">").append(String.format("%.1f%%", approvalRate)).append("</td></tr>\n");
        stats.append("</table>\n");
        
        return stats.toString();
    }
    
}
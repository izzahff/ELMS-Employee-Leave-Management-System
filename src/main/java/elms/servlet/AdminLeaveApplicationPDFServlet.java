package elms.servlet;

import elms.DAO.LeaveApplicationDAO;
import elms.DAO.EmployeeDAO;
import elms.DAO.LeaveTypeDAO;
import elms.DAO.ManagerDAO;
import elms.model.LeaveApplication;
import elms.model.Employee;
import elms.model.LeaveType;
import elms.model.Manager;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Admin servlet to generate PDF view of leave applications
 */
@WebServlet("/admin-leave-application-pdf")
public class AdminLeaveApplicationPDFServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private LeaveApplicationDAO leaveApplicationDAO;
    private LeaveTypeDAO leaveTypeDAO;
    private ManagerDAO managerDAO;
    
    @Override
    public void init() throws ServletException {
        super.init();
        try {
            leaveApplicationDAO = new LeaveApplicationDAO();
            leaveTypeDAO = new LeaveTypeDAO();
            managerDAO = new ManagerDAO();
        } catch (Exception e) {
            throw new ServletException("Failed to initialize DAOs", e);
        }
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        System.out.println("=== ADMIN LEAVE APPLICATION PDF REQUEST ===");
        
        // Check if admin is logged in
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("adminId") == null) {
            response.sendRedirect(request.getContextPath() + "/Admin/AdminLogin.jsp");
            return;
        }
        
        String adminId = (String) session.getAttribute("adminId");
        System.out.println("Admin ID: " + adminId);
        
        String applicationId = request.getParameter("applicationId");
        if (applicationId == null || applicationId.trim().isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Application ID is required");
            return;
        }
        
        try {
            // Get application details
            LeaveApplication application = leaveApplicationDAO.getLeaveApplicationById(applicationId);
            if (application == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Leave application not found");
                return;
            }
            
            // Admin can view any application, no security check needed
            System.out.println("Application found: " + applicationId + " for employee: " + application.getEmployeeid());
            
            // Get employee details
            Employee applicant = EmployeeDAO.getEmployeeByIdOnly(application.getEmployeeid());
            if (applicant == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Employee not found");
                return;
            }
            
            // Get leave type details
            LeaveType leaveType = leaveTypeDAO.getLeaveTypeById(application.getLeavetypeid());
            
            // Get manager details if application has been reviewed
            Manager reviewer = null;
            if (application.getManagerid() != null && !application.getManagerid().trim().isEmpty()) {
                try {
                    reviewer = managerDAO.getManagerById(application.getManagerid());
                    System.out.println("🔍 Manager lookup result for ID " + application.getManagerid() + ": " + 
                                     (reviewer != null ? reviewer.getManagername() : "NOT FOUND"));
                } catch (Exception e) {
                    System.err.println("Error getting manager details: " + e.getMessage());
                }
            }
            
            // Generate HTML for PDF printing
            generateAdminPDFView(response, application, applicant, leaveType, reviewer, adminId);
            
        } catch (Exception e) {
            System.err.println("❌ Error generating admin PDF view: " + e.getMessage());
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error generating PDF view");
        }
    }
    
    private void generateAdminPDFView(HttpServletResponse response, LeaveApplication application, 
                                    Employee applicant, LeaveType leaveType, Manager reviewer, String adminId) throws IOException {
        
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        
        PrintWriter out = response.getWriter();
        
        // Debug output to console
        System.out.println("🔍 GENERATING PDF - DEBUG INFO:");
        System.out.println("   Application ID: " + application.getApplicationid());
        System.out.println("   Manager ID from application: " + application.getManagerid());
        System.out.println("   Reviewer object: " + (reviewer != null ? "Found" : "NULL"));
        if (reviewer != null) {
            System.out.println("   Manager Name: " + reviewer.getManagername());
            System.out.println("   Manager Position: " + reviewer.getManagerposition());
        }
        System.out.println("   Review Date: " + application.getReviewdate());
        System.out.println("   Application Status: " + application.getLeavestatus());
        
        // Generate professional form HTML with manager details
        out.println("<!DOCTYPE html>");
        out.println("<html lang='en'>");
        out.println("<head>");
        out.println("    <meta charset='UTF-8'>");
        out.println("    <meta name='viewport' content='width=device-width, initial-scale=1.0'>");
        out.println("    <title>Leave Application Form - " + application.getApplicationid() + " (Admin View)</title>");
        
        // Include CSS styles
        out.println("    <style>");
        out.println("        @page { size: A4; margin: 10mm; }");
        out.println("        @media print {");
        out.println("            .no-print { display: none !important; }");
        out.println("            body { print-color-adjust: exact; }");
        out.println("        }");
        out.println("        body {");
        out.println("            font-family: 'Arial', 'Times New Roman', serif;");
        out.println("            line-height: 1.2; color: #000; max-width: 210mm;");
        out.println("            margin: 0 auto; padding: 0; background: white; font-size: 12px;");
        out.println("        }");
        out.println("        .form-container { border: 2px solid #000; position: relative; display: flex; flex-direction: column; }");
        out.println("        .form-header { border-bottom: 2px solid #000; padding: 10px 15px; display: flex; align-items: center; height: 60px; flex-shrink: 0; }");
        out.println("        .logo-section { width: 60px; height: 60px; border: 1px solid #000; margin-right: 15px;");
        out.println("            display: flex; align-items: center; justify-content: center; background: #f9f9f9; font-size: 8px; text-align: center; }");
        out.println("        .header-content { flex: 1; }");
        out.println("        .company-name { font-size: 16px; font-weight: bold; margin: 0 0 3px 0; text-transform: uppercase; }");
        out.println("        .form-title { font-size: 14px; font-weight: bold; margin: 3px 0; text-decoration: underline; }");
        out.println("        .form-number { font-size: 10px; margin: 3px 0 0 0; }");
        out.println("        .admin-note { background: #e3f2fd; color: #1976d2; padding: 5px 10px; font-size: 10px; border: 1px solid #1976d2; margin-top: 5px; }");
        out.println("        .form-body { padding: 12px; flex: 1; }");
        out.println("        .form-section { margin-bottom: 12px; border: 1px solid #000; }");
        out.println("        .section-title { background: #f0f0f0; padding: 5px 10px; margin: 0; font-weight: bold; font-size: 12px; border-bottom: 1px solid #000; }");
        out.println("        .section-content { padding: 10px; }");
        out.println("        .form-row { display: flex; margin-bottom: 8px; align-items: center; }");
        out.println("        .form-row.full-width { flex-direction: column; align-items: flex-start; }");
        out.println("        .field-label { font-weight: bold; min-width: 130px; margin-right: 8px; font-size: 11px; }");
        out.println("        .field-value { flex: 1; border-bottom: 1px solid #000; padding: 1px 4px; min-height: 14px; font-size: 11px; }");
        out.println("        .field-value.multi-line { border: 1px solid #000; padding: 6px; width: 100%; margin-top: 3px; white-space: pre-wrap; }");
        out.println("        .status-box { border: 2px solid #000; padding: 4px; text-align: center; font-weight: bold; background: #f9f9f9; font-size: 10px; }");
        out.println("        .status-pending { background: #fff3cd; } .status-approved { background: #d4edda; }");
        out.println("        .status-rejected { background: #f8d7da; } .status-cancelled { background: #e2e3e5; }");
        out.println("        .form-footer { border-top: 1px solid #000; padding: 6px 15px; font-size: 8px; text-align: center; background: #f9f9f9; flex-shrink: 0; }");
        out.println("        .print-controls { text-align: center; margin: 15px 0; padding: 12px; background: #f0f8ff; border: 1px solid #007bff; border-radius: 5px; }");
        out.println("        .print-button { background: #007bff; color: white; border: none; padding: 10px 20px; border-radius: 4px; cursor: pointer; font-size: 12px; margin: 0 8px; font-weight: bold; }");
        out.println("        .print-button:hover { background: #0056b3; }");
        out.println("        .print-button.secondary { background: #6c757d; } .print-button.secondary:hover { background: #545b62; }");
        out.println("        .two-column { display: flex; gap: 15px; } .column { flex: 1; width: 50%; min-width: 0; }");
        out.println("        .rejection-reason { background: #f8d7da; border: 1px solid #f5c6cb; padding: 8px; margin-top: 8px; border-radius: 3px; }");
        out.println("        .rejection-reason strong { color: #721c24; }");
        out.println("    </style>");
        out.println("</head>");
        out.println("<body>");
        
        // Print Controls
        out.println("    <div class='print-controls no-print'>");
        out.println("        <h3>🖨️ Leave Application Form - Admin View</h3>");
        out.println("        <p>Application ID: <strong>" + application.getApplicationid() + "</strong> | Employee: <strong>" + applicant.getEmployeeName() + "</strong></p>");
        out.println("        <button class='print-button' onclick='window.print()'>");
        out.println("            <i class='fas fa-print'></i> Print Form");
        out.println("        </button>");
        out.println("        <button class='print-button secondary' onclick='window.close()'>");
        out.println("            <i class='fas fa-times'></i> Close");
        out.println("        </button>");
        out.println("    </div>");
        
        out.println("    <div class='form-container'>");
        
        // Form Header
        out.println("        <div class='form-header'>");
        out.println("            <div class='logo-section'>");
        out.println("                <img src='data:image/png;base64,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' alt='IMNSB Logo' style='width: 100%; height: 100%; object-fit: contain;'>");
        out.println("            </div>");
        out.println("            <div class='header-content'>");
        out.println("                <div class='company-name'>IMNSB Employee Leave Management System</div>");
        out.println("                <div class='form-title'>LEAVE APPLICATION FORM</div>");
        out.println("                <div class='form-number'>Form No: " + application.getApplicationid() + " | Generated: " + new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()) + "</div>");
        out.println("                <div class='admin-note'>ADMIN VIEW - Viewed by: " + adminId + "</div>");
        out.println("            </div>");
        out.println("        </div>");
        
        // Form Body
        out.println("        <div class='form-body'>");
        
        // Employee Information Section
        out.println("            <div class='form-section'>");
        out.println("                <h3 class='section-title'>SECTION A: EMPLOYEE INFORMATION</h3>");
        out.println("                <div class='section-content'>");
        out.println("                    <div class='two-column'>");
        out.println("                        <div class='column'>");
        out.println("                            <div class='form-row'>");
        out.println("                                <span class='field-label'>Employee ID:</span>");
        out.println("                                <span class='field-value'>" + escapeHtml(applicant.getEmployeeId()) + "</span>");
        out.println("                            </div>");
        out.println("                            <div class='form-row'>");
        out.println("                                <span class='field-label'>Employee Name:</span>");
        out.println("                                <span class='field-value'>" + escapeHtml(applicant.getEmployeeName()) + "</span>");
        out.println("                            </div>");
        out.println("                        </div>");
        out.println("                        <div class='column'>");
        out.println("                            <div class='form-row'>");
        out.println("                                <span class='field-label'>Email Address:</span>");
        out.println("                                <span class='field-value'>" + escapeHtml(applicant.getEmployeeEmail()) + "</span>");
        out.println("                            </div>");
        out.println("                            <div class='form-row'>");
        out.println("                                <span class='field-label'>Mobile Number:</span>");
        out.println("                                <span class='field-value'>" + escapeHtml(applicant.getEmployeeNoPhone()) + "</span>");
        out.println("                            </div>");
        out.println("                        </div>");
        out.println("                    </div>");
        out.println("                </div>");
        out.println("            </div>");
        
        // Leave Details Section
        out.println("            <div class='form-section'>");
        out.println("                <h3 class='section-title'>SECTION B: LEAVE DETAILS</h3>");
        out.println("                <div class='section-content'>");
        out.println("                    <div class='two-column'>");
        out.println("                        <div class='column'>");
        out.println("                            <div class='form-row'>");
        out.println("                                <span class='field-label'>Leave Type:</span>");
        out.println("                                <span class='field-value'>" + (leaveType != null ? escapeHtml(leaveType.getLeaveTypeName()) : "N/A") + "</span>");
        out.println("                            </div>");
        out.println("                            <div class='form-row'>");
        out.println("                                <span class='field-label'>Start Date:</span>");
        out.println("                                <span class='field-value'>" + formatDate(application.getLeavestartdate()) + "</span>");
        out.println("                            </div>");
        out.println("                            <div class='form-row'>");
        out.println("                                <span class='field-label'>End Date:</span>");
        out.println("                                <span class='field-value'>" + formatDate(application.getLeaveenddate()) + "</span>");
        out.println("                            </div>");
        out.println("                        </div>");
        out.println("                        <div class='column'>");
        out.println("                            <div class='form-row'>");
        out.println("                                <span class='field-label'>Duration:</span>");
        out.println("                                <span class='field-value'>" + application.getLeaveduration() + " day(s)</span>");
        out.println("                            </div>");
        out.println("                            <div class='form-row'>");
        out.println("                                <span class='field-label'>Application Date:</span>");
        out.println("                                <span class='field-value'>" + formatDate(application.getAppliedon()) + "</span>");
        out.println("                            </div>");
        out.println("                            <div class='form-row'>");
        out.println("                                <span class='field-label'>Status:</span>");
        out.println("                                <span class='field-value'>");
        
        String status = application.getLeavestatus();
        String statusClass = "status-box ";
        if ("Pending".equalsIgnoreCase(status)) {
            statusClass += "status-pending";
        } else if ("Approved".equalsIgnoreCase(status)) {
            statusClass += "status-approved";
        } else if ("Rejected".equalsIgnoreCase(status)) {
            statusClass += "status-rejected";
        } else if ("Cancelled".equalsIgnoreCase(status)) {
            statusClass += "status-cancelled";
        }
        
        out.println("                                    <div class='" + statusClass + "'>" + escapeHtml(status).toUpperCase() + "</div>");
        out.println("                                </span>");
        out.println("                            </div>");
        out.println("                        </div>");
        out.println("                    </div>");
        out.println("                </div>");
        out.println("            </div>");
        
        // Leave Reason Section
        out.println("            <div class='form-section'>");
        out.println("                <h3 class='section-title'>SECTION C: REASON FOR LEAVE</h3>");
        out.println("                <div class='section-content'>");
        out.println("                    <div class='form-row full-width'>");
        out.println("                        <span class='field-label'>Please state the reason for your leave application:</span>");
        out.println("                        <div class='field-value multi-line' style='min-height: 40px;'>");
        String reason = application.getLeavereason();
        if (reason == null || reason.trim().isEmpty()) {
            out.println("No specific reason provided.");
        } else {
            out.println(escapeHtml(reason));
        }
        out.println("                        </div>");
        out.println("                    </div>");
        
        // Show rejection reason if applicable
        if ("Rejected".equalsIgnoreCase(status) && application.getRejectReason() != null && 
            !application.getRejectReason().trim().isEmpty()) {
            out.println("                    <div class='rejection-reason'>");
            out.println("                        <strong>Rejection Reason:</strong><br>");
            out.println("                        " + escapeHtml(application.getRejectReason()));
            out.println("                    </div>");
        }
        out.println("                </div>");
        out.println("            </div>");
        
        // Manager Approval Section - Enhanced with manager details
        out.println("            <div class='form-section'>");
        out.println("                <h3 class='section-title'>SECTION D: FOR OFFICIAL USE ONLY</h3>");
        out.println("                <div class='section-content'>");
        out.println("                    <div class='two-column'>");
        out.println("                        <div class='column'>");
        out.println("                            <div class='form-row'>");
        out.println("                                <span class='field-label'>☐ Approved</span>");
        out.println("                            </div>");
        out.println("                            <div class='form-row'>");
        out.println("                                <span class='field-label'>☐ Rejected</span>");
        out.println("                            </div>");
        out.println("                            <div class='form-row'>");
        out.println("                                <span class='field-label'>☐ Pending Review</span>");
        out.println("                            </div>");
        out.println("                        </div>");
        out.println("                        <div class='column'>");
        
        // Manager ID Field
        out.println("                            <div class='form-row'>");
        out.println("                                <span class='field-label'>Manager ID:</span>");
        out.println("                                <span class='field-value'>");
        if (application.getManagerid() != null && !application.getManagerid().trim().isEmpty()) {
            out.println(escapeHtml(application.getManagerid()));
            System.out.println("✅ Manager ID displayed: " + application.getManagerid());
        } else {
            out.println("&nbsp;");
            System.out.println("⚠️ No Manager ID found");
        }
        out.println("                                </span>");
        out.println("                            </div>");
        
        // Manager Name Field
        out.println("                            <div class='form-row'>");
        out.println("                                <span class='field-label'>Manager Name:</span>");
        out.println("                                <span class='field-value'>");
        if (reviewer != null && reviewer.getManagername() != null && !reviewer.getManagername().trim().isEmpty()) {
            out.println(escapeHtml(reviewer.getManagername()));
            if (reviewer.getManagerposition() != null && !reviewer.getManagerposition().trim().isEmpty()) {
                out.println("<br><small style='font-size: 9px; color: #666;'>(" + escapeHtml(reviewer.getManagerposition()) + ")</small>");
            }
            System.out.println("✅ Manager Name displayed: " + reviewer.getManagername());
        } else if (application.getManagerid() != null && !application.getManagerid().trim().isEmpty()) {
            // Fallback: show that we have a manager ID but couldn't get the name
            out.println("<small style='color: #666;'>Manager: " + escapeHtml(application.getManagerid()) + "</small>");
            System.out.println("⚠️ Manager ID exists but name not found for: " + application.getManagerid());
        } else {
            out.println("&nbsp;");
            System.out.println("⚠️ No Manager information available");
        }
        out.println("                                </span>");
        out.println("                            </div>");
        
        // Review Date Field
        out.println("                            <div class='form-row'>");
        out.println("                                <span class='field-label'>Review Date:</span>");
        out.println("                                <span class='field-value'>");
        if (application.getReviewdate() != null && !application.getReviewdate().trim().isEmpty()) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM dd, yyyy 'at' hh:mm a");
                Date reviewDate = inputFormat.parse(application.getReviewdate());
                out.println(outputFormat.format(reviewDate));
                System.out.println("✅ Review Date displayed: " + outputFormat.format(reviewDate));
            } catch (Exception e) {
                // Fallback to raw date if parsing fails
                out.println(escapeHtml(application.getReviewdate()));
                System.out.println("⚠️ Review Date parsing failed, showing raw: " + application.getReviewdate());
            }
        } else if (!"Pending".equalsIgnoreCase(application.getLeavestatus())) {
            // If status is not pending but no review date, show a placeholder
            out.println("<small style='color: #666;'>Review completed</small>");
            System.out.println("⚠️ Status is " + application.getLeavestatus() + " but no review date found");
        } else {
            out.println("&nbsp;");
            System.out.println("ℹ️ Application is pending - no review date yet");
        }
        out.println("                                </span>");
        out.println("                            </div>");
        
        out.println("                        </div>");
        out.println("                    </div>");
        
        // Add signature area for processed applications
        if (!"Pending".equalsIgnoreCase(application.getLeavestatus())) {
            out.println("                    <div style='margin-top: 15px; border-top: 1px solid #ccc; padding-top: 10px;'>");
            out.println("                        <div class='form-row'>");
            out.println("                            <span class='field-label' style='min-width: 100px;'>Manager Signature:</span>");
            out.println("                            <span class='field-value' style='min-height: 25px; border-bottom: 1px solid #000;'>&nbsp;</span>");
            out.println("                        </div>");
            out.println("                    </div>");
        }
        
        out.println("                </div>");
        out.println("            </div>");
        
        out.println("        </div>");
        
        // Form Footer
        out.println("        <div class='form-footer'>");
        out.println("            <div>IMNSB Employee Leave Management System | Admin View | Generated: " + new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()) + " | Application ID: " + application.getApplicationid() + " | Viewed by Admin: " + adminId + "</div>");
        out.println("        </div>");
        
        out.println("    </div>");
        
        out.println("    <script>");
        out.println("        document.addEventListener('DOMContentLoaded', function() {");
        out.println("            const status = '" + escapeHtml(status) + "'.toLowerCase();");
        out.println("            const checkboxes = document.querySelectorAll('.field-label');");
        out.println("            checkboxes.forEach(function(checkbox) {");
        out.println("                const text = checkbox.textContent.toLowerCase();");
        out.println("                if ((status === 'approved' && text.includes('approved')) ||");
        out.println("                    (status === 'rejected' && text.includes('rejected')) ||");
        out.println("                    (status === 'pending' && text.includes('pending'))) {");
        out.println("                    checkbox.textContent = checkbox.textContent.replace('☐', '☑');");
        out.println("                }");
        out.println("            });");
        out.println("        });");
        out.println("    </script>");
        
        out.println("</body>");
        out.println("</html>");
        
        out.flush();
        System.out.println("✅ Admin PDF view generated successfully for application: " + application.getApplicationid());
        System.out.println("🏁 Manager approval section generated successfully");
    }
    
    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&#x27;");
    }
    
    private String formatDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return "N/A";
        }
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM dd, yyyy");
            Date date = inputFormat.parse(dateStr);
            return outputFormat.format(date);
        } catch (Exception e) {
            return dateStr;
        }
    }
}
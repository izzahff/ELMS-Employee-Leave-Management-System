package elms.servlet;

import elms.DAO.LeaveApplicationDAO;
import elms.DAO.LeaveBalanceDAO;
import elms.DAO.EmployeeDAO;
import elms.DAO.LeaveTypeDAO;
import elms.DAO.ManagerDAO;
import elms.model.LeaveApplication;
import elms.model.Employee;
import elms.model.Manager;
import elms.model.LeaveType;

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
 * Servlet for managers to view and review leave applications
 * UPDATED: Balance display now shows current balance since no deduction happens on submission
 */
@WebServlet("/manager-leave-details")
public class ManagerLeaveApplicationServlet extends HttpServlet {
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
        
        System.out.println("=== MANAGER LEAVE APPLICATION REVIEW REQUEST ===");
        
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
            
            // Get employee details
            Employee applicant = EmployeeDAO.getEmployeeByIdOnly(application.getEmployeeid());
            if (applicant == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Employee not found");
                return;
            }
            
            // Get leave type details
            LeaveType leaveType = leaveTypeDAO.getLeaveTypeById(application.getLeavetypeid());
            
            // Generate HTML for manager review
            generateManagerReviewView(response, application, applicant, leaveType, manager);
            
        } catch (Exception e) {
            System.err.println("❌ Error generating manager review view: " + e.getMessage());
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error generating review view");
        }
    }
    
    private void generateManagerReviewView(HttpServletResponse response, LeaveApplication application, 
                                         Employee applicant, LeaveType leaveType, Manager manager) throws IOException {
        
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        
        PrintWriter out = response.getWriter();
        
        // Generate professional form HTML with manager controls
        out.println("<div class='manager-review-container'>");
        
        // Application Header with Status
        out.println("    <div class='review-header'>");
        out.println("        <div class='review-title'>");
        out.println("            <h3><i class='fas fa-file-alt'></i> Leave Application Review</h3>");
        out.println("            <div class='application-meta'>");
        out.println("                <span class='app-id'>Application ID: <strong>" + application.getApplicationid() + "</strong></span>");
        
        String status = application.getLeavestatus();
        String statusClass = "status-badge ";
        if ("Pending".equalsIgnoreCase(status)) {
            statusClass += "status-pending";
        } else if ("Approved".equalsIgnoreCase(status)) {
            statusClass += "status-approved";
        } else if ("Rejected".equalsIgnoreCase(status)) {
            statusClass += "status-rejected";
        } else if ("Cancelled".equalsIgnoreCase(status)) {
            statusClass += "status-cancelled";
        }
        
        out.println("                <span class='" + statusClass + "'>" + escapeHtml(status) + "</span>");
        out.println("            </div>");
        out.println("        </div>");
        out.println("    </div>");
        
        // Employee Information Section
        out.println("    <div class='review-section'>");
        out.println("        <h4><i class='fas fa-user'></i> Employee Information</h4>");
        out.println("        <div class='info-grid'>");
        out.println("            <div class='info-item'>");
        out.println("                <label>Employee ID:</label>");
        out.println("                <span>" + escapeHtml(applicant.getEmployeeId()) + "</span>");
        out.println("            </div>");
        out.println("            <div class='info-item'>");
        out.println("                <label>Employee Name:</label>");
        out.println("                <span>" + escapeHtml(applicant.getEmployeeName()) + "</span>");
        out.println("            </div>");
        out.println("            <div class='info-item'>");
        out.println("                <label>Email Address:</label>");
        out.println("                <span>" + escapeHtml(applicant.getEmployeeEmail()) + "</span>");
        out.println("            </div>");
        out.println("            <div class='info-item'>");
        out.println("                <label>Mobile Number:</label>");
        out.println("                <span>" + escapeHtml(applicant.getEmployeeNoPhone()) + "</span>");
        out.println("            </div>");
	     // Display balance only for the specific leave type being applied
	        if (leaveType != null && leaveType.isAffectsBalance()) {
	            double currentBalance = elms.DAO.LeaveBalanceDAO.calculateBalanceForLeaveType(
	                applicant.getEmployeeId(), 
	                application.getLeavetypeid()
	            );
	            
	            out.println("            <div class='info-item'>");
	            out.println("                <label>" + escapeHtml(leaveType.getLeaveTypeName()) + " Balance:</label>");
	            out.println("                <span class='balance-highlight'>" + String.format("%.1f", currentBalance) + " days</span>");
	            out.println("            </div>");
	        }
        out.println("        </div>");
        out.println("    </div>");
        
     // Leave Details Section
        out.println("    <div class='review-section'>");
        out.println("        <h4><i class='fas fa-calendar-alt'></i> Leave Details</h4>");
        out.println("        <div class='info-grid'>");
        out.println("            <div class='info-item'>");
        out.println("                <label>Leave Type:</label>");
        out.println("                <span class='leave-type-highlight'>" + (leaveType != null ? escapeHtml(leaveType.getLeaveTypeName()) : "N/A") + "</span>");
        out.println("            </div>");
        out.println("            <div class='info-item'>");
        out.println("                <label>Duration:</label>");
        out.println("                <span class='duration-highlight'>" + application.getLeaveduration() + " day(s)</span>");
        out.println("            </div>");
        out.println("            <div class='info-item'>");
        out.println("                <label>Start Date:</label>");
        out.println("                <span>" + formatDate(application.getLeavestartdate()) + "</span>");
        out.println("            </div>");
        out.println("            <div class='info-item'>");
        out.println("                <label>End Date:</label>");
        out.println("                <span>" + formatDate(application.getLeaveenddate()) + "</span>");
        out.println("            </div>");
        out.println("            <div class='info-item'>");
        out.println("                <label>Application Date:</label>");
        out.println("                <span>" + formatDateTime(application.getAppliedon()) + "</span>");
        out.println("            </div>");

                // Calculate urgency - KEEP ONLY ONE
                out.println("            <div class='info-item'>");
                out.println("                <label>Priority:</label>");
                out.println("                <span>");
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    Date startDate = sdf.parse(application.getLeavestartdate());
                    Date currentDate = new Date();
                    long daysDiff = (startDate.getTime() - currentDate.getTime()) / (1000 * 60 * 60 * 24);
                    
                    if (daysDiff <= 3) {
                        out.println("                    <span class='priority-urgent'><i class='fas fa-exclamation-triangle'></i> Urgent (Within 3 days)</span>");
                    } else {
                        out.println("                    <span class='priority-normal'><i class='fas fa-clock'></i> Normal</span>");
                    }
                } catch (Exception e) {
                    out.println("                    <span class='priority-normal'><i class='fas fa-clock'></i> Normal</span>");
                }
                out.println("                </span>");
                out.println("            </div>");

             // ✅ Application Type - Enhanced with Overdue Detection
                out.println("            <div class='info-item'>");
                out.println("                <label>Application Type:</label>");
                out.println("                <span>");
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    Date startDate = sdf.parse(application.getLeavestartdate());
                    Date appliedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(application.getAppliedon());
                    Date currentDate = new Date();
                    
                    // Compare only dates (ignore time)
                    SimpleDateFormat dateOnly = new SimpleDateFormat("yyyy-MM-dd");
                    String startDateStr = dateOnly.format(startDate);
                    String appliedDateStr = dateOnly.format(appliedDate);
                    String currentDateStr = dateOnly.format(currentDate);
                    
                    Date startDateOnly = dateOnly.parse(startDateStr);
                    Date appliedDateOnly = dateOnly.parse(appliedDateStr);
                    Date currentDateOnly = dateOnly.parse(currentDateStr);
                    
                    System.out.println("=== APPLICATION TYPE CHECK ===");
                    System.out.println("Start Date: " + startDateStr);
                    System.out.println("Applied Date: " + appliedDateStr);
                    System.out.println("Current Date: " + currentDateStr);
                    System.out.println("Is Backdated (Start < Applied): " + startDateOnly.before(appliedDateOnly));
                    System.out.println("Is Overdue (Start < Current): " + startDateOnly.before(currentDateOnly));
                    
                    if (startDateOnly.before(appliedDateOnly)) {
                        // BACKDATED: Applied after leave already started
                        long daysPast = (appliedDateOnly.getTime() - startDateOnly.getTime()) / (1000 * 60 * 60 * 24);
                        out.println("                    <span class='backdated-application'>");
                        out.println("                        <i class='fas fa-history'></i> Backdated Application");
                        out.println("                        <span class='backdated-details'>(" + daysPast + " day" + (daysPast > 1 ? "s" : "") + " past)</span>");
                        out.println("                    </span>");
                    } else if (startDateOnly.before(currentDateOnly) && "Pending".equalsIgnoreCase(status)) {
                        // OVERDUE: Applied in advance but start date has passed while still pending
                        long daysOverdue = (currentDateOnly.getTime() - startDateOnly.getTime()) / (1000 * 60 * 60 * 24);
                        out.println("                    <span class='overdue-application'>");
                        out.println("                        <i class='fas fa-clock'></i> Overdue Application");
                        out.println("                        <span class='overdue-details'>(started " + daysOverdue + " day" + (daysOverdue > 1 ? "s" : "") + " ago)</span>");
                        out.println("                    </span>");
                    } else {
                        // REGULAR: Normal future leave application
                        out.println("                    <span class='regular-application'><i class='fas fa-calendar-check'></i> Regular Application</span>");
                    }
                } catch (Exception e) {
                    System.err.println("Error checking application type: " + e.getMessage());
                    e.printStackTrace();
                    out.println("                    <span class='regular-application'><i class='fas fa-calendar-check'></i> Regular Application</span>");
                }
                out.println("                </span>");
                out.println("            </div>");
 
                out.println("        </div>"); // ✅ Close info-grid
                out.println("    </div>"); // ✅ Close review-section

    
        // Leave Reason Section
        out.println("    <div class='review-section'>");
        out.println("        <h4><i class='fas fa-comment-alt'></i> Reason for Leave</h4>");
        out.println("        <div class='reason-content'>");
        String reason = application.getLeavereason();
        if (reason == null || reason.trim().isEmpty()) {
            out.println("            <p class='no-reason'>No specific reason provided.</p>");
        } else {
            out.println("            <p>" + escapeHtml(reason) + "</p>");
        }
        out.println("        </div>");
        out.println("    </div>");
        
        // Attachment Section (if applicable)
        if (application.getAttachment() != null && !application.getAttachment().trim().isEmpty()) {
            out.println("    <div class='review-section'>");
            out.println("        <h4><i class='fas fa-paperclip'></i> Attachments</h4>");
            out.println("        <div class='attachment-info'>");
            out.println("            <p><i class='fas fa-file'></i> Attachment: " + escapeHtml(application.getAttachment()) + "</p>");
            out.println("            <a href='" + "/ELMS_3.0" + "/manager-file-download?file=" + application.getAttachment() + "&applicationId=" + application.getApplicationid() + "' ");
            out.println("               target='_blank' class='btn btn-sm btn-attachment'>");
            out.println("                <i class='fas fa-download'></i> Download Attachment");
            out.println("            </a>");
            out.println("        </div>");
            out.println("    </div>");
        }
        
        // Manager Review Section (only show for pending applications)
        if ("Pending".equalsIgnoreCase(status)) {
            out.println("    <div class='review-section manager-actions'>");
            out.println("        <h4><i class='fas fa-user-tie'></i> Manager Review</h4>");
            out.println("        <div class='review-actions'>");
            out.println("            <div class='action-info'>");
            out.println("                <p><strong>Reviewing Manager:</strong> " + escapeHtml(manager.getManagername()) + " (" + escapeHtml(manager.getManagerid()) + ")</p>");
            out.println("                <p class='review-note'><i class='fas fa-info-circle'></i> Please review the application details above and make your decision below.</p>");
            out.println("            </div>");
         
            
         // ✅ ADD BACKDATED WARNING IF APPLICABLE
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date startDate = sdf.parse(application.getLeavestartdate());
                Date appliedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(application.getAppliedon());
                Date currentDate = new Date();
                
                // Compare only dates (ignore time)
                SimpleDateFormat dateOnly = new SimpleDateFormat("yyyy-MM-dd");
                String startDateStr = dateOnly.format(startDate);
                String appliedDateStr = dateOnly.format(appliedDate);
                String currentDateStr = dateOnly.format(currentDate);
                
                Date startDateOnly = dateOnly.parse(startDateStr);
                Date appliedDateOnly = dateOnly.parse(appliedDateStr);
                Date currentDateOnly = dateOnly.parse(currentDateStr);
                
                if (startDateOnly.before(appliedDateOnly)) {
                    // BACKDATED WARNING
                    long daysPast = (appliedDateOnly.getTime() - startDateOnly.getTime()) / (1000 * 60 * 60 * 24);
                    out.println("            <div class='backdated-warning-box'>");
                    out.println("                <i class='fas fa-exclamation-triangle'></i>");
                    out.println("                <div>");
                    out.println("                    <strong>Backdated Application Notice:</strong>");
                    out.println("                    <p>This application is for a past date (" + formatDate(application.getLeavestartdate()) + "), ");
                    out.println("                    which was " + daysPast + " day" + (daysPast > 1 ? "s" : "") + " before the application was submitted. ");
                    out.println("                    Please verify the reason and ensure proper justification before approval.</p>");
                    out.println("                </div>");
                    out.println("            </div>");
                } else if (startDateOnly.before(currentDateOnly)) {
                    // OVERDUE WARNING
                    long daysOverdue = (currentDateOnly.getTime() - startDateOnly.getTime()) / (1000 * 60 * 60 * 24);
                    out.println("            <div class='overdue-warning-box'>");
                    out.println("                <i class='fas fa-clock'></i>");
                    out.println("                <div>");
                    out.println("                    <strong>Overdue Application Notice:</strong>");
                    out.println("                    <p>This leave was scheduled to start on " + formatDate(application.getLeavestartdate()) + ", ");
                    out.println("                    which was " + daysOverdue + " day" + (daysOverdue > 1 ? "s" : "") + " ago. ");
                    out.println("                    The leave period may have already begun. Please review urgently.</p>");
                    out.println("                </div>");
                    out.println("            </div>");
                }
            } catch (Exception e) {
                // Ignore date parsing errors
                System.err.println("Error checking backdated/overdue status: " + e.getMessage());
            }

            // UPDATED: Balance check section with new logic - no deduction has happened yet
            out.println("            <div class='balance-check'>");
            
         // Check if THIS leave type affects balance (not just "Annual Leave")
            if (leaveType != null && leaveType.isAffectsBalance()) {
                // Calculate current balance for THIS specific leave type
                double currentBalance = elms.DAO.LeaveBalanceDAO.calculateBalanceForLeaveType(
                    applicant.getEmployeeId(), 
                    application.getLeavetypeid()
                );
                double requestedDuration = application.getLeaveduration();
                double remainingBalance = currentBalance - requestedDuration;
                
                if (remainingBalance < 0) {
                    out.println("                <div class='balance-warning'>");
                    out.println("                    <i class='fas fa-exclamation-triangle'></i> ");
                    out.println("                    <strong>Insufficient " + escapeHtml(leaveType.getLeaveTypeName()) + " Balance:</strong> Employee currently has " + String.format("%.1f", currentBalance) + " days available but is requesting " + requestedDuration + " days. ");
                    out.println("                    This application cannot be approved due to insufficient balance.");
                    out.println("                </div>");
                    out.println("                <div class='balance-note'>");
                    out.println("                    <i class='fas fa-info-circle'></i> ");
                    out.println("                    <em>Note: Balance is calculated dynamically. When approved, this leave will be automatically included in the balance calculation.</em>");
                    out.println("                </div>");
                } else {
                    out.println("                <div class='balance-ok'>");
                    out.println("                    <i class='fas fa-check-circle'></i> ");
                    out.println("                    <strong>" + escapeHtml(leaveType.getLeaveTypeName()) + " Balance Check:</strong> Employee currently has " + String.format("%.1f", currentBalance) + " days available. ");
                    out.println("                    If approved, employee will have " + String.format("%.1f", remainingBalance) + " days remaining after this leave.");
                    out.println("                </div>");
                    out.println("                <div class='balance-note'>");
                    out.println("                    <i class='fas fa-info-circle'></i> ");
                    out.println("                    <em>Note: Balance is calculated dynamically. When approved, this leave will be automatically included in the balance calculation.</em>");
                    out.println("                </div>");
                }
            } else {
                out.println("                <div class='balance-info'>");
                out.println("                    <i class='fas fa-info-circle'></i> ");
                out.println("                    <strong>Leave Type:</strong> " + (leaveType != null ? escapeHtml(leaveType.getLeaveTypeName()) : "Unknown") + " - No balance tracking required.");
                out.println("                    <br><em>This leave type does not affect any leave balance. It can be approved without balance considerations.</em>");
                out.println("                </div>");
            }
            
            out.println("            </div>");
            out.println("        </div>");
            out.println("    </div>");
        } else {
            // Show review history for non-pending applications
            out.println("    <div class='review-section'>");
            out.println("        <h4><i class='fas fa-history'></i> Review History</h4>");
            out.println("        <div class='review-history'>");
            out.println("            <div class='history-item'>");
            out.println("                <span class='history-status " + statusClass + "'>" + escapeHtml(status) + "</span>");
            
            // Show manager who reviewed (using existing managerid field and ManagerDAO)
            if (application.getManagerid() != null && !application.getManagerid().trim().isEmpty()) {
                // Get manager details using existing ManagerDAO
                Manager reviewerManager = null;
                try {
                    reviewerManager = managerDAO.getManagerById(application.getManagerid());
                } catch (Exception e) {
                    System.err.println("Error getting manager details: " + e.getMessage());
                }
                
                out.println("                <span class='history-manager'>");
                out.println("                    <i class='fas fa-user-tie'></i>");
                out.println("                    Reviewed by: ");
                if (reviewerManager != null && reviewerManager.getManagername() != null) {
                    out.println("                    <strong>" + escapeHtml(reviewerManager.getManagername()) + "</strong>");
                    out.println("                    <span class='manager-details'>");
                    out.println("                        (" + escapeHtml(application.getManagerid()));
                    if (reviewerManager.getManagerposition() != null && !reviewerManager.getManagerposition().trim().isEmpty()) {
                        out.println("                        - " + escapeHtml(reviewerManager.getManagerposition()));
                    }
                    out.println("                        )");
                    out.println("                    </span>");
                } else {
                    out.println("                    <strong>Manager " + escapeHtml(application.getManagerid()) + "</strong>");
                }
                out.println("                </span>");
            }
            
            // Show when it was applied (since we don't have a separate review date)
            if (application.getAppliedon() != null && !application.getAppliedon().trim().isEmpty()) {
                out.println("                <span class='history-date'>");
                out.println("                    <i class='fas fa-calendar-alt'></i>");
                out.println("                    Application submitted: " + formatDateTime(application.getAppliedon()));
                out.println("                </span>");
            }
            
            out.println("            </div>");
            
            // Show rejection reason if available and status is rejected
            if ("Rejected".equalsIgnoreCase(status) && application.getRejectReason() != null && 
                !application.getRejectReason().trim().isEmpty()) {
                out.println("            <div class='rejection-reason'>");
                out.println("                <i class='fas fa-times-circle'></i>");
                out.println("                <div class='rejection-content'>");
                out.println("                    <strong>Rejection Reason:</strong>");
                out.println("                    <p>" + escapeHtml(application.getRejectReason()) + "</p>");
                out.println("                </div>");
                out.println("            </div>");
            }
            
            out.println("        </div>");
            out.println("    </div>");
        }
        
        // Add note for better user experience
        if (!"Pending".equalsIgnoreCase(status)) {
            out.println("    <div class='review-section'>");
            out.println("        <div class='review-note-final'>");
            out.println("            <i class='fas fa-info-circle'></i>");
            out.println("            <span>This application has been <strong>" + status.toLowerCase() + "</strong> and cannot be modified.</span>");
            out.println("        </div>");
            out.println("    </div>");
        }
        
        out.println("</div>");
        
        // Add CSS styles (same as before)
        out.println("<style>");
        out.println(".manager-review-container {");
        out.println("    max-width: 800px;");
        out.println("    margin: 0 auto;");
        out.println("    background: white;");
        out.println("    border-radius: 8px;");
        out.println("    overflow: hidden;");
        out.println("}");
        out.println("");
        out.println(".review-header {");
        out.println("    background: linear-gradient(135deg, #007bff, #0056b3);");
        out.println("    color: white;");
        out.println("    padding: 20px;");
        out.println("}");
        out.println("");
        out.println(".review-title h3 {");
        out.println("    margin: 0 0 10px 0;");
        out.println("    display: flex;");
        out.println("    align-items: center;");
        out.println("    gap: 10px;");
        out.println("    font-size: 1.4rem;");
        out.println("}");
        out.println("");
        out.println(".application-meta {");
        out.println("    display: flex;");
        out.println("    justify-content: space-between;");
        out.println("    align-items: center;");
        out.println("    flex-wrap: wrap;");
        out.println("    gap: 10px;");
        out.println("}");
        out.println("");
        out.println(".app-id {");
        out.println("    font-size: 1rem;");
        out.println("}");
        out.println("");
        out.println(".review-section {");
        out.println("    padding: 20px;");
        out.println("    border-bottom: 1px solid #eee;");
        out.println("}");
        out.println("");
        out.println(".review-section:last-child {");
        out.println("    border-bottom: none;");
        out.println("}");
        out.println("");
        out.println(".review-section h4 {");
        out.println("    margin: 0 0 15px 0;");
        out.println("    color: #333;");
        out.println("    display: flex;");
        out.println("    align-items: center;");
        out.println("    gap: 8px;");
        out.println("    font-size: 1.1rem;");
        out.println("    padding-bottom: 8px;");
        out.println("    border-bottom: 2px solid #007bff;");
        out.println("}");
        out.println("");
        out.println(".info-grid {");
        out.println("    display: grid;");
        out.println("    grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));");
        out.println("    gap: 15px;");
        out.println("}");
        out.println("");
        out.println(".info-item {");
        out.println("    display: flex;");
        out.println("    flex-direction: column;");
        out.println("    gap: 5px;");
        out.println("}");
        out.println("");
        out.println(".info-item label {");
        out.println("    font-weight: 600;");
        out.println("    color: #555;");
        out.println("    font-size: 0.9rem;");
        out.println("}");
        out.println("");
        out.println(".info-item span {");
        out.println("    padding: 8px 12px;");
        out.println("    background: #f8f9fa;");
        out.println("    border: 1px solid #dee2e6;");
        out.println("    border-radius: 4px;");
        out.println("    font-size: 0.95rem;");
        out.println("}");
        out.println("");
        out.println(".balance-highlight {");
        out.println("    background: #e8f4f8 !important;");
        out.println("    color: #0c5460 !important;");
        out.println("    font-weight: 600 !important;");
        out.println("}");
        out.println("");
        out.println(".leave-type-highlight {");
        out.println("    background: #f3e5f5 !important;");
        out.println("    color: #7b1fa2 !important;");
        out.println("    font-weight: 600 !important;");
        out.println("}");
        out.println("");
        out.println(".duration-highlight {");
        out.println("    background: #fff3cd !important;");
        out.println("    color: #856404 !important;");
        out.println("    font-weight: 600 !important;");
        out.println("}");
        out.println("");
        out.println(".priority-urgent {");
        out.println("    background: #ffebee !important;");
        out.println("    color: #c62828 !important;");
        out.println("    font-weight: 600 !important;");
        out.println("    padding: 4px 8px !important;");
        out.println("    border-radius: 12px !important;");
        out.println("    display: inline-flex !important;");
        out.println("    align-items: center !important;");
        out.println("    gap: 4px !important;");
        out.println("}");
        out.println("");
        out.println(".priority-normal {");
        out.println("    background: #e8f5e8 !important;");
        out.println("    color: #2e7d32 !important;");
        out.println("    font-weight: 600 !important;");
        out.println("    padding: 4px 8px !important;");
        out.println("    border-radius: 12px !important;");
        out.println("    display: inline-flex !important;");
        out.println("    align-items: center !important;");
        out.println("    gap: 4px !important;");
        out.println("}");
        out.println("");
        out.println(".reason-content {");
        out.println("    background: #f8f9fa;");
        out.println("    border: 1px solid #dee2e6;");
        out.println("    border-radius: 4px;");
        out.println("    padding: 15px;");
        out.println("    min-height: 60px;");
        out.println("}");
        out.println("");
        out.println(".reason-content p {");
        out.println("    margin: 0;");
        out.println("    line-height: 1.5;");
        out.println("}");
        out.println("");
        out.println(".no-reason {");
        out.println("    color: #6c757d;");
        out.println("    font-style: italic;");
        out.println("}");
        out.println("");
        out.println(".attachment-info {");
        out.println("    background: #f8f9fa;");
        out.println("    border: 1px solid #dee2e6;");
        out.println("    border-radius: 4px;");
        out.println("    padding: 15px;");
        out.println("}");
        out.println("");
        out.println(".attachment-info p {");
        out.println("    margin: 0 0 10px 0;");
        out.println("    display: flex;");
        out.println("    align-items: center;");
        out.println("    gap: 8px;");
        out.println("}");
        out.println("");
        out.println(".btn-attachment {");
        out.println("    background: #28a745;");
        out.println("    color: white;");
        out.println("    padding: 6px 12px;");
        out.println("    border-radius: 4px;");
        out.println("    text-decoration: none;");
        out.println("    display: inline-flex;");
        out.println("    align-items: center;");
        out.println("    gap: 6px;");
        out.println("    font-size: 0.9rem;");
        out.println("    transition: background 0.3s;");
        out.println("}");
        out.println("");
        out.println(".btn-attachment:hover {");
        out.println("    background: #218838;");
        out.println("    text-decoration: none;");
        out.println("    color: white;");
        out.println("}");
        out.println("");
        out.println(".manager-actions {");
        out.println("    background: #f8f9fa;");
        out.println("    border: 2px solid #007bff;");
        out.println("    border-radius: 8px;");
        out.println("    margin-top: 10px;");
        out.println("}");
        out.println("");
        out.println(".action-info p {");
        out.println("    margin: 0 0 8px 0;");
        out.println("}");
        out.println("");
        out.println(".review-note {");
        out.println("    color: #0c5460;");
        out.println("    background: #d1ecf1;");
        out.println("    padding: 10px;");
        out.println("    border-radius: 4px;");
        out.println("    border-left: 4px solid #007bff;");
        out.println("    margin-top: 10px !important;");
        out.println("}");
        out.println("");
        out.println(".balance-check {");
        out.println("    margin-top: 15px;");
        out.println("}");
        out.println("");
        out.println(".balance-warning {");
        out.println("    background: #f8d7da;");
        out.println("    color: #721c24;");
        out.println("    padding: 12px;");
        out.println("    border-radius: 4px;");
        out.println("    border-left: 4px solid #dc3545;");
        out.println("}");
        out.println("");
        out.println(".balance-ok {");
        out.println("    background: #d4edda;");
        out.println("    color: #155724;");
        out.println("    padding: 12px;");
        out.println("    border-radius: 4px;");
        out.println("    border-left: 4px solid #28a745;");
        out.println("}");
        out.println("");
        out.println(".balance-info {");
        out.println("    background: #d1ecf1;");
        out.println("    color: #0c5460;");
        out.println("    padding: 12px;");
        out.println("    border-radius: 4px;");
        out.println("    border-left: 4px solid #17a2b8;");
        out.println("}");
        out.println("");
        out.println(".balance-note {");
        out.println("    background: #e3f2fd;");
        out.println("    color: #1565c0;");
        out.println("    padding: 8px 12px;");
        out.println("    border-radius: 4px;");
        out.println("    margin-top: 8px;");
        out.println("    font-size: 0.85rem;");
        out.println("}");
        out.println("");
        out.println(".review-history {");
        out.println("    background: #f8f9fa;");
        out.println("    border: 1px solid #dee2e6;");
        out.println("    border-radius: 4px;");
        out.println("    padding: 15px;");
        out.println("}");
        out.println("");
        out.println(".history-item {");
        out.println("    display: flex;");
        out.println("    flex-direction: column;");
        out.println("    gap: 10px;");
        out.println("}");
        out.println("");
        out.println(".history-status {");
        out.println("    padding: 6px 12px;");
        out.println("    border-radius: 12px;");
        out.println("    font-size: 0.9rem;");
        out.println("    font-weight: 500;");
        out.println("}");
        out.println("");
        out.println(".history-manager {");
        out.println("    display: flex;");
        out.println("    align-items: center;");
        out.println("    gap: 8px;");
        out.println("    color: #495057;");
        out.println("    font-size: 0.95rem;");
        out.println("    flex-wrap: wrap;");
        out.println("}");
        out.println("");
        out.println(".manager-details {");
        out.println("    color: #6c757d;");
        out.println("    font-size: 0.85rem;");
        out.println("    font-weight: normal;");
        out.println("    margin-left: 4px;");
        out.println("}");
        out.println("");
        out.println(".history-date {");
        out.println("    display: flex;");
        out.println("    align-items: center;");
        out.println("    gap: 6px;");
        out.println("    color: #6c757d;");
        out.println("    font-size: 0.9rem;");
        out.println("}");
        out.println("");
        out.println(".review-note-final {");
        out.println("    background: #e9ecef;");
        out.println("    color: #495057;");
        out.println("    padding: 12px;");
        out.println("    border-radius: 4px;");
        out.println("    border-left: 4px solid #6c757d;");
        out.println("    display: flex;");
        out.println("    align-items: center;");
        out.println("    gap: 8px;");
        out.println("}");
        out.println("");
        out.println(".rejection-reason {");
        out.println("    margin-top: 12px;");
        out.println("    padding: 15px;");
        out.println("    background: #f8d7da;");
        out.println("    border: 1px solid #f5c6cb;");
        out.println("    border-radius: 6px;");
        out.println("    color: #721c24;");
        out.println("    display: flex;");
        out.println("    align-items: flex-start;");
        out.println("    gap: 12px;");
        out.println("}");
        out.println("");
        out.println(".rejection-content {");
        out.println("    flex: 1;");
        out.println("}");
        out.println("");
        out.println(".rejection-content strong {");
        out.println("    display: block;");
        out.println("    margin-bottom: 8px;");
        out.println("    font-size: 1rem;");
        out.println("    color: #721c24;");
        out.println("}");
        out.println("");
        out.println(".rejection-content p {");
        out.println("    margin: 0;");
        out.println("    line-height: 1.5;");
        out.println("    font-size: 0.95rem;");
        out.println("    color: #721c24;");
        out.println("}");
        out.println("");
        out.println(".status-badge {");
        out.println("    padding: 4px 12px;");
        out.println("    border-radius: 12px;");
        out.println("    font-size: 0.85rem;");
        out.println("    font-weight: 600;");
        out.println("    text-transform: uppercase;");
        out.println("    letter-spacing: 0.5px;");
        out.println("}");
        out.println("");
        out.println(".status-pending {");
        out.println("    background: #fff3cd;");
        out.println("    color: #856404;");
        out.println("    border: 1px solid #ffeaa7;");
        out.println("}");
        out.println("");
        out.println(".status-approved {");
        out.println("    background: #d4edda;");
        out.println("    color: #155724;");
        out.println("    border: 1px solid #c3e6cb;");
        out.println("}");
        out.println("");
        out.println(".status-rejected {");
        out.println("    background: #f8d7da;");
        out.println("    color: #721c24;");
        out.println("    border: 1px solid #f5c6cb;");
        out.println("}");
        out.println("");
        out.println(".status-cancelled {");
        out.println("    background: #e2e3e5;");
        out.println("    color: #383d41;");
        out.println("    border: 1px solid #d6d8db;");
        out.println("}");
        out.println("");
        out.println(".backdated-application {");
        out.println("    background: #fff3cd !important;");
        out.println("    color: #856404 !important;");
        out.println("    font-weight: 600 !important;");
        out.println("    padding: 4px 8px !important;");
        out.println("    border-radius: 12px !important;");
        out.println("    display: inline-flex !important;");
        out.println("    align-items: center !important;");
        out.println("    gap: 4px !important;");
        out.println("    flex-wrap: wrap !important;");
        out.println("}");
        out.println("");
        out.println(".backdated-details {");
        out.println("    font-size: 0.8rem !important;");
        out.println("    font-weight: normal !important;");
        out.println("    color: #856404 !important;");
        out.println("    margin-left: 4px !important;");
        out.println("}");
        out.println("");
        out.println(".overdue-application {");
        out.println("    background: #ffe5e5 !important;");
        out.println("    color: #d32f2f !important;");
        out.println("    font-weight: 600 !important;");
        out.println("    padding: 4px 8px !important;");
        out.println("    border-radius: 12px !important;");
        out.println("    display: inline-flex !important;");
        out.println("    align-items: center !important;");
        out.println("    gap: 4px !important;");
        out.println("    flex-wrap: wrap !important;");
        out.println("}");
        out.println("");
        out.println(".overdue-details {");
        out.println("    font-size: 0.8rem !important;");
        out.println("    font-weight: normal !important;");
        out.println("    color: #d32f2f !important;");
        out.println("    margin-left: 4px !important;");
        out.println("}");
        out.println("");
        out.println(".regular-application {");
        out.println("    background: #e8f5e8 !important;");
        out.println("    color: #2e7d32 !important;");
        out.println("    font-weight: 600 !important;");
        out.println("    padding: 4px 8px !important;");
        out.println("    border-radius: 12px !important;");
        out.println("    display: inline-flex !important;");
        out.println("    align-items: center !important;");
        out.println("    gap: 4px !important;");
        out.println("}");
        out.println("");
        out.println(".backdated-warning-box {");
        out.println("    background: linear-gradient(135deg, #fff8e1 0%, #fff3cd 100%);");
        out.println("    border: 2px solid #ffc107;");
        out.println("    border-left: 5px solid #ff9800;");
        out.println("    border-radius: 8px;");
        out.println("    padding: 15px;");
        out.println("    margin: 15px 0;");
        out.println("    display: flex;");
        out.println("    align-items: flex-start;");
        out.println("    gap: 12px;");
        out.println("    box-shadow: 0 2px 8px rgba(255, 152, 0, 0.2);");
        out.println("}");
        out.println("");
        out.println(".backdated-warning-box i {");
        out.println("    color: #ff9800;");
        out.println("    font-size: 1.3rem;");
        out.println("    margin-top: 2px;");
        out.println("}");
        out.println("");
        out.println(".backdated-warning-box strong {");
        out.println("    color: #e65100;");
        out.println("    display: block;");
        out.println("    margin-bottom: 8px;");
        out.println("    font-size: 1rem;");
        out.println("}");
        out.println("");
        out.println(".backdated-warning-box p {");
        out.println("    margin: 0;");
        out.println("    color: #5d4037;");
        out.println("    line-height: 1.5;");
        out.println("    font-size: 0.95rem;");
        out.println("}");
        out.println("");
        out.println(".overdue-warning-box {");
        out.println("    background: linear-gradient(135deg, #ffebee 0%, #ffcdd2 100%);");
        out.println("    border: 2px solid #f44336;");
        out.println("    border-left: 5px solid #d32f2f;");
        out.println("    border-radius: 8px;");
        out.println("    padding: 15px;");
        out.println("    margin: 15px 0;");
        out.println("    display: flex;");
        out.println("    align-items: flex-start;");
        out.println("    gap: 12px;");
        out.println("    box-shadow: 0 2px 8px rgba(211, 47, 47, 0.2);");
        out.println("}");
        out.println("");
        out.println(".overdue-warning-box i {");
        out.println("    color: #d32f2f;");
        out.println("    font-size: 1.3rem;");
        out.println("    margin-top: 2px;");
        out.println("}");
        out.println("");
        out.println(".overdue-warning-box strong {");
        out.println("    color: #b71c1c;");
        out.println("    display: block;");
        out.println("    margin-bottom: 8px;");
        out.println("    font-size: 1rem;");
        out.println("}");
        out.println("");
        out.println(".overdue-warning-box p {");
        out.println("    margin: 0;");
        out.println("    color: #5d4037;");
        out.println("    line-height: 1.5;");
        out.println("    font-size: 0.95rem;");
        out.println("}");
        out.println("");
        out.println("@media (max-width: 768px) {");
        out.println("    .info-grid {");
        out.println("        grid-template-columns: 1fr;");
        out.println("    }");
        out.println("    ");
        out.println("    .application-meta {");
        out.println("        flex-direction: column;");
        out.println("        align-items: flex-start;");
        out.println("    }");
        out.println("    ");
        out.println("    .history-item {");
        out.println("        gap: 8px;");
        out.println("    }");
        out.println("    ");
        out.println("    .history-manager {");
        out.println("        font-size: 0.9rem;");
        out.println("    }");
        out.println("    ");
        out.println("    .manager-details {");
        out.println("        font-size: 0.8rem;");
        out.println("    }");
        out.println("    ");
        out.println("    .history-date {");
        out.println("        font-size: 0.85rem;");
        out.println("    }");
        out.println("    ");
        out.println("    .rejection-reason {");
        out.println("        flex-direction: column;");
        out.println("        gap: 8px;");
        out.println("    }");
        out.println("    ");
        out.println("    .manager-review-container {");
        out.println("        margin: 10px;");
        out.println("        border-radius: 4px;");
        out.println("    }");
        out.println("    ");
        out.println("    .review-section {");
        out.println("        padding: 15px;");
        out.println("    }");
        out.println("}");
        out.println("</style>");
        
        out.flush();
        System.out.println("✅ Manager review view generated successfully for application: " + application.getApplicationid());
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
    
    private String formatDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
            return "N/A";
        }
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM dd, yyyy 'at' hh:mm a");
            Date date = inputFormat.parse(dateTimeStr);
            return outputFormat.format(date);
        } catch (Exception e) {
            return dateTimeStr;
        }
    }
}
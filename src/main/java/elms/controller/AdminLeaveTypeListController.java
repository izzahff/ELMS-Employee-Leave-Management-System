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
import java.util.List;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/AdminLeaveTypeListController")
public class AdminLeaveTypeListController extends HttpServlet {
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
        System.out.println("=== AdminLeaveTypeListController initialized ===");
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        System.out.println("=== AdminLeaveTypeListController doGet called ===");
        
        // Check if admin is logged in
        HttpSession session = request.getSession(false);
        String adminId = (String) session.getAttribute("adminId");
        
        if (adminId == null) {
            System.out.println("❌ NO ADMIN ID IN SESSION - redirecting to login");
            response.sendRedirect(request.getContextPath() + "/Admin/AdminLogin.jsp");
            return;
        }
        
        try {
            // Get all leave types from database
            List<LeaveType> leaveTypes = leaveTypeDAO.getAllLeaveTypes();
            System.out.println("Retrieved " + leaveTypes.size() + " leave types from database");
            
            // Create maps to store details for each leave type
            Map<String, FullDay> fullDayDetails = new HashMap<>();
            Map<String, HalfDay> halfDayDetails = new HashMap<>();
            
         // Get additional details for both Full Day and Half Day leave types
            for (LeaveType leaveType : leaveTypes) {
                String leaveTypeId = leaveType.getLeaveTypeId();
                String category = leaveType.getLeaveTypeCategory();
                
                // ✅ Get Full Day details for "Full Day" OR "Both"
                if ("Full Day".equals(category) || "Both".equals(category)) {
                    FullDay fullDay = fullDayDAO.getFullDayByLeaveTypeId(leaveTypeId);
                    if (fullDay != null) {
                        fullDayDetails.put(leaveTypeId, fullDay);
                        System.out.println("Found Full Day details for " + leaveType.getLeaveTypeName() + 
                                         " - Duration: " + fullDay.getStandardDuration() + " days");
                    }
                }
                
                // ✅ Get Half Day details for "Half Day" OR "Both"
                if ("Half Day".equals(category) || "Both".equals(category)) {
                    HalfDay halfDay = halfDayDAO.getHalfDayByLeaveTypeId(leaveTypeId);
                    if (halfDay != null) {
                        halfDayDetails.put(leaveTypeId, halfDay);
                        System.out.println("Found Half Day details for " + leaveType.getLeaveTypeName() + 
                                         " - Shift: " + halfDay.getShift());
                    }
                }
                
                // Log document requirement and balance status
                System.out.println("Leave Type: " + leaveType.getLeaveTypeName() + 
                                 " - Requires Document: " + leaveType.isRequiresDocument() +
                                 " - Affects Balance: " + leaveType.isAffectsBalance());
            }
            
            // Set attributes for JSP
            request.setAttribute("leaveTypes", leaveTypes);
            request.setAttribute("fullDayDetails", fullDayDetails);
            request.setAttribute("halfDayDetails", halfDayDetails);
            request.setAttribute("adminId", adminId);
            
         // Check for messages in session and move them to request scope for JSP display
            String successMessage = (String) session.getAttribute("successMessage");
            if (successMessage != null) {
                request.setAttribute("successMessage", successMessage);
                session.removeAttribute("successMessage");
                System.out.println("✅ Success message found in session: " + successMessage);
            }
            
            String errorMessage = (String) session.getAttribute("errorMessage");
            if (errorMessage != null) {
                request.setAttribute("errorMessage", errorMessage);
                session.removeAttribute("errorMessage");
                System.out.println("❌ Error message found in session: " + errorMessage);
            }
            
            String infoMessage = (String) session.getAttribute("infoMessage");
            if (infoMessage != null) {
                request.setAttribute("infoMessage", infoMessage);
                session.removeAttribute("infoMessage");
                System.out.println("ℹ️ Info message found in session: " + infoMessage);
            }
            
            // Check for new leave type ID for scrolling
            String newLeaveTypeId = (String) session.getAttribute("newLeaveTypeId");
            if (newLeaveTypeId != null) {
                request.setAttribute("newLeaveTypeId", newLeaveTypeId);
                session.removeAttribute("newLeaveTypeId");
                System.out.println("📍 New leave type ID for scrolling: " + newLeaveTypeId);
            }
            
            // Forward to JSP
            System.out.println("Forwarding to AdminLeaveTypeList.jsp");
            request.getRequestDispatcher("/Admin/AdminLeaveTypeList.jsp").forward(request, response);
            
        } catch (Exception e) {
            System.err.println("Error in AdminLeaveTypeListController: " + e.getMessage());
            e.printStackTrace();
            
            request.setAttribute("errorMessage", "An error occurred while retrieving leave types: " + e.getMessage());
            request.getRequestDispatcher("/Admin/AdminLeaveTypeList.jsp").forward(request, response);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String action = request.getParameter("action");
        
        if ("delete".equals(action)) {
            System.out.println("=== Redirecting delete request to AdminDeleteLeaveTypeController ===");
            
            String leaveTypeId = request.getParameter("leaveTypeId");
            String leaveTypeName = request.getParameter("leaveTypeName");
            
            System.out.println("Delete request for leaveTypeId: " + leaveTypeId + ", name: " + leaveTypeName);
            
            request.getRequestDispatcher("/AdminDeleteLeaveTypeController").forward(request, response);
        } else {
            doGet(request, response);
        }
    }
}
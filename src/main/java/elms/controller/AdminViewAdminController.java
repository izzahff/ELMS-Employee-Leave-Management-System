package elms.controller;

import elms.DAO.AdminDAO;
import elms.model.Admin;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for Admin Dashboard to display admin profile and list of other admin officers
 */
@WebServlet("/admin-dashboard")
public class AdminViewAdminController extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    private AdminDAO adminDAO;
    
    @Override
    public void init() throws ServletException {
        super.init();
        adminDAO = new AdminDAO();
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        System.out.println("=== ADMIN DASHBOARD CONTROLLER ===");
        
        // Check if admin is logged in
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("adminId") == null) {
            response.sendRedirect(request.getContextPath() + "/Admin/AdminLogin.jsp");
            return;
        }
        
        String currentAdminId = (String) session.getAttribute("adminId");
        System.out.println("Current Admin ID: " + currentAdminId);
        
        try {
            // Get all admins from database
            List<Admin> allAdmins = adminDAO.getAllAdmins();
            System.out.println("Total admins found: " + allAdmins.size());
            
            // Filter out current admin to get other admin officers
            List<AdminOfficer> otherAdminOfficers = new ArrayList<>();
            
            for (Admin admin : allAdmins) {
                // Skip current admin
                if (!admin.getAdminId().equals(currentAdminId)) {
                    AdminOfficer officer = new AdminOfficer();
                    officer.setAdminId(admin.getAdminId());
                    officer.setName(admin.getAdminName());
                    officer.setEmail(admin.getAdminEmail());
                    officer.setPhone(admin.getAdminNoPhone());
                    officer.setProfilePicturePath(admin.getProfile_picture_path());
                    officer.setStatus("active"); // Default status - you can add status field to DB if needed
                    
                    otherAdminOfficers.add(officer);
                }
            }
            
            System.out.println("Other admin officers: " + otherAdminOfficers.size());
            
            // Set attributes for JSP
            request.setAttribute("adminOfficers", otherAdminOfficers);
            request.setAttribute("totalAdminOfficers", otherAdminOfficers.size());
            
            // Forward to admin dashboard JSP
            request.getRequestDispatcher("/Admin/AdminDashboard.jsp").forward(request, response);
            
        } catch (SQLException e) {
            System.err.println("Error fetching admin officers: " + e.getMessage());
            e.printStackTrace();
            
            // Set error message and forward to dashboard
            session.setAttribute("errorMessage", "Error loading admin officers. Please try again.");
            request.getRequestDispatcher("/Admin/AdminDashboard.jsp").forward(request, response);
        }
    }
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
    
    /**
     * Helper class to represent admin officer data for display
     */
    public static class AdminOfficer {
        private String adminId;
        private String name;
        private String email;
        private String phone;
        private String profilePicturePath;
        private String status;
        
        // Constructors
        public AdminOfficer() {}
        
        public AdminOfficer(String adminId, String name, String email, String phone, String profilePicturePath, String status) {
            this.adminId = adminId;
            this.name = name;
            this.email = email;
            this.phone = phone;
            this.profilePicturePath = profilePicturePath;
            this.status = status;
        }
        
        // Getters and Setters
        public String getAdminId() { return adminId; }
        public void setAdminId(String adminId) { this.adminId = adminId; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        
        public String getProfilePicturePath() { return profilePicturePath; }
        public void setProfilePicturePath(String profilePicturePath) { this.profilePicturePath = profilePicturePath; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        @Override
        public String toString() {
            return "AdminOfficer{" +
                    "adminId='" + adminId + '\'' +
                    ", name='" + name + '\'' +
                    ", email='" + email + '\'' +
                    ", phone='" + phone + '\'' +
                    ", profilePicturePath='" + profilePicturePath + '\'' +
                    ", status='" + status + '\'' +
                    '}';
        }
    }
}
package elms.controller;

import elms.DAO.ManagerDAO;
import elms.model.Manager;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@WebServlet("/Admin/AdminViewManagerListController")
public class AdminViewManagerListController extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private ManagerDAO managerDAO;
    
    @Override
    public void init() throws ServletException {
        super.init();
        managerDAO = new ManagerDAO();
    }
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        processRequest(request, response);
    }
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        processRequest(request, response);
    }
    
    private void processRequest(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // Check if admin is logged in
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("adminId") == null) {
            response.sendRedirect(request.getContextPath() + "/Admin/AdminLogin.jsp");
            return;
        }
        
        try {
            // Get search parameter if any
            String searchTerm = request.getParameter("search");
            List<Manager> managerList;
            
            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                // Search managers
                managerList = managerDAO.searchManagers(searchTerm.trim());
                request.setAttribute("searchTerm", searchTerm);
            } else {
                // Get all managers
                managerList = managerDAO.getAllManagers();
            }
            
            // Process manager data for display
            for (Manager manager : managerList) {
                // Set position display properties
                String position = manager.getManagerposition();
                if (position != null) {
                    switch (position.toLowerCase()) {
                        case "executive director":
                            manager.setPositionBadgeClass("role-executive");
                            manager.setPositionDisplayName("Executive");
                            break;
                        case "project manager":
                            manager.setPositionBadgeClass("role-project");
                            manager.setPositionDisplayName("Project Mgr");
                            break;
                        default:
                            manager.setPositionBadgeClass("role-unknown");
                            manager.setPositionDisplayName("Manager");
                            break;
                    }
                } else {
                    manager.setPositionBadgeClass("role-unknown");
                    manager.setPositionDisplayName("Manager");
                }
            }
            
            // Set attributes for JSP
            request.setAttribute("managerList", managerList);
            request.setAttribute("totalManagers", managerList.size());
            
            // Check for messages from delete operations
            String errorMessage = (String) request.getAttribute("errorMessage");
            String successMessage = (String) request.getAttribute("successMessage");
            String infoMessage = (String) request.getAttribute("infoMessage");
            
            if (errorMessage != null) {
                request.setAttribute("errorMessage", errorMessage);
            }
            if (successMessage != null) {
                request.setAttribute("successMessage", successMessage);
            }
            if (infoMessage != null) {
                request.setAttribute("infoMessage", infoMessage);
            }
            
            // Forward to JSP
            request.getRequestDispatcher("/Admin/AdminViewManagerList.jsp").forward(request, response);
            
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "An error occurred while loading the manager list. Please try again.");
            request.getRequestDispatcher("/Admin/AdminViewManagerList.jsp").forward(request, response);
        }
    }
}
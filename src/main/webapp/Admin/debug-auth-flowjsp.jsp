<%@ page import="elms.connection.ConnectionManager" %>
<%@ page import="java.sql.*" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<!DOCTYPE html>
<html>
<head><title>Debug ADM001 Specific Issue</title></head>
<body>
    <h1>Debug ADM001 Specific Issue</h1>
    
    <h3>ADM001 Record Details:</h3>
    <%
    try {
        ConnectionManager cm = new ConnectionManager();
        Connection conn = cm.getConnection();
        
        String sql = "SELECT adminId, adminPassword, adminName, adminNoPhone, adminEmail FROM admin WHERE adminId = 'ADM001'";
        PreparedStatement ps = conn.prepareStatement(sql);
        ResultSet rs = ps.executeQuery();
        
        if (rs.next()) {
            String adminId = rs.getString("adminId");
            String adminPassword = rs.getString("adminPassword");
            String adminName = rs.getString("adminName");
            String adminPhone = rs.getString("adminNoPhone");
            String adminEmail = rs.getString("adminEmail");
            
            out.println("<table border='1' style='border-collapse: collapse; font-family: monospace;'>");
            out.println("<tr><th>Field</th><th>Value</th><th>Length</th><th>Is Null?</th><th>Status</th></tr>");
            
            String[][] fields = {
                {"adminId", adminId},
                {"adminPassword", adminPassword},
                {"adminName", adminName},
                {"adminNoPhone", adminPhone},
                {"adminEmail", adminEmail}
            };
            
            for (String[] field : fields) {
                String name = field[0];
                String value = field[1];
                
                out.println("<tr>");
                out.println("<td style='padding: 8px; font-weight: bold;'>" + name + "</td>");
                out.println("<td style='padding: 8px;'>'" + (value != null ? value : "NULL") + "'</td>");
                out.println("<td style='padding: 8px;'>" + (value != null ? value.length() : 0) + "</td>");
                out.println("<td style='padding: 8px;'>" + (value == null ? "YES" : "NO") + "</td>");
                
                String status = "✅ OK";
                if (value == null && (name.equals("adminId") || name.equals("adminPassword"))) {
                    status = "❌ CRITICAL - Cannot be NULL";
                } else if (value != null && value.trim().length() == 0) {
                    status = "⚠️ Empty string";
                } else if (value != null && !value.equals(value.trim())) {
                    status = "⚠️ Has leading/trailing spaces";
                }
                
                out.println("<td style='padding: 8px;'>" + status + "</td>");
                out.println("</tr>");
            }
            out.println("</table>");
            
            // Show what the actual password is
            out.println("<h4>ADM001 Password Analysis:</h4>");
            if (adminPassword != null) {
                out.println("<div style='background: #f8f9fa; padding: 15px; border: 1px solid #dee2e6;'>");
                out.println("<strong>Stored Password:</strong> '" + adminPassword + "'<br>");
                out.println("<strong>Password Length:</strong> " + adminPassword.length() + "<br>");
                out.println("<strong>Trimmed Password:</strong> '" + adminPassword.trim() + "'<br>");
                out.println("<strong>Is it 'admin'?</strong> " + adminPassword.equals("admin") + "<br>");
                out.println("<strong>Is it 'admin' (case insensitive)?</strong> " + adminPassword.equalsIgnoreCase("admin") + "<br>");
                out.println("<strong>After trim, is it 'admin'?</strong> " + adminPassword.trim().equals("admin") + "<br>");
                out.println("</div>");
                
                // Show character by character
                out.println("<h5>Character by Character:</h5>");
                out.println("<div style='font-family: monospace; background: #f1f3f4; padding: 10px;'>");
                for (int i = 0; i < adminPassword.length(); i++) {
                    char c = adminPassword.charAt(i);
                    out.println("Position " + i + ": '" + c + "' (ASCII: " + (int)c + ")<br>");
                }
                out.println("</div>");
            } else {
                out.println("<div style='background: #f8d7da; padding: 10px; border: 1px solid #f5c6cb; color: #721c24;'>");
                out.println("❌ Password is NULL! This is why authentication fails.");
                out.println("</div>");
            }
            
        } else {
            out.println("<p style='color: red;'>❌ ADM001 record not found!</p>");
        }
        
        rs.close();
        ps.close();
        conn.close();
        
    } catch (Exception e) {
        out.println("<p style='color: red;'>Error: " + e.getMessage() + "</p>");
        e.printStackTrace();
    }
    %>
    
    <hr>
    
    <h3>Test Different Password Variations:</h3>
    <%
    String testPass = request.getParameter("testPass");
    if (testPass != null) {
        out.println("<h4>Testing ADM001 with password: '" + testPass + "'</h4>");
        
        try {
            elms.DAO.AdminDAO dao = new elms.DAO.AdminDAO();
            elms.model.Admin admin = dao.authenticateAdmin("ADM001", testPass);
            
            if (admin != null) {
                out.println("<div style='background: #d4edda; padding: 10px; border: 1px solid #c3e6cb; color: #155724;'>");
                out.println("🎉 SUCCESS! ADM001 password is: '" + testPass + "'");
                out.println("</div>");
            } else {
                out.println("<div style='background: #f8d7da; padding: 10px; border: 1px solid #f5c6cb; color: #721c24;'>");
                out.println("❌ Failed with password: '" + testPass + "'");
                out.println("</div>");
            }
            
        } catch (Exception e) {
            out.println("<p style='color: red;'>Test error: " + e.getMessage() + "</p>");
        }
    }
    %>
    
    <div style="display: grid; grid-template-columns: repeat(3, 1fr); gap: 10px; max-width: 600px;">
        <form method="get"><input type="hidden" name="testPass" value="admin"><button type="submit">Test: admin</button></form>
        <form method="get"><input type="hidden" name="testPass" value="ADMIN"><button type="submit">Test: ADMIN</button></form>
        <form method="get"><input type="hidden" name="testPass" value="password"><button type="submit">Test: password</button></form>
        <form method="get"><input type="hidden" name="testPass" value="123456"><button type="submit">Test: 123456</button></form>
        <form method="get"><input type="hidden" name="testPass" value="ADM001"><button type="submit">Test: ADM001</button></form>
        <form method="get"><input type="hidden" name="testPass" value=""><button type="submit">Test: empty</button></form>
    </div>
    
    <h4>Custom Test:</h4>
    <form method="get">
        Password: <input type="text" name="testPass" placeholder="Enter password to test">
        <button type="submit">Test Custom Password</button>
    </form>
    
    <hr>
    
    <h3>Quick Fixes:</h3>
    
    <%
    String fixAction = request.getParameter("fix");
    if (fixAction != null) {
        try {
            ConnectionManager cm = new ConnectionManager();
            java.sql.Connection conn = cm.getConnection();
            
            String updateSql = "";
            String message = "";
            
            switch(fixAction) {
                case "password":
                    updateSql = "UPDATE admin SET adminPassword = 'admin' WHERE adminId = 'ADM001'";
                    message = "Set ADM001 password to 'admin'";
                    break;
                case "trim":
                    updateSql = "UPDATE admin SET adminPassword = TRIM(adminPassword) WHERE adminId = 'ADM001'";
                    message = "Trimmed spaces from ADM001 password";
                    break;
                case "upper":
                    updateSql = "UPDATE admin SET adminPassword = UPPER(adminPassword) WHERE adminId = 'ADM001'";
                    message = "Converted ADM001 password to uppercase";
                    break;
                case "lower":
                    updateSql = "UPDATE admin SET adminPassword = LOWER(adminPassword) WHERE adminId = 'ADM001'";
                    message = "Converted ADM001 password to lowercase";
                    break;
            }
            
            if (!updateSql.isEmpty()) {
                java.sql.PreparedStatement ps = conn.prepareStatement(updateSql);
                int rows = ps.executeUpdate();
                
                if (rows > 0) {
                    out.println("<div style='background: #d4edda; padding: 10px; border: 1px solid #c3e6cb; color: #155724;'>");
                    out.println("✅ " + message + " - Try logging in now!");
                    out.println("</div>");
                } else {
                    out.println("<p style='color: red;'>❌ Update failed</p>");
                }
                
                ps.close();
            }
            conn.close();
            
        } catch (Exception e) {
            out.println("<p style='color: red;'>Fix error: " + e.getMessage() + "</p>");
        }
    }
    %>
    
    <div style="display: flex; gap: 10px; flex-wrap: wrap;">
        <a href="?fix=password" style="background: #007bff; color: white; padding: 8px 12px; text-decoration: none; border-radius: 3px;">
            Set Password to 'admin'
        </a>
        <a href="?fix=trim" style="background: #28a745; color: white; padding: 8px 12px; text-decoration: none; border-radius: 3px;">
            Trim Spaces
        </a>
        <a href="?fix=lower" style="background: #ffc107; color: black; padding: 8px 12px; text-decoration: none; border-radius: 3px;">
            Convert to Lowercase
        </a>
        <a href="?fix=upper" style="background: #17a2b8; color: white; padding: 8px 12px; text-decoration: none; border-radius: 3px;">
            Convert to Uppercase
        </a>
    </div>
    
    <hr>
    <a href="compare-admin-records.jsp">Compare ADM001 vs ADM002</a> |
    <a href="/ELMS_3.0/AdminLoginController">Try Login</a>
</body>
</html>
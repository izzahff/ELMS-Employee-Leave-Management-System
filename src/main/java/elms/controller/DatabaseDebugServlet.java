package elms.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import elms.connection.ConnectionManager;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;

/**
 * Debug servlet to test database connectivity and table access
 */
@WebServlet("/debugDB")
public class DatabaseDebugServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        
        out.println("<!DOCTYPE html>");
        out.println("<html><head><title>Database Debug</title>");
        out.println("<style>body{font-family:Arial;margin:20px;} .success{color:green;} .error{color:red;} .info{color:blue;} pre{background:#f5f5f5;padding:10px;border-radius:5px;}</style>");
        out.println("</head><body>");
        out.println("<h1>Database Connectivity Debug</h1>");

        // Test 1: Basic Connection
        out.println("<h2>Test 1: Database Connection</h2>");
        Connection conn = null;
        try {
            conn = ConnectionManager.getConnection();
            if (conn != null && !conn.isClosed()) {
                out.println("<p class='success'>✓ Database connection successful!</p>");
                out.println("<p class='info'>Connection URL: jdbc:oracle:thin:@//localhost:1521/freepdb1</p>");
                out.println("<p class='info'>User: ELMS</p>");
            } else {
                out.println("<p class='error'>✗ Connection is null or closed</p>");
                return;
            }
        } catch (SQLException e) {
            out.println("<p class='error'>✗ Connection failed: " + e.getMessage() + "</p>");
            out.println("<pre>SQL State: " + e.getSQLState() + "\nError Code: " + e.getErrorCode() + "</pre>");
            return;
        }

        // Test 2: Check if employees table exists
        out.println("<h2>Test 2: Table Structure</h2>");
        try {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet tables = metaData.getTables(null, "ELMS", "EMPLOYEES", null);
            if (tables.next()) {
                out.println("<p class='success'>✓ EMPLOYEES table exists</p>");
                
                // Get column information
                ResultSet columns = metaData.getColumns(null, "ELMS", "EMPLOYEES", null);
                out.println("<h3>Table Columns:</h3><ul>");
                while (columns.next()) {
                    String columnName = columns.getString("COLUMN_NAME");
                    String dataType = columns.getString("TYPE_NAME");
                    int columnSize = columns.getInt("COLUMN_SIZE");
                    String nullable = columns.getString("IS_NULLABLE");
                    out.println("<li><strong>" + columnName + "</strong> - " + dataType + "(" + columnSize + ") - Nullable: " + nullable + "</li>");
                }
                out.println("</ul>");
                columns.close();
            } else {
                out.println("<p class='error'>✗ EMPLOYEES table not found</p>");
                tables.close();
                return;
            }
            tables.close();
        } catch (SQLException e) {
            out.println("<p class='error'>✗ Error checking table: " + e.getMessage() + "</p>");
        }

        // Test 3: Check current data
        out.println("<h2>Test 3: Current Data</h2>");
        try {
            PreparedStatement pstmt = conn.prepareStatement("SELECT COUNT(*) as total FROM employees");
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                int count = rs.getInt("total");
                out.println("<p class='success'>✓ Table accessible. Current record count: " + count + "</p>");
            }
            rs.close();
            pstmt.close();
            
            // Show sample data if any exists
            if (true) { // Always try to show structure
                pstmt = conn.prepareStatement("SELECT * FROM employees WHERE ROWNUM <= 3");
                rs = pstmt.executeQuery();
                ResultSetMetaData rsmd = rs.getMetaData();
                int columnCount = rsmd.getColumnCount();
                
                out.println("<h3>Sample Data (First 3 rows):</h3>");
                out.println("<table border='1' style='border-collapse:collapse;'>");
                out.println("<tr>");
                for (int i = 1; i <= columnCount; i++) {
                    out.println("<th>" + rsmd.getColumnName(i) + "</th>");
                }
                out.println("</tr>");
                
                while (rs.next()) {
                    out.println("<tr>");
                    for (int i = 1; i <= columnCount; i++) {
                        out.println("<td>" + rs.getString(i) + "</td>");
                    }
                    out.println("</tr>");
                }
                out.println("</table>");
                rs.close();
                pstmt.close();
            }
        } catch (SQLException e) {
            out.println("<p class='error'>✗ Error accessing data: " + e.getMessage() + "</p>");
            out.println("<pre>SQL State: " + e.getSQLState() + "\nError Code: " + e.getErrorCode() + "</pre>");
        }

        // Test 4: Check triggers and sequences
        out.println("<h2>Test 4: Triggers and Sequences</h2>");
        try {
            // Check triggers
            PreparedStatement pstmt = conn.prepareStatement(
                "SELECT trigger_name, status, trigger_type FROM user_triggers WHERE table_name = 'EMPLOYEES'"
            );
            ResultSet rs = pstmt.executeQuery();
            out.println("<h3>Triggers:</h3><ul>");
            while (rs.next()) {
                String name = rs.getString("trigger_name");
                String status = rs.getString("status");
                String type = rs.getString("trigger_type");
                out.println("<li>" + name + " - Status: " + status + " - Type: " + type + "</li>");
            }
            out.println("</ul>");
            rs.close();
            pstmt.close();
            
            // Check sequences
            pstmt = conn.prepareStatement("SELECT sequence_name, last_number FROM user_sequences");
            rs = pstmt.executeQuery();
            out.println("<h3>Sequences:</h3><ul>");
            while (rs.next()) {
                String name = rs.getString("sequence_name");
                int lastNumber = rs.getInt("last_number");
                out.println("<li>" + name + " - Last Number: " + lastNumber + "</li>");
            }
            out.println("</ul>");
            rs.close();
            pstmt.close();
        } catch (SQLException e) {
            out.println("<p class='error'>✗ Error checking triggers/sequences: " + e.getMessage() + "</p>");
        }

        // Test 5: Test actual insertion
        out.println("<h2>Test 5: Test Insertion</h2>");
        try {
            conn.setAutoCommit(false);
            
            // Try the exact same insert that's failing
            String testInsertSQL = "INSERT INTO employees (employeename, employeenophone, employeeemail, employeepassword) VALUES (?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(testInsertSQL);
            pstmt.setString(1, "Test User Debug");
            pstmt.setString(2, "1234567890");
            pstmt.setString(3, "debug" + System.currentTimeMillis() + "@test.com"); // Unique email
            pstmt.setString(4, "testpassword123");
            
            out.println("<p class='info'>Attempting insert with SQL: " + testInsertSQL + "</p>");
            
            int rowsAffected = pstmt.executeUpdate();
            out.println("<p class='success'>✓ Insert successful! Rows affected: " + rowsAffected + "</p>");
            
            // Try to get the generated employee ID
            PreparedStatement selectStmt = conn.prepareStatement(
                "SELECT employeeid FROM employees WHERE employeeemail = ?"
            );
            selectStmt.setString(1, "debug" + (System.currentTimeMillis() - 1000) + "@test.com");
            ResultSet rs = selectStmt.executeQuery();
            if (rs.next()) {
                String empId = rs.getString("employeeid");
                out.println("<p class='success'>✓ Generated Employee ID: " + empId + "</p>");
            } else {
                out.println("<p class='error'>✗ Could not retrieve generated employee ID</p>");
            }
            rs.close();
            selectStmt.close();
            pstmt.close();
            
            // Rollback the test data
            conn.rollback();
            out.println("<p class='info'>Test data rolled back</p>");
            
        } catch (SQLException e) {
            out.println("<p class='error'>✗ Insert test failed: " + e.getMessage() + "</p>");
            out.println("<pre>");
            out.println("SQL State: " + e.getSQLState());
            out.println("Error Code: " + e.getErrorCode());
            out.println("Class: " + e.getClass().getName());
            
            // Print stack trace
            e.printStackTrace(out);
            out.println("</pre>");
            
            try {
                conn.rollback();
            } catch (SQLException ex) {
                out.println("<p class='error'>Also failed to rollback: " + ex.getMessage() + "</p>");
            }
        }

        // Close connection
        try {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        } catch (SQLException e) {
            out.println("<p class='error'>Error closing connection: " + e.getMessage() + "</p>");
        }

        out.println("<h2>Next Steps</h2>");
        out.println("<ol>");
        out.println("<li>If any tests failed above, fix those issues first</li>");
        out.println("<li>Check your trigger code for syntax errors</li>");
        out.println("<li>Verify sequence permissions and existence</li>");
        out.println("<li>Check column name spelling (employeeid vs employee_id)</li>");
        out.println("<li>Test with a direct SQL insert in your database tool</li>");
        out.println("</ol>");
        
        out.println("</body></html>");
    }
}
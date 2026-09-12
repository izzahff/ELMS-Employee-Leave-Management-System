package elms.DAO;

import elms.model.Report;
import elms.connection.ConnectionManager;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Collections;

/**
 * DAO for managing leave reports with reporttype support
 */
public class ReportDAO {
    
    /**
     * Generate report with proper ERD relationship and reporttype
     */
    public String generateReport(List<String> applicationIds, String reportType) throws SQLException {
        System.out.println("=== GENERATING REPORT (ERD COMPLIANT WITH REPORTTYPE) ===");
        System.out.println("Report Type: " + reportType);
        System.out.println("Application IDs count: " + (applicationIds != null ? applicationIds.size() : 0));
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String generatedReportId = null;
        
        // Validate and normalize report type
        String normalizedReportType = normalizeReportType(reportType);
        
        try {
            conn = ConnectionManager.getConnection();
            if (conn == null) {
                throw new SQLException("Failed to establish database connection");
            }
            
            // Start transaction
            conn.setAutoCommit(false);
            
            // Step 1: Generate unique report ID using sequence
            String sequenceSql = "SELECT 'RPT' || LPAD(report_id_seq.NEXTVAL, 6, '0') AS report_id FROM DUAL";
            pstmt = conn.prepareStatement(sequenceSql);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                generatedReportId = rs.getString("report_id");
                System.out.println("✅ Generated report ID: " + generatedReportId);
            } else {
                throw new SQLException("Failed to generate report ID from sequence");
            }
            
            rs.close();
            pstmt.close();
            
            // Step 2: Insert multiple records - ONE FOR EACH APPLICATION with reporttype
            if (applicationIds != null && !applicationIds.isEmpty()) {
                // Insert one record for each application in this report
                String insertSql = "INSERT INTO report (reportid, generateddate, applicationid, reporttype) VALUES (?, SYSDATE, ?, ?)";
                pstmt = conn.prepareStatement(insertSql);
                
                for (String applicationId : applicationIds) {
                    pstmt.setString(1, generatedReportId);     // SAME report ID
                    pstmt.setString(2, applicationId);         // DIFFERENT application ID
                    pstmt.setString(3, normalizedReportType);  // Report type
                    pstmt.addBatch();
                }
                
                int[] batchResults = pstmt.executeBatch();
                System.out.println("✅ Inserted " + batchResults.length + " report-application relationships with type: " + normalizedReportType);
                
            } else {
                // Insert a general report record with NULL application (summary report)
                String insertSql = "INSERT INTO report (reportid, generateddate, applicationid, reporttype) VALUES (?, SYSDATE, NULL, ?)";
                pstmt = conn.prepareStatement(insertSql);
                pstmt.setString(1, generatedReportId);
                pstmt.setString(2, normalizedReportType);
                
                int rowsAffected = pstmt.executeUpdate();
                System.out.println("✅ Inserted general report record with type: " + normalizedReportType + ", rows: " + rowsAffected);
            }
            
            // Commit transaction
            conn.commit();
            System.out.println("✅ Report created successfully: " + generatedReportId + " (Type: " + normalizedReportType + ")");
            
            return generatedReportId;
            
        } catch (SQLException e) {
            System.err.println("❌ SQL Error: " + e.getMessage());
            e.printStackTrace();
            
            if (conn != null) {
                try {
                    conn.rollback();
                    System.out.println("🔄 Transaction rolled back");
                } catch (SQLException ex) {
                    System.err.println("❌ Error during rollback: " + ex.getMessage());
                }
            }
            throw e;
            
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                System.err.println("❌ Error closing resources: " + e.getMessage());
            }
        }
    }
    
    /**
     * Normalize and validate report type
     */
    private String normalizeReportType(String reportType) {
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
     * Get applications for a specific report ID
     */
    public List<String> getApplicationsForReport(String reportId) throws SQLException {
        System.out.println("=== GETTING APPLICATIONS FOR REPORT: " + reportId + " ===");
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<String> applicationIds = new ArrayList<>();
        
        try {
            conn = ConnectionManager.getConnection();
            
            String selectSql = "SELECT applicationid FROM report WHERE reportid = ? AND applicationid IS NOT NULL ORDER BY applicationid";
            pstmt = conn.prepareStatement(selectSql);
            pstmt.setString(1, reportId);
            
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                applicationIds.add(rs.getString("applicationid"));
            }
            
            System.out.println("✅ Found " + applicationIds.size() + " applications for report: " + reportId);
            
        } catch (SQLException e) {
            System.err.println("❌ Error getting applications for report: " + e.getMessage());
            throw e;
        } finally {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            if (conn != null) conn.close();
        }
        
        return applicationIds;
    }
    
    /**
     * Get report type for a specific report ID
     */
    public String getReportType(String reportId) throws SQLException {
        System.out.println("=== GETTING REPORT TYPE FOR: " + reportId + " ===");
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = ConnectionManager.getConnection();
            
            String selectSql = "SELECT DISTINCT reporttype FROM report WHERE reportid = ? AND ROWNUM = 1";
            pstmt = conn.prepareStatement(selectSql);
            pstmt.setString(1, reportId);
            
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String reportType = rs.getString("reporttype");
                System.out.println("✅ Found report type: " + reportType + " for report: " + reportId);
                return reportType;
            }
            
            System.out.println("⚠️ No report type found for report: " + reportId);
            return "summary"; // Default
            
        } catch (SQLException e) {
            System.err.println("❌ Error getting report type: " + e.getMessage());
            throw e;
        } finally {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            if (conn != null) conn.close();
        }
    }
    
    /**
     * Get all reports with their metadata including report type
     */
    public List<Map<String, Object>> getAllReportsWithCounts() throws SQLException {
        System.out.println("=== GETTING ALL REPORTS WITH APPLICATION COUNTS AND TYPES ===");
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Map<String, Object>> reports = new ArrayList<>();
        
        try {
            conn = ConnectionManager.getConnection();
            
            String selectSql = """
                SELECT reportid, 
                       MIN(generateddate) as generated_date,
                       MAX(reporttype) as report_type,
                       COUNT(applicationid) as application_count
                FROM report 
                GROUP BY reportid 
                ORDER BY MIN(generateddate) DESC
                """;
            
            pstmt = conn.prepareStatement(selectSql);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> report = new HashMap<>();
                report.put("reportId", rs.getString("reportid"));
                report.put("generatedDate", rs.getDate("generated_date"));
                report.put("reportType", rs.getString("report_type"));
                report.put("applicationCount", rs.getInt("application_count"));
                reports.add(report);
            }
            
            System.out.println("✅ Found " + reports.size() + " unique reports");
            
        } catch (SQLException e) {
            System.err.println("❌ Error getting reports: " + e.getMessage());
            throw e;
        } finally {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            if (conn != null) conn.close();
        }
        
        return reports;
    }
    
    /**
     * Get reports by type
     */
    public List<Map<String, Object>> getReportsByType(String reportType) throws SQLException {
        System.out.println("=== GETTING REPORTS BY TYPE: " + reportType + " ===");
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Map<String, Object>> reports = new ArrayList<>();
        
        String normalizedType = normalizeReportType(reportType);
        
        try {
            conn = ConnectionManager.getConnection();
            
            String selectSql = """
                SELECT reportid, 
                       MIN(generateddate) as generated_date,
                       reporttype,
                       COUNT(applicationid) as application_count
                FROM report 
                WHERE reporttype = ?
                GROUP BY reportid, reporttype 
                ORDER BY MIN(generateddate) DESC
                """;
            
            pstmt = conn.prepareStatement(selectSql);
            pstmt.setString(1, normalizedType);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> report = new HashMap<>();
                report.put("reportId", rs.getString("reportid"));
                report.put("generatedDate", rs.getDate("generated_date"));
                report.put("reportType", rs.getString("reporttype"));
                report.put("applicationCount", rs.getInt("application_count"));
                reports.add(report);
            }
            
            System.out.println("✅ Found " + reports.size() + " reports of type: " + normalizedType);
            
        } catch (SQLException e) {
            System.err.println("❌ Error getting reports by type: " + e.getMessage());
            throw e;
        } finally {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            if (conn != null) conn.close();
        }
        
        return reports;
    }
    
    /**
     * Get report by ID with all details including report type
     */
    public List<Report> getReportById(String reportId) throws SQLException {
        System.out.println("=== GETTING REPORT BY ID: " + reportId + " ===");
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        List<Report> reports = new ArrayList<>();
        
        try {
            conn = ConnectionManager.getConnection();
            
            String selectSql = "SELECT reportid, generateddate, applicationid, reporttype FROM report WHERE reportid = ? ORDER BY generateddate, applicationid";
            pstmt = conn.prepareStatement(selectSql);
            pstmt.setString(1, reportId);
            
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Report report = new Report();
                report.setReportid(rs.getString("reportid"));
                
                // Handle DATE conversion
                Date generatedDate = rs.getDate("generateddate");
                if (generatedDate != null) {
                    report.setGenerateddate(new SimpleDateFormat("yyyy-MM-dd").format(generatedDate));
                }
                
                report.setApplicationid(rs.getString("applicationid"));
                report.setReporttype(rs.getString("reporttype"));
                reports.add(report);
            }
            
            System.out.println("✅ Found " + reports.size() + " report records for ID: " + reportId);
            
        } catch (SQLException e) {
            System.err.println("❌ SQL Error in getReportById: " + e.getMessage());
            throw e;
        } finally {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            if (conn != null) conn.close();
        }
        
        return reports;
    }
    
    /**
     * Check if a report already exists for the same set of applications and report type
     */
    public String findExistingReport(List<String> applicationIds, String reportType) throws SQLException {
        if (applicationIds == null || applicationIds.isEmpty()) {
            return null;
        }
        
        System.out.println("=== CHECKING FOR EXISTING REPORT ===");
        System.out.println("Looking for report with " + applicationIds.size() + " applications and type: " + reportType);
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        String normalizedType = normalizeReportType(reportType);
        
        try {
            conn = ConnectionManager.getConnection();
            
            // Find reports that contain exactly the same applications AND same report type
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT reportid FROM (");
            sql.append("    SELECT reportid, COUNT(*) as app_count ");
            sql.append("    FROM report ");
            sql.append("    WHERE applicationid IN (");
            sql.append(String.join(",", Collections.nCopies(applicationIds.size(), "?")));
            sql.append("    ) AND reporttype = ? ");
            sql.append("    GROUP BY reportid ");
            sql.append("    HAVING COUNT(*) = ? ");
            sql.append(") r1 ");
            sql.append("WHERE r1.app_count = (");
            sql.append("    SELECT COUNT(*) FROM report r2 ");
            sql.append("    WHERE r2.reportid = r1.reportid AND r2.applicationid IS NOT NULL");
            sql.append(")");
            
            pstmt = conn.prepareStatement(sql.toString());
            
            // Set parameters for application IDs
            for (int i = 0; i < applicationIds.size(); i++) {
                pstmt.setString(i + 1, applicationIds.get(i));
            }
            // Set parameter for report type
            pstmt.setString(applicationIds.size() + 1, normalizedType);
            // Set parameter for count
            pstmt.setInt(applicationIds.size() + 2, applicationIds.size());
            
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String existingReportId = rs.getString("reportid");
                System.out.println("✅ Found existing report: " + existingReportId + " with type: " + normalizedType);
                return existingReportId;
            }
            
            System.out.println("ℹ️ No existing report found for this set of applications and type: " + normalizedType);
            
        } catch (SQLException e) {
            System.err.println("❌ Error finding existing report: " + e.getMessage());
            // Don't throw - just return null to create new report
        } finally {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            if (conn != null) conn.close();
        }
        
        return null; // No existing report found
    }
    
    /**
     * Get report statistics by type
     */
    public Map<String, Object> getReportStatisticsByType() throws SQLException {
        System.out.println("=== GETTING REPORT STATISTICS BY TYPE ===");
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        Map<String, Object> stats = new HashMap<>();
        
        try {
            conn = ConnectionManager.getConnection();
            
            // Statistics by report type
            String typeSql = """
                SELECT reporttype, 
                       COUNT(DISTINCT reportid) as report_count,
                       COUNT(applicationid) as total_applications
                FROM report 
                WHERE reporttype IS NOT NULL
                GROUP BY reporttype
                ORDER BY reporttype
                """;
            
            pstmt = conn.prepareStatement(typeSql);
            rs = pstmt.executeQuery();
            
            Map<String, Map<String, Integer>> typeStats = new HashMap<>();
            while (rs.next()) {
                String type = rs.getString("reporttype");
                Map<String, Integer> typeStat = new HashMap<>();
                typeStat.put("reportCount", rs.getInt("report_count"));
                typeStat.put("applicationCount", rs.getInt("total_applications"));
                typeStats.put(type, typeStat);
            }
            stats.put("reportsByType", typeStats);
            
            rs.close();
            pstmt.close();
            
            // Total statistics
            String totalSql = "SELECT COUNT(DISTINCT reportid) as total_reports FROM report";
            pstmt = conn.prepareStatement(totalSql);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                stats.put("totalReports", rs.getInt("total_reports"));
            }
            
            System.out.println("✅ Retrieved report statistics by type");
            
        } catch (SQLException e) {
            System.err.println("❌ Error getting report statistics by type: " + e.getMessage());
            throw e;
        } finally {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            if (conn != null) conn.close();
        }
        
        return stats;
    }
    
    /**
     * Debug method to show report table contents with report type
     */
    public void debugReportTable() throws SQLException {
        System.out.println("=== DEBUG REPORT TABLE (WITH REPORTTYPE) ===");
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = ConnectionManager.getConnection();
            
            // Show all reports with counts and types
            String debugSql = """
                SELECT reportid, 
                       TO_CHAR(MIN(generateddate), 'YYYY-MM-DD') as first_generated,
                       MAX(reporttype) as report_type,
                       COUNT(*) as total_records,
                       COUNT(applicationid) as app_records,
                       LISTAGG(applicationid, ',') WITHIN GROUP (ORDER BY applicationid) as application_list
                FROM report 
                GROUP BY reportid 
                ORDER BY MIN(generateddate) DESC
                """;
            
            pstmt = conn.prepareStatement(debugSql);
            rs = pstmt.executeQuery();
            
            System.out.println("📊 Current Report Table Contents (with Report Type):");
            System.out.println("ReportID | Generated  | Type     | Total | Apps | Application List");
            System.out.println("---------|------------|----------|-------|------|------------------");
            
            while (rs.next()) {
                String reportId = rs.getString("reportid");
                String generated = rs.getString("first_generated");
                String reportType = rs.getString("report_type");
                int totalRecords = rs.getInt("total_records");
                int appRecords = rs.getInt("app_records");
                String appList = rs.getString("application_list");
                
                System.out.printf("%-8s | %-10s | %-8s | %-5d | %-4d | %s%n", 
                    reportId, 
                    generated != null ? generated : "NULL",
                    reportType != null ? reportType : "NULL",
                    totalRecords,
                    appRecords,
                    appList != null ? (appList.length() > 50 ? appList.substring(0, 47) + "..." : appList) : "NULL"
                );
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Error in debug: " + e.getMessage());
        } finally {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            if (conn != null) conn.close();
        }
    }
    
    // ... [Keep all other existing methods: deleteReport, getTotalReportCount, reportExists, etc.]
    // ... [They remain the same as they don't need report type functionality]
    
    /**
     * Delete report by ID (deletes all application relationships)
     */
    public boolean deleteReport(String reportId) throws SQLException {
        System.out.println("=== DELETING REPORT: " + reportId + " ===");
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = ConnectionManager.getConnection();
            
            String deleteSql = "DELETE FROM report WHERE reportid = ?";
            pstmt = conn.prepareStatement(deleteSql);
            pstmt.setString(1, reportId);
            
            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("✅ Deleted " + rowsAffected + " report records");
                return true;
            } else {
                System.err.println("❌ No report found with ID: " + reportId);
                return false;
            }
            
        } catch (SQLException e) {
            System.err.println("❌ SQL Error in deleteReport: " + e.getMessage());
            throw e;
        } finally {
            if (pstmt != null) pstmt.close();
            if (conn != null) conn.close();
        }
    }
    
    /**
     * Get total number of unique reports
     */
    public int getTotalReportCount() throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = ConnectionManager.getConnection();
            
            String countSql = "SELECT COUNT(DISTINCT reportid) FROM report";
            pstmt = conn.prepareStatement(countSql);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
            
            return 0;
            
        } catch (SQLException e) {
            System.err.println("❌ SQL Error in getTotalReportCount: " + e.getMessage());
            throw e;
        } finally {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            if (conn != null) conn.close();
        }
    }
    
    /**
     * Check if report ID exists
     */
    public boolean reportExists(String reportId) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = ConnectionManager.getConnection();
            
            String checkSql = "SELECT 1 FROM report WHERE reportid = ? AND ROWNUM = 1";
            pstmt = conn.prepareStatement(checkSql);
            pstmt.setString(1, reportId);
            rs = pstmt.executeQuery();
            
            return rs.next();
            
        } catch (SQLException e) {
            System.err.println("❌ SQL Error in reportExists: " + e.getMessage());
            throw e;
        } finally {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            if (conn != null) conn.close();
        }
    }
}
package elms.model;

/**
 * Report model class with reporttype support
 */
public class Report {
    private String reportid;
    private String generateddate;
    private String applicationid;
    private String reporttype;  // NEW FIELD
    
    // Default constructor
    public Report() {
    }
    
    // Constructor with all fields
    public Report(String reportid, String generateddate, String applicationid, String reporttype) {
        this.reportid = reportid;
        this.generateddate = generateddate;
        this.applicationid = applicationid;
        this.reporttype = reporttype;
    }
    
    // Getters and Setters
    public String getReportid() {
        return reportid;
    }
    
    public void setReportid(String reportid) {
        this.reportid = reportid;
    }
    
    public String getGenerateddate() {
        return generateddate;
    }
    
    public void setGenerateddate(String generateddate) {
        this.generateddate = generateddate;
    }
    
    public String getApplicationid() {
        return applicationid;
    }
    
    public void setApplicationid(String applicationid) {
        this.applicationid = applicationid;
    }
    
    public String getReporttype() {
        return reporttype;
    }
    
    public void setReporttype(String reporttype) {
        this.reporttype = reporttype;
    }
    
    @Override
    public String toString() {
        return "Report{" +
                "reportid='" + reportid + '\'' +
                ", generateddate='" + generateddate + '\'' +
                ", applicationid='" + applicationid + '\'' +
                ", reporttype='" + reporttype + '\'' +
                '}';
    }
}
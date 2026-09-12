package elms.model;

public class LeaveApplication {
	private String applicationid;
	private String employeeid;
	private String leavetypeid;
	private String leavestartdate;
	private String leaveenddate;
	private String leavestatus;
	private String managerid;
	private String leavereason;
	private String appliedon;
	private double leaveduration;
	private String attachment;
	private String rejectreason;
	private String reviewdate;

	// Add getter and setter
	public String getReviewdate() {
	    return reviewdate;
	}

	public void setReviewdate(String reviewdate) {
	    this.reviewdate = reviewdate;
	}

	public String getRejectReason() {
	    return rejectreason;
	}

	public void setRejectReason(String rejectReason) {
	    this.rejectreason = rejectReason;
	}
	
	
	public String getApplicationid() {
		return applicationid;
	}
	public void setApplicationid(String applicationid) {
		this.applicationid = applicationid;
	}
	public String getEmployeeid() {
		return employeeid;
	}
	public void setEmployeeid(String employeeid) {
		this.employeeid = employeeid;
	}
	public String getLeavetypeid() {
		return leavetypeid;
	}
	public void setLeavetypeid(String leavetypeid) {
		this.leavetypeid = leavetypeid;
	}
	public String getLeavestartdate() {
		return leavestartdate;
	}
	public void setLeavestartdate(String leavestartdate) {
		this.leavestartdate = leavestartdate;
	}
	public String getLeaveenddate() {
		return leaveenddate;
	}
	public void setLeaveenddate(String leaveenddate) {
		this.leaveenddate = leaveenddate;
	}
	public String getLeavestatus() {
		return leavestatus;
	}
	public void setLeavestatus(String leavestatus) {
		this.leavestatus = leavestatus;
	}
	public String getManagerid() {
		return managerid;
	}
	public void setManagerid(String managerid) {
		this.managerid = managerid;
	}
	public String getLeavereason() {
		return leavereason;
	}
	public void setLeavereason(String leavereason) {
		this.leavereason = leavereason;
	}
	public String getAppliedon() {
		return appliedon;
	}
	public void setAppliedon(String appliedon) {
		this.appliedon = appliedon;
	}
	public double getLeaveduration() {
		return leaveduration;
	}
	public void setLeaveduration(double d) {
		this.leaveduration = d;
	}

	
	public String getAttachment() {
		return attachment;
	}
	public void setAttachment(String attachment) {
		this.attachment = attachment;
		
	}
	 
    public boolean hasAttachment() {
        return attachment != null && !attachment.trim().isEmpty();
    }
}

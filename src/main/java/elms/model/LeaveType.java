package elms.model;

/**
 * LeaveType Model - Updated to support flexible duration configuration
 * A leave type can now be:
 * - Full Day only
 * - Half Day only
 * - Both Full Day and Half Day (employee chooses)
 * - Fixed Duration (must take exact amount) or Flexible Duration (up to maximum)
 */
public class LeaveType {
    private String leaveTypeId;
    private String adminId;
    private String leaveTypeCategory; // "Full Day", "Half Day", or "Both"
    private String leaveTypeName;
    private String leaveTypeDescription;
    private boolean requiresDocument;
    private boolean affectsBalance;
    private boolean fixedDuration; // NEW: true = fixed duration, false = flexible duration

    // Constructors
    public LeaveType() {
    }

    public LeaveType(String leaveTypeId, String adminId, String leaveTypeCategory,
                    String leaveTypeName, String leaveTypeDescription,
                    boolean requiresDocument, boolean affectsBalance, boolean fixedDuration) {
        this.leaveTypeId = leaveTypeId;
        this.adminId = adminId;
        this.leaveTypeCategory = leaveTypeCategory;
        this.leaveTypeName = leaveTypeName;
        this.leaveTypeDescription = leaveTypeDescription;
        this.requiresDocument = requiresDocument;
        this.affectsBalance = affectsBalance;
        this.fixedDuration = fixedDuration;
    }

    // Getters and Setters
    public String getLeaveTypeId() {
        return leaveTypeId;
    }

    public void setLeaveTypeId(String leaveTypeId) {
        this.leaveTypeId = leaveTypeId;
    }

    public String getAdminId() {
        return adminId;
    }

    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }

    public String getLeaveTypeCategory() {
        return leaveTypeCategory;
    }

    public void setLeaveTypeCategory(String leaveTypeCategory) {
        this.leaveTypeCategory = leaveTypeCategory;
    }

    public String getLeaveTypeName() {
        return leaveTypeName;
    }

    public void setLeaveTypeName(String leaveTypeName) {
        this.leaveTypeName = leaveTypeName;
    }

    public String getLeaveTypeDescription() {
        return leaveTypeDescription;
    }

    public void setLeaveTypeDescription(String leaveTypeDescription) {
        this.leaveTypeDescription = leaveTypeDescription;
    }

    public boolean isRequiresDocument() {
        return requiresDocument;
    }

    public void setRequiresDocument(boolean requiresDocument) {
        this.requiresDocument = requiresDocument;
    }

    public boolean isAffectsBalance() {
        return affectsBalance;
    }

    public void setAffectsBalance(boolean affectsBalance) {
        this.affectsBalance = affectsBalance;
    }

    public boolean isFixedDuration() {
        return fixedDuration;
    }

    public void setFixedDuration(boolean fixedDuration) {
        this.fixedDuration = fixedDuration;
    }

    // Utility methods
    public boolean isFullDayOnly() {
        return "Full Day".equalsIgnoreCase(leaveTypeCategory);
    }

    public boolean isHalfDayOnly() {
        return "Half Day".equalsIgnoreCase(leaveTypeCategory);
    }

    public boolean allowsBothDurations() {
        return "Both".equalsIgnoreCase(leaveTypeCategory);
    }

    @Override
    public String toString() {
        return "LeaveType{" +
                "leaveTypeId='" + leaveTypeId + '\'' +
                ", adminId='" + adminId + '\'' +
                ", leaveTypeCategory='" + leaveTypeCategory + '\'' +
                ", leaveTypeName='" + leaveTypeName + '\'' +
                ", leaveTypeDescription='" + leaveTypeDescription + '\'' +
                ", requiresDocument=" + requiresDocument +
                ", affectsBalance=" + affectsBalance +
                ", fixedDuration=" + fixedDuration +
                '}';
    }
}
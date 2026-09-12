package elms.model;

/**
 * Model class for FullDay leave type configuration
 */
public class FullDay {
    private String leaveTypeId;
    private int standardDuration;
    
    // Default constructor
    public FullDay() {
    }
    
    // Constructor with all fields
    public FullDay(String leaveTypeId, int standardDuration) {
        this.leaveTypeId = leaveTypeId;
        this.standardDuration = standardDuration;
    }
    
    // Getters and Setters
    public String getLeaveTypeId() {
        return leaveTypeId;
    }
    
    public void setLeaveTypeId(String leaveTypeId) {
        this.leaveTypeId = leaveTypeId;
    }
    
    public int getStandardDuration() {
        return standardDuration;
    }
    
    public void setStandardDuration(int standardDuration) {
        this.standardDuration = standardDuration;
    }
    
    @Override
    public String toString() {
        return "FullDay{" +
               "leaveTypeId='" + leaveTypeId + '\'' +
               ", standardDuration=" + standardDuration +
               '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        FullDay fullDay = (FullDay) obj;
        return leaveTypeId != null ? leaveTypeId.equals(fullDay.leaveTypeId) : fullDay.leaveTypeId == null;
    }
    
    @Override
    public int hashCode() {
        return leaveTypeId != null ? leaveTypeId.hashCode() : 0;
    }
}
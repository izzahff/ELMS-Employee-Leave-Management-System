package elms.model;

/**
 * Model class for HalfDay leave type configuration
 */
public class HalfDay {
    private String leaveTypeId;
    private String shift;
    
    
    // Default constructor
    public HalfDay() {
    }
    
   
    
    // Constructor with all fields
    public HalfDay(String leaveTypeId, String shift) {
        this.leaveTypeId = leaveTypeId;
        this.shift = shift;
    }
    
    // Getters and Setters
    public String getLeaveTypeId() {
        return leaveTypeId;
    }
    
    public void setLeaveTypeId(String leaveTypeId) {
        this.leaveTypeId = leaveTypeId;
    }
    
    public String getShift() {
        return shift;
    }
    
    public void setShift(String shift) {
        this.shift = shift;
    }
    
    @Override
    public String toString() {
        return "HalfDay{" +
               "leaveTypeId='" + leaveTypeId + '\'' +
               ", shift='" + shift + '\'' +
               '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        HalfDay halfDay = (HalfDay) obj;
        return leaveTypeId != null ? leaveTypeId.equals(halfDay.leaveTypeId) : halfDay.leaveTypeId == null;
    }
    
    @Override
    public int hashCode() {
        return leaveTypeId != null ? leaveTypeId.hashCode() : 0;
    }
}
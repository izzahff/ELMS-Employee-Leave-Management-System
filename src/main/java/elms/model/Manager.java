package elms.model;

public class Manager {
    private String managerid;
    private String managername;
    private String managerpassword;
    private String managernophone;
    private String manageremail;
    private String managerposition;
    private String profilePicturePath;
    
    // Display properties for JSP
    private String positionDisplayName;
    private String positionBadgeClass;
    
    // Default constructor
    public Manager() {
    }
    
    // Constructor with all fields
    public Manager(String managerid, String managername, String managerpassword, 
                  String managernophone, String manageremail, String managerposition) {
        this.managerid = managerid;
        this.managername = managername;
        this.managerpassword = managerpassword;
        this.managernophone = managernophone;
        this.manageremail = manageremail;
        this.managerposition = managerposition;
    }
    
    // Getters and Setters
    public String getManagerid() {
        return managerid;
    }
    
    public void setManagerid(String managerid) {
        this.managerid = managerid;
    }
    
    public String getManagername() {
        return managername;
    }
    
    public void setManagername(String managername) {
        this.managername = managername;
    }
    
    public String getManagerpassword() {
        return managerpassword;
    }
    
    public void setManagerpassword(String managerpassword) {
        this.managerpassword = managerpassword;
    }
    
    public String getManagernophone() {
        return managernophone;
    }
    
    public void setManagernophone(String managernophone) {
        this.managernophone = managernophone;
    }
    
    public String getManageremail() {
        return manageremail;
    }
    
    public void setManageremail(String manageremail) {
        this.manageremail = manageremail;
    }
    
    public String getManagerposition() {
        return managerposition;
    }
    
    public void setManagerposition(String managerposition) {
        this.managerposition = managerposition;
    }
    
    public String getProfilePicturePath() {
        return profilePicturePath;
    }
    
    public void setProfilePicturePath(String profilePicturePath) {
        this.profilePicturePath = profilePicturePath;
    }
    
    // Display properties getters and setters
    public String getPositionDisplayName() {
        return positionDisplayName;
    }
    
    public void setPositionDisplayName(String positionDisplayName) {
        this.positionDisplayName = positionDisplayName;
    }
    
    public String getPositionBadgeClass() {
        return positionBadgeClass;
    }
    
    public void setPositionBadgeClass(String positionBadgeClass) {
        this.positionBadgeClass = positionBadgeClass;
    }
    
    @Override
    public String toString() {
        return "Manager{" +
                "managerid='" + managerid + '\'' +
                ", managername='" + managername + '\'' +
                ", manageremail='" + manageremail + '\'' +
                ", managerposition='" + managerposition + '\'' +
                '}';
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Manager manager = (Manager) obj;
        return managerid != null ? managerid.equals(manager.managerid) : manager.managerid == null;
    }
    
    @Override
    public int hashCode() {
        return managerid != null ? managerid.hashCode() : 0;
    }
}
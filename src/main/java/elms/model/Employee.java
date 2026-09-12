package elms.model;
/*
 * Author: Your Name 
 * June 2025
 */

/**
 * Employee model class
 */
public class Employee {
    private int id;
    private String employeeid;
    private String employeename;
    private String employeenophone;
    private String employeeemail;
    private String employeepassword;
    private String profile_picture_path;
    
    public String getProfilePicturePath() {
		return profile_picture_path;
	}

	public void setProfilePicturePath(String profile_picture_path) {
		this.profile_picture_path = profile_picture_path;
	}

	// Default constructor
    public Employee() {
    }
    
    // Constructor with parameters (without auto-generated fields)
    public Employee(String employeename, String employeenophone, String employeeemail, String employeepassword) {
        this.employeename = employeename;
        this.employeenophone = employeenophone;
        this.employeeemail = employeeemail;
        this.employeepassword = employeepassword;
    }
    
    // Constructor with all parameters
    public Employee(int id, String employeeid, String employeename, String employeenophone, 
                   String employeeemail, String employeepassword ) {
        this.id = id;
        this.employeeid = employeeid;
        this.employeename = employeename;
        this.employeenophone = employeenophone;
        this.employeeemail = employeeemail;
        this.employeepassword = employeepassword;
       
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getEmployeeId() {
        return employeeid;
    }
    
    public void setEmployeeId(String employeeid) {
        this.employeeid = employeeid;
    }
    
    public String getEmployeeName() {
        return employeename;
    }
    
    public void setEmployeeName(String employeename) {
        this.employeename = employeename;
    }
    
    public String getEmployeeNoPhone() {
        return employeenophone;
    }
    
    public void setEmployeeNoPhone(String employeenophone) {
        this.employeenophone = employeenophone;
    }
    
    public String getEmployeeEmail() {
        return employeeemail;
    }
    
    public void setEmployeeEmail(String employeeemail) {
        this.employeeemail = employeeemail;
    }
    
    public String getEmployeePassword() {
        return employeepassword;
    }
    
    public void setEmployeePassword(String employeepassword) {
        this.employeepassword = employeepassword;
    }
    
   
    
    @Override
    public String toString() {
        return "Employee{" +
                "id=" + id +
                ", employeeId='" + employeeid + '\'' +
                ", employeeName='" + employeename + '\'' +
                ", employeeNoPhone='" + employeenophone + '\'' +
                ", employeeEmail='" + employeeemail + '\'' +
               ", profilePicturePath='" + profile_picture_path + '\'' +
                '}';
    }
}

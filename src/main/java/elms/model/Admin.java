package elms.model;

public class Admin {
	
	private String adminId;
	private String adminPassword;
	private String adminName;
	private String adminNoPhone;
	private String adminEmail;
	private String profile_picture_path;
	
	
	public String getAdminId() {
		return adminId;
	}
	public void setAdminId(String adminId) {
		this.adminId = adminId;
	}
	public String getAdminPassword() {
		return adminPassword;
	}
	public void setAdminPassword(String adminPassword) {
		this.adminPassword = adminPassword;
	}
	public String getAdminName() {
		return adminName;
	}
	public void setAdminName(String adminName) {
		this.adminName = adminName;
	}
	public String getAdminNoPhone() {
		return adminNoPhone;
	}
	public String getProfile_picture_path() {
		return profile_picture_path;
	}
	public void setProfile_picture_path(String profile_picture_path) {
		this.profile_picture_path = profile_picture_path;
	}
	public void setAdminNoPhone(String adminNoPhone) {
		this.adminNoPhone = adminNoPhone;
	}
	public String getAdminEmail() {
		return adminEmail;
	}
	public void setAdminEmail(String adminEmail) {
		this.adminEmail = adminEmail;
	}
	
}

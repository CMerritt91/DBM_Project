package dbmproject.models;

public class Admin {

    private int adminId;
    private Integer aEmployeeId;

    public Admin() {}

    public Admin(Integer aEmployeeId) {
        this.aEmployeeId = aEmployeeId;
    }

    public int getAdminId() {
        return adminId;
    }

    public void setAdminId(int adminId) {
        this.adminId = adminId;
    }

    public Integer getAEmployeeId() {
        return aEmployeeId;
    }

    public void setAEmployeeId(Integer aEmployeeId) {
        this.aEmployeeId = aEmployeeId;
    }
}

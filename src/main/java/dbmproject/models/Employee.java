package dbmproject.models;

public class Employee {

    private int employeeId;
    private String jobTitle;
    private Integer eUserId;

    public Employee() {}

    public Employee(String jobTitle, Integer eUserId) {
        this.jobTitle = jobTitle;
        this.eUserId = eUserId;
    }

    public int getEmployeeId() { return employeeId; }
    public void setEmployeeId(int employeeId) { this.employeeId = employeeId; }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public Integer getEUserId() { return eUserId; }
    public void setEUserId(Integer eUserId) { this.eUserId = eUserId; }
}
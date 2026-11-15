package dbmproject.models;

import java.time.LocalDate;

public class Reporting {

    private int reportId;
    private String reportType;
    private LocalDate dateGenerated;
    private Integer generatedByEmployeeId;

    public Reporting() {}

    public Reporting(String reportType, LocalDate dateGenerated, Integer generatedByEmployeeId) {
        this.reportType = reportType;
        this.dateGenerated = dateGenerated;
        this.generatedByEmployeeId = generatedByEmployeeId;
    }

    public int getReportId() {
        return reportId;
    }

    public void setReportId(int reportId) {
        this.reportId = reportId;
    }

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public LocalDate getDateGenerated() {
        return dateGenerated;
    }

    public void setDateGenerated(LocalDate dateGenerated) {
        this.dateGenerated = dateGenerated;
    }

    public Integer getGeneratedByEmployeeId() {
        return generatedByEmployeeId;
    }

    public void setGeneratedByEmployeeId(Integer generatedByEmployeeId) {
                this.generatedByEmployeeId = generatedByEmployeeId;
    }
}

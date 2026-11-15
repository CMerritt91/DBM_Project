package dbmproject.models;

import java.time.LocalDate;

public class AdoptionApplication {

    private int applicationId;
    private LocalDate dateSubmitted;
    private String applicationStatus;
    private Integer adopterUserId;
    private Integer reviewedByEmployeeId;
    private Integer aaPetId;

    public AdoptionApplication() {}

    public AdoptionApplication(LocalDate dateSubmitted, String applicationStatus,
                               Integer adopterUserId, Integer reviewedByEmployeeId,
                               Integer aaPetId) {
        this.dateSubmitted = dateSubmitted;
        this.applicationStatus = applicationStatus;
        this.adopterUserId = adopterUserId;
        this.reviewedByEmployeeId = reviewedByEmployeeId;
        this.aaPetId = aaPetId;
    }

    public int getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(int applicationId) {
        this.applicationId = applicationId;
    }

    public LocalDate getDateSubmitted() {
        return dateSubmitted;
    }

    public void setDateSubmitted(LocalDate dateSubmitted) {
        this.dateSubmitted = dateSubmitted;
    }

    public String getApplicationStatus() {
        return applicationStatus;
    }

    public void setApplicationStatus(String applicationStatus) {
        this.applicationStatus = applicationStatus;
    }

    public Integer getAdopterUserId() {
        return adopterUserId;
    }

    public void setAdopterUserId(Integer adopterUserId) {
        this.adopterUserId = adopterUserId;
    }

    public Integer getReviewedByEmployeeId() {
        return reviewedByEmployeeId;
    }

    public void setReviewedByEmployeeId(Integer reviewedByEmployeeId) {
        this.reviewedByEmployeeId = reviewedByEmployeeId;
    }

    public Integer getAaPetId() {
        return aaPetId;
    }

    public void setAaPetId(Integer aaPetId) {
        this.aaPetId = aaPetId;
    }
}
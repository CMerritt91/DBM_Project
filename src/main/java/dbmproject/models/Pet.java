package dbmproject.models;

import java.time.LocalDate;
import java.time.Period;

public class Pet {

    private int petId;
    private String name;
    private LocalDate dob;
    private String description;
    private LocalDate intakeDate;
    private String photoUrl;
    private String adoptionStatus;
    private String sex;
    private Integer kennelId;
    private Integer breedId;
    private Integer managedByEmployeeId;
    private Integer assistedByVolunteerId;

    public Pet() {}

    public Pet(String name, LocalDate dob, String description, LocalDate intakeDate,
               String photoUrl, String adoptionStatus, String sex,
               Integer kennelId, Integer breedId,
               Integer managedByEmployeeId, Integer assistedByVolunteerId) {
        this.name = name;
        this.dob = dob;
        this.description = description;
        this.intakeDate = intakeDate;
        this.photoUrl = photoUrl;
        this.adoptionStatus = adoptionStatus;
        this.sex = sex;
        this.kennelId = kennelId;
        this.breedId = breedId;
        this.managedByEmployeeId = managedByEmployeeId;
        this.assistedByVolunteerId = assistedByVolunteerId;
    }

    public int getPetId() { return petId; }
    public void setPetId(int petId) { this.petId = petId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LocalDate getDob() { return dob; }
    public void setDob(LocalDate dob) { this.dob = dob; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getIntakeDate() { return intakeDate; }
    public void setIntakeDate(LocalDate intakeDate) { this.intakeDate = intakeDate; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public String getAdoptionStatus() { return adoptionStatus; }
    public void setAdoptionStatus(String adoptionStatus) { this.adoptionStatus = adoptionStatus; }

    public String getSex() { return sex; }
    public void setSex(String sex) { this.sex = sex; }

    public Integer getKennelId() { return kennelId; }
    public void setKennelId(Integer kennelId) { this.kennelId = kennelId; }

    public Integer getBreedId() { return breedId; }
    public void setBreedId(Integer breedId) { this.breedId = breedId; }

    public Integer getManagedByEmployeeId() { return managedByEmployeeId; }
    public void setManagedByEmployeeId(Integer managedByEmployeeId) { this.managedByEmployeeId = managedByEmployeeId; }

    public Integer getAssistedByVolunteerId() { return assistedByVolunteerId; }
    public void setAssistedByVolunteerId(Integer assistedByVolunteerId) { this.assistedByVolunteerId = assistedByVolunteerId; }

    // Calculated from the date of birth, not stored in the database
    public int getAgeInYears() {
        return dob == null ? 0 : Period.between(dob, LocalDate.now()).getYears();
    }
}
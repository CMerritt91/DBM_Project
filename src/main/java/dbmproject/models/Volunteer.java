package dbmproject.models;

public class Volunteer {

    private int vUserId;
    private String availability;

    public Volunteer() {}

    public Volunteer(int vUserId, String availability) {
        this.vUserId = vUserId;
        this.availability = availability;
    }

    public int getVUserId() { return vUserId; }
    public void setVUserId(int vUserId) { this.vUserId = vUserId; }

    public String getAvailability() { return availability; }
    public void setAvailability(String availability) { this.availability = availability; }
}
package dbmproject.models;

public class Kennel {

    private int kennelId;
    private Integer kennelNumber;
    private Integer capacity;
    private String location;

    public Kennel() {}

    public Kennel(Integer kennelNumber, Integer capacity, String location) {
        this.kennelNumber = kennelNumber;
        this.capacity = capacity;
        this.location = location;
    }

    public int getKennelId() {
        return kennelId;
    }

    public void setKennelId(int kennelId) {
        this.kennelId = kennelId;
    }

    public Integer getKennelNumber() {
        return kennelNumber;
    }

    public void setKennelNumber(Integer kennelNumber) {
        this.kennelNumber = kennelNumber;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}

package dbmproject.models;

public class Species {

    private int speciesId;
    private String speciesName;

    public Species() {}

    public Species(String speciesName) {
        this.speciesName = speciesName;
    }

    public int getSpeciesId() {
        return speciesId;
    }

    public void setSpeciesId(int speciesId) {
        this.speciesId = speciesId;
    }

    public String getSpeciesName() {
        return speciesName;
    }

    public void setSpeciesName(String speciesName) {
        this.speciesName = speciesName;
    }
}
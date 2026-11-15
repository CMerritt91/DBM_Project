package dbmproject.models;

public class Breed {

    private int breedId;
    private String breedName;
    private Integer bSpeciesId;

    public Breed() {}

    public Breed(String breedName, Integer bSpeciesId) {
        this.breedName = breedName;
        this.bSpeciesId = bSpeciesId;
    }

    public int getBreedId() {
        return breedId;
    }

    public void setBreedId(int breedId) {
        this.breedId = breedId;
    }

    public String getBreedName() {
        return breedName;
    }

    public void setBreedName(String breedName) {
        this.breedName = breedName;
    }

    public Integer getBSpeciesId() {
        return bSpeciesId;
    }

    public void setBSpeciesId(Integer bSpeciesId) {
        this.bSpeciesId = bSpeciesId;
    }
}

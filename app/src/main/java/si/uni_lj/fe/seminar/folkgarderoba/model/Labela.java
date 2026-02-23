package si.uni_lj.fe.seminar.folkgarderoba.model;

import com.google.gson.annotations.SerializedName;

public class Labela {

    @SerializedName("id")
    private int id;

    @SerializedName("naziv")   // must match backend JSON
    private String naziv;

    @SerializedName("tip")
    private String tip;

    public int getId() {
        return id;
    }

    public String getNaziv() {
        return naziv;
    }

    public String getTip() {
        return tip;
    }
}

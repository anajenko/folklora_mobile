package si.uni_lj.fe.seminar.folkgarderoba.model;

import com.google.gson.annotations.SerializedName;

public class Kos {

    @SerializedName("id")
    private int id;

    @SerializedName("ime")
    private String ime;

    @SerializedName("tip")
    private String tip;

    @SerializedName("poskodovano")
    private int poskodovano;

    public int getId() { return id; }
    public String getIme() { return ime; }
    public String getTip() { return tip; }
    public int getPoskodovano() { return poskodovano; }
}

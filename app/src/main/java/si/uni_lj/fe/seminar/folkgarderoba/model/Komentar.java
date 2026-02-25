package si.uni_lj.fe.seminar.folkgarderoba.model;

import com.google.gson.annotations.SerializedName;

public class Komentar {

    @SerializedName("id")
    private int id;

    @SerializedName("kos_id")
    private int kosId;

    @SerializedName("besedilo")
    private String besedilo;

    public int getId() {
        return id;
    }

    public int getKosId() {
        return kosId;
    }

    public String getBesedilo() {
        return besedilo;
    }
}
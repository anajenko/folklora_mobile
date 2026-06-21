package si.uni_lj.fe.seminar.folkgarderoba.model;

import com.google.gson.annotations.SerializedName;

public class Komentar {

    @SerializedName("id")
    private int id;

    @SerializedName("kos_id")
    private int kosId;

    @SerializedName("besedilo")
    private String besedilo;

    @SerializedName("uporabnik_id")
    private int uporabnikId;

    @SerializedName("uporabnisko_ime")
    private String uporabnisko_ime;

    public int getId() { return id; }

    public String getBesedilo() { return besedilo; }

    public String getUporabnisko_ime() { return uporabnisko_ime; }
}
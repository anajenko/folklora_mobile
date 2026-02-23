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

    // getterji in setterji
    public int getId() { return id; }
    public String getIme() { return ime; }
    public String getTip() { return tip; }
    public boolean isPoskodovano() { return poskodovano != 0; }

    public void setId(int id) { this.id = id; }
    public void setIme(String ime) { this.ime = ime; }
    public void setTip(String tip) { this.tip = tip; }
    public void setPoskodovano(int poskodovano) { this.poskodovano = poskodovano; }
}

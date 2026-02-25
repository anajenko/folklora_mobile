package si.uni_lj.fe.seminar.folkgarderoba.model;

import com.google.gson.annotations.SerializedName;

public class KomentarUpdateRequest {
    @SerializedName("besedilo")
    private String besedilo;

    @SerializedName("kos_id")
    private int kosId;

    public KomentarUpdateRequest(String besedilo, int kosId){
        this.besedilo = besedilo;
        this.kosId = kosId;
    }
}
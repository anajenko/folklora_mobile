package si.uni_lj.fe.seminar.folkgarderoba.model;

public class KosUpdateRequest {

    private Boolean poskodovano;
    private String ime;

    public KosUpdateRequest(Boolean poskodovano) {
        this.poskodovano = poskodovano;
    }

    public Boolean getPoskodovano() {
        return poskodovano;
    }

    public String getIme() {
        return ime;
    }
}

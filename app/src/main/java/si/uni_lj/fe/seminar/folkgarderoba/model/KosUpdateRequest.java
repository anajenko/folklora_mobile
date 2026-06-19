package si.uni_lj.fe.seminar.folkgarderoba.model;

public class KosUpdateRequest {

    private int poskodovano;
    private String ime;

    public KosUpdateRequest(int poskodovano) {
        this.poskodovano = poskodovano;
    }

    public int getPoskodovano() {
        return poskodovano;
    }

    public String getIme() {
        return ime;
    }
}

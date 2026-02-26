package si.uni_lj.fe.seminar.folkgarderoba.model;

public class KomentarCreateRequest {
    private String besedilo;

    public KomentarCreateRequest(String besedilo) {
        this.besedilo = besedilo;
    }

    public String getBesedilo() {
        return besedilo;
    }

    public void setBesedilo(String besedilo) {
        this.besedilo = besedilo;
    }
}
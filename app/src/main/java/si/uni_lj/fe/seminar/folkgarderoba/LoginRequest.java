package si.uni_lj.fe.seminar.folkgarderoba;

public class LoginRequest {
    private String uporabnisko_ime;
    private String geslo;

    public LoginRequest(String uporabnisko_ime, String geslo) {
        this.uporabnisko_ime = uporabnisko_ime;
        this.geslo = geslo;
    }
}

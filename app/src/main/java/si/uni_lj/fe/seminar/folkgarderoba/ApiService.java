package si.uni_lj.fe.seminar.folkgarderoba;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import si.uni_lj.fe.seminar.folkgarderoba.model.Komentar;
import si.uni_lj.fe.seminar.folkgarderoba.model.KomentarUpdateRequest;
import si.uni_lj.fe.seminar.folkgarderoba.model.Kos;
import si.uni_lj.fe.seminar.folkgarderoba.model.Labela;

public interface ApiService {

    @POST("api/uporabniki/prijava")
    Call<LoginResponse> login(@Body LoginRequest request);

    @GET("api/kosi")
    Call<List<Kos>> getKosi();

    @GET("api/kosi")
    Call<List<Kos>> getKosiFiltered(@Query("labels") String labels);

    @GET("api/kosi/{id}")
    Call<ResponseBody> getSlika(@Path("id") int id);
    @GET("api/labele/kos/{id}")
    Call<List<Labela>> getLabeleZaKos(@Path("id") int id);

    @GET("api/komentarji/kos/{kosId}")
    Call<List<Komentar>> getKomentarjiZaKos(@Path("kosId") int kosId);

    @GET("api/komentarji/{id}")
    Call<Komentar> getKomentar(@Path("id") int id);

    @PUT("api/komentarji/{id}")
    Call<Void> updateKomentar(@Path("id") int id, @Body KomentarUpdateRequest request);

    @DELETE("api/komentarji/{id}")
    Call<Void> deleteKomentar(@Path("id") int id);

    @GET("api/labele")
    Call<List<Labela>> getLabele();

}


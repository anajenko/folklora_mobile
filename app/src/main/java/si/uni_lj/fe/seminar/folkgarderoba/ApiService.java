package si.uni_lj.fe.seminar.folkgarderoba;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import si.uni_lj.fe.seminar.folkgarderoba.model.Kos;

public interface ApiService {

    @POST("api/uporabniki/prijava")
    Call<LoginResponse> login(@Body LoginRequest request);

    @GET("api/kosi")
    Call<List<Kos>> getKosi();

    @GET("api/kosi/{id}")
    Call<ResponseBody> getSlika(@Path("id") int id);

}


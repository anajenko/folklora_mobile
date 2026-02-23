package si.uni_lj.fe.seminar.folkgarderoba;

import android.content.Context;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    public static final String BASE_URL = "http://192.168.1.15:3000/"; // zamenjaj z IP računalnika

    // vedno ustvari novo instanco, da AuthInterceptor vidi aktualen token
    public static Retrofit getRetrofitInstance(Context context) {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(new AuthInterceptor(context))
                .addInterceptor(new UnauthorizedInterceptor(context))
                .build();

        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }
}

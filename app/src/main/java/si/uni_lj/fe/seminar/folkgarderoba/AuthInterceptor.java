package si.uni_lj.fe.seminar.folkgarderoba;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {

    private Context context;

    public AuthInterceptor(Context context) {
        this.context = context;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {

        SharedPreferences prefs =
                context.getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE);

        String token = prefs.getString("token", null);
        Log.d("AuthInterceptor", "Adding token: " + token);
        Request original = chain.request();

        if (token != null) {
            Request newRequest = original.newBuilder()
                    .addHeader("Authorization", "Bearer " + token)
                    .build();
            return chain.proceed(newRequest);
        }

        return chain.proceed(original);
    }
}

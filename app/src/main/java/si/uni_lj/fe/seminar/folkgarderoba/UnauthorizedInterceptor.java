package si.uni_lj.fe.seminar.folkgarderoba;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

public class UnauthorizedInterceptor implements Interceptor {

    private Context context;

    public UnauthorizedInterceptor(Context context) {
        this.context = context;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {

        Response response = chain.proceed(chain.request());

        if (response.code() == 401) {

            SharedPreferences prefs =
                    context.getSharedPreferences("APP_PREFS", Context.MODE_PRIVATE);

            prefs.edit().clear().apply();

            Intent intent = new Intent(context, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
        }

        return response;
    }
}

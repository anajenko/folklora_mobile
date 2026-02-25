package si.uni_lj.fe.seminar.folkgarderoba;

import android.util.Base64;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class JwtUtils {

    public static int getUserIdFromToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) return -1;

            String payload = parts[1];
            byte[] decodedBytes = Base64.decode(payload, Base64.URL_SAFE);
            String decoded = new String(decodedBytes);

            JSONObject obj = new JSONObject(decoded);
            return obj.getInt("user_id"); // Make sure your token has "user_id"
        } catch (JSONException e) {
            Log.e("JwtUtils", "Failed to parse JWT", e);
            return -1;
        }
    }
}
package si.uni_lj.fe.seminar.folkgarderoba;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import si.uni_lj.fe.seminar.folkgarderoba.adapter.KosAdapter;
import si.uni_lj.fe.seminar.folkgarderoba.model.Kos;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private List<Kos> kosList = new ArrayList<>();
    private KosAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences prefs = getSharedPreferences("APP_PREFS", MODE_PRIVATE);
        String token = prefs.getString("token", null);

        if (token == null) {
            // ni prijavljen
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        String username = prefs.getString("username", "");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Prijavljen: " + username);
        }

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new KosAdapter(kosList, kos -> {
            // tukaj bo kasneje DetailActivity
        });
        recyclerView.setAdapter(adapter);

        loadKosi();
    }

    private void loadKosi() {
        ApiService apiService = RetrofitClient
                .getRetrofitInstance(this) // pošlji aktualni Context
                .create(ApiService.class);

        apiService.getKosi().enqueue(new Callback<List<Kos>>() {
            @Override
            public void onResponse(Call<List<Kos>> call, Response<List<Kos>> response) {
                Log.d("MainActivity", "Response code: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    kosList.clear();
                    kosList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                } else {
                    Log.e("MainActivity", "Napaka pri API: " + response.errorBody());
                    Toast.makeText(MainActivity.this,
                            "Napaka pri nalaganju kosov", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Kos>> call, Throwable t) {
                Log.e("MainActivity", "API ERROR", t);
                Toast.makeText(MainActivity.this,
                        "Napaka pri nalaganju kosov", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("Odjava");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if ("Odjava".equals(item.getTitle())) {
            SharedPreferences prefs = getSharedPreferences("APP_PREFS", MODE_PRIVATE);
            prefs.edit().clear().apply();

            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
        return true;
    }
}

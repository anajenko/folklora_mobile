package si.uni_lj.fe.seminar.folkgarderoba;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
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
    private TextView selectedLabelsText;
    private ArrayList<Integer> currentFilterIds = new ArrayList<>();
    private ArrayList<String> currentFilterNazivi = new ArrayList<>();
    private ActivityResultLauncher<Intent> filterLauncher;
    private TextView filterTitleText;
    private Button clearFiltersButton;

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

        filterTitleText = findViewById(R.id.filterTitleText);
        clearFiltersButton = findViewById(R.id.clearFiltersButton);

        // Filter UI
        selectedLabelsText = findViewById(R.id.selectedLabelsText);

        // Register result launcher
        filterLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        currentFilterIds = result.getData()
                                .getIntegerArrayListExtra("selectedLabelIds");
                        currentFilterNazivi =
                                result.getData().getStringArrayListExtra("selectedLabelNazivi");
                        updateFilterText();
                        loadKosi(currentFilterIds);
                    }
                }
        );

        Button chooseFiltersButton = findViewById(R.id.chooseFiltersButton);
        chooseFiltersButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, FilterActivity.class);
            intent.putIntegerArrayListExtra("selectedLabelIds", currentFilterIds);
            filterLauncher.launch(intent);
        });

        clearFiltersButton.setOnClickListener(v -> {
            // Clear selected filters
            currentFilterIds.clear();
            currentFilterNazivi.clear();

            // Update filter text + buttons
            updateFilterText();

            // Reload unfiltered list
            loadKosi(null);
        });

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new KosAdapter(kosList, kos -> {
            // tukaj bo kasneje DetailActivity
        });
        recyclerView.setAdapter(adapter);

        loadKosi(null);
        updateFilterText();
    }

    private void updateFilterText() {
        if (currentFilterIds == null || currentFilterIds.isEmpty()) {
            filterTitleText.setVisibility(View.GONE);
            selectedLabelsText.setVisibility(View.GONE);
            clearFiltersButton.setVisibility(View.GONE);   // hide clear button
        } else {
            filterTitleText.setVisibility(View.VISIBLE);
            selectedLabelsText.setVisibility(View.VISIBLE);
            clearFiltersButton.setVisibility(View.VISIBLE); // show clear button
            selectedLabelsText.setText(String.join(", ", currentFilterNazivi));
        }
    }



    private void loadKosi(@Nullable ArrayList<Integer> labelIds) {
        ApiService apiService = RetrofitClient
                .getRetrofitInstance(this) // pošlji aktualni Context
                .create(ApiService.class);

        Call<List<Kos>> call;

        if (labelIds != null && !labelIds.isEmpty()) {
            String labelsQuery = TextUtils.join(",", labelIds);
            call = apiService.getKosiFiltered(labelsQuery);
        } else {
            call = apiService.getKosi();
        }

        call.enqueue(new Callback<List<Kos>>() {
            @Override
            public void onResponse(Call<List<Kos>> call, Response<List<Kos>> response) {
                Log.d("MainActivity", "Response code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    kosList.clear();
                    kosList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(MainActivity.this,
                            "Napaka pri nalaganju kosov",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Kos>> call, Throwable t) {
                Log.e("MainActivity", "API ERROR", t);
                Toast.makeText(MainActivity.this,
                        "Napaka pri nalaganju kosov",
                        Toast.LENGTH_SHORT).show();
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

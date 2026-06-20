package si.uni_lj.fe.seminar.folkgarderoba;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
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
    private TextView clearFiltersButton;

    private int samoPoskodovani = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences prefs = getSharedPreferences("APP_PREFS", MODE_PRIVATE);
        String token = prefs.getString("token", null);

        if (token == null) {
            startActivity(new Intent(this, LoginActivity.class));
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
            return;
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ImageView menuButton = findViewById(R.id.menuButton);

        menuButton.setOnClickListener(v -> {
            // Inflate a custom layout
            LinearLayout layout = new LinearLayout(MainActivity.this);
            layout.setOrientation(LinearLayout.HORIZONTAL);
            layout.setPadding(24, 24, 24, 24);
            layout.setGravity(Gravity.CENTER_VERTICAL);
            layout.setBackgroundColor(Color.parseColor("#333333")); // dark background

            // Username TextView
            TextView usernameTv = new TextView(MainActivity.this);
            usernameTv.setTextColor(Color.WHITE);
            usernameTv.setTextSize(16);
            usernameTv.setText(prefs.getString("username", ""));

            // Create LayoutParams with margins
            LinearLayout.LayoutParams usernameParams = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );

            // Set left margin in dp (e.g., 16dp)
            int leftMarginDp = 16;
            usernameParams.leftMargin = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    leftMarginDp,
                    getResources().getDisplayMetrics()
            );
            usernameTv.setLayoutParams(usernameParams);
            layout.addView(usernameTv);

            // Logout icon
            ImageView logoutIcon = new ImageView(MainActivity.this);
            logoutIcon.setImageResource(R.drawable.logout); // your icon
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics()),
                    (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics())
            );
            params.leftMargin = 30;
            params.rightMargin = 17;
            params.topMargin = 15;
            params.bottomMargin = 15;
            logoutIcon.setLayoutParams(params);
            layout.addView(logoutIcon);

            // Create popup window
            PopupWindow popup = new PopupWindow(layout,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    true); // focusable

            // Add shadow and elevation
            popup.setElevation(8);
            popup.setOutsideTouchable(true);
            popup.setBackgroundDrawable(getDrawable(R.drawable.popup_bg)); // rounded dark background

            // Show popup below button
            popup.showAsDropDown(v, 0, 10); // x-offset=0, y-offset=10

            // Handle click
            layout.setOnClickListener(ll -> {
                prefs.edit().clear().apply();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                finish();
                popup.dismiss();
            });
        });

        String username = prefs.getString("username", "");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
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
                        samoPoskodovani = result.getData()
                                .getIntExtra("samoPoskodovani", 0);
                        updateFilterText();
                        refreshKosi();
                    }
                }
        );

        Button chooseFiltersButton = findViewById(R.id.chooseFiltersButton);
        chooseFiltersButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, FilterActivity.class);
            intent.putIntegerArrayListExtra("selectedLabelIds", currentFilterIds);
            intent.putExtra("samoPoskodovani", samoPoskodovani);
            filterLauncher.launch(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });

        clearFiltersButton.setOnClickListener(v -> {
            // Clear selected filters
            currentFilterIds.clear();
            currentFilterNazivi.clear();
            samoPoskodovani = 0;

            // Update filter text + buttons
            updateFilterText();

            // Reload unfiltered list
            refreshKosi();
        });

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new KosAdapter(kosList, kos -> {
            Intent intent = new Intent(MainActivity.this, KosDetailActivity.class);
            intent.putExtra("kosId", kos.getId());
            intent.putExtra("kosIme", kos.getIme());
            intent.putExtra("poskodovano", kos.isPoskodovano());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);

        refreshKosi();
        updateFilterText();
    }

    private void updateFilterText() {
        if ((currentFilterIds == null || currentFilterIds.isEmpty())
                && samoPoskodovani == 0) {

            filterTitleText.setVisibility(View.GONE);
            selectedLabelsText.setVisibility(View.GONE);
            clearFiltersButton.setVisibility(View.GONE);

        } else {
            filterTitleText.setVisibility(View.VISIBLE);
            selectedLabelsText.setVisibility(View.VISIBLE);
            clearFiltersButton.setVisibility(View.VISIBLE);

            if (currentFilterNazivi != null && !currentFilterNazivi.isEmpty()) {
                selectedLabelsText.setText(String.join(", ", currentFilterNazivi));
            } else {
                selectedLabelsText.setText("Samo poškodovani");
            }
        }
    }

    private void loadKosi(@Nullable ArrayList<Integer> labelIds, @Nullable Integer poskodovano) {
        ApiService apiService = RetrofitClient
                .getRetrofitInstance(this) // pošlji aktualni Context
                .create(ApiService.class);

        String labelsQuery = null;

        if (labelIds != null && !labelIds.isEmpty()) {
            labelsQuery = TextUtils.join(",", labelIds);
        }

        Call<List<Kos>> call = apiService.getKosi(labelsQuery, poskodovano);

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
                            R.string.napaka_pri_nalaganju_kosov,
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Kos>> call, Throwable t) {
                Log.e("MainActivity", "API ERROR", t);
                Toast.makeText(MainActivity.this,
                        R.string.napaka_pri_nalaganju_kosov,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.menu_logout) {

            SharedPreferences prefs = getSharedPreferences("APP_PREFS", MODE_PRIVATE);
            prefs.edit().clear().apply();

            startActivity(new Intent(this, LoginActivity.class));
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onResume() {
        super.onResume();
        refreshKosi();
    }

    private void refreshKosi() {
        Integer poskodovanoFilter = null;

        if (samoPoskodovani == 1) {
            poskodovanoFilter = 1;
        }

        loadKosi(currentFilterIds, poskodovanoFilter);
    }
}

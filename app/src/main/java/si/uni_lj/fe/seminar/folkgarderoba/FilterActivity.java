package si.uni_lj.fe.seminar.folkgarderoba;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import si.uni_lj.fe.seminar.folkgarderoba.model.Labela;

public class FilterActivity extends AppCompatActivity {

    private LinearLayout labelsContainer;
    private Button applyButton, cancelButton;

    // Store CheckBox references to collect selected IDs
    private Map<Integer, CheckBox> checkBoxMap = new HashMap<>();

    private ArrayList<Integer> preselectedIds;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filter_activity);

        labelsContainer = findViewById(R.id.labelsContainer);
        applyButton = findViewById(R.id.applyButton);
        cancelButton = findViewById(R.id.cancelButton);

        preselectedIds = getIntent().getIntegerArrayListExtra("selectedLabelIds");
        fetchLabels();

        cancelButton.setOnClickListener(v -> finish());
        applyButton.setOnClickListener(v -> applyFilters());
    }

    private void fetchLabels() { //from backend
        ApiService apiService = RetrofitClient
                .getRetrofitInstance(this)
                .create(ApiService.class);

        apiService.getLabele().enqueue(new Callback<List<Labela>>() {
            @Override
            public void onResponse(Call<List<Labela>> call, Response<List<Labela>> response) {

                if (response.isSuccessful() && response.body() != null) {
                    populateLabels(response.body());
                } else {
                    Toast.makeText(FilterActivity.this,
                            "Napaka pri nalaganju label.",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Labela>> call, Throwable t) {
                Toast.makeText(FilterActivity.this,
                        "Napaka pri nalaganju label.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateLabels(List<Labela> labels) {

        labelsContainer.removeAllViews();

        // Group by tip
        Map<String, List<Labela>> grouped = new HashMap<>();
        for (Labela label : labels) {
            String tip = label.getTip();
            if (!grouped.containsKey(tip)) {
                grouped.put(tip, new ArrayList<>());
            }
            grouped.get(tip).add(label);
        }

        // 2️⃣ Define the display order and nice names
        Map<String, String> groupOrderMap = new LinkedHashMap<>();
        groupOrderMap.put("POKRAJINA", "POKRAJINA");
        groupOrderMap.put("TIP_OBLACILA", "TIP OBLAČILA");
        groupOrderMap.put("SPOL", "SPOL");
        groupOrderMap.put("VELIKOST", "VELIKOST");
        groupOrderMap.put("DRUGO", "DRUGO");

        Map<String, String> backendToDisplayKey = new HashMap<>();
        for (String key : groupOrderMap.keySet()) {
            backendToDisplayKey.put(key.toLowerCase(), key); // lowercase matching
        }

        for (Map.Entry<String, String> entry : groupOrderMap.entrySet()) {
            String key = entry.getKey();       // e.g., "TIP_OBLACILA"
            String displayName = entry.getValue(); // e.g., "TIP OBLAČILA"

            String backendTip = null;
            for (String tip : grouped.keySet()) {
                if (tip.equalsIgnoreCase(key)) {
                    backendTip = tip;
                    break;
                }
            }

            // Section title
            TextView typeText = new TextView(this);
            typeText.setText(displayName.toUpperCase());
            typeText.setTextSize(16);
            typeText.setPadding(0, 32, 0, 8);
            typeText.setTypeface(null, Typeface.BOLD);
            labelsContainer.addView(typeText);

            // Checkboxes
            for (Labela label : grouped.get(backendTip)) {

                CheckBox cb = new CheckBox(this);
                cb.setText(label.getNaziv());   // <-- naziv, not ime
                // ✅ auto-check if already selected
                if (preselectedIds != null && preselectedIds.contains(label.getId())) {
                    cb.setChecked(true);
                }

                labelsContainer.addView(cb);
                checkBoxMap.put(label.getId(), cb);
            }
        }
    }

    private void applyFilters() {

        ArrayList<Integer> selectedIds = new ArrayList<>();
        ArrayList<String> selectedNazivi = new ArrayList<>();

        for (Map.Entry<Integer, CheckBox> entry : checkBoxMap.entrySet()) {
            if (entry.getValue().isChecked()) {
                selectedIds.add(entry.getKey());
                selectedNazivi.add(entry.getValue().getText().toString());
            }
        }

        Intent resultIntent = new Intent();
        resultIntent.putIntegerArrayListExtra("selectedLabelIds", selectedIds);
        resultIntent.putStringArrayListExtra("selectedLabelNazivi", selectedNazivi);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }
}


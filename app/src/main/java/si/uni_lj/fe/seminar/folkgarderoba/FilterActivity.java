package si.uni_lj.fe.seminar.folkgarderoba;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.CompoundButtonCompat;

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

import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.flexbox.JustifyContent;

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

        for (Map.Entry<String, String> entry : groupOrderMap.entrySet()) {
            String backendTip = null;
            for (String tip : grouped.keySet()) {
                if (tip.equalsIgnoreCase(entry.getKey())) {
                    backendTip = tip;
                    break;
                }
            }
            if (backendTip == null) continue;

            // 1️⃣ Section title
            TextView typeText = new TextView(this);
            typeText.setText(entry.getValue().toUpperCase());
            typeText.setTextSize(16);
            typeText.setTypeface(null, Typeface.BOLD);
            typeText.setPadding(0, 60, 0, 14); // space above and below
            labelsContainer.addView(typeText);

            // 2️⃣ FlexboxLayout for checkboxes (two columns)
            FlexboxLayout groupFlex = new FlexboxLayout(this);
            groupFlex.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            groupFlex.setFlexDirection(FlexDirection.ROW);
            groupFlex.setFlexWrap(FlexWrap.WRAP);
            groupFlex.setJustifyContent(JustifyContent.FLEX_START);

            for (Labela label : grouped.get(backendTip)) {
                CheckBox cb = new CheckBox(this);
                cb.setButtonDrawable(R.drawable.custom_checkbox);
                cb.setText(label.getNaziv());

                // preselected
                if (preselectedIds != null && preselectedIds.contains(label.getId())) {
                    cb.setChecked(true);
                }

                int color = getResources().getColor(R.color.headerfooter);
                CompoundButtonCompat.setButtonTintList(cb, ColorStateList.valueOf(color));

                // Layout params: half width → two columns
                FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(
                        0,
                        FlexboxLayout.LayoutParams.WRAP_CONTENT
                );
                int verticalMargin = (int) (1 * getResources().getDisplayMetrics().density + 0.5f);
                int horizontalMargin = (int) (1 * getResources().getDisplayMetrics().density + 0.5f);
                params.setMargins(horizontalMargin, verticalMargin, horizontalMargin, verticalMargin);
                params.setFlexBasisPercent(0.5f); // half width → two columns
                params.setFlexGrow(1f);
                params.setFlexShrink(1f);
                cb.setLayoutParams(params);

                groupFlex.addView(cb);
                checkBoxMap.put(label.getId(), cb);
            }

            // 3️⃣ Add this group's FlexboxLayout to the parent LinearLayout
            labelsContainer.addView(groupFlex);
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


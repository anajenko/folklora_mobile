package si.uni_lj.fe.seminar.folkgarderoba;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import si.uni_lj.fe.seminar.folkgarderoba.model.Komentar;
import si.uni_lj.fe.seminar.folkgarderoba.model.KomentarUpdateRequest;
import si.uni_lj.fe.seminar.folkgarderoba.model.Labela;

public class KosDetailActivity extends AppCompatActivity {

    private TextView titleText, poskodovanoText;
    private ImageView imageView;
    private LinearLayout commentsContainer;

    private FlexboxLayout labelsContainer;

    private ApiService apiService;
    private int kosId;
    private String currentUsername;
    private EditText newCommentEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kos_detail);

        SharedPreferences prefs = getSharedPreferences("APP_PREFS", MODE_PRIVATE);
        currentUsername = prefs.getString("username", null);

        Toolbar toolbar = findViewById(R.id.detailToolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Podrobnosti kosa");
        }

        titleText = findViewById(R.id.detailTitle);
        imageView = findViewById(R.id.detailImage);
        poskodovanoText = findViewById(R.id.poskodovanoText);
        labelsContainer = findViewById(R.id.labelsContainer);
        commentsContainer = findViewById(R.id.commentsContainer);
        Button addCommentButton = findViewById(R.id.addCommentButton);
        newCommentEditText = findViewById(R.id.newCommentEditText);

        addCommentButton.setOnClickListener(v -> {
            String komentarText = newCommentEditText.getText().toString().trim();
            if (komentarText.isEmpty()) {
                newCommentEditText.setError("Komentar ne sme biti prazen");
                return;
            }
            addKomentar(komentarText);
        });

        apiService = RetrofitClient
                .getRetrofitInstance(this)
                .create(ApiService.class);

        kosId = getIntent().getIntExtra("kosId", -1);
        String kosIme = getIntent().getStringExtra("kosIme");
        boolean poskodovano = getIntent().getBooleanExtra("poskodovano", false);

        titleText.setText(kosIme);

        if (poskodovano) {
            poskodovanoText.setVisibility(View.VISIBLE);
        } else {
            poskodovanoText.setVisibility(View.GONE);
        }

        String imageUrl = RetrofitClient.BASE_URL + "api/kosi/" + kosId;
        Glide.with(this).load(imageUrl).into(imageView);

        loadLabele();
        loadKomentarji();

        ApiService apiService = RetrofitClient
                .getRetrofitInstance(this)
                .create(ApiService.class);
    }

    private void loadLabele() {
        apiService.getLabeleZaKos(kosId).enqueue(new Callback<List<Labela>>() {
            @Override
            public void onResponse(Call<List<Labela>> call, Response<List<Labela>> response) {
                if (response.isSuccessful() && response.body() != null) {

                    List<Labela> labels = response.body();

                    // Hide if no labels
                    if (labels.isEmpty()) {
                        labelsContainer.setVisibility(View.GONE);
                        return;
                    }
                    labelsContainer.setVisibility(View.VISIBLE);

                    // Clear any previous badges
                    labelsContainer.removeAllViews();

                    for (Labela label : labels) {

                        // Create badge
                        TextView badge = new TextView(new ContextThemeWrapper(
                                KosDetailActivity.this, android.R.style.Widget_Material_TextView
                        ));
                        badge.setText(label.getNaziv());
                        badge.setTextSize(14); // bigger text

                        badge.setPadding(24, 10, 24, 10);
                        badge.setBackgroundResource(R.drawable.label_badge_bg);

                        FlexboxLayout.LayoutParams params =
                                new FlexboxLayout.LayoutParams(
                                        ViewGroup.LayoutParams.WRAP_CONTENT,
                                        ViewGroup.LayoutParams.WRAP_CONTENT
                                );
                        params.setMargins(4, 4, 4, 4); // badges closer together
                        badge.setLayoutParams(params);

                        labelsContainer.addView(badge);
                    }
                } else {
                    labelsContainer.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<List<Labela>> call, Throwable t) {
                Log.e("LABEL_ERROR", t.getMessage());
                labelsContainer.setVisibility(View.GONE);
            }
        });
    }

    private void loadKomentarji() {
        apiService.getKomentarjiZaKos(kosId).enqueue(new Callback<List<Komentar>>() {
            @Override
            public void onResponse(Call<List<Komentar>> call, Response<List<Komentar>> response) {

                if (response.isSuccessful() && response.body() != null) {

                    commentsContainer.removeAllViews();

                    for (final Komentar komentar : response.body()) {
                        final Komentar thisKomentar = komentar;

                        // --- CREATE CARD ---
                        com.google.android.material.card.MaterialCardView card =
                                new com.google.android.material.card.MaterialCardView(KosDetailActivity.this);

                        LinearLayout.LayoutParams cardParams =
                                new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                );
                        cardParams.setMargins(0, 0, 0, 32);
                        card.setLayoutParams(cardParams);

                        card.setRadius(20f);
                        card.setCardElevation(4f);

                        // --- MAIN HORIZONTAL LAYOUT ---
                        LinearLayout mainRow = new LinearLayout(KosDetailActivity.this);
                        mainRow.setOrientation(LinearLayout.VERTICAL);
                        mainRow.setPadding(24, 24, 24, 24);
                        mainRow.setLayoutParams(new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        ));



                        // --- TOP ROW (USERNAME + ICONS) ---
                        LinearLayout topRow = new LinearLayout(KosDetailActivity.this);
                        topRow.setOrientation(LinearLayout.HORIZONTAL);
                        topRow.setLayoutParams(new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        ));

                        // USERNAME
                        TextView usernameTv = new TextView(KosDetailActivity.this);
                        usernameTv.setText(komentar.getUporabnisko_ime());
                        usernameTv.setTextSize(12);
                        usernameTv.setTextColor(Color.GRAY);
                        topRow.addView(usernameTv);

                        // SPACER to push icons to right
                        View spacer = new View(KosDetailActivity.this);
                        LinearLayout.LayoutParams spacerParams =
                                new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
                        spacer.setLayoutParams(spacerParams);
                        topRow.addView(spacer);

                        // ICONS (only for owner)
                        if (komentar.getUporabnisko_ime().equals(currentUsername)) {
                            LinearLayout iconsRow = new LinearLayout(KosDetailActivity.this);
                            iconsRow.setOrientation(LinearLayout.HORIZONTAL);

                            int sizeDp = 18;
                            float scale = getResources().getDisplayMetrics().density;
                            int sizePx = (int) (sizeDp * scale + 0.5f);

                            // EDIT ICON (pen)
                            ImageView editIcon = new ImageView(KosDetailActivity.this);
                            editIcon.setImageResource(R.drawable.pen);
                            LinearLayout.LayoutParams editParams =
                                    new LinearLayout.LayoutParams(sizePx, sizePx);
                            editParams.setMargins(0, 0, 24, 0); // margin between icons
                            editIcon.setLayoutParams(editParams);
                            editIcon.setOnClickListener(v -> enterEditMode(thisKomentar, card));

                            // DELETE ICON (X)
                            ImageView deleteIcon = new ImageView(KosDetailActivity.this);
                            deleteIcon.setImageResource(R.drawable.delete_icon);
                            LinearLayout.LayoutParams deleteParams =
                                    new LinearLayout.LayoutParams(sizePx, sizePx);
                            deleteIcon.setLayoutParams(deleteParams);
                            deleteIcon.setOnClickListener(v -> deleteKomentar(komentar.getId()));

                            iconsRow.addView(editIcon);
                            iconsRow.addView(deleteIcon);
                            topRow.addView(iconsRow);
                        }


                        // --- COMMENT TEXT ---
                        TextView commentTv = new TextView(KosDetailActivity.this);
                        String text = komentar.getBesedilo();
                        if (text == null) text = ""; // replace null with empty string
                        commentTv.setText(text.trim());
                        commentTv.setTextSize(16);
                        commentTv.setPadding(0, 12, 0, 0);
                        commentTv.setLayoutParams(new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        ));

                        mainRow.addView(topRow);
                        mainRow.addView(commentTv);
                        card.addView(mainRow);
                        commentsContainer.addView(card);
                    }

                } else {
                    Log.e("COMMENT_ERROR", "Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Komentar>> call, Throwable t) {
                Log.e("COMMENT_ERROR", t.getMessage());
            }
        });
    }

    private void addKomentar(String komentarText) {

        // Call backend
        apiService.addKomentar(kosId, new si.uni_lj.fe.seminar.folkgarderoba.model.KomentarCreateRequest(komentarText))
                .enqueue(new Callback<Void>() { // we just reload, backend returns message/url, we ignore

                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            newCommentEditText.setText(""); // clear field
                            loadKomentarji(); // reload comments
                        } else {
                            Log.e("ADD_COMMENT_ERROR", "Code: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Log.e("ADD_COMMENT_ERROR", t.getMessage());
                    }
                });
    }
    private void deleteKomentar(int komentarId) {
        apiService.deleteKomentar(kosId, komentarId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    loadKomentarji();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("DELETE_ERROR", t.getMessage());
            }
        });
    }

    private void enterEditMode(Komentar komentar, com.google.android.material.card.MaterialCardView card) {

        LinearLayout mainRow = (LinearLayout) card.getChildAt(0);
        LinearLayout topRow = (LinearLayout) mainRow.getChildAt(0);
        TextView commentTv = (TextView) mainRow.getChildAt(1);

        // Hide old edit/delete icons (keep username and spacer)
        for (int i = 0; i < topRow.getChildCount(); i++) {
            View child = topRow.getChildAt(i);
            if (child != topRow.getChildAt(0) && !(child.getLayoutParams() instanceof LinearLayout.LayoutParams &&
                    ((LinearLayout.LayoutParams) child.getLayoutParams()).weight == 1f)) {
                child.setVisibility(View.GONE);
            }
        }

        // Replace comment TextView with EditText
        EditText editText = new EditText(this);
        editText.setText(commentTv.getText());
        editText.setTextSize(16);
        editText.setPadding(0, 12, 0, 12);
        editText.setLayoutParams(commentTv.getLayoutParams());
        editText.setBackground(null); // no underline
        mainRow.removeView(commentTv);
        mainRow.addView(editText, 1);

        // Create a check icon in top-right (replace X)
        ImageView checkIcon = new ImageView(this);
        checkIcon.setImageResource(R.drawable.save); // make sure you have a check icon drawable
        int sizeDp = 18;
        float scale = getResources().getDisplayMetrics().density;
        int sizePx = (int) (sizeDp * scale + 0.5f);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(sizePx, sizePx);
        iconParams.setMargins(0, 0, 0, 0); // spacing from username if needed
        checkIcon.setLayoutParams(iconParams);

        // Add check icon to topRow
        topRow.addView(checkIcon);

        // Show keyboard
        editText.post(() -> {
            editText.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
            }
        });

        // Handle click on check icon
        checkIcon.setOnClickListener(v -> {
            String updatedText = editText.getText().toString().trim();
            if (!updatedText.isEmpty()) {
                updateKomentarWithCheck(komentar, komentar.getId(), updatedText, editText, commentTv, topRow, checkIcon, card);
            }
        });
    }

    private void updateKomentar(int komentarId, String updatedText,
                                EditText editText, Button saveBtn,
                                TextView commentTv, LinearLayout topRow) {

        KomentarUpdateRequest request =
                new KomentarUpdateRequest(updatedText, kosId);

        apiService.updateKomentar(kosId, komentarId, request)
                .enqueue(new Callback<Void>() {

                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            // Replace EditText with TextView
                            LinearLayout mainRow = (LinearLayout) editText.getParent();
                            mainRow.removeView(editText);
                            mainRow.removeView(saveBtn);

                            commentTv.setText(updatedText);
                            mainRow.addView(commentTv, 1); // add back at same position

                            // Restore edit/delete icons
                            if (topRow.getChildCount() > 2) {
                                for (int i = 2; i < topRow.getChildCount(); i++) {
                                    topRow.getChildAt(i).setVisibility(View.VISIBLE);
                                }
                            }

                        } else {
                            Log.e("UPDATE_ERROR", "Code: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Log.e("UPDATE_ERROR", t.getMessage());
                    }
                });
    }
    private void updateKomentarWithCheck(Komentar komentar, int komentarId, String updatedText,
                                         EditText editText, TextView commentTv,
                                         LinearLayout topRow, ImageView checkIcon,
                                         MaterialCardView card) {

        KomentarUpdateRequest request = new KomentarUpdateRequest(updatedText, kosId);

        apiService.updateKomentar(kosId, komentarId, request)
                .enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {

                            // Replace EditText with original TextView
                            LinearLayout mainRow = (LinearLayout) editText.getParent();
                            mainRow.removeView(editText);

                            commentTv.setText(updatedText);
                            mainRow.addView(commentTv, 1);

                            // Remove check icon
                            topRow.removeView(checkIcon);

                            // Restore edit/delete icons (for owner)
                            LinearLayout iconsRow = new LinearLayout(KosDetailActivity.this);
                            iconsRow.setOrientation(LinearLayout.HORIZONTAL);

                            int sizeDp = 18;
                            float scale = getResources().getDisplayMetrics().density;
                            int sizePx = (int) (sizeDp * scale + 0.5f);

// EDIT ICON
                            ImageView editIcon = new ImageView(KosDetailActivity.this);
                            editIcon.setImageResource(R.drawable.pen);
                            LinearLayout.LayoutParams editParams = new LinearLayout.LayoutParams(sizePx, sizePx);
                            editParams.setMargins(0, 0, 24, 0);
                            editIcon.setLayoutParams(editParams);
                            editIcon.setOnClickListener(v -> enterEditMode(komentar, card));

// DELETE ICON
                            ImageView deleteIcon = new ImageView(KosDetailActivity.this);
                            deleteIcon.setImageResource(R.drawable.delete_icon);
                            deleteIcon.setLayoutParams(new LinearLayout.LayoutParams(sizePx, sizePx));
                            deleteIcon.setOnClickListener(v -> deleteKomentar(komentarId));

                            iconsRow.addView(editIcon);
                            iconsRow.addView(deleteIcon);

                            // Add iconsRow to topRow (after spacer)
                            topRow.addView(iconsRow);

                        } else {
                            Log.e("UPDATE_ERROR", "Code: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Log.e("UPDATE_ERROR", t.getMessage());
                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        View view = getCurrentFocus();
        if (view instanceof EditText) {
            Rect outRect = new Rect();
            view.getGlobalVisibleRect(outRect);
            if (!outRect.contains((int) ev.getRawX(), (int) ev.getRawY())) {
                view.clearFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }
}
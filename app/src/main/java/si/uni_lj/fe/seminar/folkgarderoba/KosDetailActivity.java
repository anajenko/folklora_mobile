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
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.android.flexbox.FlexboxLayout;

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

    private TextView labelsTitle;

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
        labelsTitle = findViewById(R.id.labelsTitle);

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
                        labelsTitle.setVisibility(View.GONE);
                        labelsContainer.setVisibility(View.GONE);
                        return;
                    }

                    labelsTitle.setVisibility(View.VISIBLE);
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
                    // No labels
                    labelsTitle.setVisibility(View.GONE);
                    labelsContainer.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<List<Labela>> call, Throwable t) {
                Log.e("LABEL_ERROR", t.getMessage());
                labelsTitle.setVisibility(View.GONE);
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

                    for (Komentar komentar : response.body()) {

                        LinearLayout commentLayout = new LinearLayout(KosDetailActivity.this);
                        commentLayout.setOrientation(LinearLayout.HORIZONTAL);
                        commentLayout.setPadding(0, 8, 0, 8);
                        commentLayout.setLayoutParams(new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        ));

                        TextView commentTv = new TextView(KosDetailActivity.this);
                        commentTv.setText(komentar.getUporabnisko_ime() + ": " + komentar.getBesedilo());
                        commentTv.setLayoutParams(new LinearLayout.LayoutParams(
                                0,
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                1f
                        ));
                        commentLayout.addView(commentTv);

                        if (komentar.getUporabnisko_ime().equals(currentUsername)) {

                            // DELETE
                            Button deleteBtn = new Button(KosDetailActivity.this);
                            deleteBtn.setText("X");
                            deleteBtn.setOnClickListener(v ->
                                    deleteKomentar(komentar.getId()));
                            commentLayout.addView(deleteBtn);

                            // EDIT
                            Button editBtn = new Button(KosDetailActivity.this);
                            editBtn.setText("Edit");
                            editBtn.setOnClickListener(v ->
                                    enterEditMode(komentar));
                            commentLayout.addView(editBtn);
                        }

                        commentsContainer.addView(commentLayout);
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

    private void enterEditMode(Komentar komentar) {

        commentsContainer.removeAllViews();

        LinearLayout editLayout = new LinearLayout(this);
        editLayout.setOrientation(LinearLayout.VERTICAL);

        EditText editText = new EditText(this);
        editText.setText(komentar.getBesedilo());
        editLayout.addView(editText);

        Button saveBtn = new Button(this);
        saveBtn.setText("Popravi komentar");
        editLayout.addView(saveBtn);

        saveBtn.setOnClickListener(v -> {
            String updatedText = editText.getText().toString().trim();
            if (!updatedText.isEmpty()) {
                updateKomentar(komentar.getId(), updatedText);
            }
        });

        commentsContainer.addView(editLayout);
    }

    private void updateKomentar(int komentarId, String updatedText) {

        KomentarUpdateRequest request =
                new KomentarUpdateRequest(updatedText, kosId);

        apiService.updateKomentar(kosId, komentarId, request)
                .enqueue(new Callback<Void>() {

                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            loadKomentarji();
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
package si.uni_lj.fe.seminar.folkgarderoba;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;

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
    private LinearLayout labelsContainer, commentsContainer;

    private ApiService apiService;
    private int kosId;
    private String currentUsername;

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
        }

        titleText = findViewById(R.id.detailTitle);
        imageView = findViewById(R.id.detailImage);
        poskodovanoText = findViewById(R.id.poskodovanoText);
        labelsContainer = findViewById(R.id.labelsContainer);
        commentsContainer = findViewById(R.id.commentsContainer);
        Button addCommentButton = findViewById(R.id.addCommentButton);

        apiService = RetrofitClient
                .getRetrofitInstance(this)
                .create(ApiService.class);

        // 📦 Dobimo podatke iz MainActivity
        kosId = getIntent().getIntExtra("kosId", -1);
        String kosIme = getIntent().getStringExtra("kosIme");
        boolean poskodovano = getIntent().getBooleanExtra("poskodovano", false);

        titleText.setText(kosIme);

        if (poskodovano) {
            poskodovanoText.setVisibility(View.VISIBLE);
        }

        // 🖼 Slika
        String imageUrl = RetrofitClient.BASE_URL + "api/kosi/" + kosId;
        Glide.with(this).load(imageUrl).into(imageView);

        loadLabele();
        loadKomentarji();

        addCommentButton.setOnClickListener(v -> {
            // kasneje AddCommentActivity
        });
    }

    private void loadLabele() {
        apiService.getLabeleZaKos(kosId).enqueue(new Callback<List<Labela>>() {
            @Override
            public void onResponse(Call<List<Labela>> call, Response<List<Labela>> response) {
                if (response.isSuccessful() && response.body() != null) {

                    labelsContainer.removeAllViews();

                    for (Labela label : response.body()) {
                        TextView tv = new TextView(KosDetailActivity.this);
                        tv.setText(label.getNaziv());
                        labelsContainer.addView(tv);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Labela>> call, Throwable t) { }
        });
    }

    private void loadKomentarji() {
        SharedPreferences prefs = getSharedPreferences("APP_PREFS", MODE_PRIVATE);
        String currentUsername = prefs.getString("username", null);

        apiService.getKomentarjiZaKos(kosId).enqueue(new Callback<List<Komentar>>() {
            @Override
            public void onResponse(Call<List<Komentar>> call, Response<List<Komentar>> response) {
                if (response.isSuccessful() && response.body() != null) {

                    commentsContainer.removeAllViews();

                    for (Komentar komentar : response.body()) {

                        // Horizontal layout for single comment
                        LinearLayout commentLayout = new LinearLayout(KosDetailActivity.this);
                        commentLayout.setOrientation(LinearLayout.HORIZONTAL);
                        commentLayout.setPadding(0, 8, 0, 8);
                        commentLayout.setLayoutParams(new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        ));

                        // Comment text
                        TextView commentTv = new TextView(KosDetailActivity.this);
                        commentTv.setText(komentar.getUporabnisko_ime() + ": " + komentar.getBesedilo());
                        commentTv.setLayoutParams(new LinearLayout.LayoutParams(
                                0,
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                1f  // take remaining space
                        ));
                        commentLayout.addView(commentTv);

                        // Only for logged-in user
                        if (komentar.getUporabnisko_ime().equals(currentUsername)) {

                            // Delete button
                            Button deleteBtn = new Button(KosDetailActivity.this);
                            deleteBtn.setText("X");
                            deleteBtn.setOnClickListener(v -> deleteKomentar(komentar.getId(), commentLayout));
                            commentLayout.addView(deleteBtn);

                            // Edit button
                            Button editBtn = new Button(KosDetailActivity.this);
                            editBtn.setText("Edit");
                            editBtn.setOnClickListener(v -> enterEditMode(commentLayout, commentTv, komentar));
                            commentLayout.addView(editBtn);
                        }

                        // Add the whole comment row to the vertical container
                        commentsContainer.addView(commentLayout);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Komentar>> call, Throwable t) { }
        });
    }

    private void deleteKomentar(int komentarId, LinearLayout commentLayout) {
        apiService.deleteKomentar(komentarId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    // remove comment from UI
                    commentsContainer.removeView(commentLayout);
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) { }
        });
    }

    private void enterEditMode(LinearLayout commentLayout, TextView commentTv, Komentar komentar) {

        commentLayout.removeView(commentTv);

        // Input field
        EditText editText = new EditText(this);
        editText.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        editText.setText(komentar.getBesedilo());
        commentLayout.addView(editText, 0);

        // Remove old Edit button
        Button oldEditBtn = (Button) commentLayout.getChildAt(2); // assuming delete button at index 1, edit at index 2
        commentLayout.removeView(oldEditBtn);

        // Add "Popravi komentar" button
        Button saveBtn = new Button(this);
        saveBtn.setText("Popravi komentar");
        saveBtn.setOnClickListener(v -> {
            String updatedText = editText.getText().toString().trim();
            if (!updatedText.isEmpty()) {
                updateKomentar(komentar, updatedText, commentLayout, editText, saveBtn, commentTv);
            }
        });
        commentLayout.addView(saveBtn);
    }

    private void updateKomentar(Komentar komentar, String updatedText, LinearLayout commentLayout,
                                EditText editText, Button saveBtn, TextView commentTv) {

        KomentarUpdateRequest request = new KomentarUpdateRequest(updatedText, kosId);

        apiService.updateKomentar(komentar.getId(), request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    // Update TextView
                    commentTv.setText(komentar.getUporabnisko_ime() + ": " + updatedText);

                    // Remove EditText and Save button
                    commentLayout.removeView(editText);
                    commentLayout.removeView(saveBtn);

                    // Add back TextView
                    commentLayout.addView(commentTv, 0);

                    // Restore Edit button
                    Button editBtn = new Button(KosDetailActivity.this);
                    editBtn.setText("Edit");
                    editBtn.setOnClickListener(v -> enterEditMode(commentLayout, commentTv, komentar));
                    commentLayout.addView(editBtn);

                } else {
                    // Handle failure
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                // Handle failure
            }
        });
    }
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
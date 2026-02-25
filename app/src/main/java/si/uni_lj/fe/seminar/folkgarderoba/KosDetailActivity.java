package si.uni_lj.fe.seminar.folkgarderoba;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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
import si.uni_lj.fe.seminar.folkgarderoba.model.Labela;

public class KosDetailActivity extends AppCompatActivity {

    private TextView titleText, poskodovanoText;
    private ImageView imageView;
    private LinearLayout labelsContainer, commentsContainer;

    private ApiService apiService;
    private int kosId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kos_detail);

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
        apiService.getKomentarjiZaKos(kosId).enqueue(new Callback<List<Komentar>>() {
            @Override
            public void onResponse(Call<List<Komentar>> call, Response<List<Komentar>> response) {
                if (response.isSuccessful() && response.body() != null) {

                    commentsContainer.removeAllViews();

                    for (Komentar komentar : response.body()) {
                        TextView tv = new TextView(KosDetailActivity.this);
                        tv.setText("- " + komentar.getBesedilo());
                        commentsContainer.addView(tv);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Komentar>> call, Throwable t) { }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
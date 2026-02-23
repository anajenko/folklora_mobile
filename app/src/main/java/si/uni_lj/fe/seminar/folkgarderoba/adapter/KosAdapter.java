package si.uni_lj.fe.seminar.folkgarderoba.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import si.uni_lj.fe.seminar.folkgarderoba.R;
import si.uni_lj.fe.seminar.folkgarderoba.model.Kos;
import si.uni_lj.fe.seminar.folkgarderoba.RetrofitClient;

public class KosAdapter extends RecyclerView.Adapter<KosAdapter.KosViewHolder> {

    private List<Kos> kosList;
    private OnItemClickListener listener;

    // Click listener interface
    public interface OnItemClickListener {
        void onItemClick(Kos kos);
    }

    public KosAdapter(List<Kos> kosList, OnItemClickListener listener) {
        this.kosList = kosList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public KosViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_kos, parent, false);
        return new KosViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull KosViewHolder holder, int position) {

        Kos kos = kosList.get(position);

        // Nastavi naslov
        holder.textViewNaziv.setText(kos.getIme());

        // URL slike
        String imageUrl = RetrofitClient.BASE_URL + "api/kosi/" + kos.getId();

        Glide.with(holder.itemView.getContext())
                .load(imageUrl)
                .into(holder.imageViewKos);

        // Prikaz ikone poškodbe
        // Če imaš int (0 ali 1):
        if (kos.isPoskodovano()) {
            holder.imageViewPoskodba.setVisibility(View.VISIBLE);
        } else {
            holder.imageViewPoskodba.setVisibility(View.GONE);
        }

        // Click event
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(kos);
            }
        });
    }

    @Override
    public int getItemCount() {
        return kosList.size();
    }

    // ViewHolder
    public static class KosViewHolder extends RecyclerView.ViewHolder {

        TextView textViewNaziv;
        ImageView imageViewKos;
        ImageView imageViewPoskodba;

        public KosViewHolder(@NonNull View itemView) {
            super(itemView);

            textViewNaziv = itemView.findViewById(R.id.textViewNaziv);
            imageViewKos = itemView.findViewById(R.id.imageViewKos);
            imageViewPoskodba = itemView.findViewById(R.id.imageViewPoskodba);
        }
    }
}

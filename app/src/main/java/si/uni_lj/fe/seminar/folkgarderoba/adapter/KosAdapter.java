package si.uni_lj.fe.seminar.folkgarderoba.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.card.MaterialCardView;

import java.util.List;

import si.uni_lj.fe.seminar.folkgarderoba.R;
import si.uni_lj.fe.seminar.folkgarderoba.model.Kos;
import si.uni_lj.fe.seminar.folkgarderoba.RetrofitClient;

public class KosAdapter extends RecyclerView.Adapter<KosAdapter.KosViewHolder> {

    private List<Kos> kosList;
    private OnItemClickListener listener;

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

        holder.textViewNaziv.setText(kos.getIme());// Nastavi naslov

        String imageUrl = RetrofitClient.BASE_URL + "api/kosi/" + kos.getId(); // URL slike

        Glide.with(holder.itemView.getContext())
                .load(imageUrl)
                .into(holder.imageViewKos);

        MaterialCardView card = (MaterialCardView) holder.itemView;

        // Prikaz ikone poškodbe
        if (kos.getPoskodovano() == 1) {
            holder.imageViewPoskodba.setVisibility(View.VISIBLE);
            card.setCardBackgroundColor(holder.itemView.getContext().getResources().getColor(R.color.poskodovano_bg));
            card.setStrokeWidth(4);
            card.setStrokeColor(holder.itemView.getContext().getResources().getColor(R.color.poskodovano_border));
        } else {
            holder.imageViewPoskodba.setVisibility(View.GONE);
            card.setCardBackgroundColor(holder.itemView.getContext().getResources().getColor(android.R.color.white));
            card.setStrokeWidth(0);
        }
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

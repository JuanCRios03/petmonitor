package co.edu.unipiloto.petmonitor.CasosdeUso;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import co.edu.unipiloto.petmonitor.R;
public class VeterinarioAdapter extends RecyclerView.Adapter<VeterinarioAdapter.ViewHolder> {
    private List<Veterinario> listaVeterinarios;
    private OnVeterinarioClickListener listener;

    public VeterinarioAdapter(List<Veterinario> listaVeterinarios, OnVeterinarioClickListener listener) {
        this.listaVeterinarios = listaVeterinarios;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_veterinario, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Veterinario veterinario = listaVeterinarios.get(position);
        holder.tvNombre.setText(veterinario.getNombre());
        holder.tvDireccion.setText(veterinario.getDireccion());
        holder.tvRating.setText("⭐ " + veterinario.getRating());

        // Click en el ítem
        holder.itemView.setOnClickListener(v -> listener.onVeterinarioClick(veterinario));
    }

    @Override
    public int getItemCount() {
        return listaVeterinarios.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvDireccion, tvRating;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvDireccion = itemView.findViewById(R.id.tvDireccion);
            tvRating = itemView.findViewById(R.id.tvRating);
        }
    }

    // Interfaz para manejar los clics
    public interface OnVeterinarioClickListener {
        void onVeterinarioClick(Veterinario veterinario);
    }
}

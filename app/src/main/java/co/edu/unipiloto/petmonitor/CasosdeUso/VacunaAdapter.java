package co.edu.unipiloto.petmonitor.CasosdeUso;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import co.edu.unipiloto.petmonitor.R;

public class VacunaAdapter extends RecyclerView.Adapter<VacunaAdapter.VacunaViewHolder> {

    private Context context;
    private List<Vacuna> vacunaList;

    public VacunaAdapter(Context context, List<Vacuna> vacunaList) {
        this.context = context;
        this.vacunaList = vacunaList;
    }

    @NonNull
    @Override
    public VacunaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_vacuna, parent, false);
        return new VacunaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VacunaViewHolder holder, int position) {
        Vacuna vacuna = vacunaList.get(position);

        holder.tvNombreDueno.setText("Correo del Dueño: " + vacuna.getNombre());
        holder.tvNombreMascota.setText("Mascota: " + vacuna.getNombreMascota());
        holder.tvNombreVacuna.setText("Nombre de la Vacuna: " + vacuna.getTipo());
        holder.tvFechaVacuna.setText("Fecha de aplicacion: " + vacuna.getFecha());
        holder.tvNombreVeterinario.setText("Veterinario: " + vacuna.getVeterinario());
    }

    @Override
    public int getItemCount() {
        return vacunaList.size();
    }

    public static class VacunaViewHolder extends RecyclerView.ViewHolder {

        TextView tvNombreDueno, tvNombreMascota, tvNombreVacuna, tvFechaVacuna, tvNombreVeterinario;

        public VacunaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombreDueno = itemView.findViewById(R.id.tvNombreDueno);
            tvNombreMascota = itemView.findViewById(R.id.tvNombreMascota);
            tvNombreVacuna = itemView.findViewById(R.id.tvNombreVacuna);
            tvFechaVacuna = itemView.findViewById(R.id.tvFechaVacuna);
            tvNombreVeterinario = itemView.findViewById(R.id.tvNombreVeterinario);
        }
    }
}


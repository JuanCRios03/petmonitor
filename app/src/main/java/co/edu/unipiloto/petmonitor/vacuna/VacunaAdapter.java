package co.edu.unipiloto.petmonitor.vacuna;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import co.edu.unipiloto.petmonitor.R;

public class VacunaAdapter extends RecyclerView.Adapter<VacunaAdapter.VacunaViewHolder> {

    private final Context context;
    private final List<Vacuna> vacunaList;
    private final SimpleDateFormat dateFormat; // Para formatear fecha si es Date

    public VacunaAdapter(Context context, List<Vacuna> vacunaList) {
        this.context = context;
        this.vacunaList = vacunaList;
        // Define el formato que quieres mostrar, si la fecha es Date
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
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

        // Asignar datos a las vistas del item
        holder.tvFecha.setText(vacuna.getFechaVacunacion() != null ? vacuna.getFechaVacunacion() : "N/A");
        // Si guardaras la fecha como Date/Timestamp, usarías:
        // holder.tvFecha.setText(vacuna.getFechaVacunacion() != null ? dateFormat.format(vacuna.getFechaVacunacion()) : "N/A");

        holder.tvTipoVacuna.setText(vacuna.getTipoVacuna() != null ? vacuna.getTipoVacuna() : "N/A");
        holder.tvDosis.setText(vacuna.getDosis() != null ? vacuna.getDosis() : "-");
        holder.tvLote.setText(vacuna.getLote() != null ? vacuna.getLote() : "-");
        holder.tvVeterinario.setText(vacuna.getVeterinario() != null ? vacuna.getVeterinario() : "-");

        // Mostrar/ocultar observaciones
        if (vacuna.getObservaciones() != null && !vacuna.getObservaciones().trim().isEmpty()) {
            holder.tvObservaciones.setText(vacuna.getObservaciones());
            holder.tvObservaciones.setVisibility(View.VISIBLE);
            holder.labelObservaciones.setVisibility(View.VISIBLE); // Mostrar etiqueta también
        } else {
            holder.tvObservaciones.setVisibility(View.GONE);
            holder.labelObservaciones.setVisibility(View.GONE); // Ocultar etiqueta
        }
    }

    @Override
    public int getItemCount() {
        // Devuelve 0 si la lista es nula para evitar errores
        return vacunaList != null ? vacunaList.size() : 0;
    }

    // ViewHolder interno que referencia las vistas del item_vacuna.xml
    static class VacunaViewHolder extends RecyclerView.ViewHolder {
        TextView tvFecha, tvTipoVacuna, tvDosis, tvLote, tvVeterinario, labelObservaciones, tvObservaciones;

        public VacunaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFecha = itemView.findViewById(R.id.item_fecha_vacuna);
            tvTipoVacuna = itemView.findViewById(R.id.item_tipo_vacuna);
            tvDosis = itemView.findViewById(R.id.item_dosis);
            tvLote = itemView.findViewById(R.id.item_lote);
            tvVeterinario = itemView.findViewById(R.id.item_veterinario);
            labelObservaciones = itemView.findViewById(R.id.label_observaciones); // Referencia a la etiqueta
            tvObservaciones = itemView.findViewById(R.id.item_observaciones);
        }
    }
}

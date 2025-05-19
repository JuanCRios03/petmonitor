package co.edu.unipiloto.petmonitor.CasosdeUso;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import co.edu.unipiloto.petmonitor.R;

public class TratamientoAdapter extends RecyclerView.Adapter<TratamientoAdapter.TratamientoViewHolder> {

    private List<Tratamiento> tratamientos;
    private Activity activity; // Cambié Context por Activity para mayor seguridad
    private String userId;
    private String mascotaId;

    // Cambié Context por Activity aquí también
    public TratamientoAdapter(List<Tratamiento> tratamientos, Activity activity, String userId, String mascotaId) {
        this.tratamientos = tratamientos;
        this.activity = activity;
        this.userId = userId;
        this.mascotaId = mascotaId;
    }

    @NonNull
    @Override
    public TratamientoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tratamiento, parent, false);
        return new TratamientoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TratamientoViewHolder holder, int position) {
        Tratamiento tratamiento = tratamientos.get(position);

        holder.tvMedicamento.setText("Medicamento: " + tratamiento.getMedicamento());
        holder.tvDescripcion.setText("Descripción: " + tratamiento.getDescripcion());
        holder.tvFrecuencia.setText("Frecuencia: " + tratamiento.getFrecuencia());
        holder.tvHora.setText("Hora de inicio: " + tratamiento.getHora());
        holder.tvFechaInicio.setText("Fecha de Inicio: " + tratamiento.getFechaInicio());
        holder.tvFechaFin.setText("Fecha de finalización: " + tratamiento.getFechaFin());

        if (tratamiento.getFechaCumplimiento() != null && !tratamiento.getFechaCumplimiento().isEmpty()) {
            holder.tvFechaCumplimiento.setVisibility(View.VISIBLE);
            holder.tvFechaCumplimiento.setText("Cumplimiento: " + tratamiento.getFechaCumplimiento());
        } else {
            holder.tvFechaCumplimiento.setVisibility(View.GONE);
        }

        /*holder.btnVerHistorial.setOnClickListener(v -> {
            Log.d("Adapter", "Lanzando HistorialCumplimientosActivity con tratamientoId: " + tratamiento.getId());
            Intent intent = new Intent(activity, HistorialCumplimientosActivity.class);
            intent.putExtra("userId", userId);
            intent.putExtra("mascotaId", mascotaId);
            intent.putExtra("tratamientoId", tratamiento.getId());
            activity.startActivity(intent);
        });*/
    }

    @Override
    public int getItemCount() {
        return tratamientos.size();
    }

    static class TratamientoViewHolder extends RecyclerView.ViewHolder {
        TextView tvMedicamento, tvDescripcion, tvFrecuencia, tvHora, tvFechaInicio, tvFechaFin, tvFechaCumplimiento;
        Button btnVerHistorial;

        public TratamientoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMedicamento = itemView.findViewById(R.id.tvMedicamento);
            tvDescripcion = itemView.findViewById(R.id.tvDescripcion);
            tvFrecuencia = itemView.findViewById(R.id.tvFrecuencia);
            tvHora = itemView.findViewById(R.id.tvHora);
            tvFechaInicio = itemView.findViewById(R.id.tvFechaInicio);
            tvFechaFin = itemView.findViewById(R.id.tvFechaFin);
            tvFechaCumplimiento = itemView.findViewById(R.id.tvFechaCumplimiento);
            //btnVerHistorial = itemView.findViewById(R.id.btnVerHistorial);
        }
    }
}




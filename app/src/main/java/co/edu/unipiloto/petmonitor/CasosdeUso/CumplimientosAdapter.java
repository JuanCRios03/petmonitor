package co.edu.unipiloto.petmonitor.CasosdeUso;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import co.edu.unipiloto.petmonitor.R;
public class CumplimientosAdapter extends RecyclerView.Adapter<CumplimientosAdapter.ViewHolder> {
    private List<Cumplimiento> listaCumplimientos;

    public CumplimientosAdapter(List<Cumplimiento> listaCumplimientos) {
        this.listaCumplimientos = listaCumplimientos;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView fechaTextView;
        TextView medicamentoTextView;
        TextView descripcionTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            fechaTextView = itemView.findViewById(R.id.tvFechaCumplimiento);              // Asegúrate que este ID existe
            medicamentoTextView = itemView.findViewById(R.id.text_medicamento); // Asegúrate que este ID existe
            descripcionTextView = itemView.findViewById(R.id.tvDescripcionCumplimiento); // Asegúrate que este ID existe
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cumplimiento, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Cumplimiento cumplimiento = listaCumplimientos.get(position);

        // Descripción fija
        if (holder.descripcionTextView != null) {
            holder.descripcionTextView.setText("Cumplimiento registrado");
        }

        // Medicamento
        if (holder.medicamentoTextView != null) {
            if (cumplimiento.getNombreMedicamento() != null && !cumplimiento.getNombreMedicamento().isEmpty()) {
                holder.medicamentoTextView.setText(cumplimiento.getNombreMedicamento());
            } else {
                holder.medicamentoTextView.setText("Sin medicamento");
            }
        }

        // Fecha con formato y zona horaria Bogotá
        if (holder.fechaTextView != null) {
            Date fecha = cumplimiento.getFechaRegistro();
            if (fecha != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                sdf.setTimeZone(TimeZone.getTimeZone("America/Bogota"));
                holder.fechaTextView.setText(sdf.format(fecha));
            } else {
                holder.fechaTextView.setText("Sin fecha");
            }
        }
    }

    @Override
    public int getItemCount() {
        return listaCumplimientos.size();
    }
}





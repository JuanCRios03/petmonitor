package co.edu.unipiloto.petmonitor.CasosdeUso;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.text.SimpleDateFormat;
import java.util.*;

import co.edu.unipiloto.petmonitor.R;

public class historialUbicacionActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String mascotaId;
    private List<Map<String, Object>> ubicaciones = new ArrayList<>();
    private UbicacionAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial_ubicacion);

        recyclerView = findViewById(R.id.recyclerUbicaciones);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UbicacionAdapter(ubicaciones);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        mascotaId = getIntent().getStringExtra("mascotaId");
        if (mascotaId == null) {
            Log.e("HISTORIAL", "No se recibió mascotaId");
            finish();
            return;
        }

        cargarHistorial();
    }

    private void cargarHistorial() {
        String email = auth.getCurrentUser() != null ? auth.getCurrentUser().getEmail() : null;
        if (email == null) return;

        db.collection("usuarios")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.isEmpty()) {
                        String userId = snapshot.getDocuments().get(0).getId();

                        db.collection("usuarios")
                                .document(userId)
                                .collection("mascotas")
                                .document(mascotaId)
                                .collection("ubicaciones")
                                .orderBy("timestamp", Query.Direction.DESCENDING)
                                .get()
                                .addOnSuccessListener(locations -> {
                                    ubicaciones.clear();
                                    for (DocumentSnapshot doc : locations) {
                                        ubicaciones.add(doc.getData());
                                    }
                                    adapter.notifyDataSetChanged();
                                });
                    } else {
                        Log.e("HISTORIAL", "No se encontró usuario con ese email");
                    }
                });
    }

    // Adaptador interno
    class UbicacionAdapter extends RecyclerView.Adapter<UbicacionAdapter.ViewHolder> {

        private final List<Map<String, Object>> lista;

        public UbicacionAdapter(List<Map<String, Object>> lista) {
            this.lista = lista;
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView textDireccion, textHora;

            public ViewHolder(View view) {
                super(view);
                textDireccion = view.findViewById(R.id.textDireccion);
                textHora = view.findViewById(R.id.textHora);
            }
        }

        @NonNull
        @Override
        public UbicacionAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_ubicacion, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Map<String, Object> data = lista.get(position);
            holder.textDireccion.setText((String) data.get("direccion"));

            Long timestamp = (Long) data.get("timestamp");
            if (timestamp != null) {
                Date date = new Date(timestamp);
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                holder.textHora.setText(sdf.format(date));
            }
        }

        @Override
        public int getItemCount() {
            return lista.size();
        }
    }
}

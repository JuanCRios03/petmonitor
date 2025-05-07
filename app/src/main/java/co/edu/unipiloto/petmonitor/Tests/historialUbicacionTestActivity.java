package co.edu.unipiloto.petmonitor.Tests;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import co.edu.unipiloto.petmonitor.R;

public class historialUbicacionTestActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private UbicacionAdapter adapter;
    private List<Ubicacion> listaUbicaciones = new ArrayList<>();
    private FirebaseFirestore db;

    private String usuarioId = "8vegW50jyrUF8OWsnt4jvCpPtSo2"; // ID del usuario juancjunior2010@gmail.com
    private String mascotaId = "BhzARsMiZOuxLPdxajPV"; // ID de la mascota

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial_test);

        recyclerView = findViewById(R.id.recyclerUbicacionesTest);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UbicacionAdapter(listaUbicaciones);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        iniciarCargaPeriodica();
    }

    private void iniciarCargaPeriodica() {
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                cargarHistorial();
                handler.postDelayed(this, 60000); // cada 60 segundos
            }
        };
        handler.post(runnable);
    }

    private void cargarHistorial() {
        db.collection("usuarios")
                .document(usuarioId)
                .collection("mascotas")
                .document(mascotaId)
                .collection("ubicaciones")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    listaUbicaciones.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String direccion = doc.getString("direccion");
                        double latitud = doc.getDouble("latitud");
                        double longitud = doc.getDouble("longitud");
                        long timestamp = doc.getLong("timestamp");

                        listaUbicaciones.add(new Ubicacion(direccion, latitud, longitud, timestamp));
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    // CLASE INTERNA: Ubicacion
    public class Ubicacion {
        String direccion;
        double latitud;
        double longitud;
        long timestamp;

        public Ubicacion(String direccion, double latitud, double longitud, long timestamp) {
            this.direccion = direccion;
            this.latitud = latitud;
            this.longitud = longitud;
            this.timestamp = timestamp;
        }
    }

    // CLASE INTERNA: Adapter
    public class UbicacionAdapter extends RecyclerView.Adapter<UbicacionAdapter.ViewHolder> {
        private List<Ubicacion> ubicaciones;

        public UbicacionAdapter(List<Ubicacion> ubicaciones) {
            this.ubicaciones = ubicaciones;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_2, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Ubicacion u = ubicaciones.get(position);
            holder.direccion.setText("📍 " + u.direccion);
            holder.detalles.setText("Lat: " + u.latitud + ", Lon: " + u.longitud + " | " +
                    new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date(u.timestamp)));
        }

        @Override
        public int getItemCount() {
            return ubicaciones.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView direccion, detalles;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                direccion = itemView.findViewById(android.R.id.text1);
                detalles = itemView.findViewById(android.R.id.text2);
            }
        }
    }
}

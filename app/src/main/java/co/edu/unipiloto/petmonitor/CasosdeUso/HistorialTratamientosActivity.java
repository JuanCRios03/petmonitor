package co.edu.unipiloto.petmonitor.CasosdeUso;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import co.edu.unipiloto.petmonitor.R;

public class HistorialTratamientosActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TratamientoAdapter adapter;
    private List<Tratamiento> tratamientos = new ArrayList<>();
    private TextView textViewMascotaNombre;
    private FirebaseFirestore db;
    private String userId;
    private String mascotaId;
    private Button btnVerHistorial; // <- NUEVO

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial_tratamientos);

        textViewMascotaNombre = findViewById(R.id.textViewMascotaNombre);
        recyclerView = findViewById(R.id.recyclerViewTratamientos);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Obtener IDs
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mascotaId = getIntent().getStringExtra("mascotaId");

        // Adapter
        adapter = new TratamientoAdapter(tratamientos, this, userId, mascotaId);
        recyclerView.setAdapter(adapter);

        // Firebase
        db = FirebaseFirestore.getInstance();

        // Botón "Ver Historial"
        btnVerHistorial = findViewById(R.id.btnVerHistorial);
        btnVerHistorial.setOnClickListener(v -> {
            Intent intent = new Intent(this, HistorialCumplimientosActivity.class);
            intent.putExtra("userId", userId);
            intent.putExtra("mascotaId", mascotaId);
            startActivity(intent);
        });

        // Cargar tratamientos
        cargarTratamientos();
    }

    private void cargarTratamientos() {
        db.collection("usuarios").document(userId)
                .collection("mascotas").document(mascotaId)
                .collection("tratamientos")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    tratamientos.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Tratamiento t = doc.toObject(Tratamiento.class);
                        t.setId(doc.getId()); // importante para pasar id después

                        if (doc.contains("fechaCumplimiento")) {
                            t.setFechaCumplimiento(doc.getString("fechaCumplimiento"));
                        }

                        tratamientos.add(t);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error cargando historial de tratamientos", Toast.LENGTH_SHORT).show();
                    Log.e("Firestore", "Error cargando historial", e);
                });
    }
}



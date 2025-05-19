package co.edu.unipiloto.petmonitor.CasosdeUso;

import static android.content.Intent.getIntent;
import android.os.Bundle;
import android.util.Log;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

import co.edu.unipiloto.petmonitor.R;

// HistorialCumplimientosActivity.java
public class HistorialCumplimientosActivity extends AppCompatActivity {

    private RecyclerView recyclerViewCumplimientos;
    private CumplimientosAdapter adapterCumplimientos;
    private List<Cumplimiento> cumplimientoList = new ArrayList<>();
    private List<Cumplimiento> cumplimientoListFull = new ArrayList<>();  // Para guardar todos los datos

    private FirebaseFirestore db;
    private String userId;
    private String mascotaId;

    private List<Tratamiento> tratamientos = new ArrayList<>();

    private SearchView searchViewMedicamento;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial_cumplimientos);

        db = FirebaseFirestore.getInstance();

        userId = getIntent().getStringExtra("userId");
        mascotaId = getIntent().getStringExtra("mascotaId");

        searchViewMedicamento = findViewById(R.id.searchViewMedicamento);
        recyclerViewCumplimientos = findViewById(R.id.recyclerViewCumplimientos);
        recyclerViewCumplimientos.setLayoutManager(new LinearLayoutManager(this));
        adapterCumplimientos = new CumplimientosAdapter(cumplimientoList);
        recyclerViewCumplimientos.setAdapter(adapterCumplimientos);

        cargarTodosLosCumplimientos();

        configurarFiltroBusqueda();
    }

    private void configurarFiltroBusqueda() {
        searchViewMedicamento.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filtrar(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filtrar(newText);
                return false;
            }
        });
    }

    private void filtrar(String texto) {
        cumplimientoList.clear();
        if (texto == null || texto.trim().isEmpty()) {
            cumplimientoList.addAll(cumplimientoListFull);
        } else {
            String textoLower = texto.toLowerCase();
            for (Cumplimiento c : cumplimientoListFull) {
                if (c.getNombreMedicamento() != null && c.getNombreMedicamento().toLowerCase().contains(textoLower)) {
                    cumplimientoList.add(c);
                }
            }
        }
        adapterCumplimientos.notifyDataSetChanged();
    }

    private void cargarTodosLosCumplimientos() {
        if (userId == null || mascotaId == null) {
            Toast.makeText(this, "No se recibieron datos del usuario o mascota", Toast.LENGTH_SHORT).show();
            return;
        }

        cumplimientoList.clear();
        cumplimientoListFull.clear();
        adapterCumplimientos.notifyDataSetChanged();

        db.collection("usuarios").document(userId)
                .collection("mascotas").document(mascotaId)
                .collection("tratamientos")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    tratamientos.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Tratamiento t = doc.toObject(Tratamiento.class);
                        t.setId(doc.getId());
                        tratamientos.add(t);
                    }
                    if (!tratamientos.isEmpty()) {
                        for (Tratamiento t : tratamientos) {
                            cargarCumplimientosDeTratamiento(t.getId(), t.getMedicamento());
                        }
                    } else {
                        Toast.makeText(this, "No hay tratamientos para esta mascota", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error cargando tratamientos", Toast.LENGTH_SHORT).show();
                });
    }

    private void cargarCumplimientosDeTratamiento(String tratamientoId, String nombreMedicamento) {
        db.collection("usuarios").document(userId)
                .collection("mascotas").document(mascotaId)
                .collection("tratamientos").document(tratamientoId)
                .collection("cumplimientos")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Cumplimiento c = doc.toObject(Cumplimiento.class);
                        c.setNombreMedicamento(nombreMedicamento);
                        cumplimientoList.add(c);
                        cumplimientoListFull.add(c);
                    }
                    adapterCumplimientos.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar historial de cumplimientos", Toast.LENGTH_LONG).show();
                });
    }
}













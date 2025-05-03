package co.edu.unipiloto.petmonitor.CasosdeUso;


import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import co.edu.unipiloto.petmonitor.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;


public class HistorialVacunasActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private VacunaAdapter adapter;
    private List<Vacuna> vacunasList = new ArrayList<>();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String userId;
    private String mascotaId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial_vacunas);

        // Obtener los valores de userId y mascotaId
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mascotaId = getIntent().getStringExtra("mascotaId");

        recyclerView = findViewById(R.id.recyclerViewVacunas);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Configurar el adaptador
        adapter = new VacunaAdapter(this, vacunasList);
        recyclerView.setAdapter(adapter);

        // Cargar las vacunas
        cargarHistorialVacunas();
    }

    private void cargarHistorialVacunas() {
        db.collection("usuarios")
                .document(userId)
                .collection("mascotas")
                .document(mascotaId)
                .collection("vacunas")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            Vacuna vacuna = document.toObject(Vacuna.class);
                            vacunasList.add(vacuna);
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "No se encontraron vacunas", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("HistorialVacunas", "Error al cargar el historial de vacunas", e);
                    Toast.makeText(this, "Error al cargar las vacunas", Toast.LENGTH_SHORT).show();
                });
    }
}

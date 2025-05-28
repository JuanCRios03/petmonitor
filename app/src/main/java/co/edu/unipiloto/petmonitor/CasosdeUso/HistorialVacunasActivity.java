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

    // Variables para cuando un veterinario está viendo las vacunas de un cliente
    private boolean esVeterinario = false;
    private String clienteId;
    private String emailCliente;
    private String nombreCliente;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial_vacunas);

        // Obtener los valores del Intent
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        mascotaId = getIntent().getStringExtra("mascotaId");

        // Verificar si viene desde un veterinario
        esVeterinario = getIntent().getBooleanExtra("esVeterinario", false);
        clienteId = getIntent().getStringExtra("clienteId");
        emailCliente = getIntent().getStringExtra("clienteEmail");
        nombreCliente = getIntent().getStringExtra("nombreCliente");

        recyclerView = findViewById(R.id.recyclerViewVacunas);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Configurar el adaptador
        adapter = new VacunaAdapter(this, vacunasList);
        recyclerView.setAdapter(adapter);

        // Cargar las vacunas
        cargarHistorialVacunas();
    }

    private void cargarHistorialVacunas() {
        // Si es veterinario viendo cliente, usar clienteId directamente
        if (esVeterinario && clienteId != null && !clienteId.isEmpty()) {
            Log.d("HistorialVacunas", "Cargando vacunas como veterinario - ClienteId: " + clienteId + ", MascotaId: " + mascotaId);

            db.collection("usuarios")
                    .document(clienteId) // Usar directamente el clienteId
                    .collection("mascotas")
                    .document(mascotaId)
                    .collection("vacunas")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            vacunasList.clear(); // Limpiar la lista antes de agregar
                            for (DocumentSnapshot document : queryDocumentSnapshots) {
                                Vacuna vacuna = document.toObject(Vacuna.class);
                                if (vacuna != null) {
                                    vacunasList.add(vacuna);
                                }
                            }
                            adapter.notifyDataSetChanged();
                            Log.d("HistorialVacunas", "Vacunas cargadas exitosamente: " + vacunasList.size());
                        } else {
                            Toast.makeText(this, "No se encontraron vacunas para esta mascota", Toast.LENGTH_SHORT).show();
                            Log.d("HistorialVacunas", "No se encontraron vacunas");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("HistorialVacunas", "Error al cargar el historial de vacunas del cliente", e);
                        Toast.makeText(this, "Error al cargar las vacunas", Toast.LENGTH_SHORT).show();
                    });

        } else if (esVeterinario && emailCliente != null && !emailCliente.isEmpty()) {
            // Si tenemos email del cliente, buscar por email (método alternativo)
            Log.d("HistorialVacunas", "Cargando vacunas como veterinario usando email - Email: " + emailCliente + ", MascotaId: " + mascotaId);

            db.collection("usuarios")
                    .whereEqualTo("email", emailCliente)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            String docId = queryDocumentSnapshots.getDocuments().get(0).getId();

                            db.collection("usuarios")
                                    .document(docId)
                                    .collection("mascotas")
                                    .document(mascotaId)
                                    .collection("vacunas")
                                    .get()
                                    .addOnSuccessListener(snapshot -> {
                                        if (!snapshot.isEmpty()) {
                                            vacunasList.clear(); // Limpiar la lista antes de agregar
                                            for (DocumentSnapshot document : snapshot) {
                                                Vacuna vacuna = document.toObject(Vacuna.class);
                                                if (vacuna != null) {
                                                    vacunasList.add(vacuna);
                                                }
                                            }
                                            adapter.notifyDataSetChanged();
                                            Log.d("HistorialVacunas", "Vacunas cargadas exitosamente: " + vacunasList.size());
                                        } else {
                                            Toast.makeText(this, "No se encontraron vacunas para esta mascota", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("HistorialVacunas", "Error al cargar vacunas", e);
                                        Toast.makeText(this, "Error al cargar las vacunas", Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            Toast.makeText(this, "No se encontró el cliente", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("HistorialVacunas", "Error al buscar cliente por email", e);
                        Toast.makeText(this, "Error al buscar el cliente", Toast.LENGTH_SHORT).show();
                    });

        } else {
            // Comportamiento normal para usuarios regulares
            Log.d("HistorialVacunas", "Cargando vacunas como usuario regular - UserId: " + userId + ", MascotaId: " + mascotaId);

            db.collection("usuarios")
                    .document(userId)
                    .collection("mascotas")
                    .document(mascotaId)
                    .collection("vacunas")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            vacunasList.clear(); // Limpiar la lista antes de agregar
                            for (DocumentSnapshot document : queryDocumentSnapshots) {
                                Vacuna vacuna = document.toObject(Vacuna.class);
                                if (vacuna != null) {
                                    vacunasList.add(vacuna);
                                }
                            }
                            adapter.notifyDataSetChanged();
                            Log.d("HistorialVacunas", "Vacunas cargadas exitosamente: " + vacunasList.size());
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
}

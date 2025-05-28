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
    private Button btnVerHistorial;

    // Variables para cuando un veterinario está viendo los tratamientos de un cliente
    private boolean esVeterinario = false;
    private boolean esVeterinarioViendoCliente = false;
    private String clienteId;
    private String emailCliente;
    private String nombreCliente;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historial_tratamientos);

        textViewMascotaNombre = findViewById(R.id.textViewMascotaNombre);
        recyclerView = findViewById(R.id.recyclerViewTratamientos);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Obtener los valores del Intent
        Intent intent = getIntent();
        mascotaId = intent.getStringExtra("mascotaId");

        // Verificar si es un veterinario
        esVeterinario = intent.getBooleanExtra("esVeterinario", false);
        esVeterinarioViendoCliente = intent.getBooleanExtra("esVeterinarioViendoCliente", false);

        // Obtener información del cliente si es veterinario viendo cliente
        clienteId = intent.getStringExtra("clienteId");
        emailCliente = intent.getStringExtra("clienteEmail");
        nombreCliente = intent.getStringExtra("nombreCliente");

        // Determinar el userId correcto según el contexto
        if (esVeterinarioViendoCliente && clienteId != null && !clienteId.isEmpty()) {
            // Si es veterinario viendo cliente, usar clienteId
            userId = clienteId;
            Log.d("HistorialTratamientos", "Veterinario viendo cliente - usando clienteId: " + clienteId);
        } else {
            // Si es usuario normal o veterinario sin cliente específico, usar el usuario autenticado
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            Log.d("HistorialTratamientos", "Usuario normal - usando userId actual: " + userId);
        }

        // Debug: Verificar valores recibidos
        Log.d("HistorialTratamientos", "UserId final: " + userId);
        Log.d("HistorialTratamientos", "MascotaId: " + mascotaId);
        Log.d("HistorialTratamientos", "Es veterinario: " + esVeterinario);
        Log.d("HistorialTratamientos", "Es veterinario viendo cliente: " + esVeterinarioViendoCliente);

        // Validar datos esenciales
        if (userId == null || mascotaId == null) {
            Log.e("HistorialTratamientos", "Datos esenciales nulos - UserId: " + userId + ", MascotaId: " + mascotaId);
            Toast.makeText(this, "Error: Datos de sesión inválidos", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Configurar el adaptador
        adapter = new TratamientoAdapter(tratamientos, this, userId, mascotaId);
        recyclerView.setAdapter(adapter);

        // Firebase
        db = FirebaseFirestore.getInstance();

        // Verificar que Firebase esté inicializado
        if (db == null) {
            Log.e("HistorialTratamientos", "FirebaseFirestore no se pudo inicializar");
            Toast.makeText(this, "Error de conexión con la base de datos", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Botón "Ver Historial"
        btnVerHistorial = findViewById(R.id.btnVerHistorial);
        btnVerHistorial.setOnClickListener(v -> {
            Intent intentHistorial = new Intent(this, HistorialCumplimientosActivity.class);
            intentHistorial.putExtra("userId", userId);
            intentHistorial.putExtra("mascotaId", mascotaId);

            // Pasar información del veterinario si aplica
            if (esVeterinario) {
                intentHistorial.putExtra("esVeterinario", true);
            }

            if (esVeterinarioViendoCliente) {
                intentHistorial.putExtra("esVeterinarioViendoCliente", true);
                intentHistorial.putExtra("clienteId", clienteId);
                intentHistorial.putExtra("clienteEmail", emailCliente);
                intentHistorial.putExtra("nombreCliente", nombreCliente);
            }

            startActivity(intentHistorial);
        });

        // Cargar tratamientos
        cargarTratamientos();
    }

    private void cargarTratamientos() {
        Log.d("HistorialTratamientos", "Cargando tratamientos - UserId: " + userId + ", MascotaId: " + mascotaId);

        db.collection("usuarios")
                .document(userId)
                .collection("mascotas")
                .document(mascotaId)
                .collection("tratamientos")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        tratamientos.clear(); // Limpiar la lista antes de agregar
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            Tratamiento t = doc.toObject(Tratamiento.class);
                            if (t != null) {
                                t.setId(doc.getId()); // importante para pasar id después

                                if (doc.contains("fechaCumplimiento")) {
                                    t.setFechaCumplimiento(doc.getString("fechaCumplimiento"));
                                }

                                tratamientos.add(t);
                            }
                        }
                        adapter.notifyDataSetChanged();
                        Log.d("HistorialTratamientos", "Tratamientos cargados exitosamente: " + tratamientos.size());
                    } else {
                        String mensaje = esVeterinarioViendoCliente ?
                                "No se encontraron tratamientos para esta mascota del cliente" :
                                "No se encontraron tratamientos";
                        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
                        Log.d("HistorialTratamientos", "No se encontraron tratamientos");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("HistorialTratamientos", "Error al cargar el historial de tratamientos", e);
                    Toast.makeText(this, "Error al cargar los tratamientos", Toast.LENGTH_SHORT).show();
                });
    }
}

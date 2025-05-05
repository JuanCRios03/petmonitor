package co.edu.unipiloto.petmonitor.CasosdeUso;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import co.edu.unipiloto.petmonitor.R;

public class monitoreoEjercicio2 extends AppCompatActivity {
    private static final String TAG = "MonitoreoEjercicio2";
    private FirebaseFirestore db;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_monitoreo_ejercicio2);

        // Configurar los insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();


        Button btnVolverInicio = findViewById(R.id.btnVolverInicio);
        btnVolverInicio.setOnClickListener(v -> {
            finish();
        });

        // Verificar si el usuario está autenticado
        if (user == null) {
            Log.e(TAG, "Error: Usuario no autenticado");
            Toast.makeText(this, "Error: Usuario no autenticado", Toast.LENGTH_LONG).show();
            finish(); // Terminar actividad si no hay usuario autenticado
            return;
        }

        // Obtener datos del intent
        String distancia = getIntent().getStringExtra("distancia");
        String duracion = getIntent().getStringExtra("duracion");
        String calorias = getIntent().getStringExtra("calorias");

        // Validar que los datos no sean nulos
        System.out.println("Distancia: " + distancia);
        System.out.println("Duración: " + duracion);
        System.out.println("Calorías: " + calorias);

        // Mostrar los datos en la UI
        TextView txtDistancia = findViewById(R.id.txtDistancia);
        TextView txtDuracion = findViewById(R.id.txtDuracion);
        TextView txtCalorias = findViewById(R.id.txtCalorias);

        txtDistancia.setText(distancia);
        txtDuracion.setText(duracion);
        txtCalorias.setText(calorias);

        // Primero, crea una instancia de SimpleDateFormat con el mismo formato que usas en reporteActividad
        SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        Map<String, Object> datosMonitoreo = new HashMap<>();
        datosMonitoreo.put("distancia", distancia);
        datosMonitoreo.put("duracion", duracion);
        datosMonitoreo.put("calorias", calorias);
        datosMonitoreo.put("timestamp", System.currentTimeMillis());

// Agregar la fecha formateada para que coincida con el formato de consulta
        String fechaActual = formatoFecha.format(new Date());
        datosMonitoreo.put("fecha", fechaActual);

        Log.d(TAG, "Intentando guardar datos: " + datosMonitoreo.toString());

        // Guardar datos en la subcolección de la mascota
        guardarDatosMonitoreo(datosMonitoreo);
    }

    private void guardarDatosMonitoreo(Map<String, Object> datosMonitoreo) {
        String userId = user.getUid();
        Log.d(TAG, "ID de usuario: " + userId);

        // Mostrar diálogo de progreso
        Toast.makeText(this, "Guardando datos de ejercicio...", Toast.LENGTH_SHORT).show();

        // Primero verificar si el usuario existe en la colección
        db.collection("usuarios")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Usuario existe, buscar sus mascotas
                        buscarYGuardarEnMascotas(userId, datosMonitoreo);
                    } else {
                        // Usuario no existe en Firestore
                        Log.e(TAG, "Error: El usuario con ID " + userId + " no existe en Firestore");
                        Toast.makeText(monitoreoEjercicio2.this,
                                "Error: No se encontró el usuario en la base de datos", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al verificar usuario: " + e.getMessage());
                    Toast.makeText(monitoreoEjercicio2.this,
                            "Error al verificar usuario: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void buscarYGuardarEnMascotas(String userId, Map<String, Object> datosMonitoreo) {
        db.collection("usuarios")
                .document(userId)
                .collection("mascotas")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        Log.e(TAG, "Error: No se encontraron mascotas para este usuario");
                        Toast.makeText(monitoreoEjercicio2.this,
                                "Error: No se encontraron mascotas asociadas al usuario", Toast.LENGTH_LONG).show();
                        return;
                    }

                    //boolean guardadoExitoso = false;

                    for (QueryDocumentSnapshot mascotaDoc : queryDocumentSnapshots) {
                        String mascotaId = mascotaDoc.getId();
                        String nombreMascota = mascotaDoc.getString("nombreMascota");

                        Log.d(TAG, "Guardando datos para mascota: ID=" + mascotaId + ", Nombre=" + nombreMascota);

                        // Guardar en la subcolección "monitoreo" de la mascota
                        db.collection("usuarios")
                                .document(userId)
                                .collection("mascotas")
                                .document(mascotaId)
                                .collection("monitoreo")
                                .add(datosMonitoreo)
                                .addOnSuccessListener(documentReference -> {
                                    //guardadoExitoso = true;
                                    Log.d(TAG, "Datos guardados correctamente con ID: " + documentReference.getId());
                                    Toast.makeText(monitoreoEjercicio2.this,
                                            "Datos de ejercicio guardados correctamente", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error al guardar datos de monitoreo: " + e.getMessage());
                                    Toast.makeText(monitoreoEjercicio2.this,
                                            "Error al guardar datos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al buscar mascotas: " + e.getMessage());
                    Toast.makeText(monitoreoEjercicio2.this,
                            "Error al buscar mascotas: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}
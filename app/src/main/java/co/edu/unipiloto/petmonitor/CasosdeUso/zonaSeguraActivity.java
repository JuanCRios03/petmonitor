package co.edu.unipiloto.petmonitor.CasosdeUso;

import co.edu.unipiloto.petmonitor.Menu.menuActivity;
import co.edu.unipiloto.petmonitor.R;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;
import android.content.SharedPreferences;
import com.google.android.gms.location.LocationRequest;
import android.os.Looper;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;


public class zonaSeguraActivity extends AppCompatActivity {

    private EditText etLatitude, etLongitude, etRadius;
    private Button btnGuardar, btnVerMapa, btnHome;
    private FirebaseFirestore db;

    private FusedLocationProviderClient fusedLocationClient;
    private double currentLatitude = 0.0;
    private double currentLongitude = 0.0;
    private boolean ubicacionObtenida = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_safe_zone);

        // Inicializar Firestore
        db = FirebaseFirestore.getInstance();

        // Inicializar cliente de ubicación
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        obtenerUbicacionActual();

        // Referencias del layout
        etLatitude = findViewById(R.id.etLatitude);
        etLongitude = findViewById(R.id.etLongitude);
        etRadius = findViewById(R.id.etRadius);
        btnGuardar = findViewById(R.id.btnGuardar);
        btnVerMapa = findViewById(R.id.btnVerMapa);
        btnHome = findViewById(R.id.btnHome);

        findViewById(R.id.btnHome).setOnClickListener(view -> {
            Intent intent = new Intent(zonaSeguraActivity.this, menuActivity.class);
            startActivity(intent);
        });

        // Obtener el correo desde SharedPreferences
        String email = getEmailFromPreferences();

        if (email == null || email.isEmpty()) {
            Toast.makeText(this, "No se pudo obtener el correo del usuario.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Listeners
        btnGuardar.setOnClickListener(v -> {
            if (!ubicacionObtenida && (etLatitude.getText().toString().isEmpty() || etLongitude.getText().toString().isEmpty())) {
                Toast.makeText(this, "Esperando obtener ubicación actual...", Toast.LENGTH_SHORT).show();
                return;
            }
            guardarZonaSeguraEnFirestore(email);
        });

        btnVerMapa.setOnClickListener(v -> abrirMapa(email));
    }

    private void obtenerUbicacionActual() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(1000); // cada segundo

        fusedLocationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) return;

                Location location = locationResult.getLastLocation();
                if (location != null) {
                    currentLatitude = location.getLatitude();
                    currentLongitude = location.getLongitude();

                    // Opcional: actualizar campos visualmente si están vacíos
                    if (etLatitude.getText().toString().isEmpty())
                        etLatitude.setText(String.valueOf(currentLatitude));

                    if (etLongitude.getText().toString().isEmpty())
                        etLongitude.setText(String.valueOf(currentLongitude));
                }
            }
        }, Looper.getMainLooper());
    }


    private String getEmailFromPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String email = sharedPreferences.getString("email", null);
        Log.d("ZonaSegura", "Email recuperado de SharedPreferences: " + email); // Agregar log para depuración
        return email;
    }


    private void guardarZonaSeguraEnFirestore(String email) {
        if (!validarCampos()) return;

        // Buscar al usuario por su email
        db.collection("usuarios")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Usuario encontrado
                        String userId = queryDocumentSnapshots.getDocuments().get(0).getId();

                        // Verificar si tiene al menos una mascota
                        db.collection("usuarios").document(userId)
                                .collection("mascotas")
                                .get()
                                .addOnSuccessListener(mascotasSnapshots -> {
                                    if (!mascotasSnapshots.isEmpty()) {
                                        // Mascota encontrada, obtener el primer ID de mascota
                                        String mascotaId = mascotasSnapshots.getDocuments().get(0).getId();

                                        // Guardar la zona segura
                                        guardarZonaSegura(userId, mascotaId);
                                    } else {
                                        // No se encontraron mascotas
                                        Toast.makeText(this, "No se encontró ninguna mascota asociada a este usuario.", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Error al buscar mascotas: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });

                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al buscar usuario: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Método para guardar la zona segura
    private void guardarZonaSegura(String userId, String mascotaId) {
        // Obtener valores de la zona segura
        double lat = etLatitude.getText().toString().isEmpty() ? currentLatitude : Double.parseDouble(etLatitude.getText().toString());
        double lon = etLongitude.getText().toString().isEmpty() ? currentLongitude : Double.parseDouble(etLongitude.getText().toString());
        float radio = Float.parseFloat(etRadius.getText().toString());

        // Crear el mapa de datos
        Map<String, Object> datosZona = new HashMap<>();
        datosZona.put("latitud", lat);
        datosZona.put("longitud", lon);
        datosZona.put("radio", radio);
        datosZona.put("timestamp", System.currentTimeMillis());

        // Guardar en la subcolección "zonasegura" de la mascota
        db.collection("usuarios")
                .document(userId)
                .collection("mascotas")
                .document(mascotaId)
                .collection("zonasegura")
                .add(datosZona)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Zona segura guardada correctamente", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al guardar zona segura: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void abrirMapa(String email) {
        Intent intent = new Intent(this, MapaZonaSegura.class);
        intent.putExtra("email", email);
        startActivity(intent);
    }

    private boolean validarCampos() {
        if (etRadius.getText().toString().isEmpty()) {
            Toast.makeText(this, "El campo radio es obligatorio", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}


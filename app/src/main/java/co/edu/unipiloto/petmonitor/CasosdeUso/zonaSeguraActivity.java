package co.edu.unipiloto.petmonitor.CasosdeUso;

import co.edu.unipiloto.petmonitor.Menu.menuActivity;
import co.edu.unipiloto.petmonitor.R;
import android.content.Intent;
import android.os.Bundle;
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
        return sharedPreferences.getString("email", null);
    }

    private void guardarZonaSeguraEnFirestore(String email) {
        if (!validarCampos()) return;

        db.collection("usuarios")
                .document(email)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Datos del usuario
                        String nombre = documentSnapshot.getString("nombre");
                        String apellido = documentSnapshot.getString("apellido");
                        String edad = documentSnapshot.getString("edad");
                        String especie = documentSnapshot.getString("especie");
                        String nombreMascota = documentSnapshot.getString("nombreMascota");
                        String raza = documentSnapshot.getString("raza");
                        String peso = documentSnapshot.getString("peso");
                        String password = documentSnapshot.getString("password");

                        // Obtener coordenadas
                        double lat = etLatitude.getText().toString().isEmpty() ? currentLatitude : Double.parseDouble(etLatitude.getText().toString());
                        double lon = etLongitude.getText().toString().isEmpty() ? currentLongitude : Double.parseDouble(etLongitude.getText().toString());
                        float radio = Float.parseFloat(etRadius.getText().toString());

                        // Map de datos
                        Map<String, Object> datos = new HashMap<>();
                        datos.put("nombre", nombre);
                        datos.put("apellido", apellido);
                        datos.put("edad", edad);
                        datos.put("especie", especie);
                        datos.put("nombreMascota", nombreMascota);
                        datos.put("raza", raza);
                        datos.put("peso", peso);
                        datos.put("password", password);
                        datos.put("email", email);
                        datos.put("latitud", lat);
                        datos.put("longitud", lon);
                        datos.put("radio", radio);

                        db.collection("usuarios").document(email)
                                .set(datos)
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(this, "Zona segura guardada en Firestore", Toast.LENGTH_SHORT).show();

                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, "Error al guardar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Toast.makeText(this, "No se encontró el usuario con el correo: " + email, Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al obtener los datos del usuario: " + e.getMessage(), Toast.LENGTH_SHORT).show();
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


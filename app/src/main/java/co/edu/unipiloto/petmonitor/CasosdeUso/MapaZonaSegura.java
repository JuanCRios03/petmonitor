package co.edu.unipiloto.petmonitor.CasosdeUso;


import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import co.edu.unipiloto.petmonitor.R;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import android.graphics.Color;  // Esta importación es para la clase Color
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.Map;

public class MapaZonaSegura extends AppCompatActivity {
    private GoogleMap mMap;
    private Button btnVolver, btnEliminarZonaSegura, btnSimularMovimiento;
    private LatLng petLocation;
    private CircleOptions safeZoneCircle;
    private Marker petMarker;
    private double movementOffset = 0.0005;
    private String userEmail;
    private FirebaseFirestore db;
    private String nombreMascota;  // Cambiado para usar nombreMascota

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_safe_zone_map);

        // Inicializamos Firestore
        db = FirebaseFirestore.getInstance();

        // Obtener el correo del usuario del Intent
        userEmail = getIntent().getStringExtra("email");
        Log.d("MapaZonaSegura", "Email: " + userEmail);

        // Referencias del layout
        btnVolver = findViewById(R.id.btnVolver);
        btnEliminarZonaSegura = findViewById(R.id.btnEliminarZonaSegura);
        btnSimularMovimiento = findViewById(R.id.btnSimularMovimiento);

        btnVolver.setOnClickListener(v -> finish());

        btnEliminarZonaSegura.setOnClickListener(v -> {
            if (nombreMascota != null) {
                eliminarZonaSegura(userEmail);
            } else {
                Toast.makeText(this, "Nombre de mascota no válido", Toast.LENGTH_SHORT).show();
            }
        });

        btnSimularMovimiento.setOnClickListener(v -> {
            Log.d("MapaZonaSegura", "Botón de simulación presionado");
            simularMovimiento();
        });

        // Inicializar el mapa
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(googleMap -> {
                mMap = googleMap;
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                Log.d("MapaZonaSegura", "Mapa inicializado correctamente.");

                if (userEmail != null) {
                    obtenerPetIdYZonaSegura();
                } else {
                    Log.e("MapaZonaSegura", "Correo del usuario no válido");
                    Toast.makeText(this, "Correo del usuario no válido", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void obtenerPetIdYZonaSegura() {
        // Obtener el nombre de la mascota desde Firestore usando el correo del usuario
        db.collection("usuarios").whereEqualTo("email", userEmail)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Obtener el documento del usuario
                        DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                        nombreMascota = documentSnapshot.getString("nombreMascota");  // Usamos nombreMascota
                        Log.d("MapaZonaSegura", "Nombre de mascota obtenido: " + nombreMascota);

                        if (nombreMascota != null) {
                            // Aquí pasamos el email del usuario al llamar a cargarZonaSegura
                            cargarZonaSegura(userEmail);  // Pasa userEmail aquí
                        } else {
                            Log.e("MapaZonaSegura", "Nombre de mascota no encontrado");
                            Toast.makeText(this, "Nombre de mascota no encontrado", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e("MapaZonaSegura", "Usuario no encontrado en Firestore");
                        Toast.makeText(this, "Usuario no encontrado en Firestore", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("MapaZonaSegura", "Error al obtener nombreMascota: " + e.getMessage());
                    Toast.makeText(this, "Error al obtener nombreMascota", Toast.LENGTH_SHORT).show();
                });
    }

    private void cargarZonaSegura(String userEmail) {
        db.collection("usuarios")
                .whereEqualTo("email", userEmail)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);

                        // Obtener directamente los datos de la zona segura
                        Double lat = documentSnapshot.getDouble("latitud");
                        Double lon = documentSnapshot.getDouble("longitud");
                        Double radius = documentSnapshot.getDouble("radio");

                        if (lat != null && lon != null && radius != null && lat != 0 && lon != 0) {
                            Log.d("MapaZonaSegura", "Latitud: " + lat);
                            Log.d("MapaZonaSegura", "Longitud: " + lon);
                            Log.d("MapaZonaSegura", "Radio: " + radius);

                            LatLng centroZonaSegura = new LatLng(lat, lon);

                            safeZoneCircle = new CircleOptions()
                                    .center(centroZonaSegura)
                                    .radius(radius)
                                    .strokeColor(Color.RED)
                                    .fillColor(0x30FF0000)
                                    .strokeWidth(2f);

                            mMap.addCircle(safeZoneCircle);

                            petLocation = centroZonaSegura;
                            petMarker = mMap.addMarker(new MarkerOptions().position(petLocation).title("Mascota"));
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(centroZonaSegura, 15f));
                        } else {
                            Log.e("MapaZonaSegura", "Zona segura no encontrada o datos inválidos.");
                            Toast.makeText(this, "Zona segura no encontrada o datos inválidos.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e("MapaZonaSegura", "Usuario no encontrado.");
                        Toast.makeText(this, "Usuario no encontrado en Firestore.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("MapaZonaSegura", "Error al cargar zona segura: " + e.getMessage());
                    Toast.makeText(this, "Error al cargar zona segura.", Toast.LENGTH_SHORT).show();
                });
    }





    private void eliminarZonaSegura(String userEmail) {
        db.collection("usuarios")
                .whereEqualTo("email", userEmail)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Obtener el documento del usuario
                        DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                        String userId = documentSnapshot.getId();

                        // Actualizar los campos de la zona segura a null
                        db.collection("usuarios").document(userId)
                                .update("latitud", null,
                                        "longitud", null,
                                        "radio", null)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(this, "Zona segura eliminada", Toast.LENGTH_SHORT).show();
                                    finish(); // Cierra la actividad si es necesario
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("MapaZonaSegura", "Error al eliminar zona segura: " + e.getMessage());
                                    Toast.makeText(this, "Error al eliminar zona segura.", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Log.e("MapaZonaSegura", "Usuario no encontrado.");
                        Toast.makeText(this, "Usuario no encontrado en Firestore.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("MapaZonaSegura", "Error al buscar usuario: " + e.getMessage());
                    Toast.makeText(this, "Error al buscar usuario en Firestore.", Toast.LENGTH_SHORT).show();
                });
    }


    private void simularMovimiento() {
        if (mMap != null && petMarker != null && safeZoneCircle != null) {
            // Simula el nuevo movimiento
            petLocation = new LatLng(petLocation.latitude + movementOffset, petLocation.longitude + movementOffset);

            runOnUiThread(() -> {
                petMarker.setPosition(petLocation);
                mMap.animateCamera(CameraUpdateFactory.newLatLng(petLocation));

                float[] distance = new float[1];
                android.location.Location.distanceBetween(
                        safeZoneCircle.getCenter().latitude, safeZoneCircle.getCenter().longitude,
                        petLocation.latitude, petLocation.longitude, distance
                );

                if (distance[0] > safeZoneCircle.getRadius()) {
                    Toast.makeText(this, "¡Alerta! La mascota ha salido de la zona segura.", Toast.LENGTH_LONG).show();
                    Log.d("MapaZonaSegura", "Mascota fuera de la zona segura.");

                    // Enviar correo de alerta
                    String subject = "Alerta: Tu mascota ha salido de la zona segura";
                    String body = "Hola, tu mascota ha salido del área segura configurada en la app. Revisa su ubicación lo antes posible.";
                    JavaMailAPI javaMailAPI = new JavaMailAPI(MapaZonaSegura.this, userEmail, subject, body);
                    javaMailAPI.execute();
                }
            });
        } else {
            Log.e("MapaZonaSegura", "Error: petMarker o safeZoneCircle es nulo");
        }
    }
}

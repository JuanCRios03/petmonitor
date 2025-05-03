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
    private String nombreMascota;
    private String mascotaId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_safe_zone_map);

        db = FirebaseFirestore.getInstance();

        userEmail = getIntent().getStringExtra("email");
        Log.d("MapaZonaSegura", "Email: " + userEmail);

        btnVolver = findViewById(R.id.btnVolver);
        btnEliminarZonaSegura = findViewById(R.id.btnEliminarZonaSegura);
        btnSimularMovimiento = findViewById(R.id.btnSimularMovimiento);

        btnVolver.setOnClickListener(v -> finish());

        btnEliminarZonaSegura.setOnClickListener(v -> {
            if (nombreMascota != null && mascotaId != null) {
                eliminarZonaSegura(userEmail, mascotaId);
            } else {
                Toast.makeText(this, "Datos de mascota no válidos", Toast.LENGTH_SHORT).show();
            }
        });

        btnSimularMovimiento.setOnClickListener(v -> {
            Log.d("MapaZonaSegura", "Botón de simulación presionado");
            simularMovimiento();
        });

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(googleMap -> {
                mMap = googleMap;
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                Log.d("MapaZonaSegura", "Mapa inicializado correctamente.");

                if (userEmail != null) {
                    validarUsuarioYcargarDatos();
                } else {
                    Log.e("MapaZonaSegura", "Correo del usuario no válido");
                    Toast.makeText(this, "Correo del usuario no válido", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void validarUsuarioYcargarDatos() {
        db.collection("usuarios")
                .whereEqualTo("email", userEmail)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot userDocument = queryDocumentSnapshots.getDocuments().get(0);
                        String userId = userDocument.getId();

                        db.collection("usuarios").document(userId).collection("mascotas")
                                .get()
                                .addOnSuccessListener(mascotasSnapshots -> {
                                    if (!mascotasSnapshots.isEmpty()) {
                                        DocumentSnapshot mascotaDocument = mascotasSnapshots.getDocuments().get(0);
                                        nombreMascota = mascotaDocument.getString("nombreMascota");
                                        mascotaId = mascotaDocument.getId();

                                        cargarZonaSegura(userId, mascotaId);
                                    } else {
                                        Log.e("MapaZonaSegura", "No se encontraron mascotas asociadas");
                                        Toast.makeText(this, "No se encontraron mascotas asociadas", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("MapaZonaSegura", "Error al buscar mascotas: " + e.getMessage());
                                    Toast.makeText(this, "Error al buscar mascotas", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        Log.e("MapaZonaSegura", "Usuario no encontrado en Firestore");
                        Toast.makeText(this, "Usuario no encontrado en Firestore", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("MapaZonaSegura", "Error al buscar usuario: " + e.getMessage());
                    Toast.makeText(this, "Error al buscar usuario", Toast.LENGTH_SHORT).show();
                });
    }

    private void cargarZonaSegura(String userId, String mascotaId) {
        db.collection("usuarios").document(userId)
                .collection("mascotas").document(mascotaId)
                .collection("zonasegura")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot zonaSeguraDoc = querySnapshot.getDocuments().get(0);
                        Double lat = zonaSeguraDoc.getDouble("latitud");
                        Double lon = zonaSeguraDoc.getDouble("longitud");
                        Double radius = zonaSeguraDoc.getDouble("radio");

                        if (lat != null && lon != null && radius != null) {
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
                            Log.e("MapaZonaSegura", "Datos de la zona segura no válidos");
                            Toast.makeText(this, "Datos de la zona segura no válidos", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e("MapaZonaSegura", "Zona segura no encontrada");
                        Toast.makeText(this, "Zona segura no encontrada", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("MapaZonaSegura", "Error al cargar zona segura: " + e.getMessage());
                    Toast.makeText(this, "Error al cargar zona segura", Toast.LENGTH_SHORT).show();
                });
    }

    private void eliminarZonaSegura(String userEmail, String mascotaId) {
        db.collection("usuarios")
                .whereEqualTo("email", userEmail)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        String userId = queryDocumentSnapshots.getDocuments().get(0).getId();

                        db.collection("usuarios").document(userId)
                                .collection("mascotas").document(mascotaId)
                                .collection("zonasegura")
                                .get()
                                .addOnSuccessListener(snapshot -> {
                                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                                        doc.getReference().delete()
                                                .addOnSuccessListener(aVoid -> {
                                                    Toast.makeText(this, "Zona segura eliminada", Toast.LENGTH_SHORT).show();
                                                })
                                                .addOnFailureListener(e -> {
                                                    Log.e("MapaZonaSegura", "Error al eliminar zona segura: " + e.getMessage());
                                                });
                                    }
                                });
                    }
                });
    }

    private void simularMovimiento() {
        if (mMap != null && petMarker != null && safeZoneCircle != null) {
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

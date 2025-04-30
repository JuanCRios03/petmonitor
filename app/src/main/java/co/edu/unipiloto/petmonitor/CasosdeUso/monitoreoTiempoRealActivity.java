package co.edu.unipiloto.petmonitor.CasosdeUso;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.*;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.*;

import java.io.IOException;
import java.util.*;

import co.edu.unipiloto.petmonitor.R;

public class monitoreoTiempoRealActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private static final int REQUEST_LOCATION_PERMISSION = 1001;

    private String mascotaId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ubicacion);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Obtener el ID de la mascota desde el intent
        mascotaId = getIntent().getStringExtra("mascotaId");
        if (mascotaId == null) {
            Log.e("ERROR", "No se recibió el ID de la mascota");
            finish();
            return;
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Log.e("MAP_ERROR", "El fragmento de mapa es null");
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
            return;
        }

        LocationRequest locationRequest = LocationRequest.create()
                .setInterval(60000) // 60 segundos
                .setFastestInterval(60000) // también 60 segundos para asegurar consistencia
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY);


        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult result) {
                if (result == null) return;
                Location location = result.getLastLocation();

                if (location != null) {
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

                    if (mMap != null) {
                        mMap.clear();
                        mMap.addMarker(new MarkerOptions().position(latLng).title("Ubicación actual"));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                    }

                    guardarUbicacionEnFirestore(latLng);
                }
            }
        };

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    private void guardarUbicacionEnFirestore(LatLng latLng) {
        String email = auth.getCurrentUser() != null ? auth.getCurrentUser().getEmail() : null;
        if (email == null) return;

        db.collection("usuarios")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.isEmpty()) {
                        String userId = snapshot.getDocuments().get(0).getId();

                        String direccion = "Dirección no disponible";
                        try {
                            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                            List<Address> direcciones = geocoder.getFromLocation(
                                    latLng.latitude, latLng.longitude, 1);
                            if (direcciones != null && !direcciones.isEmpty()) {
                                direccion = direcciones.get(0).getAddressLine(0);
                            }
                        } catch (IOException e) {
                            Log.e("GEOCODER", "Error obteniendo dirección: " + e.getMessage());
                        }

                        Map<String, Object> data = new HashMap<>();
                        data.put("latitud", latLng.latitude);
                        data.put("longitud", latLng.longitude);
                        data.put("direccion", direccion);
                        data.put("timestamp", System.currentTimeMillis());

                        db.collection("usuarios")
                                .document(userId)
                                .collection("mascotas")
                                .document(mascotaId)
                                .collection("ubicaciones")
                                .add(data)
                                .addOnSuccessListener(docRef -> Log.d("FIRESTORE", "Ubicación guardada"))
                                .addOnFailureListener(e -> Log.e("FIRESTORE", "Error al guardar ubicación", e));
                    } else {
                        Log.e("FIRESTORE", "No se encontró usuario con el correo");
                    }
                })
                .addOnFailureListener(e -> Log.e("FIRESTORE", "Error consultando usuario", e));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                Log.e("PERMISSION", "Permiso de ubicación denegado");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }
}



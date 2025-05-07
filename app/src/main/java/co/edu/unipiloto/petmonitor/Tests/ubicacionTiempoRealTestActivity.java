package co.edu.unipiloto.petmonitor.Tests;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import co.edu.unipiloto.petmonitor.R;

public class ubicacionTiempoRealTestActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int REQUEST_LOCATION_PERMISSION = 1;

    private GoogleMap mMap;
    private Marker currentMarker;
    private Handler handler = new Handler();
    private Runnable locationUpdater;
    private FusedLocationProviderClient fusedLocationClient;
    private FirebaseFirestore db;

    private final String usuarioId = "8vegW50jyrUF8OWsnt4jvCpPtSo2";
    private final String mascotaId = "BhzARsMiZOuxLPdxajPV";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ubicacion_tiempo_real_test);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        db = FirebaseFirestore.getInstance();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapaTest);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        Button btnVerHistorial = findViewById(R.id.btnVerHistorial);
        btnVerHistorial.setOnClickListener(v -> {
            Intent intent = new Intent(this, historialUbicacionTestActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        locationUpdater = new Runnable() {
            @Override
            public void run() {
                if (ActivityCompat.checkSelfPermission(ubicacionTiempoRealTestActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(ubicacionTiempoRealTestActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
                    return;
                }

                fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                    if (location != null) {
                        showLocationOnMap(location);
                        saveLocationToFirestore(location);
                    }
                });

                handler.postDelayed(this, 60000); // cada minuto
            }
        };

        handler.post(locationUpdater);
    }

    private void showLocationOnMap(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        if (currentMarker != null) currentMarker.remove();
        currentMarker = mMap.addMarker(new MarkerOptions().position(latLng).title("Ubicación actual"));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f));
    }

    private void saveLocationToFirestore(Location location) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        String direccion = "Dirección desconocida";

        try {
            List<Address> direcciones = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (!direcciones.isEmpty()) {
                direccion = direcciones.get(0).getAddressLine(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Map<String, Object> data = new HashMap<>();
        data.put("latitud", location.getLatitude());
        data.put("longitud", location.getLongitude());
        data.put("direccion", direccion);
        data.put("timestamp", new Date().getTime());

        db.collection("usuarios")
                .document(usuarioId)
                .collection("mascotas")
                .document(mascotaId)
                .collection("ubicaciones")
                .add(data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(locationUpdater);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        }
    }
}

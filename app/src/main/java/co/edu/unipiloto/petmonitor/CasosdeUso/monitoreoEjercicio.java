package co.edu.unipiloto.petmonitor.CasosdeUso;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import co.edu.unipiloto.petmonitor.R;

public class monitoreoEjercicio extends AppCompatActivity {

    private ImageView imageView;
    private TextView tvContador;
    private Handler handler = new Handler();
    private int seconds = 0;
    private boolean contadorActivo = false;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private FusedLocationProviderClient fusedLocationClient;
    private double latitudeInicial, longitudeInicial;
    private double latitudeFinal, longitudeFinal;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String currentUserEmail;
    private String uid;
    private float distancia = 0;
    private String duracion = "00:00";
    private double calorias = 0;

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (contadorActivo) {
                seconds++;
                int minutes = seconds / 60;
                int sec = seconds % 60;
                duracion = String.format("%02d:%02d", minutes, sec);
                tvContador.setText(duracion);
                handler.postDelayed(this, 1000);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_monitoreo_ejercicio);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            uid = user.getUid();
        }

        imageView = findViewById(R.id.imageView);
        tvContador = findViewById(R.id.tvContador);
        Button btnIniciar = findViewById(R.id.btnIniciar);
        Button btnPausar = findViewById(R.id.btnPausar);
        Button btnFinalizar = findViewById(R.id.btnFinalizar);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        btnIniciar.setOnClickListener(v -> {
            imageView.setVisibility(View.GONE);
            tvContador.setVisibility(View.VISIBLE);
            if (!contadorActivo) {
                contadorActivo = true;
                handler.post(runnable);
                getInitialLocation();
            }
        });

        btnPausar.setOnClickListener(v -> {
            contadorActivo = false;
            handler.removeCallbacks(runnable);
        });

        btnFinalizar.setOnClickListener(v -> {
            contadorActivo = false;
            handler.removeCallbacks(runnable);
            tvContador.setVisibility(View.GONE);
            imageView.setVisibility(View.VISIBLE);

            getFinalLocationAndProceed();
        });
    }

    private void getInitialLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                latitudeInicial = location.getLatitude();
                longitudeInicial = location.getLongitude();
            } else {
                Toast.makeText(this, "Ubicación inicial no disponible.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getFinalLocationAndProceed() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "No tienes permisos de ubicación", Toast.LENGTH_SHORT).show();
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                latitudeFinal = location.getLatitude();
                longitudeFinal = location.getLongitude();

                Location locInicial = new Location("punto inicial");
                locInicial.setLatitude(latitudeInicial);
                locInicial.setLongitude(longitudeInicial);

                Location locFinal = new Location("punto final");
                locFinal.setLatitude(latitudeFinal);
                locFinal.setLongitude(longitudeFinal);

                distancia = locInicial.distanceTo(locFinal); // en metros

                calorias = (distancia / 1000.0) * 50; // Suponiendo 50 kcal por km

                saveLocationOnDB();

                Intent intent = new Intent(this, monitoreoEjercicio2.class);
                intent.putExtra("distancia", distancia);
                intent.putExtra("duracion", duracion);
                intent.putExtra("calorias", calorias);
                startActivity(intent);

            } else {
                Toast.makeText(this, "Ubicación final no disponible.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getInitialLocation();
            } else {
                Toast.makeText(this, "Permiso de ubicación denegado.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void saveLocationOnDB() {
        Map<String, Object> location = new HashMap<>();
        location.put("latitudeInicial", latitudeInicial);
        location.put("longitudeInicial", longitudeInicial);
        location.put("latitudeFinal", latitudeFinal);
        location.put("longitudeFinal", longitudeFinal);
        location.put("userid", uid);
        location.put("duracion", duracion);
        location.put("distancia", distancia);
        location.put("calorias", calorias);
        db.collection("exercise_location").add(location);
    }
}

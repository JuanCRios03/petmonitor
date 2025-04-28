package co.edu.unipiloto.petmonitor.CasosdeUso;

import android.Manifest;
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
    private double latitude, longitude;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String currentUserEmail;


    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (contadorActivo) {
                seconds++;
                int minutes = seconds / 60;
                int sec = seconds % 60;
                String time = String.format("%02d:%02d", minutes, sec);
                tvContador.setText(time);
                handler.postDelayed(this, 1000);
                if (seconds % 5 == 0) {
                    saveLocationOnDB(seconds);
                }
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

        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        currentUserEmail = sharedPreferences.getString("email", null);

        if (currentUserEmail == null) {
            Toast.makeText(this, "No hay sesión activa", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        imageView = findViewById(R.id.imageView);
        tvContador = findViewById(R.id.tvContador);
        Button btnIniciar = findViewById(R.id.btnIniciar);
        Button btnPausar = findViewById(R.id.btnPausar);
        Button btnFinalizar = findViewById(R.id.btnFinalizar);

        btnIniciar.setOnClickListener(v -> {
            imageView.setVisibility(View.GONE);
            tvContador.setVisibility(View.VISIBLE);
            if (!contadorActivo) {
                contadorActivo = true;
                handler.post(runnable);
            }
        });

        btnPausar.setOnClickListener(v -> {
            contadorActivo = false;
            handler.removeCallbacks(runnable);
        });

        btnFinalizar.setOnClickListener(v -> {
            contadorActivo = false;
            handler.removeCallbacks(runnable);
            seconds = 0;
            tvContador.setText("00:00");
            tvContador.setVisibility(View.GONE);
            imageView.setVisibility(View.VISIBLE);
        });
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        getLastLocation();
    }


    private double[] getLastLocation() {
        double[] locationArray = new double[2];
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "No tienes permisos de ubicación", Toast.LENGTH_SHORT).show();
            return null;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                } else {
                    Toast.makeText(monitoreoEjercicio.this, "No se pudo obtener la ubicación.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        locationArray[0] = latitude;
        locationArray[1] = longitude;
        return locationArray;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            } else {
                Toast.makeText(this, "Permiso de ubicación denegado.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void saveLocationOnDB(int second) {
        double[] currentLocation = getLastLocation();
        if (currentLocation == null) return;
        double latitude = currentLocation[0];
        double longitude = currentLocation[1];
        Map<String, Object> location = new HashMap<>();
        location.put("latitude", latitude);
        location.put("longitude", longitude);
        location.put("email", currentUserEmail);
        db.collection("exercise_location").add(location);
    }
}

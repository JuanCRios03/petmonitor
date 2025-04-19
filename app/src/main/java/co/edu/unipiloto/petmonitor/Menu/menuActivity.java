package co.edu.unipiloto.petmonitor.Menu;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

import co.edu.unipiloto.petmonitor.CasosdeUso.monitoreoTiempoRealActivity;
import co.edu.unipiloto.petmonitor.CasosdeUso.registrarNuevaMascotaActivity;
import co.edu.unipiloto.petmonitor.CasosdeUso.historialUbicacionActivity;
import co.edu.unipiloto.petmonitor.CasosdeUso.zonaSeguraActivity;
import co.edu.unipiloto.petmonitor.CasosdeUso.reporteActividadActivity;
import co.edu.unipiloto.petmonitor.CasosdeUso.buscarVeterinariasActivity;
import co.edu.unipiloto.petmonitor.CasosdeUso.registrarVacunasActivity;
import co.edu.unipiloto.petmonitor.CasosdeUso.monitoreoEjercicioActivity;
import co.edu.unipiloto.petmonitor.R;

public class menuActivity extends AppCompatActivity {

        @Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_menu);

                // Botón para monitoreo de la ubicación en tiempo real
                findViewById(R.id.btnRealTimeLocation).setOnClickListener(view -> {
                        Intent intent = new Intent(menuActivity.this, monitoreoTiempoRealActivity.class);
                        startActivity(intent);
                });

                // Botón para historial de ubicación
                findViewById(R.id.btnLocationHistory).setOnClickListener(view -> {
                        Intent intent = new Intent(menuActivity.this, historialUbicacionActivity.class);
                        startActivity(intent);
                });

                // Botón para registrar zona segura
                findViewById(R.id.btnRegisterSafeZone).setOnClickListener(view -> {
                        Intent intent = new Intent(menuActivity.this, zonaSeguraActivity.class);
                        startActivity(intent);
                });

                // Botón para reporte de actividad de la mascota
                findViewById(R.id.btnActivityReport).setOnClickListener(view -> {
                        Intent intent = new Intent(menuActivity.this, reporteActividadActivity.class);
                        startActivity(intent);
                });

                // Botón para buscar clínicas veterinarias cercanas
                findViewById(R.id.btnNearbyClinics).setOnClickListener(view -> {
                        Intent intent = new Intent(menuActivity.this, buscarVeterinariasActivity.class);
                        startActivity(intent);
                });

                // Botón para registrar vacunas de la mascota
                findViewById(R.id.btnRegisterVaccines).setOnClickListener(view -> {
                        Intent intent = new Intent(menuActivity.this, registrarVacunasActivity.class);
                        startActivity(intent);
                });

                // Botón para monitoreo de ejercicio de la mascota
                findViewById(R.id.btnExerciseMonitoring).setOnClickListener(view -> {
                        Intent intent = new Intent(menuActivity.this, monitoreoEjercicioActivity.class);
                        startActivity(intent);
                });
        }
}
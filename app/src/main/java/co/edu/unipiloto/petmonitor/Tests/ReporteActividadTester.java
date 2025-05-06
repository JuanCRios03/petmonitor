package co.edu.unipiloto.petmonitor.Tests;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import co.edu.unipiloto.petmonitor.CasosdeUso.reporteActividad;
import co.edu.unipiloto.petmonitor.R;

public class ReporteActividadTester extends AppCompatActivity {

    private Button btnProbarReporte;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reporte_actividad_tester);

        btnProbarReporte = findViewById(R.id.btnProbarReporte);

        btnProbarReporte.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Inicia la Activity de reporte real
                Intent intent = new Intent(ReporteActividadTester.this, reporteActividad.class);
                startActivity(intent);
            }
        });
    }
}

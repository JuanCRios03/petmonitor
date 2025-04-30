package co.edu.unipiloto.petmonitor.CasosdeUso;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import co.edu.unipiloto.petmonitor.R;

public class monitoreoEjercicio2 extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_monitoreo_ejercicio2);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        String distancia = getIntent().getStringExtra("distancia");
        String duracion = getIntent().getStringExtra("duracion");
        String calorias = getIntent().getStringExtra("calorias");
        TextView txtDistancia = findViewById(R.id.txtDistancia);
        TextView txtDuracion = findViewById(R.id.txtDuracion);
        TextView txtCalorias = findViewById(R.id.txtCalorias);

        txtDistancia.setText(distancia);
        txtDuracion.setText(duracion);
        txtCalorias.setText(calorias);


    }
}


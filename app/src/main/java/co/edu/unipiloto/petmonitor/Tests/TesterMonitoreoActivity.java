package co.edu.unipiloto.petmonitor.CasosdeUso;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import co.edu.unipiloto.petmonitor.R;

public class TesterMonitoreoActivity extends AppCompatActivity {

    private EditText edtDistancia, edtDuracion, edtCalorias;
    private Button btnEnviar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tester_monitoreo);

        edtDistancia = findViewById(R.id.edtDistancia);
        edtDuracion = findViewById(R.id.edtDuracion);
        edtCalorias = findViewById(R.id.edtCalorias);
        btnEnviar = findViewById(R.id.btnEnviarDatos);

        btnEnviar.setOnClickListener(v -> {
            String distancia = edtDistancia.getText().toString().trim();
            String duracion = edtDuracion.getText().toString().trim();
            String calorias = edtCalorias.getText().toString().trim();

            Intent intent = new Intent(TesterMonitoreoActivity.this, monitoreoEjercicio2.class);
            intent.putExtra("distancia", distancia);
            intent.putExtra("duracion", duracion);
            intent.putExtra("calorias", calorias);
            startActivity(intent);
        });
    }
}

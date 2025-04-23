package co.edu.unipiloto.petmonitor.Register;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import co.edu.unipiloto.petmonitor.R;

public class RegisterPaso3Activity extends AppCompatActivity {

    EditText etNombreMascota, etEspecie, etRaza, etPeso;
    Button btnGuardarMascota;
    String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_paso3);

        etNombreMascota = findViewById(R.id.etNombreMascota);
        etEspecie = findViewById(R.id.etEspecie);
        etRaza = findViewById(R.id.etRaza);
        etPeso = findViewById(R.id.etPeso);
        btnGuardarMascota = findViewById(R.id.btnGuardarMascota);

        // Obtener el email desde la actividad anterior
        email = getIntent().getStringExtra("email");

        // Rellenar campos si regresaron del paso 4
        etNombreMascota.setText(getIntent().getStringExtra("nombreMascota"));
        etEspecie.setText(getIntent().getStringExtra("especie"));
        etRaza.setText(getIntent().getStringExtra("raza"));
        etPeso.setText(getIntent().getStringExtra("peso"));

        btnGuardarMascota.setOnClickListener(v -> {
            String nombre = etNombreMascota.getText().toString().trim();
            String especie = etEspecie.getText().toString().trim();
            String raza = etRaza.getText().toString().trim();
            String peso = etPeso.getText().toString().trim();

            if (nombre.isEmpty() || especie.isEmpty() || raza.isEmpty() || peso.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            // Pasar datos a la siguiente pantalla
            Intent intent = new Intent(this, RegisterPaso4Activity.class);
            intent.putExtra("email", email);
            intent.putExtra("nombreMascota", nombre);
            intent.putExtra("especie", especie);
            intent.putExtra("raza", raza);
            intent.putExtra("peso", peso);
            startActivity(intent);
            finish();
        });
    }
}




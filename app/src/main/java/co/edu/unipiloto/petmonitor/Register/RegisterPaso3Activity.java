package co.edu.unipiloto.petmonitor.Register;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import co.edu.unipiloto.petmonitor.R;

public class RegisterPaso3Activity extends AppCompatActivity {

    EditText etNombreMascota, etEspecie, etRaza, etPeso;
    Button btnGuardarMascota;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_paso3);

        etNombreMascota = findViewById(R.id.etNombreMascota);
        etEspecie = findViewById(R.id.etEspecie);
        etRaza = findViewById(R.id.etRaza);
        etPeso = findViewById(R.id.etPeso);
        btnGuardarMascota = findViewById(R.id.btnGuardarMascota);

        // Recuperar datos si regresan del paso 4
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

            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser == null) {
                Toast.makeText(this, "Debes iniciar sesión primero", Toast.LENGTH_SHORT).show();
                return;
            }

            // Pasar datos a la siguiente actividad
            Intent intent = new Intent(this, RegisterPaso4Activity.class);
            intent.putExtra("nombreMascota", nombre);
            intent.putExtra("especie", especie);
            intent.putExtra("raza", raza);
            intent.putExtra("peso", peso);
            startActivity(intent);
            finish();
        });
    }
}












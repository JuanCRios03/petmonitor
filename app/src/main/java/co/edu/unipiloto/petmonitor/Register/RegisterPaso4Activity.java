package co.edu.unipiloto.petmonitor.Register;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

import co.edu.unipiloto.petmonitor.Login.LoginActivity;
import co.edu.unipiloto.petmonitor.R;

public class RegisterPaso4Activity extends AppCompatActivity {

    EditText spinnerEdad;
    Button btnAgregarEdad, btnCancelar;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_paso4);

        spinnerEdad = findViewById(R.id.spinnerEdad);
        btnAgregarEdad = findViewById(R.id.btnAgregarEdad);
        btnCancelar = findViewById(R.id.btnCancelar);
        db = FirebaseFirestore.getInstance();

        // Obtener datos del intent
        String email = getIntent().getStringExtra("email");
        String nombreMascota = getIntent().getStringExtra("nombreMascota");
        String especie = getIntent().getStringExtra("especie");
        String raza = getIntent().getStringExtra("raza");
        String peso = getIntent().getStringExtra("peso");

        btnAgregarEdad.setOnClickListener(v -> {
            String edad = spinnerEdad.getText().toString().trim();

            if (edad.isEmpty()) {
                Toast.makeText(this, "Por favor, ingresa la edad", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> mascota = new HashMap<>();
            mascota.put("nombreMascota", nombreMascota);
            mascota.put("especie", especie);
            mascota.put("raza", raza);
            mascota.put("peso", peso);
            mascota.put("edad", edad);

            db.collection("usuarios").document(email)
                    .set(mascota, SetOptions.merge())
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Datos de la mascota registrados", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error al guardar datos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        btnCancelar.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegisterPaso3Activity.class);
            intent.putExtra("email", email);
            intent.putExtra("nombreMascota", nombreMascota);
            intent.putExtra("especie", especie);
            intent.putExtra("raza", raza);
            intent.putExtra("peso", peso);
            startActivity(intent);
            finish();
        });
    }
}



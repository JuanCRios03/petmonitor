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

import co.edu.unipiloto.petmonitor.R;

public class RegisterPaso3Activity extends AppCompatActivity {

    EditText etNombreMascota, etEspecie, etRaza, etPeso;
    Button btnGuardarMascota;
    FirebaseFirestore db;
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

        db = FirebaseFirestore.getInstance();

        // Obtener el email desde la actividad anterior
        email = getIntent().getStringExtra("email");

        btnGuardarMascota.setOnClickListener(v -> {
            String nombre = etNombreMascota.getText().toString().trim();
            String especie = etEspecie.getText().toString().trim();
            String raza = etRaza.getText().toString().trim();
            String peso = etPeso.getText().toString().trim();

            if (nombre.isEmpty() || especie.isEmpty() || raza.isEmpty() || peso.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> mascota = new HashMap<>();
            mascota.put("nombreMascota", nombre);
            mascota.put("especie", especie);
            mascota.put("raza", raza);
            mascota.put("peso", peso);

            // Guardar los datos de la mascota dentro del documento del usuario
            db.collection("usuarios")
                    .document(email)
                    .set(mascota, SetOptions.merge()) // Merge para no borrar los datos anteriores
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Mascota guardada", Toast.LENGTH_SHORT).show();

                        // Redirigir a la siguiente pantalla
                        Intent intent = new Intent(this, RegisterPaso4Activity.class);
                        intent.putExtra("email", email); // Por si necesitas seguir usando el correo
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error al guardar en la base de datos", Toast.LENGTH_SHORT).show();
                    });
        });
    }
}



package co.edu.unipiloto.petmonitor.Register;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import co.edu.unipiloto.petmonitor.Login.LoginActivity;
import co.edu.unipiloto.petmonitor.R;

public class RegisterPaso4Activity extends AppCompatActivity {

    EditText spinnerEdad;
    Button btnAgregarEdad, btnCancelar;
    FirebaseFirestore db;

    String nombreMascota, especie, raza, peso;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_paso4);

        spinnerEdad = findViewById(R.id.spinnerEdad);
        btnAgregarEdad = findViewById(R.id.btnAgregarEdad);
        btnCancelar = findViewById(R.id.btnCancelar);
        db = FirebaseFirestore.getInstance();

        nombreMascota = getIntent().getStringExtra("nombreMascota");
        especie = getIntent().getStringExtra("especie");
        raza = getIntent().getStringExtra("raza");
        peso = getIntent().getStringExtra("peso");

        btnAgregarEdad.setOnClickListener(v -> {
            String edad = spinnerEdad.getText().toString().trim();

            if (edad.isEmpty()) {
                Toast.makeText(this, "Por favor, ingresa la edad", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser == null) {
                Toast.makeText(this, "Debes iniciar sesión primero", Toast.LENGTH_SHORT).show();
                return;
            }

            String uid = currentUser.getUid();

            Map<String, Object> mascota = new HashMap<>();
            mascota.put("nombreMascota", nombreMascota);
            mascota.put("especie", especie);
            mascota.put("raza", raza);
            mascota.put("peso", peso);
            mascota.put("edad", edad);

            db.collection("usuarios")
                    .document(uid)
                    .collection("mascotas")
                    .add(mascota)
                    .addOnSuccessListener(documentReference -> {
                        Log.d("RegisterPaso4", "Mascota registrada con ID: " + documentReference.getId());
                        Toast.makeText(this, "Mascota registrada exitosamente", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, LoginActivity.class));
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Log.e("RegisterPaso4", "Error al guardar datos", e);
                        Toast.makeText(this, "Error al guardar datos: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        btnCancelar.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegisterPaso3Activity.class);
            intent.putExtra("nombreMascota", nombreMascota);
            intent.putExtra("especie", especie);
            intent.putExtra("raza", raza);
            intent.putExtra("peso", peso);
            startActivity(intent);
            finish();
        });
    }
}





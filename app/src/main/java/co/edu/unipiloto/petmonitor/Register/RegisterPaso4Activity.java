package co.edu.unipiloto.petmonitor.Register;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import co.edu.unipiloto.petmonitor.Login.LoginActivity;
import co.edu.unipiloto.petmonitor.R;

public class RegisterPaso4Activity extends AppCompatActivity {

    EditText spinnerEdad; // Ahora es un EditText
    Button btnAgregarEdad, btnCancelar;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_paso4);

        spinnerEdad = findViewById(R.id.spinnerEdad); // EditText
        btnAgregarEdad = findViewById(R.id.btnAgregarEdad);
        btnCancelar = findViewById(R.id.btnCancelar);
        db = FirebaseFirestore.getInstance();

        String email = getIntent().getStringExtra("email");

        btnAgregarEdad.setOnClickListener(v -> {
            String edad = spinnerEdad.getText().toString().trim();

            if (edad.isEmpty()) {
                Toast.makeText(this, "Por favor, ingresa la edad", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> data = new HashMap<>();
            data.put("edad", edad);

            db.collection("usuarios").document(email)
                    .update(data)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Edad registrada exitosamente", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error al guardar edad: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        btnCancelar.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegisterPaso3Activity.class);
            intent.putExtra("email", email);
            startActivity(intent);
            finish();
        });
    }
}


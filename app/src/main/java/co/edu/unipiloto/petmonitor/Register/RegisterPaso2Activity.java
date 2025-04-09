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

import co.edu.unipiloto.petmonitor.R;

public class RegisterPaso2Activity extends AppCompatActivity {

    EditText etPassword, etConfirmPassword;
    Button btnFinalizarRegistro;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_paso2);

        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnFinalizarRegistro = findViewById(R.id.btnFinalizarRegistro);
        db = FirebaseFirestore.getInstance();

        // Datos recibidos del paso anterior
        String nombre = getIntent().getStringExtra("nombre");
        String apellido = getIntent().getStringExtra("apellido");
        String email = getIntent().getStringExtra("email");

        btnFinalizarRegistro.setOnClickListener(v -> {
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            if (password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Por favor llena todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
                return;
            }

            // Guardar en Firestore
            Map<String, Object> user = new HashMap<>();
            user.put("nombre", nombre);
            user.put("apellido", apellido);
            user.put("email", email);
            user.put("password", password); // en la vida real se debería encriptar

            db.collection("usuarios").document(email)
                    .set(user)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Usuario registrado exitosamente", Toast.LENGTH_SHORT).show();

                        // Redirigir al paso 3 pasando el correo
                        Intent intent = new Intent(this, RegisterPaso3Activity.class);
                        intent.putExtra("email", email); // <--- aquí se pasa el email correctamente
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error al registrar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
    }
}

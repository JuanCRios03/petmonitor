package co.edu.unipiloto.petmonitor.Register;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import co.edu.unipiloto.petmonitor.R;

public class RegisterPaso1Activity extends AppCompatActivity {

    EditText etNombre, etApellido, etEmail;
    Button btnSiguiente;

    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_paso1);

        etNombre = findViewById(R.id.etNombre);
        etApellido = findViewById(R.id.etApellido);
        etEmail = findViewById(R.id.etEmail);
        btnSiguiente = findViewById(R.id.btnSiguiente);

        db = FirebaseFirestore.getInstance();

        btnSiguiente.setOnClickListener(v -> {
            String nombre = etNombre.getText().toString().trim();
            String apellido = etApellido.getText().toString().trim();
            String email = etEmail.getText().toString().trim();

            if (nombre.isEmpty() || apellido.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            // Revisar si ya existe un usuario con ese email
            db.collection("usuarios")
                    .document(email)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Toast.makeText(this, "Este correo ya está registrado", Toast.LENGTH_SHORT).show();
                        } else {
                            // Crear mapa con los datos
                            Map<String, Object> usuario = new HashMap<>();
                            usuario.put("nombre", nombre);
                            usuario.put("apellido", apellido);
                            usuario.put("email", email);

                            // Guardar en Firestore usando el email como ID único
                            db.collection("usuarios")
                                    .document(email)
                                    .set(usuario)
                                    .addOnSuccessListener(unused -> {
                                        // Ir a siguiente pantalla
                                        Intent intent = new Intent(this, RegisterPaso2Activity.class);
                                        intent.putExtra("nombre", nombre);
                                        intent.putExtra("apellido", apellido);
                                        intent.putExtra("email", email);
                                        startActivity(intent);
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Error al guardar en la base de datos", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error al verificar el correo", Toast.LENGTH_SHORT).show();
                    });
        });
    }
}






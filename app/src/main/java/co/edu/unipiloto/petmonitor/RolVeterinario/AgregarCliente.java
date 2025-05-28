package co.edu.unipiloto.petmonitor.RolVeterinario;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

import co.edu.unipiloto.petmonitor.R;

public class AgregarCliente extends AppCompatActivity {

    EditText nombreCliente;
    EditText correoCliente;
    Button guardarCliente;
    private FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_agregar_cliente);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        nombreCliente = findViewById(R.id.etNombreCliente);
        correoCliente = findViewById(R.id.etCorreoCliente);
        guardarCliente = findViewById(R.id.btnGuardarCliente);
        db = FirebaseFirestore.getInstance();
        guardarCliente.setOnClickListener(v -> {
            String nombre = nombreCliente.getText().toString().trim();
            String correo = correoCliente.getText().toString().trim();

            if (nombre.isEmpty() || correo.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser == null) {
                Toast.makeText(this, "Debes iniciar sesión primero", Toast.LENGTH_SHORT).show();
                return;
            }

            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // Buscar en la colección "usuarios" por email
            db.collection("usuarios")
                    .whereEqualTo("email", correo)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            QuerySnapshot result = task.getResult();
                            if (!result.isEmpty()) {
                                for (QueryDocumentSnapshot document : result) {
                                    String clienteId = document.getId();

                                    // Añadir a la subcolección "clientes" del usuario actual
                                    Map<String, Object> clienteData = new HashMap<>();
                                    clienteData.put("clienteId", clienteId);
                                    clienteData.put("nombre", nombre);

                                    db.collection("usuarios")
                                            .document(currentUser.getUid())
                                            .collection("clientes")
                                            .document(clienteId)
                                            .set(clienteData)
                                            .addOnSuccessListener(aVoid -> {
                                                Toast.makeText(this, "Cliente agregado exitosamente", Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(this, MisClientes.class));
                                                finish();
                                            })
                                            .addOnFailureListener(e ->
                                                    Toast.makeText(this, "Error al guardar el cliente", Toast.LENGTH_SHORT).show()
                                            );
                                    break; // Salir después del primer match
                                }
                            } else {
                                Toast.makeText(this, "No se encontró un usuario con ese correo", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(this, "Error al buscar en la base de datos", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

    }
}
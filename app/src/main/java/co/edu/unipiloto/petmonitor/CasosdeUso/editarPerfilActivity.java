package co.edu.unipiloto.petmonitor.CasosdeUso;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import co.edu.unipiloto.petmonitor.R;

public class editarPerfilActivity extends AppCompatActivity {

    private EditText etEdad, etPeso;
    private ImageView ivMascota;
    private Button btnGuardar, btnRestablecerContrasena;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private String userId;
    private String mascotaId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_perfil);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        userId = user.getUid();

        mascotaId = getIntent().getStringExtra("mascotaId");
        if (mascotaId == null || mascotaId.isEmpty()) {
            Toast.makeText(this, "ID de mascota no proporcionado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        etEdad = findViewById(R.id.etEdad);
        etPeso = findViewById(R.id.etPeso);
        ivMascota = findViewById(R.id.ivMascota);
        btnGuardar = findViewById(R.id.btnGuardarCambios);
        btnRestablecerContrasena = findViewById(R.id.btnRestablecerContrasena);

        cargarDatos();

        btnGuardar.setOnClickListener(v -> mostrarDialogoConfirmacion());

        btnRestablecerContrasena.setOnClickListener(v -> enviarCorreoRestablecimiento());
    }

    private void cargarDatos() {
        DocumentReference userRef = db.collection("usuarios").document(userId);

        DocumentReference mascotaRef = userRef.collection("mascotas").document(mascotaId);
        mascotaRef.get().addOnSuccessListener(snapshot -> {
            if (snapshot.exists()) {
                String edad = snapshot.getString("edad");
                String peso = snapshot.getString("peso");
                String imagenBase64 = snapshot.getString("imagenBase64");

                etEdad.setText(edad);
                etPeso.setText(peso);

                if (imagenBase64 != null && !imagenBase64.isEmpty()) {
                    byte[] imageBytes = android.util.Base64.decode(imagenBase64, android.util.Base64.DEFAULT);
                    android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                    ivMascota.setImageBitmap(bitmap);
                }
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error al cargar datos de la mascota", Toast.LENGTH_SHORT).show();
        });
    }

    private void enviarCorreoRestablecimiento() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String email = user.getEmail();
            if (email != null && !email.isEmpty()) {
                mAuth.sendPasswordResetEmail(email)
                        .addOnSuccessListener(aVoid -> Toast.makeText(this, "Correo de restablecimiento enviado a: " + email, Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(this, "Error al enviar el correo de restablecimiento", Toast.LENGTH_SHORT).show());
            } else {
                Toast.makeText(this, "No se encontró un correo electrónico asociado a esta cuenta", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void mostrarDialogoConfirmacion() {
        // Inflar el diseño del diálogo personalizado
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.dialog_confirmacion, null);

        // Crear el diálogo
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);

        // Configurar acciones para el diálogo
        builder.setPositiveButton("Sí", (dialog, which) -> {
            guardarCambios(); // Llamar al método para guardar los cambios
            dialog.dismiss();
        });

        builder.setNegativeButton("No", (dialog, which) -> {
            dialog.dismiss(); // Cerrar el diálogo si el usuario cancela
        });

        // Mostrar el diálogo
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void guardarCambios() {
        String nuevaEdad = etEdad.getText().toString().trim();
        String nuevoPeso = etPeso.getText().toString().trim();

        if (nuevaEdad.isEmpty() || nuevoPeso.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> datos = new HashMap<>();
        datos.put("edad", nuevaEdad);
        datos.put("peso", nuevoPeso);

        DocumentReference userRef = db.collection("usuarios").document(userId);

        userRef.collection("mascotas").document(mascotaId)
                .update(datos)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Datos actualizados correctamente", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al actualizar datos", Toast.LENGTH_SHORT).show();
                });
    }
}

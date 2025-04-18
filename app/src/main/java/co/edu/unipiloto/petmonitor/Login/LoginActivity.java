package co.edu.unipiloto.petmonitor.Login;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import co.edu.unipiloto.petmonitor.R;
import co.edu.unipiloto.petmonitor.Menu.menuActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText etGmail, etPassword;
    private Button btnLogin, btnBack;

    // Instancia de Firebase Firestore
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etGmail = findViewById(R.id.etGmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnBack = findViewById(R.id.btnBack);

        // Inicializar Firestore
        db = FirebaseFirestore.getInstance();

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etGmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty()) {
                    showToast("Por favor, completa todos los campos.");
                    return;
                }

                validateCredentials(email, password);
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Regresa a la actividad anterior
            }
        });
    }

    private void validateCredentials(String email, String password) {
        db.collection("usuarios").document(email).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String storedPassword = document.getString("password");
                            if (storedPassword != null && storedPassword.equals(password)) {
                                // Guardar el correo en SharedPreferences
                                saveEmailToPreferences(email);

                                // Credenciales correctas
                                showToast("Inicio de sesión exitoso.");
                                navigateToContinuation();
                            } else {
                                // Contraseña incorrecta
                                showToast("Contraseña incorrecta. Por favor, inténtalo de nuevo.");
                            }
                        } else {
                            // Usuario no encontrado
                            showToast("El correo ingresado no está registrado.");
                        }
                    } else {
                        Log.e("FirebaseError", "Error al acceder a la base de datos", task.getException());
                        showToast("Error al conectar con la base de datos. Intenta nuevamente.");
                    }
                });
    }

    private void saveEmailToPreferences(String email) {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("email", email);
        editor.apply();  // Guardar los cambios
    }

    private void navigateToContinuation() {
        Intent intent = new Intent(LoginActivity.this, menuActivity.class);
        startActivity(intent);
    }

    private void showToast(String message) {
        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
    }
}



package co.edu.unipiloto.petmonitor.Login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import co.edu.unipiloto.petmonitor.Menu.menuActivity;
import co.edu.unipiloto.petmonitor.R;

public class LoginActivity extends AppCompatActivity {

    private EditText etGmail, etPassword;
    private Button btnLogin, btnBack;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etGmail = findViewById(R.id.etGmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnBack = findViewById(R.id.btnBack);

        db = FirebaseFirestore.getInstance();

        btnLogin.setOnClickListener(v -> {
            String email = etGmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                showToast("Por favor, completa todos los campos.");
                return;
            }

            loginUser(email, password);
        });

        btnBack.setOnClickListener(v -> finish());
    }

    private void loginUser(String email, String password) {
        db.collection("usuarios").document(email).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String storedPassword = document.getString("password");
                            if (storedPassword != null && storedPassword.equals(password)) {
                                saveUserSession(email);
                                showToast("Inicio de sesión exitoso.");
                                startActivity(new Intent(this, menuActivity.class));
                                finish();
                            } else {
                                showToast("Contraseña incorrecta.");
                            }
                        } else {
                            showToast("Usuario no registrado.");
                        }
                    } else {
                        Log.e("LoginError", "Error detallado: ", task.getException());
                        showToast("Error al conectar: " + task.getException().getMessage());
                    }
                });
    }

    private void saveUserSession(String email) {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("email", email);
        editor.apply();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}



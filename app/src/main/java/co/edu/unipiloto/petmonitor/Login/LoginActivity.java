package co.edu.unipiloto.petmonitor.Login;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import co.edu.unipiloto.petmonitor.MisMascotas;
import co.edu.unipiloto.petmonitor.R;

public class LoginActivity extends AppCompatActivity {

    private EditText etGmail, etPassword;
    private Button btnLogin, btnBack;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Verifica si ya hay un usuario logueado
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            // El usuario ya inició sesión previamente, lo llevamos directo al menú principal
            startActivity(new Intent(this, MisMascotas.class));
            finish(); // Cerramos LoginActivity para que no vuelva atrás
            return;
        }

        // Si no está logueado, mostramos el login
        setContentView(R.layout.activity_login);

        etGmail = findViewById(R.id.etGmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnBack = findViewById(R.id.btnBack);

        auth = FirebaseAuth.getInstance();
        final boolean[] isPasswordVisible = {false};
        final boolean[] isConfirmVisible = {false};

        etPassword.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                int drawableEnd = etPassword.getCompoundDrawables()[2].getBounds().width();
                if (event.getRawX() >= (etPassword.getRight() - drawableEnd)) {
                    togglePasswordVisibility(etPassword, isPasswordVisible, R.drawable.ic_eye_open, R.drawable.ic_eye_closed);
                    return true;
                }
            }
            return false;
        });

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
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            saveUserSession(user.getEmail());
                            showToast("Inicio de sesión exitoso.");
                            startActivity(new Intent(this, MisMascotas.class));
                            finish();
                        }
                    } else {
                        Log.e("LoginError", "Error: ", task.getException());
                        showToast("Error de autenticación: " + task.getException().getMessage());
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
    private void togglePasswordVisibility(EditText editText, boolean[] isVisible, int iconVisible, int iconInvisible) {
        int selection = editText.getSelectionEnd();
        if (isVisible[0]) {
            editText.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
            editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, iconInvisible, 0);
        } else {
            editText.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, iconVisible, 0);
        }
        isVisible[0] = !isVisible[0];
        editText.setSelection(selection);

        editText.setTypeface(ResourcesCompat.getFont(this, R.font.titulos));
    }
}






package co.edu.unipiloto.petmonitor.Register;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import co.edu.unipiloto.petmonitor.R;

public class RegisterPaso2Activity extends AppCompatActivity {

    EditText etPassword, etConfirmPassword;
    Button btnFinalizarRegistro;
    CheckBox isTheUserVetCheckBox;
    boolean isTheUserVet;
    FirebaseAuth auth;
    FirebaseFirestore db;

    String nombre, apellido, email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_paso2);

        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnFinalizarRegistro = findViewById(R.id.btnFinalizarRegistro);
        isTheUserVetCheckBox = findViewById(R.id.isTheUserVet);
        isTheUserVet = false;


        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        nombre = getIntent().getStringExtra("nombre");
        apellido = getIntent().getStringExtra("apellido");
        email = getIntent().getStringExtra("email");

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

        etConfirmPassword.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                int drawableEnd = etConfirmPassword.getCompoundDrawables()[2].getBounds().width();
                if (event.getRawX() >= (etConfirmPassword.getRight() - drawableEnd)) {
                    togglePasswordVisibility(etConfirmPassword, isConfirmVisible, R.drawable.ic_eye_open, R.drawable.ic_eye_closed);
                    return true;
                }
            }
            return false;
        });

        btnFinalizarRegistro.setOnClickListener(v -> {
            String password = etPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            if (password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
                return;
            }

            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            String uid = auth.getCurrentUser().getUid();

                            Map<String, Object> usuario = new HashMap<>();
                            usuario.put("nombre", nombre);
                            usuario.put("apellido", apellido);
                            usuario.put("email", email);
                            usuario.put("esVeterinario", isTheUserVet);

                            db.collection("usuarios")
                                    .document(uid)
                                    .set(usuario)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(this, "Usuario registrado exitosamente", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(this, RegisterPaso3Activity.class));
                                        finish();
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(this, "Error al guardar datos: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        } else {
                            Toast.makeText(this, "Error al registrar usuario: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });

        isTheUserVetCheckBox.setOnClickListener((v) -> {
            isTheUserVet = !isTheUserVet;
            if(isTheUserVet)
                Toast.makeText(this, "El usuario es veterinario", Toast.LENGTH_LONG).show();
            else
                Toast.makeText(this, "El usuario no es veterinario", Toast.LENGTH_LONG).show();
        });
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



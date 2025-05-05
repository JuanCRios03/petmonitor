package co.edu.unipiloto.petmonitor.Register;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import co.edu.unipiloto.petmonitor.Login.LoginActivity;
import co.edu.unipiloto.petmonitor.R;

public class RegisterPaso1Activity extends AppCompatActivity {

    EditText etNombre, etApellido, etEmail;
    Button btnSiguiente;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_paso1);

        etNombre = findViewById(R.id.etNombre);
        etApellido = findViewById(R.id.etApellido);
        etEmail = findViewById(R.id.etEmail);
        btnSiguiente = findViewById(R.id.btnSiguiente);

        btnSiguiente.setOnClickListener(v -> {
            String nombre = etNombre.getText().toString().trim();
            String apellido = etApellido.getText().toString().trim();
            String email = etEmail.getText().toString().trim();

            if (nombre.isEmpty() || apellido.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            // Pasar los datos al paso 2
            Intent intent = new Intent(this, RegisterPaso2Activity.class);
            intent.putExtra("nombre", nombre);
            intent.putExtra("apellido", apellido);
            intent.putExtra("email", email);
            startActivity(intent);
        });
        TextView tvGoToRegister = findViewById(R.id.tvGoToLogin);
        tvGoToRegister.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterPaso1Activity.this, LoginActivity.class);
            startActivity(intent);
        });

    }
}







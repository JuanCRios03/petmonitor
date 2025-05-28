package co.edu.unipiloto.petmonitor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import co.edu.unipiloto.petmonitor.RolVeterinario.MenuVeterinario;
import co.edu.unipiloto.petmonitor.RolVeterinario.MisClientes;
import co.edu.unipiloto.petmonitor.Login.LoginActivity;
import co.edu.unipiloto.petmonitor.Menu.MisMascotas;
import co.edu.unipiloto.petmonitor.Menu.menuActivity;
import co.edu.unipiloto.petmonitor.Register.RegisterPaso1Activity;

public class MainActivity extends AppCompatActivity {

    private Button btnIniciarSesion, btnRegistrarme;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();


        if (currentUser != null) {
            // El usuario ya está autenticado, revisar la última actividad
            SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            String lastActivity = prefs.getString("lastActivity", "MisMascotas");

            Intent intent;

            switch (lastActivity) {
                case "MisClientes":
                    intent = new Intent(this, MisClientes.class);
                    break;
                case "MisMascotas":
                    intent = new Intent(this, MisMascotas.class);
                    break;
                case "menuActivity":
                    intent = new Intent(this, menuActivity.class);
                    break;
                case "MenuVeterinario":
                    intent = new Intent(this, MenuVeterinario.class);
                    break;
                // Agrega más pantallas si deseas que puedan ser restauradas
                default:
                    intent = new Intent(this, MisMascotas.class);
                    break;


            }

            startActivity(intent);
            finish(); // Cerramos MainActivity para que no quede en el backstack
            return;
        }

        // Si no ha iniciado sesión, mostramos la pantalla de login/registro
        setContentView(R.layout.activity_main);

        btnIniciarSesion = findViewById(R.id.btnIniciarSesion);
        btnRegistrarme = findViewById(R.id.btnRegistrarme);

        btnIniciarSesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
            }
        });

        btnRegistrarme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, RegisterPaso1Activity.class));
            }
        });
    }

}


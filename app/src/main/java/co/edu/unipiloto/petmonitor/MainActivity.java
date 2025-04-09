package co.edu.unipiloto.petmonitor;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import co.edu.unipiloto.petmonitor.Login.LoginActivity;
import co.edu.unipiloto.petmonitor.Register.RegisterPaso1Activity;

public class MainActivity extends AppCompatActivity {

    Button btnIniciarSesion, btnRegistrarme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

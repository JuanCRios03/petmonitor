package co.edu.unipiloto.petmonitor.CasosdeUso;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import co.edu.unipiloto.petmonitor.Menu.menuActivity;
import co.edu.unipiloto.petmonitor.R;

public class buscarVeterinariasActivity  extends AppCompatActivity {
    private Button btnVerMapa, btnHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_veterinarios1);

        btnVerMapa = findViewById(R.id.btnVerMapa);
        btnHome = findViewById(R.id.btnHome);

        findViewById(R.id.btnVerMapa).setOnClickListener(view -> {
            Intent intent = new Intent(buscarVeterinariasActivity.this, VeterinariosActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.btnHome).setOnClickListener(view -> {
            Intent intent = new Intent(buscarVeterinariasActivity.this, menuActivity.class);
            startActivity(intent);
        });
    }
}

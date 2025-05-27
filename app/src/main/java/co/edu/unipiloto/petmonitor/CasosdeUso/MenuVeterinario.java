package co.edu.unipiloto.petmonitor.CasosdeUso;

import android.content.Intent;
import android.os.Bundle;
import android.widget.RelativeLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import co.edu.unipiloto.petmonitor.Login.LoginActivity;
import co.edu.unipiloto.petmonitor.R;

public class MenuVeterinario extends AppCompatActivity {

    private RelativeLayout btnRegisterVaccines, btnHistorialVaccines, btnlogout, btnRegistrarTratamiento, btnHistorialTratamiento, btnSaludMascota,btnSaludHistoriaMascota;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_menu_veterinario);

        btnRegisterVaccines = findViewById(R.id.btnRegisterVaccines);
        btnRegisterVaccines.setOnClickListener(v -> {
            Intent intent = new Intent(MenuVeterinario.this, RegistrarVacunaActivity.class);
        });

        btnHistorialVaccines = findViewById(R.id.btnHistorialVaccines);
        btnHistorialVaccines.setOnClickListener(v -> {
            Intent intent = new Intent(MenuVeterinario.this, HistorialVacunasActivity.class);
        });


        btnRegistrarTratamiento = findViewById(R.id.btnRegistrarTratamiento);
        btnRegistrarTratamiento.setOnClickListener(v -> {
            Intent intent = new Intent(MenuVeterinario.this, RegistrarTratamientoActivity.class);
        });

        btnHistorialTratamiento = findViewById(R.id.btnHistorialTratamiento);
        btnHistorialTratamiento.setOnClickListener(v -> {
            Intent intent = new Intent(MenuVeterinario.this, HistorialTratamientosActivity.class);
        });
        btnSaludMascota = findViewById(R.id.btnSaludMascota);
        btnSaludMascota.setOnClickListener(v -> {
            Intent intent = new Intent(MenuVeterinario.this, PetHealthProfileActivity.class);
            //intent.putExtra("mascotaId", mascotaId);
            //startActivity(intent);
        });


        btnlogout = findViewById(R.id.btnlogout);
        btnlogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut(); // Cierra la sesión
            Intent intent = new Intent(MenuVeterinario.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Limpia el back stack
            startActivity(intent);
            finish();
        });

    }
}
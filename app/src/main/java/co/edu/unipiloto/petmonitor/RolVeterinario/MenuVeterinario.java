package co.edu.unipiloto.petmonitor.RolVeterinario;

import android.content.Intent;
import android.os.Bundle;
import android.widget.RelativeLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

import co.edu.unipiloto.petmonitor.CasosdeUso.HistorialTratamientosActivity;
import co.edu.unipiloto.petmonitor.CasosdeUso.HistorialVacunasActivity;
import co.edu.unipiloto.petmonitor.CasosdeUso.PetHealthProfileActivity;
import co.edu.unipiloto.petmonitor.CasosdeUso.RegistrarTratamientoActivity;
import co.edu.unipiloto.petmonitor.CasosdeUso.RegistrarVacunaActivity;
import co.edu.unipiloto.petmonitor.Login.LoginActivity;
import co.edu.unipiloto.petmonitor.R;

public class MenuVeterinario extends AppCompatActivity {

    private RelativeLayout btnRegisterVaccines, btnHistorialVaccines, btnlogout, btnRegistrarTratamiento, btnHistorialTratamiento, btnSaludMascota, btnSaludHistoriaMascota;

    // Variables para cuando el veterinario está viendo un cliente específico
    private String mascotaId;
    private String clienteEmail;
    private String nombreCliente;
    private String clienteId;
    private boolean esVeterinarioViendoCliente = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_menu_veterinario);

        // Obtener datos del Intent
        Intent intent = getIntent();
        mascotaId = intent.getStringExtra("mascotaId");
        clienteEmail = intent.getStringExtra("clienteEmail");
        nombreCliente = intent.getStringExtra("nombreCliente");
        clienteId = intent.getStringExtra("clienteId");
        esVeterinarioViendoCliente = intent.getBooleanExtra("esVeterinarioViendoCliente", false);

        btnRegisterVaccines = findViewById(R.id.btnRegisterVaccines);
        btnRegisterVaccines.setOnClickListener(v -> {
            Intent intentVacuna = new Intent(MenuVeterinario.this, RegistrarVacunaActivity.class);
            intentVacuna.putExtra("esVeterinario", true);

            // Si estamos viendo un cliente específico, pasar los datos
            if (esVeterinarioViendoCliente) {
                intentVacuna.putExtra("mascotaId", mascotaId);
                intentVacuna.putExtra("clienteEmail", clienteEmail);
                intentVacuna.putExtra("nombreCliente", nombreCliente);
                intentVacuna.putExtra("clienteId", clienteId);
                intentVacuna.putExtra("esVeterinarioViendoCliente", true);
            }

            startActivity(intentVacuna);
        });

        btnHistorialVaccines = findViewById(R.id.btnHistorialVaccines);
        btnHistorialVaccines.setOnClickListener(v -> {
            Intent intentHistorial = new Intent(MenuVeterinario.this, HistorialVacunasActivity.class);
            intentHistorial.putExtra("esVeterinario", true);

            // Si estamos viendo un cliente específico, pasar los datos
            if (esVeterinarioViendoCliente) {
                intentHistorial.putExtra("mascotaId", mascotaId);
                intentHistorial.putExtra("clienteEmail", clienteEmail);
                intentHistorial.putExtra("nombreCliente", nombreCliente);
                intentHistorial.putExtra("clienteId", clienteId);
                intentHistorial.putExtra("esVeterinarioViendoCliente", true);
            }

            startActivity(intentHistorial);
        });

        btnRegistrarTratamiento = findViewById(R.id.btnRegistrarTratamiento);
        btnRegistrarTratamiento.setOnClickListener(v -> {
            Intent intentTratamiento = new Intent(MenuVeterinario.this, RegistrarTratamientoActivity.class);
            intentTratamiento.putExtra("esVeterinario", true);

            // Si estamos viendo un cliente específico, pasar los datos
            if (esVeterinarioViendoCliente) {
                intentTratamiento.putExtra("mascotaId", mascotaId);
                intentTratamiento.putExtra("clienteEmail", clienteEmail);
                intentTratamiento.putExtra("nombreCliente", nombreCliente);
                intentTratamiento.putExtra("clienteId", clienteId);
                intentTratamiento.putExtra("esVeterinarioViendoCliente", true);
            }

            startActivity(intentTratamiento);
        });

        btnHistorialTratamiento = findViewById(R.id.btnHistorialTratamiento);
        btnHistorialTratamiento.setOnClickListener(v -> {
            Intent intentHistorialTratamiento = new Intent(MenuVeterinario.this, HistorialTratamientosActivity.class);
            intentHistorialTratamiento.putExtra("esVeterinario", true);

            // Si estamos viendo un cliente específico, pasar los datos
            if (esVeterinarioViendoCliente) {
                intentHistorialTratamiento.putExtra("mascotaId", mascotaId);
                intentHistorialTratamiento.putExtra("clienteEmail", clienteEmail);
                intentHistorialTratamiento.putExtra("nombreCliente", nombreCliente);
                intentHistorialTratamiento.putExtra("clienteId", clienteId);
                intentHistorialTratamiento.putExtra("esVeterinarioViendoCliente", true);
            }

            startActivity(intentHistorialTratamiento);
        });

        btnSaludMascota = findViewById(R.id.btnSaludMascota);
        btnSaludMascota.setOnClickListener(v -> {
            Intent intentSalud = new Intent(MenuVeterinario.this, PetHealthProfileActivity.class);
            intentSalud.putExtra("esVeterinario", true);

            // Si estamos viendo un cliente específico, pasar los datos
            if (esVeterinarioViendoCliente) {
                intentSalud.putExtra("mascotaId", mascotaId);
                intentSalud.putExtra("clienteEmail", clienteEmail);
                intentSalud.putExtra("nombreCliente", nombreCliente);
                intentSalud.putExtra("clienteId", clienteId);
                intentSalud.putExtra("esVeterinarioViendoCliente", true);
            }

            startActivity(intentSalud);
        });

        btnlogout = findViewById(R.id.btnlogout);
        btnlogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut(); // Cierra la sesión
            Intent intentLogout = new Intent(MenuVeterinario.this, LoginActivity.class);
            intentLogout.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Limpia el back stack
            startActivity(intentLogout);
            finish();
        });
    }
}


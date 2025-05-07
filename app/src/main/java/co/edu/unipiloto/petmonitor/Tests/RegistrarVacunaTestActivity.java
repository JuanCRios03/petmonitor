package co.edu.unipiloto.petmonitor.Tests;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;
import co.edu.unipiloto.petmonitor.R;

public class RegistrarVacunaTestActivity extends AppCompatActivity {

    private EditText fechaVacunacion, tipoVacuna, dosis, lote, veterinario, observaciones;
    private Spinner spinnerMascotas;
    private Button btnGuardar;
    private FirebaseFirestore db;

    private final String usuarioId = "Juanprueba@gmail.com"; // Usuario de prueba
    private final String mascotaId = "Mascota_Prueba";        // Mascota de prueba

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar_vacuna); // Usa el mismo layout

        fechaVacunacion = findViewById(R.id.fechaVacunacion);
        tipoVacuna = findViewById(R.id.tipoVacuna);
        dosis = findViewById(R.id.dosis);
        lote = findViewById(R.id.lote);
        veterinario = findViewById(R.id.veterinario);
        observaciones = findViewById(R.id.observaciones);
        spinnerMascotas = findViewById(R.id.spinnerMascotas);
        btnGuardar = findViewById(R.id.btnGuardar);

        db = FirebaseFirestore.getInstance();

        // Precargar valores simulados
        fechaVacunacion.setText("2024-05-01");
        tipoVacuna.setText("Rabia");
        dosis.setText("1");
        lote.setText("ABC123");
        veterinario.setText("Dr. Prueba");
        observaciones.setText("Sin efectos secundarios");

        btnGuardar.setOnClickListener(v -> guardarVacuna());
    }

    private void guardarVacuna() {
        String fecha = fechaVacunacion.getText().toString().trim();
        String tipo = tipoVacuna.getText().toString().trim();
        String d = dosis.getText().toString().trim();
        String l = lote.getText().toString().trim();
        String vet = veterinario.getText().toString().trim();
        String obs = observaciones.getText().toString().trim();

        if (fecha.isEmpty() || tipo.isEmpty() || d.isEmpty() || l.isEmpty() || vet.isEmpty()) {
            Toast.makeText(this, "Por favor completa todos los campos obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> vacuna = new HashMap<>();
        vacuna.put("fechaVacunacion", fecha);
        vacuna.put("tipoVacuna", tipo);
        vacuna.put("dosis", d);
        vacuna.put("lote", l);
        vacuna.put("veterinario", vet);
        vacuna.put("observaciones", obs);

        db.collection("usuarios")
                .document(usuarioId)
                .collection("mascotas")
                .document(mascotaId)
                .collection("vacunas")
                .add(vacuna)
                .addOnSuccessListener(documentReference ->
                        Toast.makeText(this, "Vacuna registrada correctamente", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al registrar la vacuna", Toast.LENGTH_SHORT).show());
    }
}


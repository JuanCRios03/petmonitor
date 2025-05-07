package co.edu.unipiloto.petmonitor.Tests;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;
import co.edu.unipiloto.petmonitor.R;

public class zonaSeguraTestActivity extends AppCompatActivity {

    private EditText etLatitud, etLongitud, etRadio;
    private Button btnGuardar;
    private FirebaseFirestore db;

    private String usuarioId = "Juanprueba@gmail.com"; // Usuario de prueba
    private String mascotaId = "Mascota_Prueba";        // Mascota de prueba

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zonasegura_test); // Usa el mismo layout de zonaSeguraActivity

        etLatitud = findViewById(R.id.etLatitude);
        etLongitud = findViewById(R.id.etLongitude);
        etRadio = findViewById(R.id.etRadius);
        btnGuardar = findViewById(R.id.btnGuardar);

        db = FirebaseFirestore.getInstance();

        // Puedes precargar valores simulados
        etLatitud.setText("4.60971");
        etLongitud.setText("-74.08175");
        etRadio.setText("100");

        btnGuardar.setOnClickListener(v -> guardarZonaSegura());
    }

    private void guardarZonaSegura() {
        String latitudStr = etLatitud.getText().toString().trim();
        String longitudStr = etLongitud.getText().toString().trim();
        String radioStr = etRadio.getText().toString().trim();

        if (latitudStr.isEmpty() || longitudStr.isEmpty() || radioStr.isEmpty()) {
            Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        double latitud = Double.parseDouble(latitudStr);
        double longitud = Double.parseDouble(longitudStr);
        double radio = Double.parseDouble(radioStr);

        Map<String, Object> zonaSegura = new HashMap<>();
        zonaSegura.put("latitud", latitud);
        zonaSegura.put("longitud", longitud);
        zonaSegura.put("radio", radio);

        db.collection("usuarios")
                .document(usuarioId)
                .collection("mascotas")
                .document(mascotaId)
                .collection("zonaSegura")
                .document("zona") // puedes usar ID fijo para sobreescribir
                .set(zonaSegura)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(this, "Zona segura guardada correctamente", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Error al guardar la zona segura", Toast.LENGTH_SHORT).show());
    }
}


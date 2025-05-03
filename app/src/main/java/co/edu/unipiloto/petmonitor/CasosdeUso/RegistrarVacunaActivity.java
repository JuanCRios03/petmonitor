package co.edu.unipiloto.petmonitor.CasosdeUso;


import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import co.edu.unipiloto.petmonitor.R;
import android.util.Log;
import android.app.DatePickerDialog;
import android.widget.DatePicker;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class RegistrarVacunaActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String userEmail;
    private String nombreMascota;
    private String mascotaId;
    private String userId;

    private EditText fechaVacunacion, tipoVacuna, dosis, lote, veterinario, observaciones;
    private Button btnGuardar;
    private Spinner spinnerMascotas;
    private List<String> listaMascotas;
    private List<String> listaMascotasIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar_vacuna);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        userEmail = auth.getCurrentUser().getEmail();

        fechaVacunacion = findViewById(R.id.fechaVacunacion);
        tipoVacuna = findViewById(R.id.tipoVacuna);
        dosis = findViewById(R.id.dosis);
        lote = findViewById(R.id.lote);
        veterinario = findViewById(R.id.veterinario);
        observaciones = findViewById(R.id.observaciones);
        btnGuardar = findViewById(R.id.btnGuardar);
        spinnerMascotas = findViewById(R.id.spinnerMascotas);

        listaMascotas = new ArrayList<>();
        listaMascotasIds = new ArrayList<>();

        validarUsuarioYcargarDatos();

        // Al hacer clic en el EditText de fecha, se abre el DatePicker
        fechaVacunacion.setOnClickListener(v -> mostrarDatePickerDialog());

        btnGuardar.setOnClickListener(v -> guardarVacuna());
    }

    private void mostrarDatePickerDialog() {
        // Obtener la fecha actual
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Crear el DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        // Formatear la fecha seleccionada
                        Calendar selectedDate = Calendar.getInstance();
                        selectedDate.set(year, monthOfYear, dayOfMonth);

                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                        fechaVacunacion.setText(sdf.format(selectedDate.getTime())); // Mostrar la fecha seleccionada
                    }
                },
                year, month, day
        );
        datePickerDialog.show();
    }

    private void validarUsuarioYcargarDatos() {
        db.collection("usuarios")
                .whereEqualTo("email", userEmail)
                .get()
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        DocumentSnapshot userDoc = query.getDocuments().get(0);
                        userId = userDoc.getId();

                        // Cargar las mascotas del usuario
                        db.collection("usuarios").document(userId).collection("mascotas")
                                .get()
                                .addOnSuccessListener(mascotas -> {
                                    if (!mascotas.isEmpty()) {
                                        for (DocumentSnapshot mascotaDoc : mascotas.getDocuments()) {
                                            String nombre = mascotaDoc.getString("nombreMascota");
                                            String id = mascotaDoc.getId();

                                            listaMascotas.add(nombre);
                                            listaMascotasIds.add(id);
                                        }

                                        // Configurar el Spinner con las mascotas
                                        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listaMascotas);
                                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                        spinnerMascotas.setAdapter(adapter);

                                        // Si hay al menos una mascota seleccionada, selecciona la primera por defecto
                                        spinnerMascotas.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                            @Override
                                            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                                                mascotaId = listaMascotasIds.get(position);  // Obtener el ID de la mascota seleccionada
                                            }

                                            @Override
                                            public void onNothingSelected(AdapterView<?> parentView) {
                                                // Si no se selecciona ninguna mascota, no hacer nada
                                            }
                                        });
                                    } else {
                                        Toast.makeText(this, "No se encontraron mascotas", Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                });
                    } else {
                        Toast.makeText(this, "Usuario no encontrado", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

    private void guardarVacuna() {
        String fecha = fechaVacunacion.getText().toString().trim();
        String tipo = tipoVacuna.getText().toString().trim();
        String d = dosis.getText().toString().trim();
        String l = lote.getText().toString().trim();
        String vet = veterinario.getText().toString().trim();
        String obs = observaciones.getText().toString().trim();

        if (fecha.isEmpty() || tipo.isEmpty() || mascotaId == null) {
            Toast.makeText(this, "Completa todos los campos obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return; // Salir si no hay usuario autenticado
        }

        String nombre = currentUser.getDisplayName();
        if (nombre == null) {
            nombre = "Nombre no disponible"; // Valor por defecto si el nombre no está disponible
        } else {
            nombre = nombre.split(" ")[0]; // Usar solo el primer nombre
        }

        Vacuna vacuna = new Vacuna(
                nombre, // nombre
                "", // apellido (puedes ajustar según tu modelo de usuario)
                userEmail,
                listaMascotas.get(spinnerMascotas.getSelectedItemPosition()), // nombre de la mascota seleccionada
                "", "", "", // especie, raza, peso (si no los necesitas, pueden omitirse)
                fecha,
                tipo,
                d,
                l,
                vet,
                obs
        );

        db.collection("usuarios")
                .document(userId)
                .collection("mascotas")
                .document(mascotaId)
                .collection("vacunas")
                .add(vacuna)
                .addOnSuccessListener(documentReference -> {
                    // Registro exitoso
                    Log.d("RegistroVacuna", "Vacuna registrada con éxito: " + documentReference.getId());
                    Toast.makeText(this, "Vacuna registrada", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    // Error al registrar
                    Log.e("RegistroVacuna", "Error al registrar la vacuna: " + e.getMessage(), e);
                    Toast.makeText(this, "Error al guardar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

}


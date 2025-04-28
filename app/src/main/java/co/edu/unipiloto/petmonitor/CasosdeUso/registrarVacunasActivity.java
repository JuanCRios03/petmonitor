package co.edu.unipiloto.petmonitor.CasosdeUso;


import static android.app.ProgressDialog.show;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

import co.edu.unipiloto.petmonitor.vacuna.HistorialVacunasActivity;
import co.edu.unipiloto.petmonitor.R;
import co.edu.unipiloto.petmonitor.vacuna.Vacuna;

public class registrarVacunasActivity extends AppCompatActivity {

    private static final String TAG = "RegistroVacunaActivity";

    private EditText etNombreMascota, etRazaMascota, etFechaVacuna, etLote, etVeterinario, etObservaciones;
    private Spinner spinnerTipoMascota, spinnerTipoVacuna, spinnerDosis;
    private Button btnVolver, btnRegistrar, btnLimpiar, btnVerHistorial; // Botón de historial añadido
    private ImageView imgMascota;
    private TextView textViewTitulo;

    private FirebaseFirestore db;
    private String currentUserEmail; // Email del usuario logueado

    // Variables para guardar datos cargados del usuario y la mascota
    private String ownerNombreActual = "";
    private String ownerApellidoActual = "";
    private String petNombreActual = "";
    private String petEspecieActual = "";
    private String petRazaActual = "";
    private String petPesoActual = "";

    private final Calendar myCalendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // ASEGÚRATE que R.layout.activity_registro_vacuna sea el nombre correcto de tu archivo XML
        setContentView(R.layout.registrarvacunas);

        db = FirebaseFirestore.getInstance();

        // --- Obtener Email del usuario logueado ---
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        currentUserEmail = sharedPreferences.getString("email", null);

        if (currentUserEmail == null || currentUserEmail.isEmpty()) {
            Log.e(TAG, "Error: No se encontró email de usuario en SharedPreferences.");
            Toast.makeText(this, "Error: No se pudo identificar al usuario.", Toast.LENGTH_SHORT).show();
            finish(); // Cerrar si no hay usuario
            return;
        }
        Log.d(TAG, "Usuario actual: " + currentUserEmail);

        // --- Inicializar Vistas ---
        inicializarVistas();

        // --- Configurar Spinners (Tipo Vacuna, Dosis) ---
        configurarSpinnersVacuna();

        // --- Configurar DatePicker ---
        configurarDatePicker();

        // --- Cargar datos del USUARIO y su ÚNICA MASCOTA ---
        cargarDatosUsuarioYMascota();

        // --- Configurar Listeners de Botones ---
        configurarBotones();
    }

    private void inicializarVistas() {
        textViewTitulo = findViewById(R.id.textView2);
        imgMascota = findViewById(R.id.imgMascota);
        etNombreMascota = findViewById(R.id.etNombreMascota);
        spinnerTipoMascota = findViewById(R.id.spinnerTipoMascota); // Lo usaremos para mostrar especie
        etRazaMascota = findViewById(R.id.etRazaMascota);
        etFechaVacuna = findViewById(R.id.etFechaVacuna);
        spinnerTipoVacuna = findViewById(R.id.spinnerTipoVacuna);
        spinnerDosis = findViewById(R.id.spinnerDosis);
        etLote = findViewById(R.id.etLote);
        etVeterinario = findViewById(R.id.etVeterinario);
        etObservaciones = findViewById(R.id.etObservaciones);
        btnVolver = findViewById(R.id.btnVolver);
        btnRegistrar = findViewById(R.id.btnRegistrar);
        btnLimpiar = findViewById(R.id.btnLimpiar);
        //btnVerHistorial = findViewById(R.id.btnVerHistorial); // Inicializar botón historial

        // Hacemos no editables los campos de la mascota que cargaremos
        etNombreMascota.setEnabled(false);
        spinnerTipoMascota.setEnabled(false); // Lo deshabilitamos, solo mostrará la especie
        etRazaMascota.setEnabled(false); // También deshabilitamos la raza si se carga

        // Cambiar color de fondo para indicar que no son editables
        // ASEGÚRATE de tener el color 'lightGray' definido en res/values/colors.xml
        int disabledColor = ContextCompat.getColor(this, R.color.lightGray);
        etNombreMascota.setBackgroundColor(disabledColor);
        spinnerTipoMascota.setBackgroundColor(disabledColor);
        etRazaMascota.setBackgroundColor(disabledColor);

        // Poner el color original a los campos editables (por si acaso)
        int enabledColor = ContextCompat.getColor(this, android.R.color.transparent); // O el color de tu drawable
        etFechaVacuna.getBackground().setTintList(null); // Quita tinte si usas drawable
        spinnerTipoVacuna.getBackground().setTintList(null);
        spinnerDosis.getBackground().setTintList(null);
        etLote.getBackground().setTintList(null);
        etVeterinario.getBackground().setTintList(null);
        etObservaciones.getBackground().setTintList(null);

        etNombreMascota.setTextColor(ContextCompat.getColor(this, R.color.black)); // Color de texto normal
        etRazaMascota.setTextColor(ContextCompat.getColor(this, R.color.black));

    }

    // Configura SOLO los spinners de Vacuna y Dosis, y el adapter para Especie
    private void configurarSpinnersVacuna() {
        // ASEGÚRATE de tener estos arrays definidos en res/values/strings.xml
        // Opciones para Tipo de Vacuna
        ArrayAdapter<CharSequence> adapterTipoVacuna = ArrayAdapter.createFromResource(this,
                R.array.tipos_vacuna_array, android.R.layout.simple_spinner_item);
        adapterTipoVacuna.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipoVacuna.setAdapter(adapterTipoVacuna);
        spinnerTipoVacuna.setSelection(0, false);

        // Opciones para Dosis
        ArrayAdapter<CharSequence> adapterDosis = ArrayAdapter.createFromResource(this,
                R.array.dosis_vacuna_array, android.R.layout.simple_spinner_item);
        adapterDosis.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDosis.setAdapter(adapterDosis);
        spinnerDosis.setSelection(0, false);

        // --- Spinner de Tipo de Mascota (Especie) ---
        // Se necesita el adapter para poder usar getSpinnerIndex luego
        ArrayAdapter<CharSequence> adapterTipoMascota = ArrayAdapter.createFromResource(this,
                R.array.tipos_mascota_array, android.R.layout.simple_spinner_item);
        adapterTipoMascota.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipoMascota.setAdapter(adapterTipoMascota);
        spinnerTipoMascota.setEnabled(false); // Doble check de deshabilitado
    }

    // Método para obtener la posición de un item en el ArrayAdapter
    private int getSpinnerIndex(Spinner spinner, String myString){
        if (myString == null || spinner == null || spinner.getAdapter() == null) return 0;
        for (int i=0; i < spinner.getCount(); i++){
            // Comparación insensible a mayúsculas/minúsculas
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(myString.trim())){
                return i;
            }
        }
        Log.w(TAG, "String '" + myString + "' no encontrado en el spinner.");
        return 0; // Devuelve 0 (posición "Seleccione...") si no se encuentra
    }

    // Carga datos del documento del usuario (incluyendo los de la mascota)
    private void cargarDatosUsuarioYMascota() {
        textViewTitulo.setText("Cargando datos..."); // Mensaje temporal
        DocumentReference userRef = db.collection("usuarios").document(currentUserEmail);
        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                Log.d(TAG, "Documento de usuario encontrado. Datos: " + documentSnapshot.getData());

                // --- Obtener datos del Dueño ---
                ownerNombreActual = documentSnapshot.getString("nombre");
                ownerApellidoActual = documentSnapshot.getString("apellido");

                // --- Obtener datos de la ÚNICA Mascota ---
                // ASUME que los campos se llaman exactamente así en Firestore
                petNombreActual = documentSnapshot.getString("nombreMascota");
                petEspecieActual = documentSnapshot.getString("especie");
                petRazaActual = documentSnapshot.getString("raza");
                petPesoActual = documentSnapshot.getString("peso");
                // String petEdad = documentSnapshot.getString("edad");

                // --- Rellenar campos de la UI (Mascota) ---
                etNombreMascota.setText(petNombreActual != null ? petNombreActual : "N/A");
                etRazaMascota.setText(petRazaActual != null ? petRazaActual : "N/A");

                // Seleccionar la especie correcta en el spinner (que ahora solo muestra)
                if (petEspecieActual != null && !petEspecieActual.isEmpty()) {
                    int position = getSpinnerIndex(spinnerTipoMascota, petEspecieActual);
                    spinnerTipoMascota.setSelection(position);
                    Log.d(TAG, "Especie '" + petEspecieActual + "' seleccionada en spinner en posición " + position);
                } else {
                    Log.w(TAG, "Campo 'especie' no encontrado o vacío para usuario: " + currentUserEmail);
                    spinnerTipoMascota.setSelection(0); // Dejar en "Seleccione..."
                }

                // Cambiar título de la actividad
                if (petNombreActual != null && !petNombreActual.isEmpty()) {
                    textViewTitulo.setText("Registrar Vacuna para " + petNombreActual);
                } else {
                    textViewTitulo.setText("Registrar Vacuna");
                    Log.w(TAG,"Nombre de mascota no encontrado para el título.");
                }

            } else {
                Log.e(TAG, "¡Error Crítico! No se encontró el documento del usuario con email: " + currentUserEmail);
                Toast.makeText(this, "Error: Datos del usuario no encontrados.", Toast.LENGTH_LONG).show();
                // Considera cerrar la actividad o deshabilitar el registro
                btnRegistrar.setEnabled(false);
                btnVerHistorial.setEnabled(false);
                textViewTitulo.setText("Error al cargar datos");
            }
        }).addOnFailureListener(e -> {
            Log.e(TAG, "Error al cargar datos del usuario/mascota desde Firestore", e);
            Toast.makeText(this, "Error de conexión al cargar datos.", Toast.LENGTH_SHORT).show();
            textViewTitulo.setText("Error de conexión");
            btnRegistrar.setEnabled(false);
            btnVerHistorial.setEnabled(false);
        });
    }

    // Configura el DatePickerDialog
    private void configurarDatePicker() {
        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, dayOfMonth) -> {
            myCalendar.set(Calendar.YEAR, year);
            myCalendar.set(Calendar.MONTH, month);
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            actualizarCampoFecha();
        };

        etFechaVacuna.setOnClickListener(v -> {
            // Evitar abrir si no se han cargado los datos base
            if (petNombreActual.isEmpty() && ownerNombreActual.isEmpty()) {
                Toast.makeText(this, "Espere a que carguen los datos de la mascota.", Toast.LENGTH_SHORT).show();
                return;
            }
            new DatePickerDialog(registrarVacunasActivity.this, dateSetListener,
                    myCalendar.get(Calendar.YEAR),
                    myCalendar.get(Calendar.MONTH),
                    myCalendar.get(Calendar.DAY_OF_MONTH)).show();
        });

    }

    // Actualiza el EditText de la fecha
    private void actualizarCampoFecha() {
        String myFormat = "dd/MM/yyyy"; // Puedes cambiar el formato aquí
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.getDefault());
        etFechaVacuna.setText(sdf.format(myCalendar.getTime()));
    }

    // Configura los listeners de todos los botones
    private void configurarBotones() {
        btnVolver.setOnClickListener(v -> finish());
        btnLimpiar.setOnClickListener(v -> limpiarCamposVacuna()); // Limpia solo vacuna
        btnRegistrar.setOnClickListener(v -> registrarVacuna());
        btnVerHistorial.setOnClickListener(v -> verHistorial()); // Navega a historial
    }

    // Limpia solo los campos editables de la vacuna
    private void limpiarCamposVacuna() {
        etFechaVacuna.setText("");
        spinnerTipoVacuna.setSelection(0);
        spinnerDosis.setSelection(0);
        etLote.setText("");
        etVeterinario.setText("");
        etObservaciones.setText("");
        etFechaVacuna.setError(null); // Limpiar posibles errores
        etLote.setError(null);
        etVeterinario.setError(null);
        myCalendar.setTimeInMillis(System.currentTimeMillis()); // Resetea calendario
    }

    // Lógica para registrar la vacuna en Firestore
    private void registrarVacuna() {
        // --- Obtener datos de la vacuna desde el formulario ---
        String fecha = etFechaVacuna.getText().toString().trim();
        String tipoVacuna = "";
        if (spinnerTipoVacuna.getSelectedItemPosition() > 0) {
            tipoVacuna = spinnerTipoVacuna.getSelectedItem().toString();
        }
        String dosis = "";
        if (spinnerDosis.getSelectedItemPosition() > 0) {
            dosis = spinnerDosis.getSelectedItem().toString();
        }
        String lote = etLote.getText().toString().trim();
        String veterinario = etVeterinario.getText().toString().trim();
        String observaciones = etObservaciones.getText().toString().trim();

        // --- Validación de campos de vacuna ---
        boolean valid = true;
        if (TextUtils.isEmpty(fecha)) {
            etFechaVacuna.setError("Fecha requerida"); valid = false;
        } else { etFechaVacuna.setError(null); }

        if (TextUtils.isEmpty(tipoVacuna) || spinnerTipoVacuna.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Seleccione el tipo de vacuna.", Toast.LENGTH_SHORT).show(); valid = false;
            // Podrías añadir una indicación visual al spinner si quieres
        }
        if (TextUtils.isEmpty(dosis) || spinnerDosis.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Seleccione la dosis.", Toast.LENGTH_SHORT).show(); valid = false;
        }
        if (TextUtils.isEmpty(lote)) {
            etLote.setError("Lote requerido"); valid = false;
        } else { etLote.setError(null); }
        if (TextUtils.isEmpty(veterinario)) {
            etVeterinario.setError("Veterinario requerido"); valid = false;
        } else { etVeterinario.setError(null); }

        if (!valid) {
            Toast.makeText(this, "Por favor, complete los campos requeridos.", Toast.LENGTH_SHORT).show();
            return; // Detiene el proceso si hay errores
        }

        // --- Validar que los datos del usuario/mascota se hayan cargado ---
        if (ownerNombreActual == null || ownerNombreActual.isEmpty() ||
                petNombreActual == null || petNombreActual.isEmpty() ||
                currentUserEmail == null || currentUserEmail.isEmpty()) {
            Toast.makeText(this, "Error: Datos base no cargados. No se puede registrar.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "Intento de registro sin datos base (dueño/mascota/email).");
            return;
        }

        // --- Crear objeto Vacuna con TODOS los datos (Redundante) ---
        Vacuna nuevaVacuna = new Vacuna(
                ownerNombreActual, ownerApellidoActual, currentUserEmail,
                petNombreActual, petEspecieActual, petRazaActual, petPesoActual,
                fecha, tipoVacuna, dosis, lote, veterinario, observaciones
        );

        // --- Guardar en Firestore en la SUBCOLECCIÓN del USUARIO ---
        // Guarda en: /usuarios/{userEmail}/vacunas/{autoId}
        Log.d(TAG,"Guardando vacuna en: usuarios/" + currentUserEmail + "/vacunas");
        btnRegistrar.setEnabled(false); // Deshabilitar botón mientras guarda
        db.collection("usuarios").document(currentUserEmail).collection("vacunas")
                .add(nuevaVacuna) // Firestore asignará un ID automático
                .addOnSuccessListener(documentReference -> {
                    Log.i(TAG, "Vacuna registrada con ID: " + documentReference.getId() + " para usuario " + currentUserEmail);
                    Toast.makeText(registrarVacunasActivity.this, "Vacuna registrada exitosamente", Toast.LENGTH_SHORT).show();
                    limpiarCamposVacuna(); // Limpiar campos para posible nuevo registro
                    btnRegistrar.setEnabled(true); // Rehabilitar botón
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al registrar vacuna para usuario " + currentUserEmail, e);
                    String errorMsg = "Error al registrar vacuna.";
                    if (e instanceof FirebaseFirestoreException) {
                        errorMsg += " Código: " + ((FirebaseFirestoreException) e).getCode();
                    }
                    Toast.makeText(registrarVacunasActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    btnRegistrar.setEnabled(true); // Rehabilitar botón
                });
    }

    // Inicia la actividad para ver el historial
    private void verHistorial() {
        // Solo necesita iniciar la actividad, ella obtendrá el email necesario
        Intent intent = new Intent(this, HistorialVacunasActivity.class);
        startActivity(intent);
    }
}

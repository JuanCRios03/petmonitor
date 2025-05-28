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
import android.widget.LinearLayout;
import android.widget.TextView;

public class RegistrarVacunaActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String userEmail;
    private String nombreMascota;
    private String mascotaId;
    private String userId;
    private boolean esVeterinario = false;

    // Variables para veterinario viendo cliente específico
    private boolean esVeterinarioViendoCliente = false;
    private String clienteId;
    private String clienteEmail;
    private String nombreCliente;
    private String mascotaIdEspecifica;

    private EditText fechaVacunacion, tipoVacuna, dosis, lote, veterinario, observaciones;
    private Button btnGuardar;
    private Spinner spinnerMascotas;
    private List<String> listaMascotas;
    private List<String> listaMascotasIds;
    private LinearLayout layoutSafeZone;
    private TextView mensajeAccesoDenegado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar_vacuna);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        userEmail = auth.getCurrentUser().getEmail();

        // Obtener datos del Intent
        obtenerDatosDelIntent();

        inicializarVistas();

        listaMascotas = new ArrayList<>();
        listaMascotasIds = new ArrayList<>();

        // Primero validar si es veterinario, luego cargar datos
        validarVeterinarioYCargarDatos();

        // Al hacer clic en el EditText de fecha, se abre el DatePicker
        fechaVacunacion.setOnClickListener(v -> mostrarDatePickerDialog());

        btnGuardar.setOnClickListener(v -> {
            if (esVeterinario) {
                guardarVacuna();
            } else {
                Toast.makeText(this, "Solo los veterinarios pueden registrar vacunas", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void obtenerDatosDelIntent() {
        esVeterinarioViendoCliente = getIntent().getBooleanExtra("esVeterinarioViendoCliente", false);
        clienteId = getIntent().getStringExtra("clienteId");
        clienteEmail = getIntent().getStringExtra("clienteEmail");
        nombreCliente = getIntent().getStringExtra("nombreCliente");
        mascotaIdEspecifica = getIntent().getStringExtra("mascotaId");

        Log.d("RegistrarVacuna", "Datos recibidos:");
        Log.d("RegistrarVacuna", "esVeterinarioViendoCliente: " + esVeterinarioViendoCliente);
        Log.d("RegistrarVacuna", "clienteId: " + clienteId);
        Log.d("RegistrarVacuna", "clienteEmail: " + clienteEmail);
        Log.d("RegistrarVacuna", "nombreCliente: " + nombreCliente);
        Log.d("RegistrarVacuna", "mascotaIdEspecifica: " + mascotaIdEspecifica);
    }

    private void inicializarVistas() {
        fechaVacunacion = findViewById(R.id.fechaVacunacion);
        tipoVacuna = findViewById(R.id.tipoVacuna);
        dosis = findViewById(R.id.dosis);
        lote = findViewById(R.id.lote);
        veterinario = findViewById(R.id.veterinario);
        observaciones = findViewById(R.id.observaciones);
        btnGuardar = findViewById(R.id.btnGuardar);
        spinnerMascotas = findViewById(R.id.spinnerMascotas);
        layoutSafeZone = findViewById(R.id.layoutSafeZone);

        // Crear mensaje de acceso denegado (inicialmente oculto)
        mensajeAccesoDenegado = new TextView(this);
        mensajeAccesoDenegado.setText("⚠️ ACCESO RESTRINGIDO ⚠️\n\nSolo los veterinarios autorizados pueden registrar vacunas.\n\nSi eres veterinario, contacta al administrador para verificar tu cuenta.");
        mensajeAccesoDenegado.setTextSize(16);
        mensajeAccesoDenegado.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        mensajeAccesoDenegado.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        mensajeAccesoDenegado.setPadding(32, 64, 32, 64);
        mensajeAccesoDenegado.setVisibility(View.GONE);

        // Agregar el mensaje al layout principal
        ((android.widget.RelativeLayout) findViewById(R.id.layoutPrincipal)).addView(mensajeAccesoDenegado);
    }

    private void validarVeterinarioYCargarDatos() {
        // Mostrar mensaje de carga
        Toast.makeText(this, "Verificando permisos...", Toast.LENGTH_SHORT).show();

        db.collection("usuarios")
                .whereEqualTo("email", userEmail)
                .get()
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        DocumentSnapshot userDoc = query.getDocuments().get(0);
                        userId = userDoc.getId();

                        // Verificar si el usuario es veterinario
                        Boolean esVet = userDoc.getBoolean("esVeterinario");
                        esVeterinario = esVet != null && esVet;

                        Log.d("VeterinarioCheck", "Usuario: " + userEmail + ", esVeterinario: " + esVeterinario);

                        if (esVeterinario) {
                            // Si es veterinario, mostrar el formulario y cargar mascotas
                            mostrarFormulario();
                            cargarMascotasSegunContexto();
                            Toast.makeText(this, "Acceso autorizado - Veterinario", Toast.LENGTH_SHORT).show();
                        } else {
                            // Si no es veterinario, mostrar mensaje de acceso denegado
                            mostrarAccesoDenegado();
                            Toast.makeText(this, "Acceso denegado - Solo veterinarios", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(this, "Usuario no encontrado", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("VeterinarioCheck", "Error al verificar usuario: " + e.getMessage(), e);
                    Toast.makeText(this, "Error al verificar permisos: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    finish();
                });
    }

    private void mostrarFormulario() {
        layoutSafeZone.setVisibility(View.VISIBLE);
        mensajeAccesoDenegado.setVisibility(View.GONE);
    }

    private void mostrarAccesoDenegado() {
        layoutSafeZone.setVisibility(View.GONE);
        mensajeAccesoDenegado.setVisibility(View.VISIBLE);

        // Deshabilitar el botón guardar
        btnGuardar.setEnabled(false);
        btnGuardar.setAlpha(0.5f);
    }

    private void cargarMascotasSegunContexto() {
        if (esVeterinarioViendoCliente && clienteId != null && !clienteId.isEmpty()) {
            // Veterinario viendo cliente específico - cargar solo mascotas de ese cliente
            Log.d("RegistrarVacuna", "Cargando mascotas del cliente específico: " + clienteId);
            cargarMascotasDelCliente();
        } else {
            // Veterinario en modo general - cargar todas las mascotas
            Log.d("RegistrarVacuna", "Cargando todas las mascotas (modo general)");
            cargarTodasLasMascotas();
        }
    }

    private void cargarMascotasDelCliente() {
        Log.d("RegistrarVacuna", "Buscando mascotas del cliente con ID: " + clienteId);

        db.collection("usuarios")
                .document(clienteId)
                .collection("mascotas")
                .get()
                .addOnSuccessListener(mascotas -> {
                    Log.d("RegistrarVacuna", "Mascotas encontradas: " + mascotas.size());

                    if (!mascotas.isEmpty()) {
                        listaMascotas.clear();
                        listaMascotasIds.clear();

                        for (DocumentSnapshot mascotaDoc : mascotas.getDocuments()) {
                            String nombre = mascotaDoc.getString("nombreMascota");
                            String propietario = nombreCliente != null ? nombreCliente : mascotaDoc.getString("nombrePropietario");
                            String id = mascotaDoc.getId();

                            Log.d("RegistrarVacuna", "Mascota encontrada: " + nombre + " (ID: " + id + ")");

                            // Mostrar nombre de mascota y propietario
                            String nombreCompleto = nombre + " (Propietario: " + propietario + ")";
                            listaMascotas.add(nombreCompleto);
                            listaMascotasIds.add(id);
                        }

                        configurarSpinnerMascotas();

                        // Si hay una mascota específica seleccionada, preseleccionarla
                        if (mascotaIdEspecifica != null && !mascotaIdEspecifica.isEmpty()) {
                            preseleccionarMascota(mascotaIdEspecifica);
                        }
                    } else {
                        Toast.makeText(this, "Este cliente no tiene mascotas registradas", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("RegistrarVacuna", "Error al cargar mascotas del cliente: " + e.getMessage());
                    Toast.makeText(this, "Error al cargar mascotas del cliente", Toast.LENGTH_SHORT).show();
                });
    }

    private void cargarMascotasDelUsuario() {
        // Cargar las mascotas del usuario actual (para que el veterinario pueda seleccionar)
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

                        configurarSpinnerMascotas();
                    } else {
                        // Si el veterinario no tiene mascotas propias, cargar todas las mascotas
                        cargarTodasLasMascotas();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("CargarMascotas", "Error al cargar mascotas del usuario: " + e.getMessage());
                    // Intentar cargar todas las mascotas como alternativa
                    cargarTodasLasMascotas();
                });
    }

    private void cargarTodasLasMascotas() {
        // Cargar todas las mascotas de todos los usuarios (para veterinarios)
        db.collectionGroup("mascotas")
                .get()
                .addOnSuccessListener(mascotas -> {
                    if (!mascotas.isEmpty()) {
                        listaMascotas.clear();
                        listaMascotasIds.clear();

                        for (DocumentSnapshot mascotaDoc : mascotas.getDocuments()) {
                            String nombre = mascotaDoc.getString("nombreMascota");
                            String propietario = mascotaDoc.getString("nombrePropietario");
                            String id = mascotaDoc.getId();

                            // Mostrar nombre de mascota y propietario
                            String nombreCompleto = nombre + " (Propietario: " + propietario + ")";
                            listaMascotas.add(nombreCompleto);
                            listaMascotasIds.add(id);
                        }

                        configurarSpinnerMascotas();
                    } else {
                        Toast.makeText(this, "No se encontraron mascotas en el sistema", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("CargarMascotas", "Error al cargar todas las mascotas: " + e.getMessage());
                    Toast.makeText(this, "Error al cargar mascotas", Toast.LENGTH_SHORT).show();
                });
    }

    private void preseleccionarMascota(String mascotaIdBuscada) {
        for (int i = 0; i < listaMascotasIds.size(); i++) {
            if (listaMascotasIds.get(i).equals(mascotaIdBuscada)) {
                spinnerMascotas.setSelection(i);
                mascotaId = mascotaIdBuscada;
                Log.d("RegistrarVacuna", "Mascota preseleccionada: " + mascotaIdBuscada + " en posición " + i);
                break;
            }
        }
    }

    private void mostrarDatePickerDialog() {
        if (!esVeterinario) {
            Toast.makeText(this, "Solo los veterinarios pueden acceder a esta función", Toast.LENGTH_SHORT).show();
            return;
        }

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        Calendar selectedDate = Calendar.getInstance();
                        selectedDate.set(year, monthOfYear, dayOfMonth);

                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                        fechaVacunacion.setText(sdf.format(selectedDate.getTime()));
                    }
                },
                year, month, day
        );
        datePickerDialog.show();
    }

    private void guardarVacuna() {
        // Verificación adicional de seguridad
        if (!esVeterinario) {
            Toast.makeText(this, "Acceso denegado. Solo veterinarios pueden registrar vacunas.", Toast.LENGTH_LONG).show();
            return;
        }

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
            return;
        }

        String nombre = currentUser.getDisplayName();
        if (nombre == null) {
            nombre = "Veterinario"; // Valor por defecto para veterinarios
        } else {
            nombre = nombre.split(" ")[0];
        }

        Log.d("RegistrarVacuna", "Iniciando guardado de vacuna");
        Log.d("RegistrarVacuna", "Mascota ID: " + mascotaId);
        Log.d("RegistrarVacuna", "Cliente ID: " + clienteId);

        // Buscar la mascota y su propietario para guardar la vacuna correctamente
        if (esVeterinarioViendoCliente && clienteId != null && !clienteId.isEmpty()) {
            // Guardado directo usando clienteId conocido
            guardarVacunaDirecta(fecha, tipo, d, l, vet, obs, nombre, clienteId);
        } else {
            // Búsqueda tradicional del propietario
            buscarPropietarioYGuardarVacuna(fecha, tipo, d, l, vet, obs, nombre);
        }
    }

    private void guardarVacunaDirecta(String fecha, String tipo, String dosis, String lote, String vet, String obs, String nombreVeterinario, String propietarioId) {
        Log.d("RegistrarVacuna", "Guardando vacuna directamente - PropietarioId: " + propietarioId + ", MascotaId: " + mascotaId);

        // Primero obtener el nombre de la mascota
        db.collection("usuarios")
                .document(propietarioId)
                .collection("mascotas")
                .document(mascotaId)
                .get()
                .addOnSuccessListener(mascotaDoc -> {
                    if (mascotaDoc.exists()) {
                        String nombreMascotaReal = mascotaDoc.getString("nombreMascota");

                        // Crear el objeto Vacuna
                        Vacuna vacuna = new Vacuna(
                                nombreVeterinario,
                                "", // apellido
                                userEmail,
                                nombreMascotaReal,
                                "", "", "", // especie, raza, peso
                                fecha,
                                tipo,
                                dosis,
                                lote,
                                vet,
                                obs
                        );

                        // Guardar la vacuna en la subcolección de vacunas de la mascota
                        db.collection("usuarios")
                                .document(propietarioId)
                                .collection("mascotas")
                                .document(mascotaId)
                                .collection("vacunas")
                                .add(vacuna)
                                .addOnSuccessListener(documentReference -> {
                                    Log.d("RegistrarVacuna", "Vacuna registrada con éxito: " + documentReference.getId());
                                    Toast.makeText(this, "Vacuna registrada exitosamente", Toast.LENGTH_SHORT).show();
                                    limpiarFormulario();
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("RegistrarVacuna", "Error al registrar la vacuna: " + e.getMessage(), e);
                                    Toast.makeText(this, "Error al guardar vacuna: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                });
                    } else {
                        Log.e("RegistrarVacuna", "Mascota no encontrada: " + mascotaId);
                        Toast.makeText(this, "Mascota no encontrada", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("RegistrarVacuna", "Error al obtener datos de la mascota: " + e.getMessage());
                    Toast.makeText(this, "Error al obtener datos de la mascota", Toast.LENGTH_SHORT).show();
                });
    }

    private void buscarPropietarioYGuardarVacuna(String fecha, String tipo, String dosis, String lote, String vet, String obs, String nombreVeterinario) {
        Log.d("RegistroVacuna", "Iniciando búsqueda de mascota con ID: " + mascotaId);

        // Buscar en todas las colecciones de mascotas para encontrar el propietario
        db.collectionGroup("mascotas")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    boolean mascotaEncontrada = false;

                    Log.d("RegistroVacuna", "Documentos encontrados: " + querySnapshot.size());

                    for (DocumentSnapshot mascotaDoc : querySnapshot.getDocuments()) {
                        Log.d("RegistroVacuna", "Comparando: " + mascotaDoc.getId() + " con " + mascotaId);

                        // Comparar el ID del documento con el mascotaId seleccionado
                        if (mascotaDoc.getId().equals(mascotaId)) {
                            mascotaEncontrada = true;

                            // Obtener el ID del propietario desde la referencia del documento
                            String propietarioId = mascotaDoc.getReference().getParent().getParent().getId();

                            Log.d("RegistroVacuna", "Mascota encontrada: " + mascotaDoc.getId());
                            Log.d("RegistroVacuna", "Propietario ID: " + propietarioId);

                            // Crear el objeto Vacuna
                            Vacuna vacuna = new Vacuna(
                                    nombreVeterinario,
                                    "", // apellido
                                    userEmail,
                                    mascotaDoc.getString("nombreMascota"),
                                    "", "", "", // especie, raza, peso
                                    fecha,
                                    tipo,
                                    dosis,
                                    lote,
                                    vet,
                                    obs
                            );

                            // Guardar la vacuna en la subcolección de vacunas de la mascota
                            db.collection("usuarios")
                                    .document(propietarioId)
                                    .collection("mascotas")
                                    .document(mascotaId)
                                    .collection("vacunas")
                                    .add(vacuna)
                                    .addOnSuccessListener(documentReference -> {
                                        Log.d("RegistroVacuna", "Vacuna registrada con éxito: " + documentReference.getId());
                                        Toast.makeText(this, "Vacuna registrada exitosamente", Toast.LENGTH_SHORT).show();
                                        limpiarFormulario();
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("RegistroVacuna", "Error al registrar la vacuna: " + e.getMessage(), e);
                                        Toast.makeText(this, "Error al guardar vacuna: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    });

                            break; // Salir del bucle una vez encontrada la mascota
                        }
                    }

                    if (!mascotaEncontrada) {
                        Log.e("RegistroVacuna", "Mascota no encontrada con ID: " + mascotaId);
                        Toast.makeText(this, "Mascota no encontrada en el sistema", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("BuscarMascota", "Error al buscar mascota: " + e.getMessage(), e);
                    Toast.makeText(this, "Error al buscar mascota: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    // Método auxiliar para limpiar el formulario después de guardar
    private void limpiarFormulario() {
        fechaVacunacion.setText("");
        tipoVacuna.setText("");
        dosis.setText("");
        lote.setText("");
        veterinario.setText("");
        observaciones.setText("");

        // Resetear el spinner a la primera posición
        if (spinnerMascotas.getAdapter() != null && spinnerMascotas.getAdapter().getCount() > 0) {
            spinnerMascotas.setSelection(0);
        }
    }

    // Método mejorado para configurar el spinner con mejor debugging
    private void configurarSpinnerMascotas() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listaMascotas);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMascotas.setAdapter(adapter);

        spinnerMascotas.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (position < listaMascotasIds.size()) {
                    mascotaId = listaMascotasIds.get(position);
                    Log.d("RegistrarVacuna", "Mascota seleccionada: " + mascotaId + " (" + listaMascotas.get(position) + ")");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // No hacer nada
            }
        });
    }
}


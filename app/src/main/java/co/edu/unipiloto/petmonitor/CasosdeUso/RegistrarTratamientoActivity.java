package co.edu.unipiloto.petmonitor.CasosdeUso;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import co.edu.unipiloto.petmonitor.R;

public class RegistrarTratamientoActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String userId;

    private EditText medicamento, descripcion, hora, fechaInicio, fechaFin;
    private Spinner frecuenciaSpinner, mascotaSpinner;
    private Button btnGuardarTratamiento, btnVolver;

    private List<String> mascotaNombres = new ArrayList<>();
    private List<String> mascotaIds = new ArrayList<>();
    private String mascotaSeleccionadaId;
    private int mascotaSeleccionadaPosicion = -1;

    // Variables para rol veterinario
    private boolean esVeterinario = false;
    private boolean esVeterinarioViendoCliente = false;
    private String clienteId;
    private String clienteEmail;
    private String nombreCliente;
    private String mascotaIdEspecifica; // Para cuando viene de MenuVeterinario con mascota específica

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar_tratamiento);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Obtener datos del Intent
        Intent intent = getIntent();
        esVeterinario = intent.getBooleanExtra("esVeterinario", false);
        esVeterinarioViendoCliente = intent.getBooleanExtra("esVeterinarioViendoCliente", false);
        clienteId = intent.getStringExtra("clienteId");
        clienteEmail = intent.getStringExtra("clienteEmail");
        nombreCliente = intent.getStringExtra("nombreCliente");
        mascotaIdEspecifica = intent.getStringExtra("mascotaId");

        Log.d("RegistrarTratamiento", "esVeterinario: " + esVeterinario);
        Log.d("RegistrarTratamiento", "esVeterinarioViendoCliente: " + esVeterinarioViendoCliente);
        Log.d("RegistrarTratamiento", "clienteId: " + clienteId);
        Log.d("RegistrarTratamiento", "mascotaIdEspecifica: " + mascotaIdEspecifica);

        initializeViews();
        setupSpinners();
        setupDateTimePickers();
        setupButtons();

        cargarUsuario();
    }

    private void initializeViews() {
        medicamento = findViewById(R.id.medicamento);
        descripcion = findViewById(R.id.descripcion);
        hora = findViewById(R.id.hora);
        fechaInicio = findViewById(R.id.fechaInicio);
        fechaFin = findViewById(R.id.fechaFin);
        frecuenciaSpinner = findViewById(R.id.frecuenciaSpinner);
        mascotaSpinner = findViewById(R.id.mascotaSpinner);
        btnGuardarTratamiento = findViewById(R.id.btnGuardarTratamiento);
        btnVolver = findViewById(R.id.btnVolver);
    }

    private void setupSpinners() {
        // Configurar spinner de frecuencia
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.frecuencias_array,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        frecuenciaSpinner.setAdapter(adapter);

        // Configurar listener del spinner de mascotas
        mascotaSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position < mascotaIds.size()) {
                    mascotaSeleccionadaId = mascotaIds.get(position);
                    mascotaSeleccionadaPosicion = position;
                    Log.d("RegistrarTratamiento", "Mascota seleccionada: " + mascotaSeleccionadaId);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mascotaSeleccionadaId = null;
                mascotaSeleccionadaPosicion = -1;
            }
        });
    }

    private void setupDateTimePickers() {
        fechaInicio.setOnClickListener(v -> mostrarDatePickerDialog(fechaInicio));
        fechaFin.setOnClickListener(v -> mostrarDatePickerDialog(fechaFin));
        hora.setOnClickListener(v -> mostrarTimePickerDialog(hora));
    }

    private void setupButtons() {
        btnVolver.setOnClickListener(v -> finish());
        btnGuardarTratamiento.setOnClickListener(v -> guardarTratamiento());
    }

    private void mostrarDatePickerDialog(EditText campo) {
        Calendar calendar = Calendar.getInstance();

        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            Calendar selected = Calendar.getInstance();
            selected.set(year, month, dayOfMonth);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            campo.setText(sdf.format(selected.getTime()));
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        // Deshabilita fechas pasadas
        dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        dialog.show();
    }

    private void mostrarTimePickerDialog(EditText campo) {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePicker = new TimePickerDialog(this, (view, hourOfDay, minute1) -> {
            String horaSeleccionada = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute1);
            campo.setText(horaSeleccionada);
        }, hour, minute, true); // true para formato 24h

        timePicker.show();
    }

    private void cargarUsuario() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        if (esVeterinarioViendoCliente && clienteId != null && !clienteId.isEmpty()) {
            // Veterinario viendo cliente específico - usar clienteId directamente
            Log.d("RegistrarTratamiento", "Cargando mascotas del cliente: " + clienteId);
            cargarMascotasDelCliente(clienteId);
        } else if (esVeterinario && !esVeterinarioViendoCliente) {
            // Veterinario en modo general - necesitaría lógica adicional si es necesario
            userId = user.getUid();
            cargarMascotasDelUsuario(userId);
        } else {
            // Usuario regular
            userId = user.getUid();
            cargarMascotasDelUsuario(userId);
        }
    }

    private void cargarMascotasDelCliente(String clienteId) {
        db.collection("usuarios")
                .document(clienteId)
                .collection("mascotas")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    mascotaNombres.clear();
                    mascotaIds.clear();

                    for (QueryDocumentSnapshot mascotaDoc : queryDocumentSnapshots) {
                        String nombre = mascotaDoc.getString("nombreMascota");
                        String id = mascotaDoc.getId();

                        if (nombre != null) {
                            mascotaNombres.add(nombre);
                            mascotaIds.add(id);
                            Log.d("RegistrarTratamiento", "Mascota cargada: " + nombre + " (" + id + ")");
                        }
                    }

                    // CORRECCIÓN: Primero cargar las mascotas al spinner
                    cargarMascotas(mascotaNombres);

                    // CORRECCIÓN: Después preseleccionar usando post() para asegurar que el spinner esté listo
                    if (mascotaIdEspecifica != null && !mascotaIdEspecifica.isEmpty()) {
                        mascotaSpinner.post(() -> preseleccionarMascota(mascotaIdEspecifica));
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar mascotas del cliente", Toast.LENGTH_SHORT).show();
                    Log.e("RegistrarTratamiento", "Error cargando mascotas del cliente", e);
                });
    }

    private void cargarMascotasDelUsuario(String userId) {
        DocumentReference userRef = db.collection("usuarios").document(userId);

        userRef.collection("mascotas").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    mascotaNombres.clear();
                    mascotaIds.clear();

                    for (QueryDocumentSnapshot mascotaDoc : queryDocumentSnapshots) {
                        String nombre = mascotaDoc.getString("nombreMascota");
                        String id = mascotaDoc.getId();

                        if (nombre != null) {
                            mascotaNombres.add(nombre);
                            mascotaIds.add(id);
                        }
                    }
                    cargarMascotas(mascotaNombres);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar mascotas", Toast.LENGTH_SHORT).show();
                    Log.e("RegistrarTratamiento", "Error cargando mascotas", e);
                });
    }

    private void preseleccionarMascota(String mascotaId) {
        Log.d("RegistrarTratamiento", "Intentando preseleccionar mascota: " + mascotaId);
        Log.d("RegistrarTratamiento", "Mascotas disponibles: " + mascotaIds.toString());

        for (int i = 0; i < mascotaIds.size(); i++) {
            if (mascotaIds.get(i).equals(mascotaId)) {
                mascotaSpinner.setSelection(i);
                mascotaSeleccionadaId = mascotaId;
                mascotaSeleccionadaPosicion = i;
                Log.d("RegistrarTratamiento", "Mascota preseleccionada en posición: " + i + " con ID: " + mascotaId);

                // CORRECCIÓN: Forzar la actualización del listener
                if (mascotaSpinner.getOnItemSelectedListener() != null) {
                    mascotaSpinner.getOnItemSelectedListener().onItemSelected(mascotaSpinner, null, i, 0);
                }
                return;
            }
        }
        Log.w("RegistrarTratamiento", "No se pudo encontrar la mascota con ID: " + mascotaId);
    }

    private void cargarMascotas(List<String> nombres) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, nombres);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mascotaSpinner.setAdapter(adapter);

        Log.d("RegistrarTratamiento", "Spinner configurado con " + nombres.size() + " mascotas");
    }

    private void guardarTratamiento() {
        String med = medicamento.getText().toString().trim();
        String desc = descripcion.getText().toString().trim();
        String frec = frecuenciaSpinner.getSelectedItem().toString();
        String horaTxt = hora.getText().toString().trim();
        String inicio = fechaInicio.getText().toString().trim();
        String fin = fechaFin.getText().toString().trim();

        // CORRECCIÓN: Validación mejorada para mascota
        Log.d("RegistrarTratamiento", "Validando mascota - ID: " + mascotaSeleccionadaId + ", Posición: " + mascotaSeleccionadaPosicion);

        if (mascotaSeleccionadaId == null || mascotaSeleccionadaId.isEmpty()) {
            // CORRECCIÓN: Intentar obtener la mascota del spinner actual si no está asignada
            int posicionActual = mascotaSpinner.getSelectedItemPosition();
            if (posicionActual >= 0 && posicionActual < mascotaIds.size()) {
                mascotaSeleccionadaId = mascotaIds.get(posicionActual);
                mascotaSeleccionadaPosicion = posicionActual;
                Log.d("RegistrarTratamiento", "Mascota obtenida del spinner - ID: " + mascotaSeleccionadaId);
            } else {
                Toast.makeText(this, "Selecciona una mascota", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (med.isEmpty() || frec.isEmpty() || horaTxt.isEmpty() || inicio.isEmpty() || fin.isEmpty()) {
            Toast.makeText(this, "Por favor completa todos los campos obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        TimeZone tzColombia = TimeZone.getTimeZone("America/Bogota");
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        sdf.setTimeZone(tzColombia);

        long inicioMillisTemp = 0;
        try {
            Date fechaHora = sdf.parse(inicio + " " + horaTxt);
            if (fechaHora != null) {
                inicioMillisTemp = fechaHora.getTime();
            }
        } catch (ParseException e) {
            Toast.makeText(this, "Formato de fecha/hora inválido", Toast.LENGTH_SHORT).show();
            return;
        }
        final long inicioMillis = inicioMillisTemp;

        String nombreMascota = mascotaNombres.get(mascotaSeleccionadaPosicion);

        Map<String, Object> tratamiento = new HashMap<>();
        tratamiento.put("medicamento", med);
        tratamiento.put("descripcion", desc);
        tratamiento.put("frecuencia", frec);
        tratamiento.put("hora", horaTxt);
        tratamiento.put("fechaInicio", inicio);
        tratamiento.put("fechaFin", fin);
        tratamiento.put("timestamp", FieldValue.serverTimestamp());
        tratamiento.put("nombreMascota", nombreMascota);

        // Agregar información del veterinario si es el caso
        if (esVeterinario) {
            FirebaseUser currentUser = auth.getCurrentUser();
            if (currentUser != null) {
                tratamiento.put("veterinarioId", currentUser.getUid());
                tratamiento.put("veterinarioEmail", currentUser.getEmail());
                tratamiento.put("registradoPorVeterinario", true);
            }
        }

        // Determinar el userId correcto para guardar
        String userIdParaGuardar;
        if (esVeterinarioViendoCliente && clienteId != null && !clienteId.isEmpty()) {
            userIdParaGuardar = clienteId;
        } else {
            userIdParaGuardar = userId;
        }

        Log.d("RegistrarTratamiento", "Guardando tratamiento en userId: " + userIdParaGuardar);
        Log.d("RegistrarTratamiento", "MascotaId: " + mascotaSeleccionadaId);

        db.collection("usuarios").document(userIdParaGuardar)
                .collection("mascotas").document(mascotaSeleccionadaId)
                .collection("tratamientos")
                .add(tratamiento)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Tratamiento guardado correctamente", Toast.LENGTH_SHORT).show();

                    int frecuenciaHoras = obtenerHorasDesdeFrecuencia(frec);
                    int duracionDias = calcularDuracionDias(inicio, fin);
                    String tratamientoId = documentReference.getId();

                    programarRecordatorio(nombreMascota, med, desc, frecuenciaHoras, duracionDias, inicioMillis, tzColombia,
                            mascotaSeleccionadaId, tratamientoId);

                    // Limpiar campos después de guardar exitosamente
                    limpiarCampos();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al guardar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("RegistrarTratamiento", "Error al guardar tratamiento", e);
                });
    }

    private void limpiarCampos() {
        medicamento.setText("");
        descripcion.setText("");
        hora.setText("");
        fechaInicio.setText("");
        fechaFin.setText("");
        frecuenciaSpinner.setSelection(0);
        // No resetear mascotaSpinner si hay una preseleccionada
        if (mascotaIdEspecifica == null || mascotaIdEspecifica.isEmpty()) {
            mascotaSpinner.setSelection(0);
        }
    }

    private void programarRecordatorio(String nombreMascota, String nombre, String medicamento, int frecuenciaHoras, int duracionDias, long inicioMillis, TimeZone tzColombia,
                                       String mascotaId, String tratamientoId) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Toast.makeText(this, "No tienes permiso para programar alarmas exactas", Toast.LENGTH_LONG).show();
                return;
            }
        }

        long frecuenciaMillis = frecuenciaHoras * 60L * 60 * 1000;

        Calendar calendarColombia = Calendar.getInstance(tzColombia);
        long ahora = calendarColombia.getTimeInMillis();

        // Logs para debug
        Log.d("Recordatorio", "inicioMillis: " + inicioMillis + " (" + new Date(inicioMillis) + ")");
        Log.d("Recordatorio", "ahora: " + ahora + " (" + new Date(ahora) + ")");
        Log.d("Recordatorio", "frecuenciaMillis: " + frecuenciaMillis);

        long siguienteNotificacion = inicioMillis;
        while (siguienteNotificacion < ahora) {
            siguienteNotificacion += frecuenciaMillis;
        }

        int cantidadNotificaciones = (int) ((duracionDias * 24L) / frecuenciaHoras);

        // Log cantidad de notificaciones
        Log.d("Recordatorio", "cantidadNotificaciones: " + cantidadNotificaciones);

        for (int i = 0; i < cantidadNotificaciones; i++) {
            long triggerAtMillis = siguienteNotificacion + (i * frecuenciaMillis);

            Intent intent = new Intent(this, RecordatorioReceiver.class);
            intent.putExtra("titulo", "Tratamiento: " + nombre + " para tu mascota: " + nombreMascota);
            intent.putExtra("mensaje", "Recuerda dar medicamento: " + medicamento);
            intent.putExtra("mascotaId", mascotaId);
            intent.putExtra("tratamientoId", tratamientoId);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    this,
                    i,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
        }
    }

    public int obtenerHorasDesdeFrecuencia(String frecuencia) {
        switch (frecuencia) {
            case "Cada 8 horas":
                return 8;
            case "Cada 12 horas":
                return 12;
            case "Diario":
            default:
                return 24;
        }
    }

    public int calcularDuracionDias(String fechaInicio, String fechaFin) {
        TimeZone tzColombia = TimeZone.getTimeZone("America/Bogota");
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        sdf.setTimeZone(tzColombia);
        try {
            Date inicio = sdf.parse(fechaInicio);
            Date fin = sdf.parse(fechaFin);
            long diff = fin.getTime() - inicio.getTime();
            return (int) TimeUnit.MILLISECONDS.toDays(diff) + 1;
        } catch (ParseException e) {
            e.printStackTrace();
            return 1;
        }
    }
}
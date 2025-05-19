package co.edu.unipiloto.petmonitor.Tests;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.provider.Settings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.HashMap;
import java.util.Map;
import co.edu.unipiloto.petmonitor.CasosdeUso.RecordatorioReceiver;
import co.edu.unipiloto.petmonitor.R;

public class RegistrarTratamientoTestActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String userId;

    private String medicamento = "Paracetamol";
    private String descripcion = "Aliviar fiebre";
    private String horaTxt = "10:27";
    private String fechaInicio = "19/05/2025";
    private String fechaFin = "21/05/2025";
    private String frecuencia = "Diario";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar_tratamiento);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Verificar permiso POST_NOTIFICATIONS si es Android 13 o superior
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1001);
            }
        }

        cargarUsuario();
    }

    private void cargarUsuario() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        userId = user.getUid();
        DocumentReference userRef = db.collection("usuarios").document(userId);

        userRef.collection("mascotas").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot mascotaDoc : queryDocumentSnapshots) {
                        String mascotaId = mascotaDoc.getId();
                        String nombreMascota = mascotaDoc.getString("nombreMascota");

                        guardarTratamiento(mascotaId, nombreMascota);
                        break; // solo toma la primera para pruebas
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al cargar mascotas", Toast.LENGTH_SHORT).show();
                });
    }

    private void guardarTratamiento(String mascotaId, String nombreMascota) {
        Map<String, Object> tratamiento = new HashMap<>();
        tratamiento.put("medicamento", medicamento);
        tratamiento.put("descripcion", descripcion);
        tratamiento.put("frecuencia", frecuencia);
        tratamiento.put("hora", horaTxt);
        tratamiento.put("fechaInicio", fechaInicio);
        tratamiento.put("fechaFin", fechaFin);
        tratamiento.put("timestamp", FieldValue.serverTimestamp());
        tratamiento.put("nombreMascota", nombreMascota);

        db.collection("usuarios").document(userId)
                .collection("mascotas").document(mascotaId)
                .collection("tratamientos")
                .add(tratamiento)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Tratamiento de prueba guardado", Toast.LENGTH_SHORT).show();

                    Log.d("Alarm", "Alarma programada para 10 segundos después");
                    programarNotificacionPrueba();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al guardar tratamiento de prueba", Toast.LENGTH_SHORT).show();
                });
    }

    private void programarNotificacionPrueba() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(this, RecordatorioReceiver.class);
        intent.putExtra("titulo", "Prueba de tratamiento");
        intent.putExtra("mensaje", "Este es un recordatorio de prueba.");

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                this, 9999, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        long triggerAtMillis = System.currentTimeMillis() + 10_000; // 10 segundos

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Android 12+
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
            } else {
                Toast.makeText(this, "No se pueden programar alarmas exactas. Por favor habilita el permiso.", Toast.LENGTH_LONG).show();
                Intent intentSettings = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                startActivity(intentSettings);

                alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1001) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("Permiso", "POST_NOTIFICATIONS concedido");
            } else {
                Toast.makeText(this, "Permiso para notificaciones no concedido", Toast.LENGTH_SHORT).show();
            }
        }
    }
}




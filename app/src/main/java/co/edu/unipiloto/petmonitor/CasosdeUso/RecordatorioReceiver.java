package co.edu.unipiloto.petmonitor.CasosdeUso;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import java.util.Random;
import android.Manifest;

public class RecordatorioReceiver extends BroadcastReceiver {

    private void crearCanalNotificacion(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "CANAL_TRATAMIENTOS";
            CharSequence nombre = "Canal de Tratamientos";
            String descripcion = "Canal para recordatorios de tratamientos";
            int importancia = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel canal = new NotificationChannel(channelId, nombre, importancia);
            canal.setDescription(descripcion);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(canal);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("RecordatorioReceiver", "¡Notificación recibida!");

        crearCanalNotificacion(context);

        String titulo = intent.getStringExtra("titulo");
        String mensaje = intent.getStringExtra("mensaje");
        String tratamientoId = intent.getStringExtra("tratamientoId");
        String mascotaId = intent.getStringExtra("mascotaId");

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {

            // Intent para botón "Marcar cumplido"
            Intent cumplimientoIntent = new Intent(context, CumplimientoReceiver.class);
            cumplimientoIntent.putExtra("tratamientoId", tratamientoId);
            cumplimientoIntent.putExtra("mascotaId", mascotaId);

            PendingIntent cumplimientoPendingIntent = PendingIntent.getBroadcast(
                    context,
                    0,
                    cumplimientoIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "CANAL_TRATAMIENTOS")
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle(titulo != null ? titulo : "Recordatorio de Medicación")
                    .setContentText(mensaje != null ? mensaje : "Hora de administrar el medicamento.")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .addAction(android.R.drawable.checkbox_on_background, "Marcar cumplido", cumplimientoPendingIntent)
                    .setAutoCancel(true);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(new Random().nextInt(), builder.build());

        } else {
            Log.w("RecordatorioReceiver", "Permiso POST_NOTIFICATIONS no concedido.");
        }
    }
}




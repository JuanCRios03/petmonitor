package co.edu.unipiloto.petmonitor;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ZonaSeguraManager {
    private FirebaseFirestore db;

    public ZonaSeguraManager(FirebaseFirestore firestore) {
        this.db = firestore;
    }

    public void guardarZonaSegura(String userId, String mascotaId, double lat, double lon, float radio, final Callback callback) {
        // Validaciones básicas de latitud, longitud y radio
        if (lat < -90 || lat > 90) {
            callback.onFailure(new IllegalArgumentException("latitud inválida"));
            return;
        }
        if (lon < -180 || lon > 180) {
            callback.onFailure(new IllegalArgumentException("longitud inválida"));
            return;
        }
        if (radio <= 0) {
            callback.onFailure(new IllegalArgumentException("radio inválido"));
            return;
        }

        // Validar ubicación por defecto emulador (Googleplex)
        final double defaultLat = 37.4219983;
        final double defaultLon = -122.084;
        final double epsilon = 0.0001;
        if (Math.abs(lat - defaultLat) < epsilon && Math.abs(lon - defaultLon) < epsilon) {
            callback.onFailure(new IllegalArgumentException("ubicación por defecto del emulador detectada"));
            return;
        }

        // Validar ubicación inválida: lat y lon = 0.0 (sin ubicación real)
        if (lat == 0.0 && lon == 0.0) {
            callback.onFailure(new IllegalArgumentException("ubicación inválida"));
            return;
        }

        // Preparar datos a guardar
        Map<String, Object> datosZona = new HashMap<>();
        datosZona.put("latitud", lat);
        datosZona.put("longitud", lon);
        datosZona.put("radio", radio);
        datosZona.put("timestamp", System.currentTimeMillis());

        try {
            CollectionReference zonaseguraCollection = db
                    .collection("usuarios")
                    .document(userId)
                    .collection("mascotas")
                    .document(mascotaId)
                    .collection("zonasegura");

            if (zonaseguraCollection == null) {
                callback.onFailure(new NullPointerException("Referencia a colección zonasegura es null"));
                return;
            }

            Task<DocumentReference> addTask = zonaseguraCollection.add(datosZona);

            if (addTask == null) {
                callback.onFailure(new NullPointerException("El método add() retornó null"));
                return;
            }

            addTask
                    .addOnSuccessListener(documentReference -> callback.onSuccess())
                    .addOnFailureListener(e -> callback.onFailure(e));
        } catch (Exception e) {
            callback.onFailure(e);
        }
    }


    public interface Callback {
        void onSuccess();
        void onFailure(Exception e);
    }
}





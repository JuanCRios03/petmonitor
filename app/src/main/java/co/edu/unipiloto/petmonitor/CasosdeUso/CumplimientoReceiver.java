package co.edu.unipiloto.petmonitor.CasosdeUso;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class CumplimientoReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String tratamientoId = intent.getStringExtra("tratamientoId");
        String mascotaId = intent.getStringExtra("mascotaId");
        if (tratamientoId == null || mascotaId == null) return;

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (user == null) return;

        String userId = user.getUid();

        Map<String, Object> cumplimiento = new HashMap<>();
        cumplimiento.put("fechaRegistro", FieldValue.serverTimestamp());

        db.collection("usuarios").document(userId)
                .collection("mascotas").document(mascotaId)
                .collection("tratamientos").document(tratamientoId)
                .collection("cumplimientos")
                .add(cumplimiento)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(context, "Cumplimiento registrado", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(context, "Error al registrar cumplimiento", Toast.LENGTH_SHORT).show();
                });
    }
}


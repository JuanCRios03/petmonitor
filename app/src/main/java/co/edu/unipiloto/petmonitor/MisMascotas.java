package co.edu.unipiloto.petmonitor;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Map;

import co.edu.unipiloto.petmonitor.Menu.menuActivity;

public class MisMascotas extends AppCompatActivity {

    private LinearLayout layoutMascotas;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mis_mascotas);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        layoutMascotas = findViewById(R.id.layoutMascotas);

        ImageButton btnAgregar = findViewById(R.id.btnAgregarMascota);
        btnAgregar.setOnClickListener(v -> {
            Intent intent = new Intent(MisMascotas.this, co.edu.unipiloto.petmonitor.Register.RegisterPaso3Activity.class);
            startActivity(intent);
        });

        cargarMascotas();
    }

    private void cargarMascotas() {
        String email = auth.getCurrentUser() != null ? auth.getCurrentUser().getEmail() : null;
        if (email == null) return;

        db.collection("usuarios")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        String docId = queryDocumentSnapshots.getDocuments().get(0).getId();

                        db.collection("usuarios")
                                .document(docId)
                                .collection("mascotas")
                                .get()
                                .addOnSuccessListener(snapshot -> {
                                    List<DocumentSnapshot> mascotas = snapshot.getDocuments();
                                    for (DocumentSnapshot doc : mascotas) {
                                        Map<String, Object> data = doc.getData();
                                        if (data != null && data.containsKey("nombreMascota")) {
                                            String nombre = data.get("nombreMascota").toString();
                                            String mascotaId = doc.getId(); // <-- este es el ID que necesitas para acceder a Firestore correctamente
                                            agregarCardMascota(nombre, mascotaId);

                                        }
                                    }
                                });
                    }
                });
    }

    private void agregarCardMascota(String nombre, String mascotaId) {
        View card = getLayoutInflater().inflate(R.layout.item_mascota, layoutMascotas, false);

        ShapeableImageView imageView = card.findViewById(R.id.mascotaImage);
        TextView nombreView = card.findViewById(R.id.mascotaNombre);

        imageView.setImageResource(R.drawable.add_pet);
        nombreView.setText(nombre);

        // 🔁 Ahora abrimos monitoreoTiempoRealActivity y pasamos el ID correcto
        card.setOnClickListener(v -> {
            Intent intent = new Intent(MisMascotas.this, menuActivity.class);
            intent.putExtra("mascotaId", mascotaId);
            startActivity(intent);
        });

        layoutMascotas.addView(card);
    }


}

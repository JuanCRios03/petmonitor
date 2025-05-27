package co.edu.unipiloto.petmonitor.CasosdeUso;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Map;

import co.edu.unipiloto.petmonitor.Menu.MisMascotas;
import co.edu.unipiloto.petmonitor.Menu.menuActivity;
import co.edu.unipiloto.petmonitor.R;

public class MisClientes extends AppCompatActivity {
    private LinearLayout layoutClientes;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_mis_clientes);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        layoutClientes = findViewById(R.id.layoutClientes);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        ImageButton btnAgregar = findViewById(R.id.btnAgregarCliente);
        btnAgregar.setOnClickListener(v -> {
            Intent intent = new Intent(this, AgregarCliente.class);
            startActivity(intent);
        });
    }

    private void cargarClientes() {
        if (layoutClientes != null) {
            layoutClientes.removeAllViews();
        }

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
                                .collection("clientes")
                                .get()
                                .addOnSuccessListener(snapshot -> {
                                    List<DocumentSnapshot> clientes = snapshot.getDocuments();
                                    for (DocumentSnapshot doc : clientes) {
                                        Map<String, Object> data = doc.getData();
                                        if (data != null && data.containsKey("nombre")) {
                                            String nombre = data.get("nombre").toString();
                                            String userID = doc.getId();
                                            agregarCardCliente(nombre, userID, docId);
                                        }
                                    }
                                });
                    }
                });
    }

    private void agregarCardCliente(String nombre, String userID, String veterinarianID) {
        View card = getLayoutInflater().inflate(R.layout.item_mascota, layoutClientes, false);

        ShapeableImageView imageView = card.findViewById(R.id.mascotaImage);
        TextView nombreView = card.findViewById(R.id.mascotaNombre);

        nombreView.setText(nombre);

        // Cargar imagen desde Firestore
        db.collection("usuarios")
                .document(veterinarianID)
                .collection("clientes")
                .document(userID)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String imageBase64 = documentSnapshot.getString("imagenBase64");
                        if (imageBase64 != null && !imageBase64.isEmpty()) {
                            byte[] imageBytes = Base64.decode(imageBase64, Base64.DEFAULT);
                            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                            imageView.setImageBitmap(bitmap);
                        } else {
                            imageView.setImageResource(R.drawable.add_client);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    imageView.setImageResource(R.drawable.add_client);
                });

        card.setOnClickListener(v -> {
            Intent intent = new Intent(this, MenuVeterinario.class);
            intent.putExtra("userID", userID);
            startActivity(intent);
        });

        layoutClientes.addView(card);
    }

    @Override
    protected void onResume() {
        super.onResume();

        getSharedPreferences("UserPrefs", MODE_PRIVATE)
                .edit()
                .putString("lastActivity", "MisClientes")
                .apply();

        cargarClientes();
    }

}
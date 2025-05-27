package co.edu.unipiloto.petmonitor.Menu;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
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

import co.edu.unipiloto.petmonitor.CasosdeUso.MenuVeterinario;
import co.edu.unipiloto.petmonitor.CasosdeUso.editarPerfilActivity;
import co.edu.unipiloto.petmonitor.R;

public class MisMascotas extends AppCompatActivity {

    private LinearLayout layoutMascotas;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private boolean isUserVeterinarian;
    private String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mis_mascotas);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        userID = auth.getCurrentUser().getUid();
        checkIfUserIsVeterinarian();
        layoutMascotas = findViewById(R.id.layoutMascotas);

        ImageButton btnAgregar = findViewById(R.id.btnAgregarMascota);
        btnAgregar.setOnClickListener(v -> {
            Intent intent = new Intent(MisMascotas.this, co.edu.unipiloto.petmonitor.Register.RegisterPaso3Activity.class);
            startActivity(intent);
        });

    }

    private void cargarMascotas() {
        if (layoutMascotas != null) {
            layoutMascotas.removeAllViews();
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
                                .collection("mascotas")
                                .get()
                                .addOnSuccessListener(snapshot -> {
                                    List<DocumentSnapshot> mascotas = snapshot.getDocuments();
                                    for (DocumentSnapshot doc : mascotas) {
                                        Map<String, Object> data = doc.getData();
                                        if (data != null && data.containsKey("nombreMascota")) {
                                            String nombre = data.get("nombreMascota").toString();
                                            String mascotaId = doc.getId();
                                            agregarCardMascota(nombre, mascotaId, docId);
                                        }
                                    }
                                });
                    }
                });
    }


    private void checkIfUserIsVeterinarian() {
        db.collection("usuarios").document(userID).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Boolean esVeterinario = documentSnapshot.getBoolean("esVeterinario");
                        if (esVeterinario != null && esVeterinario) {
                            isUserVeterinarian = true;
                            System.out.println("es veterinario");
                            Intent intent = new Intent(this, MenuVeterinario.class);
                            intent.putExtra("isUserVeterinarian", isUserVeterinarian);
                            startActivity(intent);
                            finish();
                        } else {
                            isUserVeterinarian = false;

                            System.out.println("no es veterinario");
                        }
                    } else {
                        Log.d("Firestore", "El documento no existe");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error al obtener el documento", e);
                });
    }

    private void agregarCardMascota(String nombre, String mascotaId, String userId) {
        View card = getLayoutInflater().inflate(R.layout.item_mascota, layoutMascotas, false);

        ShapeableImageView imageView = card.findViewById(R.id.mascotaImage);
        TextView nombreView = card.findViewById(R.id.mascotaNombre);

        nombreView.setText(nombre);

        // Cargar imagen desde Firestore
        db.collection("usuarios")
                .document(userId)
                .collection("mascotas")
                .document(mascotaId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String imageBase64 = documentSnapshot.getString("imagenBase64");
                        if (imageBase64 != null && !imageBase64.isEmpty()) {
                            byte[] imageBytes = Base64.decode(imageBase64, Base64.DEFAULT);
                            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                            imageView.setImageBitmap(bitmap);
                        } else {
                            imageView.setImageResource(R.drawable.add_pet);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    imageView.setImageResource(R.drawable.add_pet);
                });

        card.setOnClickListener(v -> {
            Intent intent = new Intent(MisMascotas.this, menuActivity.class);
            intent.putExtra("mascotaId", mascotaId);
            startActivity(intent);
        });

        layoutMascotas.addView(card);
    }

    @Override
    protected void onResume() {
        super.onResume();

        getSharedPreferences("UserPrefs", MODE_PRIVATE)
                .edit()
                .putString("lastActivity", "MisMascotas")
                .apply();

        cargarMascotas();
    }
}



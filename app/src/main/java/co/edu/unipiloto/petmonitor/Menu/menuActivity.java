package co.edu.unipiloto.petmonitor.Menu;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import co.edu.unipiloto.petmonitor.CasosdeUso.editarPerfilActivity;
import co.edu.unipiloto.petmonitor.CasosdeUso.monitoreoEjercicio;
import co.edu.unipiloto.petmonitor.CasosdeUso.reporteActividad;
import co.edu.unipiloto.petmonitor.Login.LoginActivity;
import co.edu.unipiloto.petmonitor.R;
import android.widget.RelativeLayout;
import co.edu.unipiloto.petmonitor.CasosdeUso.zonaSeguraActivity;
import co.edu.unipiloto.petmonitor.CasosdeUso.VeterinariosActivity;
import co.edu.unipiloto.petmonitor.CasosdeUso.historialUbicacionActivity;
import co.edu.unipiloto.petmonitor.CasosdeUso.monitoreoTiempoRealActivity;
import co.edu.unipiloto.petmonitor.CasosdeUso.RegistrarVacunaActivity;
import co.edu.unipiloto.petmonitor.CasosdeUso.HistorialVacunasActivity;

public class menuActivity extends AppCompatActivity {

        private static final int PICK_IMAGE_REQUEST = 1;
        private ImageView imageView;
        private FirebaseFirestore db;
        private String currentUserEmail;
        private String mascotaId; // ← Se agrega para recibirlo por intent

        private RelativeLayout btnEditarPerfil, btnRegisterSafeZone, btnNearbyClinics, btnRealTimeLocation, btnLocationHistory, btnActivityReport, btnExerciseMonitoring, btnRegisterVaccines, btnHistorialVaccines, btnlogout;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_menu);

                // Obtener el ID de la mascota
                mascotaId = getIntent().getStringExtra("mascotaId");
                if (mascotaId == null) {
                        Toast.makeText(this, "No se recibió el ID de la mascota", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                }

                btnEditarPerfil = findViewById(R.id.btnEditarPerfil);
                btnEditarPerfil.setOnClickListener(v -> {
                        Intent intent = new Intent(menuActivity.this, editarPerfilActivity.class);
                        intent.putExtra("mascotaId", mascotaId);
                        startActivity(intent);
                });

                btnRegisterSafeZone = findViewById(R.id.btnRegisterSafeZone);
                btnRegisterSafeZone.setOnClickListener(v -> {
                        Intent intent = new Intent(menuActivity.this, zonaSeguraActivity.class);
                        intent.putExtra("mascotaId", mascotaId);
                        startActivity(intent);
                });

                btnNearbyClinics = findViewById(R.id.btnNearbyClinics);
                btnNearbyClinics.setOnClickListener(v -> {
                        Intent intent = new Intent(menuActivity.this, VeterinariosActivity.class);
                        intent.putExtra("mascotaId", mascotaId);
                        startActivity(intent);
                });

                btnRealTimeLocation = findViewById(R.id.btnRealTimeLocation);
                btnRealTimeLocation.setOnClickListener(v -> {
                        Intent intent = new Intent(menuActivity.this, monitoreoTiempoRealActivity.class);
                        intent.putExtra("mascotaId", mascotaId);
                        startActivity(intent);
                });

                btnLocationHistory = findViewById(R.id.btnLocationHistory);
                btnLocationHistory.setOnClickListener(v -> {
                        Intent intent = new Intent(menuActivity.this, historialUbicacionActivity.class);
                        intent.putExtra("mascotaId", mascotaId);
                        startActivity(intent);
                });

                btnActivityReport = findViewById(R.id.btnActivityReport);
                btnActivityReport.setOnClickListener(v -> {
                        Intent intent = new Intent(menuActivity.this, reporteActividad.class);
                        intent.putExtra("mascotaId", mascotaId);
                        startActivity(intent);
                });

                btnExerciseMonitoring = findViewById(R.id.btnExerciseMonitoring);
                btnExerciseMonitoring.setOnClickListener(v -> {
                        Intent intent = new Intent(menuActivity.this, monitoreoEjercicio.class);
                        intent.putExtra("mascotaId", mascotaId);
                        startActivity(intent);
                });

                btnRegisterVaccines = findViewById(R.id.btnRegisterVaccines);
                btnRegisterVaccines.setOnClickListener(v -> {
                        Intent intent = new Intent(menuActivity.this, RegistrarVacunaActivity.class);
                        intent.putExtra("mascotaId", mascotaId);
                        startActivity(intent);
                });

                btnHistorialVaccines = findViewById(R.id.btnHistorialVaccines);
                btnHistorialVaccines.setOnClickListener(v -> {
                        Intent intent = new Intent(menuActivity.this, HistorialVacunasActivity.class);
                        intent.putExtra("mascotaId", mascotaId);
                        startActivity(intent);
                });
                btnlogout = findViewById(R.id.btnlogout);

                btnlogout.setOnClickListener(v -> {
                        FirebaseAuth.getInstance().signOut(); // Cierra la sesión
                        Intent intent = new Intent(menuActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Limpia el back stack
                        startActivity(intent);
                        finish();
                });


                // Obtener email del usuario logueado
                SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                currentUserEmail = sharedPreferences.getString("email", null);

                if (currentUserEmail == null) {
                        Toast.makeText(this, "No hay sesión activa", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                }

                db = FirebaseFirestore.getInstance();

                // Configurar ImageView
                FrameLayout btnAddImage = findViewById(R.id.btnAddImage);
                imageView = new ImageView(this);
                imageView.setLayoutParams(new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                btnAddImage.addView(imageView);

                btnAddImage.setOnClickListener(view -> openImageChooser());

                // Cargar imagen existente
                loadExistingImage();
        }

        private void openImageChooser() {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, PICK_IMAGE_REQUEST);
        }

        @Override
        protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
                super.onActivityResult(requestCode, resultCode, data);

                if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
                        Uri imageUri = data.getData();
                        try {
                                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                                imageView.setImageBitmap(bitmap);
                                uploadImageToFirestore(bitmap);
                        } catch (IOException e) {
                                Toast.makeText(this, "Error al cargar imagen", Toast.LENGTH_SHORT).show();
                        }
                }
        }

        private void uploadImageToFirestore(Bitmap bitmap) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
                byte[] imageBytes = baos.toByteArray();
                String imageBase64 = Base64.encodeToString(imageBytes, Base64.DEFAULT);

                db.collection("usuarios")
                        .whereEqualTo("email", currentUserEmail)
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                                if (!queryDocumentSnapshots.isEmpty()) {
                                        String userId = queryDocumentSnapshots.getDocuments().get(0).getId();

                                        db.collection("usuarios")
                                                .document(userId)
                                                .collection("mascotas")
                                                .document(mascotaId)
                                                .update("imagenBase64", imageBase64)
                                                .addOnSuccessListener(aVoid ->
                                                        Toast.makeText(this, "Imagen guardada", Toast.LENGTH_SHORT).show()
                                                )
                                                .addOnFailureListener(e ->
                                                        Toast.makeText(this, "Error al guardar imagen", Toast.LENGTH_SHORT).show()
                                                );
                                }
                        })
                        .addOnFailureListener(e -> {
                                Toast.makeText(this, "Error al obtener usuario", Toast.LENGTH_SHORT).show();
                        });
        }


        private void loadExistingImage() {
                db.collection("usuarios")
                        .whereEqualTo("email", currentUserEmail)
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                                if (!queryDocumentSnapshots.isEmpty()) {
                                        String userId = queryDocumentSnapshots.getDocuments().get(0).getId();

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
                                                                        Glide.with(this)
                                                                                .load(imageBytes)
                                                                                .into(imageView);
                                                                }
                                                        }
                                                })
                                                .addOnFailureListener(e ->
                                                        Toast.makeText(this, "Error al cargar imagen", Toast.LENGTH_SHORT).show()
                                                );
                                }
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(this, "Error al buscar usuario", Toast.LENGTH_SHORT).show()
                        );
        }
}

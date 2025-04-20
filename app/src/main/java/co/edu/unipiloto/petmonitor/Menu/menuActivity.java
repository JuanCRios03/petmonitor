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
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import co.edu.unipiloto.petmonitor.R;

public class menuActivity extends AppCompatActivity {

        private static final int PICK_IMAGE_REQUEST = 1;
        private ImageView imageView;
        private FirebaseFirestore db;
        private String currentUserEmail;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_menu);

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
                // Convertir imagen a Base64 (para guardarla como string)
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos); // Comprimir imagen al 50%
                byte[] imageBytes = baos.toByteArray();
                String imageBase64 = Base64.encodeToString(imageBytes, Base64.DEFAULT);

                // Guardar en Firestore
                db.collection("usuarios").document(currentUserEmail)
                        .update("imagenBase64", imageBase64)
                        .addOnSuccessListener(aVoid ->
                                Toast.makeText(this, "Imagen guardada", Toast.LENGTH_SHORT).show()
                        )
                        .addOnFailureListener(e ->
                                Toast.makeText(this, "Error al guardar imagen", Toast.LENGTH_SHORT).show()
                        );
        }

        private void loadExistingImage() {
                db.collection("usuarios").document(currentUserEmail).get()
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
}


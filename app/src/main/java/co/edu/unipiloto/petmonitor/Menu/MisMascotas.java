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
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Map;

import co.edu.unipiloto.petmonitor.RolVeterinario.MenuVeterinario;
import co.edu.unipiloto.petmonitor.R;

public class MisMascotas extends AppCompatActivity {

    private LinearLayout layoutMascotas;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private TextView tituloMascotas;

    private boolean isUserVeterinarian;
    private String userID;

    // Variables para cuando un veterinario está viendo las mascotas de un cliente
    private boolean esVeterinarioViendoCliente = false;
    private String emailCliente;
    private String nombreCliente;
    private String clienteId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mis_mascotas);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        userID = auth.getCurrentUser().getUid();

        layoutMascotas = findViewById(R.id.layoutMascotas);
        tituloMascotas = findViewById(R.id.tituloMascotas);

        // Verificar si viene desde un veterinario viendo cliente
        Intent intent = getIntent();
        esVeterinarioViendoCliente = intent.getBooleanExtra("esVeterinarioViendoCliente", false);
        emailCliente = intent.getStringExtra("emailCliente");
        nombreCliente = intent.getStringExtra("nombreCliente");
        clienteId = intent.getStringExtra("clienteId");

        if (esVeterinarioViendoCliente && nombreCliente != null) {
            // Cambiar el título para mostrar que son las mascotas del cliente
            tituloMascotas.setText("Mascotas de " + nombreCliente);
            // Ocultar el botón de agregar mascota ya que el veterinario no puede agregar mascotas al cliente
            ImageButton btnAgregar = findViewById(R.id.btnAgregarMascota);
            btnAgregar.setVisibility(View.GONE);
        } else {
            // Comportamiento normal para usuarios regulares
            checkIfUserIsVeterinarian();
            ImageButton btnAgregar = findViewById(R.id.btnAgregarMascota);
            btnAgregar.setOnClickListener(v -> {
                Intent intentAgregar = new Intent(MisMascotas.this, co.edu.unipiloto.petmonitor.Register.RegisterPaso3Activity.class);
                startActivity(intentAgregar);
            });
        }
    }

    private void cargarMascotas() {
        if (layoutMascotas != null) {
            layoutMascotas.removeAllViews();
        }

        // Si es un veterinario viendo cliente y tenemos el clienteId
        if (esVeterinarioViendoCliente && clienteId != null && !clienteId.isEmpty()) {
            // Usar directamente el clienteId para acceder a las mascotas
            db.collection("usuarios")
                    .document(clienteId)
                    .collection("mascotas")
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        List<DocumentSnapshot> mascotas = snapshot.getDocuments();
                        if (mascotas.isEmpty()) {
                            agregarMensajeSinMascotas();
                        } else {
                            for (DocumentSnapshot doc : mascotas) {
                                Map<String, Object> data = doc.getData();
                                if (data != null && data.containsKey("nombreMascota")) {
                                    String nombre = data.get("nombreMascota").toString();
                                    String mascotaId = doc.getId();
                                    agregarCardMascota(nombre, mascotaId, clienteId);
                                }
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firestore", "Error al cargar mascotas del cliente", e);
                        agregarMensajeSinMascotas();
                    });
        } else {
            // Comportamiento normal para usuarios regulares (buscar por email)
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
                                        if (mascotas.isEmpty()) {
                                            agregarMensajeSinMascotas();
                                        } else {
                                            for (DocumentSnapshot doc : mascotas) {
                                                Map<String, Object> data = doc.getData();
                                                if (data != null && data.containsKey("nombreMascota")) {
                                                    String nombre = data.get("nombreMascota").toString();
                                                    String mascotaId = doc.getId();
                                                    agregarCardMascota(nombre, mascotaId, docId);
                                                }
                                            }
                                        }
                                    });
                        }
                    });
        }
    }

    private void agregarMensajeSinMascotas() {
        TextView mensaje = new TextView(this);
        if (esVeterinarioViendoCliente) {
            mensaje.setText("Este cliente no tiene mascotas registradas");
        } else {
            mensaje.setText("No tienes mascotas registradas");
        }
        mensaje.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        mensaje.setPadding(50, 100, 50, 100);
        mensaje.setTextSize(16);
        layoutMascotas.addView(mensaje);
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

        // AQUÍ ESTÁ EL PROBLEMA MÁS PROBABLE - AGREGAMOS LOGS PARA DEBUGGEAR
        card.setOnClickListener(v -> {
            try {
                Log.d("MisMascotas", "Click en mascota: " + nombre);
                Log.d("MisMascotas", "mascotaId: " + mascotaId);
                Log.d("MisMascotas", "esVeterinarioViendoCliente: " + esVeterinarioViendoCliente);

                if (esVeterinarioViendoCliente) {
                    Log.d("MisMascotas", "Navegando como veterinario");
                    Intent intent = new Intent(MisMascotas.this, MenuVeterinario.class);
                    intent.putExtra("mascotaId", mascotaId);
                    intent.putExtra("clienteEmail", emailCliente);
                    intent.putExtra("nombreCliente", nombreCliente);
                    intent.putExtra("clienteId", clienteId);
                    intent.putExtra("esVeterinarioViendoCliente", true);
                    startActivity(intent);
                } else {
                    Log.d("MisMascotas", "Navegando como usuario normal");

                    // VALIDACIONES ADICIONALES ANTES DE CREAR EL INTENT
                    if (mascotaId == null || mascotaId.isEmpty()) {
                        Log.e("MisMascotas", "Error: mascotaId es null o vacío");
                        Toast.makeText(this, "Error: ID de mascota inválido", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Intentar crear el Intent con manejo de errores
                    try {
                        Intent intent = new Intent(MisMascotas.this, menuActivity.class);
                        intent.putExtra("mascotaId", mascotaId);
                        Log.d("MisMascotas", "Intent creado exitosamente, iniciando actividad");
                        startActivity(intent);
                    } catch (Exception e) {
                        Log.e("MisMascotas", "Error al crear Intent para menuActivity", e);
                        Toast.makeText(this, "Error al abrir menú de mascota: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
            } catch (Exception e) {
                Log.e("MisMascotas", "Error general en onClick", e);
                Toast.makeText(this, "Error inesperado: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        layoutMascotas.addView(card);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!esVeterinarioViendoCliente) {
            getSharedPreferences("UserPrefs", MODE_PRIVATE)
                    .edit()
                    .putString("lastActivity", "MisMascotas")
                    .apply();
        }

        cargarMascotas();
    }
}
package co.edu.unipiloto.petmonitor.RolVeterinario;

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

import co.edu.unipiloto.petmonitor.R;

public class MisClientes extends AppCompatActivity {
    private LinearLayout layoutClientes;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private static final String TAG = "MisClientes";

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

        Log.d(TAG, "Email del veterinario: " + email);

        db.collection("usuarios")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        String docId = queryDocumentSnapshots.getDocuments().get(0).getId();
                        Log.d(TAG, "ID del veterinario: " + docId);

                        db.collection("usuarios")
                                .document(docId)
                                .collection("clientes")
                                .get()
                                .addOnSuccessListener(snapshot -> {
                                    List<DocumentSnapshot> clientes = snapshot.getDocuments();
                                    Log.d(TAG, "Número de clientes encontrados: " + clientes.size());

                                    for (DocumentSnapshot doc : clientes) {
                                        Map<String, Object> data = doc.getData();
                                        Log.d(TAG, "Datos del cliente: " + data);

                                        if (data != null && data.containsKey("nombre")) {
                                            String nombre = data.get("nombre").toString();
                                            String userID = doc.getId();
                                            Log.d(TAG, "Cliente encontrado - Nombre: " + nombre + ", ID: " + userID);
                                            agregarCardCliente(nombre, userID, docId, data);
                                        }
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error al cargar clientes: " + e.getMessage());
                                    Toast.makeText(this, "Error al cargar clientes", Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al encontrar veterinario: " + e.getMessage());
                    Toast.makeText(this, "Error al cargar datos del veterinario", Toast.LENGTH_SHORT).show();
                });
    }

    private void agregarCardCliente(String nombre, String userID, String veterinarianID, Map<String, Object> clienteData) {
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

        // Click del cliente - CON DEBUG COMPLETO
        card.setOnClickListener(v -> {
            Log.d(TAG, "Click en cliente: " + nombre);
            Log.d(TAG, "UserID: " + userID);
            Log.d(TAG, "VeterinarianID: " + veterinarianID);
            Log.d(TAG, "Datos completos del cliente: " + clienteData.toString());

            Toast.makeText(this, "Cargando mascotas de " + nombre + "...", Toast.LENGTH_SHORT).show();

            // El userID (ID del documento) ES el clienteId que necesitamos
            String clienteId = userID;

            // DEBUG: Verificar todos los posibles campos de email
            String emailCliente = null;

            // Buscar email con diferentes posibles nombres de campo
            if (clienteData.containsKey("email")) {
                emailCliente = clienteData.get("email").toString();
                Log.d(TAG, "Email encontrado con clave 'email': " + emailCliente);
            } else if (clienteData.containsKey("Email")) {
                emailCliente = clienteData.get("Email").toString();
                Log.d(TAG, "Email encontrado con clave 'Email': " + emailCliente);
            } else if (clienteData.containsKey("correo")) {
                emailCliente = clienteData.get("correo").toString();
                Log.d(TAG, "Email encontrado con clave 'correo': " + emailCliente);
            } else {
                Log.d(TAG, "No se encontró email en datos locales. Claves disponibles: " + clienteData.keySet().toString());
            }

            if (emailCliente != null && !emailCliente.isEmpty()) {
                // Si ya tenemos el email, redirigir directamente
                Log.d(TAG, "Redirigiendo con datos locales - Email: " + emailCliente + ", ClienteId: " + clienteId);
                redirigirAMascotas(emailCliente, nombre, clienteId);
            } else {
                // Si no tenemos el email en los datos locales, buscarlo en Firestore
                Log.d(TAG, "Buscando email en Firestore para cliente: " + userID);

                db.collection("usuarios")
                        .document(veterinarianID)
                        .collection("clientes")
                        .document(userID)
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                Log.d(TAG, "Documento del cliente encontrado en Firestore: " + documentSnapshot.getData());

                                // Buscar email con diferentes posibles nombres de campo
                                String email = null;
                                if (documentSnapshot.contains("email")) {
                                    email = documentSnapshot.getString("email");
                                    Log.d(TAG, "Email encontrado en Firestore con clave 'email': " + email);
                                } else if (documentSnapshot.contains("Email")) {
                                    email = documentSnapshot.getString("Email");
                                    Log.d(TAG, "Email encontrado en Firestore con clave 'Email': " + email);
                                } else if (documentSnapshot.contains("correo")) {
                                    email = documentSnapshot.getString("correo");
                                    Log.d(TAG, "Email encontrado en Firestore con clave 'correo': " + email);
                                } else {
                                    Log.d(TAG, "Campos disponibles en documento: " + documentSnapshot.getData().keySet().toString());
                                }

                                if (email != null && !email.isEmpty()) {
                                    Log.d(TAG, "Email encontrado en Firestore: " + email);
                                    redirigirAMascotas(email, nombre, clienteId);
                                } else {
                                    // ÚLTIMO RECURSO: Si no encontramos email, usar el clienteId que ya sabemos que funciona
                                    Log.w(TAG, "No se encontró email, usando clienteId directamente");
                                    redirigirAMascotasSinEmail(nombre, clienteId);
                                }
                            } else {
                                Log.e(TAG, "No se encontró el documento del cliente en Firestore");
                                Toast.makeText(this, "Error: No se pudo cargar la información del cliente", Toast.LENGTH_LONG).show();
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e(TAG, "Error al obtener email del cliente: " + e.getMessage());
                            // ÚLTIMO RECURSO: usar solo el clienteId
                            Log.w(TAG, "Error en Firestore, usando clienteId directamente");
                            redirigirAMascotasSinEmail(nombre, clienteId);
                        });
            }
        });

        layoutClientes.addView(card);
    }

    // Método para redirigir sin email (usando solo clienteId)
    private void redirigirAMascotasSinEmail(String nombreCliente, String clienteId) {
        Log.d(TAG, "Redirigiendo a MisMascotas SIN email - Nombre: " + nombreCliente + ", ClienteId: " + clienteId);

        Intent intent = new Intent(this, co.edu.unipiloto.petmonitor.Menu.MisMascotas.class);
        intent.putExtra("emailCliente", ""); // Email vacío
        intent.putExtra("nombreCliente", nombreCliente);
        intent.putExtra("clienteId", clienteId);
        intent.putExtra("esVeterinarioViendoCliente", true);

        Log.d(TAG, "Intent creado sin email, iniciando actividad...");
        startActivity(intent);
    }

    // Método redirigirAMascotas original (sin cambios)
    private void redirigirAMascotas(String emailCliente, String nombreCliente, String clienteId) {
        Log.d(TAG, "Redirigiendo a MisMascotas con email: " + emailCliente + ", nombre: " + nombreCliente + ", clienteId: " + clienteId);

        Intent intent = new Intent(this, co.edu.unipiloto.petmonitor.Menu.MisMascotas.class);
        intent.putExtra("emailCliente", emailCliente);
        intent.putExtra("nombreCliente", nombreCliente);
        intent.putExtra("clienteId", clienteId);
        intent.putExtra("esVeterinarioViendoCliente", true);

        Log.d(TAG, "Intent creado, iniciando actividad...");
        startActivity(intent);

        Log.d(TAG, "startActivity llamado exitosamente");
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
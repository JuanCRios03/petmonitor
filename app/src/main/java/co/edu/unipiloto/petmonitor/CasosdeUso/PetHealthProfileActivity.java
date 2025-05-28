package co.edu.unipiloto.petmonitor.CasosdeUso;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.edu.unipiloto.petmonitor.R;

public class PetHealthProfileActivity extends AppCompatActivity {

    private static final String TAG = "PetHealthProfileActivity";

    // Firebase
    private FirebaseFirestore db;
    private String petId;
    private String userId;
    private String userEmail;

    // Variables para veterinario
    private boolean esVeterinario = false;
    private boolean esVeterinarioViendoCliente = false;
    private String clienteId;
    private String clienteEmail;
    private String nombreCliente;
    private String mascotaIdFromIntent;

    // UI Elements
    private TextView weightValueTextView;
    private TextView ageValueTextView;
    private TextView diseasesValueTextView;
    private TextView allergiesValueTextView;
    private ImageView weightImageView;
    private ImageView ageImageView;
    private ImageView diseasesImageView;
    private ImageView allergiesImageView;
    private Button updateButton;

    // Variables para almacenar datos de salud
    private String weight = "";
    private String age = "";
    private List<String> diseases = new ArrayList<>();
    private List<String> allergies = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_salud_mascota);

        // Inicializar Firestore
        db = FirebaseFirestore.getInstance();

        // Obtener datos del Intent
        Intent intent = getIntent();
        esVeterinario = intent.getBooleanExtra("esVeterinario", false);
        esVeterinarioViendoCliente = intent.getBooleanExtra("esVeterinarioViendoCliente", false);
        clienteId = intent.getStringExtra("clienteId");
        clienteEmail = intent.getStringExtra("clienteEmail");
        nombreCliente = intent.getStringExtra("nombreCliente");
        mascotaIdFromIntent = intent.getStringExtra("mascotaId");

        Log.d(TAG, "Datos recibidos - esVeterinario: " + esVeterinario +
                ", esVeterinarioViendoCliente: " + esVeterinarioViendoCliente +
                ", clienteId: " + clienteId +
                ", mascotaId: " + mascotaIdFromIntent);

        if (esVeterinarioViendoCliente && clienteId != null && mascotaIdFromIntent != null) {
            // Veterinario viendo cliente específico
            userId = clienteId;
            petId = mascotaIdFromIntent;
            userEmail = clienteEmail != null ? clienteEmail : "";

            Log.d(TAG, "Modo veterinario - userId: " + userId + ", petId: " + petId);
            Toast.makeText(this, "Cargando perfil de salud de " + nombreCliente, Toast.LENGTH_SHORT).show();

            // Continuar con la inicialización
            initViews();
            setupListeners();
            loadPetHealthData();
        } else {
            // Usuario normal o veterinario sin cliente específico
            userEmail = getEmailFromPreferences();
            if (userEmail == null || userEmail.isEmpty()) {
                Toast.makeText(this, "No se pudo obtener el correo del usuario.", Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            Toast.makeText(this, "Buscando usuario con email: " + userEmail, Toast.LENGTH_SHORT).show();
            buscarUsuarioYMascota(userEmail);
        }
    }

    private String getEmailFromPreferences() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String email = sharedPreferences.getString("email", null);
        Log.d(TAG, "Email recuperado de SharedPreferences: " + email);
        return email;
    }

    private void buscarUsuarioYMascota(String email) {
        Log.d(TAG, "Buscando usuario con email: " + email);

        // Buscar al usuario por su email
        db.collection("usuarios")
                .whereEqualTo("email", email)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Usuario encontrado
                        userId = queryDocumentSnapshots.getDocuments().get(0).getId();
                        Log.d(TAG, "Usuario encontrado con ID: " + userId);
                        Toast.makeText(this, "Usuario encontrado: " + userId, Toast.LENGTH_SHORT).show();

                        // Buscar la primera mascota del usuario
                        db.collection("usuarios").document(userId)
                                .collection("mascotas")
                                .get()
                                .addOnSuccessListener(mascotasSnapshots -> {
                                    if (!mascotasSnapshots.isEmpty()) {
                                        // Tomar la primera mascota
                                        DocumentSnapshot mascotaSnapshot = mascotasSnapshots.getDocuments().get(0);
                                        petId = mascotaSnapshot.getId();
                                        Log.d(TAG, "Mascota encontrada con ID: " + petId);
                                        Toast.makeText(this, "Mascota encontrada: " + petId, Toast.LENGTH_SHORT).show();

                                        // Una vez que tenemos el ID del usuario y de la mascota, podemos continuar
                                        initViews();
                                        setupListeners();
                                        loadPetHealthData();
                                    } else {
                                        Log.d(TAG, "No se encontraron mascotas para el usuario");
                                        Toast.makeText(this, "No se encontraron mascotas para este usuario", Toast.LENGTH_LONG).show();
                                        finish();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Error al buscar mascotas: " + e.getMessage());
                                    Toast.makeText(this, "Error al buscar mascotas: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    finish();
                                });
                    } else {
                        Log.d(TAG, "No se encontró usuario con el email: " + email);
                        Toast.makeText(this, "No se encontró usuario con el email: " + email, Toast.LENGTH_LONG).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error al buscar usuario: " + e.getMessage());
                    Toast.makeText(this, "Error al buscar usuario: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    finish();
                });
    }

    private void initViews() {
        weightValueTextView = findViewById(R.id.weightValueTextView);
        ageValueTextView = findViewById(R.id.ageValueTextView);
        diseasesValueTextView = findViewById(R.id.diseasesValueTextView);
        allergiesValueTextView = findViewById(R.id.allergiesValueTextView);
        weightImageView = findViewById(R.id.weightImageView);
        ageImageView = findViewById(R.id.ageImageView);
        diseasesImageView = findViewById(R.id.diseasesImageView);
        allergiesImageView = findViewById(R.id.allergiesImageView);
        updateButton = findViewById(R.id.updateButton);
    }

    private void setupListeners() {
        View.OnClickListener weightClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditWeightDialog();
            }
        };

        // Añadir el listener al contenedor completo del peso
        findViewById(R.id.weightCardView).setOnClickListener(weightClickListener);
        weightImageView.setOnClickListener(weightClickListener);

        View.OnClickListener ageClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditAgeDialog();
            }
        };

        // Añadir el listener al contenedor completo de la edad
        findViewById(R.id.ageCardView).setOnClickListener(ageClickListener);
        ageImageView.setOnClickListener(ageClickListener);

        View.OnClickListener diseasesClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditDiseasesDialog();
            }
        };

        // Añadir el listener al contenedor completo de enfermedades
        findViewById(R.id.diseasesCardView).setOnClickListener(diseasesClickListener);
        diseasesImageView.setOnClickListener(diseasesClickListener);

        View.OnClickListener allergiesClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditAllergiesDialog();
            }
        };

        // Añadir el listener al contenedor completo de alergias
        findViewById(R.id.allergiesCardView).setOnClickListener(allergiesClickListener);
        allergiesImageView.setOnClickListener(allergiesClickListener);

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                savePetHealthData();
            }
        });
    }

    private void loadPetHealthData() {
        Log.d(TAG, "Cargando datos de salud para mascota: " + petId + " del usuario: " + userId);

        String mensaje = esVeterinarioViendoCliente ?
                "Cargando datos de salud de " + nombreCliente :
                "Cargando datos de salud para mascota: " + petId;
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();

        // Referencia directa a la subcoleción salud de la mascota
        DocumentReference healthRef = db.collection("usuarios").document(userId)
                .collection("mascotas").document(petId)
                .collection("salud").document("perfil");

        healthRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    Log.d(TAG, "Documento de salud encontrado");
                    Toast.makeText(PetHealthProfileActivity.this, "Datos de salud encontrados", Toast.LENGTH_SHORT).show();

                    // Extraer datos
                    if (documentSnapshot.contains("peso")) {
                        weight = documentSnapshot.getString("peso");
                        weightValueTextView.setText(weight);
                    }

                    if (documentSnapshot.contains("edad")) {
                        age = documentSnapshot.getString("edad");
                        ageValueTextView.setText(age);
                    }

                    if (documentSnapshot.contains("enfermedades")) {
                        diseases = (List<String>) documentSnapshot.get("enfermedades");
                        updateDiseasesTextView();
                    }

                    if (documentSnapshot.contains("alergias")) {
                        allergies = (List<String>) documentSnapshot.get("alergias");
                        updateAllergiesTextView();
                    }
                } else {
                    Log.d(TAG, "No se encontró documento de salud, cargando datos básicos");
                    Toast.makeText(PetHealthProfileActivity.this, "No se encontró perfil de salud, cargando datos básicos", Toast.LENGTH_SHORT).show();
                    loadDataFromPetDocument();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e(TAG, "Error al cargar datos de salud: " + e.getMessage());
                Toast.makeText(PetHealthProfileActivity.this,
                        "Error al cargar datos de salud: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();

                // Intentar cargar desde documento de mascota
                loadDataFromPetDocument();
            }
        });
    }

    // Método para actualizar el TextView de enfermedades
    private void updateDiseasesTextView() {
        if (diseases.isEmpty()) {
            diseasesValueTextView.setText("Ninguna");
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < diseases.size(); i++) {
                sb.append(diseases.get(i));
                if (i < diseases.size() - 1) {
                    sb.append(", ");
                }
            }
            diseasesValueTextView.setText(sb.toString());
        }
    }

    // Método para actualizar el TextView de alergias
    private void updateAllergiesTextView() {
        if (allergies.isEmpty()) {
            allergiesValueTextView.setText("Ninguna");
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < allergies.size(); i++) {
                sb.append(allergies.get(i));
                if (i < allergies.size() - 1) {
                    sb.append(", ");
                }
            }
            allergiesValueTextView.setText(sb.toString());
        }
    }

    private void loadDataFromPetDocument() {
        Log.d(TAG, "Cargando datos básicos de la mascota: " + petId);
        Toast.makeText(PetHealthProfileActivity.this, "Cargando datos básicos de la mascota", Toast.LENGTH_SHORT).show();

        // Obtener directamente del documento de la mascota
        db.collection("usuarios").document(userId)
                .collection("mascotas").document(petId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            Log.d(TAG, "Documento de mascota encontrado");
                            Toast.makeText(PetHealthProfileActivity.this,
                                    "Documento de mascota encontrado",
                                    Toast.LENGTH_SHORT).show();

                            if (documentSnapshot.contains("edad")) {
                                age = documentSnapshot.getString("edad");
                                ageValueTextView.setText(age);
                                Log.d(TAG, "Edad encontrada: " + age);
                            } else {
                                Log.d(TAG, "No se encontró campo 'edad' en el documento");
                            }

                            if (documentSnapshot.contains("peso")) {
                                weight = documentSnapshot.getString("peso");
                                weightValueTextView.setText(weight);
                                Log.d(TAG, "Peso encontrado: " + weight);
                            }
                        } else {
                            Log.d(TAG, "Documento de mascota no encontrado con ID: " + petId);
                            Toast.makeText(PetHealthProfileActivity.this,
                                    "Documento de mascota no encontrado con ID: " + petId,
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error al cargar documento de mascota: " + e.getMessage());
                        Toast.makeText(PetHealthProfileActivity.this,
                                "Error al cargar documento de mascota: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void savePetHealthData() {
        // Verificar que tengamos IDs válidos
        if (userId == null || petId == null) {
            Toast.makeText(this, "Error: No se han cargado correctamente los datos del usuario o mascota", Toast.LENGTH_LONG).show();
            return;
        }

        // Crear mapa con los datos a guardar
        Map<String, Object> healthData = new HashMap<>();
        healthData.put("peso", weight);
        healthData.put("edad", age);
        healthData.put("enfermedades", diseases);
        healthData.put("alergias", allergies);

        // Agregar información del veterinario si aplica
        if (esVeterinarioViendoCliente) {
            healthData.put("ultimaModificacionPorVeterinario", true);
            healthData.put("fechaUltimaModificacion", System.currentTimeMillis());
        }

        Log.d(TAG, "Guardando datos de salud - userId: " + userId + ", petId: " + petId);

        // Guardar en Firestore, siguiendo la estructura correcta
        db.collection("usuarios").document(userId)
                .collection("mascotas").document(petId)
                .collection("salud").document("perfil")
                .set(healthData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Perfil de salud actualizado correctamente");
                        String mensaje = esVeterinarioViendoCliente ?
                                "Perfil de salud de " + nombreCliente + " actualizado correctamente" :
                                "Perfil de salud actualizado correctamente";
                        Toast.makeText(PetHealthProfileActivity.this, mensaje, Toast.LENGTH_SHORT).show();

                        // También actualizar la edad y peso en el documento principal de la mascota
                        updateAgeInMainDocument();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error al guardar perfil de salud: " + e.getMessage());
                        Toast.makeText(PetHealthProfileActivity.this,
                                "Error al guardar: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateAgeInMainDocument() {
        // Actualizar la edad y el peso en el documento principal de la mascota
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("edad", age);
        updateData.put("peso", weight);

        db.collection("usuarios").document(userId)
                .collection("mascotas").document(petId)
                .update(updateData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Edad y peso actualizados en documento principal de la mascota");
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error al actualizar edad y peso en documento principal: " + e.getMessage());
                        // No mostrar error, ya que esto es secundario
                    }
                });
    }

    private void showEditWeightDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Actualizar peso");

        // Configurar el input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(weight);
        builder.setView(input);

        // Botones
        builder.setPositiveButton("Guardar", (dialog, which) -> {
            weight = input.getText().toString().trim();
            weightValueTextView.setText(weight);
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void showEditAgeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Actualizar edad");

        // Configurar el input
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(age);
        builder.setView(input);

        // Botones
        builder.setPositiveButton("Guardar", (dialog, which) -> {
            age = input.getText().toString().trim();
            ageValueTextView.setText(age);
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void showEditDiseasesDialog() {
        // Crear un layout para múltiples enfermedades
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enfermedades");

        // Inflar layout personalizado
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_list_edit, null);
        builder.setView(dialogView);

        // Por simplicidad, usaremos un solo EditText donde se ingresan separadas por comas
        EditText editText = dialogView.findViewById(R.id.editTextItem);

        // Convertir la lista a texto separado por comas
        StringBuilder sb = new StringBuilder();
        for (String disease : diseases) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(disease);
        }
        editText.setText(sb.toString());

        // Botones
        builder.setPositiveButton("Guardar", (dialog, which) -> {
            // Actualizar la lista desde el texto
            String[] items = editText.getText().toString().split(",");
            diseases.clear();
            for (String item : items) {
                String trimmed = item.trim();
                if (!trimmed.isEmpty()) {
                    diseases.add(trimmed);
                }
            }
            // Actualizar el TextView con las nuevas enfermedades
            updateDiseasesTextView();
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void showEditAllergiesDialog() {
        // Similar al diálogo de enfermedades
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Alergias");

        // Inflar layout personalizado
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_list_edit, null);
        builder.setView(dialogView);

        // Por simplicidad, usaremos un solo EditText
        EditText editText = dialogView.findViewById(R.id.editTextItem);

        // Convertir la lista a texto separado por comas
        StringBuilder sb = new StringBuilder();
        for (String allergy : allergies) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(allergy);
        }
        editText.setText(sb.toString());

        // Botones
        builder.setPositiveButton("Guardar", (dialog, which) -> {
            // Actualizar la lista desde el texto
            String[] items = editText.getText().toString().split(",");
            allergies.clear();
            for (String item : items) {
                String trimmed = item.trim();
                if (!trimmed.isEmpty()) {
                    allergies.add(trimmed);
                }
            }
            // Actualizar el TextView con las nuevas alergias
            updateAllergiesTextView();
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        builder.show();
    }
}
package co.edu.unipiloto.petmonitor.vacuna;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import co.edu.unipiloto.petmonitor.R;

public class HistorialVacunasActivity extends AppCompatActivity {

    // Etiqueta para logs, facilita la depuración
    private static final String TAG = "HistorialVacunasAct";

    // --- Variables Miembro (Componentes de UI y Datos) ---
    private RecyclerView recyclerViewHistorial; //La lista visual
    private VacunaAdapter vacunaAdapter;
    private List<Vacuna> vacunaList;
    private TextView tvHistorialVacio;
    private TextView tvTituloHistorial;
    private ProgressBar progressBar;

    private FirebaseFirestore db;
    private String currentUserEmail;

    // --- Método `onCreate` (Se ejecuta cuando se crea la Actividad) ---
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Vincula esta clase Java con su layout XML
        setContentView(R.layout.activity_historial_vacunas);

        // Inicializa la instancia de Firestore
        db = FirebaseFirestore.getInstance();

        // --- Paso Clave: Obtener el Email del usuario logueado ---
        // Accede a las preferencias guardadas (donde se guardó el email al hacer login)
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        // Intenta obtener el valor asociado a la clave "email"
        currentUserEmail = sharedPreferences.getString("email", null); // null si no se encuentra

        // Verifica si se pudo obtener el email. Si no, es un error crítico.
        if (currentUserEmail == null || currentUserEmail.isEmpty()) {
            // Llama a una función para manejar el error (muestra mensaje y cierra)
            handleError("Error: Sesión de usuario no encontrada. No se puede mostrar el historial.");
            return; // Detiene la ejecución de onCreate si no hay usuario
        }
        // Si llegamos aquí, tenemos el email. Lo mostramos en Logcat para depuración.
        Log.d(TAG, "Mostrando historial para el usuario: " + currentUserEmail);

        // --- Llama a métodos auxiliares para organizar el código ---
        inicializarVistas();       // Conecta las variables con los IDs del XML
        configurarRecyclerView();  // Prepara la lista para mostrar datos
        cargarHistorialVacunas();  // Inicia la carga de datos desde Firestore
    }

    // --- Método Auxiliar: Manejo de Errores Críticos ---
    private void handleError(String message) {
        Log.e(TAG, "Error Crítico: " + message); // Muestra el error en Logcat (nivel Error)
        // Muestra un mensaje corto al usuario
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        finish(); // Cierra esta actividad porque no puede continuar sin el email
    }

    // --- Método Auxiliar: Inicializar Vistas ---
    private void inicializarVistas() {
        // Usa findViewById para encontrar cada elemento en el layout por su ID
        Log.d(TAG, "Inicializando vistas...");
        recyclerViewHistorial = findViewById(R.id.recyclerViewHistorial);
        tvHistorialVacio = findViewById(R.id.textViewHistorialVacio);
        tvTituloHistorial = findViewById(R.id.textViewHistorialTitulo);
        progressBar = findViewById(R.id.progressBarHistorial);
        Log.d(TAG, "Vistas inicializadas.");
    }

    // --- Método Auxiliar: Configurar RecyclerView ---
    private void configurarRecyclerView() {
        Log.d(TAG, "Configurando RecyclerView...");
        // Optimización: si el tamaño de los items no cambia, mejora el rendimiento
        recyclerViewHistorial.setHasFixedSize(true);
        // Define cómo se organizarán los items (lista vertical estándar)
        recyclerViewHistorial.setLayoutManager(new LinearLayoutManager(this));
        // Crea la lista vacía donde se guardarán las vacunas
        vacunaList = new ArrayList<>();
        // Crea una instancia del adaptador, pasándole el contexto y la lista (aún vacía)
        vacunaAdapter = new VacunaAdapter(this, vacunaList);
        // Asigna el adaptador al RecyclerView. Ahora el RecyclerView sabe cómo mostrar los datos.
        recyclerViewHistorial.setAdapter(vacunaAdapter);
        Log.d(TAG, "RecyclerView configurado.");
    }

    // --- Método Principal: Cargar Historial desde Firestore ---
    private void cargarHistorialVacunas() {
        Log.i(TAG, "Iniciando carga de historial de vacunas para usuario: " + currentUserEmail);

        // --- Preparar UI para la Carga ---
        progressBar.setVisibility(View.VISIBLE);        // Muestra el círculo de carga
        tvHistorialVacio.setVisibility(View.GONE);      // Oculta el mensaje de "vacío"
        recyclerViewHistorial.setVisibility(View.GONE); // Oculta la lista (está vacía o desactualizada)

        // --- Construir la Consulta a Firestore ---
        // 1. Apunta a la colección "usuarios"
        // 2. Selecciona el documento específico del usuario actual usando su email
        // 3. Apunta a la subcolección "vacunas" DENTRO de ese documento de usuario
        // 4. Ordena los resultados. Usamos 'timestampRegistro' (fecha de guardado) en orden descendente (más nuevo primero)
        //    Si prefieres ordenar por 'fechaVacunacion' (que es String dd/MM/yyyy), la ordenación puede no ser precisa.
        db.collection("usuarios").document(currentUserEmail).collection("vacunas")
                .orderBy("timestampRegistro", Query.Direction.DESCENDING)
                .get() // Ejecuta la consulta para obtener los datos UNA VEZ
                .addOnCompleteListener(task -> { // Escucha cuando la tarea (consulta) termina
                    // --- Tarea Completada (Éxito o Fracaso) ---
                    Log.d(TAG, "Consulta a Firestore completada.");
                    progressBar.setVisibility(View.GONE); // Oculta el círculo de carga

                    // Verifica si la consulta fue exitosa
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Consulta exitosa.");
                        // Obtiene el resultado (un conjunto de documentos)
                        QuerySnapshot result = task.getResult();
                        vacunaList.clear(); // ¡Importante! Limpia la lista actual antes de llenarla de nuevo

                        // Verifica si el resultado no es nulo y si contiene documentos
                        if (result != null && !result.isEmpty()) {
                            Log.i(TAG, "Se encontraron " + result.size() + " registros de vacunas.");
                            // Itera sobre cada documento de vacuna encontrado
                            for (QueryDocumentSnapshot document : result) {
                                try {
                                    // Intenta convertir el documento de Firestore a un objeto Vacuna
                                    Vacuna vacuna = document.toObject(Vacuna.class);
                                    // Asigna el ID único del documento al objeto Vacuna (útil si necesitas referenciarlo)
                                    vacuna.setId(document.getId());
                                    // Añade la vacuna recuperada a nuestra lista local
                                    vacunaList.add(vacuna);
                                } catch (Exception e) {
                                    // Si ocurre un error en la conversión (ej. campo faltante o tipo incorrecto)
                                    Log.e(TAG, "Error al convertir documento Firestore a Vacuna ID: " + document.getId(), e);
                                    // Decide qué hacer: ¿continuar? ¿mostrar error? Aquí solo logueamos.
                                }
                            } // Fin del bucle for

                            // --- Actualizar UI con Datos ---
                            // Notifica al adaptador que los datos en 'vacunaList' han cambiado
                            vacunaAdapter.notifyDataSetChanged();
                            // Muestra el RecyclerView (ahora con datos)
                            recyclerViewHistorial.setVisibility(View.VISIBLE);
                            // Asegúrate que el texto de "vacío" esté oculto
                            tvHistorialVacio.setVisibility(View.GONE);
                            Log.i(TAG, "Historial cargado y mostrado en RecyclerView.");

                        } else {
                            // La consulta fue exitosa, pero no se encontraron vacunas
                            Log.i(TAG, "No se encontraron vacunas registradas para el usuario: " + currentUserEmail);
                            // Muestra el mensaje indicando que no hay vacunas
                            tvHistorialVacio.setText("No hay vacunas registradas."); // Asegura el texto correcto
                            tvHistorialVacio.setVisibility(View.VISIBLE);
                            // Mantén el RecyclerView oculto
                            recyclerViewHistorial.setVisibility(View.GONE);
                        }
                    } else {
                        Log.e(TAG, "Error al obtener historial de vacunas para " + currentUserEmail, task.getException());
                        // Informa al usuario del error
                        Toast.makeText(HistorialVacunasActivity.this, "Error al cargar el historial. Verifique conexión.", Toast.LENGTH_LONG).show();
                        // Muestra un mensaje de error en lugar del de "vacío"
                        tvHistorialVacio.setText("Error al cargar datos.");
                        tvHistorialVacio.setVisibility(View.VISIBLE);
                        // Mantén el RecyclerView oculto
                        recyclerViewHistorial.setVisibility(View.GONE);
                    }
                });
    }
}

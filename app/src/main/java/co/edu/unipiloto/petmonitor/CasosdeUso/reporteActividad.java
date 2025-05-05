package co.edu.unipiloto.petmonitor.CasosdeUso;

import android.app.DatePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import co.edu.unipiloto.petmonitor.R;


public class reporteActividad extends AppCompatActivity {

    private Spinner spinnerMascotas;
    private Button btnFechaInicio, btnFechaFin, btnConsultar, btnExportar;
    private TextView txtResultados, tvFechaInicio, tvFechaFin;
    private FirebaseFirestore db;
    private String idUsuario;
    private List<String> nombres = new ArrayList<>();
    private Map<String, String> mapaMascotas = new HashMap<>(); // nombre -> id
    private List<String> resultadosActuales = new ArrayList<>();
    private final SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private Calendar calendarInicio = Calendar.getInstance();
    private Calendar calendarFin = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reporte_actividad);

        // Inicializar vistas
        spinnerMascotas = findViewById(R.id.spinnerMascotas);
        btnFechaInicio = findViewById(R.id.btnFechaInicio);
        btnFechaFin = findViewById(R.id.btnFechaFin);
        tvFechaInicio = findViewById(R.id.tvFechaInicio);
        tvFechaFin = findViewById(R.id.tvFechaFin);
        btnConsultar = findViewById(R.id.btnConsultar);
        btnExportar = findViewById(R.id.btnExportarPDF);
        txtResultados = findViewById(R.id.txtResultados);

        // Inicializar Firebase
        idUsuario = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db = FirebaseFirestore.getInstance();

        // Configurar fecha inicial (hoy)
        actualizarFechaVista(tvFechaInicio, calendarInicio);
        actualizarFechaVista(tvFechaFin, calendarFin);

        // Configurar DatePicker para fecha inicio
        btnFechaInicio.setOnClickListener(v -> mostrarDatePicker(true));

        // Configurar DatePicker para fecha fin
        btnFechaFin.setOnClickListener(v -> mostrarDatePicker(false));

        // Cargar mascotas y configurar botones
        cargarMascotas();
        btnConsultar.setOnClickListener(v -> consultarDatos());
        btnExportar.setOnClickListener(v -> generarPDF());
    }

    private void mostrarDatePicker(final boolean esFechaInicio) {
        Calendar calendar = esFechaInicio ? calendarInicio : calendarFin;
        TextView textView = esFechaInicio ? tvFechaInicio : tvFechaFin;

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    actualizarFechaVista(textView, calendar);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        // Opcional: Establecer límites de fecha
        if (!esFechaInicio) {
            datePickerDialog.getDatePicker().setMinDate(calendarInicio.getTimeInMillis());
        }

        datePickerDialog.show();
    }

    private void actualizarFechaVista(TextView textView, Calendar calendar) {
        String fechaStr = formato.format(calendar.getTime());
        textView.setText(fechaStr);
    }

    private void cargarMascotas() {
        Log.d("ID Usuario", "ID del usuario: " + idUsuario);
        nombres.clear();

        db.collection("usuarios")
                .document(idUsuario)
                .collection("mascotas")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String nombreMascota = document.getString("nombreMascota");
                            if (nombreMascota != null) {
                                nombres.add(nombreMascota);
                                mapaMascotas.put(nombreMascota, document.getId());
                            }
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                                this,
                                android.R.layout.simple_spinner_item,
                                nombres
                        );
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerMascotas.setAdapter(adapter);

                    } else {
                        Log.e("Firestore", "Error al obtener mascotas", task.getException());
                        Toast.makeText(this, "Error al cargar mascotas", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void consultarDatos() {
        String nombreMascota = (String) spinnerMascotas.getSelectedItem();
        if (nombreMascota == null || nombreMascota.isEmpty()) {
            Toast.makeText(this, "Por favor, selecciona una mascota", Toast.LENGTH_SHORT).show();
            return;
        }

        String idMascota = mapaMascotas.get(nombreMascota);
        String fechaInicioStr = formato.format(calendarInicio.getTime());
        String fechaFinStr = formato.format(calendarFin.getTime());

        // Validar que fecha inicio no sea mayor que fecha fin
        if (calendarInicio.after(calendarFin)) {
            Toast.makeText(this, "La fecha de inicio no puede ser mayor a la fecha fin", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("usuarios")
                .document(idUsuario)
                .collection("mascotas")
                .document(idMascota)
                .collection("monitoreo")
                .whereGreaterThanOrEqualTo("fecha", fechaInicioStr)
                .whereLessThanOrEqualTo("fecha", fechaFinStr)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        StringBuilder resultados = new StringBuilder();
                        resultadosActuales.clear();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String fecha = document.getString("fecha");
                            String actividad = document.getString("actividad");
                            String duracion = document.getString("duracion");

                            String linea = "Fecha: " + fecha + ", Actividad: " + actividad + ", Duración: " + duracion;
                            resultados.append(linea).append("\n");
                            resultadosActuales.add(linea);
                        }

                        if (resultados.length() == 0) {
                            resultados.append("No se encontraron datos en ese rango.");
                            btnExportar.setVisibility(View.GONE);
                        } else {
                            btnExportar.setVisibility(View.VISIBLE);
                        }

                        txtResultados.setText(resultados.toString());
                    } else {
                        Toast.makeText(this, "Error al consultar datos", Toast.LENGTH_SHORT).show();
                        Log.e("Firestore", "Error getting documents: ", task.getException());
                    }
                });
    }

    private void generarPDF() {
        // 1. Crear el documento PDF
        PdfDocument pdfDocument = new PdfDocument();
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextSize(12f);

        // 2. Configurar página (tamaño A4)
        int pageWidth = 595; // Ancho en puntos (210mm)
        int pageHeight = 842; // Alto en puntos (297mm)
        int margin = 40;

        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        // 3. Escribir contenido del PDF
        int x = margin;
        int y = margin + 20;

        // Encabezado
        paint.setTextSize(16f);
        paint.setFakeBoldText(true);
        String titulo = "Reporte de Actividad - " + formato.format(new Date());
        canvas.drawText(titulo, x, y, paint);
        y += 30;

        // Información de la mascota
        paint.setTextSize(12f);
        paint.setFakeBoldText(false);
        String mascotaSeleccionada = (String) spinnerMascotas.getSelectedItem();
        String rangoFechas = "Mascota: " + mascotaSeleccionada + "\n" +
                "Período: " + tvFechaInicio.getText() + " al " + tvFechaFin.getText();

        for (String line : rangoFechas.split("\n")) {
            canvas.drawText(line, x, y, paint);
            y += 20;
        }
        y += 20;

        // Contenido principal
        paint.setTextSize(10f);
        for (String linea : resultadosActuales) {
            if (y > pageHeight - margin) {
                pdfDocument.finishPage(page);
                pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pdfDocument.getPages().size() + 1).create();
                page = pdfDocument.startPage(pageInfo);
                canvas = page.getCanvas();
                y = margin + 20;
            }
            canvas.drawText(linea, x, y, paint);
            y += 15;
        }
        pdfDocument.finishPage(page);

        // 4. Guardar el archivo
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = "Reporte_Mascota_" + timeStamp + ".pdf";
            File pdfFile;
            Uri pdfUri;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Para Android 10+ (API 29+)
                ContentResolver resolver = getContentResolver();
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

                pdfUri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues);
                if (pdfUri != null) {
                    try (OutputStream out = resolver.openOutputStream(pdfUri)) {
                        pdfDocument.writeTo(out);
                    }
                }
                pdfFile = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName);
            } else {
                // Para versiones anteriores a Android 10
                File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                pdfFile = new File(downloadsDir, fileName);

                try (FileOutputStream out = new FileOutputStream(pdfFile)) {
                    pdfDocument.writeTo(out);
                }

                // Notificar al sistema sobre el nuevo archivo
                MediaScannerConnection.scanFile(this,
                        new String[]{pdfFile.getAbsolutePath()},
                        new String[]{"application/pdf"},
                        null);

                pdfUri = Uri.fromFile(pdfFile);
            }

            // 5. Mostrar mensaje de éxito
            Toast.makeText(this, "PDF guardado en Descargas", Toast.LENGTH_LONG).show();

            // 6. Intentar abrir el PDF
            abrirDocumentoPDF(pdfFile, pdfUri);

        } catch (Exception e) {
            Toast.makeText(this, "Error al guardar PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e("GenerarPDF", "Error", e);
        } finally {
            pdfDocument.close();
        }
    }

    private void abrirDocumentoPDF(File pdfFile, Uri pdfUri) {
        Intent intent = new Intent(Intent.ACTION_VIEW);

        // Configurar el Uri según la versión de Android
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            intent.setDataAndType(pdfUri, "application/pdf");
        } else {
            Uri uri = FileProvider.getUriForFile(this,
                    getApplicationContext().getPackageName() + ".provider",
                    pdfFile);
            intent.setDataAndType(uri, "application/pdf");
        }

        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

        // Verificar si hay aplicaciones disponibles
        PackageManager packageManager = getPackageManager();
        List<ResolveInfo> activities = packageManager.queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);

        if (activities.size() > 0) {
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                mostrarOpcionesParaVisualizarPDF();
            }
        } else {
            mostrarOpcionesParaVisualizarPDF();
        }
    }

    private void mostrarOpcionesParaVisualizarPDF() {
        new AlertDialog.Builder(this)
                .setTitle("Visor de PDF no encontrado")
                .setMessage("Para ver este reporte necesitas una aplicación para abrir PDFs. ¿Deseas instalar un visor de PDF ahora?")
                .setPositiveButton("Instalar", (dialog, which) -> {
                    abrirPlayStoreParaVisorPDF();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void abrirPlayStoreParaVisorPDF() {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=com.adobe.reader"))); // Adobe Reader
        } catch (ActivityNotFoundException e) {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=com.adobe.reader")));
            } catch (Exception ex) {
                Toast.makeText(this, "No se pudo abrir Play Store", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
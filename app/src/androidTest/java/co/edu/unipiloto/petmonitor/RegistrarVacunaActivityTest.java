package co.edu.unipiloto.petmonitor.Tests;

import static org.mockito.Mockito.*;

import android.widget.EditText;
import android.widget.Spinner;
import android.content.Context;
import android.widget.ArrayAdapter;

import androidx.test.core.app.ApplicationProvider;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;

import co.edu.unipiloto.petmonitor.CasosdeUso.RegistrarVacunaActivity;

public class PruebaRegistro {

    @Mock
    FirebaseAuth mockAuth;

    @Mock
    FirebaseUser mockUser;

    @Mock
    FirebaseFirestore mockFirestore;

    @InjectMocks
    RegistrarVacunaActivity activity;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        Context context = ApplicationProvider.getApplicationContext();

        activity = new RegistrarVacunaActivity();

        // Simulamos los campos del formulario
        activity.fechaVacunacion = new EditText(context);
        activity.tipoVacuna = new EditText(context);
        activity.dosis = new EditText(context);
        activity.lote = new EditText(context);
        activity.veterinario = new EditText(context);
        activity.observaciones = new EditText(context);
        activity.spinnerMascotas = new Spinner(context);

        activity.listaMascotas = new ArrayList<>();
        activity.listaMascotasIds = new ArrayList<>();
    }

    @Test
    public void testGuardarVacuna_camposObligatoriosVacios() {
        // Datos incompletos
        activity.fechaVacunacion.setText(""); // Campo obligatorio vacío
        activity.tipoVacuna.setText("Rabia");
        activity.mascotaId = null;

        activity.auth = mockAuth;
        when(mockAuth.getCurrentUser()).thenReturn(mockUser);

        activity.guardarVacuna();

        // No debe fallar ni intentar guardar
    }

    @Test
    public void testGuardarVacuna_usuarioNoAutenticado() {
        // Datos válidos pero usuario no autenticado
        activity.fechaVacunacion.setText("20/05/2025");
        activity.tipoVacuna.setText("Rabia");
        activity.mascotaId = "mascotaId123";

        activity.auth = mockAuth;
        when(mockAuth.getCurrentUser()).thenReturn(null);

        activity.guardarVacuna();

        // Debe mostrar mensaje de error y no continuar
    }

    @Test
    public void testGuardarVacuna_datosCompletos() {
        activity.fechaVacunacion.setText("20/05/2025");
        activity.tipoVacuna.setText("Rabia");
        activity.dosis.setText("1 ml");
        activity.lote.setText("L123");
        activity.veterinario.setText("Dr. Juan");
        activity.observaciones.setText("Sin efectos secundarios");

        activity.mascotaId = "mascotaId123";
        activity.userId = "usuarioId123";

        activity.auth = mockAuth;
        activity.db = mockFirestore;

        when(mockAuth.getCurrentUser()).thenReturn(mockUser);
        when(mockUser.getDisplayName()).thenReturn("Bryan Diaz");
        when(mockUser.getEmail()).thenReturn("bryan@correo.com");

        activity.listaMascotas = new ArrayList<>();
        activity.listaMascotas.add("Milo");

        // Simular selección en el Spinner
        Context context = ApplicationProvider.getApplicationContext();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, activity.listaMascotas);
        activity.spinnerMascotas.setAdapter(adapter);
        activity.spinnerMascotas.setSelection(0);

        activity.guardarVacuna();

        // Solo estamos verificando que el flujo no lance errores
    }
}

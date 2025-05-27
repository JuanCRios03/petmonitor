package co.edu.unipiloto.petmonitor;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ZonaSeguraManagerTest {

    private FirebaseFirestore mockDb;
    private CollectionReference mockUsuariosCollection;
    private DocumentReference mockUserRef;
    private CollectionReference mockMascotasCollection;
    private DocumentReference mockMascotaRef;
    private CollectionReference mockZonaSeguraCollection;

    private ZonaSeguraManager manager;

    @Before
    public void setUp() {
        mockDb = mock(FirebaseFirestore.class);
        mockUsuariosCollection = mock(CollectionReference.class);
        mockUserRef = mock(DocumentReference.class);
        mockMascotasCollection = mock(CollectionReference.class);
        mockMascotaRef = mock(DocumentReference.class);
        mockZonaSeguraCollection = mock(CollectionReference.class);

        when(mockDb.collection("usuarios")).thenReturn(mockUsuariosCollection);
        when(mockUsuariosCollection.document(anyString())).thenReturn(mockUserRef);
        when(mockUserRef.collection("mascotas")).thenReturn(mockMascotasCollection);
        when(mockMascotasCollection.document(anyString())).thenReturn(mockMascotaRef);
        when(mockMascotaRef.collection("zonasegura")).thenReturn(mockZonaSeguraCollection);

        manager = new ZonaSeguraManager(mockDb);
    }

    @Test
    public void guardarZonaSegura_exitoCallbackDebeEjecutarse() throws InterruptedException {
        TaskCompletionSource<DocumentReference> taskSource = new TaskCompletionSource<>();
        taskSource.setResult(mock(DocumentReference.class)); // Simula éxito

        when(mockZonaSeguraCollection.add(anyMap())).thenReturn(taskSource.getTask());

        final boolean[] callbackCalled = {false};
        CountDownLatch latch = new CountDownLatch(1);

        manager.guardarZonaSegura("usuario123", "mascotaABC", 4.0, -74.0, 100, new ZonaSeguraManager.Callback() {
            @Override
            public void onSuccess() {
                callbackCalled[0] = true;
                latch.countDown();
            }

            @Override
            public void onFailure(Exception e) {
                fail("No se esperaba error");
            }
        });

        latch.await(2, TimeUnit.SECONDS);

        assertTrue(callbackCalled[0]);
        verify(mockZonaSeguraCollection).add(anyMap());
    }

    @Test
    public void guardarZonaSegura_errorCallbackDebeEjecutarse() throws InterruptedException {
        TaskCompletionSource<DocumentReference> taskSource = new TaskCompletionSource<>();
        Exception expectedException = new Exception("Firestore Error");
        taskSource.setException(expectedException); // Simula fallo

        when(mockZonaSeguraCollection.add(anyMap())).thenReturn(taskSource.getTask());

        final boolean[] errorCallbackCalled = {false};
        CountDownLatch latch = new CountDownLatch(1);

        manager.guardarZonaSegura("usuario123", "mascotaABC", 4.0, -74.0, 100, new ZonaSeguraManager.Callback() {
            @Override
            public void onSuccess() {
                fail("Se esperaba un error");
            }

            @Override
            public void onFailure(Exception e) {
                errorCallbackCalled[0] = true;
                assertEquals("Firestore Error", e.getMessage());
                latch.countDown();
            }
        });

        latch.await(2, TimeUnit.SECONDS);

        assertTrue(errorCallbackCalled[0]);
        verify(mockZonaSeguraCollection).add(anyMap());
    }


    // Nuevo: Validación radio inválido (<= 0)
    @Test
    public void guardarZonaSegura_radioNoValido_debeLlamarError() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] errorCallbackCalled = {false};

        manager.guardarZonaSegura("usuario123", "mascotaABC", 4.0, -74.0, -5, new ZonaSeguraManager.Callback() {
            @Override
            public void onSuccess() {
                fail("Se esperaba error por radio inválido");
            }

            @Override
            public void onFailure(Exception e) {
                errorCallbackCalled[0] = true;
                assertTrue(e instanceof IllegalArgumentException);
                assertTrue(e.getMessage().contains("radio inválido"));
                latch.countDown();
            }
        });

        latch.await(2, TimeUnit.SECONDS);
        assertTrue(errorCallbackCalled[0]);
    }

    // Nuevo: Validación ubicación por defecto emulador
    @Test
    public void guardarZonaSegura_ubicacionPorDefectoEmulador_debeLlamarError() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] errorCallbackCalled = {false};

        // Ubicación típica del emulador (Googleplex)
        double latEmulador = 37.4219983;
        double lonEmulador = -122.084;

        manager.guardarZonaSegura("usuario123", "mascotaABC", latEmulador, lonEmulador, 100, new ZonaSeguraManager.Callback() {
            @Override
            public void onSuccess() {
                fail("Se esperaba error por ubicación por defecto del emulador");
            }

            @Override
            public void onFailure(Exception e) {
                errorCallbackCalled[0] = true;
                assertTrue(e instanceof IllegalArgumentException);
                assertTrue(e.getMessage().contains("ubicación por defecto del emulador"));
                latch.countDown();
            }
        });

        latch.await(2, TimeUnit.SECONDS);
        assertTrue(errorCallbackCalled[0]);
    }

    // Nuevo: Validación ubicación no obtenida
    @Test
    public void guardarZonaSegura_ubicacionNoObtenida_debeLlamarError() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        final boolean[] errorCallbackCalled = {false};

        // Ubicación inválida: latitud fuera de rango (más de 90), longitud fuera de rango (más de 180),
        // o caso común que indica ausencia de ubicación real (ej: 0,0)
        manager.guardarZonaSegura("usuario123", "mascotaABC", 0.0, 0.0, 100, new ZonaSeguraManager.Callback() {
            @Override
            public void onSuccess() {
                fail("Se esperaba error por ubicación inválida");
            }

            @Override
            public void onFailure(Exception e) {
                errorCallbackCalled[0] = true;
                assertTrue(e instanceof IllegalArgumentException);
                assertTrue(e.getMessage().contains("latitud inválida")
                        || e.getMessage().contains("longitud inválida")
                        || e.getMessage().contains("ubicación por defecto del emulador")
                        || e.getMessage().contains("ubicación inválida"));
                latch.countDown();
            }
        });

        latch.await(2, TimeUnit.SECONDS);
        assertTrue(errorCallbackCalled[0]);
    }
}






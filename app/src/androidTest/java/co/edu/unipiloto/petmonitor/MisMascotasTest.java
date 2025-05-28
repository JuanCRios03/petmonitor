package co.edu.unipiloto.petmonitor;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import co.edu.unipiloto.petmonitor.Menu.MisMascotas;
import co.edu.unipiloto.petmonitor.R;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class MisMascotasTest {

    @Test
    public void testTituloMascotasVisible() {
        ActivityScenario.launch(MisMascotas.class);
        onView(withId(R.id.tituloMascotas))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testBotonAgregarVisibleSiNoEsVeterinario() {
        ActivityScenario.launch(MisMascotas.class);
        onView(withId(R.id.btnAgregarMascota))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testOcultaBotonAgregarSiEsVeterinarioViendoCliente() {
        Intent intent = new Intent();
        intent.putExtra("esVeterinarioViendoCliente", true);
        intent.putExtra("nombreCliente", "Juan Pérez");
        ActivityScenario.launch(MisMascotas.class, intent.getExtras());

        onView(withId(R.id.btnAgregarMascota))
                .check(matches(withEffectiveVisibility(Visibility.GONE)));

        onView(withId(R.id.tituloMascotas))
                .check(matches(withText("Mascotas de Juan Pérez")));
    }

    @Test
    public void testMensajeSinMascotasVisibleCuandoListaVacia() {
        // Este test requiere que el usuario autenticado no tenga mascotas.
        ActivityScenario.launch(MisMascotas.class);

        onView(withText("No tienes mascotas registradas"))
                .check(matches(isDisplayed()));
    }
}

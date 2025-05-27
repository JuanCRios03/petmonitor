package co.edu.unipiloto.petmonitor;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;

import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import org.junit.Rule;
import org.junit.Test;

import co.edu.unipiloto.petmonitor.CasosdeUso.monitoreoEjercicio;

public class MonitoreoEjercicioTest {

    @Rule
    public ActivityScenarioRule<monitoreoEjercicio> activityRule =
            new ActivityScenarioRule<>(monitoreoEjercicio.class);

    @Test
    public void iniciarContador_ocultaImagenYMuestraContador() {
        onView(withId(R.id.btnIniciar)).perform(click());

        onView(withId(R.id.imageView)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
        onView(withId(R.id.tvContador)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
    }

    @Test
    public void pausarContador_detieneRunnable() throws InterruptedException {
        onView(withId(R.id.btnIniciar)).perform(click());
        Thread.sleep(2000);
        onView(withId(R.id.btnPausar)).perform(click());
        // Afirmaciones más complejas requerirían acceder al valor de seconds o duracion
    }

    @Test
    public void finalizarContador_muestraImagenYEscondeContador() {
        onView(withId(R.id.btnIniciar)).perform(click());
        onView(withId(R.id.btnFinalizar)).perform(click());

        onView(withId(R.id.tvContador)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
        onView(withId(R.id.imageView)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
    }
}
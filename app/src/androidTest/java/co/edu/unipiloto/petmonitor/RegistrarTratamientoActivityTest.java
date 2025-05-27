package co.edu.unipiloto.petmonitor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import co.edu.unipiloto.petmonitor.CasosdeUso.RegistrarTratamientoActivity;

@RunWith(AndroidJUnit4.class)
public class RegistrarTratamientoActivityTest {

    @Rule
    public ActivityScenarioRule<RegistrarTratamientoActivity> activityRule =
            new ActivityScenarioRule<>(RegistrarTratamientoActivity.class);


    @Test
    public void testObtenerHorasDesdeFrecuencia_Diario() {
        activityRule.getScenario().onActivity(activity -> {
            int horas = activity.obtenerHorasDesdeFrecuencia("Diario");
            assertEquals(24, horas);
        });
    }


    @Test
    public void testCalcularDuracionDias_MismoDia() {
        activityRule.getScenario().onActivity(activity -> {
            int dias = activity.calcularDuracionDias("27/05/2025", "27/05/2025");
            assertEquals(1, dias);
        });
    }

    @Test
    public void testCalcularDuracionDias_DiasConsecutivos() {
        activityRule.getScenario().onActivity(activity -> {
            int dias = activity.calcularDuracionDias("25/05/2025", "27/05/2025");
            assertEquals(3, dias);
        });
    }

    @Test
    public void testCalcularDuracionDias_FechaFinAntes() {
        activityRule.getScenario().onActivity(activity -> {
            int dias = activity.calcularDuracionDias("27/05/2025", "25/05/2025");
            assertTrue(dias < 1); // Espera que sea cero o negativo porque fin es antes de inicio
        });
    }
}
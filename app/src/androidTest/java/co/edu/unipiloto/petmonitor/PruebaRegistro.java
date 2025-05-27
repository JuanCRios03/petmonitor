package co.edu.unipiloto.petmonitor;

import org.junit.Test;
import static org.junit.Assert.*;

public class PruebaRegistro {

    @Test
    public void camposCompletos_debenSerValidos() {
        boolean esValido = ValidacionesVacuna.validarCampos("25/05/2025", "Rabia", "mascota123");
        assertTrue(esValido);
    }

    @Test
    public void camposVacios_noDebenSerValidos() {
        assertFalse(ValidacionesVacuna.validarCampos("", "Rabia", "mascota123"));
        assertFalse(ValidacionesVacuna.validarCampos("25/05/2025", "", "mascota123"));
        assertFalse(ValidacionesVacuna.validarCampos("25/05/2025", "Rabia", ""));
    }

    @Test
    public void camposNull_noDebenSerValidos() {
        assertFalse(ValidacionesVacuna.validarCampos(null, "Rabia", "mascota123"));
        assertFalse(ValidacionesVacuna.validarCampos("25/05/2025", null, "mascota123"));
        assertFalse(ValidacionesVacuna.validarCampos("25/05/2025", "Rabia", null));
    }
}

package co.edu.unipiloto.petmonitor;

import org.junit.Test;
import static org.junit.Assert.*;

/*Pruebas unitarias*/

public class PruebaRegistro {

    @Test
    public void camposCompletos_debenSerValidos() {
        boolean esValido = ValidacionesRegistro.validarCampos("Tomás", "Pérez", "tomas@email.com");
        assertTrue(esValido);
    }

    @Test
    public void camposVacios_noDebenSerValidos() {
        assertFalse(ValidacionesRegistro.validarCampos("", "Pérez", "tomas@email.com"));
        assertFalse(ValidacionesRegistro.validarCampos("Tomás", "", "tomas@email.com"));
        assertFalse(ValidacionesRegistro.validarCampos("Tomás", "Pérez", ""));
    }

    @Test
    public void camposNull_noDebenSerValidos() {
        assertFalse(ValidacionesRegistro.validarCampos(null, "Pérez", "tomas@email.com"));
        assertFalse(ValidacionesRegistro.validarCampos("Tomás", null, "tomas@email.com"));
        assertFalse(ValidacionesRegistro.validarCampos("Tomás", "Pérez", null));
    }
}

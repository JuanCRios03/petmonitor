package co.edu.unipiloto.petmonitor;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PruebaInicioSesion {
    @Test
    public void camposCompletos_debenSerValidos() {
        assertTrue(ValidacionInicioSesion.camposCompletos("test@gmail.com", "123456"));
    }

    @Test
    public void camposVacios_noDebenSerValidos() {
        assertFalse(ValidacionInicioSesion.camposCompletos("", ""));
        assertFalse(ValidacionInicioSesion.camposCompletos("correo@algo.com", ""));
        assertFalse(ValidacionInicioSesion.camposCompletos("", "pass"));
    }

    @Test
    public void passwordCorrecta_debeValidar() {
        assertTrue(ValidacionInicioSesion.esPasswordCorrecta("abc123", "abc123"));
    }

    @Test
    public void passwordIncorrecta_debeFallar() {
        assertFalse(ValidacionInicioSesion.esPasswordCorrecta("abc123", "xyz456"));
    }
}

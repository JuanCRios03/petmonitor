package co.edu.unipiloto.petmonitor;

public class ValidacionesRegistro {

    public static boolean validarCampos(String nombre, String apellido, String email) {
        return !(nombre == null || nombre.trim().isEmpty() ||
                apellido == null || apellido.trim().isEmpty() ||
                email == null || email.trim().isEmpty());
    }
}

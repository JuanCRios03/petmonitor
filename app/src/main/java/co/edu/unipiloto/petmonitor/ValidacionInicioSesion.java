package co.edu.unipiloto.petmonitor;

/*Pruebas unitarias*/

public class ValidacionInicioSesion {

    public static boolean camposCompletos(String email, String password) {
        return email != null && !email.trim().isEmpty()
                && password != null && !password.trim().isEmpty();
    }

    public static boolean esPasswordCorrecta(String ingresada, String almacenada) {
        return ingresada != null && almacenada != null && ingresada.equals(almacenada);
    }

}

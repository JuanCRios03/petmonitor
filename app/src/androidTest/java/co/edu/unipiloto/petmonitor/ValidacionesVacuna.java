package co.edu.unipiloto.petmonitor;

public class ValidacionesVacuna {

    public static boolean validarCampos(String fecha, String tipo, String mascotaId) {
        return fecha != null && !fecha.trim().isEmpty()
                && tipo != null && !tipo.trim().isEmpty()
                && mascotaId != null && !mascotaId.trim().isEmpty();
    }
}

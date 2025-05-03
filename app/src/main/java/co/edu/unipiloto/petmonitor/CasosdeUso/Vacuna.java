package co.edu.unipiloto.petmonitor.CasosdeUso;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class Vacuna {

    @Exclude // Firestore no intentará guardar/leer este campo directamente
    private String id; // ID del documento de la vacuna en Firestore

    // Datos del Dueño (Redundante)
    private String ownerNombre;
    private String ownerApellido;
    private String ownerEmail;

    // Datos de la Mascota (Redundante)
    private String petNombre;
    private String petEspecie;
    private String petRaza;
    private String petPeso;

    // Datos de la Vacuna
    private String fechaVacunacion; // Fecha como String (según el código anterior)
    private String tipoVacuna;
    private String dosis;
    private String lote;
    private String veterinario;
    private String observaciones;

    @ServerTimestamp // Firestore asignará la fecha/hora del servidor al crear/actualizar
    private Date timestampRegistro;

    // --- Constructores ---

    /**
     * Constructor vacío requerido por Firestore para la deserialización.
     * ¡No lo elimines!
     */
    public Vacuna() {
    }

    /**
     * Constructor completo para crear instancias de Vacuna fácilmente.
     */
    public Vacuna(String ownerNombre, String ownerApellido, String ownerEmail,
                  String petNombre, String petEspecie, String petRaza, String petPeso,
                  String fechaVacunacion, String tipoVacuna, String dosis, String lote,
                  String veterinario, String observaciones) {
        this.ownerNombre = ownerNombre;
        this.ownerApellido = ownerApellido;
        this.ownerEmail = ownerEmail;
        this.petNombre = petNombre;
        this.petEspecie = petEspecie;
        this.petRaza = petRaza;
        this.petPeso = petPeso;
        this.fechaVacunacion = fechaVacunacion;
        this.tipoVacuna = tipoVacuna;
        this.dosis = dosis;
        this.lote = lote;
        this.veterinario = veterinario;
        this.observaciones = observaciones;
        // timestampRegistro se establecerá automáticamente por Firestore
    }

    // --- Getters (Métodos para obtener los valores de los campos) ---

    public String getId() {
        return id;
    }

    public String getOwnerNombre() {
        return ownerNombre;
    }

    public String getOwnerApellido() {
        return ownerApellido;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public String getPetNombre() {
        return petNombre;
    }

    public String getPetEspecie() {
        return petEspecie;
    }

    public String getPetRaza() {
        return petRaza;
    }

    public String getPetPeso() {
        return petPeso;
    }

    public String getFechaVacunacion() {
        return fechaVacunacion;
    }

    public String getTipoVacuna() {
        return tipoVacuna;
    }

    public String getDosis() {
        return dosis;
    }

    public String getLote() {
        return lote;
    }

    public String getVeterinario() {
        return veterinario;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public Date getTimestampRegistro() {
        return timestampRegistro;
    }

    // --- Setters (Métodos para establecer los valores de los campos) ---

    /**
     * Establece el ID del documento de Firestore. Útil después de recuperar el objeto.
     * @param id El ID único del documento.
     */
    public void setId(String id) {
        this.id = id;
    }

    public void setOwnerNombre(String ownerNombre) {
        this.ownerNombre = ownerNombre;
    }

    public void setOwnerApellido(String ownerApellido) {
        this.ownerApellido = ownerApellido;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }

    public void setPetNombre(String petNombre) {
        this.petNombre = petNombre;
    }

    public void setPetEspecie(String petEspecie) {
        this.petEspecie = petEspecie;
    }

    public void setPetRaza(String petRaza) {
        this.petRaza = petRaza;
    }

    public void setPetPeso(String petPeso) {
        this.petPeso = petPeso;
    }

    public void setFechaVacunacion(String fechaVacunacion) {
        this.fechaVacunacion = fechaVacunacion;
    }

    public void setTipoVacuna(String tipoVacuna) {
        this.tipoVacuna = tipoVacuna;
    }

    public void setDosis(String dosis) {
        this.dosis = dosis;
    }

    public void setLote(String lote) {
        this.lote = lote;
    }

    public void setVeterinario(String veterinario) {
        this.veterinario = veterinario;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    /**
     * Establece el timestamp de registro. Firestore maneja esto automáticamente
     * con @ServerTimestamp, pero el setter puede ser útil para pruebas o casos específicos.
     * @param timestampRegistro La fecha/hora del registro.
     */
    public void setTimestampRegistro(Date timestampRegistro) {
        this.timestampRegistro = timestampRegistro;
    }


    public String getNombre() { return ownerEmail; }
    public String getNombreMascota() { return petNombre; }
    public String getTipo() { return tipoVacuna; }
    public String getFecha() { return fechaVacunacion; }

}


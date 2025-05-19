package co.edu.unipiloto.petmonitor.CasosdeUso;

import java.util.Date;

/*public class Cumplimiento {
    private String id;
    private String descripcion;
    private Date fecha;

    public Cumplimiento() {} // Constructor vacío para Firestore

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getDescripcion() {
        return descripcion;
    }
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Date getFecha() {
        return fecha;
    }
    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }
}*/



public class Cumplimiento {
    private Date fechaRegistro;
    private String nombreMedicamento;  // Nuevo campo

    // Constructor vacío requerido por Firestore
    public Cumplimiento() {}

    public Date getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(Date fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public String getNombreMedicamento() {
        return nombreMedicamento;
    }

    public void setNombreMedicamento(String nombreMedicamento) {
        this.nombreMedicamento = nombreMedicamento;
    }

    @Override
    public String toString() {
        return "Cumplimiento{fechaRegistro=" + fechaRegistro + ", nombreMedicamento=" + nombreMedicamento + '}';
    }
}




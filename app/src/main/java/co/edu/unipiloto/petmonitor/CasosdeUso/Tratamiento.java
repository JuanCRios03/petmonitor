package co.edu.unipiloto.petmonitor.CasosdeUso;

public class Tratamiento {
    private String medicamento;
    private String descripcion;
    private String frecuencia;
    private String hora;
    private String fechaInicio;
    private String fechaFin;
    private String fechaCumplimiento;
    private String id;
    public Tratamiento() {}

    // Getters y setters
    public String getMedicamento() { return medicamento; }
    public void setMedicamento(String medicamento) { this.medicamento = medicamento; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getFrecuencia() { return frecuencia; }
    public void setFrecuencia(String frecuencia) { this.frecuencia = frecuencia; }
    public String getHora() { return hora; }
    public void setHora(String hora) { this.hora = hora; }
    public String getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(String fechaInicio) { this.fechaInicio = fechaInicio; }
    public String getFechaFin() { return fechaFin; }
    public void setFechaFin(String fechaFin) { this.fechaFin = fechaFin; }
    public String getFechaCumplimiento() { return fechaCumplimiento; }
    public void setFechaCumplimiento(String fechaCumplimiento) { this.fechaCumplimiento = fechaCumplimiento; }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}


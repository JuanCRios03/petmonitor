package co.edu.unipiloto.petmonitor.CasosdeUso;

public class Veterinario {
    private String nombre;
    private String direccion;
    private double rating;
    private String placeId;
    private double latitude;
    private double longitude;

    public Veterinario(String nombre, String direccion, double rating, String placeId, double latitude, double longitude) {
        this.nombre = nombre;
        this.direccion = direccion;
        this.rating = rating;
        this.placeId = placeId;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDireccion() {
        return direccion;
    }

    public double getRating() {
        return rating;
    }

    public String getPlaceId() {
        return placeId;
    }

    public double getLatitud() {
        return latitude;
    }

    public double getLongitud() {
        return longitude;
    }
}

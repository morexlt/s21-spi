package propiera.modelo;

public record Persona(long id, String documento, String nombre, String contacto, boolean activo) {
    @Override
    public String toString() {
        return nombre + " · " + documento + (activo ? "" : " (inactivo)");
    }
}

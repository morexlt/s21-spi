package propiera.modelo;

public record Usuario(long id, String nombre, String rol) {
    public boolean puedeCargar() {
        return "ADMINISTRATIVO".equals(rol);
    }
}

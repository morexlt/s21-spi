package propiera.modelo;

// El formulario muestra la descripción, pero guarda el identificador.
public record Opcion(long id, String descripcion) {
    @Override
    public String toString() {
        return descripcion;
    }
}

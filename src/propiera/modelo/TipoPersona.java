package propiera.modelo;

public enum TipoPersona {
    PROPIETARIO("propietario"), INQUILINO("inquilino");

    private final String tabla;

    TipoPersona(String tabla) {
        this.tabla = tabla;
    }

    public String tabla() {
        return tabla;
    }
}

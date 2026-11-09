package propiera.modelo;

public record Propiedad(long id, String codigo, String direccion,
                        long propietario, String nombrePropietario, boolean activo) { }

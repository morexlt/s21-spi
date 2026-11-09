package propiera.modelo;

import java.util.List;

public record DatosCatalogo(List<Persona> propietarios, List<Persona> inquilinos,
                            List<Propiedad> propiedades) { }

package propiera.modelo;

import java.time.LocalDate;
import java.util.List;

public record EstadoContrato(long id, String propiedad, String propietario, String inquilino,
                             LocalDate inicio, LocalDate fin, List<CuotaContrato> cuotas) {
    public EstadoContrato {
        cuotas = List.copyOf(cuotas);
    }
}

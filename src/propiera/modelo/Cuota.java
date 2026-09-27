package propiera.modelo;

import java.math.BigDecimal;
import java.time.LocalDate;

public record Cuota(long id, long contrato, String propiedad, LocalDate periodo,
                    LocalDate vencimiento, BigDecimal importe, BigDecimal saldo) {
    public boolean permiteCobro() {
        return importe != null && saldo != null && saldo.signum() > 0;
    }
}

package propiera.modelo;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CuotaContrato(LocalDate periodo, LocalDate vencimiento, BigDecimal importe,
                            BigDecimal capitalCobrado, BigDecimal moraCobrada, BigDecimal saldo) { }

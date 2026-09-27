package propiera.modelo;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AltaContrato(long propiedad, long inquilino, LocalDate inicio,
                           LocalDate fin, BigDecimal importe, BigDecimal comision,
                           int ajusteMeses, int diaVencimiento, String reglaMora) { }

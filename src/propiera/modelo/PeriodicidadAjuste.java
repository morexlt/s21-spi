package propiera.modelo;

import java.math.BigDecimal;

public abstract class PeriodicidadAjuste {
    public abstract int mesesDelPrimerTramo();

    public final BigDecimal importeInicial(int numeroMes, BigDecimal importe) {
        if (numeroMes < 0 || importe == null || importe.signum() <= 0) {
            throw new IllegalArgumentException("Mes o importe inicial inválido");
        }
        return numeroMes < mesesDelPrimerTramo() ? importe : null;
    }

    public static PeriodicidadAjuste crear(int meses) {
        return switch (meses) {
            case 3 -> new AjusteTrimestral();
            case 6 -> new AjusteSemestral();
            default -> throw new IllegalArgumentException("Ajuste trimestral o semestral");
        };
    }
}

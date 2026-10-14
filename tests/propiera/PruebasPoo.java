package propiera;

import java.math.BigDecimal;
import propiera.modelo.AjusteSemestral;
import propiera.modelo.AjusteTrimestral;
import propiera.modelo.PeriodicidadAjuste;

public final class PruebasPoo {
    private static int total;

    private static void comprobar(String caso, boolean condicion) {
        if (!condicion) {
            throw new AssertionError(caso);
        }
        total++;
        System.out.println("PASS " + caso);
    }

    public static void main(String[] args) {
        BigDecimal inicial = new BigDecimal("300000.00");
        PeriodicidadAjuste trimestral = PeriodicidadAjuste.crear(3);
        PeriodicidadAjuste semestral = PeriodicidadAjuste.crear(6);

        comprobar("POO01 fábrica trimestral", trimestral instanceof AjusteTrimestral);
        comprobar("POO02 fábrica semestral", semestral instanceof AjusteSemestral);
        comprobar("POO03 trimestre meses 1 a 3", trimestral.importeInicial(0, inicial).equals(inicial)
            && trimestral.importeInicial(2, inicial).equals(inicial));
        comprobar("POO04 trimestre mes 4 pendiente", trimestral.importeInicial(3, inicial) == null);
        comprobar("POO05 semestre meses 1 a 6", semestral.importeInicial(3, inicial).equals(inicial)
            && semestral.importeInicial(5, inicial).equals(inicial));
        comprobar("POO06 semestre mes 7 pendiente", semestral.importeInicial(6, inicial) == null);
        comprobar("POO07 despacho polimórfico", trimestral.mesesDelPrimerTramo() == 3
            && semestral.mesesDelPrimerTramo() == 6);

        try {
            PeriodicidadAjuste.crear(4);
            throw new AssertionError("POO08 periodicidad inválida");
        } catch (IllegalArgumentException error) {
            comprobar("POO08 periodicidad inválida", "Ajuste trimestral o semestral".equals(error.getMessage()));
        }
        try {
            trimestral.importeInicial(-1, inicial);
            throw new AssertionError("POO09 índice inválido");
        } catch (IllegalArgumentException error) {
            comprobar("POO09 índice inválido", "Mes o importe inicial inválido".equals(error.getMessage()));
        }
        System.out.println("RESULTADO: " + total + " verificaciones POO aprobadas.");
    }
}

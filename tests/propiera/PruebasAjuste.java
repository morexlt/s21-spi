package propiera;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;
import propiera.modelo.AltaContrato;
import propiera.modelo.Cuota;
import propiera.persistencia.BaseDatos;
import propiera.servicio.AlquilerService;
import propiera.vista.FormularioAjuste;
import propiera.vista.TablaCuotasModelo;

/** Ejecutar después de Pruebas, en la misma base ficticia. */
public final class PruebasAjuste {
    private static final BaseDatos base = new BaseDatos();
    private static final AlquilerService servicio = new AlquilerService(base);
    private static int total;

    private static void comprobar(String caso, boolean correcto) {
        if (!correcto) {
            throw new AssertionError(caso);
        }
        total++;
        System.out.println("PASS " + caso);
    }

    private static void rechazar(String caso, String motivo, Pruebas.Accion accion) throws Exception {
        try {
            accion.ejecutar();
            throw new AssertionError(caso + ": debía rechazarse");
        } catch (IllegalArgumentException error) {
            comprobar(caso, motivo.equals(error.getMessage()));
        }
    }

    private static long contrato(int meses, int vigencia, int anio) throws Exception {
        LocalDate inicio = LocalDate.of(anio, 1, 1);
        return servicio.registrarContrato(new AltaContrato(1, 1, inicio,
            inicio.plusMonths(vigencia).minusDays(1), new BigDecimal("300000.00"),
            new BigDecimal("5.00"), meses, 10, "Mora manual"), 1);
    }

    private static List<Cuota> cuotas(long contrato) throws Exception {
        return servicio.consultarCuotas().stream().filter(c -> c.contrato() == contrato).toList();
    }

    private static void sql(String sentencia) throws Exception {
        try (Connection conexion = base.abrir()) {
            BaseDatos.insertarSinClave(conexion, sentencia);
        }
    }

    public static void main(String[] args) throws Exception {
        long contrato = contrato(3, 12, 2035);
        long cuarta = cuotas(contrato).get(3).id();
        BigDecimal nuevo = new BigDecimal("360000.00");
        rechazar("AJ01 permisos", "El usuario no puede confirmar ajustes",
            () -> servicio.confirmarAjuste(cuarta, nuevo, 2));
        rechazar("AJ02 importe cero", "Importe de ajuste inválido",
            () -> servicio.confirmarAjuste(cuarta, BigDecimal.ZERO, 1));
        rechazar("AJ03 más de dos decimales", "Importe de ajuste inválido",
            () -> servicio.confirmarAjuste(cuarta, new BigDecimal("360000.000"), 1));
        rechazar("AJ04 cuota inexistente", "Cuota inexistente",
            () -> servicio.confirmarAjuste(Long.MAX_VALUE, nuevo, 1));
        rechazar("AJ05 mes intermedio", "Seleccioná la primera cuota de un tramo de ajuste",
            () -> servicio.confirmarAjuste(cuotas(contrato).get(4).id(), nuevo, 1));
        rechazar("AJ06 tramo inicial", "Seleccioná la primera cuota de un tramo de ajuste",
            () -> servicio.confirmarAjuste(cuotas(contrato).get(0).id(), nuevo, 1));
        rechazar("AJ07 orden de tramos", "Primero confirmá el tramo anterior",
            () -> servicio.confirmarAjuste(cuotas(contrato).get(6).id(), nuevo, 1));
        comprobar("AJ08 confirma tres cuotas", servicio.confirmarAjuste(cuarta, nuevo, 1) == 3);
        List<Cuota> actualizadas = cuotas(contrato);
        comprobar("AJ09 consulta conserva otros tramos", actualizadas.get(2).importe().equals(new BigDecimal("300000.00"))
            && actualizadas.subList(3, 6).stream().allMatch(c -> nuevo.equals(c.importe()) && nuevo.equals(c.saldo()))
            && actualizadas.get(6).importe() == null);
        rechazar("AJ10 sin sobrescritura", "El tramo ya tiene importes confirmados",
            () -> servicio.confirmarAjuste(cuarta, new BigDecimal("400000.00"), 1));

        long historico = contrato(3, 12, 2020);
        long cuotaCobrable = cuotas(historico).get(3).id();
        servicio.confirmarAjuste(cuotaCobrable, nuevo, 1);
        servicio.registrarPago(cuotaCobrable, LocalDate.of(2020, 4, 10), new BigDecimal("100000.00"),
            BigDecimal.ZERO, "EFECTIVO", "AP4-AJUSTE-COBRO", 1);
        comprobar("AJ11 cuota ajustada admite pago", cuotas(historico).get(3).saldo().equals(new BigDecimal("260000.00")));
        rechazar("AJ12 mantiene importe cobrado", "El tramo ya tiene importes confirmados",
            () -> servicio.confirmarAjuste(cuotaCobrable, nuevo, 1));

        long semestral = contrato(6, 12, 2037);
        comprobar("AJ13 tramo semestral", servicio.confirmarAjuste(cuotas(semestral).get(6).id(), nuevo, 1) == 6);
        long corto = contrato(3, 8, 2039);
        servicio.confirmarAjuste(cuotas(corto).get(3).id(), nuevo, 1);
        comprobar("AJ14 último tramo incompleto", servicio.confirmarAjuste(cuotas(corto).get(6).id(), nuevo, 1) == 2);

        long concurrente = contrato(3, 12, 2041);
        long cuotaConcurrente = cuotas(concurrente).get(3).id();
        Pruebas.concurrentes("AJ15 dos ajustes simultáneos", "El tramo ya tiene importes confirmados",
            () -> servicio.confirmarAjuste(cuotaConcurrente, nuevo, 1));
        comprobar("AJ15 resultado persistido", cuotas(concurrente).subList(3, 6).stream()
            .allMatch(c -> nuevo.equals(c.importe())));

        long fallido = contrato(3, 12, 2043);
        long cuotaFallida = cuotas(fallido).get(3).id();
        sql("ALTER TABLE cuota ADD CONSTRAINT ap4_error_ajuste CHECK "
            + "(periodo <> '2043-05-01' OR importe IS NULL OR importe <> 360000.00)");
        try {
            try {
                servicio.confirmarAjuste(cuotaFallida, nuevo, 1);
                throw new AssertionError("AJ16 faltó el error SQL");
            } catch (SQLException error) {
                comprobar("AJ16 error SQL y rollback", error.getErrorCode() == 3819
                    && cuotas(fallido).subList(3, 6).stream().allMatch(c -> c.importe() == null));
            }
        } finally {
            sql("ALTER TABLE cuota DROP CHECK ap4_error_ajuste");
        }
        SwingUtilities.invokeAndWait(() -> {
            FormularioAjuste formulario = new FormularioAjuste();
            Cuota cuota = new Cuota(15, 2, "DEMO-01", LocalDate.of(2035, 4, 1),
                LocalDate.of(2035, 4, 10), null, null);
            formulario.seleccionarCuota(cuota);
            boolean seleccion = formulario.cuota().equals("15") && formulario.importe().isEmpty();
            formulario.limpiar();
            comprobar("AJ17 formulario selecciona y limpia", seleccion && formulario.cuota().isEmpty());
            ArrayList<Cuota> datos = new ArrayList<>(List.of(cuota));
            TablaCuotasModelo tabla = new TablaCuotasModelo();
            tabla.actualizar(datos);
            datos.clear();
            comprobar("AJ18 arreglo de columnas y ArrayList de filas", tabla.getColumnCount() == 7
                && tabla.getColumnName(5).equals("Importe ($)") && tabla.getRowCount() == 1
                && tabla.cuota(0).id() == 15);
        });
        System.out.println("RESULTADO: " + total + " verificaciones AP4 aprobadas.");
    }
}

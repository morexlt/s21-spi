package propiera;

import propiera.persistencia.BaseDatos;
import propiera.servicio.AlquilerService;
import propiera.modelo.AltaContrato;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

/** Ejecutar sólo en una base de prueba recién inicializada. No borra datos. */
public final class Pruebas {
    private static int total = 0;
    private static final BaseDatos baseDatos = new BaseDatos();
    private static final AlquilerService servicio = new AlquilerService(baseDatos);

    interface Accion {
        void ejecutar() throws Exception;
    }

    static void comprobar(String id, boolean ok) {
        if (!ok) {
            throw new AssertionError(id);
        }
        total++;
        System.out.println("PASS " + id);
    }

    static void rechazar(String id, String motivo, Accion a) throws Exception {
        try {
            a.ejecutar();
        }
        catch (IllegalArgumentException e) {
            comprobar(id, motivo.equals(e.getMessage()));
            return;
        }
        throw new AssertionError(id + " debía rechazarse");
    }

    static void rechazarSql(String id, int codigo, String estado, Accion accion) throws Exception {
        try {
            accion.ejecutar();
        } catch (SQLException error) {
            comprobar(id, error.getErrorCode() == codigo && estado.equals(error.getSQLState()));
            return;
        }
        throw new AssertionError(id + " debía rechazarse por una restricción SQL");
    }

    static AltaContrato alta(long propiedad, BigDecimal importe, int ajuste) {
        return new AltaContrato(propiedad, 1, LocalDate.of(2026, 9, 1), LocalDate.of(2027, 8, 31), importe,
            new BigDecimal("5.00"), ajuste, 10, "Mora manual según acuerdo del contrato");
    }

    static void concurrentes(String id, String motivo, Accion accion) throws Exception {
        var inicio = new CountDownLatch(1);
        try (var pool = Executors.newFixedThreadPool(2)) {
            Callable<Boolean> tarea = () -> {
                inicio.await();
                try {
                    accion.ejecutar();
                    return true;
                }
                catch (IllegalArgumentException e) {
                    if (!motivo.equals(e.getMessage())) {
                        throw new AssertionError(id + ": rechazo inesperado", e);
                    }
                    return false;
                }
            };
            var uno = pool.submit(tarea);
            var dos = pool.submit(tarea);
            inicio.countDown();
            comprobar(id, uno.get() != dos.get());
        }
    }

    public static void main(String[] args) throws Exception {
        try (Connection conexion = baseDatos.abrir()) {
            if (!BaseDatos.consultar(conexion, "SELECT id FROM contrato LIMIT 1").isEmpty()) {
                throw new IllegalStateException("Usar una base nueva; no se borran datos existentes");
            }
        }
        long contrato = servicio.registrarContrato(alta(1, new BigDecimal("300000.00"), 3), 1);
        comprobar("CP01 alta válida", contrato > 0);
        long cuota;
        long cuotaSinPagos;
        long futura;
        try (Connection conexion = baseDatos.abrir()) {
            var cuotas = BaseDatos.consultar(conexion,
                "SELECT id,importe FROM cuota WHERE contrato_id=? ORDER BY periodo", contrato);
            cuota = Long.parseLong(cuotas.get(0).get(0));
            cuotaSinPagos = Long.parseLong(cuotas.get(1).get(0));
            futura = Long.parseLong(cuotas.get(3).get(0));
            comprobar("CP02 include genera 12 cuotas", cuotas.size() == 12);
            comprobar("CP03 primer tramo conocido y siguiente sin importe",
                cuotas.get(2).get(1).equals("300000.00") && cuotas.get(3).get(1).equals("Pendiente de ajuste"));
        }
        rechazar("CP04 contrato superpuesto", "La propiedad ya tiene un contrato superpuesto", () -> servicio.registrarContrato(alta(1,
            new BigDecimal("300000.00"), 3), 1));
        rechazar("CP05 propiedad inexistente", "Propiedad o propietario inexistente o inactivo", () -> servicio.registrarContrato(alta(999,
            new BigDecimal("300000.00"), 3), 1));
        rechazar("CP06 importe cero", "Importe inválido", () -> servicio.registrarContrato(alta(2, BigDecimal.ZERO, 3), 1));
        try (Connection conexion = baseDatos.abrir()) {
            BaseDatos.insertarSinClave(conexion, "UPDATE propietario SET activo=FALSE WHERE id=1");
        }
        try {
            rechazar("CP07 propietario inactivo", "Propiedad o propietario inexistente o inactivo", () -> servicio.registrarContrato(alta(2,
                new BigDecimal("300000.00"), 3), 1));
        }
        finally {
            try (Connection conexion = baseDatos.abrir()) {
                BaseDatos.insertarSinClave(conexion, "UPDATE propietario SET activo=TRUE WHERE id=1");
            }
        }
        try (Connection conexion = baseDatos.abrir()) {
            comprobar("CP08 sin altas residuales", BaseDatos.consultar(conexion,
                "SELECT id FROM contrato").size() == 1);
        }
        servicio.registrarPago(cuota, LocalDate.of(2026, 9, 10), new BigDecimal("100000.00"), BigDecimal.ZERO,
            "EFECTIVO", "DEMO-COBRO-01", 1);
        comprobar("CP09 pago parcial", servicio.consultarCuotas().get(0).saldo().equals(new BigDecimal("200000.00")));
        rechazarSql("CP10 referencia duplicada", 1062, "23000", () -> servicio.registrarPago(cuota, LocalDate.of(2026, 9, 10),
            new BigDecimal("1000.00"), BigDecimal.ZERO, "EFECTIVO", "DEMO-COBRO-01", 1));
        rechazar("CP11 sobrepago", "El capital supera el saldo pendiente", () -> servicio.registrarPago(cuota, LocalDate.of(2026, 9, 10),
            new BigDecimal("200000.01"), BigDecimal.ZERO, "EFECTIVO", "DEMO-X", 1));
        servicio.registrarPago(cuota, LocalDate.of(2026, 9, 10), new BigDecimal("200000.00"),
            new BigDecimal("1500.00"), "TRANSFERENCIA", "DEMO-COBRO-02", 1);
        comprobar("CP12 cancelación y mora separada",
            servicio.consultarCuotas().get(0).saldo().equals(new BigDecimal("0.00")));
        rechazar("CP13 cuota pagada", "El capital supera el saldo pendiente", () -> servicio.registrarPago(cuota, LocalDate.of(2026, 9, 10),
            BigDecimal.ONE, BigDecimal.ZERO, "EFECTIVO", "DEMO-Y", 1));
        rechazar("CP14 cuota pendiente de ajuste", "No se puede cobrar una cuota pendiente de ajuste", () -> servicio.registrarPago(futura, LocalDate.of(2026, 9,
            10), BigDecimal.ONE, BigDecimal.ZERO, "EFECTIVO", "DEMO-Z", 1));
        rechazar("CP15 cuota inexistente", "Cuota inexistente", () -> servicio.registrarPago(999999, LocalDate.of(2026, 9, 10),
            BigDecimal.ONE, BigDecimal.ZERO, "EFECTIVO", "DEMO-Z", 1));
        rechazar("CP16 mora negativa", "Capital positivo y mora no negativa", () -> servicio.registrarPago(cuotaSinPagos, LocalDate.of(2026, 9, 10),
            BigDecimal.ONE, new BigDecimal("-1"), "EFECTIVO", "DEMO-Z", 1));
        long segundo = servicio.registrarContrato(alta(2, new BigDecimal("200000.00"), 6), 1);
        try (Connection conexion = baseDatos.abrir()) {
            comprobar("CP17 ajuste semestral", BaseDatos.consultar(conexion,
                "SELECT id FROM cuota WHERE contrato_id=? AND importe IS NOT NULL", segundo).size() == 6);
            comprobar("CP18 UTF-8", BaseDatos.consultar(conexion,
                "SELECT nombre FROM propietario WHERE id=1").get(0).get(0).equals("Ana Muñoz"));
        }
        rechazarSql("CP19 integridad referencial impide borrar contrato", 1451, "23000", () -> {
            try (Connection conexion = baseDatos.abrir()) {
                BaseDatos.insertarSinClave(conexion, "DELETE FROM contrato WHERE id=?", contrato);
            }
        });
        rechazarSql("CP20 CHECK impide cuota negativa", 3819, "HY000", () -> {
            try (Connection conexion = baseDatos.abrir()) {
                BaseDatos.insertarSinClave(conexion, "UPDATE cuota SET importe=-1 WHERE id=?", cuota);
            }
        });
        comprobar("CP21 decimal y redondeo",
            new BigDecimal("10.10").multiply(new BigDecimal("5.00")).divide(new BigDecimal("100"), 2,
            RoundingMode.HALF_UP).equals(new BigDecimal("0.51")));
        rechazar("CP22 pago anterior al contrato", "El pago no puede ser anterior al inicio del contrato", () -> servicio.registrarPago(cuotaSinPagos, LocalDate.of(2026, 8,
            31), BigDecimal.ONE, BigDecimal.ZERO, "EFECTIVO", "DEMO-Z", 1));
        var futuro = new AltaContrato(1, 1, LocalDate.of(2029, 1, 1), LocalDate.of(2029, 12, 31),
            new BigDecimal("100000.00"), new BigDecimal("5.00"), 3, 10, "Condición de prueba");
        concurrentes("CP23 altas simultáneas: sólo una aceptada", "La propiedad ya tiene un contrato superpuesto", () -> servicio.registrarContrato(futuro, 1));
        long cuotaSegundo;
        try (Connection conexion = baseDatos.abrir()) {
            cuotaSegundo = Long.parseLong(BaseDatos.consultar(conexion,
                "SELECT id FROM cuota WHERE contrato_id=? ORDER BY periodo LIMIT 1", segundo).get(0).get(0));
        }
        concurrentes("CP24 cobros simultáneos: sólo uno aceptado", "El capital supera el saldo pendiente", () -> servicio.registrarPago(cuotaSegundo,
            LocalDate.of(2026, 9, 10), new BigDecimal("150000.00"), BigDecimal.ZERO, "EFECTIVO",
            "CONC-" + UUID.randomUUID(), 1));
        System.out.println("RESULTADO: " + total + " verificaciones aprobadas.");
    }
}

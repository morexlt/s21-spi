package propiera.servicio;

import propiera.persistencia.BaseDatos;
import propiera.modelo.AltaContrato;
import propiera.modelo.PeriodicidadAjuste;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import propiera.modelo.Cuota;
import propiera.modelo.Opcion;
import propiera.persistencia.AlquilerDao;
import propiera.persistencia.OperacionDao;

public final class AlquilerService {
    private final BaseDatos baseDatos;
    private final AlquilerDao consultas;
    private final OperacionDao operaciones = new OperacionDao();

    public AlquilerService(BaseDatos baseDatos) {
        this.baseDatos = baseDatos;
        consultas = new AlquilerDao(baseDatos);
    }

    public static BigDecimal dinero(String texto) {
        exigir(texto != null && texto.trim().matches("-?[0-9]+([.,][0-9]{1,2})?"),
            "Ingresá un importe sin separador de miles y con hasta dos decimales.");
        return new BigDecimal(texto.trim().replace(',', '.')).setScale(2, RoundingMode.UNNECESSARY);
    }

    private static void exigir(boolean condicion, String mensaje) {
        if (!condicion) {
            throw new IllegalArgumentException(mensaje);
        }
    }

    private void validarContrato(AltaContrato alta) {
        exigir(alta.inicio() != null && alta.fin() != null
            && !alta.fin().isBefore(alta.inicio()), "Vigencia inválida");
        exigir(alta.inicio().getDayOfMonth() == 1
            && alta.fin().equals(YearMonth.from(alta.fin()).atEndOfMonth()),
            "El contrato debe comenzar el día 1 y finalizar el último día del mes");
        long meses = ChronoUnit.MONTHS.between(YearMonth.from(alta.inicio()),
            YearMonth.from(alta.fin())) + 1;
        exigir(meses <= 120, "El contrato no puede superar los 120 meses");
        exigir(alta.importe() != null && alta.importe().signum() > 0
            && alta.importe().scale() <= 2
            && alta.importe().compareTo(new BigDecimal("9999999999.99")) <= 0, "Importe inválido");
        exigir(alta.comision() != null && alta.comision().signum() >= 0
            && alta.comision().scale() <= 2
            && alta.comision().compareTo(new BigDecimal("100")) <= 0, "Comisión entre 0 y 100 con hasta dos decimales");
        exigir(alta.ajusteMeses() == 3 || alta.ajusteMeses() == 6, "Ajuste trimestral o semestral");
        exigir(alta.diaVencimiento() >= 1 && alta.diaVencimiento() <= 28, "Vencimiento entre 1 y 28");
        exigir(alta.reglaMora() != null && !alta.reglaMora().isBlank()
            && alta.reglaMora().length() <= 250, "Indique la condición de mora");
    }

    public long registrarContrato(AltaContrato alta, long usuario) throws SQLException {
        validarContrato(alta);
        try (Connection conexion = baseDatos.abrir()) {
            conexion.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            conexion.setAutoCommit(false);
            try {
                exigir(operaciones.administrativoActivo(conexion, usuario), "El usuario no puede registrar contratos");
                exigir(operaciones.inquilinoActivo(conexion, alta.inquilino()), "Inquilino inexistente o inactivo");
                exigir(operaciones.bloquearPropiedad(conexion, alta.propiedad()), "Propiedad o propietario inexistente o inactivo");
                exigir(!operaciones.contratoSuperpuesto(conexion, alta), "La propiedad ya tiene un contrato superpuesto");
                long contrato = operaciones.insertarContrato(conexion, alta, usuario);
                generarCuotas(conexion, contrato, alta);
                conexion.commit();
                return contrato;
            } catch (SQLException | RuntimeException error) {
                conexion.rollback();
                throw error;
            }
        }
    }

    private void generarCuotas(Connection conexion, long contrato, AltaContrato alta) throws SQLException {
        long meses = ChronoUnit.MONTHS.between(YearMonth.from(alta.inicio()),
            YearMonth.from(alta.fin())) + 1;
        PeriodicidadAjuste periodicidad = PeriodicidadAjuste.crear(alta.ajusteMeses());
        for (int mes = 0; mes < meses; mes++) {
            LocalDate periodo = alta.inicio().plusMonths(mes);
            // Los meses posteriores al primer ajuste quedan sin importe confirmado.
            BigDecimal importe = periodicidad.importeInicial(mes, alta.importe());
            operaciones.insertarCuota(conexion, contrato, periodo,
                periodo.withDayOfMonth(alta.diaVencimiento()), importe);
        }
    }

    public long registrarPago(long cuota, LocalDate fecha, BigDecimal capital,
            BigDecimal mora, String medio, String referencia, long usuario) throws SQLException {
        exigir(capital != null && mora != null && capital.signum() > 0
            && mora.signum() >= 0, "Capital positivo y mora no negativa");
        exigir(capital.scale() <= 2 && mora.scale() <= 2
            && capital.compareTo(new BigDecimal("9999999999.99")) <= 0
            && mora.compareTo(new BigDecimal("9999999999.99")) <= 0,
            "Los importes admiten hasta dos decimales y deben estar dentro del límite permitido");
        exigir(fecha != null && !fecha.isAfter(LocalDate.now()), "Fecha futura no admitida");
        exigir(medio != null && Set.of("TRANSFERENCIA", "EFECTIVO").contains(medio), "Medio de cobro inválido");
        exigir(referencia != null && !referencia.isBlank()
            && referencia.length() <= 80, "Referencia obligatoria, hasta 80 caracteres");
        try (Connection conexion = baseDatos.abrir()) {
            conexion.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            conexion.setAutoCommit(false);
            try {
                exigir(operaciones.administrativoActivo(conexion, usuario), "El usuario no puede registrar pagos");
                var datos = operaciones.bloquearCuota(conexion, cuota);
                exigir(datos != null, "Cuota inexistente");
                exigir(datos.importe() != null, "No se puede cobrar una cuota pendiente de ajuste");
                exigir(!fecha.isBefore(datos.inicio()), "El pago no puede ser anterior al inicio del contrato");
                BigDecimal saldo = datos.importe().subtract(operaciones.capitalCobrado(conexion, cuota));
                exigir(capital.compareTo(saldo) <= 0, "El capital supera el saldo pendiente");
                long pago = operaciones.insertarPago(conexion, cuota, fecha, capital,
                    mora, medio, referencia, usuario);
                conexion.commit();
                return pago;
            } catch (SQLException | RuntimeException error) {
                conexion.rollback();
                throw error;
            }
        }
    }

    public List<Cuota> consultarCuotas() throws SQLException {
        return consultas.cuotas();
    }

    public List<Opcion> propiedades() throws SQLException {
        return consultas.propiedades();
    }

    public List<Opcion> inquilinos() throws SQLException {
        return consultas.inquilinos();
    }
}

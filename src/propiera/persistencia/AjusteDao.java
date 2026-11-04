package propiera.persistencia;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AjusteDao {
    public record Contrato(long id, LocalDate inicio, LocalDate fin, int meses) { }
    public record CuotaAjuste(long id, LocalDate periodo, BigDecimal importe) { }

    public Long contratoDeCuota(Connection conexion, long cuota) throws SQLException {
        try (PreparedStatement consulta = BaseDatos.preparar(conexion,
                "SELECT contrato_id FROM cuota WHERE id=?", cuota);
             ResultSet filas = consulta.executeQuery()) {
            return filas.next() ? filas.getLong(1) : null;
        }
    }

    public Contrato bloquearContrato(Connection conexion, long contrato) throws SQLException {
        try (PreparedStatement consulta = BaseDatos.preparar(conexion,
                "SELECT id,inicio,fin,ajuste_meses FROM contrato WHERE id=? FOR UPDATE", contrato);
             ResultSet filas = consulta.executeQuery()) {
            if (!filas.next()) {
                return null;
            }
            return new Contrato(filas.getLong(1), filas.getDate(2).toLocalDate(),
                filas.getDate(3).toLocalDate(), filas.getInt(4));
        }
    }

    public List<CuotaAjuste> bloquearCuotas(Connection conexion, long contrato) throws SQLException {
        List<CuotaAjuste> cuotas = new ArrayList<>();
        try (PreparedStatement consulta = BaseDatos.preparar(conexion,
                "SELECT id,periodo,importe FROM cuota WHERE contrato_id=? ORDER BY periodo FOR UPDATE",
                contrato);
             ResultSet filas = consulta.executeQuery()) {
            while (filas.next()) {
                cuotas.add(new CuotaAjuste(filas.getLong(1), filas.getDate(2).toLocalDate(),
                    filas.getBigDecimal(3)));
            }
        }
        return cuotas;
    }

    public boolean tienePagos(Connection conexion, long cuota) throws SQLException {
        return !BaseDatos.consultar(conexion,
            "SELECT id FROM pago WHERE cuota_id=? LIMIT 1", cuota).isEmpty();
    }

    public void confirmarImporte(Connection conexion, long cuota, BigDecimal importe) throws SQLException {
        try (PreparedStatement consulta = BaseDatos.preparar(conexion,
                "UPDATE cuota SET importe=? WHERE id=? AND importe IS NULL", importe, cuota)) {
            if (consulta.executeUpdate() != 1) {
                throw new SQLException("La cuota cambió durante la confirmación del ajuste");
            }
        }
    }
}

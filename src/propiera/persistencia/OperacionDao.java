package propiera.persistencia;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import propiera.persistencia.BaseDatos;
import propiera.modelo.AltaContrato;

public class OperacionDao {
    public boolean administrativoActivo(Connection conexion, long usuario) throws SQLException {
        return !BaseDatos.consultar(conexion,
            "SELECT id FROM usuario WHERE id=? AND activo=TRUE AND rol='ADMINISTRATIVO'", usuario).isEmpty();
    }

    public boolean inquilinoActivo(Connection conexion, long inquilino) throws SQLException {
        return !BaseDatos.consultar(conexion,
            "SELECT id FROM inquilino WHERE id=? AND activo=TRUE", inquilino).isEmpty();
    }

    public boolean bloquearPropiedad(Connection conexion, long propiedad) throws SQLException {
        // El bloqueo evita dos contratos superpuestos cargados al mismo tiempo.
        var filas = BaseDatos.consultar(conexion,
            "SELECT propietario_id FROM propiedad WHERE id=? AND activo=TRUE FOR UPDATE", propiedad);
        if (filas.isEmpty()) {
            return false;
        }
        long propietario = Long.parseLong(filas.get(0).get(0));
        return !BaseDatos.consultar(conexion,
            "SELECT id FROM propietario WHERE id=? AND activo=TRUE", propietario).isEmpty();
    }

    public boolean contratoSuperpuesto(Connection conexion, AltaContrato alta) throws SQLException {
        return !BaseDatos.consultar(conexion,
            "SELECT id FROM contrato WHERE propiedad_id=? AND inicio<=? AND fin>=?",
            alta.propiedad(), alta.fin(), alta.inicio()).isEmpty();
    }

    public long insertarContrato(Connection conexion, AltaContrato alta, long usuario) throws SQLException {
        return BaseDatos.insertar(conexion, """
            INSERT INTO contrato(propiedad_id,inquilino_id,inicio,fin,importe_inicial,
                comision_pct,ajuste_meses,dia_vencimiento,regla_mora,creado_por)
            VALUES (?,?,?,?,?,?,?,?,?,?)
            """, alta.propiedad(), alta.inquilino(), alta.inicio(), alta.fin(),
            alta.importe(), alta.comision(), alta.ajusteMeses(), alta.diaVencimiento(),
            alta.reglaMora(), usuario);
    }

    public void insertarCuota(Connection conexion, long contrato, LocalDate periodo,
            LocalDate vencimiento, BigDecimal importe) throws SQLException {
        BaseDatos.insertar(conexion,
            "INSERT INTO cuota(contrato_id,periodo,vencimiento,importe) VALUES (?,?,?,?)",
            contrato, periodo, vencimiento, importe);
    }

    public record CuotaCobro(BigDecimal importe, LocalDate inicio) { }

    public CuotaCobro bloquearCuota(Connection conexion, long cuota) throws SQLException {
        try (PreparedStatement consulta = BaseDatos.preparar(conexion, """
                SELECT q.importe, co.inicio FROM cuota q
                JOIN contrato co ON co.id=q.contrato_id WHERE q.id=? FOR UPDATE
                """, cuota);
             ResultSet resultado = consulta.executeQuery()) {
            if (!resultado.next()) {
                return null;
            }
            return new CuotaCobro(resultado.getBigDecimal(1), resultado.getDate(2).toLocalDate());
        }
    }

    public BigDecimal capitalCobrado(Connection conexion, long cuota) throws SQLException {
        try (PreparedStatement consulta = BaseDatos.preparar(conexion,
                "SELECT COALESCE(SUM(capital),0) FROM pago WHERE cuota_id=?", cuota);
             ResultSet resultado = consulta.executeQuery()) {
            resultado.next();
            return resultado.getBigDecimal(1);
        }
    }

    public long insertarPago(Connection conexion, long cuota, LocalDate fecha,
            BigDecimal capital, BigDecimal mora, String medio, String referencia,
            long usuario) throws SQLException {
        return BaseDatos.insertar(conexion, """
            INSERT INTO pago(cuota_id,fecha,capital,mora,medio,referencia,registrado_por)
            VALUES (?,?,?,?,?,?,?)
            """, cuota, fecha, capital, mora, medio, referencia, usuario);
    }
}

package propiera.persistencia;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import propiera.modelo.Cuota;
import propiera.modelo.Opcion;
import propiera.modelo.EstadoContrato;
import propiera.modelo.CuotaContrato;

public class AlquilerDao {
    private final BaseDatos baseDatos;

    public AlquilerDao(BaseDatos baseDatos) {
        this.baseDatos = baseDatos;
    }

    public List<Opcion> propiedades() throws SQLException {
        return opciones("""
            SELECT p.id, CONCAT(p.codigo, ' - ', p.direccion, ' / ', dueno.nombre)
            FROM propiedad p JOIN propietario dueno ON dueno.id = p.propietario_id
            WHERE p.activo = TRUE AND dueno.activo = TRUE ORDER BY p.codigo
            """);
    }

    public List<Opcion> inquilinos() throws SQLException {
        return opciones("SELECT id, nombre FROM inquilino WHERE activo = TRUE ORDER BY nombre");
    }

    private List<Opcion> opciones(String sql) throws SQLException {
        List<Opcion> opciones = new ArrayList<>();
        try (Connection conexion = baseDatos.abrir();
             PreparedStatement consulta = conexion.prepareStatement(sql);
             ResultSet resultado = consulta.executeQuery()) {
            while (resultado.next()) {
                opciones.add(new Opcion(resultado.getLong(1), resultado.getString(2)));
            }
        }
        return opciones;
    }

    public List<Cuota> cuotas() throws SQLException {
        String sql = """
            SELECT q.id, co.id, CONCAT(p.codigo, ' · ', p.direccion), q.periodo, q.vencimiento, q.importe,
                   q.importe - COALESCE(SUM(pa.capital), 0) AS saldo
            FROM cuota q JOIN contrato co ON co.id = q.contrato_id
            JOIN propiedad p ON p.id = co.propiedad_id
            LEFT JOIN pago pa ON pa.cuota_id = q.id
            GROUP BY q.id, co.id, p.codigo, p.direccion, q.periodo, q.vencimiento, q.importe
            ORDER BY q.periodo, q.id
            """;
        List<Cuota> cuotas = new ArrayList<>();
        try (Connection conexion = baseDatos.abrir();
             PreparedStatement consulta = conexion.prepareStatement(sql);
             ResultSet resultado = consulta.executeQuery()) {
            while (resultado.next()) {
                cuotas.add(new Cuota(resultado.getLong(1), resultado.getLong(2),
                    resultado.getString(3), resultado.getDate(4).toLocalDate(),
                    resultado.getDate(5).toLocalDate(), resultado.getBigDecimal(6),
                    resultado.getBigDecimal(7)));
            }
        }
        return cuotas;
    }

    public EstadoContrato estadoContrato(Connection conexion, long contrato) throws SQLException {
        try (PreparedStatement consulta = BaseDatos.preparar(conexion, """
                SELECT co.id, CONCAT(p.codigo, ' · ', p.direccion), dueno.nombre, i.nombre,
                       co.inicio, co.fin
                FROM contrato co JOIN propiedad p ON p.id=co.propiedad_id
                JOIN propietario dueno ON dueno.id=p.propietario_id
                JOIN inquilino i ON i.id=co.inquilino_id WHERE co.id=?
                """, contrato);
             ResultSet fila = consulta.executeQuery()) {
            if (!fila.next()) return null;
            return new EstadoContrato(fila.getLong(1), fila.getString(2), fila.getString(3),
                fila.getString(4), fila.getDate(5).toLocalDate(), fila.getDate(6).toLocalDate(),
                cuotasContrato(conexion, contrato));
        }
    }

    private List<CuotaContrato> cuotasContrato(Connection conexion, long contrato) throws SQLException {
        List<CuotaContrato> cuotas = new ArrayList<>();
        try (PreparedStatement consulta = BaseDatos.preparar(conexion, """
                SELECT q.periodo, q.vencimiento, q.importe,
                       COALESCE(SUM(p.capital),0), COALESCE(SUM(p.mora),0),
                       q.importe - COALESCE(SUM(p.capital),0)
                FROM cuota q LEFT JOIN pago p ON p.cuota_id=q.id
                WHERE q.contrato_id=?
                GROUP BY q.id,q.periodo,q.vencimiento,q.importe ORDER BY q.periodo,q.id
                """, contrato);
             ResultSet filas = consulta.executeQuery()) {
            while (filas.next()) {
                cuotas.add(new CuotaContrato(filas.getDate(1).toLocalDate(), filas.getDate(2).toLocalDate(),
                    filas.getBigDecimal(3), filas.getBigDecimal(4), filas.getBigDecimal(5), filas.getBigDecimal(6)));
            }
        }
        return cuotas;
    }
}

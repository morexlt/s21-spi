package propiera.persistencia;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import propiera.persistencia.BaseDatos;
import propiera.modelo.Cuota;
import propiera.modelo.Opcion;

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
            SELECT q.id, co.id, p.codigo, q.periodo, q.vencimiento, q.importe,
                   q.importe - COALESCE(SUM(pa.capital), 0) AS saldo
            FROM cuota q JOIN contrato co ON co.id = q.contrato_id
            JOIN propiedad p ON p.id = co.propiedad_id
            LEFT JOIN pago pa ON pa.cuota_id = q.id
            GROUP BY q.id, co.id, p.codigo, q.periodo, q.vencimiento, q.importe
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
}

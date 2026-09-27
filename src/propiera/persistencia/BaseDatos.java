package propiera.persistencia;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class BaseDatos {
    public Connection abrir() throws SQLException {
        String url = System.getenv().getOrDefault("PROPIERA_DB_URL",
            "jdbc:mysql://127.0.0.1:33316/propiera_ap2?sslMode=REQUIRED&connectionTimeZone=America/Argentina/Buenos_Aires");
        return DriverManager.getConnection(url,
            obligatorio("PROPIERA_DB_USER"), obligatorio("PROPIERA_DB_PASSWORD"));
    }

    public static String obligatorio(String nombre) {
        String valor = System.getenv(nombre);
        if (valor == null || valor.isBlank()) {
            throw new IllegalStateException("Falta configurar " + nombre);
        }
        return valor;
    }

    public static PreparedStatement preparar(Connection conexion, String sql,
            Object... parametros) throws SQLException {
        PreparedStatement consulta = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        for (int indice = 0; indice < parametros.length; indice++) {
            consulta.setObject(indice + 1, parametros[indice]);
        }
        return consulta;
    }

    public static long insertar(Connection conexion, String sql,
            Object... parametros) throws SQLException {
        try (PreparedStatement consulta = preparar(conexion, sql, parametros)) {
            consulta.executeUpdate();
            try (ResultSet resultado = consulta.getGeneratedKeys()) {
                if (!resultado.next()) {
                    throw new SQLException("No se obtuvo el identificador del registro");
                }
                return resultado.getLong(1);
            }
        }
    }

    public static void insertarSinClave(Connection conexion, String sql,
            Object... parametros) throws SQLException {
        try (PreparedStatement consulta = preparar(conexion, sql, parametros)) {
            consulta.executeUpdate();
        }
    }

    public static List<List<String>> consultar(Connection conexion, String sql,
            Object... parametros) throws SQLException {
        try (PreparedStatement consulta = preparar(conexion, sql, parametros);
             ResultSet resultado = consulta.executeQuery()) {
            List<List<String>> filas = new ArrayList<>();
            int columnas = resultado.getMetaData().getColumnCount();
            while (resultado.next()) {
                List<String> fila = new ArrayList<>();
                for (int columna = 1; columna <= columnas; columna++) {
                    fila.add(Objects.toString(resultado.getObject(columna), "Pendiente de ajuste"));
                }
                filas.add(fila);
            }
            return filas;
        }
    }
}

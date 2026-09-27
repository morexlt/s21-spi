package propiera.persistencia;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import propiera.modelo.Usuario;

public class UsuarioDao {
    private final BaseDatos baseDatos;

    public UsuarioDao(BaseDatos baseDatos) {
        this.baseDatos = baseDatos;
    }

    public Usuario buscarActivo(long id) throws SQLException {
        try (Connection conexion = baseDatos.abrir();
             PreparedStatement consulta = BaseDatos.preparar(conexion,
                 "SELECT nombre, rol FROM usuario WHERE id=? AND activo=TRUE", id);
             ResultSet resultado = consulta.executeQuery()) {
            if (!resultado.next()) {
                return null;
            }
            return new Usuario(id, resultado.getString("nombre"), resultado.getString("rol"));
        }
    }
}

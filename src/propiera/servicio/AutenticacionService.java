package propiera.servicio;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.SQLException;
import java.util.Arrays;
import propiera.persistencia.BaseDatos;
import propiera.modelo.Usuario;
import propiera.persistencia.UsuarioDao;

public class AutenticacionService {
    private final UsuarioDao usuarios;

    public AutenticacionService(BaseDatos baseDatos) {
        usuarios = new UsuarioDao(baseDatos);
    }

    public Usuario ingresar(String nombre, char[] clave) throws SQLException {
        String variable;
        String rol;
        long id;
        switch (nombre.trim().toLowerCase(java.util.Locale.ROOT)) {
            case "administrativo" -> {
                variable = "PROPIERA_ADMIN_PASSWORD";
                rol = "ADMINISTRATIVO";
                id = 1;
            }
            case "martillero" -> {
                variable = "PROPIERA_MARTILLERO_PASSWORD";
                rol = "MARTILLERO";
                id = 2;
            }
            default -> throw new IllegalArgumentException("Usuario o contraseña incorrectos.");
        }

        String esperada = BaseDatos.obligatorio(variable);
        byte[] ingresada = new String(clave).getBytes(StandardCharsets.UTF_8);
        boolean coincide = MessageDigest.isEqual(ingresada, esperada.getBytes(StandardCharsets.UTF_8));
        Arrays.fill(ingresada, (byte) 0);
        if (!coincide) {
            throw new IllegalArgumentException("Usuario o contraseña incorrectos.");
        }

        Usuario usuario = usuarios.buscarActivo(id);
        if (usuario == null || !rol.equals(usuario.rol())) {
            throw new IllegalArgumentException("La cuenta no está habilitada.");
        }
        return usuario;
    }
}

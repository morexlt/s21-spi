package propiera.persistencia;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import propiera.modelo.Persona;
import propiera.modelo.Propiedad;
import propiera.modelo.TipoPersona;

public class CatalogoDao {
    public List<Persona> personas(Connection conexion, TipoPersona tipo) throws SQLException {
        List<Persona> personas = new ArrayList<>();
        // El nombre de tabla proviene del enum, nunca de un campo del formulario.
        String sql = "SELECT id, documento, nombre, contacto, activo FROM " + tipo.tabla() + " ORDER BY nombre, id";
        try (PreparedStatement consulta = conexion.prepareStatement(sql);
             ResultSet filas = consulta.executeQuery()) {
            while (filas.next()) {
                personas.add(new Persona(filas.getLong(1), filas.getString(2),
                    filas.getString(3), filas.getString(4), filas.getBoolean(5)));
            }
        }
        return personas;
    }

    public List<Propiedad> propiedades(Connection conexion) throws SQLException {
        List<Propiedad> propiedades = new ArrayList<>();
        try (PreparedStatement consulta = conexion.prepareStatement("""
                SELECT p.id,p.codigo,p.direccion,p.propietario_id,d.nombre,p.activo
                FROM propiedad p JOIN propietario d ON d.id=p.propietario_id ORDER BY p.codigo
                """);
             ResultSet filas = consulta.executeQuery()) {
            while (filas.next()) {
                propiedades.add(new Propiedad(filas.getLong(1), filas.getString(2),
                    filas.getString(3), filas.getLong(4), filas.getString(5), filas.getBoolean(6)));
            }
        }
        return propiedades;
    }

    public long guardarPersona(Connection conexion, TipoPersona tipo, Persona persona) throws SQLException {
        if (persona.id() == 0) {
            return BaseDatos.insertar(conexion, "INSERT INTO " + tipo.tabla()
                + "(documento,nombre,contacto,activo) VALUES (?,?,?,?)",
                persona.documento(), persona.nombre(), persona.contacto(), persona.activo());
        }
        BaseDatos.insertarSinClave(conexion, "UPDATE " + tipo.tabla()
            + " SET documento=?,nombre=?,contacto=?,activo=? WHERE id=?",
            persona.documento(), persona.nombre(), persona.contacto(), persona.activo(), persona.id());
        return persona.id();
    }

    public boolean bloquearPersona(Connection conexion, TipoPersona tipo, long id) throws SQLException {
        return !BaseDatos.consultar(conexion,
            "SELECT id FROM " + tipo.tabla() + " WHERE id=? FOR UPDATE", id).isEmpty();
    }

    public void bajaPersona(Connection conexion, TipoPersona tipo, long id) throws SQLException {
        BaseDatos.insertarSinClave(conexion, "UPDATE " + tipo.tabla() + " SET activo=FALSE WHERE id=?", id);
    }

    public Long bloquearPropiedad(Connection conexion, long id) throws SQLException {
        var filas = BaseDatos.consultar(conexion,
            "SELECT propietario_id FROM propiedad WHERE id=? FOR UPDATE", id);
        return filas.isEmpty() ? null : Long.parseLong(filas.get(0).get(0));
    }

    public boolean propietarioActivo(Connection conexion, long id) throws SQLException {
        return !BaseDatos.consultar(conexion,
            "SELECT id FROM propietario WHERE id=? AND activo=TRUE FOR UPDATE", id).isEmpty();
    }

    public boolean tieneContratos(Connection conexion, long propiedad) throws SQLException {
        return !BaseDatos.consultar(conexion,
            "SELECT id FROM contrato WHERE propiedad_id=? LIMIT 1", propiedad).isEmpty();
    }

    public long guardarPropiedad(Connection conexion, Propiedad propiedad) throws SQLException {
        if (propiedad.id() == 0) {
            return BaseDatos.insertar(conexion, """
                INSERT INTO propiedad(codigo,direccion,propietario_id,activo) VALUES (?,?,?,?)
                """, propiedad.codigo(), propiedad.direccion(), propiedad.propietario(), propiedad.activo());
        }
        BaseDatos.insertarSinClave(conexion, """
            UPDATE propiedad SET codigo=?,direccion=?,propietario_id=?,activo=? WHERE id=?
            """, propiedad.codigo(), propiedad.direccion(), propiedad.propietario(), propiedad.activo(), propiedad.id());
        return propiedad.id();
    }

    public void bajaPropiedad(Connection conexion, long id) throws SQLException {
        BaseDatos.insertarSinClave(conexion, "UPDATE propiedad SET activo=FALSE WHERE id=?", id);
    }
}

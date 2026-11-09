package propiera.servicio;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Locale;
import propiera.modelo.DatosCatalogo;
import propiera.modelo.Persona;
import propiera.modelo.Propiedad;
import propiera.modelo.TipoPersona;
import propiera.persistencia.BaseDatos;
import propiera.persistencia.CatalogoDao;
import propiera.persistencia.OperacionDao;

public class CatalogoService {
    private final BaseDatos baseDatos;
    private final CatalogoDao catalogos = new CatalogoDao();
    private final OperacionDao operaciones = new OperacionDao();

    public CatalogoService(BaseDatos baseDatos) {
        this.baseDatos = baseDatos;
    }

    public DatosCatalogo consultar(long usuario) throws SQLException {
        try (Connection conexion = baseDatos.abrir()) {
            verificarPermiso(conexion, usuario);
            return new DatosCatalogo(catalogos.personas(conexion, TipoPersona.PROPIETARIO),
                catalogos.personas(conexion, TipoPersona.INQUILINO), catalogos.propiedades(conexion));
        }
    }

    public long guardarPersona(TipoPersona tipo, Persona datos, long usuario) throws SQLException {
        exigir(tipo != null && datos != null, "Faltan los datos de la persona");
        exigir(datos.id() >= 0, "Identificador inválido");
        Persona persona = new Persona(datos.id(), texto(datos.documento(), 20, "Documento", true),
            texto(datos.nombre(), 100, "Nombre", true), texto(datos.contacto(), 150, "Contacto", false), datos.activo());
        try (Connection conexion = baseDatos.abrir()) {
            conexion.setAutoCommit(false);
            try {
                verificarPermiso(conexion, usuario);
                if (persona.id() != 0) {
                    exigir(catalogos.bloquearPersona(conexion, tipo, persona.id()), "La persona ya no existe");
                }
                long id = catalogos.guardarPersona(conexion, tipo, persona);
                conexion.commit();
                return id;
            } catch (SQLException | RuntimeException error) {
                conexion.rollback();
                if (error instanceof SQLException sql && sql.getErrorCode() == 1062) {
                    throw new IllegalArgumentException("Ya existe una persona con ese documento. Revisá también los inactivos.", sql);
                }
                throw error;
            }
        }
    }

    public long guardarPropiedad(Propiedad datos, long usuario) throws SQLException {
        exigir(datos != null, "Faltan los datos de la propiedad");
        exigir(datos.id() >= 0 && datos.propietario() > 0, "Seleccioná un propietario");
        Propiedad propiedad = new Propiedad(datos.id(), texto(datos.codigo(), 30, "Código", true).toUpperCase(Locale.ROOT),
            texto(datos.direccion(), 200, "Dirección", true), datos.propietario(), "", datos.activo());
        try (Connection conexion = baseDatos.abrir()) {
            conexion.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            conexion.setAutoCommit(false);
            try {
                verificarPermiso(conexion, usuario);
                if (propiedad.id() != 0) {
                    Long anterior = catalogos.bloquearPropiedad(conexion, propiedad.id());
                    exigir(anterior != null, "La propiedad ya no existe");
                    exigir(anterior == propiedad.propietario() || !catalogos.tieneContratos(conexion, propiedad.id()),
                        "No se puede cambiar el propietario de una propiedad con contratos registrados");
                }
                if (propiedad.activo() || propiedad.id() == 0) {
                    exigir(catalogos.propietarioActivo(conexion, propiedad.propietario()),
                        "El propietario no existe o está inactivo");
                }
                long id = catalogos.guardarPropiedad(conexion, propiedad);
                conexion.commit();
                return id;
            } catch (SQLException | RuntimeException error) {
                conexion.rollback();
                if (error instanceof SQLException sql && sql.getErrorCode() == 1062) {
                    throw new IllegalArgumentException("Ya existe una propiedad con ese código. Revisá también las inactivas.", sql);
                }
                throw error;
            }
        }
    }

    public void bajaPersona(TipoPersona tipo, long id, long usuario) throws SQLException {
        exigir(tipo != null && id > 0, "Seleccioná una persona");
        try (Connection conexion = baseDatos.abrir()) {
            conexion.setAutoCommit(false);
            try {
                verificarPermiso(conexion, usuario);
                exigir(catalogos.bloquearPersona(conexion, tipo, id), "La persona ya no existe");
                catalogos.bajaPersona(conexion, tipo, id);
                conexion.commit();
            } catch (SQLException | RuntimeException error) {
                conexion.rollback();
                throw error;
            }
        }
    }

    public void bajaPropiedad(long id, long usuario) throws SQLException {
        exigir(id > 0, "Seleccioná una propiedad");
        try (Connection conexion = baseDatos.abrir()) {
            conexion.setAutoCommit(false);
            try {
                verificarPermiso(conexion, usuario);
                exigir(catalogos.bloquearPropiedad(conexion, id) != null, "La propiedad ya no existe");
                catalogos.bajaPropiedad(conexion, id);
                conexion.commit();
            } catch (SQLException | RuntimeException error) {
                conexion.rollback();
                throw error;
            }
        }
    }

    private void verificarPermiso(Connection conexion, long usuario) throws SQLException {
        exigir(operaciones.administrativoActivo(conexion, usuario), "El usuario no puede administrar los datos básicos");
    }

    private String texto(String valor, int maximo, String campo, boolean obligatorio) {
        String texto = valor == null ? "" : valor.trim();
        exigir(!obligatorio || !texto.isEmpty(), "Completá el campo " + campo);
        exigir(texto.codePointCount(0, texto.length()) <= maximo, campo + " supera los " + maximo + " caracteres");
        return texto;
    }

    private void exigir(boolean condicion, String mensaje) {
        if (!condicion) {
            throw new IllegalArgumentException(mensaje);
        }
    }
}

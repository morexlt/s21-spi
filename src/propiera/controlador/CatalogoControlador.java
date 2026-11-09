package propiera.controlador;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import propiera.modelo.DatosCatalogo;
import propiera.modelo.Persona;
import propiera.modelo.Propiedad;
import propiera.modelo.TipoPersona;
import propiera.servicio.CatalogoService;
import propiera.vista.CatalogosVista;
import propiera.vista.PanelPersonas;
import propiera.vista.PrincipalVista;

public class CatalogoControlador {
    private final CatalogoService servicio;
    private final PrincipalVista ventana;
    private final CatalogosVista vista;
    private final long usuario;
    private final Runnable actualizarContratos;

    public CatalogoControlador(CatalogoService servicio, PrincipalVista ventana,
            long usuario, Runnable actualizarContratos) {
        this.servicio = servicio;
        this.ventana = ventana;
        this.vista = ventana.catalogos();
        this.usuario = usuario;
        this.actualizarContratos = actualizarContratos;
    }

    public void iniciar() {
        conectarPersona(vista.propietarios(), TipoPersona.PROPIETARIO);
        conectarPersona(vista.inquilinos(), TipoPersona.INQUILINO);
        vista.propiedades().guardar().addActionListener(evento -> {
            Propiedad datos = vista.propiedades().datos();
            ejecutar(() -> servicio.guardarPropiedad(datos, usuario));
        });
        vista.propiedades().baja().addActionListener(evento -> {
            long id = vista.propiedades().idSeleccionado();
            if (confirmarBaja()) {
                ejecutar(() -> {
                    servicio.bajaPropiedad(id, usuario);
                    return id;
                });
            }
        });
    }

    private void conectarPersona(PanelPersonas panel, TipoPersona tipo) {
        panel.guardar().addActionListener(evento -> {
            Persona datos = panel.datos();
            ejecutar(() -> servicio.guardarPersona(tipo, datos, usuario));
        });
        panel.baja().addActionListener(evento -> {
            long id = panel.idSeleccionado();
            if (confirmarBaja()) {
                ejecutar(() -> {
                    servicio.bajaPersona(tipo, id, usuario);
                    return id;
                });
            }
        });
    }

    private boolean confirmarBaja() {
        return JOptionPane.showConfirmDialog(ventana,
            "El registro quedará inactivo para nuevos contratos. Se conserva el historial. ¿Continuar?",
            "Dar de baja", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    private void ejecutar(Callable<Long> operacion) {
        ventana.ocupado(true);
        new SwingWorker<DatosCatalogo, Void>() {
            @Override
            protected DatosCatalogo doInBackground() throws Exception {
                operacion.call();
                return servicio.consultar(usuario);
            }

            @Override
            protected void done() {
                ventana.ocupado(false);
                try {
                    vista.cargar(get());
                    actualizarContratos.run();
                } catch (Exception error) {
                    Throwable causa = error instanceof ExecutionException ? error.getCause() : error;
                    if (error instanceof InterruptedException) Thread.currentThread().interrupt();
                    String mensaje = causa instanceof IllegalArgumentException ? causa.getMessage()
                        : "No se pudo completar la operación. Revisá la conexión y actualizá los datos antes de reintentar.";
                    JOptionPane.showMessageDialog(ventana, mensaje, "Revisar datos", JOptionPane.WARNING_MESSAGE);
                }
            }
        }.execute();
    }
}

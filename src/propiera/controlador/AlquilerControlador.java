package propiera.controlador;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import propiera.servicio.AlquilerService;
import propiera.modelo.AltaContrato;
import propiera.modelo.Cuota;
import propiera.modelo.Opcion;
import propiera.modelo.Usuario;
import propiera.servicio.AutenticacionService;
import propiera.vista.FormularioContrato;
import propiera.vista.FormularioPago;
import propiera.vista.LoginVista;
import propiera.vista.PrincipalVista;

public class AlquilerControlador {
    private final AlquilerService servicio;
    private final AutenticacionService autenticacion;
    private final LoginVista login;
    private PrincipalVista ventana;
    private Usuario usuario;

    public AlquilerControlador(AlquilerService servicio,
            AutenticacionService autenticacion, LoginVista login) {
        this.servicio = servicio;
        this.autenticacion = autenticacion;
        this.login = login;
    }

    public void iniciar() {
        login.ingresar().addActionListener(evento -> ingresar());
        login.setVisible(true);
    }

    private void ingresar() {
        String nombre = login.usuario();
        char[] clave = login.clave();
        login.limpiarClave();
        login.ingresar().setEnabled(false);
        new SwingWorker<Usuario, Void>() {
            @Override
            protected Usuario doInBackground() throws Exception {
                try {
                    return autenticacion.ingresar(nombre, clave);
                } finally {
                    Arrays.fill(clave, '\0');
                }
            }

            @Override
            protected void done() {
                login.ingresar().setEnabled(true);
                try {
                    usuario = get();
                    abrirVentana();
                } catch (Exception error) {
                    mostrarError(error);
                }
            }
        }.execute();
    }

    private void abrirVentana() {
        ventana = new PrincipalVista(usuario);
        ventana.actualizar().addActionListener(evento -> actualizar());
        ventana.cobrar().addActionListener(evento -> seleccionarCobro());
        ventana.contrato().guardar().addActionListener(evento -> guardarContrato());
        ventana.pago().guardar().addActionListener(evento -> guardarPago());
        login.dispose();
        ventana.setVisible(true);
        actualizar();
    }

    private record Datos(List<Cuota> cuotas, List<Opcion> propiedades,
                         List<Opcion> inquilinos) { }

    private void actualizar() {
        ejecutar(() -> new Datos(servicio.consultarCuotas(), servicio.propiedades(),
                servicio.inquilinos()), datos -> {
            ventana.modeloCuotas().actualizar(datos.cuotas());
            ventana.contrato().cargarOpciones(datos.propiedades(), datos.inquilinos());
        });
    }

    private void seleccionarCobro() {
        Cuota cuota = ventana.cuotaSeleccionada();
        if (cuota == null) {
            mensaje("Seleccioná una cuota de la tabla.");
        } else if (!cuota.permiteCobro()) {
            mensaje("La cuota no tiene saldo para cobrar o está pendiente de ajuste.");
        } else {
            ventana.pago().seleccionarCuota(cuota.id(), cuota.saldo().toPlainString());
            ventana.mostrarPago();
        }
    }

    private void guardarContrato() {
        try {
            FormularioContrato formulario = ventana.contrato();
            if (formulario.propiedad() == null || formulario.inquilino() == null) {
                throw new IllegalArgumentException("Seleccioná la propiedad y el inquilino.");
            }
            AltaContrato alta = new AltaContrato(
                formulario.propiedad().id(), formulario.inquilino().id(),
                LocalDate.parse(formulario.inicio().trim()),
                LocalDate.parse(formulario.fin().trim()),
                importe(formulario.importe()), importe(formulario.comision()),
                formulario.ajusteMeses(), Integer.parseInt(formulario.vencimiento().trim()),
                formulario.mora().trim());
            ejecutar(() -> servicio.registrarContrato(alta, usuario.id()), id -> {
                mensaje("Contrato " + id + " guardado. Se generaron las cuotas mensuales.");
                actualizar();
            });
        } catch (Exception error) {
            mostrarError(error);
        }
    }

    private void guardarPago() {
        try {
            FormularioPago formulario = ventana.pago();
            long cuota = Long.parseLong(formulario.cuota().trim());
            LocalDate fecha = LocalDate.parse(formulario.fecha().trim());
            BigDecimal capital = importe(formulario.capital());
            BigDecimal mora = importe(formulario.mora());
            String medio = formulario.medio();
            String referencia = formulario.referencia().trim();
            ejecutar(() -> servicio.registrarPago(cuota, fecha, capital, mora,
                    medio, referencia, usuario.id()), id -> {
                mensaje("Pago " + id + " guardado.");
                formulario.limpiarCobro();
                actualizar();
            });
        } catch (Exception error) {
            mostrarError(error);
        }
    }

    public static BigDecimal importe(String texto) {
        return AlquilerService.dinero(texto);
    }

    private <T> void ejecutar(Callable<T> tarea, Consumer<T> resultado) {
        ventana.ocupado(true);
        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() throws Exception {
                return tarea.call();
            }

            @Override
            protected void done() {
                ventana.ocupado(false);
                try {
                    resultado.accept(get());
                } catch (Exception error) {
                    mostrarError(error);
                }
            }
        }.execute();
    }

    private void mensaje(String texto) {
        JOptionPane.showMessageDialog(ventana, texto, "Propiera", JOptionPane.INFORMATION_MESSAGE);
    }

    private void mostrarError(Exception error) {
        if (error instanceof InterruptedException) {
            Thread.currentThread().interrupt();
        }
        Throwable causa = error instanceof ExecutionException ? error.getCause() : error;
        String texto;
        if (causa instanceof java.time.format.DateTimeParseException) {
            texto = "Ingresá las fechas con el formato AAAA-MM-DD.";
        } else if (causa instanceof NumberFormatException) {
            texto = "Revisá los campos numéricos.";
        } else if (causa instanceof java.sql.SQLIntegrityConstraintViolationException) {
            texto = "No se pudo guardar. Revisá la referencia del pago y los datos relacionados.";
        } else if (causa instanceof java.sql.SQLException) {
            texto = "No se pudo completar la operación en MySQL. Revisá la conexión y volvé a intentar.";
        } else if (causa instanceof IllegalArgumentException || causa instanceof IllegalStateException) {
            texto = causa.getMessage();
        } else {
            texto = "No se pudo completar la operación.";
            causa.printStackTrace();
        }
        JOptionPane.showMessageDialog(ventana == null ? login : ventana,
            texto, "Revisar datos", JOptionPane.WARNING_MESSAGE);
    }
}

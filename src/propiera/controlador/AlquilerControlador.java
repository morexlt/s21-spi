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
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.Locale;
import propiera.persistencia.ArchivoCuotas;
import propiera.servicio.AlquilerService;
import propiera.servicio.CatalogoService;
import propiera.modelo.DatosCatalogo;
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
    private final CatalogoService catalogos;
    private final LoginVista login;
    private PrincipalVista ventana;
    private Usuario usuario;

    public AlquilerControlador(AlquilerService servicio,
            AutenticacionService autenticacion, LoginVista login, CatalogoService catalogos) {
        this.servicio = servicio;
        this.autenticacion = autenticacion;
        this.login = login;
        this.catalogos = catalogos;
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
        ventana.exportar().addActionListener(evento -> exportarCuotas());
        ventana.cobrar().addActionListener(evento -> seleccionarCobro());
        ventana.confirmarAjuste().addActionListener(evento -> seleccionarAjuste());
        ventana.ajuste().guardar().addActionListener(evento -> guardarAjuste());
        ventana.contrato().guardar().addActionListener(evento -> guardarContrato());
        ventana.pago().guardar().addActionListener(evento -> guardarPago());
        if (usuario.puedeCargar()) {
            new CatalogoControlador(catalogos, ventana, usuario.id(), this::actualizar).iniciar();
        }
        login.dispose();
        ventana.setVisible(true);
        actualizar();
    }

    private record Datos(List<Cuota> cuotas, List<Opcion> propiedades,
                         List<Opcion> inquilinos, DatosCatalogo catalogos) { }

    private void actualizar() {
        ejecutar(() -> new Datos(servicio.consultarCuotas(), servicio.propiedades(),
                servicio.inquilinos(), usuario.puedeCargar() ? catalogos.consultar(usuario.id()) : null), datos -> {
            ventana.modeloCuotas().actualizar(datos.cuotas());
            ventana.contrato().cargarOpciones(datos.propiedades(), datos.inquilinos());
            if (datos.catalogos() != null) ventana.catalogos().cargar(datos.catalogos());
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

    private void exportarCuotas() {
        Cuota cuota = ventana.cuotaSeleccionada();
        if (cuota == null) {
            mensaje("Seleccioná una cuota del contrato que querés exportar.");
            return;
        }
        JFileChooser selector = new JFileChooser();
        selector.setDialogTitle("Exportar cuotas del contrato " + cuota.contrato());
        selector.setFileFilter(new FileNameExtensionFilter("Archivo de texto (*.txt)", "txt"));
        selector.setAcceptAllFileFilterUsed(false);
        selector.setSelectedFile(new java.io.File("cuotas-contrato-" + cuota.contrato() + ".txt"));
        if (selector.showSaveDialog(ventana) != JFileChooser.APPROVE_OPTION) return;
        Path elegido = selector.getSelectedFile().toPath();
        if (!elegido.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".txt")) {
            elegido = elegido.resolveSibling(elegido.getFileName() + ".txt");
        }
        Path destino = elegido;
        boolean reemplazar = Files.exists(destino);
        if (reemplazar && JOptionPane.showConfirmDialog(ventana,
                "El archivo ya existe. ¿Querés reemplazarlo?", "Exportar cuotas",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        ejecutar(() -> {
            var estado = servicio.consultarContrato(cuota.contrato(), usuario.id());
            new ArchivoCuotas().guardar(destino, estado, reemplazar);
            return destino.toAbsolutePath();
        }, archivo -> mensaje("Archivo guardado en:\n" + archivo));
    }

    private void seleccionarAjuste() {
        Cuota cuota = ventana.cuotaSeleccionada();
        if (cuota == null) {
            mensaje("Seleccioná la primera cuota pendiente del tramo en la tabla.");
        } else if (cuota.importe() != null) {
            mensaje("La cuota ya tiene un importe confirmado.");
        } else {
            ventana.ajuste().seleccionarCuota(cuota);
            ventana.mostrarAjuste();
        }
    }

    private void guardarAjuste() {
        try {
            if (ventana.ajuste().cuota().isBlank()) {
                throw new IllegalArgumentException("Seleccioná una cuota pendiente desde la tabla.");
            }
            long cuota = Long.parseLong(ventana.ajuste().cuota());
            BigDecimal importe = importe(ventana.ajuste().importe());
            int respuesta = JOptionPane.showConfirmDialog(ventana,
                "Se confirmará un importe mensual de $" + importe.toPlainString()
                    + " para el tramo de ajuste. ¿Continuar?", "Confirmar ajuste", JOptionPane.YES_NO_OPTION);
            if (respuesta != JOptionPane.YES_OPTION) {
                return;
            }
            ejecutar(() -> servicio.confirmarAjuste(cuota, importe, usuario.id()), cantidad -> {
                mensaje("Ajuste guardado. Cuotas actualizadas: " + cantidad + ".");
                ventana.ajuste().limpiar();
                actualizar();
            });
        } catch (Exception error) {
            mostrarError(error);
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
                formulario.limpiar();
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
        } else if (causa instanceof java.nio.file.FileAlreadyExistsException) {
            texto = "El archivo ya existe. Elegí otro nombre o confirmá que querés reemplazarlo.";
        } else if (causa instanceof java.io.IOException) {
            texto = "No se pudo guardar el archivo. Revisá la carpeta y los permisos de escritura.";
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

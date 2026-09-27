package propiera;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import javax.swing.SwingUtilities;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.JLabel;
import javax.swing.SortOrder;
import javax.swing.RowSorter;
import propiera.controlador.AlquilerControlador;
import propiera.modelo.AltaContrato;
import propiera.modelo.Cuota;
import propiera.modelo.Opcion;
import propiera.modelo.Usuario;
import propiera.persistencia.BaseDatos;
import propiera.servicio.AlquilerService;
import propiera.servicio.AutenticacionService;
import propiera.vista.FormularioContrato;
import propiera.vista.FormularioPago;
import propiera.vista.TablaCuotasModelo;
import propiera.vista.ImporteRenderizador;

public class PruebasSwing {
    private static int total;

    private static void comprobar(String nombre, boolean resultado) {
        if (!resultado) {
            throw new AssertionError(nombre);
        }
        total++;
        System.out.println("PASS " + nombre);
    }

    private static Usuario ingresar(AutenticacionService servicio, String usuario,
            String variable) throws Exception {
        char[] clave = BaseDatos.obligatorio(variable).toCharArray();
        try {
            return servicio.ingresar(usuario, clave);
        } finally {
            Arrays.fill(clave, '\0');
        }
    }

    public static void main(String[] args) throws Exception {
        BaseDatos baseDatos = new BaseDatos();
        AlquilerService servicio = new AlquilerService(baseDatos);
        AutenticacionService autenticacion = new AutenticacionService(baseDatos);
        comprobar("SW01 administrativo habilitado",
            ingresar(autenticacion, "administrativo", "PROPIERA_ADMIN_PASSWORD").puedeCargar());
        comprobar("SW02 martillero sólo consulta",
            !ingresar(autenticacion, "martillero", "PROPIERA_MARTILLERO_PASSWORD").puedeCargar());
        try {
            autenticacion.ingresar("administrativo", new char[]{'x'});
            throw new AssertionError("Se aceptó una contraseña incorrecta");
        } catch (IllegalArgumentException esperado) {
            comprobar("SW03 contraseña incorrecta rechazada",
                "Usuario o contraseña incorrectos.".equals(esperado.getMessage()));
        }
        try {
            autenticacion.ingresar("desconocido", new char[]{'x'});
            throw new AssertionError("Se aceptó un usuario desconocido");
        } catch (IllegalArgumentException esperado) {
            comprobar("SW04 usuario desconocido rechazado",
                "Usuario o contraseña incorrectos.".equals(esperado.getMessage()));
        }
        comprobar("SW05 catálogo con propietario y caracteres españoles",
            servicio.propiedades().stream().anyMatch(opcion -> opcion.descripcion().contains("Muñoz")));
        comprobar("SW06 importe con coma decimal",
            AlquilerControlador.importe(" 120,50 ").equals(new BigDecimal("120.50"))
            && AlquilerControlador.importe("1000").equals(new BigDecimal("1000.00"))
            && AlquilerService.dinero("12.34").equals(new BigDecimal("12.34")));
        String[] invalidos = {"12.345", "1.000", "1,000", "12.340", "12,340",
            "1.000,00", "1,000.00", "1e3", "NaN", "", ".50", "12.", "1 000", "+10", null};
        for (String texto : invalidos) {
            for (boolean desdeControlador : new boolean[]{false, true}) {
                try {
                    if (desdeControlador) {
                        AlquilerControlador.importe(texto);
                    } else {
                        AlquilerService.dinero(texto);
                    }
                    throw new AssertionError("Se aceptó el importe: " + texto);
                } catch (IllegalArgumentException esperado) {
                    if (!"Ingresá un importe sin separador de miles y con hasta dos decimales."
                            .equals(esperado.getMessage())) {
                        throw new AssertionError("Motivo de rechazo incorrecto", esperado);
                    }
                }
            }
        }
        comprobar("SW07 formato monetario inválido rechazado en servicio y controlador", true);
        var alta = new AltaContrato(1, 1, LocalDate.of(2031, 1, 1),
            LocalDate.of(2031, 12, 31), new BigDecimal("100000.00"),
            new BigDecimal("5.00"), 3, 10, "Mora manual");
        try {
            servicio.registrarContrato(alta, 2);
            throw new AssertionError("Martillero pudo registrar un contrato");
        } catch (IllegalArgumentException esperado) {
            comprobar("SW08 permiso de contrato verificado en servicio",
                "El usuario no puede registrar contratos".equals(esperado.getMessage()));
        }
        Cuota primera = servicio.consultarCuotas().get(0);
        try {
            servicio.registrarPago(primera.id(), LocalDate.now(), BigDecimal.ONE,
                BigDecimal.ZERO, "EFECTIVO", "NO-DEBE-GUARDARSE", 2);
            throw new AssertionError("Martillero pudo registrar un pago");
        } catch (IllegalArgumentException esperado) {
            comprobar("SW09 permiso de pago verificado en servicio",
                "El usuario no puede registrar pagos".equals(esperado.getMessage()));
        }
        SwingUtilities.invokeAndWait(() -> {
            FormularioContrato contrato = new FormularioContrato();
            contrato.cargarOpciones(List.of(new Opcion(1, "Casa / Ana Muñoz"), new Opcion(2, "Casa 2")),
                List.of(new Opcion(1, "Lucía Núñez"), new Opcion(2, "Inquilino 2")));
            var selectores = Arrays.stream(contrato.getComponents())
                .filter(componente -> componente instanceof JComboBox<?>)
                .map(componente -> (JComboBox<?>) componente).toList();
            selectores.get(0).setSelectedIndex(1);
            selectores.get(1).setSelectedIndex(1);
            String importeAnterior = contrato.importe();
            String inicioAnterior = contrato.inicio();
            contrato.cargarOpciones(List.of(new Opcion(1, "Casa / Ana Muñoz"), new Opcion(2, "Casa actualizada")),
                List.of(new Opcion(1, "Lucía Núñez"), new Opcion(2, "Inquilino actualizado")));
            boolean conserva = contrato.propiedad().id() == 2 && contrato.inquilino().id() == 2
                && contrato.propiedad().descripcion().equals("Casa actualizada");
            contrato.cargarOpciones(List.of(new Opcion(2, "Casa 2"), new Opcion(1, "Casa 1")),
                List.of(new Opcion(2, "Inquilino 2"), new Opcion(1, "Lucía Núñez")));
            conserva &= contrato.propiedad().id() == 2 && contrato.inquilino().id() == 2
                && contrato.importe().equals(importeAnterior) && contrato.inicio().equals(inicioAnterior);
            contrato.cargarOpciones(List.of(new Opcion(1, "Casa 1")), List.of(new Opcion(1, "Lucía Núñez")));
            conserva &= contrato.propiedad() == null && contrato.inquilino() == null;
            contrato.cargarOpciones(List.of(new Opcion(1, "Casa 1")), List.of(new Opcion(1, "Lucía Núñez")));
            comprobar("SW10 actualización conserva selección por ID o queda sin selección", conserva
                && contrato.propiedad() == null && contrato.inquilino() == null);
            FormularioPago pago = new FormularioPago();
            pago.seleccionarCuota(17, "100.00");
            comprobar("SW11 cobro seleccionado", pago.cuota().equals("17")
                && pago.capital().equals("100.00"));
            pago.limpiarCobro();
            comprobar("SW12 formulario de cobro limpio", pago.cuota().isEmpty()
                && pago.capital().isEmpty() && pago.mora().equals("0.00"));
            TablaCuotasModelo tabla = new TablaCuotasModelo();
            Cuota pendiente = new Cuota(1, 1, "P-01", LocalDate.now(), LocalDate.now(), null, null);
            tabla.actualizar(List.of(pendiente));
            JTable vistaTabla = new JTable(tabla);
            vistaTabla.setAutoCreateRowSorter(true);
            vistaTabla.setDefaultRenderer(BigDecimal.class, new ImporteRenderizador());
            String textoPendiente = ((JLabel) vistaTabla.prepareRenderer(
                vistaTabla.getCellRenderer(0, 5), 0, 5)).getText();
            String textoSaldo = ((JLabel) vistaTabla.prepareRenderer(
                vistaTabla.getCellRenderer(0, 6), 0, 6)).getText();
            comprobar("SW13 cuota sin ajuste visible y no cobrable",
                tabla.getValueAt(0, 5) == null && textoPendiente.equals("Pendiente de ajuste")
                && textoSaldo.equals("—") && !pendiente.permiteCobro());
            Cuota mayor = new Cuota(2, 2, "P-02", LocalDate.now(), LocalDate.now(),
                new BigDecimal("1000.00"), new BigDecimal("1000.00"));
            Cuota menor = new Cuota(3, 3, "P-03", LocalDate.now(), LocalDate.now(),
                new BigDecimal("900.00"), new BigDecimal("900.00"));
            tabla.actualizar(List.of(mayor, menor, pendiente));
            boolean ordenCorrecto = true;
            for (int columna : new int[]{5, 6}) {
                vistaTabla.getRowSorter().setSortKeys(List.of(new RowSorter.SortKey(columna, SortOrder.ASCENDING)));
                ordenCorrecto &= tabla.cuota(vistaTabla.convertRowIndexToModel(0)).id() == 1
                    && tabla.cuota(vistaTabla.convertRowIndexToModel(1)).id() == 3
                    && tabla.cuota(vistaTabla.convertRowIndexToModel(2)).id() == 2;
                vistaTabla.getRowSorter().setSortKeys(List.of(new RowSorter.SortKey(columna, SortOrder.DESCENDING)));
                ordenCorrecto &= tabla.cuota(vistaTabla.convertRowIndexToModel(0)).id() == 2
                    && tabla.cuota(vistaTabla.convertRowIndexToModel(1)).id() == 3;
            }
            String textoImporte = ((JLabel) vistaTabla.prepareRenderer(
                vistaTabla.getCellRenderer(0, 5), 0, 5)).getText();
            comprobar("SW14 tabla numérica, ordenable y no editable", ordenCorrecto
                && tabla.getColumnClass(5) == BigDecimal.class && tabla.getColumnClass(6) == BigDecimal.class
                && textoImporte.equals("1000.00") && !tabla.isCellEditable(0, 5));
            comprobar("SW15 cuota pagada no cobrable", !primera.permiteCobro());
        });
        System.out.println("RESULTADO: " + total + " verificaciones Swing y permisos aprobadas.");
    }
}

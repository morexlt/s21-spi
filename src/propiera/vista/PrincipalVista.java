package propiera.vista;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import propiera.modelo.Cuota;
import propiera.modelo.Usuario;

public class PrincipalVista extends JFrame {
    private final JTabbedPane pestanas = new JTabbedPane();
    private final TablaCuotasModelo modelo = new TablaCuotasModelo();
    private final JTable tabla = new JTable(modelo);
    private final FormularioContrato contrato = new FormularioContrato();
    private final FormularioPago pago = new FormularioPago();
    private final JButton actualizar = new JButton("Actualizar cuotas");
    private final JButton cobrar = new JButton("Cobrar cuota seleccionada");
    private final JLabel estado = new JLabel(" ");
    private final boolean puedeCargar;

    public PrincipalVista(Usuario usuario) {
        super("Propiera · Gestión de alquileres");
        puedeCargar = usuario.puedeCargar();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(960, 600));
        setSize(1160, 740);
        setLocationRelativeTo(null);

        JLabel titulo = new JLabel("Propiera  |  " + usuario.nombre() + "  |  " + usuario.rol());
        titulo.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        add(titulo, BorderLayout.NORTH);
        tabla.setRowHeight(28);
        tabla.setAutoCreateRowSorter(true);
        tabla.setDefaultRenderer(java.math.BigDecimal.class, new ImporteRenderizador());
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JPanel cuotas = new JPanel(new BorderLayout(10, 10));
        cuotas.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        acciones.add(actualizar);
        if (puedeCargar) {
            acciones.add(cobrar);
        }
        cuotas.add(acciones, BorderLayout.NORTH);
        cuotas.add(new JScrollPane(tabla), BorderLayout.CENTER);
        cuotas.add(new JLabel("Sin importe: pendiente de ajuste. Saldo cero: capital cancelado."), BorderLayout.SOUTH);
        pestanas.addTab("Cuotas y saldos", cuotas);
        if (puedeCargar) {
            pestanas.addTab("Registrar contrato", new JScrollPane(contrato));
            pestanas.addTab("Registrar pago", new JScrollPane(pago));
        }
        add(pestanas, BorderLayout.CENTER);
        estado.setBorder(BorderFactory.createEmptyBorder(10, 20, 12, 20));
        add(estado, BorderLayout.SOUTH);
    }

    public Cuota cuotaSeleccionada() {
        int fila = tabla.getSelectedRow();
        return fila < 0 ? null : modelo.cuota(tabla.convertRowIndexToModel(fila));
    }

    public void mostrarPago() {
        pestanas.setSelectedIndex(2);
    }
    public FormularioContrato contrato() {
        return contrato;
    }
    public FormularioPago pago() {
        return pago;
    }
    public TablaCuotasModelo modeloCuotas() {
        return modelo;
    }
    public JButton actualizar() {
        return actualizar;
    }
    public JButton cobrar() {
        return cobrar;
    }

    public void ocupado(boolean ocupado) {
        actualizar.setEnabled(!ocupado);
        cobrar.setEnabled(!ocupado && puedeCargar);
        contrato.guardar().setEnabled(!ocupado && puedeCargar);
        pago.guardar().setEnabled(!ocupado && puedeCargar);
        estado.setText(ocupado ? "Consultando MySQL…" : "Propiera · Gestión de alquileres · Datos de prueba");
    }
}

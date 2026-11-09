package propiera.vista;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.TableRowSorter;
import propiera.modelo.Cuota;
import propiera.modelo.Usuario;

public class PrincipalVista extends JFrame {
    private final JTabbedPane pestanas = new JTabbedPane();
    private final TablaCuotasModelo modelo = new TablaCuotasModelo();
    private final JTable tabla = new JTable(modelo);
    private final TableRowSorter<TablaCuotasModelo> ordenador = new TableRowSorter<>(modelo);
    private final FiltroPropiedades filtro = new FiltroPropiedades(modelo, ordenador);
    private final FormularioContrato contrato = new FormularioContrato();
    private final FormularioPago pago = new FormularioPago();
    private final FormularioAjuste ajuste = new FormularioAjuste();
    private final CatalogosVista catalogos = new CatalogosVista();
    private final JButton actualizar = new JButton("Actualizar cuotas");
    private final JButton cobrar = new JButton("Cobrar cuota seleccionada");
    private final JButton confirmarAjuste = new JButton("Confirmar ajuste del tramo");
    private final JButton exportar = new JButton("Exportar cuotas");
    private final JLabel estado = new JLabel(" ");
    private final JLabel resumen = EstiloVista.ayuda("Sin cuotas cargadas");
    private final boolean puedeCargar;
    private boolean ocupado;

    public PrincipalVista(Usuario usuario) {
        super("Propiera · Gestión de alquileres");
        puedeCargar = usuario.puedeCargar();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(960, 600));
        setSize(1160, 800);
        setLocationRelativeTo(null);

        getContentPane().setBackground(EstiloVista.FONDO);
        JPanel cabecera = new JPanel(new BorderLayout(10, 4));
        cabecera.setBackground(Color.WHITE);
        cabecera.setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));
        cabecera.add(EstiloVista.titulo("Propiera"), BorderLayout.WEST);
        cabecera.add(EstiloVista.ayuda("Gestión de alquileres"), BorderLayout.CENTER);
        String rol = puedeCargar ? "Administrativo" : "Martillero";
        cabecera.add(EstiloVista.ayuda(usuario.nombre() + "  ·  " + rol), BorderLayout.EAST);
        add(cabecera, BorderLayout.NORTH);
        tabla.setFont(tabla.getFont().deriveFont(Font.PLAIN, 14f));
        tabla.setRowHeight(34);
        tabla.setShowGrid(false);
        tabla.setIntercellSpacing(new Dimension(0, 0));
        tabla.setFillsViewportHeight(true);
        tabla.setBackground(Color.WHITE);
        tabla.setSelectionBackground(new Color(215, 238, 233));
        tabla.setSelectionForeground(EstiloVista.TEXTO);
        tabla.getTableHeader().setFont(tabla.getFont().deriveFont(Font.BOLD, 13f));
        tabla.getTableHeader().setPreferredSize(new Dimension(0, 38));
        tabla.getTableHeader().setReorderingAllowed(false);
        tabla.setRowSorter(ordenador);
        ordenador.addRowSorterListener(evento -> actualizarResumen());
        tabla.setDefaultRenderer(String.class, new CeldaRenderizador());
        tabla.setDefaultRenderer(Long.class, new CeldaRenderizador());
        tabla.setDefaultRenderer(java.math.BigDecimal.class, new ImporteRenderizador());
        int[] anchos = {55, 65, 290, 120, 120, 190, 145};
        for (int columna = 0; columna < anchos.length; columna++) {
            tabla.getColumnModel().getColumn(columna).setPreferredWidth(anchos[columna]);
        }
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getSelectionModel().addListSelectionListener(evento -> actualizarAcciones());
        modelo.addTableModelListener(evento -> SwingUtilities.invokeLater(this::actualizarResumen));
        JPanel cuotas = new JPanel(new BorderLayout(10, 10));
        cuotas.setBackground(EstiloVista.FONDO);
        cuotas.setBorder(BorderFactory.createEmptyBorder(22, 24, 16, 24));
        JPanel encabezado = new JPanel(new BorderLayout(8, 14));
        encabezado.setOpaque(false);
        encabezado.add(EstiloVista.titulo("Cuotas y saldos"), BorderLayout.NORTH);
        encabezado.add(resumen, BorderLayout.CENTER);
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        acciones.setOpaque(false);
        acciones.add(actualizar);
        if (puedeCargar) {
            EstiloVista.botonPrincipal(cobrar);
            acciones.add(javax.swing.Box.createHorizontalStrut(12));
            acciones.add(cobrar);
            acciones.add(javax.swing.Box.createHorizontalStrut(12));
            acciones.add(confirmarAjuste);
        }
        acciones.add(javax.swing.Box.createHorizontalStrut(12));
        acciones.add(exportar);
        JPanel herramientas = new JPanel(new BorderLayout(0, 10));
        herramientas.setOpaque(false);
        herramientas.add(filtro, BorderLayout.NORTH);
        herramientas.add(acciones, BorderLayout.SOUTH);
        encabezado.add(herramientas, BorderLayout.SOUTH);
        cuotas.add(encabezado, BorderLayout.NORTH);
        JScrollPane listado = new JScrollPane(tabla);
        listado.setBorder(BorderFactory.createLineBorder(EstiloVista.BORDE));
        cuotas.add(listado, BorderLayout.CENTER);
        cuotas.add(EstiloVista.ayuda("Seleccioná una fila para operar. Saldo cero: capital cancelado."), BorderLayout.SOUTH);
        pestanas.addTab("Cuotas y saldos", cuotas);
        if (puedeCargar) {
            pestanas.addTab("Registrar contrato", contenerFormulario(contrato));
            pestanas.addTab("Registrar pago", contenerFormulario(pago));
            pestanas.addTab("Confirmar ajuste", contenerFormulario(ajuste));
            pestanas.addTab("Datos básicos", catalogos);
        }
        add(pestanas, BorderLayout.CENTER);
        estado.setBorder(BorderFactory.createEmptyBorder(10, 20, 12, 20));
        add(estado, BorderLayout.SOUTH);
        estado.setForeground(EstiloVista.SECUNDARIO);
        ocupado(false);
    }

    private JScrollPane contenerFormulario(JPanel formulario) {
        JPanel fondo = new JPanel(new GridBagLayout());
        fondo.setBackground(EstiloVista.FONDO);
        GridBagConstraints posicion = new GridBagConstraints();
        posicion.gridx = 0;
        posicion.gridy = 0;
        posicion.anchor = GridBagConstraints.NORTH;
        posicion.insets = new Insets(24, 24, 24, 24);
        fondo.add(formulario, posicion);
        posicion.gridy = 1;
        posicion.weighty = 1;
        fondo.add(javax.swing.Box.createVerticalGlue(), posicion);
        JScrollPane contenedor = new JScrollPane(fondo);
        contenedor.setBorder(BorderFactory.createEmptyBorder());
        contenedor.getVerticalScrollBar().setUnitIncrement(20);
        return contenedor;
    }

    private void actualizarResumen() {
        int conSaldo = 0;
        int pendientes = 0;
        for (int fila = 0; fila < tabla.getRowCount(); fila++) {
            Cuota cuota = modelo.cuota(tabla.convertRowIndexToModel(fila));
            if (cuota.importe() == null) {
                pendientes++;
            } else if (cuota.permiteCobro()) {
                conSaldo++;
            }
        }
        resumen.setText(tabla.getRowCount() + " de " + modelo.getRowCount() + " cuotas  ·  " + conSaldo
            + " con saldo  ·  " + pendientes + " pendientes de ajuste");
        actualizarAcciones();
    }

    private void actualizarAcciones() {
        boolean seleccionada = tabla.getSelectedRow() >= 0;
        cobrar.setEnabled(!ocupado && puedeCargar && seleccionada);
        confirmarAjuste.setEnabled(!ocupado && puedeCargar && seleccionada);
        exportar.setEnabled(!ocupado && seleccionada);
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
    public CatalogosVista catalogos() {
        return catalogos;
    }
    public FormularioAjuste ajuste() {
        return ajuste;
    }
    public JButton confirmarAjuste() {
        return confirmarAjuste;
    }
    public void mostrarAjuste() {
        pestanas.setSelectedIndex(3);
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
    public JButton exportar() {
        return exportar;
    }

    public void ocupado(boolean ocupado) {
        this.ocupado = ocupado;
        catalogos.ocupado(ocupado || !puedeCargar);
        actualizar.setEnabled(!ocupado);
        actualizarAcciones();
        ajuste.guardar().setEnabled(!ocupado && puedeCargar);
        contrato.guardar().setEnabled(!ocupado && puedeCargar);
        pago.guardar().setEnabled(!ocupado && puedeCargar);
        estado.setText(ocupado ? "Cargando datos…" : "Propiera · Gestión de alquileres · Datos de prueba");
    }
}

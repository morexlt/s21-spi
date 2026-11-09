package propiera.vista;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public abstract class PanelAbm extends JPanel {
    protected final JTable tabla;
    protected final DefaultTableModel modelo;
    protected final JCheckBox activo = new JCheckBox("Activo", true);
    protected long id;
    private final JPanel campos = new JPanel(new GridBagLayout());
    private final JButton nuevo = new JButton("Nuevo");
    private final JButton guardar = new JButton("Guardar");
    private final JButton baja = new JButton("Dar de baja");
    private int fila;
    private boolean ocupado;
    private final String[] columnas;

    protected PanelAbm(String titulo, String[] columnas) {
        super(new BorderLayout(12, 12));
        this.columnas = columnas;
        setBackground(EstiloVista.FONDO);
        setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        add(EstiloVista.titulo(titulo), BorderLayout.NORTH);
        modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int columna) {
                if (columna == 0) return Long.class;
                return columna == getColumnCount() - 1 ? Boolean.class : String.class;
            }
        };
        tabla = new JTable(modelo);
        tabla.setAutoCreateRowSorter(true);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setRowHeight(30);
        tabla.getTableHeader().setReorderingAllowed(false);
        tabla.getColumnModel().getColumn(0).setPreferredWidth(45);
        tabla.setDefaultRenderer(String.class, new CeldaRenderizador());
        tabla.setDefaultRenderer(Long.class, new CeldaRenderizador());
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        JPanel editor = new JPanel(new BorderLayout(0, 8));
        editor.setOpaque(false);
        campos.setOpaque(false);
        editor.add(campos, BorderLayout.CENTER);
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        botones.setOpaque(false);
        botones.add(EstiloVista.ayuda("La baja conserva contratos y cobros."));
        botones.add(nuevo);
        botones.add(baja);
        EstiloVista.botonPrincipal(guardar);
        botones.add(guardar);
        editor.add(botones, BorderLayout.SOUTH);
        add(editor, BorderLayout.SOUTH);
        nuevo.addActionListener(evento -> limpiar());
        activo.addActionListener(evento -> actualizarBaja());
    }

    protected void agregarCampo(String nombre, JComponent componente) {
        GridBagConstraints posicion = new GridBagConstraints();
        posicion.gridx = 0;
        posicion.gridy = fila++;
        posicion.anchor = GridBagConstraints.WEST;
        posicion.insets = new Insets(4, 0, 4, 20);
        JLabel etiqueta = new JLabel(nombre);
        etiqueta.setLabelFor(componente);
        campos.add(etiqueta, posicion);
        posicion.gridx = 1;
        posicion.weightx = 1;
        posicion.fill = GridBagConstraints.HORIZONTAL;
        posicion.insets = new Insets(4, 0, 4, 0);
        campos.add(componente, posicion);
    }

    protected void seleccion(long id, boolean activo) {
        this.id = id;
        this.activo.setSelected(activo);
        actualizarBaja();
    }

    protected void cargarFilas(Object[][] filas) {
        modelo.setDataVector(filas, columnas);
        tabla.getColumnModel().getColumn(0).setPreferredWidth(45);
    }

    public void ocupado(boolean ocupado) {
        this.ocupado = ocupado;
        habilitar(campos, !ocupado);
        tabla.setEnabled(!ocupado);
        guardar.setEnabled(!ocupado);
        nuevo.setEnabled(!ocupado);
        actualizarBaja();
    }

    private void habilitar(java.awt.Container contenedor, boolean habilitado) {
        for (var componente : contenedor.getComponents()) {
            componente.setEnabled(habilitado);
            if (componente instanceof java.awt.Container hijo) habilitar(hijo, habilitado);
        }
    }

    private void actualizarBaja() {
        baja.setEnabled(!ocupado && id > 0 && activo.isSelected());
    }

    public long idSeleccionado() { return id; }
    public JButton guardar() { return guardar; }
    public JButton baja() { return baja; }
    public abstract void limpiar();
}

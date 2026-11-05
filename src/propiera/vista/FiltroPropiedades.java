package propiera.vista;

import java.awt.FlowLayout;
import java.awt.Dimension;
import java.util.TreeSet;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.table.TableRowSorter;

public class FiltroPropiedades extends JPanel {
    private static final String TODAS = "Todas las propiedades";
    private final JComboBox<String> propiedades = new JComboBox<>();
    private final TablaCuotasModelo modelo;
    private final TableRowSorter<TablaCuotasModelo> ordenador;
    private boolean cargando;

    public FiltroPropiedades(TablaCuotasModelo modelo, TableRowSorter<TablaCuotasModelo> ordenador) {
        super(new FlowLayout(FlowLayout.LEFT, 0, 0));
        this.modelo = modelo;
        this.ordenador = ordenador;
        setOpaque(false);
        JLabel etiqueta = EstiloVista.ayuda("Propiedad  ");
        etiqueta.setLabelFor(propiedades);
        propiedades.setPreferredSize(new Dimension(420, 30));
        propiedades.setMaximumRowCount(10);
        add(etiqueta);
        add(propiedades);
        propiedades.addActionListener(evento -> {
            if (!cargando) {
                aplicar();
            }
        });
        // JTable debe terminar de actualizar sus filas antes de volver a filtrar.
        modelo.addTableModelListener(evento -> SwingUtilities.invokeLater(this::actualizarPropiedades));
        actualizarPropiedades();
    }

    private void actualizarPropiedades() {
        String anterior = (String) propiedades.getSelectedItem();
        TreeSet<String> opciones = new TreeSet<>();
        for (int fila = 0; fila < modelo.getRowCount(); fila++) {
            opciones.add(modelo.cuota(fila).propiedad());
        }
        cargando = true;
        propiedades.removeAllItems();
        propiedades.addItem(TODAS);
        opciones.forEach(propiedades::addItem);
        if (anterior != null && opciones.contains(anterior)) {
            propiedades.setSelectedItem(anterior);
        }
        cargando = false;
        aplicar();
    }

    private void aplicar() {
        String elegida = (String) propiedades.getSelectedItem();
        propiedades.setToolTipText(elegida);
        if (TODAS.equals(elegida)) {
            ordenador.setRowFilter(null);
        } else {
            ordenador.setRowFilter(new RowFilter<TablaCuotasModelo, Integer>() {
                @Override
                public boolean include(Entry<? extends TablaCuotasModelo, ? extends Integer> fila) {
                    return elegida.equals(fila.getStringValue(2));
                }
            });
        }
    }
}

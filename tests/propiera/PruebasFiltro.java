package propiera;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.TableRowSorter;
import propiera.modelo.Cuota;
import propiera.vista.FiltroPropiedades;
import propiera.vista.TablaCuotasModelo;

public class PruebasFiltro {
    private static final TablaCuotasModelo modelo = new TablaCuotasModelo();
    private static final JTable tabla = new JTable(modelo);
    private static final TableRowSorter<TablaCuotasModelo> ordenador = new TableRowSorter<>(modelo);
    private static JComboBox<?> selector;
    private static int total;

    private static void comprobar(String caso, boolean correcto) {
        if (!correcto) throw new AssertionError(caso);
        total++;
        System.out.println("PASS " + caso);
    }

    private static Cuota cuota(long id, String propiedad, String importe) {
        BigDecimal monto = importe == null ? null : new BigDecimal(importe);
        return new Cuota(id, id, propiedad, LocalDate.of(2026,1,1),
            LocalDate.of(2026,1,10), monto, monto);
    }

    public static void main(String[] args) throws Exception {
        Cuota primera = cuota(1, "P-001 · Roca 425", "900.00");
        Cuota segunda = cuota(2, "P-010 · Roca 425", "1000.00");
        Cuota pendiente = cuota(3, primera.propiedad(), null);
        SwingUtilities.invokeAndWait(() -> {
            tabla.setRowSorter(ordenador);
            FiltroPropiedades filtro = new FiltroPropiedades(modelo, ordenador);
            selector = (JComboBox<?>) filtro.getComponent(1);
            comprobar("FI01 consulta vacía", tabla.getRowCount() == 0 && selector.getItemCount() == 1);
            modelo.actualizar(List.of(primera, segunda, pendiente));
        });
        SwingUtilities.invokeAndWait(() -> {
            comprobar("FI02 todas las cuotas y opciones sin duplicados", tabla.getRowCount() == 3 && selector.getItemCount() == 3);
            selector.setSelectedItem(primera.propiedad());
            comprobar("FI03 coincidencia exacta de propiedad", tabla.getRowCount() == 2
                && tabla.getValueAt(0, 0).equals(1L) && tabla.getValueAt(1, 0).equals(3L));
            comprobar("FI04 conserva las cuotas pendientes de ajuste", tabla.getValueAt(1, 5) == null);
            ordenador.toggleSortOrder(5);
            ordenador.toggleSortOrder(5);
            comprobar("FI05 orden numérico dentro del filtro", tabla.getValueAt(0, 0).equals(1L));
            tabla.setRowSelectionInterval(0, 0);
            selector.setSelectedItem(segunda.propiedad());
            comprobar("FI06 cambia de propiedad sin operar sobre una fila oculta", tabla.getRowCount() == 1
                && tabla.getValueAt(0, 0).equals(2L) && tabla.getSelectedRow() == -1);
            modelo.actualizar(List.of(primera, segunda));
        });
        SwingUtilities.invokeAndWait(() -> {
            comprobar("FI07 conserva el filtro al actualizar", selector.getSelectedItem().equals(segunda.propiedad())
                && tabla.getRowCount() == 1 && tabla.getValueAt(0, 0).equals(2L));
            selector.setSelectedIndex(0);
            comprobar("FI08 vuelve a mostrar todas", tabla.getRowCount() == 2);
            selector.setSelectedItem(segunda.propiedad());
            modelo.actualizar(List.of(primera));
        });
        SwingUtilities.invokeAndWait(() -> {
            comprobar("FI09 propiedad retirada vuelve a todas", selector.getSelectedIndex() == 0
                && tabla.getRowCount() == 1 && tabla.getValueAt(0, 0).equals(1L));
            modelo.actualizar(List.of());
        });
        SwingUtilities.invokeAndWait(() -> comprobar("FI10 recarga vacía", tabla.getRowCount() == 0
            && selector.getItemCount() == 1));
        System.out.println("RESULTADO: " + total + " verificaciones de filtro aprobadas.");
    }
}

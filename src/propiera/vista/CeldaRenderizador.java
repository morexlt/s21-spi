package propiera.vista;

import java.awt.Color;
import java.awt.Component;
import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class CeldaRenderizador extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable tabla, Object valor,
            boolean seleccionado, boolean foco, int fila, int columna) {
        super.getTableCellRendererComponent(tabla, valor, seleccionado, foco, fila, columna);
        setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
        if (seleccionado) {
            setBackground(tabla.getSelectionBackground());
            setForeground(tabla.getSelectionForeground());
        } else {
            setBackground(fila % 2 == 0 ? Color.WHITE : EstiloVista.FONDO);
            setForeground(EstiloVista.TEXTO);
        }
        return this;
    }
}

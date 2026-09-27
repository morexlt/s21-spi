package propiera.vista;

import java.awt.Component;
import java.math.BigDecimal;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

public class ImporteRenderizador extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable tabla, Object valor,
            boolean seleccionado, boolean foco, int fila, int columna) {
        super.getTableCellRendererComponent(tabla, valor, seleccionado, foco, fila, columna);
        setHorizontalAlignment(SwingConstants.RIGHT);
        if (valor instanceof BigDecimal importe) {
            setText(importe.setScale(2).toPlainString());
        } else {
            int columnaModelo = tabla.convertColumnIndexToModel(columna);
            setText(columnaModelo == 5 ? "Pendiente de ajuste" : "—");
        }
        return this;
    }
}

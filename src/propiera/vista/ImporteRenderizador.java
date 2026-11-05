package propiera.vista;

import java.awt.Component;
import java.awt.Color;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import javax.swing.JTable;
import javax.swing.SwingConstants;

public class ImporteRenderizador extends CeldaRenderizador {
    private final NumberFormat formato = NumberFormat.getNumberInstance(Locale.forLanguageTag("es-AR"));

    public ImporteRenderizador() {
        formato.setMinimumFractionDigits(2);
        formato.setMaximumFractionDigits(2);
    }

    @Override
    public Component getTableCellRendererComponent(JTable tabla, Object valor,
            boolean seleccionado, boolean foco, int fila, int columna) {
        super.getTableCellRendererComponent(tabla, valor, seleccionado, foco, fila, columna);
        setHorizontalAlignment(SwingConstants.RIGHT);
        if (valor instanceof BigDecimal importe) {
            setText(formato.format(importe));
            if (!seleccionado && importe.signum() == 0) {
                setForeground(EstiloVista.VERDE);
            }
        } else {
            int columnaModelo = tabla.convertColumnIndexToModel(columna);
            setText(columnaModelo == 5 ? "Pendiente de ajuste" : "—");
            if (!seleccionado) {
                setForeground(new Color(139, 92, 20));
            }
        }
        return this;
    }
}

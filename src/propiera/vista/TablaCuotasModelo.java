package propiera.vista;

import java.util.List;
import java.math.BigDecimal;
import javax.swing.table.AbstractTableModel;
import propiera.modelo.Cuota;

public class TablaCuotasModelo extends AbstractTableModel {
    private final String[] columnas = {"Cuota", "Contrato", "Propiedad", "Período", "Vencimiento", "Importe ($)", "Saldo ($)"};
    private List<Cuota> cuotas = List.of();

    public void actualizar(List<Cuota> cuotas) {
        this.cuotas = List.copyOf(cuotas);
        fireTableDataChanged();
    }

    public Cuota cuota(int fila) {
        return cuotas.get(fila);
    }

    @Override
    public int getRowCount() {
        return cuotas.size();
    }

    @Override
    public int getColumnCount() {
        return columnas.length;
    }

    @Override
    public String getColumnName(int columna) {
        return columnas[columna];
    }

    @Override
    public Object getValueAt(int fila, int columna) {
        Cuota cuota = cuotas.get(fila);
        return switch (columna) {
            case 0 -> cuota.id();
            case 1 -> cuota.contrato();
            case 2 -> cuota.propiedad();
            case 3 -> cuota.periodo().toString();
            case 4 -> cuota.vencimiento().toString();
            case 5 -> cuota.importe();
            case 6 -> cuota.saldo();
            default -> throw new IndexOutOfBoundsException("Columna inválida");
        };
    }

    @Override
    public Class<?> getColumnClass(int columna) {
        if (columna < 2) {
            return Long.class;
        }
        return columna >= 5 ? BigDecimal.class : String.class;
    }
}

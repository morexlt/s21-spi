package propiera.vista;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import propiera.modelo.Cuota;

public class FormularioAjuste extends FormularioBase {
    private final JTextField cuota = new JTextField();
    private final JTextField importe = new JTextField();
    private final JLabel detalle = new JLabel("Seleccioná la primera cuota pendiente del tramo en la tabla.");
    private final JButton guardar = new JButton("Confirmar importe del tramo");

    public FormularioAjuste() {
        super("Confirmar ajuste", "El nuevo importe se aplica al tramo indicado en el contrato.");
        cuota.setEditable(false);
        agregar("Cuota inicial del tramo", cuota);
        agregar("Nuevo importe mensual ($)", importe);
        agregar("Contrato y período", detalle);
        agregar("Aplicación", new JLabel("Tramo trimestral o semestral, según el contrato."));
        terminar("Importe informado luego de consultar el ajuste pactado.", guardar);
    }

    public void seleccionarCuota(Cuota seleccionada) {
        cuota.setText(Long.toString(seleccionada.id()));
        detalle.setText("Contrato " + seleccionada.contrato() + " · " + seleccionada.propiedad()
            + " · " + seleccionada.periodo());
        importe.setText("");
    }

    public String cuota() {
        return cuota.getText();
    }

    public String importe() {
        return importe.getText().trim();
    }

    public JButton guardar() {
        return guardar;
    }

    public void limpiar() {
        cuota.setText("");
        importe.setText("");
        detalle.setText("Seleccioná la primera cuota pendiente del tramo en la tabla.");
    }
}

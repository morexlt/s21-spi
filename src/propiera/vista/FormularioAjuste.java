package propiera.vista;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import propiera.modelo.Cuota;

public class FormularioAjuste extends JPanel {
    private final JTextField cuota = new JTextField();
    private final JTextField importe = new JTextField();
    private final JLabel detalle = new JLabel("Seleccioná la primera cuota pendiente del tramo en la tabla.");
    private final JButton guardar = new JButton("Confirmar importe del tramo");

    public FormularioAjuste() {
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        cuota.setEditable(false);
        agregar(0, new JLabel("Cuota inicial del tramo"), cuota);
        agregar(1, new JLabel("Nuevo importe mensual ($)"), importe);
        agregar(2, new JLabel("Contrato y período"), detalle);
        agregar(3, new JLabel("Aplicación"), new JLabel("Tramo trimestral o semestral, según el contrato."));
        agregar(4, new JLabel("Importe informado luego de consultar el ajuste pactado."), guardar);
        GridBagConstraints espacio = new GridBagConstraints();
        espacio.gridy = 5;
        espacio.weighty = 1;
        add(new JPanel(), espacio);
    }

    private void agregar(int fila, JLabel titulo, javax.swing.JComponent campo) {
        GridBagConstraints etiqueta = new GridBagConstraints();
        etiqueta.gridx = 0;
        etiqueta.gridy = fila;
        etiqueta.anchor = GridBagConstraints.WEST;
        etiqueta.insets = new Insets(8, 0, 8, 24);
        add(titulo, etiqueta);
        GridBagConstraints valor = new GridBagConstraints();
        valor.gridx = 1;
        valor.gridy = fila;
        valor.weightx = 1;
        valor.fill = GridBagConstraints.HORIZONTAL;
        valor.insets = new Insets(8, 0, 8, 0);
        add(campo, valor);
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

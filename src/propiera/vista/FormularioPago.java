package propiera.vista;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.time.LocalDate;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class FormularioPago extends JPanel {
    private final JTextField cuota = new JTextField();
    private final JTextField fecha = new JTextField(LocalDate.now().toString());
    private final JTextField capital = new JTextField();
    private final JTextField mora = new JTextField("0.00");
    private final JComboBox<String> medio = new JComboBox<>(new String[]{"TRANSFERENCIA", "EFECTIVO"});
    private final JTextField referencia = new JTextField();
    private final JButton guardar = new JButton("Guardar pago");

    private int fila;

    public FormularioPago() {
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        agregar("Número de cuota", cuota);
        agregar("Fecha de cobro (aaaa-mm-dd)", fecha);
        agregar("Capital cobrado ($)", capital);
        agregar("Mora cobrada ($)", mora);
        agregar("Medio de pago", medio);
        agregar("Referencia única del cobro", referencia);
        agregar("Podés seleccionar la cuota desde la tabla.", guardar);
        GridBagConstraints espacio = new GridBagConstraints();
        espacio.gridy = fila;
        espacio.weighty = 1;
        add(new JPanel(), espacio);
    }

    private void agregar(String titulo, javax.swing.JComponent componente) {
        GridBagConstraints etiqueta = new GridBagConstraints();
        etiqueta.gridx = 0;
        etiqueta.gridy = fila;
        etiqueta.anchor = GridBagConstraints.WEST;
        etiqueta.insets = new Insets(8, 0, 8, 24);
        add(new JLabel(titulo), etiqueta);

        GridBagConstraints campo = new GridBagConstraints();
        campo.gridx = 1;
        campo.gridy = fila;
        campo.weightx = 1;
        campo.fill = GridBagConstraints.HORIZONTAL;
        campo.insets = new Insets(8, 0, 8, 0);
        add(componente, campo);
        fila++;
    }

    public void seleccionarCuota(long id, String saldo) {
        cuota.setText(Long.toString(id));
        capital.setText(saldo);
        mora.setText("0.00");
        referencia.setText("");
    }

    public void limpiarCobro() {
        cuota.setText("");
        capital.setText("");
        mora.setText("0.00");
        referencia.setText("");
    }

    public String cuota() {
        return cuota.getText().trim();
    }
    public String fecha() {
        return fecha.getText().trim();
    }
    public String capital() {
        return capital.getText().trim();
    }
    public String mora() {
        return mora.getText().trim();
    }
    public String medio() {
        return (String) medio.getSelectedItem();
    }
    public String referencia() {
        return referencia.getText().trim();
    }
    public JButton guardar() {
        return guardar;
    }
}

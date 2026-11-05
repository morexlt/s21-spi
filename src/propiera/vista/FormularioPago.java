package propiera.vista;

import java.time.LocalDate;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JTextField;

public class FormularioPago extends FormularioBase {
    private final JTextField cuota = new JTextField();
    private final JTextField fecha = new JTextField(LocalDate.now().toString());
    private final JTextField capital = new JTextField();
    private final JTextField mora = new JTextField("0.00");
    private final JComboBox<String> medio = new JComboBox<>(new String[]{"TRANSFERENCIA", "EFECTIVO"});
    private final JTextField referencia = new JTextField();
    private final JButton guardar = new JButton("Guardar pago");

    public FormularioPago() {
        super("Registrar pago", "Seleccioná una cuota con saldo desde Cuotas y saldos.");
        agregar("Número de cuota", cuota);
        agregar("Fecha de cobro (aaaa-mm-dd)", fecha);
        agregar("Capital cobrado ($)", capital);
        agregar("Mora cobrada ($)", mora);
        agregar("Medio de pago", medio);
        agregar("Referencia única del cobro", referencia);
        terminar("Importes sin separador de miles, con hasta dos decimales.", guardar);
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

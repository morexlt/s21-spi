package propiera.vista;

import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import propiera.modelo.Opcion;

public class FormularioContrato extends FormularioBase {
    private final JComboBox<Opcion> propiedad = new JComboBox<>();
    private final JComboBox<Opcion> inquilino = new JComboBox<>();
    private final JTextField inicio = new JTextField();
    private final JTextField fin = new JTextField();
    private final JTextField importe = new JTextField();
    private final JTextField comision = new JTextField();
    private final JComboBox<String> ajuste = new JComboBox<>(new String[]{"Trimestral", "Semestral"});
    private final JTextField vencimiento = new JTextField();
    private final JTextField mora = new JTextField();
    private final JButton guardar = new JButton("Guardar contrato y generar cuotas");

    public FormularioContrato() {
        super("Registrar contrato", "Datos de las partes, vigencia y condiciones del alquiler.");
        agregar("Propiedad y propietario", propiedad);
        agregar("Inquilino", inquilino);
        agregar("Inicio (aaaa-mm-dd)", inicio);
        agregar("Fin (aaaa-mm-dd)", fin);
        agregar("Alquiler inicial ($)", importe);
        agregar("Comisión (%)", comision);
        agregar("Ajuste", ajuste);
        agregar("Día de vencimiento (1 a 28)", vencimiento);
        agregar("Condición de mora acordada", mora);
        terminar("Vigencia por meses completos. Importes sin separador de miles.", guardar);
        limpiar();
    }

    public void cargarOpciones(List<Opcion> propiedades, List<Opcion> inquilinos) {
        actualizarOpciones(propiedad, propiedades);
        actualizarOpciones(inquilino, inquilinos);
    }

    private void actualizarOpciones(JComboBox<Opcion> selector, List<Opcion> opciones) {
        Opcion anterior = (Opcion) selector.getSelectedItem();
        selector.removeAllItems();
        opciones.forEach(selector::addItem);
        selector.setSelectedIndex(-1);
        if (anterior != null) {
            for (Opcion opcion : opciones) {
                if (opcion.id() == anterior.id()) {
                    selector.setSelectedItem(opcion);
                    break;
                }
            }
        }
    }

    public void limpiar() {
        propiedad.setSelectedIndex(-1);
        inquilino.setSelectedIndex(-1);
        ajuste.setSelectedIndex(-1);
        inicio.setText("");
        fin.setText("");
        importe.setText("");
        comision.setText("");
        vencimiento.setText("");
        mora.setText("");
    }

    public Opcion propiedad() {
        return (Opcion) propiedad.getSelectedItem();
    }
    public Opcion inquilino() {
        return (Opcion) inquilino.getSelectedItem();
    }
    public String inicio() {
        return inicio.getText().trim();
    }
    public String fin() {
        return fin.getText().trim();
    }
    public String importe() {
        return importe.getText().trim();
    }
    public String comision() {
        return comision.getText().trim();
    }
    public int ajusteMeses() {
        if (ajuste.getSelectedIndex() < 0) {
            throw new IllegalArgumentException("Seleccioná la periodicidad del ajuste");
        }
        return ajuste.getSelectedIndex() == 0 ? 3 : 6;
    }
    public String vencimiento() {
        return vencimiento.getText().trim();
    }
    public String mora() {
        return mora.getText().trim();
    }
    public JButton guardar() {
        return guardar;
    }
}

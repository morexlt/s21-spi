package propiera.vista;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import propiera.modelo.Opcion;

public class FormularioContrato extends JPanel {
    private final JComboBox<Opcion> propiedad = new JComboBox<>();
    private final JComboBox<Opcion> inquilino = new JComboBox<>();
    private final JTextField inicio = new JTextField("2026-09-01");
    private final JTextField fin = new JTextField("2027-08-31");
    private final JTextField importe = new JTextField("300000.00");
    private final JTextField comision = new JTextField("5.00");
    private final JComboBox<String> ajuste = new JComboBox<>(new String[]{"Trimestral", "Semestral"});
    private final JTextField vencimiento = new JTextField("10");
    private final JTextField mora = new JTextField();
    private final JButton guardar = new JButton("Guardar contrato y generar cuotas");

    private int fila;
    private boolean opcionesCargadas;

    public FormularioContrato() {
        setLayout(new GridBagLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        agregar("Propiedad y propietario", propiedad);
        agregar("Inquilino", inquilino);
        agregar("Inicio (aaaa-mm-dd)", inicio);
        agregar("Fin (aaaa-mm-dd)", fin);
        agregar("Alquiler inicial ($)", importe);
        agregar("Comisión (%)", comision);
        agregar("Ajuste", ajuste);
        agregar("Día de vencimiento (1 a 28)", vencimiento);
        agregar("Condición de mora acordada", mora);
        agregar("Vigencia por meses completos. Importes sin separador de miles.", guardar);
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

    public void cargarOpciones(List<Opcion> propiedades, List<Opcion> inquilinos) {
        actualizarOpciones(propiedad, propiedades);
        actualizarOpciones(inquilino, inquilinos);
        opcionesCargadas = true;
    }

    private void actualizarOpciones(JComboBox<Opcion> selector, List<Opcion> opciones) {
        Opcion anterior = (Opcion) selector.getSelectedItem();
        selector.removeAllItems();
        opciones.forEach(selector::addItem);
        if (opcionesCargadas) {
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

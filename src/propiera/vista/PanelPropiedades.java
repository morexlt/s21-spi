package propiera.vista;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import propiera.modelo.Persona;
import propiera.modelo.Propiedad;

public class PanelPropiedades extends PanelAbm {
    private final JTextField codigo = new JTextField();
    private final JTextField direccion = new JTextField();
    private final JComboBox<Persona> propietario = new JComboBox<>();
    private final ArrayList<Propiedad> propiedades = new ArrayList<>();

    public PanelPropiedades() {
        super("Propiedades", new String[]{"ID", "Código", "Dirección", "Propietario", "Activo"});
        agregarCampo("Código", codigo);
        agregarCampo("Dirección", direccion);
        agregarCampo("Propietario", propietario);
        agregarCampo("Estado", activo);
        tabla.getSelectionModel().addListSelectionListener(evento -> {
            if (!evento.getValueIsAdjusting() && tabla.getSelectedRow() >= 0) {
                Propiedad propiedad = propiedades.get(tabla.convertRowIndexToModel(tabla.getSelectedRow()));
                seleccion(propiedad.id(), propiedad.activo());
                codigo.setText(propiedad.codigo());
                direccion.setText(propiedad.direccion());
                propietario.setSelectedIndex(-1);
                for (int i = 0; i < propietario.getItemCount(); i++) {
                    if (propietario.getItemAt(i).id() == propiedad.propietario()) propietario.setSelectedIndex(i);
                }
            }
        });
        limpiar();
    }

    public void cargar(List<Propiedad> lista, List<Persona> duenos) {
        tabla.clearSelection();
        propiedades.clear();
        propiedades.addAll(lista);
        propietario.removeAllItems();
        duenos.forEach(propietario::addItem);
        cargarFilas(propiedades.stream().map(propiedad -> new Object[]{propiedad.id(), propiedad.codigo(),
            propiedad.direccion(), propiedad.nombrePropietario(), propiedad.activo()}).toArray(Object[][]::new));
        limpiar();
    }

    public Propiedad datos() {
        Persona dueno = (Persona) propietario.getSelectedItem();
        return new Propiedad(id, codigo.getText(), direccion.getText(),
            dueno == null ? 0 : dueno.id(), "", activo.isSelected());
    }

    @Override
    public void limpiar() {
        tabla.clearSelection();
        seleccion(0, true);
        codigo.setText("");
        direccion.setText("");
        propietario.setSelectedIndex(-1);
    }
}

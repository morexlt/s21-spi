package propiera.vista;

import java.util.ArrayList;
import java.util.List;
import javax.swing.JTextField;
import propiera.modelo.Persona;

public class PanelPersonas extends PanelAbm {
    private final JTextField documento = new JTextField();
    private final JTextField nombre = new JTextField();
    private final JTextField contacto = new JTextField();
    private final ArrayList<Persona> personas = new ArrayList<>();

    public PanelPersonas(String titulo) {
        super(titulo, new String[]{"ID", "Documento", "Nombre", "Contacto", "Activo"});
        agregarCampo("Documento", documento);
        agregarCampo("Nombre", nombre);
        agregarCampo("Contacto (opcional)", contacto);
        agregarCampo("Estado", activo);
        tabla.getSelectionModel().addListSelectionListener(evento -> {
            if (!evento.getValueIsAdjusting() && tabla.getSelectedRow() >= 0) {
                Persona persona = personas.get(tabla.convertRowIndexToModel(tabla.getSelectedRow()));
                seleccion(persona.id(), persona.activo());
                documento.setText(persona.documento());
                nombre.setText(persona.nombre());
                contacto.setText(persona.contacto());
            }
        });
        limpiar();
    }

    public void cargar(List<Persona> lista) {
        tabla.clearSelection();
        personas.clear();
        personas.addAll(lista);
        cargarFilas(personas.stream().map(persona -> new Object[]{persona.id(), persona.documento(),
            persona.nombre(), persona.contacto(), persona.activo()}).toArray(Object[][]::new));
        limpiar();
    }

    public Persona datos() {
        return new Persona(id, documento.getText(), nombre.getText(), contacto.getText(), activo.isSelected());
    }

    @Override
    public void limpiar() {
        tabla.clearSelection();
        seleccion(0, true);
        documento.setText("");
        nombre.setText("");
        contacto.setText("");
    }
}

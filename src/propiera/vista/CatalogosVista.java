package propiera.vista;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import propiera.modelo.DatosCatalogo;

public class CatalogosVista extends JPanel {
    private final PanelPersonas propietarios = new PanelPersonas("Propietarios");
    private final PanelPropiedades propiedades = new PanelPropiedades();
    private final PanelPersonas inquilinos = new PanelPersonas("Inquilinos");

    public CatalogosVista() {
        super(new BorderLayout());
        JTabbedPane pestanas = new JTabbedPane();
        pestanas.addTab("Propietarios", propietarios);
        pestanas.addTab("Propiedades", propiedades);
        pestanas.addTab("Inquilinos", inquilinos);
        add(pestanas, BorderLayout.CENTER);
    }

    public void cargar(DatosCatalogo datos) {
        propietarios.cargar(datos.propietarios());
        propiedades.cargar(datos.propiedades(), datos.propietarios());
        inquilinos.cargar(datos.inquilinos());
    }

    public PanelPersonas propietarios() { return propietarios; }
    public PanelPropiedades propiedades() { return propiedades; }
    public PanelPersonas inquilinos() { return inquilinos; }

    public void ocupado(boolean ocupado) {
        propietarios.ocupado(ocupado);
        propiedades.ocupado(ocupado);
        inquilinos.ocupado(ocupado);
    }
}

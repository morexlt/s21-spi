package propiera.vista;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class FormularioBase extends JPanel {
    private int fila;

    protected FormularioBase(String titulo, String descripcion) {
        setLayout(new GridBagLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(EstiloVista.BORDE),
            BorderFactory.createEmptyBorder(24, 28, 24, 28)));
        agregarAnchoCompleto(EstiloVista.titulo(titulo), 0);
        agregarAnchoCompleto(EstiloVista.ayuda(descripcion), 20);
    }

    protected void agregar(String texto, JComponent componente) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setFont(etiqueta.getFont().deriveFont(Font.PLAIN, 13f));
        etiqueta.setForeground(EstiloVista.TEXTO);
        etiqueta.setLabelFor(componente);
        GridBagConstraints posicion = new GridBagConstraints();
        posicion.gridx = 0;
        posicion.gridy = fila;
        posicion.anchor = GridBagConstraints.WEST;
        posicion.insets = new Insets(4, 0, 4, 24);
        add(etiqueta, posicion);

        componente.setFont(componente.getFont().deriveFont(Font.PLAIN, 14f));
        componente.setPreferredSize(new Dimension(460, 28));
        posicion.gridx = 1;
        posicion.weightx = 1;
        posicion.fill = GridBagConstraints.HORIZONTAL;
        posicion.insets = new Insets(4, 0, 4, 0);
        add(componente, posicion);
        fila++;
    }

    protected void terminar(String ayuda, JButton guardar) {
        agregarAnchoCompleto(EstiloVista.ayuda(ayuda), 12);
        EstiloVista.botonPrincipal(guardar);
        GridBagConstraints posicion = new GridBagConstraints();
        posicion.gridx = 1;
        posicion.gridy = fila++;
        posicion.anchor = GridBagConstraints.EAST;
        posicion.insets = new Insets(10, 0, 0, 0);
        add(guardar, posicion);
    }

    private void agregarAnchoCompleto(JComponent componente, int espacioInferior) {
        GridBagConstraints posicion = new GridBagConstraints();
        posicion.gridx = 0;
        posicion.gridy = fila++;
        posicion.gridwidth = 2;
        posicion.anchor = GridBagConstraints.WEST;
        posicion.insets = new Insets(0, 0, espacioInferior, 0);
        add(componente, posicion);
    }
}

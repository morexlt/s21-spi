package propiera.vista;

import java.awt.Color;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;

public final class EstiloVista {
    public static final Color FONDO = new Color(243, 246, 247);
    public static final Color TEXTO = new Color(35, 53, 61);
    public static final Color SECUNDARIO = new Color(91, 108, 116);
    public static final Color VERDE = new Color(16, 111, 100);
    public static final Color BORDE = new Color(218, 227, 230);

    private EstiloVista() {
    }

    public static JLabel titulo(String texto) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setFont(etiqueta.getFont().deriveFont(Font.BOLD, 23f));
        etiqueta.setForeground(TEXTO);
        return etiqueta;
    }

    public static JLabel ayuda(String texto) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setFont(etiqueta.getFont().deriveFont(Font.PLAIN, 13f));
        etiqueta.setForeground(SECUNDARIO);
        return etiqueta;
    }

    public static void botonPrincipal(JButton boton) {
        boton.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        boton.setOpaque(true);
        boton.setBackground(VERDE);
        boton.setForeground(Color.WHITE);
        boton.setFont(boton.getFont().deriveFont(Font.BOLD, 13f));
        boton.setBorder(BorderFactory.createEmptyBorder(11, 18, 11, 18));
        boton.setFocusPainted(false);
        boton.addPropertyChangeListener("enabled", evento -> {
            boton.setBackground(boton.isEnabled() ? VERDE : BORDE);
            boton.setForeground(boton.isEnabled() ? Color.WHITE : SECUNDARIO);
        });
    }
}

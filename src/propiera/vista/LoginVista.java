package propiera.vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class LoginVista extends JFrame {
    private final JTextField usuario = new JTextField("administrativo");
    private final JPasswordField clave = new JPasswordField();
    private final JButton ingresar = new JButton("Ingresar");

    public LoginVista() {
        super("Propiera · Iniciar sesión");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel campos = new JPanel(new GridBagLayout());
        campos.setBackground(Color.WHITE);
        campos.setBorder(BorderFactory.createEmptyBorder(28, 36, 28, 36));
        GridBagConstraints posicion = new GridBagConstraints();
        posicion.gridx = 0;
        posicion.weightx = 1;
        posicion.fill = GridBagConstraints.HORIZONTAL;
        posicion.anchor = GridBagConstraints.WEST;
        posicion.insets = new Insets(0, 0, 12, 0);
        campos.add(EstiloVista.titulo("Propiera"), posicion);
        posicion.gridy = 1;
        posicion.insets = new Insets(0, 0, 28, 0);
        campos.add(EstiloVista.ayuda("Gestión de alquileres · Acceso interno"), posicion);
        posicion.gridy = 2;
        posicion.insets = new Insets(0, 0, 6, 0);
        JLabel etiquetaUsuario = EstiloVista.ayuda("Usuario");
        etiquetaUsuario.setLabelFor(usuario);
        campos.add(etiquetaUsuario, posicion);
        posicion.gridy = 3;
        posicion.insets = new Insets(0, 0, 18, 0);
        usuario.setPreferredSize(new Dimension(320, 36));
        usuario.setFont(usuario.getFont().deriveFont(Font.PLAIN, 14f));
        campos.add(usuario, posicion);
        posicion.gridy = 4;
        posicion.insets = new Insets(0, 0, 6, 0);
        JLabel etiquetaClave = EstiloVista.ayuda("Contraseña");
        etiquetaClave.setLabelFor(clave);
        campos.add(etiquetaClave, posicion);
        posicion.gridy = 5;
        posicion.insets = new Insets(0, 0, 24, 0);
        clave.setPreferredSize(new Dimension(320, 36));
        campos.add(clave, posicion);
        posicion.gridy = 6;
        posicion.insets = new Insets(0, 0, 0, 0);
        EstiloVista.botonPrincipal(ingresar);
        campos.add(ingresar, posicion);
        add(campos, BorderLayout.CENTER);
        getRootPane().setDefaultButton(ingresar);
        setSize(440, 420);
        setResizable(false);
        setLocationRelativeTo(null);
    }

    public String usuario() {
        return usuario.getText().trim();
    }
    public char[] clave() {
        return clave.getPassword();
    }
    public JButton ingresar() {
        return ingresar;
    }
    public void limpiarClave() {
        clave.setText("");
    }
}

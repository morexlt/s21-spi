package propiera.vista;

import java.awt.BorderLayout;
import java.awt.GridLayout;
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
        JPanel campos = new JPanel(new GridLayout(0, 1, 8, 8));
        campos.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        campos.add(new JLabel("Propiera · Gestión de alquileres"));
        campos.add(new JLabel("Usuario"));
        campos.add(usuario);
        campos.add(new JLabel("Contraseña"));
        campos.add(clave);
        campos.add(ingresar);
        add(campos, BorderLayout.CENTER);
        getRootPane().setDefaultButton(ingresar);
        setSize(420, 340);
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

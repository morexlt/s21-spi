package propiera;

import propiera.persistencia.BaseDatos;
import propiera.servicio.AlquilerService;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import propiera.controlador.AlquilerControlador;
import propiera.servicio.AutenticacionService;
import propiera.vista.LoginVista;

public class PropieraApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception error) {
                System.err.println("Se utiliza el aspecto predeterminado de Swing.");
            }

            BaseDatos baseDatos = new BaseDatos();
            AlquilerService servicio = new AlquilerService(baseDatos);
            AutenticacionService autenticacion = new AutenticacionService(baseDatos);
            LoginVista login = new LoginVista();
            AlquilerControlador controlador = new AlquilerControlador(servicio, autenticacion, login);
            controlador.iniciar();
        });
    }
}

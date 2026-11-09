package propiera;

import propiera.persistencia.BaseDatos;
import propiera.servicio.AlquilerService;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import propiera.controlador.AlquilerControlador;
import propiera.servicio.AutenticacionService;
import propiera.servicio.CatalogoService;
import propiera.vista.LoginVista;
import propiera.configuracion.Configuracion;
import javax.swing.JOptionPane;

public class PropieraApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                Configuracion.validar();
            } catch (IllegalStateException error) {
                JOptionPane.showMessageDialog(null, error.getMessage()
                    + "\nRevisá el archivo .env en la carpeta del proyecto.",
                    "Configuración de Propiera", JOptionPane.ERROR_MESSAGE);
                return;
            }
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception error) {
                System.err.println("Se utiliza el aspecto predeterminado de Swing.");
            }

            BaseDatos baseDatos = new BaseDatos();
            AlquilerService servicio = new AlquilerService(baseDatos);
            AutenticacionService autenticacion = new AutenticacionService(baseDatos);
            LoginVista login = new LoginVista();
            AlquilerControlador controlador = new AlquilerControlador(servicio, autenticacion, login,
                new CatalogoService(baseDatos));
            controlador.iniciar();
        });
    }
}

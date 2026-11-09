package propiera;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;
import propiera.modelo.CuotaContrato;
import propiera.modelo.EstadoContrato;
import propiera.persistencia.ArchivoCuotas;
import propiera.persistencia.BaseDatos;
import propiera.servicio.AlquilerService;

/** Ejecutar con los scripts 01, 02 y 05 en una base exclusiva de pruebas. */
public class PruebasExportacion {
    private static int total;
    private static final BaseDatos base = new BaseDatos();
    private static final AlquilerService servicio = new AlquilerService(base);

    interface Accion { void ejecutar() throws Exception; }

    private static void comprobar(String caso, boolean resultado) {
        if (!resultado) throw new AssertionError(caso);
        total++;
        System.out.println("PASS " + caso);
    }

    private static void rechazar(String caso, String mensaje, Accion accion) throws Exception {
        try { accion.ejecutar(); }
        catch (IllegalArgumentException error) {
            comprobar(caso, mensaje.equals(error.getMessage()));
            return;
        }
        throw new AssertionError(caso + " debía rechazarse");
    }

    private static void sql(String instruccion) throws Exception {
        try (Connection conexion = base.abrir()) {
            BaseDatos.insertarSinClave(conexion, instruccion);
        }
    }

    public static void main(String[] args) throws Exception {
        EstadoContrato estado = servicio.consultarContrato(1, 1);
        comprobar("EX01 contrato completo ordenado", estado.cuotas().size() == 12
            && estado.cuotas().get(0).periodo().equals(LocalDate.of(2026, 1, 1))
            && estado.cuotas().get(11).periodo().equals(LocalDate.of(2026, 12, 1)));
        comprobar("EX02 identidad del contrato", estado.propiedad().equals("P-001 · Roca 425")
            && estado.propietario().equals("Ana Muñoz") && estado.inquilino().equals("Lucía Núñez"));
        comprobar("EX03 cobro parcial y saldo persistidos", estado.cuotas().get(0).capitalCobrado().compareTo(new BigDecimal("100000")) == 0
            && estado.cuotas().get(0).saldo().compareTo(new BigDecimal("200000")) == 0);
        comprobar("EX04 meses sin confirmar siguen nulos", estado.cuotas().get(6).importe() == null
            && estado.cuotas().get(6).saldo() == null);
        comprobar("EX05 Martillero puede exportar", servicio.consultarContrato(1, 2).equals(estado));
        rechazar("EX06 usuario inexistente", "El usuario no puede consultar contratos", () -> servicio.consultarContrato(1, Long.MAX_VALUE));
        rechazar("EX07 contrato inexistente", "El contrato no existe", () -> servicio.consultarContrato(Long.MAX_VALUE, 1));
        rechazar("EX08 contrato sin seleccionar", "Seleccioná una cuota del contrato que querés exportar", () -> servicio.consultarContrato(0, 1));
        sql("UPDATE usuario SET activo=FALSE WHERE id=2");
        try {
            rechazar("EX09 usuario inactivo", "El usuario no puede consultar contratos", () -> servicio.consultarContrato(1, 2));
        } finally { sql("UPDATE usuario SET activo=TRUE WHERE id=2"); }

        Path carpeta = Files.createTempDirectory("propiera-exportacion-");
        ArchivoCuotas exportador = new ArchivoCuotas();
        Path archivo = carpeta.resolve("cuotas de Muñoz.txt");
        exportador.guardar(archivo, estado, false);
        String texto = Files.readString(archivo, StandardCharsets.UTF_8);
        comprobar("EX10 escritura y lectura UTF-8", texto.contains("Muñoz") && texto.contains("Núñez")
            && texto.contains("Período") && !texto.contains("\uFFFD"));
        comprobar("EX11 todas las cuotas y vencimientos", texto.lines().filter(l -> l.matches("[0-9]{2}/2026 \\|.*")).count() == 12
            && texto.contains("01/2026 | 10/01/2026"));
        comprobar("EX12 totales confirmados y cobros", texto.contains("Capital confirmado: $1.980.000,00")
            && texto.contains("Capital cobrado: $100.000,00")
            && texto.contains("Saldo de capital de cuotas confirmadas: $1.880.000,00"));
        comprobar("EX13 pendientes identificados sin saldo ficticio", texto.contains("Cuotas pendientes de ajuste: 6")
            && texto.contains("07/2026 | 10/07/2026 | Pendiente de ajuste | $0,00 | $0,00 | Pendiente de ajuste")
            && texto.contains("no están incluidas en el saldo"));
        comprobar("EX14 sólo el contrato elegido", !texto.contains("P-002") && !texto.contains("P-003"));
        try {
            exportador.guardar(archivo, servicio.consultarContrato(2, 1), false);
            throw new AssertionError("Se reemplazó un archivo sin permiso");
        } catch (FileAlreadyExistsException esperado) {
            comprobar("EX15 no reemplazar sin confirmación", Files.readString(archivo, StandardCharsets.UTF_8).equals(texto));
        }
        exportador.guardar(archivo, servicio.consultarContrato(2, 1), true);
        String segundo = Files.readString(archivo, StandardCharsets.UTF_8);
        comprobar("EX16 reemplazo confirmado", segundo.contains("Contrato: 2") && segundo.contains("Martín Aguirre") && !segundo.equals(texto));
        comprobar("EX17 mora separada del capital", segundo.contains("Mora cobrada: $1.500,00")
            && segundo.contains("Capital cobrado: $450.000,00")
            && segundo.contains("Saldo de capital de cuotas confirmadas: $2.250.000,00"));
        comprobar("EX18 cuotas pagadas y pendientes", segundo.contains("Cuotas con capital cancelado: 1")
            && segundo.contains("Cuotas con saldo: 5") && segundo.contains("Cuotas pendientes de ajuste: 6"));
        Path sinCarpeta = carpeta.resolve("no-existe/cuotas.txt");
        try {
            exportador.guardar(sinCarpeta, estado, false);
            throw new AssertionError("Se aceptó carpeta inexistente");
        } catch (IOException esperado) {
            comprobar("EX19 error de escritura no altera archivo previo", !Files.exists(sinCarpeta)
                && Files.readString(archivo, StandardCharsets.UTF_8).equals(segundo));
        }
        Path directorio = Files.createDirectory(carpeta.resolve("carpeta.txt"));
        try {
            exportador.guardar(directorio, estado, true);
            throw new AssertionError("Se reemplazó una carpeta");
        } catch (IOException esperado) {
            comprobar("EX20 no reemplazar carpetas", Files.isDirectory(directorio));
        }
        try (var archivos = Files.list(carpeta)) {
            comprobar("EX21 temporales eliminados", archivos.noneMatch(p -> p.getFileName().toString().endsWith(".tmp")));
        }
        sql("UPDATE propiedad SET activo=FALSE WHERE id=1");
        sql("UPDATE propietario SET activo=FALSE WHERE id=1");
        sql("UPDATE inquilino SET activo=FALSE WHERE id=1");
        try {
            comprobar("EX22 baja lógica conserva exportación histórica", servicio.consultarContrato(1, 1).equals(estado));
        } finally {
            sql("UPDATE propiedad SET activo=TRUE WHERE id=1");
            sql("UPDATE propietario SET activo=TRUE WHERE id=1");
            sql("UPDATE inquilino SET activo=TRUE WHERE id=1");
        }
        BigDecimal uno = new BigDecimal("10.00");
        EstadoContrato muestra = new EstadoContrato(99, "Casa\nCentro", "Álvarez", "Núñez", LocalDate.of(2026,1,1),
            LocalDate.of(2026,1,31), List.of(new CuotaContrato(LocalDate.of(2026,1,1),LocalDate.of(2026,1,10),uno,uno,BigDecimal.ZERO,BigDecimal.ZERO)));
        Path limpio = carpeta.resolve("una-cuota.txt");
        exportador.guardar(limpio, muestra, false);
        comprobar("EX23 encabezados de una línea", Files.readString(limpio, StandardCharsets.UTF_8).contains("Propiedad: Casa Centro"));
        System.out.println("Archivos de prueba: " + carpeta);
        System.out.println("RESULTADO: " + total + " verificaciones de exportación aprobadas.");
    }
}

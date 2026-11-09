package propiera.configuracion;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class PruebasConfiguracion {
    private static int total;

    private static void comprobar(String caso, boolean correcto) {
        if (!correcto) throw new AssertionError(caso);
        total++;
        System.out.println("PASS " + caso);
    }

    private static void rechazar(Path archivo, String texto, int linea, String caso) throws Exception {
        Files.writeString(archivo, texto, StandardCharsets.UTF_8);
        try {
            Configuracion.leerArchivo(archivo);
            throw new AssertionError(caso + " debía rechazarse");
        } catch (IllegalStateException error) {
            comprobar(caso, error.getMessage().equals("Formato inválido en .env (línea " + linea + ")")
                && !error.getMessage().contains("clave_secreta"));
        }
    }

    private static String consultarEnOtroProceso(Path carpeta, String valorEntorno) throws Exception {
        String java = Path.of(System.getProperty("java.home"), "bin", "java").toString();
        String clases = Path.of(PruebasConfiguracion.class.getProtectionDomain().getCodeSource().getLocation().toURI()).toString();
        ProcessBuilder proceso = new ProcessBuilder(java, "-cp", clases,
            PruebasConfiguracion.class.getName(), "--consultar");
        proceso.directory(carpeta.toFile());
        proceso.environment().remove("PROPIERA_CONFIG_PRUEBA");
        if (valorEntorno != null) proceso.environment().put("PROPIERA_CONFIG_PRUEBA", valorEntorno);
        Process ejecucion = proceso.start();
        String salida = new String(ejecucion.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();
        if (ejecucion.waitFor() != 0) throw new AssertionError("Falló la consulta de configuración");
        return salida;
    }

    public static void main(String[] args) throws Exception {
        if (args.length > 0 && args[0].equals("--consultar")) {
            try { System.out.print(Configuracion.obligatorio("PROPIERA_CONFIG_PRUEBA")); }
            catch (IllegalStateException error) { System.out.print(error.getMessage()); }
            return;
        }
        Path carpeta = Files.createTempDirectory("propiera-configuracion-");
        Path archivo = carpeta.resolve("datos.env");
        comprobar("CF01 archivo ausente permite entorno", Configuracion.leerArchivo(archivo).isEmpty());
        Files.writeString(archivo, "\uFEFF# Comentario\n\n NOMBRE = Muñoz \nURL=jdbc:mysql://localhost/base?sslMode=REQUIRED&x=1\n"
            + "CLAVE='con # y = adentro'\nOTRA=\" con espacios \"\nVACIA=\nexport PUERTO=33316\nLITERAL=$(sin_ejecutar)\n", StandardCharsets.UTF_8);
        Map<String, String> valores = Configuracion.leerArchivo(archivo);
        comprobar("CF02 UTF-8, BOM, comentarios y espacios", valores.size() == 7 && valores.get("NOMBRE").equals("Muñoz"));
        comprobar("CF03 conserva parámetros de URL", valores.get("URL").endsWith("sslMode=REQUIRED&x=1"));
        comprobar("CF04 comillas y símbolos literales", valores.get("CLAVE").equals("con # y = adentro")
            && valores.get("OTRA").equals(" con espacios "));
        comprobar("CF05 valores vacíos y prefijo export", valores.get("VACIA").isEmpty() && valores.get("PUERTO").equals("33316"));
        comprobar("CF06 no ejecuta contenido del archivo", valores.get("LITERAL").equals("$(sin_ejecutar)"));
        rechazar(archivo, "clave_secreta\n", 1, "CF07 línea sin asignación");
        rechazar(archivo, "MAL-NOMBRE=clave_secreta\n", 1, "CF08 nombre inválido");
        rechazar(archivo, "CLAVE='clave_secreta\n", 1, "CF09 comillas incompletas");
        rechazar(archivo, "CLAVE=una\nCLAVE=clave_secreta\n", 2, "CF10 variables duplicadas");
        Files.writeString(carpeta.resolve(".env"), "PROPIERA_CONFIG_PRUEBA=desde_archivo\n", StandardCharsets.UTF_8);
        comprobar("CF11 lectura desde directorio del proyecto", consultarEnOtroProceso(carpeta, null).equals("desde_archivo"));
        comprobar("CF12 entorno explícito tiene prioridad", consultarEnOtroProceso(carpeta, "desde_entorno").equals("desde_entorno"));
        comprobar("CF13 vacío explícito no usa clave anterior", consultarEnOtroProceso(carpeta, "").equals("Falta configurar PROPIERA_CONFIG_PRUEBA"));
        Files.delete(carpeta.resolve(".env"));
        comprobar("CF14 error claro si falta configuración", consultarEnOtroProceso(carpeta, null).equals("Falta configurar PROPIERA_CONFIG_PRUEBA"));
        System.out.println("RESULTADO: " + total + " verificaciones de configuración aprobadas.");
    }
}

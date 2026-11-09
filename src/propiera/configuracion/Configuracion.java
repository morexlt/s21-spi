package propiera.configuracion;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public final class Configuracion {
    private static Map<String, String> valoresArchivo;

    private Configuracion() { }

    public static String valor(String nombre, String predeterminado) {
        String entorno = System.getenv(nombre);
        return entorno != null ? entorno : archivo().getOrDefault(nombre, predeterminado);
    }

    public static String obligatorio(String nombre) {
        String valor = valor(nombre, null);
        if (valor == null || valor.isBlank()) {
            throw new IllegalStateException("Falta configurar " + nombre);
        }
        return valor;
    }

    public static void validar() {
        for (String nombre : new String[]{"PROPIERA_DB_USER", "PROPIERA_DB_PASSWORD",
                "PROPIERA_ADMIN_PASSWORD", "PROPIERA_MARTILLERO_PASSWORD"}) {
            obligatorio(nombre);
        }
    }

    private static synchronized Map<String, String> archivo() {
        if (valoresArchivo == null) valoresArchivo = leerArchivo(Path.of(".env"));
        return valoresArchivo;
    }

    static Map<String, String> leerArchivo(Path ruta) {
        if (Files.notExists(ruta)) return Map.of();
        Map<String, String> valores = new HashMap<>();
        try (BufferedReader lector = Files.newBufferedReader(ruta, StandardCharsets.UTF_8)) {
            String linea;
            int numero = 0;
            while ((linea = lector.readLine()) != null) {
                numero++;
                if (numero == 1 && linea.startsWith("\uFEFF")) linea = linea.substring(1);
                linea = linea.trim();
                if (linea.isEmpty() || linea.startsWith("#")) continue;
                if (linea.startsWith("export ")) linea = linea.substring(7).trim();
                int separador = linea.indexOf('=');
                if (separador <= 0) throw formato(numero);
                String nombre = linea.substring(0, separador).trim();
                String valor = linea.substring(separador + 1).trim();
                if (!nombre.matches("[A-Za-z_][A-Za-z0-9_]*")) throw formato(numero);
                if (valor.startsWith("\"") || valor.startsWith("'")) {
                    if (valor.length() < 2 || valor.charAt(valor.length() - 1) != valor.charAt(0)) throw formato(numero);
                    valor = valor.substring(1, valor.length() - 1);
                }
                if (valores.putIfAbsent(nombre, valor) != null) throw formato(numero);
            }
            return Map.copyOf(valores);
        } catch (IOException error) {
            throw new IllegalStateException("No se pudo leer el archivo .env", error);
        }
    }

    private static IllegalStateException formato(int linea) {
        return new IllegalStateException("Formato inválido en .env (línea " + linea + ")");
    }
}

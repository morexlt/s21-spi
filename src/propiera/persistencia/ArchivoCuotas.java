package propiera.persistencia;

import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import propiera.modelo.CuotaContrato;
import propiera.modelo.EstadoContrato;

public class ArchivoCuotas {
    public void guardar(Path destino, EstadoContrato contrato, boolean reemplazar) throws IOException {
        Path archivo = destino.toAbsolutePath();
        if (Files.isDirectory(archivo)) throw new IOException("El destino es una carpeta");
        Path temporal = Files.createTempFile(archivo.getParent(), ".propiera-cuotas-", ".tmp");
        try {
            try (BufferedWriter salida = Files.newBufferedWriter(temporal, StandardCharsets.UTF_8)) {
                escribir(salida, contrato);
            }
            if (reemplazar) {
                Files.move(temporal, archivo, StandardCopyOption.REPLACE_EXISTING);
            } else {
                Files.move(temporal, archivo);
            }
        } finally {
            Files.deleteIfExists(temporal);
        }
    }

    private void escribir(BufferedWriter salida, EstadoContrato contrato) throws IOException {
        DateTimeFormatter fecha = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter periodo = DateTimeFormatter.ofPattern("MM/yyyy");
        DecimalFormat pesos = new DecimalFormat("#,##0.00",
            DecimalFormatSymbols.getInstance(Locale.forLanguageTag("es-AR")));
        linea(salida, "PROPIERA - ESTADO DE CUOTAS");
        linea(salida, "Contrato: " + contrato.id());
        linea(salida, "Propiedad: " + unaLinea(contrato.propiedad()));
        linea(salida, "Propietario: " + unaLinea(contrato.propietario()));
        linea(salida, "Inquilino: " + unaLinea(contrato.inquilino()));
        linea(salida, "Vigencia: " + fecha.format(contrato.inicio()) + " al " + fecha.format(contrato.fin()));
        linea(salida, "Generado: " + DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").format(LocalDateTime.now()));
        linea(salida, "Importes en pesos argentinos.");
        linea(salida, "");
        linea(salida, "Período | Vencimiento | Importe | Capital cobrado | Mora cobrada | Saldo de capital");
        BigDecimal capital = BigDecimal.ZERO;
        BigDecimal cobrado = BigDecimal.ZERO;
        BigDecimal mora = BigDecimal.ZERO;
        BigDecimal saldo = BigDecimal.ZERO;
        int pendientes = 0;
        int canceladas = 0;
        int conSaldo = 0;
        for (CuotaContrato cuota : contrato.cuotas()) {
            linea(salida, periodo.format(cuota.periodo()) + " | " + fecha.format(cuota.vencimiento())
                + " | " + importe(cuota.importe(), pesos) + " | " + importe(cuota.capitalCobrado(), pesos)
                + " | " + importe(cuota.moraCobrada(), pesos) + " | " + importe(cuota.saldo(), pesos));
            cobrado = cobrado.add(cuota.capitalCobrado());
            mora = mora.add(cuota.moraCobrada());
            if (cuota.importe() == null) {
                pendientes++;
            } else {
                capital = capital.add(cuota.importe());
                saldo = saldo.add(cuota.saldo());
                if (cuota.saldo().signum() == 0) canceladas++;
                else conSaldo++;
            }
        }
        linea(salida, "");
        linea(salida, "Cuotas del contrato: " + contrato.cuotas().size());
        linea(salida, "Cuotas con importe confirmado: " + (contrato.cuotas().size() - pendientes));
        linea(salida, "Cuotas con capital cancelado: " + canceladas);
        linea(salida, "Cuotas con saldo: " + conSaldo);
        linea(salida, "Cuotas pendientes de ajuste: " + pendientes);
        linea(salida, "Capital confirmado: " + importe(capital, pesos));
        linea(salida, "Capital cobrado: " + importe(cobrado, pesos));
        linea(salida, "Mora cobrada: " + importe(mora, pesos));
        linea(salida, "Saldo de capital de cuotas confirmadas: " + importe(saldo, pesos));
        linea(salida, "");
        linea(salida, "Las cuotas pendientes de ajuste no están incluidas en el saldo: todavía no tienen importe confirmado.");
        linea(salida, "La mora cobrada se informa por separado y no reduce el saldo de capital.");
    }

    private String importe(BigDecimal valor, DecimalFormat formato) {
        return valor == null ? "Pendiente de ajuste" : "$" + formato.format(valor);
    }

    private String unaLinea(String texto) {
        return texto.replace('\r', ' ').replace('\n', ' ').replace('\t', ' ');
    }

    private void linea(BufferedWriter salida, String texto) throws IOException {
        salida.write(texto);
        salida.newLine();
    }
}

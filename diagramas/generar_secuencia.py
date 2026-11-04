from pathlib import Path
from PIL import Image, ImageDraw, ImageFont


DESTINO = Path(__file__).with_name("05_secuencia.png")
ANCHO, ALTO = 3660, 2050
AZUL = "#173B3A"
VERDE = "#087F73"
FONDO = "#EAF5F3"


def fuente(tamano, negrita=False):
    nombre = "Arial Bold.ttf" if negrita else "Arial.ttf"
    rutas = [
        Path("/System/Library/Fonts/Supplemental") / nombre,
        Path("/Library/Fonts") / nombre,
    ]
    for ruta in rutas:
        if ruta.exists():
            return ImageFont.truetype(str(ruta), tamano)
    return ImageFont.load_default()


normal = fuente(29)
negrita = fuente(32, True)

clases = Image.new("RGB", (1800, 1050), "white")
d = ImageDraw.Draw(clases)


def caja(x1, y1, x2, y2, titulo, detalles):
    d.rounded_rectangle((x1, y1, x2, y2), 14, fill=FONDO, outline=VERDE, width=5)
    d.text(((x1 + x2) / 2, y1 + 50), titulo, font=negrita, fill=AZUL, anchor="mm")
    d.line((x1, y1 + 95, x2, y1 + 95), fill=VERDE, width=3)
    for i, linea in enumerate(detalles):
        d.text((x1 + 25, y1 + 135 + i * 52), linea, font=normal, fill=AZUL)


caja(60, 140, 600, 400, "AlquilerService", ["- operaciones: OperacionDao", "- generarCuotas(...): void" ])
caja(880, 70, 1730, 400, "«abstract» PeriodicidadAjuste", [
    "+ mesesDelPrimerTramo(): int", "+ importeInicial(mes, importe): BigDecimal",
    "+ crear(meses): PeriodicidadAjuste {static}",])
caja(655, 680, 1170, 950, "AjusteTrimestral", ["+ mesesDelPrimerTramo(): int", "Devuelve 3 meses"])
caja(1200, 680, 1740, 950, "AjusteSemestral", ["+ mesesDelPrimerTramo(): int", "Devuelve 6 meses"])
d.line((600, 270, 880, 270), fill=AZUL, width=5)
d.text((740, 228), "usa", font=normal, fill=AZUL, anchor="mm")
for inferior in [968, 1500]:
    d.line((inferior, 680, inferior, 525, 1305, 525, 1305, 400), fill=AZUL, width=5, joint="curve")
d.polygon([(1305, 400), (1286, 434), (1324, 434)], fill="white", outline=AZUL)
d.text((90, 986), "La cantidad de meses cambia según el objeto creado; el servicio usa el tipo común.",
       font=normal, fill=AZUL)
clases.save(DESTINO.with_name("06_clases_poo.png"), optimize=True)

# Secuencias del registro, la transacción y la generación de cuotas.
import secuencias_ap3

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


imagen = Image.new("RGB", (ANCHO, ALTO), "white")
dibujo = ImageDraw.Draw(imagen)
normal = fuente(29)
negrita = fuente(32, True)
chica = fuente(25)

participantes = [
    "Administrativo", "FormularioContrato", "AlquilerControlador",
    "AltaContrato", "AlquilerService", "BaseDatos",
    "java.sql.Connection", "OperacionDao", "PeriodicidadAjuste",
]
xs = [170 + i * 407 for i in range(len(participantes))]

for x, nombre in zip(xs, participantes):
    dibujo.rounded_rectangle((x - 187, 45, x + 187, 135), 12, fill=FONDO, outline=VERDE, width=4)
    dibujo.text((x, 90), nombre, font=chica, fill=AZUL, anchor="mm")
    for y in range(145, ALTO - 115, 23):
        dibujo.line((x, y, x, min(y + 12, ALTO - 115)), fill="#72908D", width=2)


def mensaje(origen, destino, y, etiqueta, retorno=False):
    x1, x2 = xs[origen], xs[destino]
    color = "#5B7775" if retorno else AZUL
    dibujo.line((x1, y, x2, y), fill=color, width=3)
    sentido = 1 if x2 > x1 else -1
    dibujo.line((x2 - sentido * 15, y - 9, x2, y), fill=color, width=3)
    dibujo.line((x2 - sentido * 15, y + 9, x2, y), fill=color, width=3)
    centro = (x1 + x2) / 2
    caja = dibujo.textbbox((0, 0), etiqueta, font=chica)
    ancho = caja[2] - caja[0]
    dibujo.rectangle((centro - ancho / 2 - 9, y - 40, centro + ancho / 2 + 9, y - 6), fill="white")
    dibujo.text((centro, y - 23), etiqueta, font=chica, fill=color, anchor="mm")


mensajes = [
    (0, 1, "Completa datos y pulsa Guardar"),
    (1, 2, "Evento; leer campos"),
    (2, 3, "new AltaContrato(...)"),
    (2, 4, "registrarContrato(alta, usuario)"),
    (4, 5, "abrir()"),
    (5, 6, "abre conexión JDBC"),
    (4, 7, "administrativoActivo / inquilinoActivo"),
    (4, 7, "bloquearPropiedad / contratoSuperpuesto"),
    (4, 7, "insertarContrato(...)"),
    (4, 8, "crear(ajusteMeses)"),
    (4, 8, "importeInicial(mes, importe)"),
    (4, 7, "insertarCuota(...)"),
    (4, 6, "commit()"),
    (4, 2, "id del contrato"),
    (2, 1, "Confirmación"),
]

for indice, (origen, destino, etiqueta) in enumerate(mensajes):
    y = 240 + indice * 105
    mensaje(origen, destino, y, etiqueta, indice >= 13)

dibujo.rounded_rectangle((1610, 1190, 3500, 1450), 16, outline=VERDE, width=3)
dibujo.text((1640, 1210), "loop: por cada mes de vigencia", font=negrita, fill=VERDE)
dibujo.text((90, 1880), "MySQL recibe las consultas JDBC preparadas. Si falla una validación o escritura: rollback().", font=normal, fill=AZUL)
dibujo.text((90, 1930), "El contrato y todas sus cuotas se confirman en una misma transacción.", font=normal, fill=AZUL)

imagen.save(DESTINO, optimize=True)
print(DESTINO)

clases = Image.new("RGB", (1800, 1050), "white")
d = ImageDraw.Draw(clases)


def caja(x1, y1, x2, y2, titulo, detalles):
    d.rounded_rectangle((x1, y1, x2, y2), 14, fill=FONDO, outline=VERDE, width=5)
    d.text(((x1 + x2) / 2, y1 + 50), titulo, font=negrita, fill=AZUL, anchor="mm")
    d.line((x1, y1 + 95, x2, y1 + 95), fill=VERDE, width=3)
    for i, linea in enumerate(detalles):
        d.text((x1 + 25, y1 + 135 + i * 52), linea, font=normal, fill=AZUL)


caja(60, 140, 600, 400, "AlquilerService", ["- operaciones: OperacionDao", "+ generarCuotas(...)" ])
caja(880, 70, 1730, 400, "«abstract» PeriodicidadAjuste", [
    "+ mesesDelPrimerTramo(): int", "+ importeInicial(mes, importe): BigDecimal",
    "+ crear(meses): PeriodicidadAjuste",])
caja(735, 680, 1200, 950, "AjusteTrimestral", ["+ mesesDelPrimerTramo(): 3"])
caja(1260, 680, 1740, 950, "AjusteSemestral", ["+ mesesDelPrimerTramo(): 6"])
d.line((600, 270, 880, 270), fill=AZUL, width=5)
d.text((740, 228), "usa", font=normal, fill=AZUL, anchor="mm")
for inferior in [968, 1500]:
    d.line((inferior, 680, inferior, 525, 1305, 525, 1305, 400), fill=AZUL, width=5, joint="curve")
d.polygon([(1305, 400), (1286, 434), (1324, 434)], fill="white", outline=AZUL)
d.text((90, 986), "La cantidad de meses cambia según el objeto creado; el servicio usa el tipo común.",
       font=normal, fill=AZUL)
clases.save(DESTINO.with_name("06_clases_poo.png"), optimize=True)

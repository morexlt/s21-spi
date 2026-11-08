from pathlib import Path
from PIL import Image, ImageDraw, ImageFont
from html import escape


CARPETA = Path(__file__).resolve().parent
ANCHO, ALTO = 1500, 1120
VERDE = '#087F73'
TEXTO = '#173B3A'
FONDO = '#EAF5F3'


def generar():
    imagen = Image.new('RGB', (ANCHO, ALTO), 'white')
    dibujo = ImageDraw.Draw(imagen)
    svg = [f'<svg xmlns="http://www.w3.org/2000/svg" width="{ANCHO}" height="{ALTO}" viewBox="0 0 {ANCHO} {ALTO}">',
           '<rect width="100%" height="100%" fill="white"/>']

    def texto(x, y, contenido, tamano=27, negrita=False, centrado=False):
        nombre = 'Arial Bold.ttf' if negrita else 'Arial.ttf'
        fuente = ImageFont.truetype('/System/Library/Fonts/Supplemental/' + nombre, tamano)
        inicio = x - dibujo.textlength(contenido, font=fuente) / 2 if centrado else x
        dibujo.text((inicio, y), contenido, font=fuente, fill=TEXTO)
        svg.append(f'<text x="{x if centrado else inicio}" y="{y + tamano}" font-family="Arial,sans-serif" '
                   f'font-size="{tamano}" font-weight="{700 if negrita else 400}" '
                   f'text-anchor="{"middle" if centrado else "start"}" fill="{TEXTO}">{escape(contenido)}</text>')

    def caja(x, y, titulo, atributos):
        dibujo.rectangle((x, y, x + 340, y + 180), fill='white', outline=VERDE, width=3)
        dibujo.rectangle((x, y, x + 340, y + 58), fill=FONDO, outline=VERDE, width=3)
        svg.append(f'<rect x="{x}" y="{y}" width="340" height="180" fill="white" stroke="{VERDE}" stroke-width="3"/>')
        svg.append(f'<rect x="{x}" y="{y}" width="340" height="58" fill="{FONDO}" stroke="{VERDE}" stroke-width="3"/>')
        texto(x + 170, y + 12, titulo, 29, True, True)
        for i, atributo in enumerate(atributos):
            texto(x + 20, y + 76 + i * 36, atributo, 25)

    def relacion(puntos, etiqueta, x, y, cardinalidades):
        dibujo.line(puntos, fill=TEXTO, width=3)
        svg.append('<polyline points="' + ' '.join(f'{a},{b}' for a, b in puntos)
                   + f'" fill="none" stroke="{TEXTO}" stroke-width="3"/>')
        fuente = ImageFont.truetype('/System/Library/Fonts/Supplemental/Arial.ttf', 25)
        mitad = dibujo.textlength(etiqueta, font=fuente) / 2 + 7
        dibujo.rectangle((x - mitad, y - 3, x + mitad, y + 33), fill='white')
        svg.append(f'<rect x="{x - mitad}" y="{y - 3}" width="{2 * mitad}" height="36" fill="white"/>')
        texto(x, y, etiqueta, 25, centrado=True)
        for a, b, valor in cardinalidades:
            texto(a, b, valor, 25)

    caja(40, 70, 'Propietario', ['nombre', 'contacto'])
    caja(580, 70, 'Propiedad', ['código', 'dirección'])
    caja(1120, 70, 'Inquilino', ['nombre', 'contacto'])
    caja(580, 440, 'Contrato', ['vigencia e importe', 'comisión y ajuste'])
    caja(40, 440, 'Liquidación', ['período', 'ingresos, gastos y neto'])
    caja(1120, 440, 'Gasto', ['fecha y concepto', 'importe y descontable'])
    caja(580, 810, 'Cuota', ['período y vencimiento', 'importe y saldo'])
    caja(1120, 810, 'Pago', ['fecha, capital y mora', 'medio y referencia'])

    relacion([(380, 155), (580, 155)], 'posee', 480, 115,
             [(395, 163, '1'), (505, 163, '0..*')])
    relacion([(750, 250), (750, 440)], 'se alquila mediante', 750, 327,
             [(770, 266, '1'), (770, 401, '0..*')])
    relacion([(1290, 250), (1290, 340), (950, 340), (950, 530), (920, 530)],
             'suscribe', 1105, 298, [(1310, 265, '1'), (963, 485, '0..*')])
    relacion([(210, 250), (210, 440)], 'recibe', 210, 327,
             [(230, 266, '1'), (230, 401, '0..*')])
    relacion([(920, 565), (1120, 565)], 'registra', 1020, 577,
             [(937, 529, '1'), (1042, 529, '0..*')])
    relacion([(750, 620), (750, 810)], 'genera', 750, 701,
             [(770, 636, '1'), (770, 769, '1..*')])
    relacion([(920, 900), (1120, 900)], 'recibe', 1020, 859,
             [(937, 912, '1'), (1042, 912, '0..*')])

    texto(750, 1035, 'Cada liquidación reúne los cobros y gastos seleccionados del propietario.', 27, centrado=True)
    texto(750, 1075, 'Gastos y liquidaciones forman parte del sistema previsto; su implementación está pendiente.', 24, centrado=True)
    imagen.save(CARPETA / '00_dominio.png')
    (CARPETA / '00_dominio.svg').write_text('\n'.join(svg + ['</svg>']), encoding='utf-8')


if __name__ == '__main__':
    generar()

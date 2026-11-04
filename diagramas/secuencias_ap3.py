from pathlib import Path
from PIL import Image, ImageDraw, ImageFont

CARPETA = Path(__file__).parent
AZUL = '#173B3A'
VERDE = '#087F73'
FONDO = '#EAF5F3'


def fuente(tamano):
    return ImageFont.truetype('/System/Library/Fonts/Supplemental/Arial.ttf', tamano)


def secuencia(archivo, nombres, pasos, nota):
    imagen = Image.new('RGB', (1800, 920), 'white')
    d = ImageDraw.Draw(imagen)
    xs = [190 + i * 355 for i in range(len(nombres))]
    cuotas = archivo == '08_secuencia_cuotas.png'
    for indice, (x, nombre) in enumerate(zip(xs, nombres)):
        inicio = 170 if cuotas and indice == 2 else 20
        d.rectangle((x - 170, inicio, x + 170, inicio + 80), fill=FONDO, outline=VERDE, width=2)
        d.multiline_text((x, inicio + 40), nombre, font=fuente(25), fill=AZUL, anchor='mm', align='center')
        for y in range(inicio + 85, 810, 20):
            d.line((x, y, x, y + 10), fill='#72908D', width=2)
    if cuotas:
        d.rectangle((90, 300, 1760, 660), outline=VERDE, width=2)
        d.text((105, 310), 'loop [por cada mes de vigencia]', font=fuente(23), fill=VERDE)
    for i, (a, b, texto) in enumerate(pasos):
        y = 155 + i * 55
        x1, x2 = xs[a], xs[b]
        if cuotas and i == 3:
            continue
        if cuotas and i == 1:
            x2 -= 170
            texto = '«create»'
        if texto.startswith('new '):
            d.rectangle((x1 + 10, y - 18, x1 + 340, y + 20), fill=FONDO, outline=VERDE, width=2)
            d.text((x1 + 20, y), texto, font=fuente(23), fill=AZUL, anchor='lm')
            continue
        if a == b:
            d.line((x1, y, x1 + 60, y, x1 + 60, y + 15, x1, y + 15), fill=AZUL, width=2)
            d.text((x1 + 70, y), texto, font=fuente(23), fill=AZUL, anchor='lm')
            continue
        retorno = texto.startswith('retorno: ')
        texto = texto.removeprefix('retorno: ')
        if retorno:
            for x in range(min(x1, x2), max(x1, x2), 18):
                d.line((x, y, min(x + 10, max(x1, x2)), y), fill=AZUL, width=2)
        else:
            d.line((x1, y, x2, y), fill=AZUL, width=2)
        sentido = 1 if x2 > x1 else -1
        d.line((x2 - sentido * 12, y - 7, x2, y, x2 - sentido * 12, y + 7), fill=AZUL, width=2)
        centro = (x1 + x2) / 2
        ancho = d.textlength(texto, font=fuente(23))
        d.rectangle((centro - ancho / 2 - 4, y - 32, centro + ancho / 2 + 4, y - 5), fill='white')
        d.text((centro, y - 20), texto, font=fuente(23), fill=AZUL, anchor='mm')
    d.multiline_text((35, 840), nota, font=fuente(24), fill=AZUL, spacing=10)
    imagen.save(CARPETA / archivo)


secuencia('05_secuencia.png', ['formulario:\nFormularioContrato', 'controlador:\nAlquilerControlador', 'servicio:\nAlquilerService', 'JOptionPane', 'ventana:\nPrincipalVista'], [
    (0, 1, 'evento Guardar'),
    (1, 0, 'leer campos'),
    (0, 1, 'retorno: datos'),
    (1, 1, 'new AltaContrato(...)'),
    (1, 2, 'registrarContrato(alta, usuario)'),
    (2, 1, 'retorno: id del contrato (detalle en figura 7)'),
    (1, 3, 'showMessageDialog(ventana, mensaje, ...)'),
    (1, 1, 'actualizar()'),
    (1, 2, 'consultarCuotas(), propiedades(), inquilinos()'),
    (2, 1, 'retorno: listas actualizadas'),
    (1, 4, 'actualizar tabla y catálogos del formulario'),
], 'El Administrativo inicia el registro. El controlador crea AltaContrato con los datos ingresados.\nSwingWorker ejecuta el servicio en segundo plano; done() confirma el resultado y actualiza la vista.')

secuencia('07_secuencia_transaccion.png', ['servicio:\nAlquilerService', 'baseDatos:\nBaseDatos', 'DriverManager', 'conexion:\nConnection', 'operaciones:\nOperacionDao'], [
    (0, 0, 'validarContrato(alta)'),
    (0, 1, 'abrir()'),
    (1, 2, 'getConnection(...)'),
    (2, 1, 'retorno: conexion'),
    (1, 0, 'retorno: conexion'),
    (0, 3, 'setTransactionIsolation(...); setAutoCommit(false)'),
    (0, 4, 'administrativoActivo(...), inquilinoActivo(...)'),
    (0, 4, 'bloquearPropiedad(...), contratoSuperpuesto(...)'),
    (0, 4, 'insertarContrato(...)'),
    (0, 0, 'generarCuotas(...) — detalle en figura 8'),
    (0, 3, 'commit()'),
    (0, 3, 'close() al salir del try'),
], 'Flujo exitoso. Si falla una validación o escritura dentro de la transacción: rollback() y propagación del error.\nOperacionDao usa BaseDatos para ejecutar las sentencias JDBC. MySQL es el motor de persistencia.')

secuencia('08_secuencia_cuotas.png', ['servicio:\nAlquilerService', 'PeriodicidadAjuste\n«fábrica estática»', 'periodicidad:\nAjusteTrimestral', 'operaciones:\nOperacionDao', 'BaseDatos'], [
    (0, 1, 'crear(3)'),
    (1, 2, '«create» AjusteTrimestral()'),
    (1, 0, 'retorno: periodicidad'),
    (0, 0, 'for: cada mes de vigencia'),
    (0, 2, 'importeInicial(mes, alta.importe())'),
    (2, 2, 'mesesDelPrimerTramo(): 3'),
    (2, 0, 'retorno: importe inicial o null desde el cuarto mes'),
    (0, 3, 'insertarCuota(conexion, contrato, periodo, vencimiento, importe)'),
    (3, 4, 'insertar(conexion, SQL, parámetros)'),
], 'Detalle de generarCuotas(): se repiten las llamadas desde importeInicial() por cada mes del contrato.\nSe muestra el caso trimestral; crear(6) devuelve AjusteSemestral, cuyo primer tramo dura seis meses.')

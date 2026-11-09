from generar_ap4 import Diagrama


def generar():
    d = Diagrama('10_archivos_configuracion', 1600, 1320)
    d.caja(40, 30, 700, 210, 'AlquilerControlador', [
        '- servicio: AlquilerService', '- exportarCuotas(): void'], 29)
    d.caja(860, 30, 700, 210, 'ArchivoCuotas', [
        '+ guardar(destino, contrato, reemplazar): void',
        '- escribir(salida, contrato): void'], 26)
    d.linea([(740, 130), (860, 130)], arrow=True)
    d.caja(40, 360, 700, 210, 'AlquilerService', [
        '+ consultarContrato(contrato, usuario):', '    EstadoContrato'], 28)
    d.caja(860, 360, 700, 210, 'EstadoContrato «record»', [
        'id, propiedad, propietario, inquilino, inicio, fin',
        'cuotas: List<CuotaContrato>'], 26)
    d.linea([(390, 240), (390, 360)], arrow=True)
    d.linea([(740, 465), (860, 465)], dash=True, arrow=True)
    d.linea([(1210, 240), (1210, 360)], dash=True, arrow=True)
    d.caja(40, 690, 700, 210, 'AlquilerDao', [
        '+ estadoContrato(conexion, id): EstadoContrato',
        '- cuotasContrato(conexion, id): List<CuotaContrato>'], 25)
    d.caja(860, 690, 700, 210, 'CuotaContrato «record»', [
        'periodo, vencimiento, importe',
        'capitalCobrado, moraCobrada, saldo'], 27)
    d.linea([(390, 570), (390, 690)], arrow=True)
    d.linea([(1210, 570), (1210, 690)], arrow=True)
    d.texto(1270, 620, '0..*', 26)
    d.caja(40, 1030, 700, 210, 'Configuracion', [
        '+ validar(): void', '+ obligatorio(nombre): String',
        '+ valor(nombre, predeterminado): String'], 26)
    d.caja(860, 1030, 700, 210, 'Lectores de configuración', [
        'PropieraApp · BaseDatos · AutenticacionService',
        'El entorno tiene prioridad sobre .env.'], 25)
    d.linea([(860, 1150), (740, 1150)], arrow=True)
    d.guardar()

    d = Diagrama('11_comunicacion_final', 1600, 1100)
    d.caja(40, 20, 1520, 270, '«device» Computadora de desarrollo', [
        '«executionEnvironment» JVM / Java 21+',
        'PropieraApp · vistas Swing · controladores · servicios · DAO · Connector/J',
        'Los componentes se comunican mediante llamadas a métodos dentro de Java.'], 29)
    d.caja(40, 390, 660, 220, '«artifact» .env', [
        'Configuración local de conexión y acceso.', 'Lectura UTF-8 mediante Configuracion.'], 27)
    d.caja(900, 390, 660, 220, '«artifact» Estado de cuotas TXT', [
        'Escritura UTF-8 mediante ArchivoCuotas.', 'Destino elegido por el usuario.'], 27)
    d.linea([(360, 290), (360, 390)], arrow=True)
    d.linea([(1230, 290), (1230, 390)], arrow=True)
    d.caja(310, 800, 980, 220, '«executionEnvironment» Docker / MySQL 8.4', [
        'Once tablas InnoDB · codificación utf8mb4',
        'Persistencia de participantes, contratos, cuotas y movimientos.'], 28)
    d.linea([(800, 290), (800, 800)], arrow=True)
    d.texto(820, 650, 'JDBC / TCP con TLS', 27)
    d.texto(820, 695, '127.0.0.1:33316 → 3306', 27)
    d.guardar()


if __name__ == '__main__':
    generar()

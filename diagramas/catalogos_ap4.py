from generar_ap4 import Diagrama


def generar():
    d = Diagrama('09_diseno_catalogos', 1600, 1240)
    d.caja(40, 30, 720, 240, 'CatalogosVista', [
        '- propietarios: PanelPersonas', '- propiedades: PanelPropiedades',
        '- inquilinos: PanelPersonas', '+ cargar(datos): void'], 29)
    d.caja(840, 30, 720, 240, 'CatalogoControlador', [
        '- servicio: CatalogoService', '- ventana: PrincipalVista',
        '+ iniciar(): void'], 29)
    d.linea([(840, 145), (760, 145)], arrow=True)
    d.caja(40, 420, 720, 290, 'CatalogoService', [
        '+ consultar(usuario): DatosCatalogo', '+ guardarPersona(tipo, datos, usuario): long',
        '+ guardarPropiedad(datos, usuario): long', '+ bajaPersona(...), bajaPropiedad(...)'], 28)
    d.caja(840, 420, 720, 290, 'CatalogoDao', [
        '+ personas(...), propiedades(...)', '+ guardarPersona(...), guardarPropiedad(...)',
        '+ bloquearPersona(...), bloquearPropiedad(...)', '+ bajaPersona(...), bajaPropiedad(...)'], 27)
    d.linea([(1200, 270), (1200, 340), (400, 340), (400, 420)], arrow=True)
    d.linea([(760, 555), (840, 555)], arrow=True)
    d.caja(40, 870, 460, 220, 'Persona «record»', [
        'id, documento, nombre, contacto', 'activo'], 25)
    d.caja(570, 870, 460, 220, 'Propiedad «record»', [
        'id, codigo, direccion', 'propietario, nombrePropietario', 'activo'], 25)
    d.caja(1100, 870, 460, 220, 'BaseDatos', [
        '+ abrir(): Connection', '+ preparar(...): PreparedStatement', '+ insertar(...): long'], 22)
    d.linea([(270, 710), (270, 870)], dash=True, arrow=True)
    d.linea([(550, 710), (550, 790), (800, 790), (800, 870)], dash=True, arrow=True)
    d.linea([(1330, 710), (1330, 870)], arrow=True)
    d.texto(800, 1150, 'El servicio valida permisos y conserva el historial. El DAO ejecuta el SQL con JDBC.', 28, center=True)
    d.guardar()


if __name__ == '__main__':
    generar()

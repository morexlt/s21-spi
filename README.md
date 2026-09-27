# Propiera — aplicación de escritorio AP2

Prototipo de escritorio en Java Swing y MySQL para la gestión interna de
alquileres de Propiera. Corresponde a la actividad práctica 2.

Repositorio: https://github.com/morexlt/s21-spi

## Funciones

- Registrar contratos y generar sus cuotas mensuales.
- Consultar cuotas, vencimientos y saldos.
- Registrar pagos parciales o completos, con capital y mora separados.
- Administrativo registra y consulta. Martillero sólo consulta.
- Validar contratos superpuestos, referencias duplicadas y pagos superiores al saldo.

Cada propiedad tiene un propietario. Los datos del propietario se obtienen de la
propiedad elegida, sin cargarlos nuevamente en el contrato. Un propietario puede
tener varias propiedades.

El contrato comienza el día 1 y termina el último día del mes, con un máximo de
120 meses. El vencimiento se fija entre el día 1 y el 28. Se configura una
periodicidad de ajuste trimestral o semestral; su aplicación está pendiente.

Se generan las cuotas de toda la vigencia. Sólo el primer tramo tiene importe
confirmado; las siguientes quedan pendientes de ajuste y no admiten cobros.
Los gastos, las liquidaciones, la actualización de ajustes y el cálculo
automático de mora no están implementados en esta entrega.

El modelo de liquidaciones prevé incluir cada pago o gasto completo una sola
vez. pago_id y gasto_id son las claves primarias de sus respectivos detalles.
El servicio de liquidación deberá comprobar el propietario, el mes y los importes
antes de confirmar. Los totales de cobros y gastos se agregan por separado.

## Organización MVC

```
src/propiera/
  PropieraApp.java       inicio de la aplicación
  modelo/               datos del usuario, contrato, cuota y opciones
  vista/                ventanas, formularios y modelo de la tabla Swing
  controlador/          acciones de los botones y coordinación de las vistas
  servicio/             validaciones y operaciones del negocio
  persistencia/         conexión JDBC y consultas SQL
```

Las vistas no consultan MySQL ni validan reglas del negocio. El controlador
lee los formularios y utiliza los servicios. Los DAO contienen el SQL.
Las operaciones de base de datos se ejecutan con SwingWorker para no bloquear
la ventana. Los cambios visuales se realizan en el hilo de Swing.

El contrato y sus cuotas se guardan en una misma transacción. Los bloqueos de
propiedad y cuota evitan contratos superpuestos y sobrepagos simultáneos.
La autorización también se verifica en el servicio antes de guardar.

## Preparación

Requisitos: JDK 21 o superior, MySQL 8.4 y Connector/J 9.4.0.
Swing forma parte del JDK. La compilación se realiza mediante compilar.sh.

Para usar una base nueva con Docker, definir PROPIERA_MYSQL_ROOT_PASSWORD
y PROPIERA_DB_PASSWORD y ejecutar `docker compose up -d`.
El servicio utiliza el puerto local 33316 y crea la base propiera_ap2.
Esperar a que termine la inicialización. Si ya existe MySQL, crear una base
vacía utf8mb4 y cargar sql/01_esquema.sql y sql/02_datos_demo.sql.
No cargar esos archivos en una base con información real.

## Ejecutar

Desde esta carpeta, definir las variables en la misma terminal:

```sh
export PROPIERA_DB_USER=propiera
export PROPIERA_DB_PASSWORD='clave de la base'
export PROPIERA_ADMIN_PASSWORD='clave del administrativo'
export PROPIERA_MARTILLERO_PASSWORD='clave del martillero'
bash ejecutar.sh
```

El script compila y abre la ventana de ingreso. Los usuarios de demostración
son `administrativo` y `martillero`. Las claves se toman del entorno y los
perfiles se comprueban contra los usuarios activos de la base.

PROPIERA_DB_URL permite cambiar la conexión. Por defecto se usa
`jdbc:mysql://127.0.0.1:33316/propiera_ap2?sslMode=REQUIRED&connectionTimeZone=America/Argentina/Buenos_Aires`.
En una base remota se debe verificar la identidad del servidor con un certificado
válido. Esta autenticación de demostración no reemplaza cuentas individuales,
contraseñas almacenadas de forma segura ni auditoría para uso real.

En macOS los scripts detectan el OpenJDK instalado por Homebrew.
En otros entornos se puede configurar JAVA_HOME o disponer de Java en PATH.
El proyecto también se puede importar con pom.xml en IntelliJ IDEA o Eclipse.

## Probar la aplicación

Ingresar como administrativo y revisar las tres pestañas.

1. En Registrar contrato, elegir propiedad e inquilino, completar la vigencia,
   importe, comisión y condición de mora, y guardar.
2. En Cuotas y saldos, seleccionar una cuota con importe confirmado y usar
   Cobrar cuota seleccionada.
3. En Registrar pago, completar fecha, capital, mora y una referencia única.
   Guardar y comprobar el saldo actualizado.
4. Intentar cargar un contrato superpuesto o un pago mayor al saldo.
5. Cerrar la aplicación e ingresar como martillero: sólo aparece la consulta.

Las fechas se escriben AAAA-MM-DD. Los importes admiten punto o coma decimal,
sin separadores de miles y con hasta dos decimales. La mora se informa manualmente.

## Pruebas automáticas

Usar una base nueva con sólo los datos iniciales y las variables configuradas:

```sh
bash compilar.sh
java -cp 'build/classes:lib/*' propiera.Pruebas
java -Djava.awt.headless=true -cp 'build/classes:lib/*' propiera.PruebasSwing
```

En Windows el separador del classpath es punto y coma. En macOS se puede usar
el Java de JAVA_HOME o /opt/homebrew/opt/openjdk/bin/java.

Pruebas se niega a continuar si encuentra contratos existentes. No borra datos.
Para repetirla, crear otra base y cambiar PROPIERA_DB_URL.
PruebasSwing verifica formularios, tabla, importes, autenticación y permisos.
También comprueba que actualizar los catálogos conserve los participantes
seleccionados por identificador y que los importes y saldos se ordenen
numéricamente. Las pruebas de rechazo comprueban el mensaje de validación
o el código y estado SQL de la restricción esperada.

sql/03_consultas.sql contiene consultas de saldos, cobros y liquidaciones.
sql/04_abm_seguro.sql realiza un ABM sobre un registro temporal y revierte
la transacción. evidencia/ conserva los resultados de las pruebas.

## Archivos

Las fuentes están en src/, las pruebas en tests/ y la base en sql/.
Los diagramas están en diagramas/ y los resultados de las pruebas en evidencia/.

El conector JDBC se descarga en lib/ sólo si falta. El paquete de entrega
contiene las fuentes, los scripts SQL, las pruebas y los diagramas.
Las dependencias binarias, los archivos compilados y las credenciales
no forman parte del paquete.

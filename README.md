# Propiera — aplicación de escritorio AP4

Prototipo de escritorio en Java Swing y MySQL para la gestión interna de
alquileres de Propiera. Corresponde a la actividad práctica 4.

Repositorio: https://github.com/morexlt/s21-spi

## Funciones

- Administrar propietarios, propiedades e inquilinos: altas, modificaciones y bajas lógicas.
- Registrar contratos y generar sus cuotas mensuales.
- Exportar las cuotas de un contrato a un archivo TXT.
- Consultar cuotas, vencimientos y saldos.
- Registrar pagos parciales o completos, con capital y mora separados.
- Confirmar el nuevo importe mensual de un tramo de ajuste.
- Administrativo registra, consulta y exporta. Martillero consulta y exporta.
- Validar contratos superpuestos, referencias duplicadas y pagos superiores al saldo.

Cada propiedad tiene un propietario. Los datos del propietario se obtienen de la
propiedad elegida, sin cargarlos nuevamente en el contrato. Un propietario puede
tener varias propiedades.

El contrato comienza el día 1 y termina el último día del mes, con un máximo de
120 meses. El vencimiento se fija entre el día 1 y el 28. Se configura una
periodicidad de ajuste trimestral o semestral. El importe de cada nuevo tramo
se informa manualmente después de consultar la condición pactada.

Se generan las cuotas de toda la vigencia. Sólo el primer tramo tiene importe
confirmado; las siguientes quedan pendientes de ajuste y no admiten cobros.
Los gastos, las liquidaciones, el cálculo de índices de ajuste y el cálculo
automático de mora no están implementados en esta entrega.

El modelo de liquidaciones prevé incluir cada movimiento seleccionado por su
importe completo y como máximo una vez. Sólo se descuentan los gastos marcados
como descontables. pago_id y gasto_id son las claves primarias de sus respectivos detalles.
El servicio de liquidación deberá comprobar el propietario, el mes y los importes
antes de confirmar. Los totales de cobros y gastos se agregan por separado.

## Interfaz

La consulta muestra la cantidad de cuotas con saldo y pendientes de ajuste.
El selector Propiedad permite consultar un inmueble o todas las propiedades.
El resumen corresponde a las filas visibles y el filtro se conserva al actualizar.
Para registrar un cobro o confirmar un ajuste se selecciona una fila de la tabla.
Los importes se presentan con punto de miles y coma decimal, y se ordenan
numéricamente. En los formularios se ingresan sin separador de miles.
Las ventanas permiten desplazarse por los campos cuando se reduce su tamaño.

## Datos básicos

En Datos básicos, elegir Propietarios, Propiedades o Inquilinos. Nuevo limpia el
formulario; seleccionar una fila permite modificar sus datos. Guardar registra
los cambios. Documento y nombre son obligatorios para las personas; el contacto
es opcional. La propiedad requiere código, dirección y un propietario activo.
El documento es único dentro de cada tipo de persona y el código es único para
las propiedades. La unicidad incluye los registros inactivos.

Dar de baja marca el registro como inactivo y conserva contratos, cuotas y cobros.
Para reactivarlo, seleccionarlo, marcar Activo y guardar. Los inactivos no pueden
participar en nuevos contratos; tampoco las propiedades de un dueño inactivo.
Una propiedad con contratos registrados conserva su propietario: no se permite
cambiarlo desde el ABM. Los cobros de contratos existentes continúan disponibles.
Sólo Administrativo puede administrar estos datos; el servicio verifica su permiso.

## Exportar cuotas

Seleccionar una cuota en Cuotas y saldos y pulsar Exportar cuotas. La exportación
incluye todas las cuotas del contrato de esa fila, aunque haya otros contratos
en la misma propiedad o cambie el orden de la tabla. Está disponible para ambos
perfiles. Se consulta MySQL nuevamente al exportar; no se usa una copia de los
valores visibles para calcular el archivo.

Elegir la ubicación del TXT. Si ya existe, se solicita confirmación antes de
reemplazarlo. Cancelar no genera un archivo. El texto usa UTF-8 y conserva tildes
y eñes. Incluye propiedad, propietario, inquilino, vigencia, cuotas, vencimientos,
capital cobrado, mora cobrada y saldo. Las cuotas pendientes de ajuste se señalan
sin importe; no se consideran de valor cero ni se suman al saldo confirmado.
La mora se informa por separado. El archivo se termina de escribir en un temporal
antes de moverlo al destino, para conservar el archivo anterior ante un error.

## Organización MVC

```
src/propiera/
  PropieraApp.java       inicio de la aplicación
  configuracion/        lectura de .env y variables de entorno
  modelo/               datos del usuario, participantes, propiedades, contratos y cuotas
  vista/                ventanas, formularios y modelo de la tabla Swing
  controlador/          acciones de los botones y coordinación de las vistas
  servicio/             validaciones y operaciones del negocio
  persistencia/         conexión JDBC, consultas SQL y escritura del TXT
```

Las vistas no consultan MySQL ni validan reglas del negocio. El controlador
lee los formularios y utiliza los servicios. Los DAO contienen el SQL.
Las operaciones de base de datos se ejecutan con SwingWorker para no bloquear
la ventana. Los cambios visuales se realizan en el hilo de Swing.

El contrato y sus cuotas se guardan en una misma transacción. Los bloqueos de
propiedad y cuota evitan contratos superpuestos y sobrepagos simultáneos.
La autorización también se verifica en el servicio antes de guardar.

La generación de cuotas utiliza `PeriodicidadAjuste`. La clase abstracta define
la operación común y `AjusteTrimestral` y `AjusteSemestral` determinan cuántos
meses conservan el importe inicial. `AlquilerService` usa la misma operación
sin distinguir el tipo concreto. Los tramos posteriores continúan pendientes
de confirmación hasta que se informe el importe del tramo.

Para confirmar un ajuste, se selecciona la primera cuota pendiente del tramo.
El servicio comprueba los permisos y el orden de los tramos, bloquea el contrato
y sus cuotas y utiliza AjusteDao para actualizar los importes. No se permite
sobrescribir un tramo confirmado ni modificar cuotas con pagos. Si el contrato
termina antes, sólo se actualizan los meses restantes. Toda la operación se
confirma o revierte junta.

TablaCuotasModelo usa un arreglo String[] para las siete columnas y un
ArrayList<Cuota> para las filas que obtiene de MySQL. Las columnas tienen
estructura fija; la cantidad de cuotas cambia con los contratos registrados.

## Preparación

Requisitos: JDK 21 o superior, MySQL 8.4 y Connector/J 9.4.0.
Swing forma parte del JDK. La compilación se realiza mediante compilar.sh.

Para usar una base nueva con Docker, completar PROPIERA_MYSQL_ROOT_PASSWORD
y PROPIERA_DB_PASSWORD en .env y ejecutar `docker compose up -d`.
El servicio utiliza el puerto local 33316 y crea la base propiera_ap4.
Esperar a que termine la inicialización. Si ya existe MySQL, crear una base
vacía utf8mb4 y cargar sql/01_esquema.sql y sql/02_datos_demo.sql.
No cargar esos archivos en una base con información real.

Para recorrer la interfaz con movimientos ya cargados, ejecutar después
sql/05_escenario_presentacion.sql, únicamente si la base todavía no tiene contratos.
Incluye tres propiedades ficticias de Neuquén (Roca 425, Mitre 680 y San Martín 1240),
36 cuotas, un pago parcial, un pago completo y un tramo de ajuste confirmado.
Las pruebas automáticas de negocio utilizan sólo 01 y 02, sin este escenario.

## Ejecutar

Crear .env en la carpeta del proyecto a partir de .env.example, si todavía
no existe. Completar la URL, el usuario de MySQL y las tres contraseñas:
la de MySQL y las de los perfiles Administrativo y Martillero.

```sh
cp -n .env.example .env
# Completar .env antes de ejecutar.
bash ejecutar.sh
```

Java lee .env directamente con Configuracion. No se necesita un complemento de
IntelliJ ni ejecutar el archivo como un script. Se admiten líneas vacías,
comentarios en líneas separadas, valores sin comillas o entre comillas simples
o dobles. No se expanden variables ni se ejecutan comandos del archivo.
Los errores indican el nombre de la variable o el número de línea, sin mostrar
contraseñas. Después de cambiar .env hay que reiniciar la aplicación.

Una variable definida explícitamente en el entorno tiene prioridad sobre .env.
Esto permite ejecutar las pruebas en otra base sin cambiar la configuración
local. Una variable vacía se considera un error si es obligatoria.

El script compila y abre la ventana de ingreso. Los usuarios de demostración
son administrativo y martillero. Sus claves están en PROPIERA_ADMIN_PASSWORD
y PROPIERA_MARTILLERO_PASSWORD; sus roles y estados se verifican en MySQL.

PROPIERA_DB_URL permite cambiar la conexión. Por defecto se usa
`jdbc:mysql://127.0.0.1:33316/propiera_ap4?sslMode=REQUIRED&connectionTimeZone=America/Argentina/Buenos_Aires`.
En una base remota se debe verificar la identidad del servidor con un certificado
válido. Esta autenticación de demostración no reemplaza cuentas individuales,
contraseñas almacenadas de forma segura ni auditoría para uso real.

.env y sus copias locales están excluidos de Git.
.env.example se incluye sin contraseñas. En IntelliJ, configurar el directorio
de trabajo como la carpeta del proyecto y ejecutar propiera.PropieraApp.

En macOS los scripts detectan el OpenJDK instalado por Homebrew.
En otros entornos se puede configurar JAVA_HOME o disponer de Java en PATH.
El proyecto también se puede importar con pom.xml en IntelliJ IDEA o Eclipse.

## Probar la aplicación

Ingresar como administrativo y revisar las cinco pestañas.
En Datos básicos se pueden cargar primero el propietario, su propiedad y el inquilino.
Los registros nuevos quedan disponibles en los selectores del contrato.
El formulario Registrar contrato comienza vacío y se limpia después de guardar.

1. En Registrar contrato, elegir propiedad e inquilino, completar la vigencia,
   importe, comisión y condición de mora, y guardar.
2. En Cuotas y saldos, seleccionar una cuota con importe confirmado y usar
   Cobrar cuota seleccionada.
3. En Registrar pago, completar fecha, capital, mora y una referencia única.
   Guardar y comprobar el saldo actualizado.
4. Intentar cargar un contrato superpuesto o un pago mayor al saldo.
5. Seleccionar la primera cuota pendiente de un tramo y usar Confirmar ajuste
   del tramo. Ingresar el nuevo importe mensual y confirmar. Al volver a la
   consulta, las cuotas de ese tramo deben mostrar el nuevo importe.
6. Cerrar la aplicación e ingresar como martillero: sólo aparece la consulta.
7. Elegir una propiedad y volver a Todas las propiedades. El resumen debe
   corresponder a las filas visibles y la selección se conserva al actualizar.

Las fechas se escriben AAAA-MM-DD. Los importes admiten punto o coma decimal,
sin separadores de miles y con hasta dos decimales. La mora se informa manualmente.

## Pruebas automáticas

Usar una base nueva con sólo los datos iniciales y las variables configuradas:

```sh
bash compilar.sh
java -cp 'build/classes:lib/*' propiera.Pruebas
java -Djava.awt.headless=true -cp 'build/classes:lib/*' propiera.PruebasSwing
java -cp 'build/classes:lib/*' propiera.PruebasPoo
java -Djava.awt.headless=true -cp 'build/classes:lib/*' propiera.PruebasAjuste
java -Djava.awt.headless=true -cp 'build/classes:lib/*' propiera.PruebasFiltro
java -Djava.awt.headless=true -cp 'build/classes:lib/*' propiera.PruebasCatalogos
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
PruebasPoo verifica los dos tipos de periodicidad y el comportamiento común
sin necesitar conexión a MySQL.
PruebasAjuste se ejecuta después de Pruebas, una sola vez por base ficticia.
Comprueba tramos trimestrales, semestrales y finales incompletos, permisos,
concurrencia, cobros posteriores y reversión ante un error SQL. Para simular
ese error crea una restricción CHECK temporal y la retira al terminar.
PruebasFiltro no requiere MySQL. Comprueba coincidencias exactas, orden numérico,
recarga, conservación del filtro y actualización de una consulta vacía.

sql/03_consultas.sql contiene consultas de saldos, cobros y liquidaciones.
sql/04_abm_seguro.sql realiza un ABM sobre un registro temporal y revierte
la transacción. evidencia/ conserva los resultados de las pruebas.
sql/06_verificacion_persistencia.sql consulta las cuotas y los cobros del
escenario de presentación. Su resultado está en evidencia/consulta-presentacion.txt
y la captura correspondiente en evidencia/pantallas/09_consulta_mysql.png.

PruebasCatalogos comprueba altas, modificaciones, bajas lógicas, reactivaciones,
permisos, duplicados concurrentes, conservación del historial y formularios vacíos.
Para probar los archivos, usar otra base exclusiva con SQL 01, 02 y 05 y ejecutar:

```sh
java -Djava.awt.headless=true -cp 'build/classes:lib/*' propiera.PruebasExportacion
```

Esta prueba escribe y vuelve a leer TXT en una carpeta temporal; comprueba UTF-8,
totales, cuotas pendientes, autorización, reemplazo y errores de escritura.

Para verificar el lector de configuración sin usar MySQL:

```sh
java -cp 'build/classes' propiera.configuracion.PruebasConfiguracion
```

## Archivos

Las fuentes están en src/, las pruebas en tests/ y la base en sql/.
Los diagramas están en diagramas/ y los resultados de las pruebas en evidencia/.

El conector JDBC se descarga en lib/ sólo si falta. El repositorio
contiene las fuentes, los scripts SQL, las pruebas y los diagramas.
Las dependencias binarias, los archivos compilados y las credenciales
no forman parte del repositorio.

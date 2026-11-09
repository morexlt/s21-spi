package propiera;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import propiera.modelo.*;
import propiera.persistencia.BaseDatos;
import propiera.servicio.AlquilerService;
import propiera.servicio.CatalogoService;
import propiera.vista.PanelPersonas;
import propiera.vista.PanelPropiedades;
import propiera.vista.FormularioContrato;

public class PruebasCatalogos {
    private static final BaseDatos base = new BaseDatos();
    private static final CatalogoService servicio = new CatalogoService(base);
    private static final AlquilerService alquiler = new AlquilerService(base);
    private static final String marca = UUID.randomUUID().toString().substring(0, 8);
    private static int total;

    interface Accion { void ejecutar() throws Exception; }

    private static void comprobar(String caso, boolean resultado) {
        if (!resultado) throw new AssertionError(caso);
        System.out.println("PASS " + caso);
        total++;
    }

    private static void rechazar(String caso, String motivo, Accion accion) throws Exception {
        try { accion.ejecutar(); }
        catch (IllegalArgumentException error) {
            comprobar(caso, motivo.equals(error.getMessage()));
            return;
        }
        throw new AssertionError(caso + " debía rechazarse");
    }

    private static Persona persona(TipoPersona tipo, long id) throws SQLException {
        DatosCatalogo datos = servicio.consultar(1);
        return (tipo == TipoPersona.PROPIETARIO ? datos.propietarios() : datos.inquilinos())
            .stream().filter(p -> p.id() == id).findFirst().orElseThrow();
    }

    private static Propiedad propiedad(long id) throws SQLException {
        return servicio.consultar(1).propiedades().stream().filter(p -> p.id() == id).findFirst().orElseThrow();
    }

    private static AltaContrato contrato(long propiedad, long inquilino, int ano) {
        return new AltaContrato(propiedad, inquilino, LocalDate.of(ano, 1, 1), LocalDate.of(ano, 12, 31),
            new BigDecimal("400000.00"), new BigDecimal("5.00"), 3, 10, "Mora pactada en contrato");
    }

    public static void main(String[] args) throws Exception {
        long dueno = servicio.guardarPersona(TipoPersona.PROPIETARIO,
            new Persona(0, " D" + marca + " ", " Ana Muñoz ", " 299 5551234 ", true), 1);
        Persona p = persona(TipoPersona.PROPIETARIO, dueno);
        comprobar("CA01 alta de propietario y espacios", p.nombre().equals("Ana Muñoz") && p.documento().equals("D" + marca));
        rechazar("CA02 documento duplicado", "Ya existe una persona con ese documento. Revisá también los inactivos.",
            () -> servicio.guardarPersona(TipoPersona.PROPIETARIO, new Persona(0, "D" + marca, "Otro", "", true), 1));
        servicio.guardarPersona(TipoPersona.PROPIETARIO, new Persona(dueno, p.documento(), "Ana Muñoz Díaz", "299 5559999", true), 1);
        comprobar("CA03 modificación de propietario", persona(TipoPersona.PROPIETARIO, dueno).nombre().equals("Ana Muñoz Díaz"));
        long inmueble = servicio.guardarPropiedad(new Propiedad(0, " ab-" + marca + " ", "Rivadavia 325", dueno, "", true), 1);
        comprobar("CA04 alta de propiedad y código normalizado", propiedad(inmueble).codigo().equals("AB-" + marca.toUpperCase()));
        rechazar("CA05 código duplicado", "Ya existe una propiedad con ese código. Revisá también las inactivas.",
            () -> servicio.guardarPropiedad(new Propiedad(0, "ab-" + marca, "Otra", dueno, "", true), 1));
        long inquilino = servicio.guardarPersona(TipoPersona.INQUILINO, new Persona(0, "I" + marca, "Lucía Núñez", "", true), 1);
        servicio.guardarPersona(TipoPersona.INQUILINO, new Persona(inquilino, "I" + marca, "Lucía Núñez", "lucia@example.invalid", true), 1);
        comprobar("CA06 alta y modificación de inquilino", persona(TipoPersona.INQUILINO, inquilino).contacto().equals("lucia@example.invalid"));
        long otro = servicio.guardarPersona(TipoPersona.PROPIETARIO, new Persona(0, "O" + marca, "Diego Pérez", "", true), 1);
        servicio.guardarPropiedad(new Propiedad(inmueble, "AB-" + marca, "Rivadavia 325", otro, "", true), 1);
        comprobar("CA07 cambio de dueño antes de contratos", propiedad(inmueble).propietario() == otro);
        long contrato = alquiler.registrarContrato(contrato(inmueble, inquilino, 2026), 1);
        comprobar("CA08 contrato desde datos cargados por servicio", alquiler.consultarCuotas().stream().filter(q -> q.contrato() == contrato).count() == 12);
        rechazar("CA09 proteger titularidad histórica", "No se puede cambiar el propietario de una propiedad con contratos registrados",
            () -> servicio.guardarPropiedad(new Propiedad(inmueble, "AB-" + marca, "Rivadavia 325", dueno, "", true), 1));
        servicio.guardarPropiedad(new Propiedad(inmueble, "AB-" + marca, "Rivadavia 325 piso 1", otro, "", true), 1);
        comprobar("CA10 editar dirección conserva contratos", propiedad(inmueble).direccion().endsWith("piso 1")
            && alquiler.consultarCuotas().stream().filter(q -> q.contrato() == contrato).count() == 12);
        servicio.bajaPropiedad(inmueble, 1);
        comprobar("CA11 baja lógica de propiedad", !propiedad(inmueble).activo()
            && alquiler.propiedades().stream().noneMatch(o -> o.id() == inmueble));
        rechazar("CA12 impedir contrato con propiedad inactiva", "Propiedad o propietario inexistente o inactivo",
            () -> alquiler.registrarContrato(contrato(inmueble, inquilino, 2027), 1));
        servicio.guardarPropiedad(new Propiedad(inmueble, "AB-" + marca, "Rivadavia 325 piso 1", otro, "", true), 1);
        comprobar("CA13 reactivar propiedad", alquiler.propiedades().stream().anyMatch(o -> o.id() == inmueble));
        servicio.bajaPersona(TipoPersona.INQUILINO, inquilino, 1);
        rechazar("CA14 impedir contrato con inquilino inactivo", "Inquilino inexistente o inactivo",
            () -> alquiler.registrarContrato(contrato(inmueble, inquilino, 2027), 1));
        comprobar("CA15 conservar identidad y cuotas", !persona(TipoPersona.INQUILINO, inquilino).activo()
            && alquiler.inquilinos().stream().noneMatch(o -> o.id() == inquilino)
            && alquiler.consultarCuotas().stream().filter(q -> q.contrato() == contrato).count() == 12);
        servicio.guardarPersona(TipoPersona.INQUILINO, new Persona(inquilino, "I" + marca, "Lucía Núñez", "", true), 1);
        comprobar("CA16 reactivar inquilino", alquiler.inquilinos().stream().anyMatch(o -> o.id() == inquilino));
        servicio.bajaPersona(TipoPersona.PROPIETARIO, otro, 1);
        rechazar("CA17 propietario inactivo al dar de alta propiedad", "El propietario no existe o está inactivo",
            () -> servicio.guardarPropiedad(new Propiedad(0, "N-" + marca, "San Luis 145", otro, "", true), 1));
        comprobar("CA18 ocultar propiedades del dueño inactivo", alquiler.propiedades().stream().noneMatch(o -> o.id() == inmueble));
        Cuota cuota = alquiler.consultarCuotas().stream().filter(q -> q.contrato() == contrato).findFirst().orElseThrow();
        alquiler.registrarPago(cuota.id(), LocalDate.of(2026, 1, 10), new BigDecimal("100000.00"), BigDecimal.ZERO,
            "TRANSFERENCIA", "CA-" + marca, 1);
        comprobar("CA19 historial cobrable tras bajas", alquiler.consultarCuotas().stream().filter(q -> q.id() == cuota.id())
            .findFirst().orElseThrow().saldo().compareTo(new BigDecimal("300000")) == 0);
        servicio.guardarPersona(TipoPersona.PROPIETARIO, new Persona(otro, "O" + marca, "Diego Pérez", "", true), 1);
        comprobar("CA20 reactivar propietario", alquiler.propiedades().stream().anyMatch(o -> o.id() == inmueble));
        rechazar("CA21 documento obligatorio", "Completá el campo Documento",
            () -> servicio.guardarPersona(TipoPersona.INQUILINO, new Persona(0, " ", "Nombre", "", true), 1));
        rechazar("CA22 nombre extenso", "Nombre supera los 100 caracteres",
            () -> servicio.guardarPersona(TipoPersona.PROPIETARIO, new Persona(0, "X", "a".repeat(101), "", true), 1));
        rechazar("CA23 dirección obligatoria", "Completá el campo Dirección",
            () -> servicio.guardarPropiedad(new Propiedad(0, "X", "", dueno, "", true), 1));
        rechazar("CA24 propietario inexistente", "El propietario no existe o está inactivo",
            () -> servicio.guardarPropiedad(new Propiedad(0, "X", "Dirección", Long.MAX_VALUE, "", true), 1));
        rechazar("CA25 persona inexistente", "La persona ya no existe",
            () -> servicio.bajaPersona(TipoPersona.INQUILINO, Long.MAX_VALUE, 1));
        rechazar("CA26 propiedad inexistente", "La propiedad ya no existe", () -> servicio.bajaPropiedad(Long.MAX_VALUE, 1));
        String permiso = "El usuario no puede administrar los datos básicos";
        rechazar("CA27 Martillero no consulta catálogo", permiso, () -> servicio.consultar(2));
        rechazar("CA28 Martillero no guarda persona", permiso,
            () -> servicio.guardarPersona(TipoPersona.PROPIETARIO, new Persona(dueno, "D" + marca, "Nombre", "", true), 2));
        rechazar("CA29 Martillero no guarda propiedad", permiso,
            () -> servicio.guardarPropiedad(new Propiedad(inmueble, "AB-" + marca, "Dirección", otro, "", true), 2));
        rechazar("CA30 Martillero no da de baja persona", permiso, () -> servicio.bajaPersona(TipoPersona.INQUILINO, inquilino, 2));
        rechazar("CA31 Martillero no da de baja propiedad", permiso, () -> servicio.bajaPropiedad(inmueble, 2));
        comprobar("CA32 rechazos no alteran los datos", propiedad(inmueble).activo()
            && propiedad(inmueble).direccion().equals("Rivadavia 325 piso 1") && persona(TipoPersona.INQUILINO, inquilino).activo());
        probarDuplicadoConcurrente(dueno);
        SwingUtilities.invokeAndWait(PruebasCatalogos::probarPaneles);
        System.out.println("RESULTADO: " + total + " verificaciones de datos básicos aprobadas.");
    }

    private static void probarDuplicadoConcurrente(long dueno) throws Exception {
        var ejecutor = Executors.newFixedThreadPool(2);
        CountDownLatch inicio = new CountDownLatch(1);
        java.util.concurrent.Callable<Boolean> insertar = () -> {
            inicio.await();
            try {
                servicio.guardarPropiedad(new Propiedad(0, "CC-" + marca, "España 152", dueno, "", true), 1);
                return true;
            } catch (IllegalArgumentException error) {
                if (!error.getMessage().startsWith("Ya existe una propiedad")) throw error;
                return false;
            }
        };
        try {
            var uno = ejecutor.submit(insertar);
            var dos = ejecutor.submit(insertar);
            inicio.countDown();
            comprobar("CA33 duplicado concurrente sin alta residual", uno.get() != dos.get()
                && servicio.consultar(1).propiedades().stream().filter(p -> p.codigo().equals("CC-" + marca.toUpperCase())).count() == 1);
        } finally { ejecutor.shutdownNow(); }
    }

    private static Object campo(Object objeto, Class<?> clase, String nombre) {
        try {
            var f = clase.getDeclaredField(nombre);
            f.setAccessible(true);
            return f.get(objeto);
        } catch (Exception error) { throw new RuntimeException(error); }
    }

    private static void probarPaneles() {
        PanelPersonas panel = new PanelPersonas("Propietarios");
        panel.cargar(List.of(new Persona(10, "10", "Zulema", "", true), new Persona(2, "2", "Ana", "", false)));
        JTable tabla = (JTable) campo(panel, propiera.vista.PanelAbm.class, "tabla");
        tabla.getRowSorter().toggleSortOrder(2);
        tabla.setRowSelectionInterval(0, 0);
        comprobar("CA34 selección ordenada conserva ID y estado", panel.datos().id() == 2 && !panel.datos().activo() && !panel.baja().isEnabled());
        panel.limpiar();
        comprobar("CA35 nuevo limpia ID y campos", panel.datos().id() == 0 && panel.datos().documento().isEmpty() && panel.datos().activo());
        panel.ocupado(true);
        comprobar("CA36 formulario bloqueado durante operación", !panel.guardar().isEnabled() && !tabla.isEnabled()
            && !((JTextField) campo(panel, PanelPersonas.class, "nombre")).isEnabled());
        PanelPropiedades propiedades = new PanelPropiedades();
        propiedades.cargar(List.of(new Propiedad(3, "P3", "Dirección", 10, "Zulema", true)),
            List.of(new Persona(2, "2", "Ana", "", true), new Persona(10, "10", "Zulema", "", false)));
        JTable lista = (JTable) campo(propiedades, propiera.vista.PanelAbm.class, "tabla");
        lista.setRowSelectionInterval(0, 0);
        comprobar("CA37 dueño por ID aunque esté inactivo", propiedades.datos().propietario() == 10 && propiedades.datos().id() == 3);
        propiedades.limpiar();
        comprobar("CA38 alta exige selección de dueño", propiedades.datos().propietario() == 0 && propiedades.datos().id() == 0);
        FormularioContrato contrato = new FormularioContrato();
        contrato.cargarOpciones(List.of(new Opcion(1, "P-001")), List.of(new Opcion(1, "Ana")));
        comprobar("CA39 contrato nuevo sin valores precargados", contrato.propiedad() == null && contrato.inquilino() == null
            && contrato.inicio().isEmpty() && contrato.fin().isEmpty() && contrato.importe().isEmpty()
            && contrato.comision().isEmpty() && contrato.vencimiento().isEmpty() && contrato.mora().isEmpty());
        try {
            contrato.ajusteMeses();
            throw new AssertionError("No se eligió periodicidad");
        } catch (IllegalArgumentException error) {
            comprobar("CA40 periodicidad requiere elección", error.getMessage().equals("Seleccioná la periodicidad del ajuste"));
        }
        for (var componente : contrato.getComponents()) {
            if (componente instanceof JTextField campo) campo.setText("dato");
            if (componente instanceof javax.swing.JComboBox<?> combo) combo.setSelectedIndex(0);
        }
        contrato.limpiar();
        contrato.cargarOpciones(List.of(new Opcion(1, "P-001")), List.of(new Opcion(1, "Ana")));
        comprobar("CA41 limpieza conserva formulario vacío tras recarga", contrato.propiedad() == null
            && contrato.inquilino() == null && contrato.inicio().isEmpty() && contrato.fin().isEmpty()
            && contrato.importe().isEmpty() && contrato.comision().isEmpty() && contrato.vencimiento().isEmpty() && contrato.mora().isEmpty());
    }
}

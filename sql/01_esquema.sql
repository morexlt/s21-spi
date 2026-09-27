-- Propiera AP2. MySQL 8.4. Ejecutar en una base NUEVA y exclusiva de pruebas.
SET NAMES utf8mb4;
CREATE TABLE usuario (
 id BIGINT PRIMARY KEY AUTO_INCREMENT,
 nombre VARCHAR(100) NOT NULL,
 rol ENUM('ADMINISTRATIVO','MARTILLERO') NOT NULL,
 activo BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB;
CREATE TABLE propietario (
 id BIGINT PRIMARY KEY AUTO_INCREMENT,
 documento VARCHAR(20) NOT NULL UNIQUE,
 nombre VARCHAR(100) NOT NULL,
 contacto VARCHAR(150), activo BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB;
CREATE TABLE inquilino (
 id BIGINT PRIMARY KEY AUTO_INCREMENT,
 documento VARCHAR(20) NOT NULL UNIQUE,
 nombre VARCHAR(100) NOT NULL,
 contacto VARCHAR(150), activo BOOLEAN NOT NULL DEFAULT TRUE
) ENGINE=InnoDB;
CREATE TABLE propiedad (
 id BIGINT PRIMARY KEY AUTO_INCREMENT,
 codigo VARCHAR(30) NOT NULL UNIQUE,
 direccion VARCHAR(200) NOT NULL,
 propietario_id BIGINT NOT NULL,
 activo BOOLEAN NOT NULL DEFAULT TRUE,
 FOREIGN KEY (propietario_id) REFERENCES propietario(id)
) ENGINE=InnoDB;
CREATE TABLE contrato (
 id BIGINT PRIMARY KEY AUTO_INCREMENT,
 propiedad_id BIGINT NOT NULL, inquilino_id BIGINT NOT NULL,
 inicio DATE NOT NULL, fin DATE NOT NULL,
 importe_inicial DECIMAL(12,2) NOT NULL,
 comision_pct DECIMAL(5,2) NOT NULL,
 ajuste_meses INT NOT NULL, dia_vencimiento INT NOT NULL,
 regla_mora VARCHAR(250) NOT NULL,
 creado_por BIGINT NOT NULL, creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 FOREIGN KEY (propiedad_id) REFERENCES propiedad(id),
 FOREIGN KEY (inquilino_id) REFERENCES inquilino(id),
 FOREIGN KEY (creado_por) REFERENCES usuario(id),
 CHECK (fin >= inicio), CHECK (importe_inicial > 0),
 CHECK (comision_pct BETWEEN 0 AND 100), CHECK (ajuste_meses IN (3,6)),
 CHECK (dia_vencimiento BETWEEN 1 AND 28),
 INDEX ix_contrato_propiedad_fechas (propiedad_id,inicio,fin)
) ENGINE=InnoDB;
CREATE TABLE cuota (
 id BIGINT PRIMARY KEY AUTO_INCREMENT,
 contrato_id BIGINT NOT NULL, periodo DATE NOT NULL, vencimiento DATE NOT NULL,
 importe DECIMAL(12,2),
 FOREIGN KEY (contrato_id) REFERENCES contrato(id),
 UNIQUE (contrato_id,periodo),
 CHECK (DAYOFMONTH(periodo)=1), CHECK (importe IS NULL OR importe > 0),
 INDEX ix_cuota_vencimiento(vencimiento)
) ENGINE=InnoDB;
CREATE TABLE pago (
 id BIGINT PRIMARY KEY AUTO_INCREMENT,
 cuota_id BIGINT NOT NULL, fecha DATE NOT NULL,
 capital DECIMAL(12,2) NOT NULL, mora DECIMAL(12,2) NOT NULL DEFAULT 0,
 medio ENUM('TRANSFERENCIA','EFECTIVO') NOT NULL,
 referencia VARCHAR(80) NOT NULL UNIQUE,
 registrado_por BIGINT NOT NULL, registrado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
 FOREIGN KEY (cuota_id) REFERENCES cuota(id),
 FOREIGN KEY (registrado_por) REFERENCES usuario(id),
 CHECK (capital>0), CHECK (mora>=0), INDEX ix_pago_fecha(fecha)
) ENGINE=InnoDB;
-- Entidades diseñadas para las próximas iteraciones; sin pantallas en AP2.
CREATE TABLE gasto (
 id BIGINT PRIMARY KEY AUTO_INCREMENT,
 contrato_id BIGINT NOT NULL, periodo DATE NOT NULL,
 fecha DATE NOT NULL, concepto VARCHAR(200) NOT NULL,
 importe DECIMAL(12,2) NOT NULL, descontable BOOLEAN NOT NULL,
 registrado_por BIGINT NOT NULL,
 FOREIGN KEY (contrato_id) REFERENCES contrato(id),
 FOREIGN KEY (registrado_por) REFERENCES usuario(id),
 CHECK (importe>0), CHECK (DAYOFMONTH(periodo)=1)
) ENGINE=InnoDB;
CREATE TABLE liquidacion (
 id BIGINT PRIMARY KEY AUTO_INCREMENT,
 propietario_id BIGINT NOT NULL, periodo DATE NOT NULL,
 estado ENUM('BORRADOR','APROBADA','PAGADA') NOT NULL DEFAULT 'BORRADOR',
 creado_por BIGINT NOT NULL, aprobado_por BIGINT, pagado_en DATE,
 FOREIGN KEY (propietario_id) REFERENCES propietario(id),
 FOREIGN KEY (creado_por) REFERENCES usuario(id),
 FOREIGN KEY (aprobado_por) REFERENCES usuario(id),
 UNIQUE (propietario_id,periodo), CHECK (DAYOFMONTH(periodo)=1),
 CHECK (estado='BORRADOR' OR aprobado_por IS NOT NULL),
 CHECK ((estado='PAGADA' AND pagado_en IS NOT NULL) OR (estado<>'PAGADA' AND pagado_en IS NULL))
) ENGINE=InnoDB;
CREATE TABLE liquidacion_cobro (
 liquidacion_id BIGINT NOT NULL, pago_id BIGINT NOT NULL,
 capital_asignado DECIMAL(12,2) NOT NULL, mora_asignada DECIMAL(12,2) NOT NULL,
 comision DECIMAL(12,2) NOT NULL,
 PRIMARY KEY (pago_id),
 FOREIGN KEY (liquidacion_id) REFERENCES liquidacion(id),
 FOREIGN KEY (pago_id) REFERENCES pago(id),
 CHECK (capital_asignado>=0 AND mora_asignada>=0 AND comision>=0)
) ENGINE=InnoDB;
CREATE TABLE liquidacion_gasto (
 liquidacion_id BIGINT NOT NULL, gasto_id BIGINT NOT NULL,
 importe_asignado DECIMAL(12,2) NOT NULL,
 PRIMARY KEY (gasto_id),
 FOREIGN KEY (liquidacion_id) REFERENCES liquidacion(id),
 FOREIGN KEY (gasto_id) REFERENCES gasto(id), CHECK (importe_asignado>0)
) ENGINE=InnoDB;

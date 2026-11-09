-- Datos ficticios, no pertenecen a clientes de Propiera.
SET NAMES utf8mb4;
INSERT INTO usuario(id,nombre,rol) VALUES
 (1,'Administración','ADMINISTRATIVO'),(2,'Martillero','MARTILLERO');
INSERT INTO propietario(id,documento,nombre,contacto) VALUES
 (1,'27456123','Ana Muñoz','ana@example.invalid'),
 (2,'23147865','José Pérez','jose@example.invalid');
INSERT INTO inquilino(id,documento,nombre,contacto) VALUES
 (1,'34678215','Lucía Núñez','lucia@example.invalid'),
 (2,'32145987','Martín Aguirre','martin@example.invalid'),
 (3,'37521964','Carolina Fernández','carolina@example.invalid');
INSERT INTO propiedad(id,codigo,direccion,propietario_id) VALUES
 (1,'P-001','Roca 425',1),
 (2,'P-002','Mitre 680, dpto. 2B',1),
 (3,'P-003','San Martín 1240',2);

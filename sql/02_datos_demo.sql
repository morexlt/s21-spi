-- Datos ficticios, no pertenecen a clientes de Propiera.
SET NAMES utf8mb4;
INSERT INTO usuario(id,nombre,rol) VALUES
 (1,'Administrativo de prueba','ADMINISTRATIVO'),(2,'Martillero de prueba','MARTILLERO');
INSERT INTO propietario(id,documento,nombre,contacto) VALUES
 (1,'DEMO-P1','Ana Muñoz','ana@example.invalid'),
 (2,'DEMO-P2','José Pérez','jose@example.invalid');
INSERT INTO inquilino(id,documento,nombre,contacto) VALUES
 (1,'DEMO-I1','Lucía Núñez','lucia@example.invalid');
INSERT INTO propiedad(id,codigo,direccion,propietario_id) VALUES
 (1,'DEMO-01','Calle de prueba 100, Neuquén',1),
 (2,'DEMO-02','Calle de prueba 200, Neuquén',1);

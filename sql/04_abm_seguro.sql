-- Demostración de INSERT, UPDATE, SELECT y DELETE: sólo un registro ficticio
-- sin relaciones. La transacción se revierte. No borra cobros ni contratos.
START TRANSACTION;
INSERT INTO propietario(documento,nombre) VALUES ('DEMO-ABM-TEMP','Propietario temporal');
SET @id_temporal=LAST_INSERT_ID();
UPDATE propietario SET contacto='demo@example.invalid' WHERE id=@id_temporal;
SELECT id,documento,nombre,contacto FROM propietario WHERE id=@id_temporal;
DELETE FROM propietario WHERE id=@id_temporal;
SELECT COUNT(*) AS restantes FROM propietario WHERE id=@id_temporal;
ROLLBACK;

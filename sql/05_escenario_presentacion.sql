-- Escenario ficticio para recorrer las pantallas y filtrar tres propiedades de Neuquén.
-- Ejecutar después de 01 y 02, en una base nueva, sin contratos ni pagos.
-- Este archivo no se utiliza para las pruebas automatizadas de negocio.
SET NAMES utf8mb4;
START TRANSACTION;
INSERT INTO contrato(id,propiedad_id,inquilino_id,inicio,fin,importe_inicial,
    comision_pct,ajuste_meses,dia_vencimiento,regla_mora,creado_por) VALUES
 (1,1,1,'2026-01-01','2026-12-31',300000,5,3,10,'Mora según lo acordado en el contrato',1),
 (2,2,2,'2026-01-01','2026-12-31',450000,5,6,10,'Mora según lo acordado en el contrato',1),
 (3,3,3,'2026-03-01','2027-02-28',620000,6,3,5,'Mora según lo acordado en el contrato',1);

INSERT INTO cuota(contrato_id,periodo,vencimiento,importe)
WITH RECURSIVE meses AS (
    SELECT 0 AS numero
    UNION ALL
    SELECT numero + 1 FROM meses WHERE numero < 11
)
SELECT co.id, DATE_ADD(co.inicio, INTERVAL meses.numero MONTH),
       DATE_ADD(DATE_ADD(co.inicio, INTERVAL meses.numero MONTH), INTERVAL (co.dia_vencimiento - 1) DAY),
       CASE WHEN meses.numero < co.ajuste_meses THEN co.importe_inicial ELSE NULL END
FROM contrato co CROSS JOIN meses
WHERE co.id IN (1,2,3)
ORDER BY co.id, meses.numero;

UPDATE cuota SET importe=360000
WHERE contrato_id=1 AND periodo BETWEEN '2026-04-01' AND '2026-06-01';

INSERT INTO pago(cuota_id,fecha,capital,mora,medio,referencia,registrado_por)
SELECT id,'2026-01-10',100000,0,'TRANSFERENCIA','ALQ-202601-001',1
FROM cuota WHERE contrato_id=1 AND periodo='2026-01-01';
INSERT INTO pago(cuota_id,fecha,capital,mora,medio,referencia,registrado_por)
SELECT id,'2026-01-10',450000,1500,'TRANSFERENCIA','ALQ-202601-002',1
FROM cuota WHERE contrato_id=2 AND periodo='2026-01-01';
COMMIT;

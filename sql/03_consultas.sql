-- Consultas no destructivas. Fechas de ejemplo; parametrizar en la aplicación.
-- Q1: cuotas, saldo de capital y estado (no se almacena un estado redundante).
SELECT c.id,c.periodo,c.vencimiento,c.importe,
       c.importe-COALESCE(p.cobrado,0) AS saldo,
       CASE WHEN c.importe IS NULL THEN 'PENDIENTE_AJUSTE'
            WHEN c.importe=COALESCE(p.cobrado,0) THEN 'PAGADA'
            WHEN c.vencimiento<CURRENT_DATE THEN 'VENCIDA'
            WHEN COALESCE(p.cobrado,0)>0 THEN 'PARCIAL'
            ELSE 'PENDIENTE' END AS estado
FROM cuota c LEFT JOIN
 (SELECT cuota_id,SUM(capital) cobrado FROM pago GROUP BY cuota_id) p
 ON p.cuota_id=c.id ORDER BY c.periodo,c.id;

-- Q2: cobros consolidados por dueño; criterio de caja por fecha del cobro.
-- Comisión propuesta: sobre capital cobrado, no sobre mora; pendiente validar.
SELECT inmueble.propietario_id,pr.nombre,
 SUM(p.capital) AS capital,
 SUM(p.mora) AS mora,
 SUM(ROUND(p.capital*co.comision_pct/100,2)) AS comision
FROM pago p JOIN cuota c ON c.id=p.cuota_id
JOIN contrato co ON co.id=c.contrato_id
JOIN propiedad inmueble ON inmueble.id=co.propiedad_id
JOIN propietario pr ON pr.id=inmueble.propietario_id
WHERE p.fecha>='2026-09-01' AND p.fecha<'2026-10-01'
GROUP BY inmueble.propietario_id,pr.nombre;

-- Q3: gastos descontables por propietario. Agregación separada para no
-- multiplicar importes al combinar varios pagos con varios gastos.
SELECT inmueble.propietario_id, SUM(g.importe) AS gastos
FROM gasto g JOIN contrato co ON co.id=g.contrato_id
JOIN propiedad inmueble ON inmueble.id=co.propiedad_id
WHERE g.periodo='2026-09-01' AND g.descontable=TRUE
GROUP BY inmueble.propietario_id;

-- Q4: neto de una liquidación ya materializada, sin producto cartesiano.
SELECT l.id,l.propietario_id,l.periodo,l.estado,
 COALESCE(c.ingresos,0)-COALESCE(c.comision,0)-COALESCE(g.gastos,0) AS neto
FROM liquidacion l
LEFT JOIN (SELECT liquidacion_id,SUM(capital_asignado+mora_asignada) ingresos,
 SUM(comision) comision FROM liquidacion_cobro GROUP BY liquidacion_id) c
 ON c.liquidacion_id=l.id
LEFT JOIN (SELECT liquidacion_id,SUM(importe_asignado) gastos
 FROM liquidacion_gasto GROUP BY liquidacion_id) g ON g.liquidacion_id=l.id;

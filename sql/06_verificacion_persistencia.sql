-- Consulta de lectura sobre el escenario inicial de presentación (script 05).
-- Relaciona la propiedad, sus cuotas y los cobros registrados.
SELECT p.codigo, q.periodo, q.importe,
       COALESCE(SUM(pa.capital), 0) AS cobrado,
       q.importe - COALESCE(SUM(pa.capital), 0) AS saldo
FROM cuota q JOIN contrato c ON c.id = q.contrato_id
JOIN propiedad p ON p.id = c.propiedad_id
LEFT JOIN pago pa ON pa.cuota_id = q.id
WHERE c.id = 1 AND q.periodo <= '2026-07-01'
GROUP BY q.id, p.codigo, q.periodo, q.importe
ORDER BY q.periodo;

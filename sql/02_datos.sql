-- =====================================================================
-- PF-02 - Órdenes de reparación de bicicletas
-- Datos de prueba (ejecutar después de 01_estructura.sql)
-- 5 servicios y 11 órdenes
-- =====================================================================

-- Servicios
INSERT INTO servicio_taller (nombre, descripcion, costo_base) VALUES
 ('Cambio de frenos',      'Reemplazo de pastillas y ajuste',          95.00),
 ('Afinación general',     'Revisión completa de la bicicleta',       120.00),
 ('Cambio de llanta',      'Reemplazo de llanta y cámara',             60.00),
 ('Lubricación de cadena', 'Limpieza y lubricación de la transmisión', 25.00),
 ('Ajuste de cambios',     'Calibración de cambios y cables',          45.00)
ON CONFLICT (nombre) DO NOTHING;

-- Órdenes (costo_estimado = costo base del servicio + recargo)
INSERT INTO orden_reparacion
    (cliente, bicicleta, fecha, estado, observaciones, recargo, costo_estimado, servicio_id)
SELECT v.cliente, v.bicicleta, v.fecha::date, v.estado, v.obs, v.recargo,
       s.costo_base + v.recargo, s.id
FROM (VALUES
 ('Carlos Méndez',  'Trek Marlin 5',          '2026-10-20', 'PENDIENTE',  'Frenos hacen ruido',          0, 'Cambio de frenos'),
 ('María López',    'Specialized Rockhopper', '2026-10-21', 'EN_PROCESO', 'Revisión completa',          15, 'Afinación general'),
 ('José Pérez',     'GW Zaanan',              '2026-10-22', 'TERMINADA',  'Llanta trasera ponchada',     0, 'Cambio de llanta'),
 ('Ana Gómez',      'Giant Talon',            '2026-10-23', 'PENDIENTE',  'Cadena seca',                 0, 'Lubricación de cadena'),
 ('Carlos Méndez',  'Trek Marlin 5',          '2026-10-24', 'TERMINADA',  'Cambio de cables',           10, 'Ajuste de cambios'),
 ('Luis Ramírez',   'Scott Aspect',           '2026-10-25', 'EN_PROCESO', 'Revisión antes de viaje',    20, 'Afinación general'),
 ('María López',    'Specialized Rockhopper', '2026-10-26', 'PENDIENTE',  'Llanta delantera desgastada', 0, 'Cambio de llanta'),
 ('Sofía Castillo', 'Cannondale Trail',       '2026-10-27', 'PENDIENTE',  'Cambios saltan',              5, 'Ajuste de cambios'),
 ('Pedro Alvarado', 'Orbea Onna',             '2026-10-28', 'TERMINADA',  'Mantenimiento anual',        25, 'Afinación general'),
 ('Ana Gómez',      'Giant Talon',            '2026-10-29', 'EN_PROCESO', 'Frenos traseros',             0, 'Cambio de frenos'),
 ('Diego Morales',  'Merida Big Nine',        '2026-10-30', 'PENDIENTE',  'Limpieza de transmisión',     0, 'Lubricación de cadena')
) AS v(cliente, bicicleta, fecha, estado, obs, recargo, servicio)
JOIN servicio_taller s ON s.nombre = v.servicio;

-- Verificación
SELECT * FROM servicio_taller ORDER BY id;
SELECT * FROM orden_reparacion ORDER BY id;

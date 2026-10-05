/* ============================================================
   SIGELAB - DATOS MÍNIMOS DE PRUEBA
   Ejecutar después de 01_esquema.sql
   ============================================================

   USUARIOS DE PRUEBA

       usuario      contraseña        rol         estado
       ---------    --------------    ---------   --------
       admin        Admin2026!        ADMIN       ACTIVO
       encargado    Encargado2026!    ENCARGADO   ACTIVO
       docente      Docente2026!      DOCENTE     ACTIVO
       inactivo     Inactivo2026!     ENCARGADO   INACTIVO

   El usuario "inactivo" existe para probar la regla RN-01.
   Las contraseñas se guardan como hash BCrypt.

   Mientras exista el usuario de prueba quemado en
   UsuarioService.java, "admin" también entra con la
   contraseña "admin".
   ============================================================ */

USE SigelabDB;
GO


/* ============================================================
   ROLES
   ============================================================ */

INSERT INTO Rol (nombre, descripcion)
VALUES
    ('ADMIN',     N'Acceso completo al sistema'),
    ('ENCARGADO', N'Operación cotidiana: equipos, préstamos, devoluciones y mantenimiento'),
    ('DOCENTE',   N'Consultas');
GO


/* ============================================================
   USUARIOS
   ============================================================ */

INSERT INTO Usuario (nombre, apellido, usuario, password_hash, correo, id_rol, estado)
VALUES
    (N'Ana', N'Administradora', 'admin',
     '$2a$10$QLAKOD43wDccRmLwCkUzPOXRauPyBAgHZRys/gHqKJqxLwAcdrZ2u',
     'admin@sigelab.edu.gt',
     (SELECT id_rol FROM Rol WHERE nombre = 'ADMIN'), 'ACTIVO'),

    (N'Erick', N'Encargado', 'encargado',
     '$2a$10$jO0Wh1wCFWwxtZtS6nDfaek9IrECINUP6laKbKEOHOAQDF/NqarCm',
     'encargado@sigelab.edu.gt',
     (SELECT id_rol FROM Rol WHERE nombre = 'ENCARGADO'), 'ACTIVO'),

    (N'Diana', N'Docente', 'docente',
     '$2a$10$QdxVfXGFZxIABZhfRiOqTO/nQRe24agT1/m5csSrTyVRz09AJ.be2',
     'docente@sigelab.edu.gt',
     (SELECT id_rol FROM Rol WHERE nombre = 'DOCENTE'), 'ACTIVO'),

    (N'Iván', N'Inactivo', 'inactivo',
     '$2a$10$d1RDOSkIjLBRPJN4mFmJaeAglW6DHegLOnwhqBFDd8CBvOT6LqI/K',
     'inactivo@sigelab.edu.gt',
     (SELECT id_rol FROM Rol WHERE nombre = 'ENCARGADO'), 'INACTIVO');
GO


/* ============================================================
   LABORATORIOS
   ============================================================ */

INSERT INTO Laboratorio (codigo, nombre, ubicacion, capacidad)
VALUES
    ('LAB-01', N'Laboratorio de Computación', N'Edificio A, nivel 2', 30),
    ('LAB-02', N'Laboratorio de Redes',       N'Edificio A, nivel 3', 24),
    ('LAB-03', N'Laboratorio de Electrónica', N'Edificio B, nivel 1', 20);
GO


/* ============================================================
   TIPOS DE EQUIPO
   ============================================================ */

INSERT INTO TipoEquipo (nombre, descripcion)
VALUES
    (N'Laptop',          N'Computadoras portátiles'),
    (N'Cámara',          N'Cámaras fotográficas y de video'),
    (N'Proyector',       N'Proyectores multimedia'),
    (N'Router',          N'Equipos de enrutamiento'),
    (N'Switch',          N'Equipos de conmutación'),
    (N'Tablet',          N'Tabletas'),
    (N'Monitor',         N'Pantallas y monitores'),
    (N'Kit electrónico', N'Kits de práctica de electrónica');
GO


/* ============================================================
   EQUIPOS
   ------------------------------------------------------------
   Todos inician DISPONIBLE: los demás estados deben ser
   consecuencia de préstamos, devoluciones y mantenimientos
   hechos desde el sistema.

   CAM-001 es el equipo del escenario de defensa del proyecto.
   ============================================================ */

INSERT INTO Equipo (codigo_inventario, nombre, id_tipo_equipo, marca, modelo, numero_serie, id_laboratorio)
VALUES
    ('CAM-001', N'Cámara réflex',
     (SELECT id_tipo_equipo FROM TipoEquipo WHERE nombre = N'Cámara'),
     N'Canon', N'EOS Rebel T7', 'CN-T7-000101',
     (SELECT id_laboratorio FROM Laboratorio WHERE codigo = 'LAB-01')),

    ('CAM-002', N'Cámara web',
     (SELECT id_tipo_equipo FROM TipoEquipo WHERE nombre = N'Cámara'),
     N'Logitech', N'C920', 'LG-C920-000102',
     (SELECT id_laboratorio FROM Laboratorio WHERE codigo = 'LAB-01')),

    ('LAP-001', N'Laptop de préstamo 1',
     (SELECT id_tipo_equipo FROM TipoEquipo WHERE nombre = N'Laptop'),
     N'Dell', N'Latitude 5440', 'DL-5440-000201',
     (SELECT id_laboratorio FROM Laboratorio WHERE codigo = 'LAB-01')),

    ('LAP-002', N'Laptop de préstamo 2',
     (SELECT id_tipo_equipo FROM TipoEquipo WHERE nombre = N'Laptop'),
     N'Lenovo', N'ThinkPad E14', 'LN-E14-000202',
     (SELECT id_laboratorio FROM Laboratorio WHERE codigo = 'LAB-01')),

    ('PRO-001', N'Proyector portátil',
     (SELECT id_tipo_equipo FROM TipoEquipo WHERE nombre = N'Proyector'),
     N'Epson', N'PowerLite X49', 'EP-X49-000301',
     (SELECT id_laboratorio FROM Laboratorio WHERE codigo = 'LAB-01')),

    ('ROU-001', N'Router de práctica',
     (SELECT id_tipo_equipo FROM TipoEquipo WHERE nombre = N'Router'),
     N'Cisco', N'ISR 4221', 'CS-4221-000401',
     (SELECT id_laboratorio FROM Laboratorio WHERE codigo = 'LAB-02')),

    ('SWI-001', N'Switch de práctica',
     (SELECT id_tipo_equipo FROM TipoEquipo WHERE nombre = N'Switch'),
     N'Cisco', N'Catalyst 2960', 'CS-2960-000501',
     (SELECT id_laboratorio FROM Laboratorio WHERE codigo = 'LAB-02')),

    ('TAB-001', N'Tablet de préstamo',
     (SELECT id_tipo_equipo FROM TipoEquipo WHERE nombre = N'Tablet'),
     N'Samsung', N'Galaxy Tab A9', 'SM-A9-000601',
     (SELECT id_laboratorio FROM Laboratorio WHERE codigo = 'LAB-02')),

    ('MON-001', N'Monitor 24 pulgadas',
     (SELECT id_tipo_equipo FROM TipoEquipo WHERE nombre = N'Monitor'),
     N'LG', N'24MP400', 'LG-24MP-000701',
     (SELECT id_laboratorio FROM Laboratorio WHERE codigo = 'LAB-03')),

    ('KIT-001', N'Kit Arduino',
     (SELECT id_tipo_equipo FROM TipoEquipo WHERE nombre = N'Kit electrónico'),
     N'Arduino', N'Starter Kit UNO R3', NULL,
     (SELECT id_laboratorio FROM Laboratorio WHERE codigo = 'LAB-03'));
GO


/* ============================================================
   COMPROBACIÓN
   ============================================================ */

SELECT 'Rol' AS tabla, COUNT(*) AS filas FROM Rol
UNION ALL SELECT 'Usuario', COUNT(*) FROM Usuario
UNION ALL SELECT 'Laboratorio', COUNT(*) FROM Laboratorio
UNION ALL SELECT 'TipoEquipo', COUNT(*) FROM TipoEquipo
UNION ALL SELECT 'Equipo', COUNT(*) FROM Equipo;
GO

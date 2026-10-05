/* ============================================================
   PROGRAMACIÓN II - PROYECTO FINAL
   SIGELAB - Sistema de Gestión y Control de Laboratorios
   Base de datos: SigelabDB
   Motor: Microsoft SQL Server
   ============================================================ */


/* ============================================================
   OPCIONAL - SOLO SI QUIERES BORRAR Y RECREAR TODO
   ============================================================ */

/*
USE master;
GO

IF DB_ID('SigelabDB') IS NOT NULL
BEGIN
    ALTER DATABASE SigelabDB
    SET SINGLE_USER WITH ROLLBACK IMMEDIATE;

    DROP DATABASE SigelabDB;
END;
GO
*/


/* ============================================================
   1. CREAR BASE DE DATOS
   ============================================================ */

IF DB_ID('SigelabDB') IS NULL
BEGIN
    CREATE DATABASE SigelabDB;
END;
GO

USE SigelabDB;
GO


/* ============================================================
   2. ROL
   ------------------------------------------------------------
   nombre:
       ADMIN
       ENCARGADO
       DOCENTE
   ============================================================ */

CREATE TABLE Rol
(
    id_rol INT IDENTITY(1,1) NOT NULL,

    nombre VARCHAR(20) NOT NULL,
    descripcion NVARCHAR(200) NULL,

    CONSTRAINT PK_Rol
        PRIMARY KEY (id_rol),

    CONSTRAINT UQ_Rol_Nombre
        UNIQUE (nombre),

    CONSTRAINT CK_Rol_Nombre
        CHECK
        (
            nombre IN
            (
                'ADMIN',
                'ENCARGADO',
                'DOCENTE'
            )
        )
);
GO


/* ============================================================
   3. USUARIO
   ------------------------------------------------------------
   estado:
       ACTIVO
       INACTIVO

   La contraseña nunca se guarda en texto plano:
   password_hash contiene un hash BCrypt.
   ============================================================ */

CREATE TABLE Usuario
(
    id_usuario INT IDENTITY(1,1) NOT NULL,

    nombre NVARCHAR(100) NOT NULL,
    apellido NVARCHAR(100) NOT NULL,

    usuario VARCHAR(50) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    correo VARCHAR(150) NOT NULL,

    id_rol INT NOT NULL,

    estado VARCHAR(10) NOT NULL
        CONSTRAINT DF_Usuario_Estado DEFAULT 'ACTIVO',

    fecha_creacion DATETIME2(0) NOT NULL
        CONSTRAINT DF_Usuario_FechaCreacion
        DEFAULT SYSDATETIME(),

    CONSTRAINT PK_Usuario
        PRIMARY KEY (id_usuario),

    CONSTRAINT UQ_Usuario_Usuario
        UNIQUE (usuario),

    CONSTRAINT UQ_Usuario_Correo
        UNIQUE (correo),

    CONSTRAINT FK_Usuario_Rol
        FOREIGN KEY (id_rol)
        REFERENCES Rol(id_rol),

    CONSTRAINT CK_Usuario_Estado
        CHECK (estado IN ('ACTIVO', 'INACTIVO'))
);
GO


/* ============================================================
   4. LABORATORIO
   ============================================================ */

CREATE TABLE Laboratorio
(
    id_laboratorio INT IDENTITY(1,1) NOT NULL,

    codigo VARCHAR(20) NOT NULL,
    nombre NVARCHAR(100) NOT NULL,
    ubicacion NVARCHAR(200) NOT NULL,
    capacidad INT NOT NULL,

    estado VARCHAR(10) NOT NULL
        CONSTRAINT DF_Laboratorio_Estado DEFAULT 'ACTIVO',

    CONSTRAINT PK_Laboratorio
        PRIMARY KEY (id_laboratorio),

    CONSTRAINT UQ_Laboratorio_Codigo
        UNIQUE (codigo),

    CONSTRAINT CK_Laboratorio_Capacidad
        CHECK (capacidad > 0),

    CONSTRAINT CK_Laboratorio_Estado
        CHECK (estado IN ('ACTIVO', 'INACTIVO'))
);
GO


/* ============================================================
   5. TIPO DE EQUIPO
   ============================================================ */

CREATE TABLE TipoEquipo
(
    id_tipo_equipo INT IDENTITY(1,1) NOT NULL,

    nombre NVARCHAR(60) NOT NULL,
    descripcion NVARCHAR(200) NULL,

    estado VARCHAR(10) NOT NULL
        CONSTRAINT DF_TipoEquipo_Estado DEFAULT 'ACTIVO',

    CONSTRAINT PK_TipoEquipo
        PRIMARY KEY (id_tipo_equipo),

    CONSTRAINT UQ_TipoEquipo_Nombre
        UNIQUE (nombre),

    CONSTRAINT CK_TipoEquipo_Estado
        CHECK (estado IN ('ACTIVO', 'INACTIVO'))
);
GO


/* ============================================================
   6. EQUIPO
   ------------------------------------------------------------
   estado:
       DISPONIBLE
       PRESTADO
       DANADO
       MANTENIMIENTO
       BAJA

   Se escribe DANADO (sin ñ) para que coincida exactamente
   con el enum EstadoEquipo de Java.
   ============================================================ */

CREATE TABLE Equipo
(
    id_equipo INT IDENTITY(1,1) NOT NULL,

    codigo_inventario VARCHAR(30) NOT NULL,
    nombre NVARCHAR(120) NOT NULL,

    id_tipo_equipo INT NOT NULL,

    marca NVARCHAR(60) NOT NULL,
    modelo NVARCHAR(60) NOT NULL,
    numero_serie VARCHAR(60) NULL,

    id_laboratorio INT NOT NULL,

    estado VARCHAR(15) NOT NULL
        CONSTRAINT DF_Equipo_Estado DEFAULT 'DISPONIBLE',

    observaciones NVARCHAR(500) NULL,

    CONSTRAINT PK_Equipo
        PRIMARY KEY (id_equipo),

    CONSTRAINT UQ_Equipo_CodigoInventario
        UNIQUE (codigo_inventario),

    CONSTRAINT FK_Equipo_TipoEquipo
        FOREIGN KEY (id_tipo_equipo)
        REFERENCES TipoEquipo(id_tipo_equipo),

    CONSTRAINT FK_Equipo_Laboratorio
        FOREIGN KEY (id_laboratorio)
        REFERENCES Laboratorio(id_laboratorio),

    CONSTRAINT CK_Equipo_Estado
        CHECK
        (
            estado IN
            (
                'DISPONIBLE',
                'PRESTADO',
                'DANADO',
                'MANTENIMIENTO',
                'BAJA'
            )
        )
);
GO


/* ============================================================
   7. PRÉSTAMO (ENCABEZADO)
   ------------------------------------------------------------
   estado:
       ACTIVO      - tiene al menos un equipo pendiente
       FINALIZADO  - todos sus equipos fueron devueltos

   VENCIDO no se guarda: es un préstamo ACTIVO cuya
   fecha_esperada_devolucion ya pasó. Se calcula al consultar.
   ============================================================ */

CREATE TABLE Prestamo
(
    id_prestamo INT IDENTITY(1,1) NOT NULL,

    solicitante_nombre NVARCHAR(150) NOT NULL,
    solicitante_identificacion VARCHAR(30) NOT NULL,

    fecha_prestamo DATETIME2(0) NOT NULL
        CONSTRAINT DF_Prestamo_FechaPrestamo
        DEFAULT SYSDATETIME(),

    fecha_esperada_devolucion DATETIME2(0) NOT NULL,

    /* Usuario del sistema que registra el préstamo */
    id_usuario_registra INT NOT NULL,

    estado VARCHAR(12) NOT NULL
        CONSTRAINT DF_Prestamo_Estado DEFAULT 'ACTIVO',

    observaciones NVARCHAR(500) NULL,

    CONSTRAINT PK_Prestamo
        PRIMARY KEY (id_prestamo),

    CONSTRAINT FK_Prestamo_UsuarioRegistra
        FOREIGN KEY (id_usuario_registra)
        REFERENCES Usuario(id_usuario),

    CONSTRAINT CK_Prestamo_Estado
        CHECK (estado IN ('ACTIVO', 'FINALIZADO')),

    CONSTRAINT CK_Prestamo_Fechas
        CHECK (fecha_esperada_devolucion >= fecha_prestamo)
);
GO


/* ============================================================
   8. DETALLE DE PRÉSTAMO
   ------------------------------------------------------------
   Resuelve:
       Prestamo N:M Equipo

   UQ_DetallePrestamo impide repetir el mismo equipo
   dentro del mismo préstamo.
   ============================================================ */

CREATE TABLE DetallePrestamo
(
    id_detalle_prestamo INT IDENTITY(1,1) NOT NULL,

    id_prestamo INT NOT NULL,
    id_equipo INT NOT NULL,

    CONSTRAINT PK_DetallePrestamo
        PRIMARY KEY (id_detalle_prestamo),

    CONSTRAINT FK_DetallePrestamo_Prestamo
        FOREIGN KEY (id_prestamo)
        REFERENCES Prestamo(id_prestamo),

    CONSTRAINT FK_DetallePrestamo_Equipo
        FOREIGN KEY (id_equipo)
        REFERENCES Equipo(id_equipo),

    CONSTRAINT UQ_DetallePrestamo
        UNIQUE
        (
            id_prestamo,
            id_equipo
        )
);
GO


/* ============================================================
   9. DEVOLUCIÓN
   ------------------------------------------------------------
   Una fila por cada equipo devuelto.
   Un detalle SIN devolución es un equipo PENDIENTE.

   UQ_Devolucion_Detalle impide devolver dos veces
   el mismo equipo de un préstamo.

   condicion:
       BUENO
       DANADO
   ============================================================ */

CREATE TABLE Devolucion
(
    id_devolucion INT IDENTITY(1,1) NOT NULL,

    id_detalle_prestamo INT NOT NULL,

    fecha_devolucion DATETIME2(0) NOT NULL
        CONSTRAINT DF_Devolucion_Fecha
        DEFAULT SYSDATETIME(),

    condicion VARCHAR(10) NOT NULL,

    observacion NVARCHAR(500) NULL,

    /* Usuario del sistema que recibe el equipo */
    id_usuario_recibe INT NOT NULL,

    CONSTRAINT PK_Devolucion
        PRIMARY KEY (id_devolucion),

    CONSTRAINT FK_Devolucion_DetallePrestamo
        FOREIGN KEY (id_detalle_prestamo)
        REFERENCES DetallePrestamo(id_detalle_prestamo),

    CONSTRAINT FK_Devolucion_UsuarioRecibe
        FOREIGN KEY (id_usuario_recibe)
        REFERENCES Usuario(id_usuario),

    CONSTRAINT UQ_Devolucion_Detalle
        UNIQUE (id_detalle_prestamo),

    CONSTRAINT CK_Devolucion_Condicion
        CHECK (condicion IN ('BUENO', 'DANADO'))
);
GO


/* ============================================================
   10. MANTENIMIENTO
   ------------------------------------------------------------
   tipo:
       PREVENTIVO
       CORRECTIVO

   estado:
       EN_PROCESO
       FINALIZADO

   resultado (se llena al finalizar):
       REPARADO      - el equipo vuelve a DISPONIBLE
       NO_REPARABLE  - el equipo pasa a BAJA
   ============================================================ */

CREATE TABLE Mantenimiento
(
    id_mantenimiento INT IDENTITY(1,1) NOT NULL,

    id_equipo INT NOT NULL,

    fecha_ingreso DATETIME2(0) NOT NULL
        CONSTRAINT DF_Mantenimiento_FechaIngreso
        DEFAULT SYSDATETIME(),

    tipo VARCHAR(12) NOT NULL,

    descripcion NVARCHAR(500) NOT NULL,
    responsable NVARCHAR(150) NOT NULL,

    fecha_salida DATETIME2(0) NULL,
    resultado VARCHAR(15) NULL,

    estado VARCHAR(12) NOT NULL
        CONSTRAINT DF_Mantenimiento_Estado
        DEFAULT 'EN_PROCESO',

    observaciones NVARCHAR(500) NULL,

    /* Usuario del sistema que registra el mantenimiento */
    id_usuario_registra INT NOT NULL,

    CONSTRAINT PK_Mantenimiento
        PRIMARY KEY (id_mantenimiento),

    CONSTRAINT FK_Mantenimiento_Equipo
        FOREIGN KEY (id_equipo)
        REFERENCES Equipo(id_equipo),

    CONSTRAINT FK_Mantenimiento_UsuarioRegistra
        FOREIGN KEY (id_usuario_registra)
        REFERENCES Usuario(id_usuario),

    CONSTRAINT CK_Mantenimiento_Tipo
        CHECK (tipo IN ('PREVENTIVO', 'CORRECTIVO')),

    CONSTRAINT CK_Mantenimiento_Estado
        CHECK (estado IN ('EN_PROCESO', 'FINALIZADO')),

    CONSTRAINT CK_Mantenimiento_Resultado
        CHECK
        (
            resultado IS NULL
            OR resultado IN ('REPARADO', 'NO_REPARABLE')
        ),

    CONSTRAINT CK_Mantenimiento_Fechas
        CHECK
        (
            fecha_salida IS NULL
            OR fecha_salida >= fecha_ingreso
        ),

    CONSTRAINT CK_Mantenimiento_Cierre
        CHECK
        (
            (
                estado = 'EN_PROCESO'
                AND fecha_salida IS NULL
                AND resultado IS NULL
            )
            OR
            (
                estado = 'FINALIZADO'
                AND fecha_salida IS NOT NULL
                AND resultado IS NOT NULL
            )
        )
);
GO


/* ============================================================
   11. AUDITORÍA
   ------------------------------------------------------------
   id_usuario admite NULL: un login fallido con un usuario
   que no existe no tiene a quién asociarse.

   accion (enum AccionAuditoria de Java):
       LOGIN_EXITOSO
       LOGIN_FALLIDO
       PRESTAMO_CREADO
       EQUIPO_DEVUELTO
       EQUIPO_ENVIADO_MANTENIMIENTO
       MANTENIMIENTO_FINALIZADO
       USUARIO_DESACTIVADO
   ============================================================ */

CREATE TABLE Auditoria
(
    id_auditoria BIGINT IDENTITY(1,1) NOT NULL,

    id_usuario INT NULL,

    fecha_hora DATETIME2(0) NOT NULL
        CONSTRAINT DF_Auditoria_FechaHora
        DEFAULT SYSDATETIME(),

    accion VARCHAR(40) NOT NULL,
    entidad VARCHAR(40) NOT NULL,

    /* ID del registro afectado dentro de la entidad */
    id_registro INT NULL,

    descripcion NVARCHAR(500) NULL,

    CONSTRAINT PK_Auditoria
        PRIMARY KEY (id_auditoria),

    CONSTRAINT FK_Auditoria_Usuario
        FOREIGN KEY (id_usuario)
        REFERENCES Usuario(id_usuario)
);
GO


/* ============================================================
   ÍNDICES
   ============================================================ */

/* Equipos por estado (dashboard y reportes) */
CREATE INDEX IX_Equipo_Estado
ON Equipo
(
    estado
);
GO


/* Equipos de un laboratorio */
CREATE INDEX IX_Equipo_Laboratorio
ON Equipo
(
    id_laboratorio
);
GO


/* Préstamos activos y vencidos */
CREATE INDEX IX_Prestamo_Estado_FechaEsperada
ON Prestamo
(
    estado,
    fecha_esperada_devolucion
);
GO


/* Historial de préstamos de un equipo */
CREATE INDEX IX_DetallePrestamo_Equipo
ON DetallePrestamo
(
    id_equipo
);
GO


/* Historial de mantenimiento de un equipo */
CREATE INDEX IX_Mantenimiento_Equipo
ON Mantenimiento
(
    id_equipo,
    fecha_ingreso
);
GO


/* Consultar la auditoría por fecha */
CREATE INDEX IX_Auditoria_FechaHora
ON Auditoria
(
    fecha_hora
);
GO


/* ============================================================
   COMPROBACIÓN
   ============================================================ */

SELECT
    TABLE_NAME
FROM INFORMATION_SCHEMA.TABLES
WHERE TABLE_TYPE = 'BASE TABLE'
ORDER BY TABLE_NAME;
GO

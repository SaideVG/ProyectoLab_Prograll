import 'equipo.dart';

DateTime? _fecha(Object? valor) => valor == null ? null : DateTime.parse(valor as String);

/// Encabezado de un préstamo con sus totales. vencido lo calcula la API (RN-17).
class Prestamo {
  final int id;
  final String solicitanteNombre;
  final String solicitanteIdentificacion;
  final DateTime fechaPrestamo;
  final DateTime fechaEsperadaDevolucion;
  final String estado;
  final String? observaciones;
  final String registradoPor;
  final int totalEquipos;
  final int pendientes;
  final bool vencido;

  const Prestamo({
    required this.id,
    required this.solicitanteNombre,
    required this.solicitanteIdentificacion,
    required this.fechaPrestamo,
    required this.fechaEsperadaDevolucion,
    required this.estado,
    required this.observaciones,
    required this.registradoPor,
    required this.totalEquipos,
    required this.pendientes,
    required this.vencido,
  });

  int get devueltos => totalEquipos - pendientes;

  factory Prestamo.desdeJson(Map<String, dynamic> json) => Prestamo(
        id: json['id'] as int,
        solicitanteNombre: json['solicitanteNombre'] as String,
        solicitanteIdentificacion: json['solicitanteIdentificacion'] as String,
        fechaPrestamo: _fecha(json['fechaPrestamo'])!,
        fechaEsperadaDevolucion: _fecha(json['fechaEsperadaDevolucion'])!,
        estado: json['estado'] as String,
        observaciones: json['observaciones'] as String?,
        registradoPor: json['registradoPor'] as String,
        totalEquipos: json['totalEquipos'] as int,
        pendientes: json['pendientes'] as int,
        vencido: json['vencido'] as bool,
      );
}

/// Un equipo dentro de un préstamo y, si ya se devolvió, cómo regresó.
class DetallePrestamo {
  final int idDetalle;
  final Equipo equipo;
  final bool devuelto;
  final DateTime? fechaDevolucion;
  final String? condicion;
  final String? observacion;

  const DetallePrestamo({
    required this.idDetalle,
    required this.equipo,
    required this.devuelto,
    required this.fechaDevolucion,
    required this.condicion,
    required this.observacion,
  });

  factory DetallePrestamo.desdeJson(Map<String, dynamic> json) => DetallePrestamo(
        idDetalle: json['idDetalle'] as int,
        equipo: Equipo.desdeJson(json['equipo'] as Map<String, dynamic>),
        devuelto: json['devuelto'] as bool,
        fechaDevolucion: _fecha(json['fechaDevolucion']),
        condicion: json['condicion'] as String?,
        observacion: json['observacion'] as String?,
      );
}

class PrestamoDetalle {
  final Prestamo prestamo;
  final List<DetallePrestamo> equipos;

  const PrestamoDetalle({required this.prestamo, required this.equipos});

  factory PrestamoDetalle.desdeJson(Map<String, dynamic> json) => PrestamoDetalle(
        prestamo: Prestamo.desdeJson(json['prestamo'] as Map<String, dynamic>),
        equipos: (json['equipos'] as List)
            .map((d) => DetallePrestamo.desdeJson(d as Map<String, dynamic>))
            .toList(),
      );
}

/// Un préstamo en el historial de un equipo.
class HistorialPrestamo {
  final int idPrestamo;
  final String solicitanteNombre;
  final String solicitanteIdentificacion;
  final DateTime fechaPrestamo;
  final DateTime fechaEsperadaDevolucion;
  final DateTime? fechaDevolucion;
  final String? condicion;

  const HistorialPrestamo({
    required this.idPrestamo,
    required this.solicitanteNombre,
    required this.solicitanteIdentificacion,
    required this.fechaPrestamo,
    required this.fechaEsperadaDevolucion,
    required this.fechaDevolucion,
    required this.condicion,
  });

  bool get devuelto => fechaDevolucion != null;

  factory HistorialPrestamo.desdeJson(Map<String, dynamic> json) => HistorialPrestamo(
        idPrestamo: json['idPrestamo'] as int,
        solicitanteNombre: json['solicitanteNombre'] as String,
        solicitanteIdentificacion: json['solicitanteIdentificacion'] as String,
        fechaPrestamo: _fecha(json['fechaPrestamo'])!,
        fechaEsperadaDevolucion: _fecha(json['fechaEsperadaDevolucion'])!,
        fechaDevolucion: _fecha(json['fechaDevolucion']),
        condicion: json['condicion'] as String?,
      );
}

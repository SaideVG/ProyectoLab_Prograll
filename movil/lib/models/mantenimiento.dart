import 'equipo.dart';

DateTime? _fecha(Object? valor) => valor == null ? null : DateTime.parse(valor as String);

/// fechaSalida y resultado son null mientras el mantenimiento sigue EN_PROCESO.
class Mantenimiento {
  final int id;
  final Equipo equipo;
  final DateTime fechaIngreso;
  final String tipo;
  final String descripcion;
  final String responsable;
  final DateTime? fechaSalida;
  final String? resultado;
  final String estado;
  final String? observaciones;
  final String registradoPor;

  const Mantenimiento({
    required this.id,
    required this.equipo,
    required this.fechaIngreso,
    required this.tipo,
    required this.descripcion,
    required this.responsable,
    required this.fechaSalida,
    required this.resultado,
    required this.estado,
    required this.observaciones,
    required this.registradoPor,
  });

  bool get enProceso => estado == 'EN_PROCESO';

  factory Mantenimiento.desdeJson(Map<String, dynamic> json) => Mantenimiento(
        id: json['id'] as int,
        equipo: Equipo.desdeJson(json['equipo'] as Map<String, dynamic>),
        fechaIngreso: _fecha(json['fechaIngreso'])!,
        tipo: json['tipo'] as String,
        descripcion: json['descripcion'] as String,
        responsable: json['responsable'] as String,
        fechaSalida: _fecha(json['fechaSalida']),
        resultado: json['resultado'] as String?,
        estado: json['estado'] as String,
        observaciones: json['observaciones'] as String?,
        registradoPor: json['registradoPor'] as String,
      );
}

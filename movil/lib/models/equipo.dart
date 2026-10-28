import 'mantenimiento.dart';
import 'prestamo.dart';

/// Equipo con el nombre de su tipo y de su laboratorio.
/// estado es el valor de la base de datos: DISPONIBLE, PRESTADO, DANADO, MANTENIMIENTO o BAJA.
class Equipo {
  final int id;
  final String codigo;
  final String nombre;
  final String tipo;
  final String marca;
  final String modelo;
  final String? numeroSerie;
  final String laboratorioCodigo;
  final String laboratorioNombre;
  final String estado;
  final String? observaciones;

  const Equipo({
    required this.id,
    required this.codigo,
    required this.nombre,
    required this.tipo,
    required this.marca,
    required this.modelo,
    required this.numeroSerie,
    required this.laboratorioCodigo,
    required this.laboratorioNombre,
    required this.estado,
    required this.observaciones,
  });

  factory Equipo.desdeJson(Map<String, dynamic> json) => Equipo(
        id: json['id'] as int,
        codigo: json['codigo'] as String,
        nombre: json['nombre'] as String,
        tipo: json['tipo'] as String,
        marca: json['marca'] as String,
        modelo: json['modelo'] as String,
        numeroSerie: json['numeroSerie'] as String?,
        laboratorioCodigo: json['laboratorioCodigo'] as String,
        laboratorioNombre: json['laboratorioNombre'] as String,
        estado: json['estado'] as String,
        observaciones: json['observaciones'] as String?,
      );
}

/// Lo que se ve al escanear el QR: el equipo, sus préstamos y sus mantenimientos.
class EquipoDetalle {
  final Equipo equipo;
  final List<HistorialPrestamo> prestamos;
  final List<Mantenimiento> mantenimientos;

  const EquipoDetalle({required this.equipo, required this.prestamos, required this.mantenimientos});

  factory EquipoDetalle.desdeJson(Map<String, dynamic> json) => EquipoDetalle(
        equipo: Equipo.desdeJson(json['equipo'] as Map<String, dynamic>),
        prestamos: (json['prestamos'] as List)
            .map((p) => HistorialPrestamo.desdeJson(p as Map<String, dynamic>))
            .toList(),
        mantenimientos: (json['mantenimientos'] as List)
            .map((m) => Mantenimiento.desdeJson(m as Map<String, dynamic>))
            .toList(),
      );
}

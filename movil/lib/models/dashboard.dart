/// Mismas cantidades que el menú principal del escritorio.
class Dashboard {
  final int totalEquipos;
  final int disponibles;
  final int prestados;
  final int danados;
  final int enMantenimiento;
  final int deBaja;
  final int prestamosActivos;
  final int prestamosVencidos;

  const Dashboard({
    required this.totalEquipos,
    required this.disponibles,
    required this.prestados,
    required this.danados,
    required this.enMantenimiento,
    required this.deBaja,
    required this.prestamosActivos,
    required this.prestamosVencidos,
  });

  factory Dashboard.desdeJson(Map<String, dynamic> json) => Dashboard(
        totalEquipos: json['totalEquipos'] as int,
        disponibles: json['disponibles'] as int,
        prestados: json['prestados'] as int,
        danados: json['danados'] as int,
        enMantenimiento: json['enMantenimiento'] as int,
        deBaja: json['deBaja'] as int,
        prestamosActivos: json['prestamosActivos'] as int,
        prestamosVencidos: json['prestamosVencidos'] as int,
      );

  /// Cantidad de equipos de cada estado, en el orden en que se muestran.
  Map<String, int> get porEstado => {
        'DISPONIBLE': disponibles,
        'PRESTADO': prestados,
        'DANADO': danados,
        'MANTENIMIENTO': enMantenimiento,
        'BAJA': deBaja,
      };
}

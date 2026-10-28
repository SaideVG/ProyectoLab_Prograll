import 'package:flutter/material.dart';

import '../config/tema.dart';

/// Textos y colores que se repiten en varias pantallas.
class Formato {
  Formato._();

  static const _meses = ['ene', 'feb', 'mar', 'abr', 'may', 'jun', 'jul', 'ago', 'sep', 'oct', 'nov', 'dic'];

  /// 25 oct 2026, 10:11
  static String fechaHora(DateTime fecha) => '${fechaCorta(fecha)} ${fecha.year}, ${_dosDigitos(fecha.hour)}:${_dosDigitos(fecha.minute)}';

  /// 25 oct
  static String fechaCorta(DateTime fecha) => '${fecha.day} ${_meses[fecha.month - 1]}';

  /// "hace 3 días", "en 2 horas", "ahora"
  static String relativo(DateTime fecha) {
    final diferencia = fecha.difference(DateTime.now());
    final futuro = !diferencia.isNegative;
    final absoluta = diferencia.abs();

    final String cantidad;
    if (absoluta.inMinutes < 1) return 'ahora';
    if (absoluta.inHours < 1) {
      cantidad = _plural(absoluta.inMinutes, 'minuto');
    } else if (absoluta.inDays < 1) {
      cantidad = _plural(absoluta.inHours, 'hora');
    } else {
      cantidad = _plural(absoluta.inDays, 'día');
    }
    return futuro ? 'en $cantidad' : 'hace $cantidad';
  }

  static String _plural(int n, String palabra) => '$n $palabra${n == 1 ? '' : 's'}';

  static String _dosDigitos(int n) => n.toString().padLeft(2, '0');

  /*
   * ========================================================
   * ESTADOS DEL EQUIPO
   * En la base se guarda DANADO sin ñ; en pantalla se muestra "Dañado".
   * ========================================================
   */

  static const estadosEquipo = ['DISPONIBLE', 'PRESTADO', 'DANADO', 'MANTENIMIENTO', 'BAJA'];

  static String estadoEquipo(String estado) => switch (estado) {
        'DISPONIBLE' => 'Disponible',
        'PRESTADO' => 'Prestado',
        'DANADO' => 'Dañado',
        'MANTENIMIENTO' => 'Mantenimiento',
        'BAJA' => 'De baja',
        _ => estado,
      };

  static Color colorEstadoEquipo(String estado) => switch (estado) {
        'DISPONIBLE' => Tema.exito,
        'PRESTADO' => Tema.azul,
        'DANADO' => Tema.peligro,
        'MANTENIMIENTO' => Tema.advertencia,
        _ => Tema.pie,
      };

  static IconData iconoEstadoEquipo(String estado) => switch (estado) {
        'DISPONIBLE' => Icons.check_circle_outline,
        'PRESTADO' => Icons.swap_horiz,
        'DANADO' => Icons.report_gmailerrorred,
        'MANTENIMIENTO' => Icons.build_outlined,
        _ => Icons.block,
      };

  /// Condición de devolución: BUENO o DANADO.
  static String condicion(String? condicion) => condicion == 'DANADO' ? 'Dañado' : 'Bueno';

  static String texto(String? valor) => valor == null || valor.trim().isEmpty ? '—' : valor;

  static String capitalizar(String texto) =>
      texto.isEmpty ? texto : texto[0].toUpperCase() + texto.substring(1).toLowerCase().replaceAll('_', ' ');
}

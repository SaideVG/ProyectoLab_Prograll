import 'package:flutter/material.dart';

/// Paleta de SIGELAB: los mismos colores del tema oscuro del escritorio
/// (clase Tema.java), para que las dos aplicaciones se vean como un solo sistema.
class Tema {
  Tema._();

  // Fondos
  static const fondo = Color(0xFF060B14);
  static const panel = Color(0xFF091122);
  static const superficie = Color(0xFF0B1222);
  static const superficieAlt = Color(0xFF0C162C);

  // Cian principal
  static const primario = Color(0xFF00C9FF);
  static const primarioPresionado = Color(0xFF00A3CC);
  static const azulClaro = Color(0xFF132640);

  // Textos y bordes
  static const texto = Color(0xFFFFFFFF);
  static const etiqueta = Color(0xFF38BDF8);
  static const textoSuave = Color(0xFF8E9BB0);
  static const placeholder = Color(0xFF475569);
  static const pie = Color(0xFF64748B);
  static const borde = Color(0xFF132640);

  // Estados
  static const peligro = Color(0xFFEF4444);
  static const exito = Color(0xFF10B981);
  static const advertencia = Color(0xFFF59E0B);
  static const azul = Color(0xFF3B82F6);

  /// Texto oscuro sobre el cian: con blanco no se leería bien.
  static const textoSobrePrimario = Color(0xFF041019);

  static const radio = 16.0;

  static ThemeData get oscuro {
    final esquema = const ColorScheme.dark(
      primary: primario,
      onPrimary: textoSobrePrimario,
      secondary: etiqueta,
      surface: superficie,
      onSurface: texto,
      error: peligro,
      outline: borde,
    );

    OutlineInputBorder bordeCampo(Color color, [double ancho = 1]) => OutlineInputBorder(
          borderRadius: BorderRadius.circular(12),
          borderSide: BorderSide(color: color, width: ancho),
        );

    return ThemeData(
      useMaterial3: true,
      brightness: Brightness.dark,
      colorScheme: esquema,
      scaffoldBackgroundColor: fondo,
      splashFactory: InkSparkle.splashFactory,
      appBarTheme: const AppBarTheme(
        backgroundColor: fondo,
        foregroundColor: texto,
        elevation: 0,
        scrolledUnderElevation: 0,
        centerTitle: false,
        titleTextStyle: TextStyle(fontSize: 20, fontWeight: FontWeight.w700, color: texto),
      ),
      textTheme: const TextTheme(
        headlineMedium: TextStyle(fontSize: 28, fontWeight: FontWeight.w800, color: texto, letterSpacing: -0.5),
        titleLarge: TextStyle(fontSize: 20, fontWeight: FontWeight.w700, color: texto),
        titleMedium: TextStyle(fontSize: 16, fontWeight: FontWeight.w600, color: texto),
        bodyLarge: TextStyle(fontSize: 15, color: texto),
        bodyMedium: TextStyle(fontSize: 14, color: texto),
        bodySmall: TextStyle(fontSize: 12.5, color: textoSuave),
        labelSmall: TextStyle(fontSize: 11, fontWeight: FontWeight.w700, color: etiqueta, letterSpacing: 1.1),
      ),
      inputDecorationTheme: InputDecorationTheme(
        filled: true,
        fillColor: panel,
        hintStyle: const TextStyle(color: placeholder),
        labelStyle: const TextStyle(color: textoSuave),
        floatingLabelStyle: const TextStyle(color: etiqueta),
        prefixIconColor: textoSuave,
        suffixIconColor: textoSuave,
        contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 16),
        border: bordeCampo(borde),
        enabledBorder: bordeCampo(borde),
        focusedBorder: bordeCampo(primario, 1.4),
        errorBorder: bordeCampo(peligro),
        focusedErrorBorder: bordeCampo(peligro, 1.4),
      ),
      filledButtonTheme: FilledButtonThemeData(
        style: FilledButton.styleFrom(
          backgroundColor: primario,
          foregroundColor: textoSobrePrimario,
          disabledBackgroundColor: azulClaro,
          minimumSize: const Size.fromHeight(52),
          textStyle: const TextStyle(fontSize: 15, fontWeight: FontWeight.w700),
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
        ),
      ),
      outlinedButtonTheme: OutlinedButtonThemeData(
        style: OutlinedButton.styleFrom(
          foregroundColor: texto,
          side: const BorderSide(color: borde),
          minimumSize: const Size.fromHeight(48),
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
        ),
      ),
      textButtonTheme: TextButtonThemeData(
        style: TextButton.styleFrom(foregroundColor: etiqueta),
      ),
      segmentedButtonTheme: SegmentedButtonThemeData(
        style: ButtonStyle(
          side: const WidgetStatePropertyAll(BorderSide(color: borde)),
          backgroundColor: WidgetStateProperty.resolveWith(
            (estados) => estados.contains(WidgetState.selected) ? azulClaro : panel,
          ),
          foregroundColor: WidgetStateProperty.resolveWith(
            (estados) => estados.contains(WidgetState.selected) ? primario : textoSuave,
          ),
          iconColor: const WidgetStatePropertyAll(primario),
          visualDensity: VisualDensity.compact,
        ),
      ),
      chipTheme: ChipThemeData(
        backgroundColor: panel,
        selectedColor: azulClaro,
        side: const BorderSide(color: borde),
        labelStyle: const TextStyle(color: textoSuave, fontSize: 13),
        secondaryLabelStyle: const TextStyle(color: primario, fontSize: 13),
        checkmarkColor: primario,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
      ),
      navigationBarTheme: NavigationBarThemeData(
        backgroundColor: superficie,
        indicatorColor: azulClaro,
        surfaceTintColor: Colors.transparent,
        height: 68,
        iconTheme: WidgetStateProperty.resolveWith(
          (estados) => IconThemeData(color: estados.contains(WidgetState.selected) ? primario : textoSuave),
        ),
        labelTextStyle: WidgetStateProperty.resolveWith(
          (estados) => TextStyle(
            fontSize: 12,
            fontWeight: estados.contains(WidgetState.selected) ? FontWeight.w700 : FontWeight.w500,
            color: estados.contains(WidgetState.selected) ? texto : textoSuave,
          ),
        ),
      ),
      floatingActionButtonTheme: const FloatingActionButtonThemeData(
        backgroundColor: primario,
        foregroundColor: textoSobrePrimario,
        elevation: 2,
      ),
      progressIndicatorTheme: const ProgressIndicatorThemeData(color: primario, linearTrackColor: azulClaro),
      dividerTheme: const DividerThemeData(color: borde, thickness: 1, space: 1),
      bottomSheetTheme: const BottomSheetThemeData(
        backgroundColor: superficie,
        showDragHandle: true,
        dragHandleColor: placeholder,
      ),
      snackBarTheme: const SnackBarThemeData(
        backgroundColor: superficieAlt,
        contentTextStyle: TextStyle(color: texto),
        behavior: SnackBarBehavior.floating,
      ),
    );
  }
}

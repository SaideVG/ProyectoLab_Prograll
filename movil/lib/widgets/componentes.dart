import 'package:flutter/material.dart';

import '../config/tema.dart';
import '../services/api_service.dart';

/// Ancho máximo del contenido: en el celular no se nota, en el navegador
/// evita que las tarjetas se estiren de lado a lado de la pantalla.
const double anchoMaximo = 620;

/// Centra el contenido y limita su ancho.
class Contenido extends StatelessWidget {
  final Widget child;

  const Contenido({super.key, required this.child});

  @override
  Widget build(BuildContext context) => Align(
        alignment: Alignment.topCenter,
        child: ConstrainedBox(constraints: const BoxConstraints(maxWidth: anchoMaximo), child: child),
      );
}

/// Tarjeta base: fondo de superficie, borde fino y esquinas redondeadas.
class Tarjeta extends StatelessWidget {
  final Widget child;
  final VoidCallback? onTap;
  final EdgeInsetsGeometry padding;
  final Color? colorBorde;
  final Color? color;

  const Tarjeta({
    super.key,
    required this.child,
    this.onTap,
    this.padding = const EdgeInsets.all(16),
    this.colorBorde,
    this.color,
  });

  @override
  Widget build(BuildContext context) => Material(
        color: color ?? Tema.superficie,
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(Tema.radio),
          side: BorderSide(color: colorBorde ?? Tema.borde),
        ),
        clipBehavior: Clip.antiAlias,
        child: InkWell(onTap: onTap, child: Padding(padding: padding, child: child)),
      );
}

/// Etiqueta pequeña de color: estado de un equipo, de un préstamo, etc.
class ChipEstado extends StatelessWidget {
  final String texto;
  final Color color;
  final IconData? icono;

  const ChipEstado({super.key, required this.texto, required this.color, this.icono});

  @override
  Widget build(BuildContext context) => Container(
        padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
        decoration: BoxDecoration(
          color: color.withValues(alpha: 0.12),
          borderRadius: BorderRadius.circular(20),
          border: Border.all(color: color.withValues(alpha: 0.35)),
        ),
        child: Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            if (icono != null) ...[Icon(icono, size: 13, color: color), const SizedBox(width: 4)],
            Text(texto, style: TextStyle(color: color, fontSize: 12, fontWeight: FontWeight.w700)),
          ],
        ),
      );
}

/// Título en mayúsculas pequeñas, como las etiquetas cian del escritorio.
class TituloSeccion extends StatelessWidget {
  final String texto;
  final Widget? accion;

  const TituloSeccion(this.texto, {super.key, this.accion});

  @override
  Widget build(BuildContext context) => Padding(
        padding: const EdgeInsets.fromLTRB(4, 24, 4, 10),
        child: Row(
          children: [
            Expanded(child: Text(texto.toUpperCase(), style: Theme.of(context).textTheme.labelSmall)),
            ?accion,
          ],
        ),
      );
}

/// Una fila "Etiqueta ........ Valor" para las pantallas de detalle.
class FilaDato extends StatelessWidget {
  final String etiqueta;
  final String valor;

  const FilaDato(this.etiqueta, this.valor, {super.key});

  @override
  Widget build(BuildContext context) => Padding(
        padding: const EdgeInsets.symmetric(vertical: 8),
        child: Row(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            SizedBox(
              width: 120,
              child: Text(etiqueta, style: const TextStyle(color: Tema.textoSuave, fontSize: 13.5)),
            ),
            Expanded(child: Text(valor, style: const TextStyle(fontSize: 14, fontWeight: FontWeight.w500))),
          ],
        ),
      );
}

/// Mensaje centrado con un ícono: lista vacía o error.
class MensajeCentral extends StatelessWidget {
  final IconData icono;
  final String titulo;
  final String? detalle;
  final Color color;
  final Widget? accion;

  const MensajeCentral({
    super.key,
    required this.icono,
    required this.titulo,
    this.detalle,
    this.color = Tema.textoSuave,
    this.accion,
  });

  @override
  Widget build(BuildContext context) => Padding(
        padding: const EdgeInsets.symmetric(horizontal: 32, vertical: 56),
        child: Column(
          children: [
            Container(
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(color: color.withValues(alpha: 0.1), shape: BoxShape.circle),
              child: Icon(icono, color: color, size: 30),
            ),
            const SizedBox(height: 16),
            Text(titulo, textAlign: TextAlign.center, style: Theme.of(context).textTheme.titleMedium),
            if (detalle != null) ...[
              const SizedBox(height: 6),
              Text(detalle!, textAlign: TextAlign.center, style: Theme.of(context).textTheme.bodySmall),
            ],
            if (accion != null) ...[const SizedBox(height: 20), accion!],
          ],
        ),
      );
}

/// Carga datos de la API y muestra, según el caso: cargando, error con
/// "Reintentar" o el contenido. Deslizar hacia abajo vuelve a cargar.
///
/// Evita repetir el mismo código de carga en todas las pantallas.
class VistaAsincrona<T> extends StatefulWidget {
  final Future<T> Function() cargar;
  final List<Widget> Function(BuildContext context, T datos) construir;
  final EdgeInsetsGeometry padding;

  const VistaAsincrona({
    super.key,
    required this.cargar,
    required this.construir,
    this.padding = const EdgeInsets.fromLTRB(16, 8, 16, 96),
  });

  @override
  State<VistaAsincrona<T>> createState() => _VistaAsincronaState<T>();
}

class _VistaAsincronaState<T> extends State<VistaAsincrona<T>> {
  late Future<T> _futuro = widget.cargar();

  Future<void> _recargar() async {
    final nuevo = widget.cargar();
    setState(() => _futuro = nuevo);
    try {
      await nuevo;
    } catch (_) {
      // El error ya lo muestra el FutureBuilder.
    }
  }

  @override
  Widget build(BuildContext context) => RefreshIndicator(
        color: Tema.primario,
        backgroundColor: Tema.superficie,
        onRefresh: _recargar,
        child: FutureBuilder<T>(
          future: _futuro,
          builder: (context, snapshot) {
            final List<Widget> hijos;
            if (snapshot.connectionState != ConnectionState.done) {
              hijos = [
                const Padding(
                  padding: EdgeInsets.only(top: 120),
                  child: Center(child: CircularProgressIndicator(strokeWidth: 2.5)),
                ),
              ];
            } else if (snapshot.hasError) {
              final error = snapshot.error;
              hijos = [
                MensajeCentral(
                  icono: Icons.cloud_off_outlined,
                  titulo: 'No se pudo cargar la información',
                  detalle: error is ApiException ? error.mensaje : '$error',
                  color: Tema.peligro,
                  accion: OutlinedButton.icon(
                    onPressed: _recargar,
                    icon: const Icon(Icons.refresh, size: 18),
                    label: const Text('Reintentar'),
                  ),
                ),
              ];
            } else {
              hijos = widget.construir(context, snapshot.data as T);
            }

            return ListView(
              physics: const AlwaysScrollableScrollPhysics(),
              padding: widget.padding,
              children: [Contenido(child: Column(crossAxisAlignment: CrossAxisAlignment.stretch, children: hijos))],
            );
          },
        ),
      );
}

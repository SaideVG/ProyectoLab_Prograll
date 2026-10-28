import 'package:flutter/material.dart';

import '../config/tema.dart';
import '../models/prestamo.dart';
import '../services/api_service.dart';
import '../util/formato.dart';
import '../widgets/componentes.dart';
import 'prestamo_detalle_view.dart';

/// Préstamos activos, vencidos (RN-17) o finalizados.
class PrestamosView extends StatefulWidget {
  final String filtroInicial;

  const PrestamosView({super.key, this.filtroInicial = 'ACTIVOS'});

  @override
  State<PrestamosView> createState() => _PrestamosViewState();
}

class _PrestamosViewState extends State<PrestamosView> {
  late String _filtro = widget.filtroInicial;

  @override
  Widget build(BuildContext context) => Scaffold(
        appBar: AppBar(
          title: const Text('Préstamos'),
          bottom: PreferredSize(
            preferredSize: const Size.fromHeight(56),
            child: Contenido(
              child: Padding(
                padding: const EdgeInsets.fromLTRB(16, 0, 16, 12),
                child: SizedBox(
                  width: double.infinity,
                  child: SegmentedButton<String>(
                    showSelectedIcon: false,
                    segments: const [
                      ButtonSegment(value: 'ACTIVOS', label: Text('Activos')),
                      ButtonSegment(value: 'VENCIDOS', label: Text('Vencidos')),
                      ButtonSegment(value: 'FINALIZADOS', label: Text('Finalizados')),
                    ],
                    selected: {_filtro},
                    onSelectionChanged: (seleccion) => setState(() => _filtro = seleccion.first),
                  ),
                ),
              ),
            ),
          ),
        ),
        body: VistaAsincrona<List<Prestamo>>(
          key: ValueKey(_filtro),
          cargar: () => ApiService.instancia.prestamos(_filtro),
          construir: (context, prestamos) => prestamos.isEmpty
              ? [
                  MensajeCentral(
                    icono: _filtro == 'VENCIDOS' ? Icons.verified_outlined : Icons.inbox_outlined,
                    titulo: switch (_filtro) {
                      'VENCIDOS' => 'No hay préstamos vencidos',
                      'FINALIZADOS' => 'No hay préstamos finalizados',
                      _ => 'No hay préstamos activos',
                    },
                    color: _filtro == 'VENCIDOS' ? Tema.exito : Tema.textoSuave,
                  ),
                ]
              : [
                  for (final prestamo in prestamos)
                    Padding(
                      padding: const EdgeInsets.only(bottom: 10),
                      child: TarjetaPrestamo(prestamo: prestamo),
                    ),
                ],
        ),
      );
}

class TarjetaPrestamo extends StatelessWidget {
  final Prestamo prestamo;

  const TarjetaPrestamo({super.key, required this.prestamo});

  @override
  Widget build(BuildContext context) {
    final finalizado = prestamo.estado == 'FINALIZADO';
    final progreso = prestamo.totalEquipos == 0 ? 0.0 : prestamo.devueltos / prestamo.totalEquipos;

    return Tarjeta(
      colorBorde: prestamo.vencido ? Tema.peligro.withValues(alpha: 0.45) : null,
      onTap: () => Navigator.of(context)
          .push(MaterialPageRoute(builder: (_) => PrestamoDetalleView(idPrestamo: prestamo.id))),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Text('PRÉSTAMO N° ${prestamo.id}', style: Theme.of(context).textTheme.labelSmall),
              const Spacer(),
              estadoDePrestamo(prestamo),
            ],
          ),
          const SizedBox(height: 10),
          Text(prestamo.solicitanteNombre, style: Theme.of(context).textTheme.titleMedium),
          const SizedBox(height: 2),
          Text('Identificación ${prestamo.solicitanteIdentificacion}', style: Theme.of(context).textTheme.bodySmall),
          const SizedBox(height: 14),
          Row(
            children: [
              Icon(finalizado ? Icons.event_available : Icons.event, size: 16, color: Tema.textoSuave),
              const SizedBox(width: 6),
              Expanded(
                child: Text(
                  finalizado
                      ? 'Prestado el ${Formato.fechaHora(prestamo.fechaPrestamo)}'
                      : '${prestamo.vencido ? 'Venció' : 'Devolver'} '
                          '${Formato.relativo(prestamo.fechaEsperadaDevolucion)} · '
                          '${Formato.fechaHora(prestamo.fechaEsperadaDevolucion)}',
                  style: TextStyle(fontSize: 13, color: prestamo.vencido ? Tema.peligro : Tema.textoSuave),
                ),
              ),
            ],
          ),
          const SizedBox(height: 12),
          ClipRRect(
            borderRadius: BorderRadius.circular(3),
            child: LinearProgressIndicator(
              value: progreso,
              minHeight: 5,
              color: finalizado ? Tema.exito : Tema.primario,
            ),
          ),
          const SizedBox(height: 6),
          Text('${prestamo.devueltos} de ${prestamo.totalEquipos} equipo${prestamo.totalEquipos == 1 ? '' : 's'} devuelto${prestamo.devueltos == 1 ? '' : 's'}',
              style: Theme.of(context).textTheme.bodySmall),
        ],
      ),
    );
  }
}

/// Activo, Vencido o Finalizado. Vencido no es un estado guardado: lo calcula la API.
Widget estadoDePrestamo(Prestamo prestamo) {
  if (prestamo.vencido) return const ChipEstado(texto: 'Vencido', color: Tema.peligro, icono: Icons.schedule);
  if (prestamo.estado == 'FINALIZADO') return const ChipEstado(texto: 'Finalizado', color: Tema.exito);
  return const ChipEstado(texto: 'Activo', color: Tema.primario);
}

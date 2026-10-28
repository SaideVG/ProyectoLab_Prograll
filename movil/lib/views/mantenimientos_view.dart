import 'package:flutter/material.dart';

import '../config/tema.dart';
import '../models/mantenimiento.dart';
import '../services/api_service.dart';
import '../util/formato.dart';
import '../widgets/componentes.dart';
import 'equipo_detalle_view.dart';

/// Equipos que están en mantenimiento o el historial completo.
class MantenimientosView extends StatefulWidget {
  const MantenimientosView({super.key});

  @override
  State<MantenimientosView> createState() => _MantenimientosViewState();
}

class _MantenimientosViewState extends State<MantenimientosView> {
  bool _soloEnProceso = true;

  @override
  Widget build(BuildContext context) => Scaffold(
        appBar: AppBar(
          title: const Text('Mantenimiento'),
          bottom: PreferredSize(
            preferredSize: const Size.fromHeight(56),
            child: Contenido(
              child: Padding(
                padding: const EdgeInsets.fromLTRB(16, 0, 16, 12),
                child: SizedBox(
                  width: double.infinity,
                  child: SegmentedButton<bool>(
                    showSelectedIcon: false,
                    segments: const [
                      ButtonSegment(value: true, label: Text('En proceso')),
                      ButtonSegment(value: false, label: Text('Historial')),
                    ],
                    selected: {_soloEnProceso},
                    onSelectionChanged: (seleccion) => setState(() => _soloEnProceso = seleccion.first),
                  ),
                ),
              ),
            ),
          ),
        ),
        body: VistaAsincrona<List<Mantenimiento>>(
          key: ValueKey(_soloEnProceso),
          cargar: () => ApiService.instancia.mantenimientos(soloEnProceso: _soloEnProceso),
          construir: (context, mantenimientos) => mantenimientos.isEmpty
              ? [
                  MensajeCentral(
                    icono: Icons.build_circle_outlined,
                    titulo: _soloEnProceso ? 'Ningún equipo está en mantenimiento' : 'Sin mantenimientos registrados',
                  ),
                ]
              : [
                  for (final mantenimiento in mantenimientos)
                    Padding(
                      padding: const EdgeInsets.only(bottom: 10),
                      child: TarjetaMantenimiento(mantenimiento: mantenimiento),
                    ),
                ],
        ),
      );
}

/// Un mantenimiento. mostrarEquipo es false dentro de la ficha del mismo equipo.
class TarjetaMantenimiento extends StatelessWidget {
  final Mantenimiento mantenimiento;
  final bool mostrarEquipo;

  const TarjetaMantenimiento({super.key, required this.mantenimiento, this.mostrarEquipo = true});

  @override
  Widget build(BuildContext context) {
    final m = mantenimiento;
    final correctivo = m.tipo == 'CORRECTIVO';

    final Widget estado;
    if (m.enProceso) {
      estado = const ChipEstado(texto: 'En proceso', color: Tema.advertencia);
    } else if (m.resultado == 'NO_REPARABLE') {
      estado = const ChipEstado(texto: 'No reparable', color: Tema.peligro);
    } else {
      estado = const ChipEstado(texto: 'Reparado', color: Tema.exito);
    }

    return Tarjeta(
      onTap: mostrarEquipo
          ? () => Navigator.of(context)
              .push(MaterialPageRoute(builder: (_) => EquipoDetalleView(codigo: m.equipo.codigo)))
          : null,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Icon(correctivo ? Icons.handyman_outlined : Icons.event_repeat,
                  size: 16, color: correctivo ? Tema.advertencia : Tema.etiqueta),
              const SizedBox(width: 6),
              Text(Formato.capitalizar(m.tipo),
                  style: TextStyle(
                    color: correctivo ? Tema.advertencia : Tema.etiqueta,
                    fontSize: 12.5,
                    fontWeight: FontWeight.w700,
                  )),
              const Spacer(),
              estado,
            ],
          ),
          if (mostrarEquipo) ...[
            const SizedBox(height: 10),
            Text('${m.equipo.codigo} · ${m.equipo.nombre}', style: Theme.of(context).textTheme.titleMedium),
          ],
          const SizedBox(height: 8),
          Text(m.descripcion),
          const SizedBox(height: 12),
          Text('Responsable: ${m.responsable}', style: Theme.of(context).textTheme.bodySmall),
          const SizedBox(height: 2),
          Text(
            m.enProceso
                ? 'Ingresó ${Formato.relativo(m.fechaIngreso)} · ${Formato.fechaHora(m.fechaIngreso)}'
                : 'Del ${Formato.fechaCorta(m.fechaIngreso)} al ${Formato.fechaCorta(m.fechaSalida!)} ${m.fechaSalida!.year}',
            style: Theme.of(context).textTheme.bodySmall,
          ),
        ],
      ),
    );
  }
}

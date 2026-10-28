import 'package:flutter/material.dart';

import '../config/tema.dart';
import '../models/prestamo.dart';
import '../services/api_service.dart';
import '../util/formato.dart';
import '../widgets/componentes.dart';
import 'equipo_detalle_view.dart';
import 'prestamos_view.dart';

/// Un préstamo con todos sus equipos y cómo se devolvió cada uno.
class PrestamoDetalleView extends StatelessWidget {
  final int idPrestamo;

  const PrestamoDetalleView({super.key, required this.idPrestamo});

  @override
  Widget build(BuildContext context) => Scaffold(
        appBar: AppBar(title: Text('Préstamo N° $idPrestamo')),
        body: VistaAsincrona<PrestamoDetalle>(
          cargar: () => ApiService.instancia.prestamo(idPrestamo),
          padding: const EdgeInsets.fromLTRB(16, 8, 16, 32),
          construir: (context, detalle) {
            final prestamo = detalle.prestamo;
            return [
              Tarjeta(
                colorBorde: prestamo.vencido ? Tema.peligro.withValues(alpha: 0.45) : null,
                padding: const EdgeInsets.all(20),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      children: [
                        Expanded(child: Text(prestamo.solicitanteNombre, style: Theme.of(context).textTheme.titleLarge)),
                        estadoDePrestamo(prestamo),
                      ],
                    ),
                    const SizedBox(height: 4),
                    Text('Identificación ${prestamo.solicitanteIdentificacion}',
                        style: Theme.of(context).textTheme.bodySmall),
                  ],
                ),
              ),
              const TituloSeccion('Datos'),
              Tarjeta(
                padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
                child: Column(
                  children: [
                    FilaDato('Prestado', Formato.fechaHora(prestamo.fechaPrestamo)),
                    FilaDato('Devolver antes de', Formato.fechaHora(prestamo.fechaEsperadaDevolucion)),
                    FilaDato('Registró', prestamo.registradoPor),
                    FilaDato('Observaciones', Formato.texto(prestamo.observaciones)),
                  ],
                ),
              ),
              TituloSeccion('Equipos (${prestamo.devueltos} de ${prestamo.totalEquipos} devueltos)'),
              for (final detalleEquipo in detalle.equipos)
                Padding(
                  padding: const EdgeInsets.only(bottom: 10),
                  child: _ItemEquipo(detalle: detalleEquipo, vencido: prestamo.vencido),
                ),
            ];
          },
        ),
      );
}

class _ItemEquipo extends StatelessWidget {
  final DetallePrestamo detalle;
  final bool vencido;

  const _ItemEquipo({required this.detalle, required this.vencido});

  @override
  Widget build(BuildContext context) {
    final Widget estado;
    if (!detalle.devuelto) {
      estado = ChipEstado(texto: 'Pendiente', color: vencido ? Tema.peligro : Tema.advertencia);
    } else if (detalle.condicion == 'DANADO') {
      estado = const ChipEstado(texto: 'Dañado', color: Tema.peligro);
    } else {
      estado = const ChipEstado(texto: 'Devuelto', color: Tema.exito);
    }

    return Tarjeta(
      padding: const EdgeInsets.all(14),
      onTap: () => Navigator.of(context)
          .push(MaterialPageRoute(builder: (_) => EquipoDetalleView(codigo: detalle.equipo.codigo))),
      child: Row(
        children: [
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(detalle.equipo.codigo,
                    style: const TextStyle(color: Tema.etiqueta, fontSize: 12, fontWeight: FontWeight.w700)),
                const SizedBox(height: 2),
                Text(detalle.equipo.nombre, style: const TextStyle(fontWeight: FontWeight.w600)),
                if (detalle.devuelto) ...[
                  const SizedBox(height: 3),
                  Text(
                    'Devuelto el ${Formato.fechaHora(detalle.fechaDevolucion!)}'
                    '${detalle.observacion == null ? '' : ' · ${detalle.observacion}'}',
                    style: Theme.of(context).textTheme.bodySmall,
                  ),
                ],
              ],
            ),
          ),
          const SizedBox(width: 8),
          estado,
        ],
      ),
    );
  }
}

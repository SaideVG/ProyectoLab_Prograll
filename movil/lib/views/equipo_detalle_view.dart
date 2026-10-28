import 'package:flutter/material.dart';

import '../config/tema.dart';
import '../models/equipo.dart';
import '../models/prestamo.dart';
import '../services/api_service.dart';
import '../util/formato.dart';
import '../widgets/componentes.dart';
import 'mantenimientos_view.dart';
import 'prestamo_detalle_view.dart';

/// Ficha de un equipo: lo que se ve al escanear su QR.
/// Muestra sus datos, su estado actual y su historial de préstamos y mantenimientos.
class EquipoDetalleView extends StatelessWidget {
  final String codigo;

  const EquipoDetalleView({super.key, required this.codigo});

  @override
  Widget build(BuildContext context) => Scaffold(
        appBar: AppBar(title: Text(codigo)),
        body: VistaAsincrona<EquipoDetalle>(
          cargar: () => ApiService.instancia.equipo(codigo),
          padding: const EdgeInsets.fromLTRB(16, 8, 16, 32),
          construir: (context, detalle) => [
            _Encabezado(equipo: detalle.equipo),
            const TituloSeccion('Datos'),
            Tarjeta(
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
              child: Column(
                children: [
                  FilaDato('Tipo', detalle.equipo.tipo),
                  FilaDato('Marca', detalle.equipo.marca),
                  FilaDato('Modelo', detalle.equipo.modelo),
                  FilaDato('N° de serie', Formato.texto(detalle.equipo.numeroSerie)),
                  FilaDato('Laboratorio', '${detalle.equipo.laboratorioNombre} (${detalle.equipo.laboratorioCodigo})'),
                  FilaDato('Observaciones', Formato.texto(detalle.equipo.observaciones)),
                ],
              ),
            ),
            TituloSeccion('Préstamos (${detalle.prestamos.length})'),
            if (detalle.prestamos.isEmpty)
              const _SinRegistros('Este equipo no ha sido prestado.')
            else
              for (final prestamo in detalle.prestamos)
                Padding(padding: const EdgeInsets.only(bottom: 10), child: _ItemHistorial(prestamo: prestamo)),
            TituloSeccion('Mantenimientos (${detalle.mantenimientos.length})'),
            if (detalle.mantenimientos.isEmpty)
              const _SinRegistros('Este equipo no ha tenido mantenimientos.')
            else
              for (final mantenimiento in detalle.mantenimientos)
                Padding(
                  padding: const EdgeInsets.only(bottom: 10),
                  child: TarjetaMantenimiento(mantenimiento: mantenimiento, mostrarEquipo: false),
                ),
          ],
        ),
      );
}

class _Encabezado extends StatelessWidget {
  final Equipo equipo;

  const _Encabezado({required this.equipo});

  @override
  Widget build(BuildContext context) {
    final color = Formato.colorEstadoEquipo(equipo.estado);

    return Tarjeta(
      padding: const EdgeInsets.all(20),
      colorBorde: color.withValues(alpha: 0.35),
      child: Row(
        children: [
          Container(
            width: 56,
            height: 56,
            decoration: BoxDecoration(color: color.withValues(alpha: 0.12), borderRadius: BorderRadius.circular(16)),
            child: Icon(Formato.iconoEstadoEquipo(equipo.estado), color: color, size: 28),
          ),
          const SizedBox(width: 16),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(equipo.nombre, style: Theme.of(context).textTheme.titleLarge),
                const SizedBox(height: 8),
                ChipEstado(
                  texto: Formato.estadoEquipo(equipo.estado),
                  color: color,
                  icono: Formato.iconoEstadoEquipo(equipo.estado),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

class _ItemHistorial extends StatelessWidget {
  final HistorialPrestamo prestamo;

  const _ItemHistorial({required this.prestamo});

  @override
  Widget build(BuildContext context) {
    final String estado;
    final Color color;
    if (prestamo.devuelto) {
      estado = 'Devuelto ${Formato.fechaCorta(prestamo.fechaDevolucion!)} · ${Formato.condicion(prestamo.condicion)}';
      color = prestamo.condicion == 'DANADO' ? Tema.peligro : Tema.exito;
    } else if (prestamo.fechaEsperadaDevolucion.isBefore(DateTime.now())) {
      estado = 'Pendiente · venció ${Formato.relativo(prestamo.fechaEsperadaDevolucion)}';
      color = Tema.peligro;
    } else {
      estado = 'Pendiente · vence ${Formato.relativo(prestamo.fechaEsperadaDevolucion)}';
      color = Tema.primario;
    }

    return Tarjeta(
      padding: const EdgeInsets.all(14),
      onTap: () => Navigator.of(context)
          .push(MaterialPageRoute(builder: (_) => PrestamoDetalleView(idPrestamo: prestamo.idPrestamo))),
      child: Row(
        children: [
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text('Préstamo N° ${prestamo.idPrestamo} · ${Formato.fechaCorta(prestamo.fechaPrestamo)}',
                    style: const TextStyle(color: Tema.etiqueta, fontSize: 12, fontWeight: FontWeight.w700)),
                const SizedBox(height: 3),
                Text(prestamo.solicitanteNombre, style: const TextStyle(fontWeight: FontWeight.w600)),
                const SizedBox(height: 3),
                Text(estado, style: TextStyle(color: color, fontSize: 13)),
              ],
            ),
          ),
          const Icon(Icons.chevron_right, color: Tema.textoSuave),
        ],
      ),
    );
  }
}

class _SinRegistros extends StatelessWidget {
  final String texto;

  const _SinRegistros(this.texto);

  @override
  Widget build(BuildContext context) => Tarjeta(
        color: Tema.panel,
        child: Text(texto, style: Theme.of(context).textTheme.bodySmall),
      );
}

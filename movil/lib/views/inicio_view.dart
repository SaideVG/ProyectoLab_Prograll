import 'package:flutter/material.dart';

import '../config/tema.dart';
import '../models/dashboard.dart';
import '../services/api_service.dart';
import '../services/sesion.dart';
import '../util/formato.dart';
import '../widgets/componentes.dart';
import 'cuenta_view.dart';

/// Resumen del laboratorio: las mismas cantidades del menú principal del escritorio.
class InicioView extends StatefulWidget {
  final void Function(String? estado) irAEquipos;
  final void Function(String filtro) irAPrestamos;
  final VoidCallback escanear;

  const InicioView({super.key, required this.irAEquipos, required this.irAPrestamos, required this.escanear});

  @override
  State<InicioView> createState() => _InicioViewState();
}

class _InicioViewState extends State<InicioView> {
  @override
  Widget build(BuildContext context) {
    final usuario = Sesion.usuario!;

    return Scaffold(
      appBar: AppBar(
        toolbarHeight: 72,
        title: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text('Hola, ${usuario.nombre}', style: Theme.of(context).textTheme.titleLarge),
            const SizedBox(height: 2),
            Text(Formato.capitalizar(usuario.rol), style: Theme.of(context).textTheme.bodySmall),
          ],
        ),
        actions: [
          Padding(
            padding: const EdgeInsets.only(right: 12),
            child: InkResponse(
              onTap: () => mostrarCuenta(context),
              radius: 26,
              child: CircleAvatar(
                radius: 20,
                backgroundColor: Tema.azulClaro,
                child: Text(usuario.iniciales,
                    style: const TextStyle(color: Tema.primario, fontWeight: FontWeight.w700, fontSize: 14)),
              ),
            ),
          ),
        ],
      ),
      body: VistaAsincrona<Dashboard>(
        cargar: ApiService.instancia.dashboard,
        construir: (context, datos) => [
          const TituloSeccion('Equipos'),
          _TarjetaEquipos(datos: datos, alElegirEstado: widget.irAEquipos),
          const TituloSeccion('Préstamos'),
          Row(
            children: [
              Expanded(
                child: _TarjetaNumero(
                  titulo: 'Activos',
                  valor: datos.prestamosActivos,
                  icono: Icons.assignment_outlined,
                  color: Tema.primario,
                  onTap: () => widget.irAPrestamos('ACTIVOS'),
                ),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: _TarjetaNumero(
                  titulo: 'Vencidos',
                  valor: datos.prestamosVencidos,
                  icono: Icons.schedule,
                  color: datos.prestamosVencidos > 0 ? Tema.peligro : Tema.textoSuave,
                  resaltar: datos.prestamosVencidos > 0,
                  onTap: () => widget.irAPrestamos('VENCIDOS'),
                ),
              ),
            ],
          ),
          const SizedBox(height: 24),
          _TarjetaEscanear(onTap: widget.escanear),
        ],
      ),
    );
  }
}

/// Total de equipos, una barra con la proporción de cada estado y la cantidad por estado.
class _TarjetaEquipos extends StatelessWidget {
  final Dashboard datos;
  final void Function(String? estado) alElegirEstado;

  const _TarjetaEquipos({required this.datos, required this.alElegirEstado});

  @override
  Widget build(BuildContext context) {
    final porEstado = datos.porEstado;

    return Tarjeta(
      padding: const EdgeInsets.fromLTRB(18, 18, 18, 8),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          InkWell(
            borderRadius: BorderRadius.circular(8),
            onTap: () => alElegirEstado(null),
            child: Row(
              crossAxisAlignment: CrossAxisAlignment.end,
              children: [
                Text('${datos.totalEquipos}',
                    style: const TextStyle(fontSize: 40, fontWeight: FontWeight.w800, height: 1)),
                const SizedBox(width: 10),
                const Padding(
                  padding: EdgeInsets.only(bottom: 4),
                  child: Text('equipos registrados', style: TextStyle(color: Tema.textoSuave)),
                ),
                const Spacer(),
                const Icon(Icons.chevron_right, color: Tema.textoSuave),
              ],
            ),
          ),
          const SizedBox(height: 16),
          _BarraEstados(porEstado: porEstado, total: datos.totalEquipos),
          const SizedBox(height: 8),
          for (final estado in Formato.estadosEquipo)
            _FilaEstado(
              estado: estado,
              cantidad: porEstado[estado] ?? 0,
              onTap: () => alElegirEstado(estado),
            ),
        ],
      ),
    );
  }
}

/// Barra horizontal dividida en segmentos, uno por estado, proporcional a la cantidad.
class _BarraEstados extends StatelessWidget {
  final Map<String, int> porEstado;
  final int total;

  const _BarraEstados({required this.porEstado, required this.total});

  @override
  Widget build(BuildContext context) {
    if (total == 0) {
      return Container(height: 8, decoration: BoxDecoration(color: Tema.azulClaro, borderRadius: BorderRadius.circular(4)));
    }
    final segmentos = porEstado.entries.where((e) => e.value > 0).toList();

    return ClipRRect(
      borderRadius: BorderRadius.circular(4),
      child: SizedBox(
        height: 8,
        child: Row(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            for (var i = 0; i < segmentos.length; i++) ...[
              if (i > 0) const SizedBox(width: 2),
              Expanded(
                flex: segmentos[i].value,
                child: ColoredBox(color: Formato.colorEstadoEquipo(segmentos[i].key)),
              ),
            ],
          ],
        ),
      ),
    );
  }
}

class _FilaEstado extends StatelessWidget {
  final String estado;
  final int cantidad;
  final VoidCallback onTap;

  const _FilaEstado({required this.estado, required this.cantidad, required this.onTap});

  @override
  Widget build(BuildContext context) => InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(8),
        child: Padding(
          padding: const EdgeInsets.symmetric(vertical: 10),
          child: Row(
            children: [
              Container(
                width: 10,
                height: 10,
                decoration: BoxDecoration(color: Formato.colorEstadoEquipo(estado), shape: BoxShape.circle),
              ),
              const SizedBox(width: 12),
              Expanded(child: Text(Formato.estadoEquipo(estado))),
              Text('$cantidad',
                  style: TextStyle(
                    fontWeight: FontWeight.w700,
                    color: cantidad == 0 ? Tema.placeholder : Tema.texto,
                  )),
            ],
          ),
        ),
      );
}

class _TarjetaNumero extends StatelessWidget {
  final String titulo;
  final int valor;
  final IconData icono;
  final Color color;
  final bool resaltar;
  final VoidCallback onTap;

  const _TarjetaNumero({
    required this.titulo,
    required this.valor,
    required this.icono,
    required this.color,
    required this.onTap,
    this.resaltar = false,
  });

  @override
  Widget build(BuildContext context) => Tarjeta(
        onTap: onTap,
        colorBorde: resaltar ? Tema.peligro.withValues(alpha: 0.45) : null,
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Icon(icono, color: color, size: 22),
            const SizedBox(height: 14),
            Text('$valor', style: TextStyle(fontSize: 30, fontWeight: FontWeight.w800, color: resaltar ? Tema.peligro : Tema.texto)),
            const SizedBox(height: 2),
            Text(titulo, style: const TextStyle(color: Tema.textoSuave)),
          ],
        ),
      );
}

class _TarjetaEscanear extends StatelessWidget {
  final VoidCallback onTap;

  const _TarjetaEscanear({required this.onTap});

  @override
  Widget build(BuildContext context) => Tarjeta(
        onTap: onTap,
        color: Tema.azulClaro.withValues(alpha: 0.5),
        colorBorde: Tema.primario.withValues(alpha: 0.35),
        child: const Row(
          children: [
            Icon(Icons.qr_code_scanner, color: Tema.primario, size: 30),
            SizedBox(width: 16),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text('Escanear código QR', style: TextStyle(fontWeight: FontWeight.w700, fontSize: 15)),
                  SizedBox(height: 3),
                  Text('Consulte el estado y el historial de un equipo',
                      style: TextStyle(color: Tema.textoSuave, fontSize: 13)),
                ],
              ),
            ),
            Icon(Icons.chevron_right, color: Tema.primario),
          ],
        ),
      );
}

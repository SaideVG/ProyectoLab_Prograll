import 'dart:async';

import 'package:flutter/material.dart';

import '../config/tema.dart';
import '../models/equipo.dart';
import '../services/api_service.dart';
import '../util/formato.dart';
import '../widgets/componentes.dart';
import 'equipo_detalle_view.dart';

/// Inventario de equipos con búsqueda por código o nombre y filtro por estado.
class EquiposView extends StatefulWidget {
  final String? estadoInicial;

  const EquiposView({super.key, this.estadoInicial});

  @override
  State<EquiposView> createState() => _EquiposViewState();
}

class _EquiposViewState extends State<EquiposView> {
  final _txtBuscar = TextEditingController();
  Timer? _espera;

  late String? _estado = widget.estadoInicial;
  String _texto = '';

  @override
  void dispose() {
    _espera?.cancel();
    _txtBuscar.dispose();
    super.dispose();
  }

  /// Espera a que el usuario deje de escribir para no consultar la API en cada letra.
  void _alEscribir(String texto) {
    _espera?.cancel();
    _espera = Timer(const Duration(milliseconds: 400), () => setState(() => _texto = texto.trim()));
  }

  @override
  Widget build(BuildContext context) => Scaffold(
        appBar: AppBar(
          title: const Text('Equipos'),
          bottom: PreferredSize(
            preferredSize: const Size.fromHeight(120),
            child: Contenido(
              child: Column(
                children: [
                  Padding(
                    padding: const EdgeInsets.fromLTRB(16, 4, 16, 10),
                    child: TextField(
                      controller: _txtBuscar,
                      onChanged: _alEscribir,
                      textInputAction: TextInputAction.search,
                      decoration: InputDecoration(
                        hintText: 'Buscar por código o nombre',
                        prefixIcon: const Icon(Icons.search),
                        isDense: true,
                        suffixIcon: _txtBuscar.text.isEmpty
                            ? null
                            : IconButton(
                                tooltip: 'Limpiar',
                                icon: const Icon(Icons.close, size: 18),
                                onPressed: () {
                                  _txtBuscar.clear();
                                  setState(() => _texto = '');
                                },
                              ),
                      ),
                    ),
                  ),
                  SizedBox(
                    height: 44,
                    child: ListView(
                      scrollDirection: Axis.horizontal,
                      padding: const EdgeInsets.symmetric(horizontal: 16),
                      children: [
                        _filtro(null, 'Todos'),
                        for (final estado in Formato.estadosEquipo) _filtro(estado, Formato.estadoEquipo(estado)),
                      ],
                    ),
                  ),
                  const SizedBox(height: 8),
                ],
              ),
            ),
          ),
        ),
        body: VistaAsincrona<List<Equipo>>(
          key: ValueKey('$_estado|$_texto'),
          cargar: () => ApiService.instancia.equipos(estado: _estado, buscar: _texto),
          construir: (context, equipos) => equipos.isEmpty
              ? [
                  const MensajeCentral(
                    icono: Icons.search_off,
                    titulo: 'Sin resultados',
                    detalle: 'No hay equipos que coincidan con la búsqueda.',
                  ),
                ]
              : [
                  Padding(
                    padding: const EdgeInsets.fromLTRB(4, 8, 4, 10),
                    child: Text('${equipos.length} equipo${equipos.length == 1 ? '' : 's'}',
                        style: Theme.of(context).textTheme.bodySmall),
                  ),
                  for (final equipo in equipos)
                    Padding(
                      padding: const EdgeInsets.only(bottom: 10),
                      child: TarjetaEquipo(equipo: equipo),
                    ),
                ],
        ),
      );

  Widget _filtro(String? estado, String texto) => Padding(
        padding: const EdgeInsets.only(right: 8),
        child: FilterChip(
          label: Text(texto),
          selected: _estado == estado,
          showCheckmark: false,
          onSelected: (_) => setState(() => _estado = estado),
        ),
      );
}

/// Un equipo en una lista. Al tocarlo abre su detalle.
class TarjetaEquipo extends StatelessWidget {
  final Equipo equipo;

  const TarjetaEquipo({super.key, required this.equipo});

  @override
  Widget build(BuildContext context) {
    final color = Formato.colorEstadoEquipo(equipo.estado);

    return Tarjeta(
      padding: const EdgeInsets.all(14),
      onTap: () => Navigator.of(context)
          .push(MaterialPageRoute(builder: (_) => EquipoDetalleView(codigo: equipo.codigo))),
      child: Row(
        children: [
          Container(
            width: 44,
            height: 44,
            decoration: BoxDecoration(color: color.withValues(alpha: 0.12), borderRadius: BorderRadius.circular(12)),
            child: Icon(Formato.iconoEstadoEquipo(equipo.estado), color: color, size: 22),
          ),
          const SizedBox(width: 14),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(equipo.codigo,
                    style: const TextStyle(color: Tema.etiqueta, fontSize: 12, fontWeight: FontWeight.w700, letterSpacing: 0.5)),
                const SizedBox(height: 2),
                Text(equipo.nombre,
                    style: const TextStyle(fontWeight: FontWeight.w600, fontSize: 15), overflow: TextOverflow.ellipsis),
                const SizedBox(height: 2),
                Text('${equipo.tipo} · ${equipo.laboratorioNombre}',
                    style: Theme.of(context).textTheme.bodySmall, overflow: TextOverflow.ellipsis),
              ],
            ),
          ),
          const SizedBox(width: 8),
          ChipEstado(texto: Formato.estadoEquipo(equipo.estado), color: color),
        ],
      ),
    );
  }
}

import 'package:flutter/material.dart';

import 'equipos_view.dart';
import 'escaner_view.dart';
import 'inicio_view.dart';
import 'mantenimientos_view.dart';
import 'prestamos_view.dart';

/// Pantalla principal con la barra de navegación inferior.
/// Es el equivalente del MainForm del escritorio.
class PrincipalView extends StatefulWidget {
  const PrincipalView({super.key});

  @override
  State<PrincipalView> createState() => _PrincipalViewState();
}

class _PrincipalViewState extends State<PrincipalView> {
  int _pestana = 0;

  // Filtros con los que se abren las pestañas cuando se llega desde el Inicio.
  String? _estadoEquipos;
  String _filtroPrestamos = 'ACTIVOS';

  // Cambian cada vez que se navega desde el Inicio para que la pestaña recargue con el filtro nuevo.
  int _versionEquipos = 0;
  int _versionPrestamos = 0;

  void _irAEquipos(String? estado) => setState(() {
        _estadoEquipos = estado;
        _versionEquipos++;
        _pestana = 1;
      });

  void _irAPrestamos(String filtro) => setState(() {
        _filtroPrestamos = filtro;
        _versionPrestamos++;
        _pestana = 2;
      });

  void _escanear() => Navigator.of(context).push(MaterialPageRoute(builder: (_) => const EscanerView()));

  @override
  Widget build(BuildContext context) {
    final pestanas = [
      InicioView(irAEquipos: _irAEquipos, irAPrestamos: _irAPrestamos, escanear: _escanear),
      EquiposView(key: ValueKey('equipos$_versionEquipos'), estadoInicial: _estadoEquipos),
      PrestamosView(key: ValueKey('prestamos$_versionPrestamos'), filtroInicial: _filtroPrestamos),
      const MantenimientosView(),
    ];

    return Scaffold(
      body: IndexedStack(index: _pestana, children: pestanas),
      // En el Inicio ya está la tarjeta "Escanear código QR"; el botón flotante se usa en Equipos.
      floatingActionButton: _pestana == 1
          ? FloatingActionButton(
              tooltip: 'Escanear código QR',
              onPressed: _escanear,
              child: const Icon(Icons.qr_code_scanner),
            )
          : null,
      bottomNavigationBar: NavigationBar(
        selectedIndex: _pestana,
        onDestinationSelected: (indice) => setState(() => _pestana = indice),
        destinations: const [
          NavigationDestination(icon: Icon(Icons.space_dashboard_outlined), selectedIcon: Icon(Icons.space_dashboard), label: 'Inicio'),
          NavigationDestination(icon: Icon(Icons.devices_other_outlined), selectedIcon: Icon(Icons.devices_other), label: 'Equipos'),
          NavigationDestination(icon: Icon(Icons.assignment_outlined), selectedIcon: Icon(Icons.assignment), label: 'Préstamos'),
          NavigationDestination(icon: Icon(Icons.build_outlined), selectedIcon: Icon(Icons.build), label: 'Mantenimiento'),
        ],
      ),
    );
  }
}

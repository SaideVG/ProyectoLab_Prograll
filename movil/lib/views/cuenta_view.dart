import 'package:flutter/material.dart';

import '../config/tema.dart';
import '../services/api_service.dart';
import '../services/sesion.dart';
import '../util/formato.dart';
import '../widgets/componentes.dart';
import 'login_view.dart';

/// Datos del usuario, servidor y cierre de sesión.
Future<void> mostrarCuenta(BuildContext context) => showModalBottomSheet<void>(
      context: context,
      builder: (_) => const _CuentaView(),
    );

class _CuentaView extends StatelessWidget {
  const _CuentaView();

  Future<void> _cerrarSesion(BuildContext context) async {
    final navegador = Navigator.of(context);
    await ApiService.instancia.cerrarSesion();
    await Sesion.cerrar();
    navegador.pushAndRemoveUntil(MaterialPageRoute(builder: (_) => const LoginView()), (_) => false);
  }

  @override
  Widget build(BuildContext context) {
    final usuario = Sesion.usuario!;

    return SafeArea(
      child: Padding(
        padding: const EdgeInsets.fromLTRB(24, 0, 24, 24),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Row(
              children: [
                CircleAvatar(
                  radius: 26,
                  backgroundColor: Tema.azulClaro,
                  child: Text(usuario.iniciales,
                      style: const TextStyle(color: Tema.primario, fontWeight: FontWeight.w700, fontSize: 17)),
                ),
                const SizedBox(width: 14),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(usuario.nombreCompleto, style: Theme.of(context).textTheme.titleMedium),
                      const SizedBox(height: 2),
                      Text('@${usuario.usuario}', style: Theme.of(context).textTheme.bodySmall),
                    ],
                  ),
                ),
                ChipEstado(texto: Formato.capitalizar(usuario.rol), color: Tema.primario),
              ],
            ),
            const SizedBox(height: 16),
            const Divider(),
            const SizedBox(height: 8),
            FilaDato('Correo', usuario.correo),
            FilaDato('Servidor', Sesion.servidor),
            const SizedBox(height: 8),
            Text(
              'La app es de consulta. Los préstamos, devoluciones y mantenimientos '
              'se registran en el sistema de escritorio.',
              style: Theme.of(context).textTheme.bodySmall,
            ),
            const SizedBox(height: 20),
            OutlinedButton.icon(
              style: OutlinedButton.styleFrom(
                foregroundColor: Tema.peligro,
                side: BorderSide(color: Tema.peligro.withValues(alpha: 0.5)),
              ),
              onPressed: () => _cerrarSesion(context),
              icon: const Icon(Icons.logout, size: 18),
              label: const Text('Cerrar sesión'),
            ),
          ],
        ),
      ),
    );
  }
}

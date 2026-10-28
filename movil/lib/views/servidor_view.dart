import 'package:flutter/material.dart';

import '../config/tema.dart';
import '../services/api_service.dart';
import '../services/sesion.dart';

/// Hoja inferior para escribir la dirección de la API y probar la conexión.
Future<void> mostrarServidor(BuildContext context) => showModalBottomSheet<void>(
      context: context,
      isScrollControlled: true,
      builder: (_) => const _ServidorView(),
    );

class _ServidorView extends StatefulWidget {
  const _ServidorView();

  @override
  State<_ServidorView> createState() => _ServidorViewState();
}

class _ServidorViewState extends State<_ServidorView> {
  late final _txtDireccion = TextEditingController(text: Sesion.servidor);

  bool _probando = false;
  String? _resultado;
  bool _exito = false;

  @override
  void dispose() {
    _txtDireccion.dispose();
    super.dispose();
  }

  Future<void> _probar() async {
    setState(() {
      _probando = true;
      _resultado = null;
      // Se muestra la dirección completa que se va a usar (por ejemplo, con el https:// agregado).
      _txtDireccion.text = ApiService.normalizarDireccion(_txtDireccion.text);
    });
    try {
      final baseDeDatos = await ApiService.instancia.probarServidor(_txtDireccion.text);
      _exito = baseDeDatos == 'conectada';
      _resultado = _exito
          ? 'Conexión correcta: la API y la base de datos responden.'
          : 'La API responde, pero no puede conectarse a la base de datos.';
    } on ApiException catch (e) {
      _exito = false;
      _resultado = e.mensaje;
    } finally {
      if (mounted) setState(() => _probando = false);
    }
  }

  Future<void> _guardar() async {
    if (_txtDireccion.text.trim().isEmpty) {
      setState(() {
        _exito = false;
        _resultado = 'Escriba la dirección del servidor.';
      });
      return;
    }
    await Sesion.cambiarServidor(_txtDireccion.text);
    if (mounted) Navigator.of(context).pop();
  }

  @override
  Widget build(BuildContext context) => Padding(
        padding: EdgeInsets.fromLTRB(24, 0, 24, 24 + MediaQuery.viewInsetsOf(context).bottom),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Text('Servidor de SIGELAB', style: Theme.of(context).textTheme.titleLarge),
            const SizedBox(height: 6),
            Text(
              'Dirección de la API que le compartió el administrador:\n'
              '•  Por internet (túnel): https://….trycloudflare.com\n'
              '•  En la misma red Wi-Fi: la IP de la computadora, por ejemplo 192.168.0.8:8080\n'
              '•  En el emulador: http://10.0.2.2:8080',
              style: Theme.of(context).textTheme.bodySmall?.copyWith(height: 1.5),
            ),
            const SizedBox(height: 18),
            TextField(
              controller: _txtDireccion,
              keyboardType: TextInputType.url,
              autocorrect: false,
              decoration: const InputDecoration(
                labelText: 'Dirección',
                hintText: 'https://….trycloudflare.com',
                prefixIcon: Icon(Icons.link),
              ),
              onChanged: (_) => setState(() => _resultado = null),
            ),
            if (_resultado != null) ...[
              const SizedBox(height: 14),
              Row(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Icon(_exito ? Icons.check_circle_outline : Icons.error_outline,
                      size: 18, color: _exito ? Tema.exito : Tema.peligro),
                  const SizedBox(width: 8),
                  Expanded(
                    child: Text(_resultado!,
                        style: TextStyle(color: _exito ? Tema.exito : Tema.peligro, fontSize: 13.5)),
                  ),
                ],
              ),
            ],
            const SizedBox(height: 20),
            Row(
              children: [
                Expanded(
                  child: OutlinedButton(
                    onPressed: _probando ? null : _probar,
                    child: _probando
                        ? const SizedBox(width: 18, height: 18, child: CircularProgressIndicator(strokeWidth: 2))
                        : const Text('Probar'),
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: FilledButton(
                    style: FilledButton.styleFrom(minimumSize: const Size.fromHeight(48)),
                    onPressed: _guardar,
                    child: const Text('Guardar'),
                  ),
                ),
              ],
            ),
          ],
        ),
      );
}

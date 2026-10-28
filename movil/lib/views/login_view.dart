import 'package:flutter/material.dart';

import '../config/tema.dart';
import '../services/api_service.dart';
import '../services/sesion.dart';
import '../widgets/componentes.dart';
import 'principal_view.dart';
import 'servidor_view.dart';

/// Inicio de sesión con los mismos usuarios del sistema de escritorio.
class LoginView extends StatefulWidget {
  /// Aviso opcional, por ejemplo cuando la sesión venció.
  final String? mensaje;

  const LoginView({super.key, this.mensaje});

  @override
  State<LoginView> createState() => _LoginViewState();
}

class _LoginViewState extends State<LoginView> {
  static const _mensajeSinServidor = 'Primero escriba la dirección del servidor.';

  final _txtUsuario = TextEditingController();
  final _txtPassword = TextEditingController();
  final _focoPassword = FocusNode();

  bool _ocultarPassword = true;
  bool _cargando = false;
  late String? _error = widget.mensaje;

  @override
  void dispose() {
    _txtUsuario.dispose();
    _txtPassword.dispose();
    _focoPassword.dispose();
    super.dispose();
  }

  Future<void> _ingresar() async {
    FocusScope.of(context).unfocus();

    // En la APK final la app no trae dirección: primero hay que escribirla.
    if (!Sesion.servidorConfigurado) {
      setState(() => _error = _mensajeSinServidor);
      await _configurarServidor();
      return;
    }

    setState(() {
      _cargando = true;
      _error = null;
    });

    try {
      final resultado = await ApiService.instancia.iniciarSesion(_txtUsuario.text, _txtPassword.text);
      await Sesion.iniciar(resultado);
      if (!mounted) return;
      Navigator.of(context).pushReplacement(MaterialPageRoute(builder: (_) => const PrincipalView()));
    } on ApiException catch (e) {
      // Sin código HTTP significa que ni siquiera se llegó a la API: casi siempre es la dirección
      // (por ejemplo, el túnel se reinició y cambió de dirección).
      setState(() => _error = e.codigoHttp == null
          ? '${e.mensaje}\nToque «Servidor» (abajo) para cambiar la dirección.'
          : e.mensaje);
    } finally {
      if (mounted) setState(() => _cargando = false);
    }
  }

  Future<void> _configurarServidor() async {
    await mostrarServidor(context);
    if (!mounted) return;
    setState(() {
      if (Sesion.servidorConfigurado && _error == _mensajeSinServidor) _error = null;
    });
  }

  @override
  Widget build(BuildContext context) {
    final textos = Theme.of(context).textTheme;

    return Scaffold(
      body: SafeArea(
        child: Center(
          child: SingleChildScrollView(
            padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 32),
            child: ConstrainedBox(
              constraints: const BoxConstraints(maxWidth: 420),
              child: AutofillGroup(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  children: [
                    const _Logo(),
                    const SizedBox(height: 28),
                    Text('SIGELAB', style: textos.headlineMedium),
                    const SizedBox(height: 6),
                    Text('Gestión y Control de Laboratorios Universitarios', style: textos.bodySmall),
                    const SizedBox(height: 36),
                    TextField(
                      controller: _txtUsuario,
                      enabled: !_cargando,
                      textInputAction: TextInputAction.next,
                      autocorrect: false,
                      autofillHints: const [AutofillHints.username],
                      decoration: const InputDecoration(
                        labelText: 'Usuario',
                        prefixIcon: Icon(Icons.person_outline),
                      ),
                      onSubmitted: (_) => _focoPassword.requestFocus(),
                    ),
                    const SizedBox(height: 14),
                    TextField(
                      controller: _txtPassword,
                      focusNode: _focoPassword,
                      enabled: !_cargando,
                      obscureText: _ocultarPassword,
                      textInputAction: TextInputAction.done,
                      autofillHints: const [AutofillHints.password],
                      decoration: InputDecoration(
                        labelText: 'Contraseña',
                        prefixIcon: const Icon(Icons.lock_outline),
                        suffixIcon: IconButton(
                          tooltip: _ocultarPassword ? 'Mostrar contraseña' : 'Ocultar contraseña',
                          icon: Icon(_ocultarPassword ? Icons.visibility_outlined : Icons.visibility_off_outlined),
                          onPressed: () => setState(() => _ocultarPassword = !_ocultarPassword),
                        ),
                      ),
                      onSubmitted: (_) => _ingresar(),
                    ),
                    AnimatedSize(
                      duration: const Duration(milliseconds: 200),
                      child: _error == null
                          ? const SizedBox(height: 24)
                          : Padding(
                              padding: const EdgeInsets.symmetric(vertical: 16),
                              child: Row(
                                crossAxisAlignment: CrossAxisAlignment.start,
                                children: [
                                  const Icon(Icons.error_outline, color: Tema.peligro, size: 18),
                                  const SizedBox(width: 8),
                                  Expanded(
                                    child: Text(_error!, style: const TextStyle(color: Tema.peligro, fontSize: 13.5)),
                                  ),
                                ],
                              ),
                            ),
                    ),
                    FilledButton(
                      onPressed: _cargando ? null : _ingresar,
                      child: _cargando
                          ? const SizedBox(
                              width: 22,
                              height: 22,
                              child: CircularProgressIndicator(strokeWidth: 2.5, color: Tema.primario),
                            )
                          : const Text('Ingresar'),
                    ),
                    const SizedBox(height: 40),
                    _BotonServidor(direccion: Sesion.servidor, onTap: _cargando ? null : _configurarServidor),
                  ],
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }
}

/// Ícono de la app: un matraz de laboratorio dentro de un cuadro cian.
class _Logo extends StatelessWidget {
  const _Logo();

  @override
  Widget build(BuildContext context) => Align(
        alignment: Alignment.centerLeft,
        child: Container(
          width: 56,
          height: 56,
          decoration: BoxDecoration(
            color: Tema.azulClaro,
            borderRadius: BorderRadius.circular(16),
            border: Border.all(color: Tema.primario.withValues(alpha: 0.5)),
          ),
          child: const Icon(Icons.science_outlined, color: Tema.primario, size: 30),
        ),
      );
}

class _BotonServidor extends StatelessWidget {
  final String direccion;
  final VoidCallback? onTap;

  const _BotonServidor({required this.direccion, required this.onTap});

  @override
  Widget build(BuildContext context) {
    final sinConfigurar = direccion.trim().isEmpty;

    // Sin dirección, el botón se resalta en cian para que se note que falta.
    return Tarjeta(
      onTap: onTap,
      color: sinConfigurar ? Tema.azulClaro : Tema.panel,
      colorBorde: sinConfigurar ? Tema.primario : null,
      padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 12),
      child: Row(
        children: [
          Icon(Icons.dns_outlined, size: 18, color: sinConfigurar ? Tema.primario : Tema.textoSuave),
          const SizedBox(width: 10),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                const Text('Servidor', style: TextStyle(color: Tema.textoSuave, fontSize: 12)),
                Text(
                  sinConfigurar ? 'Sin configurar' : direccion,
                  style: const TextStyle(fontSize: 13.5),
                  overflow: TextOverflow.ellipsis,
                ),
              ],
            ),
          ),
          Text(sinConfigurar ? 'Configurar' : 'Cambiar',
              style: const TextStyle(color: Tema.etiqueta, fontWeight: FontWeight.w600, fontSize: 13)),
        ],
      ),
    );
  }
}

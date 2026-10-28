import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

import 'config/tema.dart';
import 'services/api_service.dart';
import 'services/sesion.dart';
import 'views/login_view.dart';
import 'views/principal_view.dart';

/// Permite cambiar de pantalla desde fuera de un widget
/// (por ejemplo, cuando la API avisa que la sesión venció).
final navegador = GlobalKey<NavigatorState>();

Future<void> main() async {
  WidgetsFlutterBinding.ensureInitialized();
  SystemChrome.setSystemUIOverlayStyle(SystemUiOverlayStyle.light.copyWith(
    statusBarColor: Colors.transparent,
    systemNavigationBarColor: Tema.superficie,
  ));

  final haySesion = await Sesion.cargar();

  // Si un administrador desactiva al usuario o el token vence, la API
  // responde 401 y la app regresa al login.
  ApiService.instancia.alVencerSesion = () async {
    await Sesion.cerrar();
    navegador.currentState?.pushAndRemoveUntil(
      MaterialPageRoute(builder: (_) => const LoginView(mensaje: 'Su sesión terminó. Inicie sesión de nuevo.')),
      (_) => false,
    );
  };

  runApp(SigelabApp(haySesion: haySesion));
}

class SigelabApp extends StatelessWidget {
  final bool haySesion;

  const SigelabApp({super.key, required this.haySesion});

  @override
  Widget build(BuildContext context) => MaterialApp(
        title: 'SIGELAB',
        debugShowCheckedModeBanner: false,
        navigatorKey: navegador,
        theme: Tema.oscuro,
        home: haySesion ? const PrincipalView() : const LoginView(),
        // En el navegador de una computadora la app se muestra con ancho de celular, centrada.
        builder: (context, pantalla) => ColoredBox(
          color: Tema.fondo,
          child: Center(
            child: ConstrainedBox(
              constraints: const BoxConstraints(maxWidth: 520),
              child: pantalla,
            ),
          ),
        ),
      );
}

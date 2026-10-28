import 'dart:convert';

import 'package:flutter/foundation.dart';
import 'package:shared_preferences/shared_preferences.dart';

import '../models/usuario.dart';
import 'api_service.dart';

/// Equivale a la clase Sesion del escritorio: guarda quién inició sesión.
/// Además recuerda el token y la dirección del servidor en el celular,
/// para no pedir el login cada vez que se abre la app.
class Sesion {
  Sesion._();

  static const _claveServidor = 'servidor';
  static const _claveToken = 'token';
  static const _claveUsuario = 'usuario';

  static Usuario? usuario;

  /*
   * Dirección por defecto de la API:
   *  - Si la APK se compiló con  --dart-define=SERVIDOR=https://...  se usa esa.
   *  - En el navegador de la computadora, la API está en la misma máquina.
   *  - Al probar en el emulador (debug o profile), 10.0.2.2 es la computadora que lo ejecuta.
   *  - En la APK final no hay ninguna: en un celular real 10.0.2.2 no existe, así que
   *    el login pide escribir la dirección (la del túnel o la IP de la computadora).
   */
  static const _servidorAlCompilar = String.fromEnvironment('SERVIDOR');

  static String get servidorPorDefecto {
    if (_servidorAlCompilar.isNotEmpty) return ApiService.normalizarDireccion(_servidorAlCompilar);
    if (kIsWeb) return 'http://localhost:8080';
    return kReleaseMode ? '' : 'http://10.0.2.2:8080';
  }

  static String get servidor => ApiService.instancia.servidor;

  static bool get servidorConfigurado => servidor.trim().isNotEmpty;

  /// Lee lo guardado al abrir la app. Devuelve true si había una sesión.
  static Future<bool> cargar() async {
    final preferencias = await SharedPreferences.getInstance();
    final api = ApiService.instancia;

    api.servidor = preferencias.getString(_claveServidor) ?? servidorPorDefecto;
    api.token = preferencias.getString(_claveToken);

    final usuarioGuardado = preferencias.getString(_claveUsuario);
    usuario = usuarioGuardado == null
        ? null
        : Usuario.desdeJson(jsonDecode(usuarioGuardado) as Map<String, dynamic>);

    return api.token != null && usuario != null;
  }

  static Future<void> iniciar(ResultadoLogin resultado) async {
    usuario = resultado.usuario;
    ApiService.instancia.token = resultado.token;

    final preferencias = await SharedPreferences.getInstance();
    await preferencias.setString(_claveToken, resultado.token);
    await preferencias.setString(_claveUsuario, jsonEncode(resultado.usuario.aJson()));
  }

  static Future<void> cerrar() async {
    usuario = null;
    ApiService.instancia.token = null;

    final preferencias = await SharedPreferences.getInstance();
    await preferencias.remove(_claveToken);
    await preferencias.remove(_claveUsuario);
  }

  static Future<void> cambiarServidor(String direccion) async {
    final limpia = ApiService.normalizarDireccion(direccion);
    ApiService.instancia.servidor = limpia;
    final preferencias = await SharedPreferences.getInstance();
    await preferencias.setString(_claveServidor, limpia);
  }
}

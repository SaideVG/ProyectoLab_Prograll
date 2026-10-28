import 'dart:async';
import 'dart:convert';

import 'package:flutter/foundation.dart';
import 'package:http/http.dart' as http;

import '../models/dashboard.dart';
import '../models/equipo.dart';
import '../models/mantenimiento.dart';
import '../models/prestamo.dart';
import '../models/usuario.dart';

/// Error que la app le muestra al usuario. El mensaje viene de la API
/// (igual que los mensajes de BusinessException en el escritorio).
class ApiException implements Exception {
  final String mensaje;
  final int? codigoHttp;

  const ApiException(this.mensaje, [this.codigoHttp]);

  /// 401: el token ya no sirve y hay que volver a iniciar sesión.
  bool get sesionVencida => codigoHttp == 401;

  @override
  String toString() => mensaje;
}

/// Respuesta del login: el token que se envía en cada petición y el usuario.
class ResultadoLogin {
  final String token;
  final Usuario usuario;

  const ResultadoLogin(this.token, this.usuario);
}

/// Único punto de la app que habla con la API REST.
/// Cumple el papel de los Repository del escritorio: las pantallas no saben
/// nada de HTTP ni de JSON, solo piden datos y reciben modelos.
class ApiService {
  ApiService._();

  static final ApiService instancia = ApiService._();

  static const _tiempoMaximo = Duration(seconds: 12);

  final http.Client _cliente = http.Client();

  /// Dirección de la API, por ejemplo http://10.0.2.2:8080
  String servidor = '';

  /// Token de la sesión actual. null mientras no se ha iniciado sesión.
  String? token;

  /// Se llama cuando la API responde 401, para regresar al login.
  VoidCallback? alVencerSesion;

  /*
   * ========================================================
   * SESIÓN
   * ========================================================
   */

  Future<ResultadoLogin> iniciarSesion(String usuario, String password) async {
    final json = await _post('/api/auth/login', {'usuario': usuario, 'password': password}, conToken: false);
    return ResultadoLogin(json['token'] as String, Usuario.desdeJson(json['usuario'] as Map<String, dynamic>));
  }

  Future<Usuario> usuarioActual() async =>
      Usuario.desdeJson(await _get('/api/auth/yo') as Map<String, dynamic>);

  Future<void> cerrarSesion() async {
    try {
      await _post('/api/auth/logout', null);
    } on ApiException {
      // Aunque la API no conteste, la sesión se cierra en el celular.
    }
  }

  /// Prueba de conexión contra cualquier dirección (pantalla del servidor).
  Future<String> probarServidor(String direccion) async {
    final limpia = normalizarDireccion(direccion);
    if (limpia.isEmpty) throw const ApiException('Escriba la dirección del servidor.');

    final json = await _enviar(() => _cliente.get(Uri.parse('$limpia/api/estado')), direccion: limpia);
    return json['baseDeDatos'] as String;
  }

  /// Completa lo que se suele omitir al escribir o pegar la dirección:
  ///   abc.trycloudflare.com  ->  https://abc.trycloudflare.com  (internet: siempre https)
  ///   192.168.0.8:8080       ->  http://192.168.0.8:8080        (red local)
  /// y quita la barra final.
  static String normalizarDireccion(String direccion) {
    var limpia = direccion.trim();
    while (limpia.endsWith('/')) {
      limpia = limpia.substring(0, limpia.length - 1);
    }
    if (limpia.isEmpty || limpia.contains('://')) return limpia;

    final host = limpia.split('/').first.split(':').first;
    final esRedLocal = host == 'localhost' || RegExp(r'^\d{1,3}(\.\d{1,3}){3}$').hasMatch(host);
    return '${esRedLocal ? 'http' : 'https'}://$limpia';
  }

  /*
   * ========================================================
   * CONSULTAS
   * ========================================================
   */

  Future<Dashboard> dashboard() async =>
      Dashboard.desdeJson(await _get('/api/dashboard') as Map<String, dynamic>);

  Future<List<Equipo>> equipos({String? estado, String? buscar}) async {
    final lista = await _get('/api/equipos', {
      'estado': ?estado,
      if (buscar != null && buscar.trim().isNotEmpty) 'buscar': buscar.trim(),
    }) as List;
    return lista.map((e) => Equipo.desdeJson(e as Map<String, dynamic>)).toList();
  }

  /// codigo es el texto del QR del equipo (su código de inventario).
  Future<EquipoDetalle> equipo(String codigo) async => EquipoDetalle.desdeJson(
      await _get('/api/equipos/${Uri.encodeComponent(codigo.trim())}') as Map<String, dynamic>);

  /// filtro: ACTIVOS, VENCIDOS o FINALIZADOS.
  Future<List<Prestamo>> prestamos(String filtro) async {
    final lista = await _get('/api/prestamos', {'filtro': filtro}) as List;
    return lista.map((p) => Prestamo.desdeJson(p as Map<String, dynamic>)).toList();
  }

  Future<PrestamoDetalle> prestamo(int id) async =>
      PrestamoDetalle.desdeJson(await _get('/api/prestamos/$id') as Map<String, dynamic>);

  Future<List<Mantenimiento>> mantenimientos({required bool soloEnProceso}) async {
    final lista = await _get('/api/mantenimientos', {'soloEnProceso': '$soloEnProceso'}) as List;
    return lista.map((m) => Mantenimiento.desdeJson(m as Map<String, dynamic>)).toList();
  }

  /*
   * ========================================================
   * HTTP
   * ========================================================
   */

  Future<dynamic> _get(String ruta, [Map<String, String>? parametros]) {
    final uri = _uri(ruta).replace(queryParameters: parametros == null || parametros.isEmpty ? null : parametros);
    return _enviar(() => _cliente.get(uri, headers: _encabezados()));
  }

  Future<dynamic> _post(String ruta, Object? cuerpo, {bool conToken = true}) {
    final uri = _uri(ruta);
    return _enviar(() => _cliente.post(
          uri,
          headers: _encabezados(conToken: conToken),
          body: cuerpo == null ? null : jsonEncode(cuerpo),
        ));
  }

  Uri _uri(String ruta) {
    if (servidor.trim().isEmpty) {
      throw const ApiException('Todavía no hay un servidor configurado. Toque «Servidor» y escriba la dirección.');
    }
    return Uri.parse('${normalizarDireccion(servidor)}$ruta');
  }

  Map<String, String> _encabezados({bool conToken = true}) => {
        'Content-Type': 'application/json',
        if (conToken && token != null) 'Authorization': 'Bearer $token',
      };

  /// Envía la petición y convierte cualquier problema en un ApiException con un mensaje claro.
  Future<dynamic> _enviar(Future<http.Response> Function() peticion, {String? direccion}) async {
    final http.Response respuesta;
    try {
      respuesta = await peticion().timeout(_tiempoMaximo);
    } on TimeoutException {
      throw ApiException('El servidor ${direccion ?? servidor} no respondió a tiempo.');
    } catch (_) {
      throw ApiException('No se pudo conectar con ${direccion ?? servidor}. '
          'Revise que la API esté encendida y que la dirección sea correcta.');
    }

    // La API responde en UTF-8: se decodifica así para que las tildes y la ñ lleguen bien.
    final texto = utf8.decode(respuesta.bodyBytes);
    final dynamic json = texto.isEmpty ? null : _leerJson(texto);

    if (respuesta.statusCode >= 200 && respuesta.statusCode < 300) return json;

    final mensaje = json is Map && json['mensaje'] is String
        ? json['mensaje'] as String
        : 'El servidor respondió con un error (${respuesta.statusCode}).';

    if (respuesta.statusCode == 401 && token != null) alVencerSesion?.call();
    throw ApiException(mensaje, respuesta.statusCode);
  }

  dynamic _leerJson(String texto) {
    try {
      return jsonDecode(texto);
    } on FormatException {
      return null;
    }
  }
}

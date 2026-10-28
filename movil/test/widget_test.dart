import 'package:flutter_test/flutter_test.dart';
import 'package:sigelab_movil/services/api_service.dart';
import 'package:sigelab_movil/util/formato.dart';

void main() {
  test('la dirección del servidor se completa sola', () {
    expect(ApiService.normalizarDireccion(' abc-def.trycloudflare.com/ '), 'https://abc-def.trycloudflare.com');
    expect(ApiService.normalizarDireccion('192.168.0.8:8080'), 'http://192.168.0.8:8080');
    expect(ApiService.normalizarDireccion('localhost:8080'), 'http://localhost:8080');
    expect(ApiService.normalizarDireccion('http://10.0.2.2:8080/'), 'http://10.0.2.2:8080');
    expect(ApiService.normalizarDireccion('https://abc.trycloudflare.com'), 'https://abc.trycloudflare.com');
    expect(ApiService.normalizarDireccion('   '), '');
  });

  test('el estado DANADO de la base se muestra con ñ', () {
    expect(Formato.estadoEquipo('DANADO'), 'Dañado');
    expect(Formato.condicion('DANADO'), 'Dañado');
    expect(Formato.condicion('BUENO'), 'Bueno');
  });

  test('formato de fechas en español', () {
    expect(Formato.fechaHora(DateTime(2026, 10, 25, 9, 5)), '25 oct 2026, 09:05');
    expect(Formato.relativo(DateTime.now().subtract(const Duration(days: 3, minutes: 1))), 'hace 3 días');
    expect(Formato.relativo(DateTime.now().add(const Duration(hours: 2, minutes: 1))), 'en 2 horas');
  });
}

import 'package:flutter/material.dart';
import 'package:mobile_scanner/mobile_scanner.dart';

import '../config/tema.dart';
import 'equipo_detalle_view.dart';

/// Lee el código QR pegado en un equipo con la cámara del celular.
///
/// El QR lo genera el sistema de escritorio (botón "Generar QR" en Equipos)
/// y contiene el código de inventario, por ejemplo CAM-001. Al leerlo se abre
/// la ficha del equipo. Si no hay cámara, el código se puede escribir.
class EscanerView extends StatefulWidget {
  const EscanerView({super.key});

  @override
  State<EscanerView> createState() => _EscanerViewState();
}

class _EscanerViewState extends State<EscanerView> {
  final _camara = MobileScannerController(formats: const [BarcodeFormat.qrCode]);
  final _txtCodigo = TextEditingController();

  /// Evita abrir dos veces la ficha: la cámara detecta el mismo QR varias veces por segundo.
  bool _codigoLeido = false;

  @override
  void dispose() {
    _camara.dispose();
    _txtCodigo.dispose();
    super.dispose();
  }

  void _alDetectar(BarcodeCapture captura) {
    if (_codigoLeido) return;
    final codigo = captura.barcodes.map((b) => b.rawValue?.trim()).firstWhere(
          (texto) => texto != null && texto.isNotEmpty,
          orElse: () => null,
        );
    if (codigo != null) _abrirEquipo(codigo);
  }

  void _abrirEquipo(String codigo) {
    if (codigo.trim().isEmpty) return;
    _codigoLeido = true;
    _camara.stop();
    Navigator.of(context).pushReplacement(
      MaterialPageRoute(builder: (_) => EquipoDetalleView(codigo: codigo.trim().toUpperCase())),
    );
  }

  @override
  Widget build(BuildContext context) => Scaffold(
        backgroundColor: Colors.black,
        appBar: AppBar(
          backgroundColor: Colors.black,
          title: const Text('Escanear equipo'),
          actions: [
            ValueListenableBuilder<MobileScannerState>(
              valueListenable: _camara,
              builder: (context, estado, _) => estado.torchState == TorchState.unavailable
                  ? const SizedBox.shrink()
                  : IconButton(
                      tooltip: 'Linterna',
                      icon: Icon(estado.torchState == TorchState.on ? Icons.flash_on : Icons.flash_off),
                      onPressed: _camara.toggleTorch,
                    ),
            ),
          ],
        ),
        body: Column(
          children: [
            Expanded(
              child: Stack(
                fit: StackFit.expand,
                children: [
                  MobileScanner(
                    controller: _camara,
                    onDetect: _alDetectar,
                    errorBuilder: (context, error) => _ErrorCamara(error: error),
                  ),
                  const IgnorePointer(child: _Marco()),
                ],
              ),
            ),
            _IngresoManual(controlador: _txtCodigo, alBuscar: _abrirEquipo),
          ],
        ),
      );
}

/// Recuadro cian que indica dónde colocar el QR.
class _Marco extends StatelessWidget {
  const _Marco();

  @override
  Widget build(BuildContext context) => Stack(
        fit: StackFit.expand,
        children: [
          const CustomPaint(painter: _PintorMarco()),
          Align(
            alignment: Alignment.bottomCenter,
            child: Padding(
              padding: const EdgeInsets.only(bottom: 24),
              child: Text('Coloque el código QR dentro del recuadro',
                  style: TextStyle(color: Colors.white.withValues(alpha: 0.85))),
            ),
          ),
        ],
      );
}

/// Oscurece todo menos un cuadrado en el centro y le dibuja un borde cian.
class _PintorMarco extends CustomPainter {
  const _PintorMarco();

  @override
  void paint(Canvas canvas, Size size) {
    final lado = (size.shortestSide * 0.65).clamp(180.0, 320.0);
    final hueco = RRect.fromRectAndRadius(
      Rect.fromCenter(center: size.center(Offset.zero), width: lado, height: lado),
      const Radius.circular(24),
    );

    final sombra = Path.combine(
      PathOperation.difference,
      Path()..addRect(Offset.zero & size),
      Path()..addRRect(hueco),
    );
    canvas.drawPath(sombra, Paint()..color = Colors.black.withValues(alpha: 0.55));
    canvas.drawRRect(
      hueco,
      Paint()
        ..style = PaintingStyle.stroke
        ..strokeWidth = 2.5
        ..color = Tema.primario,
    );
  }

  @override
  bool shouldRepaint(_PintorMarco anterior) => false;
}

class _ErrorCamara extends StatelessWidget {
  final MobileScannerException error;

  const _ErrorCamara({required this.error});

  @override
  Widget build(BuildContext context) {
    final mensaje = switch (error.errorCode) {
      MobileScannerErrorCode.permissionDenied =>
        'La app no tiene permiso para usar la cámara. Actívelo en los ajustes del teléfono.',
      MobileScannerErrorCode.unsupported => 'Este dispositivo no tiene una cámara compatible.',
      _ => 'No se pudo abrir la cámara.',
    };

    return ColoredBox(
      color: Tema.fondo,
      child: Center(
        child: Padding(
          padding: const EdgeInsets.all(32),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              const Icon(Icons.no_photography_outlined, color: Tema.textoSuave, size: 40),
              const SizedBox(height: 14),
              Text(mensaje, textAlign: TextAlign.center, style: const TextStyle(color: Tema.textoSuave)),
              const SizedBox(height: 6),
              const Text('Puede escribir el código del equipo abajo.',
                  textAlign: TextAlign.center, style: TextStyle(color: Tema.placeholder, fontSize: 13)),
            ],
          ),
        ),
      ),
    );
  }
}

class _IngresoManual extends StatelessWidget {
  final TextEditingController controlador;
  final void Function(String codigo) alBuscar;

  const _IngresoManual({required this.controlador, required this.alBuscar});

  @override
  Widget build(BuildContext context) => Container(
        color: Tema.superficie,
        padding: EdgeInsets.fromLTRB(16, 14, 16, 14 + MediaQuery.paddingOf(context).bottom),
        child: Row(
          children: [
            Expanded(
              child: TextField(
                controller: controlador,
                textCapitalization: TextCapitalization.characters,
                textInputAction: TextInputAction.search,
                decoration: const InputDecoration(
                  hintText: 'O escriba el código, ej. CAM-001',
                  prefixIcon: Icon(Icons.keyboard_outlined),
                  isDense: true,
                ),
                onSubmitted: alBuscar,
              ),
            ),
            const SizedBox(width: 10),
            IconButton.filled(
              tooltip: 'Buscar',
              style: IconButton.styleFrom(
                backgroundColor: Tema.primario,
                foregroundColor: Tema.textoSobrePrimario,
                minimumSize: const Size(48, 48),
                shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
              ),
              onPressed: () => alBuscar(controlador.text),
              icon: const Icon(Icons.arrow_forward),
            ),
          ],
        ),
      );
}

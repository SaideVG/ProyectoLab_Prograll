/// Usuario que inició sesión. Viene de la API (UsuarioDto), nunca trae la contraseña.
class Usuario {
  final int id;
  final String usuario;
  final String nombre;
  final String apellido;
  final String correo;
  final String rol;

  const Usuario({
    required this.id,
    required this.usuario,
    required this.nombre,
    required this.apellido,
    required this.correo,
    required this.rol,
  });

  String get nombreCompleto => '$nombre $apellido';

  String get iniciales {
    final primera = nombre.isNotEmpty ? nombre[0] : '';
    final segunda = apellido.isNotEmpty ? apellido[0] : '';
    return (primera + segunda).toUpperCase();
  }

  factory Usuario.desdeJson(Map<String, dynamic> json) => Usuario(
        id: json['id'] as int,
        usuario: json['usuario'] as String,
        nombre: json['nombre'] as String,
        apellido: json['apellido'] as String,
        correo: json['correo'] as String,
        rol: json['rol'] as String,
      );

  Map<String, dynamic> aJson() => {
        'id': id,
        'usuario': usuario,
        'nombre': nombre,
        'apellido': apellido,
        'correo': correo,
        'rol': rol,
      };
}

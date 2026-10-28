package universidad.sigelab.api.dto;

import java.time.LocalDateTime;

/** Respuesta del login: la app guarda el token y lo envía en cada petición. */
public record LoginResponse(String token, LocalDateTime expira, UsuarioDto usuario) { }

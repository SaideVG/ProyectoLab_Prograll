package universidad.sigelab.api.dto;

/** Lo que envía la app al iniciar sesión. */
public record LoginRequest(String usuario, String password) { }

package universidad.sigelab.service;

import universidad.sigelab.model.Usuario;

/**
 * Puente entre la regla RN-02 (que vive en {@link UsuarioService}) y la pantalla
 * de la cámara.
 *
 * La View lo implementa mostrando el diálogo de verificación facial: recibe al
 * usuario y la firma facial guardada, y devuelve si el rostro capturado coincide.
 * Así el Service conserva la regla de negocio y la View, la interfaz gráfica.
 */
@FunctionalInterface
public interface ValidadorRostro {
    boolean validar(Usuario usuario, float[] rostroRegistrado);
}

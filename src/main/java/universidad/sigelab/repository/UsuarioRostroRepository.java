package universidad.sigelab.repository;

import universidad.sigelab.util.JdbcSupport;

import java.nio.ByteBuffer;
import java.util.Optional;

/**
 * Acceso a la tabla UsuarioRostro (RN-02): la firma facial de cada usuario.
 *
 * La firma son 128 números float que se guardan como VARBINARY(512). Se usa
 * {@link ByteBuffer} para convertir entre {@code float[]} y {@code byte[]};
 * siempre con el mismo orden de bytes, así que ida y vuelta coinciden.
 */
public class UsuarioRostroRepository {

    public boolean existe(int idUsuario) {
        return JdbcSupport.existe("verificar el rostro del usuario",
                "SELECT 1 FROM UsuarioRostro WHERE id_usuario = ?", idUsuario);
    }

    public Optional<float[]> buscar(int idUsuario) {
        return JdbcSupport.buscar("consultar el rostro del usuario",
                "SELECT vector_rostro FROM UsuarioRostro WHERE id_usuario = ?",
                resultado -> aVector(resultado.getBytes("vector_rostro")), idUsuario);
    }

    /** Inserta o actualiza el rostro del usuario (una sola fila por usuario). */
    public void guardar(int idUsuario, float[] vector) {
        byte[] datos = aBytes(vector);
        int filas = JdbcSupport.actualizar("actualizar el rostro del usuario",
                "UPDATE UsuarioRostro SET vector_rostro = ?, fecha_registro = SYSDATETIME() WHERE id_usuario = ?",
                datos, idUsuario);
        if (filas == 0) {
            JdbcSupport.actualizar("guardar el rostro del usuario",
                    "INSERT INTO UsuarioRostro (id_usuario, vector_rostro) VALUES (?, ?)",
                    idUsuario, datos);
        }
    }

    public void eliminar(int idUsuario) {
        JdbcSupport.actualizar("eliminar el rostro del usuario",
                "DELETE FROM UsuarioRostro WHERE id_usuario = ?", idUsuario);
    }

    private static byte[] aBytes(float[] vector) {
        ByteBuffer buffer = ByteBuffer.allocate(vector.length * Float.BYTES);
        for (float valor : vector) buffer.putFloat(valor);
        return buffer.array();
    }

    private static float[] aVector(byte[] datos) {
        ByteBuffer buffer = ByteBuffer.wrap(datos);
        float[] vector = new float[datos.length / Float.BYTES];
        for (int i = 0; i < vector.length; i++) vector[i] = buffer.getFloat();
        return vector;
    }
}

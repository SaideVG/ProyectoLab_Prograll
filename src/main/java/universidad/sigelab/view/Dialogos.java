package universidad.sigelab.view;

import universidad.sigelab.util.BusinessException;
import universidad.sigelab.util.DataAccessException;

import javax.swing.*;
import java.awt.*;

/** Mensajes al usuario. Todas las Views los muestran igual y ninguna expone un error técnico. */
final class Dialogos {
    private Dialogos() { }

    static void informar(Component padre, String mensaje, String titulo) {
        JOptionPane.showMessageDialog(padre, mensaje, titulo, JOptionPane.INFORMATION_MESSAGE);
    }

    static void advertir(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "Aviso", JOptionPane.WARNING_MESSAGE);
    }

    static void error(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Una BusinessException es una regla de negocio que no se cumplió: su mensaje es para el usuario.
     * Cualquier otra excepción es técnica: el detalle se imprime en la consola para el desarrollador
     * y en pantalla solo aparece un mensaje comprensible.
     */
    static void error(Component padre, Exception e) {
        if (!(e instanceof BusinessException)) e.printStackTrace();
        boolean mensajeParaUsuario = e instanceof BusinessException || e instanceof DataAccessException;
        error(padre, mensajeParaUsuario ? e.getMessage() : "Ocurrió un error inesperado.");
    }

    static boolean confirmar(Component padre, String pregunta, String titulo) {
        int respuesta = JOptionPane.showConfirmDialog(padre, pregunta, titulo,
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        return respuesta == JOptionPane.YES_OPTION;
    }

    /** Cierra solo la ventana que contiene al componente; el menú principal sigue abierto. */
    static void cerrarVentana(Component componente) {
        Window ventana = SwingUtilities.getWindowAncestor(componente);
        if (ventana != null) ventana.dispose();
    }
}

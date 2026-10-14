package universidad.sigelab;

import universidad.sigelab.service.ReconocimientoFacial;
import universidad.sigelab.view.LoginForm;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {

        // Adelanta la carga de OpenCV y los modelos en segundo plano para que el
        // primer inicio de sesión con rostro no espere.
        ReconocimientoFacial.precargar();

        SwingUtilities.invokeLater(
                LoginForm::mostrar
        );
    }
}

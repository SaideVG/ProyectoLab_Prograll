package universidad.sigelab;

import universidad.sigelab.view.LoginForm;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {

        SwingUtilities.invokeLater(
                LoginForm::mostrar
        );
    }
}

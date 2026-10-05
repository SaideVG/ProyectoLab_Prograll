package universidad.sigelab.view;

import universidad.sigelab.controller.UsuarioController;
import universidad.sigelab.enums.EstadoRegistro;
import universidad.sigelab.model.Rol;
import universidad.sigelab.model.Usuario;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.List;
import java.util.Optional;

public class UsuarioView {

    // Componentes vinculados desde UsuarioView.form
    private JPanel panelPrincipal;

    private JTextField txtId;
    private JTextField txtNombre;
    private JTextField txtApellido;
    private JTextField txtUsuario;
    private JPasswordField txtPassword;
    private JTextField txtCorreo;
    private JComboBox<Rol> cboRol;

    private JCheckBox chkActivo;

    private JButton btnNuevo;
    private JButton btnGuardar;
    private JButton btnActualizar;
    private JButton btnActivarDesactivar;

    private JTextField txtBuscar;
    private JButton btnBuscar;

    private JTable tblUsuarios;
    private JButton btnSalir;

    // La View solamente conoce al Controller.
    private final UsuarioController usuarioController;


    public UsuarioView() {
        this(new UsuarioController());
    }

    public UsuarioView(UsuarioController usuarioController) {
        this.usuarioController = usuarioController;

        configurarFormulario();
        configurarEventos();
        cargarRoles();
        cargarUsuarios();
    }


    /*
     * Configuración inicial.
     */
    private void configurarFormulario() {
        // El ID lo genera SQL Server.
        txtId.setEditable(false);

        // El estado no se cambia desde el checkbox, sino con el botón Activar/Desactivar.
        chkActivo.setEnabled(false);

        txtPassword.setToolTipText("Al actualizar, déjela vacía para conservar la contraseña actual.");

        tblUsuarios.setModel(new TablaSoloLectura("ID", "Usuario", "Nombre", "Apellido", "Correo", "Rol", "Estado"));
        tblUsuarios.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        limpiarFormulario();
    }

    private void configurarEventos() {
        btnNuevo.addActionListener(e -> nuevo());
        btnGuardar.addActionListener(e -> guardar());
        btnActualizar.addActionListener(e -> actualizar());
        btnActivarDesactivar.addActionListener(e -> cambiarEstado());
        btnBuscar.addActionListener(e -> buscar());
        btnSalir.addActionListener(e -> salir());

        // Al seleccionar una fila se carga ese usuario en el formulario.
        tblUsuarios.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) seleccionarUsuario();
        });
    }

    /*
     * El rol se elige de un ComboBox con los registros de la tabla Rol;
     * nunca se escribe a mano.
     */
    private void cargarRoles() {
        try {
            cboRol.removeAllItems();
            for (Rol rol : usuarioController.listarRoles()) cboRol.addItem(rol);
            cboRol.setSelectedIndex(-1);
        } catch (Exception e) {
            Dialogos.error(panelPrincipal, e);
        }
    }


    /*
     * ========================================================
     * GUARDAR
     * ========================================================
     */
    private void guardar() {
        try {
            Usuario usuario = new Usuario(txtNombre.getText(), txtApellido.getText(), txtUsuario.getText(),
                    null, txtCorreo.getText(), (Rol) cboRol.getSelectedItem());

            // La contraseña viaja aparte: el Service la convierte en hash antes de guardarla.
            if (usuarioController.guardar(usuario, leerPassword())) {
                Dialogos.informar(panelPrincipal, "Usuario guardado correctamente.", "Usuario");
                limpiarFormulario();
                cargarUsuarios();
            } else {
                Dialogos.error(panelPrincipal, "No se pudo guardar el usuario.");
            }
        } catch (Exception e) {
            Dialogos.error(panelPrincipal, e);
        }
    }


    /*
     * ========================================================
     * LISTAR
     * ========================================================
     */
    private void cargarUsuarios() {
        try {
            mostrarEnTabla(usuarioController.listar());
        } catch (Exception e) {
            Dialogos.error(panelPrincipal, e);
        }
    }

    private void mostrarEnTabla(List<Usuario> usuarios) {
        DefaultTableModel modelo = (DefaultTableModel) tblUsuarios.getModel();
        modelo.setRowCount(0);

        for (Usuario usuario : usuarios) {
            modelo.addRow(new Object[]{
                    usuario.getIdUsuario(),
                    usuario.getUsuario(),
                    usuario.getNombre(),
                    usuario.getApellido(),
                    usuario.getCorreo(),
                    usuario.getRol(),
                    usuario.getEstado()
            });
        }
    }


    /*
     * ========================================================
     * ACTUALIZAR
     * ========================================================
     */
    private void actualizar() {
        if (txtId.getText().isBlank()) {
            Dialogos.advertir(panelPrincipal, "Debe seleccionar un usuario.");
            return;
        }

        try {
            // Ni el hash ni el estado viajan desde la pantalla: el Service conserva los guardados.
            Usuario usuario = new Usuario(Integer.parseInt(txtId.getText()), txtNombre.getText(),
                    txtApellido.getText(), txtUsuario.getText(), null, txtCorreo.getText(),
                    (Rol) cboRol.getSelectedItem(), null, null);

            if (usuarioController.actualizar(usuario, leerPassword())) {
                Dialogos.informar(panelPrincipal, "Usuario actualizado correctamente.", "Usuario");
                limpiarFormulario();
                cargarUsuarios();
            } else {
                Dialogos.error(panelPrincipal, "No se pudo actualizar el usuario.");
            }
        } catch (Exception e) {
            Dialogos.error(panelPrincipal, e);
        }
    }


    /*
     * ========================================================
     * ACTIVAR / DESACTIVAR
     * Desactivación lógica: el usuario no se elimina.
     * ========================================================
     */
    private void cambiarEstado() {
        if (txtId.getText().isBlank()) {
            Dialogos.advertir(panelPrincipal, "Debe seleccionar un usuario.");
            return;
        }

        boolean activar = !chkActivo.isSelected();
        String accion = activar ? "activar" : "desactivar";
        if (!Dialogos.confirmar(panelPrincipal, "¿Está seguro de " + accion + " al usuario?", "Confirmar")) return;

        try {
            int idUsuario = Integer.parseInt(txtId.getText());
            EstadoRegistro nuevoEstado = activar ? EstadoRegistro.ACTIVO : EstadoRegistro.INACTIVO;

            if (usuarioController.cambiarEstado(idUsuario, nuevoEstado)) {
                Dialogos.informar(panelPrincipal,
                        activar ? "Usuario activado correctamente." : "Usuario desactivado correctamente.", "Usuario");
                cargarUsuarios();
                seleccionarFilaPorId(idUsuario);
            } else {
                Dialogos.error(panelPrincipal, "No se pudo cambiar el estado del usuario.");
            }
        } catch (Exception e) {
            Dialogos.error(panelPrincipal, e);
        }
    }


    /*
     * ========================================================
     * BUSCAR POR NOMBRE DE USUARIO
     * ========================================================
     */
    private void buscar() {
        String nombreUsuario = txtBuscar.getText().trim();

        // Sin texto de búsqueda se muestran de nuevo todos los usuarios.
        if (nombreUsuario.isBlank()) {
            limpiarFormulario();
            cargarUsuarios();
            return;
        }

        try {
            Optional<Usuario> resultado = usuarioController.buscarPorUsuario(nombreUsuario);

            if (resultado.isPresent()) {
                mostrarEnTabla(List.of(resultado.get()));
                seleccionarFilaPorId(resultado.get().getIdUsuario());
            } else {
                Dialogos.informar(panelPrincipal, "No se encontró ningún usuario con ese nombre.", "Búsqueda");
            }
        } catch (Exception e) {
            Dialogos.error(panelPrincipal, e);
        }
    }


    /*
     * ========================================================
     * SELECCIÓN DESDE JTable
     * ========================================================
     */
    private void seleccionarUsuario() {
        int fila = tblUsuarios.getSelectedRow();
        if (fila == -1) return;

        try {
            int idUsuario = (int) tblUsuarios.getValueAt(fila, 0);
            usuarioController.buscar(idUsuario).ifPresent(this::mostrarEnFormulario);
        } catch (Exception e) {
            Dialogos.error(panelPrincipal, e);
        }
    }

    private void mostrarEnFormulario(Usuario usuario) {
        txtId.setText(String.valueOf(usuario.getIdUsuario()));
        txtNombre.setText(usuario.getNombre());
        txtApellido.setText(usuario.getApellido());
        txtUsuario.setText(usuario.getUsuario());
        txtCorreo.setText(usuario.getCorreo());
        // La contraseña guardada es un hash y no se puede mostrar: el campo queda vacío.
        txtPassword.setText("");
        // Rol.equals() compara por ID, por eso el ComboBox encuentra el rol del usuario.
        cboRol.setSelectedItem(usuario.getRol());
        chkActivo.setSelected(usuario.isActivo());

        // Se está trabajando con un registro existente.
        btnGuardar.setEnabled(false);
        btnActualizar.setEnabled(true);
        btnActivarDesactivar.setEnabled(true);
        btnActivarDesactivar.setText(usuario.isActivo() ? "Desactivar" : "Activar");
    }

    private void seleccionarFilaPorId(int idUsuario) {
        for (int fila = 0; fila < tblUsuarios.getRowCount(); fila++) {
            if ((int) tblUsuarios.getValueAt(fila, 0) == idUsuario) {
                tblUsuarios.setRowSelectionInterval(fila, fila);
                tblUsuarios.scrollRectToVisible(tblUsuarios.getCellRect(fila, 0, true));
                return;
            }
        }
    }


    /*
     * ========================================================
     * NUEVO
     * ========================================================
     */
    private void nuevo() {
        limpiarFormulario();
        txtNombre.requestFocus();
    }

    private void limpiarFormulario() {
        txtId.setText("");
        txtNombre.setText("");
        txtApellido.setText("");
        txtUsuario.setText("");
        txtPassword.setText("");
        txtCorreo.setText("");
        txtBuscar.setText("");
        cboRol.setSelectedIndex(-1);

        // Todo usuario nuevo inicia activo.
        chkActivo.setSelected(true);
        tblUsuarios.clearSelection();

        btnGuardar.setEnabled(true);
        btnActualizar.setEnabled(false);
        btnActivarDesactivar.setEnabled(false);
        btnActivarDesactivar.setText("Desactivar");
    }

    private String leerPassword() {
        return new String(txtPassword.getPassword());
    }

    private void salir() {
        if (Dialogos.confirmar(panelPrincipal, "¿Desea regresar al menú principal?", "Regresar")) {
            Dialogos.cerrarVentana(panelPrincipal);
        }
    }

    public JPanel getPanelPrincipal() {
        return panelPrincipal;
    }
}

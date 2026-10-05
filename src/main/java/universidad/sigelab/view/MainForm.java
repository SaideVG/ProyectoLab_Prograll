package universidad.sigelab.view;

import universidad.sigelab.controller.ReporteController;
import universidad.sigelab.controller.UsuarioController;
import universidad.sigelab.enums.Modulo;
import universidad.sigelab.enums.RolUsuario;
import universidad.sigelab.model.Dashboard;
import universidad.sigelab.model.Usuario;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MainForm {

    // Componentes vinculados desde MainForm.form
    private JPanel panelPrincipal;

    private JLabel lblTitulo;
    private JLabel lblUsuario;

    // Dashboard
    private JLabel lblTotalEquipos;
    private JLabel lblDisponibles;
    private JLabel lblPrestados;
    private JLabel lblDanados;
    private JLabel lblEnMantenimiento;
    private JLabel lblDeBaja;
    private JLabel lblPrestamosActivos;
    private JLabel lblPrestamosVencidos;

    private JButton btnUsuarios;
    private JButton btnLaboratorios;
    private JButton btnTiposEquipo;
    private JButton btnEquipos;
    private JButton btnPrestamos;
    private JButton btnDevoluciones;
    private JButton btnMantenimiento;
    private JButton btnAuditoria;
    private JButton btnReportes;

    private JButton btnCerrarSesion;
    private JButton btnSalir;

    // Usuario que inició sesión.
    private final Usuario usuario;

    // La View solamente conoce a los Controllers.
    private final UsuarioController usuarioController;
    private final ReporteController reporteController;


    public MainForm(
            Usuario usuario
    ) {

        this(usuario, new UsuarioController(), new ReporteController());
    }


    public MainForm(
            Usuario usuario,
            UsuarioController usuarioController,
            ReporteController reporteController
    ) {

        this.usuario = usuario;
        this.usuarioController = usuarioController;
        this.reporteController = reporteController;

        configurarFormulario();
        configurarEventos();
        cargarDashboard();
    }


    /*
     * Crea la ventana del menú principal
     * para el usuario que acaba de iniciar sesión.
     */
    public static void mostrar(
            Usuario usuario
    ) {

        MainForm mainForm =
                new MainForm(usuario);

        JFrame frame =
                new JFrame(
                        "SIGELAB - Gestión y Control de Laboratorios"
                );

        frame.setContentPane(
                mainForm.getPanelPrincipal()
        );

        frame.setDefaultCloseOperation(
                JFrame.EXIT_ON_CLOSE
        );

        frame.setSize(
                780,
                720
        );

        frame.setLocationRelativeTo(null);

        frame.setResizable(false);

        /*
         * Cada vez que el usuario regresa al menú (por ejemplo,
         * después de registrar un préstamo) se actualizan las cantidades.
         */
        frame.addWindowListener(new WindowAdapter() {

            @Override
            public void windowActivated(WindowEvent e) {
                mainForm.cargarDashboard();
            }
        });

        frame.setVisible(true);
    }


    /*
     * Configuración general del menú.
     */
    private void configurarFormulario() {

        lblTitulo.setFont(
                lblTitulo.getFont().deriveFont(
                        Font.BOLD,
                        20f
                )
        );

        lblUsuario.setText(
                "Usuario: "
                        + usuario.getNombreCompleto()
                        + "   |   Rol: "
                        + usuario.getRol()
        );

        configurarMenuPorRol();
    }


    /*
     * El menú se adapta al rol: cada botón solo se muestra
     * si el rol tiene acceso a ese módulo.
     *
     * Esto es únicamente visual. El permiso real se valida
     * en los Services con Sesion.exigirAcceso().
     */
    private void configurarMenuPorRol() {

        RolUsuario rol =
                usuario.getRol().getNombre();

        btnUsuarios.setVisible(
                rol.puedeAcceder(Modulo.USUARIOS)
        );

        btnLaboratorios.setVisible(
                rol.puedeAcceder(Modulo.LABORATORIOS)
        );

        btnTiposEquipo.setVisible(
                rol.puedeAcceder(Modulo.TIPOS_EQUIPO)
        );

        btnEquipos.setVisible(
                rol.puedeAcceder(Modulo.EQUIPOS)
        );

        btnPrestamos.setVisible(
                rol.puedeAcceder(Modulo.PRESTAMOS)
        );

        btnDevoluciones.setVisible(
                rol.puedeAcceder(Modulo.DEVOLUCIONES)
        );

        btnMantenimiento.setVisible(
                rol.puedeAcceder(Modulo.MANTENIMIENTO)
        );

        btnAuditoria.setVisible(
                rol.puedeAcceder(Modulo.AUDITORIA)
        );

        btnReportes.setVisible(
                rol.puedeAcceder(Modulo.REPORTES)
        );
    }


    /*
     * Aquí centralizamos todos los eventos
     * del menú principal.
     */
    private void configurarEventos() {

        btnUsuarios.addActionListener(
                e -> abrirVentana("Administración de Usuarios", new UsuarioView().getPanelPrincipal(), 950, 700)
        );

        btnLaboratorios.addActionListener(
                e -> abrirVentana("Administración de Laboratorios", new LaboratorioView().getPanelPrincipal(), 900, 640)
        );

        btnTiposEquipo.addActionListener(
                e -> abrirVentana("Tipos de Equipo", new TipoEquipoView().getPanelPrincipal(), 800, 580)
        );

        btnEquipos.addActionListener(
                e -> abrirVentana("Administración de Equipos", new EquipoView().getPanelPrincipal(), 1050, 740)
        );

        btnPrestamos.addActionListener(
                e -> abrirVentana("Registro de Préstamos", new PrestamoView().getPanelPrincipal(), 950, 660)
        );

        btnDevoluciones.addActionListener(
                e -> abrirVentana("Devoluciones", new DevolucionView().getPanelPrincipal(), 1150, 720)
        );

        btnMantenimiento.addActionListener(
                e -> abrirVentana("Mantenimiento de Equipos", new MantenimientoView().getPanelPrincipal(), 1200, 720)
        );

        btnAuditoria.addActionListener(
                e -> abrirVentana("Auditoría", new AuditoriaView().getPanelPrincipal(), 1150, 660)
        );

        btnReportes.addActionListener(
                e -> abrirVentana("Reportes", new ReporteView().getPanelPrincipal(), 1200, 680)
        );

        btnCerrarSesion.addActionListener(
                e -> cerrarSesion()
        );

        btnSalir.addActionListener(
                e -> salir()
        );
    }


    /*
     * ========================================================
     * DASHBOARD
     *
     * Las cantidades se calculan en la base de datos
     * (COUNT y GROUP BY), no contando filas en pantalla.
     *
     * Si la consulta falla no se muestra un diálogo: este método
     * se ejecuta cada vez que la ventana se activa y un diálogo
     * volvería a activarla al cerrarse.
     * ========================================================
     */
    private void cargarDashboard() {

        try {

            Dashboard dashboard =
                    reporteController.obtenerDashboard();

            lblTotalEquipos.setText("Equipos: " + dashboard.totalEquipos());
            lblDisponibles.setText("Disponibles: " + dashboard.disponibles());
            lblPrestados.setText("Prestados: " + dashboard.prestados());
            lblDanados.setText("Dañados: " + dashboard.danados());
            lblEnMantenimiento.setText("En mantenimiento: " + dashboard.enMantenimiento());
            lblDeBaja.setText("De baja: " + dashboard.deBaja());
            lblPrestamosActivos.setText("Préstamos activos: " + dashboard.prestamosActivos());
            lblPrestamosVencidos.setText("Préstamos vencidos: " + dashboard.prestamosVencidos());

        } catch (Exception e) {

            lblTotalEquipos.setText("Equipos: sin datos");
            lblDisponibles.setText("Disponibles: sin datos");
            lblPrestados.setText("Prestados: sin datos");
            lblDanados.setText("Dañados: sin datos");
            lblEnMantenimiento.setText("En mantenimiento: sin datos");
            lblDeBaja.setText("De baja: sin datos");
            lblPrestamosActivos.setText("Préstamos activos: sin datos");
            lblPrestamosVencidos.setText("Préstamos vencidos: sin datos");
        }
    }


    /*
     * Método reutilizable para abrir cualquier módulo.
     *
     * Como nuestras Views están construidas como JPanel,
     * simplemente colocamos el panel dentro de un JFrame.
     */
    private void abrirVentana(
            String titulo,
            JPanel panel,
            int ancho,
            int alto
    ) {

        JFrame ventana =
                new JFrame(titulo);

        ventana.setContentPane(panel);

        ventana.setDefaultCloseOperation(
                JFrame.DISPOSE_ON_CLOSE
        );

        ventana.setSize(
                ancho,
                alto
        );

        ventana.setLocationRelativeTo(null);

        ventana.setVisible(true);
    }


    /*
     * Cierra la sesión y regresa al login.
     * Se cierran todas las ventanas para que no quede
     * ningún módulo abierto con la sesión anterior.
     */
    private void cerrarSesion() {

        int respuesta =
                JOptionPane.showConfirmDialog(
                        panelPrincipal,
                        "¿Desea cerrar la sesión?",
                        "Cerrar sesión",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );

        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }

        usuarioController.cerrarSesion();

        for (Window ventana : Window.getWindows()) {
            ventana.dispose();
        }

        LoginForm.mostrar();
    }


    /*
     * Cierra completamente la aplicación.
     */
    private void salir() {

        int respuesta =
                JOptionPane.showConfirmDialog(
                        panelPrincipal,
                        "¿Desea salir del sistema?",
                        "Confirmar salida",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );

        if (
                respuesta ==
                        JOptionPane.YES_OPTION
        ) {

            System.exit(0);
        }
    }


    public JPanel getPanelPrincipal() {

        return panelPrincipal;
    }
}

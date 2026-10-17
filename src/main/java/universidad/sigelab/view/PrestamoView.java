package universidad.sigelab.view;

import universidad.sigelab.controller.PrestamoController;
import universidad.sigelab.controller.QrController;
import universidad.sigelab.model.DetallePrestamo;
import universidad.sigelab.model.Equipo;
import universidad.sigelab.model.Prestamo;
import universidad.sigelab.util.Fechas;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PrestamoView {

    // Componentes vinculados desde PrestamoView.form
    private JPanel panelPrincipal;

    private JTextField txtSolicitante;
    private JTextField txtIdentificacion;
    private JSpinner spnFechaEsperada;
    private JTextField txtObservaciones;

    private JTextField txtCodigoEquipo;
    private JButton btnAgregar;
    private JButton btnLeerQr;
    private JButton btnLeerQrCamara;
    private JButton btnQuitar;
    private JTable tblEquipos;

    private JButton btnNuevo;
    private JButton btnConfirmar;
    private JButton btnSalir;

    // La View solamente conoce a los Controllers.
    private final PrestamoController prestamoController;
    private final QrController qrController;

    /*
     * Equipos del préstamo que se está armando (el detalle).
     * Mientras no se confirme, nada se guarda ni cambia de estado en la base.
     */
    private final List<Equipo> equipos = new ArrayList<>();


    public PrestamoView() {
        this(new PrestamoController(), new QrController());
    }

    public PrestamoView(PrestamoController prestamoController, QrController qrController) {
        this.prestamoController = prestamoController;
        this.qrController = qrController;

        configurarFormulario();
        configurarEventos();
    }


    /*
     * Configuración inicial.
     */
    private void configurarFormulario() {
        SelectorFecha.configurar(spnFechaEsperada, LocalDateTime.now().plusDays(1));

        tblEquipos.setModel(new TablaSoloLectura("Código", "Nombre", "Tipo", "Laboratorio", "Estado"));
        tblEquipos.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        nuevo();
    }

    private void configurarEventos() {
        btnAgregar.addActionListener(e -> agregarEquipo());

        // Enter en el campo de código equivale a presionar Agregar.
        txtCodigoEquipo.addActionListener(e -> agregarEquipo());

        btnLeerQr.addActionListener(e -> leerQr());
        btnLeerQrCamara.addActionListener(e -> leerQrConCamara());
        btnQuitar.addActionListener(e -> quitarEquipo());
        btnNuevo.addActionListener(e -> nuevo());
        btnConfirmar.addActionListener(e -> confirmarPrestamo());
        btnSalir.addActionListener(e -> salir());
    }


    /*
     * ========================================================
     * AGREGAR EQUIPO AL DETALLE
     *
     * El Controller busca el código en la base y valida que el
     * equipo exista, esté DISPONIBLE y no esté repetido.
     * Aquí solo se identifica el equipo: su estado no cambia.
     * ========================================================
     */
    private void agregarEquipo() {
        try {
            Equipo equipo = prestamoController.validarEquipoParaPrestamo(txtCodigoEquipo.getText(), equipos);

            equipos.add(equipo);
            mostrarEquipos();

            txtCodigoEquipo.setText("");
            txtCodigoEquipo.requestFocus();
        } catch (Exception e) {
            Dialogos.error(panelPrincipal, e);
        }
    }

    /*
     * ========================================================
     * LEER CÓDIGO QR
     *
     * El QR solo entrega el código de inventario (por ejemplo
     * CAM-001). Con ese código se sigue el mismo camino que al
     * escribirlo a mano: agregarEquipo() busca el equipo real en
     * la base y lo agrega si está DISPONIBLE o lo rechaza si no.
     * Leer el QR no cambia el estado de ningún equipo.
     *
     * Hay dos formas de leerlo y las dos terminan igual:
     *   - desde un archivo de imagen (leerQr)
     *   - con la cámara web (leerQrConCamara)
     * ========================================================
     */
    private void leerQr() {
        JFileChooser selector = new JFileChooser(qrController.carpetaDeImagenes());
        selector.setDialogTitle("Seleccione la imagen del código QR");
        selector.setFileFilter(new FileNameExtensionFilter("Imágenes (PNG, JPG, BMP, GIF)", "png", "jpg", "jpeg", "bmp", "gif"));

        if (selector.showOpenDialog(panelPrincipal) != JFileChooser.APPROVE_OPTION) return;

        try {
            agregarCodigoLeido(qrController.leerCodigo(selector.getSelectedFile()));
        } catch (Exception e) {
            Dialogos.error(panelPrincipal, e);
        }
    }

    /*
     * Abre la ventana de la cámara y espera a que vea un QR.
     * Si el usuario la cierra sin leer nada, no pasa nada.
     */
    private void leerQrConCamara() {
        try {
            String codigo = new LectorQrCamaraView(qrController).leer(panelPrincipal);
            if (codigo != null) agregarCodigoLeido(codigo);
        } catch (Exception e) {
            Dialogos.error(panelPrincipal, e);
        }
    }

    /*
     * El código leído queda visible en el campo, igual que si se
     * hubiera escrito, y pasa por las mismas validaciones.
     */
    private void agregarCodigoLeido(String codigo) {
        txtCodigoEquipo.setText(codigo);
        agregarEquipo();
    }

    private void quitarEquipo() {
        int fila = tblEquipos.getSelectedRow();
        if (fila == -1) {
            Dialogos.advertir(panelPrincipal, "Seleccione en la tabla el equipo que desea quitar.");
            return;
        }

        // Las filas de la tabla están en el mismo orden que la lista.
        equipos.remove(fila);
        mostrarEquipos();
    }

    private void mostrarEquipos() {
        DefaultTableModel modelo = (DefaultTableModel) tblEquipos.getModel();
        modelo.setRowCount(0);

        for (Equipo equipo : equipos) {
            modelo.addRow(new Object[]{
                    equipo.getCodigoInventario(),
                    equipo.getNombre(),
                    equipo.getTipo(),
                    equipo.getLaboratorio(),
                    equipo.getEstado()
            });
        }
    }


    /*
     * ========================================================
     * CONFIRMAR PRÉSTAMO
     *
     * Se arma el encabezado y un DetallePrestamo por cada equipo.
     * El Service vuelve a validar todo y lo guarda en una sola
     * transacción: encabezado, detalle, estados y auditoría.
     * ========================================================
     */
    private void confirmarPrestamo() {
        try {
            Prestamo prestamo = new Prestamo(txtSolicitante.getText(), txtIdentificacion.getText(), null,
                    SelectorFecha.leer(spnFechaEsperada, "devolución esperada"), null, txtObservaciones.getText());

            for (Equipo equipo : equipos) {
                prestamo.getDetalles().add(new DetallePrestamo(equipo));
            }

            Prestamo registrado = prestamoController.registrarPrestamo(prestamo);

            Dialogos.informar(panelPrincipal, "Préstamo N° " + registrado.getIdPrestamo() + " registrado con " +
                    registrado.getDetalles().size() + " equipo(s).\nLos equipos quedaron en estado PRESTADO.", "Préstamo");
            nuevo();
        } catch (Exception e) {
            Dialogos.error(panelPrincipal, e);
        }
    }


    /*
     * ========================================================
     * NUEVO
     * ========================================================
     */
    private void nuevo() {
        txtSolicitante.setText("");
        txtIdentificacion.setText("");
        txtObservaciones.setText("");
        txtCodigoEquipo.setText("");
        spnFechaEsperada.setValue(Fechas.aDate(LocalDateTime.now().plusDays(1)));

        equipos.clear();
        mostrarEquipos();

        txtSolicitante.requestFocus();
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

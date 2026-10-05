package universidad.sigelab.view;

import universidad.sigelab.controller.AuditoriaController;
import universidad.sigelab.enums.AccionAuditoria;
import universidad.sigelab.model.Auditoria;
import universidad.sigelab.util.Fechas;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.List;

public class AuditoriaView {

    private static final String TODAS = "TODAS";

    // Componentes vinculados desde AuditoriaView.form
    private JPanel panelPrincipal;

    private JComboBox<Object> cboAccion;
    private JButton btnConsultar;
    private JLabel lblTotal;

    private JTable tblAuditoria;
    private JButton btnSalir;

    // La View solamente conoce al Controller.
    private final AuditoriaController auditoriaController;


    public AuditoriaView() {
        this(new AuditoriaController());
    }

    public AuditoriaView(AuditoriaController auditoriaController) {
        this.auditoriaController = auditoriaController;

        configurarFormulario();
        configurarEventos();
        consultar();
    }


    /*
     * Configuración inicial.
     */
    private void configurarFormulario() {
        // La primera opción muestra los eventos de todas las acciones.
        cboAccion.addItem(TODAS);
        for (AccionAuditoria accion : AccionAuditoria.values()) cboAccion.addItem(accion);

        tblAuditoria.setModel(new TablaSoloLectura("Fecha y hora", "Usuario", "Acción", "Entidad",
                "ID del registro", "Descripción"));
        tblAuditoria.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    private void configurarEventos() {
        btnConsultar.addActionListener(e -> consultar());
        cboAccion.addActionListener(e -> consultar());
        btnSalir.addActionListener(e -> salir());
    }


    /*
     * ========================================================
     * CONSULTAR
     * La auditoría es de solo lectura: no se edita ni se borra.
     * ========================================================
     */
    private void consultar() {
        try {
            // Con "TODAS" el Controller recibe null y no filtra por acción.
            Object seleccion = cboAccion.getSelectedItem();
            AccionAuditoria accion = seleccion instanceof AccionAuditoria elegida ? elegida : null;

            List<Auditoria> eventos = auditoriaController.listar(accion);

            DefaultTableModel modelo = (DefaultTableModel) tblAuditoria.getModel();
            modelo.setRowCount(0);

            for (Auditoria evento : eventos) {
                modelo.addRow(new Object[]{
                        Fechas.texto(evento.getFechaHora()),
                        // Un login fallido con un usuario inexistente no tiene usuario asociado.
                        evento.getUsuario() == null ? "(desconocido)" : evento.getUsuario().getUsuario(),
                        evento.getAccion(),
                        evento.getEntidad(),
                        evento.getIdRegistro() == null ? "" : evento.getIdRegistro(),
                        evento.getDescripcion()
                });
            }

            lblTotal.setText(eventos.size() + " evento(s)");
        } catch (Exception e) {
            Dialogos.error(panelPrincipal, e);
        }
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

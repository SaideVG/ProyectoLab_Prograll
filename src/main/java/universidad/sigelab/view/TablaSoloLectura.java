package universidad.sigelab.view;

import javax.swing.table.DefaultTableModel;

/** Modelo de JTable con encabezados fijos y celdas que el usuario no puede editar. */
class TablaSoloLectura extends DefaultTableModel {
    private static final long serialVersionUID = 1L;

    TablaSoloLectura(String... columnas) {
        super(columnas, 0);
    }

    @Override
    public boolean isCellEditable(int fila, int columna) {
        return false;
    }
}

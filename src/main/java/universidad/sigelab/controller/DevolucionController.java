package universidad.sigelab.controller;

import universidad.sigelab.enums.CondicionDevolucion;
import universidad.sigelab.model.DetallePrestamo;
import universidad.sigelab.model.Devolucion;
import universidad.sigelab.model.PrestamoResumen;
import universidad.sigelab.service.DevolucionService;

import java.util.List;
import java.util.Optional;

public class DevolucionController {
    private final DevolucionService service;
    public DevolucionController() { this(new DevolucionService()); }
    public DevolucionController(DevolucionService service) { this.service = service; }
    public List<PrestamoResumen> listarPrestamosActivos() { return service.listarPrestamosActivos(); }
    public Optional<PrestamoResumen> buscarPrestamo(int idPrestamo) { return service.buscarPrestamo(idPrestamo); }
    public List<DetallePrestamo> listarEquipos(int idPrestamo) { return service.listarEquipos(idPrestamo); }
    public Devolucion registrarDevolucion(int idDetallePrestamo, CondicionDevolucion condicion, String observacion) { return service.registrarDevolucion(idDetallePrestamo, condicion, observacion); }
}

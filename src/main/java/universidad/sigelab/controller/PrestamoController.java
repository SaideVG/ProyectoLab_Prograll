package universidad.sigelab.controller;

import universidad.sigelab.model.Equipo;
import universidad.sigelab.model.Prestamo;
import universidad.sigelab.service.PrestamoService;

import java.util.List;

public class PrestamoController {
    private final PrestamoService service;
    public PrestamoController() { this(new PrestamoService()); }
    public PrestamoController(PrestamoService service) { this.service = service; }
    public Equipo validarEquipoParaPrestamo(String codigo, List<Equipo> yaAgregados) { return service.validarEquipoParaPrestamo(codigo, yaAgregados); }
    public Prestamo registrarPrestamo(Prestamo prestamo) { return service.registrarPrestamo(prestamo); }
}

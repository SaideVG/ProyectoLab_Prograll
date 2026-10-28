package universidad.sigelab.api.dto;

import java.util.List;

public record PrestamoDetalleDto(PrestamoDto prestamo, List<DetallePrestamoDto> equipos) { }

package universidad.sigelab.api.dto;

import java.util.List;

/** Lo que muestra la app al escanear el QR de un equipo: sus datos, préstamos y mantenimientos. */
public record EquipoDetalleDto(EquipoDto equipo, List<HistorialPrestamoDto> prestamos,
                               List<MantenimientoDto> mantenimientos) { }

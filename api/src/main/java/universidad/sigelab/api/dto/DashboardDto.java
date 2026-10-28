package universidad.sigelab.api.dto;

/** Mismas cantidades que el menú principal del escritorio. */
public record DashboardDto(int totalEquipos, int disponibles, int prestados, int danados,
                           int enMantenimiento, int deBaja, int prestamosActivos, int prestamosVencidos) { }

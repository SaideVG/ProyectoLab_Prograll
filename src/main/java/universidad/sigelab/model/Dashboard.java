package universidad.sigelab.model;

/** Cantidades que muestra el menú principal. Todas se calculan en la base de datos. */
public record Dashboard(int totalEquipos, int disponibles, int prestados, int danados,
                        int enMantenimiento, int deBaja, int prestamosActivos, int prestamosVencidos) { }

package universidad.sigelab.api.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import universidad.sigelab.api.dto.ErrorDto;
import universidad.sigelab.api.util.AccesoDenegadoException;
import universidad.sigelab.api.util.BusinessException;
import universidad.sigelab.api.util.DemasiadosIntentosException;
import universidad.sigelab.api.util.NoAutenticadoException;
import universidad.sigelab.api.util.RecursoNoEncontradoException;

/**
 * Convierte cada excepción en una respuesta JSON {"mensaje": "..."} con su
 * código HTTP. Cumple el mismo papel que los JOptionPane de error del escritorio:
 * la app muestra el mensaje tal cual.
 */
@RestControllerAdvice
public class ManejadorErrores {

    private static final Logger LOG = LoggerFactory.getLogger(ManejadorErrores.class);

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ErrorDto> noEncontrado(RecursoNoEncontradoException e) {
        return responder(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(DemasiadosIntentosException.class)
    public ResponseEntity<ErrorDto> demasiadosIntentos(DemasiadosIntentosException e) {
        return responder(HttpStatus.TOO_MANY_REQUESTS, e.getMessage());
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorDto> negocio(BusinessException e) {
        return responder(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(NoAutenticadoException.class)
    public ResponseEntity<ErrorDto> noAutenticado(NoAutenticadoException e) {
        return responder(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    @ExceptionHandler(AccesoDenegadoException.class)
    public ResponseEntity<ErrorDto> accesoDenegado(AccesoDenegadoException e) {
        return responder(HttpStatus.FORBIDDEN, e.getMessage());
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ErrorDto> peticionInvalida(Exception e) {
        return responder(HttpStatus.BAD_REQUEST, "Los datos enviados no tienen el formato esperado.");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorDto> rutaInexistente(NoResourceFoundException e) {
        return responder(HttpStatus.NOT_FOUND, "La ruta solicitada no existe.");
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorDto> baseDeDatos(DataAccessException e) {
        LOG.error("Error de base de datos", e);
        return responder(HttpStatus.SERVICE_UNAVAILABLE, "No se pudo consultar la base de datos. Intente más tarde.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDto> inesperado(Exception e) {
        LOG.error("Error inesperado", e);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error inesperado en el servidor.");
    }

    private ResponseEntity<ErrorDto> responder(HttpStatus estado, String mensaje) {
        return ResponseEntity.status(estado).body(new ErrorDto(mensaje));
    }
}

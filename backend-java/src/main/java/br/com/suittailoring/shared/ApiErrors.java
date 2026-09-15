package br.com.suittailoring.shared;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import jakarta.persistence.EntityNotFoundException;
@RestControllerAdvice
public class ApiErrors {
    @ExceptionHandler({BusinessException.class, IllegalArgumentException.class})
    ResponseEntity<ProblemDetail> invalid(RuntimeException ex) { return error(400, ex.getMessage()); }
    @ExceptionHandler(EntityNotFoundException.class)
    ResponseEntity<ProblemDetail> missing() { return error(404, "Recurso não encontrado"); }
    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ProblemDetail> conflict() { return error(409, "Registro duplicado ou operação incompatível com o estado atual"); }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ProblemDetail> validation(MethodArgumentNotValidException ex) {
        return error(400, ex.getBindingResult().getFieldErrors().stream()
            .map(e -> e.getField() + ": " + e.getDefaultMessage()).distinct().sorted().collect(java.util.stream.Collectors.joining("; ")));
    }
    @ExceptionHandler(AuthenticationException.class)
    ResponseEntity<ProblemDetail> authentication() { return error(401, "Credenciais inválidas"); }
    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ProblemDetail> denied() { return error(403, "Acesso negado"); }
    private ResponseEntity<ProblemDetail> error(int status, String detail) {
        return ResponseEntity.status(status).body(ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(status), detail));
    }
}


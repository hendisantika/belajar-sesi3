package id.autochem.belajarsesi3.common.web;

import id.autochem.belajarsesi3.coa.service.AccountNotFoundException;
import id.autochem.belajarsesi3.coa.service.DuplicateAccountException;
import id.autochem.belajarsesi3.coa.service.InvalidParentAccountException;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.core.PropertyReferenceException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;
import java.util.stream.Stream;

/**
 * Created by IntelliJ IDEA.
 * Project : belajar-sesi3
 * User: hendisantika
 * Email: hendisantika@gmail.com
 * Telegram : @hendisantika34
 * Link : s.id/hendisantika
 * Date: 24/11/26
 * Time: 12.45
 */

/**
 * Satu tempat untuk mapping exception -> HTTP status dengan format ProblemDetail (RFC 9457).
 * Turunan {@link ResponseEntityExceptionHandler} agar exception bawaan Spring MVC
 * (JSON rusak, method tidak didukung, tipe parameter salah, dst.) juga memakai format yang sama.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(AccountNotFoundException.class)
    ProblemDetail notFound(AccountNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(DuplicateAccountException.class)
    ProblemDetail duplicate(DuplicateAccountException ex) {
        return problem(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(InvalidParentAccountException.class)
    ProblemDetail invalidParent(InvalidParentAccountException ex) {
        return problem(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // Pengaman race condition: dua request lolos cek existsByCode bersamaan, unique constraint DB menolak
    @ExceptionHandler(DataIntegrityViolationException.class)
    ProblemDetail dataIntegrity(DataIntegrityViolationException ex) {
        log.warn("Pelanggaran constraint database: {}", ex.getMostSpecificCause().getMessage());
        return problem(HttpStatus.CONFLICT, "Data bertentangan dengan data yang sudah ada");
    }

    @ExceptionHandler(MissingIdempotencyKeyException.class)
    ProblemDetail missingIdempotencyKey(MissingIdempotencyKeyException ex) {
        return problem(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(PropertyReferenceException.class)
    ProblemDetail invalidSort(PropertyReferenceException ex) {
        return problem(HttpStatus.BAD_REQUEST, "Properti sort tidak dikenal: " + ex.getPropertyName());
    }

    // Fallback: log lengkap di server, pesan ringkas ke client (tanpa stack trace / SQL)
    @ExceptionHandler(Exception.class)
    ProblemDetail unexpected(Exception ex) {
        log.error("Unhandled exception", ex);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR,
                "Terjadi kesalahan pada server. Sertakan requestId saat melapor.");
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle("Validasi gagal");
        List<String> errors = Stream.concat(
                        ex.getBindingResult().getFieldErrors().stream()
                                .map(e -> e.getField() + ": " + e.getDefaultMessage()),
                        ex.getBindingResult().getGlobalErrors().stream()
                                .map(e -> e.getObjectName() + ": " + e.getDefaultMessage()))
                .sorted()
                .toList();
        pd.setProperty("errors", errors);
        return handleExceptionInternal(ex, pd, headers, status, request);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "Body request tidak bisa dibaca: format JSON salah atau nilai enum tidak dikenal");
        return handleExceptionInternal(ex, pd, headers, status, request);
    }

    // Semua ProblemDetail dari Spring MVC melewati method ini -> tambahkan requestId
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers,
                                                             HttpStatusCode statusCode, WebRequest request) {
        ResponseEntity<Object> response = super.handleExceptionInternal(ex, body, headers, statusCode, request);
        if (response != null && response.getBody() instanceof ProblemDetail pd) {
            withRequestId(pd);
        }
        return response;
    }

    private static ProblemDetail problem(HttpStatus status, String detail) {
        return withRequestId(ProblemDetail.forStatusAndDetail(status, detail));
    }

    private static ProblemDetail withRequestId(ProblemDetail pd) {
        String requestId = MDC.get(CorrelationIdFilter.MDC_KEY);
        if (requestId != null) {
            pd.setProperty("requestId", requestId);
        }
        return pd;
    }
}

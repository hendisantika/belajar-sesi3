package id.autochem.belajarsesi3.common.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

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
// preHandle() membaca anotasi pada method controller tujuan; gagal -> controller tidak dipanggil.
@Component
public class IdempotencyKeyInterceptor implements HandlerInterceptor {

    public static final String HEADER = "Idempotency-Key";

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) {
        if (handler instanceof HandlerMethod hm
                && hm.hasMethodAnnotation(RequiresIdempotencyKey.class)) {
            String key = req.getHeader(HEADER);
            if (key == null || key.isBlank()) {
                // Dilempar (bukan res.sendError) agar GlobalExceptionHandler membentuk ProblemDetail
                // yang sama dengan error lain; efeknya tetap: request berhenti sebelum controller.
                throw new MissingIdempotencyKeyException();
            }
        }
        return true;                                        // lanjut ke controller
    }
    // Opsional: postHandle() & afterCompletion() (mirip terminate() di Laravel)
}

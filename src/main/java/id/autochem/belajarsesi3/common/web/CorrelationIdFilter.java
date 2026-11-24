package id.autochem.belajarsesi3.common.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

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
// Filter level Servlet: ambil/buat ID, simpan di MDC & header respons, lalu lanjutkan chain.
// Tanpa @Component: didaftarkan lewat FilterRegistrationBean di WebFilterConfig.
public class CorrelationIdFilter extends OncePerRequestFilter {

    public static final String HEADER = "X-Correlation-Id";
    public static final String MDC_KEY = "correlationId";

    // Nilai dari client hanya dipakai bila aman ditulis ke log (cegah log injection / header raksasa)
    private static final Pattern SAFE_ID = Pattern.compile("[A-Za-z0-9._-]{1,64}");

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res,
                                    FilterChain chain) throws ServletException, IOException {
        String id = Optional.ofNullable(req.getHeader(HEADER))
                .filter(v -> SAFE_ID.matcher(v).matches())
                .orElseGet(() -> UUID.randomUUID().toString());
        MDC.put(MDC_KEY, id);         // ikut di setiap baris log
        res.setHeader(HEADER, id);    // dikembalikan ke client
        try {
            chain.doFilter(req, res); // = $next($request)
        } finally {
            MDC.remove(MDC_KEY);      // thread dipakai ulang request lain
        }
    }
}

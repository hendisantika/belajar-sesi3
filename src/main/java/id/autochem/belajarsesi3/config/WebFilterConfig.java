package id.autochem.belajarsesi3.config;

import id.autochem.belajarsesi3.common.web.AccessLogFilter;
import id.autochem.belajarsesi3.common.web.CorrelationIdFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

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
// FilterRegistrationBean mengatur URL pattern dan urutan: angka lebih kecil berjalan lebih dulu.
@Configuration
public class WebFilterConfig {

    @Bean
    FilterRegistrationBean<CorrelationIdFilter> correlationIdFilter() {
        var reg = new FilterRegistrationBean<>(new CorrelationIdFilter());
        reg.addUrlPatterns("/*");                           // semua request
        reg.setOrder(Ordered.HIGHEST_PRECEDENCE);           // paling awal
        return reg;
    }

    @Bean
    FilterRegistrationBean<AccessLogFilter> accessLogFilter() {
        var reg = new FilterRegistrationBean<>(new AccessLogFilter());
        reg.addUrlPatterns("/api/*");                       // hanya API
        reg.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);      // setelah correlation ID
        return reg;
    }
}

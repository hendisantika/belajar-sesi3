package id.autochem.belajarsesi3.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * Project : belajar-sesi3
 * User: hendisantika
 * Email: hendisantika@gmail.com
 * Telegram : @hendisantika34
 * Link : s.id/hendisantika
 * Date: 24/11/26
 * Time: 12.46
 */
@Configuration
@SecurityScheme(
        name = OpenApiConfig.BEARER_AUTH,
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT")                               // tombol Authorize di Swagger UI
public class OpenApiConfig {

    public static final String BEARER_AUTH = "bearerAuth";

    @Bean
    OpenAPI accountingOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Autochem Accounting API")
                        .version("v1")
                        .description("API Chart of Accounts, Journal, Posting & Reporting")
                        .contact(new Contact().name("Tim Engineering Autochem")
                                .email("engineering@example.id")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Lokal"),
                        new Server().url("https://staging-api.example.id").description("Staging")));
    }

    @Bean
    GroupedOpenApi coaApi() {
        return GroupedOpenApi.builder()
                .group("1-coa")
                .displayName("Chart of Accounts")
                .pathsToMatch("/api/v1/accounts/**")
                .build();
    }

    @Bean
    GroupedOpenApi journalApi() {
        return GroupedOpenApi.builder()
                .group("2-journal")
                .displayName("Journal & Posting")
                .pathsToMatch("/api/v1/journals/**", "/api/v1/ledger/**")
                .build();
    }
    // Swagger UI menampilkan dropdown grup; spec per grup: /v3/api-docs/1-coa
    // Bila Spring Security aktif (Sesi 10), izinkan akses dokumentasi:
    //   .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
}

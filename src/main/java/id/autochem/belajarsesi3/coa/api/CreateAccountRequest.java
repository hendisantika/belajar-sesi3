package id.autochem.belajarsesi3.coa.api;

import id.autochem.belajarsesi3.coa.domain.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Created by IntelliJ IDEA.
 * Project : belajar-sesi3
 * User: hendisantika
 * Email: hendisantika@gmail.com
 * Telegram : @hendisantika34
 * Link : s.id/hendisantika
 * Date: 24/11/26
 * Time: 12.43
 */
public record CreateAccountRequest(

        @NotBlank
        @Pattern(regexp = "\\d{4}", message = "harus 4 digit angka")
        String code,

        @NotBlank
        @Size(max = 150)
        String name,

        @NotNull
        AccountType type,

        @Pattern(regexp = "\\d{4}", message = "harus 4 digit angka")
        String parentCode) {
}

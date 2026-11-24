package id.autochem.belajarsesi3.coa.api;

import id.autochem.belajarsesi3.coa.domain.AccountType;
import io.swagger.v3.oas.annotations.media.Schema;
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
// Bean Validation (@NotBlank, @Size, @Pattern) otomatis menjadi aturan di skema OpenAPI
@Schema(description = "Data untuk membuat akun COA")
public record CreateAccountRequest(

        @Schema(description = "Kode akun 4 digit", example = "1301")
        @NotBlank
        @Pattern(regexp = "\\d{4}", message = "harus 4 digit angka")
        String code,

        @Schema(description = "Nama akun", example = "Persediaan", maxLength = 150)
        @NotBlank
        @Size(max = 150)
        String name,

        @Schema(description = "Tipe akun", example = "ASSET")
        @NotNull
        AccountType type,

        @Schema(description = "Kode akun induk (opsional)", example = "1000",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Pattern(regexp = "\\d{4}", message = "harus 4 digit angka")
        String parentCode) {
}

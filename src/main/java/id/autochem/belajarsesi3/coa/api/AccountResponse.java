package id.autochem.belajarsesi3.coa.api;

import id.autochem.belajarsesi3.coa.domain.AccountType;
import io.swagger.v3.oas.annotations.media.Schema;

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
// Record: DTO immutable, equals/hashCode/toString otomatis. Entity tidak pernah keluar ke API.
@Schema(description = "Detail akun COA")
public record AccountResponse(
        @Schema(description = "ID akun", example = "6") Long id,
        @Schema(description = "Kode akun", example = "1301") String code,
        @Schema(description = "Nama akun", example = "Persediaan") String name,
        @Schema(description = "Tipe akun", example = "ASSET") AccountType type,
        @Schema(description = "Kode akun induk", example = "1000", nullable = true) String parentCode,
        @Schema(description = "Status aktif", example = "true") boolean active) {
}

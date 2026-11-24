package id.autochem.belajarsesi3.coa.api;

import id.autochem.belajarsesi3.coa.domain.AccountType;

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
public record AccountResponse(
        Long id,
        String code,
        String name,
        AccountType type,
        String parentCode,
        boolean active) {
}

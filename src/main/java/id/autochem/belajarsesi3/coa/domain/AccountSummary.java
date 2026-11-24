package id.autochem.belajarsesi3.coa.domain;

/**
 * Created by IntelliJ IDEA.
 * Project : belajar-sesi3
 * User: hendisantika
 * Email: hendisantika@gmail.com
 * Telegram : @hendisantika34
 * Link : s.id/hendisantika
 * Date: 24/11/26
 * Time: 12.42
 */
// Projection: hanya kolom yang dibutuhkan daftar akun, bukan seluruh entity.
public record AccountSummary(Long id, String code, String name, AccountType type) {
}

package id.autochem.belajarsesi3.journal.api;

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
public record PostResult(Long journalId, String status, String idempotencyKey) {
}

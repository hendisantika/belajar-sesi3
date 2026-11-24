package id.autochem.belajarsesi3.coa.service;

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
// Unchecked exception untuk aturan bisnis -> 404 di GlobalExceptionHandler
public class AccountNotFoundException extends RuntimeException {

    public AccountNotFoundException(Long id) {
        super("Akun %d tidak ditemukan".formatted(id));
    }
}

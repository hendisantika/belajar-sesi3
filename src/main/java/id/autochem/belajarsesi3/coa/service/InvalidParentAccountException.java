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
// Akun induk tidak valid (tidak ada, beda tipe, nonaktif, atau membentuk siklus) -> 400
public class InvalidParentAccountException extends RuntimeException {

    public InvalidParentAccountException(String message) {
        super(message);
    }
}

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
// Pelanggaran keunikan kode akun -> 409 Conflict
public class DuplicateAccountException extends RuntimeException {

    public DuplicateAccountException(String code) {
        super("Kode akun %s sudah digunakan".formatted(code));
    }
}

package id.autochem.belajarsesi3.journal.service;

import id.autochem.belajarsesi3.journal.api.JournalResponse;
import id.autochem.belajarsesi3.journal.api.PostResult;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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
// Placeholder: logika jurnal & posting diimplementasikan di Sesi 4-5.
// Di Sesi 3 modul ini hanya dipakai untuk mendemokan alur filter & interceptor.
@Service
public class JournalService {

    public PostResult post(Long id, String idempotencyKey) {
        throw notImplemented();
    }

    public JournalResponse findById(Long id) {
        throw notImplemented();
    }

    private static ResponseStatusException notImplemented() {
        return new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED,
                "Fitur jurnal diimplementasikan di Sesi 4");
    }
}

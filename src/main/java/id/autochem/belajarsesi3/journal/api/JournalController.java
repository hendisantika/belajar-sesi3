package id.autochem.belajarsesi3.journal.api;

import id.autochem.belajarsesi3.common.web.IdempotencyKeyInterceptor;
import id.autochem.belajarsesi3.common.web.RequiresIdempotencyKey;
import id.autochem.belajarsesi3.journal.service.JournalService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
@RestController
@RequestMapping("/api/v1/journals")
@RequiredArgsConstructor
public class JournalController {

    private final JournalService journalService;

    @PostMapping("/{id}/post")
    @RequiresIdempotencyKey                                 // wajib header Idempotency-Key
    public PostResult post(@PathVariable Long id,
                           @RequestHeader(IdempotencyKeyInterceptor.HEADER) String key) {
        return journalService.post(id, key);
    }

    @GetMapping("/{id}")                                    // tanpa anotasi: bebas
    public JournalResponse get(@PathVariable Long id) {
        return journalService.findById(id);
    }
}

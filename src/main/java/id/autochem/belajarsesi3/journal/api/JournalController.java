package id.autochem.belajarsesi3.journal.api;

import id.autochem.belajarsesi3.common.web.IdempotencyKeyInterceptor;
import id.autochem.belajarsesi3.common.web.RequiresIdempotencyKey;
import id.autochem.belajarsesi3.config.OpenApiConfig;
import id.autochem.belajarsesi3.journal.service.JournalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ProblemDetail;
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
// Endpoint butuh token (dipakai penuh di Sesi 10)
@Tag(name = "Journal", description = "Pembuatan & posting jurnal (implementasi Sesi 4-5)")
@SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
@RestController
@RequestMapping("/api/v1/journals")
@RequiredArgsConstructor
public class JournalController {

    private final JournalService journalService;

    @Operation(summary = "Posting jurnal ke buku besar")
    @ApiResponse(responseCode = "200", description = "Berhasil diposting")
    @ApiResponse(responseCode = "400", description = "Header Idempotency-Key tidak dikirim",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "409", description = "Jurnal sudah diposting",
            content = @Content(mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    @PostMapping("/{id}/post")
    @RequiresIdempotencyKey                                 // wajib header Idempotency-Key
    public PostResult post(@PathVariable Long id,
                           @RequestHeader(IdempotencyKeyInterceptor.HEADER) String key) {
        return journalService.post(id, key);
    }

    @Operation(summary = "Detail jurnal")
    @GetMapping("/{id}")                                    // tanpa anotasi: bebas
    public JournalResponse get(@PathVariable Long id) {
        return journalService.findById(id);
    }
}

package id.autochem.belajarsesi3.common.web;

import id.autochem.belajarsesi3.config.WebFilterConfig;
import id.autochem.belajarsesi3.config.WebMvcConfig;
import id.autochem.belajarsesi3.journal.api.JournalController;
import id.autochem.belajarsesi3.journal.api.PostResult;
import id.autochem.belajarsesi3.journal.service.JournalService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.matchesPattern;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by IntelliJ IDEA.
 * Project : belajar-sesi3
 * User: hendisantika
 * Email: hendisantika@gmail.com
 * Telegram : @hendisantika34
 * Link : s.id/hendisantika
 * Date: 24/11/26
 * Time: 12.54
 */
// Bukti otomatis: tanpa header -> 400 dan service tidak dipanggil; correlation ID dari client dipakai ulang.
@WebMvcTest(JournalController.class)
@Import({WebMvcConfig.class, IdempotencyKeyInterceptor.class, WebFilterConfig.class})
class MiddlewareTest {

    private static final String UUID_REGEX = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";

    @Autowired
    MockMvc mvc;

    @MockitoBean
    JournalService journalService;

    @Test
    void tanpa_idempotency_key_400_dan_controller_tidak_dipanggil() throws Exception {
        mvc.perform(post("/api/v1/journals/1/post"))
                .andExpect(status().isBadRequest())
                .andExpect(header().exists("X-Correlation-Id"))
                .andExpect(jsonPath("$.detail").value("Header Idempotency-Key wajib diisi"));
        verifyNoInteractions(journalService);
    }

    @Test
    void dengan_idempotency_key_lanjut_ke_controller() throws Exception {
        when(journalService.post(1L, "post-1")).thenReturn(new PostResult(1L, "POSTED", "post-1"));

        mvc.perform(post("/api/v1/journals/1/post").header("Idempotency-Key", "post-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("POSTED"));
        verify(journalService).post(1L, "post-1");
    }

    @Test
    void correlation_id_dari_client_dipakai_ulang() throws Exception {
        mvc.perform(get("/api/v1/journals/1").header("X-Correlation-Id", "abc-123"))
                .andExpect(header().string("X-Correlation-Id", "abc-123"));
    }

    @Test
    void correlation_id_dibuat_otomatis_bila_tidak_dikirim() throws Exception {
        mvc.perform(get("/api/v1/journals/1"))
                .andExpect(header().string("X-Correlation-Id", matchesPattern(UUID_REGEX)));
    }

    @Test
    void correlation_id_tidak_aman_diganti_uuid_baru() throws Exception {
        mvc.perform(get("/api/v1/journals/1").header("X-Correlation-Id", "abc\r\nINFO palsu"))
                .andExpect(header().string("X-Correlation-Id", matchesPattern(UUID_REGEX)));
    }
}

package id.autochem.belajarsesi3.coa.api;

import id.autochem.belajarsesi3.coa.domain.AccountSummary;
import id.autochem.belajarsesi3.coa.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

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
// Controller tipis: terima, validasi, delegasikan ke service.
@Tag(name = "Chart of Accounts", description = "Kelola akun COA")
@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    private static final String PROBLEM_JSON = "application/problem+json";

    // "type" tidak ditulis karena nilainya default about:blank (RFC 9457)
    private static final String EXAMPLE_400 = """
            {"title":"Validasi gagal","status":400,"errors":["code: must not be blank"],"requestId":"a1b2c3"}""";
    private static final String EXAMPLE_404 = """
            {"title":"Not Found","status":404,"detail":"Akun 99 tidak ditemukan","requestId":"d4e5f6"}""";
    private static final String EXAMPLE_409 = """
            {"title":"Conflict","status":409,"detail":"Kode akun 1101 sudah digunakan","requestId":"g7h8i9"}""";

    private final AccountService service;

    @Operation(summary = "Buat akun baru",
            description = "Kode akun 4 digit dan harus unik. Akun induk (opsional) harus aktif dan bertipe sama.")
    @ApiResponse(responseCode = "201", description = "Akun dibuat; header Location berisi URL akun baru")
    @ApiResponse(responseCode = "400", description = "Payload tidak valid",
            content = @Content(mediaType = PROBLEM_JSON, schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(EXAMPLE_400)))
    @ApiResponse(responseCode = "409", description = "Kode akun sudah dipakai",
            content = @Content(mediaType = PROBLEM_JSON, schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(EXAMPLE_409)))
    @PostMapping
    public ResponseEntity<AccountResponse> create(@Valid @RequestBody CreateAccountRequest req) {
        AccountResponse created = service.create(req);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    // GET /api/v1/accounts?page=0&size=20&sort=name,asc -> hanya akun aktif
    @Operation(summary = "Daftar akun aktif (paginated)",
            description = "Query string: page (mulai 0), size (maks 100), sort (mis. code,asc atau name,desc).")
    @ApiResponse(responseCode = "200", description = "Halaman akun aktif")
    @ApiResponse(responseCode = "400", description = "Parameter sort tidak dikenal",
            content = @Content(mediaType = PROBLEM_JSON, schema = @Schema(implementation = ProblemDetail.class)))
    @GetMapping
    public PagedModel<AccountSummary> list(
            @ParameterObject @PageableDefault(size = 20, sort = "code") Pageable pageable) {
        return new PagedModel<>(service.listActive(pageable));
    }

    @Operation(summary = "Detail akun", description = "Mengembalikan akun aktif maupun nonaktif.")
    @ApiResponse(responseCode = "200", description = "Akun ditemukan")
    @ApiResponse(responseCode = "404", description = "Akun tidak ditemukan",
            content = @Content(mediaType = PROBLEM_JSON, schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(EXAMPLE_404)))
    @GetMapping("/{id}")
    public AccountResponse get(@Parameter(description = "ID akun", example = "1") @PathVariable Long id) {
        return service.findById(id);
    }

    @Operation(summary = "Ubah akun", description = "Mengganti nama, tipe, dan akun induk. Kode akun tidak bisa diubah.")
    @ApiResponse(responseCode = "200", description = "Akun diperbarui")
    @ApiResponse(responseCode = "400", description = "Payload tidak valid",
            content = @Content(mediaType = PROBLEM_JSON, schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(EXAMPLE_400)))
    @ApiResponse(responseCode = "404", description = "Akun tidak ditemukan",
            content = @Content(mediaType = PROBLEM_JSON, schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(EXAMPLE_404)))
    @PutMapping("/{id}")
    public AccountResponse update(@Parameter(description = "ID akun", example = "1") @PathVariable Long id,
                                  @Valid @RequestBody UpdateAccountRequest req) {
        return service.update(id, req);
    }

    @Operation(summary = "Nonaktifkan akun",
            description = "Idempotent: akun nonaktif tidak muncul di daftar, tetapi tetap bisa dilihat detailnya.")
    @ApiResponse(responseCode = "204", description = "Akun dinonaktifkan")
    @ApiResponse(responseCode = "404", description = "Akun tidak ditemukan",
            content = @Content(mediaType = PROBLEM_JSON, schema = @Schema(implementation = ProblemDetail.class),
                    examples = @ExampleObject(EXAMPLE_404)))
    @PatchMapping("/{id}/deactivate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@Parameter(description = "ID akun", example = "1") @PathVariable Long id) {
        service.deactivate(id);
    }
}

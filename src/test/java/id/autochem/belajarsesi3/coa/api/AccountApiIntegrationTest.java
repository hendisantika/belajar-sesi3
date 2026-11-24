package id.autochem.belajarsesi3.coa.api;

import id.autochem.belajarsesi3.TestcontainersConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
// 6 skenario uji API (positif & negatif) dari Hands-on Sesi 3, end-to-end ke PostgreSQL asli.
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class AccountApiIntegrationTest {

    private static final String BASE = "/api/v1/accounts";

    @Autowired
    MockMvc mvc;

    @Autowired
    JdbcTemplate jdbc;

    @AfterEach
    void cleanUp() { // sisakan 5 akun induk dari migration V2 (seed_root_accounts)
        jdbc.update("DELETE FROM account WHERE code NOT IN ('1000','2000','3000','4000','5000')");
    }

    @Test
    @DisplayName("1. Buat akun valid -> 201 + Location, tersimpan di tabel account")
    void createValidAccount() throws Exception {
        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"1101","name":"Kas","type":"ASSET","parentCode":"1000"}"""))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString(BASE + "/")))
                .andExpect(jsonPath("$.id").value(notNullValue()))
                .andExpect(jsonPath("$.code").value("1101"))
                .andExpect(jsonPath("$.parentCode").value("1000"))
                .andExpect(jsonPath("$.active").value(true));

        Map<String, Object> row = jdbc.queryForMap(
                "SELECT a.name, a.type, a.active, p.code AS parent FROM account a "
                        + "LEFT JOIN account p ON p.id = a.parent_id WHERE a.code = '1101'");
        assertThat(row).containsEntry("name", "Kas").containsEntry("type", "ASSET")
                .containsEntry("active", true).containsEntry("parent", "1000");
    }

    @Test
    @DisplayName("2. Kode duplikat -> 409 Conflict sesuai error contract")
    void duplicateCode() throws Exception {
        createAccount("1101", "Kas");

        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
                        .header("X-Correlation-Id", "dup-test-1")
                        .content("""
                                {"code":"1101","name":"Kas Kecil","type":"ASSET"}"""))
                .andExpect(status().isConflict())
                .andExpect(header().string("Content-Type", "application/problem+json"))
                .andExpect(jsonPath("$.title").value("Conflict"))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail").value("Kode akun 1101 sudah digunakan"))
                .andExpect(jsonPath("$.requestId").value("dup-test-1"));

        assertThat(jdbc.queryForObject("SELECT count(*) FROM account WHERE code = '1101'", Long.class))
                .isEqualTo(1L);
    }

    @Test
    @DisplayName("3. Payload tidak valid -> 400 + daftar error")
    void invalidPayload() throws Exception {
        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content("""
                        {"code":""}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validasi gagal"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors").value(hasItem("code: must not be blank")))
                .andExpect(jsonPath("$.errors").value(hasItem("name: must not be blank")))
                .andExpect(jsonPath("$.errors").value(hasItem("type: must not be null")))
                .andExpect(jsonPath("$.requestId").value(notNullValue()));
    }

    @Test
    @DisplayName("3b. Nilai enum tidak dikenal -> 400")
    void unknownEnumValue() throws Exception {
        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content("""
                        {"code":"1102","name":"Bank","type":"FOO"}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("3c. Akun induk beda tipe -> 400")
    void parentWithDifferentType() throws Exception {
        mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content("""
                        {"code":"1103","name":"Piutang","type":"ASSET","parentCode":"2000"}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Tipe akun induk (LIABILITY) harus sama dengan tipe akun (ASSET)"));
    }

    @Test
    @DisplayName("4. Detail tidak ada -> 404 Not Found")
    void accountNotFound() throws Exception {
        mvc.perform(get(BASE + "/9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.detail").value("Akun 9999 tidak ditemukan"))
                .andExpect(jsonPath("$.requestId").value(notNullValue()));
    }

    @Test
    @DisplayName("5. Ubah nama akun -> 200 + data baru, tersimpan di tabel")
    void updateAccountName() throws Exception {
        long id = createAccount("1101", "Kas");

        mvc.perform(put(BASE + "/" + id).contentType(MediaType.APPLICATION_JSON).content("""
                        {"name":"Kas Besar","type":"ASSET","parentCode":"1000"}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.code").value("1101"))
                .andExpect(jsonPath("$.name").value("Kas Besar"));

        assertThat(jdbc.queryForObject("SELECT name FROM account WHERE id = ?", String.class, id))
                .isEqualTo("Kas Besar");
    }

    @Test
    @DisplayName("5b. Akun menjadi induk dirinya sendiri -> 400")
    void updateWithSelfAsParent() throws Exception {
        long id = createAccount("1101", "Kas");

        mvc.perform(put(BASE + "/" + id).contentType(MediaType.APPLICATION_JSON).content("""
                        {"name":"Kas","type":"ASSET","parentCode":"1101"}"""))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("6. Nonaktifkan akun -> 204; idempotent; tidak muncul di daftar aktif")
    void deactivateAccount() throws Exception {
        long id = createAccount("1101", "Kas");

        mvc.perform(patch(BASE + "/" + id + "/deactivate")).andExpect(status().isNoContent());
        mvc.perform(patch(BASE + "/" + id + "/deactivate")).andExpect(status().isNoContent());

        mvc.perform(get(BASE).param("size", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].code").value(not(hasItem("1101"))))
                .andExpect(jsonPath("$.page.totalElements").value(5));

        mvc.perform(get(BASE + "/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));

        assertThat(jdbc.queryForObject("SELECT active FROM account WHERE id = ?", Boolean.class, id))
                .isFalse();
    }

    @Test
    @DisplayName("Daftar akun: pagination & sort; properti sort tidak dikenal -> 400")
    void listWithPaginationAndSort() throws Exception {
        mvc.perform(get(BASE).param("page", "0").param("size", "2").param("sort", "name,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].name").value("Aset"))
                .andExpect(jsonPath("$.content[1].name").value("Beban"))
                .andExpect(jsonPath("$.page.size").value(2))
                .andExpect(jsonPath("$.page.totalElements").value(5))
                .andExpect(jsonPath("$.page.totalPages").value(3));

        mvc.perform(get(BASE).param("sort", "foo"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Properti sort tidak dikenal: foo"));
    }

    private long createAccount(String code, String name) throws Exception {
        MvcResult result = mvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"%s","name":"%s","type":"ASSET","parentCode":"1000"}"""
                                .formatted(code, name)))
                .andExpect(status().isCreated())
                .andReturn();
        String location = result.getResponse().getHeader("Location");
        return Long.parseLong(location.substring(location.lastIndexOf('/') + 1));
    }
}

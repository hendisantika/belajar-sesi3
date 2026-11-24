package id.autochem.belajarsesi3.coa.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Created by IntelliJ IDEA.
 * Project : belajar-sesi3
 * User: hendisantika
 * Email: hendisantika@gmail.com
 * Telegram : @hendisantika34
 * Link : s.id/hendisantika
 * Date: 24/11/26
 * Time: 12.42
 */
public interface AccountRepository extends JpaRepository<Account, Long> {

    boolean existsByCode(String code);

    Optional<Account> findByCode(String code);

    /**
     * select a.id, a.code, a.name, a.type from account a where a.active order by ... offset ? fetch first ? rows only
     * + select count(a.id) from account a where a.active
     */
    Page<AccountSummary> findByActiveTrue(Pageable pageable);
}

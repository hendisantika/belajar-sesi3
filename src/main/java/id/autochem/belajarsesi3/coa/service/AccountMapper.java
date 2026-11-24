package id.autochem.belajarsesi3.coa.service;

import id.autochem.belajarsesi3.coa.api.AccountResponse;
import id.autochem.belajarsesi3.coa.domain.Account;
import org.springframework.stereotype.Component;

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
@Component
public class AccountMapper { // mapping eksplisit, tanpa magic

    public AccountResponse toResponse(Account a) {
        String parentCode = a.getParent() == null ? null : a.getParent().getCode();
        return new AccountResponse(a.getId(), a.getCode(), a.getName(),
                a.getType(), parentCode, a.isActive());
    }
}

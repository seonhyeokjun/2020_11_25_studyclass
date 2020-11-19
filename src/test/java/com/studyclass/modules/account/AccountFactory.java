package com.studyclass.modules.account;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountFactory {

    @Autowired AccountRepository accountRepository;

    public Account createAccount(String nickname) {
        Account seon = new Account();
        seon.setNickname(nickname);
        seon.setEmail(nickname + "@email.com");
        accountRepository.save(seon);
        return seon;
    }

}

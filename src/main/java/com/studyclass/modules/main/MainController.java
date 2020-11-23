package com.studyclass.modules.main;

import com.studyclass.modules.account.CurrentAccount;
import com.studyclass.modules.account.Account;
import com.studyclass.modules.notification.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final NotificationRepository notificationRepository;

    @GetMapping("/")
    public String home(@CurrentAccount Account account, Model model){
        if (account!=null){
            model.addAttribute(account);
        }

        return "index";
    }

    @GetMapping("/login")
    public String login(){
        return "login";
    }
}

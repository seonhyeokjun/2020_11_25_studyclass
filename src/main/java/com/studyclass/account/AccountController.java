package com.studyclass.account;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AccountController {

    @GetMapping(value = "/sign-up")
    public String signupForm(Model model){
        model.addAttribute(new SignUpForm());
        return "account/sign-up";
    }

}

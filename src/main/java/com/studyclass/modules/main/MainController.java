package com.studyclass.modules.main;

import com.studyclass.modules.account.AccountRepository;
import com.studyclass.modules.account.CurrentAccount;
import com.studyclass.modules.account.Account;
import com.studyclass.modules.event.EnrollmentRepository;
import com.studyclass.modules.notification.NotificationRepository;
import com.studyclass.modules.study.Study;
import com.studyclass.modules.study.StudyRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class MainController {

    private final StudyRepository studyRepository;

    private final EnrollmentRepository enrollmentRepository;

    private final AccountRepository accountRepository;

    @GetMapping("/")
    public String home(@CurrentAccount Account account, Model model){
        if (account!=null){
            Account accountLoaded = accountRepository.findAccountWithTagsAndZonesById(account.getId());
            model.addAttribute(accountLoaded);
            model.addAttribute("enrollmentList", enrollmentRepository.findByAccountAndAcceptedOrderByEnrolledAtDesc(accountLoaded, true));
            model.addAttribute("studyList", studyRepository.findByAccount(
                    accountLoaded.getTags(),
                    accountLoaded.getZones()));
            model.addAttribute("studyManagerOf",
                    studyRepository.findFirst5ByManagersContainingAndClosedOrderByPublishedDateTimeDesc(account, false));
            model.addAttribute("studyMemberOf",
                    studyRepository.findFirst5ByMembersContainingAndClosedOrderByPublishedDateTimeDesc(account, false));
            return "index-after-login";
        }
        model.addAttribute("studyList", studyRepository.findFirst9ByPublishedAndClosedOrderByPublishedDateTimeDesc(true, false));
        return "index";
    }

    @GetMapping("/login")
    public String login(){
        return "login";
    }

    @GetMapping("/search/study")
    public String searchStudy(@PageableDefault(size = 9, page = 0, sort = "publishedDateTime", direction = Sort.Direction.DESC) Pageable pageable, String keyword, Model model) {
        Page<Study> studyPage = studyRepository.findByKeyword(keyword, pageable);
        model.addAttribute("studyPage", studyPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("sortProperty",
                pageable.getSort().toString().contains("publishedDateTime") ? "publishedDateTime" : "memberCount");
        return "search";
    }
}

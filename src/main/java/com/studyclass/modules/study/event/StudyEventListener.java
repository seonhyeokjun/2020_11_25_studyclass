package com.studyclass.modules.study.event;

import com.studyclass.infra.config.AppProperties;
import com.studyclass.infra.mail.EmailMessage;
import com.studyclass.infra.mail.EmailService;
import com.studyclass.modules.account.Account;
import com.studyclass.modules.account.AccountPredicates;
import com.studyclass.modules.account.AccountRepository;
import com.studyclass.modules.notification.Notification;
import com.studyclass.modules.notification.NotificationRepository;
import com.studyclass.modules.notification.NotificationType;
import com.studyclass.modules.study.Study;
import com.studyclass.modules.study.StudyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;


@Slf4j
@Async
@Transactional
@Component
@RequiredArgsConstructor
public class StudyEventListener {

    private final StudyRepository studyRepository;

    private final AccountRepository accountRepository;

    private final EmailService emailService;

    private final TemplateEngine templateEngine;

    private final AppProperties appProperties;

    private final NotificationRepository notificationRepository;

    @EventListener
    public void handleStudyCreatedEvent(StudyCreatedEvent studyCreatedEvent){
        Study study = studyRepository.findStudyWithTagsAndZonesById(studyCreatedEvent.getStudy().getId());
        Iterable<Account> accounts = accountRepository.findAll(AccountPredicates.findByTagsAndZones(study.getTags(), study.getZones()));
        accounts.forEach(account -> {
            if (account.isStudyCreatedByEmail()){
                sendStudyCreatedEmail(study, account);
            }

            if (account.isStudyCreatedByWeb()){
                saveStudyCreatedNotification(study, account);
            }
        });
    }

    private void saveStudyCreatedNotification(Study study, Account account) {
        Notification notification = new Notification();
        notification.setTitle(study.getTitle());
        notification.setLink("/study/" + study.getEncodedPath());
        notification.setChecked(false);
        notification.setCreatedDateTime(LocalDateTime.now());
        notification.setMessage(study.getShortDescription());
        notification.setAccount(account);
        notification.setNotificationType(NotificationType.STUDY_CREATED);
        notificationRepository.save(notification);
    }

    private void sendStudyCreatedEmail(Study study, Account account) {
        Context context = new Context();
        context.setVariable("nickname", account.getNickname());
        context.setVariable("link", "/study/" + study.getEncodedPath());
        context.setVariable("linkName", study.getTitle());
        context.setVariable("message", "새로운 공부반이 생겼습니다.");
        context.setVariable("host", appProperties.getHost());
        String message = templateEngine.process("mail/simple-link", context);

        EmailMessage emailMessage = EmailMessage.builder()
                .subject("공부반, '" + study.getTitle() + "' 공부반이 생겼습니다.")
                .to(account.getEmail())
                .message(message)
                .build();

        emailService.sendEmail(emailMessage);
    }

}

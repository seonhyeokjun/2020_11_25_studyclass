package com.studyclass.settings;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyclass.account.AccountService;
import com.studyclass.account.CurrentAccount;
import com.studyclass.domain.Account;
import com.studyclass.domain.Tag;
import com.studyclass.domain.Zone;
import com.studyclass.settings.form.*;
import com.studyclass.settings.validator.NicknameValidator;
import com.studyclass.settings.validator.PasswordFormValidator;
import com.studyclass.study.StudyRepository;
import com.studyclass.tag.TagForm;
import com.studyclass.tag.TagRepository;
import com.studyclass.tag.TagService;
import com.studyclass.zone.ZoneForm;
import com.studyclass.zone.ZoneRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.studyclass.settings.SettingsController.ROOT;
import static com.studyclass.settings.SettingsController.SETTINGS;

@Controller
@RequestMapping(ROOT + SETTINGS)
@RequiredArgsConstructor
public class SettingsController {

    static final String ROOT = "/";
    static final String SETTINGS = "settings";
    static final String PROFILE = "/profile";
    static final String PASSWORD = "/password";
    static final String NOTIFICATIONS = "/notifications";
    static final String ACCOUNT = "/account";
    static final String TAGS = "/tags";
    static final String ZONES = "/zones";

    private final AccountService accountService;
    private final ModelMapper modelMapper;
    private final NicknameValidator nicknameValidator;
    private final TagRepository tagRepository;
    private final ObjectMapper objectMapper;
    private final ZoneRepository zoneRepository;
    private final TagService tagService;

    @InitBinder("passwordForm")
    public void passwordFormInitBinder(WebDataBinder webDataBinder){
        webDataBinder.addValidators(new PasswordFormValidator());
    }

    @InitBinder("nicknameForm")
    public void nicknameFormInitBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(nicknameValidator);
    }

    @GetMapping(PROFILE)
    public String updateProfileForm(@CurrentAccount Account account, Model model){
        model.addAttribute(account);
        model.addAttribute(modelMapper.map(account, Profile.class));

        return SETTINGS + PROFILE;
    }

    @PostMapping(PROFILE)
    public String updateProfile(@CurrentAccount Account account, @Valid Profile profile, Errors errors, Model model,
                                RedirectAttributes attributes){
        if (errors.hasErrors()){
            model.addAttribute(account);
            return SETTINGS + PROFILE;
        }

        accountService.updateProfile(account, profile);
        attributes.addFlashAttribute("message", "프로필을 수정했습니다.");
        return "redirect:/" + SETTINGS + PROFILE;
    }

    @GetMapping(PASSWORD)
    public String passwordUpdateForm(@CurrentAccount Account account, Model model){
        model.addAttribute(account);
        model.addAttribute(new PasswordForm());
        return SETTINGS + PASSWORD;
    }

    @PostMapping(PASSWORD)
    public String updatePassword(@CurrentAccount Account account, @Valid PasswordForm passwordForm, Errors errors,
                                 Model model, RedirectAttributes attributes){
        if (errors.hasErrors()){
            model.addAttribute(account);
            return SETTINGS + PASSWORD;
        }

        accountService.updatePassword(account, passwordForm.getNewPassword());
        attributes.addFlashAttribute("message", "비밀번호가 변경했습니다.");
        return "redirect:/" + SETTINGS + PASSWORD;
    }

    @GetMapping(NOTIFICATIONS)
    public String updateNotificationsForm(@CurrentAccount Account account, Model model){
        model.addAttribute(account);
        model.addAttribute(modelMapper.map(account, Notifications.class));
        return SETTINGS + NOTIFICATIONS;
    }

    @PostMapping(NOTIFICATIONS)
    public String updatedNotifications(@CurrentAccount Account account, @Valid Notifications notifications
    , Errors errors, Model model, RedirectAttributes attributes){
        if (errors.hasErrors()){
            model.addAttribute(account);
            return SETTINGS + NOTIFICATIONS;
        }

        accountService.updateNotifications(account, notifications);
        attributes.addFlashAttribute("message", "알림 설정을 변경했습니다.");
        return "redirect:/" + SETTINGS + NOTIFICATIONS;
    }

    @GetMapping(ACCOUNT)
    public String updateAccountForm(@CurrentAccount Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(modelMapper.map(account, NicknameForm.class));
        return SETTINGS + ACCOUNT;
    }

    @PostMapping(ACCOUNT)
    public String updateAccount(@CurrentAccount Account account, @Valid NicknameForm nicknameForm, Errors errors,
                                Model model, RedirectAttributes attributes) {
        if (errors.hasErrors()) {
            model.addAttribute(account);
            return SETTINGS + ACCOUNT;
        }

        accountService.updateNickname(account, nicknameForm.getNickname());
        attributes.addFlashAttribute("message", "닉네임을 수정했습니다.");
        return "redirect:/" + SETTINGS + ACCOUNT;
    }

    @GetMapping(TAGS)
    public String updateTags(@CurrentAccount Account account, Model model) throws JsonProcessingException {
        model.addAttribute(account);
        Set<Tag> tags = accountService.getTags(account);
        model.addAttribute("tags", tags.stream().map(Tag::getTitle).collect(Collectors.toList()));

        List<String> allTags = tagRepository.findAll().stream().map(Tag::getTitle).collect(Collectors.toList());
        model.addAttribute("whitelist", objectMapper.writeValueAsString(allTags));

        return SETTINGS + TAGS;
    }

    @PostMapping(TAGS + "/add")
    @ResponseBody
    public ResponseEntity addTag(@CurrentAccount Account account, @RequestBody TagForm tagForm){
        Tag tag = tagService.findOrCreateNew(tagForm.getTagTitle());
        accountService.addTag(account, tag);
        return ResponseEntity.ok().build();
    }

    @PostMapping(TAGS + "/remove")
    @ResponseBody
    public ResponseEntity removeTag(@CurrentAccount Account account, @RequestBody TagForm tagForm){
        String title = tagForm.getTagTitle();
        Tag tag = tagRepository.findByTitle(title);
        if (tag == null){
            return ResponseEntity.badRequest().build();
        }

        accountService.removeTag(account, tag);
        return ResponseEntity.ok().build();
    }

    @GetMapping(ZONES)
    public String updateZonesForm(@CurrentAccount Account account, Model model) throws JsonProcessingException {
        model.addAttribute(account);

        Set<Zone> zones = accountService.getZones(account);
        model.addAttribute("zones", zones.stream().map(Zone::toString).collect(Collectors.toList()));

        List<String> allZones = zoneRepository.findAll().stream().map(Zone::toString).collect(Collectors.toList());
        model.addAttribute("whitelist", objectMapper.writeValueAsString(allZones));

        return SETTINGS + ZONES;
    }

    @PostMapping(ZONES + "/add")
    @ResponseBody
    public ResponseEntity addZone(@CurrentAccount Account account, @RequestBody ZoneForm zoneForm) {
        Zone zone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvinceName());
        if (zone == null) {
            return ResponseEntity.badRequest().build();
        }

        accountService.addZone(account, zone);
        return ResponseEntity.ok().build();
    }

    @PostMapping(ZONES + "/remove")
    @ResponseBody
    public ResponseEntity removeZone(@CurrentAccount Account account, @RequestBody ZoneForm zoneForm) {
        Zone zone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvinceName());
        if (zone == null) {
            return ResponseEntity.badRequest().build();
        }

        accountService.removeZone(account, zone);
        return ResponseEntity.ok().build();
    }

}

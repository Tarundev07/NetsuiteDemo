package com.atomicnorth.hrm.tenant.service;

import com.atomicnorth.hrm.configuration.ApplicationProperties;
import com.atomicnorth.hrm.configuration.multitenant.TenantContextHolder;
import com.atomicnorth.hrm.tenant.domain.User;
import com.atomicnorth.hrm.tenant.domain.notificationTemplates.NotificationTemplates;
import com.atomicnorth.hrm.tenant.repository.UserRepository;
import com.atomicnorth.hrm.tenant.repository.notificationTemplates.NotificationTemplatesRepository;
import com.atomicnorth.hrm.tenant.service.dto.timeSheetDTO.CreateTimeSheetDTO;
import com.atomicnorth.hrm.tenant.service.mail.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Service
public class MailService {

    private static final String USER = "user";
    private static final String BASE_URL = "baseUrl";
    private final Logger log = LoggerFactory.getLogger(MailService.class);
    private final ApplicationProperties applicationProperties;

    private final JavaMailSender javaMailSender;

    private final MessageSource messageSource;

    private final SpringTemplateEngine templateEngine;
    private final NotificationTemplatesRepository notificationTemplatesRepository;

    private final UserRepository userRepository;

    private final EmailService emailService;

    public MailService(
            ApplicationProperties applicationProperties,
            JavaMailSender javaMailSender,
            MessageSource messageSource,
            SpringTemplateEngine templateEngine,
            NotificationTemplatesRepository notificationTemplatesRepository, UserRepository userRepository, EmailService emailService) {
        this.applicationProperties = applicationProperties;
        this.javaMailSender = javaMailSender;
        this.messageSource = messageSource;
        this.templateEngine = templateEngine;
        this.notificationTemplatesRepository = notificationTemplatesRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    @Async
    public void sendEmailFromTemplate(User user, String templateName, String titleKey) {
        if (user.getEmail() == null) {
            log.debug("Email doesn't exist for user '{}'", user.getEmail());
            return;
        }
        Locale locale = Locale.forLanguageTag("");
        Context context = new Context(locale);
        context.setVariable(USER, user);
        context.setVariable(BASE_URL, applicationProperties.getMail().getBaseUrl());
        emailService.sendMail(user.getEmail(), titleKey, templateName);

    }

    @Async
    public void sendPasswordResetMail(User user, String baseUrl) {
        log.debug("Sending password reset email to '{}'", user.getEmail());
        Optional<NotificationTemplates> templates = notificationTemplatesRepository.findByNotificationTypeAndCommunicationTypeAndTemplateType(
                "User", "EMAIL", "FORGOT_PASSWORD");
        if (templates.isEmpty()) {
            log.error("No email template found for FORGOT_PASSWORD");
            throw new IllegalArgumentException("Email template not found for FORGOT_PASSWORD");
        }
        NotificationTemplates notificationTemplates = templates.get();
        String personalizedEmailBody = notificationTemplates.getBody()
                .replace("[%DisplayName%]", user.getDisplayName())
                .replace("[%URL%]", generateResetUrl(user,baseUrl));
        sendEmailFromTemplate(user, personalizedEmailBody, notificationTemplates.getTemplateName());
    }

    public String generateResetUrl(User user,String baseUrl) {
        String resetToken = UUID.randomUUID().toString();
        user.setResetKey(resetToken);
        user.setResetDate(Instant.now());
        userRepository.save(user);
        return baseUrl + "/login/setNewPasswordLogin?token=" + resetToken + "&clientId=" + TenantContextHolder.getTenant();
    }

    public void sendTimesheetSubmitMail(CreateTimeSheetDTO loginBean) {
        Optional<User> userDetails = userRepository.findById(Long.valueOf(loginBean.getUsername()));
        User user = userDetails.get();
        Optional<NotificationTemplates> templates = notificationTemplatesRepository.findByNotificationTypeAndCommunicationTypeAndTemplateType(
                "Timesheet", "EMAIL", "TIMESHEET_SUBMITTED");
        if (templates.isEmpty()) {
            log.error("No email template found for TIMESHEET_SUBMITTED");
            throw new IllegalArgumentException("Email template not found for TIMESHEET_SUBMITTED");
        }
        NotificationTemplates notificationTemplates = templates.get();
        String personalizedEmailBody = notificationTemplates.getBody()
                .replace("[%DisplayName%]", user.getDisplayName());
        sendEmailFromTemplate(user, personalizedEmailBody, notificationTemplates.getTemplateName());

    }
}

package com.atomicnorth.hrm.util;

import com.atomicnorth.hrm.tenant.repository.attendance.AttendanceMoafRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.internet.MimeMessage;
import java.util.Date;
import java.util.Map;

@Service
public class EmailUtility {
    static final Logger logger = LoggerFactory.getLogger(EmailUtility.class);
    private static final long serialVersionUID = 1L;
    @Autowired
    CommonUtility commonUtility;
    @Autowired
    AttendanceMoafRepo attendanceMoafRepo;
    @Autowired
    private JavaMailSender mailSender;
    @Value("${ADMIN_MAIL}")
    private String ADMIN_MAIL;
    @Value("${domain}")
    private String domain;

    @Value("${TEST_DELIVERY_MAIL_TO}")
    private String TEST_DELIVERY_MAIL_TO;

    public synchronized boolean checkMailNotificationTrigger(String requestActionId) {
        try {
            return "TRUE".equalsIgnoreCase(attendanceMoafRepo.findTriggerFlagByEventId(requestActionId));
        } catch (Exception e) {
            logger.error("Error:checkMailNotificationTrigger||EvenId:" + requestActionId + "||Exception:" + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public void doSendTemplateEmail(String recipientAddress, String subject, Map<String, Object> model,
                                    String templateName) {
        final MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper email = new MimeMessageHelper(mimeMessage);
        try {
            email.setFrom(ADMIN_MAIL);
            /* Changed for v1.11 start*/
            if (domain.equalsIgnoreCase(commonUtility.getDomainName()))
                /* Changed for v1.11 end*/
                email.setTo(recipientAddress);
            else
                email.setTo(TEST_DELIVERY_MAIL_TO);
            email.setSubject(subject);
            String message = "VelocityEngineUtils will change later"; //VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, templateName, "UTF-8", model);
            email.setText(message, true);
            synchronized (this) {
                this.mailSender.send(mimeMessage);
                logger.info("Email sent to " + recipientAddress + "||Subject:" + subject + "||Template:" + templateName + "||Mail Body:" + model);
            }
        } catch (Exception e) {
            logger.error("Email send failed to " + recipientAddress + "||Subject:" + subject + "||Template:" + templateName + "||Error:" + e.getMessage());
            e.printStackTrace();
        }
    }

    public void doSendEmail(String[] to, String[] cc, String subject, String message) {
        final MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper email = new MimeMessageHelper(mimeMessage);
        try {
            email.setFrom(ADMIN_MAIL);
            /* Changed for v1.11 start*/
            if (domain.equalsIgnoreCase(commonUtility.getDomainName())) {
                /* Changed for v1.11 end*/
                email.setTo(to);
                email.setCc(cc);
            } else {
                email.setTo(TEST_DELIVERY_MAIL_TO);
            }
            email.setSubject(subject);
            email.setText(message, true);
            this.mailSender.send(mimeMessage);
            logger.info("Email sent to " + to + "||" + cc + "||Subject:" + subject + "||Message:" + message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void doSendTemplateEmail(String recipientAddress, String subject, Map<String, Object> model,
                                    String templateName, String fromUserFullname) {
        final MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper email = new MimeMessageHelper(mimeMessage);
        try {
            email.setFrom(fromUserFullname + " <timesheet@supraits.com>");
            /* Changed for v1.11 start*/
            if (domain.equalsIgnoreCase(commonUtility.getDomainName()))
                /* Changed for v1.11 end*/
                email.setTo(recipientAddress);
            else
                email.setTo(TEST_DELIVERY_MAIL_TO);
            email.setSubject(subject);
            String message = "VelocityEngineUtils will change later"; //VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, templateName, "UTF-8", model);
            email.setText(message, true);
            synchronized (this) {
                this.mailSender.send(mimeMessage);
                System.out.println("Email sent to " + recipientAddress + " on " + new Date() + " ||Subject:" + subject + "||Template:" + templateName + "||Mail Body:" + model);
                logger.info("Email sent to " + recipientAddress + "||Subject:" + subject + "||Template:" + templateName + "||Mail Body:" + model);
            }
        } catch (Exception e) {
            logger.error("Email send failed to " + recipientAddress + "||Subject:" + subject + "||Template:" + templateName + "||Error:" + e.getMessage());
            e.printStackTrace();
        }
    }
}

package com.atomicnorth.hrm.tenant.service.mail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.internet.MimeMessage;
import java.util.regex.Pattern;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public String sendMail(String to, String subject, String message) {
        try {
            if (to == null || to.trim().isEmpty()) {
                throw new IllegalArgumentException("Recipient email address (to) is missing.");
            }
            if (!isValidEmail(to)) {
                throw new IllegalArgumentException("Invalid email address: " + to);
            }
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setFrom("marketing@atomicnorth.com");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(message, true);
            mailSender.send(mimeMessage);
            System.out.println("Job Done! Email sent successfully to " + to);
            return "Email sent successfully to " + to;
        } catch (Exception e) {
            e.printStackTrace();
            return "Error while sending email: " + e.getMessage();
        }
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*$";
        return Pattern.compile(emailRegex).matcher(email).matches();
    }
}

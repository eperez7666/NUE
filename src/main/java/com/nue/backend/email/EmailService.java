package com.nue.backend.email;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    
    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // ✅ Email notification when requesting password recovery
    public void sendPasswordResetNotification(String to, String fullname) {
        MimeMessagePreparator messagePreparator = mimeMessage -> {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setTo(to);
            helper.setSubject("Password Reset Request");
            helper.setText(
                "<p>Hello " + fullname + ",</p>"
              + "<p>We received a request to reset your password.</p>"
              + "<p>If this was you, please open the app and continue the process.</p>"
              + "<p>If you didn't make this request, please contact our support team immediately.</p>"
              + "<p><strong>Note:</strong> This request will expire in 30 minutes.</p>"
              + "<p>Best regards,</p>"
              + "<p><strong>Support Team</strong></p>",
              true
            );
        };
        mailSender.send(messagePreparator);
    }


    // ✅ Confirmation email when the password is changed
    public void sendPasswordChangedEmail(String to, String fullname) {
        MimeMessagePreparator messagePreparator = mimeMessage -> {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setTo(to);
            helper.setSubject("Successful Password Change");
            helper.setText(
                "<p>Hello " + fullname + ",</p>"
              + "<p>Your password has been successfully changed.</p>"
              + "<p>If you did not make this change, please contact our support team immediately.</p>"
              + "<p>Best regards,</p>"
              + "<p><strong>Support Team</strong></p>",
              true
            );
        };

        mailSender.send(messagePreparator);
    }
}

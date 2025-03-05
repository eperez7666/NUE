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
    
    public void sendPasswordChangedEmail(String to) {
        MimeMessagePreparator messagePreparator = mimeMessage -> {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setTo(to);
            helper.setSubject("Cambio de contraseña exitoso");
            helper.setText("<p>Tu contraseña ha sido cambiada exitosamente.</p>"
                        + "<p>Si no realizaste este cambio, por favor contacta a soporte de inmediato.</p>", true);
        };

        mailSender.send(messagePreparator);
    }


    public void sendPasswordResetInstructions(String to, String resetLink) {
        MimeMessagePreparator messagePreparator = mimeMessage -> {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setTo(to);
            helper.setSubject("Restablecer tu contraseña");
            helper.setText("<p>Has solicitado restablecer tu contraseña.</p>"
                        + "<p>Haz clic en el siguiente enlace para proceder con la recuperación:</p>"
                        + "<p><a href='" + resetLink + "'>Restablecer contraseña</a></p>"
                        + "<p>Luego, ingresa el código de recuperación enviado en este correo.</p>"
                        + "<p>Este código expirará en 30 minutos.</p>", true);
        };

        mailSender.send(messagePreparator);
    }

}

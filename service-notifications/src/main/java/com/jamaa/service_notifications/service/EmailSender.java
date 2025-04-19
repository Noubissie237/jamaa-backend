package com.jamaa.service_notifications.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailSender {
    private static final Logger logger = LoggerFactory.getLogger(EmailSender.class);
    private static final String FROM_EMAIL = "supp0rt.jamaa@gmail.com";
    private static final String TEAM_NAME = "Jamaa Team";
    private static final String SUPPORT_EMAIL = "supp0rt.jamaa@gmail.com";
    private static final String LOGO_PATH = "/img/img.jpg";

    @Autowired
    private JavaMailSender mailSender;

    /**
     * Envoie un email avec une mise en forme professionnelle
     * @param toEmail L'adresse email du destinataire
     * @param subject Le sujet de l'email
     * @param body Le contenu principal de l'email
     * @throws MessagingException Si une erreur survient lors de l'envoi
     */
    public void sendMail(String toEmail, String subject, String body) throws MessagingException {
        logger.info("=== Début de l'envoi d'email ===");
        logger.info("Destinataire: {}", toEmail);
        logger.info("Sujet: {}", subject);
        
        try {
            MimeMessage message = createEmailMessage(toEmail, subject, body);
            mailSender.send(message);
            logger.info("Email envoyé avec succès à {}", toEmail);
        } catch (Exception e) {
            logger.error("Erreur lors de l'envoi de l'email: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Crée un message email avec mise en forme HTML
     */
    private MimeMessage createEmailMessage(String toEmail, String subject, String body) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        String htmlContent = buildHtmlContent(body);
        
        helper.setFrom(FROM_EMAIL);
        helper.setTo(toEmail);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);
        helper.addInline("logoImage", new ClassPathResource(LOGO_PATH));

        return message;
    }

    /**
     * Construit le contenu HTML de l'email avec la signature
     */
    private String buildHtmlContent(String body) {
        String signature = String.format(
            "<br><br><hr style='border: 1px solid #ddd;'><br>" +
            "<div style='color: #2e7d32; font-weight: bold; font-size: 14px;'>%s</div>" +
            "<div style='color: #f57c00; font-weight: bold; font-size: 13px;'>%s</div><br>" +
            "<img src='cid:logoImage' alt='%s' style='width: 45%%; height: 100px;'/>",
            TEAM_NAME, SUPPORT_EMAIL, TEAM_NAME
        );

        return String.format(
            "<div style='font-family: Arial, sans-serif; line-height: 1.6;'>%s%s</div>",
            body, signature
        );
    }
}

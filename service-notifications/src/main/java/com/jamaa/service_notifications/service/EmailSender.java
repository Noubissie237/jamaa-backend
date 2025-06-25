 package com.jamaa.service_notifications.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.beans.factory.annotation.Value;
    import org.springframework.core.io.ClassPathResource;
    import org.springframework.core.io.Resource;
    import org.springframework.core.io.ResourceLoader;
    import org.springframework.mail.javamail.JavaMailSender;
    import org.springframework.mail.javamail.MimeMessageHelper;
    import org.springframework.stereotype.Service;
    import org.springframework.util.StreamUtils;
    
    import jakarta.mail.MessagingException;
    import jakarta.mail.internet.MimeMessage;

    @Service
    public class EmailSender {
        private static final Logger logger = LoggerFactory.getLogger(EmailSender.class);
        
        private static final String TEMPLATES_BASE_PATH = "/templates/";
        private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        
        @Value("${email.sender.address:supp0rt.jamaa@gmail.com}")
        private String senderEmail;
        
        @Value("${email.sender.name:Jamaa Bank}")
        private String senderName;
        
        @Value("${email.logo.path:/img/img.jpg}")
        private String logoPath;
        
        @Value("${app.url:https://app.jamaa.com}")
        private String appBaseUrl;
        
        @Autowired
        private JavaMailSender mailSender;
        
        @Autowired
        private ResourceLoader resourceLoader;
        
        public enum NotificationType {
            DEPOSIT("deposit-notification", "Confirmation de dépôt"),
            WITHDRAWAL("withdrawal-notification", "Confirmation de retrait"),
            TRANSFER("transfer-notification", "Confirmation de transfert"),
            ACCOUNT("account-notification", "Information de compte"),
            AUTHENTICATION("auth-notification", "Notification de sécurité"),
            INSUFFICIENT_FUNDS("insufficient-funds-notification", "Alerte de solde"),
            PASSWORD_CHANGE("password-change-notification", "Modification de mot de passe"),
            CONFIRMATION_SOUSCRIPTION_BANQUE("successful-registration", "Compte créé avec success"),
            CARD_CREATE("card-create-notification", "Carte crée"),
            RECHARGE("recharge-confirmation", "Confirmation de transfert"),
            ACCOUNT_DELETION("delete-notification", "Compte supprimé"),
            ACCOUNT_CREATION_ERROR("error-registration", "Erreur lors de la création du compte");
            
            private final String templateName;
            private final String defaultSubject;
            
            NotificationType(String templateName, String defaultSubject) {
                this.templateName = templateName;
                this.defaultSubject = defaultSubject;
            }
            
            public String getTemplateName() {
                return templateName;
            }
            
            public String getDefaultSubject() {
                return defaultSubject;
            }
        }
        

        public void sendNotification(String to, NotificationType type, Map<String, Object> data) 
                throws MessagingException, IOException {
            finalSendNotification(to, type, null, data);
        }
        
        @SuppressWarnings("deprecation")
        public void finalSendNotification(String to, NotificationType type, String customSubject, Map<String, Object> data) 
                throws MessagingException, IOException {
            logger.info("Préparation de l'envoi d'une notification de type {}", type.name());
            
            // Ajout de données communes à tous les templates
            Map<String, Object> templateData = new HashMap<>(data);
            // templateData.put("date", DATE_FORMAT.format(new Date()));
            // templateData.put("appUrl", appBaseUrl);
            templateData.put("year", String.valueOf(new Date().getYear() + 1900));
            templateData.put("fullName", data.get("firstName") + " " + data.get("lastName"));
            templateData.put("firstName", data.get("firstName"));
            templateData.put("lastName", data.get("lastName"));
            templateData.put("email", data.get("email"));
            templateData.put("accountNumber", data.get("accountNumber"));
            templateData.put("registrationDate", data.get("registrationDate"));
            
            
            // Chargement et préparation du template
            String templateContent = loadAndProcessTemplate(type.getTemplateName(), templateData);
            
            // Envoi de l'email
            String subject = customSubject != null ? customSubject : type.getDefaultSubject();
            sendEmailWithTemplate(to, subject, templateContent);
            
            logger.info("Notification {} envoyée avec succès à {}", type.name(), to);
        }
        

        public void sendPasswordChangeNotification(String to, String deviceInfo, String location) 
                throws MessagingException, IOException {
            
            Map<String, Object> data = new HashMap<>();
            data.put("deviceInfo", deviceInfo);
            data.put("location", location);
            data.put("changeTime", DATE_FORMAT.format(new Date()));
            
            sendNotification(to, NotificationType.PASSWORD_CHANGE, data);
        }
        
        public void sendInsufficientFundsAlert(String to, String accountNumber, double balance, 
                double requiredAmount, String transactionType) throws MessagingException, IOException {
            
            Map<String, Object> data = new HashMap<>();
            data.put("accountNumber", accountNumber);
            data.put("balance", String.format("%.2f", balance));
            data.put("requiredAmount", String.format("%.2f", requiredAmount));
            data.put("transactionType", transactionType);
            
            sendNotification(to, NotificationType.INSUFFICIENT_FUNDS, data);
        }
        
        public String loadAndProcessTemplate(String templateName, Map<String, Object> data) throws IOException {
            String templatePath = TEMPLATES_BASE_PATH + templateName + ".html";
            Resource resource = resourceLoader.getResource("classpath:" + templatePath);
            
            if (!resource.exists()) {
                logger.error("Template {} introuvable", templatePath);
                throw new IOException("Template introuvable: " + templatePath);
            }
            
            String template;
            try (InputStream inputStream = resource.getInputStream()) {
                template = StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
            }
            
            // Remplacement des variables dans le template
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                String placeholder = "${" + entry.getKey() + "}";
                String value = entry.getValue() != null ? entry.getValue().toString() : "";
                template = template.replace(placeholder, value);
            }
            
            return template;
        }

        public void sendEmailWithTemplate(String to, String subject, String htmlContent) throws MessagingException {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
    
            try {
                helper.setFrom(senderEmail, senderName);
            } catch (Exception e) {
                logger.error("Erreur lors de la définition de l'expéditeur: {}", e.getMessage());
            }
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            
            // Ajout du logo
            try {
                helper.addInline("logoImage", new ClassPathResource(logoPath));
            } catch (Exception e) {
                logger.warn("Impossible de charger le logo: {}", e.getMessage());
            }
            
            mailSender.send(message);
        }
    }
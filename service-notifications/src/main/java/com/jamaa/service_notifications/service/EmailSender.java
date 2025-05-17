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
    
    /**
     * Service de gestion des emails avec templates HTML
     * Permet l'envoi d'emails professionnels avec une mise en forme cohérente
     */
    @Service
    public class EmailSender {
        private static final Logger logger = LoggerFactory.getLogger(EmailSender.class);
        
        private static final String TEMPLATES_BASE_PATH = "/template/";
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
        
        /**
         * Types de notifications supportés
         */
        public enum NotificationType {
            DEPOSIT("deposit-notification", "Confirmation de dépôt"),
            WITHDRAWAL("withdrawal-notification", "Confirmation de retrait"),
            TRANSFER("transfer-notification", "Confirmation de transfert"),
            ACCOUNT("account-notification", "Information de compte"),
            AUTHENTICATION("auth-notification", "Notification de sécurité"),
            INSUFFICIENT_FUNDS("insufficient-funds-notification", "Alerte de solde"),
            PASSWORD_CHANGE("password-change-notification", "Modification de mot de passe"),
            SUSPICIOUS_ACTIVITY("suspicious-activity-notification", "Alerte de sécurité");
            
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
        
        /**
         * Envoie un email avec un template prédéfini
         * 
         * @param to Adresse email du destinataire
         * @param type Type de notification
         * @param data Données à injecter dans le template
         * @throws MessagingException En cas d'erreur d'envoi
         * @throws IOException En cas d'erreur de lecture du template
         */
        public void sendNotification(String to, NotificationType type, Map<String, Object> data) 
                throws MessagingException, IOException {
            sendNotification(to, type, null, data);
        }
        
        /**
         * Envoie un email avec un template prédéfini et un sujet personnalisé
         * 
         * @param to Adresse email du destinataire
         * @param type Type de notification
         * @param customSubject Sujet personnalisé (null pour utiliser le sujet par défaut)
         * @param data Données à injecter dans le template
         * @throws MessagingException En cas d'erreur d'envoi
         * @throws IOException En cas d'erreur de lecture du template
         */
        public void sendNotification(String to, NotificationType type, String customSubject, Map<String, Object> data) 
                throws MessagingException, IOException {
            logger.info("Préparation de l'envoi d'une notification de type {}", type.name());
            
            // Ajout de données communes à tous les templates
            Map<String, Object> templateData = new HashMap<>(data);
            templateData.put("date", DATE_FORMAT.format(new Date()));
            templateData.put("appUrl", appBaseUrl);
            templateData.put("year", String.valueOf(new Date().getYear() + 1900));
            
            // Chargement et préparation du template
            String templateContent = loadAndProcessTemplate(type.getTemplateName(), templateData);
            
            // Envoi de l'email
            String subject = customSubject != null ? customSubject : type.getDefaultSubject();
            sendEmailWithTemplate(to, subject, templateContent);
            
            logger.info("Notification {} envoyée avec succès à {}", type.name(), to);
        }
        
        /**
         * Envoie un email d'alerte d'activité suspecte
         * Méthode spécialisée avec paramètres spécifiques
         */
        public void sendSuspiciousActivityAlert(String to, String activityType, String location, 
                String deviceInfo, Date activityTime) throws MessagingException, IOException {
            
            Map<String, Object> data = new HashMap<>();
            data.put("activityType", activityType);
            data.put("location", location);
            data.put("deviceInfo", deviceInfo);
            data.put("activityTime", DATE_FORMAT.format(activityTime));
            
            sendNotification(to, NotificationType.SUSPICIOUS_ACTIVITY, data);
        }
        
        /**
         * Envoie une notification de changement de mot de passe
         * Méthode spécialisée avec paramètres spécifiques
         */
        public void sendPasswordChangeNotification(String to, String deviceInfo, String location) 
                throws MessagingException, IOException {
            
            Map<String, Object> data = new HashMap<>();
            data.put("deviceInfo", deviceInfo);
            data.put("location", location);
            data.put("changeTime", DATE_FORMAT.format(new Date()));
            
            sendNotification(to, NotificationType.PASSWORD_CHANGE, data);
        }
        
        /**
         * Envoie une notification de solde insuffisant
         * Méthode spécialisée avec paramètres spécifiques
         */
        public void sendInsufficientFundsAlert(String to, String accountNumber, double balance, 
                double requiredAmount, String transactionType) throws MessagingException, IOException {
            
            Map<String, Object> data = new HashMap<>();
            data.put("accountNumber", accountNumber);
            data.put("balance", String.format("%.2f", balance));
            data.put("requiredAmount", String.format("%.2f", requiredAmount));
            data.put("transactionType", transactionType);
            
            sendNotification(to, NotificationType.INSUFFICIENT_FUNDS, data);
        }
        
        /**
         * Charge et traite un template en remplaçant les variables par leurs valeurs
         */
        private String loadAndProcessTemplate(String templateName, Map<String, Object> data) throws IOException {
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
        
        /**
         * Envoie un email avec le contenu HTML spécifié
         */
        private void sendEmailWithTemplate(String to, String subject, String htmlContent) throws MessagingException {
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
package com.jamaa.service_users.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

@Service
public class S3StorageService implements StorageService {

    private static final Logger logger = LoggerFactory.getLogger(S3StorageService.class);

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.s3.region}")
    private String region;

    private String accessKey = "AKIAQS7P6FWIA2BCUKPW";

    private String secretKey = "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";

    private S3Client s3Client;
    private S3Presigner s3Presigner;
    private boolean configLogged = false;

    private void logConfigurationOnce() {
        if (!configLogged) {
            logger.info("🔧 Configuration du service S3:");
            logger.info("   📦 Bucket: {}", bucketName);
            logger.info("   🌍 Region: {}", region);
            logger.info("   🔑 Access Key: {}...", accessKey != null && accessKey.length() > 10 ? accessKey.substring(0, 10) : "NON_DEFINIE");
            logger.info("   🔐 Secret Key: {}...", secretKey != null && secretKey.length() > 10 ? "****" : "NON_DEFINIE");
            
            if (accessKey == null || accessKey.isEmpty() || secretKey == null || secretKey.isEmpty()) {
                logger.error("❌ ERREUR: Les clés AWS ne sont pas définies!");
            } else {
                logger.info("✅ Configuration S3 validée");
            }
            configLogged = true;
        }
    }

    private S3Client getS3Client() {
        if (s3Client == null) {
            logConfigurationOnce();
            logger.info("🔌 Création du client S3...");
            try {
                AwsBasicCredentials awsCreds = AwsBasicCredentials.create(accessKey, secretKey);
                s3Client = S3Client.builder()
                        .region(Region.of(region))
                        .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                        .build();
                logger.info("✅ Client S3 créé avec succès pour la région: {}", region);
            } catch (Exception e) {
                logger.error("❌ Erreur lors de la création du client S3: {}", e.getMessage(), e);
                throw new RuntimeException("Erreur lors de l'initialisation du client S3", e);
            }
        }
        return s3Client;
    }

    private S3Presigner getS3Presigner() {
        if (s3Presigner == null) {
            logConfigurationOnce();
            logger.info("🔌 Création du presigner S3...");
            try {
                AwsBasicCredentials awsCreds = AwsBasicCredentials.create(accessKey, secretKey);
                s3Presigner = S3Presigner.builder()
                        .region(Region.of(region))
                        .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                        .build();
                logger.info("✅ Presigner S3 créé avec succès pour la région: {}", region);
            } catch (Exception e) {
                logger.error("❌ Erreur lors de la création du presigner S3: {}", e.getMessage(), e);
                throw new RuntimeException("Erreur lors de l'initialisation du presigner S3", e);
            }
        }
        return s3Presigner;
    }

    @Override
    public String save(MultipartFile file) throws IOException {
        logger.info("📤 Début de l'upload vers S3...");
        
        // Validation du fichier
        if (file == null || file.isEmpty()) {
            logger.error("❌ Le fichier est null ou vide");
            throw new IOException("Le fichier est null ou vide");
        }

        logger.info("📋 Informations du fichier:");
        logger.info("   📄 Nom original: {}", file.getOriginalFilename());
        logger.info("   📏 Taille: {} bytes", file.getSize());
        logger.info("   🎭 Type MIME: {}", file.getContentType());

        // Générer un nom de fichier unique
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String key = "cni/" + UUID.randomUUID() + extension;
        logger.info("🔑 Clé S3 générée: {}", key);

        try {
            logger.info("🏗️ Construction de la requête PutObject...");
            
            // Upload vers S3
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            logger.info("📡 Envoi du fichier vers S3...");
            logger.info("   📦 Bucket: {}", bucketName);
            logger.info("   🔑 Key: {}", key);
            logger.info("   🎭 Content-Type: {}", file.getContentType());

            long startTime = System.currentTimeMillis();
            
            getS3Client().putObject(putObjectRequest, 
                RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            long uploadTime = System.currentTimeMillis() - startTime;
            logger.info("✅ Upload réussi en {} ms", uploadTime);

            // Générer l'URL
            String url = String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, key);
            logger.info("🔗 URL générée: {}", url);
            
            return url;
            
        } catch (Exception e) {
            logger.error("❌ Erreur lors de l'upload vers S3:");
            logger.error("   🔍 Message: {}", e.getMessage());
            logger.error("   📋 Type: {}", e.getClass().getSimpleName());
            if (logger.isDebugEnabled()) {
                logger.debug("   📊 Stack trace complète:", e);
            }
            
            throw new IOException("Erreur lors de l'upload vers S3: " + e.getMessage(), e);
        }
    }


    public String generatePresignedUrl(String s3Url, int expirationHours) {
        if (s3Url == null || s3Url.isEmpty()) {
            logger.warn("⚠️ URL S3 vide fournie pour la génération d'URL pré-signée");
            return null;
        }

        try {
            // Extraire la clé du fichier depuis l'URL S3
            String key = extractKeyFromS3Url(s3Url);
            if (key == null) {
                logger.error("❌ Impossible d'extraire la clé de l'URL S3: {}", s3Url);
                return null;
            }

            logger.info("🔗 Génération d'URL pré-signée:");
            logger.info("   📦 Bucket: {}", bucketName);
            logger.info("   🔑 Key: {}", key);
            logger.info("   ⏰ Expiration: {} heures", expirationHours);

            // Créer la requête de pré-signature
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofHours(expirationHours))
                    .getObjectRequest(getObjectRequest)
                    .build();

            // Générer l'URL pré-signée
            PresignedGetObjectRequest presignedRequest = getS3Presigner().presignGetObject(presignRequest);
            String presignedUrl = presignedRequest.url().toString();

            logger.info("✅ URL pré-signée générée avec succès");
            logger.debug("🔗 URL: {}", presignedUrl);

            return presignedUrl;

        } catch (Exception e) {
            logger.error("❌ Erreur lors de la génération de l'URL pré-signée:");
            logger.error("   🔍 Message: {}", e.getMessage());
            logger.error("   📋 Type: {}", e.getClass().getSimpleName());
            if (logger.isDebugEnabled()) {
                logger.debug("   📊 Stack trace complète:", e);
            }
            return null;
        }
    }
    public String generatePresignedUrl(String s3Url) {
        return generatePresignedUrl(s3Url, 170);
    }

    private String extractKeyFromS3Url(String s3Url) {
        try {
            if (s3Url == null || s3Url.isEmpty()) {
                return null;
            }

            // Format attendu: https://bucket.s3.region.amazonaws.com/key
            if (s3Url.contains(bucketName + ".s3." + region + ".amazonaws.com/")) {
                int keyStartIndex = s3Url.indexOf(bucketName + ".s3." + region + ".amazonaws.com/") 
                                    + (bucketName + ".s3." + region + ".amazonaws.com/").length();
                return s3Url.substring(keyStartIndex);
            }

            // Format alternatif: https://s3.region.amazonaws.com/bucket/key
            if (s3Url.contains("s3." + region + ".amazonaws.com/" + bucketName + "/")) {
                int keyStartIndex = s3Url.indexOf("s3." + region + ".amazonaws.com/" + bucketName + "/") 
                                    + ("s3." + region + ".amazonaws.com/" + bucketName + "/").length();
                return s3Url.substring(keyStartIndex);
            }

            logger.warn("⚠️ Format d'URL S3 non reconnu: {}", s3Url);
            return null;

        } catch (Exception e) {
            logger.error("❌ Erreur lors de l'extraction de la clé depuis l'URL: {}", s3Url, e);
            return null;
        }
    }

    public boolean fileExists(String s3Url) {
        String key = extractKeyFromS3Url(s3Url);
        if (key == null) return false;

        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            getS3Client().getObject(getObjectRequest);
            return true;
        } catch (Exception e) {
            logger.debug("Fichier non trouvé: {}", s3Url);
            return false;
        }
    }
}
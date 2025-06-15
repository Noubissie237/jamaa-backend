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

import java.io.IOException;
import java.util.UUID;

@Service
public class S3StorageService implements StorageService {

    private static final Logger logger = LoggerFactory.getLogger(S3StorageService.class);

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.s3.region}")
    private String region;

    @Value("${aws.s3.access-key}")
    private String accessKey;

    @Value("${aws.s3.secret-key}")
    private String secretKey;

    private S3Client s3Client;
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
}
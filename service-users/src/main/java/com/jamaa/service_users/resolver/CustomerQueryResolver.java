package com.jamaa.service_users.resolver;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import com.jamaa.service_users.model.Customer;
import com.jamaa.service_users.model.SuperAdmin;
import com.jamaa.service_users.service.CustomerService;
import com.jamaa.service_users.service.S3StorageService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class CustomerQueryResolver {
    
    private static final Logger logger = LoggerFactory.getLogger(CustomerQueryResolver.class);
    
    private final CustomerService customerService;
    private final S3StorageService s3StorageService;

    @QueryMapping
    public List<Customer> getAllCustomers() {
        logger.info("🔍 Récupération de tous les clients...");
        List<Customer> customers = customerService.getAllCustomers();
        
        // Générer des URLs pré-signées pour chaque client
        customers.forEach(this::generatePresignedUrlsForCustomer);
        
        logger.info("✅ {} clients récupérés avec URLs pré-signées", customers.size());
        return customers;
    }

    @QueryMapping
    public Customer getCustomerById(@Argument Long id) {
        logger.info("🔍 Récupération du client avec ID: {}", id);
        Customer customer = customerService.getCustomerById(id);
        
        if (customer != null) {
            // Générer des URLs pré-signées pour ce client
            generatePresignedUrlsForCustomer(customer);
            logger.info("✅ Client trouvé et URLs pré-signées générées: {}", id);
        } else {
            logger.warn("⚠️ Client non trouvé avec ID: {}", id);
        }
        
        return customer;
    }

    @QueryMapping
    public Customer getCustomerByEmail(@Argument String email) {
        logger.info("🔍 Récupération du client par email: {}", email);
        Customer customer = customerService.getCustomerByEmail(email);
        
        if (customer != null) {
            generatePresignedUrlsForCustomer(customer);
            logger.info("✅ Client trouvé par email avec URLs pré-signées");
        } else {
            logger.warn("⚠️ Client non trouvé avec email: {}", email);
        }
        
        return customer;
    }

    @QueryMapping
    public Customer getCustomerByPhone(@Argument String phone) {
        logger.info("🔍 Récupération du client par téléphone: {}", phone);
        Customer customer = customerService.getCustomerByPhone(phone);
        
        if (customer != null) {
            generatePresignedUrlsForCustomer(customer);
            logger.info("✅ Client trouvé par téléphone avec URLs pré-signées");
        } else {
            logger.warn("⚠️ Client non trouvé avec téléphone: {}", phone);
        }
        
        return customer;
    }

    @QueryMapping
    public List<SuperAdmin> getAllSuperAdmins() {
        logger.info("🔍 Récupération de tous les super admins...");
        List<SuperAdmin> superAdmins = customerService.getAllSuperAdmins();
        logger.info("✅ {} super admins récupérés", superAdmins.size());
        return superAdmins;
    }

    @QueryMapping
    public SuperAdmin getSuperAdminById(@Argument Long id) {
        logger.info("🔍 Récupération du super admin avec ID: {}", id);
        SuperAdmin superAdmin = customerService.getSuperAdminById(id);
        
        if (superAdmin != null) {
            logger.info("✅ Super admin trouvé: {}", id);
        } else {
            logger.warn("⚠️ Super admin non trouvé avec ID: {}", id);
        }
        
        return superAdmin;
    }

    @QueryMapping
    public SuperAdmin getSuperAdminByUsername(@Argument String username) {
        logger.info("🔍 Récupération du super admin par username: {}", username);
        SuperAdmin superAdmin = customerService.getSuperAdminByUsername(username);
        
        if (superAdmin != null) {
            logger.info("✅ Super admin trouvé par username");
        } else {
            logger.warn("⚠️ Super admin non trouvé avec username: {}", username);
        }
        
        return superAdmin;
    }

    private void generatePresignedUrlsForCustomer(Customer customer) {
        if (customer == null) {
            return;
        }

        try {
            logger.debug("🔗 Génération des URLs pré-signées pour le client: {}", customer.getId());

            // Générer URL pré-signée pour CNI Recto
            if (customer.getCniRecto() != null && !customer.getCniRecto().trim().isEmpty()) {
                String presignedRectoUrl = s3StorageService.generatePresignedUrl(customer.getCniRecto(), 2); // 2 heures de validité
                if (presignedRectoUrl != null) {
                    customer.setCniRecto(presignedRectoUrl);
                    logger.debug("✅ URL pré-signée générée pour CNI Recto du client {}", customer.getId());
                } else {
                    logger.warn("⚠️ Échec de génération d'URL pré-signée pour CNI Recto du client {}", customer.getId());
                }
            }

            // Générer URL pré-signée pour CNI Verso
            if (customer.getCniVerso() != null && !customer.getCniVerso().trim().isEmpty()) {
                String presignedVersoUrl = s3StorageService.generatePresignedUrl(customer.getCniVerso(), 2); // 2 heures de validité
                if (presignedVersoUrl != null) {
                    customer.setCniVerso(presignedVersoUrl);
                    logger.debug("✅ URL pré-signée générée pour CNI Verso du client {}", customer.getId());
                } else {
                    logger.warn("⚠️ Échec de génération d'URL pré-signée pour CNI Verso du client {}", customer.getId());
                }
            }

            // Statistiques de debug
            boolean hasRecto = customer.getCniRecto() != null && !customer.getCniRecto().trim().isEmpty();
            boolean hasVerso = customer.getCniVerso() != null && !customer.getCniVerso().trim().isEmpty();
            logger.debug("📊 Client {} - Documents: Recto={}, Verso={}", customer.getId(), hasRecto, hasVerso);

        } catch (Exception e) {
            logger.error("❌ Erreur lors de la génération des URLs pré-signées pour le client {}: {}", 
                        customer.getId(), e.getMessage(), e);
        }
    }
}
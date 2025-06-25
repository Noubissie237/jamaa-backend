package com.jamaa_bank.service_recharge_retrait.utils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import com.jamaa_bank.service_recharge_retrait.dto.BankAccountDTO;
import com.jamaa_bank.service_recharge_retrait.dto.BankDTO;
import com.jamaa_bank.service_recharge_retrait.exception.AccountNotFoundException;
import com.jamaa_bank.service_recharge_retrait.exception.AccountServiceException;

import okhttp3.*;

@Component
public class BankUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(BankUtil.class);
    
    // Configuration pour le service bank-account
    @Value("${service.bank-account.endpoint:http://service-banks-account:8079/service-banks/graphql}")
    private String bankAccountServiceEndpoint;
    
    // Configuration pour le service bank
    @Value("${service.bank.endpoint:http://service-banks:8079/service-banks/graphql}")
    private String bankServiceEndpoint;
    
    @Value("${service.bank.timeout.connect:30}")
    private int connectTimeout;
    
    @Value("${service.bank.timeout.read:60}")
    private int readTimeout;
    
    @Value("${service.bank.timeout.write:60}")
    private int writeTimeout;
    
    private OkHttpClient client;
    
    // Lazy initialization pour éviter les problèmes avec les @Value
    private OkHttpClient getClient() {
        if (client == null) {
            client = new OkHttpClient.Builder()
                .connectTimeout(connectTimeout, TimeUnit.SECONDS)
                .readTimeout(readTimeout, TimeUnit.SECONDS)
                .writeTimeout(writeTimeout, TimeUnit.SECONDS)
                .build();
        }
        return client;
    }

    // ==================== MÉTHODES POUR BANK-ACCOUNT SERVICE ====================

    @Retryable(
        value = {IOException.class}, 
        maxAttempts = 3, 
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public BankAccountDTO getBankAccount(Long bankId) throws AccountServiceException, AccountNotFoundException {
        logger.debug("Récupération du compte bancaire pour bankId: {}", bankId);
        String query = String.format("""
            {
                getBankAccountByBankId(bankId: %s) {
                    id
                    bankId
                    totalBalance
                }
            }
        """, bankId);

        JSONObject response = executeGraphQLQuery(query, bankAccountServiceEndpoint);
        return parseBankAccountFromResponse(response, "getBankAccountByBankId");
    }

    @Retryable(
        value = {IOException.class}, 
        maxAttempts = 3, 
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public BankAccountDTO incrementTotalBalance(Long bankAccountId, BigDecimal amount) throws AccountServiceException, AccountNotFoundException {
        logger.debug("Incrémentation du solde du compte bancaire ID: {} de {}", bankAccountId, amount);
        String query = String.format("""
            mutation {
                incrementTotalBalance(id: %s, amount: %s) {
                    id
                    bankId
                    totalBalance
                }
            }
        """, bankAccountId, amount);
        
        JSONObject response = executeGraphQLQuery(query, bankAccountServiceEndpoint);
        return parseBankAccountFromResponse(response, "incrementTotalBalance");
    }

    // ==================== MÉTHODES POUR BANK SERVICE ====================

    @Retryable(
        value = {IOException.class}, 
        maxAttempts = 3, 
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public BankDTO getBank(Long bankId) throws AccountServiceException, AccountNotFoundException {
        logger.debug("Récupération de la banque ID: {}", bankId);
        String query = String.format("""
            {
                bank(id: %s) {
                    id
                    name
                    slogan
                    logoUrl
                    minimumBalance
                    withdrawFees
                    internalTransferFees
                    externalTransferFees
                    isActive
                    createdAt
                    updatedAt
                }
            }
        """, bankId);

        JSONObject response = executeGraphQLQuery(query, bankServiceEndpoint);
        return parseBankFromResponse(response, "bank");
    }

    // ==================== MÉTHODES PRIVÉES ====================

    private JSONObject executeGraphQLQuery(String query, String endpoint) throws AccountServiceException {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("query", query);

            RequestBody body = RequestBody.create(
                requestBody.toString(),
                MediaType.get("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                .url(endpoint)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();

            logger.debug("Envoi de la requête GraphQL vers: {}", endpoint);
            try (Response response = getClient().newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    logger.error("Erreur HTTP lors de l'appel au service: {} - Code: {}", endpoint, response.code());
                    throw new AccountServiceException("Erreur lors de l'appel au service: " + response.code());
                }

                String responseBody = response.body().string();
                logger.debug("Réponse reçue du service: {}", responseBody);
                return new JSONObject(responseBody);
            }
        } catch (IOException e) {
            logger.error("Erreur de communication avec le service: {}", endpoint, e);
            throw new AccountServiceException("Erreur de communication avec le service", e);
        } catch (Exception e) {
            logger.error("Erreur inattendue lors de l'appel au service: {}", endpoint, e);
            throw new AccountServiceException("Erreur inattendue lors de l'appel au service", e);
        }
    }

    private BankAccountDTO parseBankAccountFromResponse(JSONObject response, String operationName) throws AccountServiceException, AccountNotFoundException {
        try {
            if (response.has("errors")) {
                logger.error("Erreurs GraphQL reçues: {}", response.getJSONArray("errors"));
                throw new AccountServiceException("Erreur GraphQL: " + response.getJSONArray("errors"));
            }

            JSONObject data = response.getJSONObject("data");
            if (data.isNull(operationName)) {
                logger.warn("Compte bancaire non trouvé pour l'opération: {}", operationName);
                throw new AccountNotFoundException("Compte bancaire non trouvé");
            }

            JSONObject bankAccountData = data.getJSONObject(operationName);
            BankAccountDTO bankAccountDTO = new BankAccountDTO();
            bankAccountDTO.setId(bankAccountData.getLong("id"));
            bankAccountDTO.setBankId(bankAccountData.getLong("bankId"));
            
            // Gestion sécurisée de totalBalance
            if (bankAccountData.has("totalBalance") && !bankAccountData.isNull("totalBalance")) {
                bankAccountDTO.setTotalBalance(BigDecimal.valueOf(bankAccountData.getDouble("totalBalance")));
            }

            logger.debug("Compte bancaire parsé avec succès: ID={}, Balance={}, BankId={}", 
                        bankAccountDTO.getId(), bankAccountDTO.getTotalBalance(), bankAccountDTO.getBankId());
            return bankAccountDTO;
        } catch (Exception e) {
            logger.error("Erreur lors du parsing de la réponse BankAccount", e);
            throw new AccountServiceException("Erreur lors du parsing de la réponse", e);
        }
    }

    private BankDTO parseBankFromResponse(JSONObject response, String operationName) throws AccountServiceException, AccountNotFoundException {
        try {
            if (response.has("errors")) {
                logger.error("Erreurs GraphQL reçues: {}", response.getJSONArray("errors"));
                throw new AccountServiceException("Erreur GraphQL: " + response.getJSONArray("errors"));
            }

            JSONObject data = response.getJSONObject("data");
            if (data.isNull(operationName)) {
                logger.warn("Banque non trouvée pour l'opération: {}", operationName);
                throw new AccountNotFoundException("Banque non trouvée");
            }

            JSONObject bankData = data.getJSONObject(operationName);
            BankDTO bankDTO = new BankDTO();
            bankDTO.setId(bankData.getLong("id"));
            bankDTO.setName(bankData.getString("name"));
            
            // Gestion des champs optionnels
            if (bankData.has("slogan") && !bankData.isNull("slogan")) {
                bankDTO.setSlogan(bankData.getString("slogan"));
            }
            
            if (bankData.has("logoUrl") && !bankData.isNull("logoUrl")) {
                bankDTO.setLogoUrl(bankData.getString("logoUrl"));
            }
            
            if (bankData.has("createdAt") && !bankData.isNull("createdAt")) {
                bankDTO.setCreatedAt(bankData.getString("createdAt"));
            }
            
            if (bankData.has("updatedAt") && !bankData.isNull("updatedAt")) {
                bankDTO.setUpdatedAt(bankData.getString("updatedAt"));
            }

            // Gestion des BigDecimal
            if (bankData.has("minimumBalance") && !bankData.isNull("minimumBalance")) {
                bankDTO.setMinimumBalance(BigDecimal.valueOf(bankData.getDouble("minimumBalance")));
            }
            
            if (bankData.has("withdrawFees") && !bankData.isNull("withdrawFees")) {
                bankDTO.setWithdrawFees(BigDecimal.valueOf(bankData.getDouble("withdrawFees")));
            }
            
            if (bankData.has("internalTransferFees") && !bankData.isNull("internalTransferFees")) {
                bankDTO.setInternalTransferFees(BigDecimal.valueOf(bankData.getDouble("internalTransferFees")));
            }
            
            if (bankData.has("externalTransferFees") && !bankData.isNull("externalTransferFees")) {
                bankDTO.setExternalTransferFees(BigDecimal.valueOf(bankData.getDouble("externalTransferFees")));
            }

            // Gestion du boolean
            if (bankData.has("isActive")) {
                bankDTO.setIsActive(bankData.getBoolean("isActive"));
            }

            logger.debug("Banque parsée avec succès: ID={}, Name={}, IsActive={}", 
                        bankDTO.getId(), bankDTO.getName(), bankDTO.getIsActive());
            return bankDTO;
        } catch (Exception e) {
            logger.error("Erreur lors du parsing de la réponse Bank", e);
            throw new AccountServiceException("Erreur lors du parsing de la réponse Bank", e);
        }
    }
}
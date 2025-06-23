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

import com.jamaa_bank.service_recharge_retrait.dto.AccountDTO;
import com.jamaa_bank.service_recharge_retrait.exception.AccountNotFoundException;
import com.jamaa_bank.service_recharge_retrait.exception.AccountServiceException;

import okhttp3.*;

@Component
public class AccountUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(AccountUtil.class);
    
    @Value("${service.account.endpoint:http://localhost:8079/SERVICE-ACCOUNT/graphql}")
    private String accountServiceEndpoint;
    
    @Value("${service.account.timeout.connect:30}")
    private int connectTimeout;
    
    @Value("${service.account.timeout.read:60}")
    private int readTimeout;
    
    @Value("${service.account.timeout.write:60}")
    private int writeTimeout;
    
    private OkHttpClient client;
    
    public AccountUtil() {
        this.client = new OkHttpClient.Builder()
            .connectTimeout(connectTimeout, TimeUnit.SECONDS)
            .readTimeout(readTimeout, TimeUnit.SECONDS)
            .writeTimeout(writeTimeout, TimeUnit.SECONDS)
            .build();
    }

    @Retryable(
        value = {IOException.class}, 
        maxAttempts = 3, 
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public AccountDTO getAccount(Long accountId) throws AccountServiceException, AccountNotFoundException {
        logger.debug("Récupération du compte ID: {}", accountId);
        String query = String.format("""
            {
                getAccount(id: %s) {
                    id,
                    balance
                }
            }
        """, accountId);
        
        JSONObject response = executeGraphQLQuery(query);
        return parseAccountFromResponse(response, "getAccount");
    }

    @Retryable(
        value = {IOException.class}, 
        maxAttempts = 3, 
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public AccountDTO incrementBalance(Long accountId, BigDecimal amount) throws AccountServiceException, AccountNotFoundException {
        logger.debug("Incrémentation du solde du compte ID: {} de {}", accountId, amount);
        String query = String.format("""
            mutation {
                incrementBalance(accountId: %s, amount: %s) {
                    id,
                    balance
                }
            }
        """, accountId, amount);
        
        JSONObject response = executeGraphQLQuery(query);
        return parseAccountFromResponse(response, "incrementBalance");
    }

    @Retryable(
        value = {IOException.class}, 
        maxAttempts = 3, 
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public AccountDTO decrementBalance(Long accountId, BigDecimal amount) throws AccountServiceException, AccountNotFoundException {
        logger.debug("Décrémentation du solde du compte ID: {} de {}", accountId, amount);
        String query = String.format("""
            mutation {
                decrementBalance(accountId: %s, amount: %s) {
                    id,
                    balance
                }
            }
        """, accountId, amount);
        
        JSONObject response = executeGraphQLQuery(query);
        return parseAccountFromResponse(response, "decrementBalance");
    }
    
    private JSONObject executeGraphQLQuery(String query) throws AccountServiceException {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("query", query);

            RequestBody body = RequestBody.create(
                requestBody.toString(),
                MediaType.get("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                .url(accountServiceEndpoint)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();

            logger.debug("Envoi de la requête GraphQL vers: {}", accountServiceEndpoint);
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    logger.error("Erreur HTTP lors de l'appel au service Account: {}", response.code());
                    throw new AccountServiceException("Erreur lors de l'appel au service Account: " + response.code());
                }

                String responseBody = response.body().string();
                logger.debug("Réponse reçue du service Account: {}", responseBody);
                return new JSONObject(responseBody);
            }
        } catch (IOException e) {
            logger.error("Erreur de communication avec le service Account", e);
            throw new AccountServiceException("Erreur de communication avec le service Account", e);
        } catch (Exception e) {
            logger.error("Erreur inattendue lors de l'appel au service Account", e);
            throw new AccountServiceException("Erreur inattendue lors de l'appel au service Account", e);
        }
    }

    private AccountDTO parseAccountFromResponse(JSONObject response, String operationName) throws AccountServiceException, AccountNotFoundException {
        try {
            if (response.has("errors")) {
                logger.error("Erreurs GraphQL reçues: {}", response.getJSONArray("errors"));
                throw new AccountServiceException("Erreur GraphQL: " + response.getJSONArray("errors"));
            }

            JSONObject data = response.getJSONObject("data");
            if (data.isNull(operationName)) {
                logger.warn("Compte non trouvé pour l'opération: {}", operationName);
                throw new AccountNotFoundException("Compte non trouvé");
            }

            JSONObject accountData = data.getJSONObject(operationName);
            AccountDTO account = new AccountDTO();
            account.setId(accountData.getLong("id"));
            account.setBalance(new BigDecimal(accountData.getString("balance")));

            logger.debug("Compte parsé avec succès: ID={}, Balance={}", account.getId(), account.getBalance());
            return account;
        } catch (Exception e) {
            logger.error("Erreur lors du parsing de la réponse Account", e);
            throw new AccountServiceException("Erreur lors du parsing de la réponse", e);
        }
    }
}

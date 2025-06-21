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

import com.jamaa_bank.service_recharge_retrait.dto.CardDTO;
import com.jamaa_bank.service_recharge_retrait.exception.AccountNotFoundException;
import com.jamaa_bank.service_recharge_retrait.exception.AccountServiceException;

import okhttp3.*;

@Component
public class CardUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(CardUtil.class);
    
    @Value("${service.card.endpoint:http://localhost:8079/SERVICE-CARD/graphql}")
    private String cardServiceEndpoint;
    
    @Value("${service.card.timeout.connect:30}")
    private int connectTimeout;
    
    @Value("${service.card.timeout.read:60}")
    private int readTimeout;
    
    @Value("${service.card.timeout.write:60}")
    private int writeTimeout;
    
    private OkHttpClient client;
    
    public CardUtil() {
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
    public CardDTO getCard(Long cardId) throws AccountServiceException, AccountNotFoundException {
        logger.debug("Récupération de la carte ID: {}", cardId);
        String query = String.format("""
            {
                card(id: %s) {
                    id,
                    currentBalance,
                    bankId,
                    bankName
                }
            }
        """, cardId);
        
        JSONObject response = executeGraphQLQuery(query);
        return parseCardFromResponse(response, "card");
    }

    @Retryable(
        value = {IOException.class}, 
        maxAttempts = 3, 
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public CardDTO incrementCardBalance(Long cardId, BigDecimal amount) throws AccountServiceException, AccountNotFoundException {
        logger.debug("Incrémentation du solde de la carte ID: {} de {}", cardId, amount);
        String query = String.format("""
            mutation {
                incrementBalance(id: %s, amount: %s) {
                    id,
                    currentBalance,
                    bankId,
                    bankName
                }
            }
        """, cardId, amount);
        
        JSONObject response = executeGraphQLQuery(query);
        return parseCardFromResponse(response, "incrementBalance");
    }

    @Retryable(
        value = {IOException.class}, 
        maxAttempts = 3, 
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public CardDTO decrementCardBalance(Long cardId, BigDecimal amount) throws AccountServiceException, AccountNotFoundException {
        logger.debug("Décrémentation du solde de la carte ID: {} de {}", cardId, amount);
        String query = String.format("""
            mutation {
                decrementBalance(id: %s, amount: %s) {
                    id,
                    currentBalance,
                    bankId,
                    bankName
                }
            }
        """, cardId, amount);
        
        JSONObject response = executeGraphQLQuery(query);
        return parseCardFromResponse(response, "decrementBalance");
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
                .url(cardServiceEndpoint)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();

            logger.debug("Envoi de la requête GraphQL vers: {}", cardServiceEndpoint);
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    logger.error("Erreur HTTP lors de l'appel au service Card: {}", response.code());
                    throw new AccountServiceException("Erreur lors de l'appel au service Card: " + response.code());
                }

                String responseBody = response.body().string();
                logger.debug("Réponse reçue du service Card: {}", responseBody);
                return new JSONObject(responseBody);
            }
        } catch (IOException e) {
            logger.error("Erreur de communication avec le service Card", e);
            throw new AccountServiceException("Erreur de communication avec le service Card", e);
        } catch (Exception e) {
            logger.error("Erreur inattendue lors de l'appel au service Card", e);
            throw new AccountServiceException("Erreur inattendue lors de l'appel au service Card", e);
        }
    }

    private CardDTO parseCardFromResponse(JSONObject response, String operationName) throws AccountServiceException, AccountNotFoundException {
        try {
            if (response.has("errors")) {
                logger.error("Erreurs GraphQL reçues: {}", response.getJSONArray("errors"));
                throw new AccountServiceException("Erreur GraphQL: " + response.getJSONArray("errors"));
            }

            JSONObject data = response.getJSONObject("data");
            if (data.isNull(operationName)) {
                logger.warn("Carte non trouvée pour l'opération: {}", operationName);
                throw new AccountNotFoundException("Carte non trouvée");
            }

            JSONObject cardData = data.getJSONObject(operationName);
            CardDTO card = new CardDTO();
            card.setId(cardData.getLong("id"));
            card.setCurrentBalance(new BigDecimal(cardData.getFloat("currentBalance")));
            card.setBankId(cardData.getLong("bankId"));
            card.setBankName(cardData.getString("bankName"));

            logger.debug("Carte parsée avec succès: ID={}, Balance={}, BankId={}", 
                        card.getId(), card.getCurrentBalance(), card.getBankId());
            return card;
        } catch (Exception e) {
            logger.error("Erreur lors du parsing de la réponse Card", e);
            throw new AccountServiceException("Erreur lors du parsing de la réponse", e);
        }
    }
}

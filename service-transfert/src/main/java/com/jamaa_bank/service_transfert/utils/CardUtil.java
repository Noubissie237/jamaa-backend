package com.jamaa_bank.service_transfert.utils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import com.jamaa_bank.service_transfert.dto.AccountDTO;
import com.jamaa_bank.service_transfert.dto.CardDTO;
import com.jamaa_bank.service_transfert.exception.AccountNotFoundException;
import com.jamaa_bank.service_transfert.exception.AccountServiceException;

import okhttp3.*;

@Component
public class CardUtil {
    @Value("${service.account.endpoint:http://service-proxy:8079/service-card/graphql}")
    private String accountServiceEndpoint;
    
    @Value("${service.account.timeout.connect:5}")
    private int connectTimeout;
    
    @Value("${service.account.timeout.read:10}")
    private int readTimeout;
    
    @Value("${service.account.timeout.write:10}")
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
    public AccountDTO getAccount(Long id) throws AccountServiceException, AccountNotFoundException {
        String query = String.format("""
            {
                card(id: %s) {
                    currentBalance
                }
            }
        """, id);
        
        JSONObject response = executeGraphQLQuery(query);
        return parseAccountFromResponse(response, "getAccount");
    }

    @Retryable(
        value = {IOException.class}, 
        maxAttempts = 3, 
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public AccountDTO incrementBalance(Long id, BigDecimal amount) throws AccountServiceException, AccountNotFoundException {
        String query = String.format("""
            mutation {
                incrementBalance(id: %s, amount: %s) {
                    id,
                    currentBalance
                }
            }
        """, id, amount);
        
        JSONObject response = executeGraphQLQuery(query);
        return parseAccountFromResponse(response, "incrementBalance");
    }

    @Retryable(
        value = {IOException.class}, 
        maxAttempts = 3, 
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public AccountDTO decrementBalance(Long id, BigDecimal amount) throws AccountServiceException, AccountNotFoundException {
        String query = String.format("""
            mutation {
                decrementBalance(id: %s, amount: %s) {
                    id,
                    currentBalance
                }
            }
        """, id, amount);
        
        JSONObject response = executeGraphQLQuery(query);
        return parseAccountFromResponse(response, "decrementBalance");
    }
    
    @Retryable(
        value = {IOException.class}, 
        maxAttempts = 3, 
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public CardDTO getCardByBankId(Long bankId) throws AccountServiceException, AccountNotFoundException {
        String query = String.format("""
            {
                cardByBankId(bankId: %s) {
                    id,
                    currentBalance,
                    bankId,
                    bankName
                }
            }
        """, bankId);
        
        JSONObject response = executeGraphQLQuery(query);
        return parseCardFromResponse(response, "cardByBankId");
    }

    @Retryable(
        value = {IOException.class}, 
        maxAttempts = 3, 
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public CardDTO incrementCardBalance(Long cardId, BigDecimal amount) throws AccountServiceException, AccountNotFoundException {
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
        JSONObject jsonRequest = new JSONObject();
        jsonRequest.put("query", query);

        RequestBody body = RequestBody.create(
            jsonRequest.toString(),
            MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
            .url(accountServiceEndpoint)
            .post(body)
            .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new AccountServiceException(
                    "Erreur HTTP " + response.code() + " lors de la requête GraphQL: " + response.message()
                );
            }

            String responseBody = response.body().string();

            return new JSONObject(responseBody);
            
        } catch (IOException e) {
            throw new AccountServiceException("Erreur de communication avec le service de comptes", e);
        }
    }
    
    private AccountDTO parseAccountFromResponse(JSONObject response, String fieldName) 
            throws AccountNotFoundException, AccountServiceException {
        try {
            if (response.has("errors")) {
                throw new AccountServiceException("Erreur GraphQL: " + response.getJSONArray("errors").toString());
            }

            if (!response.has("data")) {
                throw new AccountServiceException("La réponse GraphQL ne contient pas de champ 'data': " + response.toString());
            }

            JSONObject data = response.getJSONObject("data");
            
            // Vérifier si des erreurs GraphQL sont présentes
            if (response.has("errors")) {
                throw new AccountServiceException("Erreur GraphQL: " + response.getJSONArray("errors").toString());
            }
            
            JSONObject account = data.optJSONObject(fieldName);
            
            // Si le compte est null, il n'existe pas
            if (account == null) {
                throw new AccountNotFoundException("Le compte demandé n'existe pas");
            }
            
            AccountDTO accountDTO = new AccountDTO();
            accountDTO.setId(account.getLong("id"));
            accountDTO.setBalance(account.getBigDecimal("balance"));
            
            return accountDTO;
            
        } catch (Exception e) {
            if (e instanceof AccountNotFoundException) {
                throw (AccountNotFoundException) e;
            }
            throw new AccountServiceException("Erreur lors du traitement de la réponse GraphQL", e);
        }
    }
    
    private CardDTO parseCardFromResponse(JSONObject response, String fieldName) 
            throws AccountNotFoundException, AccountServiceException {
        try {
            if (response.has("errors")) {
                throw new AccountServiceException("Erreur GraphQL: " + response.getJSONArray("errors").toString());
            }

            if (!response.has("data")) {
                throw new AccountServiceException("La réponse GraphQL ne contient pas de champ 'data': " + response.toString());
            }

            JSONObject data = response.getJSONObject("data");
            
            JSONObject card = data.optJSONObject(fieldName);
            
            // Si la carte est null, elle n'existe pas
            if (card == null) {
                throw new AccountNotFoundException("La carte demandée n'existe pas");
            }
            
            CardDTO cardDTO = new CardDTO();
            cardDTO.setId(card.getLong("id"));
            cardDTO.setCurrentBalance(card.getBigDecimal("currentBalance"));
            cardDTO.setBankId(card.getLong("bankId"));
            cardDTO.setBankName(card.getString("bankName"));
            
            return cardDTO;
            
        } catch (Exception e) {
            if (e instanceof AccountNotFoundException) {
                throw (AccountNotFoundException) e;
            }
            throw new AccountServiceException("Erreur lors du traitement de la réponse GraphQL", e);
        }
    }
}

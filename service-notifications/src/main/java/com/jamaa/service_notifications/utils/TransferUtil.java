package com.jamaa.service_notifications.utils;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import com.jamaa.service_notifications.dto.UserDTO;
import com.jamaa.service_notifications.exception.TransferServiceException;
import com.jamaa.service_notifications.exception.UserNotFoundException;

import okhttp3.*;

@Component
public class TransferUtil {
    
    @Value("${service.account.endpoint:http://service-proxy:8079/service-account/graphql}")
    private String accountServiceEndpoint;
    
    @Value("${service.users.endpoint:http://service-proxy:8079/service-users/graphql}")
    private String usersServiceEndpoint;
    
    @Value("${service.card.endpoint:http://service-proxy:8079/service-card/graphql}")
    private String cardServiceEndpoint;
    
    @Value("${service.timeout.connect:5}")
    private int connectTimeout;
    
    @Value("${service.timeout.read:10}")
    private int readTimeout;
    
    private final OkHttpClient httpClient;
    
    public TransferUtil() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(connectTimeout, TimeUnit.SECONDS)
                .readTimeout(readTimeout, TimeUnit.SECONDS)
                .build();
    }
    
    /**
     * Récupère l'userId depuis le service account
     */
    @Retryable(
        value = {IOException.class}, 
        maxAttempts = 3, 
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public Long getUserIdFromAccount(Long accountId) throws TransferServiceException {
        String query = String.format("""
            query {
                getAccount(id: %s) {
                    userId
                }
            }
        """, accountId);
        
        JSONObject response = executeGraphQLQuery(query, accountServiceEndpoint);
        return parseUserIdFromAccountResponse(response);
    }
    
    /**
     * Récupère le customerId depuis le service card
     */
    @Retryable(
        value = {IOException.class}, 
        maxAttempts = 3, 
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public Long getUserIdFromCard(Long cardId) throws TransferServiceException {
        String query = String.format("""
            query {
                card(id: %s) {
                    customerId
                }
            }
        """, cardId);
        
        JSONObject response = executeGraphQLQuery(query, cardServiceEndpoint);
        return parseCustomerIdFromCardResponse(response);
    }
    
    /**
     * Récupère les informations utilisateur depuis le service users
     */
    @Retryable(
        value = {IOException.class}, 
        maxAttempts = 3, 
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public UserDTO getUserById(Long userId) throws TransferServiceException, UserNotFoundException {
        String query = String.format("""
            query {
                getCustomerById(id: %s) {
                    email,
                    firstName,
                    lastName
                }
            }
        """, userId);
        
        JSONObject response = executeGraphQLQuery(query, usersServiceEndpoint);
        return parseUserFromResponse(response);
    }
    
    /**
     * Exécute une requête GraphQL
     */
    private JSONObject executeGraphQLQuery(String query, String endpoint) throws TransferServiceException {
        JSONObject jsonRequest = new JSONObject();
        jsonRequest.put("query", query);
        
        RequestBody body = RequestBody.create(
            jsonRequest.toString(),
            MediaType.get("application/json; charset=utf-8")
        );
        
        Request request = new Request.Builder()
                .url(endpoint)
                .post(body)
                .build();
        
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new TransferServiceException("Erreur HTTP lors de l'appel au service: " + response.code() + " - " + endpoint);
            }
            
            String responseBody = response.body().string();
            return new JSONObject(responseBody);
            
        } catch (IOException e) {
            throw new TransferServiceException("Erreur de communication avec le service: " + endpoint, e);
        }
    }
    
    /**
     * Parse l'userId depuis la réponse du service account
     */
    private Long parseUserIdFromAccountResponse(JSONObject response) throws TransferServiceException {
        try {
            if (response.has("errors")) {
                throw new TransferServiceException("Erreur GraphQL Account: " + response.getJSONArray("errors").toString());
            }

            if (!response.has("data")) {
                throw new TransferServiceException("La réponse GraphQL Account ne contient pas de champ 'data'");
            }

            JSONObject data = response.getJSONObject("data");
            JSONObject account = data.optJSONObject("getAccount");
            
            if (account == null) {
                throw new TransferServiceException("Compte introuvable");
            }
            
            return account.getLong("userId");
            
        } catch (Exception e) {
            throw new TransferServiceException("Erreur lors du traitement de la réponse Account GraphQL", e);
        }
    }
    
    /**
     * Parse le customerId depuis la réponse du service card
     */
    private Long parseCustomerIdFromCardResponse(JSONObject response) throws TransferServiceException {
        try {
            if (response.has("errors")) {
                throw new TransferServiceException("Erreur GraphQL Card: " + response.getJSONArray("errors").toString());
            }

            if (!response.has("data")) {
                throw new TransferServiceException("La réponse GraphQL Card ne contient pas de champ 'data'");
            }

            JSONObject data = response.getJSONObject("data");
            JSONObject card = data.optJSONObject("card");
            
            if (card == null) {
                throw new TransferServiceException("Carte introuvable");
            }
            
            return card.getLong("customerId");
            
        } catch (Exception e) {
            throw new TransferServiceException("Erreur lors du traitement de la réponse Card GraphQL", e);
        }
    }
    
    /**
     * Parse les informations utilisateur depuis la réponse du service users
     */
    private UserDTO parseUserFromResponse(JSONObject response) throws TransferServiceException, UserNotFoundException {
        try {
            if (response.has("errors")) {
                throw new TransferServiceException("Erreur GraphQL Users: " + response.getJSONArray("errors").toString());
            }

            if (!response.has("data")) {
                throw new TransferServiceException("La réponse GraphQL Users ne contient pas de champ 'data'");
            }

            JSONObject data = response.getJSONObject("data");
            JSONObject customer = data.optJSONObject("getCustomerById");
            
            if (customer == null) {
                throw new UserNotFoundException("Utilisateur introuvable");
            }
            
            UserDTO userDTO = new UserDTO();
            userDTO.setEmail(customer.getString("email"));
            userDTO.setFirstName(customer.getString("firstName"));
            userDTO.setLastName(customer.getString("lastName"));
            
            return userDTO;
            
        } catch (Exception e) {
            if (e instanceof UserNotFoundException) {
                throw (UserNotFoundException) e;
            }
            throw new TransferServiceException("Erreur lors du traitement de la réponse Users GraphQL", e);
        }
    }
}
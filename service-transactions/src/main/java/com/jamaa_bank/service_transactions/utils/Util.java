package com.jamaa_bank.service_transactions.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.security.auth.login.AccountNotFoundException;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import com.jamaa_bank.service_transactions.dto.AccountDTO;
import com.jamaa_bank.service_transactions.exceptions.AccountServiceException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Component
public class Util {
    
    @Value("${service.account.endpoint:http://127.0.0.1:8079/SERVICE-ACCOUNT/graphql}")
    private String accountServiceEndpoint;
    
    @Value("${service.account.timeout.connect:5}")
    private int connectTimeout;
    
    @Value("${service.account.timeout.read:10}")
    private int readTimeout;
    
    @Value("${service.account.timeout.write:10}")
    private int writeTimeout;
    
    private OkHttpClient client;

    public Util() {
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
        String query = String.format("""
            {
                getAccount(id: %s) {
                    id,
                    accountNumber
                }
            }
        """, accountId);
        
        JSONObject response = executeGraphQLQuery(query);
        return parseAccountFromResponse(response, "getAccount");
    }

    public List<Long> getAccountIdsByUserId(Long userId) throws AccountServiceException {
        String query = String.format("""
            {
                getAccountByUserId(userId: %s) {
                    id
                }
            }
        """, userId);

        JSONObject response = executeGraphQLQuery(query);

        try {
            if (response.has("errors")) {
                throw new AccountServiceException("Erreur GraphQL: " + response.getJSONArray("errors").toString());
            }

            JSONObject data = response.getJSONObject("data");
            if (!data.has("getAccountByUserId") || data.isNull("getAccountByUserId")) {
                return new ArrayList<>();
            }

            // Récupération de l'objet compte unique
            JSONObject account = data.getJSONObject("getAccountByUserId");
            Long accountId = account.getLong("id");

            // Retourner une liste avec cet unique ID
            return List.of(accountId);

        } catch (Exception e) {
            throw new AccountServiceException("Erreur lors de la lecture des comptes utilisateur", e);
        }
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
            accountDTO.setAccountNumber(account.getString("accountNumber"));
            
            return accountDTO;
            
        } catch (Exception e) {
            if (e instanceof AccountNotFoundException) {
                throw (AccountNotFoundException) e;
            }
            throw new AccountServiceException("Erreur lors du traitement de la réponse GraphQL", e);
        }
    }

}

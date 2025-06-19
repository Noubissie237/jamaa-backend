package com.jamaa_bank.service_transfert.utils;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import com.jamaa_bank.service_transfert.dto.BankDTO;
import com.jamaa_bank.service_transfert.exception.AccountNotFoundException;
import com.jamaa_bank.service_transfert.exception.AccountServiceException;

import okhttp3.*;

@Component
public class BankUtil {
    @Value("${service.bank.endpoint:http://127.0.0.1:8079/SERVICE-BANKS/graphql}")
    private String bankServiceEndpoint;
    
    @Value("${service.bank.timeout.connect:5}")
    private int connectTimeout;
    
    @Value("${service.bank.timeout.read:10}")
    private int readTimeout;
    
    private final OkHttpClient httpClient;
    
    public BankUtil() {
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(connectTimeout, TimeUnit.SECONDS)
                .readTimeout(readTimeout, TimeUnit.SECONDS)
                .build();
    }
    
    @Retryable(
        value = {IOException.class}, 
        maxAttempts = 3, 
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public BankDTO getBankById(Long bankId) throws AccountServiceException, AccountNotFoundException {
        String query = String.format("""
            {
                bank(id: %s) {
                    id,
                    name,
                    internalTransferFees,
                    externalTransferFees
                }
            }
        """, bankId);
        
        JSONObject response = executeGraphQLQuery(query);
        return parseBankFromResponse(response, "bank");
    }
    
    private JSONObject executeGraphQLQuery(String query) throws AccountServiceException {
        JSONObject jsonRequest = new JSONObject();
        jsonRequest.put("query", query);
        
        RequestBody body = RequestBody.create(
            jsonRequest.toString(),
            MediaType.get("application/json; charset=utf-8")
        );
        
        Request request = new Request.Builder()
                .url(bankServiceEndpoint)
                .post(body)
                .build();
        
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new AccountServiceException("Erreur HTTP lors de l'appel au service Bank: " + response.code());
            }
            
            String responseBody = response.body().string();
            return new JSONObject(responseBody);
            
        } catch (IOException e) {
            throw new AccountServiceException("Erreur de communication avec le service Bank", e);
        }
    }
    
    private BankDTO parseBankFromResponse(JSONObject response, String fieldName) 
            throws AccountNotFoundException, AccountServiceException {
        try {
            if (response.has("errors")) {
                throw new AccountServiceException("Erreur GraphQL: " + response.getJSONArray("errors").toString());
            }

            if (!response.has("data")) {
                throw new AccountServiceException("La réponse GraphQL ne contient pas de champ 'data': " + response.toString());
            }

            JSONObject data = response.getJSONObject("data");
            
            JSONObject bank = data.optJSONObject(fieldName);
            
            if (bank == null) {
                throw new AccountNotFoundException("La banque demandée n'existe pas");
            }
            
            BankDTO bankDTO = new BankDTO();
            bankDTO.setId(bank.getLong("id"));
            bankDTO.setName(bank.getString("name"));
            bankDTO.setInternalTransferFees(bank.getBigDecimal("internalTransferFees"));
            bankDTO.setExternalTransferFees(bank.getBigDecimal("externalTransferFees"));
            
            return bankDTO;
            
        } catch (Exception e) {
            if (e instanceof AccountNotFoundException) {
                throw (AccountNotFoundException) e;
            }
            throw new AccountServiceException("Erreur lors du traitement de la réponse GraphQL", e);
        }
    }
}

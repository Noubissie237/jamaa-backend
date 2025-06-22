package com.jamaa_bank.service_transfert.utils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import com.jamaa_bank.service_transfert.dto.BankAccountDTO;
import com.jamaa_bank.service_transfert.exception.AccountNotFoundException;
import com.jamaa_bank.service_transfert.exception.AccountServiceException;

import okhttp3.*;

@Component
public class BankAccountUtil {
    @Value("${service.bankaccount.endpoint:http://service-proxy:8079/SERVICE-BANKS-ACCOUNT/graphql}")    
    private String bankAccountServiceEndpoint;
    
    @Value("${service.bankaccount.timeout.connect:5}")
    private int connectTimeout;
    
    @Value("${service.bankaccount.timeout.read:10}")
    private int readTimeout;
    
    private final OkHttpClient httpClient;
    
    public BankAccountUtil() {
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
    public BankAccountDTO getBankAccountByBankId(Long bankId) throws AccountServiceException, AccountNotFoundException {
        String query = String.format("""
            {
                bankAccountByBankId(bankId: %s) {
                    id,
                    bankId,
                    totalBalance,
                    totalInternalTransferFees,
                    totalExternalTransferFees
                }
            }
        """, bankId);
        
        JSONObject response = executeGraphQLQuery(query);
        return parseBankAccountFromResponse(response, "bankAccountByBankId");
    }
    
    @Retryable(
        value = {IOException.class}, 
        maxAttempts = 3, 
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public BankAccountDTO addInternalTransferFees(Long bankAccountId, BigDecimal amount) throws AccountServiceException, AccountNotFoundException {
        String query = String.format("""
            mutation {
                addInternalTransferFees(id: %s, amount: %s) {
                    id,
                    bankId,
                    totalBalance,
                    totalInternalTransferFees,
                    totalExternalTransferFees
                }
            }
        """, bankAccountId, amount);
        
        JSONObject response = executeGraphQLQuery(query);
        return parseBankAccountFromResponse(response, "addInternalTransferFees");
    }
    
    @Retryable(
        value = {IOException.class}, 
        maxAttempts = 3, 
        backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public BankAccountDTO addExternalTransferFees(Long bankAccountId, BigDecimal amount) throws AccountServiceException, AccountNotFoundException {
        String query = String.format("""
            mutation {
                addExternalTransferFees(id: %s, amount: %s) {
                    id,
                    bankId,
                    totalBalance,
                    totalInternalTransferFees,
                    totalExternalTransferFees
                }
            }
        """, bankAccountId, amount);
        
        JSONObject response = executeGraphQLQuery(query);
        return parseBankAccountFromResponse(response, "addExternalTransferFees");
    }
    
    private JSONObject executeGraphQLQuery(String query) throws AccountServiceException {
        JSONObject jsonRequest = new JSONObject();
        jsonRequest.put("query", query);
        
        RequestBody body = RequestBody.create(
            jsonRequest.toString(),
            MediaType.get("application/json; charset=utf-8")
        );
        
        Request request = new Request.Builder()
                .url(bankAccountServiceEndpoint)
                .post(body)
                .build();
        
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new AccountServiceException("Erreur HTTP lors de l'appel au service BankAccount: " + response.code());
            }
            
            String responseBody = response.body().string();
            return new JSONObject(responseBody);
            
        } catch (IOException e) {
            throw new AccountServiceException("Erreur de communication avec le service BankAccount", e);
        }
    }
    
    private BankAccountDTO parseBankAccountFromResponse(JSONObject response, String fieldName) 
            throws AccountNotFoundException, AccountServiceException {
        try {
            if (response.has("errors")) {
                throw new AccountServiceException("Erreur GraphQL: " + response.getJSONArray("errors").toString());
            }

            if (!response.has("data")) {
                throw new AccountServiceException("La réponse GraphQL ne contient pas de champ 'data': " + response.toString());
            }

            JSONObject data = response.getJSONObject("data");
            
            JSONObject bankAccount = data.optJSONObject(fieldName);
            
            if (bankAccount == null) {
                throw new AccountNotFoundException("Le compte bancaire demandé n'existe pas");
            }
            
            BankAccountDTO bankAccountDTO = new BankAccountDTO();
            bankAccountDTO.setId(bankAccount.getLong("id"));
            bankAccountDTO.setBankId(bankAccount.getLong("bankId"));
            bankAccountDTO.setTotalBalance(BigDecimal.valueOf(bankAccount.getDouble("totalBalance")));
            bankAccountDTO.setTotalInternalTransferFees(BigDecimal.valueOf(bankAccount.getDouble("totalInternalTransferFees")));
            bankAccountDTO.setTotalExternalTransferFees(BigDecimal.valueOf(bankAccount.getDouble("totalExternalTransferFees")));
            
            return bankAccountDTO;
            
        } catch (Exception e) {
            if (e instanceof AccountNotFoundException) {
                throw (AccountNotFoundException) e;
            }
            throw new AccountServiceException("Erreur lors du traitement de la réponse GraphQL", e);
        }
    }
}

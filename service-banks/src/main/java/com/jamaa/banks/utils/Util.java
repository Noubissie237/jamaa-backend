package com.jamaa.banks.utils;

import java.io.IOException;

import org.json.JSONObject;
import org.springframework.stereotype.Component;

import com.jamaa.banks.dto.CustomerDTO;

import okhttp3.*;

@Component
public class Util {

    private final String graphqlUsersEndpoint = "http://127.0.0.1:8079/SERVICE-USERS/graphql";
    private final OkHttpClient client = new OkHttpClient();

    public CustomerDTO getCustomer(Long id) {
        String query = String.format("""
            {
                getCustomerById(id: %s) {
                    id,
                    firstName,
                    lastName
                }
            }
        """, id);
        
        JSONObject response = executeGraphQLQuery(query);
        return parseCustomerFromResponse(response, "getCustomerById");
    }

    private JSONObject executeGraphQLQuery(String query) {
        JSONObject jsonRequest = new JSONObject();
        jsonRequest.put("query", query);

        RequestBody body = RequestBody.create(
            jsonRequest.toString(),
            MediaType.parse("application/json")
        );

        Request request = new Request.Builder()
            .url(graphqlUsersEndpoint)
            .post(body)
            .build();
        
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new RuntimeException(
                    "Erreur HTTP " + response.code() + " lors de la requête GraphQL: " + response.message()
                );
            }

            String responseBody = response.body().string();

            return new JSONObject(responseBody);
            
        } catch (IOException e) {
            throw new RuntimeException("Erreur de communication avec le service de comptes", e);
        }
    }

    private CustomerDTO parseCustomerFromResponse(JSONObject response, String fieldName) {
        try {
            if (response.has("errors")) {
                throw new RuntimeException("Erreur GraphQL: " + response.getJSONArray("errors").toString());
            }

            if (!response.has("data")) {
                throw new RuntimeException("La réponse GraphQL ne contient pas de champ 'data': " + response.toString());
            }

            JSONObject data = response.getJSONObject("data");
            
            // Vérifier si des erreurs GraphQL sont présentes
            if (response.has("errors")) {
                throw new RuntimeException("Erreur GraphQL: " + response.getJSONArray("errors").toString());
            }
            
            JSONObject customer = data.optJSONObject(fieldName);
            
            // Si le compte est null, il n'existe pas
            if (customer == null) {
                throw new RuntimeException("L'utilisateur demandé n'existe pas");
            }
            
            CustomerDTO customerDTO = new CustomerDTO();
            customerDTO.setCustomerId(customer.getLong("id"));
            customerDTO.setHolderName(customer.getString("firstName") + " " + customer.getString("lastName"));
            
            return customerDTO;
            
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors du traitement de la réponse GraphQL", e);
        }
    }

}

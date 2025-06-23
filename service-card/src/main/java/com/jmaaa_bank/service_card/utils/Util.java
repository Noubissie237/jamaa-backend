package com.jmaaa_bank.service_card.utils;

import org.json.JSONObject;
import org.springframework.stereotype.Component;

import okhttp3.*;

@Component
public class Util {

    private final String graphqlUsersEndpoint = "http://service-proxy:8079/service-users/graphql";
    private final OkHttpClient client = new OkHttpClient();

    public String getEmail(Long userId) {
        String query = String.format("""
            {
                getCustomerById(id: %s) {
                    email
                }
            }
        """, userId);

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
            if(!response.isSuccessful()) {
                throw new RuntimeException("Erreur lors de la requête GraphQL");
            }

            String responseBody = response.body().string();

            JSONObject jsonResponse = new JSONObject(responseBody);
            JSONObject data = jsonResponse.getJSONObject("data");
            JSONObject customer = data.optJSONObject("getCustomerById");

            return customer.getString("email");
        
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la requête GraphQL", e);
        }
    }

}

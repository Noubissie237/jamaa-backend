package com.jamaa.service_account.utils;

import java.io.IOException;
import java.security.SecureRandom;

import org.json.JSONObject;
import org.springframework.stereotype.Component;

import okhttp3.*;

@Component
public class Util {

    private final String graphqlUsersEndpoint = "http://127.0.0.1:8079/SERVICE-USERS/graphql";
    private final OkHttpClient client = new OkHttpClient();
    private final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private final int CODE_LENGTH = 6;
    private final SecureRandom random = new SecureRandom();

    public String generateRandomCode() {
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            int index = random.nextInt(CHARACTERS.length());
            code.append(CHARACTERS.charAt(index));
        }
        return code.toString(); 
    }

    public boolean userExist(Long userId) throws IOException {
        String query = String.format("""
            {
                getCustomerById(id: %s) {
                    id
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

            return customer == null ? false : true;
        
        } catch (Exception e) {
            return false;
        }
    }

}

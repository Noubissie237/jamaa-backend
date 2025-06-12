package com.jamaa.service_auth.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import okhttp3.*;

import org.apache.hc.client5.http.auth.InvalidCredentialsException;
import org.json.JSONObject;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jamaa.service_auth.utils.Utils;

@Service
public class AuthService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    
    @SuppressWarnings("unused")
    private final RestTemplate restTemplate;
    private String token;

    @Autowired
    Utils utils;

    @Autowired
    PasswordEncoder passwordEncoder;

    private final String graphqlUsersEndpoint = "http://192.168.1.125:8079/SERVICE-USERS/graphql";
    private final OkHttpClient client = new OkHttpClient();

    public AuthService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        addAuthorizationHeaderInterceptor(restTemplate);
        logger.info("AuthService initialisé avec l'endpoint GraphQL: {}", graphqlUsersEndpoint);
    }

    public String customerLogin(String login, String password) throws Exception {
        logger.info("Tentative de connexion pour l'utilisateur: {}", login);
        
        boolean isPhone = isANumber(login);
        String queryType = isPhone ? "getCustomerByPhone" : "getCustomerByEmail";
        String paramName = isPhone ? "phone" : "email";
        
        logger.debug("Type de connexion: {} ({})", queryType, paramName);
        
        String query = String.format("""
            {
                %s(%s: "%s") {
                    id
                    lastName
                    firstName
                    email
                    phone
                    password
                }
            }
        """, queryType, paramName, login);

        logger.debug("Query GraphQL: {}", query);

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
            logger.debug("Réponse HTTP: code={}, message={}", response.code(), response.message());
            
            if(!response.isSuccessful()) {
                logger.error("Erreur lors de la requête GraphQL. Code de réponse: {}", response.code());
                throw new RuntimeException("Erreur lors de la requête GraphQL");
            }

            String responseBody = response.body().string();
            logger.debug("Corps de la réponse: {}", responseBody);
            
            JSONObject jsonResponse = new JSONObject(responseBody);
            JSONObject data = jsonResponse.getJSONObject("data");
            JSONObject customer = data.optJSONObject(queryType);

            if (customer == null) {
                String errorMsg = (isPhone ? "Ce numéro" : "Cet email") + " n'appartient à aucun compte !";
                logger.warn("Utilisateur non trouvé: {}", login);
                throw new InvalidCredentialsException(errorMsg);
            }

            logger.debug("Utilisateur trouvé: {}", customer.getString("id"));

            String hashedPassword = customer.getString("password");

            if (BCrypt.checkpw(password, hashedPassword)) {
                logger.info("Authentification réussie pour l'utilisateur: {}", login);
                String generatedToken = utils.generateToken(customer);
                logger.debug("Token généré avec succès");
                return generatedToken;
            } else {
                logger.warn("Échec de l'authentification - mot de passe incorrect pour: {}", login);
                throw new InvalidCredentialsException("Mot de passe incorrect");
            }
        } catch (InvalidCredentialsException e) {
            logger.error("Erreur d'authentification: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("Erreur inattendue lors de l'authentification", e);
            throw new RuntimeException("Erreur lors de l'authentification", e);
        }
    }

    public boolean isANumber(String str) {
        if (str == null || str.isEmpty()) {
            logger.debug("Chaîne vide ou null fournie à isANumber");
            return false;
        }

        for(char c : str.toCharArray()) {
            if (!Character.isDigit(c)) {
                logger.debug("'{}' n'est pas un nombre", str);
                return false;
            }
        }

        logger.debug("'{}' est un nombre", str);
        return true;
    }

    private void addAuthorizationHeaderInterceptor(RestTemplate restTemplate) {
        logger.debug("Ajout de l'intercepteur d'autorisation");
        
        List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>(restTemplate.getInterceptors());
        interceptors.add((request, body, execution) -> {
            if (token != null) {
                request.getHeaders().add(HttpHeaders.AUTHORIZATION, "Bearer " + token);
                logger.debug("Token d'autorisation ajouté à la requête");
            } else {
                logger.debug("Aucun token disponible pour l'autorisation");
            }
            return execution.execute(request, body);
        });
        restTemplate.setInterceptors(interceptors);
        
        logger.info("Intercepteur d'autorisation configuré");
    }
}
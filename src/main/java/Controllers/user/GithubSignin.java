package Controllers.user;


import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.AuthorizationCodeTokenRequest;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.http.*;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.MemoryDataStoreFactory;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class GithubSignin {
    private static final String CLIENT_ID = "Ov23lioOFCYoarDCP4No";
    private static final String CLIENT_SECRET = "103bfaf1f8c753686b5e491d2e0c3a4c9be21faf";
    private static final String REDIRECT_URI = "http://localhost:8888/callback";

    public Credential signIn() throws IOException {

        HttpTransport transport = new NetHttpTransport();
        JsonFactory jsonFactory = new GsonFactory();

        AuthorizationCodeFlow flow = new AuthorizationCodeFlow.Builder(
                BearerToken.authorizationHeaderAccessMethod(),
                transport,
                jsonFactory,
                new GenericUrl("https://github.com/login/oauth/access_token"),
                new ClientParametersAuthentication(CLIENT_ID, CLIENT_SECRET),
                CLIENT_ID,
                "https://github.com/login/oauth/authorize")
                .setScopes(List.of("user:email"))
                .setDataStoreFactory(new MemoryDataStoreFactory())
                .setRequestInitializer(request -> {
                    // Force GitHub to return JSON instead of form-encoded
                    request.getHeaders().setAccept("application/json");
                })
                .build();

        LocalServerReceiver receiver = new LocalServerReceiver.Builder()
                .setHost("localhost")
                .setPort(8888)
                .setCallbackPath("/callback")
                .build();

        try {
            return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
        } catch (IOException e) {
            // Add more specific error handling
            System.err.println("Error during authorization: " + e.getMessage());
            throw e;
        }

    }



    public String getUserEmail(Credential credential) throws IOException {
        if (credential == null || credential.getAccessToken() == null) {
            throw new IOException("Invalid credential - not authenticated");
        }

        HttpRequestFactory requestFactory = new NetHttpTransport().createRequestFactory();
        GenericUrl emailsUrl = new GenericUrl("https://api.github.com/user/emails");

        try {
            HttpRequest request = requestFactory.buildGetRequest(emailsUrl)
                    .setHeaders(new HttpHeaders()
                            .setAuthorization("token " + credential.getAccessToken())
                            .setAccept("application/vnd.github.v3+json"));

            String emailsJson = request.execute().parseAsString();
            JsonArray emails = JsonParser.parseString(emailsJson).getAsJsonArray();

            for (var element : emails) {
                JsonObject email = element.getAsJsonObject();
                if (email.get("primary").getAsBoolean()) {
                    return email.get("email").getAsString();
                }
            }
            return !emails.isEmpty() ? emails.get(0).getAsJsonObject().get("email").getAsString() : null;
        } catch (Exception e) {
            throw new IOException("Failed to get user email: " + e.getMessage(), e);
        }
    }


}
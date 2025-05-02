package Controllers.user;
import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.MemoryDataStoreFactory;


import java.io.IOException;
import java.util.Arrays;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class GoogleSignIn {

    private static final String CLIENT_ID = "385052442816-utknhvsv0qku3q8gfahprckmhmhbnkno.apps.googleusercontent.com";
    private static final String CLIENT_SECRET = "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";
    private static final String REDIRECT_URI = "http://localhost";

    public Credential signIn() throws IOException {
        AuthorizationCodeFlow flow = new AuthorizationCodeFlow.Builder(
                BearerToken.authorizationHeaderAccessMethod(),
                new NetHttpTransport(),
                new GsonFactory(),
                new GenericUrl("https://oauth2.googleapis.com/token"),
                new ClientParametersAuthentication(CLIENT_ID, CLIENT_SECRET),
                CLIENT_ID,
                "https://accounts.google.com/o/oauth2/auth")
                .setScopes(Arrays.asList("openid", "email", "profile"))
                .setDataStoreFactory(new MemoryDataStoreFactory())
                .build();

        LocalServerReceiver receiver = new LocalServerReceiver.Builder()
                .setHost("localhost")
                .setPort(8888)
                .build();

        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    public String getUserEmail(Credential credential) throws IOException {
        String accessToken = credential.getAccessToken();
        HttpRequestFactory requestFactory = new NetHttpTransport().createRequestFactory();
        GenericUrl url = new GenericUrl("https://www.googleapis.com/oauth2/v3/userinfo");
        HttpRequest request = requestFactory.buildGetRequest(url);
        request.getHeaders().setAuthorization("Bearer " + accessToken);
        String jsonResponse = request.execute().parseAsString();
        JsonObject userInfo = JsonParser.parseString(jsonResponse).getAsJsonObject();
        return userInfo.get("email").getAsString();
    }
}
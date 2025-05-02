package service;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class IPGeolocationService {
    private static final String API_KEY = "ef494c677c7ff43d9bd2d490e8ffdcd6d";
    private static final String BASE_URL = "https://api.ipgeolocation.io/ipgeo";

    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Fetches geolocation data for the caller's IP.
     * @return a JsonNode with fields like city, country_name, currency.code, timezone.name, etc.
     */
    public JsonNode getGeoData() throws IOException, InterruptedException {
        String url = String.format("%s?apiKey=%s", BASE_URL, API_KEY);
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != 200) {
            throw new IOException("Failed to fetch geolocation: HTTP " + resp.statusCode());
        }
        return mapper.readTree(resp.body());
    }
    /**
     * Fetches geolocation data for a specific IP.
     * @param ip the IP address to lookup
     * @return a JsonNode with geolocation fields
     */
    public JsonNode getGeoDataForIp(String ip) throws IOException, InterruptedException {
        String url = String.format("%s?apiKey=%s&ip=%s", BASE_URL, API_KEY, ip);
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() != 200) {
            throw new IOException("Failed to fetch geolocation: HTTP " + resp.statusCode());
        }
        return mapper.readTree(resp.body());
    }
}

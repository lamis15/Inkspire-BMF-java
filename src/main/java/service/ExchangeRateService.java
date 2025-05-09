package service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

public class ExchangeRateService {
    private static final String API_KEY = "ecbbff142e1bcf46d111204c0";
    private static final String BASE_URL = "https://v6.exchangerate-api.com/v6/";

    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public Map<String, Double> getRates(String baseCurrency) throws IOException, InterruptedException {
        String url = BASE_URL + API_KEY + "/latest/" + baseCurrency;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();
        HttpResponse<String> resp = http.send(request, HttpResponse.BodyHandlers.ofString());
        JsonNode root = mapper.readTree(resp.body());
        JsonNode ratesNode = root.path("conversion_rates");
        return mapper.convertValue(ratesNode, mapper.getTypeFactory()
                .constructMapType(Map.class, String.class, Double.class));
    }
    public double getRate(String fromCurrency, String toCurrency) throws IOException, InterruptedException {
        Map<String, Double> rates = getRates(fromCurrency);
        Double rate = rates.get(toCurrency);
        if (rate == null) {
            throw new IllegalArgumentException("Unsupported currency: " + toCurrency);
        }
        return rate;
    }
    public double convert(double amount, String fromCurrency, String toCurrency) throws IOException, InterruptedException {
        double rate = getRate(fromCurrency, toCurrency);
        return amount * rate;
    }
}

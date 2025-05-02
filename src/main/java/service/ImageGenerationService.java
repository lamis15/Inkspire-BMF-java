package service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ImageGenerationService {

    private static final String API_KEY = "sk-dJAVdRrlPPtWBi7rGMGck8YAUmADHZArwmjCokssPchA6ECw"; // TODO: Replace
    private static final String API_URL = "https://api.stability.ai/v2beta/stable-image/generate/core";

    public static String generateImage(String prompt) throws IOException, InterruptedException {
        String boundary = "Boundary-" + System.currentTimeMillis();

        String body = "--" + boundary + "\r\n" +
                "Content-Disposition: form-data; name=\"prompt\"\r\n\r\n" +
                prompt + "\r\n" +
                "--" + boundary + "--\r\n";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Authorization", "Bearer " + API_KEY)
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());

        // Save binary image to a temporary file
        File tempFile = File.createTempFile("generated-artwork-", ".png");
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write(response.body());
        }

        System.out.println("Image saved to: " + tempFile.getAbsolutePath());

        // Return a proper "file:///" URL
        return tempFile.toURI().toString();
    }
}

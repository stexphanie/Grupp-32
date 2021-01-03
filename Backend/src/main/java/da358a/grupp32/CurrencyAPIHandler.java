package da358a.grupp32;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;

/**
 * Manages all communication to the Currency API used to retrieve
 * actual conversion rates between currencies.
 * @author Kasper S. Skott
 */
class CurrencyAPIHandler {

    private String key;
    private Gson gson;
    private HttpClient client;

    /**
     * Initializes the API handler and sets up a new HTTPClient.
     * @param gsonInstance
     * @throws IOException if no 'currencykey.txt' file was found.
     */
    CurrencyAPIHandler(Gson gsonInstance) throws IOException {
        key = Files.readString(Path.of("currencykey.txt"), StandardCharsets.UTF_8);
        gson = gsonInstance;

        client = HttpClient
                .newBuilder()
                .connectTimeout(Duration.ofMillis(4000))
                .build();
    }

    /**
     * Attempts to retrieve the specified currency conversion rate.
     * @param fromCurrency currency to convert from
     * @param toCurrency currency to convert to
     * @return empty if no rate could be retrieved
     */
    Optional<Double> getConversionRate(String fromCurrency, String toCurrency) {
        double conversionRate;
        StringBuilder uri = new StringBuilder(
                "https://api.getgeoapi.com/api/v2/currency/convert?");

        uri.append("api_key=").append(key);
        uri.append("&from=").append(fromCurrency);
        uri.append("&to=").append(toCurrency);
        uri.append("&amount=1");
        uri.append("&format=json");

        HttpRequest request = HttpRequest
                .newBuilder(URI.create(uri.toString()))
                .GET()
                .build();

        try {
            // Send a request to the Currency API
            HttpResponse<String> response = client.send(
                    request, HttpResponse.BodyHandlers.ofString());

            JsonObject jsonObject = gson.fromJson(response.body(), JsonObject.class);
            JsonElement rate;

            // Safely extract what we want from the response.
            if ((jsonObject = jsonObject.getAsJsonObject("rates")) == null)
                return Optional.empty();
            if ((jsonObject = jsonObject.getAsJsonObject(toCurrency)) == null)
                return Optional.empty();
            if ((rate = jsonObject.get("rate")) == null)
                return Optional.empty();

            conversionRate = rate.getAsDouble();

        } catch (InterruptedException | IOException e) {
            return Optional.empty();
        }

        return Optional.of(conversionRate);
    }

}

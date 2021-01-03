package da358a.grupp32;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import spark.Route;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * The controller handles all requests made to the website and its API
 * @author Kasper S. Skott
 */
class Controller {

    private String homePageSrc;
    private String apiDocSrc;
    private Gson gson;
    private CurrencyCache currencyCache;

    // Handles requests to the home URL
    Route homePageHandler = (request, response) -> {
        response.status(200);
        response.header("Content-Type", "text/html");
        response.body(homePageSrc);
        return response.body();
    };

    // Handles requests to the api documentation URL
    Route apiDocHandler = (request, response) -> {
        response.status(200);
        response.header("Content-Type", "text/html");
        response.body(apiDocSrc);
        return response.body();
    };

    // Handles requests to use the currency conversion service
    Route conversionHandler = (request, response) -> {

        response.header("Content-Type", "application/json");

        // Handle requests that don't accept JSON
        if (!request.headers("Accept").equals("application/json")) {
            response.status(400);
            response.body(
                    "{ \"fault\": \"API request may only accept JSON\" }");
        }
        else {
            String prefCurrency = request.queryParamOrDefault("preferredCurrency", "");
            String amountString = request.queryParamOrDefault("amount", "");
            String latString = request.queryParamOrDefault("lat", "");
            String lonString = request.queryParamOrDefault("lon", "");
            String city = request.queryParamOrDefault("city", "");
            String country = request.queryParamOrDefault("country", "");

            if (amountString.isEmpty()) {
                response.status(400);
                response.body("{ \"fault\": \"Query parameter " +
                        "'amount' must be specified\" }");
                return response.body();
            }

            double amount = Double.parseDouble(amountString);
            double lat = 0.0;
            if (!latString.isEmpty())
                lat = Double.parseDouble(latString);

            double lon = 0.0;
            if (!lonString.isEmpty())
                lon = Double.parseDouble(lonString);

            if (prefCurrency.isEmpty()) {
                response.status(400);
                response.body("{ \"fault\": \"Query parameter " +
                        "'preferredCurrency' must be specified\" }");
                return response.body();
            }

            boolean hasCity = !city.isEmpty();
            boolean hasCountry = !country.isEmpty();
            boolean hasPosition = !latString.isEmpty() && !lonString.isEmpty();

            if (!hasCity && !hasCountry && !hasPosition) {
                response.status(400);
                response.body("{ \"fault\": \"You need to specify either " +
                        "a country, city, or position\" }");
                return response.body();
            }

            String currencyToConvert;

            /* TODO: Get a currency from a call to the GeoDB Cities API
            *   and set currencyToConvert below to the returned currency
            */

            // Placeholder, should be set using the value from GeoDB handler.
            currencyToConvert = "EUR";

            CurrencyResponse currResponse = currencyCache.convertCurrency(
                    prefCurrency,
                    currencyToConvert,
                    amount);

            response.status(200);
            response.body(gson.toJson(currResponse));
        }

        return response.body();
    };

    /**
     * Initializes the Controller and loads resources.
     * @throws IOException if there were problems with loading the html file
     */
    Controller() throws IOException {
        homePageSrc = new String(Files.readAllBytes(
                Paths.get("../Frontend/index.html")));
        apiDocSrc = new String(Files.readAllBytes(
                Paths.get("../Frontend/api_doc.html")));

        gson = new GsonBuilder().setPrettyPrinting().create();
        currencyCache = new CurrencyCache(gson);
    }

    /**
     * Stops the controller, and saves the currency cache to disk.
     */
    void stop() {
        currencyCache.saveCacheToDisk();
    }

}

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

        // Handle requests that don't accept JSON
        if (!request.headers("Accept").equals("application/json")) {
            response.status(400);
            response.header("Content-Type", "application/json");
            response.body(
                    "{ \"fault\": \"API request may only accept JSON\" }");
        }
        else {

            /* TODO: Get a currency from a call to the GeoDB Cities API,
                then get a CurrencyResponse object from a method call to
                the cache, using that currency.
             */

            // --- For now, send with a placeholder response --- //
            CurrencyResponse currResponse = new CurrencyResponse();
            currResponse.prefCurrency = request.params("currency");
            currResponse.currencyConverted = "EUR";
            currResponse.returnAmount = 250.125;
            // ------------------------ //

            response.status(200);
            response.body(gson.toJson(currResponse));
        }

        response.header("Content-Type", "application/json");
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

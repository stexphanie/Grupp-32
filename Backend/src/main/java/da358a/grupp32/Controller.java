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
    private GeoDBAPIHandler geoDBAPIHandler;

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

            String currencyToConvert = "";

            /* TODO: Get a currency from a call to the GeoDB Cities API
             *   and set currencyToConvert below to the returned currency
             */

            // Placeholder, should be set using the value from GeoDB handler.
            //currencyToConvert = "EUR";

            if (!hasCity && !hasCountry && !hasPosition) {
                response.status(400);
                response.body("{ \"fault\": \"You need to specify either " +
                        "a country, city, or position\" }");
                return response.body();
            }
            else if(hasPosition){
                String countryFromCoords = GeocodeAPIHandler.getCountryFromCoords(lat,lon);
                boolean isErrorCode = GeoDBAPIHandler.checkIfCode(countryFromCoords);
                if(isErrorCode){
                    response.status(404);
                    response.body("{ \"fault\": \"Couldn't retrieve a country from your position\" }");
                    return response.body();
                }
                else {
                    String currency = geoDBAPIHandler.getCurrencyFromCountry(countryFromCoords);
                    if(currency.equals("404")){
                        response.status(404);
                        response.body("{ \"fault\": \"Couldn't retrieve a currency from your country: "+countryFromCoords+"\" }");
                        return response.body();
                    }
                    currencyToConvert = currency;
                }
            }
            else if(hasCountry){
                String currency = geoDBAPIHandler.getCurrencyFromCountry(country);
                boolean isErrorCode = GeoDBAPIHandler.checkIfCode(currency);
                if(currency.equals("404")){
                    response.status(404);
                    response.body("{ \"fault\": \"Couldn't retrieve a currency from your country: "+country+"\" }");
                    return response.body();
                }
                else if(isErrorCode){
                    response.status(500);
                    response.body("{ \"fault\": \"Internal server error\" }");
                    return response.body();
                }
                else {
                    currencyToConvert = currency;
                }
            }
            else {
                String currency = geoDBAPIHandler.getCurrencyFromCity(city);
                boolean isErrorCode = GeoDBAPIHandler.checkIfCode(currency);
                if(currency.equals("404")){
                    response.status(404);
                    response.body("{ \"fault\": \"Couldn't retrieve a currency from your city: "+city+"\" }");
                    return response.body();
                }
                else if(isErrorCode){
                    response.status(500);
                    response.body("{ \"fault\": \"Internal server error\" }");
                    return response.body();
                }
                else {
                    currencyToConvert = currency;
                }
            }


            CurrencyResponse currResponse = currencyCache.convertCurrency(
                    prefCurrency,
                    currencyToConvert,
                    amount);

            response.status(200);
            response.body(gson.toJson(currResponse));
            response.header("Access-Control-Allow-Origin", "*");
        }

        return response.body();
    };

    /**
     * Initializes the Controller and loads resources.
     * @throws IOException if there were problems with loading the html file
     */
    Controller() throws IOException {
        homePageSrc = new String(Files.readAllBytes(
                Paths.get("../Webbgränssnitt/index.html")));
        apiDocSrc = new String(Files.readAllBytes(
                Paths.get("../Webbgränssnitt/api_doc.html")));

        gson = new GsonBuilder().setPrettyPrinting().create();
        currencyCache = new CurrencyCache(gson);
        geoDBAPIHandler = new GeoDBAPIHandler(gson);
    }

    /**
     * Stops the controller, and saves the currency cache to disk.
     */
    void stop() {
        currencyCache.saveCacheToDisk();
    }

}

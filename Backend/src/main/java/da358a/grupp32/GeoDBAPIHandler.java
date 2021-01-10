package da358a.grupp32;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Class to retrieve currency using country or city.
 * @author Robert L
 */
public class GeoDBAPIHandler {

    private String key;
    private Gson gson;

    public GeoDBAPIHandler(Gson gson) throws IOException {
        this.gson = gson;
        this.key = Files.readString(Path.of("geokey.txt"), StandardCharsets.UTF_8);
    }

    /**
     * @param country A country name.
     * @return currency used in the country, if country can't be found returns -1. Else if any of the
     * error codes "400,401,403,404" happen it returns them.
     */
    public String getCurrencyFromCountry(String country){
        //System.out.println(country);
        String res = null;
        country = country.replaceAll(" ", "%20");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://wft-geo-db.p.rapidapi.com/v1/geo/countries?namePrefix="+country))
                .header("x-rapidapi-key", key)
                .header("x-rapidapi-host", "wft-geo-db.p.rapidapi.com")
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();
        try {
            HttpResponse<String> response = HttpClient.newHttpClient().send(request,HttpResponse.BodyHandlers.ofString());
            //System.out.println(response.body());
            if(response.statusCode() == 200){
                //OKAY BOSS
                res = response.body();
                JsonObject object = JsonParser.parseString(res).getAsJsonObject();
                JsonArray data = object.getAsJsonArray("data");
                if(data.size() == 0){
                    return "404";
                }
                else {
                    JsonArray array = data.get(0).getAsJsonObject().getAsJsonArray("currencyCodes");
                    return array.get(0).getAsString();
                }

            }
            // THIS UNDER SHOULD NOT HAPPEN :monkaW
            else if(response.statusCode() == 400){
                //BAD REQUEST
                System.out.println("bad request");
                System.out.println(response.body());
                return "400";
            }
            else if(response.statusCode() == 401){
                //UNAUTHORIZED
                System.out.println("unauthorized");
                System.out.println(response.body());
                return "401";
            }
            else if(response.statusCode() == 403){
                //FORBIDDEN
                System.out.println("forbidden");
                System.out.println(response.body());
                return "403";
            }
            else if(response.statusCode()== 404){
                //NOT FOUND
                System.out.println("not found");
                System.out.println(response.body());
                return "404";
            }
            else {
                return "-1";
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return "-1";
        }
    }

    /**
     * Method to retrieve currency from a country that city is in.
     * @param city, a city
     * @return Returns a currency in String format from the given city.
     */
    public String getCurrencyFromCity(String city){
        String res = null;
        city = city.replaceAll(" ", "%20");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://wft-geo-db.p.rapidapi.com/v1/geo/cities?namePrefix="+city))
                .header("x-rapidapi-key", key)
                .header("x-rapidapi-host", "wft-geo-db.p.rapidapi.com")
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();
        try {
            HttpResponse<String> response = HttpClient.newHttpClient().send(request,HttpResponse.BodyHandlers.ofString());
            if(response.statusCode() == 200){
                //OKAY BOSS
                res = response.body();
                JsonObject object = JsonParser.parseString(res).getAsJsonObject();
                JsonArray data = object.getAsJsonArray("data");
                if(data.size() == 0){
                    return "404";
                }
                else {
                    Thread.sleep(1500);
                    JsonObject dataObject = data.get(0).getAsJsonObject();
                    String country = dataObject.get("country").getAsString();
                    return getCurrencyFromCountry(country);
                }

            }
            // THIS UNDER SHOULD NOT HAPPEN
            else if(response.statusCode() == 400){
                //BAD REQUEST
                System.out.println("bad request");
                System.out.println(response.body());
                return "400";
            }
            else if(response.statusCode() == 401){
                //UNAUTHORIZED
                System.out.println("unauthorized");
                System.out.println(response.body());
                return "401";
            }
            else if(response.statusCode() == 403){
                //FORBIDDEN
                System.out.println("forbidden");
                System.out.println(response.body());
                return "403";
            }
            else if(response.statusCode()== 404){
                //NOT FOUND
                System.out.println("not found");
                System.out.println(response.body());
                return "404";
            }
            else {
                //WILL RETURN -1 IF RATE LIMIT IS EXCEEDED
                return "-1";
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return "-1";
        }
    }

    /**
     * Method to check if value retrieved is an error code, or not.
     * @param codes input error codes
     * @return returns true if input codes is one of the given cases, otherwise returns false.
     */
    public static boolean checkIfCode(String codes){
        switch (codes){
            case "400":
            case "401":
            case "403":
            case "-1":
                return true;
            default:
                return false;
        }
    }






    public static void main(String[] args) {
        try {
            GeoDBAPIHandler handler = new GeoDBAPIHandler(new Gson());
            System.out.println(handler.getCurrencyFromCity("Bangkok"));
            //System.out.println(currency);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}

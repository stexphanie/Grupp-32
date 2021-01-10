package da358a.grupp32;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Class to retrieve country using coordinates.
 * @author Robert L.
 */
public class GeocodeAPIHandler {

    /**
     * @param lat latitude of location
     * @param lon longitute of location
     * @return A country given the coordinates, if no country is found -1 is returned. Else if any of the
     * error codes "400,401,403,404" happen it returns them.
     */
    public static String getCountryFromCoords(double lat, double lon){
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://geocode.xyz/"+lat+ "," + lon+"?json=1"))
                .header("Accept", "application/json")
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();
        try {
            HttpResponse<String> response = HttpClient.newHttpClient().send(request,HttpResponse.BodyHandlers.ofString());
            //System.out.println(response.body());
            if(response.statusCode() == 200){
                //OKAY BOSS
                JsonObject object = JsonParser.parseString(response.body()).getAsJsonObject();
                if(object != null) {
                    try {
                        return object.get("country").getAsString();
                    } catch (NullPointerException e) {
                        return "404";
                    }
                }
                else {
                    return "404";
                }
            }
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
            return "-1";
        }
    }

    public static void main(String[] args) {
        //System.out.println(getCountryFromCoords(56,12));
        GeoDBAPIHandler handler;


        try {
            handler = new GeoDBAPIHandler(new Gson());
            System.out.println(handler.getCurrencyFromCountry(getCountryFromCoords(5125124,12412441)));
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}

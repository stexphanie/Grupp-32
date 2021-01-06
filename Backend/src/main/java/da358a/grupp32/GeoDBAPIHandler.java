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

public class GeoDBAPIHandler {

    private String key;
    private Gson gson;

    public GeoDBAPIHandler() throws IOException {
        this.gson = new Gson();
        this.key = Files.readString(Path.of("geokey.txt"), StandardCharsets.UTF_8);
    }

    public String getCountryDetails(String countryCodeISO){
        String res = null;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://wft-geo-db.p.rapidapi.com/v1/geo/countries/"+countryCodeISO))
                .header("x-rapidapi-key", key)
                .header("x-rapidapi-host", "wft-geo-db.p.rapidapi.com")
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();
        try {
            HttpResponse<String> response = HttpClient.newHttpClient().send(request,HttpResponse.BodyHandlers.ofString());
            if(response.statusCode() == 200){

                //OKAY BOSS
                res = response.body();
            }
            else if(response.statusCode() == 400){
                //BAD REQUEST
                System.out.println("bad request");
                System.out.println(response.body());
            }
            else if(response.statusCode() == 401){
                //UNAUTHORIZED
                System.out.println("unauthorized");
                System.out.println(response.body());
            }
            else if(response.statusCode() == 403){
                //FORBIDDEN
                System.out.println("forbidden");
                System.out.println(response.body());
            }
            else if(response.statusCode()== 404){
                //NOT FOUND
                System.out.println("not found");
                System.out.println(response.body());
            }
            else {
                //IDK
                System.out.println("idk");
                System.out.println(response.statusCode());
            }

            System.out.println(response);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

       // System.out.println(res);
        return res;

    }






    public static void main(String[] args) {
        try {

            GeoDBAPIHandler handler = new GeoDBAPIHandler();
            JsonObject object = JsonParser.parseString(handler.getCountryDetails("US")).getAsJsonObject();
            System.out.println(object.toString());
            JsonObject data = object.getAsJsonObject("data");
            JsonArray array = data.getAsJsonArray("currencyCodes");
            String currency = array.get(0).getAsString();
            System.out.println(currency);





            //System.out.println(currency);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}

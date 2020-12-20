package da358a.grupp32;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

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

    public String makeRequest(){

        String res = null;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://wft-geo-db.p.rapidapi.com/v1/geo/cities"))
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
            else if(response.statusCode() == 403){
                //GATEWAY ERROR SOMETHING BAD
            }
            else {
                System.out.println(response.statusCode());
            }

            System.out.println(response);

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return res;

    }

    public static void main(String[] args) {
        try {
            GeoDBAPIHandler handler = new GeoDBAPIHandler();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}

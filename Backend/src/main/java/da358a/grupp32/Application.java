package da358a.grupp32;

import static spark.Spark.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * The entry point of the backend application.
 * @author Kasper S. Skott
 */
public class Application {

    /**
     * Starts the server application
     */
    public static void main(String[] args) {

        externalStaticFileLocation("../Frontend");

        initExceptionHandler((e) -> {
            System.err.println("Spark initialization failed");
            System.exit(1);
        });

        port(8192);

        try {
            Controller apiController = new Controller();

            // Route requests to the root URL
            get("/",
                    apiController.homePageHandler);

           // Route requests to the api documentation URL
            get("/api/v1",
                    apiController.apiDocHandler);

            // Route requests to the conversion service
            get("/api/v1/conversion/:currency",
                    apiController.conversionHandler);

            // Set up reading from the console
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(System.in));

            // Wait for the 'exit' user input. If the application isn't closed
            // using this method, the currency cache will not be saved.
            String input;
            do {
                input = reader.readLine();
            }
            while (!input.equals("exit"));

            // Exit gracefully, saving currency cache to disk
            apiController.stop();
            stop();
        }
        catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

    }

}

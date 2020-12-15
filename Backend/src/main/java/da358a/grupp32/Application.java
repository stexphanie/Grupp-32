package da358a.grupp32;

import spark.Spark;
import java.io.IOException;

/**
 * The entry point of the backend application.
 * @author Kasper S. Skott
 */
class Application {

    /**
     * Starts the server application
     */
    public static void main(String[] args) {

        Spark.initExceptionHandler((e) -> {
            System.err.println("Spark initialization failed");
            System.exit(1);
        });

        Spark.port(8192);

        try {
            Controller apiController = new Controller();

            // Route requests to the root URL
            Spark.get("/",
                    apiController.homePageHandler);

            // Route requests to the conversion service
            Spark.get("/api/v1/conversion/:currency",
                    apiController.conversionHandler);
        }
        catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

}

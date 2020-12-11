package da358a.grupp32;

import spark.Spark;

public class Application {

    public static void main(String[] args) {

        Spark.initExceptionHandler((e) -> {
            System.err.println("Spark initialization failed");
            System.exit(1);
        });

        Spark.port(8192);

        Spark.get("/", (request, response) -> {
           return "<h1>Hello, world!</h1>";
        });

    }

}

package da358a.grupp32;

import com.google.gson.Gson;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

class CurrencyAPIHandler {

    private String key;
    private Gson gson;

    CurrencyAPIHandler(Gson gsonInstance) throws IOException {
        key = Files.readString(Path.of("geokey.txt"), StandardCharsets.UTF_8);
        gson = gsonInstance;
    }

    double getConversionRate(String fromCurrency, String toCurrency) {

        // TODO: Make a call to the Currency API and
        //  return the conversion rate.

        return 1.0;
    }

}

package da358a.grupp32;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Hashtable;

/**
 * @author Kasper S. Skott
 */
class BigMacIndex {

    private static class BMIData {
        String countryName;
        double adjustmentRate;
        double convRateToUSD;
    }

    private String bmiLatestUpdate;
    private Hashtable<String, BMIData> bmiTable;

    /**
     * Constructs and loads BMI data
     */
    BigMacIndex() {
        if (!loadBMIData(Path.of("big-mac-raw-index.csv"))) {
            System.err.println("Big Mac Index data is unavailable.");
        }
    }

    /**
     * Loads and extract relevant data from a file with big mac
     * index data into memory.
     * @param file path to file
     * @return true if successful, otherwise false
     */
    private boolean loadBMIData(Path file) {
        bmiTable = new Hashtable<>(56);

        try {
            String line;
            BufferedReader br = new BufferedReader(
                    new FileReader(file.toFile()));

            br.mark(1024);
            br.readLine(); // Discard first line

            // Extract and store the date
            line = br.readLine();
            bmiLatestUpdate = line.substring(0, line.indexOf(','));

            br.reset(); // Reset to the beginning
            br.readLine(); // Discard first line again

            BMIData bmiData;
            String currency = "";

            String col;
            line = br.readLine();
            while (line != null) {

                bmiData = new BMIData();

                // Go through each column
                int colIndex = 0;
                int start = 0;
                int end = 0;
                while (end != -1) {
                    end = line.indexOf(',', start);
                    if (end == -1)
                        col = line.substring(start, line.length()-1);
                    else
                        col = line.substring(start, end);

                    start = end+1;

                    if (colIndex == 2) { // Currency code
                        currency = col;
                    }
                    else if (colIndex == 3) { // Country name
                        bmiData.countryName = col;
                    }
                    else if (colIndex == 5) { // USD conversion rate
                        bmiData.convRateToUSD = 1.0 / Double.parseDouble(col);
                    }
                    else if (colIndex == 7) { // BMI adjustment percentage
                        bmiData.adjustmentRate = Double.parseDouble(col);
                    }

                    colIndex++;
                }

                // Add entry into the hashtable
                bmiTable.put(currency, bmiData);

                line = br.readLine();
            }

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * Calculates an estimate price based on Big Mac Index.
     * @param localCurrency
     * @param prefCurrency
     * @param prefAmount
     * @param otherCurrency
     * @param city
     * @return
     */
    BMIAdjustedCityPrice getAdjustedPrice(String localCurrency,
            String prefCurrency, double prefAmount,
            String otherCurrency, String city)
    {
        BMIAdjustedCityPrice ret = new BMIAdjustedCityPrice();
        BMIData prefBMIData = bmiTable.get(prefCurrency);
        BMIData otherBMIData = bmiTable.get(otherCurrency);
        BMIData localBMIData = bmiTable.get(localCurrency);

        if (otherBMIData == null || prefBMIData == null ||
                localBMIData == null) {
            ret.country = "No BMI data";
            return ret;
        }

        ret.city = city;
        ret.country = otherBMIData.countryName;
        ret.prefCurrencyAmount = prefAmount * (1.0 + otherBMIData.adjustmentRate - localBMIData.adjustmentRate);
        ret.dollarAmount = ret.prefCurrencyAmount * prefBMIData.convRateToUSD;

        return ret;
    }

    /**
     * Appends the currency response with BMI data. This method expects
     * the response to already have valid data in the following members:
     * - prefCurrency
     * - convertedAmount
     * - originalCurrency
     * - originalAmount
     * @param response
     * @return a reference to the same object that was passed in.
     */
    CurrencyResponse appendResponse(CurrencyResponse response) {
        double prefAmount = response.convertedAmount;
        String prefCurrency = response.prefCurrency;

        BMIAdjustedCityPrice e = getAdjustedPrice(response.originalCurrency,
                prefCurrency, prefAmount, prefCurrency, "");

        if (bmiLatestUpdate == null || e.country.equals("No BMI data")) {
            response.bmiDataAvailable = false;
            return response;
        }


        response.bmiLatestUpdate = bmiLatestUpdate;
        response.bmiDataAvailable = true;
        response.bmiAdjustedAmount = e.prefCurrencyAmount;

        BMIData prefBMIData = bmiTable.get(prefCurrency);
        if (prefBMIData != null)
            response.bmiAdjustedAmountCountry = prefBMIData.countryName;

        response.cityComparison = new ArrayList<>(10);

        e = getAdjustedPrice(response.originalCurrency,
                prefCurrency, prefAmount, "USD", "Washington, D.C.");
        response.cityComparison.add(e);

        e = getAdjustedPrice(response.originalCurrency,
                prefCurrency, prefAmount, "CNY", "Beijing");
        response.cityComparison.add(e);

        e = getAdjustedPrice(response.originalCurrency,
                prefCurrency, prefAmount, "GBP", "London");
        response.cityComparison.add(e);

        e = getAdjustedPrice(response.originalCurrency,
                prefCurrency, prefAmount, "MXN", "Mexico City");
        response.cityComparison.add(e);

        e = getAdjustedPrice(response.originalCurrency,
                prefCurrency, prefAmount, "THB", "Bangkok");
        response.cityComparison.add(e);

        e = getAdjustedPrice(response.originalCurrency,
                prefCurrency, prefAmount, "TRY", "Ankara");
        response.cityComparison.add(e);

        e = getAdjustedPrice(response.originalCurrency,
                prefCurrency, prefAmount, "MYR", "Kuala Lumpur");
        response.cityComparison.add(e);

        e = getAdjustedPrice(response.originalCurrency,
                prefCurrency, prefAmount, "HKD", "Hong Kong");
        response.cityComparison.add(e);

        e = getAdjustedPrice(response.originalCurrency,
                prefCurrency, prefAmount, "RUB", "Moscow");
        response.cityComparison.add(e);

        e = getAdjustedPrice(response.originalCurrency,
                prefCurrency, prefAmount, "JPY", "Tokyo");
        response.cityComparison.add(e);

        // If the list contains the preferred currency, remove it from the list.
        e = new BMIAdjustedCityPrice();
        e.country = bmiTable.get(prefCurrency).countryName;
        response.cityComparison.remove(e);

        // If the list contains the currency converted from, remove it from the list.
        e = new BMIAdjustedCityPrice();
        e.country = bmiTable.get(response.originalCurrency).countryName;
        response.cityComparison.remove(e);

        return response;
    }

}

package da358a.grupp32;

import com.google.gson.Gson;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;
import java.util.Hashtable;

class CurrencyCache {

    private class CachedConversionRate {
        double rateLeftRight;
        double rateRightLeft;
        private Date expires;

        void refresh(double rateLeftRight) {
            this.rateLeftRight = rateLeftRight;
            this.rateRightLeft = 1.0 / rateLeftRight;
            expires = Date.from(Instant.now().plusSeconds(86400)); // 24h
        }

        boolean hasExpired() {
            return expires.compareTo(Date.from(Instant.now())) <= 0;
        }
    }

    private Hashtable<String, CachedConversionRate> cache;
    private CurrencyAPIHandler apiHandler;

    CurrencyCache(Gson gsonInstance) throws IOException {
        cache = new Hashtable<>(64);
        apiHandler = new CurrencyAPIHandler(gsonInstance);
    }

    private String buildCacheKey(String prefCurrency,
                                String currencyToConvert
    ) {
        StringBuilder key = new StringBuilder();
        prefCurrency = prefCurrency.toLowerCase();
        currencyToConvert = currencyToConvert.toLowerCase();

        if (prefCurrency.compareTo(currencyToConvert) > 0)
            key.append(currencyToConvert).append('-').append(prefCurrency);
        else
            key.append(prefCurrency).append('-').append(currencyToConvert);

        return key.toString();
    }

    private CachedConversionRate refreshCachedRate(String prefCurrency,
                                                  String currencyToConvert
    ) {
        CachedConversionRate cachedRate = new CachedConversionRate();

        String left, right;
        if (prefCurrency.compareTo(currencyToConvert) > 0) {
            left = currencyToConvert;
            right = prefCurrency;
        }
        else {
            left = prefCurrency;
            right = currencyToConvert;
        }

        cachedRate.refresh(apiHandler.getConversionRate(left, right));
        cache.put(buildCacheKey(prefCurrency, currencyToConvert), cachedRate);

        return cachedRate;
    }

    private double getConversionRate(String prefCurrency,
                                     String currencyToConvert
    ) {
        CachedConversionRate cachedRate =
                cache.get(buildCacheKey(prefCurrency, currencyToConvert));

        if (cachedRate != null) {
            if (cachedRate.hasExpired()) {
                cachedRate = refreshCachedRate(prefCurrency, currencyToConvert);
                System.out.println("The cached rate has expired");
            }
        }
        else {
            cachedRate = refreshCachedRate(prefCurrency, currencyToConvert);
        }

        // Choose rate according to lexicographic order,
        // since the key of each cached currency pair are stored A-Z
        // If prefCurrency = SEK and currencyToConvert = USD,
        // it should convert USD to SEK, which in this case means
        // it should use rateRightLeft, since the key is "SEK-USD".
        if (prefCurrency.compareTo(currencyToConvert) > 0)
            return cachedRate.rateLeftRight;
        else
            return cachedRate.rateRightLeft;

    }

    CurrencyResponse convertCurrency(String prefCurrency, String currencyToConvert, double amount) {
        CurrencyResponse res = new CurrencyResponse();
        res.prefCurrency = prefCurrency;
        res.currencyConverted = currencyToConvert;

        double conversionRate = getConversionRate(prefCurrency, currencyToConvert);
        res.returnAmount = amount * conversionRate;

        return res;
    }

}

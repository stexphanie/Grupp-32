package da358a.grupp32;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.Date;
import java.util.Hashtable;
import java.util.Optional;

/**
 * Stores and manages currency conversion rates, as well as provides
 * a method to easily convert from one conversion to another.
 * The number of API calls needed has been minimized by automatically
 * caching conversion rates for both ways when converting one.
 * Each cached entry may be used for up to 24 hours before it makes
 * another API call in order replace that entry with the update the rates.
 * That updated rate will in turn be valid for 24 hours, and so on.
 * These rates are also written to disk when the server application
 * closes. They are (obviously) loaded at start up, if the file exists.
 * @author Kasper S. Skott
 */
class CurrencyCache {

    /**
     * Internal class used to store conversion rate data
     * between two currencies.
     */
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
    private Gson gson;

    CurrencyCache(Gson gsonInstance) throws IOException {
        apiHandler = new CurrencyAPIHandler(gsonInstance);
        gson = gsonInstance;

        if(!loadLocalCache())
            cache = new Hashtable<>(64);
    }

    /**
     * Reads and loads the locally stored cache file into memory.
     * @return false if a local cache file wasn't found or
     * if there was a problem reading it
     */
    private boolean loadLocalCache() {
        String storedCache;
        try {
            storedCache = Files.readString(
                    Path.of("currency_cache.json"), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return false;
        }

        Type type = new TypeToken<Hashtable<String,
                CachedConversionRate>>() { }.getType();
        cache = gson.fromJson(storedCache, type);

        if (cache == null) {
            System.err.println(
                  "There was a problem with reading \"currency_cache.json\"");
            return false;
        }

        return true;
    }

    /**
     * Builds a suitable key from two currencies.
     * @param prefCurrency the currency to convert into
     * @param currencyToConvert what currency the amount in specified in
     * @return a key used for the Hashtable
     */
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

    /**
     * Updates the internal conversion data in the cache through
     * a call to the currency API. If, for some reason, a new rate
     * could be not be fetched, it uses the old one, and will try
     * to refresh at the next call.
     * @param prefCurrency the currency to convert into
     * @param currencyToConvert what currency the amount in specified in
     * @return a new and updated CachedConversionRate object
     */
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

        Optional<Double> rate = apiHandler.getConversionRate(left, right);
        if (rate.isPresent()) { // Use the new rate and cache it
            cachedRate.refresh(rate.get());
            cache.put(buildCacheKey(prefCurrency, currencyToConvert), cachedRate);
        }
        else { // Use the already cached rate and don't refresh it
            cachedRate = cache.get(buildCacheKey(prefCurrency, currencyToConvert));
        }

        return cachedRate;
    }

    /**
     * Gets the rate needed to convert currencyToConvert into prefCurrency.
     * May or may not update the cached currency combination internally,
     * depending on when it expires.
     * @param prefCurrency the currency to convert into
     * @param currencyToConvert what currency the amount in specified in
     * @return the factor needed in order to convert
     */
    private double getConversionRate(String prefCurrency,
                                     String currencyToConvert
    ) {
        CachedConversionRate cachedRate =
                cache.get(buildCacheKey(prefCurrency, currencyToConvert));

        if (cachedRate != null) {
            if (cachedRate.hasExpired()) {
                cachedRate = refreshCachedRate(prefCurrency, currencyToConvert);
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

    /**
     * Fetches currency conversion data, and converts the given amount from
     * currencyToConvert into prefCurrency.
     * @param prefCurrency the currency to convert into
     * @param currencyToConvert what currency the amount is specified in
     * @param amount the amount in currencyToConvert
     * @return a CurrencyResponse object, which can be serialized into JSON
     */
    CurrencyResponse convertCurrency(String prefCurrency,
                                     String currencyToConvert, double amount) {
        CurrencyResponse res = new CurrencyResponse();
        res.prefCurrency = prefCurrency;
        res.originalCurrency = currencyToConvert;

        double conversionRate = getConversionRate(prefCurrency, currencyToConvert);
        res.convertedAmount = amount * conversionRate;

        return res;
    }

    /**
     * Saves the current state of the currency cache to disk.
     * The cache is stored as JSON in the file "currency_cache.json"
     */
    void saveCacheToDisk() {
        if (cache.isEmpty())
            return;

        String cacheString = gson.toJson(cache);

        try {
            Files.writeString(Path.of("currency_cache.json"),
                    cacheString,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.CREATE);
        } catch (IOException e) {
            System.err.println(
                    "There was a problem with writing to \"currency_cache.json\"");
        }
    }

}

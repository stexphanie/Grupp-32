package da358a.grupp32;

import java.util.List;

class CurrencyResponse {
    String prefCurrency;
    double convertedAmount;
    String originalCurrency;
    double originalAmount;
    Boolean bmiDataAvailable;
    Double bmiAdjustedAmount;
    String bmiAdjustedAmountCountry;
    String bmiLatestUpdate;
    List<BMIAdjustedCityPrice> cityComparison;
}

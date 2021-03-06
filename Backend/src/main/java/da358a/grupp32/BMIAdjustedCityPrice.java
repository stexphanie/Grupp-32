package da358a.grupp32;

class BMIAdjustedCityPrice {
    String city;
    String country;
    double prefCurrencyAmount;
    double dollarAmount;



    @Override
    public String toString() {
        return "BMIAdjustedCityPrice{" +
                "city='" + city + '\'' +
                ", country='" + country + '\'' +
                ", prefCurrencyAmount=" + prefCurrencyAmount +
                ", dollarAmount=" + dollarAmount +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        BMIAdjustedCityPrice other = (BMIAdjustedCityPrice) obj;
        return this.country.equals(other.country);
    }
}

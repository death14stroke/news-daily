package com.andruid.magic.newsdaily.eventbus;

public class CountryEvent {
    private String countryCode;

    public CountryEvent(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }
}
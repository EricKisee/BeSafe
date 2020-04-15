package com.wizak.apps.besafe.models;

public class CountrySummary {
//    this class will be for the country summary json object being fetched from the api
//    the data being fetched will look like this
//   {
//   "Global":{
//      "NewConfirmed":81052,
//      "TotalConfirmed":1425371,
//      "NewDeaths":7299,
//      "TotalDeaths":81850,
//      "NewRecovered":23539,
//      "TotalRecovered":299435
//      },
//    "Countries":[
//      {
//          "Country":"ALA Aland Islands",
//          "CountryCode":"AX",
//          "Slug":"ala-aland-islands",
//          "NewConfirmed":0,
//          "TotalConfirmed":0,
//          "NewDeaths":0,
//          "TotalDeaths":0,
//          "NewRecovered":0,
//          "TotalRecovered":0,
//          "Date":"2020-04-08T01:44:14Z"
//      }, {....}
//  ]

    private String Country, CountryCode, Slug, NewConfirmed, TotalConfirmed,
            NewDeaths, TotalDeaths, NewRecovered, TotalRecovered;

    public CountrySummary (){
//
    }

    public CountrySummary(String Country, String CountryCode,String Slug, String NewConfirmed,
                          String TotalConfirmed, String NewDeaths, String TotalDeaths,
                          String NewRecovered, String TotalRecovered) {
        this.Country = Country;
        this.CountryCode = CountryCode;
        this.Slug = Slug;
        this.NewConfirmed = NewConfirmed;
        this.TotalConfirmed = TotalConfirmed;
        this.NewDeaths = NewDeaths;
        this.TotalDeaths = TotalDeaths;
        this.NewRecovered = NewRecovered;
        this.TotalRecovered = TotalRecovered;
    }

    public String getCountryCode() {
        return CountryCode;
    }



    public String getCountry() {
        return Country;
    }

    public String getSlug() {
        return Slug;
    }

    public String getNewConfirmed() {
        return NewConfirmed;
    }

    public String getTotalConfirmed() {
        return TotalConfirmed;
    }

    public String getNewDeaths() {
        return NewDeaths;
    }

    public String getTotalDeaths() {
        return TotalDeaths;
    }

    public String getNewRecovered() {
        return NewRecovered;
    }

    public String getTotalRecovered() {
        return TotalRecovered;
    }

    public void setCountry(String country) {
        this.Country = country;
    }

    public void setCountryCode(String countryCode) {
        this.CountryCode = countryCode;
    }

    public void setSlug(String slug) {
        this.Slug = slug;
    }

    public void setNewConfirmed(String newConfirmed) {
        this.NewConfirmed = newConfirmed;
    }

    public void setTotalConfirmed(String totalConfirmed) {
        this.TotalConfirmed = totalConfirmed;
    }

    public void setNewDeaths(String newDeaths) {
        this.NewDeaths = newDeaths;
    }

    public void setTotalDeaths(String totalDeaths) {
        this.TotalDeaths = totalDeaths;
    }

    public void setNewRecovered(String newRecovered) {
        this.NewRecovered = newRecovered;
    }

    public void setTotalRecovered(String totalRecovered) {
        this.TotalRecovered = totalRecovered;
    }
}

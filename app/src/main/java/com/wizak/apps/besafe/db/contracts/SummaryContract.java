package com.wizak.apps.besafe.db.contracts;

import android.provider.BaseColumns;

public final class SummaryContract {

    //private String Country, Slug, NewConfirmed, TotalConfirmed,
    //            NewDeaths, TotalDeaths, NewRecovered, TotalRecovered;

    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private SummaryContract (){}

    /* Inner class that defines the table contents */
    public static class SummaryEntry implements BaseColumns {
        public static final String TABLE_NAME = "Summary";
        public static final String COLUMN_NAME_COUNTY = "Country";
        public static final String COLUMN_NAME_CODE = "CountryCode";
        public static final String COLUMN_NAME_SLUG = "Slug";
        public static final String COLUMN_NAME_NEW_CONFIRMED = "NewConfirmed";
        public static final String COLUMN_NAME_TOTAL_CONFIRMED = "TotalConfirmed";
        public static final String COLUMN_NAME_NEW_DEATHS = "NewDeaths";
        public static final String COLUMN_NAME_TOTAL_DEATHS = "TotalDeaths";
        public static final String COLUMN_NAME_NEW_RECOVERED = "NewRecovered";
        public static final String COLUMN_NAME_TOTAL_RECOVERED = "TotalRecovered";
    }

}

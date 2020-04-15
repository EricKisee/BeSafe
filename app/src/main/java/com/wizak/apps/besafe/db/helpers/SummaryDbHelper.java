package com.wizak.apps.besafe.db.helpers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.wizak.apps.besafe.db.contracts.SummaryContract;

public class SummaryDbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASENAME = "Summary.db";

    public SummaryDbHelper(@Nullable Context context){
        super(context, DATABASENAME,null,DATABASE_VERSION);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        super.onDowngrade(db, oldVersion, newVersion);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_ENTRIES = "CREATE TABLE " + SummaryContract.SummaryEntry.TABLE_NAME  +
                "(" + SummaryContract.SummaryEntry._ID + " INTEGER PRIMARY KEY, " +
                SummaryContract.SummaryEntry.COLUMN_NAME_COUNTY + " TEXT, " +
                SummaryContract.SummaryEntry.COLUMN_NAME_CODE + " TEXT, " +
                SummaryContract.SummaryEntry.COLUMN_NAME_SLUG + " TEXT, "+
                SummaryContract.SummaryEntry.COLUMN_NAME_NEW_CONFIRMED + " TEXT, " +
                SummaryContract.SummaryEntry.COLUMN_NAME_TOTAL_CONFIRMED + " TEXT, " +
                SummaryContract.SummaryEntry.COLUMN_NAME_NEW_DEATHS + " TEXT, " +
                SummaryContract.SummaryEntry.COLUMN_NAME_TOTAL_DEATHS + " TEXT, " +
                SummaryContract.SummaryEntry.COLUMN_NAME_NEW_RECOVERED + " TEXT, " +
                SummaryContract.SummaryEntry.COLUMN_NAME_TOTAL_RECOVERED + " TEXT) "  ;
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
// This database is only a cache for online data, so its upgrade policy is
// to simply to discard the data and start over
        String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + SummaryContract.SummaryEntry.TABLE_NAME ;
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

}

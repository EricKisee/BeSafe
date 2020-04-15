package com.wizak.apps.besafe.ui.summary;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.wizak.apps.besafe.Common;
import com.wizak.apps.besafe.R;
import com.wizak.apps.besafe.adapters.CountryAdapter;
import com.wizak.apps.besafe.db.contracts.SummaryContract;
import com.wizak.apps.besafe.db.helpers.SummaryDbHelper;
import com.wizak.apps.besafe.models.CountrySummary;
import com.wizak.apps.besafe.ui.country.CountryFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

public class SummaryFragment extends Fragment implements CountryAdapter.OnCountryClickListener {

    private static final String PREFERENCE_KEY_DATE = "PREFERENCE_KEY_DATE";
    private static final String PREFERENCE_DEFAULT = "PREFERENCE_DEFAULT";
    private static final String TAG = "Summary Fragment";
    private static final String API_URL = "https://api.covid19api.com/summary";
    private boolean firstTime = false;

    private SummaryDbHelper dbHelper;
    private RecyclerView recyclerView;
    private CountryAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private ProgressBar progressBar;

    private SummaryViewModel summaryViewModel;

    private List <CountrySummary> countrySummaryList;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
//      to access the searchView.
        setHasOptionsMenu(true);
//        instantiating the database helper to be used to access the sqlite db
        dbHelper = new SummaryDbHelper(getContext());
        summaryViewModel =
                ViewModelProviders.of(this).get(SummaryViewModel.class);
        View root = inflater.inflate(R.layout.fragment_summary, container, false);

        recyclerView = root.findViewById(R.id.recyclerViewCountries);
        progressBar = root.findViewById(R.id.progressBar);

//         this setting is to improve performance if you know that changes
//         in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);

//         use a linear layout manager
        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(),LinearLayoutManager.VERTICAL));

//        check last update of local data
        String lastUpdateDate = readSharedPreference(PREFERENCE_KEY_DATE,PREFERENCE_DEFAULT);

//        check if the last update of the values was not done today
//        or if there is no update at all and update if either is true
        if (!lastUpdateDate.equals(getDate())||lastUpdateDate.equals(PREFERENCE_DEFAULT)){
            if (lastUpdateDate.equals(PREFERENCE_DEFAULT)) {
                firstTime = true;
            }
//            check if there is internet connection
            if (new Common().isNetworkAvailable(getContext())){
//            update local data with new data
//            get json from the Coronavirus COVID19 API
                getJson(API_URL);
            }else{
//                no internet connection
                // find the CoordinatorLayout id
                View contextView = getActivity().findViewById(android.R.id.content);
                // Make and display Snackbar
                Snackbar.make(contextView, "No internet connection available. Unable to update.", Snackbar.LENGTH_SHORT)
                        .show();
                progressBar.setVisibility(View.GONE);

            }
        }

        init();


        return root;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) item.getActionView();
        searchView.setQueryHint("Country");
        Log.d(TAG, "onCreateOptionsMenu: options created");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mAdapter.getFilter().filter(query);
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                mAdapter.getFilter().filter(newText);
                return false;
            }
        });
        searchView.setOnClickListener(new View.OnClickListener() {
                                          @Override
                                          public void onClick(View v) {

                                          }
                                      }
        );

        super.onCreateOptionsMenu(menu, inflater);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_search) {
            return true;
        } else if (id == R.id.action_settings)
            Log.d(TAG, "onOptionsItemSelected: settings  clicked");

        return super.onOptionsItemSelected(item);
    }

    private void init (){
        countrySummaryList = new Database().summaries();
//         specify an adapter
        mAdapter = new CountryAdapter(countrySummaryList,this);
        recyclerView.setAdapter(mAdapter);
        progressBar.setVisibility(View.GONE);

    }

    @Override
    public void OnCountryClickListener(int position) {
        String countryCode = mAdapter.getCountrySummaryListFiltered().get(position).getCountryCode();
        launchCountryFragment(countryCode);
    }

    private void launchCountryFragment (String countryCode){
        CountryFragment nextFrag= new CountryFragment();
        getActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_summary, nextFrag, countryCode)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onDestroy() {
//        closing database
        dbHelper.close();
        super.onDestroy();
    }

    private void saveData(String json) throws JSONException {
//        first check for the last update date. if there is no last update date  save the data. else update the data.

        JSONObject jsonObject = new JSONObject(json);
        JSONArray jsonArray = jsonObject.getJSONArray("Countries");

        Log.d(TAG, "loadList: size of countries" + jsonArray.length());

        for (int i = 0 ; i <jsonArray.length() ; i++){
            JSONObject country = jsonArray.getJSONObject(i);
            Gson gson = new Gson();
            CountrySummary countrySummary = gson.fromJson(String.valueOf(country),CountrySummary.class);
            if (firstTime) {
                new Database().insert(countrySummary);
            }else {
                new Database().update(countrySummary);
            }
        }

        writeSharedPreference(PREFERENCE_KEY_DATE,getDate());

    }

    private void writeSharedPreference(String key, String value){
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(key, value);
        editor.apply();
    }

    private String readSharedPreference (String key , String defaultValue){
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        return sharedPref.getString(key, defaultValue);
    }

    private String getDate (){
        return new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
    }




    private class Database {

        private SQLiteDatabase db = dbHelper.getWritableDatabase();

        public boolean insert (CountrySummary summary){

            ContentValues values = new ContentValues();
            values.put(SummaryContract.SummaryEntry.COLUMN_NAME_COUNTY,summary.getCountry());
            values.put(SummaryContract.SummaryEntry.COLUMN_NAME_CODE,summary.getCountryCode());
            values.put(SummaryContract.SummaryEntry.COLUMN_NAME_SLUG,summary.getSlug());
            values.put(SummaryContract.SummaryEntry.COLUMN_NAME_NEW_CONFIRMED,summary.getNewConfirmed());
            values.put(SummaryContract.SummaryEntry.COLUMN_NAME_NEW_DEATHS,summary.getNewDeaths());
            values.put(SummaryContract.SummaryEntry.COLUMN_NAME_NEW_RECOVERED,summary.getNewRecovered());
            values.put(SummaryContract.SummaryEntry.COLUMN_NAME_TOTAL_CONFIRMED,summary.getTotalConfirmed());
            values.put(SummaryContract.SummaryEntry.COLUMN_NAME_TOTAL_DEATHS,summary.getTotalDeaths());
            values.put(SummaryContract.SummaryEntry.COLUMN_NAME_TOTAL_RECOVERED,summary.getTotalRecovered());

            long newRowId = db.insert(SummaryContract.SummaryEntry.TABLE_NAME, null, values);
            if (newRowId==-1)
                return false;
            return true;
        }

        public boolean update (CountrySummary summary){
            ContentValues values = new ContentValues();
            values.put(SummaryContract.SummaryEntry.COLUMN_NAME_COUNTY,summary.getCountry());
            values.put(SummaryContract.SummaryEntry.COLUMN_NAME_CODE,summary.getCountryCode());
            values.put(SummaryContract.SummaryEntry.COLUMN_NAME_SLUG,summary.getSlug());
            values.put(SummaryContract.SummaryEntry.COLUMN_NAME_NEW_CONFIRMED,summary.getNewConfirmed());
            values.put(SummaryContract.SummaryEntry.COLUMN_NAME_NEW_DEATHS,summary.getNewDeaths());
            values.put(SummaryContract.SummaryEntry.COLUMN_NAME_NEW_RECOVERED,summary.getNewRecovered());
            values.put(SummaryContract.SummaryEntry.COLUMN_NAME_TOTAL_CONFIRMED,summary.getTotalConfirmed());
            values.put(SummaryContract.SummaryEntry.COLUMN_NAME_TOTAL_DEATHS,summary.getTotalDeaths());
            values.put(SummaryContract.SummaryEntry.COLUMN_NAME_TOTAL_RECOVERED,summary.getTotalRecovered());

            // Which row to update, based on the title
            String selection = SummaryContract.SummaryEntry.COLUMN_NAME_CODE + " LIKE '" + summary.getCountryCode()+"'";
            String[] selectionArgs = null;

            int count = db.update(
                    SummaryContract.SummaryEntry.TABLE_NAME,
                    values,
                    selection,
                    selectionArgs);
            if (count>0)
                return true;
            return false;
        }

        public boolean delete (int id){
            String selection = SummaryContract.SummaryEntry._ID + " LIKE " + id;
            String [] selectionArgs = null;
            int deletedRows = db.delete(SummaryContract.SummaryEntry.TABLE_NAME,selection,selectionArgs);
            if (deletedRows>1)
                return true;
            return false;
        }

        public List<CountrySummary> summaries (){
            String [] projection = {
                    SummaryContract.SummaryEntry.COLUMN_NAME_COUNTY,
                    SummaryContract.SummaryEntry.COLUMN_NAME_CODE,SummaryContract.SummaryEntry.COLUMN_NAME_SLUG,
                    SummaryContract.SummaryEntry.COLUMN_NAME_NEW_CONFIRMED, SummaryContract.SummaryEntry.COLUMN_NAME_TOTAL_CONFIRMED,
                    SummaryContract.SummaryEntry.COLUMN_NAME_NEW_DEATHS, SummaryContract.SummaryEntry.COLUMN_NAME_TOTAL_DEATHS,
                    SummaryContract.SummaryEntry.COLUMN_NAME_NEW_RECOVERED,  SummaryContract.SummaryEntry.COLUMN_NAME_TOTAL_RECOVERED
            };
            String selection = null;
            String [] selectionArgs = null;
            String sortOrder = null;
            Cursor cursor = db.query(
                SummaryContract.SummaryEntry.TABLE_NAME,         // The table to query
                    projection,                                  // The array of columns to return (pass null to get all)
                    selection,                                   // The columns for the WHERE clause
                selectionArgs,                                   // The values for the WHERE clause
                null,                                   // don't group the rows
                    null,                                // don't filter by row groups
                    sortOrder                                    // The sort order
            );

            int iCountry, iCountryCode, iSlug, iNewConfirmed, iTotalConfirmed, iNewDeaths,
                    iTotalDeaths, iNewRecovered, iTotalRecovered;
            iCountry = cursor.getColumnIndex(SummaryContract.SummaryEntry.COLUMN_NAME_COUNTY);
            iCountryCode = cursor.getColumnIndex(SummaryContract.SummaryEntry.COLUMN_NAME_CODE);
            iSlug = cursor.getColumnIndex(SummaryContract.SummaryEntry.COLUMN_NAME_SLUG);
            iNewConfirmed = cursor.getColumnIndex(SummaryContract.SummaryEntry.COLUMN_NAME_NEW_CONFIRMED);
            iTotalConfirmed = cursor.getColumnIndex( SummaryContract.SummaryEntry.COLUMN_NAME_TOTAL_CONFIRMED);
            iNewDeaths = cursor.getColumnIndex(  SummaryContract.SummaryEntry.COLUMN_NAME_NEW_DEATHS);
            iTotalDeaths = cursor.getColumnIndex( SummaryContract.SummaryEntry.COLUMN_NAME_TOTAL_DEATHS);
            iNewRecovered = cursor.getColumnIndex( SummaryContract.SummaryEntry.COLUMN_NAME_NEW_RECOVERED);
            iTotalRecovered = cursor.getColumnIndex( SummaryContract.SummaryEntry.COLUMN_NAME_TOTAL_RECOVERED);

            List <CountrySummary> countrySummaries = new ArrayList<>();
            for (cursor.moveToFirst();!cursor.isAfterLast();cursor.moveToNext()){
                CountrySummary summary = new CountrySummary();
                summary.setCountry(cursor.getString(iCountry));
                summary.setCountryCode(cursor.getString(iCountryCode));
                summary.setSlug(cursor.getString(iSlug));
                summary.setNewConfirmed(cursor.getString(iNewConfirmed));
                summary.setTotalConfirmed(cursor.getString(iTotalConfirmed));
                summary.setNewDeaths(cursor.getString(iNewDeaths));
                summary.setTotalDeaths(cursor.getString(iTotalDeaths));
                summary.setNewRecovered(cursor.getString(iNewRecovered));
                summary.setTotalRecovered(cursor.getString(iTotalRecovered));
                countrySummaries.add(summary);
            }
            return countrySummaries;
        }

    }

    private void getJson(final String apiUrl) {

        class GetJSON extends AsyncTask <Void , Void , String>{

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                Log.d(TAG, "onPostExecute: result string : "+s);
                try {
                    saveData(s);
                    init();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            protected void onProgressUpdate(Void... values) {
                super.onProgressUpdate(values);
            }

            @Override
            protected void onCancelled(String s) {
                super.onCancelled(s);
            }

            @Override
            protected void onCancelled() {
                super.onCancelled();
            }

            @Override
            protected String doInBackground(Void... voids) {
                try {
                    Log.d(TAG, "doInBackground: connecting to the internet and fetching string");
                    URL url = new URL(apiUrl);
                    HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
                    StringBuilder stringBuilder = new StringBuilder();
                    BufferedReader bufferedReader =
                            new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String json;
                    while ((json = bufferedReader.readLine())!=null){
                        stringBuilder.append(json+"\n");
                    }
                    return stringBuilder.toString().trim();
                }catch (Exception e) {
                    Log.d(TAG, "doInBackground: failed. error : " +e.getMessage());
                    return null;
                }
            }
        }

        GetJSON getJSON = new GetJSON();
        getJSON.execute();
    }

}

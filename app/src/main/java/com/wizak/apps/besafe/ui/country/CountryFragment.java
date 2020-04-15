package com.wizak.apps.besafe.ui.country;

import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.wizak.apps.besafe.Common;
import com.wizak.apps.besafe.R;
import com.wizak.apps.besafe.adapters.SimpleCountryAdapter;
import com.wizak.apps.besafe.db.contracts.SummaryContract;
import com.wizak.apps.besafe.db.helpers.SummaryDbHelper;
import com.wizak.apps.besafe.models.CountrySummary;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

public class CountryFragment extends Fragment implements SimpleCountryAdapter.OnCountryClickListener {

    private CountryViewModel countryViewModel;

    private static final String TAG = "Country Fragment";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static  final int ERROR_DIALOG_REQUEST = 9001;
    private static  final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;

    private boolean mLocationPermissionGranted = false;

    private FusedLocationProviderClient mFusedLocationProviderClient ;
    private Location deviceLocation;
    private boolean hasBundles = true;
    private String bundleCountryCode = "KE";
    private List <CountrySummary> countrySummaryList;

    private SummaryDbHelper dbHelper;

    private TextView textViewNewConfirmed, textViewTotalConfirmed, textViewNewDeaths, textViewTotalDeaths,
            textViewNewRecovered, textViewTotalRecovered , textViewCountryName, textViewCountryCode, textViewSlug;

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;

    private SimpleCountryAdapter mAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
//      to access the searchView.
            setHasOptionsMenu(true);
//        instantiating the database helper to be used to access the sqlite db
        dbHelper = new SummaryDbHelper(getContext());
        countryViewModel =
                ViewModelProviders.of(this).get(CountryViewModel.class);
        View root = inflater.inflate(R.layout.fragment_country, container, false);
        textViewNewConfirmed = root.findViewById(R.id.textViewNewConfirmed);
        textViewTotalConfirmed = root.findViewById(R.id.textViewTotalConfirmed);
        textViewNewDeaths = root.findViewById(R.id.textViewNewDeaths);
        textViewTotalDeaths = root.findViewById(R.id.textViewTotalDeaths);
        textViewNewRecovered = root.findViewById(R.id.textViewNewRecovered);
        textViewTotalRecovered = root.findViewById(R.id.textViewTotalRecovered);
        textViewCountryName = root.findViewById(R.id.textViewCountryName);
        textViewCountryCode = root.findViewById(R.id.textViewCountryCode);
        textViewSlug = root.findViewById(R.id.textViewSlug);
        recyclerView = root.findViewById(R.id.recyclerViewCountries);
//         this setting is to improve performance if you know that changes
//         in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);
//         use a linear layout manager
        layoutManager = new LinearLayoutManager(getContext() , LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(),LinearLayoutManager.HORIZONTAL));


        if (hasBundles){
            countrySummaryList = new Database().summaries(bundleCountryCode);
            init();
        }
//      check for internet connectivity before getting the location
        if (new Common().isNetworkAvailable(getContext())){
//            check that play services are working
            if(isServicesOk()){
//                check if location permission is granted
                getLocationPermission();
                if (mLocationPermissionGranted){
//                    get the current location of the device and hence the country name
                    getDeviceLocation();

                    countrySummaryList = new Database().summaries(getLocationCountry());
                }else{
                    //can not get location. location permission not granted

                }
            }
        }else{
//            no internet connection. can get current location address
            // find the CoordinatorLayout id
            View contextView = getActivity().findViewById(android.R.id.content);
            // Make and display Snackbar
            Snackbar.make(contextView, "No internet connection available.", Snackbar.LENGTH_SHORT)
                    .show();

        }




        return root;
    }

    private void init (){
//        ensure the list is not empty before trying to initialize
        if (!countrySummaryList.isEmpty()){
            mAdapter = new SimpleCountryAdapter(countrySummaryList,this);
            recyclerView.setAdapter(mAdapter);
            CountrySummary countrySummary = countrySummaryList.get(0);
            showCountry(countrySummary);
        }
    }

    @Override
    public void OnCountryClickListener(int position) {
        CountrySummary country = mAdapter.getCountrySummaryListFiltered().get(position);
        showCountry(country);
    }

    private void showCountry(CountrySummary countrySummary){
        textViewNewConfirmed.setText(countrySummary.getNewConfirmed());
        textViewNewDeaths.setText(countrySummary.getNewDeaths());
        textViewNewRecovered.setText(countrySummary.getNewRecovered());
        textViewTotalConfirmed.setText(countrySummary.getTotalConfirmed());
        textViewTotalDeaths.setText(countrySummary.getTotalDeaths());
        textViewTotalRecovered.setText(countrySummary.getTotalRecovered());
        textViewCountryName.setText(countrySummary.getCountry());
        textViewCountryCode.setText(countrySummary.getCountryCode());
        textViewSlug.setText(countrySummary.getSlug());
    }


    private String getLocationCountry (){
        Toast.makeText(getContext(), "latlng " +deviceLocation.getLatitude()+ " "+ deviceLocation.getLongitude(), Toast.LENGTH_SHORT).show();
        String countryName = "";
        try {
            Geocoder gcd = new Geocoder(getContext(), Locale.getDefault());
            List<Address> addresses = gcd.getFromLocation(deviceLocation.getLatitude(),deviceLocation.getLongitude(), 1);

            if (addresses.size() > 0)
            {
                countryName=addresses.get(0).getCountryName();
            }
        }
        catch(Exception e){
            Toast.makeText(getContext(), "No Location Name Found", Toast.LENGTH_LONG).show();
        }
        return countryName;
    }

    private String countrySlug (String localCountry){
        return localCountry.toLowerCase().replace(" ","-");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode){
            case LOCATION_PERMISSION_REQUEST_CODE:
                if (grantResults.length>0){
                    for (int i = 0 ; i < grantResults.length ; i++){
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            mLocationPermissionGranted = false;
                            return;
                        }
                    }
                    mLocationPermissionGranted = true;
                    getDeviceLocation();
                    // safe to initialize map
                }
                break;
        }
    }

    @Override
    public void onDestroy() {
//        closing database
        dbHelper.close();
        super.onDestroy();
    }

    private void getDeviceLocation (){
        Log.d(TAG, "getDeviceLocation: getting the device current location");
        Log.d(TAG, "getDeviceLocation: mLocationPermissionGranted : " + mLocationPermissionGranted);

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
        try {
            if (mLocationPermissionGranted){
                Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()){
                            Log.d(TAG, "onComplete: location found");
                            deviceLocation = (Location) task.getResult();
                        }else{
                            Log.d(TAG, "onComplete: Task not successful. Location not found");
                            Log.d(TAG, "onComplete: Error "+ task.getException().getMessage());
                        }
                    }
                });
            }
        }catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation:  security exception",e.getCause() );
        }

    }

    private void getLocationStatus (){
        LocationManager lm = (LocationManager)getContext().getSystemService(getContext().LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {}

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {}


    }

    private void getLocationPermission(){
        String permissions [] = {FINE_LOCATION,COARSE_LOCATION};
        if ((ContextCompat.checkSelfPermission(getContext(),FINE_LOCATION)==PackageManager.PERMISSION_GRANTED)&&
                (ContextCompat.checkSelfPermission(getContext(),COARSE_LOCATION)==PackageManager.PERMISSION_GRANTED)){
            mLocationPermissionGranted = true;
        }else{
            ActivityCompat.requestPermissions(getActivity(),permissions,LOCATION_PERMISSION_REQUEST_CODE);
        }

    }

    private boolean isServicesOk (){
        Log.d(TAG, "isServicesOk: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getContext());
        if (available == ConnectionResult.SUCCESS){
            // google play services is available and working
            Log.d(TAG, "isServicesOk: Google play services is working");
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)){
//            a resolvable error occured
            Log.d(TAG, "isServicesOk: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(getActivity(), available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }else
            Toast.makeText(getContext(), "You can't make map requests", Toast.LENGTH_SHORT).show();
        return false;
    }

    public class Database {


        private SQLiteDatabase db = dbHelper.getWritableDatabase();

        public void Database (){}

        public List<CountrySummary> summaries (String county){
            String [] projection = {
                    SummaryContract.SummaryEntry.COLUMN_NAME_COUNTY,
                    SummaryContract.SummaryEntry.COLUMN_NAME_CODE,SummaryContract.SummaryEntry.COLUMN_NAME_SLUG,
                    SummaryContract.SummaryEntry.COLUMN_NAME_NEW_CONFIRMED, SummaryContract.SummaryEntry.COLUMN_NAME_TOTAL_CONFIRMED,
                    SummaryContract.SummaryEntry.COLUMN_NAME_NEW_DEATHS, SummaryContract.SummaryEntry.COLUMN_NAME_TOTAL_DEATHS,
                    SummaryContract.SummaryEntry.COLUMN_NAME_NEW_RECOVERED,  SummaryContract.SummaryEntry.COLUMN_NAME_TOTAL_RECOVERED
            };
            String selection = SummaryContract.SummaryEntry.COLUMN_NAME_CODE +" LIKE '%"+county+"%' OR " +
                    SummaryContract.SummaryEntry.COLUMN_NAME_COUNTY +" LIKE '%"+county+"%' ";
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


    private void updateUI (String json) throws JSONException{
        // show results in ui
        JSONObject jsonObject = new JSONObject(json);
        JSONArray jsonArray = jsonObject.getJSONArray("Countries");


    }

    private void getJson(final String apiUrl) {

        class GetJSON extends AsyncTask<Void , Void , String> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                Log.d(TAG, "onPostExecute: result string : "+s);
                try {
                    updateUI(s);
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

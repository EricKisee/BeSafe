package com.wizak.apps.besafe.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.wizak.apps.besafe.R;
import com.wizak.apps.besafe.models.CountrySummary;

import java.util.ArrayList;
import java.util.List;

public class CountryAdapter extends RecyclerView.Adapter <CountryAdapter.MyViewHolder> implements Filterable {


    private List <CountrySummary> countrySummaryList;
    private List <CountrySummary> countrySummaryListFiltered;
    private OnCountryClickListener mOnCountryClickListener;
    private static final String TAG = "Country Adapter";


    public List <CountrySummary> getCountrySummaryListFiltered(){
        return countrySummaryListFiltered;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        OnCountryClickListener listener;

        @Override
        public void onClick(View v) {
            listener.OnCountryClickListener(getAdapterPosition());
        }

        public TextView textViewCountyName,textViewCountyCode, textViewSlug, textViewConfirmed, textViewRecovered, textViewDeaths;

        public MyViewHolder(View view , OnCountryClickListener onCountryClickListener) {
            super(view);
            this.listener = onCountryClickListener;
            textViewCountyCode =  view.findViewById(R.id.textViewCountryCode);
            textViewCountyName =  view.findViewById(R.id.textViewCountryName);
            textViewSlug = view.findViewById(R.id.textViewSlug);
            textViewConfirmed = view.findViewById(R.id.textViewConfirmed);
            textViewRecovered = view.findViewById(R.id.textViewRecovered);
            textViewDeaths = view.findViewById(R.id.textViewDeaths);
            view.setOnClickListener(this);
        }
    }

    public interface OnCountryClickListener {
        void OnCountryClickListener(int position);
    }

    public CountryAdapter(List<CountrySummary> countrySummaryList , OnCountryClickListener listener) {
        this.mOnCountryClickListener = listener;
        this.countrySummaryList = countrySummaryList;
        this.countrySummaryListFiltered = countrySummaryList;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.country_list_item, parent, false);
        return new MyViewHolder(itemView,mOnCountryClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull CountryAdapter.MyViewHolder holder, int position) {
        CountrySummary countrySummary = countrySummaryListFiltered.get(position);
        holder.textViewCountyName.setText(countrySummary.getCountry());
        holder.textViewCountyCode.setText(countrySummary.getCountryCode());
        holder.textViewSlug.setText(countrySummary.getSlug());
        holder.textViewConfirmed.setText(countrySummary.getTotalConfirmed());
        holder.textViewRecovered.setText(countrySummary.getTotalRecovered());
        holder.textViewDeaths.setText(countrySummary.getTotalDeaths());

    }

    @Override
    public int getItemCount() {
        return countrySummaryListFiltered.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                Log.d(TAG, "performFiltering: characters "+ charSequence);
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    countrySummaryListFiltered = countrySummaryList;
                } else {
                    List<CountrySummary> filteredList = new ArrayList<>();
                    for (CountrySummary row : countrySummaryList) {
                        if (row.getCountry().toLowerCase().contains(charString.toLowerCase())
                                ||row.getSlug().toLowerCase().contains(charString.toLowerCase())
                                || row.getCountryCode().toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(row);
                        }
                    }

                    countrySummaryListFiltered = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = countrySummaryListFiltered;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                countrySummaryListFiltered = (ArrayList<CountrySummary>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

}

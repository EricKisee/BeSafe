package com.wizak.apps.besafe.adapters;

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

public class SimpleCountryAdapter  extends RecyclerView.Adapter <SimpleCountryAdapter.MyViewHolder> implements Filterable {

    private List <CountrySummary> countrySummaryList;
    private List <CountrySummary> countrySummaryListFiltered;
    private OnCountryClickListener mOnCountryClickListener;
    private static final String TAG = "SimpleCountryAdapter";

    public List <CountrySummary> getCountrySummaryListFiltered(){ return countrySummaryListFiltered;}

    public SimpleCountryAdapter (List<CountrySummary> countrySummaryList , OnCountryClickListener onCountryClickListener){
        this.mOnCountryClickListener = onCountryClickListener;
        this.countrySummaryList = this.countrySummaryListFiltered = countrySummaryList;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String constraintString = constraint.toString();
                if (constraintString.isEmpty()){
                    countrySummaryListFiltered = countrySummaryList;
                } else {
                    List <CountrySummary> filteredList = new ArrayList<>();
                    for (CountrySummary row : countrySummaryList){
                        if (row.getCountry().toLowerCase().contains(constraintString.toLowerCase())
                                ||row.getSlug().toLowerCase().contains(constraintString.toLowerCase())
                                || row.getCountryCode().toLowerCase().contains(constraintString.toLowerCase())) {
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
            protected void publishResults(CharSequence constraint, FilterResults results) {
                countrySummaryListFiltered = (ArrayList<CountrySummary>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.country_list_item2,parent,false);
        return new MyViewHolder(view, mOnCountryClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        CountrySummary countrySummary = countrySummaryListFiltered.get(position);
        holder.textViewCountryCode.setText(countrySummary.getCountryCode());
        holder.textViewCountry.setText(countrySummary.getCountry());
    }

    @Override
    public int getItemCount() {
        return countrySummaryListFiltered.size();
    }



    public interface OnCountryClickListener {
        void OnCountryClickListener(int position);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        public OnCountryClickListener onCountryClickListener;
        public TextView textViewCountryCode , textViewCountry;


        public MyViewHolder(@NonNull View view , OnCountryClickListener onCountryClickListener) {
            super(view);
            this.onCountryClickListener = onCountryClickListener;
            textViewCountry = view.findViewById(R.id.textViewCountry);
            textViewCountryCode = view.findViewById(R.id.textViewCountryCode);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onCountryClickListener.OnCountryClickListener(getAdapterPosition());
        }
    }
}

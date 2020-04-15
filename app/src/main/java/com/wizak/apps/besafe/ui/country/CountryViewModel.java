package com.wizak.apps.besafe.ui.country;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class CountryViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public CountryViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is Country fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
package com.andruid.magic.newsdaily.pref;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.annotation.Nullable;
import androidx.preference.DialogPreference;

import com.andruid.magic.newsdaily.R;

public class CountryPreference extends DialogPreference {
    private String country;

    public CountryPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public CountryPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CountryPreference(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public CountryPreference(Context context) {
        super(context);
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
        persistString(country);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    @Override
    protected void onSetInitialValue(@Nullable Object defaultValue) {
        setCountry((String) defaultValue);
    }

    @Override
    public int getDialogLayoutResource() {
        return R.layout.pref_dialog_country;
    }
}
package com.namnoit.zalomaps;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Locale;
import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Objects.requireNonNull(getSupportActionBar()).setElevation(0f);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment())
                .commit();
    }

    public static class SettingsFragment extends PreferenceFragmentCompat
            implements SharedPreferences.OnSharedPreferenceChangeListener {


        public SettingsFragment() {
            // Required empty public constructor
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferences, rootKey);
            Locale currentLocale;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                currentLocale = getResources().getConfiguration().getLocales().get(0);
            }
            else {
                currentLocale = getResources().getConfiguration().locale;
            }
            String locale = getPreferenceScreen().getSharedPreferences().getString("pref_language","en");
            if (!locale.equals(currentLocale.getLanguage())) setLocale(locale);
            Preference aboutPref = findPreference("about");
            final Context context = requireContext();
        final MaterialAlertDialogBuilder builder =
                new MaterialAlertDialogBuilder(context,R.style.MaterialDialogStyle)
                .setPositiveButton(R.string.ok,null);
            if (aboutPref != null) {
                aboutPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                    Context context = requireContext();
                    LayoutInflater inflater = LayoutInflater.from(context);
                    final View convertView = inflater.inflate(R.layout.layout_about_app,null);
                    builder.setView(convertView).show();
                        return false;
                    }
                });
            }
            Preference helpPref = findPreference("help");
            if (helpPref != null) {
                helpPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        return false;
                    }
                });
            }
        }


        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals("pref_language")){
                Locale currentLocale;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    currentLocale = getResources().getConfiguration().getLocales().get(0);
                }
                else {
                    currentLocale = getResources().getConfiguration().locale;
                }
                String locale = sharedPreferences.getString(key,"en");
                if (!locale.equals(currentLocale.getLanguage())){
                    setLocale(locale);
                }
                setLocale(locale);
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        }

        void setLocale(String localeName) {
            Locale myLocale = new Locale(localeName);
            Locale.setDefault(myLocale);
            Resources res = getResources();
            DisplayMetrics dm = res.getDisplayMetrics();
            Configuration conf = res.getConfiguration();
            conf.locale = myLocale;
            res.updateConfiguration(conf, dm);
            Intent refresh = new Intent(getContext(), ListActivity.class);
            refresh.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(refresh);
        }

    }


}

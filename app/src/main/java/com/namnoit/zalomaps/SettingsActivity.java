package com.namnoit.zalomaps;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

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

        }
    }
}

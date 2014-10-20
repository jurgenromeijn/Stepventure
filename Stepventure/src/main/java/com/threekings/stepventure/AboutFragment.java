package com.threekings.stepventure;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.threekings.stepventure.introduction.IntroductionActivity;

/**
 * Created by Zwooosh on 18-12-13.
 */
public class AboutFragment extends android.app.Fragment {

    TextView textViewVersionNumber;
    PackageInfo pInfo;
    double versionCode;
    String versionName;
    Button appInfoButton;

    // Settings
    private SharedPreferences settings;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Get SharedPreferences
        settings = getActivity().getSharedPreferences(MainActivity.ID_SETTINGS, Context.MODE_PRIVATE);

        try {
            pInfo = getActivity().getApplicationContext().getPackageManager().getPackageInfo(
                    "com.threekings.stepventure", PackageManager.GET_META_DATA);
            versionCode = pInfo.versionCode;
            versionName = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return this.getView();
        }

        View rootView = inflater.inflate(R.layout.fragment_about, container, false);

        textViewVersionNumber = (TextView) rootView.findViewById(R.id.version_number);

        if(versionCode != 0)
        {
            textViewVersionNumber.setText(getResources().getString(R.string.version) + versionName);
        }

        appInfoButton = (Button) rootView.findViewById(R.id.app_info);
        appInfoButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                enterIntroduction();
            }
        });

        return rootView;
    }

    /** Enter introduction */
    public void enterIntroduction() {
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(MainActivity.ID_SHOW_INTRODUCTION, true);
        editor.commit();

        Intent i = new Intent(getActivity(), IntroductionActivity.class);
        startActivity(i);
        getActivity().finish();
    }
}

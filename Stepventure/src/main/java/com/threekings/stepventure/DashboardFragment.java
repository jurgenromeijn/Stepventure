package com.threekings.stepventure;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.threekings.stepventure.pedometer.StepService;

public class DashboardFragment extends Fragment implements View.OnClickListener {

    // Tag
    public static final String TAG = "com.threekings.stepventure.DashboardFragment";

    // View elements
    private TextView stepCounter;
    private TextView adventureStatus;
    private ToggleButton adventureButton;

    // Service
    private boolean broadcastReceiverActive = false;

    // Settings
    private SharedPreferences settings;

    // Fragment
    public DashboardFragment(){}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "-- CREATE");
        settings = getActivity().getSharedPreferences(MainActivity.ID_SETTINGS, Context.MODE_PRIVATE);
        broadcastReceiverActive = settings.getBoolean(MainActivity.ID_BROADCAST_RECEIVER_ACTIVE, false);

        // Get view
        View rootView = inflater.inflate(R.layout.fragment_dashboard, container, false);

        // Get view items
        stepCounter = (TextView) rootView.findViewById(R.id.step_counter);
        adventureButton = (ToggleButton) rootView.findViewById(R.id.adventure_button);
        adventureStatus = (TextView) rootView.findViewById(R.id.adventure_status);

        // Onclicklisteners
        adventureButton.setOnClickListener(this);

        updateStepCount();
        setAdventureStatus(((MainActivity)getActivity()).isMyServiceRunning());

        return rootView;
    }

    /** Destroy Broadcast Receiver */
    private void createBroadcastReceiver() {
        if (((MainActivity)getActivity()).isMyServiceRunning() && broadcastReceiverActive == false){
            Log.i(TAG, "START Receiver");
            MainActivity.mBroadcaster.registerReceiver(
                    DashboardReceiver, new IntentFilter(StepService.ID_COMMUNICATION_INTENT_STEP));
            broadcastReceiverActive = true;
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean(MainActivity.ID_BROADCAST_RECEIVER_ACTIVE, true);
            editor.commit();
        }
    }

    /** Destroy Broadcast Receiver */
    private void destroyBroadcastReceiver() {
        if (((MainActivity)getActivity()).isMyServiceRunning() && broadcastReceiverActive == true){
            Log.i(TAG, "STOP Receiver");
            MainActivity.mBroadcaster.unregisterReceiver(DashboardReceiver);
            broadcastReceiverActive = false;
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean(MainActivity.ID_BROADCAST_RECEIVER_ACTIVE, false);
            editor.commit();
        }
    }

    /** Update Stepcount on new step */
    private BroadcastReceiver DashboardReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateStepCount();
        }
    };

    /** Update TextView with current Stepcount */
    public void updateStepCount() {
//        Log.i(TAG, "--- UPDATE STEPS");

        stepCounter.setText(Integer.toString(settings.getInt(MainActivity.ID_STEPCOUNT, 0)));
    }

    /** Update TextView with current StepStatus */
    public void setAdventureStatus(boolean active) {
        adventureButton.setChecked(active);
        if (active){
            adventureStatus.setText(getResources().getString(R.string.status_active));
        } else {
            adventureStatus.setText(getResources().getString(R.string.status_inactive));
        }

    }

    /** State methods */
    @Override
    public void onStart() {
        super.onStart();
        Log.i(TAG, "--- START");
        updateStepCount();
    }
    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "--- RESUME");
        createBroadcastReceiver();
        updateStepCount();
    }
    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG, "--- PAUSE");
        destroyBroadcastReceiver();
    }
    @Override
    public void onStop() {
        super.onStop();
        Log.i(TAG, "--- STOP");
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "--- DESTROY");
        destroyBroadcastReceiver();
    }

    /** OnClick handling */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.adventure_button:
                if (((MainActivity)getActivity()).isMyServiceRunning()){
                    setAdventureStatus(false);
                    ((MainActivity)getActivity()).stopPedometer();
                    destroyBroadcastReceiver();
                } else {
                    setAdventureStatus(true);
                    ((MainActivity)getActivity()).startPedometer();
                    createBroadcastReceiver();
                    updateStepCount();
                }
                break;
        }
    }
}

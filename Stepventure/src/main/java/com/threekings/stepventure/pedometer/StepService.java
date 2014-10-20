package com.threekings.stepventure.pedometer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Binder;
import android.app.Service;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.threekings.stepventure.MainActivity;
import com.threekings.stepventure.R;

public class StepService extends Service implements StepListener {

    // Tag
    public static final String TAG = "com.threekings.stepventure.pedometer.StepService";

    // Wakelock
    private PowerManager.WakeLock wakeLock;

    // Settings
    private SharedPreferences settings;
    public static final String ID_COMMUNICATION_INTENT_STEP = "communicationIntent";

    //Sensor related stuff
    private SensorManager sensorManager;
    private Sensor sensor;
    private StepDetector stepDetector;

    // Notivication related stuff
    private NotificationManager notificationManager;
    private NotificationCompat.Builder trackingNotificationBuilder;
    public static final int ID_TRACKING_NOTIFICATION_ID = 1;
    public static final int ID_FIGHT_NOTIFICATION_ID = 2;

    // StepCount
    private int stepCount = 0;
    private int getStepsTillNextFight() {
        return settings.getInt(MainActivity.ID_NEXTFIGHT, 200);
    }

    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    public class StepBinder extends Binder {
        public StepService getService() {
            return StepService.this;
        }
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "[SERVICE] onCreate");
        super.onCreate();

        // Set up service hooks
        acquireWakeLock();
        setupSettings();
        setupStepDetector();
        notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Create Notification
        Intent dashboardIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingDashboardIntent = PendingIntent.getActivity(this, 0, dashboardIntent, 0);
        trackingNotificationBuilder =
                new NotificationCompat.Builder(this)
                        .setWhen(0)
                        .setSmallIcon(R.drawable.ic_stat_brobot_head)
                        .setContentTitle(getResources().getString(R.string.app_name))
                        .setContentText(getResources().getString(R.string.tracking_started))
                        .setOngoing(true)
                        .setAutoCancel(false)
                        .setContentIntent(pendingDashboardIntent);
        startForeground(ID_TRACKING_NOTIFICATION_ID,trackingNotificationBuilder.build());
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "[SERVICE] onBind");
        return mBinder;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "[SERVICE] onDestroy");
        sensorManager.unregisterListener(stepDetector);
        wakeLock.release();
        stopForeground(true);
        super.onDestroy();
    }


    private void setupSettings() {
        settings  = getSharedPreferences(MainActivity.ID_SETTINGS, Context.MODE_PRIVATE);
        stepCount = settings.getInt(MainActivity.ID_STEPCOUNT, 0);

        Intent communicationIntent = new Intent(ID_COMMUNICATION_INTENT_STEP);
        communicationIntent.putExtra(MainActivity.ID_STEPCOUNT, stepCount);
        LocalBroadcastManager.getInstance(this).sendBroadcast(communicationIntent);
    }

    private void setupStepDetector() {
        stepDetector = new StepDetector();
        stepDetector.addStepListener(this);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(
                Sensor.TYPE_ACCELEROMETER /*|
            Sensor.TYPE_MAGNETIC_FIELD |
            Sensor.TYPE_ORIENTATION*/);
        sensorManager.registerListener(stepDetector,
                sensor,
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onStep() {
        int stepsTillNextFight = getStepsTillNextFight();
        stepCount = settings.getInt("stepCount", 0);
        stepCount++;
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(MainActivity.ID_STEPCOUNT, stepCount);
        if (stepsTillNextFight > 0) {
            stepsTillNextFight--;
            editor.putInt(MainActivity.ID_NEXTFIGHT, stepsTillNextFight);
        } else  {
            //Show notification and reset steps till next fight
            showFightNotification();
            editor.putInt(MainActivity.ID_NEXTFIGHT, calculateStepsTillNextFight(500, 1300));
        }
        editor.commit();

        Log.i(TAG, "[SERVICE] step " + stepCount);

        trackingNotificationBuilder.setContentText(getResources().getString(R.string.step_coins) + String.valueOf(stepCount));
        notificationManager.notify(ID_TRACKING_NOTIFICATION_ID, trackingNotificationBuilder.build());

        // Send message to activities
        Intent communicationIntent = new Intent(ID_COMMUNICATION_INTENT_STEP);
        communicationIntent.putExtra(MainActivity.ID_STEPCOUNT, stepCount);
        LocalBroadcastManager.getInstance(this).sendBroadcast(communicationIntent);

        // Show Fight notification
        if (stepsTillNextFight < 1) {
            showFightNotification();
        }
    }

    private void showFightNotification() {
        Intent fightIntent = new Intent(this, MainActivity.class);
        fightIntent.setAction("OPEN_BATTLES");
        PendingIntent pendingFightIntent = PendingIntent.getActivity(this, 0, fightIntent, 0);

        NotificationCompat.Builder fightNotificationBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_stat_battle_notification)
                        .setContentTitle(getResources().getString(R.string.new_fight_title))
                        .setContentText(getResources().getString(R.string.new_fight_text))
                        .setAutoCancel(true)
                        .setContentIntent(pendingFightIntent)
                        .setLights(getResources().getColor(R.color.actionbar_bg),0,0)
                        .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
        notificationManager.notify(ID_FIGHT_NOTIFICATION_ID, fightNotificationBuilder.build());
    }

    public int calculateStepsTillNextFight(int Min, int Max) {
        return Min + (int)(Math.random() * ((Max - Min) + 1));
    }

    @Override
    public void passValue() { }

    /**
     * Receives messages from activity.
     */
    private final IBinder mBinder = new StepBinder();

    private void acquireWakeLock() {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
        wakeLock.acquire();
    }
}
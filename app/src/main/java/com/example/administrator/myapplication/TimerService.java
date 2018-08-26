package com.example.administrator.myapplication;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;


public class TimerService extends Service {

    Integer smokingPeriod,
            smokingPeriodSec,
            siggaretsCounter,
            timeLeft;

    String smokingPeriodType;
    Boolean timerOn;

    CountDownTimer timer;
    Notification notification;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        LoadSettings();
        if (intent.getAction() == "START_ON_BOOT") {
            LaunchTimer(timeLeft);
        } else {
            LaunchTimer(smokingPeriodSec + (int)(siggaretsCounter * smokingPeriodSec * 0.01));
        }

        return super.onStartCommand(intent, flags, startId);
    }

    public void LaunchTimer(Integer sec) {
        timerOn = true;

        if (timer != null)
            timer.cancel();

        timer = new CountDownTimer(sec * 1000 + 100, 1000) {

            public void onTick(long millisUntilFinished) {
                timeLeft = (int)(millisUntilFinished / 1000);
                NotificationUpdate(timeLeft, false);
                SaveSettings();
                sendMessageToActivity();
            }

            public void onFinish() {
                timerOn = false;
                siggaretsCounter++;
                timeLeft = 0;
                NotificationUpdate(timeLeft, true);
                SaveSettings();
                sendMessageToActivity();
            }
        };
        timer.start();
    }

    void NotificationUpdate(int smokingPeriodSec, boolean addButton) {
        Intent intentMainActivity = new Intent(this, MainActivity.class);
        intentMainActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pIntentMainActivity = PendingIntent.getActivity(this,0,intentMainActivity,0);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this)
                        .setContentIntent(pIntentMainActivity)
                        .setSmallIcon(R.mipmap.baseline_smoke_free_black_48)
                        .setContentTitle("Time left for next smoking: ")
                        .setContentText(ConvertSecToString(smokingPeriodSec));
        if (addButton) {
            Intent intent = new Intent(this, TimerService.class);

            PendingIntent pIntent = PendingIntent.getService(this,0,intent,0);

            builder.addAction(R.mipmap.ic_launcher, "Smoking", pIntent);
            builder.setContentText(ConvertSecToString(0));
            builder.setSmallIcon(R.mipmap.baseline_smoking_rooms_black_48);
        }

        notification = builder.build();
        startForeground(1,notification);
    }


    String ConvertSecToString(Integer sec) {
        Integer hoursleft = sec  / 60 / 60;
        Integer minleft = sec  / 60 - hoursleft * 60;
        Integer secleft = (sec)  - (sec  / 60) * 60;
        return hoursleft + " h " +  minleft + " m " + secleft + " s";
    }

    void sendMessageToActivity() {
        Intent intent = new Intent("tick");
        intent.putExtra("timeLeft",timeLeft);
        intent.putExtra("siggaretsCounter",siggaretsCounter);
        intent.putExtra("smokingPeriodSec",smokingPeriodSec + (int)(siggaretsCounter * smokingPeriodSec * 0.01));
        intent.putExtra("smokingPeriodSecNext",smokingPeriodSec + (int)((siggaretsCounter + 1) * smokingPeriodSec * 0.01));
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }


    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy() {
        if (timer != null)
            timer.cancel();

        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(1);
        super.onDestroy();
    }

    void LoadSettings() {
        smokingPeriod = getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                .getInt("smokingPeriod", 0);

        smokingPeriodType = getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                .getString("smokingPeriodType", "");

        siggaretsCounter = getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                .getInt("siggaretsCounter", 0);

        smokingPeriodSec = getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                .getInt("smokingPeriodSec", 0);

        timerOn = getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                .getBoolean("timerOn", false);

        timeLeft = getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                .getInt("timeLeft", 0);
    }

    void SaveSettings() {
        getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit()
                .putInt("smokingPeriod", smokingPeriod).apply();

        getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit()
                .putString("smokingPeriodType", smokingPeriodType).apply();

        getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit()
                .putInt("siggaretsCounter", siggaretsCounter).apply();

        getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit()
                .putInt("smokingPeriodSec", smokingPeriodSec).apply();

        getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit()
                .putBoolean("timerOn", timerOn).apply();

        getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit()
                .putInt("timeLeft", timeLeft).apply();
    }
}

package com.example.administrator.myapplication;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class MainActivity extends Activity {

    Integer smokingPeriod,
            smokingPeriodSec,
            siggaretsCounter,
            smokingPeriodSecNext,
            timeLeft;

    String smokingPeriodType;

    TextView textViewTimeLeft,
            textViewCooldown,
            textViewCooldownUpdated,
            textViewDailyCounter,
            textViewDailyCounterUpdated,
            textViewSmokedTotal;

    Button buttonSettings,
            buttonSmoked;

    Boolean timerOn;

    final int REQUEST_CODE_1 = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("tick"));

        textViewTimeLeft = findViewById(R.id.textViewTimeLeft);
        textViewCooldown = findViewById(R.id.textViewCooldown);
        textViewCooldownUpdated = findViewById(R.id.textViewCooldownUpdated);
        textViewDailyCounter = findViewById(R.id.textViewDailyCounter);
        textViewDailyCounterUpdated = findViewById(R.id.textViewDailyCounterUpdated);
        textViewSmokedTotal = findViewById(R.id.textViewSmokedTotal);

        buttonSettings = findViewById(R.id.buttonSetSettings);
        buttonSmoked = findViewById(R.id.buttonSmoked);

        LoadSettings();

        textViewTimeLeft.setText("none");
        textViewCooldown.setText(ConvertSecToString(smokingPeriodSec));
        textViewCooldownUpdated.setText(ConvertSecToString(smokingPeriodSec));
        textViewSmokedTotal.setText(siggaretsCounter.toString());
        buttonSmoked.setText("Smoke");

        if (!smokingPeriod.equals(0)) {
            buttonSmoked.setVisibility(View.VISIBLE);
        } else {
            buttonSmoked.setVisibility(View.INVISIBLE);
        }

        ShowOneTimeLaunchActivity(true);

        buttonSettings.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, SettingActivity.class);
                startActivityForResult(i, REQUEST_CODE_1);

                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });

        buttonSmoked.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessageToService("Start");
            }
        });
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() == "tick") {
                timeLeft = intent.getIntExtra("timeLeft",0);
                siggaretsCounter = intent.getIntExtra("siggaretsCounter",0);
                smokingPeriodSec = intent.getIntExtra("smokingPeriodSec",0);
                smokingPeriodSecNext = intent.getIntExtra("smokingPeriodSecNext",0);

                int dailyCount = 57600 / ConvertPeriodToInt(smokingPeriod, smokingPeriodType);
                int dailyCountNext = 57600 / smokingPeriodSecNext;

                textViewTimeLeft.setText(ConvertSecToString(timeLeft));
                textViewCooldown.setText(ConvertSecToString(smokingPeriodSec));
                textViewCooldownUpdated.setText(ConvertSecToString(smokingPeriodSecNext));
                textViewDailyCounter.setText(String.valueOf(dailyCount));
                textViewDailyCounterUpdated.setText(String.valueOf(dailyCountNext));
                textViewSmokedTotal.setText(siggaretsCounter.toString());

                buttonSmoked.setText(timeLeft > 0 ? "Wait" : "Smoked");
            }
        }
    };

    void sendMessageToService(String message) {
        Intent intent = new Intent(this, TimerService.class);
        intent.setAction(message);
        startService(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_CODE_1:
                if (resultCode == RESULT_OK) {
                    String messageReturn[] = data.getStringArrayExtra("message_return");

                    smokingPeriod = Integer.parseInt(messageReturn[0]);
                    smokingPeriodType = messageReturn[1];
                    smokingPeriodSec = ConvertPeriodToInt(smokingPeriod, smokingPeriodType);
                    siggaretsCounter = 0;

                    stopService(new Intent(this, TimerService.class));
                    SaveSettings();

                    textViewTimeLeft.setText("none");
                    textViewCooldown.setText(ConvertSecToString(ConvertPeriodToInt(smokingPeriod,smokingPeriodType)));
                    textViewCooldownUpdated.setText(textViewCooldown.getText());
                    textViewSmokedTotal.setText(siggaretsCounter.toString());
                    buttonSmoked.setText("Smoke");

                    if (!smokingPeriod.equals(0)) {
                        buttonSmoked.setVisibility(View.VISIBLE);
                    } else {
                        buttonSmoked.setVisibility(View.INVISIBLE);
                    }
                }
        }
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
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

    void ShowOneTimeLaunchActivity(boolean usePreferences) {
        Boolean isFirstRun = getSharedPreferences("PREFERENCE", MODE_PRIVATE)
                .getBoolean("isFirstRun", true);

        if (isFirstRun || !usePreferences) {
            //show start activity
            startActivity(new Intent(MainActivity.this, OneTimeLaunchActivity.class));
        }

        getSharedPreferences("PREFERENCE", MODE_PRIVATE).edit()
                .putBoolean("isFirstRun", false).apply();
    }

    Integer ConvertPeriodToInt(Integer smokingPeriod, String smokingPeriodType) {
        Integer sec = 0;
        switch (smokingPeriodType){
            case "days": sec = smokingPeriod * 60 * 60 * 24;
                break;
            case "hours" : sec = smokingPeriod * 60 * 60;
                break;
            case "min" : sec = smokingPeriod * 60;
                break;
            case "sec" : sec = smokingPeriod;
        }
        return sec;
    }

    String ConvertSecToString(Integer millisec) {
        Integer hoursleft = millisec  / 60 / 60;
        Integer minleft = millisec  / 60 - hoursleft * 60;
        Integer secleft = (millisec)  - (millisec  / 60) * 60;
        return hoursleft + " h " +  minleft + " m " + secleft + " s";
    }
}

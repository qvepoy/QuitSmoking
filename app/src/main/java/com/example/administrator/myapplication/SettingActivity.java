package com.example.administrator.myapplication;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.regex.Pattern;

public class SettingActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        final Spinner dropdown = findViewById(R.id.spinner);
        String[] items = new String[]{"sec", "min", "hours", "days"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, items);
        dropdown.setAdapter(adapter);

        Button clickButton = findViewById(R.id.buttonSetSettings);
        clickButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText smokeOften = findViewById(R.id.editTextSmokeOften);

                if (!Pattern.compile( "[0-9]" ).matcher(smokeOften.getText()).find()) {
                    Context context = getApplicationContext();
                    CharSequence text = "Please enter number";
                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                    return;
                }

                String smokingPeriod = smokeOften.getText().toString();
                String smokingPeriodType = dropdown.getSelectedItem().toString();

                String message[] = {smokingPeriod,smokingPeriodType};

                Intent i = new Intent();
                i.putExtra("message_return", message);
                setResult(RESULT_OK, i);
                finish();

                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });

        final EditText editTextSmokeOften = findViewById(R.id.editTextSmokeOften);
        editTextSmokeOften.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editTextSmokeOften.setText("");
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Intent i = new Intent();
        setResult(RESULT_CANCELED, i);
        finish();

        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}

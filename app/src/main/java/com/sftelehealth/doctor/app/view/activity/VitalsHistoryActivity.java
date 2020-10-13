package com.sftelehealth.doctor.app.view.activity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;


import com.sftelehealth.doctor.R;
import com.sftelehealth.doctor.domain.model.Vitals;

public class VitalsHistoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vitals_history);

        TextView temperature = findViewById(R.id.temperature);
        TextView pulse = findViewById(R.id.pulse);
        TextView respiration = findViewById(R.id.respiration);
        TextView pressure = findViewById(R.id.pressure);
        TextView weight = findViewById(R.id.weight);
        TextView sugar = findViewById(R.id.sugar);
        TextView text = findViewById(R.id.text);
        ImageView back = findViewById(R.id.back);

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            Vitals vitals = (Vitals) extras.getSerializable("VITALS");

            if (vitals != null) {
                temperature.setText(vitals.getBloodTemperature());
                pulse.setText(vitals.getPulseRate());
                respiration.setText(vitals.getRespirationRate());
                pressure.setText(vitals.getBloodPressure());
                weight.setText(vitals.getBodyWeight());
                sugar.setText(vitals.getBloodSuger());
                text.setText(vitals.getVitalsDateTime());
            } else {
                finish();
            }
        } else {
            finish();
        }

        back.setOnClickListener(view -> finish());
    }
}
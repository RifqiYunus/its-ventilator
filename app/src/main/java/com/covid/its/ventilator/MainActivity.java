package com.covid.its.ventilator;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Button newPatient = findViewById(R.id.newPatient);
        Button neonate = findViewById(R.id.graphExample);
        Button previousPatient = findViewById(R.id.previousPatient);
        final Intent PREVIOUS_PATIENT_INTENT = new Intent(this, PreviousPatient.class);
        final Intent NEW_PATIENT_INTENT = new Intent(this, MainActivity.class);
        final Intent GRAPH_INTENT = new Intent(this, GraphExample.class);



        newPatient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(NEW_PATIENT_INTENT);
            }
        });

        neonate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(GRAPH_INTENT);
            }
        });

        previousPatient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(PREVIOUS_PATIENT_INTENT);
            }
        });

    }
}

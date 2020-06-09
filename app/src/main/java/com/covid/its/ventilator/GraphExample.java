package com.covid.its.ventilator;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;


public class GraphExample extends AppCompatActivity {

    LineGraphSeries<DataPoint> series;
    LineGraphSeries<DataPoint> series1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph_example);

        double y, x;
        x = 0;

        GraphView pressure = findViewById(R.id.graph);
        GraphView flow = findViewById(R.id.graph3);

        series = new LineGraphSeries<>();
        series1 = new LineGraphSeries<>();

        for (int i = 0; i < 100; i++) {
            x = x + 0.1;
            y = Math.sin(x);
            series.appendData(new DataPoint(x, y), true, 100);
        }

        for (int i = 0; i < 100; i++) {
            x = x + 0.1;
            y = Math.sin(x+1);
            series1.appendData(new DataPoint(x, y), true, 100);
        }
        flow.addSeries(series1);
        pressure.addSeries(series);
    }

}
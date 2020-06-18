package com.covid.its.ventilator;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;


public class GraphExample extends AppCompatActivity {

    private Handler mHandler = new Handler();
    LineGraphSeries<DataPoint> series;
    LineGraphSeries<DataPoint> series1;
    double x1 = 0;

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


        flow.getViewport().setMinX(0);
        flow.getViewport().setMaxX(100);
        flow.getViewport().setXAxisBoundsManual(true);
        flow.getViewport().setScrollable(true);

        pressure.getViewport().setMinX(0);
        pressure.getViewport().setMaxX(100);
        pressure.getViewport().setXAxisBoundsManual(true);
        pressure.getViewport().setScrollable(true);

        addRandomDataPoint();

        flow.addSeries(series1);
        pressure.addSeries(series);
    }

    private void addRandomDataPoint() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                x1 = x1 + 0.1;
                series1.appendData(new DataPoint(x1, Math.sin(x1)), true, 100);
                series.appendData(new DataPoint(x1, Math.sin(x1 + 5)), true, 100);
                addRandomDataPoint();
            }
        }, 10);
    }

}
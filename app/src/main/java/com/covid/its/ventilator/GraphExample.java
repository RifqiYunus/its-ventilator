package com.covid.its.ventilator;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.HashMap;
import java.util.Map;


public class GraphExample extends AppCompatActivity {

    public final String ACTION_USB_PERMISSION = "com.covid.its.ventilator.USB_PERMISSION";
    LineGraphSeries<DataPoint> series;
    LineGraphSeries<DataPoint> series1;
    Button startButton, stopButton;
    TextView pressureView, flowView;
    UsbManager usbManager;
    UsbDevice device;
    UsbSerialDevice serialPort;
    UsbDeviceConnection connection;
    private double X = 0;

    UsbSerialInterface.UsbReadCallback mCallback = new UsbSerialInterface.UsbReadCallback() { //Defining a Callback which triggers whenever data is read.
        @Override
        public void onReceivedData(byte[] arg0) {
            try {
                int parsedPressure = ((arg0[4] & 0xff) << 8) | (arg0[3] & 0xff);
                putData(parsedPressure, pressureView);
                int parsedFlow = ((arg0[8] & 0xff) << 8) | (arg0[7] & 0xff);
                putData(parsedFlow, flowView);
                graphUpdate(parsedFlow, parsedPressure);
            } catch (Exception e) {
                onClickStart(startButton);
            }


        }
    };

    void putData(float value, TextView textView) {
        String parsedText = Float.toString(value);
        tvPut(textView, parsedText);
    }

    void graphUpdate(float flow, float pressure) {
        X = X + 0.1;
        series.appendData(new DataPoint(X, pressure), true, 100);
        series1.appendData(new DataPoint(X, flow), true, 100);

    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() { //Broadcast Receiver to automatically start and stop the Serial connection.
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(ACTION_USB_PERMISSION)) {
                boolean granted = intent.getExtras().getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);
                if (granted) {
                    connection = usbManager.openDevice(device);
                    serialPort = UsbSerialDevice.createUsbSerialDevice(device, connection);
                    if (serialPort != null) {
                        if (serialPort.open()) { //Set Serial Connection Parameters.
                            setUiEnabled(true);
                            serialPort.setBaudRate(115200);
                            serialPort.setDataBits(UsbSerialInterface.DATA_BITS_8);
                            serialPort.setStopBits(UsbSerialInterface.STOP_BITS_1);
                            serialPort.setParity(UsbSerialInterface.PARITY_NONE);
                            serialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
                            serialPort.read(mCallback);

                        } else {
                            Log.d("SERIAL", "PORT NOT OPEN");
                        }
                    } else {
                        Log.d("SERIAL", "PORT IS NULL");
                    }
                } else {
                    Log.d("SERIAL", "PERM NOT GRANTED");
                }
            } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
                onClickStart(startButton);
            } else if (intent.getAction().equals(UsbManager.ACTION_USB_DEVICE_DETACHED)) {
                onClickStop(stopButton);

            }
        }

        ;
    };

    //    private Handler mHandler = new Handler();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph_example);
        usbManager = (UsbManager) getSystemService(this.USB_SERVICE);
        startButton = (Button) findViewById(R.id.buttonStart2);
        stopButton = (Button) findViewById(R.id.buttonStop2);
        pressureView = (TextView) findViewById(R.id.pressureView2);
        flowView = (TextView) findViewById(R.id.flowView2);

        GraphView pressure = findViewById(R.id.graph);
        GraphView flow = findViewById(R.id.graph3);

        series = new LineGraphSeries<>();
        series1 = new LineGraphSeries<>();
        series.appendData(new DataPoint(0, 0), true, 100);
        series1.appendData(new DataPoint(0, 0), true, 100);

        flow.getViewport().setMinX(0);
        flow.getViewport().setMaxX(10);
        flow.getViewport().setXAxisBoundsManual(true);
        flow.getViewport().setScrollable(true);
        flow.setTitle("Flow");
        flow.getGridLabelRenderer().setHorizontalAxisTitle("Time");
        flow.getGridLabelRenderer().setVerticalAxisTitle("Flow");

        pressure.getViewport().setMinX(0);
        pressure.getViewport().setMaxX(10);
        pressure.getViewport().setXAxisBoundsManual(true);
        pressure.getViewport().setScrollable(true);
        pressure.setTitle("Pressure");
        pressure.getGridLabelRenderer().setHorizontalAxisTitle("Time");
        pressure.getGridLabelRenderer().setVerticalAxisTitle("Pressure");

        flow.addSeries(series1);
        pressure.addSeries(series);

        setUiEnabled(false);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(broadcastReceiver, filter);
    }

    public void setUiEnabled(boolean bool) {
        startButton.setEnabled(!bool);
        stopButton.setEnabled(bool);
        flowView.setEnabled(bool);
        pressureView.setEnabled(bool);
    }

    @Override
    protected void onPause() {
        super.onPause();
        onClickStop(stopButton);
        setUiEnabled(false);
    }

    public void onClickStart(View view) {

        HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList();
        if (!usbDevices.isEmpty()) {
            boolean keep = true;
            for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()) {
                device = entry.getValue();
                int deviceVID = device.getVendorId();
                if (deviceVID == 0x2341 || deviceVID == 6790)//Arduino Vendor ID or CH340
                {
                    PendingIntent pi = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
                    usbManager.requestPermission(device, pi);
                    keep = false;
                    tvPut(flowView, "00");
                    tvPut(pressureView, "00");
                } else {
                    connection = null;
                    device = null;
                }

                if (!keep)
                    break;
            }
        }


    }

    public void onClickStop(View view) {
        setUiEnabled(false);
        serialPort.close();
    }

    private void tvPut(TextView tv, CharSequence text) {
        final TextView ftv = tv;
        final CharSequence ftext = text;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ftv.setText(ftext);
            }
        });
    }
}
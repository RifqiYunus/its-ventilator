package com.covid.its.ventilator;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.felhr.usbserial.UsbSerialDevice;
import com.felhr.usbserial.UsbSerialInterface;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;


public class GraphExample extends AppCompatActivity {

    public final String ACTION_USB_PERMISSION = "com.covid.its.ventilator.USB_PERMISSION";
    Button startButton, stopButton;
    TextView pressureView, flowView;
    UsbManager usbManager;
    UsbDevice device;
    UsbSerialDevice serialPort;
    UsbDeviceConnection connection;
    private double X = 0;
    private double pValue = 0;
    private double fValue = 0;

    UsbSerialInterface.UsbReadCallback mCallback = new UsbSerialInterface.UsbReadCallback() { //Defining a Callback which triggers whenever data is read.
        @Override
        public void onReceivedData(byte[] arg0) {
            String data = null;
            try {
                data = new String(arg0, "UTF-8");
                X = X + 0.1;
                if (data.contains("flow")) {
                    String[] flowData = data.split(" ");
                    String[] flowParsed = flowData[1].split("\n");
                    tvPut(flowView, flowParsed[0]);
                    fValue = Double.parseDouble(flowParsed[0]);
                    series1.appendData(new DataPoint(X, fValue), true, 100);
                }
                if (data.contains("pressure")) {
                    String[] pressureData = data.split(" ");
                    tvPut(pressureView, pressureData[2]);
                    pValue = Double.parseDouble(pressureData[2]);
                    series.appendData(new DataPoint(X, pValue), true, 100);
                }
            } catch (Exception e) {
                onClickStart(startButton);
            }


        }
    };

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() { //Broadcast Receiver to automatically start and stop the Serial connection.
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case ACTION_USB_PERMISSION:
                    boolean granted = intent.getExtras().getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);
                    if (granted) {
                        connection = usbManager.openDevice(device);
                        serialPort = UsbSerialDevice.createUsbSerialDevice(device, connection);
                        if (serialPort != null) {
                            if (serialPort.open()) { //Set Serial Connection Parameters.
                                setUiEnabled(true);
                                serialPort.setBaudRate(9600);
                                serialPort.setDataBits(UsbSerialInterface.DATA_BITS_8);
                                serialPort.setStopBits(UsbSerialInterface.STOP_BITS_1);
                                serialPort.setParity(UsbSerialInterface.PARITY_NONE);
                                serialPort.setFlowControl(UsbSerialInterface.FLOW_CONTROL_OFF);
                                serialPort.read(mCallback);
                                tvAppend(flowView, "Serial Connection Opened!\n");

                            } else {
                                Log.d("SERIAL", "PORT NOT OPEN");
                            }
                        } else {
                            Log.d("SERIAL", "PORT IS NULL");
                        }
                    } else {
                        Log.d("SERIAL", "PERM NOT GRANTED");
                    }
                    break;
                case UsbManager.ACTION_USB_DEVICE_ATTACHED:
                    onClickStart(startButton);
                    break;
                case UsbManager.ACTION_USB_DEVICE_DETACHED:
                    onClickStop(stopButton);
                    break;
            }
        }

        ;
    };

    //    private Handler mHandler = new Handler();
    LineGraphSeries<DataPoint> series;
    LineGraphSeries<DataPoint> series1;
    double x1 = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph_example);
        usbManager = (UsbManager) getSystemService(this.USB_SERVICE);
        startButton = (Button) findViewById(R.id.buttonStart2);
        stopButton = (Button) findViewById(R.id.buttonStop2);
        pressureView = (TextView) findViewById(R.id.pressureView2);
        flowView = (TextView) findViewById(R.id.flowView2);

        setUiEnabled(false);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(broadcastReceiver, filter);

        GraphView pressure = findViewById(R.id.graph);
        GraphView flow = findViewById(R.id.graph3);

        series = new LineGraphSeries<>();
        series1 = new LineGraphSeries<>();

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

//        addRandomDataPoint();

//        usbService.changeBaudRate(9600);
        flow.addSeries(series1);
        pressure.addSeries(series);
        onClickStart(startButton);
    }

    public void setUiEnabled(boolean bool) {
//        startButton.setEnabled(!bool);
//        stopButton.setEnabled(bool);
        flowView.setEnabled(bool);
        pressureView.setEnabled(bool);
    }

//    private void addRandomDataPoint() {
//        mHandler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                x1 = x1 + 0.1;
//                series1.appendData(new DataPoint(x1, Math.sin(x1)), true, 100);
////                series.appendData(new DataPoint(x1, Math.sin(x1 + 5)), true, 100);
//                addRandomDataPoint();
//            }
//        }, 10);
//    }

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
        tvAppend(flowView, "\nSerial Connection Closed! \n");

    }

    private void tvAppend(TextView tv, CharSequence text) {
        final TextView ftv = tv;
        final CharSequence ftext = text;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ftv.append(ftext);
            }
        });
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


    /*
     * This handler will be passed to UsbService. Data received from serial port is displayed through this handler
     */
//    private static class MyHandler extends Handler {
//        private final WeakReference<GraphExample> mActivity;
//        private double x2 = 0;
//        private double y2 = 0;
//
//        public MyHandler(GraphExample activity) {
//            mActivity = new WeakReference<>(activity);
//        }
//
//        @Override
//        public void handleMessage(Message msg) {
//            switch (msg.what) {
//                case UsbService.MESSAGE_FROM_SERIAL_PORT:
//                    x2 = x2 + 0.1;
//                    String data = (String) msg.obj;
//                    try {
//                        y2 = Double.parseDouble(data);
//                        mActivity.get().series.appendData(new DataPoint(x2, y2), true, 100);
//                        mActivity.get().series1.appendData(new DataPoint(x2+4, y2), true, 100);
//                    }catch (Exception e){
////                        mActivity.get().display.append(e.getMessage());
//                    }
////                    mActivity.get().display.append(data);
//                    break;
//                case UsbService.CTS_CHANGE:
//                    Toast.makeText(mActivity.get(), "CTS_CHANGE", Toast.LENGTH_LONG).show();
//                    break;
//                case UsbService.DSR_CHANGE:
//                    Toast.makeText(mActivity.get(), "DSR_CHANGE", Toast.LENGTH_LONG).show();
//                    break;
//                case UsbService.SYNC_READ:
//                    x2 = x2 + 0.1;
//                    String buffer = (String) msg.obj;
//                    try {
//                        y2 = Double.parseDouble(buffer);
//                        mActivity.get().series.appendData(new DataPoint(x2, y2), true, 100);
//                        mActivity.get().series1.appendData(new DataPoint(x2+4, y2), true, 100);
//                    } catch (Exception e) {
////                        mActivity.get().display.append(e.getMessage());
//                    }
//
//                    break;
//            }
//        }
//    }
}
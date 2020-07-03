package com.covid.its.ventilator;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.lang.ref.WeakReference;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class GraphExample extends AppCompatActivity {

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case UsbService.ACTION_USB_PERMISSION_GRANTED: // USB PERMISSION GRANTED
                    Toast.makeText(context, "USB Ready", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_PERMISSION_NOT_GRANTED: // USB PERMISSION NOT GRANTED
                    Toast.makeText(context, "USB Permission not granted", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_NO_USB: // NO USB CONNECTED
                    Toast.makeText(context, "No USB connected", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_DISCONNECTED: // USB DISCONNECTED
                    Toast.makeText(context, "USB disconnected", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_NOT_SUPPORTED: // USB NOT SUPPORTED
                    Toast.makeText(context, "USB device not supported", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
    private UsbService usbService;
    private TextView display;
    private TextView display2;
    private CheckBox box9600, box38400;
    private MyHandler mHandler;
    private final ServiceConnection usbConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            usbService = ((UsbService.UsbBinder) arg1).getService();
            usbService.setHandler(mHandler);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            usbService = null;
        }
    };

    LineGraphSeries<DataPoint> series;
    LineGraphSeries<DataPoint> series1;
    double x1 = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph_example);
        mHandler = new MyHandler(this);

        GraphView pressure = findViewById(R.id.graph);
        GraphView flow = findViewById(R.id.graph3);
        display = (TextView) findViewById(R.id.textView1);
        display2 = (TextView) findViewById(R.id.textView32);

        series = new LineGraphSeries<>();
        series1 = new LineGraphSeries<>();

        flow.getViewport().setMinX(0);
        flow.getViewport().setMaxX(10);
        flow.getViewport().setXAxisBoundsManual(true);
        flow.getViewport().setScrollable(true);

        pressure.getViewport().setMinX(0);
        pressure.getViewport().setMaxX(10);
        pressure.getViewport().setXAxisBoundsManual(true);
        pressure.getViewport().setScrollable(true);

        flow.addSeries(series1);
        pressure.addSeries(series);
    }


    @Override
    public void onResume() {
        super.onResume();
        setFilters();  // Start listening notifications from UsbService
        startService(UsbService.class, usbConnection, null); // Start UsbService(if it was not started before) and Bind it
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(mUsbReceiver);
        unbindService(usbConnection);
    }

    private void startService(Class<?> service, ServiceConnection serviceConnection, Bundle extras) {
        if (!UsbService.SERVICE_CONNECTED) {
            Intent startService = new Intent(this, service);
            if (extras != null && !extras.isEmpty()) {
                Set<String> keys = extras.keySet();
                for (String key : keys) {
                    String extra = extras.getString(key);
                    startService.putExtra(key, extra);
                }
            }
            startService(startService);
        }
        Intent bindingIntent = new Intent(this, service);
        bindService(bindingIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void setFilters() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbService.ACTION_USB_PERMISSION_GRANTED);
        filter.addAction(UsbService.ACTION_NO_USB);
        filter.addAction(UsbService.ACTION_USB_DISCONNECTED);
        filter.addAction(UsbService.ACTION_USB_NOT_SUPPORTED);
        filter.addAction(UsbService.ACTION_USB_PERMISSION_NOT_GRANTED);
        registerReceiver(mUsbReceiver, filter);
    }

    /*
     * This handler will be passed to UsbService. Data received from serial port is displayed through this handler
     */
    private static class MyHandler extends Handler {
        private final WeakReference<GraphExample> mActivity;
        //        private static final Pattern p = Pattern.compile("[^\\d]*[\\d]+[^\\d]+([\\d]+)");
        private double xFlow = 0;
        private double xPressure = 0;
        private String buffer;
        private int index = 0;
        private String[] arrayBuffer;

        public MyHandler(GraphExample activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UsbService.MESSAGE_FROM_SERIAL_PORT:
                case UsbService.SYNC_READ:
                    String data = (String) msg.obj;
                    try {
                        arrayBuffer[index] = data;
                        buffer = String.join("", arrayBuffer);
                        if (buffer.contains("\n")) {
                            if (buffer.contains("flow")) {
                                xFlow = xFlow + 0.1;
                                String replaced = buffer.replaceAll("[^\\d]", "");
                                double yFlow = Double.parseDouble(replaced);
                                mActivity.get().series.appendData(new DataPoint(xFlow, yFlow), true, 100);
                            }
                            if (buffer.contains("pressure")) {
                                xPressure = xPressure + 0.1;
                                String replaced = buffer.replaceAll("[^\\d]", "");
                                double yPressure = Double.parseDouble(replaced);
                                mActivity.get().series1.appendData(new DataPoint(xPressure, yPressure), true, 100);
                            }
                            buffer = "";
                            arrayBuffer = null;
                            index = 0;
                        }
                        index++;
                        mActivity.get().display.append(data);
                        mActivity.get().display2.append(buffer);
                    } catch (Exception ignored) {
                    }
                    break;
                case UsbService.CTS_CHANGE:
                    Toast.makeText(mActivity.get(), "CTS_CHANGE", Toast.LENGTH_LONG).show();
                    break;
                case UsbService.DSR_CHANGE:
                    Toast.makeText(mActivity.get(), "DSR_CHANGE", Toast.LENGTH_LONG).show();
                    break;
            }
        }
    }
}
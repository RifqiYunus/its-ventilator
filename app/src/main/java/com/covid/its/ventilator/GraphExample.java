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
    private EditText editText;
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

    //    private Handler mHandler = new Handler();
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

        addRandomDataPoint();

//        usbService.changeBaudRate(9600);
        flow.addSeries(series1);
        pressure.addSeries(series);
    }

    private void addRandomDataPoint() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                x1 = x1 + 0.1;
                series1.appendData(new DataPoint(x1, Math.sin(x1)), true, 100);
//                series.appendData(new DataPoint(x1, Math.sin(x1 + 5)), true, 100);
                addRandomDataPoint();
            }
        }, 10);
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
        private double x2 = 0;
        private double y2 = 0;

        public MyHandler(GraphExample activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UsbService.MESSAGE_FROM_SERIAL_PORT:
                    x2 = x2 + 0.1;
                    String data = (String) msg.obj;
                    try {
                        y2 = Double.parseDouble(data);
                        mActivity.get().series.appendData(new DataPoint(x2, y2), true, 100);
                    }catch (Exception e){
//                        mActivity.get().display.append(e.getMessage());
                    }
                    mActivity.get().display.append(data);
                    break;
                case UsbService.CTS_CHANGE:
                    Toast.makeText(mActivity.get(), "CTS_CHANGE", Toast.LENGTH_LONG).show();
                    break;
                case UsbService.DSR_CHANGE:
                    Toast.makeText(mActivity.get(), "DSR_CHANGE", Toast.LENGTH_LONG).show();
                    break;
                case UsbService.SYNC_READ:
                    x2 = x2 + 0.1;
                    String buffer = (String) msg.obj;
                    try {
                        y2 = Double.parseDouble(buffer);
                        mActivity.get().series.appendData(new DataPoint(x2, y2), true, 100);
                    } catch (Exception e) {
//                        mActivity.get().display.append(e.getMessage());
                    }
                    mActivity.get().display.append(buffer);
                    break;
            }
        }
    }
}
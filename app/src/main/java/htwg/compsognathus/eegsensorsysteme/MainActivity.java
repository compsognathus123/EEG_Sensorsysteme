package htwg.compsognathus.eegsensorsysteme;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.jjoe64.graphview.GraphView;

import java.lang.ref.WeakReference;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private UsbService usbService;
    private TextView display;
    private EditText editText;
    private MyHandler mHandler;

    GraphView graph_view;
    EEGGraph graph;

    public boolean streaming_data = false;
    /*
     * Notifications from UsbService will be received here.
     */
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

    public boolean isStreaming_data()
    {
        return streaming_data;
    }

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHandler = new MyHandler(this);

        display = (TextView) findViewById(R.id.textView2);
        display.setMovementMethod(new ScrollingMovementMethod());
        editText = (EditText) findViewById(R.id.editText);


        defineListeners();

        graph_view = (GraphView) findViewById(R.id.graph);
        graph_view.setTitle("EEG Timeplot");
        graph = new EEGGraph(graph_view);


    }

    private  void defineListeners()
    {
        Button sendButton = (Button) findViewById(R.id.button);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!editText.getText().toString().equals("")) {
                    String data = editText.getText().toString();
                    if (usbService != null) { // if UsbService was correctly binded, Send data
                        if(data.equalsIgnoreCase("0x0A"))
                        {
                            usbService.write(new byte[]{ (byte)0xF0});
                            usbService.write(new byte[]{ (byte)0x0A});
                            Log.d("MODEBUG", "Write 0xf0 0x0a");
                        }
                        else
                        {
                            usbService.write(data.getBytes());
                        }
                        Log.d("MODEBUG", "Write: " + data);
                    }
                }
            }
        });

        Button startButton = (Button) findViewById(R.id.buttonStart);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                usbService.write(new byte[]{(byte)'b'});
                Log.d("MODEBUG", "Send b, start data stream.");
                streaming_data = true;
            }
        });

        Button stopButton = (Button) findViewById(R.id.buttonStop);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                usbService.write(new byte[]{(byte) 's'});

                double freq = usbService.getReceivingFrequency();
                int num_received = usbService.getNumReceived();
                streaming_data = false;

                Log.d("MODEBUG", "Send s, stop data stream.");
                Log.d("MODEBUG", "Average recesving freqency: " + freq );
                Log.d("MODEBUG", "Received samples: " + num_received );
            }
        });

        Button connectButton = (Button) findViewById(R.id.buttonConnect);
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                usbService.write(new byte[]{(byte) 'v'});
                Log.d("MODEBUG", "Send v, init Cyton.");
            }
        });
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

    @Override
    protected void onStop() {
        finish();
        super.onStop();
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
    private static class MyHandler extends Handler
    {
        //private final WeakReference<MainActivity> mActivity;
        private MainActivity mActivity;

        public MyHandler(MainActivity mActivity)
        {
            this.mActivity = mActivity;
           // mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UsbService.MESSAGE_FROM_SERIAL_PORT:
                    String data = (String) msg.obj;
                    mActivity.display.append(data);
                    break;
                case UsbService.CTS_CHANGE:
                    Toast.makeText(mActivity, "CTS_CHANGE",Toast.LENGTH_LONG).show();
                    break;
                case UsbService.DSR_CHANGE:
                    Toast.makeText(mActivity, "DSR_CHANGE",Toast.LENGTH_LONG).show();
                    break;
                case UsbService.SYNC_READ:
                    String buffer = (String) msg.obj;
                    mActivity.display.append(buffer);
                    break;
                case EEGDataHandler.SAMPLE:
                    Log.d("MODEBUG", "main obtained sample");
                    EEGSample sample = (EEGSample) msg.obj;
                    mActivity.graph.addSample(sample);
                    break;

                case EEGDataHandler.PSD:
                    double psd[][] = (double[][]) msg.obj;
                    mActivity.graph.updatePSD(psd);
                    break;
                case EEGDataHandler.NUM_UPDATE:
                    int num_psd = (int) msg.obj;
                    mActivity.display.append(num_psd + "\n");
                    break;
            }
        }
    }
}
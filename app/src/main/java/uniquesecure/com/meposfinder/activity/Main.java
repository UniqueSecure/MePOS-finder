package uniquesecure.com.meposfinder.activity;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.format.Formatter;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.uniquesecure.meposconnect.MePOS;
import com.uniquesecure.meposconnect.MePOSColorCodes;
import com.uniquesecure.meposconnect.MePOSConnectionType;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import uniquesecure.com.meposfinder.R;
import uniquesecure.com.meposfinder.persistence.MePOSDevice;
import uniquesecure.com.meposfinder.persistence.MePOSFinderCallback;
import uniquesecure.com.meposfinder.persistence.MePOSSingleton;

public class Main extends AppCompatActivity implements MePOSFinderCallback {

    @BindView(R.id.test)
    TextView mTest;
    @BindView(R.id.progress_bar)
    ProgressBar mProgressBar;
    @BindView(R.id.buttonScan)
    Button mButtonScan;
    @BindView(R.id.list_devices)
    ListView mListDevices;
    String foundAddress;

    String[] devices = new String[0];
    ArrayList<String> list = new ArrayList<>(Arrays.asList(devices));

    private Boolean isScanning = false;
    private int meposCurrentColor = 0;
    final String LOG_TAG = "Task canceled";
    public static final int MEPOS_VENDOR_ID = 11406;
    public static final int MEPOS_PRODUCT_ID = 9220;
    private static final int port = 80;

    @Override
    protected void onStart() {
        super.onStart();
        mTest.setText("Your Android device IP is\n" + GetDeviceIP());

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mProgressBar.setMax(255);
        mButtonScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onMePOSFinderStarted();
                list.clear();
                startScanMePOS();
                mProgressBar.setVisibility(View.VISIBLE);

            }
        });
    }

    public void startScanMePOS() {
        isScanning = true;
        ScanUSBAsync scanMePOSUSB = new ScanUSBAsync();
        scanMePOSUSB.execute();
        ScanMePOSTCP scanMePOSTCP = new ScanMePOSTCP();
        scanMePOSTCP.execute();
    }

    public void onMePOSFinderStarted() {
    }


    public void onMePOSFinderProgress(int progress) {

    }


    public void onMePOSFound(MePOSDevice device) {
        String type = device.getType();
        String ip = device.getIpAddress();
        if (ip!=null){
            list.add("Found mepos Over " + type + " Ip Address "+ ip);
        }else {
            list.add("Found mepos Over " + type);
        }

        updateList();
    }


    public void onMePOSFinderCompleted() {

    }

    public void onMePOSFinderError(Exception e) {

    }


    public class ScanMePOSTCP extends AsyncTask<String, Integer, Boolean> {
        Socket mePOS_Socket;
        String ipToLookUp;
        String[] segments = GetDeviceIP().split("\\.");

        @Override
        protected Boolean doInBackground(String... strings) {
            if (segments.length == 4) {
                String generalSegment = segments[0] + "." + segments[1] + "." + segments[2] + ".";
                for (int i = 0; i < 256; i++) {
                    ipToLookUp = generalSegment + i;
                    this.publishProgress(i);
                    mePOS_Socket = new Socket();
                    try {
                        mePOS_Socket.connect((new InetSocketAddress(ipToLookUp, port)), 60);
                        String[] address = mePOS_Socket.getRemoteSocketAddress().toString().split(":");
                        foundAddress = address[0];
                        foundAddress = foundAddress.replace("/", "");
                        mePOS_Socket.close();
                        if (isMePOSOnIp(ipToLookUp)) {
                            registerMePOS(foundAddress);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            mProgressBar.setProgress(values[0]);
            if (values[0].equals(254)) {
                mButtonScan.setText("Find");
                try {
                    MePOSSingleton.getInstance().setCosmeticLedCol(MePOSColorCodes.COSMETIC_OFF);
                } catch (Exception e) {
                }

            }
        }

        private boolean isMePOSOnIp(String ipAddress) {
            MePOS mepos = new MePOS(Main.this, MePOSConnectionType.WIFI);
            mepos.getConnectionManager().setConnectionIPAddress(ipAddress);
            return mepos.isMePOSConnected();
        }
    }


    public void registerMePOS(String ipFound) {
        swapColor();
        MePOSSingleton.createInstance(getApplicationContext(), MePOSConnectionType.WIFI);
        MePOSSingleton.getInstance().getConnectionManager().setConnectionIPAddress(ipFound);
        MePOSSingleton.getInstance().setCosmeticLedCol(meposCurrentColor);
        String serialNumber = null;
        try {
            serialNumber = MePOSSingleton.getInstance().getSerialNumber();
        } catch (Exception e) {
            e.printStackTrace();
        }
        final MePOSDevice mePOSDeviceWifi = new MePOSDevice();
        mePOSDeviceWifi.setType("WIFI");
        mePOSDeviceWifi.setSerialNumber(serialNumber);
        mePOSDeviceWifi.setIpAddress(ipFound);
        final String finalSerialNumber = serialNumber;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (finalSerialNumber != null)
                onMePOSFound(mePOSDeviceWifi);
            }
        });

    }


    public class ScanUSBAsync extends AsyncTask<String, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(String... strings) {
            findMePOSUSB();
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }


    }

    protected void findMePOSUSB() {
        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
        for (UsbDevice device : deviceList.values()) {
            if (isAMePOS(device)) {
                final MePOSDevice mePOSDeviceUSB = new MePOSDevice();
                mePOSDeviceUSB.setType("USB");
                mePOSDeviceUSB.setIpAddress(null);
                mePOSDeviceUSB.setSerialNumber(device.getSerialNumber());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onMePOSFound(mePOSDeviceUSB);
                    }
                });
            }
        }
    }

    protected boolean isAMePOS(UsbDevice device) {
        return device.getVendorId() == MEPOS_VENDOR_ID &&
                device.getProductId() == MEPOS_PRODUCT_ID;
    }

    @SuppressWarnings("deprecation")
    public String GetDeviceIP() {
        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        String address = Formatter.formatIpAddress(wm.getConnectionInfo().getIpAddress());
        return address;
    }

    public void swapColor() {
        meposCurrentColor += 1;
        if (meposCurrentColor > MePOSColorCodes.COSMETIC_WHITE) {
            meposCurrentColor = MePOSColorCodes.COSMETIC_BLUE;
        }
    }

    public void updateList(){
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(Main.this, android.R.layout.simple_list_item_1, list);
        adapter.notifyDataSetChanged();
        mListDevices.setAdapter(adapter);

    }
}

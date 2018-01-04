package com.bluecreation.melodysmart.testapp;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.bluecreation.melodysmart.BuildConfig;
import com.bluecreation.melodysmart.MelodySmartDevice;
import com.bluecreation.melodysmart.R;

import java.util.ArrayList;

/**
 * Created by genis on 20/10/2014.
 */
/**
 * Activity for scanning and displaying available Bluetooth LE devices.
 */
public class ScanActivity extends ListActivity {
    private LeDeviceListAdapter mLeDeviceListAdapter;
    private boolean mScanning;

    private static final int REQUEST_ENABLE_BT      = 1;
    private static final int REQUEST_OPEN_DEVICE    = 2;

    protected static final String TAG = ScanActivity.class.getSimpleName();

    protected String mDeviceAddress;
    protected String mDeviceName;
    private AlertDialog connectingDialog;

    private MelodySmartDevice melodySmartDevice;

    private static class ScanResult {
        public BluetoothDevice device;
        public byte[] scanRecord;

        public ScanResult(BluetoothDevice device, byte[] scanRecord) {
            this.device = device;
            this.scanRecord = scanRecord;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(String.format("%s v%s", getString(R.string.app_name), BuildConfig.VERSION_NAME));
        getListView().setPadding(4, 4, 4, 4);

        melodySmartDevice = MelodySmartDevice.getInstance();
        melodySmartDevice.init(this);

        // Initializes list view adapter.
        mLeDeviceListAdapter = new LeDeviceListAdapter(getLayoutInflater());
        setListAdapter(mLeDeviceListAdapter);
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy()");
        melodySmartDevice.disconnect();
        melodySmartDevice.close(this);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        if (!mScanning) {
            menu.findItem(R.id.menu_stop).setVisible(false);
            menu.findItem(R.id.menu_scan).setVisible(true);
            menu.findItem(R.id.menu_refresh).setActionView(null);
        } else {
            menu.findItem(R.id.menu_stop).setVisible(true);
            menu.findItem(R.id.menu_scan).setVisible(false);
            menu.findItem(R.id.menu_refresh).setActionView(R.layout.actionbar_indeterminate_progress);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_scan:
                mLeDeviceListAdapter.clear();
                scanLeDevice(true);
                break;

            case R.id.menu_stop:
                scanLeDevice(false);
                break;
        }

        return true;
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        LeDeviceListAdapter.ViewHolder viewHolder = (LeDeviceListAdapter.ViewHolder) v.getTag();

        scanLeDevice(false);

        mDeviceAddress = viewHolder.deviceAddress.getText().toString();
        mDeviceName = viewHolder.deviceName.getText().toString();

        Intent i = new Intent(getApplicationContext(), Bt2irdaActivity.class);
        i.putExtra("deviceAddress", mDeviceAddress);
        i.putExtra("deviceName", mDeviceName);
        startActivity(i);
    }

    private synchronized void scanLeDevice(final boolean enable) {
        mScanning = enable;
        if (enable) {
            melodySmartDevice.startLeScan(mLeScanCallback);
        } else {
            melodySmartDevice.stopLeScan(mLeScanCallback);
        }
        invalidateOptionsMenu();
    }

    // Adapter for holding devices found through scanning.
    private static class LeDeviceListAdapter extends BaseAdapter {
        private final ArrayList<ScanResult> mLeResults;
        private final LayoutInflater mInflater;

        static class ViewHolder {
            TextView deviceName;
            TextView deviceAddress;
            TextView scanRecord;
        }

        public LeDeviceListAdapter(LayoutInflater layoutInflater) {
            super();
            mLeResults = new ArrayList<>();
            mInflater = layoutInflater;
        }

        public void addDevice(ScanResult scanResult) {
            for (ScanResult mLeResult : mLeResults) {
                if (mLeResult.device.equals(scanResult.device)) {
                    mLeResult.device = scanResult.device;
                    mLeResult.scanRecord = scanResult.scanRecord;
                    notifyDataSetChanged();
                    return;
                }
            }
            mLeResults.add(scanResult);
            notifyDataSetChanged();
        }


        public void clear() {
            mLeResults.clear();
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mLeResults.size();
        }

        @Override
        public Object getItem(int i) {
            return mLeResults.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            // General ListView optimization code.
            if (view == null) {
                view = mInflater.inflate(R.layout.listitem_device, viewGroup, false);
                viewHolder = new ViewHolder();
                viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
                viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
                viewHolder.scanRecord = (TextView) view.findViewById(R.id.scan_record);

                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            BluetoothDevice device = mLeResults.get(i).device;
            String manufacturerData = getManufacturerData(mLeResults.get(i).scanRecord);

            final String deviceName = device.getName();
            if (deviceName != null && deviceName.length() > 0) {
                viewHolder.deviceName.setText(deviceName);
            } else {
                viewHolder.deviceName.setText("Unknown device");
            }
            viewHolder.deviceAddress.setText(device.getAddress());
            viewHolder.scanRecord.setText("data: " + manufacturerData);


            return view;
        }
    }

    // Device scan callback.
    private final BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
            @Override
        public void onLeScan(final BluetoothDevice device, int rssi, final byte[] scanRecord) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "Found device " + device.getAddress());
                    mLeDeviceListAdapter.addDevice(new ScanResult(device, scanRecord));
                }
            });
        }
    };

    private static String getManufacturerData(byte[] record) {
        int i = 0;
        String recordString = "";
        boolean found = false;
        while (i < record.length) {
            int size = record[i];
            int id = record[i+1];

            if (size == 0) {
                break;
            }

            if (id == (byte)0xff) {
                found = true;
                for (int j = i + 2; j < i + size + 1; j++) {
                    recordString += String.format("%02x ", record[j]);
                }
                break;
            }

            i += size + 1;
        }
        return (found) ? recordString : null;
    }
}
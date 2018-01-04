package com.bluecreation.melodysmart.testapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bluecreation.melodysmart.BLEError;
import com.bluecreation.melodysmart.DataService;
import com.bluecreation.melodysmart.DeviceDatabase;
import com.bluecreation.melodysmart.DeviceInfoService;
import com.bluecreation.melodysmart.I2CService;
import com.bluecreation.melodysmart.MelodySmartDevice;
import com.bluecreation.melodysmart.MelodySmartListener;
import com.bluecreation.melodysmart.R;
import com.bluecreation.melodysmart.RemoteCommandsService;

/**
 * Created by genis on 15/10/2014.
 */
public class MelodySmartTestActivity extends BleActivity implements MelodySmartListener {

    private String TAG = "TestActivity";
    private static final int OTAU_ACTIVITY_CODE = 1;
    private MelodySmartDevice device;
    private EditText dataToSend;
    private EditText dataReceived;
    private Button otaButton;
    private Button i2cButton;
    private Button batteryButton;
    private Button remoteCommandsButton;
    private AlertDialog alertDialog;
    private int nextInfoType = 0;

    private static final boolean BATTERY_SERVICE = false;
    private static final boolean DEVICE_INFO_SERVICE = false;

    private enum ChildActivity {
        OTAU,
        REMOTE_COMMANDS,
        I2C
    };


    //Uncomment to enable battery service
    /*
    private BatteryService.Listener batteryServiceListener = new BatteryService.Listener() {

        @Override
        public void onBatteryLevelChanged(final int level) {
            Log.d(TAG, "Got battery level " + level);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((TextView) findViewById(R.id.batteryView)).setText(String.format("Battery : %d", level));
                }
            });
        }

        @Override
        public void onConnected(final boolean found) {

            Log.d(TAG, ((found) ? "Connected " : "Not connected  ") + "to battery level service");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    batteryButton.setEnabled(found);
                }
            });
            // device.getBatteryService().enableNotifications(true);
        }
    };
    */

    private I2CService.Listener i2cListener = new I2CService.Listener() {

        @Override
        public void onConnected(final boolean found) {
            Log.d(TAG, ((found) ? "Connected " : "Not connected  ") + "to I2C service");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    i2cButton.setEnabled(found);
                }
            });
        }

        @Override
        public void handleReply(boolean success, byte[] data) {
            /* do nothing */
        }
    };

    private DataService.Listener dataServiceListener = new DataService.Listener() {

        @Override
        public void onConnected(final boolean found) {
            Log.d(TAG, ((found) ? "Connected " : "Not connected  ") + "to MelodySmart data service");
            if (found) {
                device.getDataService().enableNotifications(true);
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (alertDialog != null && alertDialog.isShowing()) {
                        alertDialog.dismiss();
                    }
                    dataToSend.setEnabled(found);

                    if (!found) {
                        Toast.makeText(MelodySmartTestActivity.this, "MelodySmart service not found on the remote device.", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

        @Override
        public void onReceived(final byte[] data) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dataReceived.setText(new String(data));
                }
            });
        }
    };

    //Uncomment to enable battery service
    /*
    private DeviceInfoService.Listener deviceInfoListener = new DeviceInfoService.Listener() {
        @Override
        public void onInfoRead(final DeviceInfoService.INFO_TYPE type, final String value) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    switch (type) {
                        case MANUFACTURER_NAME:
                            ((TextView) findViewById(R.id.manufacturerNameView)).setText("Manufacturer name: " + value);
                            break;
                        case MODEL_NUMBER:
                            ((TextView) findViewById(R.id.modelNumberView)).setText("Model number: " + value);
                            break;
                        case SERIAL_NUMBER:
                            ((TextView) findViewById(R.id.serialNumberView)).setText("Serial number: " + value);
                            break;
                        case HARDWARE_REV:
                            ((TextView) findViewById(R.id.hwRevView)).setText("HW rev: " + value);
                            break;
                        case FIRMWARE_REV:
                            ((TextView) findViewById(R.id.fwRevView)).setText("FW rev " + value);
                            break;
                        case SOFTWARE_REV:
                            ((TextView) findViewById(R.id.swRevView)).setText("SW rev: " + value);
                            break;
                        case SYSTEM_ID:
                            ((TextView) findViewById(R.id.sysIdView)).setText("System id: " + value);
                            break;
                        case PNP_ID:
                            ((TextView) findViewById(R.id.pnpIdView)).setText("PNP id: " + value);
                            break;
                    }
                }
            });
            readNextInfo();
        }

        @Override
        public void onReadError(int error) {
            Log.d(TAG, "Error reading from the Device Information service: " + error);
        }

        @Override
        public void onConnected(boolean found) {
            Log.d(TAG, ((found) ? "Connected " : "Not connected  ") + "to Device info service");
            // Start reading all the characteristics
            readNextInfo();
        }
    };
    */

    private RemoteCommandsService.Listener remoteCommandsListener = new RemoteCommandsService.Listener() {

        @Override
        public void handleReply(byte[] reply) {
            /* Do nothing */
        }

        @Override
        public void onConnected(final boolean found) {
            Log.d(TAG, ((found) ? "Connected " : "Not connected  ") + "to remote commands service");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    remoteCommandsButton.setEnabled(found);
                }
            });
        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_activity);
        Log.d(TAG, "Starting");

        /* Get the instance of the Melody Smart Android library and initialize it */
        device = MelodySmartDevice.getInstance();
        device.registerListener((MelodySmartListener) this);
        device.getDataService().registerListener(dataServiceListener);
        device.getI2CService().registerListener(i2cListener);

        //Uncomment to enable battery or device info services
        /*
        device.getBatteryService().registerListener(batteryServiceListener);
        device.getDeviceInfoService().registerListener(deviceInfoListener);
        */

        device.getRemoteCommandsService().registerListener(remoteCommandsListener);

        String deviceAddress = getIntent().getStringExtra("deviceAddress");
        String deviceName = getIntent().getStringExtra("deviceAddress");

        alertDialog = new AlertBuilder(this)
                .setMessage(String.format("Connecting to:\n%s\n(%s)...", deviceName, deviceAddress))
                .setTitle(R.string.app_name)
                .create();
        alertDialog.show();

        try {
            device.connect(deviceAddress);
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }

        dataReceived = (EditText) findViewById(R.id.etReceivedData);
        dataToSend = (EditText) findViewById(R.id.etDataToSend);
        dataToSend.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                device.getDataService().send(textView.getText().toString().getBytes());
                textView.setText("");
                return true;
            }
        });

        dataToSend.setEnabled(false);
        dataReceived.setEnabled(false);

        otaButton = (Button) findViewById(R.id.otaButton);
        otaButton.setEnabled(false);
        otaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startOtauActivity(false, null);
            }
        });

        remoteCommandsButton = (Button) findViewById(R.id.remoteCommandsButton);
        remoteCommandsButton.setEnabled(false);
        remoteCommandsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startRemoteCommandsActivity();
            }
        });

        i2cButton = (Button) findViewById(R.id.i2cButton);
        i2cButton.setEnabled(false);
        i2cButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startI2CActivity();
            }
        });

        // Uncomment to enable battery service
        /*
        batteryButton = (Button) findViewById(R.id.batteryButton);
        batteryButton.setEnabled(false);
        batteryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                device.getBatteryService().read();
            }
        });
        */
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == ChildActivity.OTAU.ordinal() && resultCode == RESULT_OK) {
            finish();
        }
    }


    @Override
    public void onDestroy() {

        device.getDataService().unregisterListener(dataServiceListener);
        //Uncomment to enable battery or device info services
        /*
        device.getBatteryService().unregisterListener(batteryServiceListener);
        device.getDeviceInfoService().unregisterListener(deviceInfoListener);
        */
        device.getI2CService().unregisterListener(i2cListener);
        device.getRemoteCommandsService().unregisterListener(remoteCommandsListener);
        device.unregisterListener((MelodySmartListener) this);

        device.disconnect();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onDeviceConnected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                alertDialog.setMessage("Discovering MelodySmart service...");
            }
        });
    }

    @Override
    public void onDeviceDisconnected(final BLEError error) {

        for (ChildActivity child : ChildActivity.values()) {
            finishActivity(child.ordinal());
        }

        if (error.getType() == BLEError.Type.NO_ERROR) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dataToSend.setEnabled(false);
                    Toast.makeText(MelodySmartTestActivity.this, "Disconnected from the device.", Toast.LENGTH_LONG).show();
                    finish();
                }
            });
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (alertDialog != null && alertDialog.isShowing()) {
                        alertDialog.dismiss();
                    }
                    AlertDialog.Builder builder = new AlertBuilder(MelodySmartTestActivity.this)
                            .setMessage(getDisconnectionMessage(error))
                            .setTitle("Disconnected")
                            .setCancelable(false)
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    finish();
                                }
                            });
                    alertDialog = builder.show();
                }
            });
        }
    }

    @Override
    public void onOtauAvailable() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                otaButton.setEnabled(true);
            }
        });
    }

    @Override
    public void onOtauRecovery(DeviceDatabase.DeviceData deviceData) {
        // Automatically go to OTAU
        startOtauActivity(true, deviceData);
    }

    private void readNextInfo() {
        DeviceInfoService.INFO_TYPE[] types = DeviceInfoService.INFO_TYPE.values();
        if (nextInfoType < types.length) {
            device.getDeviceInfoService().read(types[nextInfoType]);
            nextInfoType++;
        }
    }

    private void startOtauActivity(boolean isRecovery, DeviceDatabase.DeviceData deviceData) {

        device.getDataService().unregisterListener(dataServiceListener);
        //Uncomment to enable battery or device info services
        /*
        device.getBatteryService().unregisterListener(batteryServiceListener);
        device.getDeviceInfoService().unregisterListener(deviceInfoListener);
        */
        device.unregisterListener((MelodySmartListener) this);

        Intent intent = new Intent(this, OtauTestActivity.class);
        intent.putExtra(OtauTestActivity.EXTRAS_IS_RECOVER_OTA, isRecovery);
        intent.putExtra(OtauTestActivity.EXTRAS_DEVICE_DATA, deviceData);
        startActivityForResult(intent, ChildActivity.OTAU.ordinal());
    }

    private void startI2CActivity() {
        Intent intent = new Intent(this, I2CActivity.class);
        device.getI2CService().unregisterListener(i2cListener);
        startActivityForResult(intent, ChildActivity.I2C.ordinal());
    }

    private void startRemoteCommandsActivity() {
        Intent intent = new Intent(this, RemoteCommandsActivity.class);
        device.getRemoteCommandsService().unregisterListener(remoteCommandsListener);
        startActivityForResult(intent, ChildActivity.REMOTE_COMMANDS.ordinal());
    }


    private String getDisconnectionMessage(BLEError error) {
        String message = "";
        switch (error.getType()) {
            case AUTHENTICATION_ERROR:
                message = "Authentication error: ";
                if (device.isBonded()) {
                    if (device.removeBond()) {
                        message += " bonding information has been removed on your Android phone. Please remove it on your MelodySmart device if necessary and reconnect.";
                    } else {
                        message += " could not remove bonding information on your Android phone. Please remove it manually on the Bluetooth settings screen, " +
                                "remove it on your MelodySmart device if necessary and reconnect.";
                    }
                } else {
                    message += " please remove bonding information on your MelodySmart device and reconnect.";

                }
                break;

            case REMOTE_DISCONNECTION:
                message = error.getMessage();
                break;

            default:
                message = String.format("Disconnected: %s\n\n[error code: %d]", error.getMessage(), error.getCode());
                break;
        }

        return message;
    }
}

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
import com.bluecreation.melodysmart.*;

/**
 * Created by genis on 15/10/2014.
 */
public class Bt2irdaActivity extends BleActivity implements MelodySmartListener {

    private String TAG = "TestActivity";
    private static final int OTAU_ACTIVITY_CODE = 1;
    private MelodySmartDevice device;
    private EditText dataToSend;
    private EditText dataReceived;
    private Button play0Button;
    private Button play1Button;
    private Button play2Button;
    private Button play3Button;
    private Button play4Button;
    private Button play5Button;
    private Button play6Button;
    private Button play7Button;
    private Button record0Button;
    private Button record1Button;
    private Button record2Button;
    private Button record3Button;
    private Button record4Button;
    private Button record5Button;
    private Button record6Button;
    private Button record7Button;
    private AlertDialog alertDialog;
    private int nextInfoType = 0;

    private static final boolean BATTERY_SERVICE = false;
    private static final boolean DEVICE_INFO_SERVICE = false;

    private enum ChildActivity {
        OTAU,
        REMOTE_COMMANDS,
        I2C
    }

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
                        Toast.makeText(Bt2irdaActivity.this, "MelodySmart service not found on the remote device.", Toast.LENGTH_LONG).show();
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bt2irda);
        Log.d(TAG, "Starting");

        /* Get the instance of the Melody Smart Android library and initialize it */
        device = MelodySmartDevice.getInstance();
        device.registerListener((MelodySmartListener) this);
        device.getDataService().registerListener(dataServiceListener);

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

        play0Button = (Button) findViewById(R.id.play0Button);
        play0Button.setEnabled(true);
        play0Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                play(0);
            }
        });
        play1Button = (Button) findViewById(R.id.play1Button);
        play1Button.setEnabled(true);
        play1Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                play(1);
            }
        });
        play2Button = (Button) findViewById(R.id.play2Button);
        play2Button.setEnabled(true);
        play2Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                play(2);
            }
        });
        play3Button = (Button) findViewById(R.id.play3Button);
        play3Button.setEnabled(true);
        play3Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                play(3);
            }
        });
        play4Button = (Button) findViewById(R.id.play4Button);
        play4Button.setEnabled(true);
        play4Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                play(4);
            }
        });
        play5Button = (Button) findViewById(R.id.play5Button);
        play5Button.setEnabled(true);
        play5Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                play(5);
            }
        });
        play6Button = (Button) findViewById(R.id.play6Button);
        play6Button.setEnabled(true);
        play6Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                play(6);
            }
        });
        play7Button = (Button) findViewById(R.id.play7Button);
        play7Button.setEnabled(true);
        play7Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                play(7);
            }
        });

        record0Button = (Button) findViewById(R.id.record0Button);
        record0Button.setEnabled(true);
        record0Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                record(0);
            }
        });
        record1Button = (Button) findViewById(R.id.record1Button);
        record1Button.setEnabled(true);
        record1Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                record(1);
            }
        });
        record2Button = (Button) findViewById(R.id.record2Button);
        record2Button.setEnabled(true);
        record2Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                record(2);
            }
        });
        record3Button = (Button) findViewById(R.id.record3Button);
        record3Button.setEnabled(true);
        record3Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                record(3);
            }
        });
        record4Button = (Button) findViewById(R.id.record4Button);
        record4Button.setEnabled(true);
        record4Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                record(4);
            }
        });
        record5Button = (Button) findViewById(R.id.record5Button);
        record5Button.setEnabled(true);
        record5Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                record(5);
            }
        });
        record6Button = (Button) findViewById(R.id.record6Button);
        record6Button.setEnabled(true);
        record6Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                record(6);
            }
        });
        record7Button = (Button) findViewById(R.id.record7Button);
        record7Button.setEnabled(true);
        record7Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                record(7);
            }
        });

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
                    Toast.makeText(Bt2irdaActivity.this, "Disconnected from the device.", Toast.LENGTH_LONG).show();
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
                    AlertDialog.Builder builder = new AlertBuilder(Bt2irdaActivity.this)
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
    }

    @Override
    public void onOtauRecovery(DeviceDatabase.DeviceData deviceData) {
    }

    private void readNextInfo() {
        DeviceInfoService.INFO_TYPE[] types = DeviceInfoService.INFO_TYPE.values();
        if (nextInfoType < types.length) {
            device.getDeviceInfoService().read(types[nextInfoType]);
            nextInfoType++;
        }
    }

    private void record(int i) {
        device.getDataService().send(("record " + i).getBytes());
    }

    private void play(int i) {
        device.getDataService().send(("play " + i).getBytes());
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

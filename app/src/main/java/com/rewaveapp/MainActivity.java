package com.rewaveapp;

import android.app.AlertDialog;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.btwiz.library.BTSocket;
import com.btwiz.library.BTWiz;
import com.btwiz.library.DeviceNotSupportBluetooth;
import com.btwiz.library.IDeviceConnectionListener;
import com.btwiz.library.IDeviceLookupListener;
import com.btwiz.library.IReadListener;
import com.btwiz.library.SecureMode;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.UUID;


public class MainActivity
        extends
            AppCompatActivity
        implements
            ListerFragment.OnFragmentInteractionListener,
            IDeviceLookupListener,
            IDeviceConnectionListener,
            // IReadListener,
            ControllerFragment.OnFragmentInteractionListener,
            BtSwitchOffReceiver.OnBtSwitchOffInteractionListener,
            BtDisconnectedReceiver.OnBtDisconnectInteractionListener {

    private final static int REQUEST_ENABLE_BT = 1;
    private BtSwitchOffReceiver btSwitchOffReceiver;
    private BtDisconnectedReceiver btDisconnectedReceiver;
    private BTSocket socket;
    private BluetoothDevice device;
    private Vibrator vibrator;

    private boolean isDiscovering = false;
    private boolean controlling = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.e("Main", "onCreate called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            if (BTWiz.isEnabled(getApplicationContext())) showListerFragment();
            else startSwitchBtOnIntent();

            // see that the user or some other app doesn't switches off bt in the background
            btSwitchOffReceiver = new BtSwitchOffReceiver(this);
            registerReceiver(btSwitchOffReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));

            vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        } catch (DeviceNotSupportBluetooth e) {
            Toast.makeText(getApplicationContext(), "No Bluetooth Available : Rewave needs bluetooth to function", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        findViewById(R.id.progress_indicator_central).setVisibility(View.GONE);
        if (controlling) {
            controlling = false;
            sendCommand("exit"); // indicate the server that the connection is being closed
            if (btDisconnectedReceiver != null) {
                unregisterReceiver(btDisconnectedReceiver);
                btDisconnectedReceiver = null;
            }
            showListerFragment();
            //Util.recreateActivityCompat(this);
        } else {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        Log.e("Main", "onDestroy called");
        super.onDestroy();
        unregisterReceiver(btSwitchOffReceiver);
        BTWiz.cleanup(getApplicationContext());
        try {
            socket.close();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public void startSwitchBtOnIntent() {
        startActivityForResult(BTWiz.enableBTIntent(), REQUEST_ENABLE_BT); // ask user to allow using BT
    }

    // called when user interacts with initial enableBtIntent dialog
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        switch (resultCode) {
            case RESULT_OK:
                showListerFragment();
                break;
            case RESULT_CANCELED:
                final AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
                alert.setTitle(getString(R.string.main_bluetooth_required_title));
                alert.setMessage(getString(R.string.main_bluetooth_required_body));
                alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startSwitchBtOnIntent();
                    }
                });
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        alert.show();
                    }
                });

                break;
            default:
                break;
        }
    }

    public void showListerFragment() {
        setTitle(getString(R.string.title_activity_main));
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.container, ListerFragment.newInstance())
                .commit()
        ;
    }

    /*
     * ListerFragment Interactions
     */

    // Helper methods for Lister
    public void connectTo(BluetoothDevice device) {
        setTitle(R.string.connecting);
        BTWiz.connectAsClientAsync(getApplicationContext(), device, this, SecureMode.SECURE, UUID.fromString("a1a738e0-c3b3-11e3-9c1a-0800200c9a66"));
    }

    @Override
    public Set<BluetoothDevice> getBondedDevices() {
        return BTWiz.getAllBondedDevices(getApplicationContext());
    }

    @Override
    public void startDiscovery() {
        if (!isDiscovering) {
            isDiscovering = true;
            BTWiz.startDiscoveryAsync(getApplicationContext(), null, this);
            ((ListerFragment) getFragmentManager().findFragmentById(R.id.container)).discoveryStarted();
        }
    }

    @Override
    public void stopDiscovery() {
        if (isDiscovering) {
            isDiscovering = false;
            BTWiz.stopDiscovery(getApplicationContext());
            ((ListerFragment) getFragmentManager().findFragmentById(R.id.container)).discoveryFinished();
        }
    }

    @Override
    public void onDeviceClicked(BluetoothDevice device) {
        Log.e("Main", "Device Clicked " + device.getName());

        controlling = true;
        this.device = device;
        findViewById(R.id.progress_indicator_central).setVisibility(View.VISIBLE);

        getFragmentManager()
                .beginTransaction()
                .replace(R.id.container, ControllerFragment.newInstance())
                .addToBackStack(null)
                .commit()
        ;

        connectTo(device);
    }

    /*
     * IDeviceLookupListener
     */

    @Override
    public boolean onDeviceFound(BluetoothDevice bluetoothDevice, boolean b) {
        ((ListerFragment) getFragmentManager().findFragmentById(R.id.container)).onDeviceFound(bluetoothDevice, b);
        return false;
    }

    @Override
    public void onDeviceNotFound(boolean b) {
        stopDiscovery();
    }

    /*
     * IDeviceConnectionListener
     */

    @Override
    public void onConnectSuccess(BTSocket btSocket) {
        Log.e("Main", "Connect Success");
        this.socket = btSocket;

        btDisconnectedReceiver = new BtDisconnectedReceiver(this);
        registerReceiver(btDisconnectedReceiver, new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED));

        // TODO : read 255 bytes of data
        // btSocket.readAsync(new byte[255], this); // triggers IReadListener

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.progress_indicator_central).setVisibility(View.GONE);
                setTitle(device.getName());

                Fragment current = getFragmentManager().findFragmentById(R.id.container);
                if (current instanceof ControllerFragment) {
                    ((ControllerFragment) current).onConnectionSuccess();
                } else {
                    // the user has already pressed back while loading
                }
            }
        });
    }

    @Override
    public void onConnectionError(Exception e, String s) {
        Log.e("Main", "onConnectionError called");
        Log.e("Main", "s = " + s);
        final AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
        alert.setTitle(getString(R.string.main_no_server_title));
        alert.setMessage(getString(R.string.main_no_server_body));
        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                onBackPressed();
            }
        });
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.progress_indicator_central).setVisibility(View.GONE);
                if (controlling) {
                    // show only if controlling because if the user pressed back button in the loading time then this will be irrelevant
                    alert.show();
                }
            }
        });
    }

    /*
     * IReadListener
     */

    /* TODO //
    @Override
    public void onError(int code, IOException e) {
        // unable to read data sent from server
        e.printStackTrace();
        socket.readAsync(new byte[255], this);
    }

    @Override
    public void onSuccess(int code) {
        Log.e("Main", "Got data" + String.valueOf(code) + " from server");
        socket.readAsync(new byte[255], this);
    }
    //*/

    /*
     * Control Fragment Interactions
     */
    @Override
    public void sendCommand(String command) {
        try {
            try {
                socket.write(command);
                vibrator.vibrate(100);
            } catch (NullPointerException n) {
                n.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     * BtSwitchOffReceiver
     */
    @Override
    public void onBtSwitchedOff() {
        Util.recreateActivityCompat(this);
    }

    /*
     * BtDisconnectedReceiver
     */
    @Override
    public void onBtDisconnected() {
        onBackPressed();
    }
}

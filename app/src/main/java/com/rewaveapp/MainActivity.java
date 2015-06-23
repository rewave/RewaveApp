package com.rewaveapp;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
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

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Set;


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
            BondStateChangedReceiver.OnBondStateChangedInteractionListener,
            BtDisconnectedReceiver.OnBtDisconnectInteractionListener {

    private final static int REQUEST_ENABLE_BT = 1;
    private BtSwitchOffReceiver btSwitchOffReceiver;
    private BondStateChangedReceiver bondStateChangedReceiver;
    private BtDisconnectedReceiver btDisconnectedReceiver;
    private BTSocket socket;
    private BluetoothDevice device;

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

        } catch (DeviceNotSupportBluetooth e) {
            Toast.makeText(getApplicationContext(), "No Bluetooth Available : Rewave needs bluetooth to function", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        if (controlling) {
            controlling = false;
            sendCommand("exit"); // indicate the server that the connection is being closed
            Util.recreateActivityCompat(this);
        } else {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        Log.e("Main", "onDestroy called");
        super.onDestroy();
        unregisterReceiver(btSwitchOffReceiver);
        if (bondStateChangedReceiver != null) unregisterReceiver(bondStateChangedReceiver);
        if (btDisconnectedReceiver != null) unregisterReceiver(btDisconnectedReceiver);
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
        getFragmentManager()
                .beginTransaction()
                .add(R.id.container, ListerFragment.newInstance())
                .addToBackStack(ListerFragment.class.getName())
                .commit()
        ;
    }

    /*
     * ListerFragment Interactions
     */

    // Helper methods for Lister
    public void connectTo(BluetoothDevice device) {
        setTitle(R.string.connecting);
        BTWiz.connectAsClientAsync(getApplicationContext(), device, this);
    }

    public boolean createBond(BluetoothDevice device)
            throws Exception
    {
        Class deviceClass = Class.forName("android.bluetooth.BluetoothDevice");
        Method createBondMethod = deviceClass.getMethod("createBond");
        return (Boolean) createBondMethod.invoke(device);
    }

    private boolean isDiscovering = false;

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

        if (BTWiz.getAllBondedDevices(getApplicationContext()).contains(device)) connectTo(device);
        else {
            try {
                bondStateChangedReceiver = new BondStateChangedReceiver(this);
                registerReceiver(bondStateChangedReceiver, new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));
                setTitle(R.string.pairing);
                createBond(device);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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
                setTitle(R.string.controller_connection_success);
                ((ControllerFragment) getFragmentManager().findFragmentById(R.id.container)).onConnectionSuccess();
            }
        });
    }

    @Override
    public void onConnectionError(Exception e, String s) {
        Log.e("Main", "onConnectionError called");
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
                alert.show();
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
        onBackPressed();
    }

    /*
     * BondStateChangeReceiver
     */
    // called from BondStateChangedReceiver when the the devices are paired after call to createBond
    @Override
    public void triggerBondStateChange() throws Exception{
        if (device != null) {
            // when bond state changes, simply connectTo existing device
            connectTo(device);
        } else {
            throw new Exception();
        }
    }

    /*
     * BtDisconnectedReceiver
     */
    @Override
    public void onBtDisconnected() {
        onBackPressed();
    }
}

package com.rewaveapp;

import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.btwiz.library.BTSocket;
import com.btwiz.library.BTWiz;
import com.btwiz.library.DeviceNotSupportBluetooth;
import com.btwiz.library.IDeviceConnectionListener;

import java.io.IOException;
import java.lang.reflect.Method;


public class MainActivity
        extends
            AppCompatActivity
        implements
            ListerFragment.OnFragmentInteractionListener,
            IDeviceConnectionListener,
            ControllerFragment.OnFragmentInteractionListener,
            BtSwitchOffReceiver.onBtSwitchOffInteractionListener,
            BondStateChangedReceiver.onBondStateChangedInteractionListener {

    private final static int REQUEST_ENABLE_BT = 1;
    private BtSwitchOffReceiver btSwitchOffReceiver;
    private BondStateChangedReceiver bondStateChangedReceiver;
    private BTSocket socket;
    private BluetoothDevice device;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            if (BTWiz.isEnabled(getApplicationContext())) showListerFragment();
            else startSwitchBtOnIntent();

            // see that the user or some other app doesn't switches off bt in the background
            btSwitchOffReceiver = new BtSwitchOffReceiver(this);
            registerReceiver(btSwitchOffReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));

        } catch (DeviceNotSupportBluetooth e) {
            Toast.makeText(getApplicationContext(), "No Bluetooth Available : Rewave needs bluetooth to function",Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(btSwitchOffReceiver);
        if (bondStateChangedReceiver != null) {
            unregisterReceiver(bondStateChangedReceiver);
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
                // TODO : Give a primer as to why we need bt
                startSwitchBtOnIntent();
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
     * Lister Fragment Interactions
     */

    // Helper methods for Lister
    public void connectTo(BluetoothDevice device) {
        setTitle(R.string.connecting);
        BTWiz.connectAsClientAsync(getApplicationContext(), device, this);
    }

    public boolean createBond(BluetoothDevice device)
            throws Exception
    {
        setTitle(R.string.pairing);
        Class deviceClass = Class.forName("android.bluetooth.BluetoothDevice");
        Method createBondMethod = deviceClass.getMethod("createBond");
        return (Boolean) createBondMethod.invoke(device);
    }

    @Override
    public void onDeviceClicked(BluetoothDevice device) {
        Log.e("Main", "Device Clicked " + device.getName());
        this.device = device;
        findViewById(R.id.progress_indicator_central).setVisibility(View.VISIBLE);
        if (BTWiz.getAllBondedDevices(getApplicationContext()).contains(device)) connectTo(device);
        else {
            try {
                bondStateChangedReceiver = new BondStateChangedReceiver(this);
                setTitle(R.string.pairing);
                registerReceiver(bondStateChangedReceiver, new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));
                createBond(device);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /*
     * IDeviceConnectionLister
     */

    @Override
    public void onConnectSuccess(BTSocket btSocket) {
        Log.e("Main", "Connect Success");
        this.socket = btSocket;
        findViewById(R.id.progress_indicator_central).setVisibility(View.GONE);
        setTitle(R.string.controller_connection_success);

        getFragmentManager()
                .beginTransaction()
                .setTransitionStyle(android.R.anim.slide_out_right)
                .replace(R.id.container, ControllerFragment.newInstance())
                .addToBackStack(null)
                .commit()
        ;
    }

    @Override
    public void onConnectionError(Exception e, String s) {
        if (e != null) {
            Log.d("Main", "Connect Error");
            // Todo : here either the server is not running or is not installed. A dialog to fix this.
        }
    }


    /*
     * Control Fragment Interactions
     */

    @Override
    public void sendCommand(String command) {
        try {
            socket.write(command);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     * BtSwitchOffReceiver
     */
    @Override
    public void btSwitchedOff() {

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
}

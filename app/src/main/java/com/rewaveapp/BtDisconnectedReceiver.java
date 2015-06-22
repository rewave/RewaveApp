package com.rewaveapp;

import android.app.Fragment;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/*
 * Called when connection to the device is lost while controlling
 */
public class BtDisconnectedReceiver extends BroadcastReceiver {

    OnBtDisconnectInteractionListener listener;


    public BtDisconnectedReceiver(Context context) {
        listener = (OnBtDisconnectInteractionListener) context;
    }

    public BtDisconnectedReceiver(Fragment fragment) {
        listener = (OnBtDisconnectInteractionListener) fragment;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(intent.getAction())) {
            Toast.makeText(context, "Bt disconnected in background", Toast.LENGTH_LONG).show();
            listener.onBtDisconnected();
        }
    }

    public interface OnBtDisconnectInteractionListener {
        void onBtDisconnected();
    }
}

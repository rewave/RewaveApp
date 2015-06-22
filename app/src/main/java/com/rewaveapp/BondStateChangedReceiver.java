package com.rewaveapp;

import android.app.Fragment;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

class BondStateChangedReceiver extends BroadcastReceiver {

    OnBondStateChangedInteractionListener listener;

    public BondStateChangedReceiver(Context context) {
        listener = (OnBondStateChangedInteractionListener) context;
    }

    public BondStateChangedReceiver(Fragment fragment) {
        listener = (OnBondStateChangedInteractionListener) fragment;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
            try {
                listener.triggerBondStateChange();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public interface OnBondStateChangedInteractionListener {
        void triggerBondStateChange() throws Exception;
    }
}

package com.rewaveapp;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class BtSwitchOffReceiver extends BroadcastReceiver {

    private OnBtSwitchOffInteractionListener listener;

    public BtSwitchOffReceiver(Context context) {
        listener = (OnBtSwitchOffInteractionListener) context;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(intent.getAction()) &&
                intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1) == BluetoothAdapter.STATE_OFF) {
            Toast.makeText(context, "Bt switched off in background", Toast.LENGTH_LONG).show();
            listener.btSwitchedOff();
        }
    }

    public interface OnBtSwitchOffInteractionListener {
        void btSwitchedOff();
    }

}

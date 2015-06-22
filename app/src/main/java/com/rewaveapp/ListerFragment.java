package com.rewaveapp;

import android.app.Activity;
import android.app.Fragment;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.btwiz.library.BTWiz;
import com.btwiz.library.IDeviceLookupListener;

import java.util.ArrayList;
import java.util.List;


<<<<<<< HEAD
public class ListerFragment extends Fragment implements AdapterView.OnItemClickListener, IDeviceLookupListener {
=======
public class ListerFragment extends Fragment implements AdapterView.OnItemClickListener {
>>>>>>> 08f5b6690ec3c5ba13ba42e3b14ccc2357c6c617

    OnFragmentInteractionListener listener;
    ListView devicesListView;
    Menu menu;
    ListerAdapter listerAdapter;
    List<BluetoothDevice> btDevices;

    boolean isDiscovering = false;

    public static ListerFragment newInstance() {
        return new ListerFragment();
    }

<<<<<<< HEAD
    public ListerFragment() {}
=======
    public ListerFragment() {
        // Required empty public constructor
    }
>>>>>>> 08f5b6690ec3c5ba13ba42e3b14ccc2357c6c617

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_lister, container, false);
    }

<<<<<<< HEAD
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
=======
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
>>>>>>> 08f5b6690ec3c5ba13ba42e3b14ccc2357c6c617
        inflater.inflate(R.menu.menu_lister, menu);
        this.menu = menu;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                startDiscovery();
                break;
            case R.id.action_stop_refreshing:
                stopDiscovery();
<<<<<<< HEAD
                break;
=======
>>>>>>> 08f5b6690ec3c5ba13ba42e3b14ccc2357c6c617
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        devicesListView = (ListView) getActivity().findViewById(R.id.devices_list);
        devicesListView.setOnItemClickListener(this);
        btDevices = new ArrayList<>(BTWiz.getAllBondedDevices(getActivity()));
        listerAdapter = new ListerAdapter(getActivity(), btDevices);
        devicesListView.setAdapter(listerAdapter);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
<<<<<<< HEAD
=======
        Log.d("Lister", "OnAttach called");
>>>>>>> 08f5b6690ec3c5ba13ba42e3b14ccc2357c6c617
        try {
            listener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
<<<<<<< HEAD
=======
        Log.d("Lister", "OnDetach called");
>>>>>>> 08f5b6690ec3c5ba13ba42e3b14ccc2357c6c617
        listener = null;
        BTWiz.cleanup(getActivity());
    }

    public void startDiscovery() {
        if (!isDiscovering) {
            isDiscovering = true;
            getActivity().setTitle(R.string.lister_discovering);
            menu.findItem(R.id.action_stop_refreshing).setVisible(true);
            menu.findItem(R.id.action_refresh).setVisible(false);
            menu.findItem(R.id.progress_indicator).setVisible(true);
<<<<<<< HEAD
            BTWiz.startDiscoveryAsync(getActivity(), null, this);
=======

            BTWiz.startDiscoveryAsync(getActivity(), new Runnable() {
                @Override
                public void run() {
                }
            }, new IDeviceLookupListener() {
                @Override
                public boolean onDeviceFound(BluetoothDevice bluetoothDevice, boolean b) {
                    if (!btDevices.contains(bluetoothDevice)) {
                        btDevices.add(bluetoothDevice);
                        listerAdapter.notifyDataSetChanged();
                    }
                    return false;
                }

                @Override
                public void onDeviceNotFound(boolean b) {
                    stopDiscovery();
                }
            });
>>>>>>> 08f5b6690ec3c5ba13ba42e3b14ccc2357c6c617
        }
    }

    public void stopDiscovery() {
        if (isDiscovering) {
            isDiscovering = false;
            BTWiz.stopDiscovery(getActivity());
            getActivity().setTitle(R.string.lister_title);
            menu.findItem(R.id.action_stop_refreshing).setVisible(false);
            menu.findItem(R.id.action_refresh).setVisible(true);
            menu.findItem(R.id.progress_indicator).setVisible(false);
        }
    }

<<<<<<< HEAD
    public interface OnFragmentInteractionListener {
        void onDeviceClicked(BluetoothDevice device);
    }

    /*
     * AdapterView.OnItemClickListener
     */

=======
>>>>>>> 08f5b6690ec3c5ba13ba42e3b14ccc2357c6c617
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        stopDiscovery();
        BluetoothDevice selectedDevice = (BluetoothDevice) listerAdapter.getItem(position);
        listener.onDeviceClicked(selectedDevice);
    }

<<<<<<< HEAD
    /*
     * IDeviceLookupListener
     */

    @Override
    public boolean onDeviceFound(BluetoothDevice bluetoothDevice, boolean b) {
        // TODO : bluetoothDevice.getUuids() and see if its a valid service id. Show differently if valid
        if (!btDevices.contains(bluetoothDevice)) {
            btDevices.add(bluetoothDevice);
            listerAdapter.notifyDataSetChanged();
        }
        return false;
    }

    @Override
    public void onDeviceNotFound(boolean b) {
        stopDiscovery();
    }

=======
    public interface OnFragmentInteractionListener {
        void onDeviceClicked(BluetoothDevice device);
    }
>>>>>>> 08f5b6690ec3c5ba13ba42e3b14ccc2357c6c617
}
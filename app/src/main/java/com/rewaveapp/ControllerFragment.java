package com.rewaveapp;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class ControllerFragment extends Fragment implements SensorEventListener {

    private OnFragmentInteractionListener listener;
    private Menu menu;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private List<List<Double>> motionLog = new ArrayList<>();

    public static ControllerFragment newInstance() {
        return new ControllerFragment();
    }

    public ControllerFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_controller, container, false);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            listener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    public void onDetach() {
        listener = null;
        sensorManager.unregisterListener(this);
        super.onDetach();
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        inflater.inflate(R.menu.menu_controller, menu);
        this.menu = menu;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case (R.id.ping):
                listener.sendCommand("ping");
                break;

            case (R.id.left):
                listener.sendCommand("left");
                break;

            case (R.id.right):
                listener.sendCommand("right");
                break;

            case (R.id.exit):
                getActivity().onBackPressed();
                break;

            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onConnectionSuccess() {
        setHasOptionsMenu(true);
        getActivity().findViewById(R.id.connected_message).setVisibility(View.VISIBLE);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            double ax = event.values[0];
            double ay = event.values[1];
            double sos = (Math.pow(ax, 2.0) + Math.pow(ay, 2.0));

            if (Math.pow(sos, 0.5) >= 0.6) {
                // some relevant motion data is being received so record
                motionLog.add(Arrays.asList(ax, ay));
            } else {

                try {
                    // motion data has been received and recorded, now process
                    if (motionLog.get(0).get(0) > 0 && motionLog.get(motionLog.size() - 1).get(0) < 0) {
                        // maybe left wave
                        Log.e("Controller", "Left wave");
                        listener.sendCommand("right");
                    }

                    if (motionLog.get(0).get(0) < 0 && motionLog.get(motionLog.size() - 1).get(0) > 0) {
                        // maybe right wave
                        Log.e("Controller", "Right wave");
                        listener.sendCommand("left");
                    }

                    motionLog = new ArrayList<>();
                } catch (IndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public interface OnFragmentInteractionListener {
        void sendCommand(String command);
    }
}

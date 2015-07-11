package com.rewaveapp;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class ControllerFragment
        extends
            Fragment
        implements
        GestureDetector.OnGestureListener,
        GestureDetector.OnDoubleTapListener,
        View.OnTouchListener {

    private OnFragmentInteractionListener listener;
    private Menu menu;
    private GestureDetector gestureDetector;
    private TextView waveHandTextView;
    private int screenWidth;
    private int screenHeight;
    private float xHistory;
    private float yHistory;

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
        View v = inflater.inflate(R.layout.fragment_controller, container, false);
        v.setOnTouchListener(this);
        return v;
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
        gestureDetector = new GestureDetector(getActivity(), this);
        gestureDetector.setOnDoubleTapListener(this);
        waveHandTextView = (TextView) getActivity().findViewById(R.id.wave_your_hand);
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screenWidth = size.x;
        screenHeight = size.y;
    }

    @Override
    public void onDetach() {
        listener = null;
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

    public interface OnFragmentInteractionListener {
        void sendCommand(String command);
        void vibrate(int time);
    }

    /**
     * Gesture.Detector.OnGesture
     */

    @Override
    public boolean onDown(MotionEvent e) {
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        Log.e("Controller", "Long Pressed " + String.valueOf(e));
        listener.vibrate(200);
        xHistory = 0.0f;
        yHistory = 0.0f;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return true;
    }

    /**
     * GestureDetector.OnDoubleTap
     */
    @Override
    public boolean onDoubleTap(MotionEvent e) {
        waveHandTextView.setText("Back");
        listener.sendCommand("left");
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        waveHandTextView.setText("Forward");
        listener.sendCommand("right");
        return false;
    }

    /**
     * View.OnTouchListener
     */
    @Override
    public boolean onTouch(View v, MotionEvent e) {
        /*
            M units on phone, C units on computer (M, C are vectors such that M & C = [x_dim, y_dim])
            M = C
            1 = C/M
            x = (C/M)*x;

            we send x/M from here and server will multiply that with C to map touch on phone to mouse motion on computer
        */
        float xCurrent = e.getX() / screenWidth;
        float yCurrent = e.getY() / screenHeight;
        float threshold = Math.abs(xCurrent - xHistory) + Math.abs(yCurrent - yHistory);
        if (xHistory == 0.0f && yHistory == 0.0f) {
            // first time long press
            listener.sendCommand("move_mouse-" + String.valueOf(xCurrent) + "-" + String.valueOf(yCurrent));
        } else {
            // now threshold mouse motion
            if ( Math.abs(xCurrent - xHistory) + Math.abs(yCurrent - yHistory) >= 0.005) {
                listener.sendCommand("move_mouse-" + String.valueOf(xCurrent) + "-" + String.valueOf(yCurrent));
            }
        }

        xHistory = xCurrent;
        yHistory = yCurrent;

        //Log.e("Controller", "LongPressMotion " + String.valueOf(e));

        gestureDetector.onTouchEvent(e);
        return true;
    }
}
